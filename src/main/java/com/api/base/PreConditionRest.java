package com.api.base;

import com.api.pojo.ConfigJson;
import com.api.pojo.Headers;
import com.api.pojo.RestInvoke;
import io.restassured.http.Header;
import io.restassured.response.Response;
import org.json.JSONObject;
import java.util.Map;
import java.util.TreeMap;

public class PreConditionRest {
/*
Invoke pre condition and get required details
 */
    public RestInvoke validatePreCondition(ConfigJson obj){
        if(obj.isPreRequest()){
            RestInvoke restInvoke = new RestInvoke();
            if(obj.getPreSendJson() != null && obj.getPreSendJson().getJsonPre() != null){
                    restInvoke.setType(obj.getPreSendJson().getPreRequestType());
                    restInvoke.setRequestBody(obj.getPreSendJson().getJsonPre().toString());
                    restInvoke.setUrlInvoke(obj.getPreSendJson().getPreRequestUrl());
                    restInvoke.setContentType(obj.getPreSendJson().getPreContentType());
                    if (obj.getPreSendJson().getPreHeaders().length != 0){
                        Map<String, String> reqHeaderMap = new TreeMap<>();
                        for (Headers headerKeyVal: obj.getPreSendJson().getPreHeaders()) {
                            reqHeaderMap.put(headerKeyVal.getHeader(), headerKeyVal.getValue());
                        }
                        restInvoke.setRequestHeaderMap(reqHeaderMap);
                    }
                    RestCaller restCaller = new RestCaller();
                   Response response = restCaller.apiRequestSender(restInvoke);
                   restInvoke.setCode(response.statusCode());
                   restInvoke.setResponseBody(response.getBody().asString());
                   response.headers();
                Map<String, String> resHeaderMap = new TreeMap<>();
                for (Header header: response.headers()) {
                    resHeaderMap.put(header.getName(), header.getValue());
                }
                restInvoke.setResponseHeaderMap(resHeaderMap);
            }
            return restInvoke;
        }
        return null;
    }
}
