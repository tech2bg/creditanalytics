
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
	private double _dblFX = 1.;
	private double _dblAccrued = java.lang.Double.NaN;
	private double _dblEndDate = java.lang.Double.NaN;
	private double _dblNotional = java.lang.Double.NaN;
	private double _dblAccrued01 = java.lang.Double.NaN;
	private double _dblStartDate = java.lang.Double.NaN;
	private double _dblAccrualDCF = java.lang.Double.NaN;
	private double _dblCompoundedAccrualRate = java.lang.Double.NaN;

	public FixedCouponAccrualMetrics (
		final double dblStartDate,
		final double dblEndDate,
		final double dblNotional,
		final double dblFX,
		final double dblAccrualDCF,
		final double dblAccrued,
		final double dblAccrued01,
		final double dblCompoundedAccrualRate)
		throws java.lang.Exception
	{
		if (!org.drip.quant.common.NumberUtil.IsValid (_dblStartDate = dblStartDate) ||
			!org.drip.quant.common.NumberUtil.IsValid (_dblEndDate = dblEndDate) ||
				!org.drip.quant.common.NumberUtil.IsValid (_dblNotional = dblNotional) ||
					!org.drip.quant.common.NumberUtil.IsValid (_dblFX = dblFX) ||
						!org.drip.quant.common.NumberUtil.IsValid (_dblAccrualDCF = dblAccrualDCF) ||
							!org.drip.quant.common.NumberUtil.IsValid (_dblAccrued = dblAccrued) ||
								!org.drip.quant.common.NumberUtil.IsValid (_dblAccrued01 = dblAccrued01) ||
									!org.drip.quant.common.NumberUtil.IsValid (_dblCompoundedAccrualRate =
										dblCompoundedAccrualRate))
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
	 * Retrieve the Period Notional
	 * 
	 * @return The Period Notional
	 */

	public double notional()
	{
		return _dblNotional;
	}

	/**
	 * Retrieve the FX Rate applied
	 * 
	 * @return The FX Rate applied
	 */

	public double fx()
	{
		return _dblFX;
	}

	/**
	 * Retrieve the Accrual DCF
	 * 
	 * @return The Accrual DCF
	 */

	public double accrualDCF()
	{
		return _dblAccrualDCF;
	}

	/**
	 * Retrieve the Accrued Amount
	 * 
	 * @return The Accrued Amount
	 */

	public double accrued()
	{
		return _dblAccrued;
	}

	/**
	 * Retrieve the Accrued01
	 * 
	 * @return The Accrued01
	 */

	public double accrued01()
	{
		return _dblAccrued01;
	}

	/**
	 * Retrieve the Compounded Accrual Rate
	 * 
	 * @return The Compounded Accrual Rate
	 */

	public double compoundedAccrualRate()
	{
		return _dblCompoundedAccrualRate;
	}
}
