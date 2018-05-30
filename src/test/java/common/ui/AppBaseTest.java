package common.ui;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Optional;
import org.testng.annotations.Parameters;

/**
 * This class is the base class for all PrismHR based tests.
 * If a test(class) will want to initially launch PrismHR, it should extend this class
 * @author mlabbe
 *
 */
public class AppBaseTest extends BaseTest {
	String baseURL = testProperties.getString(APP_URL);
	protected String baseTarget = "";
	
	
	@Parameters({"browser_config", "lang_value", "testrail_id", "bamboo_buildkey", "bamboo_buildno"})
	@BeforeMethod(alwaysRun=true)
	public void BeforeMethod(@Optional("") String browserConfig, @Optional("EN") String langValue, @Optional("") String testRailID, 
			@Optional("") String bambooBuildKey, @Optional("") String bambooBuildNo) throws Exception {
		super.BeforeMethod(baseURL, browserConfig, locale.valueOf(langValue), testRailID, bambooBuildKey, bambooBuildNo);
	}
	
}
