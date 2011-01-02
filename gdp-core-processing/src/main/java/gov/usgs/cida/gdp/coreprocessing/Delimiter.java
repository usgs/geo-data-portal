package gov.usgs.cida.gdp.coreprocessing;

/**
 *
 * @author tkunicki
 */
public enum Delimiter {

        COMMA(","),
        TAB("\t"),
        SPACE(" ");

        public final String delimiter;

        private Delimiter(String value) {
            this.delimiter = value;
        }

        public static Delimiter getDefault() {
            return COMMA;
        }

}
