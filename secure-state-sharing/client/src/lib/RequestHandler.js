const CONFIG = require('../../resources/config.json');
const OrionHandler = require('./OrionHandler');
const orionHandler = new OrionHandler();

const LoggerManager = require('./LoggerManager');
const loggerManager = new LoggerManager();


class RequestHandler {

    constructor() {}
    async updateEntityFromRequest(entityId) {
        try {
            return await orionHandler.getEntity(entityId, orionHandler.TYPE);
        } catch (error) {
            console.error(error);
            throw new Error(error);
        }
    }

    getId(req) {
        let idParam;
        if (req.params && req.params[0])
            idParam = req.params[0].split("/")[2];
        if (idParam) return idParam;
        if (req.body.id) return req.body.id;
        if (req.path) return req.path.split("/")[3];

        throw new Error('Entity id not found');
    }

    getType(req) {
        if (req.query.type) {
            return req.query.type;
        }
        if (req.body.type) return req.body.type;
        throw new Error('Entity type not found');
    }

    isUpdate(req) {
        return req.method === 'PUT';
    }

    isDelete(req) {
        return req.method === 'DELETE';
    }

    isMigration(req) {
        if (req.path.indexOf('migration') >= 0) {
            return true;
        }
        return false;
    }

    isOnBehalfOfChain(req) {
        try {
            const route = req.route;
            const method = req.method;
            const pathExclude = CONFIG.path_exclude.split(',');

            if (method === 'PUT' ||
                method === 'POST' ||
                method === 'DELETE' ||
                method === 'PATCH' //TODO Delete Context
            ) {
                for (let exclusion of pathExclude) {
                    if (req.path.indexOf(exclusion.trim()) >= 0) {
                        return false;
                    }
                }
                return true;
            }
            return false;
        } catch (error) {
            console.error(error)
            return false;
        }
    }

    createProxyResMessage(proxyResData, message) {
        let data = {};
        if (proxyResData.length > 0) {
            data = JSON.parse(proxyResData.toString('utf8'));
        }
        data.blockchainStatus = message;
        return JSON.stringify(data);
    }
    
    createProxyResError(proxyResData, error) {
        let data = {};
        if (proxyResData.length > 0) {
            data = JSON.parse(proxyResData.toString('utf8'));
        }
        data.blockchainError = error;
        return JSON.stringify(data);
    }
    createProxyResDataMigration(proxyResData, message) {
        let data = {};
        if (proxyResData.length > 0) {
            data = JSON.parse(proxyResData.toString('utf8'));
        }
        delete data.error;
        data.description = message;
        data.blockchainStatus = message;
        return JSON.stringify(data);
    }

}



module.exports = RequestHandler;