
package org.drip.analytics.cashflow;

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
 * CompositePeriod implements the composite coupon period functionality.
 *
 * @author Lakshmi Krishnamurthy
 */

public abstract class CompositePeriod {
	private int _iFreq = 2;
	private int _iAccrualCompoundingRule = -1;
	private java.lang.String _strPayCurrency = "";
	private double _dblPayDate = java.lang.Double.NaN;
	private double _dblBaseNotional = java.lang.Double.NaN;
	private double _dblFXFixingDate = java.lang.Double.NaN;
	private org.drip.state.identifier.CreditLabel _creditLabel = null;
	private org.drip.product.params.FactorSchedule _notlSchedule = null;
	private java.util.List<org.drip.analytics.cashflow.ComposableUnitPeriod> _lsCUP = null;

	private double calibAccrued (
		final org.drip.analytics.cashflow.CompositePeriodQuoteSet cpqs,
		final double dblValueDate,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs)
		throws java.lang.Exception
	{
		org.drip.analytics.output.CompositePeriodMetrics cpm = accrualMetrics (dblValueDate, csqs);

		if (null == cpm) return 0.;

		double dblAccrualDCF = 0.;

		for (org.drip.analytics.output.UnitPeriodMetrics upm : cpm.unitMetrics())
			dblAccrualDCF += upm.dcf();

		return notional (_dblPayDate) * fx (csqs) * dblAccrualDCF * (cpqs.baseRate() + cpqs.basis());
	}

	/**
	 * ComposedPeriod Constructor
	 * 
	 * @param lsCUP List of the Composable Unit Periods
	 * @param iFreq Frequency
	 * @param dblPayDate Period Pay Date
	 * @param strPayCurrency Pay Currency
	 * @param iAccrualCompoundingRule The Accrual Compounding Rule
	 * @param dblBaseNotional Coupon Period Base Notional
	 * @param notlSchedule Coupon Period Notional Schedule
	 * @param creditLabel The Credit Label
	 * @param dblFXFixingDate The FX Fixing Date for non-MTM'ed Cash-flow
	 * 
	 * @throws java.lang.Exception Thrown if the Accrual Compounding Rule is invalid
	 */

	public CompositePeriod (
		final java.util.List<org.drip.analytics.cashflow.ComposableUnitPeriod> lsCUP,
		final int iFreq,
		final double dblPayDate,
		final java.lang.String strPayCurrency,
		final int iAccrualCompoundingRule,
		final double dblBaseNotional,
		final org.drip.product.params.FactorSchedule notlSchedule,
		final org.drip.state.identifier.CreditLabel creditLabel,
		final double dblFXFixingDate)
		throws java.lang.Exception
	{
		if (null == (_lsCUP = lsCUP) || 0 == _lsCUP.size() || 0 >= (_iFreq = iFreq) ||
			!org.drip.quant.common.NumberUtil.IsValid (_dblPayDate = dblPayDate) || null == (_strPayCurrency
				= strPayCurrency) || _strPayCurrency.isEmpty() ||
					!org.drip.analytics.support.CompositePeriodUtil.ValidateCompoundingRule (_iAccrualCompoundingRule =
						iAccrualCompoundingRule) || !org.drip.quant.common.NumberUtil.IsValid
							(_dblBaseNotional = dblBaseNotional))
			throw new java.lang.Exception ("CompositePeriod ctr: Invalid Inputs");

		_creditLabel = creditLabel;
		_dblFXFixingDate = dblFXFixingDate;

		if (null == (_notlSchedule = notlSchedule))
			_notlSchedule = org.drip.product.params.FactorSchedule.CreateBulletSchedule();
	}

	/**
	 * Retrieve the List of Composable Periods
	 * 
	 * @return The List of Composable Periods
	 */

	public java.util.List<org.drip.analytics.cashflow.ComposableUnitPeriod> periods()
	{
		return _lsCUP;
	}

	/**
	 * Period Start Date
	 * 
	 * @return The Period Start Date
	 */

	public double startDate()
	{
		return _lsCUP.get (0).startDate();
	}

	/**
	 * Period End Date
	 * 
	 * @return The Period End Date
	 */

	public double endDate()
	{
		return _lsCUP.get (_lsCUP.size() - 1).endDate();
	}

	/**
	 * Check whether the supplied date is inside the period specified
	 * 
	 * @param dblDate Date input
	 * 
	 * @return True indicates the specified date is inside the period
	 * 
	 * @throws java.lang.Exception Thrown if input is invalid
	 */

