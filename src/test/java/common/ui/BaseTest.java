package common.ui;


import java.io.File;
import java.net.URL;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.openqa.selenium.Alert;
import org.openqa.selenium.Capabilities;
// import org.openqa.selenium.Platform;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.edge.EdgeDriver;
import org.openqa.selenium.edge.EdgeOptions;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.firefox.FirefoxProfile;
import org.openqa.selenium.ie.InternetExplorerDriver;
import org.openqa.selenium.ie.InternetExplorerOptions;
import org.openqa.selenium.remote.Augmenter;
// import org.openqa.selenium.remote.BrowserType;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.LocalFileDetector;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.safari.SafariDriver;
import org.testng.Reporter;
import org.testng.annotations.AfterMethod;

import common.testrail.TestRail;
import common.utils.GeneralUtils;
import common.utils.ValidationUtils;

// * appium
// import io.appium.java_client.remote.MobileCapabilityType;

// * Docker
// import com.spotify.docker.client.DefaultDockerClient;
// import com.spotify.docker.client.DockerClient;
// import com.spotify.docker.client.messages.ContainerConfig;
// import com.spotify.docker.client.messages.ContainerCreation;
// import com.spotify.docker.client.messages.HostConfig;


/**
 * This class is the base class for test case classes. - It launches the
 * driver/browser session before each test class (BeforeClass). - It handles
 * capturing screenshots for failed test methods (AferMethod). - It handles
 * killing the Selenium driver/browser session after each test class
 * (AfterClass)
 * 
 * @author mlabbe
 */
public class BaseTest extends ValidationUtils {

	private static final String chromeDriverLocation = System.getProperty("user.dir") + "/bin/chromedriver.exe";
	private static final String ieDriverLocation = System.getProperty("user.dir") + "/bin/IEDriverServer.exe";
	private static final String firefoxDriverLocation = System.getProperty("user.dir") + "/bin/geckodriver.exe"; 
	private static final String edgeDriverLocation = System.getProperty("user.dir") + "/bin/MicrosoftWebDriver.exe";

	// private static final String dockerCerts = System.getProperty("user.dir")
	// + "/.docker/machine/machines/default";

	protected static ThreadLocal<ValidationUtils> softAssert = new ThreadLocal<ValidationUtils>();
	private int defaultImplicitWaitTime = 20; // In seconds

	private static String runLocation = "local";
	private static String serverURL = "";
	private static String browser = "chrome";
	private static String browserVersion = "";
	private static String platformOS = "";
	private static final String defaultChromeVersion = "54";
	private static final String defaultFirefoxVersion = "47";
	private static final String defaultIEVersion = "11";
	private static final String defaultEdgeVersion = "14";
	private static final String defaultPlatformOS = "WIN8";
	private static final String defaultSafariVersion = "11";
	private static final String defaultOSX = "Mac OSX 10.13";

	private static final String defaultSeleniumHubIP = "";

	// TESTINGBOT
	private static final String testBotKey = "";
	private static final String testBotSecret = "";
	private static final String testBotURL = "http://" + testBotKey + ":" + testBotSecret + "@" + defaultSeleniumHubIP
			+ ":4445/wd/hub";

	// SmartBear CrossBrowserTesting
	private static final String smartBearUserName = "";
	private static final String smartBearAuthKey = "";
	private static final String smartBearURL = "http://" + smartBearUserName+":" + smartBearAuthKey + "@hub.crossbrowsertesting.com:80/wd/hub";
	protected static final String smartBearAPIURL = "https://"+smartBearUserName+":"+smartBearAuthKey+"@crossbrowsertesting.com/api/v3/selenium";
	protected static final String smartBearScreenRes = "1366x768";
	protected static final int smartBearDefaultTimeout = 2000;

	private ChromeOptions chromeOptions = new ChromeOptions();
	private FirefoxProfile ffProfile = new FirefoxProfile();
	private EdgeOptions edgeOptions = new EdgeOptions();
	private DesiredCapabilities capabilities;
	
	private GeneralUtils genUtils = new GeneralUtils();

