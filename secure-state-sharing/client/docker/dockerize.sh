#!/bin/bash

if [ "$1" == "" ]; then
	echo "Insert the version of your package: '1.0.0'"
	exit 0
fi
rm -rf ../resources
mv resources ../resources 
rm -rf ../resources_1
mv resources_1 ../resources_1
docker build -t faredge/secure-state-sharing:$1 .

