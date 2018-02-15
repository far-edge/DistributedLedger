package eu.faredge.smartledger.client;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import eu.faredge.dm.dcd.DCD;
import eu.faredge.dm.dcm.DCM;
import eu.faredge.dm.dsm.DSM;
import eu.faredge.smartledger.client.base.ISmartLedgerClient;
import eu.faredge.smartledger.client.exception.SmartLedgerClientException;
import eu.faredge.smartledger.client.helper.SmartLedgerClientHelper;
import eu.faredge.smartledger.client.helper.InvokeReturn;
import eu.faredge.smartledger.client.model.Org;
import eu.faredge.smartledger.client.utils.Config;
import eu.faredge.smartledger.client.utils.Utils;
import eu.faredge.smartledger.client.utils.Validator;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.concurrent.ConcurrentException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hyperledger.fabric.sdk.BlockEvent;
import org.hyperledger.fabric.sdk.Channel;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;

public class SmartLedgerClient implements ISmartLedgerClient {


    private static final Log logger = LogFactory.getLog(SmartLedgerClient.class);
    public static final int TIMEOUT = Config.TIMEOUT;
    /**
     * Mappings of Strings used inside the chaincode to execute the functions.
     */
    private static final String Q_GET_DSM_BY_MAC_ADD = "qGetDSMByMacAdd";
    private static final String Q_GET_DSM_BY_DSD = "qGetDSMByDSD";
    private static final String Q_GET_ALL_DS_MS = "qGetAllDSMs";
    private static final String Q_GET_ALL_DSMS_BY_DSDS = "qGetAllDSMsByDSDs";
    private static final String Q_GET_ALL_DC_MS = "qGetAllDCMs";
    private static final String Q_GET_ALL_DCD_MS = "qGetAllDCDs";
    private static final String I_EDIT_DCM = "iEditDCM";
    private static final String I_REMOVE_DSM = "iRemoveDSM";
    private static final String I_REMOVE_DCM = "iRemoveDCM";
    private static final String I_EDIT_DCD = "iEditDCD";
    private static final String I_REMOVE_DCD = "iRemoveDCD";
    private static final String Q_GET_DSM_BY_ID = "qGetDSMById";
    private static final String Q_GET_DCM_BY_MAC_ADD = "qGetDCMByMacAdd";
    private static final String Q_GET_DCM_BY_ID = "qGetDCMById";
    private static final String Q_GET_DCD_BY_ID = "qGetDCDById";
    private static final String I_EDIT_DSM = "iEditDSM";

    private SmartLedgerClientHelper helper;
    private Channel channel;
    private Validator validator;
    private static List<Org> orgs;
    private static final Config CONFIG = Config.getConfig();

    public SmartLedgerClient() {
        doSmartLedgerClient(Config.channelName, null, null);
    }

    public SmartLedgerClient(String username) {
        doSmartLedgerClient(Config.channelName, username, null);
    }

    public SmartLedgerClient(String channelName, String username) {
        doSmartLedgerClient(channelName, username, null);
    }

    public SmartLedgerClient(String channelName, String username, String enrollmentSecret) {
        doSmartLedgerClient(channelName, username, enrollmentSecret);
    }

    private void doSmartLedgerClient(String channelName, String username, String enrollmentSecret) {
        try {
            validator = new Validator();
            orgs = new ArrayList<>();
            helper = new SmartLedgerClientHelper();
            orgs.addAll(CONFIG.getIntegrationTestsSampleOrgs());
            for (Org org : orgs) {
                helper.checkConfig(org);
                helper.setup(org, username, enrollmentSecret);
                channel = helper.initializeChannel(channelName, org);
            }
        } catch (Exception e) {
                Utils.fail(e.getMessage());
        }
    }


    /**
     * @param instantiate
     * @param upgrade
     * @throws Exception
     * @exclude Installation function for the chaincode link @
     */
    //@Override
    public void installChaincode(boolean instantiate, boolean upgrade) throws SmartLedgerClientException {
        SmartLedgerClientHelper.installChaincode(channel, orgs.get(0)); //TODO Only the first
        Utils.out("Chaincode installed correctly!!!");
        if (instantiate) {
            instantiateOrUpgradeChaincode(upgrade);
        }
    }

    /**
     * @param isUpgrade
     * @throws Exception
     * @exclude
     */
    //@Override
    public void instantiateOrUpgradeChaincode(boolean isUpgrade) throws SmartLedgerClientException {
        String[] args = {};
        CompletableFuture<BlockEvent.TransactionEvent> transactionEventCompletableFuture = null;
        try {
            transactionEventCompletableFuture = SmartLedgerClientHelper
                    .instantiateOrUpgradeChaincode(channel, args, isUpgrade);

            BlockEvent.TransactionEvent event = null;
            transactionEventCompletableFuture.get(TIMEOUT, TimeUnit.SECONDS);
        } catch (TimeoutException | InterruptedException | ExecutionException e) {
            Utils.out(e.getMessage());
        } catch (Exception e) {
            Utils.out(e.getMessage());
            throw new SmartLedgerClientException(e);
        }
    }

