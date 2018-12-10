const proxy = require('express-http-proxy'); // https://www.npmjs.com/package/express-http-proxy
const express = require('express');
const CONFIG = require('../resources/config.json');
const OCB_URL = CONFIG.ocb;
const app = express();
const SecureStateSharing = require('./SecureStateSharing');
const secureStateSharing = new SecureStateSharing();
const RequestHandler = require('./lib/RequestHandler');
const requestHandler = new RequestHandler();
const LoggerManager = require('./lib/LoggerManager');
const loggerManager = new LoggerManager();
let child = null;
/**
 * 
 */
function onExit() {
    loggerManager.info('Process Exit');
    child.kill('SIGINT');
    process.exit(0);
}
process.on('SIGINT', onExit);
process.on('exit', onExit);

/**
 * This function launch the SecureStateSharing Companion that updates the current local Orion 
 * with the updates coming from the other Orion in channel
 */
(async function launchCompanion() {
    try {
        const {
            spawn,
        } = require('child_process');

        child = spawn('node', ['./src/companion.js'], {
            detached: true,
            stdio: [process.stdin, process.stdout, process.stderr],
        });
        // child.unref();
        loggerManager.info('Companion started correctly!');
    } catch (error) {
        loggerManager.error(error);
        onExit();
    }
}());

/**
 * 
 * @param {Object} proxyResData 
 * @param {Object} req 
 * @param {Object} res 
 */
async function serveResponse(proxyResData, req, res) {
    {
        loggerManager.info('redirecting to Orion Context Broker');
        try {
            if (req.path.indexOf('v1') >= 0) {
                res.status(400).send('Warning!!! v1 APIs not supported use v2 -> http://telefonicaid.github.io/fiware-orion/api/v2/stable/');
                return '';
            }
            if (req.path === '/info') {
                res.json({
                    name: 'Secure state Sharing',
                    'version': '1.0.0',
                    'author': 'Antonio Scatoloni',
                });
                return '';
                // Editing mode of OCB
            }
            if (requestHandler.isOnBehalfOfChain(req)) { // I'm updating attributes
                const id = requestHandler.getId(req);
                const type = requestHandler.getType(req);
                const method = req.method;
                const result = await secureStateSharing.executeRequest(id, type, method);
                res.status(200);
                return requestHandler.createProxyResMessage(proxyResData, result);
            }
            return proxyResData;
        } catch (error) {
            loggerManager.error(error);
            res.status(500);
            return requestHandler.createProxyResError(proxyResData, error.message);
        }
    }
}

/**
 * 
 * @param {Object} req 
 */
async function processReq(req) {
    try {
        const method = 'MIGRATION';
        const id = null;
        const type = null;
        const result = await secureStateSharing.executeRequest(id, type, method);
        loggerManager.info('Migration finished correctly');
    } catch (error) {
        loggerManager.error(error);
    }
}
/**
 * This function save the original Orion Entity in order to revert changes in case of error.
 * @param {Object} req 
 */
async function saveOrigReq(req) {
    try {
        const id = requestHandler.getId(req);
        const type = requestHandler.getType(req);
        await secureStateSharing.saveOrigRequest(id, type);
    } catch (error) {
        loggerManager.error(error);
    }
}

app.use('/', proxy(OCB_URL, {
    proxyReqOptDecorator(proxyReqOpts, srcReq) {
        //proxyReqOpts.headers['Content-Type'] = 'application/json';
        //proxyReqOpts.method = 'GET';
        const isMigration = requestHandler.isMigration(proxyReqOpts);
        if (isMigration) {
            processReq(proxyReqOpts);
        }
        if (proxyReqOpts.headers['Fiware-Service'])
            secureStateSharing.getOrionHandler().setService(proxyReqOpts.headers['Fiware-Service']);
        if (proxyReqOpts.headers['Fiware-ServicePath'])
            secureStateSharing.getOrionHandler().setServicePath(proxyReqOpts.headers['Fiware-ServicePath']);
        return proxyReqOpts;
    },
    proxyReqBodyDecorator: function (bodyContent, srcReq) {
        if (bodyContent.length > 0) {
            srcReq.body = JSON.parse(bodyContent.toString('utf8'));
        }
        saveOrigReq(srcReq);
        return bodyContent;
    },
    userResHeaderDecorator(headers, userReq, userRes, proxyReq, proxyRes) {
        // recieves an Object of headers, returns an Object of headers.
        return headers;
    },
    userResDecorator(proxyRes, proxyResData, userReq, userRes) {
        if (requestHandler.isMigration(userReq)) {
            userRes.status(200);
            return requestHandler.createProxyResDataMigration(proxyResData, 'Migration running...');
        }
        if (proxyResData.length > 0) {
            const data = JSON.parse(proxyResData.toString('utf8'));
            if (data.hasOwnProperty('error')) {
                return proxyResData;
            }
        }
        return serveResponse(proxyResData, userReq, userRes);
    },
    /* ,proxyErrorHandler: function (err, res, next) {
           errorHandler(err, res);
       } */
}));
app.listen(CONFIG.port);

function errorHandler(err, res, next) {
    if (res.headersSent) {
        return next(err);
    }
    res.status(500);
    res.json(err.message);
}
module.exports = app;