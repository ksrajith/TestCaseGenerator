package com.api.base;

import com.api.core.KeyMapper;
import com.api.pojo.*;
import com.github.wnameless.json.flattener.JsonFlattener;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.*;

public class RequestManager {

    public static ConfigJson mainJsonConf = new ConfigJson();

    private RestInvoke sendPreRequestCall(ConfigJson confObj){
        PreConditionRest preConditionRest = new PreConditionRest();
        return preConditionRest.validatePreCondition(confObj);
    }


    private RestInvoke requestUpdater(ConfigJson confObj){
        try {
            JsonUtils mnJson = new JsonUtils();
            return mnJson.updateJsonWithPreResponse(confObj, sendPreRequestCall(confObj));
        } catch (Exception ex){
            ex.printStackTrace();
            return null;
        }

    }

    public TreeMap<String, RestInvoke> prepareModifyJsonConfList(ConfigJson confObj){
        TreeMap <String, RestInvoke> treeMapModifyJson = new TreeMap<>();
        JsonUtils jsonUtils = new JsonUtils();
        if(confObj.getModifyJson().length != 0){
            for (JsonConstructs confJsn: confObj.getModifyJson()) {
                if(!confJsn.isIgnore() && confJsn.getModifyType() != null && confJsn.getFields().length !=0) {
                    Object json = null;
                    RestInvoke preReqest=null;
                    if(Runner.isPreRequest){
                        preReqest= requestUpdater(confObj);
                    }
                    json=assignJson(confObj);
                    if(confObj.getDynamicKeys().length > 0){
                        json= setValuesToDynamicKeys(confObj);
                    }
                    for (String field: confJsn.getFields()) {
                        String[] tmpArray = field.split("\\.");
                        switch (confJsn.getModifyType()) {
                            case ONLY_USE:
                                json = jsonUtils.onlyUsePayload(json, confJsn.getFields());
                                break;
                            case NULL_CHECK:
                                json = jsonUtils.updateOrRemoveJsonProperty(json, field, JSONObject.NULL, ConfigData.JsonBuildType.NULLCHECK, tmpArray[tmpArray.length-1]);
                                break;
                            case EMPTY_CHECK:
                                json = jsonUtils.updateOrRemoveJsonProperty(json, field, "", ConfigData.JsonBuildType.EMPTYCHECK, tmpArray[tmpArray.length-1]);
                                break;
                            case REMOVE_FIELD:
                                json = jsonUtils.updateOrRemoveJsonProperty(json, field, JSONObject.NULL, ConfigData.JsonBuildType.REMOVE, tmpArray[tmpArray.length-1]);
                                break;
                        }
                    }
                    RestInvoke restInvoke = new RestInvoke();
                    restInvoke.setExpectedCode(confJsn.getStatusCode());
                    restInvoke.setExpectedResponse(objectToJson(confJsn.getExpectedPayload()));
                    restInvoke.setRequestBody(json.toString());
                    restInvoke.setStrictCompare(confJsn.isStrictCompare());
                    restInvoke.setUrlInvoke(confObj.getMainRequestUrl());
                    restInvoke.setType(confObj.getMainRequestType());
                    restInvoke.setContentType(confObj.getMainContentType());
                    restInvoke.setRequestHeaderMap(setMainHeader(preReqest !=null ? preReqest.getResponseHeaderMap() :null, confObj.getMainHeaders()));
                    treeMapModifyJson.put(confJsn.getModifyType()+"__"+String.join("__", confJsn.getFields()),restInvoke);
                }

                }
        }
        return treeMapModifyJson;
    }

