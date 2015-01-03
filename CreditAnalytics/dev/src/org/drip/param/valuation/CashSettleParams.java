
package org.drip.param.valuation;

/*
 * -*- mode: java; tab-width: 4; indent-tabs-mode: nil; c-basic-offset: 4 -*-
 */

/*!
 * Copyright (C) 2015 Lakshmi Krishnamurthy
 * Copyright (C) 2014 Lakshmi Krishnamurthy
 * Copyright (C) 2013 Lakshmi Krishnamurthy
 * Copyright (C) 2012 Lakshmi Krishnamurthy
 * Copyright (C) 2011 Lakshmi Krishnamurthy
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
 * CashSettleParams is the place-holder for the cash settlement parameters for a given product. It contains
 *  the cash settle lag, the calendar, and the date adjustment mode.
 *
 * @author Lakshmi Krishnamurthy
 */

public class CashSettleParams {
	private int _iLag = 3;
	private java.lang.String _strCalendar = "";
	private int _iAdjustMode = org.drip.analytics.daycount.Convention.DATE_ROLL_FOLLOWING;

	/**
	 * Construct the CashSettleParams object from the settle lag and the settle calendar objects
	 * 
	 * @param iLag Cash Settle Lag
	 * @param iAdjustMode Settle adjust Mode
	 * @param strCalendar Settlement Calendar
	 */

	public CashSettleParams (
		final int iLag,
		final java.lang.String strCalendar,
		final int iAdjustMode)
	{
		_iLag = iLag;
		_iAdjustMode = iAdjustMode;
		_strCalendar = strCalendar;
	}

	/**
	 * Retrieve the Settle Lag
	 * 
	 * @return The Settle Lag
	 */

	public int lag()
	{
		return _iLag;
	}

	/**
	 * Retrieve the Settle Calendar
	 * 
	 * @return The Settle Calendar
	 */

	public java.lang.String calendar()
	{
		return _strCalendar;
	}

	/**
	 * Retrieve the Adjustment Mode
	 * 
	 * @return The Adjustment Mode
	 */

	public int adjustMode()
	{
		return _iAdjustMode;
	}

	/**
	 * Construct and return the cash settle date from the valuation date
	 * 
	 * @param dblValue Valuation Date
	 * 
	 * @return Cash settle date
	 */

	public double cashSettleDate (
		final double dblValue)
		throws java.lang.Exception
	{
		if (!org.drip.quant.common.NumberUtil.IsValid (dblValue))
			throw new java.lang.Exception ("CashSettleParams::cashSettleDate Invalid input valuation date");

		return org.drip.analytics.daycount.Convention.Adjust (dblValue + _iLag, _strCalendar, _iAdjustMode);
	}
}
