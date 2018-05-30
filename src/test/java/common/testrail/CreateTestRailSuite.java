package common.testrail;

import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.List;

import org.json.simple.JSONObject;
import org.testng.Reporter;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;
import org.testng.xml.XmlPackage;
import org.testng.xml.XmlSuite;
import org.testng.xml.XmlTest;


/**
 * This class contains a main @Test method that will read a specific TestRail
 * 'run' and build a new TestNG.xml suite file from the tests listed in the
 * TestRail run that are automated.
 * 
 * This new xml suite file can then be triggered in a Bamboo config to
 * 
 * @author mlabbe
 *
 */
public class CreateTestRailSuite {

	int parallelThreadCount = 5;

	@Parameters({ "testrail_id" })
	@Test
	public void createSuiteTestRailRun(String testRailId) throws Exception {
		TestRail testRailAPI = new TestRail();
		ArrayList<String> testIds = new ArrayList<String>();
		Throwable t = null;

		String runID = "";
		String automatedStatus = "1";
		String automatedNeedsUpdateStatus = "3";
		

		try {
			if (!testRailId.isEmpty() && testRailId.contains(":") && testRailId.split("[:]").length == 2) {
				runID = testRailId.split("[:]")[1];
			} else {
				runID = testRailId;
			}

			Reporter.log("-- TestRail Run ID = " + runID, true);

			ArrayList<String> listeners = new ArrayList<String>();
			listeners.add("org.uncommons.reportng.HTMLReporter");
			listeners.add("org.uncommons.reportng.JUnitXMLReporter");

			// Fetch Automated tests from TestRail run
			Reporter.log("-- Fetching Automated Tests from TestRail Run", true);
			ArrayList<String> runTestCaseIdList = testRailAPI.getRunTestIds(runID);
			for (String singleId : runTestCaseIdList) {
				JSONObject thisCase = testRailAPI.getTestCase(singleId);
				if (String.valueOf((long) thisCase.get("custom_executionmethod")).equals(automatedStatus)
						|| String.valueOf((long) thisCase.get("custom_executionmethod")).equals(automatedNeedsUpdateStatus)) {
					testIds.add(singleId);
				}
			}

			Reporter.log("-- Building XML File", true);
			// Build Packages
			XmlPackage myPackage = new XmlPackage();
			myPackage.setName("web.prismhr.*");
			List<XmlPackage> packageList = new ArrayList<XmlPackage>();
			packageList.add(myPackage);

			// Build Suite
			XmlSuite suite = new XmlSuite();
			suite.setName("Suite for TestRail Run " + runID);
			suite.setParallel(XmlSuite.ParallelMode.METHODS);
			suite.setThreadCount(parallelThreadCount);
			suite.setListeners(listeners);

			// Build Test
			XmlTest test = new XmlTest();
			test.setName("Suite for TestRail Run " + runID);
			for (String oneId : testIds) {
				test.addIncludedGroup(oneId);
			}
			test.setPackages(packageList);
			suite.addTest(test);
			test.setSuite(suite);

			// Write XML file
			String fileName = System.getProperty("user.dir") + "/src/test/resources/Common_suites/TRRun.xml";
			Reporter.log("-- Writing out XML file to '" + fileName + "'", true);
			FileWriter writer = new FileWriter(new File(fileName));
			writer.write(suite.toXml());
			writer.flush();
			writer.close();
		} catch (Exception e) {
			throw (e);
		}
	}

}
