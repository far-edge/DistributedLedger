package it.eng.dda.base;

import it.eng.dda.model.Function;
import it.eng.jledgerclient.exception.JLedgerClientException;
import it.eng.jledgerclient.fabric.HLFLedgerClient;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.hyperledger.fabric.sdk.ChaincodeEventListener;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public final class DDALedgerClientImpl extends HLFLedgerClient implements DDALedgerClient {

    private final static Logger log = LogManager.getLogger(DDALedgerClientImpl.class);


    public DDALedgerClientImpl() throws JLedgerClientException {
        super();
    }

    public DDALedgerClientImpl(InputStream configFabricNetwork, InputStream certificate, InputStream keystore) throws JLedgerClientException {
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

  /*  private String doInvokeByJson(Function fcn, List<String> args) throws JLedgerClientException {
        final InvokeReturn invokeReturn = ledgerInteractionHelper.invokeChaincode(fcn.name(), args);
        try {
            log.debug("BEFORE -> Store Completable Future at " + System.currentTimeMillis());
            invokeReturn.getCompletableFuture().get(configManager.getConfiguration().getTimeout(), TimeUnit.MILLISECONDS);
            log.debug("AFTER -> Store Completable Future at " + System.currentTimeMillis());
            final String payload = invokeReturn.getPayload();
            return payload;
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            log.error(fcn.name().toUpperCase() + " " + e.getMessage());
            throw new JLedgerClientException(fcn.name() + " " + e.getMessage());
        }
    }*/

    @Override
    public void createAnalyticsInstance(String id, String name, String edgeGatewayReferenceID, String payload) throws JLedgerClientException {

        if ((id.isEmpty()) || (edgeGatewayReferenceID.isEmpty()) || payload.isEmpty()) {
            throw new JLedgerClientException(Function.createAnalyticsInstance.name() + "is in error, ID or EdgeGatewayReferenceID or payload are empty");
        }
        List<String> args = new ArrayList<>();
        args.add(id);
        args.add(name);
        args.add(edgeGatewayReferenceID);
        args.add(payload);
        final String invokeReturn = doInvoke(Function.createAnalyticsInstance.name(), args);
        log.debug("Payload retrived: " + invokeReturn);
    }

    @Override
    public void updateAnalyticsInstance(String id, String name, String edgeGatewayReferenceID, String payload) throws JLedgerClientException {
        if ((id.isEmpty()) || (edgeGatewayReferenceID.isEmpty()) || payload.isEmpty()) {
            throw new JLedgerClientException(Function.updateAnalyticsInstance.name() + "is in error, ID or EdgeGatewayReferenceID or payload are empty");
        }
        List<String> args = new ArrayList<>();
        args.add(id);
        args.add(name);
        args.add(edgeGatewayReferenceID);
        args.add(payload);
        final String invokeReturn = doInvoke(Function.updateAnalyticsInstance.name(), args);
        log.debug("Payload retrived: " + invokeReturn);
    }

    @Override
    public void deleteAnalyticsInstance(String id, String edgeGatewayReferenceID) throws JLedgerClientException {

        if ((id.isEmpty()) || (edgeGatewayReferenceID.isEmpty())) {
            throw new JLedgerClientException(Function.deleteAnalyticsInstance.name() + "is in error, ID or EdgeGatewayReferenceID are empty");
        }
        List<String> args = new ArrayList<>();
        args.add(id);
        args.add(edgeGatewayReferenceID);

        final String payload = doInvoke(Function.deleteAnalyticsInstance.name(), args);
        log.debug("Payload retrived: " + payload);
    }

    @Override
    public String getAnalyticsInstanceById(String id) throws JLedgerClientException {
        if (id.isEmpty()) {
            throw new JLedgerClientException(Function.getAnalyticsInstanceById.name() + "is in error, ID is empty");
        }
        List<String> args = new ArrayList<>();
        args.add(id);
        final String payload = doInvoke(Function.getAnalyticsInstanceById.name(), args);
        log.debug("Payload retrived: " + payload);
        return payload;
    }

    @Override
    public String getAnalyticsInstancesBySpecification(String id, String edgeGatewayReferenceID) throws JLedgerClientException {

        if ((id.isEmpty()) || (edgeGatewayReferenceID.isEmpty())) {
            throw new JLedgerClientException(Function.getAnalyticsInstancesBySpecification.name() + "is in error, ID or EdgeGatewayReferenceID are empty");
        }
        List<String> args = new ArrayList<>();
        args.add(id);
        args.add(edgeGatewayReferenceID);
        final String payload = doInvoke(Function.getAnalyticsInstancesBySpecification.name(), args);
        log.debug("Payload retrived: " + payload);
        return payload;
    }

    @Override
    public String discoverAnalyticsInstances(String id, String name, String edgeGatewayReferenceID) throws JLedgerClientException {
        List<String> args = new ArrayList<>();
        args.add(id);
        args.add(name);
        args.add(edgeGatewayReferenceID);
        final String payload = doInvoke(Function.discoverAnalyticsInstances.name(), args);
        log.debug("Payload retrived: " + payload);
        return payload;
    }

    @Override
    public void createDataSource(String id, String name, String edgeGatewayReferenceID, String dataSourceDefinitionReferenceID, String payload) throws JLedgerClientException {

        if ((id.isEmpty()) || (edgeGatewayReferenceID.isEmpty()) || payload.isEmpty()) {
            throw new JLedgerClientException(Function.createDataSource.name() + "is in error, ID or EdgeGatewayReferenceID or payload are empty");
        }
        List<String> args = new ArrayList<>();
        args.add(id);
        args.add(name);
        args.add(edgeGatewayReferenceID);
        args.add(dataSourceDefinitionReferenceID);
        args.add(payload);
        final String invokeReturn = doInvoke(Function.createDataSource.name(), args);
        log.debug("Payload retrived: " + invokeReturn);
    }

    @Override
    public void deleteDataSource(String id, String edgeGatewayReferenceID) throws JLedgerClientException {
        if ((id.isEmpty()) || (edgeGatewayReferenceID.isEmpty())) {
            throw new JLedgerClientException(Function.deleteDataSource.name() + "is in error, ID or EdgeGatewayReferenceID are empty");
        }
        List<String> args = new ArrayList<>();
        args.add(id);
        args.add(edgeGatewayReferenceID);
        final String invokeReturn = doInvoke(Function.deleteAnalyticsInstance.name(), args);
        log.debug("Payload retrived: " + invokeReturn);
    }

    @Override
    public String discoverDataSources(String id, String name, String edgeGatewayReferenceID, String dataSourceDefinitionReferenceID) throws JLedgerClientException {
        List<String> args = new ArrayList<>();
        args.add(id);
        args.add(name);
        args.add(edgeGatewayReferenceID);
        args.add(dataSourceDefinitionReferenceID);
        final String payload = doInvoke(Function.discoverDataSources.name(), args);
        log.debug("Payload retrived: " + payload);
        return payload;
    }

    @Override
    public void createEdgeGateway(String id, String name, String namespace, String macAddress, String payload) throws JLedgerClientException {
        if ((id.isEmpty()) || payload.isEmpty()) {
            throw new JLedgerClientException(Function.createEdgeGateway.name() + "is in error, ID or payload are empty");
        }
        List<String> args = new ArrayList<>();
        args.add(id);
        args.add(name);
        args.add(namespace);
        args.add(macAddress);
        args.add(payload);
        final String invokeReturn = doInvoke(Function.createEdgeGateway.name(), args);
        log.debug("Payload retrived: " + invokeReturn);
    }

    @Override
    public void updateEdgeGateway(String id, String name, String namespace, String macAddress, String payload) throws JLedgerClientException {
        if (id.isEmpty() || payload.isEmpty()) {
            throw new JLedgerClientException(Function.updateEdgeGateway.name() + "is in error, ID or payload are empty");
        }
        List<String> args = new ArrayList<>();
        args.add(id);
        args.add(name);
        args.add(namespace);
        args.add(macAddress);
        args.add(payload);
        final String invokeReturn = doInvoke(Function.updateEdgeGateway.name(), args);
        log.debug("Payload retrived: " + invokeReturn);
    }

    @Override
    public void deleteEdgeGateway(String id) throws JLedgerClientException {
        if (id.isEmpty()) {
            throw new JLedgerClientException(Function.deleteEdgeGateway.name() + "is in error, ID is empty");
        }
        List<String> args = new ArrayList<>();
        args.add(id);
        final String invokeReturn = doInvoke(Function.deleteEdgeGateway.name(), args);
        log.debug("Payload retrived: " + invokeReturn);
    }

    @Override
    public String getEdgeGateway(String id) throws JLedgerClientException {
        if (id.isEmpty()) {
            throw new JLedgerClientException(Function.getEdgeGateway.name() + "is in error, ID is empty");
        }
        List<String> args = new ArrayList<>();
        args.add(id);
        final String invokeReturn = doInvoke(Function.getEdgeGateway.name(), args);
        log.debug("Payload retrived: " + invokeReturn);
        return invokeReturn;
    }

    @Override
    public String discoverEdgeGateways(String id, String name, String namespace, String macAddress) throws JLedgerClientException {
        List<String> args = new ArrayList<>();
        args.add(id);
        args.add(name);
        args.add(namespace);
        args.add(macAddress);
        final String payload = doInvoke(Function.discoverEdgeGateways.name(), args);
        log.debug("Payload retrived: " + payload);
        return payload;
    }
}
