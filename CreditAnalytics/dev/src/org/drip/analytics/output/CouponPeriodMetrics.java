
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
	private double _dblNotional = java.lang.Double.NaN;
	private double _dblSurvival = java.lang.Double.NaN;
	private double _dblStartDate = java.lang.Double.NaN;
	private org.drip.analytics.output.ConvexityAdjustment _convAdj = null;
	private java.util.List<org.drip.analytics.output.ResetPeriodMetrics> _lsRPM = null;

	/**
	 * CouponPeriodMetrics constructor
	 * 
	 * @param dblStartDate Coupon Period Start Date
	 * @param dblEndDate Coupon Period End Date
	 * @param dblDF Coupon Period Discount Factor
	 * @param dblSurvival Coupon Period Survival
	 * @param dblFX The Coupon Period FX Rate
	 * @param dblNotional Period Annuity Notional
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
		final double dblNotional,
		final int iAccrualCompoundingRule)
		throws java.lang.Exception
	{
		if (!org.drip.quant.common.NumberUtil.IsValid (_dblStartDate = dblStartDate) ||
			!org.drip.quant.common.NumberUtil.IsValid (_dblEndDate = dblEndDate) ||
				!org.drip.quant.common.NumberUtil.IsValid (_dblFX = dblFX) ||
					!org.drip.quant.common.NumberUtil.IsValid (_dblNotional = dblNotional) ||
						(org.drip.analytics.period.ResetPeriodContainer.ACCRUAL_COMPOUNDING_RULE_ARITHMETIC
			!= (_iAccrualCompoundingRule = iAccrualCompoundingRule) &&
				org.drip.analytics.period.ResetPeriodContainer.ACCRUAL_COMPOUNDING_RULE_GEOMETRIC !=
					_iAccrualCompoundingRule))
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
	 * Retrieve the Nominal Accrual Rate in the Coupon Currency
	 * 
	 * @return The Nominal Accrual Rate in the Coupon Currency
	 * 
	 * @throws java.lang.Exception Thrown if the Nominal Accrual Rate cannot be calculated
	 */

	public double nominalAccrualRate()
		throws java.lang.Exception
	{
		if (null == _lsRPM || 0 == _lsRPM.size())
			throw new java.lang.Exception
				("CouponPeriodMetrics::nominalAccrualRate => No Reset Period Metrics available!");

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
	 * Retrieve the Nominal Accrual in the Coupon Currency
	 * 
	 * @return The Nominal Accrual in the Coupon Currency
	 * 
	 * @throws java.lang.Exception Thrown if the Nominal Accrual cannot be calculated
	 */

	public double nominalAccrual()
		throws java.lang.Exception
	{
		if (null == _lsRPM || 0 == _lsRPM.size())
			throw new java.lang.Exception
				("CouponPeriodMetrics::nominalAccrual => No Reset Period Metrics available!");

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
	 * Retrieve the Period Annuity in the Pay Currency
	 * 
	 * @return The Period Annuity in the Pay Currency
	 * 
	 * @throws If the Annuity in the Pay Currency cannot be computed
	 */

	public double annuity()
		throws java.lang.Exception
	{
		if (!org.drip.quant.common.NumberUtil.IsValid (_dblDF))
			throw new java.lang.Exception ("CouponPeriodMetrics::annuity => Valid Metrics not avaliable");

		return _dblDF * _dblNotional * _dblFX;
	}

	/**
	 * Retrieve the Period Risky Annuity in the Pay Currency
	 * 
	 * @return The Period Risky Annuity in the Pay Currency
	 * 
	 * throws If the Risky Annuity in the Pay Currency cannot be computed
	 */

	public double riskyAnnuity()
		throws java.lang.Exception
	{
		if (!org.drip.quant.common.NumberUtil.IsValid (_dblDF) || !org.drip.quant.common.NumberUtil.IsValid
			(_dblSurvival))
			throw new java.lang.Exception
				("CouponPeriodMetrics::riskyAnnuity => Valid Metrics not avaliable");

		return _dblDF * _dblSurvival * _dblNotional * _dblFX;
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
	 * Set the Convexity Adjustment for the Reset Period
	 * 
	 * @param convAdj The Convexity Adjustment for the Reset Period
	 * 
	 * @return TRUE => The Convexity Adjustment for the Reset Period successfully set
	 */

	public boolean setConvAdj (
		final org.drip.analytics.output.ConvexityAdjustment convAdj)
	{
		if (null == convAdj) return false;

		_convAdj = convAdj;
		return true;
	}

	/**
	 * Retrieve the Convexity Adjustment for the Reset Period
	 * 
	 * @return The Convexity Adjustment for the Reset Period
	 */

	public org.drip.analytics.output.ConvexityAdjustment convexityAdjustment()
	{
		if (org.drip.analytics.period.ResetPeriodContainer.ACCRUAL_COMPOUNDING_RULE_GEOMETRIC ==
			_iAccrualCompoundingRule)
			return _convAdj;

		double dblUnadjustedAccrual = 0.;
		double dblCreditFXAdjAccrual = 0.;
		double dblForwardFXAdjAccrual = 0.;
		double dblFundingFXAdjAccrual = 0.;
		double dblCreditForwardAdjAccrual = 0.;
		double dblCreditFundingAdjAccrual = 0.;
		double dblForwardFundingAdjAccrual = 0.;

		for (org.drip.analytics.output.ResetPeriodMetrics rpm : _lsRPM) {
			org.drip.analytics.output.ConvexityAdjustment convAdjResetPeriod = rpm.convexityAdjustment();

			if (null == convAdjResetPeriod) return null;

			double dblPeriodAccrual = rpm.nominalRate() * rpm.dcf();

			dblUnadjustedAccrual += dblPeriodAccrual;

			dblCreditFXAdjAccrual += dblPeriodAccrual * convAdjResetPeriod.creditFX();

			dblForwardFXAdjAccrual += dblPeriodAccrual * convAdjResetPeriod.forwardFX();

			dblFundingFXAdjAccrual += dblPeriodAccrual * convAdjResetPeriod.fundingFX();

			dblCreditForwardAdjAccrual += dblPeriodAccrual * convAdjResetPeriod.creditForward();

			dblCreditFundingAdjAccrual += dblPeriodAccrual * convAdjResetPeriod.creditFunding();

			dblForwardFundingAdjAccrual += dblPeriodAccrual * convAdjResetPeriod.forwardFunding();
		}

		org.drip.analytics.output.ConvexityAdjustment convAdj = new
			org.drip.analytics.output.ConvexityAdjustment();

		return convAdj.setCreditForward (dblCreditForwardAdjAccrual / dblUnadjustedAccrual) &&
			convAdj.setCreditFunding (dblCreditFundingAdjAccrual / dblUnadjustedAccrual) &&
				convAdj.setCreditFX (dblCreditFXAdjAccrual / dblUnadjustedAccrual) &&
					convAdj.setForwardFunding (dblForwardFundingAdjAccrual / dblUnadjustedAccrual) &&
						convAdj.setForwardFX (dblForwardFXAdjAccrual / dblUnadjustedAccrual) &&
							convAdj.setFundingFX (dblFundingFXAdjAccrual / dblUnadjustedAccrual) ? convAdj :
								null;
	}

	/**
	 * Retrieve the Compounding Convexity Adjustment Factor for the Reset Period
	 * 
	 * @return The Compounding Convexity Adjustment Factor for the Reset Period
	 * 
	 * @throws If the Compounding Convexity Adjustment Factor cannot be computed
	 */

	public double compoundingConvexityFactor()
		throws java.lang.Exception
	{
		if (org.drip.analytics.period.ResetPeriodContainer.ACCRUAL_COMPOUNDING_RULE_GEOMETRIC ==
			_iAccrualCompoundingRule)
			return 1.;

		double dblUnadjustedAccrual = 0.;
		double dblForwardFundingAdjAccrual = 0.;

		for (org.drip.analytics.output.ResetPeriodMetrics rpm : _lsRPM) {
			org.drip.analytics.output.ConvexityAdjustment convAdjResetPeriod = rpm.convexityAdjustment();

			if (null == convAdjResetPeriod)
				throw new java.lang.Exception
					("CouponPeriodMetrics::compoundingConvexityFactor => No Convexity Adjustment for on/more reset periods");

			double dblPeriodAccrual = rpm.nominalRate() * rpm.dcf();

			dblUnadjustedAccrual += dblPeriodAccrual;

			dblForwardFundingAdjAccrual += dblPeriodAccrual * convAdjResetPeriod.forwardFunding();
		}

		return dblForwardFundingAdjAccrual / dblUnadjustedAccrual;
	}
}
