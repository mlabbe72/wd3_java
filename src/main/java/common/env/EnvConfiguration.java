package common.env;

import java.io.FileInputStream;
import java.util.HashMap;

import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.testng.Reporter;

public class EnvConfiguration {

	// Where to read Property File from
	private static final String PROPERTY_BASE = System.getProperty("user.dir") + "/src/test/resources/";
	// Name of Property Excel file
	private static final String PROPERTY_FILE = "env_properties.xlsx";

	private String envName = null;
	private static EnvConfiguration environment = null;

	private EnvConfiguration(String envName) {
		setEnvironmentName(envName);
	}

	public static synchronized EnvConfiguration getInstance() {
		if (environment == null) {
			environment = new EnvConfiguration(getEnvironmentFromSystem());
		}
		return environment;
	}

	// Get the TEST_ENV environment variable. If null or empty default to
	// LOCALHOST
	private static String getEnvironmentFromSystem() {
		String env = System.getenv("TEST_ENV").trim();
		// Default to LOCALHOST if desired not found.
		if ((env == null) || (env.isEmpty())) {
			env = "LOCALHOST";
		}
		Reporter.log("!!!Test Environment Request is " + env, true);
		return env.toUpperCase();
	}
	
	// Fetch ALL environments available from Excel - Check if desired ENV is in
	// list
	private String setEnvironmentName(String envName) {
		boolean envFound = false;
		this.envName = (envName != null) ? envName : getEnvironmentFromSystem();

		try {
			// XSSFWorkbook excelBook;
			FileInputStream excelFile = new FileInputStream(PROPERTY_BASE + PROPERTY_FILE);
			XSSFWorkbook excelBook = new XSSFWorkbook(excelFile);
			int sheetCount = excelBook.getNumberOfSheets();
			// Reporter.log("!!! Sheet Count is " + sheetCount, true);
			for (int i = 0; i < sheetCount; i++) {
				String sheetName = excelBook.getSheetName(i);
				// Reporter.log("!!! Sheet Name: " + sheetName, true);
				if (sheetName.equals(this.envName)) {
					envFound = true;
					break;
				}
			}
			excelFile.close();
			// excelBook.close();
			if (!envFound) {
				Reporter.log("!! Requested Env " + envName + " sheet NOT FOUND - Setting Default !!", true);
				this.envName = "LOCALHOST";
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		getPropertyPairs();
		Reporter.log("!!! Test Environment Selected is " + this.envName, true);
		return envName;
	}

	HashMap<String, String> configData = new HashMap<>();

	// Fetch ALL property/value pairs from desired ENV sheet
	private void getPropertyPairs() {
		XSSFWorkbook excelBook;
		XSSFSheet excelSheet;
		XSSFRow excelRow;
		int searchKeyColumn = 0;
		int valueColumn = 1;

		try {
			FileInputStream excelFile = new FileInputStream(PROPERTY_BASE + PROPERTY_FILE);
			excelBook = new XSSFWorkbook(excelFile);
			Reporter.log("GET CONFIG FOR: '" + this.envName + "'", true);
			excelSheet = excelBook.getSheet(this.envName);

			int rowCount = excelSheet.getLastRowNum() - excelSheet.getFirstRowNum();
			Reporter.log("FOUND ROWS: " + rowCount, true);
			for (int i = 0; i <= rowCount; i++) {
				excelRow = excelSheet.getRow(i);
				if (excelRow != null) {
					String rowValue = excelRow.getCell(valueColumn).toString();
					String rowKey = excelRow.getCell(searchKeyColumn).toString();
					Reporter.log("ADDING: " + rowKey + " : " + rowValue,
					 true);
					configData.put(rowKey, rowValue);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	// Fetch value based on property(key)
	public String getString(String key) {
		if (configData.containsKey(key)) {
			return configData.get(key);
		} else {
			Reporter.log("!! PROPERTY '" + key + "' NOT FOUND !!", true);
			return "";
		}
	}

	public String getEnvironmentName() {
		return this.envName;
	}

}
