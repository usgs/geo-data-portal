package gov.usgs.cida.gdp.wps.algorithm;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import net.opengis.ows.x11.AllowedValuesDocument.AllowedValues;
import net.opengis.ows.x11.DomainMetadataType;
import net.opengis.wps.x100.ComplexDataCombinationType;
import net.opengis.wps.x100.ComplexDataCombinationsType;
import net.opengis.wps.x100.ComplexDataDescriptionType;
import net.opengis.wps.x100.InputDescriptionType;
import net.opengis.wps.x100.LiteralInputType;
import net.opengis.wps.x100.LiteralOutputType;
import net.opengis.wps.x100.OutputDescriptionType;
import net.opengis.wps.x100.ProcessDescriptionType;
import net.opengis.wps.x100.ProcessDescriptionsDocument;
import net.opengis.wps.x100.SupportedComplexDataInputType;
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
import org.n52.wps.io.data.IComplexData;
import org.n52.wps.io.data.ILiteralData;
import org.n52.wps.server.AbstractAlgorithm;
import org.n52.wps.server.observerpattern.IObserver;
import org.n52.wps.server.observerpattern.ISubject;

public abstract class AbstractSelfDescribingAlgorithm extends AbstractAlgorithm implements ISubject {

    public AbstractSelfDescribingAlgorithm() {
        super();
    }

    public AbstractSelfDescribingAlgorithm(String wellKnownName) {
        super(wellKnownName);
    }

    @Override
    protected ProcessDescriptionType initializeDescription() {

        ProcessDescriptionsDocument document = ProcessDescriptionsDocument.Factory.newInstance();
        ProcessDescriptions processDescriptions = document.addNewProcessDescriptions();
        ProcessDescriptionType processDescription = processDescriptions.addNewProcessDescription();
        processDescription.setStatusSupported(true);
        processDescription.setStoreSupported(true);
        processDescription.setProcessVersion("1.0.0");

        // 1. Identifer
        String wellKnownName = getWellKnownName();
        processDescription.addNewIdentifier().setStringValue(wellKnownName == null ? getClass().getCanonicalName() : wellKnownName);
        processDescription.addNewTitle().setStringValue(wellKnownName == null ? getClass().getCanonicalName() : wellKnownName);

        // 2. Inputs
        Map<String, InputDescriptor> identifiers = getInputDescriptorMap();
        DataInputs dataInputs = null;
        if (identifiers.size() > 0) {
            dataInputs = processDescription.addNewDataInputs();
        }

        for (String identifier : identifiers.keySet()) {
            InputDescriptor descriptor = identifiers.get(identifier);

            InputDescriptionType dataInput = dataInputs.addNewInput();
            dataInput.setMinOccurs(descriptor.getMinOccurs());
            dataInput.setMaxOccurs(descriptor.getMaxOccurs());
            dataInput.addNewIdentifier().setStringValue(identifier);
            dataInput.addNewTitle().setStringValue(identifier);
            dataInput.addNewAbstract().setStringValue(identifier);

            if (descriptor instanceof LiteralDataInputDescriptor) {
                LiteralDataInputDescriptor literalDescriptor = (LiteralDataInputDescriptor)descriptor;

                LiteralInputType literalData = dataInput.addNewLiteralData();
                DomainMetadataType datatype = literalData.addNewDataType();
                datatype.setReference(literalDescriptor.getSchemaType());

                Object defaultValue = literalDescriptor.getDefaultValue();
                if (defaultValue != null) {
                    literalData.setDefaultValue(defaultValue.toString());
                }
                List<? extends Object> allowedValues = literalDescriptor.getAllowedValues();
                if (allowedValues != null && allowedValues.size() > 0) {
                    AllowedValues allowed = literalData.addNewAllowedValues();
                    for (Object allow : allowedValues) {
                        allowed.addNewValue().setStringValue(allow.toString());
                    }
                } else {
                    literalData.addNewAnyValue();
                }

            } else if (descriptor instanceof ComplexDataInputDescriptor) {
                SupportedComplexDataInputType complexData = dataInput.addNewComplexData();
                describeComplexDataInput(complexData, descriptor.getBinding());
            }
        }

        //3. Outputs
        ProcessOutputs dataOutputs = processDescription.addNewProcessOutputs();
        Map<String, Class> outputIdentifiers = getOutputDescriptorMap();
        for (String identifier : outputIdentifiers.keySet()) {
            OutputDescriptionType dataOutput = dataOutputs.addNewOutput();

            dataOutput.addNewIdentifier().setStringValue(identifier);
            dataOutput.addNewTitle().setStringValue(identifier);
            dataOutput.addNewAbstract().setStringValue(identifier);

            Class outputDataTypeClass = this.getOutputDataType(identifier);

            if (ILiteralData.class.isAssignableFrom(outputDataTypeClass)) {

                LiteralOutputType literalData = dataOutput.addNewLiteralOutput();
                String outputClassType = "";

                Constructor[] constructors = outputDataTypeClass.getConstructors();
                for (Constructor constructor : constructors) {
                    Class[] parameters = constructor.getParameterTypes();
                    if (parameters.length == 1) {
                        outputClassType = parameters[0].getSimpleName();
                    }
                }
                if (outputClassType.length() > 0) {
                    literalData.addNewDataType().setReference("xs:" + outputClassType.toLowerCase());
                }
            } else if (IComplexData.class.isAssignableFrom(outputDataTypeClass)) {
                SupportedComplexDataType complexData = dataOutput.addNewComplexOutput();
                describeComplexDataOutput(complexData, outputDataTypeClass);
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
                System.out.println("\tMessage: " + error.getMessage());
                System.out.println("\tLocation of invalid XML: " +
                     error.getCursorLocation().xmlText());
            }
        }
        return true;
    }

    protected abstract Map<String, InputDescriptor> getInputDescriptorMap();

    protected abstract Map<String, Class> getOutputDescriptorMap();

    @Override
    public Class getInputDataType(String string) {
        return getInputDescriptorMap().get(string).getBinding();
    }

    @Override
    public Class getOutputDataType(String string) {
        return getOutputDescriptorMap().get(string);
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
