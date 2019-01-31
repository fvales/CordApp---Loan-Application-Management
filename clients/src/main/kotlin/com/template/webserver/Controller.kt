package com.template.webserver



import com.template.flows.InitiateLoanFlow
import com.template.flows.LoanResponseFlow
import com.template.flows.RequestCRAFlow
import com.template.states.LoanRequestState
import com.template.flows.CRAResponse
import com.template.states.LoanVerificationState
import net.corda.core.contracts.UniqueIdentifier

import net.corda.core.identity.Party
/*
import com.template.flows.InitiateLoanFlow
import com.template.flows.LoanResponseFlow
import com.template.contracts.LoanRequestContract
import com.template.contracts.LoanVerificationContract
import com.template.states.LoanVerificationState
import com.template.Schema.LoanRequestSchemaV1
import com.template.Schema.LoanVerificationSchemaV1
*/

import net.corda.core.messaging.CordaRPCOps
import net.corda.core.utilities.OpaqueBytes
import net.corda.core.utilities.getOrThrow
import java.util.*
/*import javax.ws.rs.GET
import javax.ws.rs.Path
import javax.ws.rs.Produces
import javax.ws.rs.QueryParam
import javax.ws.rs.core.MediaType
import javax.ws.rs.core.Response
import javax.ws.rs.core.Response.Status.BAD_REQUEST
import javax.ws.rs.core.Response.Status.CREATED*/

import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.util.UUID.fromString


/*import javax.ws.rs.GET
import javax.ws.rs.Path
import javax.ws.rs.Produces*/

/*import javax.enterprise.inject.Produces
import javax.ws.rs.GET
import javax.ws.rs.Path*/

/**
 * Define your API endpoints here.
 */
@RestController
@RequestMapping("/") // The paths for HTTP requests are relative to this base path.
class Controller(rpc: NodeRPCConnection) {

    companion object {
        private val logger = LoggerFactory.getLogger(Controller::class.java)
    }

    private val proxy = rpc.proxy

    @GetMapping(value = "/templateendpoint", produces = arrayOf("text/plain"))
    private fun templateendpoint(): String {
        return "Define an endpoint here."
    }

    private val myIdentity = proxy.nodeInfo().legalIdentities.first()

    // Node info
    @GetMapping(value = "/me", produces = arrayOf("text/plain"))
    fun me():String
    {
        return(myIdentity.name.toString())
    }

    //Peer info
    @GetMapping(value = "/peers", produces = arrayOf("text/plain"))
    fun peers(): String {
        return mapOf("peers" to proxy.networkMapSnapshot()
                .filter { nodeInfo -> nodeInfo.legalIdentities.first() != myIdentity }
                .map { it.legalIdentities.first().name.organisation }).toString()
    }



    //LonRequestState
    @GetMapping(value = "/LoanRequestState", produces = arrayOf("text/plain"))
    fun LoanRequestState(): String {
        return (proxy.vaultQuery(LoanRequestState::class.java).states).toString()
    }

    //LonVerificationState
    @GetMapping(value = "/LonVerificationState", produces = arrayOf("text/plain"))
    fun LoanVerificationState(): String {
        return (proxy.vaultQuery(LoanVerificationState::class.java).states).toString()
    }

    //InitiateLoanFLow
    @GetMapping(value = "/InitiateLoanFlow", produces = arrayOf("text/plain"))
    fun InitiateLoanFlow(
            @RequestParam("LoanAmount") LoanAmount: Int,
            @RequestParam("CustomerName") CustomerName: String,
            @RequestParam("party") party : String
    ): String {

        // 1. Get party objects for the counterparty.
        val Bank = proxy.partiesFromName(party, exactMatch = false).singleOrNull()
                ?: throw IllegalStateException("Couldn't lookup node identity")


        // 2. Start the InitiateLoan Flow. We block and wait for the flow to return.

        val (status) = try {
            val flowHandle = proxy.startFlowDynamic(
                    InitiateLoanFlow.Initiator::class.java,
                    Bank,
                    CustomerName,
                    LoanAmount
            )

            flowHandle.use { flowHandle.returnValue.getOrThrow() }
            HttpStatus.CREATED to "Initiated loan"
        } catch (e: Exception) {
            HttpStatus.BAD_REQUEST to e.message
        }

        // return HttpStatus.status(status).entity(message).build()
        return status.toString()
    }

    //RequestCRAFLow
    @GetMapping(value = "/RequestCRAFLow", produces = arrayOf("text/plain"))
    fun RequestCRAFLow(
            @RequestParam("linearIdentifier") linearIdentifier: String,
            @RequestParam("party") party : String
    ): String {

        // 1. Get party objects for the counterparty.
        val CreditRatingAgency = proxy.partiesFromName(party, exactMatch = false).singleOrNull()
                ?: throw IllegalStateException("Couldn't lookup node identity")


        // 2. Start the RequestCRA FLow. We block and wait for the flow to return.

        val (status) = try {
            val flowHandle = proxy.startFlowDynamic(
                    RequestCRAFlow.Initiator::class.java,
                    CreditRatingAgency,
                    UniqueIdentifier.fromString(linearIdentifier)
            )

            flowHandle.use { flowHandle.returnValue.getOrThrow() }
            HttpStatus.CREATED to "Request CRA"
        } catch (e: Exception) {
            HttpStatus.BAD_REQUEST to e.message
        }

        // return HttpStatus.status(status).entity(message).build()
        return status.toString()
    }

    //CRAResponse
    @GetMapping(value = "/CRAResponse", produces = arrayOf("text/plain"))
    fun CRAResponse(
            @RequestParam("linearIdentifier") linearIdentifier: String,
            @RequestParam("party") party : String
    ): String {

        // 1. Get party objects for the counterparty.
        val Bank = proxy.partiesFromName(party, exactMatch = false).singleOrNull()
                ?: throw IllegalStateException("Couldn't lookup node identity")


        // 2. Start the CRAResponse FLow. We block and wait for the flow to return.

        val (status) = try {
            val flowHandle = proxy.startFlowDynamic(
                    CRAResponse.Initiator::class.java,
                    Bank,
                    UniqueIdentifier.fromString(linearIdentifier)
            )



            flowHandle.use { flowHandle.returnValue.getOrThrow() }
            HttpStatus.CREATED to "CRA Response"
        } catch (e: Exception) {
            HttpStatus.BAD_REQUEST to e.message
        }

        // return HttpStatus.status(status).entity(message).build()
        return status.toString()
    }

    //LoanResponseFLow
    @GetMapping(value = "/LoanResponseFLow", produces = arrayOf("text/plain"))
    fun LoanResponseFLow(
            @RequestParam("linearIdentifier") linearIdentifier: String,
            @RequestParam("party") party : String
    ): String {

        // 1. Get party objects for the counterparty.
        val FinanceAgency = proxy.partiesFromName(party, exactMatch = false).singleOrNull()
                ?: throw IllegalStateException("Couldn't lookup node identity")


        // 2. Start the LoanResponseFLow. We block and wait for the flow to return.

        val (status) = try {
            val flowHandle = proxy.startFlowDynamic(
                    LoanResponseFlow.Initiator::class.java,
                    FinanceAgency,
                    UniqueIdentifier.fromString(linearIdentifier)
            )

            flowHandle.use { flowHandle.returnValue.getOrThrow() }
            HttpStatus.CREATED to "Loan Response"
        } catch (e: Exception) {
            HttpStatus.BAD_REQUEST to e.message
        }

        // return HttpStatus.status(status).entity(message).build()
        return status.toString()
    }


}