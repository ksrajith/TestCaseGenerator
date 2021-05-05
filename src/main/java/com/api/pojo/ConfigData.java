package com.api.pojo;

public class ConfigData {

    private String[] nullChk;
    private String jsnStr="";
    private String[] emptyChk;
    private String targetKey ="";
    private String[] removeField;
    private String[] valueUpdate;
    private String[] lengthCheck;
    private String postConfigURL;

    public enum JsonBuildType {
        EMPTYCHECK,
        REMOVE,
        LENGTHCHECK,
        NULLCHECK,
        VALUEUPDATE
    }

    public String[] getNullChk() {
        return nullChk;
    }

    public void setNullChk(String[] nullChk) {
        this.nullChk = nullChk;
    }

    public String getJsnStr() {
        return jsnStr;
    }

    public void setJsnStr(String jsnStr) {
        this.jsnStr = jsnStr;
    }

    public String[] getEmptyChk() {
        return emptyChk;
    }

    public void setEmptyChk(String[] emptyChk) {
        this.emptyChk = emptyChk;
    }

    public String getTargetKey() {
        return targetKey;
    }

    public void setTargetKey(String targetKey) {
        this.targetKey = targetKey;
    }

    public String[] getRemoveField() {
        return removeField;
    }

    public void setRemoveField(String[] removeField) {
        this.removeField = removeField;
    }

    public String[] getValueUpdate() {
        return valueUpdate;
    }

    public void setValueUpdate(String[] valueUpdate) {
        this.valueUpdate = valueUpdate;
    }

    public String[] getLengthCheck() {
        return lengthCheck;
    }

    public void setLengthCheck(String[] lengthCheck) {
        this.lengthCheck = lengthCheck;
    }

    public String getPostConfigURL() {
        return postConfigURL;
    }

    public void setPostConfigURL(String postConfigURL) {
        this.postConfigURL = postConfigURL;
    }
}
