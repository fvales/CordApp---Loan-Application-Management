package com.template.flows

import co.paralleluniverse.fibers.Suspendable
import com.template.contracts.LoanRequestContract
import com.template.states.LoanRequestState
import com.template.states.LoanVerificationState
import net.corda.core.contracts.UniqueIdentifier
import net.corda.core.contracts.requireThat
import net.corda.core.flows.*
import net.corda.core.identity.Party
import net.corda.core.node.services.Vault
import net.corda.core.node.services.vault.QueryCriteria
import net.corda.core.transactions.SignedTransaction
import net.corda.core.transactions.TransactionBuilder
import net.corda.core.utilities.ProgressTracker
import net.corda.core.node.services.queryBy


object LoanResponseFlow {

    @InitiatingFlow
    @StartableByRPC
    class Initiator(private val FinanceAgency: Party,
                    private val linearIdentifier: UniqueIdentifier): FlowLogic<SignedTransaction>() {

        override val progressTracker: ProgressTracker? = ProgressTracker()

        @Suspendable
        @Throws(FlowException::class)
        override fun call(): SignedTransaction {

            // Get the notary
            println("Get the notary")
            val notary = serviceHub.networkMapCache.notaryIdentities.first()

            // Build the transaction
            // 1. Query LoanRequestState by linearId
            println("Query LoanRequestState by linearId")
            val vaultQueryCriteria = QueryCriteria.LinearStateQueryCriteria(listOf(ourIdentity), listOf(linearIdentifier), Vault.StateStatus.UNCONSUMED, null)
            val inputState = serviceHub.vaultService.queryBy<LoanRequestState>(vaultQueryCriteria).states.first()

            // 2. Create new LoanRequest state
            println("Create new LoanRequest state")
            val linearIdLoanReqDataState = inputState.state.data.linearId
            val isEligibleForLoan = inputState.state.data.isEligibleForLoan
            val amount = inputState.state.data.LoanAmount
            val customerName = inputState.state.data.CustomerName

            val outputState = LoanRequestState(amount, customerName, ourIdentity, FinanceAgency, isEligibleForLoan, linearIdLoanReqDataState)

            // 3. Add command, signers as Bank and FinanceAgency
            println("Add command, signers as Bank and FinanceAgency")
            val transactionBuilder = TransactionBuilder(notary)
                    .addInputState(inputState)
                    .addOutputState(outputState, LoanRequestContract.LOANREQUEST_CONTRACT_ID)
                    .addCommand(LoanRequestContract.Commands.LoanResponse(), ourIdentity.owningKey, outputState.FinanceAgency.owningKey)

            // Verify the transaction builder
            println("Verify the transaction builder")
            transactionBuilder.verify(serviceHub)

            // Sign the transaction
            println("Sign the transaction")
            val partiallySignedTransaction = serviceHub.signInitialTransaction(transactionBuilder)

            // Send transaction to the seller node for signing
            println("Send transaction to the seller node for signing")
            val otherPartySession = initiateFlow(outputState.FinanceAgency)
            val completelySignedTransaction = subFlow(CollectSignaturesFlow(partiallySignedTransaction, listOf(otherPartySession)))

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
                    "This must be an LoanRequestState." using (output is LoanRequestState)
                }
            }
            return subFlow(signTransactionFlow)
        }
    }
}
