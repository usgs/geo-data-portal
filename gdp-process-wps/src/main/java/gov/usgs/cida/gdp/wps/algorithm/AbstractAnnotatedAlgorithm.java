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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.n52.wps.io.data.IData;

/**
 *
 * @author tkunicki
 */
public abstract class AbstractAnnotatedAlgorithm extends AbstractSelfDescribingAlgorithm {

    public static class Introspector {

        private Class<?> algorithmClass;

        private AlgorithmDescriptor algorithmDescriptor;

        private Method processMethod;

        private Map<String, Field> inputFieldMap;
        private Map<String, Field> outputFieldMap;
        private Map<String, Method> outputMethodMap;
        private Map<String, Method> inputMethodMap;

        public Introspector(Class<?> algorithmClass) {

            this.algorithmClass = algorithmClass;

            inputFieldMap = new LinkedHashMap<String, Field>();
            outputFieldMap = new LinkedHashMap<String, Field>();
            inputMethodMap = new LinkedHashMap<String, Method>();
            outputMethodMap = new LinkedHashMap<String, Method>();

            parseClass();

            inputFieldMap = Collections.unmodifiableMap(inputFieldMap);
            outputFieldMap = Collections.unmodifiableMap(outputFieldMap);
            inputMethodMap = Collections.unmodifiableMap(inputMethodMap);
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

                parseElements(
                        algorithmBuilder,
                        inputFieldMap,
                        outputFieldMap,
                        new InputFieldValidator(),
                        new OutputFieldValidator(),
                        algorithmClass.getDeclaredFields());
                parseElements(
                        algorithmBuilder,
                        inputMethodMap,
                        outputMethodMap,
                        new InputMethodValidator(),
                        new OutputMethodValidator(),
                        algorithmClass.getDeclaredMethods());

                Iterator<Method> methodIterator = Arrays.asList(algorithmClass.getDeclaredMethods()).iterator();
                ProcessValidator processValidator = new ProcessValidator();
                while (methodIterator.hasNext() && processMethod == null) {
                    Method method = methodIterator.next();
                    if (method.getAnnotation(Process.class) != null) {
                        if (processValidator.validate(method)) {
                            processMethod = method;
                        } else {
                            // TODO: error
                        }
                    }
                }

                algorithmDescriptor = algorithmBuilder.build();

            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        protected <T extends AccessibleObject> void parseElements(
                AlgorithmDescriptor.Builder<?> algorithmBuilder,
                Map<String, T> inputElementMap,
                Map<String, T> outputElementMap,
                InputValidator<T> inputElementValidator,
                OutputValidator<T> outputElementValidator,
                T[] elements) {

            for (T element : elements) {
                LiteralDataOutput ldo = element.getAnnotation(LiteralDataOutput.class);
                if (ldo != null) {
                    LiteralDataOutputDescriptor ldod =
                            LiteralDataOutputDescriptor.builder(ldo.binding(), ldo.identifier()).
                            title(ldo.title()).
                            abstrakt(ldo.abstrakt()).build();

                    if (outputElementValidator.validate(element, ldod)) {
                        outputElementMap.put(ldo.identifier(), element);
                        algorithmBuilder.addOutputDesciptor(ldod);
                    } else {
                        // TODO: error
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

                    if (outputElementValidator.validate(element, cdod)) {
                        outputElementMap.put(cdo.identifier(), element);
                        algorithmBuilder.addOutputDesciptor(cdod);
                    } else {
                        // TODO: error
                    }
                }
                LiteralDataInput ldi = element.getAnnotation(LiteralDataInput.class);
                if (ldi != null) {
                    LiteralDataInputDescriptor ldid =
                            LiteralDataInputDescriptor.builder(ldi.binding(), ldi.identifier()).
                            title(ldi.title()).
                            abstrakt(ldi.abstrakt()).
                            minOccurs(ldi.minOccurs()).
                            maxOccurs(ldi.maxOccurs()).
                            defaultValue(ldi.defaultValue()).
                            allowedValues(ldi.allowedValues()).
                            build();

                    if (inputElementValidator.validate(element, ldid)) {
                        inputElementMap.put(ldi.identifier(), element);
                        algorithmBuilder.addInputDesciptor(ldid);
                    } else {
                        // TODO: error
                    }
                }
                ComplexDataInput cdi = element.getAnnotation(ComplexDataInput.class);
                if (cdi != null) {
                    ComplexDataInputDescriptor cdid =
                            ComplexDataInputDescriptor.builder(cdi.binding(), cdi.identifier()).
                            title(cdi.title()).
                            abstrakt(cdi.abstrakt()).
                            minOccurs(cdi.minOccurs()).
                            maxOccurs(cdi.maxOccurs()).
                            maximumMegaBytes(cdi.maximumMegaBytes()).
                            build();

                    if (inputElementValidator.validate(element, cdid)) {
                        inputElementMap.put(cdi.identifier(), element);
                        algorithmBuilder.addInputDesciptor(cdid);
                    } else {
                        // TODO: error
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

        public Map<String, Field> getInputFieldMap() {
            return inputFieldMap;
        }

        public Map<String, Field> getOutputFieldMap() {
            return outputFieldMap;
        }

        public Map<String, Method> getInputMethodMap() {
            return inputMethodMap;
        }

        public Map<String, Method> getOutputMethodMap() {
            return outputMethodMap;
        }
        
        private static abstract class Validator {

            protected static boolean checkModifier(Member member) {
                return ((member.getModifiers() & Modifier.PUBLIC) != 0);
            }

            protected static boolean checkType(Class<?> candidateClass, Class<? extends IData> bindingClass, boolean listRequired) {
                try {
                    Class<?> payloadClass = bindingClass.getMethod("getPayload", (Class<?>[])null).getReturnType();
                    if (List.class.isAssignableFrom(candidateClass)) {
                        return true;
                    } else if (payloadClass.isAssignableFrom(candidateClass) && !listRequired) {
                        return true;
                    }
                } catch (NoSuchMethodException e) {
                    return false;
                }
                return false;
            }
        }

        private static class ProcessValidator extends Validator {
            public boolean validate(Method method) {
                return checkModifier(method) &&
                        (method.getReturnType().equals(void.class)) &&
                        (method.getParameterTypes().length == 0);
            }
        }

        private static abstract class InputValidator<T extends AccessibleObject> extends Validator {
            public abstract boolean validate(T member, InputDescriptor<?> inputDescriptor);
        }

        private static abstract class OutputValidator<T extends AccessibleObject> extends Validator {
            public abstract boolean  validate(T member, OutputDescriptor<?> outputDescriptor);
        }

        private static class InputFieldValidator extends InputValidator<Field> {
            @Override
            public boolean validate(Field field, InputDescriptor<?> descriptor) {
                return checkModifier(field) &&
                        checkType(field.getType(),
                            descriptor.getBinding(),
                            descriptor.getMaxOccurs().intValue() > 1);
            }
        }

        private static class OutputFieldValidator extends OutputValidator<Field> {
            @Override
            public boolean validate(Field field, OutputDescriptor<?> descriptor) {
                return checkModifier(field) &&
                        checkType(field.getType(),
                            descriptor.getBinding(),
                            false);
            }
        }
        
        private class InputMethodValidator extends InputValidator<Method> {
            @Override
            public boolean validate(Method method, InputDescriptor<?> descriptor) {
                Class<?>[] parameterTypes = method.getParameterTypes();
                return parameterTypes.length == 1 &&
                        checkModifier(method) &&
                        checkType(parameterTypes[0],
                            descriptor.getBinding(),
                            descriptor.getMaxOccurs().intValue() > 1);
            }
        }

        private class OutputMethodValidator extends OutputValidator<Method> {
            @Override
            public boolean validate(Method method, OutputDescriptor<?> descriptor) {
                return method.getParameterTypes().length == 0 &&
                        checkModifier(method) &&
                        checkType(method.getReturnType(),
                            descriptor.getBinding(),
                            false);
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
        for (Map.Entry<String, Field> iEntry : introspector.getInputFieldMap().entrySet()) {
            String iIdentifier = iEntry.getKey();
            Field iField = iEntry.getValue();
            try {
                List<IData> boundList = inputMap.get(iIdentifier);
                // validate
                InputDescriptor iDescriptor = getAlgorithmDescriptor().getInputDescriptor(iIdentifier);
                int minOccurs = iDescriptor.getMinOccurs().intValue();
                int maxOccurs = iDescriptor.getMaxOccurs().intValue();
                if (boundList == null) {
                    Arrays.asList(new IData[0]);
                }
                if (boundList.size() < minOccurs) {
                    throw new RuntimeException("Found " + boundList.size() + " for INPUT " + iIdentifier + ", require minimum of " + minOccurs);
                }
                if (boundList.size() > maxOccurs) {
                    throw new RuntimeException("Found " + boundList.size() + " for INPUT " + iIdentifier + ", maximum allowed is " + minOccurs);
                }
                Class unboundType = iField.getType();
                iField.set(target, unbindInputValue(iDescriptor, boundList, unboundType));
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
        for (Map.Entry<String, Method> iEntry : introspector.getInputMethodMap().entrySet()) {
            String iIdentifier = iEntry.getKey();
            Method iMethod = iEntry.getValue();
            try {
                List<IData> boundList = inputMap.get(iIdentifier);
                // validate
                InputDescriptor iDescriptor = getAlgorithmDescriptor().getInputDescriptor(iIdentifier);
                int minOccurs = iDescriptor.getMinOccurs().intValue();
                int maxOccurs = iDescriptor.getMaxOccurs().intValue();
                if (boundList == null) {
                    Arrays.asList(new IData[0]);
                }
                if (boundList.size() < minOccurs) {
                    throw new RuntimeException("Found " + boundList.size() + " for INPUT " + iIdentifier + ", require minimum of " + minOccurs);
                }
                if (boundList.size() > maxOccurs) {
                    throw new RuntimeException("Found " + boundList.size() + " for INPUT " + iIdentifier + ", maximum allowed is " + minOccurs);
                }
                Class unboundType = iMethod.getParameterTypes()[0];
                iMethod.invoke(target, unbindInputValue(iDescriptor, boundList, unboundType));
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    private Object unbindInputValue(InputDescriptor descriptor, List<IData> boundValueList, Class<?> unboundType) {
        Object value = null;
        if (List.class.isAssignableFrom(unboundType)) {
            List valueList = new ArrayList(boundValueList.size());
            for (IData bound : boundValueList) {
                valueList.add(bound.getPayload());
            }
            value = valueList;
        } else if (boundValueList.size() == 1) {
            value = boundValueList.get(0).getPayload();
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
                    // TODO: error
                }
            } catch (Exception ex) {
                ex.printStackTrace();
                // TODO: error
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
                    // TODO: error
                }
            } catch (Exception ex) {
                ex.printStackTrace();
                // TODO: error
            }
        }
        return oMap;
    }

    private Object bindOutputValue(OutputDescriptor descriptor, Object outputValue)
            throws NoSuchMethodException,
                InstantiationException,
                IllegalAccessException,
                IllegalArgumentException,
                InvocationTargetException
    {
        Class<?> bindingClass = descriptor.getBinding();
        Constructor bindingConstructor = bindingClass.getConstructor(outputValue.getClass());
        return bindingConstructor.newInstance(outputValue);
    }

    @Override
    public Map<String, IData> run(Map<String, List<IData>> inputMap) {
        // this is here as I plan on  separating the implementation
        // from the annotated process/algorithm, there's no reason it needs to me
        // a subclass (simply that way not for development convenience)
        Object target = this;
        processInputs(target, inputMap);
        try {
            getInstrospector(target.getClass()).getProcessMethod().invoke(target);
        } catch (Exception ex) {
            ex.printStackTrace();
            // TODO: error
        }
        return processOutput(target);
    }

}
