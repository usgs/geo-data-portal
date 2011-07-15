package gov.usgs.cida.blacklist;

import java.util.Properties;

/**
 *
 * @author jwalker
 */
public class DelayOnlyBlacklist implements BlacklistInterface {
    
    private static final long FIXED_SLEEP_TIME = 1000 * 5; // 5 seconds
    
    private static DelayOnlyBlacklist thisSingleton = null;
    
    private DelayOnlyBlacklist() {
    }
    
    public synchronized static DelayOnlyBlacklist getInstance() {
		if (thisSingleton == null) {
			thisSingleton = new DelayOnlyBlacklist();
		}
		return thisSingleton;
	}
    

    @Override
    public long determineSleepTime(String key) {
        return FIXED_SLEEP_TIME;
    }

    @Override
    public boolean isBlacklisted(String key) {
        return false;
    }

    @Override
    public Properties outputBlacklist() {
        return new Properties();
    }

    @Override
    public void remove(String key) {
        // do nothing
    }
}
