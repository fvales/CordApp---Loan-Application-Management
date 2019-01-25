package com.template.flows

import co.paralleluniverse.fibers.Suspendable
import com.template.contracts.LoanRequestContract
import com.template.states.LoanRequestState
import net.corda.core.contracts.UniqueIdentifier
import net.corda.core.contracts.requireThat
import net.corda.core.flows.*
import net.corda.core.identity.Party
import net.corda.core.transactions.SignedTransaction
import net.corda.core.transactions.TransactionBuilder
import net.corda.core.utilities.ProgressTracker
import net.corda.core.utilities.ProgressTracker.Step

object InitiateLoanFlow {

        @InitiatingFlow
        @StartableByRPC
        class Initiator(private val Bank: Party,
                        private val CustomerName: String,
                        private val LoanAmount: Int): FlowLogic<SignedTransaction>() {
            /**
            * The progress tracker checkpoints each stage of the flow and outputs the specified messages when each
            * checkpoint is reached in the code. See the 'progressTracker.currentStep' expressions within the call() function.
            */
            companion object {
                object GENERATING_TRANSACTION : Step("Generating transaction based on new LoanRequestState.")
                object VERIFYING_TRANSACTION : Step("Verifying contract constraints.")
                object SIGNING_TRANSACTION : Step("Signing transaction with our private key.")
                object GATHERING_SIGS : Step("Gathering the counterparty's signature.") {
                    override fun childProgressTracker() = CollectSignaturesFlow.tracker()
                }

                object FINALISING_TRANSACTION : Step("Obtaining notary signature and recording transaction.") {
                    override fun childProgressTracker() = FinalityFlow.tracker()
                }

                fun tracker() = ProgressTracker(
                        GENERATING_TRANSACTION,
                        VERIFYING_TRANSACTION,
                        SIGNING_TRANSACTION,
                        GATHERING_SIGS,
                        FINALISING_TRANSACTION
                )
            }
            override val progressTracker: ProgressTracker? = ProgressTracker()

            @Suspendable
            @Throws(FlowException::class)
            override fun call(): SignedTransaction {
                // Get the notary
                println("Get the notary")
                val notary = serviceHub.networkMapCache.notaryIdentities.first()
                // progressTracker!!.currentStep = GENERATING_TRANSACTION
                // Create the output state
                println("Create the output state")
                val outputState = LoanRequestState(LoanAmount, CustomerName, Bank, ourIdentity, false, UniqueIdentifier())

                // Building the transaction
                println("Building the transaction")
                val transactionBuilder = TransactionBuilder(notary).
                        addOutputState(outputState, LoanRequestContract.LOANREQUEST_CONTRACT_ID).
                        addCommand(LoanRequestContract.Commands.InitiateLoan(), ourIdentity.owningKey, Bank.owningKey)

                // Verify transaction Builder
                println("Verify transaction Builder")
                // progressTracker.currentStep = VERIFYING_TRANSACTION
                transactionBuilder.verify(serviceHub)

                // Sign the transaction
                println("Sign the transaction")
                // progressTracker.currentStep = GATHERING_SIGS
                // progressTracker.currentStep = SIGNING_TRANSACTION
                val partSignedTx = serviceHub.signInitialTransaction(transactionBuilder)

                // Send the state to the counterparty, and receive it back with their signature.
                println("Send the state to the counterparty, and receive it back with their signature.")
                val otherPartySession = initiateFlow(Bank)
                val fullySignedTx = subFlow(CollectSignaturesFlow(partSignedTx, setOf(otherPartySession)))

                // Notarise and record the transaction in both parties' vaults.
                println("Notarise and record the transaction in both parties' vaults.")
                // progressTracker.currentStep = FINALISING_TRANSACTION
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
                        "This must be an LoanRequestState." using (output is LoanRequestState)
                        val loanRequestState = output as LoanRequestState
                        "Loan amount should be positive" using (loanRequestState.LoanAmount > 0)
                    }
                }
//                val txId = subFlow(signTransactionFlow).id
//
//                return subFlow(ReceiveFinalityFlow(otherPartyFlow, expectedTxId = txId))
                return subFlow(signTransactionFlow)
            }

        }
    }
