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

  acquire(req) {
    try {
      // Presence in OCB
      const {
        id,
      } = req.params;
      const sealedEntity = this.blockchainHandler.executeOperation(id,
        this.blockchainHandler.chaincodeOperation.ACQUIRE);
      if (_.isEmpty(sealedEntity)) {
        return this.reqStates.NO_ENTIY_EXISTS;
      }
      try {
        this.orionHandler.executeOperation(sealedEntity, req.method);
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

  dispose(req) {
    try {
      const {
        id,
      } = req.params;
      try {
        this.blockchainHandler.executeOperation(id,
          this.blockchainHandler.chaincodeOperation.DISPOSE);
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


  release(req) {
    try {
      const {
        id,
      } = req.params;
      const ocbEntity = this.orionHandler.getEntity(id, this.TYPE);
      if (_.isEmpty(ocbEntity)) {
        return this.reqStates.NO_ENTIY_EXISTS;
      }
      try {
        const status = this.blockchainHandler.executeOperation(ocbEntity,
          this.blockchainHandler.chaincodeOperation.RELEASE);

        if (this.reqStatesMap.get(status)) {
          this.orionHandler.deleteEntity(id, this.TYPE);
        }
        return this.reqStatesMap.get(status);
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
