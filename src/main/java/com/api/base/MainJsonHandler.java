package com.api.base;

import com.api.core.KeyMapper;
import com.api.pojo.*;
import com.github.wnameless.json.flattener.JsonFlattener;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.*;

public class MainJsonHandler {

    private ConfigJson mainConf=new ConfigJson();
    public void mainJsonConstruct(ConfigJson obj, RestInvoke restInvoke) throws JSONException {
        TreeMap<String, RestInvoke> tmpMap = new TreeMap<>();
        if(obj.isPreRequest() && obj.getJsonMain() != null && obj.getSetFromPreResponse().length != 0 && obj.getSetFromPreResponse()[0].getPathOrName() != null) {
           tmpMap = updateJsonWithPreResponse(obj, restInvoke);
         } else {
            tmpMap.put(KeyMapper.PREINVOKE,restInvoke);
            mainConf=obj;
        }

//        if(obj.getOnlyUsing() != null && obj.getOnlyUsing().length != 0 && obj.getOnlyUsing()[0].getFields().length != 0){
//            useOnlyJsonFields(tmpMap);
//        }
        // JsonFlattener.flattenAsMap((new JSONObject((LinkedHashMap)obj.getJsonMain())).toString())


//        if(obj.getNullCheck() != null && obj.getNullCheck().length != 0 && obj.getNullCheck()[0].getFields().length != 0){
//            updateJson(tmpMap);
//        }

        prepareJSONConstructList(tmpMap);
        jsonLengthModify(tmpMap);
        jsonValueModify(tmpMap);
        constructJsonForEachField(tmpMap);
        tmpMap.remove(KeyMapper.PREINVOKE);
        ValidateMainRequest validateMainRequest = new ValidateMainRequest();
        validateMainRequest.iterateRequestMap(tmpMap);
        System.out.println(tmpMap.size());
    }

    public TreeMap<String, RestInvoke> prepareJSONConstructList(TreeMap<String, RestInvoke> tmpMap){
        ConfigJson cnfJson = mainConf;
        if(cnfJson.getModifyJson().length != 0){
            for (JsonConstructs confJsn: cnfJson.getModifyJson()) {
                Object json = null;
                try {
                    json = new JSONObject((cnfJson.getJsonMain()).toString());
                } catch (Exception ex){
                    json = new JSONArray((ArrayList)cnfJson.getJsonMain());
                }
                if(!confJsn.isIgnore() && confJsn.getModifyType() != null && confJsn.getFields().length !=0){
                    for (String field: confJsn.getFields()) {
                        String[] tmpArray = field.split("\\.");
                    switch (confJsn.getModifyType()) {
                        case ONLY_USE:
                            json = onlyUsePayload(json, confJsn.getFields());
                            break;
                        case NULL_CHECK:
                            json = updateOrRemoveJsonProperty(json, field, JSONObject.NULL, ConfigData.JsonBuildType.NULLCHECK, tmpArray[tmpArray.length-1]);
                            break;
                        case EMPTY_CHECK:
                            json = updateOrRemoveJsonProperty(json, field, "", ConfigData.JsonBuildType.EMPTYCHECK, tmpArray[tmpArray.length-1]);
                            break;
                        case REMOVE_FIELD:
                            json = updateOrRemoveJsonProperty(json, field, JSONObject.NULL, ConfigData.JsonBuildType.REMOVE, tmpArray[tmpArray.length-1]);
                            break;
                    }
                    }
                    RestInvoke restInvoke = new RestInvoke();
                    restInvoke.setExpectedCode(confJsn.getStatusCode());
                    restInvoke.setExpectedResponse(confJsn.getResponsePayLoad().toString());
                    restInvoke.setRequestBody(json.toString());
                    restInvoke.setStrictCompare(confJsn.isStrictCompare());
                    restInvoke.setUrlInvoke(cnfJson.getMainRequestUrl());
                    restInvoke.setType(cnfJson.getMainRequestType());
                    restInvoke.setContentType(cnfJson.getMainContentType());
                    if(tmpMap.get(KeyMapper.MAININVOKE) != null && (tmpMap.get(KeyMapper.MAININVOKE)).getRequestHeaderMap().size() > 0) {
                        restInvoke.setRequestHeaderMap((tmpMap.get(KeyMapper.MAININVOKE)).getRequestHeaderMap());
                    } else {
                        restInvoke.setRequestHeaderMap(setMainHeader(cnfJson.getMainHeaders()));
                    }
                    tmpMap.put(confJsn.getModifyType()+"__"+String.join("__", confJsn.getFields()),restInvoke);
                }
            }
        }

        return tmpMap;
    }

