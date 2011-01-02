package gov.usgs.cida.gdp.wps.algorithm.descriptor;

import java.math.BigInteger;
import org.n52.wps.io.data.IComplexData;

/**
 *
 * @author tkunicki
 */
public class ComplexDataInputDescriptor<T extends Class<? extends IComplexData>> extends InputDescriptor<T> {

    private final BigInteger maximumMegaBytes;

	private ComplexDataInputDescriptor(Builder builder) {
        super(builder);
		this.maximumMegaBytes = builder.maximumMegaBytes;
    }

    public BigInteger getMaximumMegaBytes() {
        return maximumMegaBytes;
    }
    
    public static <T extends Class<? extends IComplexData>> Builder<?,T> builder(T binding, String identifier) {
        return new BuilderTyped(binding, identifier);
    }

    private static class BuilderTyped<T extends Class<? extends IComplexData>> extends Builder<BuilderTyped<T>, T> {
        public BuilderTyped(T binding, String identifier) {
            super(binding, identifier);
        }
        @Override
        protected BuilderTyped self() {
            return this;
        }
    }

    public static abstract class Builder<B extends Builder<B,T>, T extends Class<? extends IComplexData>> extends InputDescriptor.Builder<B,T> {

        private BigInteger maximumMegaBytes;
        
        private Builder(T binding, String identifier) {
            super(binding, identifier);
        }

        public B maximumMegaBytes(int maximumMegaBytes) {
            return maximumMegaBytes(BigInteger.valueOf(maximumMegaBytes));
        }
        
        public B maximumMegaBytes(BigInteger maximumMegaBytes) {
            this.maximumMegaBytes = maximumMegaBytes;
            return self();
        }

        @Override
        public ComplexDataInputDescriptor<T> build() {
            return new ComplexDataInputDescriptor<T>(this);
        }
    }

}
