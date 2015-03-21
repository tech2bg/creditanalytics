
package org.drip.state.dynamics;

/*
 * -*- mode: java; tab-width: 4; indent-tabs-mode: nil; c-basic-offset: 4 -*-
 */

/*!
 * Copyright (C) 2015 Lakshmi Krishnamurthy
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
 * HullWhiteEvolutionMetrics records the Metrics associated with the Evolution of the Instantaneous Short
 * 	Rate from a Starting to the Terminal Date.
 *
 * @author Lakshmi Krishnamurthy
 */

public class HullWhiteEvolutionMetrics {
	private double _dblFinalDate = java.lang.Double.NaN;
	private double _dblInitialDate = java.lang.Double.NaN;
	private double _dblInitialShortRate = java.lang.Double.NaN;
	private double _dblZeroCouponBondPrice = java.lang.Double.NaN;
	private double _dblFinalShortRateVariance = java.lang.Double.NaN;
	private double _dblExpectedFinalShortRate = java.lang.Double.NaN;
	private double _dblRealizedFinalShortRate = java.lang.Double.NaN;

	/**
	 * HullWhiteEvolutionMetrics Constructor
	 * 
	 * @param dblInitialDate The Initial Date
	 * @param dblFinalDate The Final Date
	 * @param dblInitialShortRate The Initial Short Rate
	 * @param dblRealizedFinalShortRate The Realized Final Short Rate
	 * @param dblExpectedFinalShortRate The Expected Final Short Rate
	 * @param dblFinalShortRateVariance The Final Variance of the Short Rate
	 * @param dblZeroCouponBondPrice The Zero Coupon Bond Price
	 *  
	 * @throws java.lang.Exception Thrown if the Inputs are Invalid
	 */

	public HullWhiteEvolutionMetrics (
		final double dblInitialDate,
		final double dblFinalDate,
		final double dblInitialShortRate,
		final double dblRealizedFinalShortRate,
		final double dblExpectedFinalShortRate,
		final double dblFinalShortRateVariance,
		final double dblZeroCouponBondPrice)
		throws java.lang.Exception
	{
		if (!org.drip.quant.common.NumberUtil.IsValid (_dblInitialDate = dblInitialDate) ||
			!org.drip.quant.common.NumberUtil.IsValid (_dblFinalDate = dblFinalDate) || _dblFinalDate <=
				_dblInitialDate || !org.drip.quant.common.NumberUtil.IsValid (_dblInitialShortRate =
					dblInitialShortRate) || !org.drip.quant.common.NumberUtil.IsValid
						(_dblRealizedFinalShortRate = dblRealizedFinalShortRate) ||
							!org.drip.quant.common.NumberUtil.IsValid (_dblExpectedFinalShortRate =
								dblExpectedFinalShortRate) || !org.drip.quant.common.NumberUtil.IsValid
									(_dblFinalShortRateVariance = dblFinalShortRateVariance) ||
										!org.drip.quant.common.NumberUtil.IsValid (_dblZeroCouponBondPrice =
											dblZeroCouponBondPrice))
			throw new java.lang.Exception ("HullWhiteEvolutionMetrics ctr: Invalid Inputs!");
	}

	/**
	 * Retrieve the Initial Date
	 * 
	 * @return The Initial Date
	 */

	public double initialDate()
	{
		return _dblInitialDate;
	}

	/**
	 * Retrieve the Final Date
	 * 
	 * @return The Final Date
	 */

	public double finalDate()
	{
		return _dblFinalDate;
	}

	/**
	 * Retrieve the Initial Short Rate
	 * 
	 * @return The Initiaal Short Rate
	 */

	public double initialShortRate()
	{
		return _dblInitialShortRate;
	}

	/**
	 * Retrieve the Realized Final Short Rate
	 * 
	 * @return The Realized Final Short Rate
	 */

	public double realizedFinalShortRate()
	{
		return _dblRealizedFinalShortRate;
	}

	/**
	 * Retrieve the Expected Final Short Rate
	 * 
	 * @return The Expected FInal Short Rate
	 */

	public double expectedFinalShortRate()
	{
		return _dblExpectedFinalShortRate;
	}

	/**
	 * Retrieve the Final Short Rate Variance
	 * 
	 * @return The Final Short Rate Variance
	 */

	public double finalShortRateVariance()
	{
		return _dblFinalShortRateVariance;
	}

	/**
	 * Compute the Zero Coupon Bond Price
	 * 
	 * @param dblFinalInitialZeroRatio The Final-to-Initial Zero-Coupon Bond Price Ratio
	 * 
	 * @return The Zero Coupon Bond Price
	 * 
	 * @throws java.lang.Exception Thrown if the Zero Coupon Bond Price cannot be computed
	 */

	public double zeroCouponBondPrice (
		final double dblFinalInitialZeroRatio)
		throws java.lang.Exception
	{
		if (!org.drip.quant.common.NumberUtil.IsValid (dblFinalInitialZeroRatio))
			throw new java.lang.Exception
				("HullWhiteEvolutionMetrics::zeroCouponBondPrice => Invalid Inputs");

		return dblFinalInitialZeroRatio * _dblZeroCouponBondPrice;
	}
}
