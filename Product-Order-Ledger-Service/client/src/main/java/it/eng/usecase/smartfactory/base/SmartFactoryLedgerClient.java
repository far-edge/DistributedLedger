package it.eng.usecase.smartfactory.base;

import it.eng.jledgerclient.exception.JLedgerClientException;
import it.eng.jledgerclient.fabric.HLFLedgerClient;
import it.eng.usecase.smartfactory.model.OrderCommand;

import java.util.Collection;

public interface SmartFactoryLedgerClient {


    void storeOrderCommand(OrderCommand orderCommand) throws JLedgerClientException;

    Collection<OrderCommand> getAllOrderCommand() throws JLedgerClientException;

    Collection<OrderCommand> getOrderCommandByOrder(String order) throws JLedgerClientException;

    Collection<OrderCommand> getOrderCommandByCustomer(String customer) throws JLedgerClientException;

    OrderCommand getOrderCommand(String order, String customer) throws JLedgerClientException;

}
