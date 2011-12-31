package gov.usgs.derivative;

/**
 *
 * @author tkunicki
 */
public class OpenCLUtil {
    
    public final static int PADDING = 512;

    public static int pad(int length, int divisor) {
        int pad = divisor - (length % divisor);
        if (pad != divisor) {
            return pad + length;
        } else {
            return length;
        }
    }
    
    public static int pad(int length) {
        return pad(length, PADDING);
    }
}
