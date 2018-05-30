package common.env;


/**
 * This class defines the various variables that can be found
 * in the property file.
 * 
 * @author mlabbe
 *
 */
public interface TestProperties {
	public static EnvConfiguration testProperties = EnvConfiguration.getInstance();

	
	public static final String TEST_ENV = "test.ENV";
	public static final String TEST_BROWSER = "test.browser.type";
	

	// *** URLS ***
	public static final String APP_URL = "app.URL";
	
	// *** Logins ***
	public static final String ONE_USERNAME = "one.username";
	public static final String ONE_PASSWORD = "one.password";

	
}
