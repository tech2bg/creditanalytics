
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
	private double _dblDF = java.lang.Double.NaN;
	private double _dblFX = java.lang.Double.NaN;
	private double _dblDCF = java.lang.Double.NaN;
	private double _dblRate = java.lang.Double.NaN;
	private double _dblNotional = java.lang.Double.NaN;
	private double _dblSurvival = java.lang.Double.NaN;
	private org.drip.analytics.output.ConvexityAdjustment _convAdj = null;

	/**
	 * ComposablePeriodMetrics constructor
	 * 
	 * @param dblDCF Fixed Coupon Period Coupon DCF
	 * @param dblRate Fixed Coupon Period Coupon Rate
	 * @param dblNotional Fixed Coupon Period Notional
	 * @param dblSurvival Fixed Coupon Period End Survival Probability
	 * @param dblDF Fixed Coupon Period End Discount Factor
	 * @param dblFX Fixed Coupon Period End FX Rate
	 * @param convAdj Fixed Coupon Period Convexity Adjustment
	 * 
	 * @throws java.lang.Exception Thrown if the inputs are invalid
	 */

	public ComposablePeriodMetrics (
		final double dblDCF,
		final double dblRate,
		final double dblNotional,
		final double dblSurvival,
		final double dblDF,
		final double dblFX,
		final org.drip.analytics.output.ConvexityAdjustment convAdj)
		throws java.lang.Exception
	{
		if (!org.drip.quant.common.NumberUtil.IsValid (_dblDCF = dblDCF) ||
			!org.drip.quant.common.NumberUtil.IsValid (_dblRate = dblRate) ||
				!org.drip.quant.common.NumberUtil.IsValid (_dblNotional = dblNotional) ||
					!org.drip.quant.common.NumberUtil.IsValid (_dblSurvival = dblSurvival) ||
						!org.drip.quant.common.NumberUtil.IsValid (_dblDF = dblDF) ||
							!org.drip.quant.common.NumberUtil.IsValid (_dblFX = dblFX) || null ==
								(_convAdj = convAdj))
			throw new java.lang.Exception ("ComposablePeriodMetrics ctr: Invalid Inputs");
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
	 * Retrieve the Coupon Notional
	 * 
	 * @return The Coupon Notional
	 */

	public double notional()
	{
		return _dblNotional;
	}

	/**
	 * Retrieve the Survival Probability
	 * 
	 * @return The Survival Probability
	 */

	public double survival()
	{
		return _dblSurvival;
	}

	/**
	 * Retrieve the Discount Factor
	 * 
	 * @return The Discount Factor
	 */

	public double df()
	{
		return _dblDF;
	}

	/**
	 * Retrieve the FX Rate
	 * 
	 * @return The FX Rate
	 */

	public double fx()
	{
		return _dblFX;
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
