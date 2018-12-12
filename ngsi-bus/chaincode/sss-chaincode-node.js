//clod16 chaincode

//var flatten = require('flat')
const shim = require("fabric-shim");
const datatransform = require("./utils/datatransform");
var logger = shim.newLogger("SSS-Chaincode");

logger.level = "debug";

var Profile = Object.freeze({
  "fabric-user": "",
  Admin: "admin",
  User1: ""
});

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
    logger.info("getFunctionAndParameters:" + ret);

    logger.info("do this fuction:" + fcn);
    logger.info(" List of args: " + args);

    let argument = stub.getArgs();
    logger.info("getArgs:" + argument);

    //list of methods

    if (fcn === "putEntity") {
      return this.putEntity(stub, args);
    }
    if (fcn === "getEntity") {
      return this.getEntity(stub, args);
    }
    if (fcn === "updateEntity") {
      return this.updateEntity(stub, args);
    }
    if (fcn === "deleteEntity") {
      return this.deleteEntity(stub, args);
    }
    if ( fcn === "migrate") {
      return this.migrate(stub);
    }
    /*  if (fcn === "putPrivateEntity") {
      return this.putPrivateEntity(stub, args);
    }
    if (fcn === "getPrivateEntity") {
      return this.getPrivateEntity(stub, args);
    } */
    logger.error("Error...probably wrong name of fuction!!!" + fcn);
    return shim.error("Error...probably wrong name of fuction!!!" + fcn);
  }

  async migrate(stub){
    logger.debug("___migrate___");

    let entityGetbytes = null;
    let arg = new Array();
    arg = [];
    try {
      entityGetbytes = await stub.getStateByPartialCompositeKey("FE_SSS", arg);
      if (!entityGetbytes) {
        return shim.error(" getState() Error: retrive empty data!!!");
      }
      const strings = await  datatransform.Transform.iteratorToList(entityGetbytes);
      logger.debug("getEntity extract: " + strings);
      //stub.setEvent("FE_SSS_MIGRATE", Buffer.from(stringGet));
      return shim.success(Buffer.from(JSON.stringify(strings)));
    } catch (e) {
      logger.error("migrate() - ERROR CATCH: " + e);
      return shim.error("migrate() - ERROR CATCH!!" + e);
    }
  }

  async deleteEntity(stub, args) {
    logger.debug("___deleteEntity___");
    let promiseDelete = null;
    if (args.length != 2) {
      return shim.error("Number of argument is wrong, expected two!!");
    }
    let keySSS = stub.createCompositeKey("FE_SSS", [args[0], args[1]]);

    try {
      logger.info("Deleting entity...");
      promiseDelete = await stub.deleteState(keySSS);
      if (!promiseDelete) {
        return shim.error("stub.deleteState(): no entity with key: " + keySSS);
      }

      //let eventString = args[0] + args[1];
      let eventKey = {
        id:args[0],
        type:args[1]
      }
      stub.setEvent("FE_SSS_DELETE-ENTITY", Buffer.from(JSON.stringify(eventKey)));

      return shim.success(Buffer.from(promiseDelete));
    } catch (e) {
      let revertEntityByte = await stub.getState(keySSS);
      logger.error("deleteEntity - ERROR CATCH: " + e);
      return shim.error(datatransform.Transform.bufferToString(revertEntityByte));
    }
  }

  async updateEntity(stub, args) {
    logger.debug("___updateEntity___");
    if (args.length == 1) {
      try {
        //logger.info("args:" +args[0])
        let entityInput = JSON.parse(args[0]);
        if (
          typeof entityInput == "undefined" ||
          entityInput == null ||
          typeof entityInput != "object"
        ) {
          return shim.error("entityInput undefined or null or not object");
        }
        logger.info("Entity parsed:" + entityInput);
        //const entity = entityInput;

        try {
          let keySSS = stub.createCompositeKey("FE_SSS", [
            entityInput.id,
            entityInput.type
          ]);
          logger.info("keySSS:" + keySSS);
          let entityGetbytes = await stub.getState(keySSS);
          if (!entityGetbytes) {
            return shim.error(" Entity with key" + keySSS + " not found!!!");
          }
          const entityString = datatransform.Transform.bufferToString(
            entityGetbytes
          );
          let entityGetFlat = JSON.parse(entityString);
          //let entityInputFlat = flatten(entityInput);
          //let entityGetFlat = flatten(entityGet);

          for (var field in entityInput) {
            if (
              entityInput.hasOwnProperty(field) !=
              entityGetFlat.hasOwnProperty(field)
            ) {
              return shim.error("updateEntity Error: Incorrect structure!!!");
            }
          }
          logger.info("updateEntity: Correct structure!!");
          logger.info(" Start updating the entity...");
          await stub.putState(keySSS, Buffer.from(JSON.stringify(entityInput)));
          logger.debug("updateEntity - Store successfull!!!");

          stub.setEvent(
            "FE_SSS_PUT-ENTITY",
            Buffer.from(JSON.stringify(entityInput))
          );
          
          logger.info("eventName = FE_SSS_PUT-ENTITY payload: " +JSON.stringify(entityInput));
          return shim.success(
            Buffer.from("updateEntity - Update successfull!")
          );
        } catch (e) {
          let keySSS = stub.createCompositeKey("FE_SSS", [
            entityInput.id,
            entityInput.type
          ]);
          let revertEntityByte = await stub.getState(keySSS);
          logger.error("updateEntity - ERROR CATCH (updateEntity): " + e);
          return shim.error(datatransform.Transform.bufferToString(
            revertEntityByte));
        }
      } catch (e) {
        logger.error("putEntity - ERROR CATCH (JSON.parse()): " + e);
        return shim.error("Parse error found");
      }
    }
  }

  async getEntity(stub, args) {
    logger.debug("___getEntity___");
    let entityGetbytes = null;
    if (args.length != 2) {
      return shim.error("Number of argument is wrong, expected two!!");
    }
    let keySSS = stub.createCompositeKey("FE_SSS", [args[0], args[1]]);

    try {
      entityGetbytes = await stub.getState(keySSS);
      if (!entityGetbytes) {
        return shim.error(" Entity with key" + keySSS + " not found!!!");
      }
      const stringGet = datatransform.Transform.bufferToString(entityGetbytes);
      logger.debug("getEntity extract: " + stringGet);
      //let payload = JSON.parse(stringGet);

      // stub.setEvent("FE_SSS_GET_ENTITY", Buffer.from(stringGet));

      return shim.success(Buffer.from(stringGet));
    } catch (e) {
      logger.error("getEntity - ERROR CATCH: " + e);
      return shim.error("getEntity - Failed to get state with key: " + keySSS);
    }
  }

  async putEntity(stub, args) {
    logger.debug("___putEntity___");
    if (args.length == 1) {
      try {
        logger.info("args:" + args[0]);
        let entityContainer = JSON.parse(args[0]);
        if (
          typeof entityContainer == "undefined" ||
          entityContainer == null ||
          typeof entityContainer != "object"
        ) {
          return shim.error("entityContainer undefined or null or not object");
        }
        logger.info("Entity parsed:" + entityContainer);
        //const entity = entityContainer;

        try {
          logger.info("ID:" + entityContainer.id);
          logger.info("Type:" + entityContainer.type);

          var keySSS = stub.createCompositeKey("FE_SSS", [
            entityContainer.id,
            entityContainer.type
          ]);
          logger.info("keySSS:" + keySSS);

          await stub.putState(
            keySSS,
            Buffer.from(JSON.stringify(entityContainer))
          );
          logger.debug("putEntity payload:" + args[0]);
          logger.debug("putEntity - Store successfull!!");

          stub.setEvent(
            "FE_SSS_POST-ENTITY",
            Buffer.from(JSON.stringify(entityContainer))
          );

          return shim.success(Buffer.from("putEntity - Store successfull!!!"));
        } catch (e) {
          let revertKey = {
            id:entityContainer.id,
            type:entityContainer.type 
          };
          logger.error("putEntity - ERROR CATCH (putEntity): " + e);
          return shim.error(JSON.stringify(revertKey));
        }
      } catch (e) {
        logger.error("putEntity - ERROR CATCH (JSON.parse()): " + e);
        return shim.error("Parse error found");
      }
    } else {
      return shim.error("putEntity ERROR: wrong argument!!");
    }
  }
};

