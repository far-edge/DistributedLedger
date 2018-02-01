/*
 *  Copyright 2016, 2017 IBM, DTCC, Fujitsu Australia Software Technology, IBM - All Rights Reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *        http://www.apache.org/licenses/LICENSE-2.0
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package eu.faredge.smartledger.client.utils;

import java.io.*;
import java.nio.file.Paths;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import eu.faredge.smartledger.client.model.Org;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hyperledger.fabric.sdk.helper.Utils;

/**
 * Config allows for a global config of the toolkit. Central location for all
 * toolkit configuration defaults. Has a local config file that can override any
 * property defaults. Config file can be relocated via a system property
 * "org.hyperledger.fabric.sdk.configuration". Any property can be overridden
 * with environment variable and then overridden
 * with a java system property. Property hierarchy goes System property
 * overrides environment variable which overrides config file for default values specified here.
 */

/**
 * Test Configuration
 */


public class Config {

    public static final String CSV_DELIMITER_GOLANG = ";";
    private static ResourceBundle finder = ResourceBundle.getBundle("smart-ledger");
    private static final String CONFIG_NETWORK_MAIN_FILE = finder.getString("CONFIG_NETWORK_MAIN_FILE");
    public static final String SMART_LEDGER_CLIENT_PROPS = finder.getString("STORE_FILE_PROPS");
    public static final int NUMBER_OF_PEERS_DEFAULT = Integer.parseInt(finder.getString("NUMBER_OF_PEERS"));
    public static final int NUMBER_OF_ORGS_DEFAULT = Integer.parseInt(finder.getString("NUMBER_OF_ORGS"));
    public static final String HOST = finder.getString("FABRIC_HOST");
    public static final String PEER_HOST = finder.getString("FABRIC_PEER_HOST");
    private static final String CRYPTO_CONFIG_DIR_DEFAULT = finder.getString("CRYPTO_CONFIG_DIR");
    private static final String CRYPTO_CONFIG_DIR_CONFIG = "cryptoConfigDir";
    public static String cryptoConfigDirSelected = CRYPTO_CONFIG_DIR_DEFAULT;
    public static final String USE_CRYPTO_CONFIG = finder.getString("USE_CRYPTO_CONFIG");
    public static final String CHANNEL_NAME_DEFAULT = finder.getString("CHANNEL_NAME");
    private static final String CHANNEL_NAME = "channelName";
    public static String channelName = CHANNEL_NAME_DEFAULT;
    private static final Log logger = LogFactory.getLog(Config.class);

    private static final String DEFAULT_CONFIG = "src/main/java/eu/faredge/fabric/client/utils.properties";
    private static final String ORG_HYPERLEDGER_FABRIC_SDK_CONFIGURATION = "org.hyperledger.fabric.sdktest" +
            ".configuration";

    private static final String PROPBASE = "";
    //"org.hyperledger.fabric.sdktest.";

    public static final String INVOKEWAITTIME = "100000";
    public static final String DEPLOYWAITTIME = "120000";
    public static final String PROPOSALWAITTIME = "120000";

    private static final String NUMBER_OF_PEERS_CONFIG = "numberPeers";
    private static final String NUMBER_OF_ORGS_CONFIG = "numberOrgs";

    private static final String INTEGRATIONTESTS_ORG = "";
    //PROPBASE + "integrationTests.org.";
    private static final Pattern orgPat = Pattern.compile("^" + Pattern.quote(INTEGRATIONTESTS_ORG) + "([^\\.]+)\\" +
            ".mspid$");

    private static final String INTEGRATIONTESTSTLS = PROPBASE + "use.tls";
    private static Config config;
    private static final Properties sdkProperties = new Properties();
    private boolean runningTLS;
    private boolean runningFabricCATLS;
    private boolean runningFabricTLS;
    private static final HashMap<String, Org> sampleOrgs = new HashMap<>();
    public static final Integer TIMEOUT = Integer.parseInt(finder.getString("TIMEOUT"));

