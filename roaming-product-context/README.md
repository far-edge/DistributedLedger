# Roaming Product Context 

The **RPC Web Service** Provides a local Web API that interacts with both local OCB instance and the global RPC Chaincode. Three service endpoints are available to clients: Release, Acquire and Dispose.

this **Ledger Service** is actually composed by three separate elements:

-   **[Orion Context Broker (OCB)](https://fiware-orion.readthedocs.io/)**

    See the [NGSI Bus](https://github.com/far-edge/DistributedLedger/edit/develop/ngsi-bus) for some general information about the OCB component. In this context, local OCB instances hold the product context one at a time: the same NGSI entity cannot exist in more than one local scope, and is transferred from one scope to another with the mediation of the RPC Ledger Service and the RPC Web Service.

-   **[RPC Web Service](https://github.com/far-edge/DistributedLedger/edit/develop/roaming-product-context/client)**

    Provides a local Web API that interacts with both local OCB instance and the global RPC Chaincode. Three service endpoints are available to clients: Release, Acquire and Dispose -- see below for their documentation, which also includes an explaination of their functionality.

    This asset has been implemented in FAR-EDGE as a Node.js server application and is based on the HLF SDK -- i.e., the library made available by the HLF project to support Node.js clients.

-   **[RPC Ledger Service](https://github.com/far-edge/DistributedLedger/edit/develop/roaming-product-context/chaincode)**

    Manages NGSI entities as *sealed objects* -- i.e., an immutable copy of an NGSI entity that is identified by a unique ID and is signed by the digital identity associated to the local RPC Service. Sealed objects can be moved to any local scope, where they are unpacked and transformed again into fully-functional NGSI entity, provided that only one local scope at a time has the ownership of the "live" entity. The role of the chaincode is not only to store and retrieve individual objects on the global Distributed Ledger (DL), but also to enforce a system of "exclusive locks" on their access.
This asset has been implemented in FAR-EDGE as a Node.js chaincode.
