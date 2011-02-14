package gov.usgs.cida.gdp.wps.database;

import com.google.common.base.Joiner;
import gov.usgs.cida.gdp.constants.AppConstant;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.sql.Connection;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;
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

    private final static String MIMETYPE = "mime-type";
    private final static String TEMP = "temp";
    private final static String DELIMITER = ".";

    private static ResultsDatabase instance;

    public synchronized static IDatabase getInstance() {
        if (instance == null) {
            instance = new ResultsDatabase();
        }
        return instance;
    }

    protected final File baseDirectory;
    protected final String baseResultURL;
    protected final Joiner fileNameJoiner;
    protected final Timer wipeTimer;

    protected ResultsDatabase() {

        String baseDirectoryPath = Joiner.on(File.separator).join(AppConstant.WORK_LOCATION, "Database", "Results");
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

        fileNameJoiner = Joiner.on(DELIMITER);

        long periodMillis = 1000 * 60 * 60;
        long thresholdMillis = 1000 * 60 * 60 * 24 * 7;
        wipeTimer = new Timer(getClass().getSimpleName() + " File Wiper", true);
        wipeTimer.scheduleAtFixedRate(new WipeTimerTask(thresholdMillis), periodMillis, periodMillis);
        LOGGER.info("Started {} file wiper timer; period {} ms, threshold {} ms",
                new Object[] { getDatabaseName(), periodMillis, thresholdMillis});
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
    public InputStream lookupResponse(String request_id) {

        String mimeType = getMimeTypeForStoreResponse(request_id);
        if (mimeType != null) {
            String suffix = fileSuffixFromMimeType(mimeType);
            synchronized (this) {
                File[] allFiles = baseDirectory.listFiles();
                try {
                    for (File tempFile : allFiles) {
                        String fileName = tempFile.getName();
                        if (fileName.equalsIgnoreCase(request_id)) {
                            return new FileInputStream(tempFile);
                        }
                        if (fileName.startsWith(request_id) && fileName.endsWith(suffix)) {
                            return new FileInputStream(tempFile);
                        }
                    }
                    return new FileInputStream(new File(baseDirectory, request_id));
                } catch (FileNotFoundException e) {
                    throw new RuntimeException("Could not find requested file in ResultsDatabase");
                }
            }
        } else {
            return null;
        }
    }

    @Override
    public void shutdown() {
        wipeTimer.cancel();
    }

    @Override
    public String storeComplexValue(String id, LargeBufferStream stream, String type, String mimeType) {

        String suffix = fileSuffixFromMimeType(mimeType);

        String resultBaseFileName = fileNameJoiner.join(id, UUID.randomUUID().toString());
        String resultFileName = fileNameJoiner.join(resultBaseFileName, suffix);
        String resultMimeTypeFileName = fileNameJoiner.join(resultBaseFileName, MIMETYPE);
        try {
            File resultFile = new File(baseDirectory, resultFileName);
            File mimeTypeFile = new File(baseDirectory, resultMimeTypeFileName);

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
        return generateRetrieveResultURL(resultBaseFileName);
    }

    @Override
    public String storeResponse(Response response) {

        String mimeType = "text/xml";
        String suffix = fileSuffixFromMimeType(mimeType);

        String responseBaseFileName = Long.toString(response.getUniqueId());
        String responseFileName = fileNameJoiner.join(responseBaseFileName, suffix);
        String responseMimeTypeFileName = fileNameJoiner.join(responseBaseFileName, MIMETYPE);

        try {
            File responseTempFile = new File(baseDirectory, fileNameJoiner.join(responseFileName, TEMP));
            File mimeTypeTempFile = new File(baseDirectory, fileNameJoiner.join(responseMimeTypeFileName, TEMP));

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

            OutputStream mimeTypeOutputStream = null;
            try {
                mimeTypeOutputStream = new BufferedOutputStream(new FileOutputStream(mimeTypeTempFile));
                IOUtils.write(mimeType, mimeTypeOutputStream);
            } finally {
                IOUtils.closeQuietly(mimeTypeOutputStream);
            }

            File responseFile = new File(baseDirectory, responseFileName);
            File mimeTypeFile = new File(baseDirectory, responseMimeTypeFileName);
            synchronized (this) {
                responseTempFile.renameTo(responseFile);
                mimeTypeTempFile.renameTo(mimeTypeFile);
            }
        } catch (ExceptionReport e) {
            throw new RuntimeException(e);
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return generateRetrieveResultURL(responseBaseFileName);
    }

    @Override
    public void updateResponse(Response response) {
        this.storeResponse(response);
    }

    @Override
    public String getMimeTypeForStoreResponse(String id) {
        synchronized (this) {
            File mimeTypeFile = new File(baseDirectory, fileNameJoiner.join(id, MIMETYPE));
            if (mimeTypeFile.canRead()) {
                InputStream mimeTypeInputStream = null;
                try {
                    mimeTypeInputStream = new DataInputStream(new FileInputStream(mimeTypeFile));
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

    @Override
    public File lookupResponseAsFile(String id) {
        synchronized (this) {
            File[] files = baseDirectory.listFiles();
            for (File file : files) {
                String fileName = file.getName();
                if (fileName.equalsIgnoreCase(id)) {
                    return file;
                }
            }
            return new File(baseDirectory, id);
        }
    }

    private String fileSuffixFromMimeType(String mimeType) {
        String[] mimeTypeSplit = mimeType.split("/");
        return mimeTypeSplit[mimeTypeSplit.length - 1];
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
