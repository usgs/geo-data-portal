/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package gov.usgs.cida.gdp.utilities;

import gov.usgs.cida.gdp.utilities.bean.AvailableFiles;
import gov.usgs.cida.gdp.utilities.bean.ShapeFileSet;
import java.io.File;
import java.util.List;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;

/**
 *
 * @author isuftin
 */
public class CookieHelper {
	// Used for calculating the amount of days a cookie should live for
	public static final int ONE_HOUR = 3600; // seconds

	public static Cookie getCookie(HttpServletRequest request, String name) {
		Cookie result = null;
		Cookie[] cookies = request.getCookies();

		for (Cookie cookie : cookies) if (name.equals(cookie.getName())) result = cookie;
		return result;
	}

	/**
	 * Return path to the user's files stored on the server
	 * @param request
	 * @return user path, ending with a path separator (e.g. "/usr/bin/")
	 */
	public static String getUserPath(HttpServletRequest request) {
		// TODO: don't hardcode for second cookie
		String userDir = request.getCookies()[1].getValue();
		String appUserPath = System.getProperty("applicationUserSpaceDir");

		return appUserPath + userDir + FileHelper.getSeparator();
	}

	/**
	 * Return path to shapefile.
	 * @param request
	 * @param shapefileName Name of shapefile set, without any extension, i.e. no .shp
	 * @return full path to shapefile
	 */
	public static String getShapefilePath(String userDirectory, String shapefileName) {
		// Set up the shapefile
		String appTempDir = System.getProperty("applicationTempDir");
		String userDir = System.getProperty("applicationUserSpaceDir") + userDirectory;

		// Make sure userDir exists. It won't be checked in getAvailableFilesBean.
		File userDirCheck = new File(userDir);
		if (!userDirCheck.exists()) userDir = null;

		AvailableFiles afb = AvailableFiles.getAvailableFilesBean(appTempDir, userDir);
		List<ShapeFileSet> shapeBeanList = afb.getShapeSetList();
		File shapeFile = null;
		for (ShapeFileSet sfsb : shapeBeanList) {
			if (shapefileName.equals(sfsb.getName())) {
				shapeFile = sfsb.getShapeFile();
				return shapeFile.getAbsolutePath();
			}
		}

		return null;
	}
}
