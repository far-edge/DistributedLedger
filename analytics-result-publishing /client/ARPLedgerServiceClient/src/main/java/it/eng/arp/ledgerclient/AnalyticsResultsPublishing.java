package it.eng.arp.ledgerclient;

import it.eng.jledgerclient.exception.JLedgerClientException;

public interface AnalyticsResultsPublishing {


    void postResult(String key, String payload) throws JLedgerClientException;

    void putResult(String key, String payload) throws JLedgerClientException;

    void deleteResult(String key) throws JLedgerClientException;

    String getResult(String key) throws JLedgerClientException;
}