	public boolean contains (
		final double dblDate)
		throws java.lang.Exception
	{
		if (!org.drip.quant.common.NumberUtil.IsValid (dblDate))
			throw new java.lang.Exception ("CompositePeriod::contains => Invalid Inputs");

		return dblDate >= startDate() && dblDate <= endDate();
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
	 * Return the period Pay Date
	 * 
	 * @return Period Pay Date
	 */

	public double payDate()
	{
		return _dblPayDate;
	}

	/**
	 * Return the period FX Fixing Date
	 * 
	 * @return Period FX Fixing Date
	 */

	public double fxFixingDate()
	{
		return _dblFXFixingDate;
	}

	/**
	 * Is this Cash Flow FX MTM'ed?
	 * 
	 * @return TRUE => FX MTM is on (i.e., FX is not driven by fixing)
	 */

	public boolean isFXMTM()
	{
		return !org.drip.quant.common.NumberUtil.IsValid (_dblFXFixingDate);
	}

	/**
	 * Coupon Period FX
	 * 
	 * @param csqs Market Parameters
	 * 
	 * @return The Period FX
	 * 
	 * @throws java.lang.Exception Thrown if the Inputs are Invalid
	 */

	public double fx (
		final org.drip.param.market.CurveSurfaceQuoteSet csqs)
		throws java.lang.Exception
	{
		org.drip.state.identifier.FXLabel fxLabel = fxLabel();

		if (null == fxLabel) return 1.;

		if (null == csqs) throw new java.lang.Exception ("CompositePeriod::fx => Invalid Inputs");

		if (!isFXMTM()) return csqs.getFixing (_dblFXFixingDate, fxLabel);

		org.drip.quant.function1D.AbstractUnivariate auFX = csqs.fxCurve (fxLabel);

		if (null == auFX)
			throw new java.lang.Exception ("CompositePeriod::fx => No Curve for " +
				fxLabel.fullyQualifiedName());

		return auFX.evaluate (_dblPayDate);
	}

	/**
	 * Retrieve the Coupon Frequency
	 * 
	 * @return The Coupon Frequency
	 */

	public int freq()
	{
		return _iFreq;
	}

	/**
	 * Convert the Coupon Frequency into a Tenor
	 * 
	 * @return The Coupon Frequency converted into a Tenor
	 */

	public java.lang.String tenor()
	{
		int iTenorInMonths = 12 / _iFreq ;
		return 1 == iTenorInMonths || 2 == iTenorInMonths || 3 == iTenorInMonths || 6 == iTenorInMonths || 12
			== iTenorInMonths ? iTenorInMonths + "M" : "ON";
	}

	/**
	 * Retrieve the Pay Currency
	 * 
	 * @return The Pay Currency
	 */

	public java.lang.String payCurrency()
	{
		return _strPayCurrency;
	}

	/**
	 * Retrieve the Coupon Currency
	 * 
	 * @return The Coupon Currency
	 */

	public java.lang.String couponCurrency()
	{
		return _lsCUP.get (0).couponCurrency();
	}

	/**
	 * Coupon Period Survival Probability
	 * 
	 * @param csqs Market Parameters
	 * 
	 * @return The Period Survival Probability
	 * 
	 * @throws java.lang.Exception Thrown if the Inputs are Invalid
	 */

	public double survival (
		final org.drip.param.market.CurveSurfaceQuoteSet csqs)
		throws java.lang.Exception
	{
		org.drip.state.identifier.CreditLabel creditLabel = creditLabel();

		if (null == creditLabel) return 1.;

		if (null == csqs) throw new java.lang.Exception ("CompositePeriod::survival => Invalid Inputs");

		org.drip.analytics.definition.CreditCurve cc = csqs.creditCurve (creditLabel);

		if (null == cc)
			throw new java.lang.Exception ("CompositePeriod::survival => No Curve for " +
				creditLabel.fullyQualifiedName());

		return cc.survival (_dblPayDate);
	}

	/**
	 * Coupon Period Discount Factor
	 * 
	 * @param csqs Market Parameters
	 * 
	 * @return The Period Discount Factor
	 * 
	 * @throws java.lang.Exception Thrown if the Inputs are Invalid
	 */

	public double df (
		final org.drip.param.market.CurveSurfaceQuoteSet csqs)
		throws java.lang.Exception
	{
		org.drip.state.identifier.FundingLabel fundingLabel = fundingLabel();

		if (null == csqs) throw new java.lang.Exception ("CompositePeriod::df => Invalid Inputs");

		org.drip.analytics.rates.DiscountCurve dc = csqs.fundingCurve (fundingLabel);

		if (null == dc)
			throw new java.lang.Exception ("CompositePeriod::df => No Curve for " +
				fundingLabel.fullyQualifiedName());

		return dc.df (_dblPayDate);
	}

	/**
	 * Get the Period Base Notional
	 * 
	 * @return Period Base Notional
	 */

	public double baseNotional()
	{
		return _dblBaseNotional;
	}

	/**
	 * Get the period Notional Schedule
	 * 
	 * @return Period Notional Schedule
	 */

	public org.drip.product.params.FactorSchedule notionalSchedule()
	{
		return _notlSchedule;
	}

	/**
	 * Coupon Period Notional Corresponding to the specified Date
	 * 
	 * @param dblDate The Specified Date
	 * 
	 * @return The Period Notional Corresponding to the specified Date
	 * 
	 * @throws java.lang.Exception Thrown if the Inputs are Invalid
	 */

	public double notional (
		final double dblDate)
		throws java.lang.Exception
	{
		if (!org.drip.quant.common.NumberUtil.IsValid (dblDate) || !contains (dblDate))
			throw new java.lang.Exception ("CompositePeriod::notional => Invalid Inputs");

		return _dblBaseNotional * (null == _notlSchedule ? 1. : _notlSchedule.getFactor (dblDate));
	}

	/**
	 * Coupon Period Notional Aggregated over the specified Dates
	 * 
	 * @param dblDate1 The Date #1
	 * @param dblDate2 The Date #2
	 * 
	 * @return The Period Notional Aggregated over the specified Dates
	 * 
	 * @throws java.lang.Exception Thrown if the Inputs are Invalid
	 */

	public double notional (
		final double dblDate1,
		final double dblDate2)
		throws java.lang.Exception
	{
		if (!org.drip.quant.common.NumberUtil.IsValid (dblDate1) || !org.drip.quant.common.NumberUtil.IsValid
			(dblDate2) || !contains (dblDate1) || !contains (dblDate2))
			throw new java.lang.Exception ("CompositePeriod::notional => Invalid Dates");

		return _dblBaseNotional * (null == _notlSchedule ? 1. : _notlSchedule.getFactor (dblDate1,
			dblDate2));
	}

	/**
	 * Return the Collateral Label
	 * 
	 * @return The Collateral Label
	 */

	public org.drip.state.identifier.CollateralLabel collateralLabel()
	{
		return org.drip.state.identifier.CollateralLabel.Standard (_strPayCurrency);
	}

	/**
	 * Return the Credit Label
	 * 
	 * @return The Credit Label
	 */

	public org.drip.state.identifier.CreditLabel creditLabel()
	{
		return _creditLabel;
	}

	/**
	 * Return the Forward Label
	 * 
	 * @return The Forward Label
	 */

	public org.drip.state.identifier.ForwardLabel forwardLabel()
	{
		org.drip.analytics.cashflow.ComposableUnitPeriod cp = _lsCUP.get (0);

		if (cp instanceof org.drip.analytics.cashflow.ComposableUnitFixedPeriod) return null;

		return ((org.drip.analytics.cashflow.ComposableUnitFloatingPeriod)
			cp).referenceIndexPeriod().forwardLabel();
	}

	/**
	 * Return the Funding Label
	 * 
	 * @return The Funding Label
	 */

	public org.drip.state.identifier.FundingLabel fundingLabel()
	{
		return org.drip.state.identifier.FundingLabel.Standard (_strPayCurrency);
	}

	/**
	 * Return the FX Label
	 * 
	 * @return The FX Label
	 */

	public org.drip.state.identifier.FXLabel fxLabel()
	{
		java.lang.String strCouponCurrency = couponCurrency();

		return _strPayCurrency.equalsIgnoreCase (strCouponCurrency) ? null :
			org.drip.state.identifier.FXLabel.Standard (_strPayCurrency + "/" + strCouponCurrency);
	}

	/**
	 * Compute the Convexity Adjustment for the composable periods that use arithmetic compounding using the
	 *  specified value date using the market data provided
	 * 
	 * @param dblValueDate The Valuation Date
	 * @param csqs The Market Curves/Surface
	 * 
	 * @return The List of Convexity Adjustments
	 */

	public java.util.List<org.drip.analytics.output.ConvexityAdjustment> periodWiseConvexityAdjustment (
		final double dblValueDate,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs)
	{
		java.util.List<org.drip.analytics.output.ConvexityAdjustment> lsConvAdj = new
			java.util.ArrayList<org.drip.analytics.output.ConvexityAdjustment>();

		if (null == csqs || dblValueDate >= _dblPayDate) {
			for (int i = 0; i < _lsCUP.size(); ++i)
				lsConvAdj.add (new org.drip.analytics.output.ConvexityAdjustment());

			return lsConvAdj;
		}

		org.drip.state.identifier.CreditLabel creditLabel = creditLabel();

		org.drip.state.identifier.ForwardLabel forwardLabel = forwardLabel();

		org.drip.state.identifier.FundingLabel fundingLabel = fundingLabel();

		org.drip.state.identifier.FXLabel fxLabel = fxLabel();

		org.drip.quant.function1D.AbstractUnivariate auCreditVol = csqs.creditCurveVolSurface (creditLabel);

		org.drip.quant.function1D.AbstractUnivariate auForwardVol = csqs.forwardCurveVolSurface
			(forwardLabel);

		org.drip.quant.function1D.AbstractUnivariate auFundingVol = csqs.fundingCurveVolSurface
			(fundingLabel);

		org.drip.quant.function1D.AbstractUnivariate auFXVol = csqs.fxCurveVolSurface (fxLabel);

		org.drip.quant.function1D.AbstractUnivariate auCreditForwardCorr = csqs.creditForwardCorrSurface
			(creditLabel, forwardLabel);

		org.drip.quant.function1D.AbstractUnivariate auForwardFundingCorr = csqs.forwardFundingCorrSurface
			(forwardLabel, fundingLabel);

		org.drip.quant.function1D.AbstractUnivariate auForwardFXCorr = csqs.forwardFXCorrSurface
			(forwardLabel, fxLabel);

		try {
			double dblCreditFundingConvexityAdjustment = java.lang.Math.exp
				(org.drip.analytics.support.OptionHelper.IntegratedCrossVolQuanto (auCreditVol, auFundingVol,
					csqs.creditFundingCorrSurface (creditLabel, fundingLabel), dblValueDate, _dblPayDate));

			double dblCreditFXConvexityAdjustment = isFXMTM() ? java.lang.Math.exp
				(org.drip.analytics.support.OptionHelper.IntegratedCrossVolQuanto (auCreditVol, auFXVol,
					csqs.creditFXCorrSurface (creditLabel, fxLabel), dblValueDate, _dblPayDate)) : 1.;

			double dblFundingFXConvexityAdjustment = isFXMTM() ? java.lang.Math.exp
				(org.drip.analytics.support.OptionHelper.IntegratedCrossVolQuanto (auFundingVol, auFXVol,
					csqs.fundingFXCorrSurface (fundingLabel, fxLabel), dblValueDate, _dblPayDate)): 1.;

			for (org.drip.analytics.cashflow.ComposableUnitPeriod cup : _lsCUP) {
				org.drip.analytics.output.ConvexityAdjustment convAdj = new
					org.drip.analytics.output.ConvexityAdjustment();

				if (!convAdj.setCreditFunding (dblCreditFundingConvexityAdjustment) || !convAdj.setCreditFX
					(dblCreditFXConvexityAdjustment) || !convAdj.setFundingFX
					(dblFundingFXConvexityAdjustment))
					return null;

				if (null != forwardLabel) {
					if (!(cup instanceof org.drip.analytics.cashflow.ComposableUnitFloatingPeriod))
						return null;

					double dblFixingDate = ((org.drip.analytics.cashflow.ComposableUnitFloatingPeriod)
						cup).referenceIndexPeriod().fixingDate();

					if (!convAdj.setCreditForward (dblValueDate < dblFixingDate ? java.lang.Math.exp
						(org.drip.analytics.support.OptionHelper.IntegratedCrossVolQuanto (auCreditVol,
							auForwardVol, auCreditForwardCorr, dblValueDate, dblFixingDate)) : 1.))
						return null;

					if (!convAdj.setForwardFunding (dblValueDate < dblFixingDate ? java.lang.Math.exp
						(org.drip.analytics.support.OptionHelper.IntegratedCrossVolQuanto (auForwardVol,
							auFundingVol, auForwardFundingCorr, dblValueDate, dblFixingDate)) : 1.))
						return null;

					if (!convAdj.setForwardFX (isFXMTM() && dblValueDate < dblFixingDate ? java.lang.Math.exp
						(org.drip.analytics.support.OptionHelper.IntegratedCrossVolQuanto (auForwardVol,
							auFXVol, auForwardFXCorr, dblValueDate, dblFixingDate)) : 1.))
						return null;
				}

				lsConvAdj.add (convAdj);
			}

			return lsConvAdj;
		} catch (java.lang.Exception e) {
			e.printStackTrace();
		}

		return null;
	}

	/**
	 * Compute the Convexity Adjustment for the composable periods that use geometric compounding using the
	 *  specified value date using the market data provided
	 * 
	 * @param dblValueDate The Valuation Date
	 * @param csqs The Market Curves/Surface
	 * 
	 * @return The Convexity Adjustment
	 */

	public org.drip.analytics.output.ConvexityAdjustment terminalConvexityAdjustment (
		final double dblValueDate,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs)
	{
		if (null == csqs || dblValueDate >= _dblPayDate)
			return new org.drip.analytics.output.ConvexityAdjustment();

		org.drip.state.identifier.CreditLabel creditLabel = creditLabel();

		org.drip.state.identifier.ForwardLabel forwardLabel = forwardLabel();

		org.drip.state.identifier.FundingLabel fundingLabel = fundingLabel();

		org.drip.state.identifier.FXLabel fxLabel = fxLabel();

		org.drip.quant.function1D.AbstractUnivariate auCreditVol = csqs.creditCurveVolSurface (creditLabel);

		org.drip.quant.function1D.AbstractUnivariate auForwardVol = csqs.forwardCurveVolSurface
			(forwardLabel);

		org.drip.quant.function1D.AbstractUnivariate auFundingVol = csqs.fundingCurveVolSurface
			(fundingLabel);

		org.drip.quant.function1D.AbstractUnivariate auFXVol = csqs.fxCurveVolSurface (fxLabel);

		org.drip.analytics.output.ConvexityAdjustment convAdj = new
			org.drip.analytics.output.ConvexityAdjustment();

		try {
			if (!convAdj.setCreditFunding (java.lang.Math.exp
				(org.drip.analytics.support.OptionHelper.IntegratedCrossVolQuanto (auCreditVol, auFundingVol,
					csqs.creditFundingCorrSurface (creditLabel, fundingLabel), dblValueDate, _dblPayDate))))
				return null;

			if (isFXMTM() && !convAdj.setCreditFX (java.lang.Math.exp
				(org.drip.analytics.support.OptionHelper.IntegratedCrossVolQuanto (auCreditVol, auFXVol,
					csqs.creditFXCorrSurface (creditLabel, fxLabel), dblValueDate, _dblPayDate))))
				return null;

			if (isFXMTM() && !convAdj.setFundingFX (java.lang.Math.exp
				(org.drip.analytics.support.OptionHelper.IntegratedCrossVolQuanto (auFundingVol, auFXVol,
					csqs.fundingFXCorrSurface (fundingLabel, fxLabel), dblValueDate, _dblPayDate))))
				return null;

			if (null == forwardLabel) return convAdj;

			org.drip.analytics.cashflow.ComposableUnitPeriod cup = _lsCUP.get (0);

			if (!(cup instanceof org.drip.analytics.cashflow.ComposableUnitFloatingPeriod)) return null;

			double dblFixingDate = ((org.drip.analytics.cashflow.ComposableUnitFloatingPeriod)
				cup).referenceIndexPeriod().fixingDate();

			if (dblValueDate < dblFixingDate) {
				if (!convAdj.setCreditForward (java.lang.Math.exp
					(org.drip.analytics.support.OptionHelper.IntegratedCrossVolQuanto (auCreditVol,
						auForwardVol, csqs.creditForwardCorrSurface (creditLabel, forwardLabel),
							dblValueDate, dblFixingDate))))
					return null;

				if (!convAdj.setForwardFunding (java.lang.Math.exp
					(org.drip.analytics.support.OptionHelper.IntegratedCrossVolQuanto (auForwardVol,
						auFundingVol, csqs.forwardFundingCorrSurface (forwardLabel, fundingLabel),
							dblValueDate, dblFixingDate))))
					return null;

				if (isFXMTM() && !convAdj.setForwardFX (java.lang.Math.exp
					(org.drip.analytics.support.OptionHelper.IntegratedCrossVolQuanto (auForwardVol, auFXVol,
						csqs.forwardFXCorrSurface (forwardLabel, fxLabel), dblValueDate, dblFixingDate))))
					return null;
			}

			return convAdj;
		} catch (java.lang.Exception e) {
			e.printStackTrace();
		}

		return null;
	}

	/**
	 * Compute the Period Coupon Measures
	 * 
	 * @param dblValueDate Valuation Date
	 * @param csqs The Market Curve Surface/Quote Set
	 * 
	 * @return The Period Coupon Measures
	 */

	public org.drip.analytics.output.CompositePeriodMetrics couponMetrics (
		final double dblValueDate,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs)
	{
		if (!org.drip.quant.common.NumberUtil.IsValid (dblValueDate)) return null;

		java.util.List<org.drip.analytics.output.UnitPeriodMetrics> lsUPM = new
			java.util.ArrayList<org.drip.analytics.output.UnitPeriodMetrics>();

		int iNumPeriodUnit = _lsCUP.size();

		try {
			if (org.drip.analytics.support.CompositePeriodUtil.ACCRUAL_COMPOUNDING_RULE_ARITHMETIC ==
				_iAccrualCompoundingRule) {
				java.util.List<org.drip.analytics.output.ConvexityAdjustment> lsConvAdj =
					periodWiseConvexityAdjustment (dblValueDate, csqs);

				if (null == lsConvAdj || iNumPeriodUnit != lsConvAdj.size()) return null;

				for (int i = 0; i < iNumPeriodUnit; ++i) {
					org.drip.analytics.cashflow.ComposableUnitPeriod cup = _lsCUP.get (i);

					lsUPM.add (new org.drip.analytics.output.UnitPeriodMetrics (cup.startDate(),
						cup.endDate(), cup.fullCouponDCF(), cup.fullCouponRate (csqs), lsConvAdj.get (i)));
				}
			} else if (org.drip.analytics.support.CompositePeriodUtil.ACCRUAL_COMPOUNDING_RULE_GEOMETRIC ==
				_iAccrualCompoundingRule) {
				double dblDCF = 0.;
				double dblRate = 1.;

				for (int i = 0; i < iNumPeriodUnit; ++i) {
					org.drip.analytics.cashflow.ComposableUnitPeriod cup = _lsCUP.get (i);

					double dblPeriodDCF = cup.fullCouponDCF();

					dblDCF += dblPeriodDCF;

					dblRate *= (1. + cup.fullCouponRate (csqs) * dblPeriodDCF);
				}

				lsUPM.add (new org.drip.analytics.output.UnitPeriodMetrics (startDate(), endDate(), dblDCF,
					(dblRate - 1.) / dblDCF, terminalConvexityAdjustment (dblValueDate, csqs)));
			}

			return new org.drip.analytics.output.CompositePeriodMetrics (lsUPM);
		} catch (java.lang.Exception e) {
			e.printStackTrace();
		}

		return null;
	}

	/**
	 * Compute the Coupon Accrual Measures to the specified Accrual End Date
	 * 
	 * @param dblValueDate The Valuation Date
	 * @param csqs The Market Curve Surface/Quote Set
	 * 
	 * @return The Coupon Accrual Measures to the specified Accrual End Date
	 */

	public org.drip.analytics.output.CompositePeriodMetrics accrualMetrics (
		final double dblValueDate,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs)
	{
		try {
			if (!contains (dblValueDate)) return null;

			java.util.List<org.drip.analytics.output.UnitPeriodMetrics> lsUPM = new
				java.util.ArrayList<org.drip.analytics.output.UnitPeriodMetrics>();

			int iNumPeriodUnit = _lsCUP.size();

			if (org.drip.analytics.support.CompositePeriodUtil.ACCRUAL_COMPOUNDING_RULE_ARITHMETIC ==
				_iAccrualCompoundingRule) {
				java.util.List<org.drip.analytics.output.ConvexityAdjustment> lsConvAdj =
					periodWiseConvexityAdjustment (dblValueDate, csqs);

				if (null == lsConvAdj || iNumPeriodUnit != lsConvAdj.size()) return null;

				for (int i = 0; i < iNumPeriodUnit; ++i) {
					org.drip.analytics.cashflow.ComposableUnitPeriod cup = _lsCUP.get (i);

					int iDateLocation = cup.dateLocation (dblValueDate);

					if (org.drip.analytics.cashflow.ComposableUnitFixedPeriod.NODE_INSIDE_SEGMENT ==
						iDateLocation)
						lsUPM.add (new org.drip.analytics.output.UnitPeriodMetrics (cup.startDate(),
							dblValueDate, cup.accrualDCF (dblValueDate), cup.fullCouponRate (csqs),
								lsConvAdj.get (i)));
					else if (org.drip.analytics.cashflow.ComposableUnitFixedPeriod.NODE_LEFT_OF_SEGMENT ==
						iDateLocation)
						lsUPM.add (new org.drip.analytics.output.UnitPeriodMetrics (cup.startDate(),
							cup.endDate(), cup.fullCouponDCF(), cup.fullCouponRate (csqs), lsConvAdj.get
								(i)));
				}
			} else if (org.drip.analytics.support.CompositePeriodUtil.ACCRUAL_COMPOUNDING_RULE_GEOMETRIC ==
				_iAccrualCompoundingRule) {
				double dblAccrualDCF = 0.;
				double dblAccrualRate = 1.;

				for (int i = 0; i < iNumPeriodUnit; ++i) {
					org.drip.analytics.cashflow.ComposableUnitPeriod cup = _lsCUP.get (i);

					int iDateLocation = cup.dateLocation (dblValueDate);

					if (org.drip.analytics.cashflow.ComposableUnitFixedPeriod.NODE_INSIDE_SEGMENT ==
						iDateLocation) {
						double dblPeriodAccrualDCF = cup.accrualDCF (dblValueDate);

						dblAccrualDCF += dblPeriodAccrualDCF;

						dblAccrualRate *= (1. + cup.fullCouponRate (csqs) * dblPeriodAccrualDCF);
					} else if (org.drip.analytics.cashflow.ComposableUnitFixedPeriod.NODE_LEFT_OF_SEGMENT ==
						iDateLocation) {
						double dblPeriodDCF = cup.fullCouponDCF();

						dblAccrualDCF += dblPeriodDCF;

						dblAccrualRate *= (1. + cup.fullCouponRate (csqs) * dblPeriodDCF);
					}
				}

				lsUPM.add (new org.drip.analytics.output.UnitPeriodMetrics (startDate(), dblValueDate,
					dblAccrualDCF, (dblAccrualRate - 1.) / dblAccrualDCF, terminalConvexityAdjustment
						(dblValueDate, csqs)));
			}

			return new org.drip.analytics.output.CompositePeriodMetrics (lsUPM);
		} catch (java.lang.Exception e) {
			e.printStackTrace();
		}

		return null;
	}

	/**
	 * Generate the Forward Predictor/Response Constraint
	 * 
	 * @param dblValueDate The Valuation Date
	 * @param csqs The Market Curve Surface/Quote Set
	 * @param pqs Product Quote Set
	 * 
	 * @return The Forward Predictor/Response Constraint
	 */

	public org.drip.state.estimator.PredictorResponseWeightConstraint forwardPRWC (
		final double dblValueDate,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.product.calib.ProductQuoteSet pqs)
	{
		if (null == pqs) return null;

		double dblDF = java.lang.Double.NaN;
		double dblFX = java.lang.Double.NaN;
		double dblAccrued = java.lang.Double.NaN;
		double dblNotional = java.lang.Double.NaN;
		double dblSurvival = java.lang.Double.NaN;

		org.drip.analytics.cashflow.CompositePeriodQuoteSet cpqs = periodQuoteSet (pqs, csqs);

		try {
			dblFX = fx (csqs);

			dblDF = df (csqs);

			dblSurvival = survival (csqs);

			dblNotional = notional (_dblPayDate);

			dblAccrued = calibAccrued (cpqs, dblValueDate, csqs);
		} catch (java.lang.Exception e) {
			e.printStackTrace();

			return null;
		}

		org.drip.analytics.output.CompositePeriodMetrics cpm = couponMetrics (dblValueDate, csqs);

		if (null == cpm) return null;

		org.drip.state.estimator.PredictorResponseWeightConstraint prwc = new
			org.drip.state.estimator.PredictorResponseWeightConstraint();

		org.drip.state.identifier.ForwardLabel forwardLabel = forwardLabel();

		if (null == forwardLabel || !forwardLabel.match (pqs.forwardLabel())) {
			for (org.drip.analytics.output.UnitPeriodMetrics upm : cpm.unitMetrics()) {
				if (!prwc.updateValue (-1. * dblNotional * dblFX * upm.dcf() * (cpqs.baseRate() +
					cpqs.basis()) * dblSurvival * dblDF * upm.convAdj().cumulative()))
					return null;
			}
		} else {
			for (org.drip.analytics.output.UnitPeriodMetrics upm : cpm.unitMetrics()) {
				double dblDateAnchor = upm.endDate();

				double dblForwardLoading = dblNotional * dblFX * upm.dcf() * dblSurvival * dblDF *
					upm.convAdj().cumulative();

				if (!prwc.addPredictorResponseWeight (dblDateAnchor, dblForwardLoading)) return null;

				if (!prwc.addDResponseWeightDManifestMeasure ("PV", dblDateAnchor, dblForwardLoading))
					return null;

				if (!prwc.updateValue (-1. * dblForwardLoading * cpqs.basis())) return null;
			}
		}

		if (!prwc.updateValue (dblAccrued)) return null;

		if (!prwc.updateDValueDManifestMeasure ("PV", 1.)) return null;

		return prwc;
	}

	/**
	 * Generate the Funding Predictor/Response Constraint
	 * 
	 * @param dblValueDate The Valuation Date
	 * @param csqs The Market Curve Surface/Quote Set
	 * @param pqs Product Quote Set
	 * 
	 * @return The Funding Predictor/Response Constraint
	 */

	public org.drip.state.estimator.PredictorResponseWeightConstraint fundingPRWC (
		final double dblValueDate,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.product.calib.ProductQuoteSet pqs)
	{
		if (null == pqs) return null;

		double dblDF = java.lang.Double.NaN;
		double dblFX = java.lang.Double.NaN;
		double dblAccrued = java.lang.Double.NaN;
		double dblNotional = java.lang.Double.NaN;
		double dblSurvival = java.lang.Double.NaN;

		org.drip.analytics.cashflow.CompositePeriodQuoteSet cpqs = periodQuoteSet (pqs, csqs);

		try {
			dblFX = fx (csqs);

			dblSurvival = survival (csqs);

			dblNotional = notional (_dblPayDate);

			dblAccrued = calibAccrued (cpqs, dblValueDate, csqs);
		} catch (java.lang.Exception e) {
			e.printStackTrace();

			return null;
		}

		org.drip.analytics.output.CompositePeriodMetrics cpm = couponMetrics (dblValueDate, csqs);

		if (null == cpm) return null;

		org.drip.state.estimator.PredictorResponseWeightConstraint prwc = new
			org.drip.state.estimator.PredictorResponseWeightConstraint();

		org.drip.state.identifier.FundingLabel fundingLabel = fundingLabel();

		if (!fundingLabel.match (pqs.fundingLabel())) {
			try {
				dblDF = df (csqs);
			} catch (java.lang.Exception e) {
				e.printStackTrace();

				return null;
			}

			for (org.drip.analytics.output.UnitPeriodMetrics upm : cpm.unitMetrics()) {
				if (!prwc.updateValue (-1. * dblNotional * dblFX * upm.dcf() * (cpqs.baseRate() +
					cpqs.basis()) * dblSurvival * dblDF * upm.convAdj().cumulative()))
					return null;
			}
		} else {
			for (org.drip.analytics.output.UnitPeriodMetrics upm : cpm.unitMetrics()) {
				double dblFundingLoading = dblNotional * dblFX * upm.dcf() * (cpqs.baseRate() +
					cpqs.basis()) * dblSurvival * upm.convAdj().cumulative();

				if (!prwc.addPredictorResponseWeight (_dblPayDate, dblFundingLoading)) return null;

				if (!prwc.addDResponseWeightDManifestMeasure ("PV", _dblPayDate, dblFundingLoading))
					return null;
			}
		}

		if (!prwc.updateValue (dblAccrued)) return null;

		if (!prwc.updateDValueDManifestMeasure ("PV", 1.)) return null;

		return prwc;
	}

	/**
	 * Generate the Merged Forward/Funding Predictor/Response Constraint
	 * 
	 * @param dblValueDate The Valuation Date
	 * @param csqs The Market Curve Surface/Quote Set
	 * @param pqs Product Quote Set
	 * 
	 * @return The Merged Forward/Funding Predictor/Response Constraint
	 */

	public org.drip.state.estimator.PredictorResponseWeightConstraint forwardFundingPRWC (
		final double dblValueDate,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.product.calib.ProductQuoteSet pqs)
	{
		if (null == pqs) return null;

		org.drip.state.identifier.ForwardLabel forwardLabel = forwardLabel();

		if (null == forwardLabel || !fundingLabel().match (pqs.fundingLabel()))
			return fundingPRWC (dblValueDate, csqs, pqs);

		double dblFX = java.lang.Double.NaN;
		double dblAccrued = java.lang.Double.NaN;
		double dblNotional = java.lang.Double.NaN;
		double dblSurvival = java.lang.Double.NaN;

		org.drip.analytics.cashflow.CompositePeriodQuoteSet cpqs = periodQuoteSet (pqs, csqs);

		try {
			dblFX = fx (csqs);

			dblSurvival = survival (csqs);

			dblNotional = notional (_dblPayDate);

			dblAccrued = calibAccrued (cpqs, dblValueDate, csqs);
		} catch (java.lang.Exception e) {
			e.printStackTrace();

			return null;
		}

		org.drip.state.estimator.PredictorResponseWeightConstraint prwc = new
			org.drip.state.estimator.PredictorResponseWeightConstraint();

		org.drip.analytics.output.CompositePeriodMetrics cpm = couponMetrics (dblValueDate, csqs);

		if (null == cpm) return null;

		for (org.drip.analytics.output.UnitPeriodMetrics upm : cpm.unitMetrics()) {
			double dblFundingLoading = dblNotional * dblFX * dblSurvival * upm.convAdj().cumulative();

			double dblStartDate = upm.startDate();

			double dblEndDate = upm.endDate();

			if (!prwc.addPredictorResponseWeight (dblStartDate, dblFundingLoading)) return null;

			if (!prwc.addPredictorResponseWeight (dblEndDate, -1. * dblFundingLoading)) return null;

			if (!prwc.addDResponseWeightDManifestMeasure ("PV", dblStartDate, dblFundingLoading))
				return null;

			if (!prwc.addDResponseWeightDManifestMeasure ("PV", dblEndDate, -1. * dblFundingLoading))
				return null;
		}

		if (!prwc.updateValue (dblAccrued)) return null;

		if (!prwc.updateDValueDManifestMeasure ("PV", 1.)) return null;

		return prwc;
	}

	/**
	 * Retrieve the Period Calibration Quotes from the specified product quote set
	 * 
	 * @param pqs The Product Quote Set
	 * @param csqs The Market Curve Surface/Quote Set
	 * 
	 * @return The Composed Period Quote Set
	 */

	abstract public org.drip.analytics.cashflow.CompositePeriodQuoteSet periodQuoteSet (
		final org.drip.product.calib.ProductQuoteSet pqs,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs);
}
