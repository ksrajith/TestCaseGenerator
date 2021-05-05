package com.api.base;

import com.fasterxml.jackson.databind.JsonNode;

public class JsonReader {

    public Object fileReader(String src, String clsName) {
        try {
            JsonNode jsonNode = JsonMapper.parse( src);
            return JsonMapper.fromJson(jsonNode, Class.forName(clsName));

        }catch (Exception ex){
            ex.printStackTrace();
            return null;
        }
    }
}
