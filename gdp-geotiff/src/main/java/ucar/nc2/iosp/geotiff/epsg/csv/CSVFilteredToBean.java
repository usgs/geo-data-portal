package ucar.nc2.iosp.geotiff.epsg.csv;

import au.com.bytecode.opencsv.CSVReader;
import au.com.bytecode.opencsv.bean.CsvToBean;
import au.com.bytecode.opencsv.bean.MappingStrategy;
import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import ucar.nc2.iosp.geotiff.epsg.csv.CSVFilteredMappingStrategy.Filter;

class CSVFilteredToBean<T> extends CsvToBean<T> {

    private final static int FILTER_INDEX_UNSET = -1;
    private final static int FILTER_INDEX_UNUSED = -2;

    private int filteredIndex = FILTER_INDEX_UNSET;

    @Override
    protected T processLine(MappingStrategy<T> ms, String[] line) throws IllegalAccessException, InvocationTargetException, InstantiationException, IntrospectionException {
        if (filteredIndex == FILTER_INDEX_UNSET) {
            filteredIndex = findFilteredIndex(ms, line.length);
        }
        if (filteredIndex == FILTER_INDEX_UNUSED) {
            return processLineX(ms, line);
        }
        Filter filter = ((CSVFilteredMappingStrategy)ms).getFilter();
        if (filteredIndex < line.length && filter.accept(line[filteredIndex])) {
            return processLineX(ms, line);
        }
        return null;

    }

    // this is the original implementation of processLine(...) with a modification
    // to check to avoid parsing beyond line.length.
    protected T processLineX(MappingStrategy<T> mapper, String[] line) throws IllegalAccessException, InvocationTargetException, InstantiationException, IntrospectionException {
    	T bean = mapper.createBean();
        for(int col = 0; col < line.length; col++) {
            String value = line[col];
            PropertyDescriptor prop = mapper.findDescriptor(col);
            if (null != prop) {
                Object obj = convertValue(value, prop);
                prop.getWriteMethod().invoke(bean, obj);
            }
        }
        return bean;
    }

    private int findFilteredIndex(MappingStrategy<T> ms, int cc) {
        if (ms instanceof CSVFilteredMappingStrategy) {
            CSVFilteredMappingStrategy fms = (CSVFilteredMappingStrategy)ms;
            Filter f = fms.getFilter();
            if (f == null) {
                return FILTER_INDEX_UNUSED;
            }
            String fpn = f.getPropertyName();
            if (fpn == null || fpn.length() == 0) {
                return FILTER_INDEX_UNUSED;
            }
            for (int ci = 0; ci < cc; ++ci) {
                String pn = fms.getPropertyName(ci);
                if (fpn.equals(pn)) {
                    return ci;
                }
            }
            return FILTER_INDEX_UNUSED;
        }
        return FILTER_INDEX_UNUSED;
    }

    @Override
    protected Object convertValue(String string, PropertyDescriptor pd) throws InstantiationException, IllegalAccessException {
        if (string == null || string.length() == 0) {
            Class clazz = pd.getPropertyType();
            if (clazz == Boolean.class || clazz == Boolean.TYPE) {
                return Boolean.FALSE;
            }
            if (clazz == Byte.class || clazz == Byte.TYPE) {
                return Byte.MIN_VALUE;
            }
            if (clazz == Short.class || clazz == Short.TYPE) {
                return Short.MIN_VALUE;
            }
            if (clazz == Integer.class || clazz == Integer.TYPE) {
                return Integer.MIN_VALUE;
            }
            if (clazz == Long.class || clazz == Long.TYPE) {
                return Long.MIN_VALUE;
            }
            if (clazz == Float.class || clazz == Float.TYPE) {
                return Float.NaN;
            }
            if (clazz == Double.class || clazz == Double.TYPE) {
                return Double.NaN;
            } else {
                return super.convertValue(string, pd);
            }
        }
        return super.convertValue(string, pd);
    }



    @Override
    public List<T> parse(MappingStrategy<T> mapper, CSVReader csv) {
        filteredIndex = FILTER_INDEX_UNSET;
        try {
            mapper.captureHeader(csv);
            String[] line;
            List<T> list = new ArrayList<T>();
            while(null != (line = csv.readNext())) {
                T obj = processLine(mapper, line);
                if (obj != null) {
                    list.add(obj);
                }
            }
            return list;
        } catch (Exception e) {
            throw new RuntimeException("Error parsing CSV!", e);
        }
    }

}
