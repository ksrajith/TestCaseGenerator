package com.api.base;

import com.api.core.KeyMapper;
import com.api.pojo.*;
import com.github.wnameless.json.flattener.JsonFlattener;
import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.*;

public class JsonUtils {

    /*
   Update Main JSON with pre-request data
    */
    public RestInvoke updateJsonWithPreResponse(ConfigJson obj, RestInvoke restInvoke) throws JSONException {
       // TreeMap<String, RestInvoke> restInvokeNConfigList = new TreeMap<>();
        RestInvoke mainInvoke = new RestInvoke();
        Object json = new JSONObject(obj.getJsonMain().toString());
        mainInvoke.setRequestHeaderMap(setMainHeader(obj.getMainHeaders()));
        for (PreResponseAtr pre: obj.getFromPreResponse()) {
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
                Object tmpJsonVal = getJsonValue(restInvoke.getResponseBody(),pre.getPathOrName());
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
//        restInvokeNConfigList.put(KeyMapper.PREINVOKE,restInvoke);
//        restInvokeNConfigList.put(KeyMapper.MAININVOKE,mainInvoke);
        RequestManager.mainJsonConf = obj;
        return mainInvoke;
    }

    public Map<String, String> setMainHeader(Headers[]  headers){
        Map<String, String> headerMap = new LinkedHashMap<>();
        for (Headers header:headers ) {
            headerMap.put(header.getHeader(), header.getValue());
        }
        return headerMap;
    }

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
                            System.out.println("aaaaaa :"+js1.toString()+ "keyMain.get(i) :"+keyMain.get(i));
                            ((JSONObject) js1).put(keyMain.get(i), valueNew);
                        } else if (targetKey != "" && keyMain.get(i).equals(targetKey) && payloadEnum.equals(ConfigData.JsonBuildType.REMOVE)){
                            ((JSONObject) js1).remove(keyMain.get(i));
                        }
                    }
                }
            }
        }catch (JSONException  ex){
            ex.printStackTrace();
        }

        return (JSONObject) js1;
    }

    public Object getJsonValue(Object json, String jsnPth) {
        try{
            Map<String, Object> flattenJson = JsonFlattener.flattenAsMap(json.toString());
            return flattenJson.get(jsnPth);
        }catch (Exception ex){
            return null;
        }
    }

    public Object onlyUsePayload(Object jsonPayload, String[] fields){
        ConfigJson cnfJson = RequestManager.mainJsonConf;
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

}
