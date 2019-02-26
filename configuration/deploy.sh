#!/bin/bash
CHANNEL='ledgerchannel'
VERSION='1.0'
LANG=''
OPERATION='instantiate'
PERCORSO='github.com/hyperledger/fabric/examples/chaincode/go/'$1
PERCORSO_NODE='/opt/gopath/src/github.com/hyperledger/fabric/chaincode/'

if [ "$1" == "" ]; then
	echo "Insert the name of your chaincode ex: 'chaincode-name'"
	echo "Usage ./deploy.sh <name of chaincode> <version of chaincode ex: '1.0'>  <upgrade or instantiate> <language type ex: 'Node'>"
	echo "Usage ./deploy.sh your-chaincode 1.0 instantiate Node >"

	exit 0
fi
if [ "$2" != "" ]; then
	VERSION=$2
fi
if [ "$4" != "" ]; then
	LANG='-l Node'
    PERCORSO=$PERCORSO_NODE$1
fi
if [ "$3" != "" ]; then
	OPERATION='upgrade'
fi

echo "Install chaincode on peer0 from $PWD"
echo "command: peer chaincode install -n $1 -v $VERSION -p $PERCORSO $LANG"
sleep 10
docker exec -it cli peer chaincode install -n $1 -v $VERSION -p $PERCORSO $LANG
sleep 10 
echo "Instantiate chaincode"
docker exec -it cli peer chaincode $OPERATION -n $1 -c '{"Args":["a","10"]}' -C $CHANNEL -v $VERSION