    private Map<String, String> setMainHeader(Headers[]  headers){
        Map<String, String> headerMap = new LinkedHashMap<>();
        for (Headers header:headers ) {
            headerMap.put(header.getHeader(), header.getValue());
        }
        return headerMap;
    }

    public Object onlyUsePayload(Object jsonPayload, String[] fields){
        ConfigJson cnfJson = mainConf;
        Map<String, Object> flattenJson = JsonFlattener.flattenAsMap(jsonPayload.toString());
        List<String> feldLst = new LinkedList<>(Arrays.asList(fields));

        Iterator<Map.Entry<String,Object>> iter = flattenJson.entrySet().iterator();
        while (iter.hasNext()) {
            Map.Entry<String,Object> entry = iter.next();
            for (String str: feldLst) {
                if(entry.getKey().equals(str)){
                    iter.remove();
                    feldLst.remove(str);
                }
            }
        }

        Object json = null;

            try {
                json = new JSONObject(cnfJson.getJsonMain().toString());
            } catch (ClassCastException ex) {
                try {
                    json = new JSONObject((LinkedHashMap) cnfJson.getJsonMain());

                } catch (ClassCastException ext) {
                    json = new JSONArray((ArrayList) cnfJson.getJsonMain());
                }
            } catch (JSONException ext){
                System.out.println("Exeception...........");
            }

        for (Map.Entry<String, Object> entry: flattenJson.entrySet()) {
            String[] tmpArray = entry.getKey().split("\\.");
            String targetKey =tmpArray[tmpArray.length-1];
            if(targetKey.matches(KeyMapper.KEYLSTREGX)){
                String[] matcher = targetKey.replaceFirst(KeyMapper.KEYLSTREGX, "$1, $2").split(",");
                targetKey = matcher[0].trim();
            }

            json =  updateOrRemoveJsonProperty(json, entry.getKey(),JSONObject.NULL,ConfigData.JsonBuildType.REMOVE,targetKey);

        }

        Iterator<String> iterator = ((JSONObject)json).keys();
        while (iterator.hasNext()) {
            String key = iterator.next();
            boolean fiedStatus=false;
            for (String str: fields ) {
                if(str.contains(key)){
                    fiedStatus=true;
                }
            }

            if(!fiedStatus){
                json =  updateOrRemoveJsonProperty(json, key,JSONObject.NULL,ConfigData.JsonBuildType.REMOVE,key);
                iterator = ((JSONObject)json).keys();
            }
        }

        return json;
    }

    /*
    Update Main JSON with pre-request data
     */
    public TreeMap<String, RestInvoke> updateJsonWithPreResponse(ConfigJson obj, RestInvoke restInvoke) throws JSONException {
            TreeMap<String, RestInvoke> restInvokeNConfigList = new TreeMap<>();
            RestInvoke mainInvoke = new RestInvoke();
            Object json = new JSONObject(obj.getJsonMain().toString());
            mainInvoke.setRequestHeaderMap(setMainHeader(obj.getMainHeaders()));
            for (PreResponseAtr pre: obj.getSetFromPreResponse()) {
                if(pre.isFromPreHeader()){
                    String headerVal = restInvoke.getResponseHeaderMap().get(pre.getPathOrName());
                    if(pre.isToMainHeader()) {
                        mainInvoke.getRequestHeaderMap().put(pre.getPathOrName(), headerVal);
                    } else {
                        String [] str = pre.getPathOrName().split("\\.");
                        String targetKey = str[str.length-1];
                        json = updateOrRemoveJsonProperty(json, pre.getUpdateKeyPath(), headerVal, ConfigData.JsonBuildType.VALUEUPDATE,targetKey);

                    }
                } else {
                    Object tmpJsonVal = returnJsonValue(restInvoke.getResponseBody(),pre.getPathOrName());
                    if(pre.isToMainHeader()){
                        mainInvoke.getRequestHeaderMap().put(pre.getUpdateKeyPath(), tmpJsonVal.toString());
                    } else {
                        String[] tmpArray = pre.getUpdateKeyPath().split("\\.");
                        String targetKey =tmpArray[tmpArray.length-1];
                        if(targetKey.matches(KeyMapper.KEYLSTREGX)){
                            String[] matcher = targetKey.replaceFirst(KeyMapper.KEYLSTREGX, "$1, $2").split(",");
                            targetKey = matcher[0].trim();
                        }

                       json = updateOrRemoveJsonProperty(json, pre.getUpdateKeyPath(), tmpJsonVal, ConfigData.JsonBuildType.VALUEUPDATE,targetKey);
                       //json = updateJsonValues((LinkedHashMap) json, pre.getUpdateKeyPath(),tmpJsonVal);
                    }
                    }
            }
            obj.setJsonMain(json);
            mainInvoke.setContentType(obj.getMainContentType());
            mainInvoke.setType(obj.getMainRequestType());
            mainInvoke.setUrlInvoke(obj.getMainRequestUrl());
            mainInvoke.setRequestBody(json.toString());
            restInvokeNConfigList.put(KeyMapper.PREINVOKE,restInvoke);
            restInvokeNConfigList.put(KeyMapper.MAININVOKE,mainInvoke);
            mainConf = obj;
            return restInvokeNConfigList;
    }

