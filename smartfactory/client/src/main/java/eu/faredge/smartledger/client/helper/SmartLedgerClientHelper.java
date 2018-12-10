package eu.faredge.smartledger.client.helper;

import eu.faredge.smartledger.client.exception.SmartLedgerClientException;
import eu.faredge.smartledger.client.model.Org;
import eu.faredge.smartledger.client.model.Store;
import eu.faredge.smartledger.client.model.User;
import eu.faredge.smartledger.client.utils.Config;
import eu.faredge.smartledger.client.utils.Utils;
import org.apache.commons.codec.binary.Hex;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hyperledger.fabric.protos.ledger.rwset.kvrwset.KvRwset;
import org.hyperledger.fabric.protos.peer.ChaincodeEventOuterClass;
import org.hyperledger.fabric.sdk.*;
import org.hyperledger.fabric.sdk.exception.ChaincodeEndorsementPolicyParseException;
import org.hyperledger.fabric.sdk.exception.InvalidArgumentException;
import org.hyperledger.fabric.sdk.exception.InvalidProtocolBufferRuntimeException;
import org.hyperledger.fabric.sdk.exception.ProposalException;
import org.hyperledger.fabric.sdk.security.CryptoSuite;
import org.hyperledger.fabric_ca.sdk.HFCAClient;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import static java.lang.String.format;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.hyperledger.fabric.sdk.BlockInfo.EnvelopeType.TRANSACTION_ENVELOPE;

public class SmartLedgerClientHelper {
    private static final Log logger = LogFactory.getLog(SmartLedgerClientHelper.class);
    private static ResourceBundle finder = ResourceBundle.getBundle("smart-ledger");
    private static final Config CONFIG = Config.getConfig();
    private static final String TEST_FIXTURES_PATH = finder.getString("TEST_FIXTURES_PATH");
    private static final String CHAIN_CODE_NAME = finder.getString("CHAIN_CODE_NAME");
    private static final String CHAIN_CODE_PATH = finder.getString("CHAIN_CODE_PATH");
    private static final String CHAIN_CODE_VERSION = finder.getString("CHAIN_CODE_VERSION");
    private static final byte[] EXPECTED_EVENT_DATA = "!".getBytes(UTF_8);
    private static final String EXPECTED_EVENT_NAME = "event";

    private static String testTxID = null;  // save the CC invoke TxID and use in queries

    private static ChaincodeID chaincodeID = null;
    private static final int DELTA = 100;
    private static HFClient client = HFClient.createNewInstance();
    private static Collection<Orderer> orderers = null;

    private static User user = null;
    private static User admin = null;
    //private static User peerOrgAdmin = null;

    private static final Map<String, String> TX_EXPECTED;

    static {
        TX_EXPECTED = new HashMap<>();
        TX_EXPECTED.put("readset1", "Missing readset for channel bar block 1");
        TX_EXPECTED.put("writeset1", "Missing writeset for channel bar block 1");
    }


    static class ChaincodeEventCapture { //A test class to capture chaincode events
        final String handle;
        final BlockEvent blockEvent;
        final ChaincodeEventOuterClass.ChaincodeEvent chaincodeEvent;

        ChaincodeEventCapture(String handle, BlockEvent blockEvent, ChaincodeEventOuterClass.ChaincodeEvent
                chaincodeEvent) {
            this.handle = handle;
            this.blockEvent = blockEvent;
            this.chaincodeEvent = chaincodeEvent;
        }

        static ChaincodeEventCapture createChaincodeEventCapture(String handle, BlockEvent blockEvent,
                                                                 ChaincodeEventOuterClass.ChaincodeEvent
                                                                         chaincodeEvent) {
            return new ChaincodeEventCapture(handle, blockEvent, chaincodeEvent);
        }
    }

    private static String chaincodeEventListenerHandle = null;

    private static Vector<ChaincodeEventCapture> chaincodeEvents = new Vector<>(); // Test list to capture chaincode
    // events.

    public static void checkConfig(Org org) throws SmartLedgerClientException {
        Utils.out("\n\n\nRUNNING: ISmartLedgerClient.\n");

        try {
            chaincodeID = ChaincodeID.newBuilder().setName(CHAIN_CODE_NAME)
                    .setVersion(CHAIN_CODE_VERSION)
                    .setPath(CHAIN_CODE_PATH).build();

            //Set up hfca for each sample org

            String caName = org.getCAName(); //Try one of each name and no name.
            if (caName != null && !caName.isEmpty()) {
                org.setCAClient(HFCAClient.createNewInstance(caName, org.getCALocation(), org
                        .getCAProperties()));
            } else {
                org.setCAClient(HFCAClient.createNewInstance(org.getCALocation(), org
                        .getCAProperties()));
            }
        } catch (Exception e) {
            throw new SmartLedgerClientException(e);
        }
    }

