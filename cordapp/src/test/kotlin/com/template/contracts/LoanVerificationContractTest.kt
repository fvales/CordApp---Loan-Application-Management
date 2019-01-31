package com.template.contracts

import com.template.states.LoanRequestState
import com.template.states.LoanVerificationState
import net.corda.core.contracts.Contract
import net.corda.core.contracts.UniqueIdentifier
import net.corda.core.identity.CordaX500Name
import net.corda.testing.contracts.DummyState
import net.corda.testing.core.DummyCommandData
import net.corda.testing.core.TestIdentity
import net.corda.testing.node.MockServices
import net.corda.testing.node.ledger
import org.junit.Test

class LoanVerificationContractTest {
    val Bank = TestIdentity(CordaX500Name("Bank", "", "GB")).party
    val CreditRatingAgency = TestIdentity(CordaX500Name("CreditRatingAgency", "", "GB")).party
    private val ledgerServices = MockServices(TestIdentity(CordaX500Name("TestId", "", "GB")))
    private val loanVerificationState = LoanVerificationState(100,
            "Freeda",
            Bank,
            CreditRatingAgency,
            false,
            UniqueIdentifier())

    @Test
    fun loanVerificationContractImplementsContract() {
        assert((LoanVerificationContract() is Contract))
    }
    @Test
    fun loanVerificationContractSendForApprovalRequiresZeroInputInTheTransaction() {
        ledgerServices.ledger {
            transaction {
                // Has zero input, will verify.
                output(LoanVerificationContract.LOANVERIFICATION_CONTRACT_ID, loanVerificationState)
                command(listOf(Bank.owningKey,CreditRatingAgency.owningKey), LoanVerificationContract.Commands.SendForApproval())
                verifies()
            }
            transaction{
                // Has one input, will fail.
                input(LoanVerificationContract.LOANVERIFICATION_CONTRACT_ID, loanVerificationState)
                output(LoanVerificationContract.LOANVERIFICATION_CONTRACT_ID, loanVerificationState)
                command(listOf(Bank.owningKey,CreditRatingAgency.owningKey), LoanVerificationContract.Commands.SendForApproval())
                fails()
            }
        }
    }
    @Test
    fun loanVerificationContractApprovalResponseRequiresOneInputInTheTransaction() {
        ledgerServices.ledger {
            transaction {
                // Has zero input, will Fail.
                output(LoanVerificationContract.LOANVERIFICATION_CONTRACT_ID, loanVerificationState)
                command(listOf(Bank.owningKey,CreditRatingAgency.owningKey), LoanVerificationContract.Commands.ApprovalResponse())
                fails()
            }
            transaction{
                // Has one input, will verify.
                input(LoanVerificationContract.LOANVERIFICATION_CONTRACT_ID, loanVerificationState)
                output(LoanVerificationContract.LOANVERIFICATION_CONTRACT_ID, loanVerificationState)
                command(listOf(Bank.owningKey,CreditRatingAgency.owningKey), LoanVerificationContract.Commands.ApprovalResponse())
                verifies()
            }
        }
    }
    @Test
    fun loanVerifyContractSendForApprovalRequiresOneOutputInTheTransaction() {
        ledgerServices.ledger {
            transaction {
                // Has two outputs, will fail.
                output(LoanVerificationContract.LOANVERIFICATION_CONTRACT_ID, loanVerificationState)
                output(LoanVerificationContract.LOANVERIFICATION_CONTRACT_ID, loanVerificationState)
                command(listOf(Bank.owningKey,CreditRatingAgency.owningKey), LoanVerificationContract.Commands.SendForApproval())
                fails()
            }
            transaction {
                // Has one output, will verify.
                output(LoanVerificationContract.LOANVERIFICATION_CONTRACT_ID, loanVerificationState)
                command(listOf(Bank.owningKey,CreditRatingAgency.owningKey), LoanVerificationContract.Commands.SendForApproval())
                verifies()
            }
        }
    }
    @Test
    fun loanVerifyContractApprovalRequestRequiresOneOutputInTheTransaction() {
        ledgerServices.ledger {
            transaction {
                input(LoanVerificationContract.LOANVERIFICATION_CONTRACT_ID, loanVerificationState)
                // Has two outputs, will fail.
                output(LoanVerificationContract.LOANVERIFICATION_CONTRACT_ID, loanVerificationState)
                output(LoanVerificationContract.LOANVERIFICATION_CONTRACT_ID, loanVerificationState)
                command(listOf(Bank.owningKey,CreditRatingAgency.owningKey), LoanVerificationContract.Commands.ApprovalResponse())
                fails()
            }
            transaction {
                input(LoanVerificationContract.LOANVERIFICATION_CONTRACT_ID, loanVerificationState)
                // Has one output, will verify.
                output(LoanVerificationContract.LOANVERIFICATION_CONTRACT_ID, loanVerificationState)
                command(listOf(Bank.owningKey,CreditRatingAgency.owningKey), LoanVerificationContract.Commands.ApprovalResponse())
                verifies()
            }
        }
    }
    @Test
    fun loanVerificationContractRequiresOneCommandInTheTransaction() {

        ledgerServices.ledger {
            transaction {
                output(LoanVerificationContract.LOANVERIFICATION_CONTRACT_ID, loanVerificationState)
                // Has two commands, will fail.
                command(listOf(Bank.owningKey,CreditRatingAgency.owningKey), LoanVerificationContract.Commands.SendForApproval())
                command(listOf(Bank.owningKey,CreditRatingAgency.owningKey), LoanVerificationContract.Commands.SendForApproval())
                fails()
            }
            transaction {
                output(LoanVerificationContract.LOANVERIFICATION_CONTRACT_ID, loanVerificationState)
                // Has one command, will verify.
                command(listOf(Bank.owningKey,CreditRatingAgency.owningKey) ,LoanVerificationContract.Commands.SendForApproval())
                verifies()
            }
        }
    }
    @Test
    fun loanVerificationContractSendForApprovalRequiresTheTransactionsOutputToBeLoanVeriicationState() {
        ledgerServices.ledger {
            transaction {
                // Has wrong output type, will fail.
                output(LoanVerificationContract.LOANVERIFICATION_CONTRACT_ID, DummyState())
                command(listOf(Bank.owningKey,CreditRatingAgency.owningKey), LoanVerificationContract.Commands.SendForApproval())
                fails()
            }
            transaction {
                // Has correct output type, will verify.
                output(LoanVerificationContract.LOANVERIFICATION_CONTRACT_ID, loanVerificationState)
                command(listOf(Bank.owningKey,CreditRatingAgency.owningKey), LoanVerificationContract.Commands.SendForApproval())
                verifies()
            }
        }
    }
    @Test
    fun loanVerificationContractApprovalRequestRequiresTheTransactionsOutputToBeLoanVeriicationState() {
        ledgerServices.ledger {
            transaction {
                input(LoanVerificationContract.LOANVERIFICATION_CONTRACT_ID, loanVerificationState)
                // Has wrong output type, will fail.
                output(LoanVerificationContract.LOANVERIFICATION_CONTRACT_ID, DummyState())
                command(listOf(Bank.owningKey,CreditRatingAgency.owningKey), LoanVerificationContract.Commands.ApprovalResponse())
                fails()
            }
            transaction {
                input(LoanVerificationContract.LOANVERIFICATION_CONTRACT_ID, loanVerificationState)
                // Has correct output type, will verify.
                output(LoanVerificationContract.LOANVERIFICATION_CONTRACT_ID, loanVerificationState)
                command(listOf(Bank.owningKey,CreditRatingAgency.owningKey), LoanVerificationContract.Commands.ApprovalResponse())
                verifies()
            }
        }
    }
    @Test
    fun loanVerificationContractRequiresTheTransactionsCommandToBeAnSendForApprovalCommand() {
        ledgerServices.ledger {
            transaction {
                // Has wrong command type, will fail.
                output(LoanVerificationContract.LOANVERIFICATION_CONTRACT_ID, loanVerificationState)
                command(listOf(Bank.owningKey,CreditRatingAgency.owningKey), DummyCommandData)
                fails()
            }
            transaction {
                // Has correct command type, will verify.
                output(LoanVerificationContract.LOANVERIFICATION_CONTRACT_ID, loanVerificationState)
                command(listOf(Bank.owningKey,CreditRatingAgency.owningKey), LoanVerificationContract.Commands.SendForApproval())
                verifies()

            }
        }
    }
//    @Test
//    fun loanRequestContractRequiresTheIssuerToBeARequiredSignerInTheTransaction() {
//        val loanRequestStateWhereFAInitiatesLoan = LoanRequestState(100,
//                "Freeda",
//                Bank,
//                FinanceAgency,
//                false,
//                UniqueIdentifier())
//        ledgerServices.ledger {
//            transaction {
//                // Issuer is not a required signer, will fail.
//                output(LoanRequestContract.LOANREQUEST_CONTRACT_ID, loanRequestState)
//                command(listOf(FinanceAgency.owningKey,Bank.owningKey), LoanRequestContract.Commands.InitiateLoan())
//                fails()
//            }
//            transaction {
//                // Issuer is also not a required signer, will fail.
//                output(LoanRequestContract.LOANREQUEST_CONTRACT_ID, loanRequestStateWhereFAInitiatesLoan)
//                command(listOf(FinanceAgency.owningKey,Bank.owningKey), LoanRequestContract.Commands.InitiateLoan())
//                fails()
//            }
//            transaction {
//                // Issuer is a required signer, will verify.
//                output(LoanRequestContract.LOANREQUEST_CONTRACT_ID, loanRequestState)
//                command(listOf(FinanceAgency.owningKey,Bank.owningKey), LoanRequestContract.Commands.InitiateLoan())
//                verifies()
//            }
//            transaction {
//                // FinanceAgency is also a required signer, will verify.
//                output(LoanRequestContract.LOANREQUEST_CONTRACT_ID, loanRequestStateWhereFAInitiatesLoan)
//                command(listOf(FinanceAgency.owningKey,Bank.owningKey), LoanRequestContract.Commands.InitiateLoan())
//                verifies()
//            }
//        }
//    }
}