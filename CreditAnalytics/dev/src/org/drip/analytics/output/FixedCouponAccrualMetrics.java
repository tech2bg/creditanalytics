
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
 * FixedCouponAccrualMetrics holds the results of the fixed period coupon accrual metrics estimate output.
 *
 * @author Lakshmi Krishnamurthy
 */

public class FixedCouponAccrualMetrics {
	private double _dblFX = java.lang.Double.NaN;
	private double _dblDCF = java.lang.Double.NaN;
	private double _dblRate = java.lang.Double.NaN;
	private double _dblAmount = java.lang.Double.NaN;
	private double _dblEndDate = java.lang.Double.NaN;
	private double _dblNotional = java.lang.Double.NaN;
	private double _dblStartDate = java.lang.Double.NaN;

	/**
	 * FixedCouponAccrualMetrics constructor
	 * 
	 * @param dblStartDate The Accrual Period start Date
	 * @param dblEndDate The Accrual Period End Date
	 * @param dblDCF The Accrual Period DCF
	 * @param dblRate The Accrual Period Compounded Accrual Rate
	 * @param dblNotional The Accrual Notional
	 * @param dblAmount The Accrual Period Accrued Amount
	 * @param dblFX The Accrual Period FX Rate
	 * 
	 * @throws java.lang.Exception Thrown if the inputs are invalid
	 */

	public FixedCouponAccrualMetrics (
		final double dblStartDate,
		final double dblEndDate,
		final double dblDCF,
		final double dblRate,
		final double dblNotional,
		final double dblAmount,
		final double dblFX)
		throws java.lang.Exception
	{
		if (!org.drip.quant.common.NumberUtil.IsValid (_dblStartDate = dblStartDate) ||
			!org.drip.quant.common.NumberUtil.IsValid (_dblEndDate = dblEndDate) ||
				!org.drip.quant.common.NumberUtil.IsValid (_dblDCF = dblDCF) ||
					!org.drip.quant.common.NumberUtil.IsValid (_dblRate = dblRate) ||
						!org.drip.quant.common.NumberUtil.IsValid (_dblNotional = dblNotional) ||
							!org.drip.quant.common.NumberUtil.IsValid (_dblAmount = dblAmount) ||
								!org.drip.quant.common.NumberUtil.IsValid (_dblFX = dblFX))
			throw new java.lang.Exception ("FixedCouponAccrualMetrics ctr: Invalid Inputs");

	}

	/**
	 * Retrieve the Period Start Date
	 * 
	 * @return The Period Start Date
	 */

	public double startDate()
	{
		return _dblStartDate;
	}

	/**
	 * Retrieve the Period End Date
	 * 
	 * @return The Period End Date
	 */

	public double endDate()
	{
		return _dblEndDate;
	}

	/**
	 * Retrieve the Accrual DCF
	 * 
	 * @return The Accrual DCF
	 */

	public double dcf()
	{
		return _dblDCF;
	}

	/**
	 * Retrieve the Compounded Accrual Rate
	 * 
	 * @return The Compounded Accrual Rate
	 */

	public double rate()
	{
		return _dblRate;
	}

	/**
	 * Retrieve the Accrual Notional
	 * 
	 * @return The Accrual Notional
	 */

	public double notional()
	{
		return _dblNotional;
	}

	/**
	 * Retrieve the Accrued Amount
	 * 
	 * @return The Accrued Amount
	 */

	public double amount()
	{
		return _dblAmount;
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
}
