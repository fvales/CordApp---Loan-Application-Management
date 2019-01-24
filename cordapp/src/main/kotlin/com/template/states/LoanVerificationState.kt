package com.template.states

import net.corda.core.contracts.ContractState
import net.corda.core.contracts.LinearState
import net.corda.core.contracts.UniqueIdentifier
import net.corda.core.identity.Party

/**
 * The state object recording LoanRequestState between Credit rating Agency and Bank.
 *
 * A state must implement [ContractState] or one of its descendants.
 *
 * @param LoanAmount the value of the loan amount.
 * @CustomerName  Individual name/company name.
 * @param Bank the party issuing the loan application.
 * @param CreditRatingAgency the party checking the eligibility of the loan application.
 */


data class LoanVerificationState(val LoanAmount: Int,
                            val CustomerName: String,
                            val Bank: Party,
                            val CreditRatingAgency: Party,
                            val isEligibleForLoan: Boolean = false,
                            override val linearId: UniqueIdentifier = UniqueIdentifier()): LinearState {

    /** The public keys of the involved parties. */
    override val participants get() = listOf(Bank, CreditRatingAgency)

}
