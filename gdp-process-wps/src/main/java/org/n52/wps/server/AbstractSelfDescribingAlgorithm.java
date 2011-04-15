package org.n52.wps.server;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
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
import net.opengis.wps.x100.SupportedComplexDataType;
import net.opengis.wps.x100.ProcessDescriptionType.DataInputs;
import net.opengis.wps.x100.ProcessDescriptionType.ProcessOutputs;
import net.opengis.wps.x100.ProcessDescriptionsDocument.ProcessDescriptions;
import net.opengis.wps.x100.SupportedComplexDataInputType;
import org.apache.xmlbeans.XmlOptions;
import org.apache.xmlbeans.XmlValidationError;
import org.n52.wps.io.GeneratorFactory;
import org.n52.wps.io.IGenerator;
import org.n52.wps.io.IOHandler;
import org.n52.wps.io.IParser;
import org.n52.wps.io.ParserFactory;
import org.n52.wps.io.data.IComplexData;
import org.n52.wps.io.data.IData;
import org.n52.wps.io.data.ILiteralData;
import org.n52.wps.algorithm.descriptor.AlgorithmDescriptor;
import org.n52.wps.algorithm.descriptor.ComplexDataInputDescriptor;
import org.n52.wps.algorithm.descriptor.ComplexDataOutputDescriptor;
import org.n52.wps.algorithm.descriptor.InputDescriptor;
import org.n52.wps.algorithm.descriptor.LiteralDataInputDescriptor;
import org.n52.wps.algorithm.descriptor.LiteralDataOutputDescriptor;
import org.n52.wps.algorithm.descriptor.OutputDescriptor;
import org.n52.wps.server.observerpattern.IObserver;
import org.n52.wps.server.observerpattern.ISubject;
import org.n52.wps.util.BasicXMLTypeFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractSelfDescribingAlgorithm extends AbstractAlgorithm implements ISubject {

    public final static Logger LOGGER = LoggerFactory.getLogger(AbstractSelfDescribingAlgorithm.class);
    
    public AbstractSelfDescribingAlgorithm() {
        super();
    }

    @Override
    protected ProcessDescriptionType initializeDescription() {

        AlgorithmDescriptor algorithmDescriptor = getAlgorithmDescriptor();

        ProcessDescriptionsDocument document = ProcessDescriptionsDocument.Factory.newInstance();
        ProcessDescriptions processDescriptions = document.addNewProcessDescriptions();
        ProcessDescriptionType processDescription = processDescriptions.addNewProcessDescription();

        if (algorithmDescriptor == null) {

            // Old, here for backwards compatibility

            // 1) Identifier
            processDescription.setStatusSupported(true);
            processDescription.setStoreSupported(true);
            processDescription.setProcessVersion("1.0.0");
            processDescription.addNewIdentifier().setStringValue(this.getClass().getName());
            processDescription.addNewTitle().setStringValue(this.getClass().getCanonicalName());

            // 2. Inputs
            List<String> inputIdentifiers = this.getInputIdentifiers();
            DataInputs dataInputs = null;
            if(inputIdentifiers.size()>0){
                dataInputs = processDescription.addNewDataInputs();
            }
            for (String identifier : inputIdentifiers) {

                InputDescriptionType dataInput = dataInputs.addNewInput();
                dataInput.setMinOccurs(getMinOccurs(identifier));
                dataInput.setMaxOccurs(getMaxOccurs(identifier));
                dataInput.addNewIdentifier().setStringValue(identifier);
                dataInput.addNewTitle().setStringValue(identifier);

                Class<? extends IData> inputDataTypeClass = getInputDataType(identifier);
                if (ILiteralData.class.isAssignableFrom(inputDataTypeClass)) {
                    LiteralInputType literalData = dataInput.addNewLiteralData();
                    String xmlDataType = BasicXMLTypeFactory.getXMLDataTypeforBinding((Class<? extends ILiteralData>)inputDataTypeClass);
                    DomainMetadataType dataType = literalData.addNewDataType();
                    dataType.setReference(xmlDataType);
                    literalData.addNewAnyValue();
                 } else if (IComplexData.class.isAssignableFrom(inputDataTypeClass)){
                    SupportedComplexDataInputType complexData = dataInput.addNewComplexData();
                    describeComplexDataInputType(complexData, inputDataTypeClass);
                }
            }

            // 3. Outputs
            ProcessOutputs dataOutputs = processDescription.addNewProcessOutputs();
            List<String> outputIdentifiers = getOutputIdentifiers();
            for (String identifier : outputIdentifiers) {

                OutputDescriptionType dataOutput = dataOutputs.addNewOutput();
                dataOutput.addNewIdentifier().setStringValue(identifier);
                dataOutput.addNewTitle().setStringValue(identifier);
                dataOutput.addNewAbstract().setStringValue(identifier);

                Class<? extends IData> ouputDataTypeClass = getOutputDataType(identifier);
                if (ILiteralData.class.isAssignableFrom(ouputDataTypeClass)) {
                    LiteralOutputType literalData = dataOutput.addNewLiteralOutput();
                    String xmlDataType = BasicXMLTypeFactory.getXMLDataTypeforBinding((Class<? extends ILiteralData>)ouputDataTypeClass);
                    DomainMetadataType dataType = literalData.addNewDataType();
                    dataType.setReference(xmlDataType);
                } else if (IComplexData.class.isAssignableFrom(ouputDataTypeClass)) {
                    SupportedComplexDataType complexData = dataOutput.addNewComplexOutput();
                    describeComplexDataOutputType(complexData, ouputDataTypeClass);
                }
            }

        } else {

            // New, AlgorithmDescriptor based implementation

            // 1. Identifier
            processDescription.setStatusSupported(algorithmDescriptor.getStatusSupported());
            processDescription.setStoreSupported(algorithmDescriptor.getStoreSupported());
            processDescription.setProcessVersion(algorithmDescriptor.getVersion());
            processDescription.addNewIdentifier().setStringValue(algorithmDescriptor.getIdentifier());
            processDescription.addNewTitle().setStringValue( algorithmDescriptor.hasTitle() ?
                    algorithmDescriptor.getTitle() :
                    algorithmDescriptor.getIdentifier());
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
                    LiteralDataInputDescriptor<?> literalDescriptor = (LiteralDataInputDescriptor)inputDescriptor;

                    LiteralInputType literalData = dataInput.addNewLiteralData();
                    literalData.addNewDataType().setReference(literalDescriptor.getDataType());

                    if (literalDescriptor.hasDefaultValue()) {
                        literalData.setDefaultValue(literalDescriptor.getDefaultValue());
                    }
                    if (literalDescriptor.hasAllowedValues()) {
                        AllowedValues allowed = literalData.addNewAllowedValues();
                        for (String allowedValue : literalDescriptor.getAllowedValues()) {
                            allowed.addNewValue().setStringValue(allowedValue);
                        }
                    } else {
                        literalData.addNewAnyValue();
                    }

                } else if (inputDescriptor instanceof ComplexDataInputDescriptor) {
                    SupportedComplexDataInputType complexDataType = dataInput.addNewComplexData();
                    ComplexDataInputDescriptor complexInputDescriptor =
                            (ComplexDataInputDescriptor)inputDescriptor;
                    if (complexInputDescriptor.hasMaximumMegaBytes()) {
                        complexDataType.setMaximumMegabytes(complexInputDescriptor.getMaximumMegaBytes());
                    }
                    describeComplexDataInputType(complexDataType, inputDescriptor.getBinding());
                }
            }

            // 3. Outputs
            ProcessOutputs dataOutputs = processDescription.addNewProcessOutputs();
            Collection<OutputDescriptor> outputDescriptors = algorithmDescriptor.getOutputDescriptors();
            if (outputDescriptors.size() < 1) {
               // TODO:  Log?  Exception?
            }
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
                    LiteralDataOutputDescriptor<?> literalDescriptor = (LiteralDataOutputDescriptor)outputDescriptor;
                    dataOutput.addNewLiteralOutput().addNewDataType().
                            setReference(literalDescriptor.getDataType());
                } else if (outputDescriptor instanceof ComplexDataOutputDescriptor) {
                    describeComplexDataOutputType(dataOutput.addNewComplexOutput(), outputDescriptor.getBinding());
               }
            }
        }
        return document.getProcessDescriptions().getProcessDescriptionArray(0);
    }

    private void describeComplexDataInputType(SupportedComplexDataType complexData, Class dataTypeClass) {
        List<IParser> parsers = ParserFactory.getInstance().getAllParsers();
        List<IParser> foundParsers = new ArrayList<IParser>();
        for (IParser parser : parsers) {
            Class[] supportedClasses = parser.getSupportedInternalOutputDataType();
            for (Class clazz : supportedClasses) {
                if (dataTypeClass.isAssignableFrom(clazz)) {
                    foundParsers.add(parser);
                }
            }
        }
        describeComplexDataType(complexData, foundParsers);
    }

    private void describeComplexDataOutputType(SupportedComplexDataType complexData, Class dataTypeClass) {

        List<IGenerator> generators = GeneratorFactory.getInstance().getAllGenerators();
        List<IGenerator> foundGenerators = new ArrayList<IGenerator>();
        for (IGenerator generator : generators) {
            Class[] supportedClasses = generator.getSupportedInternalInputDataType();
            for (Class clazz : supportedClasses) {
                if (clazz.isAssignableFrom(dataTypeClass)) {
                    foundGenerators.add(generator);
                }
            }
        }
        describeComplexDataType(complexData, foundGenerators);
    }

    private void describeComplexDataType(
            SupportedComplexDataType complexData,
            List<? extends IOHandler> handlers)
    {
        ComplexDataCombinationType defaultFormatType = complexData.addNewDefault();
        ComplexDataCombinationsType supportedFormatType = complexData.addNewSupported();

        int formatCount = 0;
        for (IOHandler generator : handlers) {

            String[] formats = generator.getSupportedFormats();
            String[] encodings = generator.getSupportedEncodings();
            String[] schemas = generator.getSupportedSchemas();

            // if formats, encodings or schemas arrays are 'null' or empty, create
            // new array with single 'null' element.  We do this so we can utilize
            // a single set of nested loops to process all permutations.  'null'
            // values will not be output...
            if (formats == null || formats.length == 0) {
                formats = new String[] { null }; 
            }
            if (encodings == null || encodings.length == 0) {
                encodings = new String[] { null };
            }
            if (schemas == null || schemas.length == 0) {
                schemas = new String[] { null };
            }
            
            for (String format : formats) {
                for (String encoding : encodings) {
                    for (String schema : schemas) {
                        if(formatCount++ == 0) {
                            describeComplexDataFormat(
                                    defaultFormatType.addNewFormat(),
                                    format, encoding, schema);
                        }
                        describeComplexDataFormat(
                                supportedFormatType.addNewFormat(),
                                format, encoding, schema);
                    }
                }
            }
        }
    }
    
    public void describeComplexDataFormat(
            ComplexDataDescriptionType description,
            String format,
            String encoding,
            String schema)
    {
        if (format != null) description.setMimeType(format);
        if (encoding != null) description.setEncoding(encoding);
        if (schema != null) description.setSchema(schema);
    }

    @Override
    public boolean processDescriptionIsValid() {
        XmlOptions xmlOptions = new XmlOptions();
        List<XmlValidationError> xmlValidationErrorList = new ArrayList<XmlValidationError>();
            xmlOptions.setErrorListener(xmlValidationErrorList);
        boolean valid = getDescription().validate(xmlOptions);
        if (!valid) {
            LOGGER.error("Error validating process description for " + getClass().getCanonicalName());
            for (XmlValidationError xmlValidationError : xmlValidationErrorList) {
                LOGGER.error("\tMessage: {}", xmlValidationError.getMessage());
                LOGGER.error("\tLocation of invalid XML: {}",
                     xmlValidationError.getCursorLocation().xmlText());
            }
        }
        return valid;
    }

    protected AlgorithmDescriptor getAlgorithmDescriptor() {
        return null;
    }

    @Deprecated
    public List<String> getInputIdentifiers() {
        AlgorithmDescriptor algorithmDescriptor = getAlgorithmDescriptor();
        if (algorithmDescriptor != null) {
            return algorithmDescriptor.getInputIdentifiers();
        } else {
            throw new RuntimeException("Subclasses of AbstractSelfDescribingAlgorithm must override getInputIdentifiers() or implement getAlgorithmDescriptor()");
        }
    }

    @Deprecated
	public List<String> getOutputIdentifiers() {
        AlgorithmDescriptor algorithmDescriptor = getAlgorithmDescriptor();
        if (algorithmDescriptor != null) {
            return algorithmDescriptor.getOutputIdentifiers();
        } else {
            throw new RuntimeException("Subclasses of AbstractSelfDescribingAlgorithm must override getOutputIdentifiers() or implement getAlgorithmDescriptor()");
        }
    }

    @Deprecated
    public BigInteger getMinOccurs(String identifier){
		AlgorithmDescriptor algorithmDescriptor = getAlgorithmDescriptor();
        if (algorithmDescriptor != null) {
            return algorithmDescriptor.getInputDescriptor(identifier).getMinOccurs();
        } else {
            return BigInteger.valueOf(1);
        }
	}

    @Deprecated
	public BigInteger getMaxOccurs(String identifier){
		AlgorithmDescriptor algorithmDescriptor = getAlgorithmDescriptor();
        if (algorithmDescriptor != null) {
            return algorithmDescriptor.getInputDescriptor(identifier).getMaxOccurs();
        } else {
            return BigInteger.valueOf(1);
        }
	}

    @Override
    public Class<? extends IData> getInputDataType(String identifier) {
        AlgorithmDescriptor algorithmDescriptor = getAlgorithmDescriptor();
        if (algorithmDescriptor != null) {
            return getAlgorithmDescriptor().getInputDescriptor(identifier).getBinding();
        } else {
            throw new RuntimeException("Subclasses of AbstractSelfDescribingAlgorithm must override getInputDataType(...) or implement getAlgorithmDescriptor()");
        }
    }

    @Override
    public Class<? extends IData> getOutputDataType(String identifier) {
        AlgorithmDescriptor algorithmDescriptor = getAlgorithmDescriptor();
        if (algorithmDescriptor != null) {
            return getAlgorithmDescriptor().getOutputDescriptor(identifier).getBinding();
        } else {
            throw new RuntimeException("Subclasses of AbstractSelfDescribingAlgorithm must override getOutputDataType(...) or implement getAlgorithmDescriptor()");
        }
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

    List<String> errorList = new ArrayList();
    protected List<String> addError(String error) {
        errorList.add(error);
        return errorList;
    }

    @Override
    public List<String> getErrors() {
        return errorList;
    }
}
