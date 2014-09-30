
package org.drip.analytics.output;

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
 * ComposablePeriodMetrics holds the results of a single composable period metrics estimate output.
 *
 * @author Lakshmi Krishnamurthy
 */

public class ComposablePeriodMetrics {
	private double _dblDCF = java.lang.Double.NaN;
	private double _dblRate = java.lang.Double.NaN;
	private double _dblEndDate = java.lang.Double.NaN;
	private double _dblStartDate = java.lang.Double.NaN;
	private org.drip.analytics.output.ConvexityAdjustment _convAdj = null;

	/**
	 * ComposablePeriodMetrics constructor
	 * 
	 * @param dblStartDate Metric Period Start Date
	 * @param dblEndDate Metric Period End Date
	 * @param dblDCF Coupon Period Coupon DCF
	 * @param dblRate Coupon Period Coupon Rate
	 * @param convAdj Coupon Period Convexity Adjustment
	 * 
	 * @throws java.lang.Exception Thrown if the inputs are invalid
	 */

	public ComposablePeriodMetrics (
		final double dblStartDate,
		final double dblEndDate,
		final double dblDCF,
		final double dblRate,
		final org.drip.analytics.output.ConvexityAdjustment convAdj)
		throws java.lang.Exception
	{
		if (!org.drip.quant.common.NumberUtil.IsValid (_dblStartDate = dblStartDate) ||
			!org.drip.quant.common.NumberUtil.IsValid (_dblEndDate = dblEndDate) ||
				!org.drip.quant.common.NumberUtil.IsValid (_dblDCF = dblDCF) ||
					!org.drip.quant.common.NumberUtil.IsValid (_dblRate = dblRate) || null == (_convAdj =
						convAdj))
			throw new java.lang.Exception ("ComposablePeriodMetrics ctr: Invalid Inputs");
	}

	/**
	 * Retrieve the Start Date
	 * 
	 * @return The Start Date
	 */

	public double startDate()
	{
		return _dblStartDate;
	}

	/**
	 * Retrieve the End Date
	 * 
	 * @return The End Date
	 */

	public double endDate()
	{
		return _dblEndDate;
	}

	/**
	 * Retrieve the Day Count Fraction
	 * 
	 * @return The DCF
	 */

	public double dcf()
	{
		return _dblDCF;
	}

	/**
	 * Retrieve the Coupon Rate
	 * 
	 * @return The Coupon Rate
	 */

	public double rate()
	{
		return _dblRate;
	}

	/**
	 * Retrieve the Convexity Adjustment
	 * 
	 * @return The Convexity Adjustment
	 */

	public org.drip.analytics.output.ConvexityAdjustment convAdj()
	{
		return _convAdj;
	}
}
