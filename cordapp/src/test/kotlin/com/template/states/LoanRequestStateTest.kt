package com.template.states

import com.template.states.LoanRequestState
import net.corda.core.contracts.ContractState
import net.corda.core.contracts.UniqueIdentifier
import net.corda.core.identity.CordaX500Name
import net.corda.core.identity.Party
import net.corda.testing.core.TestIdentity
import org.junit.Test
import kotlin.test.assertEquals

class LoanRequestStateTest{
    val Bank = TestIdentity(CordaX500Name("Bank", "", "GB")).party
    val FinanceAgency = TestIdentity(CordaX500Name("FinanceAgency", "", "GB")).party

    @Test
    fun loanRequestStateHasParamsOfCorrectTypeInConstructor() {
        LoanRequestState(100,
        "Freeda",
        Bank,
        FinanceAgency,
        "INITIALISED",
        UniqueIdentifier())
    }

    @Test
    fun loanRequestStateHasGettersForBankFinanceAgencyLoanAmountCustomerName() {
        var loanRequestState = LoanRequestState(100,
                "Freeda",
                Bank,
                FinanceAgency,
                "INITIALISED",
                UniqueIdentifier())
        assertEquals(Bank, loanRequestState.Bank)
        assertEquals(FinanceAgency, loanRequestState.FinanceAgency)
        assertEquals(100, loanRequestState.LoanAmount)
        assertEquals("Freeda", loanRequestState.CustomerName)
    }

    @Test
    fun LoanRequestStateImplementsContractState() {
        assert(LoanRequestState(100,
                "Freeda",
                Bank,
                FinanceAgency,
                "INITIALISED",
                UniqueIdentifier()) is ContractState)
    }

    @Test
    fun loanRequestStateHasTwoParticipantsTheBankAndTheFinanceAgency() {
        var loanRequestState = LoanRequestState(100,
                "Freeda",
                Bank,
                FinanceAgency,
                "INITIALISED",
                UniqueIdentifier())
        assertEquals(2, loanRequestState.participants.size)
        assert(loanRequestState.participants.contains(Bank))
        assert(loanRequestState.participants.contains(FinanceAgency))
    }
}