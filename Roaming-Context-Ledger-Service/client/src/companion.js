
const nodeLedgerClient = require('node-ledger-client');
const OrionHandler = require('./lib/OrionHandler');
const orionHandler = new OrionHandler();
const config = require('../resources/config-fabric-network.json');
const ccid = config.chaincode.name;
const eventName = '^FE_SSS_*';
let ledgerClient = null;

const LoggerManager = require('./lib/LoggerManager');
const loggerManager = new LoggerManager();
let handlers = [];

const eventType = new Map([
  ['FE_SSS_POST-ENTITY', 'POST'],
  ['FE_SSS_PUT-ENTITY', 'PUT'],
  ['FE_SSS_DELETE-ENTITY', 'DELETE'],
]);


const ledger = async () => {
  try {
    ledgerClient = await nodeLedgerClient.LedgerClient.init(config);
    handlers = await registerAllEvents();
  } catch (error) {
    loggerManager.error(error);
  }
};
ledger();

process.stdin.resume();
// TODO process.onExit
// TODO Controllare se esiste un contesto prima di crearlo o cancellarlo
async function onEvent(event) {
    try {
      if (event) {
        loggerManager.info(`COMPANION: Event arrived: ${event.event_name}`);
        if (event.hasOwnProperty('payload')) {
          const payloadJson = Buffer.from(event.payload);
          const payload = JSON.parse(payloadJson);
          loggerManager.info(`COMPANION: Event payload: ${payloadJson}`);
          const eventName = event.event_name;
          orionHandler.executeOperation(eventType.get(eventName), payload);
        }
      } else {loggerManager.error('COMPANION: Event undefined');}
    } catch (error) {
      loggerManager.error(error);
    }
  }
  
  function onError(error) {
    loggerManager.error(error);
  }
  
function registerAllEvents() {
  const handlersMod = ledgerClient.registerAllPeersChaincodeEvent(ccid, eventName, onEvent, onError);
  loggerManager.debug(`COMPANION: Handlers obtained: ${JSON.stringify(handlersMod)}`);
  return handlersMod;
}

function unRegisterAllEvents(handlers) {
  ledgerClient.unregisterAllPeersChaincodeEvent(handlers);
}

