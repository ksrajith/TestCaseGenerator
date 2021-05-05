package com.api.base;

import com.api.core.KeyMapper;
import com.api.pojo.ConfigData;

import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Properties;

public class ConfigRead {

    public ConfigData readProperties() {
        try (
            InputStream input = new FileInputStream("src/main/java/com/api/con/test.properties")) {
            Properties prop = new Properties();

            // load a properties file
            prop.load(input);

            // get the property value and assign them
            ConfigData configData = new ConfigData();
            configData.setJsnStr(prop.getProperty("myObj"));
            configData.setPostConfigURL(prop.getProperty("postURL").trim());
            configData.setNullChk(prop.getProperty("nullCheck").split(KeyMapper.ARRYSPLIT));
            configData.setEmptyChk(prop.getProperty("emptyCheck").split(KeyMapper.ARRYSPLIT));
            configData.setRemoveField(prop.getProperty("removeField").split(KeyMapper.ARRYSPLIT));
            configData.setValueUpdate(prop.getProperty("valueUpdate").split(KeyMapper.ARRYSPLIT));
            configData.setLengthCheck(prop.getProperty("lengthCheck").split(KeyMapper.ARRYSPLIT));
            return configData;
        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
        }
    }
}
