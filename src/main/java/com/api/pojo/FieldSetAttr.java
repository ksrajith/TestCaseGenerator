package com.api.pojo;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Data
public class FieldSetAttr {
    private String field;
    private Object value;
}
