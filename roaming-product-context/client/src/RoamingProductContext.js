const _ = require('underscore');
const BlockchainHandler = require('./lib/BlockchainHandler');
const OrionHandler = require('./lib/OrionHandler');
const LoggerManager = require('./lib/LoggerManager');


class RoamingProductContext {
  constructor() {
    this.blockchainHandler = new BlockchainHandler();
    this.orionHandler = new OrionHandler();
    this.loggerManager = new LoggerManager();

    this.TYPE = 'type';
    this.reqStates = Object.freeze({
      BAD_REQUEST: {
        name: 'BAD_REQUEST',
        status: '400',
      },
      NO_ENTIY_EXISTS: {
        name: 'NO_ENTIY_EXISTS',
        status: '409',
      },
      LOCKED_STATE_PRESENT: {
        name: 'LOCKED_STATE_PRESENT',
        status: '409',
      },
      INTERNAL_SERVER_ERROR: {
        name: 'INTERNAL_SERVER_ERROR',
        status: '500',
      },
      SUCCESS_CREATED: {
        name: 'SUCCESS_CREATED',
        status: '201',
      },
      SUCCESS_NO_CONTENT: {
        name: 'SUCCESS_CREATED',
        status: '204',
      },
    });
    this.reqStatesMap = new Map([
      ['201', this.reqStates.SUCCESS_CREATED],
      ['204', this.reqStates.SUCCESS_NO_CONTENT],
      ['500', this.reqStates.INTERNAL_SERVER_ERROR],
      ['409', this.reqStates.LOCKED_STATE_PRESENT],
    ]);
  }

  static clearFieldStatus(entity) {
    const entityObj = entity;
    if (entityObj.rpcStatus) {
      delete entityObj.rpcStatus;
    }
    return entityObj;
  }

  // eslint-disable-next-line class-methods-use-this
  validateRequest(req) {
    // Validation of request
    if (_.isEmpty(req)) {
      return this.reqStates.BAD_REQUEST;
    }
    const {
      id,
    } = req.params;
    if (_.isEmpty(id)) {
      return this.reqStates.BAD_REQUEST;
    }
    return null;
  }

  async acquire(req) {
    try {
      // Presence in OCB
      const {
        id,
      } = req.params;
      const sealedEntity = await this.blockchainHandler.executeOperation(id,
        this.blockchainHandler.chaincodeOperation.ACQUIRE, true);
      if (_.isEmpty(sealedEntity)) {
        return this.reqStates.NO_ENTIY_EXISTS;
      }
      try {
        const sealedEntityObj = JSON.parse(sealedEntity);
        await this.orionHandler.executeOperation(RoamingProductContext
          .clearFieldStatus(sealedEntityObj),
        req.method);
        return this.reqStates.SUCCESS_NO_CONTENT;
      } catch (error) {
        this.loggerManager.logger.error(`Error coming from Blockchain: ${error}`);
        return this.reqStatesMap.get(error);
      }
    } catch (error) {
      this.loggerManager.logger.error(error);
      return this.reqStates.INTERNAL_SERVER_ERROR;
    }
  }

  async dispose(req) {
    try {
      const {
        id,
      } = req.params;
      try {
        await this.blockchainHandler.executeOperation(id,
          this.blockchainHandler.chaincodeOperation.DISPOSE, true);
        return this.reqStates.SUCCESS_NO_CONTENT;
      } catch (error) {
        this.loggerManager.logger.error(`Error coming from Blockchain: ${error}`);
        return this.reqStatesMap.get(error);
      }
    } catch (error) {
      this.loggerManager.logger.error(error);
      return this.reqStates.INTERNAL_SERVER_ERROR;
    }
  }


  async release(req) {
    try {
      const {
        id,
      } = req.params;
      const ocbEntity = await this.orionHandler.getEntity(id, this.TYPE);
      if (_.isEmpty(ocbEntity)) {
        return this.reqStates.NO_ENTIY_EXISTS;
      }
      try {
        const entityResponseCode = await this.blockchainHandler.executeOperation(ocbEntity,
          this.blockchainHandler.chaincodeOperation.RELEASE, false);
        const {
          entity,
          responseCode,
        } = JSON.parse(entityResponseCode);
        if (this.reqStatesMap.get(entity)) {
          await this.orionHandler.deleteEntity(entity.id, entity.type);
        }
        return this.reqStatesMap.get(responseCode);
      } catch (error) {
        this.loggerManager.logger.error(`Error coming from Blockchain: ${error}`);
        return this.reqStatesMap.get(error);
      }
    } catch (error) {
      this.loggerManager.logger.error(error);
      return this.reqStates.INTERNAL_SERVER_ERROR;
    }
  }
}


module.exports = RoamingProductContext;
