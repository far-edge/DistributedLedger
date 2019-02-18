const shim = require("fabric-shim");
const datatransform = require("./utils/datatransform");
var logger = shim.newLogger("Roaming-Product-Context-Logger");
logger.level = "debug";

var rpcChaincode = class {
  async Init(stub) {
    logger.info(" Roaming-Product-Context-Init ");
    return shim.success(Buffer.from("Init - OK!"));
  }

  async Invoke(stub) {
    logger.info(" Roaming-Product-Context-Invoke ");

    let ret = stub.getFunctionAndParameters();
    let fcn = ret.fcn;
    let args = ret.params;

    logger.info("getFunctionAndParameters:" + ret);
    logger.info("do this fuction:" + fcn);
    logger.info(" List of args: " + args);

    if (fcn === "release") {
      return this.release(stub, args);
    }
    if (fcn === "acquire") {
      return this.acquire(stub, args);
    }
    if (fcn === "dispose") {
      return this.dispose(stub);
    }
    logger.error("Error...probably wrong name of fuction!!!" + fcn);
    return shim.error("Error...probably wrong name of fuction!!!" + fcn);
  }

  generateKey(stub, id) {
    logger.info("####generateKey#### ");
    if (typeof id === "undefined" || id == null) {
      return shim.error("generateKey ERROR: NGSI's ID is empty or null!!");
    }
    return stub.createCompositeKey("FE_RPC", [id]);
  }

  /* 
  NGSI_ENTITY{
    Id;
    Type;
    Status;
    Payload
  }
  */

  /*This call receives the ID of an NGSI entity that currently exists on the local OCB. 
  If successful, it copies the NGSI entity to the global DL as a sealed object, identified by the ID. 
  Note: the global DL can either contain no sealed object with the same ID or a matching sealed 
  object which is not in “locked” state (see Acquire); in the latter case, the old sealed object 
  is logically deleted and replaced with the new one.*/
  async release(stub, args) {
    logger.info("--------release-------");

    try {
      let ngsiEntity = JSON.parse(args);
      if (
        typeof ngsiEntity == "undefined" ||
        ngsiEntity == null ||
        typeof ngsiEntity != "object"
      ) {
        logger.error("Entity undefined or null!");
        return shim.error("Entity undefined or null!");
      }
      const entity = ngsiEntity;
      let key = this.generateKey(stub, entity.Id);
      try {
        entityBytesResp = await stub.getState(key);
        const entityResp = datatransform.Transform.bufferToObject(
          entityBytesResp
        );
        if (entityResp.Status === "locked") {
          logger.console.warn("release - WARNING : entity locked!");
        } else {
          try {
            entity.Status = "sealed";
            await stub.putState(key, Buffer.from(JSON.stringify(entity)));
            logger.info("release - Entity STORED	with key: " + key);
            return shim.success(Buffer.from("release - Store succeed"));
          } catch (e) {
            logger.error("release - ERROR CATCH (putState): " + e);
            return shim.error(e);
          }
        }
      } catch (e) {
        logger.info(
          "release - No Entity with key: " + key + "...start putState..."
        );
        try {
          entity.Status = "sealed";
          await stub.putState(key, Buffer.from(JSON.stringify(entity)));
          logger.info("release - Entity STORED	with key: " + key);
          return shim.success(Buffer.from("release - Store succeed"));
        } catch (e) {
          logger.error("release - ERROR CATCH (putState): " + e);
          return shim.error(e);
        }
      }
    } catch (e) {
      logger.error("release - ERROR CATCH (JSON.parse): " + e);
      return shim.error("Parse error found");
    }
  }

  /*This call receives the ID of a sealed object that currently exists on the global DL. 
  If successful, it copies the sealed object as an NGSI entity with the same ID on the local OCB;
  at the same time, it marks it as “locked” on the global DL.
  Note: the sealed object on the global DL cannot be already in “locked” state.*/
  async acquire(stub, args) {
    logger.info("--------acquire-------");
    /*try {
      let ngsiEntity = JSON.parse(args);
      if (
        typeof ngsiEntity == "undefined" ||
        ngsiEntity == null ||
        typeof ngsiEntity != "object"
      ) {
        return shim.error("ngsiEntity undefined or null!");
      }
      const entity = ngsiEntity;
      */
    let key = this.generateKey(stub, entity.Id);
    try {
      entityBytesResp = await stub.getState(key);
      const entityResp = datatransform.Transform.bufferToObject(
        entityBytesResp
      );
      if (entityResp.Status === "locked") {
        logger.error("acquire - ERROR : Entity locked!");
        return shim.error("acquire - ERROR : Entity locked!");
      }
      try {
        entityResp.Status = "locked";
        await stub.putState(key, Buffer.from(JSON.stringify(entityResp)));
        logger.info("acquire - putState complete...entity is returning...");
        return shim.success(Buffer.from(entityResp));
      } catch (e) {
        logger.error("acquire - ERROR CATCH (putState): " + e);
        return shim.error(e);
      }
    } catch (e) {
      logger.error("acquire - ERROR CATCH (getState): " + e);
      return shim.error(e);
    }
  }

  /*This call receives the ID of a sealed object that currently exists on the global DL. 
  If successful, it performs a logical deletion (i.e., the sealed object disappears 
  from the global DL to all practical effects, but is maintained in the history log).*/
  async dispose(stub, args) {
    logger.info("--------dispose-------");

    try {
      let ngsiEntity = JSON.parse(args);
      if (
        typeof ngsiEntity == "undefined" ||
        ngsiEntity == null ||
        typeof ngsiEntity != "object"
      ) {
        return shim.error("ngsiEntity undefined or null or not object");
      }
      const entity = ngsiEntity;
      let key = this.generateKey(stub, entity.Id);
      try {
        promiseDelete = await stub.deleteState(key);
        return shim.success(Buffer.from("dispose - Delete succeed"));
      } catch (e) {
        logger.info("release - ERROR CATCH (deleteState): " + e);
        return shim.error(e);
      }
    } catch (e) {
      logger.info("release - ERROR CATCH (JSON.parse): " + e);
      return shim.error("Parse error found");
    }
  }
};
shim.start(new rpcChaincode());
