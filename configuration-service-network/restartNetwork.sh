#!/bin/bash
echo "WARNING!!! ALL Fabric Data will be destroyed"
docker volume rm $(docker volume ls)
rm -rf ~/data/* && ./byfn.sh -m up -c ledgerchannel -s couchdb