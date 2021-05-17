package com.api.pojo;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Data
public class ConfigJson {
    @JsonProperty(value="isPreRequest")
    private boolean isPreRequest;
    private PreCondition preSendJson;
    private Object jsonMain;
    private String mainRequestType;
    private String mainContentType;
    private String mainRequestUrl;
    private String[] constructJsonForEachField;
    private Headers[] mainHeaders;
    private DynamicData[] dynamicKeys;
    private PreResponseAtr[] fromPreResponse;
    private JsonConstructs[] modifyJson;
    private JsonFieldLength[] lengthCheck;
    private UpdateFieldValue[] valueUpdate;
}
