package util;

import com.fasterxml.jackson.databind.ObjectMapper;

public class JsonUtil {
    private static final ObjectMapper mapper = new ObjectMapper();

    public static <T> T fromJson(String json, Class<T> clazz) {
        try { return mapper.readValue(json, clazz); }
        catch (Exception e) { throw new RuntimeException("JSON parse error", e); }
    }

    public static String toJson(Object obj) {
        try { return mapper.writeValueAsString(obj); }
        catch (Exception e) { throw new RuntimeException("JSON serialize error", e); }
    }
}