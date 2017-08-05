package jhs.lc.tools;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.cli.CommandLine;

public abstract class AbstractTool {
	protected static final int DEF_OUT_NUM_PIXELS = 100000;

	protected int getOptionInt(CommandLine cmdLine, String option, int defaultValue) {
		String textValue = cmdLine.getOptionValue(option);
		if(textValue == null) {
			return defaultValue;
		}
		try {
			return Integer.parseInt(textValue);
		} catch(NumberFormatException nfe) {
			throw new IllegalStateException("Option " + option + " requires an integer value.");
		}
	}

	protected double getOptionDouble(CommandLine cmdLine, String option, double defaultValue) {
		String textValue = cmdLine.getOptionValue(option);
		if(textValue == null) {
			return defaultValue;
		}
		try {
			return Double.parseDouble(textValue);
		} catch(NumberFormatException nfe) {
			throw new IllegalStateException("Option " + option + " requires a numeric value.");
		}
	}
	
	protected Double getOptionDouble(CommandLine cmdLine, String option, Double defaultValue) {
		String textValue = cmdLine.getOptionValue(option);
		if(textValue == null) {
			return defaultValue;
		}
		try {
			return Double.parseDouble(textValue);
		} catch(NumberFormatException nfe) {
			throw new IllegalStateException("Option " + option + " requires an numeric value.");
		}
	}
	
	protected void configureLoggingLevel(CommandLine cmdLine) {
		Level level = Level.WARNING;
		try {
			String logText = cmdLine.getOptionValue("log");
			if(logText != null) {
				level = Level.parse(logText);
			}
		} finally {
			Logger.getLogger("").setLevel(level);
		}
	}
	
}
