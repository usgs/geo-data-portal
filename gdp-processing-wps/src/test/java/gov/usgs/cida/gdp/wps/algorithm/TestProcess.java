package gov.usgs.cida.gdp.wps.algorithm;

import gov.usgs.cida.gdp.wps.algorithm.annotation.Algorithm;
import gov.usgs.cida.gdp.wps.algorithm.annotation.LiteralDataInput;
import gov.usgs.cida.gdp.wps.algorithm.annotation.LiteralDataOutput;
import gov.usgs.cida.gdp.wps.algorithm.annotation.Process;
import java.util.Iterator;
import java.util.List;
import org.n52.wps.io.data.binding.literal.LiteralStringBinding;

/**
 *
 * @author tkunicki
 */
@Algorithm(
    title="TestProcessTitle",
    abstrakt="TestProcessAbstract",
    version="1.0.0",
    storeSupported=true,
    statusSupported=true)
public class TestProcess extends AbstractAnnotatedAlgorithm {

    @LiteralDataInput(
        identifier="LITERAL_LIST_FIELD",
        minOccurs=0,
        maxOccurs=4,
        binding=LiteralStringBinding.class)
    private List<String> fieldStrings;

    @LiteralDataInput(
        identifier="LITERAL_FIELD",
        binding=LiteralStringBinding.class)
    private String fieldString;

    @LiteralDataOutput(
        identifier="CONCATENATION_FIELD",
        binding=LiteralStringBinding.class)
    private String fieldOutput;

    //
    private List<String> methodStrings;
    private String methodString;
    private String methodOutput;

    @Process
    public void process() {
        StringBuilder sb = new StringBuilder();
        sb.append("[");
        Iterator<String> i0 = fieldStrings.iterator();
        if (i0.hasNext()) { sb.append(i0.next()); } while (i0.hasNext()) { sb.append("|").append(i0.next()); }
        sb.append("]:[");
        if (fieldString != null && fieldString.length() > 0) { sb.append(fieldString); }
        sb.append("]:[");
        if (methodString != null && methodString.length() > 0) { sb.append(methodString); }
        sb.append("]:[");
        Iterator<String> i1 = methodStrings.iterator();
        if (i1.hasNext()) { sb.append(i1.next()); } while (i1.hasNext()) { sb.append("|").append(i1.next()); }
        sb.append("]");
        fieldOutput = sb.toString();
        methodOutput = sb.reverse().toString();
    }

    @LiteralDataInput(
        identifier="LITERAL_LIST_METHOD",
        minOccurs=0,
        maxOccurs=4,
        binding=LiteralStringBinding.class)
    public void setInput0(List<String> strings) {
        this.methodStrings = strings;
    }

   @LiteralDataInput(
        identifier="LITERAL_METHOD",
        binding=LiteralStringBinding.class)
    public void setInput1(String string) {
        this.methodString = string;
    }

    @LiteralDataOutput(
        identifier="CONCATENATION_METHOD",
        binding=LiteralStringBinding.class)
    public String getString() {
        return methodOutput;
    }

}
