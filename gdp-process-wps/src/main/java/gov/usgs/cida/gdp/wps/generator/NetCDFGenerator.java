package gov.usgs.cida.gdp.wps.generator;

import gov.usgs.cida.gdp.wps.binding.NetCDFFileBinding;
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
public class NetCDFGenerator implements IGenerator, IStreamableGenerator {

    public static final String FORMAT = "application/netcdf";
    public static final String ENCODING = "UTF-8";  // temporary HACK

    @Override
    public void writeToStream(IData data, OutputStream os) {
        if (data instanceof NetCDFFileBinding) {
            Object payload = data.getPayload();
            if (payload instanceof File) {
                File payloadFile = (File) payload;
                InputStream is = null;
                try {
                    is = new FileInputStream((File)payload);
                    IOUtils.copy(is, os);
                } catch (IOException ex) {
                } finally {
                    IOUtils.closeQuietly(is);
                    payloadFile.delete();
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
        return new Class[] { NetCDFFileBinding.class };
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
        return null;
    }

}
