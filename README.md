<p align="center">
  <img src="https://www.corda.net/wp-content/uploads/2016/11/fg005_corda_b.png" alt="Corda" width="500">
</p>


# Problem Statement

* Implement a CorDapp to digitize the process of loan application approval based on a credit check.
* A finance agency sends loan applications to a Bank. 
* The Bank checks the credit worthiness of the borrower through a credit rating agency and accordingly approves or rejects the             loan.
* The CorDapp should help to maintain a trail of all the activities happening for a loan application.
* Communication between Bank and credit rating agency or any data exchanged between Bank and credit rating agency should remain            private to Finance Agency.
* Finance agency should not see what Bank and Credit rating agency communicate.

# Operations

* Finance Agency should send the loan application to the bank which contains Individual name/company name and amount (Loan                 Amount).
* Bank should receive the application and forward it to Credit rating agency to check the eligibility of loan applicant/                    loan application (Example CIBIL score of an applicant).
* Credit rating agency should respond back to bank with the eligibility of the loan application.
* The credit rating agency could check loan eligibility through an Oracle service.
* Bank should receive the eligibility and decide whether to lend the loan, acknowledging its response to the Finance agency.
* Finance agency should be able to see approved loan applications. Hint: Check use of queryable states to filter vault                     states with specific field values

//Flow

** On FinanceAgency and Bank shell:
* run vaultquery contractStateType: com.template.states.LoanRequeststate

** On CreditRatingAgency and Bank shell
* run vaultquery contractStateType: com.template.states.LoanVerificationstate

** FinanceAgency shell:
* flow start InitiateLoanFlow Bank: "Bank", CustomerName: "JETSAIRWAYS", LoanAmount: 100

** Bank Shell:
* flow start RequestCRAFlow CreditRatingAgency: "CreditRatingAgnecy", linearIdentifier: "*Enter linear id*"

** CreditRatingAgency shell
* flow start CRAResponse Bank: "Bank", linearIdentifier: "*Enter linear id*"

** Bank shell
* flow start LoanResponseFlow FinanceAgency: "FinanceAgency", linearIdentifier: "*Enter linear id*"


** On FinanceAgency and Bank shell:
* run vaultquery contractStateType: com.template.states.LoanRequeststate

// Rurnning API

1. Run the servers in /clients/build.gradle

2. Do the call in postman client
e.g localhost:10013/me

# CorDapp Template - Kotlin

Welcome to the Kotlin CorDapp template. The CorDapp template is a stubbed-out CorDapp that you can use to bootstrap 
your own CorDapps.

**This is the Kotlin version of the CorDapp template. The Java equivalent is 
[here](https://github.com/corda/cordapp-template-java/).**

# Pre-Requisites

See https://docs.corda.net/getting-set-up.html.

# Usage

## Running the nodes

See https://docs.corda.net/tutorial-cordapp.html#running-the-example-cordapp.

## Interacting with the nodes

### Shell

When started via the command line, each node will display an interactive shell:

    Welcome to the Corda interactive shell.
    Useful commands include 'help' to see what is available, and 'bye' to shut down the node.
    
    Tue Nov 06 11:58:13 GMT 2018>>>

You can use this shell to interact with your node. For example, enter `run networkMapSnapshot` to see a list of 
the other nodes on the network:

    Tue Nov 06 11:58:13 GMT 2018>>> run networkMapSnapshot
    [
      {
      "addresses" : [ "localhost:10002" ],
      "legalIdentitiesAndCerts" : [ "O=Notary, L=London, C=GB" ],
      "platformVersion" : 3,
      "serial" : 1541505484825
    },
      {
      "addresses" : [ "localhost:10005" ],
      "legalIdentitiesAndCerts" : [ "O=PartyA, L=London, C=GB" ],
      "platformVersion" : 3,
      "serial" : 1541505382560
    },
      {
      "addresses" : [ "localhost:10008" ],
      "legalIdentitiesAndCerts" : [ "O=PartyB, L=New York, C=US" ],
      "platformVersion" : 3,
      "serial" : 1541505384742
    }
    ]
    
    Tue Nov 06 12:30:11 GMT 2018>>> 

You can find out more about the node shell [here](https://docs.corda.net/shell.html).

### Client

`clients/src/main/kotlin/com/template/Client.kt` defines a simple command-line client that connects to a node via RPC 
and prints a list of the other nodes on the network.

#### Running the client

##### Via the command line

Run the `runTemplateClient` Gradle task. By default, it connects to the node with RPC address `localhost:10006` with 
the username `user1` and the password `test`.

##### Via IntelliJ

Run the `Run Template Client` run configuration. By default, it connects to the node with RPC address `localhost:10006` 
with the username `user1` and the password `test`.

### Webserver

`clients/src/main/kotlin/com/template/webserver/` defines a simple Spring webserver that connects to a node via RPC and 
allows you to interact with the node over HTTP.

The API endpoints are defined here:

     clients/src/main/kotlin/com/template/webserver/Controller.kt

And a static webpage is defined here:

     clients/src/main/resources/static/

#### Running the webserver

##### Via the command line

Run the `runTemplateServer` Gradle task. By default, it connects to the node with RPC address `localhost:10006` with 
the username `user1` and the password `test`, and serves the webserver on port `localhost:10050`.

##### Via IntelliJ

Run the `Run Template Server` run configuration. By default, it connects to the node with RPC address `localhost:10006` 
with the username `user1` and the password `test`, and serves the webserver on port `localhost:10050`.

#### Interacting with the webserver

The static webpage is served on:

    http://localhost:10050

While the sole template endpoint is served on:

    http://localhost:10050/templateendpoint
    
# Extending the template

You should extend this template as follows:

* Add your own state and contract definitions under `cordapp-contracts-states/src/main/kotlin/`
* Add your own flow definitions under `cordapp/src/main/kotlin/`
* Extend or replace the client and webserver under `clients/src/main/kotlin/`

For a guided example of how to extend this template, see the Hello, World! tutorial 
[here](https://docs.corda.net/hello-world-introduction.html).
