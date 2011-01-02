package gov.usgs.cida.gdp.wps.algorithm.descriptor;

import org.n52.wps.io.data.ILiteralData;
import org.n52.wps.io.data.binding.literal.LiteralAnyURIBinding;
import org.n52.wps.io.data.binding.literal.LiteralBase64BinaryBinding;
import org.n52.wps.io.data.binding.literal.LiteralBooleanBinding;
import org.n52.wps.io.data.binding.literal.LiteralByteBinding;
import org.n52.wps.io.data.binding.literal.LiteralDateTimeBinding;
import org.n52.wps.io.data.binding.literal.LiteralDoubleBinding;
import org.n52.wps.io.data.binding.literal.LiteralFloatBinding;
import org.n52.wps.io.data.binding.literal.LiteralIntBinding;
import org.n52.wps.io.data.binding.literal.LiteralShortBinding;
import org.n52.wps.io.data.binding.literal.LiteralStringBinding;

/**
 *
 * @author tkunicki
 */
public class LiteralDataOutputDescriptor<T extends Class<? extends ILiteralData>> extends OutputDescriptor<T> {

    private final String dataType;

	protected LiteralDataOutputDescriptor(Builder builder) {
		super(builder);
        this.dataType = builder.dataType;
	}

    public String getDataType() {
        return dataType;
    }

    private static <T extends Class<? extends ILiteralData>> Builder<?,T> builder(T binding, String identifier, String schemaType) {
        return new BuilderTyped(binding, identifier, schemaType);
    }

    // utility functions, quite verbose...  should have some factory method somewhere to
    // match binding class and schema type.  see analot in LiteralDataOutputDescriptor
    public static Builder<?,Class<LiteralAnyURIBinding>> anyURIBuilder(String identifier) {
        return builder(LiteralAnyURIBinding.class, identifier, "xs:anyURI");
    }

    public static Builder<?,Class<LiteralBase64BinaryBinding>> base64BinaryBuilder(String identifier) {
        return builder(LiteralBase64BinaryBinding.class, identifier, "xs:base64Binary");
    }

    public static Builder<?,Class<LiteralBooleanBinding>> booleanBuilder(String identifier) {
        return builder(LiteralBooleanBinding.class, identifier, "xs:boolean");
    }

    public static Builder<?,Class<LiteralByteBinding>> byteBuilder(String identifier) {
        return builder(LiteralByteBinding.class, identifier, "xs:byte");
    }

    public static Builder<?,Class<LiteralDateTimeBinding>> dateBuilder(String identifier) {
        return builder(LiteralDateTimeBinding.class, identifier, "xs:date");
    }

    public static Builder<?,Class<LiteralDateTimeBinding>> timeBuilder(String identifier) {
        return builder(LiteralDateTimeBinding.class, identifier, "xs:time");
    }

    public static Builder<?,Class<LiteralDateTimeBinding>> dateTimeBuilder(String identifier) {
        return builder(LiteralDateTimeBinding.class, identifier, "xs:dateTime");
    }

    public static Builder<?,Class<LiteralDateTimeBinding>> durationBuilder(String identifier) {
        return builder(LiteralDateTimeBinding.class, identifier, "xs:duration");
    }

    public static Builder<?,Class<LiteralDoubleBinding>> doubleBuilder(String identifier) {
        return builder(LiteralDoubleBinding.class, identifier, "xs:double");
    }

    public static Builder<?,Class<LiteralFloatBinding>> floatBuilder(String identifier) {
        return builder(LiteralFloatBinding.class, identifier, "xs:float");
    }

    public static Builder<?,Class<LiteralIntBinding>> intBuilder(String identifier) {
        return builder(LiteralIntBinding.class, identifier, "xs:int");
    }

    public static Builder<?,Class<LiteralShortBinding>> shortBuilder(String identifier) {
        return builder(LiteralShortBinding.class, identifier, "xs:short");
    }

    public static Builder<?,Class<LiteralStringBinding>> stringBuilder(String identifier) {
        return builder(LiteralStringBinding.class, identifier, "xs:string");
    }

    private static class BuilderTyped<T extends Class<? extends ILiteralData>> extends Builder<BuilderTyped<T>, T> {
        public BuilderTyped(T binding, String identifier, String schemaType) {
            super(binding, identifier, schemaType);
        }
        @Override
        protected BuilderTyped self() {
            return this;
        }
    }

    public static abstract class Builder<B extends Builder<B,T>, T extends Class<? extends ILiteralData>> extends OutputDescriptor.Builder<B,T> {

        private final String dataType;

        protected Builder(T binding, String identifier, String dataType) {
            super(binding, identifier);
            this.dataType = dataType;
        }

        @Override
        public LiteralDataOutputDescriptor<T> build() {
            return new LiteralDataOutputDescriptor<T>(this);
        }
    }

}
