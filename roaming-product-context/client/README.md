# RPC Service
The **RPC Web Service** Provides a local Web API that interacts with both local OCB instance and the global RPC Chaincode. Three service endpoints are available to clients: Release, Acquire and Dispose.

## Installation
### Prerequisites
* Linux Environment.
* Administrative access to the machine.
* Access to Internet.
* [Node](https://nodejs.org/en/download/) and pm correctly installed (version 8.x or greater)
* Docker and Docker Compose correctly installed and working.
* Access to a HLF v1.3 system and availability of an administrator that will provide the required artifacts (digital certificate and configuration file) for connecting the OCB Proxy to the HLF network.
* Clone the repository: `git clone https://github.com/far-edge/DistributedLedger.git && cd roaming-product-context`

### Installation Guide
#### HLF Administrator
* Point to the `chaincode` folder.
*	Deploy the Node.js Chaincode in the **HLF 1.3** Network.
*	Give users, **the digital certificates and a configuration file** in order to access to the Chaincode and Network Installation.
#### User
* Open a Terminal Console on your system. 
*	Point to the `client` folder.
*	Copy the `config-fabric-network.json` given by your HLF administrator under the folder `docker/resources`.
*	Copy the digital certificates given by your HLF administrator, under the `docker/resources/crypto-config` folder.
*	Create the Docker artifact containing the OCB + OCB Proxy with the command:  
    `npm run docker`.
*	Launch the system with the command: `./start.sh`. 



## Usage
<br/>The RPC Service is now available on port `4026` of your environment.

### The RPC Web Service API

<table><thead><tr class="header"><th>Title</th><th>Release</th></tr></thead><tbody><tr class="odd"><td>Description</td><td><p>This call receives the ID of an NGSI entity that currently exists on the local OCB. If successful, it copies the NGSI entity to the global DL as a sealed object, identified by the ID; at the same time, it deletes it from the local OCB.</p><p>Note: the global DL can either contain <em>no</em> sealed object with the same ID or a matching sealed object which is <em>not</em> in “locked” state (see Acquire); in the latter case, the old sealed object is logically deleted and replaced with the new one.</p></td></tr><tr class="even"><td>URL</td><td>/rpc/v1/entities</td></tr><tr class="odd"><td>Method</td><td>POST</td></tr><tr class="even"><td>URL Params</td><td>id=[string]</td></tr><tr class="odd"><td>Success Response</td><td><p>Code: 201 CREATED (a new sealed object was created)</p><p>Code: 204 NO CONTENT (an existing sealed object was released)</p></td></tr><tr class="even"><td>Error Responses</td><td><p>Code: 400 BAD REQUEST (invalid format of request)</p><p>Code: 409 CONFLICT (any of the following:</p><blockquote><p>no matching entity exists on OCB</p><p>a matching sealed object in “locked” state exists in the DL)</p></blockquote><p>Code: 500 INTERNAL SERVER ERROR (any other error)</p></td></tr></tbody></table>

<table><thead><tr class="header"><th>Title</th><th>Acquire</th></tr></thead><tbody><tr class="odd"><td>Description</td><td><p>This call receives the ID of a sealed object that currently exists on the global DL. If successful, it copies the sealed object as an NGSI entity with the same ID on the local OCB; at the same time, it marks it as “locked” on the global DL.</p><p>Note: the sealed object on the global DL <em>cannot</em> be already in “locked” state.</p></td></tr><tr class="even"><td>URL</td><td>/rpc/v1/entities</td></tr><tr class="odd"><td>Method</td><td>PUT</td></tr><tr class="even"><td>URL Params</td><td>id=[string]</td></tr><tr class="odd"><td>Success Response</td><td>Code: 204 NO CONTENT</td></tr><tr class="even"><td>Error Responses</td><td><p>Code: 400 BAD REQUEST (invalid format of request)</p><p>Code: 409 CONFLICT (any of the following:</p><blockquote><p>no matching sealed object exists on the DL</p><p>the matching sealed object on the DL is in “locked” state</p><p>a matching entity already exists on OCB)</p></blockquote><p>Code: 500 INTERNAL SERVER ERROR (any other error)</p></td></tr></tbody></table>

<table><thead><tr class="header"><th>Title</th><th>Dispose</th></tr></thead><tbody><tr class="odd"><td>Description</td><td>This call receives the ID of a sealed object that currently exists on the global DL. If successful, it performs a logical deletion (i.e., the sealed object disappears from the global DL to all practical effects, but is maintained in the history log).</td></tr><tr class="even"><td>URL</td><td>/rpc/v1/entities/{id}</td></tr><tr class="odd"><td>Method</td><td>DELETE</td></tr><tr class="even"><td>URL Params</td><td></td></tr><tr class="odd"><td>Success Response</td><td>Code: 204 NO CONTENT</td></tr><tr class="even"><td>Error Responses</td><td><p>Code: 400 BAD REQUEST (invalid format of request)</p><p>Code: 409 CONFLICT (no matching sealed object exists on the DL)</p><p>Code: 500 INTERNAL SERVER ERROR (any other error)</p></td></tr></tbody></table>
