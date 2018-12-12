const winston = require('winston');
const CONFIG = require('../../resources/config.json');
const chalk = require('chalk');

const error_ = chalk.bold.red;
const warning = chalk.keyword('orange');

class LoggerManager {

    constructor() {
        this.logger = winston.createLogger({
            level: CONFIG.logLevel,
            format: winston.format.combine(
                winston.format.timestamp(),
                winston.format.printf(info => {
                    return `${info.timestamp} ${info.level}: ${info.message}`;
                })
            ),
            transports: [new winston.transports.Console()]
        });
    }

    get getLogger() {
        return this.logger;
    }

    debug(message) {
        if (!CONFIG.development)
            this.logger.log('debug', chalk.blue(message));
        else
            console.log(message);
    }
    info(message) {
        if (!CONFIG.development)
            this.logger.log('info', chalk.yellow(message));
        else console.log(message);

    }

    warn(message) {
        if (!CONFIG.development)
            this.logger.log('warn', warning(message));
        else console.log(message);
    }

    error(error) {
        let message;
        if (error.hasOwnProperty('message'))
            message = error.message;
        else
            message = JSON.stringify(error);
        if (!CONFIG.development)
            this.logger.log('error', error_(message));
        else console.error(message);
    }


}
module.exports = LoggerManager;