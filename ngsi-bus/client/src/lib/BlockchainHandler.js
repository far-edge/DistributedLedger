const nodeLedgerClient = require('node-ledger-client');
const config = require('../../resources/config-fabric-network.json');
const CONFIG = require('../../resources/config.json');

let ledgerClient;

const LoggerManager = require('./LoggerManager');

const loggerManager = new LoggerManager();

const chaincodeOperation = new Map([
  ['POST', 'putEntity'],
  ['PUT', 'updateEntity'],
  ['DELETE', 'deleteEntity'],
  ['GET', 'getEntity'],
  ['MIGRATION', 'migrate'],
]);

const operation = new Map([
  ['POST', 'editEntity'],
  ['PUT', 'editEntity'],
  ['DELETE', 'deleteEntity'],
  ['GET', 'getEntity'],
  ['MIGRATION', 'migrateEntity'],
]);

class BlockchainHandler {
  constructor() {
    const ledger = async () => {
      ledgerClient = await nodeLedgerClient.LedgerClient.init(config);
    };
    ledger();
  }

  async executeOperation(entity, operationType) {
    try {
      const method = this[operation.get(operationType)];
      return await method.call(this, entity, operationType);
    } catch (error) {
      loggerManager.error(error);
      throw new Error(error);
    }
  }

  async migrateEntity(entity, operationType) {
    let result = null;
    try {
      result = await ledgerClient.doInvoke(chaincodeOperation.get(operationType), []);
    } catch (error) {
      loggerManager.error(error);
      throw new Error(error);
    }
    return result;
  }

  async editEntity(entity, operationType) {
    let result = null;
    try {
      if (entity) {
        const args = [JSON.stringify(entity)];
        result = await ledgerClient.doInvoke(chaincodeOperation.get(operationType), args);
      } else {throw new Error('Entity could not be empty or null');}
    } catch (error) {
      loggerManager.error(error);
      throw new Error(error);
    }
    return result;
  }


  async deleteEntity(entity, operationType) {
    let result = null;
    try {
      if (entity) {
        args = [entity.id, entity.type];
        result = await ledgerClient.doInvoke(chaincodeOperation.get(operationType), args);
      } else {throw new Error('Entity could not be empty or null');}
    } catch (error) {
      loggerManager.error(error);
      throw new Error(error);
    }
    return result;
  }

  async getEntity(id, type) {
    return await ledgerClient.doInvoke(chaincodeOperation.get('GET'), [id, type]);
  }
}

module.exports = BlockchainHandler;