    /**
     * @param id
     * @return
     * @throws Exception
     */
    @Override
    public DSM getDataSourceManifestById(String id) throws SmartLedgerClientException {
        if (StringUtils.isEmpty(id))
            throw new IllegalArgumentException("Error in method getDataSourceManifestById " +
                    "id " +
                    "cannot be empty");
        String[] args = {id};
        final List<String[]> payloads = SmartLedgerClientHelper.queryChainCode(channel, Q_GET_DSM_BY_ID, args);
        List<DSM> dsms = Utils.extractDSMFromPayloads(payloads);

        if (dsms.isEmpty()) {
            logger.warn("No DSM retrieved from getDataSourceManifestByUri with URI: '" + id + "'");
            return new DSM();
        }
        return dsms.get(0);
    }

    /**
     * @param macAddress
     * @return
     * @throws Exception
     */
    @Override
    public DSM getDataSourceManifestByMacAddress(String macAddress) throws SmartLedgerClientException {
        if (StringUtils.isEmpty(macAddress))
            throw new IllegalArgumentException("Error in method getDataSourceManifestByMacAddress " +
                    "macAddress " +
                    "cannot be empty");
        String[] args = {macAddress};
        final List<String[]> payloads = SmartLedgerClientHelper.queryChainCode(channel, Q_GET_DSM_BY_MAC_ADD, args);
        List<DSM> dsms = Utils.extractDSMFromPayloads(payloads);
        if (dsms.isEmpty()) {
            logger.warn("No DSM retrieved from getDataSourceManifestByUri with MAC Address: '" + macAddress + "'");
            return new DSM();
        }
        return dsms.get(0);
    }

    /**
     * @param dsdId
     * @return
     * @throws Exception
     */
    @Override
    public DSM getDataSourceManifestByDSD(String dsdId) throws SmartLedgerClientException {
        if (StringUtils.isEmpty(dsdId))
            throw new IllegalArgumentException("Error in method getDataSourceManifestByDSD " +
                    "DSD " +
                    "cannot be empty");
        String[] args = {dsdId};
        final List<String[]> payloads = SmartLedgerClientHelper.queryChainCode(channel, Q_GET_DSM_BY_DSD, args);
        List<DSM> dsms = Utils.extractDSMFromPayloads(payloads);
        if (dsms.isEmpty()) {
            logger.warn("No DSM retrieved from getDataSourceManifestByDSD with DSDid: '" + dsdId + "'");
            return new DSM();
        }
        return dsms.get(0);
    }


    /**
     * @param macAddress
     * @return
     * @throws Exception
     */
    @Override
    public DCM getDataConsumerManifestByMacAddress(String macAddress) throws SmartLedgerClientException {
        if (StringUtils.isEmpty(macAddress))
            throw new IllegalArgumentException("Error in method getDataConsumerManifestByMacAddress " +
                    "macAddress " +
                    "cannot be empty");
        String[] args = {macAddress};
        final List<String[]> payloads = SmartLedgerClientHelper.queryChainCode(channel, Q_GET_DCM_BY_MAC_ADD, args);
        List<DCM> dcms = Utils.extractDCMFromPayloads(payloads);

        if (dcms.isEmpty()) {
            logger.warn("No DSM retrieved from getDataSourceManifestByUri with MAC Address: '" + macAddress + "'");
            return new DCM();
        }
        return dcms.get(0);

    }

    /**
     * @param id
     * @return
     */
    @Override
    public DCM getDataConsumerManifestById(String id) throws SmartLedgerClientException {
        if (StringUtils.isEmpty(id))
            throw new IllegalArgumentException("Error in method getDataConsumerManifestById " +
                    "id " +
                    "cannot be empty");
        String[] args = {id};
        final List<String[]> payloads = SmartLedgerClientHelper.queryChainCode(channel, Q_GET_DCM_BY_ID, args);
        List<DCM> dcms = Utils.extractDCMFromPayloads(payloads);
        if (dcms.isEmpty()) {
            logger.warn("No DCM retrieved from getDataConsumerManifestByUri with URI: '" + id + "'");
            return new DCM();
        }
        return dcms.get(0);
    }

    /**
     * @return
     * @throws Exception
     */
    @Override
    public List<DSM> getAllDataSourceManifests() throws SmartLedgerClientException {
        String[] args = {};
        final List<String[]> payloads = SmartLedgerClientHelper.queryChainCode(channel, Q_GET_ALL_DS_MS, args);
        return Utils.extractDSMFromPayloads(payloads);
    }

