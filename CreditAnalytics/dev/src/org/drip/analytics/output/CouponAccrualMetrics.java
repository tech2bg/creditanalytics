
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
 * CouponAccrualMetrics holds the results of the period coupon accrual metrics estimate output.
 *
 * @author Lakshmi Krishnamurthy
 */

public class CouponAccrualMetrics {
	private double _dblFX = 1.;
	private int _iAccrualCompoundingRule = -1;
	private double _dblEndDate = java.lang.Double.NaN;
	private double _dblStartDate = java.lang.Double.NaN;
	private double _dblNotionalFactor = java.lang.Double.NaN;
	private double _dblCurrentResetDate = java.lang.Double.NaN;
	private java.util.List<org.drip.analytics.output.ResetPeriodMetrics> _lsRPM = null;

	/**
	 * CouponAccrualMetrics constructor
	 * 
	 * @param dblStartDate Coupon Period Start Date
	 * @param dblEndDate Coupon Period End Date
	 * @param dblFX The Coupon Period FX Rate
	 * @param dblNotionalFactor Period Annuity Notional Factor
	 * @param iAccrualCompoundingRule The Accrual Compounding Rule
	 * 
	 * @throws java.lang.Exception Thrown if Inputs are Invalid
	 */

	public CouponAccrualMetrics (
		final double dblStartDate,
		final double dblEndDate,
		final double dblFX,
		final double dblNotionalFactor,
		final int iAccrualCompoundingRule)
		throws java.lang.Exception
	{
		if (!org.drip.quant.common.NumberUtil.IsValid (_dblStartDate = dblStartDate) ||
			!org.drip.quant.common.NumberUtil.IsValid (_dblEndDate = dblEndDate) ||
				!org.drip.quant.common.NumberUtil.IsValid (_dblFX = dblFX) ||
					!org.drip.quant.common.NumberUtil.IsValid (_dblNotionalFactor = dblNotionalFactor) ||
						(org.drip.analytics.period.ResetPeriodContainer.ACCRUAL_COMPOUNDING_RULE_ARITHMETIC
							!= (_iAccrualCompoundingRule = iAccrualCompoundingRule) &&
								org.drip.analytics.period.ResetPeriodContainer.ACCRUAL_COMPOUNDING_RULE_GEOMETRIC
			!= _iAccrualCompoundingRule))
			throw new java.lang.Exception ("CouponPeriodMetrics ctr: Invalid Inputs");
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
	 * Retrieve the FX Rate applied
	 * 
	 * @return The FX Rate applied
	 */

	public double fx()
	{
		return _dblFX;
	}

	/**
	 * Retrieve the Period Notional Factor
	 * 
	 * @return The Period Notional Factor
	 */

	public double notionalFactor()
	{
		return _dblNotionalFactor;
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
	 * Retrieve the Nominal Accrual Rate in the Coupon Currency
	 * 
	 * @return The Nominal Accrual Rate in the Coupon Currency
	 * 
	 * @throws java.lang.Exception Thrown if the Nominal Accrual Rate cannot be calculated
	 */

	public double nominalAccrualRateCouponCurrency()
		throws java.lang.Exception
	{
		if (null == _lsRPM || 0 == _lsRPM.size())
			throw new java.lang.Exception
				("CouponPeriodMetrics::nominalAccrualRateCouponCurrency => No Reset Period Metrics available!");

		double dblCumulativeDCF = 0.;
		double dblCumulativeNominalRate = java.lang.Double.NaN;

		if (org.drip.analytics.period.ResetPeriodContainer.ACCRUAL_COMPOUNDING_RULE_ARITHMETIC ==
			_iAccrualCompoundingRule)
			dblCumulativeNominalRate = 0.;
		else if (org.drip.analytics.period.ResetPeriodContainer.ACCRUAL_COMPOUNDING_RULE_GEOMETRIC ==
			_iAccrualCompoundingRule)
			dblCumulativeNominalRate = 1.;

		for (org.drip.analytics.output.ResetPeriodMetrics rpm : _lsRPM) {
			double dblDCF = rpm.dcf();

			dblCumulativeDCF += dblDCF;

			if (org.drip.analytics.period.ResetPeriodContainer.ACCRUAL_COMPOUNDING_RULE_ARITHMETIC ==
				_iAccrualCompoundingRule)
				dblCumulativeNominalRate += rpm.nominalRate() * dblDCF;
			else if (org.drip.analytics.period.ResetPeriodContainer.ACCRUAL_COMPOUNDING_RULE_GEOMETRIC ==
				_iAccrualCompoundingRule)
				dblCumulativeNominalRate *= (1. + rpm.nominalRate() * dblDCF);
		}

		if (org.drip.analytics.period.ResetPeriodContainer.ACCRUAL_COMPOUNDING_RULE_GEOMETRIC ==
			_iAccrualCompoundingRule)
			dblCumulativeNominalRate -= 1.;

		return dblCumulativeNominalRate / dblCumulativeDCF;
	}

	/**
	 * Retrieve the Nominal Accrual Rate in the Pay Currency
	 * 
	 * @return The Nominal Accrual Rate in the Pay Currency
	 * 
	 * @throws java.lang.Exception Thrown if the Nominal Accrual Rate cannot be calculated
	 */

	public double nominalAccrualRatePayCurrency()
		throws java.lang.Exception
	{
		return nominalAccrualRateCouponCurrency() * _dblFX;
	}

	/**
	 * Retrieve the Nominal Accrual Rate in the Pay Currency
	 * 
	 * @return The Nominal Accrual Rate in the Pay Currency
	 * 
	 * @throws java.lang.Exception Thrown if the Nominal Accrual Rate cannot be calculated
	 */

	public double nominalAccrualRate()
		throws java.lang.Exception
	{
		return nominalAccrualRatePayCurrency();
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
			throw new java.lang.Exception ("CouponPeriodMetrics::dcf => No Reset Period Metrics available!");

		double dblDCF = 0.;

		for (org.drip.analytics.output.ResetPeriodMetrics rpm : _lsRPM)
			dblDCF += rpm.dcf();

		return dblDCF;
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

	/**
	 * Retrieve the Constituent Reset Period Metrics
	 * 
	 * @return The Constituent Reset Period Metrics
	 */

	public java.util.List<org.drip.analytics.output.ResetPeriodMetrics> resetPeriodMetrics()
	{
		return _lsRPM;
	}

	/**
	 * Set the Current Reset Date
	 * 
	 * @param dblCurrentResetDate The Current Reset Date
	 * 
	 * @return TRUE => The Current Reset Date set
	 */

	public boolean setCurrentResetDate (
		final double dblCurrentResetDate)
	{
		if (!org.drip.quant.common.NumberUtil.IsValid (dblCurrentResetDate)) return false;

		_dblCurrentResetDate = dblCurrentResetDate;
		return true;
	}

	/**
	 * Retrieve the Current Reset Date
	 * 
	 * @return The Current Reset Date
	 */
	
	public double currentResetDate()
	{
		return _dblCurrentResetDate;
	}
}
