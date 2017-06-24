package jhs.lc.tools.inputs;

import java.io.File;
import java.io.IOException;

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class SpecMapper {
	public static SimSpec parseSimSpec(File inFile) throws JsonMappingException, JsonParseException, IOException {
		ObjectMapper mapper = new ObjectMapper();
		return mapper.readValue(inFile, SimSpec.class);
	}
	
	public static OptSpec parseOptSpec(File inFile) throws JsonMappingException, JsonParseException, IOException {
		ObjectMapper mapper = new ObjectMapper();
		return mapper.readValue(inFile, OptSpec.class);
	}
	
	public static void writeOptResultsSpec(File outFile, OptResultsSpec spec) throws JsonMappingException, JsonGenerationException, IOException {
		ObjectMapper mapper = new ObjectMapper();
		mapper.writerWithDefaultPrettyPrinter().writeValue(outFile, spec);		
	}
}
