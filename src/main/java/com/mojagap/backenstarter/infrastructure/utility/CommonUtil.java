package com.mojagap.backenstarter.infrastructure.utility;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import org.springframework.beans.BeanUtils;

public class CommonUtil {

    public static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    public static final PhoneNumberUtil PHONE_NUMBER_UTIL = PhoneNumberUtil.getInstance();

    public static String getEnvProperty(String property, String defaultValue) {
        String sysProperty = System.getProperty(property);
        if (sysProperty == null) {
            sysProperty = System.getenv(property);
        }
        if (sysProperty == null) {
            sysProperty = defaultValue;
        }
        return sysProperty;
    }

    public static String getRootProjectDirectory() {
        return System.getProperty("user.dir");
    }

    public static <R> R copyProperties(Object source, R target) {
        BeanUtils.copyProperties(source, target);
        return target;
    }

}
