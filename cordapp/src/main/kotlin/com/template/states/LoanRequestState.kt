package com.template.states

import com.template.Schema.LoanRequestSchemaV1
import net.corda.core.contracts.LinearState
import net.corda.core.contracts.UniqueIdentifier
import net.corda.core.identity.AbstractParty
import net.corda.core.identity.Party
import net.corda.core.schemas.MappedSchema
import net.corda.core.schemas.PersistentState
import net.corda.core.schemas.QueryableState

/**
 * The state object recording LoanRequestState between Finance Agecy and Bank.
 *
 * A state must implement [ContractState] or one of its descendants.
 *
 * @param LoanAmount the value of the loan amount.
 * @CustomerName  Individual name/company name.
 * @param FinanceAgency the party initiating the loan application.
 * @param Bank the party receiving the loan application.
 */


data class LoanRequestState(val LoanAmount: Int,
                            val CustomerName: String,
                            val Bank: Party,
                            val FinanceAgency: Party,
                            val loan_status: String = "INITIATED",
                    override val linearId: UniqueIdentifier = UniqueIdentifier()): LinearState, QueryableState {
    //constructor() : this()

    /** The public keys of the involved parties. */
    override val participants get() = listOf(Bank, FinanceAgency)

    override fun generateMappedObject(schema: MappedSchema): PersistentState {
        return when (schema) {
            is LoanRequestSchemaV1 -> LoanRequestSchemaV1.PersistentIOU(
                    this.LoanAmount,
                    this.CustomerName,
                    this.Bank.name.toString(),
                    this.FinanceAgency.name.toString(),
                    this.loan_status,
                    this.linearId.id
            )
            else -> throw IllegalArgumentException("Unrecognised schema $schema")
        }
    }

    override fun supportedSchemas(): Iterable<MappedSchema> = listOf(LoanRequestSchemaV1)




}

