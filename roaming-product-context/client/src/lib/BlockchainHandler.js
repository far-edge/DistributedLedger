// eslint-disable-next-line import/no-unresolved
const nodeLedgerClient = require('node-ledger-client');
const config = require('../../resources/config-fabric-network.json');
const LoggerManager = require('./LoggerManager');

class BlockchainHandler {
  constructor() {
    this.loggerManager = new LoggerManager();

    this.chaincodeOperation = Object.freeze({
      ACQUIRE: {
        name: 'acquire',
      },
      DISPOSE: {
        name: 'dispose',
      },
      RELEASE: {
        name: 'release',
      },
    });
    const ledger = async () => {
      this.ledgerClient = await nodeLedgerClient.LedgerClient.init(config);
    };
    ledger();
  }

  async executeOperation(asset, chaincodeOperation, isOnlyID) {
    let result = null;
    try {
      if (!isOnlyID) {
        if (asset) {
          const args = [JSON.stringify(asset)];
          result = await this.ledgerClient.doInvoke(chaincodeOperation.name, args);
        } else {
          throw new Error('Entity could not be empty or null');
        }
      } else {
        const args = [asset];
        result = await this.ledgerClient.doInvoke(chaincodeOperation.name, args);
      }
    } catch (error) {
      this.loggerManager.logger.error(error);
      throw new Error(error);
    }
    return result;
  }
}


module.exports = BlockchainHandler;
