# NGSI Bus Chaincode

## Installation
### Prerequisites
* Linux Environment.
* Administrative access to the machine.
* Access to Internet.
* [Node](https://nodejs.org/en/download/) and npm correctly installed (version 8.x or greater) (*Optional*)
* Install **Hyperledger Fabric version 1.3** following this installation [guide](https://hyperledger-fabric.readthedocs.io/en/release-1.3/getting_started.html).
* Clone the repository: `git clone https://github.com/far-edge/DistributedLedger.git && cd secure-state-sharing`
* Copy the `chaincode` folder and rename it as `sss-chaincode` under your HLF installation machine.
* Use these commands to install and instantiate the chaincode: 
```
$ docker exec -it cli bash
$ cd .. && cd chaincode
$ peer chaincode install -p sss-chaincode -n sss-chaincode -v 1.0
$ peer chaincode instantiate -n sss-chaincode -c '{"Args":["a","10"]}' -C mychannel -v 1.0
```

## Usage
In order to use the Secure State Sharing chaincode please install the Secure [State Sharing client](https://github.com/far-edge/DistributedLedger/tree/develop/secure-state-sharing/client).