    public static void setup(Org org, String userName, String enrollmentSecret) throws
            SmartLedgerClientException {
        try {
            ////////////////////////////
            // Setup client
            client.setCryptoSuite(CryptoSuite.Factory.getCryptoSuite());
            Store store = new Store();
            HFCAClient ca = org.getCAClient();
            ca.setCryptoSuite(CryptoSuite.Factory.getCryptoSuite());

            User peerOrgAdmin = null;
            if (Boolean.parseBoolean(Config.USE_CRYPTO_CONFIG)) {
                File sampleStoreFile = new File(System.getProperty("user.home") + Config.SMART_LEDGER_CLIENT_PROPS +
                        ".properties");
                store = new Store(sampleStoreFile);
                final String sampleOrgName = org.getName();
                final String sampleOrgDomainName = org.getDomainName();
                peerOrgAdmin = store.getMember(sampleOrgName + "Admin", sampleOrgName, org
                                .getMSPID(),
                        Utils.findFileSk(Paths.get(CONFIG.getCryptoConfigPath(),
                                "/peerOrganizations/",
                                sampleOrgDomainName, format("/users/Admin@%s/msp/keystore", sampleOrgDomainName))
                                .toFile()),
                        Paths.get(CONFIG.getCryptoConfigPath(), "/peerOrganizations/",
                                sampleOrgDomainName,
                                format("/users/Admin@%s/msp/signcerts/Admin@%s-cert.pem", sampleOrgDomainName,
                                        sampleOrgDomainName)).toFile());
                user = peerOrgAdmin;
            } else {
                String USER_ID = finder.getString("USER_ID");
                if (StringUtils.isEmpty(userName)) userName = USER_ID;
                String PEER_ADMIN_ID = finder.getString("PEER_ADMIN_ID");
                String WALLET_DIR = finder.getString("WALLET_DIR");
                user = store.getMember(userName, org.getName(), WALLET_DIR);
                peerOrgAdmin = store.getMember(PEER_ADMIN_ID, org.getName(),
                        WALLET_DIR);
            }
            org.setPeerAdmin(peerOrgAdmin); //A special user that can create channels, join peers and install
            // instantiate chaincode
        } catch (Exception e) {
            logger.error(e);
            throw new SmartLedgerClientException(e);
        }
    }

    public static List<String[]> queryChainCode(Channel channel, String functionName, String[] args) throws
            SmartLedgerClientException {
        return queryChainCode(client, channel, functionName, null, args);
    }

    private static List<String[]> queryChainCode(HFClient client, Channel channel, String functionName,
                                                 BlockEvent.TransactionEvent transactionEvent, String[] args) throws
            SmartLedgerClientException {
        try {
            if (null != transactionEvent) {
                waitOnFabric(0);
                Utils.out("Finished transaction with transaction id %s", transactionEvent.getTransactionID());
                testTxID = transactionEvent.getTransactionID(); // used in the channel queries later
            }
            ////////////////////////////
            // Send Query Proposal to all peers
            //
            Utils.out("Now query chaincode for the values rquired.");

            QueryByChaincodeRequest queryByChaincodeRequest = client.newQueryProposalRequest();
            queryByChaincodeRequest.setArgs(args);
            queryByChaincodeRequest.setFcn(functionName);
            queryByChaincodeRequest.setChaincodeID(chaincodeID);

            Map<String, byte[]> tm2 = new HashMap<>();
            tm2.put("HyperLedgerFabric", "QueryByChaincodeRequest:JavaSDK".getBytes(UTF_8));
            tm2.put("method", "QueryByChaincodeRequest".getBytes(UTF_8));
            queryByChaincodeRequest.setTransientMap(tm2);
            List<String[]> payloads = new ArrayList<>();

            Collection<ProposalResponse> queryProposals = channel.queryByChaincode(queryByChaincodeRequest, channel
                    .getPeers());
            for (ProposalResponse proposalResponse : queryProposals) {
                if (!proposalResponse.isVerified() || proposalResponse.getStatus() != ProposalResponse.Status
                        .SUCCESS) {
                    Utils.out("Failed query proposal from peer " + proposalResponse.getPeer().getName() + " status: "
                            + proposalResponse.getStatus() +
                            ". Messages: " + proposalResponse.getMessage()
                            + ". Was verified : " + proposalResponse.isVerified());
                    String[] returnPayload = new String[2];
                    returnPayload[0] = proposalResponse.getPeer().getName();
                    returnPayload[1] = null;
                    payloads.add(returnPayload);
                } else {
                    String payload = proposalResponse.getProposalResponse().getResponse().getPayload()
                            .toStringUtf8();
                    Utils.out("Query payload from peer %s returned %s", proposalResponse.getPeer().getName(),
                            payload);
                    String[] returnPayload = new String[2];
                    returnPayload[0] = proposalResponse.getPeer().getName();
                    returnPayload[1] = payload;
                    payloads.add(returnPayload);
                }
            }
            //  manageChannelEvents(channel);
            return payloads;
        } catch (Exception e) {
            logger.error(e);
            throw new SmartLedgerClientException("Failed during chaincode query with error : " + e.getMessage());
        }
    }

