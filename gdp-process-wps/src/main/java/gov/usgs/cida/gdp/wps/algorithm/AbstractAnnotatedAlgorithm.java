package gov.usgs.cida.gdp.wps.algorithm;

import gov.usgs.cida.gdp.wps.algorithm.annotation.Algorithm;
import gov.usgs.cida.gdp.wps.algorithm.annotation.ComplexDataInput;
import gov.usgs.cida.gdp.wps.algorithm.annotation.ComplexDataOutput;
import gov.usgs.cida.gdp.wps.algorithm.annotation.LiteralDataInput;
import gov.usgs.cida.gdp.wps.algorithm.annotation.LiteralDataOutput;
import gov.usgs.cida.gdp.wps.algorithm.annotation.Process;
import gov.usgs.cida.gdp.wps.algorithm.descriptor.AlgorithmDescriptor;
import gov.usgs.cida.gdp.wps.algorithm.descriptor.ComplexDataInputDescriptor;
import gov.usgs.cida.gdp.wps.algorithm.descriptor.ComplexDataOutputDescriptor;
import gov.usgs.cida.gdp.wps.algorithm.descriptor.InputDescriptor;
import gov.usgs.cida.gdp.wps.algorithm.descriptor.LiteralDataInputDescriptor;
import gov.usgs.cida.gdp.wps.algorithm.descriptor.LiteralDataOutputDescriptor;
import gov.usgs.cida.gdp.wps.algorithm.descriptor.OutputDescriptor;
import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.WildcardType;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.n52.wps.io.data.IData;
import org.n52.wps.io.data.ILiteralData;
import org.n52.wps.util.BasicXMLTypeFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author tkunicki
 */
public abstract class AbstractAnnotatedAlgorithm extends AbstractSelfDescribingAlgorithm {

    public final static Logger LOGGER = LoggerFactory.getLogger(AbstractAnnotatedAlgorithm.class);

    public static class Introspector {

        private Class<?> algorithmClass;

        private AlgorithmDescriptor algorithmDescriptor;

        private Method processMethod;

        private Map<String, AnnotatedInputBinding<Field>> inputFieldMap;
        private Map<String, Field> outputFieldMap;
        private Map<String, AnnotatedInputBinding<Method>> inputMethodMap;
        private Map<String, Method> outputMethodMap;

        public Introspector(Class<?> algorithmClass) {

            this.algorithmClass = algorithmClass;

            inputFieldMap = new LinkedHashMap<String, AnnotatedInputBinding<Field>>();
            inputMethodMap = new LinkedHashMap<String, AnnotatedInputBinding<Method>>();
            outputFieldMap = new LinkedHashMap<String, Field>();
            outputMethodMap = new LinkedHashMap<String, Method>();

            parseClass();

            inputFieldMap = Collections.unmodifiableMap(inputFieldMap);
            inputMethodMap = Collections.unmodifiableMap(inputMethodMap);
            outputFieldMap = Collections.unmodifiableMap(outputFieldMap);
            outputMethodMap = Collections.unmodifiableMap(outputMethodMap);
        }

