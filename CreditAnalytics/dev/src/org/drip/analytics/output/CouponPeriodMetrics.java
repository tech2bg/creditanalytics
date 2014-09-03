
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

	/*
	 * Period Specification Fields
	 */

	private double _dblStartDate = java.lang.Double.NaN;
	private double _dblEndDate = java.lang.Double.NaN;
	private double _dblPayDate = java.lang.Double.NaN;
	private double _dblNotional = java.lang.Double.NaN;
	private int _iAccrualCompoundingRule = -1;

	/*
	 * Period State Point Value Fields
	 */

	private java.util.List<org.drip.analytics.output.ResetPeriodMetrics> _lsRPM = null;
	private double _dblSurvival = java.lang.Double.NaN;
	private double _dblDF = java.lang.Double.NaN;
	private double _dblFX = java.lang.Double.NaN;

	/*
	 * Period Joint State Dynamical Adjustment Fields
	 */

	private org.drip.analytics.output.ConvexityAdjustment _convAdj = null;

	/*
	 * The Computed Coupon Period Metrics
	 */

	private double _dblCumulativeDCF = 0.;
	private double _dblCumulativeAccrual = java.lang.Double.NaN;

	/**
	 * Create an Instance of CouponPeriodMetrics from the parameters
	 * 
	 * @param dblStartDate Coupon Period Start Date
	 * @param dblEndDate Coupon Period End Date
	 * @param dblPayDate Coupon Period Pay Date
	 * @param dblNotional Period Annuity Notional
	 * @param iAccrualCompoundingRule The Accrual Compounding Rule
	 * @param lsRPM List of Reset Period Metrics
	 * @param dblSurvival Coupon Period Survival
	 * @param dblDF Coupon Period Discount Factor
	 * @param dblFX The Coupon Period FX Rate
	 * @param convAdj The Convexity Adjustment for the Reset Period
	 * 
	 * @return Instance of CouponPeriodMetrics
	 */

	public static final CouponPeriodMetrics Create (
		final double dblStartDate,
		final double dblEndDate,
		final double dblPayDate,
		final double dblNotional,
		final int iAccrualCompoundingRule,
		final java.util.List<org.drip.analytics.output.ResetPeriodMetrics> lsRPM,
		final double dblSurvival,
		final double dblDF,
		final double dblFX,
		final org.drip.analytics.output.ConvexityAdjustment convAdj)
	{
		try {
			CouponPeriodMetrics cpm = new CouponPeriodMetrics (dblStartDate, dblEndDate, dblPayDate,
				dblNotional, iAccrualCompoundingRule, lsRPM, dblSurvival, dblDF, dblFX, convAdj);

			return cpm.initialize() && cpm.setConvexityAdjustment() ? cpm : null;
		} catch (java.lang.Exception e) {
			e.printStackTrace();
		}

		return null;
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
	 * Retrieve the Period Pay Date
	 * 
	 * @return The Period Pay Date
	 */

	public double payDate()
	{
		return _dblPayDate;
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
	 * Retrieve the Constituent Reset Period Metrics
	 * 
	 * @return The Constituent Reset Period Metrics
	 */

	public java.util.List<org.drip.analytics.output.ResetPeriodMetrics> resetPeriodMetrics()
	{
		return _lsRPM;
	}

	/**
	 * Retrieve the Compounded Accrual Rate in the Coupon Currency
	 * 
	 * @return The Compounded Accrual Rate in the Coupon Currency
	 */

	public double compoundedAccrualRate()
	{
		return _dblCumulativeAccrual / _dblCumulativeDCF;
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
	 * Retrieve the Period Single Point Credit Loading Coefficient
	 * 
	 * @return The Period Single Point Credit Loading Coefficient
	 */

	public java.util.Map<java.lang.Double, java.lang.Double> singlePointCreditLoading()
	{
		java.util.Map<java.lang.Double, java.lang.Double> mapCreditLoading = new
			java.util.TreeMap<java.lang.Double, java.lang.Double>();

		try {
			mapCreditLoading.put (_dblPayDate, _dblNotional * _dblCumulativeAccrual * _dblDF * _dblFX *
				_convAdj.cumulative());
		} catch (java.lang.Exception e) {
			e.printStackTrace();

			return null;
		}

		return mapCreditLoading;
	}

	/**
	 * Retrieve the Period Single Point Forward Loading Coefficient
	 * 
	 * @return The Period Single Point Forward Loading Coefficient
	 */

	public java.util.Map<java.lang.Double, java.lang.Double> singlePointForwardLoading()
	{
		java.util.Map<java.lang.Double, java.lang.Double> mapForwardLoading = new
			java.util.TreeMap<java.lang.Double, java.lang.Double>();

		try {
			mapForwardLoading.put (_dblEndDate, _dblNotional * _dblCumulativeDCF * _dblSurvival * _dblDF *
				_dblFX * _convAdj.cumulative());
		} catch (java.lang.Exception e) {
			e.printStackTrace();

			return null;
		}

		return mapForwardLoading;
	}

	/**
	 * Retrieve the Period Single Point Funding Loading Coefficient
	 * 
	 * @return The Period Single Point Funding Loading Coefficient
	 */

	public java.util.Map<java.lang.Double, java.lang.Double> singlePointFundingLoading()
	{
		java.util.Map<java.lang.Double, java.lang.Double> mapFundingLoading = new
			java.util.TreeMap<java.lang.Double, java.lang.Double>();

		try {
			mapFundingLoading.put (_dblPayDate, _dblNotional * _dblSurvival * _dblCumulativeAccrual * _dblFX
				* _convAdj.cumulative());
		} catch (java.lang.Exception e) {
			e.printStackTrace();

			return null;
		}

		return mapFundingLoading;
	}

	/**
	 * Retrieve the Period Single Point FX Loading Coefficient
	 * 
	 * @return The Period Single Point FX Loading Coefficient
	 */

	public java.util.Map<java.lang.Double, java.lang.Double> singlePointFXLoading()
	{
		java.util.Map<java.lang.Double, java.lang.Double> mapFXLoading = new
			java.util.TreeMap<java.lang.Double, java.lang.Double>();

		try {
			mapFXLoading.put (_dblPayDate, _dblNotional * _dblSurvival * _dblCumulativeAccrual * _dblDF *
				_convAdj.cumulative());
		} catch (java.lang.Exception e) {
			e.printStackTrace();

			return null;
		}

		return mapFXLoading;
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
	 * Retrieve the FX Rate applied
	 * 
	 * @return The FX Rate applied
	 */

	public double fx()
	{
		return _dblFX;
	}

	/**
	 * Return the Accrual DCF
	 * 
	 * @return The Accrual DCF
	 */

	public double dcf()
	{
		return _dblCumulativeDCF;
	}

	/**
	 * Retrieve the Compounded Accrual in the Coupon Currency
	 * 
	 * @return The Compounded Accrual in the Coupon Currency
	 */

	public double compoundedAccrual()
	{
		return _dblCumulativeAccrual;
	}

	/**
	 * Retrieve the Period Annuity in the Pay Currency
	 * 
	 * @return The Period Annuity in the Pay Currency
	 */

	public double annuity()
	{
		return _dblNotional * _dblSurvival * _dblDF * _dblFX;
	}

	/**
	 * Retrieve the Convexity Adjustment for the Reset Period
	 * 
	 * @return The Convexity Adjustment for the Reset Period
	 */

	public org.drip.analytics.output.ConvexityAdjustment convexityAdjustment()
	{
		return _convAdj;
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
		if (org.drip.analytics.support.ResetUtil.ACCRUAL_COMPOUNDING_RULE_GEOMETRIC ==
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

	private CouponPeriodMetrics (
		final double dblStartDate,
		final double dblEndDate,
		final double dblPayDate,
		final double dblNotional,
		final int iAccrualCompoundingRule,
		final java.util.List<org.drip.analytics.output.ResetPeriodMetrics> lsRPM,
		final double dblSurvival,
		final double dblDF,
		final double dblFX,
		final org.drip.analytics.output.ConvexityAdjustment convAdj)
		throws java.lang.Exception
	{
		if (!org.drip.quant.common.NumberUtil.IsValid (_dblStartDate = dblStartDate) ||
			!org.drip.quant.common.NumberUtil.IsValid (_dblEndDate = dblEndDate) ||
				!org.drip.quant.common.NumberUtil.IsValid (_dblPayDate = dblPayDate) ||
					!org.drip.quant.common.NumberUtil.IsValid (_dblNotional = dblNotional) ||
						!org.drip.analytics.support.ResetUtil.ValidateCompoundingRule
							(_iAccrualCompoundingRule = iAccrualCompoundingRule) || null == (_lsRPM = lsRPM)
								|| 0 == _lsRPM.size() || !org.drip.quant.common.NumberUtil.IsValid
									(_dblSurvival = dblSurvival) || !org.drip.quant.common.NumberUtil.IsValid
										(_dblDF = dblDF) || !org.drip.quant.common.NumberUtil.IsValid (_dblFX
											= dblFX))
			throw new java.lang.Exception ("CouponPeriodMetrics ctr: Invalid Inputs");

		_convAdj = convAdj;
	}

	private boolean initialize()
	{
		if (org.drip.analytics.support.ResetUtil.ACCRUAL_COMPOUNDING_RULE_ARITHMETIC ==
			_iAccrualCompoundingRule)
			_dblCumulativeAccrual = 0.;
		else if (org.drip.analytics.support.ResetUtil.ACCRUAL_COMPOUNDING_RULE_GEOMETRIC ==
			_iAccrualCompoundingRule)
			_dblCumulativeAccrual = 1.;

		for (org.drip.analytics.output.ResetPeriodMetrics rpm : _lsRPM) {
			double dblDCF = rpm.dcf();

			_dblCumulativeDCF += dblDCF;

			if (org.drip.analytics.support.ResetUtil.ACCRUAL_COMPOUNDING_RULE_ARITHMETIC ==
				_iAccrualCompoundingRule)
				_dblCumulativeAccrual += rpm.nominalRate() * dblDCF;
			else if (org.drip.analytics.support.ResetUtil.ACCRUAL_COMPOUNDING_RULE_GEOMETRIC ==
				_iAccrualCompoundingRule)
				_dblCumulativeAccrual *= (1. + rpm.nominalRate() * dblDCF);
		}

		if (org.drip.analytics.support.ResetUtil.ACCRUAL_COMPOUNDING_RULE_GEOMETRIC ==
			_iAccrualCompoundingRule)
			_dblCumulativeAccrual -= 1.;

		return true;
	}

	private boolean setConvexityAdjustment()
	{
		if (org.drip.analytics.support.ResetUtil.ACCRUAL_COMPOUNDING_RULE_GEOMETRIC ==
			_iAccrualCompoundingRule)
			return true;

		double dblUnadjustedAccrual = 0.;
		double dblCreditFXAdjAccrual = 0.;
		double dblForwardFXAdjAccrual = 0.;
		double dblFundingFXAdjAccrual = 0.;
		double dblCreditForwardAdjAccrual = 0.;
		double dblCreditFundingAdjAccrual = 0.;
		double dblForwardFundingAdjAccrual = 0.;

		for (org.drip.analytics.output.ResetPeriodMetrics rpm : _lsRPM) {
			org.drip.analytics.output.ConvexityAdjustment convAdjResetPeriod = rpm.convexityAdjustment();

			if (null == convAdjResetPeriod) return false;

			double dblPeriodAccrual = rpm.nominalRate() * rpm.dcf();

			dblUnadjustedAccrual += dblPeriodAccrual;

			dblCreditFXAdjAccrual += dblPeriodAccrual * convAdjResetPeriod.creditFX();

			dblForwardFXAdjAccrual += dblPeriodAccrual * convAdjResetPeriod.forwardFX();

			dblFundingFXAdjAccrual += dblPeriodAccrual * convAdjResetPeriod.fundingFX();

			dblCreditForwardAdjAccrual += dblPeriodAccrual * convAdjResetPeriod.creditForward();

			dblCreditFundingAdjAccrual += dblPeriodAccrual * convAdjResetPeriod.creditFunding();

			dblForwardFundingAdjAccrual += dblPeriodAccrual * convAdjResetPeriod.forwardFunding();
		}

		_convAdj = new org.drip.analytics.output.ConvexityAdjustment();

		return 0. == dblUnadjustedAccrual ? true : _convAdj.setCreditForward (dblCreditForwardAdjAccrual /
			dblUnadjustedAccrual) && _convAdj.setCreditFunding (dblCreditFundingAdjAccrual /
				dblUnadjustedAccrual) && _convAdj.setCreditFX (dblCreditFXAdjAccrual / dblUnadjustedAccrual)
					&& _convAdj.setForwardFunding (dblForwardFundingAdjAccrual / dblUnadjustedAccrual) &&
						_convAdj.setForwardFX (dblForwardFXAdjAccrual / dblUnadjustedAccrual) &&
							_convAdj.setFundingFX (dblFundingFXAdjAccrual / dblUnadjustedAccrual);
	}
}