    public static InvokeReturn invokeChaincode(Channel channel, String
            functionName, String[] args)
            throws Exception {
        return invokeChaincode(client, channel, chaincodeID, functionName, args, user);
    }

    private static InvokeReturn invokeChaincode(HFClient client, Channel channel,
                                                ChaincodeID chaincodeID,
                                                String functionName, String[]
                                                                                          args, org.hyperledger
                                                                                          .fabric.sdk.User user) throws
            SmartLedgerClientException {
        try {
            Collection<ProposalResponse> successful = new LinkedList<>();
            Collection<ProposalResponse> failed = new LinkedList<>();
            String payload = null;

            ///////////////
            /// Send transaction proposal to all peers
            TransactionProposalRequest transactionProposalRequest = client.newTransactionProposalRequest();
            transactionProposalRequest.setChaincodeID(chaincodeID);
            transactionProposalRequest.setFcn(functionName);
            transactionProposalRequest.setArgs(args);
            transactionProposalRequest.setProposalWaitTime(CONFIG.getProposalWaitTime());
            if (user != null) { // specific user use that
                transactionProposalRequest.setUserContext(user);
            }
            Utils.out("sending transaction proposal to all peers with arguments: (" + StringUtils.join(args, ",") +
                    "\"");

            Collection<ProposalResponse> invokePropResp = channel.sendTransactionProposal
                    (transactionProposalRequest);
            for (ProposalResponse response : invokePropResp) {
                if (response.getStatus() == ChaincodeResponse.Status.SUCCESS) {
                    Utils.out("Successful transaction proposal response Txid: %s from peer %s", response
                            .getTransactionID(), response.getPeer().getName());
                    successful.add(response);
                    payload = response.getProposalResponse().getResponse().getPayload()
                            .toStringUtf8();
                } else {
                    failed.add(response);
                }
            }
            Utils.out("Received %d transaction proposal responses. Successful+verified: %d . Failed: %d",
                    invokePropResp.size(), successful.size(), failed.size());
            if (failed.size() > 0) {

                ProposalResponse firstTransactionProposalResponse = failed.iterator().next();
                failed.stream().forEach(response -> logger.error(response.getMessage()));
                throw new ProposalException(format("Not enough endorsers for invoke(" + StringUtils.join(args, "," +
                                "") +
                                ")" +
                                ":%d " +
                                "endorser " +
                                "error:%s. Was verified:%b",
                        args[args.length - 1], firstTransactionProposalResponse.getStatus().getStatus(),
                        firstTransactionProposalResponse.getMessage(),
                        firstTransactionProposalResponse.isVerified()));
            }
            Utils.out("Successfully received transaction proposal responses.");

            ////////////////////////////
            // Send transaction to orderer
            Utils.out("Sending chaincode transaction " + functionName + " to orderer.");
            if (user != null) {
                return new InvokeReturn(channel.sendTransaction(successful, user) ,payload);
            }
            return new InvokeReturn(channel.sendTransaction(successful), payload);
        } catch (Exception e) {
            logger.error(e);
            throw new SmartLedgerClientException(e);
        }
    }

    public static Channel initializeChannel(String name, Org org) throws Exception {
        return initializeChannel(name, client, org);
    }

