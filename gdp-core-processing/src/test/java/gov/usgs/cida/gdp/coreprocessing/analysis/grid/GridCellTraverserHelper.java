package gov.usgs.cida.gdp.coreprocessing.analysis.grid;

import gov.usgs.cida.gdp.coreprocessing.analysis.grid.GridCellTraverser;
import gov.usgs.cida.gdp.coreprocessing.analysis.grid.GridCellTraverser;
import ucar.nc2.dt.GridDatatype;

public class GridCellTraverserHelper extends GridCellTraverser {

	public GridCellTraverserHelper(GridDatatype gridDatatype) {
		super(gridDatatype);
	}
	
	public int getXCellCount() {
		return this.xCellCount;
	}
	
	public int getYCellCount() {
		return this.yCellCount;
	}
	
	public int getZCellCount() {
		return this.zCellCount;
	}
	
	public int getTCellCount() {
		return this.tCellCount;
	}
	
}
