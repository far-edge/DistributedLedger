# Secure State Sharing

## Installation
### Prerequisites
* Linux Environment.
* Administrative access to the machine.
* Access to Internet.
* [Node](https://nodejs.org/en/download/) and npm correctly installed (version 8.x or greater)
* Install Hyperledger Fabric version 1.3 following this installation [guide](https://hyperledger-fabric.readthedocs.io/en/release-1.3/getting_started.html). Copy the `crypto-config` dir in local path accessible for the client.
* Clone the repository: `git clone https://github.com/far-edge/DistributedLedger.git && cd secure-state-sharing`
* Install the Chaincode in your Fabric installation.

### Single Client
In order to innstall the Secure State Sharing as single client:
* Install Fiware Orion Contest Broker, following this [guide](https://fiware-orion.readthedocs.io/en/master/admin/install/index.html).
* Configure the SSS:
* * under the `resource` folder, editing the `config-fabric-network.json`, in particular susbstitute `localhost` with your HLF host and ports. Insert in the `cryptoconfigdir` the prerequisites chosen path.
* * edit the `config.json`, choosing che client port (default 3026) and the Orion Contest Broker host and port.
* Execute the command: `npm install && npm start`

### Multi Client (reccomendend)
In order to innstall the Secure State Sharing as Multi client installation (Two clients and Two OCBs sinstalled):
* Inside the `docker` folder edit the configuration for the two instances as in the previous steps, editing the file under `resources`for the first system and under `reosurces_1` for the other.
* If you can, *choose different HLF peers* (editing the config-fabric-network.json) for the two systems.
* From the root of the project execute: `npm run docker`.
* You have now two different installations working, composed by the two clients and two different OCBs.

## Usage
In order to use the Secure State Sharing please refer to the [NGSI v2 APIs](http://telefonicaid.github.io/fiware-orion/api/v2/stable/l).
The service is available at the choosen PORT (default 3026) in config.json file.