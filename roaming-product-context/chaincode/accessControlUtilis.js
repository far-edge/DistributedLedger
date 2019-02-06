"use strict";
Object.defineProperty(exports, "__esModule", {
    value: true
});
const shim = require("fabric-shim");
var logger = shim.newLogger("SSS-Chaincode");

class AccessControl {


    static getTxID(stub) {
        try {
            let callerID = stub.getTxID();
            return callerID;
        } catch (e) {
            console.error("Error getTxID()...");
            return shim.error(e);

        }
    }
}

exports.AccessControl = AccessControl;