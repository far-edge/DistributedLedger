## Start the Hyperledger Fabric Configuration Network v1.1.0
### Prerequisites
* Access to internet.
* Administrative access to the machine with `sudo`.
* [Docker](https://www.digitalocean.com/community/tutorials/how-to-install-and-use-docker-on-ubuntu-16-04) correctly installed.
### Network Topology:
1 Org, 4 peers, 1 orderer with 2 [Kafka](https://kafka.apache.org/) instances and 1 [Zookeeper](https://zookeeper.apache.org/) instances.
## Initialize the Configuration Service Network
```bash
cd configuration-network-fabric
sudo chmod -R +x ./*.sh && sudo chmod +x ./bin/* && sudo chmod +x ./scripts/*.sh
sudo bash initNetwork.sh
docker ps -a
```
Point your browser to `http://<your_host_address>:8180` to look at [Blockchain Explorer](https://github.com/hyperledger/blockchain-explorer) or at `http://<your_host_address>:8080` for [Hyperledger Composer](https://hyperledger.github.io/composer/).

## Stop the Configuration Service Network
`sudo bash stopNetwork.sh`
## Restart the Configuration Service Network
`sudo bash restartNetwork.sh`

### Troubleshooting
If you can't copy `the crypto-config` folder with error `Permission denied`:
```bash
sudo chown -R $(whoami) crypto-config
```

## Chaincode
Chaincode folder contains the smartfactory chaincode installed at network startup.






*PS: Commands tested with Ubuntu 16.04*