    public Object returnJsonValue(Object json, String jsnPth) {
        try{
            Map<String, Object> flattenJson = JsonFlattener.flattenAsMap(json.toString());
            return flattenJson.get(jsnPth);
        }catch (Exception ex){
            return null;
        }

    }

    /*
        JSON create as per the length requests
     */
    public TreeMap<String, RestInvoke>  jsonLengthModify(TreeMap<String, RestInvoke> tmpMap){
        ConfigJson cnfJson = mainConf;
        if(cnfJson.getLengthCheck().length > 0){
            for (JsonFieldLength lengthJsn: cnfJson.getLengthCheck()) {
                Object json = null;
                try {
                    json = new JSONObject(cnfJson.getJsonMain().toString());
                } catch (Exception ex){
                    json = new JSONArray((ArrayList)cnfJson.getJsonMain());
                }
                if(!lengthJsn.isIgnore() && lengthJsn.getKey() != null && lengthJsn.getType() != null){
                    Object value = attrValueGenerate(lengthJsn.getType(), lengthJsn.getLength());
                    String [] str = lengthJsn.getKey().split("\\.");
                    String targetKey = str[str.length-1];
                    json = updateOrRemoveJsonProperty(json, lengthJsn.getKey(), value, ConfigData.JsonBuildType.LENGTHCHECK, targetKey);
                }
                RestInvoke restInvoke = new RestInvoke();
                restInvoke.setExpectedCode(lengthJsn.getStatusCode());
                restInvoke.setExpectedResponse(lengthJsn.getResponsePayLoad().toString());
                if(tmpMap.get(KeyMapper.MAININVOKE) != null && (tmpMap.get(KeyMapper.MAININVOKE)).getRequestHeaderMap().size() > 0) {
                    restInvoke.setRequestHeaderMap((tmpMap.get(KeyMapper.MAININVOKE)).getRequestHeaderMap());
                } else {
                    restInvoke.setRequestHeaderMap(setMainHeader(cnfJson.getMainHeaders()));
                }
                restInvoke.setStrictCompare(lengthJsn.isStrictCompare());
                restInvoke.setUrlInvoke(cnfJson.getMainRequestUrl());
                restInvoke.setType(cnfJson.getMainRequestType());
                restInvoke.setContentType(cnfJson.getMainContentType());
                restInvoke.setRequestBody(json.toString());
                tmpMap.put("LENGTH_CHECK"+"__"+lengthJsn.getKey(),restInvoke);

            }
        }
        return tmpMap;
    }

