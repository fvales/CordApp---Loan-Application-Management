package com.template.flows

import co.paralleluniverse.fibers.Suspendable
import com.template.contracts.LoanRequestContract
import com.template.contracts.LoanVerificationContract
import com.template.states.LoanRequestState
import com.template.states.LoanVerificationState
import net.corda.core.contracts.UniqueIdentifier
import net.corda.core.contracts.requireThat
import net.corda.core.flows.*
import net.corda.core.identity.Party
import net.corda.core.node.services.Vault
import net.corda.core.node.services.queryBy
import net.corda.core.node.services.vault.QueryCriteria
import net.corda.core.transactions.SignedTransaction
import net.corda.core.transactions.TransactionBuilder
import net.corda.core.utilities.ProgressTracker

object RequestCRAFlow {

    @InitiatingFlow
    @StartableByRPC
    class Initiator(private val CreditRatingAgency: Party,
                    private val linearIdentifier: UniqueIdentifier): FlowLogic<SignedTransaction>() {

        override val progressTracker: ProgressTracker? = ProgressTracker()

        @Suspendable
        @Throws(FlowException::class)
        override fun call(): SignedTransaction {
            // Get the notary
            val notary = serviceHub.networkMapCache.notaryIdentities.first()

            // Build the transaction
            // 1. Query LoanRequestState by linearId
            val vaultQueryCriteria = QueryCriteria.LinearStateQueryCriteria(listOf(ourIdentity), listOf(linearIdentifier), Vault.StateStatus.UNCONSUMED, null)
            val inputState = serviceHub.vaultService.queryBy<LoanRequestState>(vaultQueryCriteria).states.first()

            /*** Getting the amount, companyName and loan-eligibility from the vault of Previous State  */
            val loanAmount = inputState.state.data.LoanAmount
            val customerName = inputState.state.data.CustomerName
            val isEligibleForLoan = false

            // Create the output state
            val outputState = LoanVerificationState(loanAmount, customerName, ourIdentity, CreditRatingAgency, isEligibleForLoan, linearIdentifier)

            // Building the transaction
            val transactionBuilder = TransactionBuilder(notary).
                    addOutputState(outputState, LoanVerificationContract.LOANVERIFICATION_CONTRACT_ID).
                    addCommand(LoanVerificationContract.Commands.SendForApproval(), ourIdentity.owningKey)

            // Verify transaction Builder
            transactionBuilder.verify(serviceHub)

            // Sign the transaction
            val partSignedTx = serviceHub.signInitialTransaction(transactionBuilder)

            // Send the state to the counterparty, and receive it back with their signature.
            val otherPartySession = initiateFlow(CreditRatingAgency)
            val fullySignedTx = subFlow(CollectSignaturesFlow(partSignedTx, setOf(otherPartySession)))

            // Notarise and record the transaction in both parties' vaults.
            return subFlow(FinalityFlow(fullySignedTx))
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