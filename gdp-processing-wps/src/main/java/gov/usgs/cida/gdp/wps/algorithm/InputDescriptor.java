package gov.usgs.cida.gdp.wps.algorithm;

import java.math.BigInteger;
import org.n52.wps.io.data.IData;

/**
 *
 * @author tkunicki
 */
public abstract class InputDescriptor<T extends Class<? extends IData>> {

    private final T binding;
	private final BigInteger minOccurs;
	private final BigInteger maxOccurs;
    private final String abstrakt; // want 'abstract' but it's a java keyword

	InputDescriptor(Builder<? extends Builder<?,T>, T> builder) {
		this.binding = builder.binding;
		this.minOccurs = builder.minOccurs;
		this.maxOccurs = builder.maxOccurs;
        this.abstrakt = builder.abstrakt;
    }

    public T getBinding() {
        return binding;
    }

    public BigInteger getMinOccurs() {
        return minOccurs;
    }

    public BigInteger getMaxOccurs() {
        return maxOccurs;
    }

    public String getAbstract() {
        return abstrakt;
    }

    static abstract class Builder<B extends Builder<B,T>, T extends Class<? extends IData>> {

        private final T binding;
        private BigInteger minOccurs = BigInteger.ONE;
        private BigInteger maxOccurs = BigInteger.ONE;
        private String abstrakt; // want 'abstract' but it's a java keyword

        Builder(T binding) {
            this.binding = binding;
        }

        public B minOccurs(int minOccurs) {
            return minOccurs(BigInteger.valueOf(minOccurs));
        }

        public B minOccurs(BigInteger minOccurs) {
            this.minOccurs = minOccurs;
            return self();
        }

        public B maxOccurs(int maxOccurs) {
            return maxOccurs(BigInteger.valueOf(maxOccurs));
        }

        public B maxOccurs(BigInteger maxOccurs) {
            this.maxOccurs = maxOccurs;
            return self();
        }

        // want 'abstract' but it's a java keyword
        B abstrakt(String abstrakt) {
            this.abstrakt = abstrakt;
            return self();
        }

        protected abstract B self();

        public abstract InputDescriptor<T> build();
    }
    
}
