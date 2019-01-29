package com.template.Schema


import net.corda.core.schemas.MappedSchema
import net.corda.core.schemas.PersistentState
import java.util.*
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.Table

/**
 * The family of schemas for IOUState.
 */
object LoanRequestSchema

/**
 * An LoanRequestState schema.
 */
object LoanRequestSchemaV1 : MappedSchema(
        schemaFamily = LoanRequestSchema.javaClass,
        version = 1,
        mappedTypes = listOf(PersistentIOU::class.java)) {
    @Entity
    @Table(name = "loanrequest_states")
    class PersistentIOU(
            @Column(name = "LoanAmount")
            var LoanAmount: Int,

            @Column(name = "CustomerName")
            var CustomerName: String,

            @Column(name = "Bank")
            var BankName: String,

            @Column(name = "FinanceAgency")
            var FinanceAgencyName: String,

            @Column(name = "isEligibleForLoan")
            var isEligibleForLoan: Boolean,

            @Column(name = "linear_id")
            var linearId: UUID
    ) : PersistentState() {
        // Default constructor required by hibernate.
       // constructor(): this( LoanAmount: 0, CustomerName: "", BankName: "", FinanceAgencyName: "", isEligibleForLoan: false, UUID.randomUUID())
    }
}