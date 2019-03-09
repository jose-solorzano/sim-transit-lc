package jhs.lc.sims;

import static org.junit.Assert.*;

import org.junit.Test;

import jhs.lc.geom.LimbDarkeningParams;

public class TestStarCellInfo {

	@Test
	public void testSymmetry() {
		LimbDarkeningParams ldParams = LimbDarkeningParams.SUN;
		StarCellInfo sci = StarCellInfo.create(ldParams, 2, 2);
		StarCell[] cells = sci.getCells();
		double sumFlux = 0;
		for(int i = 0; i < cells.length; i++) {
			StarCell cell1 = cells[i];
			sumFlux += cell1.flux;
			for(int j = i + 1; j < cells.length; j++) {
				StarCell cell2 = cells[j];
				assertEquals("Mismatch " + i + ", " + j, cell1.flux,  cell2.flux, 0.0001);
			}
		}
		assertEquals(sci.getTotalFlux(), sumFlux, 0.0001);
	}

}
