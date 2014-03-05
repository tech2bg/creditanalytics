
package org.drip.product.definition;

/*
 * -*- mode: java; tab-width: 4; indent-tabs-mode: nil; c-basic-offset: 4 -*-
 */

/*!
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
 * FixedIncomeOptionComponent extends ComponentMarketParamRef and provides the following methods:
 *  - Get the component's initial notional, notional, and coupon.
 *  - Get the Effective date, Maturity date, First Coupon Date.
 *  - Set the market curves - discount, TSY, forward, Credit, and EDSF curves.
 *  - Retrieve the component's settlement parameters.
 *  - Value the component using standard/custom market parameters.
 *  - Retrieve the component's named measures and named measure values.
 *  - Retrieve the Underlying Fixed Income Product, Day Count, Strike, Calendar, and Manifest Measure.
 *
 * @author Lakshmi Krishnamurthy
 */

public class FixedIncomeOptionComponent {
	private java.lang.String _strCalendar = "";
	private java.lang.String _strDayCount = "";
	private double _dblStrike = java.lang.Double.NaN;
	private java.lang.String _strManifestMeasure = "";
	private org.drip.product.definition.FixedIncomeOptionComponent _comp = null;

	/**
	 * FixedIncomeOptionComponent constructor
	 * 
	 * @param comp The Underlying Component
	 * @param strManifestMeasure Measure of the Underlying Component
	 * @param dblStrike Strike of the Underlying Component's Measure
	 * @param strDayCount Day Count Convention
	 * @param strCalendar Holiday Calendar
	 * 
	 * @throws java.lang.Exception Thrown if the Inputs are Invalid
	 */

	public FixedIncomeOptionComponent (
		final org.drip.product.definition.FixedIncomeOptionComponent comp,
		final java.lang.String strManifestMeasure,
		final double dblStrike,
		final java.lang.String strDayCount,
		final java.lang.String strCalendar)
		throws java.lang.Exception
	{
		if (null == (_comp = comp) || null == (_strManifestMeasure = strManifestMeasure) ||
			_strManifestMeasure.isEmpty() || !org.drip.quant.common.NumberUtil.IsValid (_dblStrike =
				dblStrike) || null == (_strDayCount = strDayCount) || _strDayCount.isEmpty() || null ==
					(_strCalendar = strCalendar) || _strCalendar.isEmpty())
			throw new java.lang.Exception ("FixedIncomeOptionComponent ctr: Invalid Inputs");
	}

	/**
	 * Retrieve the Underlying Component
	 * 
	 * @return The Underlying Component
	 */

	public org.drip.product.definition.FixedIncomeOptionComponent underlying()
	{
		return _comp;
	}

	/**
	 * Retrieve the Manifest Measure on which the Option's Strike is quoted
	 * 
	 * @return The Manifest Measure on which the Option's Strike is quoted
	 */

	public java.lang.String manifestMeasure()
	{
		return _strManifestMeasure;
	}

	/**
	 * Retrieve the Strike
	 * 
	 * @return The Strike
	 */

	public double strike()
	{
		return _dblStrike;
	}

	/**
	 * Retrieve the Day Count
	 * 
	 * @return The Day Count
	 */

	public java.lang.String dayCount()
	{
		return _strDayCount;
	}

	/**
	 * Retrieve the Holiday Calendar
	 * 
	 * @return The Holiday Calendar
	 */

	public java.lang.String calendar()
	{
		return _strCalendar;
	}
}
