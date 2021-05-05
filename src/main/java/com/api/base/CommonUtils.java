package com.api.base;

import com.api.core.KeyMapper;
import com.api.core.TestMapper;
import com.api.pojo.ConfigData;
import com.github.wnameless.json.flattener.JsonFlattener;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.skyscreamer.jsonassert.JSONAssert;
import org.skyscreamer.jsonassert.JSONCompareMode;

import java.util.*;

public class CommonUtils {

    public TreeMap<String, JSONObject> constructJson(String testCase, String[] userParamList, ConfigData configData, ConfigData.JsonBuildType payloadEnum){
        TreeMap<String, JSONObject> tmpMap = new TreeMap<>();
        try {
            boolean isJsonCompre=false;
            if(userParamList.length != 0) {
                if(userParamList[0].equals("_ALL")){
                    Map<String, Object> flattenJson = JsonFlattener.flattenAsMap(configData.getJsnStr());
                    userParamList = (flattenJson.keySet()).toArray(new String[flattenJson.size()]);
                    isJsonCompre=true;
                }
                for (String keyNvalSet: userParamList) {
                    String tmpJson = configData.getJsnStr();
                    JSONObject jsObj = new JSONObject(tmpJson);
                    TreeMap<String, JSONObject> lst5 = new TreeMap<>();
                    lst5.put(testCase,jsObj);
                    String[] tmpSet = keyNvalSet.split(KeyMapper.VALUESSETSPLITER);
                    for (String keyNval: tmpSet) {
                        String[] tmp = keyNval.split(KeyMapper.VALSPLIT);
                        String[] tmpPass = new String[] {tmp[0].trim()};
                        Object value=null;
                        if(tmp.length > 1){
                            value = valueGenerate(tmp[1].trim(), tmp[2].trim());
                        } else if (payloadEnum.equals(ConfigData.JsonBuildType.NULLCHECK)){
                            value = valueGenerate("NULL", null);
                        } else if (payloadEnum.equals(ConfigData.JsonBuildType.EMPTYCHECK)){
                            value = valueGenerate("EMPTY", null);
                        }
                        String fieldAppend = lst5.firstKey()+ KeyMapper.TESTKEYSAPPEND +tmp[0];
                        lst5= stringToJson1(fieldAppend, lst5.firstEntry().getValue().toString(), tmpPass, value, payloadEnum);
                    }
                    tmpMap.put(lst5.firstKey(), lst5.firstEntry().getValue());
                }

                if(isJsonCompre){
                    tmpMap = comparePayloads(tmpMap);
                }
            }

        }catch (Exception ex){
            ex.printStackTrace();
        }

        return tmpMap;
    }


    public TreeMap<String, JSONObject> selectJsonBuildType(ConfigData.JsonBuildType payloadEnum , ConfigData configData){
        switch (payloadEnum){
            case REMOVE:
                return constructJson(KeyMapper.REMOVEFIELDS, configData.getRemoveField(), configData, payloadEnum);
            case LENGTHCHECK:
                return constructJson(KeyMapper.LENGTHCHECKFIELDS, configData.getLengthCheck(), configData, payloadEnum);
            case NULLCHECK:
                return constructJson(KeyMapper.NULLVAL, configData.getNullChk(), configData, payloadEnum);
            case EMPTYCHECK:
                return constructJson(KeyMapper.EMPTYVAL, configData.getEmptyChk(), configData, payloadEnum);
            case VALUEUPDATE:
                return constructJson(KeyMapper.VALUEUPDATE, configData.getValueUpdate(), configData, payloadEnum);
        }
        return null;
    }

    public Object valueGenerate (String type, String val){
        Object value = null;
        switch (type) {
            case "NULL": value = JSONObject.NULL;
            break;
            case "EMPTY": value = "";
            break;
            case "String": value = val.trim();
            break;
            case "int": value = Integer.parseInt(val.trim());
            break;
            case "UUID": value = UUID.randomUUID();
            break;
            case "numOnly": value = RandomStringUtils.randomNumeric(Integer.parseInt(val.trim()));
            break;
            case "charOnly": value = RandomStringUtils.randomAlphabetic(Integer.parseInt(val.trim()));
            break;
            case "numNChars": value = RandomStringUtils.randomAlphanumeric(Integer.parseInt(val.trim()));
            break;
        }

        return value;
    }

