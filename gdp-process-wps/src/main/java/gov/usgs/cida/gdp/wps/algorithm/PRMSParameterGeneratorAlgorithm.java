package gov.usgs.cida.gdp.wps.algorithm;

import com.google.common.base.Joiner;
import gov.usgs.cida.gdp.coreprocessing.Delimiter;
import gov.usgs.cida.gdp.coreprocessing.analysis.grid.FeatureCoverageWeightedGridStatistics;
import gov.usgs.cida.gdp.coreprocessing.analysis.grid.Statistics1DWriter.GroupBy;
import gov.usgs.cida.gdp.coreprocessing.analysis.grid.Statistics1DWriter.Statistic;
import gov.usgs.cida.gdp.coreprocessing.analysis.statistics.WeightedStatistics1D;
import gov.usgs.cida.gdp.wps.binding.GMLStreamingFeatureCollectionBinding;
import gov.usgs.cida.gdp.wps.binding.ZipFileBinding;
import java.io.*;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.SchemaException;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;
import org.n52.wps.algorithm.annotation.Algorithm;
import org.n52.wps.algorithm.annotation.ComplexDataInput;
import org.n52.wps.algorithm.annotation.ComplexDataOutput;
import org.n52.wps.algorithm.annotation.Execute;
import org.n52.wps.algorithm.annotation.LiteralDataInput;
import org.n52.wps.server.AbstractAnnotatedAlgorithm;
import org.opengis.feature.type.PropertyDescriptor;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.operation.TransformException;
import ucar.ma2.InvalidRangeException;
import ucar.ma2.Range;
import ucar.nc2.dt.GridDatatype;


/**
 *
 * @author tkunicki
 */
@Algorithm(
    version="1.0.0",
    title="PRMS Parameter Generator",
    abstrakt="PRMS Parameter Generator")
public class PRMSParameterGeneratorAlgorithm extends AbstractAnnotatedAlgorithm {

    private FeatureCollection featureCollection;
    private String featureAttributeName;
    
    private List<URI> inputUri;
    private List<String> inputId;
    private List<String> outputId;
    private List<String> outputUnit;
   
    private Date timeStart;
    private Date timeEnd;
    
    private boolean requireFullCoverage = true;

    private File output;

    @ComplexDataInput(
            identifier=GDPAlgorithmConstants.FEATURE_COLLECTION_IDENTIFIER,
            title=GDPAlgorithmConstants.FEATURE_COLLECTION_TITLE,
            abstrakt=GDPAlgorithmConstants.FEATURE_COLLECTION_ABSTRACT,
            binding=GMLStreamingFeatureCollectionBinding.class)
    public void setFeatureCollection(FeatureCollection featureCollection) {
        this.featureCollection = featureCollection;
    }

    @LiteralDataInput(
            identifier=GDPAlgorithmConstants.FEATURE_ATTRIBUTE_NAME_IDENTIFIER,
            title=GDPAlgorithmConstants.FEATURE_ATTRIBUTE_NAME_TITLE,
            abstrakt=GDPAlgorithmConstants.FEATURE_ATTRIBUTE_NAME_ABSTRACT)
    public void setFeatureAttributeName(String featureAttributeName) {
        this.featureAttributeName = featureAttributeName;
    }

    @LiteralDataInput(
            identifier=GDPAlgorithmConstants.DATASET_URI_IDENTIFIER,
            maxOccurs= Integer.MAX_VALUE)
    public void setInputUri(List<URI> inputUri) {
        this.inputUri = inputUri;
    }

    @LiteralDataInput(
            identifier=GDPAlgorithmConstants.DATASET_ID_IDENTIFIER,
            maxOccurs= Integer.MAX_VALUE)
    public void setInputId(List<String> inputId) {
        this.inputId = inputId;
    }
    
    @LiteralDataInput(
            identifier="OUTPUT_ID",
            minOccurs=0,
            maxOccurs= Integer.MAX_VALUE)
    public void setOutputId(List<String> outputId) {
        this.outputId = outputId;
    }
    
    @LiteralDataInput(
            identifier="OUTPUT_UNIT",
            minOccurs=0,
            maxOccurs= Integer.MAX_VALUE)
    public void setOutputUnit(List<String> outputUnit) {
        this.outputUnit = outputUnit;
    }
    

