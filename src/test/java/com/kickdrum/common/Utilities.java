package com.kickdrum.common;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Date;
import java.util.List;
import java.util.Properties;
import java.util.Random;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class Utilities {

	private static String jwToken;
	private final static Logger log = LogManager.getLogger(Utilities.class.getName());

	/***
	 * Method to return a random non-zero integer number within the given limit
	 * 
	 * @return
	 */
	public int getRandomInt(int limit) {
		Random random = new Random();
		int randomInteger = Math.abs(random.nextInt(limit));
		log.debug("Random integer generated as: " + randomInteger);
		return randomInteger;
	}

	/***
	 * Method to generate a random AlphaNumeric String
	 */
	public String getAlphaNumericString(int string_size) {
		String AlphaNumericString = "ABCDEFGHIJKLMNOPQRSTUVWXYZ" + "0123456789" + "abcdefghijklmnopqrstuvxyz";
		StringBuilder sb = new StringBuilder(string_size);

		for (int i = 0; i < string_size; i++) {
			int index = (int) (AlphaNumericString.length() * Math.random());
			sb.append(AlphaNumericString.charAt(index));
		}
		return sb.toString();
	}

	/***
	 * Method to read a property key value from application.properties file
	 * 
	 * @param propertyKey
	 * @return
	 */
	public static String getAppProperty(String propertyKey) {
		Properties props = new Properties();

		try {
			InputStream appProps = new FileInputStream(
					System.getProperty("user.dir") + "//src/test//resources//application.properties");
			props.load(appProps);
		} catch (IOException e) {
			log.error("*** The 'application.properties' file was not found");
			e.printStackTrace();
		}
		String result = props.getProperty(propertyKey);
		log.debug("Retrieved the value of " + propertyKey + " from 'application.properties' file");
		return String.valueOf(result);
	}

	/**
	 * Gets the timestamp in required format
	 * 
	 * @param format
	 */
	public static String getTimestampInFormat(String format) {
		log.debug("Getting Timestamp in format: " + format);
		Date date = new Date();
		SimpleDateFormat sdf = new SimpleDateFormat(format);
		return sdf.format(date);
	}

	/**
	 * Encodes the passed String value to Base64
	 * 
	 * @param input_string
	 * @return Base64 encoded value
	 */
	private static String encodeToBase64(String input_string) {
		byte[] encodedBytes = Base64.getEncoder().encode(input_string.getBytes());
		String encodedValue = new String(encodedBytes);
		log.debug("encodedBytes: " + encodedValue);
		return encodedValue;
	}

	/**
	 * Decodes the passed Base64 string value to String
	 * 
	 * @param nput_base64_string
	 * @return String
	 */
	private static String decodeFromBase64(String input_base64_string) {
		byte[] encodedBytes = Base64.getDecoder().decode(input_base64_string.getBytes());
		String decodedValue = new String(encodedBytes);
		log.debug("decodedBytes: " + decodedValue);
		return decodedValue;
	}

	/**
	 * Returns a list of file names that contain 'request'
	 * 
	 * @param dirPath
	 * @return List<Request JSON File Names>
	 */
	public static List<String> getRequestFileNames(File dirPath) {
		List<String> fileDetails = new ArrayList<String>();

		File[] listOfFiles = dirPath.listFiles();

		if (listOfFiles != null) {
			for (File file : listOfFiles) {
				if (file.isFile()) {
					if ((file.toString().contains("-request.json")) && (!file.isHidden())) {
						fileDetails.add(file.getName());
					}
				} else {
					fileDetails.addAll(getRequestFileNames(file));
				}
			}
		}
		log.debug("Found Request File List: " + fileDetails);
		return fileDetails;
	}

	/**
	 * Extracts the JIRA Issue Id from file name
	 * 
	 * @param fileName
	 * @return fileName
	 */
	public static String trimFileName(String fileName) {
		String[] namePieces = fileName.split("-");
		return namePieces[0];
	}

	/***
	 * To get the JSON Root Node of the input JSON file
	 * 
	 * @param directory
	 * @param fileName
	 * @return rootNode
	 * @throws FileNotFoundException
	 * @throws IOException
	 */
	public static JsonNode getJSONFileRootNode(String directory, String fileName) {
		JsonNode rootNode = null;
		ObjectMapper mapper = new ObjectMapper();
		try {
			rootNode = mapper.readTree(new FileInputStream(directory + fileName));			
			log.debug("JSON Root Node of the input file: " + directory + fileName);
			log.debug(rootNode);
		} catch (IOException e) {
			log.error("*** ERROR - File Not Found: " + directory + fileName);
			e.printStackTrace();
		}
		
		return rootNode;
	}

	/***
	 * To get the JSON Root Node of the input string content
	 * 
	 * @param content
	 * @return rootNode
	 * @throws FileNotFoundException
	 * @throws IOException
	 */
	public static JsonNode getJSONStreamRootNode(String content) throws IOException {
		ObjectMapper mapper = new ObjectMapper();
		JsonNode rootNode = mapper.readTree(content);
		log.debug("JSON Root Node of the parameter content: " + rootNode);
		return rootNode;
	}

	/***
	 * To get the String representation of the JsonRootNode
	 * 
	 * @param jsonRootNode
	 * @return
	 * @throws JsonProcessingException
	 */
	public static String getJSONBodyString(JsonNode jsonRootNode) throws JsonProcessingException {
		ObjectMapper mapper = new ObjectMapper();
		String jsonBody = mapper.writeValueAsString(jsonRootNode);
		return jsonBody;
	}

}
