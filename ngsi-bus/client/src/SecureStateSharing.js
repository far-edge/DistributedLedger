const BlockchainHandler = require('./lib/BlockchainHandler');
const blockchainHandler = new BlockchainHandler();
const OrionHandler = require('./lib/OrionHandler');
const orionHandler = new OrionHandler();

const LoggerManager = require('./lib/LoggerManager');
const loggerManager = new LoggerManager();

class SecureStateSharing {
   
    constructor() {
        let origEntity = '';
    }


    /**
     * public method
     * Entry point for the entire application
     * @param {String} id 
     * @param {String} type 
     * @param {String} requestType 
     */
    async executeRequest(id, type, requestType) {
        let entity;
        try {
            entity = await orionHandler.getEntity(id, type);
            if (entity && entity.hasOwnProperty('entity'))
                entity = entity.entity;
            let result = null;
            result = await blockchainHandler.executeOperation(entity, requestType);
            if (result && requestType === 'MIGRATION')
                orionHandler.executeOperation('MIGRATION', result);
            loggerManager.debug('Transaction correctly committed to the chain with result: ' + JSON.stringify(result));
            return result;
        } catch (error) {
            loggerManager.error(error);
            //orionHandler.revertLocalChanges(requestType, JSON.parse(error));
            orionHandler.revertLocalChanges(requestType, this.origEntity);
            throw new Error(error);
        }
    }

    async saveOrigRequest(id, type) {
        try {
           const entity = await orionHandler.getEntity(id, type);
            if (entity && entity.hasOwnProperty('entity'))
                this.origEntity = entity.entity;
        } catch (error) {
            loggerManager.error(error);
            throw new Error(error);
        }
    }

    getOrionHandler() {
        return orionHandler;
    }

    getBlockchainHandler() {
        return blockchainHandler;
    }

}

module.exports = SecureStateSharing;