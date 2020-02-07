/**
  * StyleGrid.java
  *
  **/

// PACKAGE
package com.custom.yantra.afs;

import	java.util.*;

public class StyleGrid 
{
    @SuppressWarnings("rawtypes")
	public StyleGrid()
    {
		m_vecRows = new Vector();
		m_vecCols = new Vector();
    }

	public	void	addUniqueRow (String strRowTitle)
	{
		addUniqueRow (strRowTitle, strRowTitle);
	}
	
	public	void	addUniqueCol (String strColTitle)
	{
		addUniqueCol (strColTitle, strColTitle);
	}

	@SuppressWarnings("unchecked")
	public	void	addUniqueRow (String strRowTitle, String strRowSortKey)
	{
		StyleGrid.StyleGridElement	oEle = new StyleGrid.StyleGridElement (strRowTitle, strRowSortKey);
			
		if (findRowColTitle (m_vecRows, strRowTitle) < 0)		
			m_vecRows.add (oEle);		
	}
	
	@SuppressWarnings("unchecked")
	public	void	addUniqueCol (String strColTitle, String strColSortKey)
	{
		StyleGrid.StyleGridElement	oEle = new StyleGrid.StyleGridElement (strColTitle, strColSortKey);
		if (findRowColTitle (m_vecCols, strColTitle) < 0)		
			m_vecCols.add (oEle);
	}
	
	public	void	createGrid (String sRowTitle, String sColTitle)
	{
		m_sRowTitle = sRowTitle;
		m_sColTitle = sColTitle;
		m_bIsAvailable = new boolean [m_vecRows.size()+1][m_vecCols.size()+1];
		m_sUnitsAvailable = new String [m_vecRows.size()+1][m_vecCols.size()+1];
		
		resetGrid();		
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public	void sortGrid (Comparator oRowCompare, Comparator oColCompare)
	{
		// this method should sort rows/columns of grid so they're presented
		// in a layout that makes sense
		// for example, x small comes before x large
		Object []	oRowsArray = m_vecRows.toArray ();
		Object []	oColsArray = m_vecCols.toArray ();
		
		// sort the array of rows and columns
		Arrays.sort (oRowsArray, oRowCompare);
		Arrays.sort (oColsArray, oColCompare);

		// reload rows vector with sorted row array
		for (int iRow = 0; iRow < m_vecRows.size(); iRow++)
			m_vecRows.setElementAt (oRowsArray[iRow], iRow);

		// reload cols vector with sorted column array		
		for (int iCol = 0; iCol < m_vecCols.size(); iCol++)
			m_vecCols.setElementAt (oColsArray[iCol], iCol);

		return;
	}

	public	String	getRowTitle() { return m_sRowTitle; }
	public	String	getColTitle() { return m_sColTitle;	}
	
	public	String	getRowTitle (int iRow)
	{
		 return ((StyleGrid.StyleGridElement)m_vecRows.elementAt (iRow)).getTitle();
	}

	public	String	getColTitle (int iCol)
	{ 
		return ((StyleGrid.StyleGridElement)m_vecCols.elementAt (iCol)).getTitle();
	}

	protected void	resetGrid ()
	{
		zeroGrid();
		protectGrid();
	}

	public	void	protectGrid ()
	{
		int iRow, iCol;
		for (iRow = 0; iRow < m_vecRows.size(); iRow++)
		{
			for (iCol = 0; iCol < m_vecCols.size(); iCol++);
				m_bIsAvailable[iRow][iCol] = false;
		}	
	}	
	
	public	void zeroGrid ()
	{
		int iRow, iCol;
		for (iRow = 0; iRow < m_vecRows.size(); iRow++)
		{
			for (iCol = 0; iCol < m_vecCols.size(); iCol++);
				m_sUnitsAvailable[iRow][iCol] = "0";
		}	
	}
	
	public	int	getRowCount ()
	{
		return m_vecRows.size();
	}
	
	public	int getColCount ()
	{
		return m_vecCols.size();
	}
	
	public	void addAvailableQty (String sRowTitle, String sColTitle, String sQty)
	{
		int	iRowIdx = findRowColTitle (m_vecRows, sRowTitle);
		int	iColIdx = findRowColTitle (m_vecCols, sColTitle);
		
		if (iRowIdx >= 0 && iColIdx >= 0)
			m_sUnitsAvailable[iRowIdx][iColIdx] = sQty;		
	}

	public	void addIsAvailable (String sRowTitle, String sColTitle)
	{
		int	iRowIdx = findRowColTitle (m_vecRows, sRowTitle);
		int	iColIdx = findRowColTitle (m_vecCols, sColTitle);
		
		if (iRowIdx >= 0 && iColIdx >= 0)
			m_bIsAvailable[iRowIdx][iColIdx] = true;		
	}

	public	String getRowColIsAvailable (int iRow, int iCol)
	{
		return (m_bIsAvailable[iRow][iCol] ? "Y" : "N");
	}

	public	String getRowColInputType (int iRow, int iCol)
	{
		return (m_bIsAvailable[iRow][iCol] ? "unprotected" : "protected");
	}
	
	public	String getRowColQty (int iRow, int iCol)
	{
		return m_sUnitsAvailable[iRow][iCol];
	}
				
	@SuppressWarnings("rawtypes")
	private	int	findRowColTitle (Vector vecRowOrCol, String sTitle)
	{
		int iRC;
		
		for (iRC = 0; iRC < vecRowOrCol.size(); iRC++)
		{
			StyleGridElement	oEle = (StyleGridElement)vecRowOrCol.elementAt (iRC);
			
			if (sTitle.equalsIgnoreCase (oEle.getTitle()))
				break;
		}
		return (iRC < vecRowOrCol.size() ? iRC : -1);	
	}
	
	public	class	StyleGridElement {
		public	StyleGridElement (String sEleTitle, String sEleSortKey)
		{
			m_sEleTitle = sEleTitle;
			m_sEleSortKey = sEleSortKey;
		}

		public	String	getTitle () { return m_sEleTitle; }
		public	String	getSortKey () { return m_sEleSortKey; }

		// 		
		protected	String	m_sEleTitle;
		protected	String	m_sEleSortKey;
	}
		
	@SuppressWarnings("rawtypes")
	protected	Vector	m_vecRows;
	@SuppressWarnings("rawtypes")
	protected	Vector	m_vecCols;
	protected	String	m_sRowTitle;
	protected	String	m_sColTitle;
	protected	boolean [] []	m_bIsAvailable;
	protected	String	[] []	m_sUnitsAvailable;
}