    public Config() {
        //  File loadFile;
        //  FileInputStream configProps;

        try {
           /* loadFile = new File(System.getProperty(CONFIG_NETWORK_MAIN_FILE, "config-network.properties"))
                    .getAbsoluteFile();
            configProps = new FileInputStream(loadFile);
            */
            InputStream input = Config.class.getResourceAsStream(CONFIG_NETWORK_MAIN_FILE);
            sdkProperties.load(input);
            loadFabricNetwork(false);
        } catch (Exception e) {
            logger.warn("File configuration config.properties not loaded correctly!!!\nLoading default values...");
            // Default values
            defaultProperty(INVOKEWAITTIME, "100000");
            defaultProperty(DEPLOYWAITTIME, "120000");
            defaultProperty(PROPOSALWAITTIME, "120000");

            defaultProperty(INTEGRATIONTESTS_ORG + "peerOrg1.mspid", "Org1MSP");
            defaultProperty(INTEGRATIONTESTS_ORG + "peerOrg1.domname", "org1.example.com");
            defaultProperty(INTEGRATIONTESTS_ORG + "peerOrg1.ca_location", "http://" + HOST + ":7054");
            defaultProperty(INTEGRATIONTESTS_ORG + "peerOrg1.caName", "ca.example.com");
            defaultProperty(INTEGRATIONTESTS_ORG + "peerOrg1.peer_locations_0", "peer0.org1.example.com@grpc://" +
                    PEER_HOST
                    + ":7051,");
            defaultProperty(INTEGRATIONTESTS_ORG + "peerOrg1.peer_locations_1", "peer1.org1.example.com@grpc://" +
                    PEER_HOST
                    + ":8051,");
            //" peer1.org1.example.com@grpc://localhost:7053");
            defaultProperty(INTEGRATIONTESTS_ORG + "peerOrg1.orderer_locations", "orderer.example.com@grpc://" +
                    HOST
                    + ":7050");
            defaultProperty(INTEGRATIONTESTS_ORG + "peerOrg1.eventhub_locations", "peer0.org1.example.com@grpc://" +
                            PEER_HOST + ":7053"
                    //+ ",peer1.org1.example.com@grpc://" + PEER_HOST + ":7058"
            );

            if (NUMBER_OF_ORGS_DEFAULT == 2) {
                defaultProperty(INTEGRATIONTESTS_ORG + "peerOrg2.mspid", "Org2MSP");
                defaultProperty(INTEGRATIONTESTS_ORG + "peerOrg2.domname", "org2.example.com");
                defaultProperty(INTEGRATIONTESTS_ORG + "peerOrg2.ca_location", "http://" + HOST + ":7054");
                defaultProperty(INTEGRATIONTESTS_ORG + "peerOrg2.caName", "ca.example.com");
                defaultProperty(INTEGRATIONTESTS_ORG + "peerOrg2.peer_locations_0", "peer0.org2.example" +
                        ".com@grpc://" +
                        PEER_HOST
                        + ":7051,");
                defaultProperty(INTEGRATIONTESTS_ORG + "peerOrg2.peer_locations_1", "peer1.org2.example" +
                        ".com@grpc://" +
                        PEER_HOST
                        + ":8051,");
                //" peer1.org1.example.com@grpc://localhost:7053");
                defaultProperty(INTEGRATIONTESTS_ORG + "peerOrg2.orderer_locations", "orderer.example.com@grpc://" +
                        HOST

                        + ":7050");
                defaultProperty(INTEGRATIONTESTS_ORG + "peerOrg2.eventhub_locations", "peer0.org2.example" +
                                ".com@grpc://" +
                                PEER_HOST + ":8053"
                        //+ ",peer1.org1.example.com@grpc://" + PEER_HOST + ":7058"
                );
            }
            defaultProperty(INTEGRATIONTESTSTLS, null);
            loadFabricNetwork(true);
        } finally {

        }

    }