shim.start(new Chaincode());

//PRIVATE DATA

/* async putPrivateEntity(stub, args) {
    logger.debug("___putPrivateEntity___");
    if (args.length == 1) {
      try {
        let entityContainer = JSON.parse(args[0]);
        if (
          typeof entityContainer == "undefined" ||
          entityContainer == null ||
          typeof entityContainer != "object"
        ) {
          return shim.error("entityContainer undefined or null or not object");
        }
        //const entity = entityContainer;

        try {
          var keySSS = stub.createCompositeKey("private", [
            entityContainer.id,
            entityContainer.type
          ]);

          await stub.putPrivateData({
              privateCollection: "entityCollection"
            },
            keySSS,
            Buffer.from(JSON.stringify(entityContainer))
          );
          logger.debug("putPrivateEntity - Store successfull!!");
          return shim.success(
            Buffer.from("putPrivateEntity - Store successfull!!!")
          );
        } catch (e) {
          logger.error(
            "putPrivateEntity - ERROR CATCH (stub.putPrivateEntity()): " + e
          );
          return shim.error(e);
        }
      } catch (e) {
        logger.error("putPrivateEntity - ERROR CATCH (JSON.parse()): " + e);
        return shim.error("Parse error found");
      }
    } else {
      return shim.error("putPrivateEntity ERROR: wrong argument!!");
    }
  }

  async getPrivateEntity(stub, args) {
    logger.debug("___getPrivateEntity___");
    let entityGetbytes = null;
    if (args.length != 2) {
      return shim.error("Number of argument is wrong, expected two!!");
    }
    let keySSS = stub.createCompositeKey("private", [args[0], args[1]]);

    try {
      entityGetbytes = await stub.getPrivateData({
          privateCollection: "carCollection"
        },
        keySSS
      );
      if (!entityGetbytes) {
        return shim.error(" privateEntity with key not found!!!");
      }
      const stringGet = datatransform.Transform.bufferToString(entityGetbytes);
      //let payload = JSON.parse(stringGet);
      return shim.success(Buffer.from(stringGet));
    } catch (e) {
      logger.error("getPrivateEntity - ERROR CATCH: " + e);
      return shim.error(
        "getPrivateEntity - Failed to get state with key: " + keySSS
      );
    }
  } */
