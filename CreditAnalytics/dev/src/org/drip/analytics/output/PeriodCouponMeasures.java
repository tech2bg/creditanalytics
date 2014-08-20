
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
 * PeriodCouponMeasures holds the results of the period coupon estimate output.
 *
 * @author Lakshmi Krishnamurthy
 */

public class PeriodCouponMeasures {
	private double _dblDCF = java.lang.Double.NaN;
	private double _dblNominalRate = java.lang.Double.NaN;
	private double _dblConvexityAdjustedRate = java.lang.Double.NaN;

	/**
	 * Make a PeriodCouponMeasures Instance from the Nominal Rate
	 * 
	 * @param dblNominalRate The Nominal Rate
	 * @param dblDCF The Period DCF
	 * 
	 * @return The PeriodCouponMeasures Instance
	 */

	public static final PeriodCouponMeasures Nominal (
		final double dblNominalRate,
		final double dblDCF)
	{
		try {
			return new PeriodCouponMeasures (dblNominalRate, dblNominalRate, dblDCF);
		} catch (java.lang.Exception e) {
			e.printStackTrace();
		}

		return null;
	}

	/**
	 * PeriodCouponMeasures constructor
	 * 
	 * @param dblNominalRate The Nominal Coupon Rate
	 * @param dblConvexityAdjustedRate The Convexity Adjusted Coupon Rate
	 * @param dblDCF The Period DCF
	 * 
	 * @throws java.lang.Exception Thrown if Inputs are Invalid
	 */

	public PeriodCouponMeasures (
		final double dblNominalRate,
		final double dblConvexityAdjustedRate,
		final double dblDCF)
		throws java.lang.Exception
	{
		if (!org.drip.quant.common.NumberUtil.IsValid (_dblNominalRate = dblNominalRate) ||
			!org.drip.quant.common.NumberUtil.IsValid (_dblConvexityAdjustedRate = dblConvexityAdjustedRate)
				|| !org.drip.quant.common.NumberUtil.IsValid (_dblDCF = dblDCF))
			throw new java.lang.Exception ("PeriodCouponMeaures ctr: Invalid Inputs");
	}

	/**
	 * Retrieve the Nominal Coupon Rate
	 * 
	 * @return The Nominal Coupon Rate
	 */

	public double nominal()
	{
		return _dblNominalRate;
	}

	/**
	 * Retrieve the Convexity Adjusted Coupon Rate
	 * 
	 * @return The Convexity Adjusted Coupon Rate
	 */

	public double convexityAdjusted()
	{
		return _dblConvexityAdjustedRate;
	}

	/**
	 * Return the DCF
	 * 
	 * @return The DCF
	 */

	public double dcf()
	{
		return _dblDCF;
	}

	/**
	 * Retrieve the Convexity Adjustment Factor
	 * 
	 * @return The Convexity Adjustment Factor
	 */

	public double convexityAdjustmentFactor()
	{
		return 0. == _dblConvexityAdjustedRate && 0. == _dblNominalRate ? 0. : _dblConvexityAdjustedRate /
			_dblNominalRate;
	}

	/**
	 * Retrieve the Convexity Adjustment
	 * 
	 * @return The Convexity Adjustment
	 */

	public double convexityAdjustment()
	{
		return _dblConvexityAdjustedRate - _dblNominalRate;
	}

	/**
	 * Absorb the supplied PCM
	 * 
	 * @param pcmOther The "Other" PCM
	 * 
	 * @return TRUE => At least one Entry in the PCM successfully absorbed
	 */

	public boolean absorb (
		final PeriodCouponMeasures pcmOther)
	{
		if (null == pcmOther) return false;

		_dblConvexityAdjustedRate = _dblDCF * _dblConvexityAdjustedRate + pcmOther._dblDCF *
			pcmOther._dblConvexityAdjustedRate;
		_dblNominalRate = _dblDCF * _dblNominalRate + pcmOther._dblDCF * pcmOther._dblNominalRate;
		_dblDCF += pcmOther._dblDCF;

		if (0. != _dblDCF) {
			_dblNominalRate /= _dblDCF;
			_dblConvexityAdjustedRate /= _dblDCF;
		}

		return true;
	}
}
