/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package gov.usgs.service;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author jwalker
 */

public class Endpoint {

	private static final Logger log = LoggerFactory.getLogger(Endpoint.class);;
	private static final Pattern wmsPattern;
	private static final Pattern wcsPattern;
	private static final Pattern wfsPattern;
	private static final Pattern wpsPattern;
	private static final Pattern cswPattern;
    private static final Pattern sosPattern;

	static {
		Pattern pat1 = null;
		Pattern pat2 = null;
		Pattern pat3 = null;
		Pattern pat4 = null;
		Pattern pat5 = null;
        Pattern pat6 = null;
		try {
			pat1 = Pattern.compile(".*(?i:service=(?:wms)|wms).*");
			pat2 = Pattern.compile(".*(?i:service=(?:wcs)|wcs).*");
			pat3 = Pattern.compile(".*(?i:service=(?:wfs)|wfs).*");
			pat4 = Pattern.compile(".*(?i:service=(?:wps)|wps|webprocessingservice).*");
			pat5 = Pattern.compile(".*(?i:service=(?:csw)|csw).*");
            pat6 = Pattern.compile(".*(?i:service=(?:sos)|sos).*");
		}
		catch (Exception ex) {
			log.error("Unable to compile regular expression " + ex.getMessage());
		}
		finally {
			wmsPattern = pat1;
			wcsPattern = pat2;
			wfsPattern = pat3;
			wpsPattern = pat4;
			cswPattern = pat5;
            sosPattern = pat6;
		}
	}

	public enum EndpointType {
		WCS,
		WMS,
		WFS,
		WPS,
		CSW,
        SOS,
		UNKNOWN;
	}

	private EndpointType type;
	private String url;
	private String comparisonString;

	public Endpoint(String url) {
		this.url = url;
		createComparisonString();
		{
			Matcher matcher = wmsPattern.matcher(url);
			if (matcher.matches()) {
				type = EndpointType.WMS;
				return;
			}
			matcher = wcsPattern.matcher(url);
			if (matcher.matches()) {
				type = EndpointType.WCS;
				return;
			}
			matcher = wfsPattern.matcher(url);
			if (matcher.matches()) {
				type = EndpointType.WFS;
				return;
			}
			matcher = wpsPattern.matcher(url);
			if (matcher.matches()) {
				type = EndpointType.WPS;
				return;
			}
			matcher = cswPattern.matcher(url);
			if (matcher.matches()) {
				type = EndpointType.CSW;
				return;
			}
            matcher = sosPattern.matcher(url);
			if (matcher.matches()) {
				type = EndpointType.SOS;
				return;
			}
			type = EndpointType.UNKNOWN;
		}
	}

	public String getURL() {
		return url;
	}

	public EndpointType getType() {
		return type;
	}

	/**
	 *
	 * Override me if you want to calculate string differently
	 *
	 * @param url URL that contains a query string
	 * @return
	 */
	private void createComparisonString() {
		// TODO Fix regex to make it more readable
        // Description in words: capture everything up to last slash, not query string
		Pattern urlPattern = Pattern.compile("([^\\?]*/)(?:[^/\\?]*(?:\\?.*)?){1}");
		Matcher matcher = urlPattern.matcher(url);
		if (matcher.matches()) {
			comparisonString = matcher.group(1);
		}
		else {
			throw new UnsupportedOperationException("Cannot create endpoint with invalid URL");
		}
	}

	public String getComparisonString() {
		return comparisonString;
	}

	public URL generateGetCapabilitiesURL() throws MalformedURLException {
		StringBuilder buildstr = new StringBuilder(url.replaceAll("\\?[^?]*$", ""));
		buildstr.append("?request=GetCapabilities&service=");
		buildstr.append(type);
        if (type == EndpointType.SOS) {
            // current ncSOS implementation requires all or none
            buildstr.append("&version=1.0.0");
        }
		return new URL(buildstr.toString());
	}

	/**
	 * We are talking about endpoints, so the query string does not factor into
	 * the identity of the endpoint, or two urls that only differ in their query
	 * string are the same endpoint
	 *
	 * @param other Endpoint to compare to
	 * @return
	 */
	public boolean equals(Endpoint other) {
        if (comparisonString != null) {
            return comparisonString.equals(other.getComparisonString());
        }
        else {
            return false;
        }
	}

	@Override
	public int hashCode() {
        if (comparisonString != null) {
            return comparisonString.hashCode();
        }   
        else {
            return 0;
        }
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof Endpoint) {
			return equals((Endpoint)obj);
		}
		return false;
	}
}