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
import java.util.logging.Level;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;
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
    private final static String SUFFIX_GZIP = "gz";

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

    protected final boolean gzipComplexValues = true;

    protected final Object storeResponseSerialNumberLock;
    protected final Set<Thread> storeResponseReentrantCheckSet;

    protected final Timer wipeTimer;

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

        long periodMillis = 1000 * 60 * 60; // 1h
        long thresholdMillis = 1000 * 60 * 60 * 24 * 7; // 7d
        wipeTimer = new Timer(getClass().getSimpleName() + " File Wiper", true);
        wipeTimer.scheduleAtFixedRate(new WipeTimerTask(thresholdMillis), 0, periodMillis);
        LOGGER.info("Started {} file wiper timer; period {} ms, threshold {} ms",
                new Object[] { getDatabaseName(), periodMillis, thresholdMillis});

        storeResponseSerialNumberLock = new Object();
        storeResponseReentrantCheckSet = Collections.synchronizedSet(new HashSet<Thread>());
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
        LOGGER.info("response file for {} is {}", id, responseFile.getPath());
        if (responseFile != null && responseFile.exists()) {
            try {
                return responseFile.getName().endsWith(SUFFIX_GZIP) ?
                    new GZIPInputStream(new FileInputStream(responseFile)) :
                    new FileInputStream(responseFile);
            } catch (FileNotFoundException ex) {
                // should never get here do to checks above...
                LOGGER.warn("Response not found for id {}", id);
            } catch (IOException ex) {
                LOGGER.warn("Error processing response for id {}", id);
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
            synchronized (storeResponseSerialNumberLock) {
                return findLatestResponseFile(responseDirectory);
            }
        } else {
            String mimeType = getMimeTypeForStoreResponse(id);
            if (mimeType != null) {
                // ignore gzipComplexValues in case file was stored when value
                // was inconsistent with current value;
                responseFile = generateComplexDataFile(id, mimeType, false);
                if (!responseFile.exists()) {
                    responseFile = generateComplexDataFile(id, mimeType, true);
                }
                if (!responseFile.exists()) {
                    responseFile = null;
                }
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
            File resultFile = generateComplexDataFile(resultId, mimeType, gzipComplexValues);
            File mimeTypeFile = generateComplexDataMimeTypeFile(resultId);

            LOGGER.info("initiating storage of complex value for {} as {}", id, resultFile.getPath());
            
            OutputStream resultOutputStream = null;
            try {
                resultOutputStream = gzipComplexValues ?
                    new GZIPOutputStream(new FileOutputStream(resultFile)):
                    new BufferedOutputStream(new FileOutputStream(resultFile));
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
            
            try {
                Thread.sleep(5000);
            } catch (InterruptedException ex) {
                java.util.logging.Logger.getLogger(ResultsDatabase.class.getName()).log(Level.SEVERE, null, ex);
            }
            

            LOGGER.info("completed storage of complex value for {} as {}", id, resultFile.getPath());
            
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return generateRetrieveResultURL(resultId);
    }

    @Override
    public String storeResponse(Response response) {

        String reponseId = Long.toString(response.getUniqueId());
        
        // Detect reentrant calls.
        if (storeResponseReentrantCheckSet.add(Thread.currentThread()) == false) {
            // If the call above returns false, this set was not modified meaning
            // this thread has already called storeResponse.
            return generateRetrieveResultURL(reponseId);
        }

        try {

            File responseTempFile = null;
            File responseFile = null;
            synchronized (storeResponseSerialNumberLock) {
                File responseDirectory = generateResponseDirectory(reponseId);
                boolean created = responseDirectory.mkdir();
                int responseIndex = created ? 0 : (findLatestResponseIndex(responseDirectory, true) + 1);
                responseFile = generateResponseFile(responseDirectory, responseIndex);
                responseTempFile = generateResponseTempFile(responseDirectory, responseIndex);
                try {
                    // create the file so that the reponse serial number is correctly
                    // incremented if this method is called again for this reponse
                    // before this reponse is completed.
                    responseTempFile.createNewFile();
                } catch (IOException e) {
                    throw new RuntimeException("Error storing response to {}", e);
                }
                LOGGER.info("creating temp file for {} as {}", reponseId, responseTempFile.getPath());
            }
            OutputStream responseOutputStream = null;
            try {
                responseOutputStream = new BufferedOutputStream(new FileOutputStream(responseTempFile));
                // In order to allow the prior response to be available we write
                // to a temp file and rename these when completed.  Large responses
                // can cause the call below to take a significant amount of time.
                response.save(responseOutputStream);
            } finally {
                IOUtils.closeQuietly(responseOutputStream);
            }

            synchronized (storeResponseSerialNumberLock) {
                responseTempFile.renameTo(responseFile);
                LOGGER.info("renamed temp file for {} to {}", reponseId, responseFile.getPath());
            }

            return generateRetrieveResultURL(reponseId);

        } catch (ExceptionReport e) {
                throw new RuntimeException("Error storing response to {}", e);
        } catch (FileNotFoundException e) {
                throw new RuntimeException("Error storing response to {}", e);
        } catch (IOException e) {
                throw new RuntimeException("Error storing response to {}", e);
        } finally {
            storeResponseReentrantCheckSet.remove(Thread.currentThread());
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

    private File generateComplexDataFile(String id, String mimeType, boolean gzip) {
        String fileName = gzip ?
            JOINER.join(id, MIMEUtil.getSuffixFromMIMEType(mimeType), SUFFIX_GZIP) :
            JOINER.join(id, MIMEUtil.getSuffixFromMIMEType(mimeType));
        return new File(baseDirectory, fileName);
    }

    private File generateComplexDataMimeTypeFile(String id) {
        return new File(baseDirectory, JOINER.join(id, SUFFIX_MIMETYPE));
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
    
}
