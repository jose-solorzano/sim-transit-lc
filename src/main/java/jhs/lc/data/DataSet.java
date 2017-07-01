package jhs.lc.data;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import jhs.math.util.ArrayUtil;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

public class DataSet {
	public static LightCurvePoint[] load(File csvFile) throws IOException {
		List<LightCurvePoint> pointList = new ArrayList<>();
		Reader reader = new FileReader(csvFile);
		CSVParser parser = new CSVParser(reader, CSVFormat.EXCEL.withHeader());
		try {
			Map<String,Integer> headers = parser.getHeaderMap();
			if(headers == null) {
				throw new IllegalStateException("No headers in CSV file " + csvFile + ".");
			}
			Integer timestampIdx = headers.get("Timestamp");
			if(timestampIdx == null) {
				timestampIdx = headers.get("Time");						
				if(timestampIdx == null) {
					throw new IllegalStateException("CSV file is missing 'Timestamp' header.");
				}
			}
			Integer fluxIdx = headers.get("Flux");
			if(fluxIdx == null) {
				fluxIdx = headers.get("n_flux");
				if(fluxIdx == null) {
					throw new IllegalStateException("CSV file is missing 'Flux' header.");
				}
			}
			for(CSVRecord record : parser) {
				String tsText = record.get(timestampIdx);
				String fluxText = record.get(fluxIdx);
				double timestamp = Double.parseDouble(tsText);
				double flux = Double.parseDouble(fluxText);
				pointList.add(new LightCurvePoint(timestamp, flux));
			}
		} finally {
			parser.close();
			reader.close();
		}
		LightCurvePoint[] points = ArrayUtil.fromCollection(pointList, LightCurvePoint.class);
		Arrays.sort(points);
		return points;
	}
}
