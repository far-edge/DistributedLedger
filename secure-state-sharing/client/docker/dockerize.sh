#!/bin/bash

if [ "$1" == "" ]; then
	echo "Insert the version of your package: '1.0.0'"
	exit 0
fi
rm -rf ../resources
mv resources ../resources 
docker build -t faredge/secure-state-sharing:$1 .

