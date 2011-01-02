package gov.usgs.cida.gdp.wps.algorithm;

import gov.usgs.cida.gdp.wps.algorithm.descriptor.AlgorithmDescriptor;
import gov.usgs.cida.gdp.wps.algorithm.descriptor.ComplexDataInputDescriptor;
import gov.usgs.cida.gdp.wps.algorithm.descriptor.ComplexDataOutputDescriptor;
import gov.usgs.cida.gdp.wps.algorithm.descriptor.InputDescriptor;
import gov.usgs.cida.gdp.wps.algorithm.descriptor.LiteralDataInputDescriptor;
import gov.usgs.cida.gdp.wps.algorithm.descriptor.LiteralDataOutputDescriptor;
import gov.usgs.cida.gdp.wps.algorithm.descriptor.OutputDescriptor;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import net.opengis.ows.x11.AllowedValuesDocument.AllowedValues;
import net.opengis.wps.x100.ComplexDataCombinationType;
import net.opengis.wps.x100.ComplexDataCombinationsType;
import net.opengis.wps.x100.ComplexDataDescriptionType;
import net.opengis.wps.x100.InputDescriptionType;
import net.opengis.wps.x100.LiteralInputType;
import net.opengis.wps.x100.OutputDescriptionType;
import net.opengis.wps.x100.ProcessDescriptionType;
import net.opengis.wps.x100.ProcessDescriptionsDocument;
import net.opengis.wps.x100.SupportedComplexDataType;
import net.opengis.wps.x100.ProcessDescriptionType.DataInputs;
import net.opengis.wps.x100.ProcessDescriptionType.ProcessOutputs;
import net.opengis.wps.x100.ProcessDescriptionsDocument.ProcessDescriptions;
import org.apache.xmlbeans.XmlOptions;
import org.apache.xmlbeans.XmlValidationError;
import org.n52.wps.io.GeneratorFactory;
import org.n52.wps.io.IGenerator;
import org.n52.wps.io.IOHandler;
import org.n52.wps.io.IParser;
import org.n52.wps.io.ParserFactory;
import org.n52.wps.io.data.IData;
import org.n52.wps.server.AbstractAlgorithm;
import org.n52.wps.server.observerpattern.IObserver;
import org.n52.wps.server.observerpattern.ISubject;

public abstract class AbstractSelfDescribingAlgorithm extends AbstractAlgorithm implements ISubject {

    public AbstractSelfDescribingAlgorithm() {
        super();
    }

