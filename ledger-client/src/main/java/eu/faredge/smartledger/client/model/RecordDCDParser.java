
package eu.faredge.smartledger.client.model;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import eu.faredge.dm.dcd.DCD;
import eu.faredge.smartledger.client.utils.Utils;

import javax.xml.datatype.DatatypeConfigurationException;
import java.text.ParseException;

public class RecordDCDParser {


    public static DCD parse(JsonElement jsonElement) throws ParseException, DatatypeConfigurationException {
        JsonObject jsonObject = jsonElement.getAsJsonObject();
        JsonObject jobj = jsonObject.get("Record").getAsJsonObject();
        DCD dcd = new DCD();
        String id = jobj.get("id").getAsString();
        String expirationDateTime = jobj.get("expirationDateTime").getAsString();
        String validFrom = jobj.get("validFrom").getAsString();
        String dsmId = jobj.get("dsmId").getAsString();
        String dcmId =  jobj.get("dcmId").getAsString();
        dcd.setId(id);
        dcd.setDataSourceManifestID(dsmId);
        dcd.setDataConsumerManifestID(dcmId);
        dcd.setExpirationDateTime(Utils.convertStringToXMLGregorianCalendr(expirationDateTime));
        dcd.setValidFrom(Utils.convertStringToXMLGregorianCalendr(validFrom));
        return dcd;
    }


}

