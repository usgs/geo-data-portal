package gov.usgs.cida.gdp.wps.algorithm;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author tkunicki
 */
public class ClassUtil {

    private final static Map<Class<?>, Class<?>> TO_WRAPPER;
    private final static Map<Class<?>, Class<?>> FROM_WRAPPER;

    static {
        Map<Class<?>, Class<?>> to = new HashMap<Class<?>, Class<?>>();
        to.put(Float.TYPE, Float.class);
        to.put(Double.TYPE, Double.class);
        to.put(Byte.TYPE, Byte.class);
        to.put(Short.TYPE, Short.class);
        to.put(Integer.TYPE, Integer.class);
        to.put(Long.TYPE, Long.class);
        to.put(Character.TYPE, Character.class);
        to.put(Boolean.TYPE, Boolean.class);
        TO_WRAPPER = Collections.unmodifiableMap(to);

        Map<Class<?>, Class<?>> from = new HashMap<Class<?>, Class<?>>();
        to.put(Float.class, Float.TYPE);
        to.put(Double.class, Double.TYPE);
        to.put(Byte.class, Byte.TYPE);
        to.put(Short.class, Short.TYPE);
        to.put(Integer.class, Integer.TYPE);
        to.put(Long.class, Long.TYPE);
        to.put(Character.class, Character.TYPE);
        to.put(Boolean.class, Boolean.TYPE);
        FROM_WRAPPER = Collections.unmodifiableMap(from);

    }


    public static <T extends Enum<T>> List<T> convertStringToEnumList(Class<T> enumType, List<String> stringList) {
        List<T> enumList = new ArrayList<T>();
        for (String string : stringList) {
            enumList.add(Enum.valueOf(enumType, string));
        }
        return enumList;
    }

    public static <T extends Enum<T>> String[] convertEnumToStringArray(Class<T> enumType) {
        String[] strings = null;
        T[] constants = enumType.getEnumConstants();
        if (constants != null && constants.length > 0) {
            strings = new String[constants.length];
            for (int index = 0; index < constants.length; ++index) {
                strings[index] = constants[index].name();
            }
        }
        return strings;
    }

    public static boolean isWrapper(Class<?> clazz) {
        return FROM_WRAPPER.containsKey(clazz);
    }

    public static Class<?> unwrap(Class<?> clazz) {
        return clazz.isPrimitive() ? FROM_WRAPPER.get(clazz) : clazz;
    }

    public static Class<?> wrap(Class<?> clazz) {
        return clazz.isPrimitive() ? TO_WRAPPER.get(clazz) : clazz;
    }
    
}