    public TreeMap<String, JSONObject> stringToJson1(String testCaseKey ,String stringToJson, String[] fields, Object value, ConfigData.JsonBuildType payloadEnum){
        try {
            TreeMap<String, JSONObject> jsnMap = new TreeMap<>();
            List<JSONObject> lst = new ArrayList<>();
            for (String field: fields) {
                JSONObject json = new JSONObject(stringToJson);
                String [] str = field.split("\\.");
                String targetKey = str[str.length-1];
                if(targetKey.matches(KeyMapper.KEYLSTREGX)){
                    String[] matcher = targetKey.replaceFirst(KeyMapper.KEYLSTREGX, "$1, $2").split(",");
                    targetKey = matcher[0].trim();
                }
                JSONObject js = setOrRemoveProperty1(json, field, value, payloadEnum, targetKey);
                jsnMap.put(testCaseKey, js);
                lst.add(js);
            }
            return jsnMap;
        }catch (Exception ex){
            ex.printStackTrace();
            return null;
        }

    }

    public JSONObject setOrRemoveProperty1(Object js1, String keys, Object valueNew, ConfigData.JsonBuildType payloadEnum, String targetKey) throws JSONException {
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
                        setOrRemoveProperty1(((JSONObject) js1).get(tmpKey), stringArray, valueNew, payloadEnum, targetKey);
                    }catch (JSONException js){
                        if(!tmpKey.isEmpty() && tmpKey.matches(KeyMapper.KEYLSTREGX)){
                            String[] tmp = tmpKey.replaceFirst(KeyMapper.KEYLSTREGX, "$1, $2").split(",");
                            try {
                                setOrRemoveProperty1((JSONArray)(((JSONArray)((JSONObject) js1).get(tmp[0])).get(Integer.parseInt(tmp[1].trim()))), stringArray, valueNew, payloadEnum, targetKey);
                            }catch (ClassCastException ex){
                                setOrRemoveProperty1((JSONObject)(((JSONArray)((JSONObject) js1).get(tmp[0])).get(Integer.parseInt(tmp[1].trim()))), stringArray, valueNew, payloadEnum, targetKey);
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

        return (JSONObject) js1;
    }

    public TreeMap<String, JSONObject> comparePayloads(TreeMap<String, JSONObject> jsMap) {
        String[] keys = jsMap.keySet().toArray(new String[0]);
        for (int i = 0; i < keys.length - 1; i++) {
            try {
                JSONAssert.assertEquals(jsMap.get(keys[i]), jsMap.get(keys[i + 1]), JSONCompareMode.STRICT);
                jsMap.remove(keys[i + 1]);
                comparePayloads(jsMap);
            } catch (AssertionError | JSONException ae) {
                // System.out.println("Not a duplicate");
            } catch (NullPointerException ex) {
            }
        }
        return jsMap;
    }

//        for (Map.Entry<String, JSONObject> entry: jsMap.entrySet()) {
//            int index = Arrays.asList(keys).indexOf(entry.getKey());
//            JSONAssert.assertEquals(entry.getValue(), js., JSONCompareMode.STRICT);
//        }
//        for (int i = 0; i < jsLst.size()-1; i++) {
//            try{
//                JSONAssert.assertEquals(jsLst.get(i), jsLst.get(i+1), JSONCompareMode.STRICT);
//                System.out.println("found duplicate :"+jsLst.get(i) + " and :"+jsLst.get(i+1));
//                jsLst.remove(jsLst.get(i+1));
//                comparePayloads(jsLst);
//            } catch (AssertionError | JSONException ae){
//                System.out.println("Not a duplicate");
//            }
//        }
//        return jsLst;
}
