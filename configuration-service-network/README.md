## Start the Hyperledger Fabric Configuration Service Network
### Network Topology: 1 Org, 4 peers, 1 orderer with 4 [kafka](https://kafka.apache.org/) instances and 3 [Zookeeper](https://zookeeper.apache.org/) instances.
* `cd configuration-service-network`
* `chmod -R +x ./*.sh`
* `sudo bash startNetwork.sh`
* `docker ps -a` show the network
* Point your browser to **http://YOUR_HOST_ADDRESS:8080** to look at [Blockchain Explorer](https://github.com/hyperledger/blockchain-explorer)

## Stop the Configuration Service Network
* `sudo bash stopNetwork.sh`


*PS: Commands tested with Ubuntu 16.04*
