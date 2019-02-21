const nodeLedgerClient = require('node-ledger-client');
const config = require('../../resources/config-fabric-network.json');

class BlockchainHandler {
  constructor() {
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

  async executeOperation(asset, chaincodeOperation) {
    let result = null;
    try {
      if (asset) {
        const args = [JSON.stringify(asset)];
        result = await this.ledgerClient.doInvoke(chaincodeOperation.name, args);
      } else {
        throw new Error('Entity could not be empty or null');
      }
    } catch (error) {
      throw new Error(error);
    }
    return result;
  }
}


module.exports = BlockchainHandler;
