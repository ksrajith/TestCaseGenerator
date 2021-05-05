package com.api.base;

import com.api.pojo.ConfigData;
import com.api.pojo.RestInvoke;
import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.opencsv.CSVWriter;
import io.restassured.response.Response;
import org.json.JSONObject;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
public class GenerateResults {
    File file = new File("src/main/resources/output.csv");

    public void writeToCSVFile(List<TreeMap<String, JSONObject>> maps, ConfigData configData){
        try {
            // create FileWriter object with file as parameter
            FileWriter outputfile = null;
            outputfile = new FileWriter(file);

            // create CSVWriter object filewriter object as parameter
            CSVWriter writer = new CSVWriter(outputfile);

            // adding header to csv
            String[] header = {"TestCase", "RequestBody", "ResponseBody", "StatusCode"};
            writer.writeNext(header);
            RequestSender requestSender = new RequestSender();
            for (TreeMap<String, JSONObject> treemap: maps) {
                for (Map.Entry<String,JSONObject> entry : treemap.entrySet()) {
                    JSONObject jsonObject = entry.getValue();
                    String key = entry.getKey();
                    Response response = requestSender.apiRequestSend("POST", configData.getPostConfigURL(), jsonObject);
                    String[] dataSet = new String[]{key, jsonObject.toString(), response.body().asString(), String.valueOf(response.getStatusCode())};
                    // add data to csv
                    writer.writeNext(dataSet);
                }
            }
            // closing writer connection
            writer.close();
        } catch (IOException ioException) {
            ioException.printStackTrace();
        }

    }

    public void writeResultsToCSVFile(TreeMap<String, RestInvoke> maps){
        try {
            // create FileWriter object with file as parameter
            FileWriter outputfile = null;
            outputfile = new FileWriter(file);

            // create CSVWriter object filewriter object as parameter
            CSVWriter writer = new CSVWriter(outputfile);

            // adding header to csv
            String[] header = {"TestCase", "RequestBody", "ResponseBody", "StatusCode", "ExpectedBody", "ExpectedCode", "PassOrFail", "IsStrictMatch"};
            writer.writeNext(header);
            RequestSender requestSender = new RequestSender();
                for (Map.Entry<String,RestInvoke> entry : maps.entrySet()) {
                    String key = entry.getKey();
                    String[] dataSet = new String[]{key, entry.getValue().getRequestBody(), entry.getValue().getResponseBody(), String.valueOf(entry.getValue().getCode()), entry.getValue().getExpectedResponse(), String.valueOf(entry.getValue().getExpectedCode()), String.valueOf(entry.getValue().isCodeCompare() && entry.getValue().isJsonCompare()), String.valueOf(entry.getValue().isStrictCompare())};
                    // add data to csv
                    writer.writeNext(dataSet);
                }

            // closing writer connection
            writer.close();
        } catch (IOException ioException) {
            ioException.printStackTrace();
        }

    }

}
