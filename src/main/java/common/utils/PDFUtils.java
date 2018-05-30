package common.utils;

import java.awt.event.KeyEvent;
import java.io.File;
import java.net.URL;

import org.apache.commons.io.FileUtils;
import org.apache.pdfbox.io.RandomAccessFile;
import org.apache.pdfbox.pdfparser.PDFParser;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.openqa.selenium.WebDriver;

import common.ui.BasePage;

/**
 * This class contains methods for handling
 * PDF files.
 * 
 * @author mlabbe
 *
 */
public class PDFUtils extends BasePage {
	
	public PDFUtils(WebDriver driver) {
		super(driver);
	}


	/*
	 * This method saves a local copy of a PDF file from a given URL.
	 * It stores it in the temp downloads folder, using the given filename.
	 * ** This should be used for non IE browsers.
	 */
	private File fetchPDFFile(URL url, String savedFilename) throws Exception {
		String filePathString = myTempDownloadsFolder+"\\"+savedFilename+".pdf";
		File dir = new File(myTempDownloadsFolder);
		File returnValue = new File(filePathString);
		
		if (!dir.exists()) { // Make Dir if needed
			dir.mkdir();
		} else {
			if (returnValue.exists()) { // Delete file if already exists
				returnValue.delete();
			}
		}
		
		FileUtils.copyURLToFile(url, returnValue); // Save the PRDF URL to the desired file.
		
		return returnValue;
	}
	
	/*
	 * This method saves a local copy of a PDF file using Robot to trigger
	 * the save dialog, enter filename, and enter to complete save.
	 * ** This is designed for IE
	 */
	private File fetchIEPDFFile(String savedFileName) throws Exception {
		RobotUtils robotUtils = new RobotUtils(driver);
		String filePathString = myTempDownloadsFolder+"\\"+savedFileName+".pdf";
		File returnValue = new File(filePathString);
		File dir = new File(myTempDownloadsFolder);
		
		if (!dir.exists()) { // Make Dir if needed
			dir.mkdir();
		} else {
			if (returnValue.exists()) { // Delete PDF file if already exists
				returnValue.delete();
			}
		}
				
		// Open Save Dialog
		int[] events = new int[] {KeyEvent.VK_CONTROL, KeyEvent.VK_SHIFT, KeyEvent.VK_S};
		robotUtils.pressComboKeys(events);
		pause(2000);
		// Backspace (Erase Current file name)
		robotUtils.pressComboKeys(new int[] {KeyEvent.VK_BACK_SPACE});
		pause(1000);
		// Type the full FilePath Name
		robotUtils.type(filePathString);
		pause(1000);
		// Hit Enter to save
		robotUtils.pressComboKeys(new int[] {KeyEvent.VK_ENTER});
		pause(2000);
		return returnValue;
	}
	
	/**
	 * This method returns the contents of a given PDF file
	 * as a list of strings.  Each string in the list
	 * represents a line in the PDF file.
	 * 
	 * @param url
	 * 			URL to use
	 * @param savedFilename
	 * 			File name saved
	 * @return String[]		List of strings
	 * @throws Exception	throws Exception
	 */
	public String[] getPDFContents(URL url, String savedFilename) throws Exception {
		String[] returnValue;
		File savedPDF;
		
		// Saving local PDF
		if (url != null) { // Non-IE browser - use URL
			savedPDF = fetchPDFFile(url, savedFilename);
		} else { // IE - use Robot
			savedPDF = fetchIEPDFFile(savedFilename);
		}
		
		RandomAccessFile randFile = new RandomAccessFile(savedPDF, "r");
		PDFParser parser = new PDFParser(randFile);
		parser.parse();
		PDDocument doc = parser.getPDDocument();
		PDFTextStripper pdfStrip = new PDFTextStripper();
		String lineSep = pdfStrip.getLineSeparator();
		String output = pdfStrip.getText(doc);
		parser.getPDDocument().close();
		returnValue = stringToStringArray(output, lineSep);
		
		return returnValue;
	}
	

}
