package common.ui;

import org.openqa.selenium.Alert;
import org.openqa.selenium.By;
import org.openqa.selenium.Capabilities;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.Keys;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
//import org.openqa.selenium.htmlunit.HtmlUnitDriver;
import org.openqa.selenium.ie.InternetExplorerDriver;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.Reporter;

import common.utils.ExcelDataUtil;
import common.utils.ReflectionUtils;
import common.utils.ValidationUtils;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * This class is the root class for pages in the applications.
 * It contains wrapper methods.
 * 
 * @author mlabbe
 *
 */
public class BasePage extends ValidationUtils {
	protected WebDriver driver;
	public final int defaultLongWaitTime = 60; // Max long wait time
	public final int defaultLoginPageWait = 30; // Max seconds to wait for Login Page to render
	public final int defaultWaitTime = 20;  // Seconds for standard default timeout waiting for elements.
	public final int defaultShortWaitTime = 10; // Short wait time.
	
	// For switching between windows, such as when help or video is open.
	public Set<String> AllWindowHandles;
	public String window1;
	public String window2;

	// This is where we will store downloaded files, such as downloaded reports and PDFs
	public static final String myTempDownloadsFolder = ""; // "C:\\temp_downloads_"+testProperties.getString(TEST_ENV);

	// This is where we will store files to be uploaded for various tests.
	protected static final String uploadLocation = "src/test/resources/uploads/";
	protected static final String envDataLocationBase = "src/main/resources/testdata/";
	
	// File holding Locale text strings
	private static final String localeExcelFile = "Locales.xlsx";

	// TODO: see if this is really needed or if we can get rid of all instances of driver, including constructors.
	public BasePage(WebDriver driver) {
		this.driver = driver;
	}
	
	/**
	 * Fetches localized strings from the Locales.xlsx sheet, based on the given list of keys for the given app
	 * Used from within a page object constructor BEFORE the initElements.
	 * @param keys		list of keys to fetch strings for
	 * @param app		the application we are looking for
	 * @return HashMap	A map of the keys and their localized strings
	 * @throws Exception		throws Exception
	 */
	protected HashMap<String, String> getLocaleStrings(String[] keys, String app) throws Exception {
		HashMap<String, String> returnSet = new HashMap<String, String>();
		ExcelDataUtil excelDataUtil = new ExcelDataUtil();

		for (String key : keys) {
			returnSet.put(key, excelDataUtil.getLocaleKeyValue(localeExcelFile, app, key, localizationValue.get()));
		}
		return returnSet;
	}
	
	// ------ Alert Dialogs -------
	/**
	 * Clicks on Alert Cancel button if an alert appears
	 * @param waitTimeout		timeout looking for alert
	 * @return String		Alert message
	 */
	public String dismissAlert(Integer waitTimeout) {
		String alertMsg = "";
		if (waitTimeout == null) {
			waitTimeout = 5;
		}
		WebDriverWait wait = new WebDriverWait(getDriver(), waitTimeout);
		try {		
			if (wait.until(ExpectedConditions.alertIsPresent())!=null) {
				Alert alert = getDriver().switchTo().alert();
				alertMsg = alert.getText();
				alert.dismiss();
			}
		} catch (Exception e) {
			// Nothing - No alert
		}
		return alertMsg;
	}

	/**
	 * Clicks on Alert OK button if an alert appears
	 * @param waitTimeout		timeout looking for alert
	 * @return String		Alert message
	 */
	public String acceptAlert(Integer waitTimeout) {
		String alertMsg = "";
		if (waitTimeout == null) {
			waitTimeout = 5;
		}

		WebDriverWait wait = new WebDriverWait(getDriver(), waitTimeout);
		try {
			if (wait.until(ExpectedConditions.alertIsPresent())!=null) {
				Alert alert = getDriver().switchTo().alert();
				alertMsg = alert.getText();
				alert.accept();
			}
		} catch (Exception e) {
			// nothing
		}
		return alertMsg;
	}
	// ----------------------------
	
	
	/**
	 * Checks if an element is present within the specified timeout
	 * Throws exception if times out.
	 *  @param by  The element identifier
	 *  @param maxWaitInSeconds  The number of seconds as timeout value
	 *  @throws Exception  throws Exception
	 */
	public void assertElementPresent(By by, int maxWaitInSeconds) throws Exception {
		(new WebDriverWait(getDriver(), maxWaitInSeconds)).until(ExpectedConditions.presenceOfElementLocated(by));
	}
	