    private void loadFabricNetwork(boolean loadDefaults) {
        if (!loadDefaults) {
            cryptoConfigDirSelected = sdkProperties.getProperty(CRYPTO_CONFIG_DIR_CONFIG);
            channelName = sdkProperties.getProperty(CHANNEL_NAME);
        }
        String tlsProp = sdkProperties.getProperty(INTEGRATIONTESTSTLS, null);
        String tlsPropMatch = null == tlsProp || "null".equals(tlsProp) ? null : tlsProp;
        runningTLS = null != tlsPropMatch;
        runningFabricCATLS = runningTLS;
        runningFabricTLS = runningTLS;
        int numberOfPeers = NUMBER_OF_PEERS_DEFAULT;
        if (!loadDefaults)
            numberOfPeers = Integer.parseInt(sdkProperties.getProperty(NUMBER_OF_PEERS_CONFIG));

        for (Map.Entry<Object, Object> x : sdkProperties.entrySet()) {
            final String key = x.getKey() + "";
            final String val = x.getValue() + "";

            if (key.startsWith(INTEGRATIONTESTS_ORG)) {

                Matcher match = orgPat.matcher(key);

                if (match.matches() && match.groupCount() == 1) {
                    String orgName = match.group(1).trim();
                    sampleOrgs.put(orgName, new Org(orgName, val.trim()));

                }
            }
        }
        for (Map.Entry<String, Org> org : sampleOrgs.entrySet()) {
            final Org sampleOrg = org.getValue();
            final String orgName = org.getKey();

            String[] ps = null;
            for (int i = 0; i < numberOfPeers; i++) {
                String peerNames = sdkProperties.getProperty(INTEGRATIONTESTS_ORG + orgName + ".peer_locations_"
                        + i);
                ps = peerNames.split("[ \t]*,[ \t]*");
                for (String peer : ps) {
                    String[] nl = peer.split("[ \t]*@[ \t]*");
                    sampleOrg.addPeerLocation(nl[0], grpcTLSify(nl[1]));
                }
            }
            final String domainName = sdkProperties.getProperty(INTEGRATIONTESTS_ORG + orgName + ".domname");

            sampleOrg.setDomainName(domainName);

            String ordererNames = sdkProperties.getProperty(INTEGRATIONTESTS_ORG + orgName + ".orderer_locations");
            ps = ordererNames.split("[ \t]*,[ \t]*");
            for (String peer : ps) {
                String[] nl = peer.split("[ \t]*@[ \t]*");
                sampleOrg.addOrdererLocation(nl[0], grpcTLSify(nl[1]));
            }

            String eventHubNames = sdkProperties.getProperty(INTEGRATIONTESTS_ORG + orgName + "" +
                    ".eventhub_locations");
            ps = eventHubNames.split("[ \t]*,[ \t]*");
            for (String peer : ps) {
                String[] nl = peer.split("[ \t]*@[ \t]*");
                sampleOrg.addEventHubLocation(nl[0], grpcTLSify(nl[1]));
            }

            sampleOrg.setCALocation(httpTLSify(sdkProperties.getProperty((INTEGRATIONTESTS_ORG + org.getKey() + "" +
                    ".ca_location"))));

            sampleOrg.setCAName(sdkProperties.getProperty((INTEGRATIONTESTS_ORG + org.getKey() + ".caName")));

            if (runningFabricCATLS) {
                String dirPath = getCryptoConfigPath();
                String cert =
                        (
                                //"src/CaUser/fixture/sdkintegration/e2e-2Orgs/channel/crypto-config" +
                                dirPath +
                                        "/peerOrganizations" +
                                        "/DNAME/ca/ca.DNAME-cert.pem").replaceAll("DNAME", domainName);
                File cf = new File(cert);
                if (!cf.exists() || !cf.isFile()) {
                    throw new RuntimeException("TEST is missing cert file " + cf.getAbsolutePath());
                }
                Properties properties = new Properties();
                properties.setProperty("pemFile", cf.getAbsolutePath());

                properties.setProperty("allowAllHostNames", "true"); //testing environment only NOT FOR PRODUCTION!

                sampleOrg.setCAProperties(properties);
            }
        }
    }

