//clod16 chaincode

const shim = require("fabric-shim");
const datatransform = require("./utils/datatransform");
var logger = shim.newLogger("testChaincode");

logger.level = "debug";

var Chaincode = class {
    async Init(stub) {
        logger.debug("________Init________");
        return shim.success(Buffer.from("Init - OK!"));
    }

    async Invoke(stub) {
        logger.info("________Invoke________");
        let ret = stub.getFunctionAndParameters();
        let fcn = ret.fcn;
        let args = ret.params;

        logger.info("do this fuction:" + fcn);
        logger.info(" List of args: " + args);

        //list of methods

        if (fcn === "get") {
            return this.edit(stub, args);
        }
        if (fcn === "post") {
            return this.edit(stub, args);
        }
        if (fcn === "put") {
            return this.put(stub, args);
        }
        if (fcn === "delete") {
            return this.delstate(stub, args);
        }

        logger.error("Error...probably wrong name of fuction!!!" + fcn);
        return shim.error("Error...probably wrong name of fuction!!!" + fcn);
    }

    async get(stub, args) {
        logger.debug("________get()________");
        let getbytes = null;
        if (args.length != 1) {
            logger.error("Number of argument is wrong, expected one!!");
            return shim.error("Number of argument is wrong, expected one!!");
        }
        try {
            getbytes = await stub.getState(args[0]);
            if (!getbytes) {
                logger.error(" Data with key" + args[0] + " not found!!!");
                return shim.error(" Data with key" + args[0] + " not found!!!");
            }
            const stringGet = datatransform.Transform.bufferToString(
                Buffer.from(getbytes)
            );
            logger.debug("get() extract: " + getbytes);
            return shim.success(Buffer.from(stringGet));
        } catch (e) {
            logger.error("get() - ERROR CATCH: " + e);
            return shim.error("get() - Failed to get state with key: " + key);
        }
    }
    async delete(stub, args) {
        logger.debug("________delete()________");
        if (args.length == 1) {
            try {
                await stub.deleteState(args[0]);
                logger.debug("delete() - successfull!!");
                return shim.success(Buffer.from("delete - Store successfull"));
            } catch (e) {
                logger.info("delete() - ERROR CATCH (edit): " + e);
                return shim.error(e);
            }
        } else {
            logger.error("Argument wrong, aspected exactly one argument!!");
            return shim.error("Argument wrong, aspected exactly one argument!!");
        }
    }
    async edit(stub, args) {
        logger.debug("________edit()________");
        if (args.length == 2) {
            try {
                await stub.putState(args[0], Buffer.from(args[1]));
                logger.debug("edit payload:" + args[1]);
                logger.debug("edit - successfull!!");
                return shim.success(Buffer.from("edit - Store successfull"));
            } catch (e) {
                logger.info("edit - ERROR CATCH (edit): " + e);
                return shim.error(e);
            }
        } else {
            logger.error("Argument wrong, aspected exactly two argument!!");
            return shim.error("Argument wrong, aspected exactly two argument!!");
        }
    }
};

shim.start(new Chaincode());
