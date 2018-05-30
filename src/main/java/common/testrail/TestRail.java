package common.testrail;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.testng.Reporter;


/**
 * This class is for integrating with TestRails
 * @author mlabbe
 *
 */
public class TestRail {

	// TestRails connection info.
	public static final String testRailURL = "https://xxxx.testrail.com/";
	public static final String testRailUser = "";
	public static final String testRailPassword = "";

	TestRailAPIClient client = null;

	// TestRails test result status options
	public final static Integer PASSED = 1;
	public final static Integer BLOCKED = 2;
	public final static Integer UNTESTED = 3;
	public final static Integer RETEST = 4;
	public final static Integer FAILED = 5;
	
	/*
	 * Set the TestRailAPI client
	 */
	private void setClient() {
		client = new TestRailAPIClient(testRailURL);
		client.setUser(testRailUser);
		client.setPassword(testRailPassword);		
	}
	
	/*
	 * Get the Plan, based on Plan ID
	 */
	private JSONObject getPlan(String planID) throws MalformedURLException, IOException, TestRailAPIException {
		return (JSONObject) client.sendGet("get_plan/"+planID);
	}

	/*
	 * Get the Plan's Entries
	 */
	private JSONArray getPlanEntries(JSONObject plan) {
		return (JSONArray) plan.get("entries");
	}

	/*
	 * Get the Test Case
	 */
	public JSONObject getTestCase(String caseID) throws Exception {
		return (JSONObject) client.sendGet("get_case/"+caseID);
	}

	/*
	 * Get the desired Run, based on passing in
	 * Entries and the string to search for in Run Name
	 */
	private Long getRunID(JSONArray entries, String runString) {
		Long returnID = (long) 0;
		for (int i=0; i < entries.size(); i++) {
			JSONObject thisEntry = (JSONObject) entries.get(i);
			if (thisEntry.get("name").toString().toLowerCase().contains(runString.toLowerCase())) {
				JSONObject thisRun = (JSONObject) ((JSONArray) thisEntry.get("runs")).get(0);
				returnID = (Long) thisRun.get("id");
				break;
			}
		}
		return returnID;
	}
	
	/*
	 * Returns true/false if the given Test Case ID
	 * is found in list of tests for the given Run ID.
	 */
	private boolean runTestCaseIDFound(Long runID, Long testCaseID) throws MalformedURLException, IOException, TestRailAPIException {
		boolean foundIt = false;
		JSONArray run = (JSONArray) client.sendGet("get_tests/"+runID);
		for (int i=0; i<run.size(); i++) {
			JSONObject thisTest = (JSONObject) run.get(i);
			if ((Long)thisTest.get("case_id") == testCaseID.longValue()) {
				foundIt = true;
				break;
			}
		}
		return foundIt;
	}
	
