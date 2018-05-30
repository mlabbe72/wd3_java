package app.pageobjects;


import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;

import common.ui.BasePage;

public class AppBasePage extends BasePage {

	/*
	 * Constructor for each page object
	 * - used to verify that the page has fully loaded.
	 * - can handle alerts or popups that may occur upon opening the page
	 */
	public AppBasePage(WebDriver driver) throws Exception {
		super(driver);
		getDriver().switchTo().defaultContent();
		PageFactory.initElements(driver, this);
		assertElementPresent(By.xpath("//td[@title='MyLogo']"), defaultWaitTime);
	}

	@FindBy(css = "img[src='images/logo.png']")
	protected WebElement homeLogoImage;
	
	/*
	 * navigating and return new instance of an expected page.
	 */
	@SuppressWarnings("unchecked")
	public <W> W clickHomeLogo(Class<W> expectedPage) throws Exception {
		click(homeLogoImage);
		return (W) expectedPage.getConstructors()[0].newInstance(getDriver());
	}
	
}
