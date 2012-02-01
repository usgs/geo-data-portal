package gov.usgs.cida.gdp.wps.algorithm;

import gov.usgs.cida.gdp.coreprocessing.Delimiter;
import gov.usgs.cida.gdp.coreprocessing.analysis.grid.FeatureCoverageWeightedGridStatistics;
import gov.usgs.cida.gdp.coreprocessing.analysis.grid.FeatureCoverageWeightedGridStatistics.GroupBy;
import gov.usgs.cida.gdp.coreprocessing.analysis.grid.FeatureCoverageWeightedGridStatisticsWriter.Statistic;
import gov.usgs.cida.gdp.coreprocessing.analysis.statistics.WeightedStatistics1D;
import gov.usgs.cida.gdp.wps.binding.CSVFileBinding;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import org.apache.commons.io.IOUtils;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.SchemaException;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;
import org.n52.wps.algorithm.annotation.Algorithm;
import org.n52.wps.algorithm.annotation.ComplexDataInput;
import org.n52.wps.algorithm.annotation.ComplexDataOutput;
import org.n52.wps.algorithm.annotation.LiteralDataInput;
import org.n52.wps.algorithm.annotation.Process;
import org.n52.wps.io.data.binding.complex.GTVectorDataBinding;
import org.n52.wps.server.AbstractAnnotatedAlgorithm;
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
    title="PRMS Feature Weighted Grid Statistics",
    abstrakt="PRMS Model Parameter Generator")
public class PRMSFeatureWeightedGridStatisticsAlgorithm extends AbstractAnnotatedAlgorithm {

    private FeatureCollection featureCollection;
    private String featureAttributeName;
    private URI datasetURI;
    private List<String> datasetId;
    private boolean requireFullCoverage = true;
    private Date timeStart;
    private Date timeEnd;

    final private List<Statistic> statistics = Arrays.asList( new Statistic[] { Statistic.MEAN } );
    final private GroupBy groupBy = GroupBy.STATISTIC;
    final private Delimiter delimiter = Delimiter.COMMA;
    final private boolean summarizeTimeStep = false;
    final private boolean summarizeFeatureAttribute = false;

    private File output;