    @Override
    protected ProcessDescriptionType initializeDescription() {

        AlgorithmDescriptor algorithmDescriptor = getAlgorithmDescriptor();

        ProcessDescriptionsDocument document = ProcessDescriptionsDocument.Factory.newInstance();
        ProcessDescriptions processDescriptions = document.addNewProcessDescriptions();
        ProcessDescriptionType processDescription = processDescriptions.addNewProcessDescription();
        processDescription.setStatusSupported(algorithmDescriptor.getStatusSupported());
        processDescription.setStoreSupported(algorithmDescriptor.getStoreSupported());
        processDescription.setProcessVersion(algorithmDescriptor.getVersion());

        // 1. Identifer
        processDescription.addNewIdentifier().setStringValue(algorithmDescriptor.getIdentifier());
        processDescription.addNewTitle().setStringValue( algorithmDescriptor.hasTitle() ?
                algorithmDescriptor.getIdentifier() :
                algorithmDescriptor.getTitle());
        if (algorithmDescriptor.hasAbstract()) {
            processDescription.addNewAbstract().setStringValue(algorithmDescriptor.getAbstract());
        }

        // 2. Inputs
        Collection<InputDescriptor> inputDescriptors = algorithmDescriptor.getInputDescriptors();
        DataInputs dataInputs = null;
        if (inputDescriptors.size() > 0) {
            dataInputs = processDescription.addNewDataInputs();
        }

        for (InputDescriptor inputDescriptor : inputDescriptors) {

            InputDescriptionType dataInput = dataInputs.addNewInput();
            dataInput.setMinOccurs(inputDescriptor.getMinOccurs());
            dataInput.setMaxOccurs(inputDescriptor.getMaxOccurs());

            dataInput.addNewIdentifier().setStringValue(inputDescriptor.getIdentifier());
            dataInput.addNewTitle().setStringValue( inputDescriptor.hasTitle() ?
                    inputDescriptor.getTitle() :
                    inputDescriptor.getIdentifier());
            if (inputDescriptor.hasAbstract()) {
                dataInput.addNewAbstract().setStringValue(inputDescriptor.getAbstract());
            }

            if (inputDescriptor instanceof LiteralDataInputDescriptor) {
                LiteralDataInputDescriptor literalDescriptor = (LiteralDataInputDescriptor)inputDescriptor;

                LiteralInputType literalData = dataInput.addNewLiteralData();
                literalData.addNewDataType().setReference(literalDescriptor.getDataType());

                if (literalDescriptor.hasDefaultValue()) {
                    literalData.setDefaultValue(literalDescriptor.getDefaultValue().toString());
                }
                if (literalDescriptor.hasAllowedValues()) {
                    AllowedValues allowed = literalData.addNewAllowedValues();
                    for (Object allow : literalDescriptor.getAllowedValues()) {
                        allowed.addNewValue().setStringValue(allow.toString());
                    }
                } else {
                    literalData.addNewAnyValue();
                }

            } else if (inputDescriptor instanceof ComplexDataInputDescriptor) {
                describeComplexDataInput(dataInput.addNewComplexData(), inputDescriptor.getBinding());
            }
        }

        //3. Outputs
        ProcessOutputs dataOutputs = processDescription.addNewProcessOutputs();
        Collection<OutputDescriptor> outputDescriptors = algorithmDescriptor.getOutputDescriptors();
        for (OutputDescriptor outputDescriptor : outputDescriptors) {

            OutputDescriptionType dataOutput = dataOutputs.addNewOutput();
            dataOutput.addNewIdentifier().setStringValue(outputDescriptor.getIdentifier());
            dataOutput.addNewTitle().setStringValue( outputDescriptor.hasTitle() ?
                    outputDescriptor.getTitle() :
                    outputDescriptor.getIdentifier());
            if (outputDescriptor.hasAbstract()) {
                dataOutput.addNewAbstract().setStringValue(outputDescriptor.getAbstract());
            }

            if (outputDescriptor instanceof LiteralDataOutputDescriptor) {
                LiteralDataOutputDescriptor literalDescriptor = (LiteralDataOutputDescriptor)outputDescriptor;
                dataOutput.addNewLiteralOutput().addNewDataType().
                        setReference(literalDescriptor.getDataType());
            } else if (outputDescriptor instanceof ComplexDataOutputDescriptor) {
                describeComplexDataOutput(dataOutput.addNewComplexOutput(), outputDescriptor.getBinding());
           }
        }
        return document.getProcessDescriptions().getProcessDescriptionArray(0);
    }

    private void describeComplexDataInput(SupportedComplexDataType complexData, Class dataTypeClass) {
        List<IParser> parsers = ParserFactory.getInstance().getAllParsers();
        List<IParser> foundParsers = new ArrayList<IParser>();
        for (IParser parser : parsers) {
            Class[] supportedClasses = parser.getSupportedInternalOutputDataType();
            for (Class clazz : supportedClasses) {
                if (clazz.equals(dataTypeClass)) {
                    foundParsers.add(parser);
                }
            }
        }
        describeComplexData(complexData, foundParsers);
    }

