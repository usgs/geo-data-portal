/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package gov.usgs.cida.gdp.coreprocessing;

/**
 *
 * @author admin
 */
public enum DelimiterOption {

        c("[comma]", ","), t("[tab]", "\t"), s("[space]", " ");
        public final String description;
        public final String delimiter;

        private DelimiterOption(String description, String value) {
            this.description = description;
            this.delimiter = value;
        }

        public static DelimiterOption getDefault() {
            return c;
        }

        @Override
        public String toString() {
            return description;
        }
}
