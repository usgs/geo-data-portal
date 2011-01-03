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
import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.n52.wps.io.data.IData;
import org.n52.wps.server.IAlgorithm;

/**
 *
 * @author tkunicki
 */
public abstract class AbstractAnnotatedAlgorithm extends AbstractSelfDescribingAlgorithm {

    public static class Introspector {

        private Class<? extends IAlgorithm> algorithmClass;

        private AlgorithmDescriptor<?> algorithmDescriptor;

        private Method processMethod;

        private Map<String, Field> inputFieldMap;
        private Map<String, Field> outputFieldMap;
        private Map<String, Method> outputMethodMap;
        private Map<String, Method> inputMethodMap;

        public Introspector(Class<? extends IAlgorithm> algorithmClass) {

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

            AlgorithmDescriptor.Builder<?,?> algorithmBuilder = AlgorithmDescriptor.builder(algorithmClass);
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

                parseElements(algorithmBuilder, inputFieldMap, outputFieldMap, algorithmClass.getDeclaredFields());
                parseElements(algorithmBuilder, inputMethodMap, outputMethodMap, algorithmClass.getDeclaredMethods());

                Iterator<Method> methodIterator = Arrays.asList(algorithmClass.getDeclaredMethods()).iterator();
                while (methodIterator.hasNext() && processMethod == null) {
                    Method method = methodIterator.next();
                    if (method.getAnnotation(Process.class) != null) {
                        processMethod = method;
                        // TODO check signature
                        processMethod.setAccessible(true);
                    }
                }

                algorithmDescriptor = algorithmBuilder.build();

            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        protected <T extends AccessibleObject> void parseElements(
                AlgorithmDescriptor.Builder<?, ?> algorithmBuilder,
                Map<String, T> inputElementMap,
                Map<String, T> outputElementMap,
                T[] elements) {

            for (T element : elements) {
                {
                    LiteralDataOutput ldo = element.getAnnotation(LiteralDataOutput.class);
                    if (ldo != null) {
                        LiteralDataOutputDescriptor ldod =
                                LiteralDataOutputDescriptor.builder(ldo.binding(), ldo.identifier()).
                                title(ldo.title()).
                                abstrakt(ldo.abstrakt()).build();

                        // TODO validate here!!!
                        element.setAccessible(true);
                        outputElementMap.put(ldo.identifier(), element);
                        algorithmBuilder.addOutputDesciptor(ldod);
                    }
                }
                {
                    ComplexDataOutput cdo = element.getAnnotation(ComplexDataOutput.class);
                    if (cdo != null) {
                        ComplexDataOutputDescriptor cdod =
                                ComplexDataOutputDescriptor.builder(
                                cdo.binding(),
                                cdo.identifier()).
                                title(cdo.title()).
                                abstrakt(cdo.abstrakt()).
                                build();

                        // TODO validate here!!!

                        element.setAccessible(true);
                        outputElementMap.put(cdo.identifier(), element);
                        algorithmBuilder.addOutputDesciptor(cdod);
                    }
                }
                {
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

                        // TODO validate here!!!

                        element.setAccessible(true);
                        inputElementMap.put(ldi.identifier(), element);
                        algorithmBuilder.addInputDesciptor(ldid);
                    }
                }
                {
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

                        // TODO validate here!!!

                        element.setAccessible(true);
                        inputElementMap.put(cdi.identifier(), element);
                        algorithmBuilder.addInputDesciptor(cdid);
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
    }

    private final static Map<Class<? extends IAlgorithm>, Introspector> introspectorMap =
            new HashMap<Class<? extends IAlgorithm>, Introspector>();
    public static synchronized Introspector getInstrospector(Class<? extends IAlgorithm> algorithmClass) {
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

    @Override
    public Map<String, IData> run(Map<String, List<IData>> inputMap) {
        Map<String, IData> oMap = new HashMap<String, IData>();

        // this is here as I plan on  separating the implementation
        // from the annotated process/algorithm, there's no reason it needs to me
        // a subclass (simply that way not for development convenience)
        IAlgorithm target = this;
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
                Class fieldType = iField.getType();
                // TODO: move to annotation parsing
                if (maxOccurs > 1 && !List.class.isAssignableFrom(fieldType)) {
                    throw new RuntimeException("Invalid setter parameter for INPUT " + iIdentifier + " maxOccurs > 1 and field is not of type List");
                }
                Object value = null;
                if (List.class.isAssignableFrom(fieldType)) {
                    List valueList = new ArrayList(boundList.size());
                    for (IData bound : inputMap.get(iIdentifier)) {
                        valueList.add(bound.getPayload());
                    }
                    value = valueList;
                } else if (boundList.size() == 1) {
                    value = boundList.get(0).getPayload();
                }
                iField.set(target, value);
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
                Class[] parameterTypes = iMethod.getParameterTypes();
                // TODO: move to annotation parsing
                if (parameterTypes.length != 1) {
                    throw new RuntimeException("Invalid setter method for INPUT " + iIdentifier + " incorrect argument count");
                }
                // TODO: move to annotation parsing
                if (maxOccurs > 1 && !List.class.isAssignableFrom(parameterTypes[0])) {
                    throw new RuntimeException("Invalid setter parameter for INPUT " + iIdentifier + " maxOccurs > 1 and method parameter is not of type List");
                }
                Object value = null;
                if (List.class.isAssignableFrom(parameterTypes[0])) {
                    List valueList = new ArrayList(boundList.size());
                    for (IData bound : inputMap.get(iIdentifier)) {
                        valueList.add(bound.getPayload());
                    }
                    value = valueList;
                } else if (boundList.size() == 1) {
                    value = boundList.get(0).getPayload();
                }
                iMethod.invoke(target, value);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }

        try {
            introspector.getProcessMethod().invoke(target);
        } catch (Exception ex) {
                ex.printStackTrace();
        }

        for (Map.Entry<String, Field> oEntry : introspector.getOutputFieldMap().entrySet()) {
            String oIdentifier = oEntry.getKey();
            Field oField = oEntry.getValue();
            try {
                Object value = oField.get(target);
                Class<?> bindingClass = getAlgorithmDescriptor().getOutputDescriptor(oIdentifier).getBinding();
                Constructor bindingConstructor = bindingClass.getConstructor(value.getClass());
                Object binding = bindingConstructor.newInstance(value);
                oMap.put(oEntry.getKey(), (IData) binding);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
        for (Map.Entry<String, Method> oEntry : introspector.getOutputMethodMap().entrySet()) {
            String oIdentifier = oEntry.getKey();
            Method oMethod = oEntry.getValue();
            try {
                Object value = oMethod.invoke(target);
                Class<?> bindingClass = getAlgorithmDescriptor().getOutputDescriptor(oIdentifier).getBinding();
                Constructor bindingConstructor = bindingClass.getConstructor(value.getClass());
                Object binding = bindingConstructor.newInstance(value);
                oMap.put(oEntry.getKey(), (IData) binding);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
        return oMap;
    }

}
