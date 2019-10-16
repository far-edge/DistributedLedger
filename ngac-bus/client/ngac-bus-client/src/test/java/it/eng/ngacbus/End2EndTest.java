package it.eng.ngacbus;

import it.eng.jledgerclient.exception.JLedgerClientException;
import org.hyperledger.fabric.sdk.ChaincodeEventListener;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.InputStream;
import java.time.LocalDateTime;

/**
 * @author clod16
 */
public class End2EndTest {
    private static NgacBusImpl ngacBus;
    private final static String ngacEventChaincode = "ngacBusEvent_EDIT";
    private static String eventFromChaincode;

    @BeforeClass
    public static void begin() throws JLedgerClientException {
        InputStream config = ClassLoader.getSystemResourceAsStream("./config-fabric-network.json");
        InputStream cert = ClassLoader.getSystemResourceAsStream("./ca-cert.pem");
        InputStream keystore = ClassLoader.getSystemResourceAsStream("./keystore");
        ngacBus = new NgacBusImpl(config, cert, keystore);
        ChaincodeEventListener chaincodeEventListener = (handle, blockEvent, chaincodeEvent) -> {
            String payload = new String(chaincodeEvent.getPayload());
            System.out.println("Event from chaincode: " + LocalDateTime.now() + "  " + chaincodeEvent.getEventName() + " " + payload);
        };
        eventFromChaincode = ngacBus.doRegisterEvent(ngacEventChaincode, chaincodeEventListener);
    }

    @AfterClass
    public static void end() {
        try {
            ngacBus.doUnregisterEvent(eventFromChaincode);
        } catch (JLedgerClientException e) {
            e.printStackTrace();
        }
        ngacBus = null;
    }

    @Test
    public void testNgacBusClient() {
        try {
            ngacBus.post("key", "test");
            ngacBus.put("key", "test after put");
            String payload = ngacBus.get("key");
            System.out.println("payload: " + payload);
            ngacBus.delete("key");

        } catch (
                JLedgerClientException e) {
            e.printStackTrace();
        }
    }

    /**
     * Use only for test Events receiving
     */
//    @Test
    public void waitForEvents() {
        for(;;) {
        }
    }
}