//package com.template.api
//
//import net.corda.core.messaging.CordaRPCOps
//import java.util.*
//import javax.ws.rs.GET
//import javax.ws.rs.Path
//import javax.ws.rs.Produces
//import javax.ws.rs.QueryParam
//import javax.ws.rs.core.MediaType
//import javax.ws.rs.core.Response
//import javax.ws.rs.core.Response.Status.BAD_REQUEST
//import javax.ws.rs.core.Response.Status.CREATED
//
//@Path("LoanApplicationMgt")
//class LoanApplicationMgtApi (val rpcOps: CordaRPCOps) {
//
//    private val myIdentity = rpcOps.nodeInfo().legalIdentities.first()
//
//    @GET
//    @Path("me")
//    @Produces(MediaType.APPLICATION_JSON)
//    fun me() = mapOf("me" to myIdentity)
//}