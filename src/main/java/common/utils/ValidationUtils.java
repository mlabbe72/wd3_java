package common.utils;

import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.io.FileUtils;
import org.openqa.selenium.Capabilities;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.testng.Reporter;
import org.testng.asserts.IAssert;
import org.testng.asserts.SoftAssert;
import org.testng.collections.Maps;

import common.env.TestProperties;
import common.testrail.TestRail;



/**
 * A Class for general validation utilities, including various
 * verify and assert methods.
 */
public class ValidationUtils extends SoftAssert implements TestProperties {
	private StringBuffer verificationErrors = new StringBuffer();
	
	// THE MAIN DRIVER - Thread Safe
	protected static ThreadLocal<WebDriver> driver = new ThreadLocal<WebDriver>();
	
	// THE LOCALIZATION VALUE - Set in BeforeMethod
	protected static ThreadLocal<locale> localizationValue = new ThreadLocal<locale>();
	public enum locale {
			EN, ES
	}

	@SuppressWarnings("rawtypes")
	private Map<AssertionError, IAssert> m_errors = Maps.newHashMap();
	ArrayList<Object> g_errors = new ArrayList<Object>();

	// Screenshot related
	private String imageFileName;
	private String stackClassName;
	private String stackMethodName;
	private int stackLineNum;
	private Map<AssertionError, String> imagePathList = Maps.newHashMap();
	private Map<AssertionError, String> imageNameList = Maps.newHashMap();
	protected static final String screenshotFileLocation = "target/screenshots/";
	public String screenshotAdditionalData = "";

	// TestRails related
	protected TestRail testRailAPI = new TestRail();
	public boolean recordTestRailResults = false;
	public String testRailPlanOrRun = "";
	public String testRailID = "";
	public boolean exceptionOccurred = false; // Defaults to false
	
	// Bamboo related
	public final String bambooURLBase = ""; 
	public String bambooBuildKey = "";
	public String bambooBuildNo = "";
	
	public ValidationUtils() {
		// Escape=false for reportng, allowing to log with html formatting.
		System.setProperty("org.uncommons.reportng.escape-output", "false");
	}
	
	// Returns instance of the current ThreadLocal WebDriver
	public WebDriver getDriver() {
		return driver.get();
	}

	@Override
	protected void doAssert(IAssert<?> assertCommand) {		
		onBeforeAssert(assertCommand);
		try {
			executeAssert(assertCommand);
			onAssertSuccess(assertCommand);
		} catch (AssertionError ex) {
			m_errors.put(ex, assertCommand);
			onAssertFailure(assertCommand, ex);
		} finally {
			onAfterAssert(assertCommand);
		}
	}
	
	/**
	 * AssertAll for asserts
	 * @param testCases
	 * 		List of TestRails cases associated with a given test
	 * @return boolean
	 * 		pass/fail (true/false)
	 * @throws Exception
	 * 		throws exception
	 */
	public boolean assertAll(Long[] testCases) throws Exception {
		return assertAll(testCases, "");
	}
	
