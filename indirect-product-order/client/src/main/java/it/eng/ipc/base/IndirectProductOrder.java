package it.eng.ipc.base;

import it.eng.jledgerclient.exception.JLedgerClientException;
import it.eng.jledgerclient.fabric.HLFLedgerClient;
import it.eng.ipc.model.OrderCommand;

import java.util.Collection;

public interface IndirectProductOrder {


    void storeOrderCommand(OrderCommand orderCommand) throws JLedgerClientException;

    Collection<OrderCommand> getAllOrderCommand() throws JLedgerClientException;

    Collection<OrderCommand> getOrderCommandByOrder(String order) throws JLedgerClientException;

    Collection<OrderCommand> getOrderCommandByCustomer(String customer) throws JLedgerClientException;

    OrderCommand getOrderCommand(String order, String customer) throws JLedgerClientException;

}
