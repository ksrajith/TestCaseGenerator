package com.api.base;

import com.api.core.KeyMapper;
import com.api.pojo.ConfigData;
import com.api.pojo.ConfigJson;
import com.api.pojo.PreCondition;
import com.api.pojo.RestInvoke;
import io.restassured.response.Response;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class Runner {

//    public static void main(String[] args) {
//        // Read property file and load data to object
//        ConfigRead configRead = new ConfigRead();
//       ConfigData configData = configRead.readProperties();
//       CommonUtils commonUtils = new CommonUtils();
//        List<TreeMap<String, JSONObject>> mapList= new LinkedList<>();
//
//        for (ConfigData.JsonBuildType jsEnum: ConfigData.JsonBuildType.values()) {
//            mapList.add(commonUtils.selectJsonBuildType(jsEnum, configData));
//        }
//        System.out.println(mapList.size());
//        GenerateResults generateResults = new GenerateResults();
//        generateResults.writeToCSVFile(mapList, configData);
//    }

    public static void main(String[] args) throws JSONException {
        JsonReader jsonReader = new JsonReader();
        // configuration JSON file location
        String src = "src/main/java/com/api/con/configs.json";
        // Assign configuration JSON to ConfigJson Class
        ConfigJson obj= (ConfigJson) jsonReader.fileReader(src, ConfigJson.class.getName());
        PreConditionRest preConditionRest = new PreConditionRest();
        // invoke pre condition and assign data
        RestInvoke restInvoke = preConditionRest.validatePreCondition(obj);
        MainJsonHandler mainJsonHandler = new MainJsonHandler();
        // Invoke main JSON data
        mainJsonHandler.mainJsonConstruct(obj, restInvoke);
    }
}
