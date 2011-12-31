/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package gov.usgs.derivative;

import java.util.List;

/**
 *
 * @author tkunicki
 */
public class DerivativeUtil {
    
    // bah, parameterize this as part of visitor later...
    public final static String DEFAULT_P1Y_PATH = "/Users/tkunicki/Downloads/derivatives/P1Y/";
    public final static String DEFAULT_P30Y_PATH = "/Users/tkunicki/Downloads/derivatives/P30Y/";
    public final static String DEFAULT_P30Y_DELTA_PATH = "/Users/tkunicki/Downloads/derivatives/P30Y-delta/";
    
    public static boolean[] generateMissingValuesMask(List<float[]> yxValuesList, List<Number> missingValueList) {
        int yxCount = yxValuesList.get(0).length;
        boolean[] mask = new boolean[yxCount];
        for (int index = 0; index < yxValuesList.size() ; ++index) {
            float[] yxValues = yxValuesList.get(index);
            float missingValue = missingValueList.get(index).floatValue();
            for (int yxIndex = 0; yxIndex < yxCount; ++yxIndex) {
                float value = yxValues[yxIndex];
                if (Float.isNaN(value) || value == missingValue) {
                    mask[yxIndex] = true;
                }
            }
        }
        return mask;
    }
    
}
