package com.template.contracts

import com.template.states.LoanRequestState
import net.corda.core.contracts.Contract
import net.corda.core.contracts.UniqueIdentifier
import net.corda.core.identity.CordaX500Name
import net.corda.testing.contracts.DummyState
import net.corda.testing.core.DummyCommandData
import net.corda.testing.core.TestIdentity
import net.corda.testing.node.MockServices
import net.corda.testing.node.ledger
import org.junit.Test

class LoanRequestContractTest {
    val Bank = TestIdentity(CordaX500Name("Bank", "", "GB")).party
    val FinanceAgency = TestIdentity(CordaX500Name("FinanceAgency", "", "GB")).party
    private val ledgerServices = MockServices(TestIdentity(CordaX500Name("TestId", "", "GB")))
    private val loanRequestState = LoanRequestState(100,
            "Freeda",
            Bank,
            FinanceAgency,
            false,
            UniqueIdentifier())

    @Test
    fun loanRequestContractImplementsContract() {
        assert((LoanRequestContract() is Contract))
    }
    @Test
    fun loanRequestContractRequiresZeroInputsInTheTransaction() {
        ledgerServices.ledger {
            transaction {
                // Has an input, will fail.
                input(LoanRequestContract.LOANREQUEST_CONTRACT_ID, loanRequestState)
                output(LoanRequestContract.LOANREQUEST_CONTRACT_ID, loanRequestState)
                command(listOf(FinanceAgency.owningKey,Bank.owningKey), LoanRequestContract.Commands.InitiateLoan())
                fails()
            }
            transaction{
                // Has no input, will verify.
                output(LoanRequestContract.LOANREQUEST_CONTRACT_ID, loanRequestState)
                command(listOf(FinanceAgency.owningKey,Bank.owningKey), LoanRequestContract.Commands.InitiateLoan())
                verifies()
            }
        }
    }
    @Test
    fun loanRequestContractRequiresOneOutputInTheTransaction() {
        ledgerServices.ledger {
            transaction {
                // Has two outputs, will fail.
                output(LoanRequestContract.LOANREQUEST_CONTRACT_ID, loanRequestState)
                output(LoanRequestContract.LOANREQUEST_CONTRACT_ID, loanRequestState)
                command(listOf(FinanceAgency.owningKey,Bank.owningKey), LoanRequestContract.Commands.InitiateLoan())
                fails()
            }
            transaction {
                // Has one output, will verify.
                output(LoanRequestContract.LOANREQUEST_CONTRACT_ID, loanRequestState)
                command(listOf(FinanceAgency.owningKey,Bank.owningKey), LoanRequestContract.Commands.InitiateLoan())
                verifies()
            }
        }
    }
    @Test
    fun loanRequestContractRequiresOneCommandInTheTransaction() {

        ledgerServices.ledger {
            transaction {
                output(LoanRequestContract.LOANREQUEST_CONTRACT_ID, loanRequestState)
                // Has two commands, will fail.
                command(listOf(FinanceAgency.owningKey,Bank.owningKey), LoanRequestContract.Commands.InitiateLoan())
                command(listOf(FinanceAgency.owningKey,Bank.owningKey), LoanRequestContract.Commands.InitiateLoan())
                fails()
            }
            transaction {
                output(LoanRequestContract.LOANREQUEST_CONTRACT_ID, loanRequestState)
                // Has one command, will verify.
                command(listOf(FinanceAgency.owningKey,Bank.owningKey) ,LoanRequestContract.Commands.InitiateLoan())
               // command(Bank.owningKey, LoanRequestContract.Commands.InitiateLoan())
                verifies()
            }
        }
    }
    @Test
    fun loanRequestContractRequiresTheTransactionsOutputToBeATokenState() {
        ledgerServices.ledger {
            transaction {
                // Has wrong output type, will fail.
                output(LoanRequestContract.LOANREQUEST_CONTRACT_ID, DummyState())
                command(listOf(FinanceAgency.owningKey,Bank.owningKey), LoanRequestContract.Commands.InitiateLoan())
                fails()
            }
            transaction {
                // Has correct output type, will verify.
                output(LoanRequestContract.LOANREQUEST_CONTRACT_ID, loanRequestState)
                command(listOf(FinanceAgency.owningKey,Bank.owningKey), LoanRequestContract.Commands.InitiateLoan())
                verifies()
            }
        }
    }
    @Test
    fun loanRequestContractRequiresTheTransactionsOutputToHaveAPositiveLoanAmount() {
        val zeroLoanRequestState = LoanRequestState(0,
                "Freeda",
                Bank,
                FinanceAgency,
                false,
                UniqueIdentifier())
        val negativeLoanRequestState = LoanRequestState(-1,
                "Freeda",
                Bank,
                FinanceAgency,
                false,
                UniqueIdentifier())
        val positiveLoanRequestState = LoanRequestState(100,
                "Freeda",
                Bank,
                FinanceAgency,
                false,
                UniqueIdentifier())
        ledgerServices.ledger {
            transaction {
                // Has zero-amount TokenState, will fail.
                output(LoanRequestContract.LOANREQUEST_CONTRACT_ID, zeroLoanRequestState)
                command(listOf(FinanceAgency.owningKey,Bank.owningKey), LoanRequestContract.Commands.InitiateLoan())
                fails()
            }
            transaction {
                // Has negative-amount TokenState, will fail.
                output(LoanRequestContract.LOANREQUEST_CONTRACT_ID, negativeLoanRequestState)
                command(listOf(FinanceAgency.owningKey,Bank.owningKey), LoanRequestContract.Commands.InitiateLoan())
                fails()
            }
            transaction {
                // Has positive-amount TokenState, will verify.
                output(LoanRequestContract.LOANREQUEST_CONTRACT_ID, loanRequestState)
                command(listOf(FinanceAgency.owningKey,Bank.owningKey), LoanRequestContract.Commands.InitiateLoan())
                verifies()
            }
            transaction {
                // Also has positive-amount TokenState, will verify.
                output(LoanRequestContract.LOANREQUEST_CONTRACT_ID, positiveLoanRequestState)
                command(listOf(FinanceAgency.owningKey,Bank.owningKey), LoanRequestContract.Commands.InitiateLoan())
                verifies()
            }
        }
    }
    @Test
    fun loanRequestContractRequiresTheTransactionsCommandToBeAnIssueCommand() {
        ledgerServices.ledger {
            transaction {
                // Has wrong command type, will fail.
                output(LoanRequestContract.LOANREQUEST_CONTRACT_ID, loanRequestState)
                command(listOf(FinanceAgency.owningKey,Bank.owningKey), DummyCommandData)
                fails()
            }
            transaction {
                // Has correct command type, will verify.
                output(LoanRequestContract.LOANREQUEST_CONTRACT_ID, loanRequestState)
                command(listOf(FinanceAgency.owningKey,Bank.owningKey), LoanRequestContract.Commands.InitiateLoan())
                verifies()

            }
        }
    }
    @Test
    fun loanRequestContractRequiresTheIssuerToBeARequiredSignerInTheTransaction() {
        val loanRequestStateWhereFAInitiatesLoan = LoanRequestState(100,
                "Freeda",
                Bank,
                FinanceAgency,
                false,
                UniqueIdentifier())
        ledgerServices.ledger {
            transaction {
                // Issuer is not a required signer, will fail.
                output(LoanRequestContract.LOANREQUEST_CONTRACT_ID, loanRequestState)
                command(listOf(FinanceAgency.owningKey,Bank.owningKey), LoanRequestContract.Commands.InitiateLoan())
                fails()
            }
            transaction {
                // Issuer is also not a required signer, will fail.
                output(LoanRequestContract.LOANREQUEST_CONTRACT_ID, loanRequestStateWhereFAInitiatesLoan)
                command(listOf(FinanceAgency.owningKey,Bank.owningKey), LoanRequestContract.Commands.InitiateLoan())
                fails()
            }
            transaction {
                // Issuer is a required signer, will verify.
                output(LoanRequestContract.LOANREQUEST_CONTRACT_ID, loanRequestState)
                command(listOf(FinanceAgency.owningKey,Bank.owningKey), LoanRequestContract.Commands.InitiateLoan())
                verifies()
            }
            transaction {
                // FinanceAgency is also a required signer, will verify.
                output(LoanRequestContract.LOANREQUEST_CONTRACT_ID, loanRequestStateWhereFAInitiatesLoan)
                command(listOf(FinanceAgency.owningKey,Bank.owningKey), LoanRequestContract.Commands.InitiateLoan())
                verifies()
            }
        }
    }
}