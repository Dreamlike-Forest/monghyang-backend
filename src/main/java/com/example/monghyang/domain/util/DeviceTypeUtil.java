package com.example.monghyang.domain.util;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Component;

@Component
public class DeviceTypeUtil {
    public enum DeviceType{
        MOBILE, TABLET, DESKTOP, UNKNOWN
    }

    public static DeviceType getDeviceType(HttpServletRequest request){
        String userAgent = request.getHeader("User-Agent");
        if(userAgent == null) {
            return DeviceType.UNKNOWN;
        }

        if(userAgent.contains("mobi") || userAgent.contains("iphone") || (userAgent.contains("android") && !userAgent.contains("tablet"))) {
            return DeviceType.MOBILE;
        }
        if(userAgent.contains("tablet") || userAgent.contains("ipad")) {
            return DeviceType.TABLET;
        }
        if(userAgent.contains("windows") || userAgent.contains("macintosh") || (userAgent.contains("x11") || userAgent.contains("linux"))) {
            return DeviceType.DESKTOP;
        }
        return DeviceType.UNKNOWN;
    }
}