    /*
       JSON create as per the length requests
    */
    public TreeMap<String, RestInvoke>  jsonLengthModify(TreeMap<String, RestInvoke> tmpMap, ConfigJson confObj){
        JsonUtils jsonUtils = new JsonUtils();
        if(confObj.getLengthCheck().length > 0){
            for (JsonFieldLength lengthJsn: confObj.getLengthCheck()) {
                Object json = null;
                RestInvoke preReqest=null;
                if(Runner.isPreRequest){
                    preReqest= requestUpdater(confObj);
                }
                json=assignJson(confObj);
                if(confObj.getDynamicKeys().length > 0){
                    json= setValuesToDynamicKeys(confObj);
                }
                if(!lengthJsn.isIgnore() && lengthJsn.getKey() != null && lengthJsn.getType() != null){
                    Object value = attrValueGenerate(lengthJsn.getType(), lengthJsn.getLength());
                    String [] str = lengthJsn.getKey().split("\\.");
                    String targetKey = str[str.length-1];
                    json = jsonUtils.updateOrRemoveJsonProperty(json, lengthJsn.getKey(), value, ConfigData.JsonBuildType.LENGTHCHECK, targetKey);
                }
                RestInvoke restInvoke = new RestInvoke();
                restInvoke.setExpectedCode(lengthJsn.getStatusCode());
                restInvoke.setExpectedResponse(objectToJson(lengthJsn.getExpectedPayload()));
                restInvoke.setRequestHeaderMap(setMainHeader(preReqest !=null ? preReqest.getResponseHeaderMap() :null, confObj.getMainHeaders()));
                restInvoke.setStrictCompare(lengthJsn.isStrictCompare());
                restInvoke.setUrlInvoke(confObj.getMainRequestUrl());
                restInvoke.setType(confObj.getMainRequestType());
                restInvoke.setContentType(confObj.getMainContentType());
                restInvoke.setRequestBody(json.toString());
                tmpMap.put("LENGTH_CHECK"+"__"+lengthJsn.getKey(),restInvoke);

            }
        }
        return tmpMap;
    }

    // Update the JSON as per the configed value
    public TreeMap<String, RestInvoke>  jsonValueModify(TreeMap<String, RestInvoke> tmpMap, ConfigJson confObj){
        JsonUtils jsonUtils = new JsonUtils();
        if(confObj.getLengthCheck().length > 0){
            for (UpdateFieldValue updteAtr: confObj.getValueUpdate()) {
                Object json = null;
                RestInvoke preReqest=null;
                if(Runner.isPreRequest){
                    preReqest= requestUpdater(confObj);
                }
                json=assignJson(confObj);
                if(confObj.getDynamicKeys().length > 0){
                    json= setValuesToDynamicKeys(confObj);
                }
                if(!updteAtr.isIgnore() && updteAtr.getFieldSet().length != 0){
                    String str="";
                    for (FieldSetAttr field: updteAtr.getFieldSet()) {
                        String [] strAry = field.getField().split("\\.");
                        String targetKey = strAry[strAry.length-1];
                        json = jsonUtils.updateOrRemoveJsonProperty(json, field.getField(), field.getValue(), ConfigData.JsonBuildType.VALUEUPDATE, targetKey);
                        str = "__"+field.getField();
                    }
                    RestInvoke restInvoke = new RestInvoke();
                    restInvoke.setExpectedCode(updteAtr.getStatusCode());
                    restInvoke.setExpectedResponse(objectToJson(updteAtr.getExpectedPayload()));
                    restInvoke.setRequestHeaderMap(setMainHeader(preReqest !=null ? preReqest.getResponseHeaderMap() :null, confObj.getMainHeaders()));
                    restInvoke.setStrictCompare(updteAtr.isStrictCompare());
                    restInvoke.setUrlInvoke(confObj.getMainRequestUrl());
                    restInvoke.setType(confObj.getMainRequestType());
                    restInvoke.setContentType(confObj.getMainContentType());
                    restInvoke.setRequestBody(json.toString());
                    tmpMap.put("VALUE_UPDATE"+str,restInvoke);
                }
            }
        }
        return tmpMap;
    }

