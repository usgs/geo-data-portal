/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package gov.usgs.cida.blacklist;

import java.util.Properties;

/**
 *
 * @author jwalker
 */
public interface BlacklistInterface {

    /**
     * Bad requests end up getting delayed, this determines how long
     * @param key Internet Address of client
     * @return milliseconds to sleep for requested client
     */
    long determineSleepTime(String key);

    /**
     * Used by proxy to determine whether to attempt a lookup for a client
     * @param key Internet Address of client
     * @return true if client is blacklisted
     */
    boolean isBlacklisted(String key);

    /**
     * Used for management interface (removing blacklisted ip's)
     * @return properties object representing blacklist
     */
    Properties outputBlacklist();

    /**
     * Gives management interface a way to remove items from blacklist
     * @param key Internet Address of client
     * @return Date associated with client's entry into blacklist
     */
    void remove(String key);
    
}
