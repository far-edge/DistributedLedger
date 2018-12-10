
package eu.faredge.smartledger.client.model;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import eu.faredge.dm.dcm.DCM;
import eu.faredge.smartledger.client.utils.Config;
import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;
import java.util.List;

public class RecordDCMParser {


    public static DCM parse(JsonElement jsonElement) {
        JsonObject jsonObject = jsonElement.getAsJsonObject();
        JsonObject jobj = jsonObject.get("Record").getAsJsonObject();
        DCM dcm = new DCM();
        String id = jobj.get("id").getAsString();
        String macAddress = jobj.get("macAddress").getAsString();
        String dsds = jobj.get("dsds").getAsString();
        dcm.setId(id);
        dcm.setMacAddress(macAddress);
        dcm.getDataSourceDefinitionsIDs().addAll(parseDataSourceDefinitionIDs(dsds));
        return dcm;
    }

    private static List<String> parseDataSourceDefinitionIDs(String dsds) {
        if (StringUtils.isEmpty(dsds)) return null;
        String[] strings = StringUtils.split(dsds, Config.CSV_DELIMITER_GOLANG);
        return Arrays.asList(strings);
    }


}

