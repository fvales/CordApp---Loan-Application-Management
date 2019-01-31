package com.template.flows

import com.template.states.LoanRequestState
import net.corda.core.contracts.TransactionVerificationException
import net.corda.core.node.services.queryBy
import net.corda.core.utilities.getOrThrow
import net.corda.testing.core.singleIdentity
import net.corda.testing.node.MockNetwork
import net.corda.testing.node.StartedMockNode
import org.junit.After
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith


class InitiateLoanFLowTests {
    lateinit var network: MockNetwork
    lateinit var a: StartedMockNode
    lateinit var b: StartedMockNode

    @Before
    fun setup() {
        network = MockNetwork(listOf("com.template.contracts", "com.template.Schema"))
        a = network.createPartyNode()
        b = network.createPartyNode()
        // For real nodes this happens automatically, but we have to manually register the flow for tests.
        listOf(a, b).forEach { it.registerInitiatedFlow(InitiateLoanFlow.Acceptor::class.java) }
        network.runNetwork()
    }

    @After
    fun tearDown() {
        network.stopNodes()
    }

    @Test
    fun `flow rejects invalid LoanRequests`() {
        val flow = InitiateLoanFlow.Initiator(b.info.singleIdentity(),"sujay",-1)
        val future = a.startFlow(flow)
        network.runNetwork()

        // The LoanRequestContract specifies that LoanRequests cannot have negative values.
        assertFailsWith<TransactionVerificationException> { future.getOrThrow() }
    }

    @Test
    fun `SignedTransaction returned by the flow is signed by the initiator`() {
        val flow = InitiateLoanFlow.Initiator(b.info.singleIdentity(),"sujay",1)
        val future = a.startFlow(flow)
        network.runNetwork()

        val signedTx = future.getOrThrow()
        signedTx.verifySignaturesExcept(b.info.singleIdentity().owningKey)
    }

    @Test
    fun `SignedTransaction returned by the flow is signed by the acceptor`() {
        val flow = InitiateLoanFlow.Initiator(b.info.singleIdentity(),"sujay",1)
        val future = a.startFlow(flow)
        network.runNetwork()

        val signedTx = future.getOrThrow()
        signedTx.verifySignaturesExcept(a.info.singleIdentity().owningKey)
    }

    @Test
    fun `flow records a transaction in both parties' transaction storages`() {
        val flow = InitiateLoanFlow.Initiator(b.info.singleIdentity(),"sujay",1)
        val future = a.startFlow(flow)
        network.runNetwork()
        val signedTx = future.getOrThrow()

        // We check the recorded transaction in both transaction storages.
        for (node in listOf(a, b)) {
            assertEquals(signedTx, node.services.validatedTransactions.getTransaction(signedTx.id))
        }

        println("All Flow Tests passed")
    }

    @Test
    fun `recorded transaction has no inputs and a single output`() {
        val LoanAmount = 1
        val flow = InitiateLoanFlow.Initiator(b.info.singleIdentity(), "Sujay", LoanAmount)
        val future = a.startFlow(flow)
        network.runNetwork()
        val signedTx = future.getOrThrow()

        // We check the recorded transaction in both vaults.
        for (node in listOf(a, b)) {
            val recordedTx = node.services.validatedTransactions.getTransaction(signedTx.id)
            val txOutputs = recordedTx!!.tx.outputs
            assert(txOutputs.size == 1)

            val recordedState = txOutputs[0].data as LoanRequestState
            assertEquals(recordedState.LoanAmount, LoanAmount)
            assertEquals(recordedState.FinanceAgency, a.info.singleIdentity())
            assertEquals(recordedState.Bank, b.info.singleIdentity())
        }
    }

    @Test
    fun `flow records the correct LoanRequest in both parties' vaults`() {
        val LoanAmount = 1
        val flow = InitiateLoanFlow.Initiator(b.info.singleIdentity(), "Sujay", LoanAmount)
        val future = a.startFlow(flow)
        network.runNetwork()
        future.getOrThrow()

        // We check the recorded LoanRequests in both vaults.
        for (node in listOf(a, b)) {
            node.transaction {
                val LoanRequests = node.services.vaultService.queryBy<LoanRequestState>().states
                assertEquals(1, LoanRequests.size)
                val recordedState = LoanRequests.single().state.data
                assertEquals(recordedState.LoanAmount, LoanAmount)
                assertEquals(recordedState.FinanceAgency, a.info.singleIdentity())
                assertEquals(recordedState.Bank, b.info.singleIdentity())
            }
        }
    }
}