package com.api.pojo;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Data
public class UpdateFieldValue {
    private int statusCode;
    private Object expectedPayload;
    @JsonProperty(value="isStrictCompare")
    private boolean isStrictCompare;
    private String type;
    private FieldSetAttr[] fieldSet;
    @JsonProperty(value="isIgnore")
    private boolean isIgnore;
}
