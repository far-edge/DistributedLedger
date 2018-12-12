/**
 * @author ascatox
 */
package eu.faredge.smartledger.client;

import com.fasterxml.jackson.core.JsonProcessingException;
import eu.faredge.dm.dsm.DSM;
import eu.faredge.smartledger.client.base.ISmartLedgerClient;
import eu.faredge.smartledger.client.exception.SmartLedgerClientException;
import eu.faredge.smartledger.client.utils.Utils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.List;
import java.util.Random;

import static org.hamcrest.CoreMatchers.not;
import static org.junit.Assert.*;

public class End2EndTestSmartLedgerClientDSM {

    static ISmartLedgerClient client = null;
    private static final Log logger = LogFactory.getLog(End2EndTestSmartLedgerClientDSM.class);


    @BeforeClass
    public static void begin() {
        client = new SmartLedgerClient();
    }

    @AfterClass
    public static void end() {
        client = null;
    }

    @Test
    public void testGetDataSourceManifestById() {
        DSM dsm = null;
        try {
            dsm = doRegisterDSM();
            DSM dataSourceManifestByUri = client.getDataSourceManifestById(dsm.getId());
            assertNotNull(dataSourceManifestByUri);
            assertFalse(StringUtils.isEmpty(dataSourceManifestByUri.getId()));
        } catch (SmartLedgerClientException e) {
            assertFalse(e.getMessage(), true);
        } catch (Exception e) {
            assertFalse(e.getMessage(), true);
        } finally {
            doRemoveDSM(dsm);
        }
    }

    @Test
    public void testGetDataSourceManifestByMacAddress() {
        DSM dsm = null;
        try {
            dsm = doRegisterDSM();
            DSM dataSourceManifestByMacAddress = client.getDataSourceManifestByMacAddress(dsm.getMacAddress());
            assertNotNull(dataSourceManifestByMacAddress);
            assertFalse(StringUtils.isEmpty(dataSourceManifestByMacAddress.getId()));
        } catch (SmartLedgerClientException e) {
            assertFalse(e.getMessage(), true);
        } finally {
            doRemoveDSM(dsm);
        }
    }

    @Test
    public void testGetDataSourceManifestByDSD() {
        DSM dsm = null;
        try {
            dsm = doRegisterDSM();
            DSM dataSourceManifestByDSD = client.getDataSourceManifestByDSD(dsm.getDataSourceDefinitionID());
            assertNotNull(dataSourceManifestByDSD);
            assertFalse(StringUtils.isEmpty(dataSourceManifestByDSD.getId()));
        } catch (SmartLedgerClientException e) {
            assertFalse(e.getMessage(), true);
        } finally {
            doRemoveDSM(dsm);
        }
    }

    @Test
    public void testGetAllDataSourceManifests() {
        try {
            List<DSM> allDSMs = client.getAllDataSourceManifests();
            assertNotNull(allDSMs);
            assertFalse(allDSMs.isEmpty());
        } catch (SmartLedgerClientException e) {
            assertFalse(e.getMessage(), true);
        }
    }


    @Test
    public void testRegisterDSM() {
        DSM dsm = null;
        try {
            dsm = doRegisterDSM();
            DSM dataSourceManifestById = client.getDataSourceManifestById(dsm.getId());
            assertNotNull(dataSourceManifestById);
            assertFalse(dataSourceManifestById.getId().isEmpty());
            assertFalse(dataSourceManifestById.getDataSourceDefinitionID().isEmpty());
        } catch (SmartLedgerClientException e) {
            assertFalse(e.getMessage(), true);
        } finally {
            doRemoveDSM(dsm);
        }
    }

    @Test
    public void testRemoveDSM() {
        try {
            DSM dsm = doRegisterDSM();
            doRemoveDSM(dsm);
            DSM dsmBack = null;
            try {
                dsmBack = client.getDataSourceManifestById(dsm.getId());
            } catch (SmartLedgerClientException e) {
                assertFalse(e.getMessage(), true);
            } catch (Exception e) {
                assertFalse(e.getMessage(), true); e.printStackTrace();
            }
            assertTrue(null == dsmBack.getId() || dsmBack.getId().isEmpty());
        } catch (SmartLedgerClientException e) {
            assertFalse(e.getMessage(), true);
        }
    }


    @Test
    public void testEditRegisteredDSMWhenIsPresent() {
        DSM dsmBack = null;
        DSM dsmBack2 = null;
        try {
            DSM dsm = doRegisterDSM();
            dsmBack = client.getDataSourceManifestById(dsm.getId());
            assertTrue(Utils.areEquals(dsm, dsmBack));
            Random random = new Random();
            dsm.setMacAddress("b8:e8:56:41:43:06:" + Math.abs(random.nextInt(100)));
            client.editRegisteredDSM(dsm);
            dsmBack2 = client.getDataSourceManifestById(dsm.getId());
            assertTrue(!Utils.areEquals(dsmBack2, dsmBack));
        } catch (SmartLedgerClientException e) {
            assertFalse(e.getMessage(), true);
        } catch (JsonProcessingException e) {
            assertFalse(e.getMessage(), true);
        } finally {
            doRemoveDSM(dsmBack);
        }
    }

    public static DSM init() {
        Random random = new Random();
        DSM dsm = new DSM();
        dsm.setDataSourceDefinitionID("manifest_dsd_plant_" + Math.abs(random.nextInt(10)));
        dsm.setId(new String());
        dsm.setMacAddress("b8:e8:56:41:43:06:" + Math.abs(random.nextInt(100)));
        setDataDefinitionParameters(dsm);
        return dsm;
    }

    private static void setDataDefinitionParameters(DSM dsm) {
        DSM.DataSourceDefinitionParameters.Parameters parameters = new DSM.DataSourceDefinitionParameters
                .Parameters();
        parameters.setKey("connection");
        parameters.setValue("connectionVals:21121");
        DSM.DataSourceDefinitionParameters definitionParameters = new DSM.DataSourceDefinitionParameters();
        definitionParameters.getParameters().add(parameters);
        dsm.setDataSourceDefinitionParameters(definitionParameters);
    }

    public static DSM doRegisterDSM() throws SmartLedgerClientException {
        DSM dsm = init();
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
}