    @LiteralDataInput(
            identifier=GDPAlgorithmConstants.REQUIRE_FULL_COVERAGE_IDENTIFIER,
            title=GDPAlgorithmConstants.REQUIRE_FULL_COVERAGE_TITLE,
            abstrakt=GDPAlgorithmConstants.REQUIRE_FULL_COVERAGE_ABSTRACT,
            defaultValue="true")
    public void setRequireFullCoverage(boolean requireFullCoverage) {
        this.requireFullCoverage = requireFullCoverage;
    }

    @LiteralDataInput(
            identifier=GDPAlgorithmConstants.TIME_START_IDENTIFIER,
            title=GDPAlgorithmConstants.TIME_START_TITLE,
            abstrakt=GDPAlgorithmConstants.TIME_START_ABSTRACT,
            minOccurs=0)
    public void setTimeStart(Date timeStart) {
        this.timeStart = timeStart;
    }

    @LiteralDataInput(
        identifier=GDPAlgorithmConstants.TIME_END_IDENTIFIER,
        title=GDPAlgorithmConstants.TIME_END_TITLE,
        abstrakt=GDPAlgorithmConstants.TIME_END_ABSTRACT,
        minOccurs=0)
    public void setTimeEnd(Date timeEnd) {
        this.timeEnd = timeEnd;
    }

    @ComplexDataOutput(
            identifier="OUTPUT",
            title="Output File",
            abstrakt="A zip file containing requested PRMS model input data.",
            binding=ZipFileBinding.class)
    public File getOutput() {
        return output;
    }

