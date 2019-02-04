package utilis;


import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.List;


public class JsonConverter {
    private static Log log = LogFactory.getLog(JsonConverter.class);

    public static String convertToJson(Object obj) throws Throwable {
        try {
            ObjectMapper mapper = new ObjectMapper();
            mapper.setPropertyNamingStrategy(PropertyNamingStrategy.UPPER_CAMEL_CASE); //This property put data in upper camel case
            return mapper.writeValueAsString(obj);
        } catch (Exception e) {
            log.error(e);
            throw new Throwable(e);
        }
    }

    public static String convertToJsonNode(Object obj) throws Throwable {
        try {
            ObjectMapper mapper = new ObjectMapper();
            return mapper.writeValueAsString(obj);
        } catch (Exception e) {
            log.error(e);
            throw new Throwable(e);
        }
    }

    public static Object convertFromJson(String json, Class clazz, boolean isCollection) throws Throwable {
        try {
            if (StringUtils.isEmpty(json))
                throw new Throwable("Json data is EMPTY");
            ObjectMapper mapper = new ObjectMapper();
            mapper.configure(MapperFeature.ACCEPT_CASE_INSENSITIVE_PROPERTIES, true); //This property serialize/deserialize not considering the case of fields
            if (isCollection) {
                //ObjectReader objectReader = mapper.reader().forType(new TypeReference<List<?>>() {
                //});
                // return objectReader.readValue(json);
                return mapper.readValue(json, mapper.getTypeFactory().constructCollectionType(List.class, clazz));
            }
            return mapper.readValue(json, clazz);
        } catch (Exception e) {
            log.error(e);
            throw new Throwable(e);
        }
    }


}