	/**
	 * This method is the primary method for starting up the desired browser
	 * driver.
	 * 
	 * @param browserType
	 */
	protected void setDriver() throws Exception {
		switch (browser.split("[-]")[0].toLowerCase()) {
		case "firefox":
			setFirefoxDriver();
			break;
		case "chrome":
			// runDocker();
			setChromeDriver();
			break;
		case "ie":
			setIEDriver();
			break;
		case "edge":
			setEdgeDriver();
			break;
		case "safari":
			setSafariDriver();
			break;
		default:
			setChromeDriver();
			break;
		}
		getDriver().manage().window().maximize(); // Maximize the browser.
		getDriver().manage().timeouts().implicitlyWait(defaultImplicitWaitTime, TimeUnit.SECONDS);
	}

	
	/*
	 * This will set the Firefox driver.
	 */
	private void setFirefoxDriver() throws Exception {
		// Disable cache
		ffProfile.setPreference("browser.cache.disk.enable", false);
		ffProfile.setPreference("browser.cache.disk_cache_ssl", false);
		ffProfile.setPreference("browser.cache.memory.enable", false);
		ffProfile.setPreference("browser.cache.offline.enable", false);
		// Set to download automatically
		ffProfile.setPreference("browser.download.folderList", 2);
		ffProfile.setPreference("browser.download.manager.showWhenStarting", false);
		ffProfile.setPreference("browser.helperApps.neverAsk.saveToDisk", "application/zip");
		ffProfile.setPreference("browser.download.dir", BasePage.myTempDownloadsFolder);// "\\temp_downloads");
		// TODO: Using "C:" will not work for Linux or OS X support
		File dir = new File(BasePage.myTempDownloadsFolder);// "\\temp_downloads");
		if (!dir.exists()) {
			dir.mkdir();
		}
		// Disable hardware acceleration
		ffProfile.setPreference("gfx.direct2d.disabled", false);
		ffProfile.setPreference("layers.acceleration.disabled", false);
		FirefoxOptions ffOptions = new FirefoxOptions();

		/*
		 * Set FF to run headless -- Need to make conditional from browser
		 * parameter -- Does NOT work properly with all necessary tests.
		 */
		// ffOptions.setHeadless(true);

		ffOptions.setProfile(ffProfile);
		capabilities = DesiredCapabilities.firefox();
		capabilities.setCapability(FirefoxDriver.PROFILE, ffProfile);
		WebDriver myDriver = null;
		RemoteWebDriver rcDriver;

		switch (runLocation.toLowerCase()) {
		case "local":
			System.setProperty("webdriver.gecko.driver", firefoxDriverLocation);
			myDriver = new FirefoxDriver(ffOptions);
			break;
		case "grid":
			rcDriver = new RemoteWebDriver(new URL(serverURL), ffOptions);
			rcDriver.setFileDetector(new LocalFileDetector());
			myDriver = new Augmenter().augment(rcDriver);
			break;
		case "testingbot":
			if (browserVersion.isEmpty()) {
				browserVersion = defaultFirefoxVersion;
			}
			if (platformOS.isEmpty()) {
				platformOS = defaultPlatformOS;
			}
			capabilities.setCapability("browserName", browser);
			capabilities.setCapability("version", browserVersion);
			capabilities.setCapability("platform", platformOS);
			// capabilities.setCapability("name", testName); // TODO: set a test
			// name (suite name maybe) or combined with env
			rcDriver = new RemoteWebDriver(new URL(serverURL), ffOptions);
			myDriver = new Augmenter().augment(rcDriver);
			break;
		case "smartbear":
			if (browserVersion.isEmpty()) {
				browserVersion = defaultFirefoxVersion;
			}
			if (platformOS.isEmpty()) {
				platformOS = defaultPlatformOS;
			}
			//capabilities.setCapability("name", testMethod.get());
			capabilities.setCapability("build", testProperties.getString(TEST_ENV)+" FF-"+platformOS);
			capabilities.setCapability("max_duration", smartBearDefaultTimeout);
			capabilities.setCapability("browserName", browser);
			capabilities.setCapability("version", browserVersion);
			capabilities.setCapability("platform", platformOS);
			capabilities.setCapability("screenResolution", smartBearScreenRes);
			capabilities.setCapability("record_video", "true"); Reporter.log(
					 "BROWSER: " + browser, true); Reporter.log("BROWSER Version: " +
							 browserVersion, true); Reporter.log("PLATFORM: " + platformOS, true);
			Reporter.log("URL '" + serverURL + "'", true); rcDriver = new
			RemoteWebDriver(new URL(serverURL), capabilities); myDriver = new
			Augmenter().augment(rcDriver);
			break;
		default:
			myDriver = new FirefoxDriver(ffOptions);
			break;
		}
		driver.set(myDriver);
	}