    private String grpcTLSify(String location) {
        location = location.trim();
        Exception e = Utils.checkGrpcUrl(location);
        if (e != null) {
            throw new RuntimeException(String.format("Bad TEST parameters for grpc url %s", location), e);
        }
        return runningFabricTLS ?
                location.replaceFirst("^grpc://", "grpcs://") : location;

    }

    private String httpTLSify(String location) {
        location = location.trim();

        return runningFabricCATLS ?
                location.replaceFirst("^http://", "https://") : location;
    }

    /**
     * getConfig return back singleton for SDK configuration.
     *
     * @return Global configuration
     */
    public static Config getConfig() {
        if (null == config) {
            config = new Config();
        }
        return config;
    }

    /**
     * getProperty return back property for the given value.
     *
     * @param property
     * @return String value for the property
     */
    private String getProperty(String property) {

        String ret = sdkProperties.getProperty(property);

        if (null == ret) {
            logger.warn(String.format("No configuration value found for '%s'", property));
        }
        return ret;
    }

    private static void defaultProperty(String key, String value) {

        String ret = System.getProperty(key);
        if (ret != null) {
            sdkProperties.put(key, ret);
        } else {
            String envKey = key.toUpperCase().replaceAll("\\.", "_");
            ret = System.getenv(envKey);
            if (null != ret) {
                sdkProperties.put(key, ret);
            } else {
                if (null == sdkProperties.getProperty(key) && value != null) {
                    sdkProperties.put(key, value);
                }
            }
        }
    }

    public int getTransactionWaitTime() {
        return Integer.parseInt(INVOKEWAITTIME);
    }

    public int getDeployWaitTime() {
        return Integer.parseInt(DEPLOYWAITTIME);
    }

    public long getProposalWaitTime() {
        return Integer.parseInt(PROPOSALWAITTIME);
    }

    public Collection<Org> getIntegrationTestsSampleOrgs() {
        return Collections.unmodifiableCollection(sampleOrgs.values());
    }

    public Org getIntegrationTestsSampleOrg(String name) {
        return sampleOrgs.get(name);

    }

    public Properties getPeerProperties(String name) {

        return getEndPointProperties("peer", name);

    }

    public Properties getOrdererProperties(String name) {

        return getEndPointProperties("orderer", name);

    }

    private Properties getEndPointProperties(final String type, final String name) {

        final String domainName = getDomainName(name);

        File cert = Paths.get(getCryptoConfigPath(), "/ordererOrganizations".replace("orderer", type),
                domainName, type + "s",
                name, "tls/server.crt").toFile();
        if (!cert.exists()) {
            throw new RuntimeException(String.format("Missing cert file for: %s. Could not find at location: %s", name,
                    cert.getAbsolutePath()));
        }

        Properties ret = new Properties();
        ret.setProperty("pemFile", cert.getAbsolutePath());
        //      ret.setProperty("trustServerCertificate", "true"); //testing environment only NOT FOR PRODUCTION!
        ret.setProperty("hostnameOverride", name);
        ret.setProperty("sslProvider", "openSSL");
        ret.setProperty("negotiationType", "TLS");

        return ret;
    }

    public Properties getEventHubProperties(String name) {

        return getEndPointProperties("peer", name); //uses same as named peer

    }

    public String getTestChannelPath() {
        return "src/main/java/fixture/sdkintegration/e2e-2Orgs/channel";
    }

    public String getCryptoConfigPath() {
        return cryptoConfigDirSelected;
    }

    private String getDomainName(final String name) {
        int dot = name.indexOf(".");
        if (-1 == dot) {
            return null;
        } else {
            return name.substring(dot + 1);
        }
    }

}
