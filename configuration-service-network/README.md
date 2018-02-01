## Start the Hyperledger Fabric Configuration Service Network (1 Org, 4 peers, orderer with 4 kafka)

* `chmod -R +x ./*.sh`
* `sudo bash startNetwork.sh`
* `docker ps -a` Show the network
* Point your browser to **http://YOUR_HOST_ADDRESS:8080** to look at [Blockchain Explorer](https://github.com/hyperledger/blockchain-explorer)

## Stop the Configuration Service Network
* `sudo bash stopNetwork.sh`


*PS: Commands tested with Ubuntu 16.04*