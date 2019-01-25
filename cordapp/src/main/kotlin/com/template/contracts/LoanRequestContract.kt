package com.template.contracts

import com.template.states.LoanRequestState
import net.corda.core.contracts.*
import net.corda.core.transactions.LedgerTransaction


class LoanRequestContract: Contract {

    companion object {
        val LOANREQUEST_CONTRACT_ID = "com.template.contracts.LoanRequestContract"
    }

    interface Commands : CommandData {
        class InitiateLoan : Commands, TypeOnlyCommandData()
        class LoanResponse : Commands, TypeOnlyCommandData()
    }

    override fun verify(tx: LedgerTransaction) {
        val command = tx.commands.requireSingleCommand<Commands>()

        when (command.value){
            is Commands.InitiateLoan -> verifyInitiateLoan(tx, command)
            is Commands.LoanResponse -> verifyLoanResponse(tx, command)
        }
    }

    private fun verifyInitiateLoan(tx: LedgerTransaction, command: CommandWithParties<Commands>) {
        requireThat {
            "Transaction should have zero inputs" using (tx.inputs.isEmpty())
            "Transaction should have one output" using (tx.outputs.size == 1)

            val outputState = tx.outputStates[0]
            "Output must be a LoanRequestState" using (outputState is LoanRequestState)

            val loanRequestState = outputState as LoanRequestState

            "The Loan amount should be positive" using (loanRequestState.LoanAmount > 0)

            "Finance agency should sign the transaction" using (command.signers.contains(outputState.FinanceAgency.owningKey))
            "Bank should sign the transaction" using (command.signers.contains(outputState.Bank.owningKey))
        }
    }

    private fun verifyLoanResponse(tx: LedgerTransaction, command: CommandWithParties<Commands>) {
        requireThat {
            "Transaction should have one inputs" using (tx.inputs.size == 1)
            "Transaction should have one output" using (tx.outputs.size == 1)

            val input = tx.inputStates[0]
            val output = tx.outputStates[0]

            "Input must be a LoanRequestState" using (input is LoanRequestState)
            "Output must be a LoanRequestState" using (output is LoanRequestState)

            val inputState = input as LoanRequestState
            val outputState = output as LoanRequestState

            "Finance agency should sign the transaction" using (command.signers.contains(inputState.FinanceAgency.owningKey))
            "Bank should sign the transaction" using (command.signers.contains(outputState.Bank.owningKey))
        }
    }
}