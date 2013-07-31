package gov.usgs.cida.gdp.constants;

import gov.usgs.cida.config.DynamicReadOnlyProperties;
import gov.usgs.cida.gdp.utilities.JNDISingleton;
import java.io.File;
import org.slf4j.LoggerFactory;

/**
 * @author isuftin
 */
public enum AppConstant {

    USERSPACE_LOCATION("gdp.path.userspace"), // The Base User Space Directory
    SHAPEFILE_LOCATION("gdp.path.shapefile"), // The Base Shapefile Space Directory
    WORK_LOCATION("gdp.path.workspace"), // A temporary location for the app to work (unzipping files to, etc)
    CACHE_LOCATION("gdp.path.cache"), // A Dataset Extent and Variable Cache Directory
    FILE_WIPE_MILLIS("gdp.file.age.limit"), // Age, in milliseconds, that a file can be before it gets wiped (Default: 48 hours)

    WFS_ENDPOINT("gdp.geoserver.url"), // Default location where Geoserver can be found
    WFS_USER("gdp.geoserver.username"), // Username for Geoserver endpoint if needed
    WFS_PASS("gdp.geoserver.password"), // Passwords for Geoserver endpoint if needed
    WPS_ENDPOINT("gdp.endpoint.wps.url"), // Endpoint of the WPS server to use
    CSW_ENDPOINT("gdp.endpoint.csw.url"), // Endpoint of the CSW server to use

    BREAK_ON_SYSERR("gdp.check.timer.break_on_syserr"), // Should the process checker stop checking on system error (error code recv from server)
    EMAIL_ON_SYSERR("gdp.check.timer.email_on_syserr"), // Should the process checker email on system error (error code recv from server). Only used when BREAK_ON_SYSERR = true.
    CHECK_COMPLETE_MILLIS("gdp.communication.recheck"), // Time, in milliseconds, to recheck for process completion (Default: 1 min)
    CHECK_PROC_ERR_LIMIT("gdp.communication.recheck.err_limit"), // How many tries will the system attempt to check the process and get an error before giving up. Default is -1 (never give up)
    FROM_EMAIL("gdp.communication.from.addr"), // Email address from which to send messages
    TRACK_EMAIL("gdp.communication.bcc.addr"), // Email address to track requests on
    EMAIL_HOST("gdp.communication.smtp.host"), // Email smtp server address
    EMAIL_PORT("gdp.communication.smtp.port"); // Email smtp port number
    private String input;
    private String value;
    private final String BASE_LOCATION = System.getProperty("java.io.tmpdir") + File.separator + "GDP";
    private final int FILE_AGE_LIMIT_IN_HOURS = 48;
    private final int CHECK_COMPLETE_IN_SECONDS = 60;
    private static org.slf4j.Logger log = LoggerFactory.getLogger(AppConstant.class);

    private AppConstant(final String input) {
        DynamicReadOnlyProperties properties = JNDISingleton.getInstance();
        String result = properties.getProperty(input);

        if (result == null || "".equals(result)) {
            ///////////////////// File system constants
            // USERSPACE_LOCATION
            if (input.equals("gdp.path.userspace")) {
                result = this.BASE_LOCATION + File.separator + "UserSpace";
            }
            // SHAPEFILE_LOCATION
            if (input.equals("gdp.path.shapefile")) {
                result = this.BASE_LOCATION + File.separator + "ShapeFiles";
            }
            // WORK_LOCATION
            if (input.equals("gdp.path.workspace")) {
                result = this.BASE_LOCATION + File.separator + "WorkSpace";
            }
            // CACHE_LOCATION
            if (input.equals("gdp.path.cache")) {
                result = this.BASE_LOCATION + File.separator + "Cache";
            }
            // FILE_WIPE_MILLIS
            if (input.equals("gdp.file.age.limit")) {
                result = Long.valueOf(this.FILE_AGE_LIMIT_IN_HOURS * 3600000l).toString();
            }

            ///////////////////// Endpoint constants
            // WFS_ENDPOINT
            if (input.equals("gdp.geoserver.url")) {
                result = "http://localhost:8082/geoserver";
            }
            // WFS_USER
            if (input.equals("gdp.geoserver.username")) {
                result = "admin";
            }
            // WFS_PASS
            if (input.equals("gdp.geoserver.password")) {
                result = "not_the_password";
            }
            // WPS_ENDPOINT
            if (input.equals("gdp.endpoint.wps.url")) {
                result = "http://localhost:8080/gdp-utility-wps/WebProcessingService";
            }
            // CSW_ENDPOINT
            if (input.equals("gdp.endpoint.csw.url")) {
                result = "http://localhost:8082/geonetwork/srv/en/csw";
            }

            ///////////////////// Communication constants
            // BREAK_ON_SYSERR
            if (input.equals("gdp.check.timer.break_on_syserr")) {
                result = "false";
            }
            // EMAIL_ON_SYSERR
            if (input.equals("gdp.check.timer.email_on_syserr")) {
                result = "false";
            }
            // CHECK_COMPLETE_MILLIS
            if (input.equals("gdp.communication.recheck")) {
                result = Long.valueOf(this.CHECK_COMPLETE_IN_SECONDS * 1000l).toString();
            }
            // CHECK_PROC_ERR_LIMIT
            if (input.equals("gdp.communication.recheck.err_limit")) {
                result = "-1";
            }
            // FROM_EMAIL
            if (input.equals("gdp.communication.from.addr")) {
                result = "gdp_data@usgs.gov";
            }
            // EMAIL_HOST
            if (input.equals("gdp.communication.smtp.host")) {
                result = "mail.host.com";
            }
            // EMAIL_PORT
            if (input.equals("gdp.communication.smtp.port")) {
                result = "25";
            }
            // TRACK_EMAIL
            if (input.equals("gdp.communication.bcc.addr")) {
                result = "gdp_data@usgs.gov";
            }

            System.setProperty(input, result);
        }

        this.value = result;
    }

    public String getKey() {
        return this.input;
    }

    public String getValue() {
        log.trace(new StringBuilder("Key: ").append(this.input).append(" Value: ").append(this.value).toString());
        return this.value;
    }
}
