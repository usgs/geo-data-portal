package gov.usgs.cida.blacklist;

/**
 *
 * @author jwalker
 */
public class BlacklistFactory {

    public enum BlacklistType {
        DEFAULT,
        DELAY_ONLY,
        EXPONENTIAL
    }
    private static BlacklistInterface activeBlacklist = null;

    public synchronized static BlacklistInterface getActiveBlacklist() {
        if (activeBlacklist == null) {
            setActiveBlacklist(BlacklistType.DEFAULT);
        }
        return activeBlacklist;
    }

    public synchronized static BlacklistInterface setActiveBlacklist(BlacklistType blacklistType) {
        switch (blacklistType) {
            case DELAY_ONLY:
                activeBlacklist = DelayOnlyBlacklist.getInstance();
                break;
            case EXPONENTIAL:
                activeBlacklist = ExponentialDelayBlacklist.getInstance();
                break;
            default:
                activeBlacklist = DelayOnlyBlacklist.getInstance();
                break;
        }
        return activeBlacklist;
    }
}
