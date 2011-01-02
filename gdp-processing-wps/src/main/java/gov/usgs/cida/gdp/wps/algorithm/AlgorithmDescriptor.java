package gov.usgs.cida.gdp.wps.algorithm;

import org.n52.wps.server.IAlgorithm;

/**
 *
 * @author tkunicki
 */
public class AlgorithmDescriptor<T extends Class<? extends IAlgorithm>> extends BoundDescriptor<T> {

    private final String version;
    private final boolean storeSupported;
    private final boolean statusSupported;

	AlgorithmDescriptor(Builder<? extends Builder<?,T>, T> builder) {
        super(builder);
        this.version = builder.version;
        this.storeSupported = builder.storeSupported;
        this.statusSupported = builder.statusSupported;
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

    public static <T extends Class<? extends IAlgorithm>> Builder<?,T> builder(T binding) {
        return new BuilderTyped(binding);
    }

    private static class BuilderTyped<T extends Class<? extends IAlgorithm>> extends Builder<BuilderTyped<T>, T> {
        public BuilderTyped(T binding) {
            super(binding);
        }
        @Override
        protected BuilderTyped self() {
            return this;
        }
    }

    public static abstract class Builder<B extends Builder<B,T>, T extends Class<? extends IAlgorithm>> extends BoundDescriptor.Builder<B,T>{

        private String version;
        private boolean storeSupported;
        private boolean statusSupported;

        protected Builder(T binding) {
            super(binding);
            identifier(binding.getCanonicalName());
            title(binding.getCanonicalName());
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
        
        @Override
        public AlgorithmDescriptor<T> build() {
            return new AlgorithmDescriptor<T>(this);
        }

    }
    
}
