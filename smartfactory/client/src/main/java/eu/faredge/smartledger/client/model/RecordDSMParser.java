
package eu.faredge.smartledger.client.model;

import com.google.gson.*;
import eu.faredge.dm.dsm.DSM;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.ArrayList;
import java.util.List;

public class RecordDSMParser {
    private static final Log logger = LogFactory.getLog(RecordDSMParser.class);

    public static DSM parse(JsonElement jsonElement) {
        JsonObject jsonObject = jsonElement.getAsJsonObject();
        JsonObject jobj = jsonObject.get("Record").getAsJsonObject();
        DSM dsm = new DSM();
        String id = jobj.get("id").getAsString();
        String dsd = jobj.get("dsd").getAsString();
        String macAddress = jobj.get("macAddress").getAsString();
        String dsmParameters = jobj.get("dsmParameters").getAsString();
        dsm.setId(id);
        dsm.setMacAddress(macAddress);
        dsm.setDataSourceDefinitionID(dsd);
        dsm.setDataSourceDefinitionParameters(parseParameters(dsmParameters));
        return dsm;
    }


    private static DSM.DataSourceDefinitionParameters parseParameters(String params) {
        try {
            if (StringUtils.isEmpty(params)) return null;
            DSM.DataSourceDefinitionParameters dataSourceDefinitionParameters = new DSM
                    .DataSourceDefinitionParameters();
            List<DSM.DataSourceDefinitionParameters.Parameters> parametersList = new ArrayList<>();
            dataSourceDefinitionParameters.getParameters().addAll(parametersList);
            if (!params.contains("parameters")) {
                DSM.DataSourceDefinitionParameters.Parameters parameters = new DSM.DataSourceDefinitionParameters
                        .Parameters();
                parameters.setKey(new String());
                parameters.setValue(new String());
                parametersList.add(parameters);
                return dataSourceDefinitionParameters;
            }
            JsonObject jobj = new Gson().fromJson(params, JsonObject.class);
            final JsonArray parametersJson = jobj.get("parameters").getAsJsonArray();

            for (JsonElement param : parametersJson) {
                DSM.DataSourceDefinitionParameters.Parameters parameters = new DSM.DataSourceDefinitionParameters
                        .Parameters();
                JsonObject obj = param.getAsJsonObject();
                parameters.setKey(obj.get("key").getAsString());
                parameters.setValue(obj.get("value").getAsString());
                parametersList.add(parameters);
            }
            dataSourceDefinitionParameters.getParameters().addAll(parametersList);
            return dataSourceDefinitionParameters;
        } catch (JsonSyntaxException e) {
            logger.warn(e);
            return null;
        }
    }
}

