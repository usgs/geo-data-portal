package gov.usgs.cida.gdp.wps.algorithm;

import org.n52.wps.io.data.IData;

/**
 *
 * @author tkunicki
 */
public abstract class OutputDescriptor<T extends Class<? extends IData>> extends BoundDescriptor<T> {

	OutputDescriptor(Builder<? extends Builder<?,T>, T> builder) {
        super(builder);
    }

    public static abstract class Builder<B extends Builder<B,T>, T extends Class<? extends IData>> extends BoundDescriptor.Builder<B,T>{

        protected Builder(T binding) {
            super(binding);
        }

    }
    
}
