package com.api.core;

import com.github.wnameless.json.flattener.JsonFlattener;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.skyscreamer.jsonassert.JSONAssert;
import org.skyscreamer.jsonassert.JSONCompareMode;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;


public class TestMapper {

    String regxNum = "^(.*)\\[(\\d+)\\]"; ///\[(-?\d+)\]$
    String[] nullChk;
    String jsnStr="";
    String[] emptyChk;
    String targetKey ="";
    String[] removeField;
    String[] valuesCheck;
    String[] lengthcheck;
    public static void main(String[] args) {
        try (
            InputStream input = new FileInputStream("src/main/java/com/api/con/test.properties")) {
            Properties prop = new Properties();

            // load a properties file
            prop.load(input);

            // get the property value and print it out
            System.out.println(prop.getProperty("myObj"));

            System.out.println(prop.getProperty("optional"));
            TestMapper tm = new TestMapper();
            // tm.stringToJson(prop.getProperty("myObj"), values);
            tm.readProperties();
          //  tm.test(prop.getProperty("myObj"));
        } catch (IOException ex) {
            ex.printStackTrace();
        } catch (Exception ex){
            ex.printStackTrace();
        }

    }

    public void readProperties() throws Exception{
        InputStream input = new FileInputStream("src/main/java/com/api/con/test.properties");
            Properties prop = new Properties();
            // load a properties file
            prop.load(input);
            // get the property value and print it out
            System.out.println(prop.getProperty("myObj"));
            nullChk = prop.getProperty("nullcheck").split(KeyMapper.ARRYSPLIT);
            jsnStr = prop.getProperty("myObj");
            emptyChk = prop.getProperty("emptycheck").split(KeyMapper.ARRYSPLIT);
            removeField = prop.get("removeField").toString().split(KeyMapper.ARRYSPLIT);
            valuesCheck = prop.get("valuesCheck").toString().split(KeyMapper.ARRYSPLIT);
            lengthcheck = prop.get("lengthcheck").toString().split(KeyMapper.ARRYSPLIT);
            //flattenJson.keySet()
            callValues();
    }

    public void callValues(){
        if (emptyChk != null && emptyChk.length != 0) {
            List<JSONObject> lst2= assignValues(emptyChk, "", false);
            System.out.println(lst2);
            lst2 = compareJsons(lst2);
            System.out.println(lst2.size());
        }

        try {
            List<JSONObject> lst4 = new LinkedList<>();
            if(removeField != null && removeField.length != 0) {
                for (String keyNvalSet: removeField) {
                    String tmpJson = jsnStr;
                    JSONObject jsObj = new JSONObject(tmpJson);
                    List<JSONObject> lst5 = new LinkedList<>();
                    lst5.add(jsObj);
                    String[] tmpSet = keyNvalSet.split("#&&#");
                    for (String keyNval: tmpSet) {
                        String[] tmpPass = new String[] {keyNval.trim()};
                        lst5= replaceValSet(lst5.get(0).toString(),tmpPass, "REMOVE", true);
                    }
                    lst4.add(lst5.get(0));
                    lst5.remove(0);
                }
            }
            System.out.println(lst4.size());
        }catch (Exception ex){
            ex.printStackTrace();
        }

        List<JSONObject> lst6 = new LinkedList<>();
        try {
            if(lengthcheck!= null && lengthcheck.length != 0){
                for (int i = 0; i < lengthcheck.length; i++) {
                    String[] tmpSet = lengthcheck[i].split("#&&#");
                    String tmpJson = jsnStr;
                    JSONObject jsObj = new JSONObject(tmpJson);
                    List<JSONObject> lst5 = new LinkedList<>();
                    lst5.add(jsObj);
                    for (int j = 0; j < tmpSet.length; j++) {
                        String[] lenghSet = tmpSet[j].split("::");
                        int valLength = Integer.parseInt(lenghSet[2].trim());
                        Object value = null;
                        if(lenghSet[1].trim().equals("numOnly")){
                            value = RandomStringUtils.randomNumeric(valLength);
                        } else if(lenghSet[1].trim().equals("charOnly")){
                            value = RandomStringUtils.randomAlphabetic(valLength);
                        } else if(lenghSet[1].trim().equals("numNChars")){
                            value = RandomStringUtils.randomAlphanumeric(valLength);
                        }
                        String[] tmpPass = new String[] {lenghSet[0].trim()};
                        lst5 = replaceValSet(lst5.get(0).toString(), tmpPass, value, false);
                    }
                    lst6.add(lst5.get(0));
                    lst5.remove(0);
                }
            }
        }catch (Exception ex){
            ex.printStackTrace();
        }

        try {
            List<JSONObject> lst4 = new LinkedList<>();
            if(valuesCheck != null && valuesCheck.length != 0) {
                for (String keyNvalSet: valuesCheck) {
                    String tmpJson = jsnStr;
                    JSONObject jsObj = new JSONObject(tmpJson);
                    List<JSONObject> lst5 = new LinkedList<>();
                    lst5.add(jsObj);
                    String[] tmpSet = keyNvalSet.split("#&&#");
                    for (String keyNval: tmpSet) {
                        String[] tmp = keyNval.split("::");
                        String[] tmpPass = new String[] {tmp[0].trim()};
                        Object value = null;
                        if(tmp[1].trim().equals("NULL")){
                            value = JSONObject.NULL;
                        } else if (tmp[1].trim().equals("String")){
                            value =tmp[2].trim();
                        } else if (tmp[1].trim().equals("int")){
                            value = Integer.parseInt(tmp[2].trim());
                        } else if(tmp[1].trim().equals("UUID")){
                            value = UUID.randomUUID();
                        }
                        lst5= replaceValSet(lst5.get(0).toString(), tmpPass, value, false);
                    }
                    lst4.add(lst5.get(0));
                    lst5.remove(0);
                }
            }
            System.out.println(lst4.size());
        }catch (Exception ex){
            ex.printStackTrace();
        }

        if (nullChk != null && nullChk.length != 0) {
            List<JSONObject> lst1=  assignValues(nullChk, JSONObject.NULL, false);
            System.out.println(lst1);
            lst1 = compareJsons(lst1);
            System.out.println(lst1.size());
        }

    }

