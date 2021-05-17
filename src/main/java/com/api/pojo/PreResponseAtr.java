package com.api.pojo;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Data
public class PreResponseAtr {
    private String pathOrName;
    @JsonProperty(value="isFromPreHeader")
    private boolean isFromPreHeader;
    private String updateKeyPath;
    @JsonProperty(value="isToMainHeader")
    private boolean isToMainHeader;
}
