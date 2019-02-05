package it.eng.oor.ledgerclient;

import it.eng.jledgerclient.exception.JLedgerClientException;

public interface AnalyticsResultsPublishing {


    void post(String key, String payload) throws JLedgerClientException;

    void put(String key, String payload) throws JLedgerClientException;

    void delete(String key) throws JLedgerClientException;

    String get(String key) throws JLedgerClientException;
}
