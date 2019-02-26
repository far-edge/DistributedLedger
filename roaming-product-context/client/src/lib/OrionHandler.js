// eslint-disable-next-line import/no-unresolved
const NGSI = require('ngsijs');
const CONFIG = require('../../resources/config.json');
const LoggerManager = require('./LoggerManager');

const OCB_URL = CONFIG.ocb;
const {
  service,
} = CONFIG;
const {
  servicepath,
} = CONFIG;
// const MASTER = '_master';


const operation = new Map([
  ['POST', 'createEntity'],
  ['PUT', 'updateEntity'],
  ['DELETE', 'deleteEntity'],
  ['GET', 'getEntity'],
]);

const reverseOperation = new Map([
  ['POST', 'DELETE'],
  ['PUT', 'PUT'],
  ['DELETE', 'POST'],
]);
class OrionHandler {
  constructor() {
    this.ngsiConnection = new NGSI.Connection(OCB_URL); // TODO se non va Orion killa il processo!!!
    this.loggerManager = new LoggerManager();
  }

  /**
   * public method used to revert local chainges in case of BlockChain errors
   * @param {String} operationType
   * @param {Object} payload
   */
  async revertLocalChanges(payload, operationType) {
    try {
      if (payload) await this.executeOperation(reverseOperation.get(operationType), payload);
      else this.loggerManager.error('Error coming from Blockchain could not be empty!');
    } catch (error) {
      this.loggerManager.error(error);
      throw new Error(error);
    }
  }

  async executeOperation(payload, operationType) {
    try {
      const method = this[operation.get(operationType)];
      if (operationType.indexOf('DELETE') >= 0) {
        method.call(this, payload.id, payload.type);
      } else {
        const entity = payload;
        await method.call(this, entity);
      }
    } catch (error) {
      this.loggerManager.error(error);
      throw new Error(error);
    }
  }

  async getEntity(id, type) {
    let entity = null;
    try {
      entity = await this.ngsiConnection.v2.getEntity({
        id,
        type,
        service: this.service,
        servicepath: this.servicepath,
      });
    } catch (error) {
      this.loggerManager.error(`Entity ${id} not found on Orion ${error}`);
      entity = null;
    }
    if (entity && entity.entity) {
      return entity.entity;
    }
    return entity;
  }

  async listEntities() {
    try {
      return this.ngsiConnection.v2.listEntities({
        service: this.service,
        servicepath: this.servicepath,
      });
    } catch (error) {
      this.loggerManager.error(error);
      throw new Error(error);
    }
  }

  async updateEntity(entity) {
    try {
      return await this.ngsiConnection.v2.appendEntityAttributes(entity, {
        service: this.service,
        servicepath: this.servicepath,
      });
    } catch (error) {
      this.loggerManager.error(error);
      throw new Error(error);
    }
  }

  async createEntity(entity) {
    try {
      return await this.ngsiConnection.v2.createEntity(entity, {
        service,
        servicepath,
      });
    } catch (error) {
      this.loggerManager.error(error);
      throw new Error(error);
    }
  }

  async deleteEntity(id, type) {
    try {
      await this.ngsiConnection.v2.deleteEntity({
        id,
        type,
        service,
        servicepath,
      });
    } catch (error) {
      this.loggerManager.error(error);
      throw new Error(error);
    }
  }
}
module.exports = OrionHandler;
