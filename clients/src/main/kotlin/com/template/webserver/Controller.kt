package com.template.webserver



import com.template.flows.InitiateLoanFlow
import com.template.states.LoanRequestState
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


        // 2. Start the InitiateLoanFlow. We block and wait for the flow to return.

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


*//*@GET
@Path("LoanAmount")
@Produces(MediaType.APPLICATION_JSON)
fun LoanAmount() = proxy.vaultQuery(LoanRequestState::class.java).LoanAmount*//*
*/
}