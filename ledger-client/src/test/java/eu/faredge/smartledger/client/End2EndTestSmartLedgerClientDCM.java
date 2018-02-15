/**
 * @author ascatox
 */
package eu.faredge.smartledger.client;

import com.fasterxml.jackson.core.JsonProcessingException;
import eu.faredge.dm.dcm.DCM;
import eu.faredge.dm.dsm.DSM;
import eu.faredge.smartledger.client.base.ISmartLedgerClient;
import eu.faredge.smartledger.client.exception.SmartLedgerClientException;
import eu.faredge.smartledger.client.utils.Utils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static org.junit.Assert.*;

public class End2EndTestSmartLedgerClientDCM {

    static ISmartLedgerClient client = null;
    private static List<DSM> dsmsToRemove = new ArrayList<>();
    private static final Log logger = LogFactory.getLog(End2EndTestSmartLedgerClientDCM.class);

    @BeforeClass
    public static void begin() {
        client = new SmartLedgerClient();
    }

    @AfterClass
    public static void end() {
        client = null;
    }


    @Test
    public void testGetDataConsumerManifestById() {
        DCM dcm = null;
        try {
            dcm = doRegisterDCM();
            DCM dataConsumerManifestById = client.getDataConsumerManifestById(dcm.getId());
            assertNotNull(dataConsumerManifestById);
            assertFalse(StringUtils.isEmpty(dataConsumerManifestById.getId()));
        } catch (SmartLedgerClientException e) {
            assertFalse(e.getMessage(), true);
        } catch (Exception e) {
            assertFalse(e.getMessage(), true);
        } finally {
            doRemoveDCM(dcm);
        }
    }


    @Test
    public void testGetDataConsumerManifestByMacAddress() {
        DCM dcm = null;
        try {
            dcm = doRegisterDCM();
            DCM dataConsumerManifestByMacAddress = client.getDataConsumerManifestByMacAddress(dcm.getMacAddress());
            assertNotNull(dataConsumerManifestByMacAddress);
            assertFalse(StringUtils.isEmpty(dataConsumerManifestByMacAddress.getId()));
        } catch (SmartLedgerClientException e) {
            assertFalse(e.getMessage(), true);
        } finally {
            doRemoveDCM(dcm);
        }
    }

       @Test
    public void testGetAllDataConsumerManifests() {
        try {
            List<DCM> all = client.getAllDataConsumerManifests();
            assertNotNull(all);
            assertFalse(all.isEmpty());
        } catch (SmartLedgerClientException e) {
            assertFalse(e.getMessage(), true);
        }
    }

    @Test
    public void testRegisterDCM() {
        DCM dcm = null;
        try {
            dcm = doRegisterDCM();
            DCM dataConsumerManifestById = client.getDataConsumerManifestById(dcm.getId());
            assertNotNull(dataConsumerManifestById);
            assertFalse(dataConsumerManifestById.getId().isEmpty());
            assertFalse(dataConsumerManifestById.getDataSourceDefinitionsIDs().isEmpty());
        } catch (SmartLedgerClientException e) {
            assertFalse(e.getMessage(), true);
        } finally {
            doRemoveDCM(dcm);
        }
    }


    @Test
    public void testRemoveDCM() {
        try {
            DCM dcm = doRegisterDCM();
            doRemoveDCM(dcm);
            DCM dcmBack = null;
            try {
                dcmBack = client.getDataConsumerManifestById(dcm.getId());
            } catch (SmartLedgerClientException e) {
                assertFalse(e.getMessage(), true);
            } catch (Exception e) {
                assertFalse(e.getMessage(), true); e.printStackTrace();
            }
            assertTrue(null == dcmBack.getId() || dcmBack.getId().isEmpty());
        } catch (SmartLedgerClientException e) {
            assertFalse(e.getMessage(), true);
        }
    }


    @Test
    public void testEditRegisteredDCMWhenIsPresent() {
        DCM dcmBack = null;
        DCM dcmBack2 = null;
        try {
            DCM dcm = doRegisterDCM();
            dcmBack = client.getDataConsumerManifestById(dcm.getId());
            assertTrue(Utils.areEquals(dcm, dcmBack));
            Random random = new Random();
            dcm.setMacAddress("b8:e8:56:41:43:06:" + Math.abs(random.nextInt(100)));
            client.editRegisteredDCM(dcm);
            dcmBack2 = client.getDataConsumerManifestById(dcm.getId());
            assertTrue(!Utils.areEquals(dcmBack2, dcmBack));
        } catch (SmartLedgerClientException e) {
            assertFalse(e.getMessage(), true);
        } catch (JsonProcessingException e) {
            assertFalse(e.getMessage(), true);
        } finally {
            doRemoveDCM(dcmBack2);
        }
    }

    @Test
    public void testGetCompatibleDSM() {
        try {
            DCM dcm = doRegisterDCM();
            List<DSM> allDSMs = client.getCompatibleDSM(dcm);
            assertNotNull(allDSMs);
            assertFalse("Empty Result!", allDSMs.isEmpty());
        } catch (SmartLedgerClientException e) {
            assertFalse(e.getMessage(), true);
        }
    }


    public static DCM init() throws SmartLedgerClientException {
        Random random = new Random();
        DCM dcm = new DCM();
        dcm.setId(new String());
        dcm.setMacAddress("f8:d8:53:21:32:09:" + Math.abs(random.nextInt(100)));
        DSM dsm = doRegisterDSM();
        dcm.getDataSourceDefinitionsIDs().add(dsm.getDataSourceDefinitionID());
        DSM dsmOne = doRegisterDSM();
    /*    Decommentare per fare test con un DCM con una lista di DSD composta da due elementi (invece di uno soltanto)*/
    /*    dcm.getDataSourceDefinitionsIDs().add(dsmOne.getDataSourceDefinitionID()); */
        dsmsToRemove.add(dsm);
        dsmsToRemove.add(dsmOne);
        return dcm;
    }

    public static DCM doRegisterDCM() throws SmartLedgerClientException {
        DCM dcm = init();
        String id = client.registerDCM(dcm);
        dcm.setId(id);
        return dcm;
    }

    private void doRemoveDCM(DCM dcm) {
        try {
            client.removeDCM(dcm.getId());
        } catch (SmartLedgerClientException e) {
            logger.error(e);
        }
    }

    public static DSM doRegisterDSM() throws SmartLedgerClientException {
        DSM dsm = End2EndTestSmartLedgerClientDSM.init();
        String id = client.registerDSM(dsm);
        dsm.setId(id);
        return dsm;
    }

    public static void doRemoveDSM(DSM dsm) {
        try {
            client.removeDSM(dsm.getId());
        } catch (SmartLedgerClientException e) {
            logger.error(e);
        }
    }

    @After
    public void tearDown() {
        try {
            for (DSM dsm : dsmsToRemove) {
                doRemoveDSM(dsm);
            }
            dsmsToRemove.clear();
        } catch (Exception e) {
            logger.warn("Final DSM Cleaning...\n");
            logger.warn(e);
        }
    }


}
