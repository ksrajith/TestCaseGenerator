package com.api.pojo;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import java.util.Map;
import java.util.TreeMap;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Data
public class RestInvoke {
    private String urlInvoke;
    private String contentType;
    private String type;
    private String requestBody;
    private Map<String, String> requestHeaderMap = new TreeMap<>();
    private Map<String, String> responseHeaderMap = new TreeMap<>();
    private String pathParam;
    private int code;
    private String responseBody;
    private String expectedResponse;
    private int expectedCode;
    private boolean jsonCompare;
    private boolean codeCompare;
    private boolean isStrictCompare;
}
