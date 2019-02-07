package it.eng.ipc;

import it.eng.jledgerclient.exception.JLedgerClientException;
import it.eng.ipc.base.IndirectProductOrderImpl;
import it.eng.ipc.model.OrderCommand;
import org.hyperledger.fabric.sdk.BlockEvent;
import org.hyperledger.fabric.sdk.ChaincodeEvent;
import org.hyperledger.fabric.sdk.ChaincodeEventListener;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.InputStream;
import java.util.Collection;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.*;

public class End2ndTest {

    static IndirectProductOrderImpl smartFactoryLedgerClient;
    static ChaincodeEventListener chaincodeEventListener;
    static String smartfactoryUsecaseEvent;

    @BeforeClass
    public static void begin() {
        try {
            InputStream config = ClassLoader.getSystemResourceAsStream("./config-fabric-network.json");
            InputStream cert = ClassLoader.getSystemResourceAsStream("./ca-cert.pem");
            InputStream keystore = ClassLoader.getSystemResourceAsStream("./5366960a102f9db91f686c8f43004ff0ce29875c93ed72f2fa6dce90731aad4c_sk");
            smartFactoryLedgerClient = new IndirectProductOrderImpl(config, cert, keystore);
            chaincodeEventListener = new ChaincodeEventListener() {
                @Override
                public void received(String handle, BlockEvent blockEvent, ChaincodeEvent chaincodeEvent) {
                    String payload = new String(chaincodeEvent.getPayload());
                    System.out.println("Event from chaincode: " + chaincodeEvent.getEventName() + " " + payload);

                }
            };
            smartfactoryUsecaseEvent = smartFactoryLedgerClient.doRegisterEvent("SMARTFACTORY_USECASE_EVENT", chaincodeEventListener);
        } catch (JLedgerClientException e) {
            assertFalse(e.getMessage(), true);
        }
    }

    @AfterClass
    public static void end() {
        try {
            smartFactoryLedgerClient.doUnregisterEvent(smartfactoryUsecaseEvent);
        } catch (JLedgerClientException e) {
            e.printStackTrace();
        }
        smartFactoryLedgerClient = null;

    }

    @Test
    public void testStoreGetOrderCommand() {

        OrderCommand orderCommand = new OrderCommand();
        orderCommand.setCustomer("Alpha");
        orderCommand.setOrder("Beta");
        orderCommand.setFirstName("Gamma");
        try {

            smartFactoryLedgerClient.storeOrderCommand(orderCommand);
            OrderCommand orderCommand1 = smartFactoryLedgerClient.getOrderCommand("Beta", "Alpha");
            //Collection<OrderCommand> orderCommandCollection2 = smartFactoryLedgerClient.getOrderCommandByOrder("Beta");
            Collection<OrderCommand> orderCommandCollection2 = smartFactoryLedgerClient.getOrderCommandByCustomer("Alpha");

            String firstName = orderCommand1.getFirstName();
            if ((orderCommandCollection2 != null && !orderCommandCollection2.isEmpty() && (firstName.equals("Gamma")))) {
                assertFalse(false);

            } else {
                assertFalse(true);
            }

        } catch (JLedgerClientException e) {
            e.printStackTrace();
            assertFalse(true);
        }
    }


    @Test
    public void testGetBy() {
        OrderCommand orderCommand = new OrderCommand();
        orderCommand.setCustomer("Alpha");
        orderCommand.setOrder("Beta");
        orderCommand.setFirstName("Gamma");
        OrderCommand orderCommand1 = new OrderCommand();
        orderCommand1.setCustomer("Alpha");
        orderCommand1.setOrder("Omega");
        OrderCommand orderCommand2 = new OrderCommand();
        orderCommand2.setCustomer("Delta");
        orderCommand2.setOrder("Beta");
        try {
            smartFactoryLedgerClient.storeOrderCommand(orderCommand);
            smartFactoryLedgerClient.storeOrderCommand(orderCommand1);
            smartFactoryLedgerClient.storeOrderCommand(orderCommand2);
            TimeUnit.SECONDS.sleep(2);
            Collection<OrderCommand> orderCommandCollection = smartFactoryLedgerClient.getAllOrderCommand();
            Collection<OrderCommand> orderCommandCollection1 = smartFactoryLedgerClient.getOrderCommandByCustomer("Alpha");
            Collection<OrderCommand> orderCommandCollection2 = smartFactoryLedgerClient.getOrderCommandByOrder("Beta");
            if ((orderCommandCollection != null && !orderCommandCollection.isEmpty()) && (orderCommandCollection1 != null && !orderCommandCollection1.isEmpty()) && (orderCommandCollection2 != null && !orderCommandCollection2.isEmpty())) {
                assertTrue(true);
            } else {
                Assert.fail();
            }

        } catch (JLedgerClientException e) {
            e.printStackTrace();
            assertFalse(true);
        } catch (InterruptedException e) {
            e.printStackTrace();
            assertFalse(true);

        }
    }

}