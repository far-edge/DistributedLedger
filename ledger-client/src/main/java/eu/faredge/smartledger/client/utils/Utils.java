/*
 *
 *  Copyright 2016,2017 DTCC, Fujitsu Australia Software Technology, IBM - All Rights Reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *     http://www.apache.org/licenses/LICENSE-2.0
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */

package eu.faredge.smartledger.client.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import eu.faredge.dm.dcd.DCD;
import eu.faredge.dm.dcm.DCM;
import eu.faredge.dm.dsm.DSM;
import eu.faredge.smartledger.client.model.RecordDCDParser;
import eu.faredge.smartledger.client.model.RecordDCMParser;
import eu.faredge.smartledger.client.model.RecordDSMParser;
import org.apache.commons.compress.archivers.ArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveOutputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorOutputStream;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hyperledger.fabric.sdk.helper.Config;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import java.io.*;
import java.lang.reflect.Field;
import java.net.URL;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

import static java.lang.String.format;

public class Utils {

    public static final String REGEX_MAC_ADDRESS = "\\b([0-9a-fA-F]{2}:??){5}([0-9a-fA-F]{2})\\b";
    private static final Log logger = LogFactory.getLog(Utils.class);


    private Utils() {
    }

    /**
     * Sets the value of a field on an object
     *
     * @param o         The object that contains the field
     * @param fieldName The name of the field
     * @param value     The new value
     * @return The previous value of the field
     */
    public static Object setField(Object o, String fieldName, Object value) {
        Object oldVal = null;
        try {
            final Field field = o.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            oldVal = field.get(o);
            field.set(o, value);
        } catch (Exception e) {
            throw new RuntimeException("Cannot get value of field " + fieldName, e);
        }
        return oldVal;
    }

    /**
     * Sets a Config property value
     * <p>
     * The Config instance is initialized once on startup which means that
     * its properties don't change throughout its lifetime.
     * This method allows a Config property to be changed temporarily for testing purposes
     *
     * @param key   The key of the property (eg Config.LOGGERLEVEL)
     * @param value The new value
     * @return The previous value
     */
    public static String setConfigProperty(String key, String value) throws Exception {

        String oldVal = null;

        try {
            Config config = Config.getConfig();

            final Field sdkPropertiesInstance = config.getClass().getDeclaredField("sdkProperties");
            sdkPropertiesInstance.setAccessible(true);

            final Properties sdkProperties = (Properties) sdkPropertiesInstance.get(config);
            oldVal = sdkProperties.getProperty(key);
            sdkProperties.put(key, value);

        } catch (Exception e) {
            throw new RuntimeException("Failed to set Config property " + key, e);
        }

        return oldVal;
    }


    /**
     * Generate a targz inputstream from source folder.
     *
     * @param src        Source location
     * @param pathPrefix prefix to add to the all files found.
     * @return return inputstream.
     * @throws IOException
     */
    public static InputStream generateTarGzInputStream(File src, String pathPrefix) throws IOException {
        File sourceDirectory = src;

        ByteArrayOutputStream bos = new ByteArrayOutputStream(500000);

        String sourcePath = sourceDirectory.getAbsolutePath();

        TarArchiveOutputStream archiveOutputStream = new TarArchiveOutputStream(new GzipCompressorOutputStream(new
                BufferedOutputStream(bos)));
        archiveOutputStream.setLongFileMode(TarArchiveOutputStream.LONGFILE_GNU);

        try {
            Collection<File> childrenFiles = org.apache.commons.io.FileUtils.listFiles(sourceDirectory, null, true);

            ArchiveEntry archiveEntry;
            FileInputStream fileInputStream;
            for (File childFile : childrenFiles) {
                String childPath = childFile.getAbsolutePath();
                String relativePath = childPath.substring((sourcePath.length() + 1), childPath.length());

                if (pathPrefix != null) {
                    relativePath = org.hyperledger.fabric.sdk.helper.Utils.combinePaths(pathPrefix, relativePath);
                }

                relativePath = FilenameUtils.separatorsToUnix(relativePath);

                archiveEntry = new TarArchiveEntry(childFile, relativePath);
                fileInputStream = new FileInputStream(childFile);
                archiveOutputStream.putArchiveEntry(archiveEntry);

                try {
                    IOUtils.copy(fileInputStream, archiveOutputStream);
                } finally {
                    IOUtils.closeQuietly(fileInputStream);
                    archiveOutputStream.closeArchiveEntry();
                }
            }
        } finally {
            IOUtils.closeQuietly(archiveOutputStream);
        }

        return new ByteArrayInputStream(bos.toByteArray());
    }

