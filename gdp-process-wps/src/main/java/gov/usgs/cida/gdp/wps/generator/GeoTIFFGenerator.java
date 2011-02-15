package gov.usgs.cida.gdp.wps.generator;

import gov.usgs.cida.gdp.wps.binding.CSVDataBinding;
import gov.usgs.cida.gdp.wps.binding.GeoTIFFFileBinding;
import gov.usgs.cida.gdp.wps.util.GeoTIFFUtil;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import org.apache.commons.io.IOUtils;
import org.n52.wps.io.IGenerator;
import org.n52.wps.io.IStreamableGenerator;
import org.n52.wps.io.data.IData;
import org.n52.wps.io.datahandler.binary.LargeBufferStream;

/**
 *
 * @author tkunicki
 */
public class GeoTIFFGenerator implements IGenerator, IStreamableGenerator {

    public static final String FORMAT = GeoTIFFUtil.getDefaultMimeType();
    public static final String ENCODING = "UTF-8";

    @Override
    public void writeToStream(IData data, OutputStream os) {
        if (data instanceof GeoTIFFFileBinding) {
            Object payload = data.getPayload();
            if (payload instanceof File) {
                InputStream is = null;
                try {
                    is = new FileInputStream((File)payload);
                    IOUtils.copy(is, os);
                } catch (IOException ex) {
                } finally {
                    IOUtils.closeQuietly(is);
                }
            }
        }
    }

    @Override
    public OutputStream generate(IData data) {
        OutputStream os = new LargeBufferStream();
        writeToStream(data, os);
        return os;
    }

    @Override
    public Class[] getSupportedInternalInputDataType() {
        return new Class[] { GeoTIFFFileBinding.class };
    }

    @Override
    public boolean isSupportedSchema(String schema) {
        return schema == null || schema.length() == 0;
    }

    @Override
    public boolean isSupportedFormat(String format) {
        return FORMAT.equals(format);
    }

    @Override
    public boolean isSupportedEncoding(String encoding) {
        return ENCODING.equals(encoding);
    }

    @Override
    public String[] getSupportedSchemas() {
        return null;
    }

    @Override
    public String[] getSupportedFormats() {
        return new String[] { FORMAT };
    }

    @Override
    public String[] getSupportedEncodings() {
        return new String[] { ENCODING };
    }

}