	/*
	 * This will set the Chrome driver
	 */
	private void setChromeDriver() throws Exception {
		// boolean headless = false;
		HashMap<String, Object> chromePrefs = new HashMap<String, Object>();
		chromePrefs.put("profile.default_content_settings.popups", 0);
		chromePrefs.put("download.default_directory", BasePage.myTempDownloadsFolder);
		chromeOptions.setExperimentalOption("prefs", chromePrefs);
		// TODO: Using "C:" will not work for Linux or OS X
		File dir = new File(BasePage.myTempDownloadsFolder);
		if (!dir.exists()) {
			dir.mkdir();
		}

		chromeOptions.addArguments("disable-popup-blocking");
		chromeOptions.addArguments("--disable-extensions");
		chromeOptions.addArguments("start-maximized");

		/*
		 * To set headless mode for chrome. Would need to make it conditional
		 * from browser parameter Does not currently work for all tests.
		 */
		// chromeOptions.setHeadless(true);

		if (runLocation.toLowerCase().equals("smartbear")) {
			Reporter.log("-- SMARTBEAR:  standard capabilities.  Not ChromeOptions", true);
			capabilities = new DesiredCapabilities();
		} else {
			capabilities = DesiredCapabilities.chrome();
			capabilities.setCapability(ChromeOptions.CAPABILITY, chromeOptions);
		}

		WebDriver myDriver = null;
		RemoteWebDriver rcDriver;

		switch (runLocation.toLowerCase()) {
		case "local":
			System.setProperty("webdriver.chrome.driver", chromeDriverLocation);
			myDriver = new ChromeDriver(chromeOptions);
			break;
		case "grid":
			rcDriver = new RemoteWebDriver(new URL(serverURL), capabilities);
			rcDriver.setFileDetector(new LocalFileDetector());
			myDriver = new Augmenter().augment(rcDriver);
			break;
		case "testingbot":
			if (browserVersion.isEmpty()) {
				browserVersion = defaultChromeVersion;
			}
			if (platformOS.isEmpty()) {
				platformOS = defaultPlatformOS;
			}
			capabilities.setCapability("browserName", browser);
			capabilities.setCapability("version", browserVersion);
			capabilities.setCapability("platform", platformOS);
			// capabilities.setCapability("name", testName); // TODO: set a test
			// name (suite name maybe) or combined with env
			rcDriver = new RemoteWebDriver(new URL(serverURL), capabilities);
			myDriver = new Augmenter().augment(rcDriver);
			break;
		case "smartbear":
			if (browserVersion.isEmpty()) {
				browserVersion = defaultChromeVersion;
			}
			if (platformOS.isEmpty()) {
				platformOS = defaultPlatformOS;
			}
			 
			//capabilities.setCapability("name", testMethod.get());
			capabilities.setCapability("build", testProperties.getString(TEST_ENV)+" Chrome-"+platformOS);
			capabilities.setCapability("max_duration", smartBearDefaultTimeout);
			capabilities.setCapability("browserName", browser);
			capabilities.setCapability("version", browserVersion);
			capabilities.setCapability("platform", platformOS);
			capabilities.setCapability("screenResolution", smartBearScreenRes);
			capabilities.setCapability("record_video", "true"); Reporter.log(
					 "BROWSER: " + browser, true); Reporter.log("BROWSER Version: " +
							 browserVersion, true); Reporter.log("PLATFORM: " + platformOS, true);
			Reporter.log("URL '" + serverURL + "'", true); rcDriver = new
			RemoteWebDriver(new URL(serverURL), capabilities); myDriver = new
			Augmenter().augment(rcDriver);
			break;
		default:
			System.setProperty("webdriver.chrome.driver", chromeDriverLocation);
			myDriver = new ChromeDriver(chromeOptions);
			break;
		}
		driver.set(myDriver);
	}