	/**
	 * Checks that an element is visible, using the locator within the timeout.
	 * Throws exception if times out.
	 * @param by  The element identifier
	 * @param maxWaitInSeconds		
	 * 				The number of seconds as a timeout value
	 * @throws Exception
	 * 			throws Exception
	 */
	public void assertElementLocatorVisible(By by, int maxWaitInSeconds) throws Exception {
		(new WebDriverWait(getDriver(), maxWaitInSeconds)).until(ExpectedConditions.visibilityOfElementLocated(by));
	}
	
	/**
	 * Checks if an element is visible within the timeout.
	 * Throws exception if times out
	 * @param element		The element
	 * @param maxWaitInSeconds		The number of seconds as a timeout value
	 * @throws Exception  throws Exception
	 */
	public void assertElementVisible(WebElement element, int maxWaitInSeconds) throws Exception {
		(new WebDriverWait(getDriver(), maxWaitInSeconds)).until(ExpectedConditions.visibilityOf(element));
	}

	/**
	 * Checks if an element becomes invisible within the timeout.
	 * Throws exception if times out.
	 * @param by		The element identifier
	 * @param maxWaitInSeconds		The number of seconds as a timeout value
	 * @throws Exception  throws Exception
	 */
	public void assertElementLocatorInvisible(By by, int maxWaitInSeconds) throws Exception {
		(new WebDriverWait(getDriver(), maxWaitInSeconds)).until(ExpectedConditions.invisibilityOfElementLocated(by));
	}
	
	/**
	 * Checks if an element becomes invisible within the timeout, by Element
	 * Throws exception if times out.
	 * @param element		The element
	 * @param maxWaitInSeconds		The number of seconds as a timeout value.
	 * @throws Exception	throws Exception
	 */
	public void assertElementInvisible(WebElement element, int maxWaitInSeconds) throws Exception {
		List<WebElement> elements = new ArrayList<WebElement>();
		elements.add(element);
		(new WebDriverWait(getDriver(), maxWaitInSeconds)).until(ExpectedConditions.invisibilityOfAllElements(elements));
	}
	
	
	/**
	 * Checks if an element has been refreshed/redrawn within the timeout.
	 * Throws exception if times out. 
	 * @param element		The element
	 * @param maxWaitInSeconds		The number of seconds as a timeout value
	 * @throws Exception  throws Exception
	 */
	public void assertElementRefreshed(WebElement element, int maxWaitInSeconds) throws Exception {
		(new WebDriverWait(getDriver(), maxWaitInSeconds)).until(ExpectedConditions.refreshed(ExpectedConditions.visibilityOf(element)));
	}
	
	public void myAssertElementPresent(By by, int maxWaitInSeconds) throws Exception {
		boolean foundIt = false;
		Long start = System.nanoTime();
		while ((((double) System.nanoTime() - start) / 1000000000.0) < maxWaitInSeconds) {
			if (getDriver().findElements(by).size() > 0) {
				foundIt = true;
				break; // Found it
			}
		}
		if (!foundIt) {
			throw new NoSuchElementException("Element '"+by.toString()+"' NOT FOUND.");
		}
	}
	
	/**
	 * Is this element currently displayed or not? This method avoids the
	 * problem of having to parse an element's "style" attribute.
	 * 
	 * @param element		The element
	 * @return boolean		Whether or not the element is displayed
	 */
	protected boolean isDisplayed(WebElement element) {
		return element.isDisplayed();
	}

	/**
	 * Returns true/false if an element is visible
	 * @param element		The element
	 * @param maxWaitInSeconds		seconds to wait
	 * @return boolean		True/False
	 */
	public boolean isElementVisible(WebElement element, int maxWaitInSeconds) {
		try {
			(new WebDriverWait(getDriver(), maxWaitInSeconds)).until(ExpectedConditions.visibilityOf(element));
			return true;
		} catch (Exception e) {
			return false;
		}
	}
	
