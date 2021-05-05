package com.api.base;

import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.json.JSONObject;
import static io.restassured.RestAssured.given;

public class RequestSender {

    /**
     * Can use this method to send REST requests and can extend
     * the switch cases as per the needs
     * @param type http method tye
     * @param url url which user need to send data
     * @param rqstBody if the request has request body pass here
     * @return Response object which returns as the response
     */
    public Response apiRequestSend(String type, String url, JSONObject rqstBody){

        String tmpRest = rqstBody.toString();
        Response response = null;
        switch (type){
            case "GET":
                response= given()
                        .header("Accept", ContentType.JSON.getAcceptHeader())
                        .get(url)
                        .then().using().extract().response();
                break;
            case "POST":
                response= given().contentType(ContentType.JSON).body(tmpRest)
                        .header("Accept", ContentType.JSON.getAcceptHeader())
                        .post(url)
                        .then().using().extract().response();
                break;
            case "PUT":
                response= given().contentType(ContentType.JSON).body(tmpRest)
                        .header("Accept", ContentType.JSON.getAcceptHeader())
                        .put(url)
                        .then().using().extract().response();
                break;
        }
        return response;
    }
}
