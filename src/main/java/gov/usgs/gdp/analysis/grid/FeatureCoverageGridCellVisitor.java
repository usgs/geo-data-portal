package gov.usgs.gdp.analysis.grid;

import java.util.Map;

public abstract class FeatureCoverageGridCellVisitor extends GridCellVisitor {

    protected Map<Object, GridCellCoverage> attributeCoverageMap;

    public FeatureCoverageGridCellVisitor(Map<Object, GridCellCoverage> attributeCoverageMap) {
        this.attributeCoverageMap = attributeCoverageMap;
    }

    @Override
    public void processGridCell(int xCellIndex, int yCellIndex, double value) {
        double coverageTotal = 0;
        for (Map.Entry<Object, GridCellCoverage> entry : attributeCoverageMap.entrySet()) {
            Object attribute = entry.getKey();
            GridCellCoverage gridCellCoverage = entry.getValue();
            double coverage = gridCellCoverage.getCellCoverageFraction(xCellIndex, yCellIndex);
            if (coverage > 0.0) {
                processPerAttributeGridCellCoverage(value, coverage, attribute);
            }
            coverageTotal += coverage;
        }
        if (coverageTotal > 0.0) {
            processAllAttributeGridCellCoverage(value, coverageTotal);
        }
    }

    public abstract void processPerAttributeGridCellCoverage(double value, double coverage, Object attribute);

    public abstract void processAllAttributeGridCellCoverage(double value, double coverage);
}