    @Override
    public List<DSM> getCompatibleDSM(DCM dcm) throws SmartLedgerClientException {
        if (null == dcm || null == dcm.getDataSourceDefinitionsIDs() || dcm.getDataSourceDefinitionsIDs().size() == 0)
            throw new IllegalArgumentException("Error in method getAllDataSourceManifestsByDCM " +
                    "list of dsds  " +
                    "cannot be empty or null");
        String[] args = dcm.getDataSourceDefinitionsIDs().toArray(new String[dcm.getDataSourceDefinitionsIDs().size()]);
        final List<String[]> payloads = SmartLedgerClientHelper.queryChainCode(channel, Q_GET_ALL_DSMS_BY_DSDS, args);
        return Utils.extractDSMFromPayloads(payloads);
    }

    /**
     * @return
     * @throws Exception
     */
    @Override
    public List<DCM> getAllDataConsumerManifests() throws SmartLedgerClientException {
        String[] args = {};
        final List<String[]> payloads = SmartLedgerClientHelper.queryChainCode(channel, Q_GET_ALL_DC_MS, args);
        return Utils.extractDCMFromPayloads(payloads);
    }


    /**
     * @return
     * @throws Exception
     */

    @Override
    public List<DCD> getAllDataChannelDescriptors() throws SmartLedgerClientException {
        String[] args = {};
        final List<String[]> payloads = SmartLedgerClientHelper.queryChainCode(channel, Q_GET_ALL_DCD_MS, args);
        return Utils.extractDCDFromPayloads(payloads);
    }


    /**
     * @throws Exception
     */
    @Override
    public String registerDSM(DSM dsm) throws SmartLedgerClientException {
        validator.validateBean(dsm);
        ObjectMapper mapper = new ObjectMapper();
        String json = null;
        InvokeReturn invokeReturn = null;
        try {
            json = mapper.writeValueAsString(dsm.getDataSourceDefinitionParameters());
        } catch (JsonProcessingException e) {
            Utils.fail("Error in json conversion! " + e.getMessage());
        }
        String id=new String();
        if(!StringUtils.isEmpty(dsm.getId()))
            id=dsm.getId();

        String[] args = {id, dsm.getMacAddress(), dsm.getDataSourceDefinitionID(), json};
        BlockEvent.TransactionEvent event = null;
        try {
            invokeReturn = SmartLedgerClientHelper.invokeChaincode(channel,
                    I_EDIT_DSM, args);
            invokeReturn.getCompletableFuture().get(TIMEOUT, TimeUnit.SECONDS);

        } catch (TimeoutException | InterruptedException | ExecutionException | ConcurrentException e) {
            Utils.out(e.getMessage());
        } catch (Exception e) {

            Utils.out(e.getMessage());
            throw new SmartLedgerClientException(e);
        }
        return invokeReturn.getId();
    }

    /**
     * @param dcm
     * @throws Exception
     */
    @Override
    public String registerDCM(DCM dcm) throws SmartLedgerClientException {
        InvokeReturn invokeReturn = null;
        validator.validateBean(dcm);
        String id=new String();
        if(!StringUtils.isEmpty(dcm.getId()))
            id=dcm.getId();

        String[] args = {id, dcm.getMacAddress(), dcm.getDataSourceDefinitionsIDs().stream().collect
                (Collectors.joining(Config.CSV_DELIMITER_GOLANG))};

        BlockEvent.TransactionEvent event = null;
        try {
            invokeReturn = SmartLedgerClientHelper.invokeChaincode(channel,
                    I_EDIT_DCM, args);

            /* invokeReturn.getCompletableFuture().get(100, TimeUnit.SECONDS); */
            invokeReturn.getCompletableFuture().get(TIMEOUT, TimeUnit.SECONDS);
        } catch (TimeoutException | InterruptedException | ExecutionException | ConcurrentException e) {
            Utils.out(e.getMessage());
        } catch (Exception e) {
            Utils.out(e.getMessage());
            throw new SmartLedgerClientException(e);
        }
        return invokeReturn.getId();
    }

    @Override
    public void editRegisteredDSM(DSM dsm) throws SmartLedgerClientException {
        if (StringUtils.isEmpty(dsm.getId())) {
            Utils.out("DSM in EDIT don't have id");
            throw new SmartLedgerClientException("DSM in EDIT don't have id");
        }
        validator.validateBean(dsm);
        DSM dataSourceManifestByID = null;
        try {
            dataSourceManifestByID = getDataSourceManifestById(dsm.getId());
        } catch (Exception e) {
            throw new SmartLedgerClientException(e);
        }
        registerDSM(dsm);
    }

