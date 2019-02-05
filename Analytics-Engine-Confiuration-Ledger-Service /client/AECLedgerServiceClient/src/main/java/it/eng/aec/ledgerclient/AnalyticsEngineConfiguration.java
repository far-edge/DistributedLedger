package it.eng.aec.ledgerclient;

import it.eng.jledgerclient.exception.JLedgerClientException;

public interface AnalyticsEngineConfiguration {


    void postConfiguration(String key, String payload) throws JLedgerClientException;

    void putConfiguration(String key, String payload) throws JLedgerClientException;

    void deleteConfiguration(String key) throws JLedgerClientException;

    String getConfiguration(String key) throws JLedgerClientException;
}
