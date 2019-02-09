package com.template.flows

import co.paralleluniverse.fibers.Suspendable
import com.template.contracts.LoanVerificationContract
import com.template.states.LoanVerificationState
import net.corda.core.contracts.Command
import net.corda.core.contracts.UniqueIdentifier
import net.corda.core.contracts.requireThat
import net.corda.core.flows.*
import net.corda.core.identity.CordaX500Name
import net.corda.core.identity.Party
import net.corda.core.node.services.Vault
import net.corda.core.node.services.vault.QueryCriteria
import net.corda.core.transactions.SignedTransaction
import net.corda.core.transactions.TransactionBuilder
import net.corda.core.utilities.ProgressTracker
import net.corda.core.node.services.queryBy
import java.util.function.Predicate

object CRAResponse  {
    @InitiatingFlow
    @StartableByRPC
    class Initiator(private val Bank: Party,
                    private val linearIdentifier: UniqueIdentifier): FlowLogic<SignedTransaction>() {

        override val progressTracker: ProgressTracker? = ProgressTracker()
        internal val values = arrayOf("JETSAIRWAYS", "AMERICONAIRWAYS", "SAHARAAIRLINES", "JETBLUEAIRLINE")

        @Suspendable
        @Throws(FlowException::class)
        override fun call(): SignedTransaction {

            // Get the notary
            println("Get the notary")
            val notary = serviceHub.networkMapCache.notaryIdentities.first()

            // Build the transaction
            // 1. Query LoanVerificationState by linearId
            println("Query LoanVerificationState by linearId")
            val vaultQueryCriteria = QueryCriteria.LinearStateQueryCriteria(listOf(ourIdentity), listOf(linearIdentifier), Vault.StateStatus.UNCONSUMED, null)
            val inputState = serviceHub.vaultService.queryBy<LoanVerificationState>(vaultQueryCriteria).states.first()

            // 2. Create new LoanRequest state
            println("Create new LoanRequest state")
            val linearIdLoanReqDataState = inputState.state.data.linearId
            val amount = inputState.state.data.LoanAmount
            val customerName = inputState.state.data.CustomerName
            println(linearIdLoanReqDataState)
            println(amount)
            println(customerName)

            // val contains = values.contains(customerName)


            //get cibil rating from oracle
            val oracleName = CordaX500Name("Oracle", "New York","US")
            val oracle = serviceHub.networkMapCache.getNodeByLegalName(oracleName)?.legalIdentities?.first()
                    ?: throw IllegalArgumentException("Requested oracle $oracleName not found on network.")

            val cibilRating = subFlow(CRAQueryFlow(oracle,customerName))

            println(cibilRating)

            var loanEligibility = false
            if (cibilRating > 600){
                loanEligibility = true
            }

            val outputState = LoanVerificationState(amount, customerName, Bank, ourIdentity,loanEligibility, linearIdLoanReqDataState)

//            /** Setting the loanEligibility flag in the state's vault  */
//            println("Setting the loanEligibility flag in the state's vault")
//            if (contains) {
//                outputState = LoanVerificationState(amount, customerName, Bank, ourIdentity,loanEligibility, linearIdLoanReqDataState)
//            } else {
//                outputState = LoanVerificationState(amount, customerName, Bank, ourIdentity, loanEligibility, linearIdLoanReqDataState)
//            }
//            println(contains)


            // 3. Add command, signers as Bank and CRA
            println("Add command, signers as Bank and CRA")
            val transactionBuilder = TransactionBuilder(notary)
                    .addInputState(inputState)
                    .addOutputState(outputState, LoanVerificationContract.LOANVERIFICATION_CONTRACT_ID)
                    .addCommand(LoanVerificationContract.Commands.GenerateRating(customerName, cibilRating), listOf(oracle.owningKey, ourIdentity.owningKey))

            // Verify the transaction builder
            println("Verify the transaction builder")
            transactionBuilder.verify(serviceHub)

            // Sign the transaction
            println("Sign the transaction")
            val partiallySignedTransaction = serviceHub.signInitialTransaction(transactionBuilder)


            // Oracle Sign the transaction
            println("Oracle Sign the transaction")
            val ftx = partiallySignedTransaction.buildFilteredTransaction(Predicate {
                when (it) {
                    is Command<*> -> oracle.owningKey in it.signers && it.value is LoanVerificationContract.Commands.GenerateRating
                    else -> false
                }
            })

            val oracleSignature = subFlow(CRASignFlow(oracle, ftx))
            val stx = partiallySignedTransaction.withAdditionalSignature(oracleSignature)


            // Send transaction to the seller node for signing
            println("Send transaction to the seller node for signing")
            val otherPartySession = initiateFlow(outputState.Bank)
            val completelySignedTransaction = subFlow(CollectSignaturesFlow(stx, listOf(otherPartySession)))

            // Notarize and commit
            println("Notarize and commit")
            return subFlow(FinalityFlow(completelySignedTransaction))
        }
    }

    @InitiatedBy(Initiator::class)
    class Acceptor(val otherPartyFlow: FlowSession) : FlowLogic<SignedTransaction>() {
        @Suspendable
        @Throws(FlowException::class)
        override fun call(): SignedTransaction {
            val signTransactionFlow = object : SignTransactionFlow(otherPartyFlow) {
                override fun checkTransaction(stx: SignedTransaction) = requireThat {
                    val output = stx.tx.outputs.single().data
                    "This must be an LoanVerificationState." using (output is LoanVerificationState)
                }
            }
            return subFlow(signTransactionFlow)
        }
    }
}