    private void describeComplexDataOutput(SupportedComplexDataType complexData, Class dataTypeClass) {

        List<IGenerator> generators = GeneratorFactory.getInstance().getAllGenerators();
        List<IGenerator> foundGenerators = new ArrayList<IGenerator>();
        for (IGenerator generator : generators) {
            Class[] supportedClasses = generator.getSupportedInternalInputDataType();
            for (Class clazz : supportedClasses) {
                if (clazz.equals(dataTypeClass)) {
                    foundGenerators.add(generator);
                }
            }
        }
        describeComplexData(complexData, foundGenerators);
    }

    private void describeComplexData(
            SupportedComplexDataType complexData,
            List<? extends IOHandler> handlers)
    {
        ComplexDataCombinationType defaultFormatType = complexData.addNewDefault();
        ComplexDataCombinationsType supportedFormatType = complexData.addNewSupported();

        for (IOHandler generator : handlers) {

            String[] supportedFormats = generator.getSupportedFormats();
            String[] supportedEncodings = generator.getSupportedEncodings();
            String[] supportedSchemas = generator.getSupportedSchemas();

            if ((supportedFormats != null && supportedFormats.length > 0) &&
                (supportedEncodings != null && supportedEncodings.length > 0)) {

                if (supportedSchemas == null) { supportedSchemas = new String[0]; }

                ComplexDataDescriptionType defaultFormat = defaultFormatType.addNewFormat();
                defaultFormat.setMimeType(supportedFormats[0]);
                defaultFormat.setEncoding(supportedEncodings[0]);
                if (supportedSchemas.length > 0) {
                    defaultFormat.setSchema(supportedSchemas[0]);
                }
                for (String supportedFormat : supportedFormats) {
                    for (String supportedEncoding : supportedEncodings) {
                        if (supportedSchemas.length > 0) {
                            for (String supportedSchema : supportedSchemas) {
                                ComplexDataDescriptionType supportedCreatedFormat = supportedFormatType.addNewFormat();
                                supportedCreatedFormat.setMimeType(supportedFormat);
                                supportedCreatedFormat.setEncoding(supportedEncoding);
                                supportedCreatedFormat.setSchema(supportedSchema);
                            }
                        } else {
                            ComplexDataDescriptionType supportedCreatedFormat = supportedFormatType.addNewFormat();
                            supportedCreatedFormat.setMimeType(supportedFormat);
                            supportedCreatedFormat.setEncoding(supportedEncoding);
                        }
                    }
                }
            }
        }
    }

    @Override
    public boolean processDescriptionIsValid() {
        XmlOptions xmlOptions = new XmlOptions();
        List<XmlValidationError> errorList = new ArrayList<XmlValidationError>();
            xmlOptions.setErrorListener(errorList);
        boolean valid = getDescription().validate(xmlOptions);
        if (!valid) {
            System.err.println("Error validating process description for " + getClass().getCanonicalName());
            for (XmlValidationError error : errorList) {
                System.err.println("\tMessage: " + error.getMessage());
                System.err.println("\tLocation of invalid XML: " +
                     error.getCursorLocation().xmlText());
            }
        }
        return true;
    }

    protected abstract AlgorithmDescriptor getAlgorithmDescriptor();

    @Override
    public Class<? extends IData> getInputDataType(String identifier) {
        return getAlgorithmDescriptor().getInputDescriptor(identifier).getBinding();
    }

    @Override
    public Class<? extends IData> getOutputDataType(String identifier) {
        return getAlgorithmDescriptor().getOutputDescriptor(identifier).getBinding();
    }

    private List observers = new ArrayList();
    private Object state = null;

    @Override
    public Object getState() {
        return state;
    }

    @Override
    public void update(Object state) {
        this.state = state;
        notifyObservers();
    }

    @Override
    public void addObserver(IObserver o) {
        observers.add(o);
    }

    @Override
    public void removeObserver(IObserver o) {
        observers.remove(o);
    }

    public void notifyObservers() {
        Iterator i = observers.iterator();
        while (i.hasNext()) {
            IObserver o = (IObserver) i.next();
            o.update(this);
        }
    }

    @Override
    public List<String> getErrors() {
        List<String> errors = new ArrayList();
        return errors;
    }
}