    // generate json for each field
    public TreeMap<String, RestInvoke> constructJsonForEachField(TreeMap<String, RestInvoke> tmpMap, ConfigJson confObj){
        JsonUtils jsonUtils = new JsonUtils();

        if(confObj.getConstructJsonForEachField().length != 0){
            for (String str: confObj.getConstructJsonForEachField()) {
                Map<String, Object> flattenJson =null;
                try {
                    flattenJson = JsonFlattener.flattenAsMap((confObj.getJsonMain()).toString());
                } catch (RuntimeException re){
                    flattenJson = JsonFlattener.flattenAsMap((new JSONObject((LinkedHashMap) confObj.getJsonMain())).toString());
                }

                for (Map.Entry<String, Object> entry: flattenJson.entrySet()) {
                    Object json = null;
                    RestInvoke preReqest=null;
                    if(Runner.isPreRequest){
                        preReqest= requestUpdater(confObj);
                    }
                    String[] tmpArray = entry.getKey().split("\\.");
                    json=assignJson(confObj);
                    if(confObj.getDynamicKeys().length > 0){
                        json= setValuesToDynamicKeys(confObj);
                    }
                    switch (str) {
                        case "NULL_CHECK":
                            json =  jsonUtils.updateOrRemoveJsonProperty(json, entry.getKey(),JSONObject.NULL,ConfigData.JsonBuildType.NULLCHECK,tmpArray[tmpArray.length-1]);
                            break;
                        case "EMPTY_CHECK":
                            json =  jsonUtils.updateOrRemoveJsonProperty(json, entry.getKey(),"",ConfigData.JsonBuildType.EMPTYCHECK,tmpArray[tmpArray.length-1]);
                            break;
                        case "REMOVE_FIELD":
                            json =  jsonUtils.updateOrRemoveJsonProperty(json, entry.getKey(),JSONObject.NULL,ConfigData.JsonBuildType.REMOVE,tmpArray[tmpArray.length-1]);
                            break;
                    }
                    RestInvoke restInvoke = new RestInvoke();
                    restInvoke.setRequestHeaderMap(setMainHeader(preReqest !=null ? preReqest.getResponseHeaderMap() :null, confObj.getMainHeaders()));
                    restInvoke.setUrlInvoke(confObj.getMainRequestUrl());
                    restInvoke.setType(confObj.getMainRequestType());
                    restInvoke.setContentType(confObj.getMainContentType());
                    restInvoke.setRequestBody(json.toString());
                    tmpMap.put(str +"__"+entry.getKey(), restInvoke);
                }
            }
        }
        return tmpMap;
    }

    private Object assignJson(ConfigJson cnfJson){
        Object json;
        try {
            json = new JSONObject(cnfJson.getJsonMain().toString());
        } catch (Exception ex){
            json = new JSONArray((ArrayList)cnfJson.getJsonMain());
        }

        return json;
    }

    private Map<String, String> setMainHeader(Map<String, String> responseHeaders, Headers[]  headers){
        Map<String, String> headerMap = new LinkedHashMap<>();
        if(responseHeaders != null && responseHeaders.size() > 0){
            headerMap = responseHeaders;
        }
        for (Headers header:headers ) {
            headerMap.put(header.getHeader(), header.getValue());
        }
        return headerMap;
    }

    private JSONObject setValuesToDynamicKeys(ConfigJson confObj){
        JsonUtils jsonUtils = new JsonUtils();
        JSONObject json=null;
        for (DynamicData dynamic: confObj.getDynamicKeys()) {
            int val=8;
            if(StringUtils.isEmpty(dynamic.getIsLength())){
                val = Integer.getInteger(dynamic.getIsLength());
            }
            Object tmpValue = attrValueGenerate(dynamic.getType(), val);
                if(dynamic.isAppend()){
                    Object appenStr;
                    if (confObj.getJsonMain() instanceof JSONObject){
                        appenStr = jsonUtils.getJsonValue(confObj.getJsonMain(),dynamic.getPath());
                    } else {
                        appenStr = jsonUtils.getJsonValue(new JSONObject((LinkedHashMap)confObj.getJsonMain()),dynamic.getPath());
                    }
                    tmpValue = appenStr.toString().concat(tmpValue.toString());
                }
                String [] strAry = dynamic.getPath().split("\\.");
                String targetKey = strAry[strAry.length-1];
            try {
                json = new JSONObject(confObj.getJsonMain().toString());
            } catch (Exception ex){
                ex.printStackTrace();
            }
                json = jsonUtils.updateOrRemoveJsonProperty(json, dynamic.getPath(), tmpValue, ConfigData.JsonBuildType.VALUEUPDATE, targetKey);
        }
        return json;
    }

    private String objectToJson(Object obj){
        try {
            if (obj instanceof LinkedHashMap){
                return (new JSONObject((LinkedHashMap)obj).toString());
            } else if (obj instanceof ArrayList){
                return (new JSONArray((ArrayList)obj).toString());
            }
            return obj.toString();
        } catch (Exception ex){
            return null;
        }
    }

    // generate value for given type and limitation
    private Object attrValueGenerate (String type, int val){
        Object value = null;
        switch (type) {
            case "UUID": value = UUID.randomUUID();
                break;
            case "numOnly": value = RandomStringUtils.randomNumeric(val);
                break;
            case "charOnly": value = RandomStringUtils.randomAlphabetic(val);
                break;
            case "numNChars": value = RandomStringUtils.randomAlphanumeric(val);
                break;
        }

        return value;
    }

}
