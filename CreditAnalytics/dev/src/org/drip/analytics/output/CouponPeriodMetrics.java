
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
	private double _dblDF = java.lang.Double.NaN;
	private double _dblEndDate = java.lang.Double.NaN;
	private double _dblSurvival = java.lang.Double.NaN;
	private double _dblStartDate = java.lang.Double.NaN;
	private double _dblNotionalFactor = java.lang.Double.NaN;
	private java.util.List<org.drip.analytics.output.ResetPeriodMetrics> _lsRPM = null;

	/**
	 * CouponPeriodMetrics constructor
	 * 
	 * @param dblStartDate Coupon Period Start Date
	 * @param dblEndDate Coupon Period End Date
	 * @param dblDF Coupon Period Discount Factor
	 * @param dblSurvival Coupon Period Survival
	 * @param dblFX The Coupon Period FX Rate
	 * @param dblNotionalFactor Period Annuity Notional Factor
	 * @param iAccrualCompoundingRule The Accrual Compounding Rule
	 * 
	 * @throws java.lang.Exception Thrown if Inputs are Invalid
	 */

	public CouponPeriodMetrics (
		final double dblStartDate,
		final double dblEndDate,
		final double dblDF,
		final double dblSurvival,
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

		_dblDF = dblDF;
		_dblSurvival = dblSurvival;
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
	 * Retrieve the Nominal Accrual in the Coupon Currency
	 * 
	 * @return The Nominal Accrual in the Coupon Currency
	 * 
	 * @throws java.lang.Exception Thrown if the Nominal Accrual cannot be calculated
	 */

	public double nominalAccrualCouponCurrency()
		throws java.lang.Exception
	{
		if (null == _lsRPM || 0 == _lsRPM.size())
			throw new java.lang.Exception
				("CouponPeriodMetrics::nominalAccrualCouponCurrency => No Reset Period Metrics available!");

		double dblNominalAccrual = java.lang.Double.NaN;

		if (org.drip.analytics.period.ResetPeriodContainer.ACCRUAL_COMPOUNDING_RULE_ARITHMETIC ==
			_iAccrualCompoundingRule)
			dblNominalAccrual = 0.;
		else if (org.drip.analytics.period.ResetPeriodContainer.ACCRUAL_COMPOUNDING_RULE_GEOMETRIC ==
			_iAccrualCompoundingRule)
			dblNominalAccrual = 1.;

		for (org.drip.analytics.output.ResetPeriodMetrics rpm : _lsRPM) {
			if (org.drip.analytics.period.ResetPeriodContainer.ACCRUAL_COMPOUNDING_RULE_ARITHMETIC ==
				_iAccrualCompoundingRule)
				dblNominalAccrual += rpm.nominalRate() * rpm.dcf();
			else if (org.drip.analytics.period.ResetPeriodContainer.ACCRUAL_COMPOUNDING_RULE_GEOMETRIC ==
				_iAccrualCompoundingRule)
				dblNominalAccrual *= (1. + rpm.nominalRate() * rpm.dcf());
		}

		if (org.drip.analytics.period.ResetPeriodContainer.ACCRUAL_COMPOUNDING_RULE_GEOMETRIC ==
			_iAccrualCompoundingRule)
			dblNominalAccrual -= 1.;

		return dblNominalAccrual;
	}

	/**
	 * Retrieve the Nominal Accrual in the Pay Currency
	 * 
	 * @return The Nominal Accrual in the Pay Currency
	 * 
	 * @throws java.lang.Exception Thrown if the Nominal Accrual cannot be calculated
	 */

	public double nominalAccrualPayCurrency()
		throws java.lang.Exception
	{
		return nominalAccrualCouponCurrency() * _dblFX;
	}

	/**
	 * Retrieve the Nominal Accrual in the Pay Currency
	 * 
	 * @return The Nominal Accrual in the Pay Currency
	 * 
	 * @throws java.lang.Exception Thrown if the Nominal Accrual cannot be calculated
	 */

	public double nominalAccrual()
		throws java.lang.Exception
	{
		return nominalAccrualPayCurrency();
	}

	/**
	 * Retrieve the Convexity Adjusted Accrual Rate in the Coupon Currency
	 * 
	 * @return The Convexity Adjusted Accrual Rate in the Coupon Currency
	 * 
	 * @throws java.lang.Exception Thrown if the Convexity Adjusted Accrual Rate cannot be calculated
	 */

	public double convexityAdjustedAccrualRateCouponCurrency()
		throws java.lang.Exception
	{
		if (null == _lsRPM || 0 == _lsRPM.size())
			throw new java.lang.Exception
				("CouponPeriodMetrics::convexityAdjustedAccrualRateCouponCurrency => No Reset Period Metrics available!");

		double dblCumulativeDCF = 0.;
		double dblCumulativeConvexityAdjustedRate = java.lang.Double.NaN;

		if (org.drip.analytics.period.ResetPeriodContainer.ACCRUAL_COMPOUNDING_RULE_ARITHMETIC ==
			_iAccrualCompoundingRule)
			dblCumulativeConvexityAdjustedRate = 0.;
		else if (org.drip.analytics.period.ResetPeriodContainer.ACCRUAL_COMPOUNDING_RULE_GEOMETRIC ==
			_iAccrualCompoundingRule)
			dblCumulativeConvexityAdjustedRate = 1.;

		for (org.drip.analytics.output.ResetPeriodMetrics rpm : _lsRPM) {
			double dblDCF = rpm.dcf();

			dblCumulativeDCF += dblDCF;

			if (org.drip.analytics.period.ResetPeriodContainer.ACCRUAL_COMPOUNDING_RULE_ARITHMETIC ==
				_iAccrualCompoundingRule)
				dblCumulativeConvexityAdjustedRate += rpm.nominalRate() * dblDCF;
			else if (org.drip.analytics.period.ResetPeriodContainer.ACCRUAL_COMPOUNDING_RULE_GEOMETRIC ==
				_iAccrualCompoundingRule)
				dblCumulativeConvexityAdjustedRate *= (1. + rpm.nominalRate() * dblDCF);
		}

		if (org.drip.analytics.period.ResetPeriodContainer.ACCRUAL_COMPOUNDING_RULE_GEOMETRIC ==
			_iAccrualCompoundingRule)
			dblCumulativeConvexityAdjustedRate -= 1.;

		return dblCumulativeConvexityAdjustedRate / dblCumulativeDCF;
	}

	/**
	 * Retrieve the Convexity Adjusted Accrual Rate in the Pay Currency
	 * 
	 * @return The Convexity Adjusted Accrual Rate in the Pay Currency
	 * 
	 * @throws java.lang.Exception Thrown if the Convexity Adjusted Accrual Rate cannot be calculated
	 */

	public double convexityAdjustedAccrualRatePayCurrency()
		throws java.lang.Exception
	{
		return convexityAdjustedAccrualRateCouponCurrency() * _dblFX;
	}

	/**
	 * Retrieve the Convexity Adjusted Accrual Rate in the Pay Currency
	 * 
	 * @return The Convexity Adjusted Accrual Rate in the Pay Currency
	 * 
	 * @throws java.lang.Exception Thrown if the Convexity Adjusted Accrual Rate cannot be calculated
	 */

	public double convexityAdjustedAccrualRate()
		throws java.lang.Exception
	{
		return convexityAdjustedAccrualRatePayCurrency();
	}

	/**
	 * Retrieve the Convexity Adjusted Accrual in the Coupon Currency
	 * 
	 * @return The Convexity Adjusted Accrual in the Coupon Currency
	 * 
	 * @throws java.lang.Exception Thrown if the Convexity Adjusted Accrual cannot be calculated
	 */

	public double convexityAdjustedAccrualCouponCurrency()
		throws java.lang.Exception
	{
		if (null == _lsRPM || 0 == _lsRPM.size())
			throw new java.lang.Exception
				("CouponPeriodMetrics::convexityAdjustedAccrualCouponCurrency => No Reset Period Metrics available!");

		double dblConvexityAdjustedAccrual = java.lang.Double.NaN;

		if (org.drip.analytics.period.ResetPeriodContainer.ACCRUAL_COMPOUNDING_RULE_ARITHMETIC ==
			_iAccrualCompoundingRule)
			dblConvexityAdjustedAccrual = 0.;
		else if (org.drip.analytics.period.ResetPeriodContainer.ACCRUAL_COMPOUNDING_RULE_GEOMETRIC ==
			_iAccrualCompoundingRule)
			dblConvexityAdjustedAccrual = 1.;

		for (org.drip.analytics.output.ResetPeriodMetrics rpm : _lsRPM) {
			if (org.drip.analytics.period.ResetPeriodContainer.ACCRUAL_COMPOUNDING_RULE_ARITHMETIC ==
				_iAccrualCompoundingRule)
				dblConvexityAdjustedAccrual += rpm.nominalRate() * rpm.dcf();
			else if (org.drip.analytics.period.ResetPeriodContainer.ACCRUAL_COMPOUNDING_RULE_GEOMETRIC ==
				_iAccrualCompoundingRule)
				dblConvexityAdjustedAccrual *= (1. + rpm.nominalRate() * rpm.dcf());
		}

		if (org.drip.analytics.period.ResetPeriodContainer.ACCRUAL_COMPOUNDING_RULE_GEOMETRIC ==
			_iAccrualCompoundingRule)
			dblConvexityAdjustedAccrual -= 1.;

		return dblConvexityAdjustedAccrual;
	}

	/**
	 * Retrieve the Convexity Adjusted Accrual in the Pay Currency
	 * 
	 * @return The Convexity Adjusted Accrual in the Pay Currency
	 * 
	 * @throws java.lang.Exception Thrown if the Convexity Adjusted Accrual cannot be calculated
	 */

	public double convexityAdjustedAccrualPayCurrency()
		throws java.lang.Exception
	{
		return convexityAdjustedAccrualCouponCurrency() * _dblFX;
	}

	/**
	 * Retrieve the Convexity Adjusted Accrual in the Pay Currency
	 * 
	 * @return The Convexity Adjusted Accrual in the Pay Currency
	 * 
	 * @throws java.lang.Exception Thrown if the Convexity Adjusted Accrual cannot be calculated
	 */

	public double convexityAdjustedAccrual()
		throws java.lang.Exception
	{
		return convexityAdjustedAccrualPayCurrency();
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
	 * Retrieve the Convexity Adjustment in the Coupon Currency
	 * 
	 * @return The Convexity Adjustment in the Coupon Currency
	 */

	public double convexityAdjustmentCouponCurrency()
		throws java.lang.Exception
	{
		if (null == _lsRPM || 0 == _lsRPM.size())
			throw new java.lang.Exception
				("CouponPeriodMetrics::convexityAdjustmentCouponCurrency => No Reset Period Metrics available!");

		double dblCumulativeDCF = 0.;
		double dblCumulativeConvexityAdjustment = java.lang.Double.NaN;

		if (org.drip.analytics.period.ResetPeriodContainer.ACCRUAL_COMPOUNDING_RULE_ARITHMETIC ==
			_iAccrualCompoundingRule)
			dblCumulativeConvexityAdjustment = 0.;
		else if (org.drip.analytics.period.ResetPeriodContainer.ACCRUAL_COMPOUNDING_RULE_GEOMETRIC ==
			_iAccrualCompoundingRule)
			dblCumulativeConvexityAdjustment = 1.;

		for (org.drip.analytics.output.ResetPeriodMetrics rpm : _lsRPM) {
			double dblDCF = rpm.dcf();

			dblCumulativeDCF += dblDCF;

			if (org.drip.analytics.period.ResetPeriodContainer.ACCRUAL_COMPOUNDING_RULE_ARITHMETIC ==
				_iAccrualCompoundingRule)
				dblCumulativeConvexityAdjustment += rpm.nominalRate() * dblDCF;
			else if (org.drip.analytics.period.ResetPeriodContainer.ACCRUAL_COMPOUNDING_RULE_GEOMETRIC ==
				_iAccrualCompoundingRule)
				dblCumulativeConvexityAdjustment *= (1. + rpm.nominalRate() * dblDCF);
		}

		if (org.drip.analytics.period.ResetPeriodContainer.ACCRUAL_COMPOUNDING_RULE_GEOMETRIC ==
			_iAccrualCompoundingRule)
			dblCumulativeConvexityAdjustment -= 1.;

		return dblCumulativeConvexityAdjustment / dblCumulativeDCF;
	}

	/**
	 * Retrieve the Convexity Adjustment in the Pay Currency
	 * 
	 * @return The Convexity Adjustment in the Pay Currency
	 */

	public double convexityAdjustmentPayCurrency()
		throws java.lang.Exception
	{
		return convexityAdjustmentCouponCurrency() * _dblFX;
	}

	/**
	 * Retrieve the Convexity Adjustment in the Pay Currency
	 * 
	 * @return The Convexity Adjustment in the Pay Currency
	 */

	public double convexityAdjustment()
		throws java.lang.Exception
	{
		return convexityAdjustmentPayCurrency();
	}

	/**
	 * Retrieve the Period DF
	 * 
	 * @return The Period DF
	 */

	public double df()
	{
		return _dblDF;
	}

	/**
	 * Set the Period DF
	 * 
	 * @param dblDF The Discount Factor
	 * 
	 * @return TRUE => The Period DF Successfully Set
	 */

	public boolean setDF (
		final double dblDF)
	{
		if (!org.drip.quant.common.NumberUtil.IsValid (dblDF)) return false;

		_dblDF = dblDF;
		return true;
	}

	/**
	 * Retrieve the Period Survival Probability
	 * 
	 * @return The Period Survival Probability
	 */

	public double survival()
	{
		return _dblSurvival;
	}

	/**
	 * Set the Period DF
	 * 
	 * @param dblDF The Discount Factor
	 * 
	 * @return TRUE => The Period DF Successfully Set
	 */

	public boolean setPeriodSurvival (
		final org.drip.analytics.definition.CreditCurve cc)
	{
		if (null == cc) return false;

		try {
			_dblSurvival = cc.survival (_dblEndDate);
		} catch (java.lang.Exception e) {
			e.printStackTrace();
		}

		return false;
	}

	/**
	 * Retrieve the Period Annuity
	 * 
	 * @return The Period Annuity
	 * 
	 * @throws If the Annuity cannot be computed
	 */

	public double annuity()
		throws java.lang.Exception
	{
		if (!org.drip.quant.common.NumberUtil.IsValid (_dblDF))
			throw new java.lang.Exception ("CouponPeriodMetrics::annuity => Valid Metrics not avaliable");

		return _dblDF * _dblNotionalFactor;
	}

	/**
	 * Retrieve the Period Risky Annuity
	 * 
	 * @return The Period Risky Annuity
	 * 
	 * throws If the Risky Annuity cannot be computed
	 */

	public double riskyAnnuity()
		throws java.lang.Exception
	{
		if (!org.drip.quant.common.NumberUtil.IsValid (_dblDF) || !org.drip.quant.common.NumberUtil.IsValid
			(_dblSurvival))
			throw new java.lang.Exception
				("CouponPeriodMetrics::riskyAnnuity => Valid Metrics not avaliable");

		return _dblDF * _dblSurvival * _dblNotionalFactor;
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
}
