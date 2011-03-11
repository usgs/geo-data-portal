package gov.usgs.cida.gdp.wps.database;

import com.google.common.base.Joiner;
import gov.usgs.cida.gdp.constants.AppConstant;
import gov.usgs.cida.gdp.wps.util.MIMEUtil;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.sql.Connection;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.io.IOUtils;
import org.n52.wps.commons.WPSConfig;
import org.n52.wps.io.datahandler.binary.LargeBufferStream;
import org.n52.wps.server.ExceptionReport;
import org.n52.wps.server.RetrieveResultServlet;
import org.n52.wps.server.database.IDatabase;
import org.n52.wps.server.response.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/*
 * @author tkunicki
 *
 */
public final class ResultsDatabase implements IDatabase {

    private final static Logger LOGGER = LoggerFactory.getLogger(ResultsDatabase.class);

    private final static String SUFFIX_MIMETYPE = "mime-type";
    private final static String SUFFIX_XML = "xml";
    private final static String SUFFIX_TEMP = "tmp";

    // If the delimiter changes, examine Patterns below.
    private final static Joiner JOINER = Joiner.on(".");

    // Grouping is used to pull out integer index of response, if these patterns
    // change examine findLatestResponseIndex(...), generateResponseFile(...)
    // and generateResponseFile(...)
    private final static Pattern PATTERN_RESPONSE = Pattern.compile("([\\d]+)\\." + SUFFIX_XML);
    private final static Pattern PATTERN_RESPONSE_TEMP = Pattern.compile("([\\d]+)\\."  + SUFFIX_XML + "(:?\\." + SUFFIX_TEMP + ")?");

    private static ResultsDatabase instance;

    public synchronized static IDatabase getInstance() {
        if (instance == null) {
            instance = new ResultsDatabase();
        }
        return instance;
    }

    protected final File baseDirectory;
    protected final String baseResultURL;
    protected final Timer wipeTimer;
    protected final Set<Thread> threadReentrantCheckSet;

    protected ResultsDatabase() {

        String baseDirectoryPath = Joiner.on(File.separator).join(AppConstant.WORK_LOCATION.getValue(), "Database", "Results");
        baseDirectory = new File(baseDirectoryPath);
        LOGGER.info("Using \"{}\" as base directory for results database", baseDirectoryPath);
        if (!baseDirectory.exists()) {
            LOGGER.info("Results database does not exist, creating.", baseDirectoryPath);
            baseDirectory.mkdirs();
        }

        // NOTE:  The hostname and port are hard coded as part of the 52n framework design/implementation.
        baseResultURL = "http://"
                + WPSConfig.getInstance().getWPSConfig().getServer().getHostname() + ":"
                + WPSConfig.getInstance().getWPSConfig().getServer().getHostport() + "/"
                + WPSConfig.getInstance().getWPSConfig().getServer().getWebappPath() + "/"
                + RetrieveResultServlet.SERVLET_PATH + "?id=";
        LOGGER.info("Using \"{}\" as base URL for results", baseResultURL);

        long periodMillis = 1000 * 60 * 60;
        long thresholdMillis = 1000 * 60 * 60 * 24 * 7;
        wipeTimer = new Timer(getClass().getSimpleName() + " File Wiper", true);
        wipeTimer.scheduleAtFixedRate(new WipeTimerTask(thresholdMillis), 0, periodMillis);
        LOGGER.info("Started {} file wiper timer; period {} ms, threshold {} ms",
                new Object[] { getDatabaseName(), periodMillis, thresholdMillis});

        threadReentrantCheckSet = Collections.synchronizedSet(new HashSet<Thread>());
    }

    @Override
    public String generateRetrieveResultURL(String id) {
        return baseResultURL + id;
    }

    @Override
    public Connection getConnection() {
        return null;
    }

    @Override
    public String getConnectionURL() {
        return null;
    }

    @Override
    public String getDatabaseName() {
        return getClass().getSimpleName();
    }

    @Override
    public String insertResponse(Response response) {
        return this.storeResponse(response);
    }

