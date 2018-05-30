package common.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Random;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.testng.Reporter;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import common.ui.BasePage;

/**
 * This Class contains general utilities, such as
 *  - Date utilities
 *  - Number generators
 *  - String parsing, etc.
 *  
 * @author mlabbe
 *
 */
public class GeneralUtils {


	public GeneralUtils() {
		//
	}

	/**
	 * Returns today's date in the desired format.
	 * Format should be valid
	 * @param format	Format to return
	 * @return String	Today's date
	 */
	public String getTodayDateString(String format) {
		return new SimpleDateFormat(format).format(new Date());	
	}
	
	//TODO:  investigate further about grabbing time and making central
	/**
	 * Returns the current time-1 hour.
	 * Given this runs in East Coast (New York) time, the result
	 * should be Central (Chicago) time, which is where the servers are.
	 * 
	 * @return String	Current Time -1 hour
	 */
	public String getCurrentCentralTimeStamp() {
		String returnString = "";
		Calendar calendar = Calendar.getInstance();
		calendar.add(Calendar.HOUR_OF_DAY, -1);
		Date currentTimestamp = new java.sql.Timestamp(calendar.getTime().getTime());
		returnString = new SimpleDateFormat("hh:mma").format(currentTimestamp);
		return returnString;
	}
	
	/**
	 * This takes a date/time string in a give format,
	 * adds/subtracts the number of desired minutes and
	 * returns a string format "01:30AM" of the new time.
	 * 
	 * @param dateTimeString	Date Time String
	 * @param formatterString	Format to return as
	 * @param minutesToAdd		Minutes to add
	 * @return String			New Date Time string
	 * @throws Exception 		throws Exception
	 */
	public static String addMinutesToTime(String dateTimeString, String formatterString, int minutesToAdd) throws Exception {
		String returnValue = "";	
		SimpleDateFormat formatter = new SimpleDateFormat(formatterString);
		Date date = formatter.parse(dateTimeString);
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(date);
		calendar.add(Calendar.MINUTE, minutesToAdd);
		Date currentTimestamp = new java.sql.Timestamp(calendar.getTime().getTime());
		returnValue = new SimpleDateFormat("hh:mma").format(currentTimestamp);		
		return returnValue;
	}

	/**
	 * This takes a search string and list of strings and
	 * returns true/false if it finds the contents of the search string
	 * within one of the lines of the list of strings.
	 * 
	 * @param searchString		Search String
	 * @param stringList		List of Strings to search
	 * @return boolean		True/false if found
	 */
	public boolean doesStringExistInListOfStrings(String searchString, String[] stringList) {
		boolean returnValue = false;
		
		String[] searchTokens = searchString.split("[ ]");
		
		// Look through each line of stringList
		for (int i=0; i < stringList.length ; i++) {
			// Check for each word in searchString within given line
			for (int x=0; x < searchTokens.length; x++) {
				if (stringList[i].contains(searchTokens[x])) {
					returnValue = true;
				}
				if (!returnValue) { // A Token not found in line, so move on to next line
					break;
				}
			}
			if (returnValue) { // Stop. Found ALL tokens in given line.
				break;
			}
		}		
		return returnValue;
	}	
		
	/**
	 * Extract ALL files from a Zip file
	 * @param zipFilePath		full path of the zip file
	 * @param outputFolder		full path to extract to
	 * @return List				List of extracted files
	 * @throws Exception		throws exception
	 */
	public List<String> extractFromZipFile(String zipFilePath, String outputFolder) throws Exception {
		List<String> fileList = new ArrayList<String>();
		byte[] buffer = new byte[1024];
		File output = new File(outputFolder);
		if (!output.exists()) {
			output.mkdir();
		}
		
		ZipInputStream zis = new ZipInputStream(new FileInputStream(zipFilePath));
		ZipEntry ze = zis.getNextEntry();
		
		while(ze != null) {
			String fileName = ze.getName();
			File newFile = new File(output + File.separator + fileName);
			if (newFile.exists()) {
				newFile.delete();
			}
			fileList.add(fileName);
			new File(newFile.getParent()).mkdirs();
			FileOutputStream fos = new FileOutputStream(newFile);
			int len;
			while ((len=zis.read(buffer)) > 0) {
				fos.write(buffer, 0, len);
			}
			fos.close();
			ze = zis.getNextEntry();
		}
		zis.closeEntry();
		zis.close();
		return fileList;
	}
	