    @ComplexDataInput(
            identifier=GDPAlgorithmConstants.FEATURE_COLLECTION_IDENTIFIER,
            title=GDPAlgorithmConstants.FEATURE_COLLECTION_TITLE,
            abstrakt=GDPAlgorithmConstants.FEATURE_COLLECTION_ABSTRACT,
            binding=GTVectorDataBinding.class)
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
            title=GDPAlgorithmConstants.DATASET_URI_TITLE,
            abstrakt=GDPAlgorithmConstants.DATASET_URI_ABSTRACT)
    public void setDatasetURI(URI datasetURI) {
        this.datasetURI = datasetURI;
    }

    @LiteralDataInput(
            identifier=GDPAlgorithmConstants.DATASET_ID_IDENTIFIER,
            title=GDPAlgorithmConstants.DATASET_ID_TITLE,
            abstrakt=GDPAlgorithmConstants.DATASET_ID_ABSTRACT,
            maxOccurs= Integer.MAX_VALUE)
    public void setDatasetId(List<String> datasetId) {
        this.datasetId = datasetId;
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
            abstrakt="A delimited text file containing requested PRMS model input data.",
            binding=CSVFileBinding.class)
    public File getOutput() {
        return output;
    }

    @Process
    public void process() {

//        FeatureDataset featureDataset = null;
        BufferedWriter writer = null;
        try {
            if (featureCollection.getSchema().getDescriptor(featureAttributeName) == null) {
                addError("Attribute " + featureAttributeName + " not found in feature collection");
                return;
            }

            output = File.createTempFile(getClass().getSimpleName(), ".csv");
            writer = new BufferedWriter(new FileWriter(output));

            for (String currentDatasetId : datasetId) {
                GridDatatype gridDatatype = GDPAlgorithmUtil.generateGridDataType(
                        datasetURI,
                        currentDatasetId,
                        featureCollection.getBounds(),
                        requireFullCoverage);

                Range timeRange = GDPAlgorithmUtil.generateTimeRange(
                        gridDatatype,
                        timeStart,
                        timeEnd);

                // TODO:  all I/O instances need try/finally cleanup
                File csvTempFile = File.createTempFile(getClass().getSimpleName(), ".temp.csv");
                BufferedWriter csvWriter = new BufferedWriter(new FileWriter(csvTempFile));
                csvWriter.write("# " + currentDatasetId);
                csvWriter.newLine();
                FeatureCoverageWeightedGridStatistics.execute(
                        featureCollection,
                        featureAttributeName,
                        gridDatatype,
                        timeRange,
                        statistics == null || statistics.isEmpty() ? Arrays.asList(Statistic.values()) : statistics,
                        csvWriter,
                        groupBy == null ? GroupBy.STATISTIC : groupBy,
                        delimiter == null ? Delimiter.COMMA : delimiter,
                        requireFullCoverage,
                        summarizeTimeStep,
                        summarizeFeatureAttribute);
                csvWriter.close();
                BufferedReader csvReader = new BufferedReader(new FileReader(csvTempFile));
                csv2prms(csvReader, writer);
                csvReader.close();
                csvTempFile.delete();


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
            IOUtils.closeQuietly(writer);
        }
    }


    public static void csv2prms(File in, File out) throws FileNotFoundException, IOException {
        BufferedReader r = null;
        BufferedWriter w = null;
        try {
            r = new BufferedReader(new FileReader(in));
            w = new BufferedWriter(new FileWriter(out));
            csv2prms(r, w);
        } finally {
            if (r != null) {
                try { r.close(); } catch (IOException e) {}
            }
            if (w != null) {
                try { w.close(); } catch (IOException e) {}
            }
        }
    }

    private static String[] mon = {
        "jan", "feb", "mar", "apr", "may", "jun", "jul", "aug", "sep", "oct", "nov", "dec",
    };

    public static void csv2prms(BufferedReader csvReader, BufferedWriter prmsWriter) throws IOException {

        String varName = csvReader.readLine().substring(2).toLowerCase(); // assumes '# '

        String[] split = csvReader.readLine().split(",");
        String[] hruLabel = Arrays.copyOfRange(split, 2, split.length);
        
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

        String csvLine = null;

        while ((csvLine = csvReader.readLine()) != null && !csvLine.startsWith("ALL")) {
            split = csvLine.split(",");
            DateTime currentDateTime = formatter.parseDateTime(split[0]).toDateTime(DateTimeZone.UTC);

            String[] hruValues = Arrays.copyOfRange(split, 2, split.length);

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
        Collections.sort(hruList, new ComparatorHRU());

        prmsWriter.write("####"); prmsWriter.newLine();
        prmsWriter.write("mean_" + varName + " 0"); prmsWriter.newLine();
        prmsWriter.write("1"); prmsWriter.newLine();
        prmsWriter.write("nhru"); prmsWriter.newLine();
        prmsWriter.write(Integer.toString(hruCount)); prmsWriter.newLine();
        prmsWriter.write("2"); prmsWriter.newLine();
        for (int h = 0; h < hruCount; ++h) {
            prmsWriter.write(Double.toString(mean[hruList.get(h).inputIndex].getMean())); prmsWriter.newLine();
        }
        for (int m = 0; m < 12; ++m) {
            prmsWriter.write("####"); prmsWriter.newLine();
            prmsWriter.write("mean_" + varName + "_" + mon[m] + " 0"); prmsWriter.newLine();
            prmsWriter.write("1"); prmsWriter.newLine();
            prmsWriter.write("nhru"); prmsWriter.newLine();
            prmsWriter.write(Integer.toString(hruCount)); prmsWriter.newLine();
            prmsWriter.write("2"); prmsWriter.newLine();
            for (int h = 0; h < hruCount; ++h) {
                prmsWriter.write(Double.toString(meanMonthly[m][hruList.get(h).inputIndex].getMean())); prmsWriter.newLine();
            }
        }

    }

    private static class HRU {
        final String name;
        final int inputIndex;
        final int outputIndex;
        public HRU(String name, int inputIndex) {
            this.name = name;
            this.outputIndex = Integer.parseInt(name);
            this.inputIndex = inputIndex;
        }
    }

    private static class ComparatorHRU implements Comparator<HRU> {

        @Override
        public int compare(HRU hl, HRU hr) {
            return Double.compare(hl.outputIndex, hr.outputIndex);
        }
    }
}

