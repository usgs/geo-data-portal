package gov.usgs.cida.gdp.wps.algorithm;

import org.n52.wps.io.data.IComplexData;

/**
 *
 * @author tkunicki
 */
public class ComplexDataOutputDescriptor<T extends Class<? extends IComplexData>> extends OutputDescriptor<T> {


	private ComplexDataOutputDescriptor(Builder builder) {
        super(builder);
    }
    
    public static <T extends Class<? extends IComplexData>> Builder<?,T> builder(T binding) {
        return new BuilderTyped(binding);
    }

    private static class BuilderTyped<T extends Class<? extends IComplexData>> extends Builder<BuilderTyped<T>, T> {
        public BuilderTyped(T binding) {
            super(binding);
        }
        @Override
        protected BuilderTyped self() {
            return this;
        }
    }

    public static abstract class Builder<B extends Builder<B,T>, T extends Class<? extends IComplexData>> extends OutputDescriptor.Builder<B,T> {
        
        private Builder(T binding) {
            super(binding);
        }

        @Override
        public ComplexDataOutputDescriptor<T> build() {
            return new ComplexDataOutputDescriptor<T>(this);
        }
    }

}
