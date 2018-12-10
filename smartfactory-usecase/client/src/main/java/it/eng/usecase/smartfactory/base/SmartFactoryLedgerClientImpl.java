package it.eng.usecase.smartfactory.base;

import it.eng.jledgerclient.exception.JLedgerClientException;
import it.eng.jledgerclient.fabric.HLFLedgerClient;
import it.eng.jledgerclient.fabric.helper.InvokeReturn;
import it.eng.jledgerclient.fabric.helper.QueryReturn;
import it.eng.jledgerclient.fabric.utils.JsonConverter;
import it.eng.usecase.smartfactory.model.Function;
import it.eng.usecase.smartfactory.model.OrderCommand;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.hyperledger.fabric.sdk.ChaincodeEventListener;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public final class SmartFactoryLedgerClientImpl extends HLFLedgerClient implements SmartFactoryLedgerClient {

    private final static Logger log = LogManager.getLogger(SmartFactoryLedgerClientImpl.class);


    public SmartFactoryLedgerClientImpl() throws JLedgerClientException {
        super();
    }

    public SmartFactoryLedgerClientImpl(InputStream configFabricNetwork, InputStream certificate, InputStream keystore) throws JLedgerClientException {
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

    private String doInvokeByJson(Function fcn, List<String> args) throws JLedgerClientException {
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
    }

    private String doQueryByJson(Function fcn, List<String> args) throws JLedgerClientException {
        String data = "";
        try {
            final List<QueryReturn> queryReturns = ledgerInteractionHelper.queryChainCode(fcn.name(), args, null);
            for (QueryReturn queryReturn : queryReturns) {
                data += queryReturn.getPayload();
            }
            return data;
        } catch (Exception e) {
            log.error(fcn.name() + " " + e.getMessage());
            throw new JLedgerClientException(fcn.name() + " " + e.getMessage());
        }
    }

    @Override
    public void storeOrderCommand(OrderCommand orderCommand) throws JLedgerClientException {

        if (orderCommand == null) {
            throw new JLedgerClientException(Function.storeOrderCommand.name() + "is in error, No input data!");

        }
        String json = JsonConverter.convertToJson(orderCommand);
        List<String> args = new ArrayList<>();
        args.add(json);
        final String payload = doInvokeByJson(Function.storeOrderCommand, args);
        log.debug(("Payload retrieved: " + payload));
    }

    @Override
    public Collection<OrderCommand> getAllOrderCommand() throws JLedgerClientException {

        List<String> args = new ArrayList<>();
        final String payload = doQueryByJson(Function.getAllOrderCommand, args);
        log.debug("Payload retrieved: " + payload);
        final Collection<OrderCommand> orderCommandCollection = (Collection<OrderCommand>) JsonConverter.convertFromJson(payload, OrderCommand.class, true);
        return orderCommandCollection;
    }

    @Override
    public Collection<OrderCommand> getOrderCommandByOrder(String order) throws JLedgerClientException {

        if (order.isEmpty()) {
            throw new JLedgerClientException(Function.getOrderCommandByOrder.name() + "is in error, No input data!");
        }
        List<String> args = new ArrayList<>();
        args.add(order);
        final String payload = doQueryByJson(Function.getOrderCommandByOrder, args);
        final Collection<OrderCommand> orderCommandCollection = (Collection<OrderCommand>) JsonConverter.convertFromJson(payload, OrderCommand.class, true);
        return orderCommandCollection;
    }

    @Override
    public Collection<OrderCommand> getOrderCommandByCustomer(String customer) throws JLedgerClientException {
        if (customer.isEmpty()) {
            throw new JLedgerClientException(Function.getOrderCommandByOrder.name() + "is in error, No input data!");
        }
        List<String> args = new ArrayList<>();
        args.add(customer);
        final String payload = doQueryByJson(Function.getOrderCommandByCustomer, args);
        final Collection<OrderCommand> orderCommandCollection = (Collection<OrderCommand>) JsonConverter.convertFromJson(payload, OrderCommand.class, true);
        return orderCommandCollection;
    }

    @Override
    public OrderCommand getOrderCommand(String order, String customer) throws JLedgerClientException {
        if (customer.isEmpty() && order.isEmpty()) {
            throw new JLedgerClientException(Function.getOrderCommand.name() + "is in error, No input data!");
        }
        List<String> args = new ArrayList<>();
        args.add(customer);
        args.add(order);
        final String payload = doQueryByJson(Function.getOrderCommand, args);
        final OrderCommand orderCommand = (OrderCommand) JsonConverter.convertFromJson(payload, OrderCommand.class, false);
        return orderCommand;
    }
}
