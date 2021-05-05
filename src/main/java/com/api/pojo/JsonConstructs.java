package com.api.pojo;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Data
public class JsonConstructs {
    private String[] fields;
    private int statusCode;
    private Object responsePayLoad;
    @JsonProperty(value="isStrictCompare")
    private boolean isStrictCompare;
    private DataMapper modifyType;
    @JsonProperty(value="isIgnore")
    private boolean isIgnore;


}
