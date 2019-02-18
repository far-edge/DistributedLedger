# NGSI Bus - OCB Proxy	NGSI Bus

 --------
The NGSI Bus acts as a Proxy for the [Orion Context Broker](https://fiware-orion.readthedocs.io/en/master/), connected to the [Hyperledger Fabric](https://hyperledger-fabric.readthedocs.io) in order to make the Orion Entities global in the blockchain network. 	

 This Ledger Service is actually composed by three separate elements:
## Installation	
 ### Prerequisites	-   **Orion Context Broker (OCB)**
* Linux Environment.	
 * Administrative access to the machine.	    This is an open source component from the FIWARE platform. It implements a *context sharing* service that is delivered throught the NGSI API, which is a FIWARE standard for IoT interoperability. This RESTful Web API follows a publish / subscribe pattern and allows multiple clients (IoT devices and applications) to share common context information, which is packaged as an *entity*. However, the API is *not* directly available to clients, as it is hidden behind the OCB Proxy (see next point).
* Access to Internet.	
 * [Node](https://nodejs.org/en/download/) and pm correctly installed (version 8.x or greater)	    The OCB component and the NGSI API are not FAR-EDGE foreground, so they are not documented here.
* Docker and Docker Compose correctly installed and working.	
 * Access to a HLF v1.3 system and availability of an administrator that will provide the required artifacts (digital certificate and configuration file) for connecting the OCB Proxy to the HLF network.	-   **OCB Proxy**
* Clone the repository: `git clone https://github.com/far-edge/DistributedLedger.git && cd ngsi-bus`	

     Provides clients with an access path to a hidden OCB instance, acting as a *reverse proxy* that replicates the same NGSI API. Thanks to its *man-in-the-middle* role, it injects some additional functionality in the NGSI protocol: A) intercepts NGSI *write* calls (entity create, update and delete) from the clients and transforms them into corresponding calls to the Ledger Service (see next point); B) receives entity-related notifications from the Ledger Service and transforms them into corresponding NGSI write calls to the hidden OCB instance; C) transparently forwards NGSI *read* calls from the clients to the hidden OCB instance.
### Installation Guide	
 #### HLF Administrator	    This asset has been implemented in FAR-EDGE as a Node.js server application and is based on the HLF SDK -- i.e., the library made available by the HLF project to support Node.js clients.
*   Point to the `chaincode` folder.	
 *	Deploy the Chaincode in the **HLF 1.3** Network.	    It is worth noting that its very existence stems from a trivial matter of fact: the OCB component is impractical to extend, because it's a native C++ application. It that was not the case, the best approach would have been to directly integrate the OCB with the Ledger Service, thus avoiding the inefficiency of NGSI call forwarding.
*	Give users, **the digital certificates and a configuration file** in order to access to the Chaincode and Network Installation.	
 #### User	-   **NGSI Bus Ledger Service**
*   Open a Terminal Console on your system. 	
 *	Point to the `client` folder.	    Maintains, in the global scope, the *master copy* of NGSI entities that are deployed on local OCB instances. Entities created in one local scope are also created on the Distributed Ledger and then propagated to all local scopes by means of the HLF event notification mechanism and with the support of local OCB Proxies. The same propagation happens for updates and deletes.
*	Copy the `config-fabric-network.json` given by your HLF administrator under the folder `docker/resources`.	
 *	Copy the digital certificates given by your HLF administrator, under the `docker/resources/crypto-config` folder.	    This asset has been implemented in FAR-EDGE as a Node.js chaincode.
*	Create the Docker artifact containing the OCB + OCB Proxy with the command:  	
     `npm run docker`.	In a working system there are typically multiple local scopes, each served by one OCB + OCB Proxy couple running on a dedicated EG. This architecture was previously illustrated in Figure 1. NGSI Bus Chaincode, on the other hand, is a "virtualized" facility: a copy of it hosted on each HLF *peer node*, but peer nodes can be physically installed anywhere. The most decentralized and elegant approach would be to run one peer node on each EG, so that the EG machine becomes a self-contained local scope which can be relocated anywhere -- along with all the edge nodes connected to it -- with a minimum of hassle. In this deployment, however, we resorted to the more centralized HyperLab environment in the FAR-EDGE testbed -- see deliverable 6.7.
*	Launch the system with the command: `./start.sh`. 	

 [Here](https://github.com/far-edge/DistributedLedger/tree/develop/ngsi-bus/client) you can find the installation steps for the NGSI Bus components on one single EG machine. The same steps should be repeated for each EG machine of the target system.


 ## Usage	
In order to use the Ngsi Bus please refer to the [NGSI v2 APIs](http://telefonicaid.github.io/fiware-orion/api/v2/stable).	
<br/>The OCB Proxy is now available on port `3026` of your environment.	
<br/>Furthermore, users are allowed to edit and customize the `start.sh` script if they need to add some instructions to the system during the bootstrap phase.