	private void setEdgeDriver() throws Exception {
		capabilities = DesiredCapabilities.edge();
		capabilities.setJavascriptEnabled(true);
		edgeOptions.setPageLoadStrategy("eager");
		edgeOptions.merge(capabilities);
		// capabilities.setCapability(EdgeOptions.CAPABILITY, edgeOptions);
		WebDriver myDriver = null;
		RemoteWebDriver rcDriver;

		switch (runLocation.toLowerCase()) {
		case "local":
			System.setProperty("webdriver.edge.driver", edgeDriverLocation);
			myDriver = new EdgeDriver(edgeOptions);
			break;
		case "grid":
			rcDriver = new RemoteWebDriver(new URL(serverURL), edgeOptions);
			rcDriver.setFileDetector(new LocalFileDetector());
			myDriver = new Augmenter().augment(rcDriver);
			break;
		case "testingbot":
			if (browserVersion.isEmpty()) {
				browserVersion = defaultEdgeVersion;
			}
			if (platformOS.isEmpty()) {
				platformOS = "WIN10";
			}
			capabilities.setCapability("browserName", browser);
			capabilities.setCapability("version", browserVersion);
			capabilities.setCapability("platform", platformOS);
			rcDriver = new RemoteWebDriver(new URL(serverURL), capabilities);
			rcDriver.setFileDetector(new LocalFileDetector());
			myDriver = new Augmenter().augment(rcDriver);
			break;
		case "smartbear":
			if (browserVersion.isEmpty()) {
				browserVersion = defaultEdgeVersion;
			}
			if (platformOS.isEmpty()) {
				platformOS = defaultPlatformOS;
			}
			//capabilities.setCapability("name", testMethod.get());
			capabilities.setCapability("build", testProperties.getString(TEST_ENV)+" Edge-"+platformOS);
			capabilities.setCapability("max_duration", smartBearDefaultTimeout);
			capabilities.setCapability("browserName", browser);
			capabilities.setCapability("version", browserVersion);
			capabilities.setCapability("platform", platformOS);
			capabilities.setCapability("screenResolution", smartBearScreenRes);
			capabilities.setCapability("record_video", "true"); Reporter.log(
					 "BROWSER: " + browser, true); Reporter.log("BROWSER Version: " +
							 browserVersion, true); Reporter.log("PLATFORM: " + platformOS, true);
			Reporter.log("URL '" + serverURL + "'", true); rcDriver = new
			RemoteWebDriver(new URL(serverURL), capabilities); myDriver = new
			Augmenter().augment(rcDriver);
			break;
		default:
			System.setProperty("webdriver.edge.driver", edgeDriverLocation);
			myDriver = new EdgeDriver(edgeOptions);
			break;
		}
		driver.set(myDriver);
	}

	/*
	 * This will set the IE driver
	 */
	private void setIEDriver() throws Exception {
		capabilities = DesiredCapabilities.internetExplorer();
		// capabilities.setCapability("ignoreProtectedModeSettings", true);
		// capabilities.setCapability("ignoreZoomSetting", true);
		// capabilities.setCapability(InternetExplorerDriver.IE_ENSURE_CLEAN_SESSION,
		// true);
		capabilities.setJavascriptEnabled(true);

		InternetExplorerOptions ieOptions = new InternetExplorerOptions();
		ieOptions.destructivelyEnsureCleanSession();
		ieOptions.ignoreZoomSettings();
		ieOptions.setCapability("ignoreProtectedModeSettings", true);
		ieOptions.merge(capabilities);

		WebDriver myDriver = null;
		RemoteWebDriver rcDriver;

		switch (runLocation.toLowerCase()) {
		case "local":
			System.setProperty("webdriver.ie.driver", ieDriverLocation);
			myDriver = new InternetExplorerDriver(ieOptions);
			break;
		case "grid":
			rcDriver = new RemoteWebDriver(new URL(serverURL), capabilities);
			rcDriver.setFileDetector(new LocalFileDetector());
			myDriver = new Augmenter().augment(rcDriver);
			break;
		case "testingbot":
			if (browserVersion.isEmpty()) {
				browserVersion = defaultIEVersion;
			}
			if (platformOS.isEmpty()) {
				platformOS = defaultPlatformOS;
			}
			capabilities.setCapability("browserName", browser);
			capabilities.setCapability("version", browserVersion);
			capabilities.setCapability("platform", platformOS);
			// capabilities.setCapability("name", testName); // TODO: set a test
			// name (suite name maybe) or combined with env
			rcDriver = new RemoteWebDriver(new URL(serverURL), capabilities);
			rcDriver.setFileDetector(new LocalFileDetector());
			myDriver = new Augmenter().augment(rcDriver);
			break;
		case "smartbear":
			if (browserVersion.isEmpty()) {
				browserVersion = defaultIEVersion;
			}
			if (platformOS.isEmpty()) {
				platformOS = defaultPlatformOS;
			}
			//capabilities.setCapability("name", testMethod.get());
			capabilities.setCapability("build", testProperties.getString(TEST_ENV)+" IE-"+platformOS);
			capabilities.setCapability("max_duration", smartBearDefaultTimeout);
			capabilities.setCapability("browserName", browser);
			capabilities.setCapability("version", browserVersion);
			capabilities.setCapability("platform", platformOS);
			capabilities.setCapability("screenResolution", smartBearScreenRes);
			capabilities.setCapability("record_video", "true"); Reporter.log(
					 "BROWSER: " + browser, true); Reporter.log("BROWSER Version: " +
							 browserVersion, true); Reporter.log("PLATFORM: " + platformOS, true);
			Reporter.log("URL '" + serverURL + "'", true); rcDriver = new
			RemoteWebDriver(new URL(serverURL), capabilities); myDriver = new
			Augmenter().augment(rcDriver);
			break;
		default:
			System.setProperty("webdriver.ie.driver", ieDriverLocation);
			myDriver = new InternetExplorerDriver(ieOptions);
			break;
		}
		driver.set(myDriver);
	}

