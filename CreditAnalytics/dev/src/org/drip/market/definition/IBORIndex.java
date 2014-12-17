
package org.drip.market.definition;

/*
 * -*- mode: java; tab-width: 4; indent-tabs-mode: nil; c-basic-offset: 4 -*-
 */

/*!
 * Copyright (C) 2014 Lakshmi Krishnamurthy
 * 
 *  This file is part of DRIP, a free-software/open-source library for fixed income analysts and developers -
 * 		http://www.credit-trader.org/Begin.html
 * 
 *  DRIP is a free, full featured, fixed income rates, credit, and FX analytics library with a focus towards
 *  	pricing/valuation, risk, and market making.
 * 
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *   	you may not use this file except in compliance with the License.
 *   
 *  You may obtain a copy of the License at
 *  	http://www.apache.org/licenses/LICENSE-2.0
 *  
 *  Unless required by applicable law or agreed to in writing, software
 *  	distributed under the License is distributed on an "AS IS" BASIS,
 *  	WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  
 *  See the License for the specific language governing permissions and
 *  	limitations under the License.
 */

/**
 * IBORIndex contains the definitions of the floater indexes of different jurisdictions.
 *
 * @author Lakshmi Krishnamurthy
 */

public class IBORIndex {
	private int _iSpotLag = 0;
	private java.lang.String _strName = "";
	private java.lang.String _strCurrency = "";
	private java.lang.String _strDayCount = "";
	private java.lang.String _strLongestMaturity = "";
	private java.lang.String _strShortestMaturity = "";

	/**
	 * IBORIndex Constructor
	 * 
	 * @param strName Index Name
	 * @param strCurrency Index Currency
	 * @param strDayCount Index Day Count
	 * @param iSpotLag Index Spot Lag
	 * @param strShortestMaturity Index Shortest Maturity
	 * @param strLongestMaturity Index Longest Maturity
	 * 
	 * @throws java.lang.Exception Thrown if Inputs are invalid
	 */

	public IBORIndex (
		final java.lang.String strName,
		final java.lang.String strCurrency,
		final java.lang.String strDayCount,
		final int iSpotLag,
		final java.lang.String strShortestMaturity,
		final java.lang.String strLongestMaturity)
		throws java.lang.Exception
	{
		if (0 > (_iSpotLag = iSpotLag) || null == (_strName = strName) || _strName.isEmpty() || null ==
			(_strCurrency = strCurrency) || _strCurrency.isEmpty() || null == (_strDayCount = strDayCount) ||
				_strDayCount.isEmpty())
			throw new java.lang.Exception ("IBORIndex ctr: Invalid Inputs");

		_strLongestMaturity = strLongestMaturity;
		_strShortestMaturity = strShortestMaturity;
	}

	/**
	 * Retrieve the Index Name
	 * 
	 * @return The Index Name
	 */

	public java.lang.String name()
	{
		return _strName;
	}

	/**
	 * Retrieve the Index Currency
	 * 
	 * @return The Index Currency
	 */

	public java.lang.String currency()
	{
		return _strCurrency;
	}

	/**
	 * Retrieve the Index Day Count
	 * 
	 * @return The Index Day Count
	 */

	public java.lang.String dayCount()
	{
		return _strDayCount;
	}

	/**
	 * Retrieve the Index Spot Lag
	 * 
	 * @return The Index Spot Lag
	 */

	public int spotLag()
	{
		return _iSpotLag;
	}

	/**
	 * Retrieve the Index Shortest Maturity
	 * 
	 * @return The Index Shortest Maturity
	 */

	public java.lang.String shortestMaturity()
	{
		return _strShortestMaturity;
	}

	/**
	 * Retrieve the Longest Maturity
	 * 
	 * @return The Index Longest Maturity
	 */

	public java.lang.String longestMaturity()
	{
		return _strLongestMaturity;
	}

	/**
	 * Generate the Forward Label corresponding to the specified Tenor
	 * 
	 * @param strTenor The Specified Tenor
	 * 
	 * @return The Forward Label corresponding to the specified Tenor
	 */

	public org.drip.state.identifier.ForwardLabel ForwardStateLabel (
		final java.lang.String strTenor)
	{
		return org.drip.state.identifier.ForwardLabel.Create (_strCurrency, _strName, strTenor);
	}
}