    public List<JSONObject> assignValues(String[] keys, Object value, boolean isRemove){
        Map<String, Object> flattenJson = JsonFlattener.flattenAsMap(jsnStr);
        List<JSONObject> lst;
        if (keys.length >0 && keys[0].equals("_ALL")) {
            lst = stringToJson(jsnStr, (flattenJson.keySet()).toArray(new String[flattenJson.size()]), value, isRemove);
        } else {
            lst = stringToJson(jsnStr, keys, value, isRemove);
        }
        return lst;
    }

    public List<JSONObject> replaceValSet(String jsnStr, String[] keys, Object value, boolean isRemove){
        //Map<String, Object> flattenJson = JsonFlattener.flattenAsMap(jsnStr);
        List<JSONObject> lst;
            lst = stringToJson(jsnStr, keys, value, isRemove);
        return lst;
    }

    public List<JSONObject> stringToJson(String jsonString, String[] fields, Object value, boolean isRemove){
        try {
            List<JSONObject> lst = new ArrayList<>();
            for (String field: fields) {
                JSONObject json = new JSONObject(jsonString);
                String [] str = field.split("\\.");
                targetKey = str[str.length-1];
                if(targetKey.matches(regxNum)){
                    String[] matcher = targetKey.replaceFirst(regxNum, "$1, $2").split(",");
                    targetKey = matcher[0].trim();
                }
               JSONObject js = setOrRemoveProperty(json, field, value, isRemove);
                lst.add(js);
            }
            System.out.println(lst.size());
            return lst;
        }catch (Exception ex){
            ex.printStackTrace();
            return null;
        }

    }

    public JSONObject nullCheck(){

        return null;
    }

    public JSONObject setOrRemoveProperty(Object js1, String keys, Object valueNew, boolean isRemove) throws JSONException {
         List <String> keyMain = new LinkedList<String>(Arrays.asList(keys.split("\\.")));

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
                        setOrRemoveProperty(((JSONObject) js1).get(tmpKey), stringArray, valueNew, isRemove);
                    }catch (JSONException js){
                        if(!tmpKey.isEmpty() && tmpKey.matches(regxNum)){
                            String[] tmp = tmpKey.replaceFirst(regxNum, "$1, $2").split(",");
                            try {
                                setOrRemoveProperty((JSONArray)(((JSONArray)((JSONObject) js1).get(tmp[0])).get(Integer.parseInt(tmp[1].trim()))), stringArray, valueNew, isRemove);
                            }catch (ClassCastException ex){
                                setOrRemoveProperty((JSONObject)(((JSONArray)((JSONObject) js1).get(tmp[0])).get(Integer.parseInt(tmp[1].trim()))), stringArray, valueNew, isRemove);
                            }
                        }
                    }
                } else {
                    if((keyMain.get(i)).length() > 2 && keyMain.get(i).matches(regxNum)){
                        String[] tmp = keyMain.get(i).replaceFirst(regxNum, "$1, $2").split(",");
                        if(targetKey != "" && tmp[0].trim().equals(targetKey) && !isRemove) {
                            ((JSONObject) js1).put(tmp[0], valueNew);
                        } else if (targetKey != "" && tmp[0].trim().equals(targetKey) && isRemove){
                            ((JSONObject) js1).remove(tmp[0]);
                        }

                    }
                    if(targetKey != "" && keyMain.get(i).equals(targetKey) && !isRemove) {
                        ((JSONObject) js1).put(keyMain.get(i), valueNew);
                    } else if (targetKey != "" && keyMain.get(i).equals(targetKey) && isRemove){
                        ((JSONObject) js1).remove(keyMain.get(i));
                    }
                }
            }
        }

        return (JSONObject) js1;
    }

    public List<JSONObject> compareJsons(List<JSONObject> jsLst) {
        for (int i = 0; i < jsLst.size()-1; i++) {
                try{
                    JSONAssert.assertEquals(jsLst.get(i), jsLst.get(i+1), JSONCompareMode.STRICT);
                    System.out.println("found duplicate :"+jsLst.get(i) + " and :"+jsLst.get(i+1));
                    jsLst.remove(jsLst.get(i+1));
                    compareJsons(jsLst);
                } catch (AssertionError | JSONException ae){
                    System.out.println("Not a duplicate");
                }
        }
        return jsLst;
    }
}
