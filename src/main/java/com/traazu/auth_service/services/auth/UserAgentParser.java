package com.traazu.auth_service.services.auth;

import com.traazu.auth_service.domain.dtos.auth.ClientDeviceInfo;

public class UserAgentParser {

    public static ClientDeviceInfo parse(String userAgent) {
        if (userAgent == null || userAgent.isBlank()) {
            return new ClientDeviceInfo("Unknown", "Unknown", "Unknown", "");
        }

        String os = parseOS(userAgent);
        String browser = parseBrowser(userAgent);
        String deviceType = parseDeviceType(userAgent, os);

        return new ClientDeviceInfo(browser, os, deviceType, userAgent);
    }

    private static String parseOS(String ua) {
        if (ua.contains("Windows NT 10.0") || ua.contains("Windows NT 11.0")) return "Windows 10/11";
        if (ua.contains("Windows")) return "Windows";
        if (ua.contains("Android")) return "Android";
        if (ua.contains("iPhone") || ua.contains("iPad") || ua.contains("iPod")) return "iOS";
        if (ua.contains("Mac OS X") || ua.contains("Macintosh")) return "macOS";
        if (ua.contains("Linux")) return "Linux";
        return "Unknown OS";
    }

    private static String parseBrowser(String ua) {
        if (ua.contains("Edg/") || ua.contains("Edge/")) return "Microsoft Edge";
        if (ua.contains("OPR/") || ua.contains("Opera")) return "Opera";
        if (ua.contains("Chrome/")) return "Google Chrome";
        if (ua.contains("Firefox/")) return "Mozilla Firefox";
        if (ua.contains("Safari/") && !ua.contains("Chrome/")) return "Safari";
        return "Unknown Browser";
    }

    private static String parseDeviceType(String ua, String os) {
        if (ua.contains("iPad") || (ua.contains("Android") && !ua.contains("Mobile"))) {
            return "Tablet";
        }
        if (ua.contains("Mobile") || os.equals("Android") || os.equals("iOS")) {
            return "Mobile";
        }
        return "Desktop";
    }
    
}