    public TreeMap<String, RestInvoke>  jsonValueModify(TreeMap<String, RestInvoke> tmpMap){
        ConfigJson cnfJson = mainConf;
        if(cnfJson.getLengthCheck().length > 0){
            for (UpdateFieldValue updteAtr: cnfJson.getValueUpdate()) {
                Object json = null;
                try {
                    json = new JSONObject(cnfJson.getJsonMain().toString());
                } catch (Exception ex){
                    json = new JSONArray((ArrayList)cnfJson.getJsonMain());
                }
                if(!updteAtr.isIgnore() && updteAtr.getFieldSet().length != 0){
                    String str="";
                    for (FieldSetAttr field: updteAtr.getFieldSet()) {
                        String [] strAry = field.getField().split("\\.");
                        String targetKey = strAry[strAry.length-1];
                        json = updateOrRemoveJsonProperty(json, field.getField(), field.getValue(), ConfigData.JsonBuildType.VALUEUPDATE, targetKey);
                        str = "__"+field.getField();
                    }
                    RestInvoke restInvoke = new RestInvoke();
                    restInvoke.setExpectedCode(updteAtr.getStatusCode());
                    restInvoke.setExpectedResponse(updteAtr.getExpectedPayload());
                    if(tmpMap.get(KeyMapper.MAININVOKE) != null && (tmpMap.get(KeyMapper.MAININVOKE)).getRequestHeaderMap().size() > 0) {
                        restInvoke.setRequestHeaderMap((tmpMap.get(KeyMapper.MAININVOKE)).getRequestHeaderMap());
                    } else {
                        restInvoke.setRequestHeaderMap(setMainHeader(cnfJson.getMainHeaders()));
                    }
                    restInvoke.setStrictCompare(updteAtr.isStrictCompare());
                    restInvoke.setUrlInvoke(cnfJson.getMainRequestUrl());
                    restInvoke.setType(cnfJson.getMainRequestType());
                    restInvoke.setContentType(cnfJson.getMainContentType());
                    restInvoke.setRequestBody(json.toString());
                    tmpMap.put("VALUE_UPDATE"+str,restInvoke);
                }
            }
        }
        return tmpMap;
    }

    public TreeMap<String, RestInvoke> constructJsonForEachField(TreeMap<String, RestInvoke> tmpMap){
        ConfigJson cnfJson = mainConf;
        Object json = null;

        if(cnfJson.getConstructJsonForEachField().length != 0){
            for (String str:cnfJson.getConstructJsonForEachField()) {
                Map<String, Object> flattenJson =null;
                try {
                    flattenJson = JsonFlattener.flattenAsMap((cnfJson.getJsonMain()).toString());
                } catch (RuntimeException re){
                    flattenJson = JsonFlattener.flattenAsMap((new JSONObject((LinkedHashMap)cnfJson.getJsonMain())).toString());
                }

                for (Map.Entry<String, Object> entry: flattenJson.entrySet()) {
                    String[] tmpArray = entry.getKey().split("\\.");
                    try {
                        json = new JSONObject(cnfJson.getJsonMain().toString());
                    } catch (Exception ex){
                        json = new JSONArray((ArrayList)cnfJson.getJsonMain());
                    }
                    switch (str) {
                        case "NULL_CHECK":
                            json =  updateOrRemoveJsonProperty(json, entry.getKey(),JSONObject.NULL,ConfigData.JsonBuildType.NULLCHECK,tmpArray[tmpArray.length-1]);
                            break;
                        case "EMPTY_CHECK":
                            json =  updateOrRemoveJsonProperty(json, entry.getKey(),"",ConfigData.JsonBuildType.EMPTYCHECK,tmpArray[tmpArray.length-1]);
                            break;
                        case "REMOVE_FIELD":
                            json =  updateOrRemoveJsonProperty(json, entry.getKey(),JSONObject.NULL,ConfigData.JsonBuildType.REMOVE,tmpArray[tmpArray.length-1]);
                            break;
                    }
                    RestInvoke restInvoke = new RestInvoke();
                    if(tmpMap.get(KeyMapper.MAININVOKE) != null && (tmpMap.get(KeyMapper.MAININVOKE)).getRequestHeaderMap().size() > 0) {
                        restInvoke.setRequestHeaderMap((tmpMap.get(KeyMapper.MAININVOKE)).getRequestHeaderMap());
                    } else {
                        restInvoke.setRequestHeaderMap(setMainHeader(cnfJson.getMainHeaders()));
                    }
                    restInvoke.setUrlInvoke(cnfJson.getMainRequestUrl());
                    restInvoke.setType(cnfJson.getMainRequestType());
                    restInvoke.setContentType(cnfJson.getMainContentType());
                    restInvoke.setRequestBody(json.toString());
                    tmpMap.put(str +"__"+entry.getKey(), restInvoke);
                }
            }
        }
        return tmpMap;
    }
/*
    public Object returnJsonValues(LinkedHashMap jsonMap, String jsnPth){
        try{
            List<String> strList = new LinkedList<String>(Arrays.asList(jsnPth.split("\\.")));
        if(jsonMap.size() >0 ){
            if (strList.size() > 1){
                String tmpKey= strList.get(0);
                if (jsonMap.get(tmpKey) instanceof LinkedHashMap){
                    strList.remove(0);
                    returnJsonValues((LinkedHashMap) jsonMap.get(tmpKey),String.join(".",strList));
                } else {
                    return jsonMap.get(strList.get(0));
                }

            } else {
                return jsonMap.get(strList.get(0));
            }
        }
        }catch (Exception ex){

        }
        return null;

    }


    public LinkedHashMap updateJsonValues(LinkedHashMap jsonMap, String jsnPth, Object value){
        try{
            List<String> strList = new LinkedList<String>(Arrays.asList(jsnPth.split("\\.")));
            if(jsonMap.size() >0 ){
                if (strList.size() > 1){
                    String tmpKey= strList.get(0);
                    if (jsonMap.get(tmpKey) instanceof LinkedHashMap){
                        strList.remove(0);
                        updateJsonValues((LinkedHashMap) jsonMap.get(tmpKey),String.join(".",strList), value);
                    } else {
                         jsonMap.put(strList.get(0),value);
                        return jsonMap;
                    }

                } else {
                    jsonMap.put(strList.get(0),value);
                    return jsonMap;
                }
            }
        }catch (Exception ex){
        }

        return jsonMap;
    }
*/

