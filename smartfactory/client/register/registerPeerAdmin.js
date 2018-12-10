'use strict';

var Fabric_Client = require('fabric-client');
var Fabric_CA_Client = require('fabric-ca-client');
var path = require('path');
var util = require('util');
var os = require('os');

var argv = require('yargs')
.usage('Usage: $0 -e [string] -a [string] -h [string] -m [string]  -o [string]')
.alias('e','enrollmentId')
.alias('a','affiliation')
.alias('h','host')
.alias('m','mspid')
.alias('o','attrs')
.describe('e', 'Enter peerAdmin identifier: peerAdminUser')
.describe('a', 'Enter affiliation default: org1.department1')
.describe('h', 'Enter CA Server Host default: http://localhost:7054')
.describe('m', 'Enter MSPID name default: Org1MSP')
.describe('o', 'Enter attrs name default: ""')
.demandOption(['e'])
.argv;
//
var fabric_client = new Fabric_Client();
var fabric_ca_client = null;
var admin_user = null;
var member_user = null;
var store_path = path.join(os.homedir(), 'hfc-key-store');
console.log(' Store path:' + store_path);

var userId = argv.e;
var affiliationCode = argv.a || 'org1.department1';
var host = argv.h || 'http://localhost:7054';
var mspid = argv.m || 'Org1MSP';
var attrs = argv.o || '';
var type = 'peer';

userId = userId.trim();
affiliationCode = affiliationCode.trim();
host = host.trim();
mspid = mspid.trim();
attrs = attrs.trim() || '';

console.log('PeerAdmin Id given is: ' + argv.e);
console.log('Affiliation code given is: ' + argv.a);
console.log('CA Server HOST: '+ host);
console.log('MSPID name: '+ mspid);
console.log('attrs: '+ attrs);

// create the key value store as defined in the fabric-client/config/default.json 'key-value-store' setting


Fabric_Client.newDefaultKeyValueStore({ path: store_path
}).then((state_store) => {
    // assign the store to the fabric client
    fabric_client.setStateStore(state_store);
    var crypto_suite = Fabric_Client.newCryptoSuite();
    // use the same location for the state store (where the users' certificate are kept)
    // and the crypto store (where the users' keys are kept)
    var crypto_store = Fabric_Client.newCryptoKeyStore({path: store_path});
    crypto_suite.setCryptoKeyStore(crypto_store);
    fabric_client.setCryptoSuite(crypto_suite);
    var	tlsOptions = {
    	trustedRoots: [],
    	verify: false
    };
    // be sure to change the http to https when the CA is running TLS enabled
    fabric_ca_client = new Fabric_CA_Client(host, null , '', crypto_suite);

    // first check to see if the admin is already enrolled
    return fabric_client.getUserContext('admin', true);
}).then((user_from_store) => {
    if (user_from_store && user_from_store.isEnrolled()) {
        console.log('Successfully loaded admin from persistence');
        admin_user = user_from_store;
    } else {
        throw new Error('Failed to get admin.... run registerAdmin.js');
    }
    // at this point we should have the admin user
    // first need to register the user with the CA server
    var registerData = {enrollmentID: userId, affiliation: affiliationCode};
    if (attrs)
        registerData = {enrollmentID: userId, affiliation: affiliationCode, attrs: attrs, type: type};
    return fabric_ca_client.register(registerData, admin_user);
}).then((secret) => {
    // next we need to enroll the user with CA server
    console.log('Successfully registered "'+userId+'" - secret: "'+ secret+'"');

    return fabric_ca_client.enroll({enrollmentID : userId, enrollmentSecret: secret});
}).then((enrollment) => {
  console.log('Successfully enrolled member user  "' + userId + '"');
  return fabric_client.createUser(
     {username: userId,
     mspid: mspid ,
     cryptoContent: { privateKeyPEM: enrollment.key.toBytes(), signedCertPEM: enrollment.certificate }
     });
}).then((user) => {
     member_user = user;
     return fabric_client.setUserContext(member_user);
}).then(()=>{
     console.log('"'+userId+'" was successfully registered and enrolled and is ready to interact with the fabric network\n');
}).catch((err) => {
    console.error('Failed to register: ' + err);
	if(err.toString().indexOf('Authorization') > -1) {
		console.error('Authorization failures may be caused by having admin credentials from a previous CA instance.\n' +
		'Try again after deleting the contents of the store directory '+store_path);
	}
});