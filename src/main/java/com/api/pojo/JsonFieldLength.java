package com.api.pojo;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Data
public class JsonFieldLength {
    private String key;
    private int length;
    private String type;
    private int statusCode;
    private Object expectedPayload;
    @JsonProperty(value="isStrictCompare")
    private boolean isStrictCompare;
    @JsonProperty(value="isIgnore")
    private boolean isIgnore;

}
