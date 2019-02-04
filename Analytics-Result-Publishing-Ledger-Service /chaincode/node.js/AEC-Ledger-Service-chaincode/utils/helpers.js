"use strict";
Object.defineProperty(exports, "__esModule", { value: true });
const winston_1 = require("winston");
/**
 * helper functions
 */
class Helpers {
    /**
     * Winston Logger with default level: debug
     *
     * @static
     * @param {string} name
     * @param {string} [level]
     * @returns {LoggerInstance}
     * @memberof Helpers
     */
    static getLoggerInstance(name, level) {
        return new winston_1.Logger({
            transports: [new winston_1.transports.Console({
                    level: level || 'debug',
                    prettyPrint: true,
                    handleExceptions: true,
                    json: false,
                    label: name,
                    colorize: true,
                })],
            exitOnError: false,
        });
    }
    ;
    /**
     * Check number of arguments
     * try to cast object using yup
     * validate arguments against predefined types using yup
     * return validated object
     *
     * @static
     * @template T
     * @param {string[]} args
     * @param {*} yupSchema
     * @returns {Promise<T>}
     * @memberof Helpers
     */
    static checkArgs(args, yupSchema) {
        const keys = yupSchema._nodes;
        if (!keys || args.length != keys.length) {
            throw new Error(`Incorrect number of arguments. Expecting ${keys.length}`);
        }
        let objectToValidate = {};
        keys.reverse().forEach((key, index) => {
            objectToValidate[key] = args[index];
        });
        yupSchema.cast(objectToValidate);
        return yupSchema.validate(objectToValidate).then((validatedObject) => {
            return validatedObject;
        }).catch((errors) => {
            throw new Error(errors);
        });
    }
}
exports.Helpers = Helpers;
//# sourceMappingURL=helpers.js.map