package gov.usgs.gdp.analysis.grid;

public class GridCellVisitorMock extends GridCellVisitor {

	private int processCount = 0;
	private int traverseCount = 0;
	private int tCount = 0;
	private int zCount = 0;
	private int yxCount = 0;
	
	@Override
	public void processGridCell(int xCellIndex, int yCellIndex, double value) {
		processCount++;
	}
	
    public void traverseStart() {}
    public void traverseEnd() {
    	traverseCount++;
    }

    public void tStart(int tIndex) {}
    public void tEnd(int tIndex) {
    	tCount++;
    }

    public void zStart(int zIndex) {}
    public void zEnd(int zIndex) {
    	zCount++;
    }

    public void yxStart() {}
    public void yxEnd() {
    	yxCount++;
    }
	
	public int getProcessCount() {
		return processCount;
	}

	public int getTraverseCount() {
		return traverseCount;
	}

	public int getTCount() {
		return tCount;
	}

	public int getZCount() {
		return zCount;
	}

	public int getYXCount() {
		return yxCount;
	}
}
