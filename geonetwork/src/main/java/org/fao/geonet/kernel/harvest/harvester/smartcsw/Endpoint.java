/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.fao.geonet.kernel.harvest.harvester.smartcsw;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author jwalker
 * @version 0.1
 * This class represents an "OGC" (also includes OPeNDAP) endpoint.
 * Any string that appears to be an endpoint URL (in a metadata field associated with
 * service URLs) will be pushed in and typed according to the regular expressions
 * below.
 *
 * In all likelihood we are losing information by only keeping the Endpoint (example:
 * WMS requires a layer to be specified when calling GetMap, and the endpoint isn't
 * enough to get the map relating to a specific record).  So this may need to be
 * adjusted to allow other information to feed into the Object.  One could even see
 * pushing a higher up XML element into this constructor and having it recognize the
 * many patterns that exist for these endpoints.
 */
public class Endpoint {

	private static final Pattern dodsPattern = Pattern.compile("^dods\\://.*|.*dodsC.*");
	private static final Pattern wmsPattern = Pattern.compile(".*(?:(?:service|SERVICE)=(?:WMS|wms)|WMSServer).*");
	private static final Pattern wcsPattern = Pattern.compile(".*(?:(?:service|SERVICE)=(?:WCS|wcs)|WCSServer).*");
	private static final Pattern wfsPattern = Pattern.compile(".*(?:(?:service|SERVICE)=(?:WFS|wfs)|WFSServer).*");

	public enum EndpointType {
		DODS,
		WCS,
		WMS,
		WFS,
		UNKNOWN;
	}

	private EndpointType type;
	private String endpoint;

	public Endpoint(String endpoint) {
		this.endpoint = endpoint;
		{
			Matcher matcher = dodsPattern.matcher(endpoint);
			if (matcher.matches()) {
				type = EndpointType.DODS;
				return;
			}
			matcher = wmsPattern.matcher(endpoint);
			if (matcher.matches()) {
				type = EndpointType.WMS;
				return;
			}
			matcher = wcsPattern.matcher(endpoint);
			if (matcher.matches()) {
				type = EndpointType.WCS;
				return;
			}
			matcher = wfsPattern.matcher(endpoint);
			if (matcher.matches()) {
				type = EndpointType.WFS;
				return;
			}
			type = EndpointType.UNKNOWN;
		}
	}

	public String getEndpoint() {
		return endpoint;
	}

	public EndpointType getType() {
		return type;
	}
}
