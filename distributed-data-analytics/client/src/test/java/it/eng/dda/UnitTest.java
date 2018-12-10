package it.eng.dda;

import it.eng.dda.base.DDALedgerClientImpl;
import it.eng.jledgerclient.exception.JLedgerClientException;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.hyperledger.fabric.sdk.BlockEvent;
import org.hyperledger.fabric.sdk.ChaincodeEvent;
import org.hyperledger.fabric.sdk.ChaincodeEventListener;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.InputStream;

import static org.junit.Assert.assertFalse;

public class UnitTest {

    static DDALedgerClientImpl ddaLedgerClient;
    static ChaincodeEventListener chaincodeEventListener;
    static String DDAEvent;
    private final static Logger log = LogManager.getLogger(UnitTest.class);

    @BeforeClass
    public static void begin() {
        try {
            InputStream config = ClassLoader.getSystemResourceAsStream("./config-fabric-network.json");
            InputStream cert = ClassLoader.getSystemResourceAsStream("./.PEM NAME");
            InputStream keystore = ClassLoader.getSystemResourceAsStream("./_SK FILE NAME");
            ddaLedgerClient = new DDALedgerClientImpl(config, cert, keystore);
            chaincodeEventListener = new ChaincodeEventListener() {
                @Override
                public void received(String handle, BlockEvent blockEvent, ChaincodeEvent chaincodeEvent) {
                    String payload = new String(chaincodeEvent.getPayload());
                    System.out.println("Event from chaincode: " + chaincodeEvent.getEventName() + " " + payload);

                }
            };
            DDAEvent = ddaLedgerClient.doRegisterEvent("EVENT NAME", chaincodeEventListener);
        } catch (JLedgerClientException e) {
            assertFalse(e.getMessage(), true);
        }
    }

    @AfterClass
    public static void end() {
        try {
            ddaLedgerClient.doUnregisterEvent(DDAEvent);
        } catch (JLedgerClientException e) {
            e.printStackTrace();
        }
        ddaLedgerClient = null;

    }

    @Test
    public void UnitTestAnalyticsInstance() {

        try {
            ddaLedgerClient.createAnalyticsInstance("AI_ID", "AI_NAME", "AI_EGID", "TEST_TEST_TEST_TEST_TEST_TEST_TEST_TEST_TEST_TEST_TEST_TEST");
            ddaLedgerClient.updateAnalyticsInstance("AI_ID", "AI_NAME", "AI_EGID", "TEST_TEST_TEST_TEST_TEST_TEST_TEST_TEST_TEST_TEST_TEST_TEST");


            String payloadBySpecification = ddaLedgerClient.getAnalyticsInstancesBySpecification("AI_ID", "AI_EGID");

            String payloadByID = ddaLedgerClient.getAnalyticsInstanceById("AI_ID");

            String payloadDiscovered = ddaLedgerClient.discoverAnalyticsInstances("", "AI_NAME", "");

            ddaLedgerClient.deleteAnalyticsInstance("AI_ID", "AI_EGID");
            if ((payloadBySpecification.isEmpty() && (payloadByID.isEmpty()) && (payloadDiscovered.isEmpty()))) {
                assertFalse(true);
            }
            assertFalse(false);
        } catch (JLedgerClientException e) {
            assertFalse(true);
        }

    }

    @Test
    public void UnitTestDataSource() {
        try {
            ddaLedgerClient.createDataSource("DS_ID", "DS_NAME", "DS_EGID", "DS_DRID", "TEST_TEST_TEST_TEST_TEST_TEST_TEST_TEST_TEST_TEST_TEST_TEST");
            String discover1 = ddaLedgerClient.discoverDataSources("", "DS_NAME", "", "");
            String discover2 = ddaLedgerClient.discoverDataSources("DS_ID", "", "", "");
            String discover3 = ddaLedgerClient.discoverDataSources("", "", "", "DS_DRID");
            String discover4 = ddaLedgerClient.discoverDataSources("", "", "DS_EGID", "");
            ddaLedgerClient.deleteDataSource("DS_ID", "DS_EGID");
            if ((discover1.isEmpty() && (discover2.isEmpty()) && (discover3.isEmpty())) && (discover4.isEmpty())) {
                assertFalse(true);
            }
            assertFalse(false);

        } catch (JLedgerClientException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void UnitTestEdgeGateway() {
        try {

            ddaLedgerClient.createEdgeGateway("EG_ID", "EG_NAME", "EG_NAMESPACE", "EG_MACADDRESS", "TEST_TEST_TEST_TEST_TEST_TEST_TEST_TEST_TEST_TEST_TEST_TEST");
            ddaLedgerClient.updateEdgeGateway("EG_ID", "EG_NAME", "EG_NAMESPACE", "EG_MACADDRESS", "TEST_TEST_TEST_TEST_TEST_TEST_TEST_TEST_TEST_TEST_TEST_TEST");
            String getEdgeGateway = ddaLedgerClient.getEdgeGateway("EG_ID");
            System.out.println("getEdgeGateway:" + getEdgeGateway);
            String discover1 = ddaLedgerClient.discoverEdgeGateways("", "EG_NAME", "", "");
            String discover2 = ddaLedgerClient.discoverEdgeGateways("", "", "EG_NAMESPACE", "");
            String discover3 = ddaLedgerClient.discoverEdgeGateways("", "", "", "EG_MACADDRESS");
            String discover4 = ddaLedgerClient.discoverEdgeGateways("EG_ID", "", "EG_NAMESPACE", "");
            if ((discover1.isEmpty() && (discover2.isEmpty()) && (discover3.isEmpty())) && (discover4.isEmpty())) {
                assertFalse(true);
            }
            assertFalse(false);
        } catch (JLedgerClientException e) {
            e.printStackTrace();
        }


    }


}