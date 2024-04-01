package settleup.backend.global.Util;

import jakarta.servlet.http.HttpServletRequest;

public class HeaderUtil {
    public static String getAccessToken(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
}
