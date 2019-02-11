package com.template.service
import com.template.contracts.LoanVerificationContract
import net.corda.core.contracts.Command
import net.corda.core.crypto.TransactionSignature
import net.corda.core.node.ServiceHub
import net.corda.core.node.services.CordaService
import net.corda.core.serialization.SingletonSerializeAsToken
import net.corda.core.transactions.FilteredTransaction

import java.math.BigInteger

// We sub-class 'SingletonSerializeAsToken' to ensure that instances of this class are never serialised by Kryo.
// When a flow is check-pointed, the annotated @Suspendable methods and any object referenced from within those
// annotated methods are serialised onto the stack. Kryo, the reflection based serialisation framework we use, crawls
// the object graph and serialises anything it encounters, producing a graph of serialised objects.
// This can cause issues. For example, we do not want to serialise large objects on to the stack or objects which may
// reference databases or other external services (which cannot be serialised!). Therefore we mark certain objects with
// tokens. When Kryo encounters one of these tokens, it doesn't serialise the object. Instead, it creates a
// reference to the type of the object. When flows are de-serialised, the token is used to connect up the object
// reference to an instance which should already exist on the stack.
@CordaService
class Oracle(val services: ServiceHub) : SingletonSerializeAsToken() {
    private val myKey = services.myInfo.legalIdentities.first().owningKey

    // Returns the Credit Ratings for given pancCard
    fun query(customerName: String): Int {
        require(customerName.isNotEmpty())
        if (customerName.equals("JETSAIRWAYS")) {
            return 800
        } else if(customerName.equals("AMERICONAIRWAYS")) {
            return 700
        }
        else if(customerName.equals("SAHARAAIRLINES")) {
            return 600
        }
        else if(customerName.equals("JETBLUEAIRLINE")) {
            return 500
        }
        else{
            return 300
        }
    }
    // Signs over a transaction if the specified Credit Rating for a particular panCardNo is correct.
    // This function takes a filtered transaction which is a partial Merkle tree. Any parts of the transaction which
    // the oracle doesn't need to see in order to verify the correctness of the credit rating have been removed. In this
    // case, all but the [EligibilityContract.Commands.GenerateRating] commands have been removed. If the Credit rating is correct then the oracle
    // signs over the Merkle root (the hash) of the transaction.
    fun sign(ftx: FilteredTransaction): TransactionSignature {
        // Check the partial Merkle tree is valid.
        ftx.verify()

        /** Returns true if the component is an Create command that:
         *  - States the correct prime
         *  - Has the oracle listed as a signer
         */
        fun isCommandWithCorrectCreditRatingAndIAmSigner(elem: Any) = when {
            elem is Command<*> && elem.value is LoanVerificationContract.Commands.GenerateRating -> {
                val cmdData = elem.value as LoanVerificationContract.Commands.GenerateRating
                myKey in elem.signers && query(cmdData.customerName) == cmdData.cibilRating
            }
            else -> false
        }

        // Is it a Merkle tree we are willing to sign over?
        val isValidMerkleTree = ftx.checkWithFun(::isCommandWithCorrectCreditRatingAndIAmSigner)

        if (isValidMerkleTree) {
            return services.createSignature(ftx, myKey)
        } else {
            throw IllegalArgumentException("Oracle signature requested over invalid transaction.")
        }
    }
}