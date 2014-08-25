
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
 * ResetPeriodMetrics holds the results of the reset period coupon measures estimate output.
 *
 * @author Lakshmi Krishnamurthy
 */

public class ResetPeriodMetrics {
	private double _dblDCF = java.lang.Double.NaN;
	private double _dblNominalRate = java.lang.Double.NaN;
	private double _dblConvexityAdjustedRate = java.lang.Double.NaN;

	/**
	 * ResetPeriodMetrics constructor
	 * 
	 * @param dblNominalRate The Nominal Coupon Rate
	 * @param dblConvexityAdjustedRate The Convexity Adjusted Coupon Rate
	 * @param dblDCF The Period DCF
	 * 
	 * @throws java.lang.Exception Thrown if Inputs are Invalid
	 */

	public ResetPeriodMetrics (
		final double dblNominalRate,
		final double dblConvexityAdjustedRate,
		final double dblDCF)
		throws java.lang.Exception
	{
		if (!org.drip.quant.common.NumberUtil.IsValid (_dblNominalRate = dblNominalRate) ||
			!org.drip.quant.common.NumberUtil.IsValid (_dblConvexityAdjustedRate = dblConvexityAdjustedRate)
				|| !org.drip.quant.common.NumberUtil.IsValid (_dblDCF = dblDCF) || 0. == _dblDCF)
			throw new java.lang.Exception ("ResetPeriodMetrics ctr: Invalid Inputs");
	}

	/**
	 * Retrieve the Nominal Coupon Accrual Rate in the Floater Currency
	 * 
	 * @return The Nominal Coupon Accrual Rate in the Floater Currency
	 */

	public double nominalRate()
	{
		return _dblNominalRate;
	}

	/**
	 * Retrieve the Convexity Adjusted Coupon Accrual Rate in the Floater Currency
	 * 
	 * @return The Convexity Adjusted Coupon Accrual Rate in the Floater Currency
	 */

	public double convexityAdjustedRate()
	{
		return _dblConvexityAdjustedRate;
	}

	/**
	 * Retrieve the Coupon Accrual Rate Convexity Adjustment in the Floater Currency
	 * 
	 * @return The Coupon Accrual Rate Convexity Adjustment in the Floater Currency
	 */

	public double convexityAdjustment()
	{
		return _dblConvexityAdjustedRate - _dblNominalRate;
	}

	/**
	 * Retrieve the Reset Period DCF
	 * 
	 * @return The Reset Period DCF
	 */

	public double dcf()
	{
		return _dblDCF;
	}
}
