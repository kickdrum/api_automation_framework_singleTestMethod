package com.kickdrum.entrypoint;

import static io.restassured.RestAssured.given;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.testng.ITestResult;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import com.aventstack.extentreports.ExtentReports;
import com.aventstack.extentreports.ExtentTest;
import com.aventstack.extentreports.Status;
import com.aventstack.extentreports.markuputils.ExtentColor;
import com.aventstack.extentreports.markuputils.Markup;
import com.aventstack.extentreports.markuputils.MarkupHelper;
import com.aventstack.extentreports.reporter.ExtentSparkReporter;
import com.aventstack.extentreports.reporter.configuration.Theme;
import com.fasterxml.jackson.databind.JsonNode;
import com.kickdrum.common.RestUtilities;
import com.kickdrum.common.Utilities;
import com.kickdrum.constants.Endpoints;
import com.kickdrum.constants.Path;

import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import io.restassured.specification.ResponseSpecification;

public class APIAutomationTest {
	RequestSpecification reqSpec;
	ResponseSpecification resSpec;

	private ExtentSparkReporter sparkReporter;
	private ExtentReports report;
	private ExtentTest test;

	private String testName;

	private static final Logger log = LogManager.getLogger(APIAutomationTest.class.getName());

	// Path of Request & Response JSON files (Test Case files)
	String testCaseFilesDir = System.getProperty("user.dir") + Utilities.getAppProperty("JSON_FILE_DIR");

	@BeforeClass
	public void setup() throws InterruptedException {
		log.info(
				"\n**************************************************************** API TEST AUTOMATION STARTED ****************************************************************");

		reqSpec = RestUtilities.getRequestSpecification();
		reqSpec.basePath(Path.BASE_PATH);
		resSpec = RestUtilities.getResponseSpecification();

		sparkReporter = new ExtentSparkReporter(System.getProperty("user.dir") + "./Reports/APIAutomationTestReport.html");
		sparkReporter.config().setEncoding("utf-8");
		sparkReporter.config().setDocumentTitle("API Automation Test Report");
		sparkReporter.config().setReportName("API Automation Test Report");
		sparkReporter.config().setTheme(Theme.DARK);

		report = new ExtentReports();
		report.setSystemInfo("Project Name", "Sample API Automation Framework - Single Test Method");
		report.setSystemInfo("Organisation", "Kickdrum");
		report.setSystemInfo("Customer", "Customer Name");
		report.attachReporter(sparkReporter);

	}

	@DataProvider(name = "RequestJsons_Provider")
	private Object[] getTestCaseList() {
		File testCaseFilesDirPath = new File(testCaseFilesDir);
		// Get the Request File details in form of {Path, Test Name from REquest JSON
		// file name}
		List<String> TEST_FILES = Utilities.getRequestFileNames(testCaseFilesDirPath);
		// Convert the Map in {key=value} format to Object Array in [key=value] format
		Object[] objArray = TEST_FILES.toArray();
		return objArray;
	}

	@Test(dataProvider = "RequestJsons_Provider")
	public void apiTest(String ReqFileName) throws FileNotFoundException, IOException {
		JsonNode actualResponseJsonNode = null;
		JsonNode expectedResponseJsonNode = null;
		
		// Extract the Test Name from the input Request JSON file name
		testName = Utilities.trimFileName(ReqFileName);

		test = report.createTest("API automation for Test Name = " + testName);
		log.info("Starting API automation for Test Name = " + testName);
		
		// Get the JSON Root Node for the Request JSON file
		JsonNode reqJsonNode = Utilities.getJSONFileRootNode(testCaseFilesDir, testName + "-request.json");
		
		// Perform the API Call and store the response
		if (reqJsonNode != null) {
			Response actualResponse = performAPICall(Endpoints.LOGIN, reqJsonNode);

			// Get the JSON Root Node for the Actual Response from the API call
			actualResponseJsonNode = Utilities.getJSONStreamRootNode(actualResponse.asString());

			// Get the JSON Root Node for the Expected Response from the JSON file
			expectedResponseJsonNode = Utilities.getJSONFileRootNode(testCaseFilesDir,	testName + "-response.json");

			// Comparing the Actual Response and Expected Response
			test.info("Comparing the Actual Response and Expected Response");
			
			assertTrue(actualResponseJsonNode.equals(expectedResponseJsonNode));
		}
	}

	/***
	 * Method to update Log, Extent Report and screenshot based on Test status of
	 * each method
	 * 
	 * @param result
	 * @throws Exception
	 */
	@AfterMethod
	public void postTestMethodTasks(ITestResult result) throws Exception {
		String methodName = result.getMethod().getMethodName();

		if (result.getStatus() == ITestResult.FAILURE) {
			log.error(methodName + ": failed");

			String exceptionMessage = Arrays.toString(result.getThrowable().getStackTrace());
			test.fail("<details><summary><b><font color=red>Exception occurred, click to see details:"
					+ "</font></b></summary>" + exceptionMessage.replaceAll(",", "<br>") + "</details> \n");

			String logText = "<b>Test Method " + methodName + " Failed</b>";
			Markup m = MarkupHelper.createLabel(logText, ExtentColor.RED);
			test.log(Status.FAIL, m);
		} else if (result.getStatus() == ITestResult.SUCCESS) {
			log.info(methodName + ": passed");

			String logText = "<b>Test Method " + methodName + " Successful</b>";
			Markup m = MarkupHelper.createLabel(logText, ExtentColor.GREEN);
			test.log(Status.PASS, m);
		} else if (result.getStatus() == ITestResult.SKIP) {
			log.info(methodName + ": skipped");

			String logText = "<b>Test Method " + methodName + " Skipped</b>";
			Markup m = MarkupHelper.createLabel(logText, ExtentColor.GREY);
			test.log(Status.SKIP, m);
		}

	}

	/**
	 * Performs the API call to the server
	 * 
	 * @param endpoint
	 * @param reqJsonNode
	 * @return
	 */
	private Response performAPICall(String endpoint, JsonNode reqJsonNode) {
		Response res = null;
		test.info("Performing API call for : " + endpoint);

		try {
			res = given().spec(reqSpec).body(reqJsonNode).when().post(endpoint).then().spec(resSpec).extract()
					.response();
		} catch (AssertionError e) {
			log.fatal("Error while making API call: " + e.getMessage());
			test.fail("Error while making API call: " + e.getMessage());
		}
		
		return res;
	}

	/**
	 * Final Method to generate the HTML report
	 */
	@AfterClass(alwaysRun = true)
	public void tearDown() {
		log.info("\n********************************************************** API TEST AUTOMATION COMPLETED **********************************************************\n\n\n");

		report.flush();
		log.info("Extent Report for API Automation was generated");
	}

}
