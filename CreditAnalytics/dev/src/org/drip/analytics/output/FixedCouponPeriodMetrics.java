
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
 * FixedCouponPeriodMetrics holds the results of the period fixed coupon metrics estimate output.
 *
 * @author Lakshmi Krishnamurthy
 */

public class FixedCouponPeriodMetrics {
	private double _dblDCF = 0.;
	private double _dblDF = java.lang.Double.NaN;
	private double _dblFX = java.lang.Double.NaN;
	private double _dblDV01 = java.lang.Double.NaN;
	private double _dblEndDate = java.lang.Double.NaN;
	private double _dblNotional = java.lang.Double.NaN;
	private double _dblSurvival = java.lang.Double.NaN;
	private double _dblStartDate = java.lang.Double.NaN;
	private double _dblCouponAmount = java.lang.Double.NaN;
	private org.drip.analytics.output.ConvexityAdjustment _convAdj = null;

	/**
	 * FixedCouponPeriodMetrics constructor
	 * 
	 * @param dblStartDate Fixed Coupon Period Start Date
	 * @param dblEndDate Fixed Coupon Period End Date
	 * @param dblNotional Fixed Coupon Period Pay Notional
	 * @param dblDCF Fixed Coupon Period Coupon DCF
	 * @param dblCouponAmount Fixed Coupon Period Full Coupon Amount
	 * @param dblDV01 Fixed Coupon Period DV01
	 * @param dblSurvival Fixed Coupon Period End Survival Probability
	 * @param dblDF Fixed Coupon Period End Discount Factor
	 * @param dblFX Fixed Coupon Period End FX Rate
	 * @param convAdj Fixed Coupon Period Convexity Adjustment
	 * 
	 * @throws java.lang.Exception Thrown if the inputs are invalid
	 */

	public FixedCouponPeriodMetrics (
		final double dblStartDate,
		final double dblEndDate,
		final double dblNotional,
		final double dblDCF,
		final double dblCouponAmount,
		final double dblDV01,
		final double dblSurvival,
		final double dblDF,
		final double dblFX,
		final org.drip.analytics.output.ConvexityAdjustment convAdj)
		throws java.lang.Exception
	{
		if (!org.drip.quant.common.NumberUtil.IsValid (_dblStartDate = dblStartDate) ||
			!org.drip.quant.common.NumberUtil.IsValid (_dblEndDate = dblEndDate) ||
				!org.drip.quant.common.NumberUtil.IsValid (_dblNotional = dblNotional) ||
					!org.drip.quant.common.NumberUtil.IsValid (_dblDCF = dblDCF) ||
						!org.drip.quant.common.NumberUtil.IsValid (_dblCouponAmount = dblCouponAmount) ||
							!org.drip.quant.common.NumberUtil.IsValid (_dblDV01 = dblDV01) ||
								!org.drip.quant.common.NumberUtil.IsValid (_dblSurvival = dblSurvival) ||
									!org.drip.quant.common.NumberUtil.IsValid (_dblDF = dblDF) ||
										!org.drip.quant.common.NumberUtil.IsValid (_dblFX = dblFX) || null ==
											(_convAdj = convAdj))
			throw new java.lang.Exception ("FixedCouponPeriodMetrics ctr: Invalid Inputs");

		_dblDV01 = dblDV01;
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
	 * Retrieve the Pay Notional
	 * 
	 * @return The Pay Notional
	 */

	public double notional()
	{
		return _dblNotional;
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
	 * Retrieve the Coupon Pay Amount
	 * 
	 * @return The Coupon Pay Amount
	 */

	public double couponAmount()
	{
		return _dblCouponAmount;
	}

	/**
	 * Retrieve the DV01
	 * 
	 * @return The DV01
	 */

	public double dv01()
	{
		return _dblDV01;
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
