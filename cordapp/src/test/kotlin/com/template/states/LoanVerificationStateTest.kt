package com.template.states

import com.template.states.LoanRequestState
import net.corda.core.contracts.ContractState
import net.corda.core.contracts.UniqueIdentifier
import net.corda.core.identity.CordaX500Name
import net.corda.core.identity.Party
import net.corda.testing.core.TestIdentity
import org.junit.Test
import kotlin.test.assertEquals

class LoanVerificationStateTest{
    val Bank = TestIdentity(CordaX500Name("Bank", "", "GB")).party
    val CreditRatingAgency = TestIdentity(CordaX500Name("CreditRatingAgency", "", "GB")).party

    @Test
    fun loanVerificationStateHasParamsOfCorrectTypeInConstructor() {
        LoanVerificationState(100,
                "Freeda",
                Bank,
                CreditRatingAgency,
                false,
                UniqueIdentifier())
    }

    @Test
    fun loanVerificationStateHasGettersForBankCRALoanAmountCustomerName() {
        var loanVerificationState = LoanVerificationState(100,
                "Freeda",
                Bank,
                CreditRatingAgency,
                false,
                UniqueIdentifier())
        assertEquals(Bank, loanVerificationState.Bank)
        assertEquals(CreditRatingAgency, loanVerificationState.CreditRatingAgency)
        assertEquals(100, loanVerificationState.LoanAmount)
        assertEquals("Freeda", loanVerificationState.CustomerName)
    }

    @Test
    fun LoanVerificationStateImplementsContractState() {
        assert(LoanVerificationState(100,
                "Freeda",
                Bank,
                CreditRatingAgency,
                false,
                UniqueIdentifier()) is ContractState)
    }

    @Test
    fun loanVerificationStateHasTwoParticipantsTheBankAndCRA() {
        var loanVerificationState = LoanVerificationState(100,
                "Freeda",
                Bank,
                CreditRatingAgency,
                false,
                UniqueIdentifier())
        assertEquals(2, loanVerificationState.participants.size)
        assert(loanVerificationState.participants.contains(Bank))
        assert(loanVerificationState.participants.contains(CreditRatingAgency))
    }
}