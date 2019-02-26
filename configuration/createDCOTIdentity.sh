#!/bin/bash

ORG_NAME="org1.example.com"
TYPE="user"
PASSWORD="faredge2018"
#ATTRS='"hf.Registrar.Roles=peer,client,user"'
UUID=$(cat /proc/sys/kernel/random/uuid)
DCOT_ROLE="dcot-user"
BASEDIR=$(pwd)

if [ "$1" == "" ]; then
	echo "Insert the 'name' of the user to create"
	echo "Usage ./createIdentity <name> <dcot-user/dcot-operator> <password> <user/peer> <uid>"
	exit 0
fi

if [ "$2" != "" ]; then
	DCOT_ROLE=$2
fi

if [ "$3" != "" ]; then
	PASSWORD=$3
fi

if [ "$4" != "" ]; then
	TYPE=$4
fi

if [ "$5" != "" ]; then
	UUID=$5
fi
ATTRS="role=$DCOT_ROLE:ecert,uid=$UUID:ecert"

# id.maxenrollments -1 infinite
docker exec -it ca.example.com fabric-ca-client enroll -u http://admin:adminpw@localhost:7054

docker exec -it ca.example.com fabric-ca-client register  --id.name $1  --id.type $TYPE  --id.secret $PASSWORD --id.maxenrollments -1 --id.attrs $ATTRS
docker exec -it ca.example.com  fabric-ca-client enroll -u http://$1:$PASSWORD@ca.example.com:7054 -M crypto-config/peerOrganizations/org1.example.com/msp
echo "UID is $UUID"
echo "UID of $1 is $UUID with role $DCOT_ROLE" >> log_uids.txt

docker stop ca.example.com
sudo cp crypto-config/peerOrganizations/$ORG_NAME/ca/*pem ca/data/ca-cert.pem
sudo cp crypto-config/peerOrganizations/$ORG_NAME/ca/*_sk ca/data/msp/keystore/.

sudo mkdir -p crypto-users/$ORG_NAME/$1 && cp -f crypto-config/peerOrganizations/$ORG_NAME/msp/signcerts/cert.pem crypto-users/$ORG_NAME/$1/ca-cert.pem
sudo mkdir -p crypto-users/$ORG_NAME/$1/keystore && cd crypto-config/peerOrganizations/$ORG_NAME/msp/keystore && cp -f $(ls -t | head -1) $BASEDIR/crypto-users/$ORG_NAME/$1/keystore
sudo mkdir -p $BASEDIR/crypto-users/archives && cd $BASEDIR && tar -zcvf crypto-users/archives/$1.tar.gz crypto-users/$ORG_NAME/$1
docker start ca.example.com