	/**
	 * Returns true/false if the element is visible
	 * @param locator		The locator to look for
	 * @param maxWaitInSeconds		seconds to wait
	 * @return boolean		True/False
	 */
	public boolean isElementVisibleByLocator(By locator, int maxWaitInSeconds) {
		try {
			(new WebDriverWait(getDriver(), maxWaitInSeconds)).until(ExpectedConditions.visibilityOf(getDriver().findElement(locator)));
			return true;
		} catch (Exception e) {
			return false;
		}
	}

	/**
	 * Verifies that an element is clickable within the specified timeout
	 * 	@param locator  The element locator value
	 * 	@param maxWaitInSeconds The number of seconds as a timeout value
	 */
	public void assertElementClickable(By locator, int maxWaitInSeconds) {
		(new WebDriverWait(getDriver(), maxWaitInSeconds)).until(ExpectedConditions
				.elementToBeClickable(locator));
	}

	protected String getElementName(WebElement element) {
		Field field = ReflectionUtils.findField(this, element);
		return field == null ? null : field.getDeclaringClass().getName() + "."
				+ field.getName();
	}

	/*
	 * IE workaround for Click Method
	 *  - Uses 'sendKeys' for specific element types
	 */
	private void internetExplorerClickWorkAround(WebElement element) {
		String tagName = element.getTagName();
		//new Actions(driver).moveToElement(element).perform();
		if (tagName.equals("a")) {
			element.sendKeys(Keys.ENTER);
			return;
		} else if (tagName.equals("input")) {
			String attributeType = element.getAttribute("type");
			if (attributeType.equals("checkbox")) {
				element.sendKeys(Keys.SPACE);
				return;
			} else if (attributeType.equals("radio")) {
				element.sendKeys(Keys.SPACE);
				return;
			} else if (attributeType.equals("button")) {
				element.sendKeys(Keys.ENTER);
				return;
			} else if (attributeType.equals("reset")) {
				element.sendKeys(Keys.ENTER);
				return;
			} else if (attributeType.equals("submit")) {
				element.sendKeys(Keys.ENTER);
				return;
			} else if (attributeType.equals("image")) {
				element.click();
				return;
			} else {
				element.click();
				return;
			}
		} else {
			element.click();
			return;
		}
	}

	/**
	 * Click Method:
	 *    - contains IE workaround
	 *    - contains input type 'submit'
	 *    - logs what element is being clicked
	 *    @param element		The element to click
	 */
	protected void click(WebElement element) {
		// IE work around
		if (getDriver() instanceof InternetExplorerDriver) {
			internetExplorerClickWorkAround(element);
			return;
		}

		// Other Browsers
		String tagName = element.getTagName();
		String attributeType = element.getAttribute("type");
		if (tagName.equals("input")
				&& (attributeType.equals("submit"))) {
			element.submit();
			return;
		}
		element.click();
	}


	/**
	 * Sets text for an input type.
	 *   - Clears any existing text in the input element.
	 *   - Tabs out of element after entering text.
	 * @param element		The element to set text for
	 * @param text			The text to set
	 */
	protected void setInputText(WebElement element, String text) {
 		click(element);
 		element.clear();
 		pause(500);
		element.sendKeys(text);
		pause(500);
		element.sendKeys(Keys.TAB);
	}
	
	protected void setInputText(WebElement element, String text, boolean backspace) {
		click(element);
		if (backspace) {
			element.sendKeys(Keys.END); // Handles cases where click places cursor on wrong end of string
			int len = element.getAttribute("value").length();
			for (int i=0; i<=len; i++) {
				element.sendKeys(Keys.BACK_SPACE);
			}
		} else {
			element.clear();
		}
		element.sendKeys(text);
		element.sendKeys(Keys.TAB);
	}
	
	/**
	 * Sets text for an input type.
	 *   - Clears any existing text in the input element.
	 *   - Does not tab out of element after entering text.
	 * @param element		The element to set text for
	 * @param text			The text to set
	 */
	protected void setInputTextNoTab(WebElement element, String text) {
		click(element);
 		element.clear();
 		pause(500);
 		element.sendKeys(text);
 		pause(500);
	}
	
	protected void setInputBackspaceText(WebElement element, String text) {
 		click(element);
 		//instead of clear
 		element.sendKeys(Keys.END); // Handles cases where click places cursor on wrong end of string
 		for (int i=0; i<7; i++) {
 			element.sendKeys(Keys.BACK_SPACE);
 		} 		
 		pause(500);
		element.sendKeys(text);
		pause(500);
		element.sendKeys(Keys.TAB);
	}
		