    @Execute
    public void process() {

        // validate argument count preconditions
        int inputUriCount = inputUri == null ? 0 : inputUri.size();
        if (inputUriCount < 1) {
            throw new IllegalStateException("INPUT_URI count must be greater than 0");
        } else {
            for (int inputUriIndex = 0; inputUriIndex < inputUriCount; ++inputUriIndex) {
                URI uri = inputUri.get(inputUriIndex);
                if (uri == null) {
                    throw new IllegalArgumentException("INPUT_URI at index " + inputUriIndex + " is empty.");
                }
                String uriScheme = uri.getScheme();
                if ( !("dods".equals(uriScheme) || "http".equalsIgnoreCase(uriScheme)) ) {
                    throw new IllegalArgumentException("INPUT_URI protocol at index " + inputUriIndex + " of \"" + uriScheme + "\" in invalid for this process, must be \"dods\" or \"http\"");
                }
            }
        }
        int inputIdCount = inputId == null ? 0 : inputId.size();
        if (inputIdCount < 1) {
            throw new IllegalStateException("INPUT_ID count must be greater than 0");
        } else {
            for (int inputIdIndex = 0; inputIdIndex < inputIdCount; ++inputIdIndex) {
                String id = inputId.get(inputIdIndex);
                if (StringUtils.isBlank(id)) {
                    throw new IllegalArgumentException("INPUT_ID at index " + inputIdIndex + " is empty.");
                }
            }
        }
        
        int inputCount = inputIdCount;
        if (inputUriCount != inputIdCount) {
            if (inputUriCount != 1) {
                throw new IllegalStateException("INPUT_URI argument count must equal INPUT_ID argument count or 1");
            } else {
                // Shim to use GDP UI for this process
                URI uri = inputUri.get(0);
                inputUri = new ArrayList<URI>(inputCount);
                for (int inputIndex = 0; inputIndex < inputCount; ++inputIndex) {
                    inputUri.add(uri);
                }
            }
        }
        
        int outputIdCount = outputId == null ? 0 : outputId.size();
        if ( ! (outputIdCount == 0 || inputCount == outputIdCount ) ) {
            if (outputIdCount == 1 && StringUtils.isBlank(outputId.get(0))) {
                // Bug in GDP UI where empty optional fields are passed...
                outputId.clear();
                outputIdCount = 0;
            } else {
                throw new IllegalStateException("OUTPUT_ID argument count must equal 0 or INPUT_ID argument count");
            }
        }
        if (outputIdCount == 0) {
            outputId = inputId;
        }
        
        int outputUnitCount = outputUnit == null ? 0 : outputUnit.size();
        if ( ! (outputUnitCount == 0 || inputCount == outputUnitCount ) ) {
            if (outputUnitCount == 1 && StringUtils.isBlank(outputUnit.get(0))) {
                // Bug in GDP UI where empty optional fields are passed...
                outputUnit.clear();
                outputUnitCount = 0;
            } else {
                throw new IllegalStateException("OUTPUT_UNIT argument count must equal 0 or INPUT_ID argument count");
            }
        }
        
        File prmsParamFile = null;
        File prmsDataFile = null;
        BufferedWriter prmsParamWriter = null;
        BufferedWriter prmsDataWriter = null;
        
        List<File> csvFileList = new ArrayList<File>(inputCount);
        
        try {
            
            PropertyDescriptor propertyDesciptor = featureCollection.getSchema().getDescriptor(featureAttributeName);
            if (propertyDesciptor == null) {
                addError("Attribute " + featureAttributeName + " not found in feature collection");
                return;
            }
            
            if (propertyDesciptor.getType().getBinding().isAssignableFrom(Number.class)) {
                /* This can happen with valid HRU values where the column in the original
                 * shapefile DBF was not defined with a numberic type.  The values *may*
                 * still be numeric and sequential.  Because of this we pre-populate and error
                 * message in case an exception is thrown.  It's too expensive (?) to check
                 * actual values (I might change my mind on this...)
                 */
                addError("Attribute " + featureAttributeName + " is not Numeric type, unable to properly index HRU");
            }

            prmsParamFile = File.createTempFile(getClass().getSimpleName(), ".param");
            prmsDataFile = File.createTempFile(getClass().getSimpleName(), ".data");
            
            prmsParamWriter = new BufferedWriter(new FileWriter(prmsParamFile));
            prmsDataWriter = new BufferedWriter(new FileWriter(prmsDataFile));
            
            for (int inputIndex = 0; inputIndex < inputCount; ++inputIndex) {
                
                URI currentInputURI = inputUri.get(inputIndex);
                String currentInputId = inputId.get(inputIndex);
                
                GridDatatype gridDatatype = GDPAlgorithmUtil.generateGridDataType(
                        currentInputURI,
                        currentInputId,
                        featureCollection.getBounds(),
                        requireFullCoverage);

                Range timeRange = GDPAlgorithmUtil.generateTimeRange(
                        gridDatatype,
                        timeStart,
                        timeEnd);

                // TODO:  all I/O instances need try/finally cleanup
                csvFileList.add(File.createTempFile(getClass().getSimpleName(), ".temp.csv"));
                BufferedWriter csvWriter = null;
                try {
                    csvWriter = new BufferedWriter(new FileWriter(csvFileList.get(inputIndex)));
                    FeatureCoverageWeightedGridStatistics.execute(
                            featureCollection,
                            featureAttributeName,
                            gridDatatype.makeSubset(null, null, timeRange, null, null, null),
                            Arrays.asList(new Statistic[] { Statistic.MEAN } ),
                            csvWriter,
                            GroupBy.STATISTIC,
                            Delimiter.COMMA,
                            requireFullCoverage,
                            false,
                            false);
                } finally {
                    IOUtils.closeQuietly(csvWriter);
                }
                
                // PRMS .params writer
                BufferedReader csvReader = null;
                try {
                    csvReader = new BufferedReader(new FileReader(csvFileList.get(inputIndex)));
                    csv2param(csvReader, prmsParamWriter, outputId.get(inputIndex));
                } finally {
                    IOUtils.closeQuietly(csvReader);
                }

            }
            
            // PRMS .data writer
            List<BufferedReader> csvReaderList = new ArrayList<BufferedReader>(csvFileList.size());
            try {
                for (File csvFile : csvFileList) {
                    csvReaderList.add(new BufferedReader(new FileReader(csvFile)));
                }
                csv2data(csvReaderList, prmsDataWriter, outputId);
            } finally {
                for (BufferedReader csvReader : csvReaderList) {
                    IOUtils.closeQuietly(csvReader);
                }
                for (File csvFile : csvFileList) {
                    FileUtils.deleteQuietly(csvFile);
                }
            }

            prmsParamWriter.close();
            prmsDataWriter.close();
            
            ZipOutputStream zipOutputStream = null;
            FileInputStream prmsParamsInputStream = null;
            FileInputStream prmsDataInputStream = null;
            try {
                output = File.createTempFile(getClass().getName(), ".zip");
                zipOutputStream = new ZipOutputStream(
                        new FileOutputStream(output));
                prmsParamsInputStream = new FileInputStream(prmsParamFile);
                zipOutputStream.putNextEntry(new ZipEntry("prms.param"));
                IOUtils.copy(prmsParamsInputStream, zipOutputStream);
                prmsParamsInputStream.close();
                zipOutputStream.closeEntry();
                prmsDataInputStream = new FileInputStream(prmsDataFile);
                zipOutputStream.putNextEntry(new ZipEntry("prms.data"));
                IOUtils.copy(prmsDataInputStream, zipOutputStream);
                prmsDataInputStream.close();
                zipOutputStream.closeEntry();
                zipOutputStream.close();
            } catch (IOException e) {
                FileUtils.deleteQuietly(output);
                throw e;
            } finally {
                IOUtils.closeQuietly(zipOutputStream);
                IOUtils.closeQuietly(prmsParamsInputStream);
                IOUtils.closeQuietly(prmsDataInputStream);
            }

        } catch (InvalidRangeException e) {
            addError("Error subsetting gridded data: " + e.getMessage());
        } catch (IOException e) {
            addError("IO Error :" + e.getMessage());
        } catch (FactoryException e) {
            addError("Error initializing CRS factory: " + e.getMessage());
        } catch (TransformException e) {
            addError("Error attempting CRS transform: " + e.getMessage());
        } catch (SchemaException e) {
            addError("Error subsetting gridded data : " + e.getMessage());
        } catch (Exception e) {
            addError("General Error: " + e.getMessage());
        } finally {
//            if (featureDataset != null) try { featureDataset.close(); } catch (IOException e) { }
            IOUtils.closeQuietly(prmsParamWriter);
            IOUtils.closeQuietly(prmsDataWriter);
            FileUtils.deleteQuietly(prmsParamFile);
            FileUtils.deleteQuietly(prmsDataFile);
            for (File csvFile : csvFileList) {
                FileUtils.deleteQuietly(csvFile);
            }
        }
    }


