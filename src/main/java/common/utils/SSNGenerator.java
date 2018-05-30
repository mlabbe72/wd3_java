package common.utils;

import java.util.concurrent.ThreadLocalRandom;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/******************************************************************************
* A social security number is a nine-digit number broken into three groups
* in the form NNN-NN-NNNN.  The first three digits (the "area") are between
* 001 and 799 (inclusive).  The next two digits (the "group") are between
* 01 and 99 (inclusive).  The last four digits (the "serial") are between
* 0001 and 9999 (inclusive).
*
******************************************************************************/
public class SSNGenerator {
	final int MIN_SSN = 001010001; // Smallest legal ssn
	final int MAX_SSN = 899999999; // Biggest valid ssn
	final int SERIAL_LENGTH = 4;   // Number of digits in serial part
	final int GROUP_LENGTH = 2;    // Number of digits in group part
	final int WHOLE_LENGTH = 11;   // Number of characters in entire ssn String (includes '-')
   
	private boolean valid;   // True for a valid SSN; false for invalid number
	int randomInt;			// random value for SSN
	
	private int area;    // First three digits: xxx-nn-nnnn
	private int group;   // Nest two digits: nnn-xx-nnnn
	private int serial;  // Last four digits: nnn-nn-xxxx

	private StringBuffer whole;// = new StringBuffer(WHOLE_LENGTH);    // All of the SSN, in the form "nn-nnn-nnnn"

	public SSNGenerator() {
	}


	/**
	 * This method creates an SSN value, returning it as a string
	 * in the format 'aa-gg-ssss'
	 * set makeSureValid to true to be sure it returns an SSN that
	 * has passed basic validation as a valid SSN.  Otherwise, it
	 * will just return the random number in SSN format.
	 * 
	 * @param makeSureValid
	 * 		true/false if want to validate
	 * @return String
	 * 		return SSN in formatted string
	 */
	public String createSSN(boolean makeSureValid) {
		valid = false;
		whole = new StringBuffer(WHOLE_LENGTH);
		
		// Loop until we have a number that passes basic SSN validation
		// if makeSureValid is true
		while (!valid) {
			generate();
			// Only validate if desired.
			if (makeSureValid) {
				validate();
			} else {
				valid = true;
			}
		}
		format();
		return whole.toString();
	}
	
	/*
	 * This method generates a random number in SSN range.
	 * It then sets the area, group, and serial values.
	 */
	private void generate() {
		randomInt = ThreadLocalRandom.current().nextInt(MIN_SSN, MAX_SSN + 1);
		area = randomInt / 1000000;            // First three digits: xxx-nn-nnnn
		group = (randomInt / 10000) % 100;     // Next two digits: nnn-xx-nnnn
		serial = randomInt % 10000;            // Last four digits: nnn-nn-xxxx
	}
	
	/*
	 * This method checks that the random number passes
	 * basic validation for a valid SSN.
	 * It sets the 'valid' flag true/false accordingly.
	 */
	private void validate() {
		/* REGEX VALIDATION PATTERN:
		 ^           # Assert position at the beginning of the string.
		(?!000|666)  # Assert that neither "000" nor "666" can be matched here. AREA
		[0-8]        # Match a digit between 0 and 8. AREA
		[0-9]{2}     # Match a digit, exactly two times. AREA
		(?!00)       # Assert that "00" cannot be matched here. GROUP
		[0-9]{2}     # Match a digit, exactly two times. GROUP
		(?!0000)     # Assert that "0000" cannot be matched here. SERIAL
		[0-9]{4}     # Match a digit, exactly four times. SERIAL
		$            # Assert position at the end of the string.
		*/
		
		// Pattern to validate if a Valid SSN
		String regex = "^(?!000|666)[0-8][0-9]{2}(?!00)[0-9]{2}(?!0000)[0-9]{4}$";
		Pattern pattern = Pattern.compile(regex);
		
		String ssnString = String.valueOf(randomInt); // Put as String for the regex validation
		Matcher matcher = pattern.matcher(ssnString); // Match regex
		valid = matcher.matches();
	}	
	
	/*
	 * This method formats the SSN into a String.
	 * 
	 */
	private void format() {
		whole.insert(0, serial); // Insert last 4 digit serial
		// If serial length is less than 4, insert a 0 at beginning of serial
		while (whole.length() < SERIAL_LENGTH) {
			whole.insert(0, '0');
		}
		whole.insert(0, '-'); // Insert '-' before serial
		whole.insert(0, group); // Insert 2 digit group
		whole.insert(0, '-'); // Insert '-' before group
		// If group length is short, insert a 0 at beginning of group
		while (whole.length() < GROUP_LENGTH + 1 + SERIAL_LENGTH) {
			whole.insert(0, '0');
		}
		whole.insert(0, area); // Insert 3 digit area
		// If area length is short, insert a 0 at beginning
		while (whole.length() < WHOLE_LENGTH) {
				whole.insert(0, '0');
		}
	}
	
	/**
	 * Return an encrypted version of SSN
	 * in format ###-##-nnnn
	 * @param SSN
	 * 		SSN to encrypt
	 * @return String
	 * 		Encrypted SSN
	 */
	//TODO: add ability to set the encrypted chars, such as xxx-xx-nnnn or ###-##-nnnn
	public String encryptedSSN(String SSN) {
		StringBuilder encryptedString = new StringBuilder(SSN);
		// Replace first 3 digits with #
		for (int i=0; i<=2; i++) {
			encryptedString.setCharAt(i, '#');
		}
		// Replace second 2 digits with #
		for (int i=4; i<=5; i++) {
			encryptedString.setCharAt(i, '#');
		}
		return encryptedString.toString();
	}
}