	/**
	 * Sets text for an input type, BUT
	 * instead of tabbing out, it hits Enter key.
	 * @param element
	 * @param text
	 */
	protected void setInputTextEnter(WebElement element, String text) {
		click(element);
		element.clear();
		pause(500);
		element.sendKeys(text);
		pause(500);
		element.sendKeys(Keys.ENTER);
	}
	
	/**
	 * Sets text for a Date input type.
	 * - Instead of Clear, we backspace (10 would handle mm/dd/yyyy)
	 * - Tab out after entering text.
	 * @param element		The element to set date text for
	 * @param value			The date to set
	 */
	protected void setDateInputText(WebElement element, String value) {
 		click(element);
 		// Be sure cursor is at end of Date text
		element.sendKeys(Keys.END); // Handles cases where click places cursor on wrong end of string
 		element.sendKeys("1"); // Handles case where having a blank date causes error
 		pause(1500);
 		// Backspace to erase any current date
 		for (int i=0; i<11; i++) {
 			element.sendKeys(Keys.BACK_SPACE);
 		}
 		element.sendKeys(value);
 		pause(2000);
		element.sendKeys(Keys.TAB);
		pause(1000);
	}
	
	protected void setDateInputTextNoTab(WebElement element, String value) {
 		click(element);
 		 		// Be sure cursor is at end of Date text
		element.sendKeys(Keys.END); // Handles cases where click places cursor on wrong end of string
 		element.sendKeys("1"); // Handles case where having a blank date causes error
 		pause(1500);
 		// Backspace to erase any current date
 		for (int i=0; i<11; i++) {
 			element.sendKeys(Keys.BACK_SPACE);
 		}
 		element.sendKeys(value);
 		pause(2000);
	}

	protected void setDateInputNoClickText(WebElement element, String value) {	
 		// Be sure cursor is at end of Date text
		element.sendKeys(Keys.END); // Handles cases where click places cursor on wrong end of string
 		 		// Backspace to erase any current date
 		for (int i=0; i<10; i++) {
 			element.sendKeys(Keys.BACK_SPACE);
 		}
 		element.sendKeys(value);
		element.sendKeys(Keys.TAB);
	}

	/**
	 * Get the content of an input text field.
	 * 
	 * @param element
	 *            the element identifying the text field.
	 * @return the text in the text field
	 */
	protected String getInputText(WebElement element) {
		return element.getAttribute("value");
	}
	/**
	 * A service method for Page Objects to an HTML Select type object (both for
	 * lists and drop downs). This method for accessing and mutating the select
	 * object were not included directly here, as there are quite a few, and too
	 * many methods would have to be defined for each element in each Page
	 * Object.
	 * 
	 * @param element
	 *            the select element
	 * @return the object for manipulating this select object. This object
	 *         remains valid as long as the containing Page Object remains valid
	 *         (not including JavaScript dynamic editing of DOM)
	 */
	protected SelectFixture getSelectObject(WebElement element) {
		return new SelectFixture(element, getElementName(element));
	}

	protected TableFixture getTableObject(WebElement element) throws Exception {
		return new TableFixture(element, getElementName(element));
	}
	
	/**
	 * Determine is a text string is anywhere on the current page.
	 * 
	 * @param string
	 *            any string
	 * @return <code>true</code> if found; Otherwise, <code>false</code>
	 */
	public boolean isStringOnPage(String string) {
		return getDriver().getPageSource().indexOf(string) != -1;
	}
	

	/**
	 * Check for a given string for a period of time
	 * @param string		string to look for
	 * @param timeout		max seconds to wait
	 * @return boolean		true/false
	 */
	public boolean isStringOnPage(String string, long timeout) {
		boolean returnValue = false;
		long startTime = System.nanoTime();
		while ((((double) System.nanoTime() - startTime) / 1000000000.0) < timeout) {
			returnValue = getDriver().getPageSource().indexOf(string) != -1;
			if (returnValue) { // Stop waiting for timeout to hit
				break;
			}
		}
		return returnValue;
	}
	
	
	/**
	 * A service method for Page Objects to get the visible (i.e. not hidden by
	 * CSS) innerText of this element, including sub-elements, without any
	 * leading or trailing whitespace.
	 * 
	 * @param element 	The element
	 * @return The innerText of this element.
	 */
	protected String getDisplayText(WebElement element) {
		return element.getText();
	}