	private void setSafariDriver() throws Exception {
		WebDriver myDriver = null;
		RemoteWebDriver rcDriver;
		capabilities = new DesiredCapabilities();
		
		switch (runLocation.toLowerCase()) {
		case "local":
			myDriver = new SafariDriver();
			break;
		case "grid":
			rcDriver = new RemoteWebDriver(new URL(serverURL), capabilities);
			rcDriver.setFileDetector(new LocalFileDetector());
			myDriver = new Augmenter().augment(rcDriver);
			break;
		case "smartbear":
			if (browserVersion.isEmpty()) {
				browserVersion = defaultSafariVersion;
			}
			if (platformOS.isEmpty()) {
				platformOS = defaultOSX;
			}
			//capabilities.setCapability("name", testMethod.get());
			capabilities.setCapability("build", testProperties.getString(TEST_ENV)+" Safari-"+platformOS);
			capabilities.setCapability("max_duration", smartBearDefaultTimeout);
			capabilities.setCapability("browserName", browser);
			capabilities.setCapability("version", browserVersion);
			capabilities.setCapability("platform", platformOS);
			capabilities.setCapability("screenResolution", smartBearScreenRes);
			capabilities.setCapability("record_video", "true"); Reporter.log(
					 "BROWSER: " + browser, true); Reporter.log("BROWSER Version: " +
							 browserVersion, true); Reporter.log("PLATFORM: " + platformOS, true);
			Reporter.log("URL '" + serverURL + "'", true); rcDriver = new
			RemoteWebDriver(new URL(serverURL), capabilities); myDriver = new
			Augmenter().augment(rcDriver);
			break;
		default:
			break;
		}
		driver.set(myDriver);
	}

	/*
	 * Parse and set the values for browser and if local or remote selenium
	 * server
	 */
	private void parseDriverValues(String browserConfig) {
		String configDelim = "[;]";
		String gridDelim = "[-]";

		// If not passed in, grab default from property file.
		if (browserConfig.isEmpty() || browserConfig == null) {
			browserConfig = "local;" + testProperties.getString(TEST_BROWSER);
		}

		String[] tokens = browserConfig.split(configDelim);
		runLocation = tokens[0].split(gridDelim)[0].toLowerCase();
		browser = tokens[1];

		// Reporter.log("--- Running against '"+runLocation+"'", true);

		switch (runLocation) {
		case "local":
			serverURL = "";
			break;
		case "grid":
			if (tokens[0].contains("-")) {
				// Passed in a specific IP for the grid
				serverURL = "http://" + tokens[0].split(gridDelim)[1] + ":4444/wd/hub";
			} else {
				// Use default grid
				serverURL = "http://" + defaultSeleniumHubIP + ":4444/wd/hub";
			}
			// Reporter.log("-- Grid Server is '"+serverURL+"'", true);
			break;
		case "smartbear":
			serverURL = smartBearURL;
			browserVersion = tokens[2];
			platformOS = tokens[3];
			break;
		case "testingbot":
			serverURL = testBotURL;
			browserVersion = tokens[2];
			platformOS = tokens[3];
			break;
		default:
			serverURL = "";
			break;
		}
	}

	/*
	 * Set the SoftAssert thread
	 */
	protected void setSoftAssert() {
		ValidationUtils mySoftAssert = new ValidationUtils();
		softAssert.set(mySoftAssert);
	}

