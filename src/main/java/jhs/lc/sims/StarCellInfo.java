package jhs.lc.sims;

import java.util.ArrayList;
import java.util.List;

import jhs.lc.geom.LimbDarkeningParams;
import jhs.lc.geom.SolidSphere;
import jhs.lc.geom.Sphere;
import jhs.math.util.ListUtil;

public class StarCellInfo {
	private static final int N_PER_CELL = 5;
	private final int widthPixels, heightPixels;
	private final StarCell[] cells;
	private final double totalFlux;
	
	public StarCellInfo(int widthPixels, int heightPixels, StarCell[] cells, double totalFlux) {
		super();
		this.widthPixels = widthPixels;
		this.heightPixels = heightPixels;
		this.cells = cells;
		this.totalFlux = totalFlux;
	}

	public static StarCellInfo create(LimbDarkeningParams ldParams, int widthPixels, int heightPixels) {
		double cellWidth = 2.0 / widthPixels;
		double cellHeight = 2.0 / heightPixels;
		double innerCellWidth = cellWidth / N_PER_CELL;
		double innerCellHeight = cellHeight / N_PER_CELL;
		double offset = -(N_PER_CELL - 1.0) / 2.0;
		Sphere sphere = new SolidSphere(1.0, ldParams);
		List<StarCell> cellList = new ArrayList<>();
		double totalFlux = 0;
		for(int c = 0; c < widthPixels; c++) {
			double x = -1.0 + (c + 0.5) * cellWidth;
			for(int r = 0; r < heightPixels; r++) {
				double y = -1.0 + (r + 0.5) * cellHeight;
				double flux = 0;
				// TODO should be possible to calculate more accurately with integration.
				for(int i = 0; i < N_PER_CELL; i++) {
					double cx = x + innerCellWidth * (i + offset);
					for(int j = 0; j < N_PER_CELL; j++) {
						double cy = y + innerCellHeight * (j + offset);
						double b = sphere.getBrightness(cx, cy, true);
						if(!Double.isNaN(b)) {
							flux += b;
						}
					}
				}
				if(flux > 0) {
					cellList.add(new StarCell((float) flux, (float) x, (float) y));
					totalFlux += flux;
				}		
			}
		}
		return new StarCellInfo(widthPixels, heightPixels, ListUtil.asArray(cellList, StarCell.class), totalFlux);
	}

	public final StarCell[] getCells() {
		return cells;
	}

	public final double getTotalFlux() {
		return totalFlux;
	}

	public final int getWidthPixels() {
		return widthPixels;
	}

	public final int getHeightPixels() {
		return heightPixels;
	}
}
