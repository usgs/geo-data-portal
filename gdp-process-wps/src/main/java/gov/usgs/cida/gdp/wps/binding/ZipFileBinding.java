package gov.usgs.cida.gdp.wps.binding;

import java.io.File;
import org.n52.wps.io.data.IComplexData;

public class ZipFileBinding implements IComplexData {

    protected final File file;

    public ZipFileBinding(File file) {
        this.file = file;
    }

    @Override
    public File getPayload() {
        return file;
    }

    @Override
    public Class getSupportedClass() {
        return File.class;
    }

}