    public static File findFileSk(File directory) {

        File[] matches = directory.listFiles((dir, name) -> name.endsWith("_sk"));

        if (null == matches) {
            throw new RuntimeException(format("Matches returned null does %s directory exist?", directory
                    .getAbsoluteFile().getName()));
        }

        if (matches.length != 1) {
            throw new RuntimeException(format("Expected in %s only 1 sk file but found %d", directory.getAbsoluteFile
                    ().getName(), matches.length));
        }

        return matches[0];

    }

    public static void out(String format, Object... args) {
        if (StringUtils.isEmpty(format) || null == args)
            return;
        //   System.err.flush();
        //  System.out.flush();

        if (StringUtils.isNotEmpty(format) && null != args)
            logger.info(format(format, args));
        //System.err.flush();
        //System.out.flush();

    }

    public static void fail(String message) {
        logger.error(message);
        if (message == null) {
            throw new RuntimeException();
        } else {
            throw new RuntimeException(message);
        }
    }

    /**
     * Fabric Certificate authority information
     * Contains information for the Fabric certificate authority
     */
    public static class HFCAInfo {

        private final String caName;
        private final String caChain;

        public HFCAInfo(String caName, String caChain) {
            this.caName = caName;
            this.caChain = caChain;
        }

        /**
         * The CAName for the Fabric Certificate Authority.
         *
         * @return The CA Name.
         */

        public String getCAName() {
            return caName;
        }

        /**
         * The Certificate Authority's Certificate Chain.
         *
         * @return Certificate Chain in X509 PEM format.
         */

        public String getCACertificateChain() {
            return caChain;
        }
    }

    /**
     * Transform payloads in DSM with Array Structure payload[0] = peer's name payload owner
     * payload[1] = Data coming from peer
     *
     * @param payloads
     * @return
     */

    public static List<DSM> extractDSMFromPayloads(List<String[]> payloads) {
        List<DSM> dsms = new ArrayList<>();
        payloads.stream().forEach(val -> {
            String dsmString = val[1];
            Utils.out(dsmString);
            if (null != dsmString && !StringUtils.isBlank(dsmString)) {
                try {
                    //RecordDSMParser[] recordDSMs = mapper.readValue(dsmString, RecordDSMParser[].class);
                    JsonParser parser = new JsonParser();
                    JsonArray recordDSMs = parser.parse(dsmString).getAsJsonArray();
                    for (JsonElement recordDSM : recordDSMs) {
                        if (null != recordDSM) {
                            dsms.add(RecordDSMParser.parse(recordDSM));
                        }
                    }
                } catch (Exception e) {
                    Utils.fail(e.getMessage());
                }
            }
        });
        return dsms;
    }

    /**
     * Transform payloads in DCM with Array Structure payload[0] = peer's name payload owner
     * payload[1] = Data coming from peer
     *
     * @param payloads
     * @return
     */
    public static List<DCM> extractDCMFromPayloads(List<String[]> payloads) {
        List<DCM> dcms = new ArrayList<>();
        payloads.stream().forEach(val -> {
            String dcmString = val[1];
            Utils.out(dcmString);
            if (null != dcmString && !StringUtils.isBlank(dcmString)) {
                try {
                    JsonParser parser = new JsonParser();
                    JsonArray recordDCMs = parser.parse(dcmString).getAsJsonArray();

                    for (JsonElement recordDCM : recordDCMs) {
                        if (null != recordDCM)
                            dcms.add(RecordDCMParser.parse(recordDCM));
                    }
                } catch (Exception e) {
                    Utils.fail(e.getMessage());
                }
            }
        });
        return dcms;
    }


