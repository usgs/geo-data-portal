/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package gov.usgs.cida.gdp.wps.algorithm.descriptor;

import org.n52.wps.io.data.ILiteralData;
import org.n52.wps.io.data.binding.literal.LiteralAnyURIBinding;
import org.n52.wps.io.data.binding.literal.LiteralBase64BinaryBinding;
import org.n52.wps.io.data.binding.literal.LiteralBooleanBinding;
import org.n52.wps.io.data.binding.literal.LiteralByteBinding;
import org.n52.wps.io.data.binding.literal.LiteralDateBinding;
import org.n52.wps.io.data.binding.literal.LiteralDateTimeBinding;
import org.n52.wps.io.data.binding.literal.LiteralDoubleBinding;
import org.n52.wps.io.data.binding.literal.LiteralDurationBinding;
import org.n52.wps.io.data.binding.literal.LiteralFloatBinding;
import org.n52.wps.io.data.binding.literal.LiteralIntBinding;
import org.n52.wps.io.data.binding.literal.LiteralShortBinding;
import org.n52.wps.io.data.binding.literal.LiteralStringBinding;
import org.n52.wps.io.data.binding.literal.LiteralTimeBinding;

/**
 *
 * @author tkunicki
 */
public class LiteralDataDescriptorUtility {
    
    public static String dataTypeForBinding(Class<? extends ILiteralData> bindingClass) {
        if (bindingClass == LiteralAnyURIBinding.class) return "xs:anyURI";
        else if (bindingClass == LiteralBase64BinaryBinding.class) return "xs:base64Binary";
        else if (bindingClass == LiteralBooleanBinding.class) return "xs:boolean";
        else if (bindingClass == LiteralByteBinding.class) return "xs:byte";
        else if (bindingClass == LiteralDateBinding.class) return "xs:date";
        else if (bindingClass == LiteralTimeBinding.class) return "xs:time";
        else if (bindingClass == LiteralDateTimeBinding.class) return "xs:dateTime";
        else if (bindingClass == LiteralDurationBinding.class) return "xs:duration";
        else if (bindingClass == LiteralDoubleBinding.class) return "xs:double";
        else if (bindingClass == LiteralFloatBinding.class) return "xs:float";
        else if (bindingClass == LiteralIntBinding.class) return "xs:int";
        else if (bindingClass == LiteralShortBinding.class) return "xs:short";
        else if (bindingClass == LiteralStringBinding.class) return "xs:string";
        else return null;
    }

}
