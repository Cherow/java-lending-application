package com.example.main.common;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class Utils {

    public static Object setJsonStringToObject(String content, Class<?> object) {
        ObjectMapper mapper = new ObjectMapper();
        try {
            return mapper.readValue(content, object);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    public static String setJsonString(Object content) {
        ObjectMapper mapper = new ObjectMapper();
        try {
            return mapper.writeValueAsString(content);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }
}
