package gov.usgs.gdp.helper;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.UnknownHostException;

public class THREDDSServerHelper {

	/**
	 * Tests whether or not a THREDDS server is reachable
	 * 
	 * @param host
	 * @param port
	 * @return
	 * @throws IOException 
	 * @throws UnknownHostException 
	 */
	public static boolean isServerReachable(final String host, final int port, final int timeout) throws UnknownHostException, IOException {
		boolean result = false;
		
		Socket testSocket = new Socket();
		InetSocketAddress address = new InetSocketAddress(host, port);
		testSocket.connect(address, timeout);
		result = testSocket.isConnected();
		if (result) testSocket.close();
		
		return result;
	}
	
}
