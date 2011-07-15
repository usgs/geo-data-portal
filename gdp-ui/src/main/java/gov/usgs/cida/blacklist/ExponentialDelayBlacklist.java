package gov.usgs.cida.blacklist;

import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 *
 * @author jwalker
 */
public class ExponentialDelayBlacklist implements BlacklistInterface {

	public static final long BLACKLIST_EXPIRY_TIME = 1000 * 60 * 60 * 24; // one day
	public static final long INITIAL_SLEEP = 1000 * 5; // five seconds
	public static final long REQUEST_EXPIRY_TIME = 1000 * 60 * 60; // one hour
	public static final Integer MAX_REQUESTS = new Integer(5);

	private static ExponentialDelayBlacklist singleton = null;
	private Map<String, Date> ipBlacklist = null;
	private Map<String, Pair<Date, Integer>> requestDelayMap = null;

	private ExponentialDelayBlacklist() {
		ipBlacklist = Collections.synchronizedMap(new HashMap<String, Date>());
		requestDelayMap = Collections.synchronizedMap(new HashMap<String, Pair<Date, Integer>>());
	}

	public synchronized static ExponentialDelayBlacklist getInstance() {
		if (singleton == null) {
			singleton = new ExponentialDelayBlacklist();
		}
		return singleton;
	}

	/**
	 * Used for management interface (removing blacklisted ip's)
	 * @return properties object representing blacklist
	 */
    @Override
	public Properties outputBlacklist() {
		Properties props = new Properties();
		for (String key : ipBlacklist.keySet()) {
			props.put(key, ipBlacklist.get(key).toString());
		}
		return props;
	}

	/**
	 * Gives management interface a way to remove items from blacklist
	 * @param key Internet Address of client
	 * @return Date associated with client's entry into blacklist
	 */
    @Override
	public void remove(String key) {
		ipBlacklist.remove(key);
		requestDelayMap.remove(key);
	}

	/**
	 * Used by proxy to determine whether to attempt a lookup for a client
	 * @param key Internet Address of client
	 * @return true if client is blacklisted
	 */
    @Override
	public boolean isBlacklisted(String key) {
		if (ipBlacklist.containsKey(key)) {
			Date addedToBlacklist = ipBlacklist.get(key);

			Date expires = new Date(addedToBlacklist.getTime() + BLACKLIST_EXPIRY_TIME);
			Date now = new Date();
			if (now.after(expires)) {
				ipBlacklist.remove(key);
				return false;
			}
			return true;
		}
		else {
			return false;
		}
	}

	/**
	 * Bad requests end up getting delayed, this determines how long
	 * @param key Internet Address of client
	 * @return milliseconds to sleep for requested client
	 */
    @Override
	public long determineSleepTime(String key) {
		long timeToSleep = INITIAL_SLEEP;
		if (requestDelayMap.containsKey(key)) {
			Pair<Date, Integer> pair = requestDelayMap.get(key);
			Date now = new Date();
			Date requestWindow = new Date(pair.val1.getTime() + REQUEST_EXPIRY_TIME);
			if (now.after(requestWindow)) {
				requestDelayMap.put(key, new Pair<Date, Integer>(now, new Integer(0)));
			}
			timeToSleep = INITIAL_SLEEP * (long)Math.pow(2, pair.val2);
			pair.val2 = new Integer(pair.val2.intValue() + 1);
			if (pair.val2.compareTo(MAX_REQUESTS) >= 0) {
				ipBlacklist.put(key, new Date());
			}
		}
		else {
			Pair<Date, Integer> requestCounts = new Pair<Date, Integer>(new Date(), new Integer(1));
			requestDelayMap.put(key, requestCounts);
		}
		return timeToSleep;
	}
}