    public static void csv2param(List<File> inList, File out) throws FileNotFoundException, IOException {
        BufferedReader r = null;
        BufferedWriter w = null;
        try {
            w = new BufferedWriter(new FileWriter(out));
            for (File in : inList) {
                r = new BufferedReader(new FileReader(in));
                csv2param(r, w, in.getName());
                IOUtils.closeQuietly(r);
            }
        } finally {
            IOUtils.closeQuietly(w);
        }
    }

    public static void csv2data(List<File> inList, File out) throws FileNotFoundException, IOException {
        List<BufferedReader> rList = new ArrayList<BufferedReader>(inList.size());
        List<String> rName = new ArrayList<String>(inList.size());
        BufferedWriter w = null;
        try {
            for (File in : inList) {
                rList.add(new BufferedReader(new FileReader(in)));
                rName.add(in.getName());
            }
            w = new BufferedWriter(new FileWriter(out));
            csv2data(rList, w, rName);
        } finally {
            for (BufferedReader r : rList) {
                IOUtils.closeQuietly(r);
            }
            IOUtils.closeQuietly(w);
        }
    }

    private static String[] mon = {
        "jan", "feb", "mar", "apr", "may", "jun", "jul", "aug", "sep", "oct", "nov", "dec",
    };

    public static void csv2param(BufferedReader csvReader, BufferedWriter paramWriter, String variableName) throws IOException {

        String csvLine;
        do { csvLine = csvReader.readLine(); } while (csvLine.startsWith("#"));
        String[] split = csvLine.split(",");
        String[] hruLabel = Arrays.copyOfRange(split, 1, split.length);
        
        csvReader.readLine();

        int hruCount = hruLabel.length;

        WeightedStatistics1D[] mean = new WeightedStatistics1D[hruCount];
        WeightedStatistics1D[][] meanMonthly = new WeightedStatistics1D[12][hruCount];

        for (int h = 0; h < hruCount; ++h) {
            mean[h] = new WeightedStatistics1D();
            for (int m = 0; m < 12; ++m) {
                meanMonthly[m][h] = new WeightedStatistics1D();
            }
        }

        DateTimeFormatter formatter = ISODateTimeFormat.dateTimeParser();

        while ((csvLine = csvReader.readLine()) != null && !csvLine.startsWith("ALL")) {
            split = csvLine.split(",");
            DateTime currentDateTime = formatter.parseDateTime(split[0]).toDateTime(DateTimeZone.UTC);

            String[] hruValues = Arrays.copyOfRange(split, 1, split.length);

            int m = currentDateTime.getMonthOfYear() - 1;  // returns 1 indexed, we want 0 indexed
            for (int h = 0; h < hruCount; ++h) {
                double v = Double.parseDouble(hruValues[h]);
                mean[h].accumulate(v, 1);
                meanMonthly[m][h].accumulate(v, 1);
            }
        }

        List<HRU> hruList = new ArrayList<HRU>(hruCount);
        for (int h = 0; h < hruCount; ++h) {
            hruList.add(new HRU(hruLabel[h], h));
        }
        Collections.sort(hruList);

        paramWriter.write("####"); paramWriter.newLine();
        paramWriter.write("mean_" + variableName + " 0"); paramWriter.newLine();
        paramWriter.write("1"); paramWriter.newLine();
        paramWriter.write("nhru"); paramWriter.newLine();
        paramWriter.write(Integer.toString(hruCount)); paramWriter.newLine();
        paramWriter.write("2"); paramWriter.newLine();
        for (int h = 0; h < hruCount; ++h) {
            paramWriter.write(Double.toString(mean[hruList.get(h).inputIndex].getMean())); paramWriter.newLine();
        }
        for (int m = 0; m < 12; ++m) {
            paramWriter.write("####"); paramWriter.newLine();
            paramWriter.write("mean_" + variableName + "_" + mon[m] + " 0"); paramWriter.newLine();
            paramWriter.write("1"); paramWriter.newLine();
            paramWriter.write("nhru"); paramWriter.newLine();
            paramWriter.write(Integer.toString(hruCount)); paramWriter.newLine();
            paramWriter.write("2"); paramWriter.newLine();
            for (int h = 0; h < hruCount; ++h) {
                paramWriter.write(Double.toString(meanMonthly[m][hruList.get(h).inputIndex].getMean())); paramWriter.newLine();
            }
        }

    }

