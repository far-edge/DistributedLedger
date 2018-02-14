## Start the Hyperledger Fabric Configuration Service Network
### Prerequisites
* Access to internet.
* Administrative access to the machine with `sudo`.
* [Docker](https://www.digitalocean.com/community/tutorials/how-to-install-and-use-docker-on-ubuntu-16-04) correctly installed.
### Network Topology:
1 Org, 4 peers, 1 orderer with 4 [Kafka](https://kafka.apache.org/) instances and 3 [Zookeeper](https://zookeeper.apache.org/) instances.
## Start the Configuration Service Network
```bash
cd configuration-service-network
sudo chmod -R +x ./*.sh && sudo chmod +x ./bin/* && sudo chmod +x ./scripts/script.sh
sudo bash startNetwork.sh
docker ps -a
```
Point your browser to http://<your_host_address>:8080 to look at [Blockchain Explorer](https://github.com/hyperledger/blockchain-explorer)

## Stop the Configuration Service Network
`sudo bash stopNetwork.sh`


### Troubleshooting
If you can't copy `the crypto-config` folder with error `Permission denied`:
```bash
sudo chown -R $(whoami) crypto-config
```

*PS: Commands tested with Ubuntu 16.04*
