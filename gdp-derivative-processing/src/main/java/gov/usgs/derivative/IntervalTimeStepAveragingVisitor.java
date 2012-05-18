package gov.usgs.derivative;

import gov.usgs.derivative.time.IntervalTimeStepDescriptor;
import gov.usgs.derivative.time.NetCDFDateUtil;
import gov.usgs.derivative.time.TimeStepDescriptor;
import java.util.List;
import org.joda.time.Interval;
import ucar.nc2.dt.GridDatatype;

/**
 *
 * @author tkunicki
 */
public class IntervalTimeStepAveragingVisitor extends AbstractTimeStepAveragingVisitor {

    private List<Interval> intervalList;

    public IntervalTimeStepAveragingVisitor(List<Interval> intervalList) {
        this.intervalList = intervalList;
    }
   
    @Override
    protected TimeStepDescriptor generateDerivativeTimeStepDescriptor(List<GridDatatype> gridDatatypeList) {
        return new IntervalTimeStepDescriptor(
            NetCDFDateUtil.toIntervalUTC(gridDatatypeList.get(0)),
            intervalList); 
    }
    
}
