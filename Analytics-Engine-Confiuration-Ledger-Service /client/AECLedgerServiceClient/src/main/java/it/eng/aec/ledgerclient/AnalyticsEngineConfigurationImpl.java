package it.eng.aec.ledgerclient;

import it.eng.jledgerclient.exception.JLedgerClientException;
import it.eng.jledgerclient.fabric.HLFLedgerClient;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.hyperledger.fabric.sdk.ChaincodeEventListener;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class AnalyticsEngineConfigurationImpl extends HLFLedgerClient implements AnalyticsEngineConfiguration {
    private final static Logger log = LogManager.getLogger(AnalyticsEngineConfigurationImpl.class);

    public AnalyticsEngineConfigurationImpl() throws JLedgerClientException {
    }

    public AnalyticsEngineConfigurationImpl(InputStream configFabricNetwork, InputStream certificate, InputStream keystore) throws JLedgerClientException {
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
    public void postConfiguration(String key, String payload) throws JLedgerClientException {

        if (key.isEmpty() || payload.isEmpty()) {
            throw new JLedgerClientException((Function.postConfiguration.name()) + "is in error, No input data!");
        }
        List<String> args = new ArrayList<>();
        args.add(key);
        args.add(payload);
        final String invokeReturns = doInvoke(Function.postConfiguration.name(), args);
        log.debug(("Payload retrieved: " + invokeReturns));
    }

    @Override
    public void putConfiguration(String key, String payload) throws JLedgerClientException {
        if (key.isEmpty() || payload.isEmpty()) {
            throw new JLedgerClientException((Function.putConfiguration.name()) + "is in error, No input data!");
        }
        List<String> args = new ArrayList<>();
        args.add(key);
        args.add(payload);
        final String invokeReturns = doInvoke(Function.putConfiguration.name(), args);
        log.debug(("Payload retrieved: " + invokeReturns));
    }

    @Override
    public void deleteConfiguration(String key) throws JLedgerClientException {
        if (key.isEmpty()) {
            throw new JLedgerClientException((Function.deleteConfiguration.name()) + "is in error, No input data!");
        }
        List<String> args = new ArrayList<>();
        args.add(key);
        final String invokeReturns = doInvoke(Function.deleteConfiguration.name(), args);
        log.debug(("Payload retrieved: " + invokeReturns));

    }

    @Override
    public String getConfiguration(String key) throws JLedgerClientException {
        if (key.isEmpty()) {
            throw new JLedgerClientException((Function.getConfiguration.name()) + "is in error, No input data!");
        }
        List<String> args = new ArrayList<>();
        args.add(key);
        final String invokeReturns = doInvoke(Function.getConfiguration.name(), args);
        log.debug(("Payload retrieved: " + invokeReturns));
        return invokeReturns;
    }
}
