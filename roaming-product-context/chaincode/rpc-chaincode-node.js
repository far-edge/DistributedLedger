const shim = require('fabric-shim');
const datatransform = require('./utils/datatransform');
var logger = shim.newLogger('Roaming-Product-Context-Logger');
logger.level = 'debug';

var rpcChaincode = class {
  async Init(stub) {
    logger.info(' Roaming-Product-Context-Init ');
    return shim.success(Buffer.from('Init - OK!'));
  }

  async Invoke(stub) {
    logger.info(' Roaming-Product-Context-Invoke ');
    let ret = stub.getFunctionAndParameters();
    let fcn = ret.fcn;
    let args = ret.params;
    logger.info('getFunctionAndParameters:' + ret);
    logger.info('do this fuction:' + fcn);
    logger.info(' List of args: ' + args);

    if (fcn === 'release') {
      return this.release(stub, args);
    }
    if (fcn === 'acquire') {
      return this.acquire(stub, args);
    }
    if (fcn === 'dispose') {
      return this.dispose(stub, args);
    }
    logger.error('Error...probably wrong name of fuction!!!' + fcn);
    return shim.error(Buffer.from('500'));
  }

  async generateKey(stub, id) {
    logger.info('####generateKey#### ');
    if (typeof id === 'undefined' || id == null) {
      return shim.error(Buffer.from('500'));
    }
    return stub.createCompositeKey('FE_RPC', [id]);
  }
  /* 
  NGSI_ENTITY{
    Id;
    Type;
    rpcStatus;
    Payload
  }
  */
  /*This call receives the ID of an NGSI entity that currently exists on the local OCB. 
  If successful, it copies the NGSI entity to the global DL as a sealed object, identified by the ID. 
  Note: the global DL can either contain no sealed object with the same ID or a matching sealed 
  object which is not in “locked” state (see Acquire); in the latter case, the old sealed object 
  is logically deleted and replaced with the new one.*/
  async release(stub, args) {
    logger.info('--------release-------');

    try {
      let rpcEntity = JSON.parse(args[0]);
      if (
        typeof rpcEntity == 'undefined' ||
        rpcEntity == null ||
        typeof rpcEntity != 'object'
      ) {
        logger.error('Entity undefined or null!');
        return shim.error(Buffer.from('500'));
      }
      const entity = rpcEntity;
      const key = await this.generateKey(stub, entity.id);
      try {
        const entityBytesResp = await stub.getState(key);
        const entityResp = datatransform.Transform.bufferToObject(
          entityBytesResp
        );
        if (entityResp.rpcStatus === 'locked') {
          logger.error('release - WARNING : entity locked!');
          return shim.error(Buffer.from('409'));
        } else {
          try {
            entity.rpcStatus = 'sealed';
            await stub.putState(key, Buffer.from(JSON.stringify(entity)));
            let entityResp = {
              entity: entity,
              responseCode: '204'
            };
            logger.info('release - Entity STORED	with key: ' + key);
            return shim.success(Buffer.from(JSON.stringify(entityResp)));
          } catch (e) {
            logger.error('release - ERROR CATCH (putState): ' + e);
            return shim.error(Buffer.from('500'));
          }
        }
      } catch (e) {
        logger.info(
          'release - No Entity with key: ' + key + '...start putState...'
        );
        try {
          entity.rpcStatus = 'sealed';
          await stub.putState(key, Buffer.from(JSON.stringify(entity)));
          let entityResp = {
            entity: entity,
            responseCode: '201'
          };
          logger.info('release - Entity STORED	with key: ' + key);
          return shim.success(Buffer.from(JSON.stringify(entityResp)));
        } catch (e) {
          logger.error('release - ERROR CATCH (putState): ' + e);
          return shim.error(Buffer.from('500'));
        }
      }
    } catch (e) {
      logger.error('release - ERROR CATCH (JSON.parse): ' + e);
      return shim.error(Buffer.from('500'));
    }
  }
  /*This call receives the ID of a sealed object that currently exists on the global DL. 
  If successful, it copies the sealed object as an NGSI entity with the same ID on the local OCB;
  at the same time, it marks it as “locked” on the global DL.
  Note: the sealed object on the global DL cannot be already in “locked” state.*/
  async acquire(stub, args) {
    logger.info('--------acquire-------');
    if (args[0] == null | args[0] === 'undefined') {
      logger.error('acquire - ERROR entity ID empty or null!');
      return shim.error(Buffer.from('500'));
    }
      let id = args[0];
      const key = await this.generateKey(stub, id);
      try {
        const entityBytesResp = await stub.getState(key);
        const entityResp = datatransform.Transform.bufferToObject(
          entityBytesResp
        );
        if (entityResp.rpcStatus === 'locked') {
          logger.error('acquire - ERROR : Entity locked!');
          return shim.error(Buffer.from('409'));
        }
        try {
          entityResp.rpcStatus = 'locked';
          logger.info("    KEY    :   "  +key);
          await stub.putState(key.toString(), Buffer.from(JSON.stringify(entityResp)));
          logger.info('acquire - putState complete...');
          logger.info('...      Entity  -->    ' + JSON.stringify(entityResp));
          return shim.success(Buffer.from(JSON.stringify(entityResp)));
        } catch (e) {
          logger.error('acquire - ERROR CATCH (putState): ' + e);
          return shim.error(Buffer.from('500'));
        }
      } catch (e) {
        logger.error('acquire - ERROR CATCH (getState): ' + e);
        return shim.error(Buffer.from('409'));
      }
  }

  /*This call receives the ID of a sealed object that currently exists on the global DL. 
  If successful, it performs a logical deletion (i.e., the sealed object disappears 
  from the global DL to all practical effects, but is maintained in the history log).*/
  async dispose(stub, args) {
    logger.info('--------dispose-------');

    if (args[0] == null | args[0] === 'undefined') {
      logger.error('dispose - ERROR entity ID empty or null!');
      return shim.error(Buffer.from('500'));
    }
    try {
      let id = args[0];
      const key = await this.generateKey(stub, id);
      try {
        const promiseDelete = await stub.deleteState(key);
        logger.info('dispose - deleteState complete!');
        return shim.success(Buffer.from('204'));
      } catch (e) {
        logger.info('dispose - ERROR CATCH (deleteState): ' + e);
        return shim.error(Buffer.from('409'));
      }
    } catch (e) {
      logger.info('dispose - ERROR CATCH (generateKey): ' + e);
      return shim.error(Buffer.from('500'));
    }
  }
};
shim.start(new rpcChaincode());