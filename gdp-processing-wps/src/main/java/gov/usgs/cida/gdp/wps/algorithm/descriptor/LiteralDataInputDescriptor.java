package gov.usgs.cida.gdp.wps.algorithm.descriptor;

import java.util.AbstractList;
import java.util.ArrayList;
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

    private final String dataType;
    private final String defaultValue;
    private final List<String> allowedValues;

	protected LiteralDataInputDescriptor(Builder builder) {
		super(builder);
        this.dataType = builder.schemaType;
		this.defaultValue = builder.defaultValue;
		this.allowedValues = builder.allowedValues != null ?
            Collections.unmodifiableList(builder.allowedValues) :
            null;
	}

    public String getDataType() {
        return dataType;
    }

    public boolean hasDefaultValue() {
        return getDefaultValue() != null && getDefaultValue().length() > 0;
    }

    public String getDefaultValue() {
        return this.defaultValue;
    }

    public boolean hasAllowedValues() {
        return getAllowedValues() != null && getAllowedValues().size() > 0;
    }

    public List<String> getAllowedValues() {
        return this.allowedValues;
    }

    public static <T extends Class<? extends ILiteralData>> Builder<?,T> builder(T binding, String identifier) {
        return new BuilderTyped(binding, identifier, LiteralDataDescriptorUtility.dataTypeForBinding(binding));
    }

    // utility functions, quite verbose...  should have some factory method somewhere to
    // match binding class and schema type.  see analot in LiteralDataOutputDescriptor
    public static Builder<?,Class<LiteralAnyURIBinding>> anyURIBuilder(String identifier) {
        return builder(LiteralAnyURIBinding.class, identifier);
    }

    public static Builder<?,Class<LiteralBase64BinaryBinding>> base64BinaryBuilder(String identifier) {
        return builder(LiteralBase64BinaryBinding.class, identifier);
    }

    public static Builder<?,Class<LiteralBooleanBinding>> booleanBuilder(String identifier) {
        return builder(LiteralBooleanBinding.class, identifier);
    }

    public static Builder<?,Class<LiteralByteBinding>> byteBuilder(String identifier) {
        return builder(LiteralByteBinding.class, identifier);
    }

    public static Builder<?,Class<LiteralDateTimeBinding>> dateBuilder(String identifier) {
        return builder(LiteralDateTimeBinding.class, identifier);
    }

    public static Builder<?,Class<LiteralDateTimeBinding>> timeBuilder(String identifier) {
        return builder(LiteralDateTimeBinding.class, identifier);
    }

    public static Builder<?,Class<LiteralDateTimeBinding>> dateTimeBuilder(String identifier) {
        return builder(LiteralDateTimeBinding.class, identifier);
    }

    public static Builder<?,Class<LiteralDateTimeBinding>> durationBuilder(String identifier) {
        return builder(LiteralDateTimeBinding.class, identifier);
    }

    public static Builder<?,Class<LiteralDoubleBinding>> doubleBuilder(String identifier) {
        return builder(LiteralDoubleBinding.class, identifier);
    }

    public static Builder<?,Class<LiteralFloatBinding>> floatBuilder(String identifier) {
        return builder(LiteralFloatBinding.class, identifier);
    }

    public static Builder<?,Class<LiteralIntBinding>> intBuilder(String identifier) {
        return builder(LiteralIntBinding.class, identifier);
    }

    public static Builder<?,Class<LiteralShortBinding>> shortBuilder(String identifier) {
        return builder(LiteralShortBinding.class, identifier);
    }

    public static Builder<?,Class<LiteralStringBinding>> stringBuilder(String identifier) {
        return builder(LiteralStringBinding.class, identifier);
    }

    private static class BuilderTyped<T extends Class<? extends ILiteralData>> extends Builder<BuilderTyped<T>, T> {
        public BuilderTyped(T binding, String identifier, String dataType) {
            super(binding, identifier, dataType);
        }
        @Override
        protected BuilderTyped self() {
            return this;
        }
    }

    public static abstract class Builder<B extends Builder<B,T>, T extends Class<? extends ILiteralData>> extends InputDescriptor.Builder<B,T> {

        private final String schemaType;
        private String defaultValue;
        private List<String> allowedValues;
        
        protected Builder(T binding, String identifier, String schemaType) {
            super(binding, identifier);
            this.schemaType = schemaType;
        }

        public B defaultValue(String defaultValue) {
            this.defaultValue = defaultValue;
            return self();
        }

        public B allowedValues(Class<? extends Enum> allowedValues) {
            Enum[] constants = allowedValues.getEnumConstants();
            List<String> names = new ArrayList<String>(constants.length);
            for (Enum constant : constants) { names.add(constant.name()); }
            return allowedValues(names);
        }
        
        public B allowedValues(String[] allowedValues) {
            return allowedValues(Arrays.asList(allowedValues));
        }

        public B allowedValues(List<String> allowedValues) {
            this.allowedValues = allowedValues;
            return self();
        }

        @Override
        public LiteralDataInputDescriptor<T> build() {
            return new LiteralDataInputDescriptor<T>(this);
        }
    }

}
