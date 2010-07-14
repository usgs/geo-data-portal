package ucar.nc2.iosp.geotiff.epsg.csv;

import au.com.bytecode.opencsv.bean.HeaderColumnNameTranslateMappingStrategy;

/**
 *
 * @author tkunicki
 */
class CSVFilteredMappingStrategy<T> extends HeaderColumnNameTranslateMappingStrategy<T> {

    public static interface Filter {
        public String getPropertyName();
        public boolean accept(String columnValue);
    }

    private Filter filter;

    public void setFilter(Filter filter) {
        this.filter = filter;
    }

    public Filter getFilter() {
        return filter;
    }

    public String getPropertyName(int i) {
        return getColumnName(i);
    }


}