	/*
	 * Before each test method.
	 */
	protected void BeforeMethod(String baseURL, String browserConfig, locale langValue, String testRailID,
			String bambooBuildKey, String bambooBuildNo) throws Exception {

		/** DOCKER STUFF
		 * System.setProperty("DOCKER_HOST", "tcp://192.168.99.100:2376");
		 * System.setProperty("DOCKER_MACHINE_NAME", "default");
		 * System.setProperty("DOCKER_TLS_VERIFY", "1");
		 * String userDir = System.getProperty("user.dir");
		 * Path userPath = Paths.get(System.getProperty("user.dir"));
		 * userDir = userPath.getRoot().toString()+userPath.subpath(0,
		 * 2).toString()+"\\.docker\\machine\\machines\\default";
		 * System.setProperty("DOCKER_CERT_PATH", userDir);
		 * System.setProperty("DOCKER_CERT_PATH", dockerCerts);
		 */
		// -----------------------------------

		// Set the localization language
		localizationValue.set(langValue);
		// Parse the driver configuration
		parseDriverValues(browserConfig);
		// Set the driver
		setDriver();
		// instantiate softAssert
		setSoftAssert();
		// ** If a testRail ID passed in, set recording the results in TestRail
		// to true
		if (!testRailID.isEmpty() && testRailID.contains(":") && testRailID.split("[:]").length == 2) {
			softAssert.get().recordTestRailResults = true;
			softAssert.get().testRailPlanOrRun = testRailID.split("[:]")[0];
			softAssert.get().testRailID = testRailID.split("[:]")[1];
			Reporter.log("--- TestRail=true ; PlanOrRun=" + softAssert.get().testRailPlanOrRun + " ; ID="
					+ softAssert.get().testRailID, true);
		} else {
			Reporter.log("--- TestRail=false", true);
			softAssert.get().recordTestRailResults = false;
		}
		// ** If running from Bamboo, set Bamboo key and build
		if (!bambooBuildKey.isEmpty() && !bambooBuildNo.isEmpty()) {
			softAssert.get().bambooBuildKey = bambooBuildKey;
			softAssert.get().bambooBuildNo = bambooBuildNo;
		}
		// Load the starting URL
		getDriver().get(baseURL);
	}

