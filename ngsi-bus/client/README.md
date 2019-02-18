# NGSI Bus - OCB Proxy	NGSI Bus

 --------
The NGSI Bus acts as a Proxy for the [Orion Context Broker](https://fiware-orion.readthedocs.io/en/master/), connected to the [Hyperledger Fabric](https://hyperledger-fabric.readthedocs.io) in order to make the Orion Entities global in the blockchain network. 	

 This Ledger Service is actually composed by three separate elements:
## Installation
### Prerequisites
* Linux Environment.
* Administrative access to the machine.
* Access to Internet.
* [Node](https://nodejs.org/en/download/) and pm correctly installed (version 8.x or greater)
* Docker and Docker Compose correctly installed and working.
* Clone the repository: `git clone https://github.com/far-edge/DistributedLedger.git && cd ngsi-bus`

### Installation Guide
#### HLF Administrator
* Point to the `chaincode` folder.
*	Deploy the Node.js Chaincode in the **HLF 1.3** Network.
*	Give users, **the digital certificates and a configuration file** in order to access to the Chaincode and Network Installation.
#### User
* Open a Terminal Console on your system. 
*	Point to the `client` folder.
*	Copy the `config-fabric-network.json` given by your HLF administrator under the folder `docker/resources`.
*	Copy the digital certificates given by your HLF administrator, under the `docker/resources/crypto-config` folder.
*	Create the Docker artifact containing the OCB + OCB Proxy with the command:  
    `npm run docker`.
*	Launch the system with the command: `./start.sh`. 



## Usage
<br/>The NGSI Bus is now available on port `3026` of your environment.
<br/>Furthermore, users are allowed to edit and customize the `start.sh` script if they need to add some instructions to the system during the bootstrap phase.
