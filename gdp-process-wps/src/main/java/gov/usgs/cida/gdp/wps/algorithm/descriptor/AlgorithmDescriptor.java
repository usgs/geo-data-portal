package gov.usgs.cida.gdp.wps.algorithm.descriptor;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author tkunicki
 */
public class AlgorithmDescriptor extends Descriptor {

    private final String version;
    private final boolean storeSupported;
    private final boolean statusSupported;
    private final Map<String, InputDescriptor> inputDescriptorMap;
    private final Map<String, OutputDescriptor> outputDescriptorMap;

	AlgorithmDescriptor(Builder<? extends Builder<?>> builder) {
        super(builder);
        this.version = builder.version;
        this.storeSupported = builder.storeSupported;
        this.statusSupported = builder.statusSupported;

        // LinkedHaskMap to preserve order
        Map<String, InputDescriptor> iMap = new LinkedHashMap<String, InputDescriptor>();
        for (InputDescriptor iDescriptor : builder.inputDescriptors) {
            iMap.put(iDescriptor.getIdentifier(), iDescriptor);
        }
        inputDescriptorMap = Collections.unmodifiableMap(iMap);

        Map<String, OutputDescriptor> oMap = new LinkedHashMap<String, OutputDescriptor>();
        for (OutputDescriptor oDescriptor : builder.outputDescriptors) {
            oMap.put(oDescriptor.getIdentifier(), oDescriptor);
        }
        outputDescriptorMap = Collections.unmodifiableMap(oMap);
    }

    public String getVersion() {
        return version;
    }

    public boolean getStoreSupported() {
        return storeSupported;
    }

    public boolean getStatusSupported() {
        return statusSupported;
    }
    
    public InputDescriptor getInputDescriptor(String identifier) {
        return inputDescriptorMap.get(identifier);
    }

    public Collection<InputDescriptor> getInputDescriptors() {
        return inputDescriptorMap.values();
    }

    public OutputDescriptor getOutputDescriptor(String identifier) {
        return outputDescriptorMap.get(identifier);
    }

    public Collection<OutputDescriptor> getOutputDescriptors() {
        return outputDescriptorMap.values();
    }

    public static Builder<?> builder(String identifier) {
        return new BuilderTyped(identifier);
    }

    public static Builder<?> builder(Class<?> clazz) {
        return new BuilderTyped(clazz.getCanonicalName());
    }

    private static class BuilderTyped extends Builder<BuilderTyped> {
        public BuilderTyped(String identifier) {
            super(identifier);
        }
        @Override
        protected BuilderTyped self() {
            return this;
        }
    }

    public static abstract class Builder<B extends Builder<B>> extends Descriptor.Builder<B>{

        private String version = "1.0.0";
        private boolean storeSupported = true;
        private boolean statusSupported = true;
        private List<InputDescriptor> inputDescriptors;
        private List<OutputDescriptor> outputDescriptors;

        protected Builder(String identifier) {
            super();
            identifier(identifier);
            title(identifier);
            inputDescriptors = new ArrayList<InputDescriptor>();
            outputDescriptors = new ArrayList<OutputDescriptor>();
        }

        public B version(String version) {
            this.version = version;
            return self();
        }

        public B storeSupported(boolean storeSupported) {
            this.storeSupported = storeSupported;
            return self();
        }

        public B statusSupported(boolean statusSupported) {
            this.statusSupported = statusSupported;
            return self();
        }

        public B addInputDesciptor(InputDescriptor.Builder inputDescriptorBuilder) {
            return addInputDesciptor(inputDescriptorBuilder.build());
        }

        public B addInputDesciptor(InputDescriptor inputDescriptor) {
            this.inputDescriptors.add(inputDescriptor);
            return self();
        }

        public B addInputDesciptors(List<? extends InputDescriptor> inputDescriptors) {
            this.inputDescriptors.addAll(inputDescriptors);
            return self();
        }

        public B addOutputDesciptor(OutputDescriptor.Builder outputDescriptorBuilder) {
            return addOutputDesciptor(outputDescriptorBuilder.build());
        }

        public B addOutputDesciptor(OutputDescriptor outputDescriptor) {
            this.outputDescriptors.add(outputDescriptor);
            return self();
        }

        public B addOutputDesciptors(List<? extends OutputDescriptor> outputDescriptors) {
            this.outputDescriptors.addAll(outputDescriptors);
            return self();
        }
        
        public AlgorithmDescriptor build() {
            return new AlgorithmDescriptor(this);
        }

    }
    
}
