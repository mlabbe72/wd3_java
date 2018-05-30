package app.sample;

import org.testng.annotations.Test;

import app.pageobjects.AppBasePage;
import common.ui.AppBaseTest;

public class SampleTestClass extends AppBaseTest {

	@Test
	public void SampleTest() throws Exception {
		Long[] testCases = {(long) 28747, (long) 28761, (long) 28762};
		String methodName = Thread.currentThread().getStackTrace()[1].getMethodName();
		long startTime = logTestStart(methodName);
		Throwable t = null;

		AppBasePage appBase = new AppBasePage(getDriver()); // instantiate initial page
		try {
			// Test Steps
			
		} catch (Exception e) {
			t = e; // to use for finally
			handleException(e, testCases, startTime);
		} finally {
			handleFinally(startTime, methodName, t, testCases);
		}
	}
	
}
