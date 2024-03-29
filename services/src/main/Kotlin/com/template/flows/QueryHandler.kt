package com.template.flow

import co.paralleluniverse.fibers.Suspendable
import com.template.flows.CRAQueryFlow
import net.corda.core.flows.FlowException
import net.corda.core.flows.FlowLogic
import net.corda.core.flows.FlowSession
import net.corda.core.flows.InitiatedBy
import net.corda.core.utilities.ProgressTracker
import net.corda.core.utilities.unwrap
import com.template.service.Oracle

// The oracle flow to handle credit rating queries.
@InitiatedBy(CRAQueryFlow::class)
class QueryHandler(val session: FlowSession) : FlowLogic<Unit>() {
    companion object {
        object RECEIVING : ProgressTracker.Step("Receiving query request.")
        object CALCULATING : ProgressTracker.Step("Calculating credit ratings.")
        object SENDING : ProgressTracker.Step("Sending query response.")
    }

    override val progressTracker = ProgressTracker(RECEIVING, CALCULATING, SENDING)

    @Suspendable
    override fun call() {
        progressTracker.currentStep = RECEIVING
        val request = session.receive<String>().unwrap { it }

        progressTracker.currentStep = CALCULATING
        val response = try {
            // Get the credit rating from the oracle.
            serviceHub.cordaService(Oracle::class.java).query(request)
        } catch (e: Exception) {
            // Re-throw the exception as a FlowException so its propagated to the querying node.
            throw FlowException(e)
        }

        progressTracker.currentStep = SENDING
        session.send(response)
    }
}