    /**
     * Transform payloads in DCD with Array Structure payload[0] = peer's name payload owner
     * payload[1] = Data coming from peer
     *
     * @param payloads
     * @return
     */
    public static List<DCD> extractDCDFromPayloads(List<String[]> payloads) {
        List<DCD> dcds = new ArrayList<>();
        payloads.stream().forEach(val -> {
            String dcdString = val[1];
            Utils.out(dcdString);
            if (null != dcdString && !StringUtils.isBlank(dcdString)) {
                try {
                    JsonParser parser = new JsonParser();
                    JsonArray recordDCDs = parser.parse(dcdString).getAsJsonArray();
                    ;
                    for (JsonElement recordDCM : recordDCDs) {
                        if (null != recordDCM)
                            dcds.add(RecordDCDParser.parse(recordDCM));
                    }
                } catch (Exception e) {
                    Utils.fail(e.getMessage());
                }
            }
        });
        return dcds;
    }


    public static boolean validateUri(String uri) throws IllegalArgumentException {
        if (StringUtils.isEmpty(uri))
            throw new IllegalArgumentException("uri cannot be empty");
        final URL url;
        try {
            url = new URL(uri);
            return true;
        } catch (Exception e1) {
            return false;
        }
    }

    public static boolean validateMacAddress(String macAddress) throws IllegalArgumentException {
        if (StringUtils.isEmpty(macAddress))
            throw new IllegalArgumentException("macAddress cannot be empty");
        return macAddress.matches(REGEX_MAC_ADDRESS);
    }


    public static boolean areEquals(DSM a, DSM b) throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        String jsonA = mapper.writeValueAsString(a);
        String jsonB = mapper.writeValueAsString(b);
        if (jsonA.equalsIgnoreCase(jsonB))
            return true;
        return false;
    }

    public static boolean areEquals(DCM a, DCM b) throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        String jsonA = mapper.writeValueAsString(a);
        String jsonB = mapper.writeValueAsString(b);
        if (jsonA.equalsIgnoreCase(jsonB))
            return true;
        return false;
    }

    public static boolean areEquals(DCD a, DCD b) throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        String jsonA = mapper.writeValueAsString(a);
        String jsonB = mapper.writeValueAsString(b);
        if (jsonA.equalsIgnoreCase(jsonB))
            return true;
        return false;
    }


    public static String convertXmlGregorianToString(XMLGregorianCalendar xc) {
        TimeZone gmtTimeZone = TimeZone.getTimeZone("GMT");
        TimeZone fromTimeZone = TimeZone.getDefault();
        GregorianCalendar gCalendar = xc.toGregorianCalendar();
        DateFormat df = new SimpleDateFormat("MM/dd/yyyy hh:mm a z");
        Date date = adjustToTimezone(gCalendar.getTime(), fromTimeZone, gmtTimeZone);
        String dateString = df.format(date);
        return dateString;
    }

    private static Date adjustToTimezone(Date date, TimeZone fromZone, TimeZone toZone) {
        Date adjustedToTimezone = new Date(date.getTime() + toZone.getRawOffset() - fromZone.getRawOffset());
        return adjustedToTimezone;
    }


    public static XMLGregorianCalendar convertStringToXMLGregorianCalendr(String date) throws ParseException, DatatypeConfigurationException {
        TimeZone gmtTimeZone = TimeZone.getTimeZone("GMT");
        TimeZone fromTimeZone = TimeZone.getDefault();
        DateFormat df = new SimpleDateFormat("MM/dd/yyyy hh:mm a z");
        Date parse = df.parse(date);
        Date dates = adjustToTimezone(parse, fromTimeZone, gmtTimeZone);
        GregorianCalendar gCalendar = new GregorianCalendar();
        gCalendar.setTime(dates);
        return DatatypeFactory.newInstance().newXMLGregorianCalendar(gCalendar);

    }


}
