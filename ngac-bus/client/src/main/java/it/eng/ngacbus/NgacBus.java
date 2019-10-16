package it.eng.ngacbus;

import it.eng.jledgerclient.exception.JLedgerClientException;


/**
 * @author clod16
 */

public interface NgacBus {


    void post(String key, String payload) throws JLedgerClientException;

    void put(String key, String payload) throws JLedgerClientException;

    void delete(String key) throws JLedgerClientException;

    String get(String key) throws JLedgerClientException;
}
