//package com.template.api
//
//import net.corda.core.messaging.CordaRPCOps
//import net.corda.webserver.services.WebServerPluginRegistry
//import java.util.function.Function
//
//class LoanApplicationMgtPlugin : WebServerPluginRegistry {
//    override val webApis: List<Function<CordaRPCOps, out Any>> = listOf(Function(::LoanApplicationMgtApi))
//    override val staticServeDirs: Map<String, String> = mapOf(
//            "LoanApplicationMgt" to javaClass.classLoader.getResource("loanApplicationMgtWeb").toExternalForm()
//    )
//}