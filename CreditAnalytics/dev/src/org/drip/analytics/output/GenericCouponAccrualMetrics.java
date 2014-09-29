
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
 * GenericCouponAccrualMetrics holds the results of the period coupon accrual metrics estimate output.
 *
 * @author Lakshmi Krishnamurthy
 */

public class GenericCouponAccrualMetrics {
	private double _dblFX = 1.;
	private int _iAccrualCompoundingRule = -1;
	private double _dblEndDate = java.lang.Double.NaN;
	private double _dblNotional = java.lang.Double.NaN;
	private double _dblStartDate = java.lang.Double.NaN;
	private java.util.List<org.drip.analytics.output.ResetPeriodMetrics> _lsRPM = null;

	/**
	 * GenericCouponAccrualMetrics constructor
	 * 
	 * @param dblStartDate Coupon Period Start Date
	 * @param dblEndDate Coupon Period End Date
	 * @param dblFX The Coupon Period FX Rate
	 * @param dblNotional Period Notional
	 * @param iAccrualCompoundingRule The Accrual Compounding Rule
	 * @param lsRPM List of Reset Period Metrics
	 * 
	 * @throws java.lang.Exception Thrown if Inputs are Invalid
	 */

	public GenericCouponAccrualMetrics (
		final double dblStartDate,
		final double dblEndDate,
		final double dblFX,
		final double dblNotional,
		final int iAccrualCompoundingRule,
		final java.util.List<org.drip.analytics.output.ResetPeriodMetrics> lsRPM)
		throws java.lang.Exception
	{
		if (!org.drip.quant.common.NumberUtil.IsValid (_dblStartDate = dblStartDate) ||
			!org.drip.quant.common.NumberUtil.IsValid (_dblEndDate = dblEndDate) ||
				!org.drip.quant.common.NumberUtil.IsValid (_dblFX = dblFX) ||
					!org.drip.quant.common.NumberUtil.IsValid (_dblNotional = dblNotional) ||
						!org.drip.analytics.support.ResetUtil.ValidateCompoundingRule
							(_iAccrualCompoundingRule = iAccrualCompoundingRule) || null == (_lsRPM = lsRPM)
								|| 0 == _lsRPM.size())
			throw new java.lang.Exception ("GenericCouponAccrualMetrics ctr: Invalid Inputs");
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
	 * Retrieve the Period Notional
	 * 
	 * @return The Period Notional
	 */

	public double notional()
	{
		return _dblNotional;
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
	 * Retrieve the Compounded Accrual Rate in the Coupon Currency
	 * 
	 * @return The Compounded Accrual Rate in the Coupon Currency
	 * 
	 * @throws java.lang.Exception Thrown if the Compounded Accrual Rate cannot be calculated
	 */

	public double compoundedAccrualRate()
		throws java.lang.Exception
	{
		if (null == _lsRPM || 0 == _lsRPM.size())
			throw new java.lang.Exception
				("GenericCouponAccrualMetrics::compoundedAccrualRate => No Reset Period Metrics available!");

		double dblCumulativeDCF = 0.;
		double dblCumulativeNominalRate = java.lang.Double.NaN;

		if (org.drip.analytics.support.ResetUtil.ACCRUAL_COMPOUNDING_RULE_ARITHMETIC ==
			_iAccrualCompoundingRule)
			dblCumulativeNominalRate = 0.;
		else if (org.drip.analytics.support.ResetUtil.ACCRUAL_COMPOUNDING_RULE_GEOMETRIC ==
			_iAccrualCompoundingRule)
			dblCumulativeNominalRate = 1.;

		for (org.drip.analytics.output.ResetPeriodMetrics rpm : _lsRPM) {
			double dblDCF = rpm.dcf();

			dblCumulativeDCF += dblDCF;

			if (org.drip.analytics.support.ResetUtil.ACCRUAL_COMPOUNDING_RULE_ARITHMETIC ==
				_iAccrualCompoundingRule)
				dblCumulativeNominalRate += rpm.nominalRate() * dblDCF;
			else if (org.drip.analytics.support.ResetUtil.ACCRUAL_COMPOUNDING_RULE_GEOMETRIC ==
				_iAccrualCompoundingRule)
				dblCumulativeNominalRate *= (1. + rpm.nominalRate() * dblDCF);
		}

		if (org.drip.analytics.support.ResetUtil.ACCRUAL_COMPOUNDING_RULE_GEOMETRIC ==
			_iAccrualCompoundingRule)
			dblCumulativeNominalRate -= 1.;

		return dblCumulativeNominalRate / dblCumulativeDCF;
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
			throw new java.lang.Exception
				("GenericCouponAccrualMetrics::dcf => No Reset Period Metrics available!");

		double dblDCF = 0.;

		for (org.drip.analytics.output.ResetPeriodMetrics rpm : _lsRPM)
			dblDCF += rpm.dcf();

		return dblDCF;
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
	 * Retrieve the Fixing Date for the Outstanding Valuation Period
	 * 
	 * @return The Fixing Date for the Outstanding Valuation Period
	 */
	
	public double outstandingFixingDate()
	{
		return _lsRPM.get (_lsRPM.size() - 1).fixing();
	}

	/**
	 * Retrieve the Fixing Rate for the Outstanding Valuation Period
	 * 
	 * @return The Fixing Rate for the Outstanding Valuation Period
	 */
	
	public double outstandingFixingRate()
	{
		return _lsRPM.get (_lsRPM.size() - 1).nominalRate();
	}

	/**
	 * Retrieve the Computed Accrual01
	 * 
	 * @return The Computed Accrual01
	 * 
	 * @throws java.lang.Exception Thrown if the Computed Accrual cannot be calculated
	 */
	
	public double accrual01()
		throws java.lang.Exception
	{
		return 0.0001 * dcf() * notional() * _dblFX;
	}

	/**
	 * Retrieve the Computed Accrued
	 * 
	 * @return The Computed Accrued
	 * 
	 * @throws java.lang.Exception Thrown if the Accrual DCF cannot be calculated
	 */
	
	public double accrued()
		throws java.lang.Exception
	{
		if (null == _lsRPM || 0 == _lsRPM.size())
			throw new java.lang.Exception
				("GenericCouponAccrualMetrics::accrued => No Reset Period Metrics available!");

		double dblCumulativeNominalRate = java.lang.Double.NaN;

		if (org.drip.analytics.support.ResetUtil.ACCRUAL_COMPOUNDING_RULE_ARITHMETIC ==
			_iAccrualCompoundingRule)
			dblCumulativeNominalRate = 0.;
		else if (org.drip.analytics.support.ResetUtil.ACCRUAL_COMPOUNDING_RULE_GEOMETRIC ==
			_iAccrualCompoundingRule)
			dblCumulativeNominalRate = 1.;

		for (org.drip.analytics.output.ResetPeriodMetrics rpm : _lsRPM) {
			double dblDCF = rpm.dcf();

			if (org.drip.analytics.support.ResetUtil.ACCRUAL_COMPOUNDING_RULE_ARITHMETIC ==
				_iAccrualCompoundingRule)
				dblCumulativeNominalRate += rpm.nominalRate() * dblDCF;
			else if (org.drip.analytics.support.ResetUtil.ACCRUAL_COMPOUNDING_RULE_GEOMETRIC ==
				_iAccrualCompoundingRule)
				dblCumulativeNominalRate *= (1. + rpm.nominalRate() * dblDCF);
		}

		if (org.drip.analytics.support.ResetUtil.ACCRUAL_COMPOUNDING_RULE_GEOMETRIC ==
			_iAccrualCompoundingRule)
			dblCumulativeNominalRate -= 1.;

		return dblCumulativeNominalRate;
	}
}
