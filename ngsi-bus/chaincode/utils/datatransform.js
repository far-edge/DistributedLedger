"use strict";
Object.defineProperty(exports, "__esModule", { value: true });
const _ = require("lodash");
/**
 * The Transform class is a helper to provide data transformation to and from the formats required by hyperledger fabric.
 */
class Transform {
    /**
     * serialize payload
     *
     * @static
     * @param {*} value
     * @returns
     * @memberof Transform
     */
    static serialize(value) {
        if (_.isDate(value) || _.isString(value)) {
            return Buffer.from(this.normalizePayload(value).toString());
        }
        return Buffer.from(JSON.stringify(this.normalizePayload(value)));
    }
    ;
    /**
     * parse string to object
     *
     * @static
     * @param {Buffer} buffer
     * @returns {(object | undefined)}
     * @memberof Transform
     */
    static bufferToObject(buffer) {
        if (buffer == null) {
            return;
        }
        const bufferString = buffer.toString();
        if (bufferString.length <= 0) {
            return;
        }
        return JSON.parse(bufferString);
    }
    ;
    /**
     * bufferToDate
     *
     * @static
     * @param {Buffer} buffer
     * @returns {(Date | undefined)}
     * @memberof Transform
     */

     static binToString(array){
        var result = "";
        for(var i = 0; i < array.length; ++i){
            result+= (String.fromCharCode(array[i]));
        }
        return result;
    }

    static bufferToDate(buffer) {
        if (buffer == null) {
            return;
        }
        const bufferString = buffer.toString();
        if (bufferString.length <= 0) {
            return;
        }
        if (/\d+/g.test(bufferString)) {
            return new Date(parseInt(bufferString, 10));
        }
        return;
    }
    ;
    static bufferToString(buffer) {
        if (buffer == null) {
            return null;
        }
        return buffer.toString();
    }
    ;
    /**
     * Transform iterator to array of objects
     *
     * @param {'fabric-shim'.Iterators.Iterator} iterator
     * @returns {Promise<Array>}
     */
    static async iteratorToList(iterator) {
        const allResults = [];
        let res;
        while (res == null || !res.done) {
            res = await iterator.next();
            if (res.value && res.value.value.toString()) {
                let parsedItem;
                try {
                    parsedItem = JSON.parse(res.value.value.toString('utf8'));
                }
                catch (err) {
                    parsedItem = res.value.value.toString('utf8');
                }
                allResults.push(parsedItem);
            }
        }
        await iterator.close();
        return allResults;
    }
    ;
    /**
     * Transform iterator to array of objects
     *
     * @param {'fabric-shim'.Iterators.Iterator} iterator
     * @returns {Promise<Array>}
     */
    static async iteratorToKVList(iterator) {
        const allResults = [];
        let res;
        while (res == null || !res.done) {
            res = await iterator.next();
            if (res.value && res.value.value.toString()) {
                let parsedItem = { key: '', value: {} };
                parsedItem.key = res.value.key;
                try {
                    parsedItem.value = JSON.parse(res.value.value.toString('utf8'));
                }
                catch (err) {
                    parsedItem.value = res.value.value.toString('utf8');
                }
                allResults.push(parsedItem);
            }
        }
        await iterator.close();
        return allResults;
    }
    ;

    /**
     * Transform iterator to array of objects
     *
     * @param {'fabric-shim'.Iterators.Iterator} iterator
     * @returns {Promise<Array>}
     */
    static async iteratorToObjectList(iterator) {
        const allResults = [];
        let res;
        while (res == null || !res.done) {
            res = await iterator.next();
            if (res.value && res.value.value.toString()) {
               let parsedItem = {};
                try {
                    parsedItem = JSON.parse(res.value.value.toString('utf8'));
                }
                catch (err) {
                    parsedItem = res.value.value.toString('utf8');
                }
                allResults.push(parsedItem);
            }
        }
        await iterator.close();
        return allResults;
    }
    ;
    /**
     * normalizePayload
     *
     * @static
     * @param {*} value
     * @returns {*}
     * @memberof Transform
     */
    static normalizePayload(value) {
        if (_.isDate(value)) {
            return value.getTime();
        }
        else if (_.isString(value)) {
            return value;
        }
        else if (_.isArray(value)) {
            return _.map(value, (v) => {
                return this.normalizePayload(v);
            });
        }
        else if (_.isObject(value)) {
            return _.mapValues(value, (v) => {
                return this.normalizePayload(v);
            });
        }
        return value;
    }
    ;
}
exports.Transform = Transform;
//# sourceMappingURL=datatransform.js.map