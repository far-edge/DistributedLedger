/*
 *  Copyright 2016, 2017 DTCC, Fujitsu Australia Software Technology, IBM - All Rights Reserved.
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

package eu.faredge.smartledger.client.model;

import com.fasterxml.jackson.databind.ObjectMapper;
import eu.faredge.smartledger.client.model.certificate.CaUser;
import eu.faredge.smartledger.client.utils.Utils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.bouncycastle.asn1.pkcs.PrivateKeyInfo;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.openssl.PEMParser;
import org.bouncycastle.openssl.jcajce.JcaPEMKeyConverter;
import org.hyperledger.fabric.sdk.Enrollment;

import java.io.*;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.Security;
import java.security.spec.InvalidKeySpecException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * A local file-based key value store.
 */
public class Store {

    private static final String STORE_PATH = "";
    private String file;
    private Log logger = LogFactory.getLog(Store.class);

    public Store(File file) {
        if (!file.exists())
            try {
                if (file.createNewFile()) {
                    Utils.out("Successful created file: " + file.getAbsolutePath() + " !!!");
                } else {
                    Utils.out("Failed to create file: " + file.getAbsolutePath() + " !!!");

                }
            } catch (IOException e) {
                Utils.out(e.getMessage());
            }
        this.file = file.getAbsolutePath();
    }

    public Store() {
    }

    /**
     * Get the value associated with name.
     *
     * @param name
     * @return value associated with the name
     */
    public String getValue(String name) {
        Properties properties = loadProperties();
        return properties.getProperty(name);
    }

    /**
     * Has the value present.
     *
     * @param name
     * @return true if it's present.
     */
    public boolean hasValue(String name) {
        Properties properties = loadProperties();
        return properties.containsKey(name);
    }

    private Properties loadProperties() {
        Properties properties = new Properties();
        try (InputStream input = new FileInputStream(file)) {
            properties.load(input);
            input.close();
        } catch (FileNotFoundException e) {
            logger.warn(String.format("Could not find the file \"%s\"", file));
        } catch (IOException e) {
            logger.warn(String.format("Could not load keyvalue store from file \"%s\", reason:%s",
                    file, e.getMessage()));
        }

        return properties;
    }

    /**
     * Set the value associated with name.
     *
     * @param name  The name of the parameter
     * @param value Value for the parameter
     */
    public void setValue(String name, String value) {
        Properties properties = loadProperties();
        try (
                OutputStream output = new FileOutputStream(file)
        ) {
            properties.setProperty(name, value);
            properties.store(output, "");
            output.close();

        } catch (IOException e) {
            logger.warn(String.format("Could not save the keyvalue store, reason:%s", e.getMessage()));
        }
    }

    private final Map<String, User> members = new HashMap<>();

    /**
     * Get the user with a given name
     *
     * @param name
     * @param org
     * @return user
     */
    public User getMember(String name, String org) {

        // Try to get the User state from the cache
        User user = members.get(User.toKeyValStoreName(name, org));
        if (null != user) {
            return user;
        }

        // Create the User and try to restore it's state from the key value store (if found).
        user = new User(name, org, this);

        return user;

    }

    /**
     * Check if store has user.
     *
     * @param name
     * @param org
     * @return true if the user exists.
     */
    public boolean hasMember(String name, String org) {

        // Try to get the User state from the cache

        if (members.containsKey(User.toKeyValStoreName(name, org))) {
            return true;
        }
        return User.isStored(name, org, this);
    }

    /**
     * Get the user with a given name
     *
     * @param name
     * @param org
     * @param mspId
     * @param privateKeyFile
     * @param certificateFile
     * @return user
     * @throws IOException
     * @throws NoSuchAlgorithmException
     * @throws NoSuchProviderException
     * @throws InvalidKeySpecException
     */
    public User getMember(String name, String org, String mspId, File privateKeyFile,
                          File certificateFile) throws IOException, NoSuchAlgorithmException,
            NoSuchProviderException, InvalidKeySpecException {
        try {
            // Try to get the User state from the cache
            User user = members.get(User.toKeyValStoreName(name, org));
            if (null != user) {
                return user;
            }

            // Create the User and try to restore it's state from the key value store (if found).
            user = new User(name, org, this);
            user.setMspId(mspId);

            String certificate = new String(IOUtils.toByteArray(new FileInputStream(certificateFile)), "UTF-8");

            PrivateKey privateKey = getPrivateKeyFromBytes(IOUtils.toByteArray(new FileInputStream(privateKeyFile)));

            user.setEnrollment(new SampleStoreEnrollement(privateKey, certificate));

            user.saveState();

            return user;
        } catch (IOException e) {
            e.printStackTrace();
            throw e;

        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            throw e;
        } catch (NoSuchProviderException e) {
            e.printStackTrace();
            throw e;
        } catch (InvalidKeySpecException e) {
            e.printStackTrace();
            throw e;
        } catch (ClassCastException e) {
            e.printStackTrace();
            throw e;
        }
    }

    public User getMember(String name, String org, String storePath) throws IOException, NoSuchAlgorithmException,
            NoSuchProviderException, InvalidKeySpecException {
        try {
            // Try to get the User state from the cache
            User user = members.get(User.toKeyValStoreName(name, org));
            if (null != user) {
                return user;
            }

            // Create the User and try to restore it's state from the key value store (if found).
            user = new User();
            String storePathAbsolute = System.getProperty("user.home") + storePath + "/";
            ObjectMapper mapper = new ObjectMapper();
            CaUser caUser = mapper.readValue(new FileInputStream(storePathAbsolute + name), CaUser.class);
            user.setMspId(caUser.getMspid());
            user.setName(caUser.getName());
            String certificate = caUser.getEnrollment().getIdentity().getCertificate();
            String privateKeyStr = caUser.getEnrollment().getSigningIdentity() + "-priv";
            PrivateKey privateKey = getPrivateKeyFromBytes(IOUtils.toByteArray(new FileInputStream
                    (storePathAbsolute + privateKeyStr)));
            user.setEnrollment(new SampleStoreEnrollement(privateKey, certificate));
            user.saveState();
            return user;
        } catch (IOException e) {
            e.printStackTrace();
            throw e;

        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            throw e;
        } catch (NoSuchProviderException e) {
            e.printStackTrace();
            throw e;
        } catch (InvalidKeySpecException e) {
            e.printStackTrace();
            throw e;
        } catch (ClassCastException e) {
            e.printStackTrace();
            throw e;
        }
    }

    static {
        Security.addProvider(new BouncyCastleProvider());
    }

    static PrivateKey getPrivateKeyFromBytes(byte[] data) throws IOException, NoSuchProviderException,
            NoSuchAlgorithmException, InvalidKeySpecException {
        final Reader pemReader = new StringReader(new String(data));

        final PrivateKeyInfo pemPair;
        try (PEMParser pemParser = new PEMParser(pemReader)) {
            pemPair = (PrivateKeyInfo) pemParser.readObject();
        }

        PrivateKey privateKey = new JcaPEMKeyConverter().setProvider(BouncyCastleProvider.PROVIDER_NAME)
                .getPrivateKey(pemPair);

        return privateKey;
    }

    static final class SampleStoreEnrollement implements Enrollment, Serializable {

        private static final long serialVersionUID = -2784835212445309006L;
        private final PrivateKey privateKey;
        private final String certificate;


        SampleStoreEnrollement(PrivateKey privateKey, String certificate) {


            this.certificate = certificate;

            this.privateKey = privateKey;
        }

        @Override
        public PrivateKey getKey() {

            return privateKey;
        }

        @Override
        public String getCert() {
            return certificate;
        }

    }

}