    public JSONObject updateOrRemoveJsonProperty(Object js1, String keys, Object valueNew, ConfigData.JsonBuildType payloadEnum, String targetKey){
       try {
           List<String> keyMain = new LinkedList<String>(Arrays.asList(keys.split("\\.")));

           for (int i = 0; i < keyMain.size(); i++) {
               if(js1 instanceof JSONObject || js1 instanceof JSONArray){
                   if(keyMain.size() >1 && i+1 < keyMain.size()) {
                       String tmpKey= "";
                       String stringArray ="";
                       try {
                           tmpKey = keyMain.get(i);
                           keyMain.remove(i);
                           stringArray = StringUtils.join(keyMain, ".");
                           keyMain.clear();
                           updateOrRemoveJsonProperty(((JSONObject) js1).get(tmpKey), stringArray, valueNew, payloadEnum, targetKey);
                       }catch (JSONException js){
                           if(!tmpKey.isEmpty() && tmpKey.matches(KeyMapper.KEYLSTREGX)){
                               String[] tmp = tmpKey.replaceFirst(KeyMapper.KEYLSTREGX, "$1, $2").split(",");
                               try {
                                   updateOrRemoveJsonProperty((JSONArray)(((JSONArray)((JSONObject) js1).get(tmp[0])).get(Integer.parseInt(tmp[1].trim()))), stringArray, valueNew, payloadEnum, targetKey);
                               }catch (ClassCastException ex){
                                   updateOrRemoveJsonProperty((JSONObject)(((JSONArray)((JSONObject) js1).get(tmp[0])).get(Integer.parseInt(tmp[1].trim()))), stringArray, valueNew, payloadEnum, targetKey);
                               }
                           }
                       }
                   } else {
                       if((keyMain.get(i)).length() > 2 && keyMain.get(i).matches(KeyMapper.KEYLSTREGX)){
                           String[] tmp = keyMain.get(i).replaceFirst(KeyMapper.KEYLSTREGX, "$1, $2").split(",");
                           if(targetKey != "" && tmp[0].trim().equals(targetKey) && !payloadEnum.equals(ConfigData.JsonBuildType.REMOVE)) {
                               ((JSONObject) js1).put(tmp[0], valueNew);
                           } else if (targetKey != "" && tmp[0].trim().equals(targetKey) && payloadEnum.equals(ConfigData.JsonBuildType.REMOVE)){
                               ((JSONObject) js1).remove(tmp[0]);
                           }

                       }
                       if(targetKey != "" && keyMain.get(i).equals(targetKey) && !payloadEnum.equals(ConfigData.JsonBuildType.REMOVE)) {
                           ((JSONObject) js1).put(keyMain.get(i), valueNew);
                       } else if (targetKey != "" && keyMain.get(i).equals(targetKey) && payloadEnum.equals(ConfigData.JsonBuildType.REMOVE)){
                           ((JSONObject) js1).remove(keyMain.get(i));
                       }
                   }
               }
           }
       }catch (JSONException  ex){

       }

        return (JSONObject) js1;
    }

    public Object attrValueGenerate (String type, int val){
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
