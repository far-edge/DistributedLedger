const express = require('express');
const _ = require('underscore');
const CONFIG = require('../resources/config.json');

const app = express();
const RoamingProductContext = require('./RoamingProductContext');

const rpc = new RoamingProductContext();
const LoggerManager = require('./lib/LoggerManager');

const loggerManager = new LoggerManager();

function onExit() {
  loggerManager.info('Process Exit');
  process.exit(0);
}

function errorHandler(err, res, next) {
  if (res.headersSent) {
    return next(err);
  }
  res.status(500);
  res.json(err.message);
  return null;
}

process.on('SIGINT', onExit);
process.on('exit', onExit);

app.use(errorHandler);


app.post('/rpc/v1/entities/:id', (req, res) => {
  const status = rpc.validateRequest(req);
  if (!_.isEmpty(status)) {
    res.sendStatus(status.status);
  }
  res.sendStatus(rpc.release(req));
});

app.put('/rpc/v1/entities/:id', (req, res) => {
  const status = rpc.validateRequest(req);
  if (!_.isEmpty(status)) {
    res.sendStatus(status.status);
  }
  res.sendStatus(rpc.acquire(req));
});

app.delete('/rpc/v1/entities/:id', (req, res) => {
  const status = rpc.validateRequest(req);
  if (!_.isEmpty(status)) {
    res.sendStatus(status.status);
  }
  res.sendStatus(rpc.dispose(req));
});

app.get('');

app.listen(CONFIG.port, () => loggerManager.logger.info(`RPC Service listening on port ${CONFIG.port}!`));


process.on('uncaughtException', (er) => {
  loggerManager.logger.error(er.stack);
  process.exit(1);
});

module.exports = app;
