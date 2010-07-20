package gov.usgs.gdp.analysis.grid;

/**
 * @author jordan
 * I realize that this class is pretty much just to test the default Visitor
 * functions, and that they don't really do anything currently.
 * So this isn't very useful, but I'm going to leave it in place anyway.
 */
public class GridCellVisitorInheritMock extends GridCellVisitor {

	private int processCount = 0;

	@Override
	public void processGridCell(int xCellIndex, int yCellIndex, double value) {
		processCount++;
	}
	
	public int getProcessCount() {
		return processCount;
	}
}