    @Override
    public void editRegisteredDCM(DCM dcm) throws SmartLedgerClientException {
        if (StringUtils.isEmpty(dcm.getId())) {
            Utils.out("DCM in EDIT don't have id");
            throw new SmartLedgerClientException("DCM in EDIT don't have id");
        }
        validator.validateBean(dcm);
        DCM dataConsumerManifestByID = null;
        try {
            dataConsumerManifestByID = getDataConsumerManifestById(dcm.getId());
        } catch (Exception e) {
            throw new SmartLedgerClientException(e);
        }

        registerDCM(dcm);
    }

    /**
     * @param id
     * @throws Exception
     */

    @Override
    public void removeDSM(String id) throws SmartLedgerClientException {
        if (StringUtils.isEmpty(id))
            throw new IllegalArgumentException("Error in method removeDSM " +
                    "id " +
                    "cannot be empty");
        String[] args = {id};
        BlockEvent.TransactionEvent event = null;
        try {
            InvokeReturn invokeReturn = SmartLedgerClientHelper.invokeChaincode(channel,
                    I_REMOVE_DSM, args);
            invokeReturn.getCompletableFuture().get(TIMEOUT, TimeUnit.SECONDS);
        } catch (TimeoutException | InterruptedException | ExecutionException | ConcurrentException e) {
            Utils.out(e.getMessage());
        } catch (Exception e) {
            Utils.out(e.getMessage());
            throw new SmartLedgerClientException("Error removing DSM: " + e.getMessage());
        }
    }

    /**
     * @param id
     * @throws Exception
     */
    @Override
    public void removeDCM(String id) throws SmartLedgerClientException {
        if (StringUtils.isEmpty(id))
            throw new IllegalArgumentException("Error in method removeDCM " +
                    "id " +
                    "cannot be empty");
        String[] args = {id};
        BlockEvent.TransactionEvent event = null;
        try {
            InvokeReturn invokeReturn = SmartLedgerClientHelper.invokeChaincode(channel,
                    I_REMOVE_DCM, args);
            invokeReturn.getCompletableFuture().get(TIMEOUT, TimeUnit.SECONDS);
        } catch (TimeoutException | InterruptedException | ExecutionException | ConcurrentException e) {
            Utils.out(e.getMessage());
        } catch (Exception e) {
            Utils.out(e.getMessage());
            throw new SmartLedgerClientException("Error removing DCM " + e.getMessage());
        }
    }

    @Override
    public String registerDCD(DCD dcd) throws SmartLedgerClientException {
        InvokeReturn invokeReturn = null;
        validator.validateBean(dcd);
        String[] args = {Utils.convertXmlGregorianToString(dcd.getExpirationDateTime()), Utils.convertXmlGregorianToString(dcd.getValidFrom()), dcd
                .getDataSourceManifestID(), dcd.getDataConsumerManifestID(), new String()};
        BlockEvent.TransactionEvent event = null;
        try {
            invokeReturn = SmartLedgerClientHelper.invokeChaincode(channel,
                    I_EDIT_DCD, args);
            invokeReturn.getCompletableFuture().get(TIMEOUT, TimeUnit.SECONDS);
        } catch (TimeoutException | InterruptedException | ExecutionException | ConcurrentException e) {
            Utils.out(e.getMessage());
        } catch (Exception e) {
            Utils.out(e.getMessage());
            throw new SmartLedgerClientException(e);
        }
        return invokeReturn.getId();
    }

    @Override
    public void removeDCD(String id) throws SmartLedgerClientException {
        if (StringUtils.isEmpty(id))
            throw new IllegalArgumentException("Error in method removeDCD " +
                    "id " +
                    "cannot be empty");
        String[] args = {id};
        BlockEvent.TransactionEvent event = null;
        try {
            InvokeReturn invokeReturn = SmartLedgerClientHelper.invokeChaincode(channel,
                    I_REMOVE_DCD, args);
            invokeReturn.getCompletableFuture().get(TIMEOUT, TimeUnit.SECONDS);
        } catch (TimeoutException | InterruptedException | ExecutionException | ConcurrentException e) {
            Utils.out(e.getMessage());
        } catch (Exception e) {
            Utils.out(e.getMessage());
            throw new SmartLedgerClientException("Error removing DCD " + e.getMessage());
        }
    }

    @Override
    public DCD getDataChannelDescriptorById(String id) throws SmartLedgerClientException {
        if (StringUtils.isEmpty(id))
            throw new IllegalArgumentException("Error in method getDataChannelDescriptorById " +
                    "id " +
                    "cannot be empty");
        String[] args = {id};
        final List<String[]> payloads = SmartLedgerClientHelper.queryChainCode(channel, Q_GET_DCD_BY_ID, args);
        List<DCD> dcds = Utils.extractDCDFromPayloads(payloads);
        if (dcds.isEmpty()) {
            logger.warn("No DCD retrieved from getDataChannelDescriptor with ID: '" + id + "'");
            return new DCD();
        }
        return dcds.get(0);
    }
}//end Class