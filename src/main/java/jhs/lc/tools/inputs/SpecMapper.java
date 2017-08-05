package jhs.lc.tools.inputs;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Map;

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class SpecMapper {
	private static final ObjectMapper DEFAULT_MAPPER = new ObjectMapper()
			.enable(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY)
			.enable(DeserializationFeature.FAIL_ON_NULL_FOR_PRIMITIVES);

	private static final ObjectMapper PARAMS_MAPPER = new ObjectMapper()
			.enable(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY)
			.disable(DeserializationFeature.FAIL_ON_NULL_FOR_PRIMITIVES);

	private static ObjectMapper getObjectMapper() {
		return DEFAULT_MAPPER;
	}
	
	public static SimSpec parseSimSpec(File inFile) throws JsonMappingException, JsonParseException, IOException {
		ObjectMapper mapper = getObjectMapper();
		return mapper.readValue(inFile, SimSpec.class);
	}
	
	public static OptSpec parseOptSpec(File inFile) throws JsonMappingException, JsonParseException, IOException {
		ObjectMapper mapper = getObjectMapper();
		return mapper.readValue(inFile, OptSpec.class);
	}
	
	public static void writeObject(File outFile, Object spec) throws JsonMappingException, JsonGenerationException, IOException {
		ObjectMapper mapper = getObjectMapper();
		mapper.writerWithDefaultPrettyPrinter().writeValue(outFile, spec);		
	}
	
	public static void writeObject(OutputStream out, Object spec) throws JsonMappingException, JsonGenerationException, IOException {
		ObjectMapper mapper = getObjectMapper();
		ByteArrayOutputStream bout = new ByteArrayOutputStream();
		mapper.writerWithDefaultPrettyPrinter().writeValue(bout, spec);		
		out.write(bout.toByteArray());
	}
	
	public static <T> T mapToPojo(Map<String, Object> map, Class<T> clazz) {
		return PARAMS_MAPPER.convertValue(map, clazz);
	}
}
