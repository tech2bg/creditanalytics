
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
 * CouponPeriodMetrics holds the results of the period coupon metrics estimate output.
 *
 * @author Lakshmi Krishnamurthy
 */

public class CouponPeriodMetrics {
	private double _dblFX = 1.;
	private int _iAccrualCompoundingRule = -1;
	private java.util.List<org.drip.analytics.output.ResetPeriodMetrics> _lsRPM = null;

	/**
	 * CouponPeriodMetrics constructor
	 * 
	 * @param dblFX The Coupon Period FX Rate
	 * @param iAccrualCompoundingRule The Accrual Compounding Rule
	 * 
	 * @throws java.lang.Exception Thrown if Inputs are Invalid
	 */

	public CouponPeriodMetrics (
		final double dblFX,
		final int iAccrualCompoundingRule)
		throws java.lang.Exception
	{
		if (!org.drip.quant.common.NumberUtil.IsValid (_dblFX = dblFX) ||
			(org.drip.analytics.period.ResetPeriodContainer.ACCRUAL_COMPOUNDING_RULE_ARITHMETIC !=
				(_iAccrualCompoundingRule = iAccrualCompoundingRule) &&
					org.drip.analytics.period.ResetPeriodContainer.ACCRUAL_COMPOUNDING_RULE_GEOMETRIC !=
						_iAccrualCompoundingRule))
			throw new java.lang.Exception ("CouponPeriodMetrics ctr: Invalid Inputs");
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
	 * Retrieve the Accrual Compounding Rule
	 * 
	 * @return The Accrual Compounding Rule
	 */

	public int accrualCompoundingRule()
	{
		return _iAccrualCompoundingRule;
	}

	/**
	 * Retrieve the Nominal Accrual Rate
	 * 
	 * @return The Nominal Accrual Rate
	 * 
	 * @throws java.lang.Exception Thrown if the Nominal Accrual Rate cannot be calculated
	 */

	public double nominalAccrualRate()
		throws java.lang.Exception
	{
		if (null == _lsRPM || 0 == _lsRPM.size())
			throw new java.lang.Exception
				("CouponPeriodMetrics::nominalAccrualRate => No Reset Period Metrics avaliable!");

		double dblCumulativeDCF = 0.;
		double dblCumulativeNominalRate = 0.;

		for (org.drip.analytics.output.ResetPeriodMetrics rpm : _lsRPM) {
			double dblDCF = rpm.dcf();

			dblCumulativeDCF += dblDCF;

			dblCumulativeNominalRate += rpm.nominalRate() * dblDCF;
		}

		return dblCumulativeNominalRate * _dblFX / dblCumulativeDCF;
	}

	/**
	 * Retrieve the Nominal Accrual
	 * 
	 * @return The Nominal Accrual
	 * 
	 * @throws java.lang.Exception Thrown if the Nominal Accrual cannot be calculated
	 */

	public double nominalAccrual()
		throws java.lang.Exception
	{
		if (null == _lsRPM || 0 == _lsRPM.size())
			throw new java.lang.Exception
				("CouponPeriodMetrics::nominalAccrual => No Reset Period Metrics avaliable!");

		double dblNominalAccrual = 0.;

		for (org.drip.analytics.output.ResetPeriodMetrics rpm : _lsRPM)
			dblNominalAccrual += rpm.nominalRate() * rpm.dcf();

		return dblNominalAccrual * _dblFX;
	}

	/**
	 * Retrieve the Convexity Adjusted Accrual Rate
	 * 
	 * @return The Convexity Adjusted Accrual Rate
	 * 
	 * @throws java.lang.Exception Thrown if the Convexity Adjusted Accrual Rate cannot be calculated
	 */

	public double convexityAdjustedAccrualRate()
		throws java.lang.Exception
	{
		if (null == _lsRPM || 0 == _lsRPM.size())
			throw new java.lang.Exception
				("CouponPeriodMetrics::convexityAdjustedAccrualRate => No Reset Period Metrics avaliable!");

		double dblCumulativeDCF = 0.;
		double dblCumulativeConvexityAdjustedRate = 0.;

		for (org.drip.analytics.output.ResetPeriodMetrics rpm : _lsRPM) {
			double dblDCF = rpm.dcf();

			dblCumulativeDCF += dblDCF;

			dblCumulativeConvexityAdjustedRate += rpm.convexityAdjustedRate() * dblDCF;
		}

		return dblCumulativeConvexityAdjustedRate * _dblFX / dblCumulativeDCF;
	}

	/**
	 * Retrieve the Convexity Adjusted Accrual
	 * 
	 * @return The Convexity Adjusted Accrual
	 * 
	 * @throws java.lang.Exception Thrown if the Convexity Adjusted Accrual cannot be calculated
	 */

	public double convexityAdjustedAccrual()
		throws java.lang.Exception
	{
		if (null == _lsRPM || 0 == _lsRPM.size())
			throw new java.lang.Exception
				("CouponPeriodMetrics::convexityAdjustedAccrual => No Reset Period Metrics avaliable!");

		double dblConvexityAdjustedAccrual = 0.;

		for (org.drip.analytics.output.ResetPeriodMetrics rpm : _lsRPM)
			dblConvexityAdjustedAccrual += rpm.convexityAdjustedRate() * rpm.dcf();

		return dblConvexityAdjustedAccrual * _dblFX;
	}

	/**
	 * Return the Accrual DCF
	 * 
	 * @return The Accrual DCF
	 * 
	 * @throws java.lang.Exception Thrown if the Accrual DCF cannot be calculated
	 */

	public double dcf()
		throws java.lang.Exception
	{
		if (null == _lsRPM || 0 == _lsRPM.size())
			throw new java.lang.Exception ("CouponPeriodMetrics::dcf => No Reset Period Metrics avaliable!");

		double dblDCF = 0.;

		for (org.drip.analytics.output.ResetPeriodMetrics rpm : _lsRPM)
			dblDCF += rpm.dcf();

		return dblDCF;
	}

	/**
	 * Retrieve the Convexity Adjustment Factor
	 * 
	 * @return The Convexity Adjustment Factor
	 */

	public double convexityAdjustmentFactor()
		throws java.lang.Exception
	{
		return convexityAdjustedAccrual() / nominalAccrual();
	}

	/**
	 * Retrieve the Convexity Adjustment
	 * 
	 * @return The Convexity Adjustment
	 */

	public double convexityAdjustment()
		throws java.lang.Exception
	{
		if (null == _lsRPM || 0 == _lsRPM.size())
			throw new java.lang.Exception
				("CouponPeriodMetrics::convexityAdjustment => No Reset Period Metrics avaliable!");

		double dblCumulativeDCF = 0.;
		double dblCumulativeConvexityAdjustment = 0.;

		for (org.drip.analytics.output.ResetPeriodMetrics rpm : _lsRPM) {
			double dblDCF = rpm.dcf();

			dblCumulativeDCF += dblDCF;

			dblCumulativeConvexityAdjustment += rpm.convexityAdjustment() * dblDCF;
		}

		return dblCumulativeConvexityAdjustment * _dblFX / dblCumulativeDCF;
	}

	/**
	 * Compound the supplied PCM
	 * 
	 * @param pcmOther The "Other" PCM
	 * @param iAccrualCompoundingRule The Accrual Compounding Rule
	 * 
	 * @return TRUE => The "Other" PCM has been successfully compounded to the current one
	 */

	public boolean addResetPeriodMetrics (
		final org.drip.analytics.output.ResetPeriodMetrics rpm)
	{
		if (null == rpm) return false;

		if (null == _lsRPM) _lsRPM = new java.util.ArrayList<org.drip.analytics.output.ResetPeriodMetrics>();

		_lsRPM.add (rpm);

		return true;
	}
}
