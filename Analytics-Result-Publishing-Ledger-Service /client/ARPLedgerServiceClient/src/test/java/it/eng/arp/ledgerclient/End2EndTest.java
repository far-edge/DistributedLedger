package it.eng.arp.ledgerclient;

import it.eng.jledgerclient.exception.JLedgerClientException;
import org.hyperledger.fabric.sdk.BlockEvent;
import org.hyperledger.fabric.sdk.ChaincodeEvent;
import org.hyperledger.fabric.sdk.ChaincodeEventListener;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;


import java.io.InputStream;

public class End2EndTest {

    static AnalyticsResultsPublishingImpl oor;
    static ChaincodeEventListener chaincodeEventListener;
    static String oorEvent;

    @BeforeClass
    public static void begin() throws JLedgerClientException {

        InputStream config = ClassLoader.getSystemResourceAsStream("./config-fabric-network.json");
        InputStream cert = ClassLoader.getSystemResourceAsStream("./ca-cert.pem");
        InputStream keystore = ClassLoader.getSystemResourceAsStream("./");
        oor = new AnalyticsResultsPublishingImpl(config, cert, keystore);
        chaincodeEventListener = new ChaincodeEventListener() {
            @Override
            public void received(String handle, BlockEvent blockEvent, ChaincodeEvent chaincodeEvent) {
                String payload = new String(chaincodeEvent.getPayload());
                System.out.println("Event from chaincode: " + chaincodeEvent.getEventName() + " " + payload);

            }
        };
        oorEvent = oor.doRegisterEvent("", chaincodeEventListener);
    }


    @AfterClass
    public static void end() {
        try {
            oor.doUnregisterEvent(oorEvent);
        } catch (JLedgerClientException e) {
            e.printStackTrace();
        }
        oor = null;
    }

    @Test
    public void testOOR() {
        try {
            oor.postResult("key", "test");
            oor.putResult("key", "test after putResult");
            String payload = oor.getResult("key");
            oor.deleteResult("key");
        } catch (JLedgerClientException e) {
            e.printStackTrace();
        }

    }

}