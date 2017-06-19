package jhs.lc.tools;

import org.apache.commons.cli.CommandLine;

public abstract class AbstractTool {
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
	
}
