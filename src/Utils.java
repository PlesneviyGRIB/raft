import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

public class Utils {
    private final static ObjectMapper objectMapper = new ObjectMapper();

    public static String writeObject(Object object) {
        try {
            return objectMapper.writeValueAsString(object);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    public static <V> V readObject(String object) {
        try {
            return objectMapper.readValue(object, new TypeReference<V>(){});
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

}
