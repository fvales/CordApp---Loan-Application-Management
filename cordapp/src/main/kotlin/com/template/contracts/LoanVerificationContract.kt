package com.template.contracts

import com.template.states.LoanRequestState
import com.template.states.LoanVerificationState
import net.corda.core.contracts.*
import net.corda.core.transactions.LedgerTransaction


class LoanVerificationContract: Contract {

    companion object {
        val LOANVERIFICATION_CONTRACT_ID = "com.template.contracts.LoanVerificationContract"
    }

    interface Commands : CommandData {
        class SendForApproval : Commands, TypeOnlyCommandData()
        class ApprovalResponse : Commands, TypeOnlyCommandData()
    }

    override fun verify(tx: LedgerTransaction) {
        val command = tx.commands.requireSingleCommand<LoanVerificationContract.Commands>()
        when (command.value){
            is Commands.SendForApproval -> verifySendForApproval(tx, command)
            is Commands.ApprovalResponse -> verifyApprovalResponse(tx, command)
        }
    }

    private fun verifySendForApproval(tx: LedgerTransaction, command: CommandWithParties<LoanVerificationContract.Commands>) {
        requireThat {
            "Transaction should have zero inputs" using (tx.inputs.isEmpty())
            "Transaction should have one output" using (tx.outputs.size == 1)

            val outputState = tx.outputStates[0]
            "Output must be a LoanVerificationState" using (outputState is LoanVerificationState)

            val loanVerificationState = tx.outputStates[0] as LoanVerificationState

            "The Loan amount should be positive" using (loanVerificationState.LoanAmount > 0)

            "Bank should sign the transaction" using (command.signers.contains(loanVerificationState.Bank.owningKey))
            "Credit Rating Agency should sign the transaction" using (command.signers.contains(loanVerificationState.CreditRatingAgency.owningKey))
        }
    }

    private fun verifyApprovalResponse(tx: LedgerTransaction, command: CommandWithParties<LoanVerificationContract.Commands>) {
        requireThat {

            "Transaction should have one inputs" using (tx.inputs.size == 1)
            "Transaction should have one output" using (tx.outputs.size == 1)

            val input = tx.inputStates[0]
            val output = tx.outputStates[0]

            "input should only be of type LoanVerificationState " using (input is LoanVerificationState)
            "output shoud be of the type LoanVerificationState" using (output is LoanVerificationState)

            val inputState = input as LoanVerificationState
            val outputState = output as LoanVerificationState

            "bank must sign the transaction" using (command.signers.contains(inputState.CreditRatingAgency.owningKey))
            "creditAgency must sign the transaction" using (command.signers.contains(outputState.Bank.owningKey))
            null
        }
    }
}