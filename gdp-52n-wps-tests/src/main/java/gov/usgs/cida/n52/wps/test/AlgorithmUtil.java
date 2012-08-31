/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package gov.usgs.cida.n52.wps.test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import net.opengis.wps.x100.ProcessDescriptionType;
import org.apache.xmlbeans.XmlOptions;
import org.apache.xmlbeans.XmlValidationError;
import org.n52.wps.server.IAlgorithm;

/**
 *
 * @author tkunicki
 */
public class AlgorithmUtil {
 
    public static String convertProcessDescriptionToXMLString(IAlgorithm algorithm) {
        return convertProcessDescriptionToXMLString(algorithm.getDescription());
    }
    
    public static String convertProcessDescriptionToXMLString(ProcessDescriptionType decription) {
        XmlOptions options = new XmlOptions();
        options.setSavePrettyPrint();
        options.setSaveOuter();
        HashMap ns = new HashMap();
        ns.put("http://www.opengis.net/wps/1.0.0", "wps");
        ns.put("http://www.opengis.net/ows/1.1", "ows");
        options.setSaveNamespacesFirst().
                setSaveSuggestedPrefixes(ns).
                setSaveAggressiveNamespaces();
        return decription.xmlText(options);
    }
    
    public static boolean processDescriptionIsValid(IAlgorithm algorithm) {
        return processDescriptionIsValid(algorithm.getDescription());
    }
    
    public static boolean processDescriptionIsValid(ProcessDescriptionType processDescirption) {
        XmlOptions xmlOptions = new XmlOptions();
            List<XmlValidationError> xmlValidationErrorList = new ArrayList<XmlValidationError>();
                xmlOptions.setErrorListener(xmlValidationErrorList);
            boolean valid = processDescirption.validate(xmlOptions);
            if (!valid) {
                System.out.println("Error validating process description for " + processDescirption.getIdentifier().getStringValue());
                for (XmlValidationError xmlValidationError : xmlValidationErrorList) {
                    System.out.println("\tMessage: " + xmlValidationError.getMessage());
                   System.out.println("\tLocation of invalid XML: " + 
                         xmlValidationError.getCursorLocation().xmlText());
                }
            }
            return valid;
    }
    
}