    private static Channel initializeChannel(String name, HFClient client, Org org) throws
            SmartLedgerClientException {
        ////////////////////////////
        //Initialize the channel
        //
        try {
            Utils.out("Constructing channel java structures %s", name);
            //Only peer Admin org
            client.setUserContext(org.getPeerAdmin());

            orderers = new LinkedList<>();

            for (String orderName : org.getOrdererNames()) {

                Properties ordererProperties = CONFIG.getOrdererProperties(orderName);
                //example of setting keepAlive to avoid timeouts on inactive http2 connections.
                // Under 5 minutes would require changes to server side to accept faster ping rates.
                ordererProperties.put("grpc.NettyChannelBuilderOption.keepAliveTime", new Object[]{5L, TimeUnit
                        .MINUTES});
                ordererProperties.put("grpc.NettyChannelBuilderOption.keepAliveTimeout", new Object[]{8L, TimeUnit
                        .SECONDS});

                orderers.add(client.newOrderer(orderName, org.getOrdererLocation(orderName),
                        ordererProperties));
            }

            //Just pick the first orderer in the list to create the channel.

            Orderer anOrderer = orderers.iterator().next();
            Channel newChannel = client.getChannel(name);
            if (null == newChannel) {
                // @ascatox Constructs a new channel
                newChannel = client.newChannel(name);
            }
            //Util.out("Created channel %s", name);
            newChannel.addOrderer(anOrderer);

            for (String peerName : org.getPeerNames()) {
                String peerLocation = org.getPeerLocation(peerName);

                Properties peerProperties = CONFIG.getPeerProperties(peerName); //CaUser properties for
                // peer.. if
                // any.
                if (peerProperties == null) {
                    peerProperties = new Properties();
                }
                //Example of setting specific options on grpc's NettyChannelBuilder
                peerProperties.put("grpc.NettyChannelBuilderOption.maxInboundMessageSize", 9000000);

                Peer peer = client.newPeer(peerName, peerLocation, peerProperties);
                //            newChannel.joinPeer(peer);
                newChannel.addPeer(peer);
                Utils.out("Peer %s joined channel %s", peerName, name);
                org.addPeer(peer);
            }

            for (Orderer orderer : orderers) { //add remaining orderers if any.
                if (!orderer.equals(anOrderer))
                    newChannel.addOrderer(orderer);
            }
            for (String eventHubName : org.getEventHubNames()) {

                final Properties eventHubProperties = CONFIG.getEventHubProperties(eventHubName);

                eventHubProperties.put("grpc.NettyChannelBuilderOption.keepAliveTime", new Object[]{5L, TimeUnit
                        .MINUTES});
                eventHubProperties.put("grpc.NettyChannelBuilderOption.keepAliveTimeout", new Object[]{8L, TimeUnit
                        .SECONDS});

                EventHub eventHub = client.newEventHub(eventHubName, org.getEventHubLocation(eventHubName),
                        eventHubProperties);
                newChannel.addEventHub(eventHub);
            }
            //TODO
            /*String chaincodeEventListenerHandle = newChannel.registerChaincodeEventListener(Pattern.compile(".*"),
                    Pattern.compile(Pattern.quote(EXPECTED_EVENT_NAME)),
                    (handle, blockEvent, chaincodeEvent) -> {
                        chaincodeEvents.add(new ChaincodeEventCapture(handle, blockEvent, chaincodeEvent));

                        String es = blockEvent.getPeer() != null ? blockEvent.getPeer().getName() : blockEvent
                                .getEventHub().getName();
                        Util.out("RECEIVED Chaincode event with handle: %s, chaincode Id: %s, chaincode event " +
                                        "name: " +
                                        "%s, "
                                        + "transaction id: %s, event payload: \"%s\", from eventhub: %s",
                                handle, chaincodeEvent.getChaincodeId(),
                                chaincodeEvent.getEventName(),
                                chaincodeEvent.getTxId(),
                                new String(chaincodeEvent.getPayload()), es);

                    });*/
            newChannel.initialize(); //There's no need to initialize the channel we are only building the java
            // structures.
            Utils.out("Finished initialization channel java structures %s", name);
            return newChannel;
        } catch (
                InvalidArgumentException e)

        {
            throw new SmartLedgerClientException(e);
        /*} catch (TransactionException e) {
            throw new SmartLedgerClientException(e);*/
        } catch (
                Exception e)

        {
            logger.error(e);
            throw new SmartLedgerClientException(e);
        }

    }

    private static void waitOnFabric(int additional) {
        //NOOP today
    }


