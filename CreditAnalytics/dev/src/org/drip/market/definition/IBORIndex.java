
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
 * IBORIndex contains the definitions of the IBOR indexes of different jurisdictions.
 *
 * @author Lakshmi Krishnamurthy
 */

public class IBORIndex extends org.drip.market.definition.FloaterIndex {
	private int _iSpotLag = 0;
	private java.lang.String _strLongestMaturity = "";
	private java.lang.String _strShortestMaturity = "";

	/**
	 * IBORIndex Constructor
	 * 
	 * @param strName Index Name
	 * @param strCurrency Index Currency
	 * @param strDayCount Index Day Count
	 * @param strCalendar Index Holiday Calendar
	 * @param iSpotLag Index Spot Lag
	 * @param strShortestMaturity Index Shortest Maturity
	 * @param strLongestMaturity Index Longest Maturity
	 * @param iAccrualCompoundingRule Accrual Compounding Rule
	 * 
	 * @throws java.lang.Exception Thrown if Inputs are invalid
	 */

	public IBORIndex (
		final java.lang.String strName,
		final java.lang.String strCurrency,
		final java.lang.String strDayCount,
		final java.lang.String strCalendar,
		final int iSpotLag,
		final java.lang.String strShortestMaturity,
		final java.lang.String strLongestMaturity,
		final int iAccrualCompoundingRule)
		throws java.lang.Exception
	{
		super (strName, strCurrency, strDayCount, strCalendar, iAccrualCompoundingRule);

		if (0 > (_iSpotLag = iSpotLag)) throw new java.lang.Exception ("IBORIndex ctr: Invalid Inputs");

		_strLongestMaturity = strLongestMaturity;
		_strShortestMaturity = strShortestMaturity;
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

	@Override public org.drip.analytics.daycount.DateAdjustParams spotLagDAP()
	{
		return new org.drip.analytics.daycount.DateAdjustParams
			(org.drip.analytics.daycount.Convention.DATE_ROLL_PREVIOUS, _iSpotLag, calendar());
	}
}
