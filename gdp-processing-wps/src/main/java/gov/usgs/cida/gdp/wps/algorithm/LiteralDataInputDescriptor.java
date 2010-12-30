package gov.usgs.cida.gdp.wps.algorithm;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
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
public class LiteralDataInputDescriptor<T extends Class<? extends ILiteralData>> extends InputDescriptor<T> {

    private final String schemaType;
    private final Object defaultValue;
    private final List<? extends Object> allowedValues;

	protected LiteralDataInputDescriptor(Builder builder) {
		super(builder);
        this.schemaType = builder.schemaType;
		this.defaultValue = builder.defaultValue;
		this.allowedValues = builder.allowedValues != null ?
            Collections.unmodifiableList(builder.allowedValues) :
            null;
	}

    public String getSchemaType() {
        return schemaType;
    }

    public Object getDefaultValue() {
        return this.defaultValue;
    }

    public List<? extends Object> getAllowedValues() {
        return this.allowedValues;
    }

    private static <T extends Class<? extends ILiteralData>> Builder<?,T> builder(T binding, String schemaType) {
        return new BuilderTyped(binding, schemaType);
    }
    
    public static Builder<?,Class<LiteralAnyURIBinding>> anyURIBuilder() {
        return builder(LiteralAnyURIBinding.class, "xs:anyURI");
    }

    public static Builder<?,Class<LiteralBase64BinaryBinding>> base64BinaryBuilder() {
        return builder(LiteralBase64BinaryBinding.class, "xs:base64Binary");
    }

    public static Builder<?,Class<LiteralBooleanBinding>> booleanBuilder() {
        return builder(LiteralBooleanBinding.class, "xs:boolean");
    }

    public static Builder<?,Class<LiteralByteBinding>> byteBuilder() {
        return builder(LiteralByteBinding.class, "xs:byte");
    }

    public static Builder<?,Class<LiteralDateTimeBinding>> dateTimeBuilder() {
        return builder(LiteralDateTimeBinding.class, "xs:dateTime");
    }

    public static Builder<?,Class<LiteralDoubleBinding>> doubleBuilder() {
        return builder(LiteralDoubleBinding.class, "xs:double");
    }

    public static Builder<?,Class<LiteralFloatBinding>> floatBuilder() {
        return builder(LiteralFloatBinding.class, "xs:float");
    }

    public static Builder<?,Class<LiteralIntBinding>> intBuilder() {
        return builder(LiteralIntBinding.class, "xs:int");
    }

    public static Builder<?,Class<LiteralShortBinding>> shortBuilder() {
        return builder(LiteralShortBinding.class, "xs:short");
    }

    public static Builder<?,Class<LiteralStringBinding>> stringBuilder() {
        return builder(LiteralStringBinding.class, "xs:string");
    }

    private static class BuilderTyped<T extends Class<? extends ILiteralData>> extends Builder<BuilderTyped<T>, T> {
        public BuilderTyped(T binding, String schemaType) {
            super(binding, schemaType);
        }
        @Override
        protected BuilderTyped self() {
            return this;
        }
    }

    public static abstract class Builder<B extends Builder<B,T>, T extends Class<? extends ILiteralData>> extends InputDescriptor.Builder<B,T> {

        private final String schemaType;
        private Object defaultValue;
        private List<? extends Object> allowedValues;
        
        public Builder(T binding, String schemaType) {
            super(binding);
            this.schemaType = schemaType;
        }

        public B defaultValue(Object defaultValue) {
            this.defaultValue = defaultValue;
            return self();
        }

        public B allowedValues(Class<? extends Enum> allowedValues) {
            return allowedValues(allowedValues.getEnumConstants());
        }
        
        public <A> B allowedValues(A[] allowedValues) {
            return allowedValues(Arrays.asList(allowedValues));
        }

        public B allowedValues(List<? extends Object> allowedValues) {
            this.allowedValues = allowedValues;
            return self();
        }

        @Override
        public LiteralDataInputDescriptor<T> build() {
            return new LiteralDataInputDescriptor<T>(this);
        }
    }

}