	/**
	 * Initiates a hard pause for x number of milliseconds
	 * @param milliseconds	pause for this number of milliseconds
	 */
	public void pause(int milliseconds) {
		try {
			Thread.sleep(milliseconds);
		} catch (InterruptedException e) {
			// nothing to do
		}
	}
	
	
	/**
	 * Returns true if element is enabled and false if disabled
	 * @param element	the element
	 * @return boolean	return true/false
	 */
	protected boolean isEnabled(WebElement element) {
		return element.isEnabled();
	}
	
	/**
	 * Returns true/false if a checkbox is checked 
	 * @param element	the element
	 * @return boolean	Return true/false
	 */
	protected boolean isChecked(WebElement element) {
		return element.isSelected();
	}
	
	/**
	 * Checks/unchecks a checkbox
	 * @param element	The element
	 * @param checkRequested	true/false to check
	 */
	protected void setChecked(WebElement element, boolean checkRequested) {
		boolean isChecked = element.isSelected();
		if ((checkRequested && !isChecked) || (!checkRequested && isChecked)) {
			click(element);
		}
	}

	/**
	 * This accepts an alert, and returns the message text
	 * @param timeoutSeconds	time out value
	 * @return String		Text Message
	 */
	protected String checkAlert(int timeoutSeconds) {
		String returnValue = "";
	    try {
	        WebDriverWait wait = new WebDriverWait(getDriver(), timeoutSeconds);
	        wait.until(ExpectedConditions.alertIsPresent());
	        Alert alert = getDriver().switchTo().alert();
	        returnValue = alert.getText();
	        alert.accept();
	    } catch (Exception e) {
	        //exception handling
	    }
	    return returnValue;
	}
	
	/**
	 * Set Implicit Wait to 0.
	 */
	protected void nullifyImplicitWait() {
		introduceImplicitWaitPeriod(0);
	} 

	/**
	 * Set Implicit Wait to a given number of seconds.
	 * @param waitTimeSeconds	time out
	 */
	protected void introduceImplicitWaitPeriod(int waitTimeSeconds) {
		getDriver().manage().timeouts().implicitlyWait(waitTimeSeconds, TimeUnit.SECONDS);
	}

	/**
	 * Check for existence of 2 browser windows open.
	 * @param waitTimeSeconds	time out
	 * @throws Exception	throws Exception
	 */
	protected boolean checkForMultipleWindows(int waitTimeSeconds) throws Exception {
		//TODO: may want to have number of expected windows be a parameter.  for now 2 is good.
		try {
			(new WebDriverWait(getDriver(), waitTimeSeconds)).until(ExpectedConditions.numberOfWindowsToBe(2));
			return true;
		} catch (Exception e) {
			return false;
		}
	}
	
	/**
	 * Scrolls an element into view
	 * @param element
	 */
	protected void scrollElementIntoView(WebElement element) {
		((JavascriptExecutor) getDriver()).executeScript("arguments[0].scrollIntoView(true)", element);
	}
	
	/**
	 * Sends x number of key tabs
	 * @param numTabs		how many tabs to send
	 */
	public void sendTab(int numTabs) {
		for (int i=0; i<numTabs; i++) {
			new Actions(getDriver()).sendKeys(Keys.TAB).build().perform();
		}
	}
	
	/**
	 * To use for page constructors.  Better error messaging when a page is not detected.
	 * Plus a screenshot capture to help.
	 * @param e		the exception
	 * @param expectedPage		text passed in to include in exception text - typically name of expected form
	 * @throws Exception		throws exception
	 */
	protected void handlePageNotFoundException(Exception e, String expectedPage) throws Exception {
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
		Reporter.log("<br>"+e.toString());
		Reporter.log("\n\t");

		throw new Exception("!!! Exception: Failed to detect '"+expectedPage+"'");
	}
	
	/**
	 * Return the name of the current browser (chrome, firefox, internet explorer)
	 * @return String 	current browser
	 */
	public String getBrowserType() {
		String returnValue = "";
		
		Capabilities cap = ((RemoteWebDriver) driver).getCapabilities();
		returnValue = cap.getBrowserName();
		
		return returnValue;
	}
	
}
