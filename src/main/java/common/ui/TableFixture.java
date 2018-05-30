package common.ui;

import java.util.ArrayList;
import java.util.List;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;


/**
 * This class is a "Helper class" for interacting with
 * tables.
 * @author mlabbe
 *
 */
public class TableFixture {

	@SuppressWarnings("unused")
	private final WebElement element;
	@SuppressWarnings("unused")
	private String elementName;
	
	public List<WebElement> tableRows;
	public List<WebElement> tableData;
	public List<WebElement> tableHeaders;
	public List<WebElement> visibleTableRows;
	public List<WebElement> tableRowsDiv;
	
	// Constructor - Sets the rows and data for the table
	public TableFixture(WebElement element, String elementName) throws Exception {
		this.element = element;
		this.elementName = elementName;
		
		// Set Table Rows, Data
		tableRows = element.findElements(By.tagName("tr"));
		tableData = element.findElements(By.tagName("td"));
		tableHeaders = element.findElements(By.tagName("th"));
		tableRowsDiv = element.findElements(By.tagName("div"));
		
		//Find non hidden tr elements for instances where rows are hidden and tableRows returns hidden rows
		visibleTableRows = new ArrayList<WebElement>();
		for (WebElement row : tableRows) {
			if(row.isDisplayed()) {
				visibleTableRows.add(row);
			}
		}
	}
	
	
	/**
	 * Return row count
	 * @return int
	 * 		The number of rows
	 */
	public int getRowCount() {
		return tableRows.size();
	}

	/**
	 * Return the count of visible rows
	 * @return int		the number of visible rows
	 */
	public int getVisibleRowCount() {
		return visibleTableRows.size();
	}
	
	/**
	 * Return count of data items
	 * @return int
	 * 		The number of data elements
	 */
	public int getDataCount() {
		return tableData.size();
	}
	
	/**
	 * Returns a list of all rows where
	 * the search item is found.
	 * @param item
	 * 		Item to look for
	 * @param searchColumn
	 * 		The column to look in
	 * @return List
	 * 		List of row numbers for all items found.
	 */
	public ArrayList<Integer> findAllItemRows(String item, int searchColumn) {
		ArrayList<Integer> returnList = new ArrayList<Integer>();
		
		for (int i=0; i<getRowCount(); i++) {
			if (tableRows.get(i).findElement(By.xpath(".//td["+searchColumn+"]")).getText().trim().equalsIgnoreCase(item.trim())) {
				returnList.add(i);
			}
		}
		return returnList;
	}
	
	/**
	 * Return row number if a column text equals
	 * the given search string.
	 * @param item
	 * 		The item to search for
	 * @param searchColumn
	 * 		The column to search in
	 * @return int
	 * 		The row found.  -1 if not found.
	 */
	public int findItemRow(String item, int searchColumn) {
		int foundRow = -1;
		for (int i=0; i<getRowCount(); i++) {
			//Reporter.log(tableRows.get(i).findElement(By.xpath(".//td["+searchColumn+"]")).getText().trim(), true);
			if (tableRows.get(i).findElement(By.xpath(".//td["+searchColumn+"]")).getText().trim().equalsIgnoreCase(item.trim())) {
				foundRow = i;
				break;
			}
		}
		return foundRow;
	}
	
	/**
	 * Return row number if a column text contains
	 * the given search string.
	 * @param item
	 * 		The item to search for
	 * @param searchColumn
	 * 		The column to search in
	 * @return int
	 * 		The row found in
	 */
	public int findRowContains(String item, int searchColumn) {
		int foundRow = -1;
		for (int i=0; i<getRowCount(); i++) {
			if (tableRows.get(i).findElement(By.xpath(".//td["+searchColumn+"]")).getText().trim().toLowerCase().contains(item.toLowerCase())) {
				foundRow = i;
				break;
			}
		}
		return foundRow;
	}
	
	/**
	 * Return row number if it contains the given string
	 * @param item
	 * @return
	 */
	public int findRowContains(String item) {
		int foundRow = -1;
		for (int i=0; i<getRowCount(); i++) {
			if (tableRows.get(i).getText().trim().toLowerCase().contains(item.toLowerCase())) {
				foundRow = i;
				break;
			}
		}
		return foundRow;
	}
	
	/**
	 * Return the column number if the desired header is found.
	 * @param header
	 * 		header to search for
	 * @return int
	 * 		Column index of the found header
	 */
	public int findHeaderColumn(String header) {
		int foundColumn = -1;
		for (int i=0; i<tableHeaders.size(); i++) {
			if (tableHeaders.get(i).getText().trim().toLowerCase().equals(header.toLowerCase())) {
				foundColumn = i;
				break;
			}
		}
		return foundColumn;
	}
	
	//----------------Temporary method to find table header with td tag----------------//
	public int findTDHeaderColumn(String header) {
		int foundColumn = -1;
		for (int i=0; i<tableData.size(); i++) {
			if (tableData.get(i).getText().trim().toLowerCase().equals(header.toLowerCase())) {
				foundColumn = i;
				break;
			}
		}
		return foundColumn;
	}
	//-------------------------------------------------------------------------//
	
	/**
	 * Get Number of columns from first row
	 * @return int
	 * 		columns found
	 */
	public int getColumnCount() {
		List<WebElement> columns = tableRows.get(0).findElements(By.xpath(".//td"));
		return columns.size();
	}
	
	/**
	 * Get a specific cell element
	 * @param row
	 * 		row to choose
	 * @param column
	 * 		column to choose
	 * @return	WebElement
	 * 		element of found cell
	 */
	public WebElement getCell(int row, int column) {
		return tableRows.get(row).findElement(By.xpath(".//td["+column+"]"));
	}
	
	/**
	 * Click a specific cell, based on row and column
	 * @param row
	 * 		The row
	 * @param column
	 * 		The column
	 */
	public void clickCell(int row, int column) {
		WebElement cell = getCell(row, column);
		cell.click();
	}
		
	/**
	 * Returns the text of a desired cell
	 * @param row
	 * 		The row
	 * @param column
	 * 		The column
	 * @return String
	 * 		Text from the cell
	 */
	public String getCellText(int row, int column) {
		return tableRows.get(row).findElement(By.xpath(".//td["+column+"]")).getText().trim();
	}
	
	/**
	 * Returns the text of a desired cell, allowing
	 * ability to pass in a specific locator for the column.
	 * @param row
	 * 		The row
	 * @param by
	 * 		The element identifier
	 * @return String
	 * 		The text
	 */
	public String getCellText(int row, By by) {
		return tableRows.get(row).findElement(by).getText().trim();
	}
	
	/**
	 * Returns the text of a given Table Data item
	 * @param index
	 * 		The index
	 * @return String
	 * 		The text
	 */
	public String getDataIndexText(int index) {
		return tableData.get(index).getText().trim();
	}
	
	/**
	 * Clicks on the desired index from tableData
	 * @param index
	 * 		The index
	 */
	public void clickDataIndex(int index) {
		(tableData.get(index)).click();
	}
}
