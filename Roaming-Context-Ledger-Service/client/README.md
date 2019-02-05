# Roaming Context Ledger Service - Client

The NGSI Bus acts as a Proxy for the [Orion Context Broker](https://fiware-orion.readthedocs.io/en/master/), connected to the [Hyperledger Fabric](https://hyperledger-fabric.readthedocs.io) in order to make the Orion Entities global in the blockchain network. 

## Installation
### Prerequisites
* Linux Environment.
* Administrative access to the machine.
* Access to Internet.
* [Node](https://nodejs.org/en/download/) and npm correctly installed (version 8.x or greater)
* Install **Hyperledger Fabric version 1.3** following this installation [guide](https://hyperledger-fabric.readthedocs.io/en/release-1.3/getting_started.html). 
* * Copy the `crypto-config` dir in local path accessible for the client.
* Clone the repository: `git clone https://github.com/far-edge/DistributedLedger.git && cd secure-state-sharing`
* [Install the Chaincode](https://github.com/far-edge/DistributedLedger/tree/develop/secure-state-sharing/chaincode) in your Fabric installation.
* Install [Docker](https://www.docker.com/get-started) and Docker Compose (17.x or greater).

### Single Client
In order to install the Secure State Sharing as single client:
* Install Fiware Orion Contest Broker, following this [guide](https://fiware-orion.readthedocs.io/en/master/admin/install/index.html).
* Configure the SSS:
* * Under the `resource` folder, editing the `config-fabric-network.json`, in particular susbstitute `localhost` with your HLF host and ports. 
* * Insert in the `cryptoconfigdir` the prerequisites chosen path.
* * Edit the `config.json`, choosing che client port (default 3026) and the Orion Contest Broker host and port.
* Execute the command: `npm install && npm start`

### Multi Client (reccomendend)
In order to innstall the Secure State Sharing as Multi client installation (Two clients and Two OCBs installed):
* Inside the `docker` folder edit the configuration for the two instances as in the previous steps, editing the file under `resources`for the first system and under `reosurces_1` for the other.
* If you can, *choose different HLF PEERS* (editing the `config-fabric-network.json`) for the two systems.
* From the root of the project execute: `npm run docker` to build the docker image.
* Under the `docker` directory launch the commands: `./start.sh` to run the full system.
* You have now two different installations working, composed by the two clients and two different OCBs.

### Migrate

The Migrate method allows the migration of a context from one Context broker to another connected to the same network.
It is a combination of POST and GET, which returns a lis of Entities that we want to be subscrive it.
