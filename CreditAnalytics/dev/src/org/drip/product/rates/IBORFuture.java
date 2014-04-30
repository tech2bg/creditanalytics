
package org.drip.product.rates;

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
 * IBORFuture implements the future on IBOR. This is analogous to Euro-dollar future, across a generic IBOR
 * 	range of applicability.
 * 
 * @author Lakshmi Krishnamurthy
 */

public class IBORFuture {
	private java.lang.String _strIR = "";
	private java.lang.String _strCalendar = "";
	private java.lang.String _strDayCount = "";
	private org.drip.product.params.FloatingRateIndex _fri = null;
	private org.drip.analytics.date.JulianDate _dtEffective = null;

	/**
	 * Construct an IBORFuture Instance
	 * 
	 * @param dtEffective Effective Date
	 * @param fri Floating Rate Index
	 * @param strIR IR Curve
	 * @param strDayCount Day Count
	 * @param strCalendar Calendar
	 * 
	 * @throws java.lang.Exception Thrown if the inputs are invalid
	 */

	public IBORFuture (
		final org.drip.analytics.date.JulianDate dtEffective,
		final org.drip.product.params.FloatingRateIndex fri,
		final java.lang.String strIR,
		final java.lang.String strDayCount,
		final java.lang.String strCalendar)
		throws java.lang.Exception
	{
		if (null == (_dtEffective = dtEffective) || null == (_strIR = strIR) || _strIR.isEmpty())
			throw new java.lang.Exception ("IBORFuture ctr:: Invalid Params!");

		_fri = fri;
		_strCalendar = strCalendar;
		_strDayCount = strDayCount;
	}
}
