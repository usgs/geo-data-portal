package gov.usgs.gdp.helper;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;


public class CookieHelper {
	// Used for calculating the amount of days a cookie should live for
	public static final int ONE_HOUR = 3600; // seconds
	
	public static Cookie getCookie(HttpServletRequest request, String name) {
		Cookie result = null;
		Cookie[] cookies = request.getCookies();
		
		for (Cookie cookie : cookies) {
			if (name.equals(cookie.getName())) result = cookie;
		}
		
		return result;
	}
}
