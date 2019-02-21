const winston = require('winston');
const chalk = require('chalk');
const CONFIG = require('../../resources/config.json');

// eslint-disable-next-line no-underscore-dangle
const _error = chalk.bold.red;
const warning = chalk.keyword('orange');

class LoggerManager {
  constructor() {
    this.logger = winston.createLogger({
      level: CONFIG.logLevel,
      format: winston.format.combine(
        winston.format.timestamp(),
        winston.format.printf(info => `${info.timestamp} ${info.level}: ${info.message}`),
      ),
      transports: [new winston.transports.Console()],
    });
  }

  get getLogger() {
    return this.logger;
  }

  debug(message) {
    if (!CONFIG.development) this.logger.log('debug', chalk.blue(message));
    else console.log(message);
  }

  info(message) {
    if (!CONFIG.development) this.logger.log('info', chalk.yellow(message));
    else console.log(message);
  }

  warn(message) {
    if (!CONFIG.development) this.logger.log('warn', warning(message));
    else console.log(message);
  }

  error(error) {
    let { message } = error;
    if (!message) { message = JSON.stringify(error); }
    if (!CONFIG.development) this.logger.log('error', _error(message));
    else console.error(message);
  }
}
module.exports = LoggerManager;