	/**
	 * AssertAll for asserts
	 * @param testCases
	 * 		List of TestRail cases associated with a given test
	 * @param comment
	 * 		Comment string to add for TestRail results
	 * @return
	 * 		pass/fail (true/false)
	 * @throws Exception
	 * 		throws exception
	 */
	@SuppressWarnings("rawtypes")
	public boolean assertAll(Long[] testCases, String comment) throws Exception {
		boolean returnPassed = true;
		Integer testRailStatus = -1;
		String testRailComment = "";
		
		if (!m_errors.isEmpty()) {
			returnPassed = false;
			StringBuilder sb = new StringBuilder("The following asserts failed:");
			boolean first = true;
	      			
			for (Entry<AssertionError, IAssert> ae : m_errors.entrySet()) {
				String myClass = "";
				String myMethod = "";
				int myLineNum = 0;
				
				if (first) {
					first = false;
				} else {
					sb.append(",");
				}
				
				// New Line
				sb.append("\n\t");

				// Log the Class, Method, Line# from the stacktrace
				for (StackTraceElement line : ae.getKey().getStackTrace()) {
					if (line.getClassName().startsWith("web.prismhr") && !line.getClassName().contains("common")) {
						myClass = line.getClassName();
						myMethod = line.getMethodName();
						myLineNum = line.getLineNumber();
					}
				}

				Reporter.log("<br>"); // Set new line between each error
				// Log Screenshot
				logScreenshot(imageNameList.get(ae.getKey()), imagePathList.get(ae.getKey()));				
				
				// Log Error
				Reporter.log("<br><font color=\"red\">"+ae.getKey().getMessage()+"</font>");
				sb.append(ae.getKey().getMessage());				
				Reporter.log("<br><font color=\"green\">Class: " + myClass + " Test Case: " + myMethod + " Line: " + myLineNum + "</font>");
			}
			//** Set TestRail status as failed
			Capabilities cap = ((RemoteWebDriver) getDriver()).getCapabilities();
			String myBrowser = cap.getBrowserName() + " " + cap.getVersion();
			testRailStatus = TestRail.FAILED;
			testRailComment = "(Automation ENV) Env: "+testProperties.getString(TEST_ENV);
			if (!comment.isEmpty()) {
				testRailComment = testRailComment+comment;
			}
			testRailComment = testRailComment+" / "+sb.toString();
			// * If running from Bamboo, add Bamboo build URL to comment.
			if (!bambooBuildKey.isEmpty() && !bambooBuildNo.isEmpty()) {
				testRailComment = testRailComment+" / Bamboo Results: "+bambooURLBase+"browse/"+bambooBuildKey+"-"+bambooBuildNo;
			}

			// ** If TestRail=true, Update TestRail before throwing back the Assertion Error
			if (recordTestRailResults && (testCases != null) && (!exceptionOccurred)) {
				String planOrRun = testRailPlanOrRun;
				String ID = testRailID;
				String env = testProperties.getString(TEST_ENV);
				//Reporter.log("******** TESTRAILS **********", true);
				//Reporter.log("		'"+planOrRun+"'" ,true);
				//Reporter.log("		'"+ID+"'", true);
				//Reporter.log("*****************************", true);
				testRailAPI.addRunTestResult(ID, planOrRun, env, testCases, testRailStatus, testRailComment, myBrowser);			
			}
			throw new AssertionError(sb.toString());
	   	} else {
	   		// Set TestRail status to passed
			Capabilities cap = ((RemoteWebDriver) getDriver()).getCapabilities();
			String myBrowser = cap.getBrowserName() + " " + cap.getVersion();
	   		testRailStatus = TestRail.PASSED;
	   		testRailComment = "(Automation ENV) PASSED Env: "+testProperties.getString(TEST_ENV);
	   		if (!comment.isEmpty()) {
	   			testRailComment = testRailComment+comment;
	   		}
			// * If running from Bamboo, add Bamboo build URL to comment.
			if (!bambooBuildKey.isEmpty() && !bambooBuildNo.isEmpty()) {
				testRailComment = testRailComment+" / Bamboo Results: "+bambooURLBase+"browse/"+bambooBuildKey+"-"+bambooBuildNo;
			}
	   		// ** It TestRail=true, update TestRail results
			if (recordTestRailResults && (testCases != null) && (!exceptionOccurred)) {
				String planOrRun = testRailPlanOrRun;
				String ID = testRailID;
				String env = testProperties.getString(TEST_ENV);
				//Reporter.log("******** TESTRAILS **********", true);
				//Reporter.log("		'"+planOrRun+"'", true);
				//Reporter.log("		'"+ID+"'", true);
				//Reporter.log("*****************************", true);
				testRailAPI.addRunTestResult(ID, planOrRun, env, testCases, testRailStatus, testRailComment, myBrowser);			
			}
	   	}
		return returnPassed;
	}
	