	/**
	 * Return the value of a specific element in XML file
	 * @param xmlFilePath		The full path of the file
	 * @param element		the element
	 * @return String		the value read
	 * @throws Exception		throws exception
	 */
	public String getXMLElementValue(String xmlFilePath, String element) throws Exception {
		String returnValue = "";
		File inputFile = new File(xmlFilePath);
		DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
		Document doc = dBuilder.parse(inputFile);
		Element root = doc.getDocumentElement();
		try {
			NodeList myList = root.getElementsByTagName(element);
			returnValue = myList.item(0).getTextContent();
		} catch (NullPointerException e) {
			// Nothing - leave as empty string.
		}
		return returnValue;
	}
	
	/**
	 * Returns string format of a random numeric value between a low and upper limit. Can specify number of decimal places to include.
	 * @param random		Random
	 * @param lowerBound		lower limit, inclusive.
	 * @param upperBound		upper limit, inclusive.  should be > lowerBound
	 * @param decimalPlaces		decimal places to include.  should be => 0
	 * @return String		formatted string
	 */
	public String getRandomNumericValue(final Random random, final int lowerBound, final int upperBound, final int decimalPlaces) {
		if (upperBound <= lowerBound || decimalPlaces < 0) {
			throw new IllegalArgumentException("!! Exception: arguments not correct");
		}
		
		final double dbl = ((random == null ? new Random() : random).nextDouble() * (upperBound - lowerBound)) + lowerBound;
		return String.format("%."+ decimalPlaces + "f", dbl);
	}
	
	/**
	 * Converts a nanoTime to a String format for Hours, Days, Minutes, and
	 * Seconds
	 * 
	 * @param nanoTime
	 * @return
	 */
	public String convertNanoTime(long nanoTime) {
		String returnString = "";
		DecimalFormat df = new DecimalFormat("#.##");
		double newValue = nanoTime / 1000000000;
		int minHourDay = 0;
		if (newValue > 60) {
			minHourDay++;
			newValue /= 60;
			if (newValue > 60) {
				minHourDay++;
				newValue /= 60;
				if (newValue > 24) {
					minHourDay++;
					newValue /= 24;
				}
			}
		}
		switch (minHourDay) {
		case 3:
			returnString += (int) (newValue) + "d";
			newValue *= 24;
			newValue %= 24;
		case 2:
			returnString += (int) (newValue) + "h";
			newValue *= 60;
			newValue %= 60;
		case 1:
			returnString += (int) (newValue) + "m";
			newValue *= 60;
			newValue %= 60;
		case 0:
			returnString += df.format(newValue) + "s";
		}
		return returnString.equals("Os") ? "<ls" : returnString;
	}

	/**
	 * Gets the elapsed time between two nanotime values
	 * 
	 * @param start Start time
	 * @param end End time
	 * @return String Formatted string of elapsed time
	 */
	public String getElapsedTime(long start, long end) {
		long difference = (end - start);
		return convertNanoTime(difference);
	}

	/**
	 * Verifies that a file exists or not
	 * 
	 * @param filePathString The full path of the file to look for
	 * @return boolean true/false
	 */
	public boolean verifyFileExists(String filePathString) {
		boolean returnValue = false;
		int timeout = 20;
		File dir = new File(BasePage.myTempDownloadsFolder);

		Reporter.log(dir.getAbsolutePath(), true);
		Reporter.log("-- ", true);
		String[] list = dir.list();
		for (String thisOne : list) {
			Reporter.log(thisOne, true);
		}
		Reporter.log("-- ", true);

		File fileValue = new File(filePathString);

		Long start = System.nanoTime();
		while ((((double) System.nanoTime() - start) / 1000000000.0) < timeout) {
			if (fileValue.exists()) {
				Reporter.log("-- Waited '" + (((double) System.nanoTime() - start) / 1000000000.0) + "' seconds", true);
				returnValue = true;
				break;
			}
		}
		return returnValue;
	}

	/**
	 * Returns the size of a given file.
	 * 
	 * @param filePathString The full path of the file to look for
	 * @return long the file size
	 */
	public long getFileSize(String filePathString) {
		File fileValue = new File(filePathString);
		return fileValue.length();
	}

}
