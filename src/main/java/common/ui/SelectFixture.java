package common.ui;

import java.util.ArrayList;
import java.util.List;

import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.Select;

/**
 * A Helper object for Web Page object that assists with interacting with an
 * HTML Select object (also known as Options)
 * <p>
 * The SelectFixture acts as an interface to the HTML Select object. The select
 * object allows the programmer to offer a list of options. In the terms of HTML
 * documentation the option is the text displayed, while the attribute returned
 * to the program when the user selects an option is the value. The
 * SelectFixture methods that interact with the Option interact with the display
 * value. Those that interact with the value interact with the 'value' attribute
 * returned to the program when a selection occurs. Normal testing interacts
 * with the Options. The value methods are available for those special cases
 * (can't think of one now) where such access Is needed.
 * 
 * 
 */
public class SelectFixture {

	//private final FrameNavigator frameNavigator;
	@SuppressWarnings("unused")
	private final WebElement element;
	private final Select select;
	@SuppressWarnings("unused")
	private String elementName;

	/**
	 * Constructor for simple Web pages
	 * 
	 * @param element
	 *            the web element used to located a select element
	 */
	/*public SelectFixture(WebElement element, String elementName) {
		this(element, elementName);
	}*/

	/**
	 * Constructor for Web pages that implements frames
	 * 
	 * @param element
	 *            the web element used to located a select element
	 * @param elementName
	 * 			element name
	 */
	public SelectFixture(WebElement element, String elementName) {
		//this.frameNavigator = frameNavigator;
		this.element = element;
		this.elementName = elementName;
		//switchFrame();
		this.select = new Select(element);
	}

	/*private void switchFrame() {
		if (frameNavigator != null) {
			frameNavigator.switchFrame(element);
		}
	}*/

	/**
	 * Does the select object allow multiple select.
	 * 
	 * @return true, if it does; otherwise, false.
	 */
	public boolean isMultiple() {
		//switchFrame();
		return select.isMultiple();
	}

	/**
	 * Get the list of option from which the user can select
	 * 
	 * @return the list of option from which the user can select
	 */
	public List<String> getOptions() {
		//switchFrame();
		List<String> optionList = new ArrayList<String>();
		for (WebElement option : select.getOptions()) {
			optionList.add(option.getText());
		}
		return optionList;
	}

	/**
	 * Get the list of values returned that can be returned to the back-end.
	 * 
	 * @return the list of option from which the user can select
	 */
	public List<String> getValues() {
		//switchFrame();
		List<String> optionList = new ArrayList<String>();
		for (WebElement option : select.getOptions()) {
			optionList.add(option.getAttribute("value"));
		}
		return optionList;
	}

	/**
	 * Get all the currently Selected options (by user or driver)
	 * 
	 * @return all the currently Selected options (by user or driver)
	 */
	public List<String> getAllSelectedOptions() {
		//switchFrame();
		List<String> optionList = new ArrayList<String>();
		for (WebElement option : select.getAllSelectedOptions()) {
			optionList.add(option.getText());
		}
		return optionList;
	}

	/**
	 * Get all the values of the currently Selected options (by user or driver)
	 * 
	 * @return all the values of the currently Selected options (by user or
	 *         driver)
	 */
	public List<String> getAllSelectedValues() {
		//switchFrame();
		List<String> optionList = new ArrayList<String>();
		for (WebElement option : select.getAllSelectedOptions()) {
			optionList.add(option.getAttribute("value"));
		}
		return optionList;
	}

	/**
	 * Get the first selected option (could be only one)
	 * 
	 * @return the first selected option
	 * @throws NoSuchElementException
	 *             if no element is selected
	 */
	public String getFirstSelectedOption() throws NoSuchElementException {
		//switchFrame();
		return select.getFirstSelectedOption().getText();
	}

	/**
	 * Get the values of the first selected option (could be only one)
	 * 
	 * @return the first selected option
	 * @throws NoSuchElementException
	 *             if no element is selected
	 */
	public String getFirstSelectedValue() throws NoSuchElementException {
		//switchFrame();
		return select.getFirstSelectedOption().getAttribute("value").trim();
	}

	/**
	 * Select an option in the select object. This does not de-select other
	 * options, if this is a multi-select object.
	 * 
	 * @param option
	 *            the option to select
	 * @throws NoSuchElementException
	 *             if no such option is found
	 */
	public void selectByOption(String option) throws NoSuchElementException {
		//switchFrame();
		//WebLogger.logAction("SELECT option " + option + " in " + elementName);
		select.selectByVisibleText(option);
	}

	/**
	 * Select an option based on partial visible text match.
	 * @param option
	 * 			the partial text to match for selecting
	 */
	public void selectByOptionContains(String option) {
		List<WebElement> options = select.getOptions();
		for (WebElement oneOption : options) {
			String optionText = oneOption.getText();
			if (optionText.contains(option)) {
				select.selectByVisibleText(optionText);
				break;
			}
		}
	}
	
	/**
	 * Select an option in the select object by its value. This does not
	 * de-select other options, if this is a multi-select object.
	 * 
	 * @param value
	 *            the value of the option to select
	 * @throws NoSuchElementException
	 *             if no such option is found
	 */
	public void selectByValue(String value) throws NoSuchElementException {
		//switchFrame();
		select.selectByValue(value);
	}

	/**
	 * De-select all selected options.
	 */
	public void deselectAll() {
		//switchFrame();
		select.deselectAll();
	}

	/**
	 * De-Select an option in the select object. This does not affect any other
	 * option.
	 * 
	 * @param option
	 *            the option to de-select
	 */
	public void deselectByOption(String option) {
		//switchFrame();
		select.deselectByVisibleText(option);
	}

	/**
	 * De-Select an option in the select object by its value. This does not
	 * affect any other option.
	 * 
	 * @param value
	 *            the option to de-select
	 */
	public void deselectByValue(String value) {
		//switchFrame();
		select.deselectByValue(value);
	}
}