    public static void csv2data(List<BufferedReader> csvReaderList, BufferedWriter dataWriter, List<String> variableNames) throws IOException {

        final Joiner joiner = Joiner.on(' ');

        dataWriter.write("Created by USGS GeoDataPortal"); dataWriter.newLine();
        dataWriter.write("########################################"); dataWriter.newLine();

        String csvLine;
        
        int varCount = csvReaderList.size();

        String[] split = null;
        String[] hruLabel = null;
        int hruCount = 0;
        for (int v = 0; v < varCount; ++v) {
            
            // swallow lines with comments if they exist
            do { csvLine = csvReaderList.get(v).readLine(); } while (csvLine.startsWith("#"));

            if (v == 0) {
                split = csvLine.split(",");
                hruLabel = Arrays.copyOfRange(split, 1, split.length);
                hruCount = hruLabel.length;
            }
            csvReaderList.get(v).readLine();
            
            dataWriter.write(variableNames.get(v) + " " + hruCount); dataWriter.newLine();
        }

        List<HRU> hruList = new ArrayList<HRU>(hruCount);
        for (int h = 0; h < hruCount; ++h) {
            hruList.add(new HRU(hruLabel[h], h));
        }
        Collections.sort(hruList);

        DateTimeFormatter formatter = ISODateTimeFormat.dateTimeParser();

        while ((csvLine = csvReaderList.get(0).readLine()) != null && !csvLine.startsWith("ALL")) {
            split = csvLine.split(",");
            DateTime currentDateTime = formatter.parseDateTime(split[0]).toDateTime(DateTimeZone.UTC);

            String[] hruValues = Arrays.copyOfRange(split, 1, split.length);

            dataWriter.write(joiner.join(
                    currentDateTime.getYear(),
                    currentDateTime.getMonthOfYear(),
                    currentDateTime.getDayOfMonth(),
                    currentDateTime.getHourOfDay(),
                    currentDateTime.getMinuteOfHour(),
                    currentDateTime.getSecondOfMinute()));

            for (int h = 0; h < hruCount; ++h) {
                dataWriter.write(" " + hruValues[hruList.get(h).inputIndex]);
            }

            for (int v = 1; v < varCount; ++v) {
                csvLine = csvReaderList.get(v).readLine();
                split = csvLine.split(",");
                hruValues = Arrays.copyOfRange(split, 1, split.length);
                for (int h = 0; h < hruCount; ++h) {
                    dataWriter.write(" " + hruValues[hruList.get(h).inputIndex]);
                }
            }

            dataWriter.newLine();
        }

    }

    static class HRU implements Comparable<HRU> {
        final String name;
        final int inputIndex;
        private final int outputIndex;
        public HRU(String name, int inputIndex) {
            this.name = name;
            this.outputIndex = Integer.parseInt(name);
            this.inputIndex = inputIndex;
        }

        @Override
        public int compareTo(HRU o) {
            return Double.compare(outputIndex, o.outputIndex);
        }
    }
}

