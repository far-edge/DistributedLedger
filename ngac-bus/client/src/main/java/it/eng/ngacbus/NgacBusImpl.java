package it.eng.ngacbus;

import it.eng.jledgerclient.exception.JLedgerClientException;
import it.eng.jledgerclient.fabric.HLFLedgerClient;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.hyperledger.fabric.sdk.ChaincodeEventListener;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * @author clod16
 */
public class NgacBusImpl extends HLFLedgerClient implements NgacBus {
    private final static Logger log = LogManager.getLogger(NgacBusImpl.class);

    public NgacBusImpl() throws JLedgerClientException {
    }

    NgacBusImpl(InputStream configFabricNetwork, InputStream certificate, InputStream keystore) throws JLedgerClientException {
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
    public void post(String key, String payload) throws JLedgerClientException {

        if (key.isEmpty() || payload.isEmpty()) {
            throw new JLedgerClientException((Function.post.name()) + "is in error, No input data!");
        }
        List<String> args = new ArrayList<>();
        args.add(key);
        args.add(payload);
        final String invokeReturns = doInvoke(Function.post.name(), args);
        log.debug(("Payload retrieved: " + invokeReturns));
    }

    @Override
    public void put(String key, String payload) throws JLedgerClientException {
        if (key.isEmpty() || payload.isEmpty()) {
            throw new JLedgerClientException((Function.put.name()) + "is in error, No input data!");
        }
        List<String> args = new ArrayList<>();
        args.add(key);
        args.add(payload);
        final String invokeReturns = doInvoke(Function.put.name(), args);
        log.debug(("Payload retrieved: " + invokeReturns));
    }

    @Override
    public void delete(String key) throws JLedgerClientException {
        if (key.isEmpty()) {
            throw new JLedgerClientException((Function.delete.name()) + "is in error, No input data!");
        }
        List<String> args = new ArrayList<>();
        args.add(key);
        final String invokeReturns = doInvoke(Function.delete.name(), args);
        log.debug(("Payload retrieved: " + invokeReturns));

    }

    @Override
    public String get(String key) throws JLedgerClientException {
        if (key.isEmpty()) {
            throw new JLedgerClientException((Function.get.name()) + "is in error, No input data!");
        }
        List<String> args = new ArrayList<>();
        args.add(key);
        final String invokeReturns = doInvoke(Function.get.name(), args);
        log.debug(("Payload retrieved: " + invokeReturns));
        return invokeReturns;
    }
}
