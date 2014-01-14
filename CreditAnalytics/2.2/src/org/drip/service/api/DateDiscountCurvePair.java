
package org.drip.service.api;

/*
 * -*- mode: java; tab-width: 4; indent-tabs-mode: nil; c-basic-offset: 4 -*-
 */

/*!
 * Copyright (C) 2014 Lakshmi Krishnamurthy
 * Copyright (C) 2014 Lakshmi Krishnamurthy
 * 
 * This file is part of CreditAnalytics, a free-software/open-source library for fixed income analysts and
 * 		developers - http://www.credit-trader.org
 * 
 * CreditAnalytics is a free, full featured, fixed income credit analytics library, developed with a special
 * 		focus towards the needs of the bonds and credit products community.
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
 * DateDiscountCurvePair contains the COB/Discount Curve Pair, and the corresponding computed outputs.
 * 
 * @author Lakshmi Krishnamurthy
 */

public class DateDiscountCurvePair {
	private org.drip.analytics.date.JulianDate _dt = null;
	private org.drip.analytics.rates.DiscountCurve _dc = null;
	private java.util.List<java.lang.String> _lsstrDump = null;

	/**
	 * DateDiscountCurvePair constructor
	 * 
	 * @param dt The COB
	 * @param dc The COB Discount Curve
	 * @param lsstrDump List of Output String Dump
	 * 
	 * @throws java.lang.Exception Thrown if the Inputs are invalid
	 */

	public DateDiscountCurvePair (
		final org.drip.analytics.date.JulianDate dt,
		final org.drip.analytics.rates.DiscountCurve dc,
		final java.util.List<java.lang.String> lsstrDump)
		throws java.lang.Exception
	{
		if (null == (_dt = dt)) throw new java.lang.Exception ("DateDiscountCurvePair ctr: Invalid Inputs");

		_dc = dc;
		_lsstrDump = lsstrDump;
	}

	/**
	 * Retrieve the COB
	 * 
	 * @return The COB
	 */

	public org.drip.analytics.date.JulianDate date()
	{
		return _dt;
	}

	/**
	 * Retrieve the Discount Curve
	 * 
	 * @return The Discount Curve
	 */

	public org.drip.analytics.rates.DiscountCurve dc()
	{
		return _dc;
	}

	/**
	 * Retrieve the Output Dump
	 * 
	 * @return The Output Dump
	 */

	public java.util.List<java.lang.String> output()
	{
		return _lsstrDump;
	}
}