	/*
	 * After each test method, set Reporter results.
	 */
	@AfterMethod(alwaysRun = true)
	protected void afterMethod() throws Exception {
		// Close any secondary windows
		try {
			closeAllSecondaryWindows();
		} catch (Exception e1) {
			// do nothing
		}
		Reporter.setCurrentTestResult(null);
		dismissAlert(); // Dismiss any alert that may be open
		// Close Browser
		try {
			getDriver().close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		// Quit Driver
		try {
			getDriver().quit();
		} catch (Exception e) {
			// do nothing
		}
		// ** Docker container would close here
		// stopDocker();
	}

	/**
	 * Dismiss an alert
	 */
	public void dismissAlert() {
		try {
			Alert alert = getDriver().switchTo().alert();
			alert.dismiss();
		} catch (Exception e) {
			// Nothing - No alert
		}
	}

	/**
	 * Accept an alert
	 */
	public void acceptAlert() {
		try {
			Alert alert = getDriver().switchTo().alert();
			alert.accept();
		} catch (Exception e) {
			// nothing
		}
	}

	/**
	 * Will close all windows, except the main window.
	 */
	protected void closeAllSecondaryWindows() throws Exception {
		Set<String> AllWindowHandles = getDriver().getWindowHandles();
		Object[] windows = AllWindowHandles.toArray();

		// Only need to close if more than 1 window open.
		if (windows.length > 1) {
			// Close all BUT the first (0) index of the Window Handle List
			for (int i = 2; i < windows.length; i++) {
				getDriver().switchTo().window((String) windows[i]);
				getDriver().close();
			}
		}
	}

	/**
	 * Refreshes the browser
	 */
	protected void refreshMyBrowser() {
		getDriver().navigate().refresh();
		try {
			Thread.sleep(5000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	/*
	 * This should be called from a catch block within test case. This captures
	 * screenshot, logs message and throws back exception.
	 */
	/**
	 * Call from test case catch blocks. Handles logging of exceptions
	 * 
	 * @param e Exception caught
	 * @param testCases List of TestRails cases associated with given test
	 * @throws Exception throws exception
	 */
	protected void handleException(Exception e, Long[] testCases, long startTime) throws Exception {
		handleException(e, testCases, "", startTime);
	}

	/**
	 * Call from test case catch blocks. Handles logging of exceptions
	 * 
	 * @param e Exception caught
	 * @param testCases List of TestRails cases associated with given test
	 * @param comment Comment string to include for TestRails results
	 * @throws Exception throws exception
	 */
	protected void handleException(Exception e, Long[] testCases, String comment, long startTime) throws Exception {
		String imageFileName = "";
		String stackClassName = "";
		String stackMethodName = "";
		int stackLineNum = 0;

		for (StackTraceElement line : e.getStackTrace()) {
			if (line.getClassName().startsWith("web.prismhr") && !line.getClassName().contains("common")) {
				stackClassName = line.getClassName();
				stackMethodName = line.getMethodName();
				stackLineNum = line.getLineNumber();
			}
		}
		// Record screenshot & Exception Message
		imageFileName = stackMethodName + "-Line" + stackLineNum;
		if (!screenshotAdditionalData.isEmpty()) {
			imageFileName = imageFileName + "-" + screenshotAdditionalData;
		}
		String imagePath = captureScreenshot(imageFileName);
		logScreenshot(imageFileName, imagePath);
		Reporter.log("<br>" + e.toString());
		Reporter.log("\n\t");

		softAssert.get().exceptionOccurred = true; // set true that an exception
													// occurred.
		// ** If Recording TestRail results, record failure due to exception.
		if (softAssert.get().recordTestRailResults && (testCases != null)) {
			Capabilities cap = ((RemoteWebDriver) getDriver()).getCapabilities();
			String myBrowser = cap.getBrowserName() + " " + cap.getVersion();
			String ID = softAssert.get().testRailID;
			String planOrRun = softAssert.get().testRailPlanOrRun;
			String env = testProperties.getString(TEST_ENV);
			Integer status = TestRail.RETEST;
			String testRailComment = "(Automation Run) EXCEPTION / Env: " + env;
			if (!comment.isEmpty()) {
				testRailComment = testRailComment + comment;
			}
			// Get Approximate execution length
			long endTime = System.nanoTime();
			String elapsedTime = genUtils.getElapsedTime(startTime, endTime);
			testRailComment = testRailComment + " / Time: " + elapsedTime;

			// Add Exception Stack to the comments
			testRailComment = testRailComment + " / " + e.toString();

			// * If running from Bamboo, add Bamboo build URL to comment.
			if (!softAssert.get().bambooBuildKey.isEmpty() && !softAssert.get().bambooBuildNo.isEmpty()) {
				testRailComment = testRailComment + " / Bamboo Results: " + softAssert.get().bambooURLBase + "browse/"
						+ softAssert.get().bambooBuildKey + "-" + softAssert.get().bambooBuildNo;
			}
			testRailAPI.addRunTestResult(ID, planOrRun, env, testCases, status, testRailComment, myBrowser);
		}
		// Throw back to test
		throw (e);
	}

	/**
	 * Called from finally blocks. AssertAll the softAsserts Logs end of test
	 * 
	 * @param startTime Start Time
	 * @param methodName Method Name
	 * @param exception Exception from Catch block
	 * @param testCases List of test cases
	 * @param comment Comment string for TestRails results
	 * @throws Exception throws exception
	 */
	protected void handleFinally(long startTime, String methodName, Throwable exception, Long[] testCases,
			String comment) throws Exception {
		long endTime = System.nanoTime();
		String elapsedTime = genUtils.getElapsedTime(startTime, endTime);
		try {
			softAssert.get().assertAll(testCases, comment);
			logTestEnd(elapsedTime, methodName, exception, true, comment);
		} catch (AssertionError e) {
			logTestEnd(elapsedTime, methodName, exception, false, comment);
			throw (e);
		}
	}

	/**
	 * Called from finally blocks. AssertAll the softAsserts Logs end of test
	 * 
	 * @param startTime Start Time
	 * @param methodName Method Name
	 * @param exception Exception from Catch block
	 * @param testCases List of test cases
	 * @throws Exception throws exception
	 */
	protected void handleFinally(long startTime, String methodName, Throwable exception, Long[] testCases)
			throws Exception {
		handleFinally(startTime, methodName, exception, testCases, "");
	}

	/**
	 * Will load the desired URL
	 * 
	 * @param thisURL
	 */
	protected void loadURL(String thisURL) {
		getDriver().get(thisURL);
		try {
			Thread.sleep(2000);
		} catch (Exception e) {
			// nothing
		}
	}


	// *** Logging related
	// TODO: might want to move to own class and expand logging support.

	/**
	 * Log some message
	 * 
	 * @param logString The string message to log
	 */
	protected void logEntry(String logString) {
		Reporter.log(logString, true);
	}

	/**
	 * Log that a test is starting
	 * 
	 * @param methodName The test method name
	 * @return long Start time in Nano time
	 */
	protected long logTestStart(String methodName) {
		String startTimeString = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss").format(new Date());
		logEntry("*** START " + methodName + ":  " + testProperties.getEnvironmentName() + " / " + browser + " / "
				+ startTimeString);
		return System.nanoTime();
	}

	/**
	 * Log that a test is starting
	 * 
	 * @param methodName The test method name
	 * @param comment Comment to add next to method name
	 * @return long Start time in Nano time
	 */
	protected long logTestStart(String methodName, String comment) {
		screenshotAdditionalData = comment;
		String startTimeString = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss").format(new Date());
		logEntry("*** START " + methodName + " (" + comment + "):  " + testProperties.getEnvironmentName() + " / "
				+ browser + " / " + startTimeString);
		return System.nanoTime();
	}

	/**
	 * Log End of a test
	 * 
	 * @param elapsedTime elapsed Time
	 * @param methodName Method Name
	 * @param exception Exception
	 * @param assertsPassed Asserts Passed or Failed
	 */
	protected void logTestEnd(String elapsedTime, String methodName, Throwable exception, boolean assertsPassed) {
		logTestEnd(elapsedTime, methodName, exception, assertsPassed, "");
	}

	/**
	 * Logs the ending of a test, including basic pass/fail, and time to execute
	 * 
	 * @param elapsedTime elapsed Time
	 * @param methodName Method name
	 * @param result pass/fail result
	 */
	protected void logTestEnd(String elapsedTime, String methodName, Throwable exception, boolean assertsPassed,
			String comment) {
		String result = "";
		if ((exception != null) || (!assertsPassed)) {
			result = "FAILED";
		} else {
			result = "passed";
		}

		if (!comment.isEmpty()) {
			logEntry("**** END " + methodName + " (" + comment + "): Result - " + result + " / Timer - " + elapsedTime);
		} else {
			logEntry("**** END " + methodName + ": Result - " + result + " / Timer - " + elapsedTime);
		}
	}


	/**
	 * Hard pause for given period of time
	 * 
	 * @param milliseconds time to pause in milliseconds
	 */
	public void pause(int milliseconds) {
		try {
			Thread.sleep(milliseconds);
		} catch (Exception e) {
			// Nothing to capture
		}
	}



	// ----- TODO: DOCKER STUFF ------
	/*
	 * ThreadLocal<DockerClient> docker = new ThreadLocal<DockerClient>();
	 * ThreadLocal<ContainerCreation> currentContainer = new
	 * ThreadLocal<ContainerCreation>(); ThreadLocal<String> currentContainerID
	 * = new ThreadLocal<String>(); String chromeImage =
	 * "selenium/node-chrome:2.53.1"; //String chromeHubContainer =
	 * "69705e936fd9"; //** Full Docker Setup public void runDocker() throws
	 * Exception { dockerSetClient(); dockerCreateChromeContainer();
	 * dockerStartContainer(); } public void stopDocker() throws Exception {
	 * dockerStopContainer(); dockerRemoveContainer(); dockerCloseClient(); }
	 * //** Set the Docker Client public void dockerSetClient() throws Exception
	 * { docker.set(DefaultDockerClient.fromEnv().build()); } //** Close the
	 * Docker Client connection public void dockerCloseClient() throws Exception
	 * { docker.get().close(); } //** Create a new Chrome/Node Container public
	 * void dockerCreateChromeContainer() throws Exception { HostConfig
	 * hostConfig = HostConfig.builder().links("selenium-hub:hub").build();
	 * ContainerConfig containerConfig =
	 * ContainerConfig.builder().image(chromeImage).hostConfig(hostConfig).build
	 * (); currentContainer.set(docker.get().createContainer(containerConfig));
	 * currentContainerID.set(currentContainer.get().id().substring(0, 12)); }
	 * //** Remove a Container public void dockerRemoveContainer() throws
	 * Exception { docker.get().removeContainer(currentContainerID.get()); }
	 * //** Start a Container public void dockerStartContainer() throws
	 * Exception { docker.get().startContainer(currentContainerID.get());
	 * pause(10000); } //** Stop a Container public void dockerStopContainer()
	 * throws Exception { docker.get().killContainer(currentContainerID.get());
	 * pause(10000); }
	 */
}
