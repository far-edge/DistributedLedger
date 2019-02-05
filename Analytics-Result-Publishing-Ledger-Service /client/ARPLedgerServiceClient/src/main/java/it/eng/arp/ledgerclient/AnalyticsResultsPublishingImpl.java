package it.eng.arp.ledgerclient;

import it.eng.jledgerclient.exception.JLedgerClientException;
import it.eng.jledgerclient.fabric.HLFLedgerClient;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.hyperledger.fabric.sdk.ChaincodeEventListener;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class AnalyticsResultsPublishingImpl extends HLFLedgerClient implements AnalyticsResultsPublishing {
    private final static Logger log = LogManager.getLogger(AnalyticsResultsPublishingImpl.class);

    public AnalyticsResultsPublishingImpl() throws JLedgerClientException {
    }

    public AnalyticsResultsPublishingImpl(InputStream configFabricNetwork, InputStream certificate, InputStream keystore) throws JLedgerClientException {
        super(configFabricNetwork, certificate, keystore);
    }

    @Override
    public String doRegisterEvent(String eventName, ChaincodeEventListener chaincodeEventListener) throws JLedgerClientException {
        return super.doRegisterEvent(eventName, chaincodeEventListener);
    }

    @Override
    public void doUnregisterEvent(String chaincodeEventListenerHandle) throws JLedgerClientException {
        ledgerInteractionHelper.getEventHandler().unregister(chaincodeEventListenerHandle);
    }

    @Override
    public void postResult(String key, String payload) throws JLedgerClientException {

        if (key.isEmpty() || payload.isEmpty()) {
            throw new JLedgerClientException((Function.postResult.name()) + "is in error, No input data!");
        }
        List<String> args = new ArrayList<>();
        args.add(key);
        args.add(payload);
        final String invokeReturns = doInvoke(Function.postResult.name(), args);
        log.debug(("Payload retrieved: " + invokeReturns));
    }

    @Override
    public void putResult(String key, String payload) throws JLedgerClientException {
        if (key.isEmpty() || payload.isEmpty()) {
            throw new JLedgerClientException((Function.putResult.name()) + "is in error, No input data!");
        }
        List<String> args = new ArrayList<>();
        args.add(key);
        args.add(payload);
        final String invokeReturns = doInvoke(Function.putResult.name(), args);
        log.debug(("Payload retrieved: " + invokeReturns));
    }

    @Override
    public void deleteResult(String key) throws JLedgerClientException {
        if (key.isEmpty()) {
            throw new JLedgerClientException((Function.deleteResult.name()) + "is in error, No input data!");
        }
        List<String> args = new ArrayList<>();
        args.add(key);
        final String invokeReturns = doInvoke(Function.deleteResult.name(), args);
        log.debug(("Payload retrieved: " + invokeReturns));

    }

    @Override
    public String getResult(String key) throws JLedgerClientException {
        if (key.isEmpty()) {
            throw new JLedgerClientException((Function.getResult.name()) + "is in error, No input data!");
        }
        List<String> args = new ArrayList<>();
        args.add(key);
        final String invokeReturns = doInvoke(Function.getResult.name(), args);
        log.debug(("Payload retrieved: " + invokeReturns));
        return invokeReturns;
    }
}
