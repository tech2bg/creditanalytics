
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
 * FloaterIndex contains the definitions of the floating rate indexes of different jurisdictions.
 *
 * @author Lakshmi Krishnamurthy
 */

public class FloaterIndex {
	private java.lang.String _strName = "";
	private java.lang.String _strFamily = "";
	private int _iAccrualCompoundingRule = -1;
	private java.lang.String _strCalendar = "";
	private java.lang.String _strCurrency = "";
	private java.lang.String _strDayCount = "";

	/**
	 * IBORIndex Constructor
	 * 
	 * @param strName Index Name
	 * @param strFamily Index Family
	 * @param strCurrency Index Currency
	 * @param strDayCount Index Day Count
	 * @param strCalendar Index Holiday Calendar
	 * @param iAccrualCompoundingRule Accrual Compounding Rule
	 * 
	 * @throws java.lang.Exception Thrown if Inputs are invalid
	 */

	public FloaterIndex (
		final java.lang.String strName,
		final java.lang.String strFamily,
		final java.lang.String strCurrency,
		final java.lang.String strDayCount,
		final java.lang.String strCalendar,
		final int iAccrualCompoundingRule)
		throws java.lang.Exception
	{
		if (null == (_strName = strName) || _strName.isEmpty() || null == (_strFamily = strFamily) ||
			_strFamily.isEmpty() || null == (_strCurrency = strCurrency) || _strCurrency.isEmpty() || null ==
				(_strDayCount = strDayCount) || _strDayCount.isEmpty() ||
					!org.drip.analytics.support.CompositePeriodBuilder.ValidateCompoundingRule
						(_iAccrualCompoundingRule = iAccrualCompoundingRule))
			throw new java.lang.Exception ("FloaterIndex ctr: Invalid Inputs");

		_strCalendar = strCalendar;
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
	 * Retrieve the Index Family
	 * 
	 * @return The Index Family
	 */

	public java.lang.String family()
	{
		return _strFamily;
	}

	/**
	 * Retrieve the Index Holiday Calendar
	 * 
	 * @return The Index Holiday Calendar
	 */

	public java.lang.String calendar()
	{
		return _strCalendar;
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
	 * Retrieve the Index Day Count Convention
	 * 
	 * @return The Index Day Count Convention
	 */

	public java.lang.String dayCount()
	{
		return _strDayCount;
	}

	/**
	 * Retrieve the Accrual Compounding Rule
	 * 
	 * @return The Accrual Compounding Rule
	 */

	public int accrualCompoundingRule()
	{
		return _iAccrualCompoundingRule;
	}

	/**
	 * Retrieve the Spot Lag DAP
	 * 
	 * @return The Spot Lag DAP
	 */

	public org.drip.analytics.daycount.DateAdjustParams spotLagDAP()
	{
		return new org.drip.analytics.daycount.DateAdjustParams
			(org.drip.analytics.daycount.Convention.DATE_ROLL_PREVIOUS, 1, _strCalendar);
	}
}