	/*
	 * This override will capture a screenshot at point of assert failure.
	 * 
	 * (non-Javadoc)
	 * @see org.testng.asserts.Assertion#onAssertFailure(org.testng.asserts.IAssert, java.lang.AssertionError)
	 */
	@Override
	public void onAssertFailure(IAssert<?> a, AssertionError ex) {
		m_errors.put(ex, a);
		
		for (StackTraceElement line : ex.getStackTrace()) {
			if (line.getClassName().startsWith("web.prismhr") && !line.getClassName().contains("common")) {
				stackClassName = line.getClassName();
				stackMethodName = line.getMethodName();
				stackLineNum = line.getLineNumber();
			}
		}
		imageFileName = stackMethodName + "-Line" + stackLineNum;
		if (!screenshotAdditionalData.isEmpty()) {
			imageFileName = imageFileName + "-" + screenshotAdditionalData;
		}
		String imagePath = captureScreenshot(imageFileName);
		System.out.println("IMAGE FILE: '"+imagePath+"'");
		imagePathList.put(ex, imagePath);
		imageNameList.put(ex, imageFileName);
		super.onAssertFailure(a, ex);
	}
	/**
	 * Capture a screenshot, and save to relative path for the ReportNG results
	 * @param fileName
	 * 		name of file to save screenshot to
	 * @return String
	 * 		returns full path of the captured image file
	 */
	public String captureScreenshot(String fileName) {
		String imagePath = "";
		try {
			String userDirectory = System.getProperty("user.dir") + "/";
			File destFile = new File(userDirectory+screenshotFileLocation+fileName+"-"+(new SimpleDateFormat("yyyyMMddHHmmss").format(new Date()))+".png");
			imagePath = destFile.toString();
			File imageFile = null;
			
			imageFile = ((TakesScreenshot)getDriver()).getScreenshotAs(OutputType.FILE);
			
			// Save Screenshot to results folder
			FileUtils.copyFile(imageFile, destFile);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return imagePath; // Return the full path to the screenshot file
	}
	
	/**
	 * Adds a screenshot to the ReportNG results.
	 * 
	 * @param imageName
	 * 		Name of the image
	 * @param imageFilePath
	 * 		Path to the screenshot file
	 */
	public void logScreenshot(String imageName, String imageFilePath) {
		Reporter.log("<br><font style=\"text-decoration: underline;\" size=\"3\" color=\"blue\"><a href=\""+ imageFilePath + "\"><b>"+imageName+"</b></font></a>");
	}	
	
	private String throwableToString(Throwable t) {
		StringWriter sw = new StringWriter();
		PrintWriter pw = new PrintWriter(sw);
		t.printStackTrace(pw);
		return sw.toString();
	}
	
	/**
	 * Converts a StringArray to a String
	 * @param stringArray
	 * 		String Array to convert to String
	 * @return String
	 * 		return a String of the stringArray
	 */
	public String stringArrayToString(String[] stringArray) {
		StringBuffer sb = new StringBuffer("{");
		for (int j = 0; j < stringArray.length; j++) {
			sb.append(" ").append("\"").append(stringArray[j]).append("\"");
		}
		sb.append(" }");
		return sb.toString();
	}
	
	/**
	 * Converts a String to a StringArray, based on the delimeter
	 * @param myString
	 * 		String to parts
	 * @param myDelimiter
	 * 		Delimiter to use for parsing
	 * @return 
	 * 		Return a String[]
	 */
	public String[] stringToStringArray(String myString, String myDelimiter) {
		String[] returnArray = myString.split(myDelimiter);
		return returnArray;
	}

	/**
	 * Joins the strings in an array into a delimited string. This method does
	 * not escape any instance of the delimiter in the strings being joined.
	 * 
	 * @param stringArray
	 *            the array of string to join
	 * @param delimiter
	 *            the delimiter between strings
	 * @return String
	 * 			Returned String
	 */
	public String join(String[] stringArray, char delimiter) {
		StringBuffer sb = new StringBuffer();
		for (int j = 0; j < stringArray.length; j++) {
			sb.append(stringArray[j]);
			if (j < stringArray.length - 1) {
				sb.append(delimiter);
			}
		}
		return sb.toString();
	}

	/**
	 * Compares two objects, but handles "regexp:" strings like HTML Selenese
	 * 
	 * @param actual
	 * 			Actual value
	 * @param expected
	 * 			Expected value
	 * @return true if actual matches the expectedPattern, or false otherwise
	 */
	public boolean seleniumEquals(Object actual, Object expected) {
		if (expected instanceof String && actual instanceof String) {
			return seleniumEquals((String) expected, (String) actual);
		}
		return expected.equals(actual);
	}


	private String verifyEqualsAndReturnComparisonDumpIfNot(String[] actual, String[] expected) {
		boolean misMatch = false;
		if (actual.length != expected.length) {
			misMatch = true;
		}
		for (int j = 0; j < expected.length; j++) {
			if (!seleniumEquals(actual[j], expected[j])) {
				misMatch = true;
				break;
			}
		}
		if (misMatch) {
			return "Expected " + stringArrayToString(expected) + " but saw "
					+ stringArrayToString(actual);
		}
		return null;
	}


	
	//**************** VERIFY METHODS *********************
	/**
	 * Like assertTrue, verifies that a condition is true but does not throw
	 * until the end of the test (during tearDown).
	 * 
	 * @param condition
	 *            the condition to evaluate
	 */
	public void verifyTrue(boolean condition) {
		try {
			assertTrue(condition);
		} catch (Error e) {
			verificationErrors.append(throwableToString(e));
		}
	}
	
	/**
	 * Like assertTrue, verifies that a condition is true, but does not throw
	 * exception until end of test (during tearDown).
	 * 
	 * @param condition
	 * 				the condition to evaluate
	 * @param msg
	 * 				the message to include if exception thrown.
	 */
	public void verifyTrue(boolean condition, String msg) {
		try {
			assertTrue(condition, msg);
		} catch (Error e) {
			verificationErrors.append(throwableToString(e));
		}
	}

	/**
	 * Like assertFalse, verifies that a condition is false but does not throw
	 * until the end of the test (during tearDown).
	 * 
	 * @param condition
	 *            the condition to evaluate
	 */
	public void verifyFalse(boolean condition) {
		try {
			assertFalse(condition);
		} catch (Error e) {
			verificationErrors.append(throwableToString(e));
		}
	}

	/**
	 * 	 * Like assertFalse, verifies that a condition is false but does not throw
	 * until the end of the test (during tearDown).
	 * 
	 * @param condition
	 * 				the condition to evaluate
	 * @param msg
	 * 				the message to include if exception thrown.
	 */
	public void verifyFalse(boolean condition, String msg) {
		try {
			assertFalse(condition, msg);
		} catch (Error e) {
			verificationErrors.append(throwableToString(e));
		}
	}

	/**
	 * Like assertEquals, verifies that two objects are equal. but does not
	 * throw until the end of the test (during tearDown).
	 * 
	 * @param actual
	 *            the actual value
	 * @param expected
	 *            the expected value
	 */
	public void verifyEquals(Object actual, Object expected) {
		try {
			assertEquals(actual, expected);
		} catch (Error e) {
			verificationErrors.append(throwableToString(e));
		}
	}

	/**
	 * Like assertEquals, verifies that two objects are equal. but does not
	 * throw until the end of the test (during tearDown).
	 * 
	 * @param actual
	 *            	the actual value
	 * @param expected
	 *            	the expected value
	 * @param msg
	 * 				a message to include if exception thrown.
	 */
	public void verifyEquals(Object actual, Object expected, String msg) {
		try {
			assertEquals(actual, expected);
		} catch (Error e) {
			verificationErrors.append(msg + " " + throwableToString(e));
		}
	}

	/**
	 * Like assertEquals, verifies that two booleans are equal. but does not
	 * throw until the end of the test (during tearDown).
	 * 
	 * @param actual
	 *            the actual value
	 * @param expected
	 *            the expected value
	 */
	public void verifyEquals(boolean actual, boolean expected) {
		try {
			assertEquals(Boolean.valueOf(actual), Boolean.valueOf(expected));
		} catch (Error e) {
			verificationErrors.append(throwableToString(e));
		}
	}

	/**
	 * Like assertEquals, verifies that two booleans are equal. but does not
	 * throw until the end of the test (during tearDown).
	 * 
	 * @param actual
	 *            	the actual value
	 * @param expected
	 *            	the expected value
	 * @param msg
	 * 				a message to include if exception thrown
	 */
	public void verifyEquals(boolean actual, boolean expected, String msg) {
		try {
			assertEquals(Boolean.valueOf(actual), Boolean.valueOf(expected));
		} catch (Error e) {
			verificationErrors.append(msg + " " + throwableToString(e));
		}
	}

	/**
	 * Like assertEquals, verifies that two String arrays contain the same
	 * strings in the same order, but does not throw until the end of the test
	 * (during tearDown).
	 * 
	 * @param actual
	 *            the actual value
	 * @param expected
	 *            the expected value
	 */
	public void verifyEquals(String[] actual, String[] expected) {
		String comparisonDumpIfNotEqual = verifyEqualsAndReturnComparisonDumpIfNot(
				actual, expected);
		if (comparisonDumpIfNotEqual != null) {
			verificationErrors.append(comparisonDumpIfNotEqual);
		}
	}
	
	/**
	 * Like assertEquals, verifies that two String arrays contain the same
	 * strings in the same order, but does not throw until the end of the test
	 * (during tearDown).
	 * 
	 * @param actual
	 *            the actual value
	 * @param expected
	 *            the expected value
	 * @param msg
	 * 				a message to include if exception thrown
	 */
	public void verifyEquals(String[] actual, String[] expected, String msg) {
		String comparisonDumpIfNotEqual = verifyEqualsAndReturnComparisonDumpIfNot(
				actual, expected);
		if (comparisonDumpIfNotEqual != null) {
			verificationErrors.append(msg + " " + comparisonDumpIfNotEqual);
		}
	}

	/**
	 * Like assertNotEquals, verifies that two objects are NOT equal. but does
	 * not throw until the end of the test (during tearDown).
	 * 
	 * @param actual
	 *            the actual value
	 * @param expected
	 *            the expected value
	 */
	public void verifyNotEquals(Object actual, Object expected) {
		try {
			assertNotEquals(actual, expected);
		} catch (AssertionError e) {
			verificationErrors.append(throwableToString(e));
		}
	}

	/**
	 * Like assertNotEquals, verifies that two objects are NOT equal. but does
	 * not throw until the end of the test (during tearDown).
	 * 
	 * @param actual
	 *            	the actual value
	 * @param expected
	 *            	the expected value
	 * @param msg
	 * 				a message to include when exception thrown
	 */
	public void verifyNotEquals(Object actual, Object expected, String msg) {
		try {
			assertNotEquals(actual, expected, msg);
		} catch (AssertionError e) {
			verificationErrors.append(throwableToString(e));
		}
	}

	/**
	 * Like assertNotEquals, verifies that two booleans are NOT equal. but does
	 * not throw until the end of the test (during tearDown).
	 * 
	 * @param actual
	 *            the actual value
	 * @param expected
	 *            the expected value
	 */
	public void verifyNotEquals(boolean actual, boolean expected) {
		try {
			assertNotEquals(Boolean.valueOf(actual), Boolean.valueOf(expected));
		} catch (AssertionError e) {
			verificationErrors.append(throwableToString(e));
		}
	}

	/**
	 * Like assertNotEquals, verifies that two booleans are NOT equal. but does
	 * not throw until the end of the test (during tearDown).
	 * 
	 * @param actual
	 *            	the actual value
	 * @param expected
	 *            	the expected value
	 * @param msg
	 * 				a message to include if an exception is thrown
	 */
	public void verifyNotEquals(boolean actual, boolean expected, String msg) {
		try {
			assertNotEquals(Boolean.valueOf(actual), Boolean.valueOf(expected), msg);
		} catch (AssertionError e) {
			verificationErrors.append(throwableToString(e));
		}
	}

	
	
	//********************* Asserts *******************
	/**
	 * Like TestNg's Assert.assertEquals, but knows how to compare string arrays
	 * 
	 * @param actual
	 * 			The actual value
	 * @param expected
	 * 			The expected value
	 */
	public void assertEquals(Object actual, Object expected) {
		if (actual instanceof String && expected instanceof String) {
			assertEquals((String) actual, (String) expected);
			return;
		}

		if (actual instanceof String && expected instanceof String[]) {
			assertEquals((String) actual, (String[]) expected);
			return;
		}

		if (actual instanceof String && expected instanceof Number) {
			assertEquals((String) actual, expected.toString());
			return;
		}

		if (actual instanceof Number && expected instanceof String) {
			assertEquals(actual.toString(), (String) expected);
			return;
		}

		if (actual instanceof String[] && expected instanceof String[]) {
			assertEquals((String[]) actual, (String[]) expected);
			return;
		}

		if (expected == null) {
			assertNull(actual);
			return;
		}

		assertTrue(expected.equals(actual));
	}
	
	/**
	 * Asserts that two objects are not the same (compares using .equals())
	 * 
	 * @param actual
	 *            	the actual value
	 * @param expected
	 *            	the expected value
	 */
	public void assertNotEquals(Object actual, Object expected) {
		if (actual.equals(expected)) {
			fail("did not expect values to be equal (" + expected.toString()
					+ ")");
		}
	}

	/**
	 * Asserts that two objects are not the same (compares using .equals())
	 * 
	 * @param actual
	 *            	the actual value
	 * @param expected
	 *            	the expected value
	 * @param msg
	 * 				the message if fails
	 */
	public void assertNotEquals(Object actual, Object expected, String msg) {
		if (actual.equals(expected)) {
			fail(msg + " " + "(" + expected.toString()
					+ ")");
		}
	}

	/**
	 * Like TestNG's Assert.assertEquals, but handles "regexp:" strings like
	 * HTML Selenese
	 * 
	 * @param actual
	 *            	the actual value
	 * @param expected
	 *            	the expected value
	 */
	public void assertEquals(String actual, String expected) {
		assertTrue(seleniumEquals(actual, expected), "Expected \"" + expected
				+ "\" but saw \"" + actual + "\" instead");
	}

	/**
	 * Like TestNG's Assert.assertEquals, but joins the string array with
	 * commas, and handles "regexp:" strings like HTML Selenese
	 * 
	 * @param actual
	 *            	the actual value
	 * @param expected
	 *            	the expected value
	 */
	public void assertEquals(String[] actual, String expected) {
		assertEquals(join(actual, ','), expected);
	}

	/**
	 * Asserts that two string arrays have identical string contents
	 * 
	 * @param actual
	 *            	the actual value
	 * @param expected
	 *            	the expected value
	 */
	public void assertEquals(String[] actual, String[] expected) {
		String comparisonDumpIfNotEqual = verifyEqualsAndReturnComparisonDumpIfNot(
				actual, expected);
		if (comparisonDumpIfNotEqual != null) {
			throw new AssertionError(comparisonDumpIfNotEqual);
		}
	}
}