	/*
	 * Updates a result for a given run and test
	 * Specify the status and any comment.
	 */
	@SuppressWarnings("rawtypes")
	private JSONObject updateTestResult(Long runID, Long testID, Map resultData) throws MalformedURLException, IOException, TestRailAPIException {
		String postString = "add_result_for_case/"+runID+"/"+testID;
		return (JSONObject) client.sendPost(postString, resultData);
	}
	

	
	/**
	 * Submit a Test Run Result to TestRail
	 * @param ID
	 * 		The ID (plan or run)
	 * @param planOrRun
	 * 		'plan' or 'run' for recording results
	 * @param runSearch
	 * 		The String to search for in Run Title
	 * @param testCases
	 * 		The list of test case IDs
	 * @param status
	 * 		The status to set
	 * @param comment
	 * 		Comment to set
	 * @param browser
	 * 		Browser in use
	 * @throws Exception
	 * 		throws Exception
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public void addRunTestResult(String ID, String planOrRun, String runSearch, Long[] testCases, Integer status, String comment, String browser) throws Exception {
		Long runID;
		Map newResult = new HashMap();
		newResult.put("status_id", status);
		if (!browser.isEmpty()) {
			newResult.put("custom_browser", browser);
		}
		newResult.put("comment", comment);
				
		// Set Client
		setClient();
		
		// If PlanID, then need these steps to get plan, and plan entries(runs), find my run
		if (planOrRun.toLowerCase().equals("plan")) {
			// Get Plan
			JSONObject thisPlan = getPlan(ID);
			// Get Entries
			JSONArray thisEntries = getPlanEntries(thisPlan);
			// Get RunID
			runID = getRunID(thisEntries, runSearch);
		} else {
			runID = Long.parseLong(ID);
		}
			
		// For each Test String, get ID and record result
		for (Long thisTest : testCases) {
			// Update Result
			boolean foundMe = runTestCaseIDFound(runID, thisTest);
			if (foundMe) { // Only record if Test is Found.
				Reporter.log("***** TestRail ADD: "+planOrRun+":"+ID+" / test("+thisTest+") / "+runSearch+" / status("+status+") / comment("+comment+")", true);
				try {
					JSONObject resultResponse = updateTestResult(runID, thisTest, newResult);
				} catch (Exception e) {
					Reporter.log("***** TestRail API ERROR: "+e.getMessage(), true);
				}
			} else {
				Reporter.log("***** TestRail ERROR: Test Case ID "+thisTest+" NOT found in Run ID "+runID, true);
			}
		}
	}

	/**
	 * Submit a Test Run Result to TestRail
	 * @param ID
	 * 		The ID (plan or run)
	 * @param planOrRun
	 * 		'plan' or 'run' for recording results
	 * @param runSearch
	 * 		The String to search for in Run Title
	 * @param testCases
	 * 		The list of test case IDs
	 * @param status
	 * 		The status to set
	 * @param comment
	 * 		Comment to set
	 * @throws Exception
	 * 		throws Exception
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public void addRunTestResult(String ID, String planOrRun, String runSearch, Long[] testCases, Integer status, String comment) throws Exception {
		Long runID;
		Map newResult = new HashMap();
		newResult.put("status_id", status);
		newResult.put("comment", comment);
				
		// Set Client
		setClient();
		
		// If PlanID, then need these steps to get plan, and plan entries(runs), find my run
		if (planOrRun.toLowerCase().equals("plan")) {
			// Get Plan
			JSONObject thisPlan = getPlan(ID);
			// Get Entries
			JSONArray thisEntries = getPlanEntries(thisPlan);
			// Get RunID
			runID = getRunID(thisEntries, runSearch);
		} else {
			runID = Long.parseLong(ID);
		}
			
		// For each Test String, get ID and record result
		for (Long thisTest : testCases) {
			// Update Result
			boolean foundMe = runTestCaseIDFound(runID, thisTest);
			if (foundMe) { // Only record if Test is Found.
				Reporter.log("***** TestRail ADD: "+planOrRun+":"+ID+" / test("+thisTest+") / "+runSearch+" / status("+status+") / comment("+comment+")", true);
				try {
					JSONObject resultResponse = updateTestResult(runID, thisTest, newResult);
				} catch (Exception e) {
					Reporter.log("***** TestRail API ERROR: "+e.getMessage(), true);
				}
			} else {
				Reporter.log("***** TestRail ERROR: Test Case ID "+thisTest+" NOT found in Run ID "+runID, true);
			}
		}
	}

	/**
	 * Returns list of test IDs from a specific test run
	 * @param runId
	 * @return
	 * @throws MalformedURLException
	 * @throws IOException
	 * @throws APIException
	 */
	public ArrayList<String> getRunTestIds(String runId) throws Exception {
		ArrayList<String> returnList = new ArrayList<String>();
		setClient();
		JSONArray run = (JSONArray) client.sendGet("get_tests/"+runId);
		for (int i=0; i<run.size(); i++) {
			JSONObject thisTest = (JSONObject) run.get(i);
			String ll = Long.toString((Long)thisTest.get("case_id"));
			returnList.add(Long.toString((Long)thisTest.get("case_id")));
		}
		return returnList;
	}
}
