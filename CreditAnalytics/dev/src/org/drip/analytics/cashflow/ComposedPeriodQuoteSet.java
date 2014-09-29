
package org.drip.analytics.cashflow;

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
 * ComposedPeriodQuoteSet implements the composed period quote set functionality.
 *
 * @author Lakshmi Krishnamurthy
 */

public class ComposedPeriodQuoteSet {
	private double _dblBasis = java.lang.Double.NaN;
	private double _dblBaseRate = java.lang.Double.NaN;

	/**
	 * ComposedPeriodQuoteSet constructor
	 * 
	 * @param dblBaseRate Base Rate
	 * @param dblBasis Basis
	 * 
	 * @throws java.lang.Exception Thrown if inputs are invalid
	 */

	public ComposedPeriodQuoteSet (
		final double dblBaseRate,
		final double dblBasis)
		throws java.lang.Exception
	{
		if (!org.drip.quant.common.NumberUtil.IsValid (_dblBaseRate = dblBaseRate) ||
			!org.drip.quant.common.NumberUtil.IsValid (_dblBasis = dblBasis))
			throw new java.lang.Exception ("ComposedPeriodQuoteSet ctr: Invalid Inputs");
	}

	/**
	 * Get the Period Base Coupon Rate
	 * 
	 * @return The Period Base Coupon Rate
	 */

	public double baseRate()
	{
		return _dblBaseRate;
	}

	/**
	 * Get the Period Coupon Basis
	 * 
	 * @return The Period Coupon Basis
	 */

	public double basis()
	{
		return _dblBasis;
	}
}