    void blockWalker(Channel channel) throws InvalidArgumentException, ProposalException, IOException {
        try {
            BlockchainInfo channelInfo = channel.queryBlockchainInfo();

            for (long current = channelInfo.getHeight() - 1; current > -1; --current) {
                BlockInfo returnedBlock = channel.queryBlockByNumber(current);
                final long blockNumber = returnedBlock.getBlockNumber();

                Utils.out("current block number %d has data hash: %s", blockNumber, Hex.encodeHexString(returnedBlock
                        .getDataHash()));
                Utils.out("current block number %d has previous hash id: %s", blockNumber, Hex.encodeHexString
                        (returnedBlock.getPreviousHash()));
                Utils.out("current block number %d has calculated block hash is %s", blockNumber, Hex.encodeHexString
                        (SDKUtils.calculateBlockHash(blockNumber, returnedBlock
                                .getPreviousHash(), returnedBlock.getDataHash())));

                final int envelopeCount = returnedBlock.getEnvelopeCount();
                Utils.out("current block number %d has %d envelope count:", blockNumber, returnedBlock
                        .getEnvelopeCount());
                int i = 0;
                for (BlockInfo.EnvelopeInfo envelopeInfo : returnedBlock.getEnvelopeInfos()) {
                    ++i;

                    Utils.out("  Transaction number %d has transaction id: %s", i, envelopeInfo.getTransactionID());
                    final String channelId = envelopeInfo.getChannelId();

                    Utils.out("  Transaction number %d has channel id: %s", i, channelId);
                    Utils.out("  Transaction number %d has epoch: %d", i, envelopeInfo.getEpoch());
                    Utils.out("  Transaction number %d has transaction timestamp: %tB %<te,  %<tY  %<tT %<Tp", i,
                            envelopeInfo.getTimestamp());
                    Utils.out("  Transaction number %d has type id: %s", i, "" + envelopeInfo.getType());

                    if (envelopeInfo.getType() == TRANSACTION_ENVELOPE) {
                        BlockInfo.TransactionEnvelopeInfo transactionEnvelopeInfo = (BlockInfo
                                .TransactionEnvelopeInfo) envelopeInfo;

                        Utils.out("  Transaction number %d has %d actions", i, transactionEnvelopeInfo
                                .getTransactionActionInfoCount());
                        Utils.out("  Transaction number %d isValid %b", i, transactionEnvelopeInfo.isValid());
                        Utils.out("  Transaction number %d validation code %d", i, transactionEnvelopeInfo
                                .getValidationCode());

                        int j = 0;
                        for (BlockInfo.TransactionEnvelopeInfo.TransactionActionInfo transactionActionInfo :
                                transactionEnvelopeInfo.getTransactionActionInfos()) {
                            ++j;
                            Utils.out("   Transaction action %d has response status %d", j, transactionActionInfo
                                    .getResponseStatus());
                            Utils.out("   Transaction action %d has response message bytes as string: %s", j,
                                    printableString(new String(transactionActionInfo.getResponseMessageBytes(),
                                            "UTF-8")));
                            Utils.out("   Transaction action %d has %d endorsements", j, transactionActionInfo
                                    .getEndorsementsCount());

                            for (int n = 0; n < transactionActionInfo.getEndorsementsCount(); ++n) {
                                BlockInfo.EndorserInfo endorserInfo = transactionActionInfo.getEndorsementInfo(n);
                                Utils.out("Endorser %d signature: %s", n, Hex.encodeHexString(endorserInfo
                                        .getSignature()));
                                Utils.out("Endorser %d endorser: %s", n, new String(endorserInfo.getEndorser(),
                                        "UTF-8"));
                            }
                            Utils.out("   Transaction action %d has %d chaincode input arguments", j,
                                    transactionActionInfo.getChaincodeInputArgsCount());
                            for (int z = 0; z < transactionActionInfo.getChaincodeInputArgsCount(); ++z) {
                                Utils.out("     Transaction action %d has chaincode input argument %d is: %s", j, z,
                                        printableString(new String(transactionActionInfo.getChaincodeInputArgs(z),
                                                "UTF-8")));
                            }

                            Utils.out("   Transaction action %d proposal response status: %d", j,
                                    transactionActionInfo.getProposalResponseStatus());
                            Utils.out("   Transaction action %d proposal response payload: %s", j,
                                    printableString(new String(transactionActionInfo.getProposalResponsePayload()
                                    )));

                            // Check to see if we have our expected event.
//                            if (blockNumber == 2) {
//                                ChaincodeEvent chaincodeEvent = transactionActionInfo.getEvent();
//                            }

                            TxReadWriteSetInfo rwsetInfo = transactionActionInfo.getTxReadWriteSet();
                            if (null != rwsetInfo) {
                                Utils.out("   Transaction action %d has %d name space read write sets", j, rwsetInfo
                                        .getNsRwsetCount());

                                for (TxReadWriteSetInfo.NsRwsetInfo nsRwsetInfo : rwsetInfo.getNsRwsetInfos()) {
                                    final String namespace = nsRwsetInfo.getNamespace();
                                    KvRwset.KVRWSet rws = nsRwsetInfo.getRwset();

                                    int rs = -1;
                                    for (KvRwset.KVRead readList : rws.getReadsList()) {
                                        rs++;

                                        Utils.out("     Namespace %s read set %d key %s  version [%d:%d]", namespace,
                                                rs, readList.getKey(),
                                                readList.getVersion().getBlockNum(), readList.getVersion()
                                                        .getTxNum());

                                        if ("bar".equals(channelId) && blockNumber == 2) {
                                            if ("example_cc_go".equals(namespace)) {
                                                if (rs == 0) {
                                                } else if (rs == 1) {
                                                } else {
                                                    Utils.fail(format("unexpected readset %d", rs));
                                                }

                                                TX_EXPECTED.remove("readset1");
                                            }
                                        }
                                    }

                                    rs = -1;
                                    for (KvRwset.KVWrite writeList : rws.getWritesList()) {
                                        rs++;
                                        String valAsString = printableString(new String(writeList.getValue()
                                                .toByteArray(), "UTF-8"));

                                        Utils.out("     Namespace %s write set %d key %s has value '%s' ",
                                                namespace, rs,
                                                writeList.getKey(),
                                                valAsString);

                                        if ("bar".equals(channelId) && blockNumber == 2) {
                                            if (rs == 0) {
                                            } else if (rs == 1) {
                                            } else {
                                                Utils.fail(format("unexpected writeset %d", rs));
                                            }

                                            TX_EXPECTED.remove("writeset1");
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
            if (!TX_EXPECTED.isEmpty()) {
                Utils.fail(TX_EXPECTED.get(0));
            }
        } catch (InvalidProtocolBufferRuntimeException e) {
            throw e.getCause();
        }
    }

    static String printableString(final String string) {
        int maxLogStringLength = 64;
        if (string == null || string.length() == 0) {
            return string;
        }

        String ret = string.replaceAll("[^\\p{Print}]", "?");

        ret = ret.substring(0, Math.min(ret.length(), maxLogStringLength)) + (ret.length() > maxLogStringLength ?
                "..." : "");

        return ret;
    }

    public static void installChaincode(Channel channel, Org org) throws SmartLedgerClientException {
        Collection<ProposalResponse> successful = new ArrayList<>();
        Collection<ProposalResponse> failed = new ArrayList<>();
        installChaincode(client, channel, org, successful, failed);
    }

    private static void installChaincode(HFClient client, Channel channel, Org org,
                                         Collection<ProposalResponse> successful, Collection<ProposalResponse>
                                                 failed)
            throws SmartLedgerClientException {
        ////////////////////////////
        // Install Proposal Request
        try {
            final String channelName = channel.getName();
            boolean isFooChain = Config.channelName.equals(channelName);
            Utils.out("Running channel %s", channelName);

            //channel.setTransactionWaitTime(CONFIG.getTransactionWaitTime());
            //channel.setDeployWaitTime(CONFIG.getDeployWaitTime());

            Collection<ProposalResponse> responses;
            //

            client.setUserContext(org.getPeerAdmin());

            Utils.out("Creating install proposal");

            InstallProposalRequest installProposalRequest = client.newInstallProposalRequest();
            installProposalRequest.setChaincodeID(chaincodeID);

            String chaincodePathPrefix = finder.getString("CHAIN_CODE_PATH_PREFIX");
            // "/sdkintegration/gocc/sample1";
            if (isFooChain) {
                // on foo chain install from directory.
                ////For GO language and serving just a single user, chaincodeSource is mostly likely the users
                // GOPATH
                installProposalRequest.setChaincodeSourceLocation(new File(TEST_FIXTURES_PATH +
                        chaincodePathPrefix));
            } else {
                // On bar chain install from an input stream.
                installProposalRequest.setChaincodeInputStream(Utils.generateTarGzInputStream(
                        (Paths.get(TEST_FIXTURES_PATH, chaincodePathPrefix, "src", CHAIN_CODE_PATH).toFile()),
                        Paths.get("src", CHAIN_CODE_PATH).toString()));
            }

            installProposalRequest.setChaincodeVersion(CHAIN_CODE_VERSION);
            Utils.out("Sending install proposal");

            ////////////////////////////
            // only a client from the same org as the peer can issue an install request
            int numInstallProposal = 0;
            //    Set<String> orgs = orgPeers.keySet();
            //   for (Org org : testSampleOrgs) {

            Set<Peer> peersFromOrg = org.getPeers();
            numInstallProposal = numInstallProposal + peersFromOrg.size();
            responses = client.sendInstallProposal(installProposalRequest, peersFromOrg);

            for (ProposalResponse response : responses) {
                if (response.getStatus() == ProposalResponse.Status.SUCCESS) {
                    Utils.out("Successful install proposal response Txid: %s from peer %s", response
                                    .getTransactionID(),
                            response.getPeer().getName());
                    successful.add(response);
                } else {
                    failed.add(response);
                }
            }
            //   }
            Utils.out("Received %d install proposal responses. Successful+verified: %d . Failed: %d",
                    numInstallProposal,
                    successful.size(), failed.size());
            if (failed.size() > 0) {
                ProposalResponse first = failed.iterator().next();
                logger.error(first.getMessage());
                Utils.fail("Not enough endorsers for install :" + successful.size() + ".  " + first.getMessage());
            }
        } catch (InvalidArgumentException e) {
            throw new SmartLedgerClientException(e);
        } catch (IOException e) {
            throw new SmartLedgerClientException(e);
        } catch (ProposalException e) {
            throw new SmartLedgerClientException(e);
        } catch (Exception e) {
            throw new SmartLedgerClientException(e);
        }
    }


    public static CompletableFuture<BlockEvent.TransactionEvent> instantiateOrUpgradeChaincode(Channel channel,
                                                                                               String[] args,
                                                                                               boolean
                                                                                                       isUpgrade)
            throws SmartLedgerClientException {
        if (isUpgrade)
            return upgradeChaincode(channel, args);
        else
            return instantiateChaincode(channel, args);
    }

    public static CompletableFuture<BlockEvent.TransactionEvent> instantiateChaincode(Channel channel, String[]
            args)
            throws SmartLedgerClientException {
        Collection<ProposalResponse> successful = new ArrayList<>();
        Collection<ProposalResponse> failed = new ArrayList<>();
        return instantiateChaincode(client, channel, args, successful, failed, true, orderers);
    }

    private static CompletableFuture<BlockEvent.TransactionEvent> instantiateChaincode(HFClient client, Channel
            channel, String[] args,
                                                                                       Collection<ProposalResponse>
                                                                                               successful,
                                                                                       Collection<ProposalResponse>
                                                                                               failed, boolean
                                                                                               isFooChain,
                                                                                       Collection<Orderer> orderers)
            throws SmartLedgerClientException {
        try {
            Collection<ProposalResponse> responses;///////////////
            //// Instantiate chaincode.
            InstantiateProposalRequest instantiateProposalRequest = client.newInstantiationProposalRequest();
            instantiateProposalRequest.setProposalWaitTime(CONFIG.getProposalWaitTime());
            instantiateProposalRequest.setChaincodeID(chaincodeID);
            String function = "init";
            instantiateProposalRequest.setFcn(function);
            instantiateProposalRequest.setArgs(args);
            Map<String, byte[]> tm = new HashMap<>();
            tm.put("HyperLedgerFabric", "InstantiateProposalRequest:JavaSDK".getBytes(UTF_8));
            tm.put("method", "InstantiateProposalRequest".getBytes(UTF_8));
            instantiateProposalRequest.setTransientMap(tm);

            ChaincodeEndorsementPolicy chaincodeEndorsementPolicy = new ChaincodeEndorsementPolicy();
            chaincodeEndorsementPolicy.fromYamlFile(new File(TEST_FIXTURES_PATH +
                    "/sdkintegration/chaincodeendorsementpolicy.yaml"));
            instantiateProposalRequest.setChaincodeEndorsementPolicy(chaincodeEndorsementPolicy);

            Utils.out("Sending instantiateProposalRequest to all peers with arguments: " + StringUtils.join(args,
                    ",") +
                    " %s" +
                    " " +
                    "respectively", "" + (200 + DELTA));
            successful.clear();
            failed.clear();

            if (isFooChain) {  //Send responses both ways with specifying peers and by using those on the channel.
                responses = channel.sendInstantiationProposal(instantiateProposalRequest, channel.getPeers());
            } else {
                responses = channel.sendInstantiationProposal(instantiateProposalRequest);
            }
            for (ProposalResponse response : responses) {
                if (response.isVerified() && response.getStatus() == ProposalResponse.Status.SUCCESS) {
                    successful.add(response);
                    Utils.out("Successful instantiate proposal response Txid: %s from peer %s", response
                                    .getTransactionID()
                            , response.getPeer().getName());
                } else {
                    failed.add(response);
                }
            }
            Utils.out("Received %d instantiate proposal responses. Successful+verified: %d . Failed: %d", responses
                            .size()
                    , successful.size(), failed.size());
            if (failed.size() > 0) {
                ProposalResponse first = failed.iterator().next();
                logger.error(first.getMessage());
                Utils.fail(first.getMessage() + ". Was verified:" + first.isVerified());
            }
            ///////////////
            /// Send instantiate transaction to orderer
            Utils.out("Sending instantiateTransaction to orderer %s respectively", "" + (200 + DELTA));
            return channel.sendTransaction(successful, orderers);
        } catch (InvalidArgumentException e) {
            throw new SmartLedgerClientException(e);
        } catch (IOException e) {
            throw new SmartLedgerClientException(e);
        } catch (ChaincodeEndorsementPolicyParseException e) {
            throw new SmartLedgerClientException(e);
        } catch (ProposalException e) {
            throw new SmartLedgerClientException(e);
        } catch (Exception e) {
            logger.error(e);
            throw new SmartLedgerClientException(e);
        }
    }


    public static CompletableFuture<BlockEvent.TransactionEvent> upgradeChaincode(Channel channel, String[] args
    )
            throws SmartLedgerClientException {
        Collection<ProposalResponse> successful = new ArrayList<>();
        Collection<ProposalResponse> failed = new ArrayList<>();
        return upgradeChaincode(client, channel, args, successful, failed, true, orderers);
    }

    private static CompletableFuture<BlockEvent.TransactionEvent> upgradeChaincode(HFClient client, Channel
            channel, String[] args,
                                                                                   Collection<ProposalResponse>
                                                                                           successful,
                                                                                   Collection<ProposalResponse>
                                                                                           failed, boolean
                                                                                           isFooChain,
                                                                                   Collection<Orderer> orderers)
            throws SmartLedgerClientException {
        try {
            Collection<ProposalResponse> responses;
            //// Upgrade chaincode.
            UpgradeProposalRequest upgradeProposalRequest = client.newUpgradeProposalRequest();
            upgradeProposalRequest.setProposalWaitTime(CONFIG.getProposalWaitTime());
            upgradeProposalRequest.setChaincodeID(chaincodeID);
            String function = "init";
            upgradeProposalRequest.setFcn(function);
            upgradeProposalRequest.setArgs(args);
            Map<String, byte[]> tm = new HashMap<>();
            tm.put("HyperLedgerFabric", "UpgradeProposalRequest:JavaSDK".getBytes(UTF_8));
            tm.put("method", "UpgradeProposalRequest".getBytes(UTF_8));
            upgradeProposalRequest.setTransientMap(tm);

            ChaincodeEndorsementPolicy chaincodeEndorsementPolicy = new ChaincodeEndorsementPolicy();
            chaincodeEndorsementPolicy.fromYamlFile(new File(TEST_FIXTURES_PATH +
                    "/sdkintegration/chaincodeendorsementpolicy.yaml"));
            upgradeProposalRequest.setChaincodeEndorsementPolicy(chaincodeEndorsementPolicy);

            Utils.out("Sending upgradeProposalRequest to all peers with arguments: " + StringUtils.join(args, ",") +
                    "" +
                    " %s" +
                    " " +
                    "respectively", "" + (200 + DELTA));
            successful.clear();
            failed.clear();

            if (isFooChain) {  //Send responses both ways with specifying peers and by using those on the channel.
                responses = channel.sendUpgradeProposal(upgradeProposalRequest, channel.getPeers());
            } else {
                responses = channel.sendUpgradeProposal(upgradeProposalRequest);
            }
            for (ProposalResponse response : responses) {
                if (response.isVerified() && response.getStatus() == ProposalResponse.Status.SUCCESS) {
                    successful.add(response);
                    Utils.out("Successful upgrade proposal response Txid: %s from peer %s", response.getTransactionID()
                            , response.getPeer().getName());
                } else {
                    failed.add(response);
                }
            }
            Utils.out("Received %d upgrade proposal responses. Successful+verified: %d . Failed: %d", responses.size()
                    , successful.size(), failed.size());
            if (failed.size() > 0) {
                ProposalResponse first = failed.iterator().next();
                logger.error(first.getMessage());
                Utils.fail(first.getMessage() + ". Was verified:" + first.isVerified());
            }
            ///////////////
            /// Send upgrade transaction to orderer
            Utils.out("Sending upgradeTransaction to orderer %s respectively", "" + (200 + DELTA));
            return channel.sendTransaction(successful, orderers);
        } catch (InvalidArgumentException e) {
            throw new SmartLedgerClientException(e);
        } catch (IOException e) {
            throw new SmartLedgerClientException(e);
        } catch (ChaincodeEndorsementPolicyParseException e) {
            throw new SmartLedgerClientException(e);
        } catch (ProposalException e) {
            throw new SmartLedgerClientException(e);
        } catch (Exception e) {
            logger.error(e);
            throw new SmartLedgerClientException(e);
        }
    }

    //TODO
    /*private static void manageChannelEvents(Channel channel) throws SmartLedgerClientException,
            InvalidArgumentException,
            InterruptedException {

        if (chaincodeEventListenerHandle != null) {
            channel.unregisterChaincodeEventListener(chaincodeEventListenerHandle);
            //Should be two. One event in chaincode and two notification for each of the two event hubs

            final int numberEventsExpected = channel.getEventHubs().size() +
                    channel.getPeers(EnumSet.of(Peer.PeerRole.EVENT_SOURCE)).size();
            //just make sure we get the notifications.
            for (int i = 15; i > 0; --i) {
                if (chaincodeEvents.size() == numberEventsExpected) {
                    break;
                } else {
                    Thread.sleep(90); // wait for the events.
                }
            }
            //assertEquals(numberEventsExpected, chaincodeEvents.size());

            for (ChaincodeEventCapture chaincodeEventCapture : chaincodeEvents) {
//                assertEquals(chaincodeEventListenerHandle, chaincodeEventCapture.handle);
//                assertEquals(testTxID, chaincodeEventCapture.chaincodeEvent.getTxId());
//                assertEquals(EXPECTED_EVENT_NAME, chaincodeEventCapture.chaincodeEvent.getEventName());
//                assertTrue(Arrays.equals(EXPECTED_EVENT_DATA, chaincodeEventCapture.chaincodeEvent.getPayload()));
//                assertEquals(CHAIN_CODE_NAME, chaincodeEventCapture.chaincodeEvent.getChaincodeId());

                BlockEvent blockEvent = chaincodeEventCapture.blockEvent;
                //assertEquals(channelName, blockEvent.getChannelId());
                //   assertTrue(channel.getEventHubs().contains(blockEvent.getEventHub()));
            }
        } else {
           Util.out("ChaincodeEvents is empty");
        }
    }*/


}