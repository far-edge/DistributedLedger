NGSI Bus
--------

This Ledger Service is actually composed by three separate elements:

-   **Orion Context Broker (OCB)**

    This is an open source component from the FIWARE platform. It implements a *context sharing* service that is delivered throught the NGSI API, which is a FIWARE standard for IoT interoperability. This RESTful Web API follows a publish / subscribe pattern and allows multiple clients (IoT devices and applications) to share common context information, which is packaged as an *entity*. However, the API is *not* directly available to clients, as it is hidden behind the OCB Proxy (see next point).

    The OCB component and the NGSI API are not FAR-EDGE foreground, so they are not documented here.

-   **OCB Proxy**

    Provides clients with an access path to a hidden OCB instance, acting as a *reverse proxy* that replicates the same NGSI API. Thanks to its *man-in-the-middle* role, it injects some additional functionality in the NGSI protocol: A) intercepts NGSI *write* calls (entity create, update and delete) from the clients and transforms them into corresponding calls to the Ledger Service (see next point); B) receives entity-related notifications from the Ledger Service and transforms them into corresponding NGSI write calls to the hidden OCB instance; C) transparently forwards NGSI *read* calls from the clients to the hidden OCB instance.

    This asset has been implemented in FAR-EDGE as a Node.js server application and is based on the HLF SDK -- i.e., the library made available by the HLF project to support Node.js clients.

    It is worth noting that its very existence stems from a trivial matter of fact: the OCB component is impractical to extend, because it's a native C++ application. It that was not the case, the best approach would have been to directly integrate the OCB with the Ledger Service, thus avoiding the inefficiency of NGSI call forwarding.

-   **NGSI Bus Ledger Service**

    Maintains, in the global scope, the *master copy* of NGSI entities that are deployed on local OCB instances. Entities created in one local scope are also created on the Distributed Ledger and then propagated to all local scopes by means of the HLF event notification mechanism and with the support of local OCB Proxies. The same propagation happens for updates and deletes.

    This asset has been implemented in FAR-EDGE as a Node.js chaincode.

In a working system there are typically multiple local scopes, each served by one OCB + OCB Proxy couple running on a dedicated EG. This architecture was previously illustrated in Figure 1. NGSI Bus Chaincode, on the other hand, is a "virtualized" facility: a copy of it hosted on each HLF *peer node*, but peer nodes can be physically installed anywhere. The most decentralized and elegant approach would be to run one peer node on each EG, so that the EG machine becomes a self-contained local scope which can be relocated anywhere -- along with all the edge nodes connected to it -- with a minimum of hassle. In this deployment, however, we resorted to the more centralized HyperLab environment in the FAR-EDGE testbed -- see deliverable 6.7.

[Here](https://github.com/far-edge/DistributedLedger/tree/develop/ngsi-bus/client) you can find the installation steps for the NGSI Bus components on one single EG machine. The same steps should be repeated for each EG machine of the target system.
