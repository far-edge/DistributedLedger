const CONFIG = require('../../resources/config.json');
const OCB_URL = CONFIG.ocb;
const NGSI = require('ngsijs');
const ngsiConnection = new NGSI.Connection(OCB_URL); //TODO se non va Orion killa il processo!!!
const LoggerManager = require('./LoggerManager');
const loggerManager = new LoggerManager();
//const MASTER = '_master';

const operation = new Map([
    ['POST', 'createEntity'],
    ['PUT', 'updateEntity'],
    ['DELETE', 'deleteEntity'],
    ['GET', 'getEntity'],
    ['MIGRATION', 'migrateEntities']

]);

const reverseOperation = new Map([
    ['POST', 'DELETE'],
    ['PUT', 'PUT'],
    ['DELETE', 'POST']
]);

class OrionHandler {

    constructor() {
        let service = CONFIG.service;
        let servicepath = CONFIG.servicepath;
    }

    /**
     * public method used to revert local chainges in case of BlockChain errors
     * @param {String} operationType 
     * @param {Object} payload 
     */
    async revertLocalChanges(operationType, payload) {
        try {
            if (payload)
                await this.executeOperation(reverseOperation.get(operationType), payload);
            else
                loggerManager.error('Error coming from Blockchain could not be empty!');
        } catch (error) {
            loggerManager.error(error);
            throw new Error(error);
        }
    }

    async executeOperation(operationType, payload) {
        try {
            let method = this[operation.get(operationType)];
            if (operationType.indexOf('DELETE') >= 0) {
                method.call(this, payload.id, payload.type);
            } else {
                const entity = payload;
                await method.call(this, entity);
            }
        } catch (error) {
            loggerManager.error(error);
            throw new Error(error);
        }
    }

    async migrateEntities(entities) {
        const entities_ = JSON.parse(entities);
        //TODO
        try {
            for (const entity of entities_) {
                const isPresent = await this.getEntity(entity.id, entity.type);
                if (isPresent)
                    await this.updateEntity(entity);
                else
                    await this.createEntity(entity);
            }
        } catch (error) {
            loggerManager.error(error);
            throw new Error(error);
        }
    }

    async getEntity(id, type) {
        let entity = null;
        try {
            entity = await ngsiConnection.v2.getEntity({
                id: id,
                type: type,
                service: this.service,
                servicepath: this.servicepath
            });
        } catch (error) {
            loggerManager.error('Entity ' + id + ' not found on Orion ' + error);
            entity = null;
        }
        return entity;
    }

    async listEntities() {
        try {
            return ngsiConnection.v2.listEntities({
                service: this.service,
                servicepath: this.servicepath
            });
        } catch (error) {
            loggerManager.error(error);
            throw new Error(error);
        }
    }

    async updateEntity(entity) {
        try {
            return await ngsiConnection.v2.appendEntityAttributes(entity, {
                service: this.service,
                servicepath: this.servicepath
            });
        } catch (error) {
            loggerManager.error(error);
            throw new Error(error);
        }
    }

    async createEntity(entity) {
        try {
            return await ngsiConnection.v2.createEntity(entity, {
                service: this.service,
                servicepath: this.servicepath
            });
        } catch (error) {
            loggerManager.error(error);
            throw new Error(error);
        }
    }

    async deleteEntity(id, type) {
        try {
            await ngsiConnection.v2.deleteEntity({
                id: id,
                type: type,
                service: this.service,
                servicepath: this.servicepath
            });
        } catch (error) {
            loggerManager.error(error);
            throw new Error(error);
        }
    }

    setService(service){
        this.service = service;
    }

    setServicePath(servicepath){
        this.servicepath = servicepath;
    }
}
module.exports = OrionHandler;