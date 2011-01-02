package gov.usgs.cida.gdp.wps.algorithm;

import java.math.BigInteger;
import org.n52.wps.io.data.IData;

/**
 *
 * @author tkunicki
 */
public abstract class InputDescriptor<T extends Class<? extends IData>> extends BoundDescriptor<T> {

	private final BigInteger minOccurs;
	private final BigInteger maxOccurs;

	protected InputDescriptor(Builder<? extends Builder<?,T>, T> builder) {
        super(builder);
		this.minOccurs = builder.minOccurs;
		this.maxOccurs = builder.maxOccurs;
    }

    public BigInteger getMinOccurs() {
        return minOccurs;
    }

    public BigInteger getMaxOccurs() {
        return maxOccurs;
    }

    public static abstract class Builder<B extends Builder<B,T>, T extends Class<? extends IData>> extends BoundDescriptor.Builder<B,T>{

        private BigInteger minOccurs = BigInteger.ONE;
        private BigInteger maxOccurs = BigInteger.ONE;

        protected Builder(T binding) {
            super(binding);
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
    }
    
}