    @Override
    public InputStream lookupResponse(String id) {
        File responseFile = lookupResponseAsFile(id);
        if (responseFile != null) {
            try {
                return new FileInputStream(responseFile);
            } catch (FileNotFoundException ex) {
                // error logged on fall through...
            }
        }
        LOGGER.warn("Response not found for id {}", id);
        return null;
    }

    @Override
    public File lookupResponseAsFile(String id) {
        File responseFile = null;
        File responseDirectory = generateResponseDirectory(id);
        if (responseDirectory.exists()) {
            synchronized (this) {
                return findLatestResponseFile(responseDirectory);
            }
        } else {
            String mimeType = getMimeTypeForStoreResponse(id);
            if (mimeType != null) {
                return generateComplexDataFile(id, mimeType);
            }
        }
        return responseFile;
    }

    @Override
    public void shutdown() {
        wipeTimer.cancel();
    }

    @Override
    public String storeComplexValue(String id, LargeBufferStream stream, String type, String mimeType) {

        String resultId = JOINER.join(id, UUID.randomUUID().toString());
        try {
            File resultFile = generateComplexDataFile(resultId, mimeType);
            File mimeTypeFile = generateComplexDataMimeTypeFile(resultId);

            OutputStream resultOutputStream = null;
            try {
                resultOutputStream = new BufferedOutputStream(new FileOutputStream(resultFile));
                stream.close();
                stream.writeTo(resultOutputStream);
                stream.destroy();
            } finally {
                IOUtils.closeQuietly(resultOutputStream);
            }

            OutputStream mimeTypeOutputStream = null;
            try {
                mimeTypeOutputStream = new BufferedOutputStream(new FileOutputStream(mimeTypeFile));
                IOUtils.write(mimeType, mimeTypeOutputStream);
            } finally {
                IOUtils.closeQuietly(mimeTypeOutputStream);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return generateRetrieveResultURL(resultId);
    }

    @Override
    public String storeResponse(Response response) {

        String reponseId = Long.toString(response.getUniqueId());
        
        // detect reentrant calls...
        if (threadReentrantCheckSet.add(Thread.currentThread()) == false) {
            // thread already added, we're reentrant...
            return generateRetrieveResultURL(reponseId);
        }

        try {

            File responseTempFile = null;
            File responseFile = null;
            synchronized (this) {
                File responseDirectory = generateResponseDirectory(reponseId);
                boolean created = responseDirectory.mkdir();
                int responseIndex = created ? 0 : findLatestResponseIndex(responseDirectory, true) + 1;
                responseFile = generateResponseFile(responseDirectory, responseIndex);
                responseTempFile = generateResponseTempFile(responseDirectory, responseIndex);
                try {
                    responseTempFile.createNewFile();
                } catch (IOException e) {
                    throw new RuntimeException("Error storing response to {}", e);
                }
            }
            OutputStream responseOutputStream = null;
            try {
                responseOutputStream = new BufferedOutputStream(new FileOutputStream(responseTempFile));
                // this call effectively will remove a stored response if it already
                // exists and block until the new reponse is created.  If the reponse is
                // to include a referenced complex output (calls storeComplexValue() above)
                // this could take a significant amount of time.  In order to allow
                // the prior response to be available we write to temp files and remame these when completed.
                response.save(responseOutputStream);
            } finally {
                IOUtils.closeQuietly(responseOutputStream);
            }

            synchronized (this) {
                responseTempFile.renameTo(responseFile);
            }

            return generateRetrieveResultURL(reponseId);

        } catch (ExceptionReport e) {
                throw new RuntimeException("Error storing response to {}", e);
        } catch (FileNotFoundException e) {
                throw new RuntimeException("Error storing response to {}", e);
        } catch (IOException e) {
                throw new RuntimeException("Error storing response to {}", e);
        } finally {
            threadReentrantCheckSet.remove(Thread.currentThread());
        }
    }

    @Override
    public void updateResponse(Response response) {
        this.storeResponse(response);
    }

    @Override
    public String getMimeTypeForStoreResponse(String id) {

        File responseDirectory = generateResponseDirectory(id);
        if (responseDirectory.exists()) {
            return "text/xml";
        } else {
            File mimeTypeFile = generateComplexDataMimeTypeFile(id);
            if (mimeTypeFile.canRead()) {
                InputStream mimeTypeInputStream = null;
                try {
                    mimeTypeInputStream = new FileInputStream(mimeTypeFile);
                    return IOUtils.toString(mimeTypeInputStream);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                } finally {
                    IOUtils.closeQuietly(mimeTypeInputStream);
                }
            }
            return null;
        }
    }

    @Override
    public boolean deleteStoredResponse(String id) {
        return true;
    }

    int findLatestResponseIndex(File responseDirectory, boolean includeTemp) {
        int responseIndex = Integer.MIN_VALUE;
        for (File file : responseDirectory.listFiles()) {
            Matcher matcher = includeTemp ?
                PATTERN_RESPONSE_TEMP.matcher(file.getName()) :
                PATTERN_RESPONSE.matcher(file.getName());
            if (matcher.matches()) {
                int fileIndex = Integer.parseInt(matcher.group(1));
                if (fileIndex > responseIndex) {
                    responseIndex = fileIndex;
                }
            }
        }
        return responseIndex;
    }

    private File findLatestResponseFile(File responseDirectory) {
        int responseIndex = findLatestResponseIndex(responseDirectory, false);
        return responseIndex < 0 ? null : generateResponseFile(responseDirectory, responseIndex);
    }

    private File generateResponseFile(File responseDirectory, int index) {
        return new File(responseDirectory, JOINER.join(index, SUFFIX_XML));
    }

    private File generateResponseTempFile(File responseDirectory, int index) {
        return new File(responseDirectory, JOINER.join(index, SUFFIX_XML, SUFFIX_TEMP));
    }

    private File generateResponseDirectory(String id) {
        return new File(baseDirectory, id);
    }

    private File generateComplexDataFile(String id, String mimeType) {
        return  new File(baseDirectory, JOINER.join(id, MIMEUtil.getSuffixFromMIMEType(mimeType)));
    }

    private File generateComplexDataMimeTypeFile(String id) {
        return  new File(baseDirectory, JOINER.join(id, SUFFIX_MIMETYPE));
    }

    private class WipeTimerTask extends TimerTask {

        public final long thresholdMillis;

        WipeTimerTask(long thresholdMillis) {
            this.thresholdMillis = thresholdMillis;
        }

        @Override
        public void run() {
            wipe(baseDirectory, thresholdMillis);
        }

        private void wipe(File rootFile, long thresholdMillis) {
            // SimpleDataFormat is not thread-safe.
            SimpleDateFormat iso8601DateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ");
            long currentTimeMillis = System.currentTimeMillis();
            LOGGER.info(getDatabaseName() + " file wiper, checking {} for files older than {} ms", rootFile.getAbsolutePath(), thresholdMillis);
            for (File file : rootFile.listFiles()) {
                long lastModifiedMillis = file.lastModified();
                long ageMillis = currentTimeMillis - lastModifiedMillis;
                if (ageMillis > thresholdMillis) {
                    LOGGER.info("Deleting {}, last modified date is {}",
                            file.getName(),
                            iso8601DateFormat.format(new Date(lastModifiedMillis)));
                    delete(file);
                    if (file.exists()) {
                        LOGGER.warn("Deletion of {} failed", file.getName());
                    }
                }
            }
        }

        private void delete(File file) {
            if (file.isDirectory()) {
                for (File child : file.listFiles()) {
                    delete(child);
                }
            }
            file.delete();
        }
    }
    public static void main(String... args) {
        Matcher m = PATTERN_RESPONSE.matcher("1.xml.tmp");
        Matcher m1 = PATTERN_RESPONSE_TEMP.matcher("1.xml.tmp");
        Matcher m2 = PATTERN_RESPONSE.matcher("1.xml.tmp");
        Matcher m3 = PATTERN_RESPONSE.matcher("1.xml.tmp");
    }
}
