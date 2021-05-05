package com.api.base;

import com.api.pojo.RestInvoke;
import io.restassured.internal.common.assertion.Assertion;
import io.restassured.response.Response;
import org.json.JSONException;
import org.json.JSONObject;
import org.skyscreamer.jsonassert.JSONAssert;
import org.skyscreamer.jsonassert.JSONCompareMode;

import java.util.Map;
import java.util.TreeMap;

public class ValidateMainRequest {

    public void iterateRequestMap(TreeMap<String, RestInvoke> map){

        for (Map.Entry<String, RestInvoke> entry: map.entrySet() ) {
        RestCaller restCaller = new RestCaller();
        Response response = restCaller.apiRequestSender(entry.getValue());
            try {
                entry.getValue().setCode(response.statusCode());
                entry.getValue().setResponseBody(response.getBody().asString());
                if(entry.getValue().getResponseBody() != null && entry.getValue().getResponseBody() != "") {
                    entry.getValue().setJsonCompare(compareJsons(response.getBody().asString(), entry.getValue().getExpectedResponse(), entry.getValue().isStrictCompare()));
                }
                if(entry.getValue().getExpectedCode() > 0){
                    entry.getValue().setCodeCompare(compreStatusCodes(response.getStatusCode(), entry.getValue().getExpectedCode()));
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        GenerateResults generateResults = new GenerateResults();
        generateResults.writeResultsToCSVFile(map);
    }


    private boolean compareJsons(String actJson, String exptJson, boolean isStrictCompare){
        try {
                if (isStrictCompare) {
                    JSONAssert.assertEquals(actJson, exptJson, JSONCompareMode.STRICT);
                    return true;
                } else {
                    JSONAssert.assertEquals(actJson, exptJson, JSONCompareMode.LENIENT);
                    return true;
                }
        } catch (AssertionError e) {
            return false;

        } catch (Exception ex){
            return false;
        }
    }

    private boolean compreStatusCodes(int act, int expt){
        return act == expt;
    }
}
