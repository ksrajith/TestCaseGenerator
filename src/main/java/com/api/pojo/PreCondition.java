package com.api.pojo;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Data
public class PreCondition {
    private Object jsonPre;
    private String preRequestType;
    private String preContentType;
    private String preRequestUrl;
    private String[] getResponseVals;
    private Headers[] preHeaders;
}