        private void parseClass() {

            AlgorithmDescriptor.Builder<?> algorithmBuilder = AlgorithmDescriptor.builder(algorithmClass);
            try {

                Algorithm algorithm = algorithmClass.getAnnotation(Algorithm.class);

                algorithmBuilder.identifier(
                            algorithm.identifier().length() > 0 ?
                                algorithm.identifier() :
                                algorithmClass.getCanonicalName()).
                        title(algorithm.title()).
                        abstrakt(algorithm.abstrakt()).
                        version(algorithm.version()).
                        storeSupported(algorithm.storeSupported()).
                        statusSupported(algorithm.statusSupported());

                parseMembers(
                        algorithmBuilder,
                        inputFieldMap,
                        outputFieldMap,
                        new InputFieldValidator(),
                        new OutputFieldValidator(),
                        algorithmClass.getDeclaredFields());
                parseMembers(
                        algorithmBuilder,
                        inputMethodMap,
                        outputMethodMap,
                        new InputMethodValidator(),
                        new OutputMethodValidator(),
                        algorithmClass.getDeclaredMethods());

                Iterator<Method> methodIterator = Arrays.asList(algorithmClass.getDeclaredMethods()).iterator();
                ProcessMethodValidator processValidator = new ProcessMethodValidator();
                while (methodIterator.hasNext() && processMethod == null) {
                    Method method = methodIterator.next();
                    if (method.getAnnotation(Process.class) != null) {
                        if (processValidator.validate(method)) {
                            processMethod = method;
                        } else {
                        // TODO: error? warning?
                        }
                    }
                }

                algorithmDescriptor = algorithmBuilder.build();

            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        protected <T extends AccessibleObject & Member> void parseMembers(
                AlgorithmDescriptor.Builder<?> algorithmBuilder,
                Map<String, AnnotatedInputBinding<T>> inputMemberMap,
                Map<String, T> outputMemberMap,
                InputValidator<T> inputMemberValidator,
                OutputValidator<T> outputMemberValidator,
                T[] elements) {

            for (T element : elements) {
                LiteralDataOutput ldo = element.getAnnotation(LiteralDataOutput.class);
                if (ldo != null) {
                    LiteralDataOutputDescriptor ldod =
                            LiteralDataOutputDescriptor.builder(ldo.binding(), ldo.identifier()).
                            title(ldo.title()).
                            abstrakt(ldo.abstrakt()).build();

                    if (outputMemberValidator.validate(element, ldod)) {
                        outputMemberMap.put(ldo.identifier(), element);
                        algorithmBuilder.addOutputDesciptor(ldod);
                    } else {
                        // TODO: error? warning?
                    }
                }
                ComplexDataOutput cdo = element.getAnnotation(ComplexDataOutput.class);
                if (cdo != null) {
                    ComplexDataOutputDescriptor cdod =
                            ComplexDataOutputDescriptor.builder(
                            cdo.binding(),
                            cdo.identifier()).
                            title(cdo.title()).
                            abstrakt(cdo.abstrakt()).
                            build();

                    if (outputMemberValidator.validate(element, cdod)) {
                        outputMemberMap.put(cdo.identifier(), element);
                        algorithmBuilder.addOutputDesciptor(cdod);
                    } else {
                        // TODO: error? warning?
                    }
                }

                LiteralDataInput ldi = element.getAnnotation(LiteralDataInput.class);
                if (ldi != null) {
                    AnnotatedInputBinding aib = inputMemberValidator.getInputBinding(element);

                    // auto generate binding if it's not explicitly declared
                    Type payloadType = aib.getPayloadType();
                    Class<? extends ILiteralData> binding = ldi.binding();
                    if (binding == null || ILiteralData.class.equals(binding)) {
                        if (payloadType instanceof Class<?>) {
                            binding = BasicXMLTypeFactory.getBindingForType((Class<?>)payloadType);
                            if (binding == null) {
                                LOGGER.warn("Unable to locate binding class for {}; binding not found.", payloadType);
                            }
                        } else {
                            if (aib.isMemberTypeList()) {
                                LOGGER.warn("Unable to determine binding class for {}; List must be parameterized with a type matching a known binding payload to use auto-binding.", payloadType);
                            } else {
                                LOGGER.warn("Unable to determine binding class for {}; type must fully resolved to use auto-binding", payloadType);
                            }
                        }
                    }

                    String[] allowedValues = ldi.allowedValues();
                    String defaultValue = ldi.defaultValue();
                    int maxOccurs = ldi.maxOccurs();

                    // If InputType is enum
                    //  1) generate allowedValues if not explicitly declared
                    //  2) validate allowedValues if explicitly declared
                    //  3) validate defaultValue if declared
                    //  4) check for special ENUM_COUNT maxOccurs flag
                    Type inputType = aib.getInputType();
                    if (aib.isInputTypeEnum()) {
                        Class<? extends Enum> inputEnumClass = (Class<? extends Enum>)inputType;
                        // validate contents of allowed values maps to enum
                        if (allowedValues.length > 0) {
                            List<String> invalidValues = new ArrayList<String>();
                            for (String value : allowedValues) {
                                try {
                                    Enum.valueOf(inputEnumClass, value);
                                } catch (IllegalArgumentException e) {
                                    invalidValues.add(value);
                                    LOGGER.warn("Invalid allowed value \"{}\" specified for for enumerated input type {}", value, inputType);
                                }
                            }
                            if (invalidValues.size() > 0) {
                                List<String> updatedValues = new ArrayList<String>(Arrays.asList(allowedValues));
                                updatedValues.removeAll(invalidValues);
                                allowedValues = updatedValues.toArray(new String[0]);
                            }
                        }
                        // if list is empty, populated with values from enum
                        if (allowedValues.length == 0) {
                            allowedValues = GDPAlgorithmUtil.convertEnumToStringArray(inputEnumClass);
                        }
                        if (defaultValue.length() > 0) {
                            try {
                                Enum.valueOf(inputEnumClass, defaultValue);
                            } catch (IllegalArgumentException e) {
                                LOGGER.warn("Invalid default value \"{}\" specified for for enumerated input type {}", defaultValue, inputType);
                                defaultValue = "";
                            }
                        }
                        if (maxOccurs == LiteralDataInput.ENUM_COUNT) {
                            maxOccurs = inputEnumClass.getEnumConstants().length;
                        }
                    } else {
                        if (maxOccurs == LiteralDataInput.ENUM_COUNT) {
                            maxOccurs = 1;
                            LOGGER.warn("Invalid maxOccurs \"ENUM_COUNT\" specified for for input type {}", inputType);
                        }
                    }

                    if (binding != null) {
                        LiteralDataInputDescriptor ldid =
                                LiteralDataInputDescriptor.builder(binding, ldi.identifier()).
                                title(ldi.title()).
                                abstrakt(ldi.abstrakt()).
                                minOccurs(ldi.minOccurs()).
                                maxOccurs(maxOccurs).
                                defaultValue(defaultValue).
                                allowedValues(allowedValues).
                                build();
                        if (inputMemberValidator.validate(aib, ldid)) {
                            inputMemberMap.put(ldi.identifier(), aib);
                            algorithmBuilder.addInputDesciptor(ldid);
                        } else {
                        // TODO: error? warning?
                        }
                    } else {

                    }
                }

                ComplexDataInput cdi = element.getAnnotation(ComplexDataInput.class);
                if (cdi != null) {
                    AnnotatedInputBinding aib = inputMemberValidator.getInputBinding(element);
                    ComplexDataInputDescriptor cdid =
                            ComplexDataInputDescriptor.builder(cdi.binding(), cdi.identifier()).
                            title(cdi.title()).
                            abstrakt(cdi.abstrakt()).
                            minOccurs(cdi.minOccurs()).
                            maxOccurs(cdi.maxOccurs()).
                            maximumMegaBytes(cdi.maximumMegaBytes()).
                            build();

                    if (inputMemberValidator.validate(aib, cdid)) {
                        inputMemberMap.put(cdi.identifier(), aib);
                        algorithmBuilder.addInputDesciptor(cdid);
                    } else {
                        // TODO: error? warning?
                    }
                }
            }
        }

        public AlgorithmDescriptor getAlgorithmDescriptor() {
            return algorithmDescriptor;
        }

        public Method getProcessMethod() {
            return processMethod;
        }

        public Map<String, AnnotatedInputBinding<Field>> getInputFieldMap() {
            return inputFieldMap;
        }

        public Map<String, Field> getOutputFieldMap() {
            return outputFieldMap;
        }

        public Map<String, AnnotatedInputBinding<Method>> getInputMethodMap() {
            return inputMethodMap;
        }

        public Map<String, Method> getOutputMethodMap() {
            return outputMethodMap;
        }
        
        private static abstract class Validator {
            protected static boolean checkModifier(Member member) {
                return ((member.getModifiers() & Modifier.PUBLIC) != 0);
            }
        }

        private static class ProcessMethodValidator extends Validator {
            public boolean validate(Method method) {
                return checkModifier(method) &&
                        (method.getReturnType().equals(void.class)) &&
                        (method.getParameterTypes().length == 0);
            }
        }

        private static abstract class InputValidator<T extends AccessibleObject & Member> extends Validator {

            public abstract boolean validate(AnnotatedInputBinding<T> annotatedBinding, InputDescriptor<?> inputDescriptor);

            protected boolean checkInputType(AnnotatedInputBinding<T> annotatedBinding, Class<? extends IData> bindingClass) {
                Type inputPayloadType = annotatedBinding.getPayloadType();
                try {
                    Class<?> bindingPayloadClass = bindingClass.getMethod("getPayload", (Class<?>[])null).getReturnType();
                    if (inputPayloadType instanceof Class<?>) {
                        return bindingPayloadClass.isAssignableFrom((Class<?>)inputPayloadType);
                    } else if (inputPayloadType instanceof ParameterizedType) {
                        // i.e. List<FeatureCollection<SimpleFeatureType,SimpleFeature>>
                        return bindingPayloadClass.isAssignableFrom(((Class<?>)((ParameterizedType)inputPayloadType).getRawType()));
                    } else if (inputPayloadType instanceof WildcardType) {
                        // i.e. List<? extends String> or List<? super String>
                        WildcardType inputTypeWildcardType = (WildcardType)inputPayloadType;
                        Type[] lowerBounds = inputTypeWildcardType.getLowerBounds();
                        Type[] upperBounds = inputTypeWildcardType.getUpperBounds();
                        Class<?> lowerBoundClass = null;
                        Class<?> upperBoundClass = null;
                        if (lowerBounds != null && lowerBounds.length > 0) {
                            if (lowerBounds[0] instanceof Class<?>) {
                                lowerBoundClass = (Class<?>)lowerBounds[0];
                            } else if (lowerBounds[0] instanceof ParameterizedType) {
                                lowerBoundClass = (Class<?>)((ParameterizedType)lowerBounds[0]).getRawType();
                            }
                        }
                        if (upperBounds != null && upperBounds.length > 0) {
                            if (upperBounds[0] instanceof Class<?>) {
                                upperBoundClass = (Class<?>)upperBounds[0];
                            } else if (upperBounds[0] instanceof ParameterizedType) {
                                upperBoundClass = (Class<?>)((ParameterizedType)upperBounds[0]).getRawType();
                            }
                        }
                        return ( upperBoundClass == null || upperBoundClass.isAssignableFrom(bindingPayloadClass)) &&
                               ( lowerBounds == null || bindingPayloadClass.isAssignableFrom(lowerBoundClass));
                    }
                } catch (NoSuchMethodException e) {
                    return false;
                }
                return false;
            }

            public abstract AnnotatedInputBinding<T> getInputBinding(T Member);
        }

        private static abstract class OutputValidator<T extends AccessibleObject & Member> extends Validator {
            public abstract boolean  validate(T member, OutputDescriptor<?> outputDescriptor);
            protected static boolean checkType(Class<?> candidateClass, Class<? extends IData> bindingClass) {
                try {
                    Class<?> payloadClass = bindingClass.getMethod("getPayload", (Class<?>[])null).getReturnType();
                    if (payloadClass.isAssignableFrom(candidateClass)) {
                        return true;
                    }
                } catch (NoSuchMethodException e) {
                    return false;
                }
                return false;
            }
        }

        private static class InputFieldValidator extends InputValidator<Field> {
            @Override
            public boolean validate(AnnotatedInputBinding<Field> binding, InputDescriptor<?> descriptor) {
                return checkModifier(binding.getMember()) &&
                       (descriptor.getMaxOccurs().intValue() < 2 || binding.isMemberTypeList()) &&
                       checkInputType(binding, descriptor.getBinding());
            }

            @Override
            public AnnotatedInputBinding<Field> getInputBinding(Field member) {
                return new AnnotatedFieldInputBinding(member);
            }
        }

        private static class OutputFieldValidator extends OutputValidator<Field> {
            @Override
            public boolean validate(Field field, OutputDescriptor<?> descriptor) {
                return checkModifier(field) &&
                        checkType(field.getType(), descriptor.getBinding());
            }
        }
        
        private class InputMethodValidator extends InputValidator<Method> {
            @Override
            public boolean validate(AnnotatedInputBinding<Method> binding, InputDescriptor<?> descriptor) {
                Type[] genericParameterTypes = binding.getMember().getGenericParameterTypes();
                return genericParameterTypes.length == 1 &&
                       checkModifier(binding.getMember()) &&
                       (descriptor.getMaxOccurs().intValue() < 2 || binding.isMemberTypeList()) &&
                       checkInputType(binding, descriptor.getBinding());
            }

            @Override
            public AnnotatedInputBinding<Method> getInputBinding(Method member) {
                return new AnnotatedMethodInputBinding(member);
            }
        }

        private class OutputMethodValidator extends OutputValidator<Method> {
            @Override
            public boolean validate(Method method, OutputDescriptor<?> descriptor) {
                return method.getParameterTypes().length == 0 &&
                        checkModifier(method) &&
                        checkType(method.getReturnType(), descriptor.getBinding());
            }
        }

        public static abstract class AnnotatedInputBinding<T extends AccessibleObject & Member> {

            private T member;

            public AnnotatedInputBinding(T member) {
                this.member = member;
            }

            public T getMember() {
                return member;
            }
            
            public abstract Type getMemberType();

            public Type getInputType() {
                Type memberType = getMemberType();
                Type inputType = memberType;
                if (memberType instanceof Class<?>) {
                    Class<?> memberClass = (Class<?>)memberType;
                    if (List.class.isAssignableFrom(memberClass)) {
                        // We treat List as List<? extends Object>
                        inputType =  new WildcardType() {
                            @Override public Type[] getUpperBounds() { return new Type[] { Object.class }; }
                            @Override public Type[] getLowerBounds() { return new Type[0]; }
                        };
                    }
                } else {
                    if (memberType instanceof ParameterizedType) {
                        ParameterizedType parameterizedMemberType =
                                (ParameterizedType) memberType;
                        Class<?> rawClass = (Class<?>) parameterizedMemberType.getRawType();
                        if (List.class.isAssignableFrom(rawClass)) {
                            inputType = parameterizedMemberType.getActualTypeArguments()[0];
                        }
                    } else {
                        // can't do much with others
                    }
                }
                return inputType;
            }

            public Type getPayloadType() {
                Type inputType = getInputType();
                return isInputTypeEnum() ? String.class : inputType;
            }

            public boolean isMemberTypeList() {
                Type memberType = getMemberType();
                if (memberType instanceof Class<?>) {
                    return List.class.isAssignableFrom((Class<?>)memberType);
                } else if (memberType instanceof ParameterizedType) {
                    Class<?> rawClass = (Class<?>) ((ParameterizedType)memberType).getRawType();
                    return List.class.isAssignableFrom(rawClass);
                } else {
                    // can't do much with others
                }
                return false;
            }

            public boolean isInputTypeEnum() {
                Type inputType = getInputType();
                return (inputType instanceof Class<?>) && ((Class<?>)inputType).isEnum();
            }
        }

        public static class AnnotatedFieldInputBinding extends AnnotatedInputBinding<Field> {
            public AnnotatedFieldInputBinding(Field field) {
                super(field);
            }
            @Override public Type getMemberType() {
                return getMember().getGenericType();
            }
        }

        public static class AnnotatedMethodInputBinding extends AnnotatedInputBinding<Method> {
            public AnnotatedMethodInputBinding(Method method) {
                super(method);
            }
            @Override public Type getMemberType() {
                Type[] genericParameterTypes = getMember().getGenericParameterTypes();
                return (genericParameterTypes.length == 0) ?
                    Void.class :
                    genericParameterTypes[0];
            }
        }
    }

    private final static Map<Class<?>, Introspector> introspectorMap =
            new HashMap<Class<?>, Introspector>();
    public static synchronized Introspector getInstrospector(Class<?> algorithmClass) {
        Introspector introspector = introspectorMap.get(algorithmClass);
        if (introspector == null) {
            introspector = new Introspector(algorithmClass);
            introspectorMap.put(algorithmClass, introspector);
        }
        return introspector;
    }

    @Override
    protected AlgorithmDescriptor getAlgorithmDescriptor() {
        return getInstrospector(getClass()).getAlgorithmDescriptor();
    }

    private void processInputs(Object target, Map<String, List<IData>> inputMap) {

        Introspector introspector = getInstrospector(target.getClass());

        for (Map.Entry<String, Introspector.AnnotatedInputBinding<Field>> iEntry : introspector.getInputFieldMap().entrySet()) {
            String iIdentifier = iEntry.getKey();
            Introspector.AnnotatedInputBinding<Field> iAnnotatedBinding = iEntry.getValue();
            InputDescriptor iDescriptor = getAlgorithmDescriptor().getInputDescriptor(iIdentifier);
            List<IData> boundList = inputMap.get(iIdentifier);
            try {
                iAnnotatedBinding.getMember().set(target, unbindInputValue(iDescriptor, boundList, iAnnotatedBinding));
            } catch (IllegalArgumentException ex) {
                throw new RuntimeException("Internal error processing inputs", ex);
            } catch (IllegalAccessException ex) {
                throw new RuntimeException("Internal error processing inputs", ex);
            }

        }
        for (Map.Entry<String, Introspector.AnnotatedInputBinding<Method>> iEntry : introspector.getInputMethodMap().entrySet()) {
            String iIdentifier = iEntry.getKey();
            Introspector.AnnotatedInputBinding<Method> iAnnotatedBinding = iEntry.getValue();
            InputDescriptor iDescriptor = getAlgorithmDescriptor().getInputDescriptor(iIdentifier);
            List<IData> boundList = inputMap.get(iIdentifier);
            try {
                iAnnotatedBinding.getMember().invoke(target, unbindInputValue(iDescriptor, boundList, iAnnotatedBinding));
            } catch (IllegalAccessException ex) {
                throw new RuntimeException("Internal error processing inputs", ex);
            } catch (IllegalArgumentException ex) {
                throw new RuntimeException("Internal error processing inputs", ex);
            } catch (InvocationTargetException ex) {
                throw new RuntimeException("Internal error processing inputs", ex);
            }

        }
    }

    private Object unbindInputValue(InputDescriptor descriptor, List<IData> boundValueList, Introspector.AnnotatedInputBinding<?> annotatedBinding) {
        Object value = null;
        int minOccurs = descriptor.getMinOccurs().intValue();
        int maxOccurs = descriptor.getMaxOccurs().intValue();
        if (boundValueList == null) {
            Arrays.asList(new IData[0]);
        }
        if (boundValueList.size() < minOccurs) {
            throw new RuntimeException("Found " + boundValueList.size() + "values for INPUT " + descriptor.getIdentifier() + ", require minimum of " + minOccurs);
        }
        if (boundValueList.size() > maxOccurs) {
            throw new RuntimeException("Found " + boundValueList.size() + "values for INPUT " + descriptor.getIdentifier() + ", maximum allowed is " + minOccurs);
        }
        if (annotatedBinding.isMemberTypeList()) {
            List valueList = new ArrayList(boundValueList.size());
            for (IData bound : boundValueList) {
                value = bound.getPayload();
                if (annotatedBinding.isInputTypeEnum()) {
                    value = Enum.valueOf((Class<? extends Enum>)annotatedBinding.getInputType(), (String)value);
                }
                valueList.add(value);
            }
            value = valueList;
        } else if (boundValueList.size() == 1) {
            value = boundValueList.get(0).getPayload();
            if (annotatedBinding.isInputTypeEnum()) {
                value = Enum.valueOf((Class<? extends Enum>)annotatedBinding.getInputType(), (String)value);
            }
        }
        return value;
    }

    private Map<String, IData> processOutput(Object target) {
        Introspector introspector = getInstrospector(target.getClass());
        Map<String, IData> oMap = new HashMap<String, IData>();
        for (Map.Entry<String, Field> oEntry : introspector.getOutputFieldMap().entrySet()) {
            String oIdentifier = oEntry.getKey();
            Field oField = oEntry.getValue();
            OutputDescriptor<?> oDescriptor = getAlgorithmDescriptor().getOutputDescriptor(oIdentifier);
            try {
                Object value = oField.get(target);
                if (value != null) {
                    oMap.put(oEntry.getKey(), (IData) bindOutputValue(oDescriptor, value));
                } else {
                    // TODO: error? warning?
                }
            } catch (InvocationTargetException ex) {
                throw new RuntimeException(ex.getMessage(), ex);
            } catch (IllegalArgumentException ex) {
                throw new RuntimeException("Internal error processing outputs", ex);
            } catch (IllegalAccessException ex) {
                throw new RuntimeException("Internal error processing outputs", ex);
            }
        }
        for (Map.Entry<String, Method> oEntry : introspector.getOutputMethodMap().entrySet()) {
            String oIdentifier = oEntry.getKey();
            Method oMethod = oEntry.getValue();
            OutputDescriptor<?> oDescriptor = getAlgorithmDescriptor().getOutputDescriptor(oIdentifier);
            try {
                Object value = oMethod.invoke(target);
                if (value != null) {
                    oMap.put(oEntry.getKey(), (IData) bindOutputValue(oDescriptor, value));
                } else {
                    // TODO: error? warning?
                }
            } catch (InvocationTargetException ex) {
                throw new RuntimeException(ex.getMessage(), ex);
            } catch (IllegalArgumentException ex) {
                throw new RuntimeException("Internal error processing outputs", ex);
            } catch (IllegalAccessException ex) {
                throw new RuntimeException("Internal error processing outputs", ex);
            }
        }
        return oMap;
    }

    private Object bindOutputValue(OutputDescriptor descriptor, Object outputValue)
            throws IllegalAccessException, IllegalArgumentException, InvocationTargetException {
        Object binding = null;
        try {
            Class<?> bindingClass = descriptor.getBinding();
            Constructor bindingConstructor = bindingClass.getConstructor(outputValue.getClass());
            binding =  bindingConstructor.newInstance(outputValue);
        } catch (InstantiationException ex) {
                throw new RuntimeException("Internal error processing outputs", ex);
        } catch (NoSuchMethodException ex) {
                throw new RuntimeException("Internal error processing outputs", ex);
        } catch (SecurityException ex) {
                throw new RuntimeException("Internal error processing outputs", ex);
        }
        return binding;
    }

    @Override
    public Map<String, IData> run(Map<String, List<IData>> inputMap) {
        // this is where as I plan on  separating the implementation
        // from the annotated process/algorithm, there's no reason it needs to me
        // a subclass (simply that way not for development convenience)
        Object target = this;
        processInputs(target, inputMap);
        try {
            getInstrospector(target.getClass()).getProcessMethod().invoke(target);
        } catch (IllegalAccessException ex) {
            throw new RuntimeException("Internal error executing process", ex);
        } catch (IllegalArgumentException ex) {
            throw new RuntimeException("Internal error executing process", ex);
        } catch (InvocationTargetException ex) {
            throw new RuntimeException(ex.getMessage(), ex);
        }
        return processOutput(target);
    }

}
