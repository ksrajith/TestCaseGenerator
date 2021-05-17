package com.api.pojo;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Data
public class DynamicData {
    private String path;
    private String type;
    private String isLength;
    @JsonProperty(value="isAppend")
    private boolean isAppend;
}
