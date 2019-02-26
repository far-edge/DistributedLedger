// eslint-disable-next-line import/no-unresolved
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

/*
function errorHandler(err, res, next) {
  if (res.headersSent) {
    return next(err);
  }
  res.status(500);
  res.json(err.message);
  return null;
} */

process.on('SIGINT', onExit);
process.on('exit', onExit);

app.get('/', (req, res) => {
  res.send('RPC Server...');
});

app.route('/rpc/v1/entities/:id')
  .post((req, res) => {
    const status = rpc.validateRequest(req);
    if (!_.isEmpty(status)) {
      res.sendStatus(status.status);
    }
    const result = rpc.release(req);
    result.then((data) => {
      res.sendStatus(data.status);
    })
      .catch((err) => {
        res.send(err);
      });
  })
  .put((req, res) => {
    const status = rpc.validateRequest(req);
    if (!_.isEmpty(status)) {
      res.sendStatus(status.status);
    }
    const result = rpc.acquire(req);
    result.then((data) => {
      res.sendStatus(data.status);
    })
      .catch((err) => {
        res.send(err);
      });
  })
  .delete((req, res) => {
    const status = rpc.validateRequest(req);
    if (!_.isEmpty(status)) {
      res.sendStatus(status.status);
    }
    const result = rpc.dispose(req);
    result.then((data) => {
      res.sendStatus(data.status);
    })
      .catch((err) => {
        res.send(err);
      });
  });


app.use((req, res) => {
  res.status(404).send("Sorry, that route doesn't exist.");
});

app.listen(CONFIG.port, () => {
  loggerManager.logger.info(`Server listening at port ${CONFIG.port}`);
});

process.on('uncaughtException', (er) => {
  loggerManager.logger.error(er.stack);
  process.exit(1);
});

module.exports = app;
