
package org.drip.product.cashflow;

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
 * FixedStream contains an implementation of the Fixed leg cash flow stream. It exports the following
 * functionality:
 *  - Standard/Custom Constructor for the FixedStream Component
 *  - Dates: Effective, Maturity, Coupon dates and Product settlement Parameters
 *  - Coupon/Notional Outstanding as well as schedules
 *  - Market Parameters: Discount, Forward, Credit, Treasury Curves
 *  - Cash Flow Periods: Coupon flows and (Optionally) Loss Flows
 *  - Valuation: Named Measure Generation
 *  - Calibration: The codes and constraints generation
 *  - Jacobians: Quote/DF and PV/DF micro-Jacobian generation
 *  - Serialization into and de-serialization out of byte arrays
 * 
 * @author Lakshmi Krishnamurthy
 */

public class ConsolidatedStream {
	private boolean _bFXMTMMode = false;
	private java.lang.String _strName = "";
	private double _dblCouponSpread = 0.0001;
	private java.lang.String _strPayCurrency = "";
	private java.lang.String _strCouponCurrency = "";
	private double _dblMaturity = java.lang.Double.NaN;
	private double _dblEffective = java.lang.Double.NaN;
	private org.drip.product.params.CurrencyPair _cp = null;
	private double _dblInitialNotional = java.lang.Double.NaN;
	private org.drip.state.identifier.ForwardLabel _fri = null;
	private org.drip.product.params.FactorSchedule _notlSchedule = null;
	private java.util.List<org.drip.analytics.period.CashflowPeriod> _lsCouponPeriod = null;

	private org.drip.state.estimator.PredictorResponseWeightConstraint telescopedPRWC (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.pricer.PricerParams pricerParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams vcp,
		final org.drip.product.calib.StreamQuoteSet sqs)
	{
		double dblValueDate = valParams.valueDate();

		if (dblValueDate >= _dblMaturity) return null;

		double dblPV = 0.;
		boolean bFirstPeriod = true;
		double dblSpread = _dblCouponSpread;
		double dblTerminalNotional = java.lang.Double.NaN;

		try {
			if (sqs.containsCouponSpread()) dblSpread = sqs.couponSpread();

			if (sqs.containsPV()) dblPV = sqs.pv();
		} catch (java.lang.Exception e) {
			e.printStackTrace();

			return null;
		}

		org.drip.state.estimator.PredictorResponseWeightConstraint prwc = new
			org.drip.state.estimator.PredictorResponseWeightConstraint();

		for (org.drip.analytics.period.CashflowPeriod period : _lsCouponPeriod) {
			double dblPeriodEndDate = period.endDate();

			if (dblPeriodEndDate < dblValueDate) continue;

			double dblPeriodStartDate = period.startDate();

			double dblAccrued = 0.;

			try {
				double dblPeriodNotional = _dblInitialNotional * notionalFactor (dblPeriodEndDate);

				if (bFirstPeriod) {
					bFirstPeriod = false;
					double dblDFDate = dblPeriodStartDate > dblValueDate ? dblPeriodStartDate : dblValueDate;

					dblAccrued = period.accrualDCF (dblValueDate);

					if (!prwc.addPredictorResponseWeight (dblDFDate, dblPeriodNotional)) return null;

					if (!prwc.addDResponseWeightDManifestMeasure ("PV", dblDFDate, dblPeriodNotional))
						return null;
				}

				dblPV -= dblPeriodNotional * (period.couponDCF() - dblAccrued) * dblSpread;
			} catch (java.lang.Exception e) {
				e.printStackTrace();

				return null;
			}
		}

		try {
			dblTerminalNotional = _dblInitialNotional * notionalFactor (_dblMaturity);
		} catch (java.lang.Exception e) {
			e.printStackTrace();

			return null;
		}

		if (!prwc.addPredictorResponseWeight (_dblMaturity, -1. * dblTerminalNotional)) return null;

		if (!prwc.addDResponseWeightDManifestMeasure ("PV", _dblMaturity, -1. * dblTerminalNotional))
			return null;

		if (!prwc.updateValue (dblPV))return null; 

		if (!prwc.updateDValueDManifestMeasure ("PV", 1.))return null; 

		if (!prwc.addMergeLabel (_fri)) return null;

		return prwc;
	}

	private org.drip.analytics.output.PeriodCouponMeasures compoundFixing (
		final double dblAccrualEndDate,
		final org.drip.analytics.period.CashflowPeriod currentPeriod,
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs)
	{
		if (_fri.overnight())
			return _fri.isArithmeticCompounding() ? org.drip.analytics.support.CompoundingUtil.Arithmetic
				(dblAccrualEndDate, _fri, currentPeriod, valParams, csqs) :
					org.drip.analytics.support.CompoundingUtil.Geometric (dblAccrualEndDate, _fri,
						currentPeriod, csqs);

		double dblResetDate = currentPeriod.resetDate();

		if (!csqs.available (dblResetDate, _fri)) return null;

		try {
			return org.drip.analytics.output.PeriodCouponMeasures.Nominal (csqs.getFixing (dblResetDate,
				_fri));
		} catch (java.lang.Exception e) {
			e.printStackTrace();
		}

		return null;
	}

	/**
	 * FixedStream constructor
	 * 
	 * @param strName The Stream Name
	 * @param dblCouponSpread The Coupon/Spread Rate
	 * @param strCouponCurrency Cash Flow Coupon Currency
	 * @param strPayCurrency Cash Flow Pay Currency
	 * @param fri Floating Rate Index
	 * @param bFXMTMMode FX MTM Mode
	 * @param lsCouponPeriod List of the Coupon Periods
	 * @param dblInitialNotional The Initial Notional
	 * @param notlSchedule Notional Schedule
	 * 
	 * @throws java.lang.Exception Thrown if inputs are invalid
	 */

	public ConsolidatedStream (
		final java.lang.String strName,
		final double dblCouponSpread,
		final java.lang.String strCouponCurrency,
		final java.lang.String strPayCurrency,
		final org.drip.state.identifier.ForwardLabel fri,
		final boolean bFXMTMMode,
		final java.util.List<org.drip.analytics.period.CashflowPeriod> lsCouponPeriod,
		final double dblInitialNotional,
		final org.drip.product.params.FactorSchedule notlSchedule)
		throws java.lang.Exception
	{
		if (null == (_strName = strName) || _strName.isEmpty() || null == (_strCouponCurrency =
			strCouponCurrency) || _strCouponCurrency.isEmpty() || null == (_strPayCurrency = strPayCurrency)
				|| _strPayCurrency.isEmpty() || null == (_lsCouponPeriod = lsCouponPeriod) ||
					!org.drip.quant.common.NumberUtil.IsValid (_dblInitialNotional = dblInitialNotional) ||
						0. == _dblInitialNotional)
			throw new java.lang.Exception ("Stream ctr => Invalid Input params!");

		int iNumPeriod = _lsCouponPeriod.size();

		if (0 == iNumPeriod || !org.drip.quant.common.NumberUtil.IsValid (_dblCouponSpread =
			dblCouponSpread))
			throw new java.lang.Exception ("Stream ctr => Invalid Input params!");

		_fri = fri;
		_bFXMTMMode = bFXMTMMode;

		if (null == (_notlSchedule = notlSchedule))
			_notlSchedule = org.drip.product.params.FactorSchedule.CreateBulletSchedule();

		_dblEffective = _lsCouponPeriod.get (0).startDate();

		_dblMaturity = _lsCouponPeriod.get (iNumPeriod - 1).endDate();

		if (!_strPayCurrency.equalsIgnoreCase (_strCouponCurrency))
			_cp = org.drip.product.params.CurrencyPair.FromCode (_strPayCurrency + "/" + _strCouponCurrency);
	}

	/**
	 * Get the Notional Factor Relative on the the Initial Notional at the given date
	 * 
	 * @param dblDate Double date input
	 * 
	 * @return The Notional Factor Relative on the the Initial Notional
	 * 
	 * @throws java.lang.Exception Thrown if Notional Factorcannot be computed
	 */

	public double notionalFactor (
		final double dblDate)
		throws java.lang.Exception
	{
		if (null == _notlSchedule || !org.drip.quant.common.NumberUtil.IsValid (dblDate))
			throw new java.lang.Exception ("Stream::notionalFactor => Invalid Date");

		return _notlSchedule.getFactor (dblDate);
	}

	/**
	 * Get the Time-weighted Notional Factor Relative on the the Initial Notional between 2 dates
	 * 
	 * @param dblDate1 Double date first
	 * @param dblDate2 Double date second
	 * 
	 * @return The Time-weighted  Notional Factor Relative on the the Initial Notional
	 * 
	 * @throws java.lang.Exception Thrown if Notional Factor cannot be computed
	 */

	public double notionalFactor (
		final double dblDate1,
		final double dblDate2)
		throws java.lang.Exception
	{
		if (null == _notlSchedule || !org.drip.quant.common.NumberUtil.IsValid (dblDate1) ||
			!org.drip.quant.common.NumberUtil.IsValid (dblDate2))
			throw new java.lang.Exception ("Stream::notionalFactor => Invalid Date");

		return _notlSchedule.getFactor (dblDate1, dblDate2);
	}

	/**
	 * Return the Stream Name
	 * 
	 * @return The Stream Name
	 */

	public java.lang.String name()
	{
		return _strName;
	}

	/**
	 * Return the Coupon Currency
	 * 
	 * @return The Coupon Currency
	 */

	public java.lang.String couponCurrency()
	{
		return _strCouponCurrency;
	}

	/**
	 * Return the Pay Currency
	 * 
	 * @return The Pay Currency
	 */

	public java.lang.String payCurrency()
	{
		return _strPayCurrency;
	}

	/**
	 * Return the Initial Notional
	 * 
	 * @return The Initial Notional
	 */

	public double initialNotional()
	{
		return _dblInitialNotional;
	}

	/**
	 * Get the Coupon at the specified accrual date
	 * 
	 * @param dblAccrualEndDate Accrual End Date
	 * @param valParams The Valuation Parameters
	 * @param csqs Component Market Parameters
	 * 
	 * @return The Coupon Nominal/Adjusted Coupon Measures
	 */

	public org.drip.analytics.output.PeriodCouponMeasures floatingRate (
		final double dblAccrualEndDate,
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs)
	{
		if (!org.drip.quant.common.NumberUtil.IsValid (dblAccrualEndDate) || null == csqs) return null;

		org.drip.analytics.period.CashflowPeriod currentPeriod = null;

		if (dblAccrualEndDate <= _dblEffective)
			currentPeriod = _lsCouponPeriod.get (0);
		else {
			for (org.drip.analytics.period.CashflowPeriod period : _lsCouponPeriod) {
				if (null == period) continue;

				if (dblAccrualEndDate >= period.startDate() && dblAccrualEndDate <= period.endDate()) {
					currentPeriod = period;
					break;
				}
			}
		}

		if (null == currentPeriod) return null;

		org.drip.analytics.output.PeriodCouponMeasures pcm = compoundFixing (dblAccrualEndDate,
			currentPeriod, valParams, csqs);

		if (null != pcm) return pcm;

		org.drip.analytics.rates.ForwardRateEstimator fc = csqs.forwardCurve (_fri);

		double dblEndDate = currentPeriod.payDate();

		if (null != fc) {
			try {
				return org.drip.analytics.output.PeriodCouponMeasures.Nominal (fc.forward (dblEndDate));
			} catch (java.lang.Exception e) {
				e.printStackTrace();

				return null;
			}
		}

		org.drip.analytics.rates.DiscountCurve dcFunding = csqs.fundingCurve
			(org.drip.state.identifier.FundingLabel.Standard (_strPayCurrency));

		if (null == dcFunding) return null;

		double dblEpochDate = dcFunding.epoch().julian();

		double dblStartDate = currentPeriod.startDate();

		try {
			if (dblEpochDate > dblStartDate)
				dblEndDate = new org.drip.analytics.date.JulianDate (dblStartDate = dblEpochDate).addTenor
					(_fri.tenor()).julian();

			return org.drip.analytics.output.PeriodCouponMeasures.Nominal (dcFunding.libor (dblStartDate,
				dblEndDate, currentPeriod.couponDCF()));
		} catch (java.lang.Exception e) {
			e.printStackTrace();
		}

		return null;
	}

	/**
	 * Retrieve the Coupon Frequency
	 * 
	 * @return The Coupon Frequency
	 */

	public int freq()
	{
		return _lsCouponPeriod.get (0).freq();
	}

	/**
	 * Get the Effective Date
	 * 
	 * @return Effective Date
	 */

	public double effective()
	{
		return _dblEffective;
	}

	/**
	 * Get the Maturity Date
	 * 
	 * @return Maturity Date
	 */

	public double maturity()
	{
		return _dblMaturity;
	}

	/**
	 * Get the First Coupon Date
	 * 
	 * @return First Coupon Date
	 */

	public org.drip.analytics.date.JulianDate firstCouponDate()
	{
		try {
			return new org.drip.analytics.date.JulianDate (_lsCouponPeriod.get (0).payDate());
		} catch (java.lang.Exception e) {
			e.printStackTrace();
		}

		return null;
	}

	/**
	 * Get the Cash Flow Periods
	 * 
	 * @return List of the Cash Flow Periods
	 */

	public java.util.List<org.drip.analytics.period.CashflowPeriod> cashFlowPeriod()
	{
		return _lsCouponPeriod;
	}

	/**
	 * Return the FX MTM Mode
	 * 
	 * @return The FX MTM Mode
	 */

	public boolean fxMTMMode()
	{
		return _bFXMTMMode;
	}

	/**
	 * Generate a full list of the Product measures for the full input set of market parameters
	 * 
	 * @param valParams ValuationParams
	 * @param pricerParams PricerParams
	 * @param csqs Market Parameters
	 * @param quotingParams Quoting Parameters
	 * 
	 * @return Map of measure name and value
	 */

	public org.drip.analytics.support.CaseInsensitiveTreeMap<java.lang.Double> value (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.pricer.PricerParams pricerParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams vcp)
	{
		if (null == valParams || null == csqs) return null;

		org.drip.state.identifier.FundingLabel fundingLabel = org.drip.state.identifier.FundingLabel.Standard
			(_strPayCurrency);

		org.drip.analytics.rates.DiscountCurve dcFunding = csqs.fundingCurve (fundingLabel);

		if (null == dcFunding) return null;

		double dblValueDate = valParams.valueDate();

		long lStart = System.nanoTime();

		double dblAccrued01 = 0.;
		boolean bFirstPeriod = true;
		double dblUnadjustedDirtyDV01 = 0.;
		double dblQuantoAdjustedDirtyDV01 = 0.;
		double dblCashPayDF = java.lang.Double.NaN;
		double dblValueNotional = java.lang.Double.NaN;
		double dblAdjustedNotional = _dblInitialNotional;
		org.drip.quant.function1D.AbstractUnivariate auFX = null;

		org.drip.state.identifier.FXLabel fxLabel = null == _cp ? null :
			org.drip.state.identifier.FXLabel.Standard (_cp);

		if (null != fxLabel) auFX = csqs.fxCurve (fxLabel);

		try {
			dblAdjustedNotional *= (null != auFX && _bFXMTMMode ? auFX.evaluate (dblValueDate) : 1.);
		} catch (java.lang.Exception e) {
			e.printStackTrace();

			return null;
		}

		for (org.drip.analytics.period.CashflowPeriod period : _lsCouponPeriod) {
			double dblPeriodQuantoAdjustment = 1.;
			double dblPeriodUnadjustedDirtyDV01 = java.lang.Double.NaN;

			double dblPeriodAccrualStartDate = period.accrualStartDate();

			double dblPeriodPayDate = period.payDate();

			if (dblPeriodPayDate < dblValueDate) continue;

			try {
				if (bFirstPeriod) {
					bFirstPeriod = false;

					if (period.startDate() < dblValueDate)
						dblAccrued01 = period.accrualDCF (dblValueDate) * 0.0001 * notionalFactor
							(dblPeriodAccrualStartDate, dblValueDate);
				}

				if (_bFXMTMMode)
					dblPeriodQuantoAdjustment *= java.lang.Math.exp
						(org.drip.analytics.support.OptionHelper.IntegratedCrossVolQuanto
							(csqs.fundingCurveVolSurface (fundingLabel), csqs.fxCurveVolSurface (fxLabel),
								csqs.fundingFXCorrSurface (fundingLabel, fxLabel), dblValueDate,
									dblPeriodPayDate));

				dblPeriodUnadjustedDirtyDV01 = 0.0001 * period.couponDCF() * dcFunding.df (dblPeriodPayDate)
					* notionalFactor (dblPeriodAccrualStartDate, period.endDate());
			} catch (java.lang.Exception e) {
				e.printStackTrace();

				return null;
			}

			dblUnadjustedDirtyDV01 += dblPeriodUnadjustedDirtyDV01;
			dblQuantoAdjustedDirtyDV01 += dblPeriodUnadjustedDirtyDV01 * dblPeriodQuantoAdjustment;
		}

		try {
			dblCashPayDF = dcFunding.df (dblValueDate);
		} catch (java.lang.Exception e) {
			e.printStackTrace();

			return null;
		}

		dblAccrued01 *= dblAdjustedNotional;
		double dblAccrued = dblAccrued01 * 10000. * _dblCouponSpread;
		dblUnadjustedDirtyDV01 *= (dblAdjustedNotional / dblCashPayDF);
		dblQuantoAdjustedDirtyDV01 *= (dblAdjustedNotional / dblCashPayDF);
		double dblUnadjustedCleanDV01 = dblUnadjustedDirtyDV01 - dblAccrued01;
		double dblQuantoAdjustedCleanDV01 = dblQuantoAdjustedDirtyDV01 - dblAccrued01;
		double dblUnadjustedCleanPV = dblUnadjustedCleanDV01 * 10000. * _dblCouponSpread;
		double dblUnadjustedDirtyPV = dblUnadjustedDirtyDV01 * 10000. * _dblCouponSpread;
		double dblQuantoAdjustedCleanPV = dblQuantoAdjustedCleanDV01 * 10000. * _dblCouponSpread;
		double dblQuantoAdjustedDirtyPV = dblQuantoAdjustedDirtyDV01 * 10000. * _dblCouponSpread;

		org.drip.analytics.support.CaseInsensitiveTreeMap<java.lang.Double> mapResult = new
			org.drip.analytics.support.CaseInsensitiveTreeMap<java.lang.Double>();

		mapResult.put ("Accrued", dblAccrued);

		mapResult.put ("Accrued01", dblAccrued01);

		mapResult.put ("CleanDV01", dblQuantoAdjustedCleanDV01);

		mapResult.put ("CleanPV", dblQuantoAdjustedCleanPV);

		mapResult.put ("CV01", dblQuantoAdjustedCleanDV01);

		mapResult.put ("DirtyDV01", dblQuantoAdjustedDirtyDV01);

		mapResult.put ("DirtyPV", dblQuantoAdjustedDirtyPV);

		mapResult.put ("DV01", dblQuantoAdjustedCleanDV01);

		mapResult.put ("PV", dblQuantoAdjustedCleanPV);

		mapResult.put ("QuantoAdjustedCleanDV01", dblQuantoAdjustedCleanDV01);

		mapResult.put ("QuantoAdjustedCleanPV", dblQuantoAdjustedCleanPV);

		mapResult.put ("QuantoAdjustedDirtyDV01", dblQuantoAdjustedDirtyDV01);

		mapResult.put ("QuantoAdjustedDirtyPV", dblQuantoAdjustedDirtyPV);

		mapResult.put ("QuantoAdjustedDV01", dblQuantoAdjustedCleanDV01);

		mapResult.put ("QuantoAdjustedPV", dblQuantoAdjustedCleanPV);

		mapResult.put ("QuantoAdjustedUpfront", dblQuantoAdjustedCleanPV);

		mapResult.put ("QuantoAdjustmentFactor", dblQuantoAdjustedDirtyDV01 / dblUnadjustedDirtyDV01);

		mapResult.put ("QuantoAdjustmentPremium", (dblQuantoAdjustedCleanPV - dblUnadjustedCleanPV) /
			dblAdjustedNotional);

		mapResult.put ("UnadjustedCleanDV01", dblUnadjustedCleanDV01);

		mapResult.put ("UnadjustedCleanPV", dblUnadjustedCleanPV);

		mapResult.put ("UnadjustedDirtyDV01", dblUnadjustedDirtyDV01);

		mapResult.put ("UnadjustedDirtyPV", dblUnadjustedDirtyPV);

		mapResult.put ("UnadjustedDV01", dblUnadjustedCleanDV01);

		mapResult.put ("UnadjustedPV", dblUnadjustedCleanPV);

		mapResult.put ("UnadjustedUpfront", dblUnadjustedCleanPV);

		mapResult.put ("Upfront", dblQuantoAdjustedCleanPV);

		try {
			dblValueNotional = notionalFactor (dblValueDate) * (null != auFX && _bFXMTMMode ? auFX.evaluate
				(dblValueDate) : 1.);
		} catch (java.lang.Exception e) {
			e.printStackTrace();
		}

		if (org.drip.quant.common.NumberUtil.IsValid (dblValueNotional)) {
			double dblUnadjustedCleanPrice = 100. * (1. + (dblUnadjustedCleanPV / dblValueNotional));

			double dblUnadjustedDirtyPrice = 100. * (1. + (dblUnadjustedDirtyPV / dblValueNotional));

			double dblQuantoAdjustedCleanPrice = 100. * (1. + (dblQuantoAdjustedCleanPV / dblValueNotional));

			double dblQuantoAdjustedDirtyPrice = 100. * (1. + (dblQuantoAdjustedDirtyPV / dblValueNotional));

			mapResult.put ("CleanPrice", dblQuantoAdjustedCleanPrice);

			mapResult.put ("DirtyPrice", dblQuantoAdjustedDirtyPrice);

			mapResult.put ("Price", dblQuantoAdjustedCleanPrice);

			mapResult.put ("QuantoAdjustedCleanPrice", dblQuantoAdjustedCleanPrice);

			mapResult.put ("QuantoAdjustedDirtyPrice", dblQuantoAdjustedDirtyPrice);

			mapResult.put ("QuantoAdjustedPrice", dblQuantoAdjustedCleanPrice);

			mapResult.put ("UnadjustedCleanPrice", dblUnadjustedCleanPrice);

			mapResult.put ("UnadjustedDirtyPrice", dblUnadjustedDirtyPrice);

			mapResult.put ("UnadjustedPrice", dblUnadjustedCleanPrice);
		}

		mapResult.put ("CalcTime", (System.nanoTime() - lStart) * 1.e-09);

		return mapResult;
	}

	/**
	 * Retrieve the ordered set of the measure names whose values will be calculated
	 * 
	 * @return Set of Measure Names
	 */

	public java.util.Set<java.lang.String> measureNames()
	{
		java.util.Set<java.lang.String> setstrMeasureNames = new java.util.TreeSet<java.lang.String>();

		setstrMeasureNames.add ("Accrued");

		setstrMeasureNames.add ("Accrued01");

		setstrMeasureNames.add ("CalcTime");

		setstrMeasureNames.add ("CleanDV01");

		setstrMeasureNames.add ("CleanPrice");

		setstrMeasureNames.add ("CleanPV");

		setstrMeasureNames.add ("CV01");

		setstrMeasureNames.add ("DirtyDV01");

		setstrMeasureNames.add ("DirtyPrice");

		setstrMeasureNames.add ("DirtyPV");

		setstrMeasureNames.add ("DV01");

		setstrMeasureNames.add ("Price");

		setstrMeasureNames.add ("PV");

		setstrMeasureNames.add ("QuantoAdjustedCleanDV01");

		setstrMeasureNames.add ("QuantoAdjustedCleanPrice");

		setstrMeasureNames.add ("QuantoAdjustedCleanPV");

		setstrMeasureNames.add ("QuantoAdjustedDirtyDV01");

		setstrMeasureNames.add ("QuantoAdjustedDirtyPrice");

		setstrMeasureNames.add ("QuantoAdjustedDirtyPV");

		setstrMeasureNames.add ("QuantoAdjustedDV01");

		setstrMeasureNames.add ("QuantoAdjustedPrice");

		setstrMeasureNames.add ("QuantoAdjustedPV");

		setstrMeasureNames.add ("QuantoAdjustedUpfront");

		setstrMeasureNames.add ("QuantoAdjustmentFactor");

		setstrMeasureNames.add ("QuantoAdjustmentPremium");

		setstrMeasureNames.add ("UnadjustedCleanDV01");

		setstrMeasureNames.add ("UnadjustedCleanPrice");

		setstrMeasureNames.add ("UnadjustedCleanPV");

		setstrMeasureNames.add ("UnadjustedDirtyDV01");

		setstrMeasureNames.add ("UnadjustedDirtyPrice");

		setstrMeasureNames.add ("UnadjustedDirtyPV");

		setstrMeasureNames.add ("UnadjustedDV01");

		setstrMeasureNames.add ("UnadjustedPrice");

		setstrMeasureNames.add ("UnadjustedPV");

		setstrMeasureNames.add ("UnadjustedUpfront");

		setstrMeasureNames.add ("Upfront");

		return setstrMeasureNames;
	}

	/**
	 * Retrieve the Currency Pair for the Stream
	 * 
	 * @return The Currency Pair for the Stream
	 */

	public org.drip.product.params.CurrencyPair currencyPair()
	{
		return _strPayCurrency.equalsIgnoreCase (_strCouponCurrency) ? null :
			org.drip.product.params.CurrencyPair.FromCode (_strPayCurrency + "/" + _strCouponCurrency);
	}

	/**
	 * Generate the Calibration Linearized Predictor/Response Constraint Weights for the Non-merged Funding
	 * 	Curve Discount Factor Latent State from the Stream's Cash Flows. The Constraints here typically
	 *  correspond to Date/Cash Flow pairs and the corresponding leading PV.
	 * 
	 * @param valParams Valuation Parameters
	 * @param pricerParams Pricer Parameters
	 * @param csqs Market Parameters
	 * @param vcp Valuation Customization Parameters
	 * @param calibFundingLabel The Funding Latent State Label for which the Loadings need to be generated
	 * @param sqs The Stream Calibration Inputs
	 * 
	 * @return The Calibration Linearized Predictor/Response Constraints (Date/Cash Flow pairs and the
	 * 	corresponding PV)
	 */

	public org.drip.state.estimator.PredictorResponseWeightConstraint fundingPRWC (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.pricer.PricerParams pricerParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams vcp,
		final org.drip.state.identifier.FundingLabel calibFundingLabel,
		final org.drip.product.calib.StreamQuoteSet sqs)
	{
		if (null == valParams || null == sqs || null == calibFundingLabel || !calibFundingLabel.match
			(org.drip.state.identifier.FundingLabel.Standard (_strPayCurrency)) || (null != csqs && null !=
				csqs.fundingCurve (calibFundingLabel)))
			return null;

		if (null != _fri && null != csqs && null == csqs.forwardCurve (_fri))
			return telescopedPRWC (valParams, pricerParams, csqs, vcp, sqs);

		double dblValueDate = valParams.valueDate();

		if (dblValueDate >= _dblMaturity) return null;

		double dblPV = 0.;
		double dblBasis = 0.;
		double dblBaseCoupon = 0.;

		if (null == _fri)
			dblBaseCoupon = _dblCouponSpread;
		else
			dblBasis = _dblCouponSpread;

		try {
			if (sqs.containsPV()) dblPV = sqs.pv();

			if (sqs.containsCouponSpread()) {
				if (null == _fri)
					dblBaseCoupon = sqs.couponSpread();
				else
					dblBasis = sqs.couponSpread();
			}

			if (null != _fri && sqs.containsBasis()) dblBasis += sqs.basis();
		} catch (java.lang.Exception e) {
			e.printStackTrace();

			return null;
		}

		org.drip.state.estimator.PredictorResponseWeightConstraint prwc = new
			org.drip.state.estimator.PredictorResponseWeightConstraint();

		for (org.drip.analytics.period.CashflowPeriod period : _lsCouponPeriod) {
			double dblPeriodEndDate = period.endDate();

			if (dblPeriodEndDate < dblValueDate) continue;

			try {
				double dblAccrued = period.contains (dblValueDate) ? period.accrualDCF (dblValueDate) : 0.;

				if (null != _fri) {
					org.drip.analytics.output.PeriodCouponMeasures pcm = floatingRate (dblPeriodEndDate,
						valParams, csqs);

					if (null == pcm) return null;

					dblBaseCoupon = pcm.convexityAdjusted();
				}

				double dblPeriodCV100 = _dblInitialNotional * notionalFactor (dblPeriodEndDate) *
					(period.couponDCF() - dblAccrued) * (dblBaseCoupon + dblBasis);

				double dblPeriodPayDate = period.payDate();

				if (!prwc.addPredictorResponseWeight (dblPeriodPayDate, dblPeriodCV100)) return null;

				if (!prwc.addDResponseWeightDManifestMeasure ("PV", dblPeriodPayDate, dblPeriodCV100))
					return null;
			} catch (java.lang.Exception e) {
				e.printStackTrace();

				return null;
			}
		}

		if (!prwc.updateValue (dblPV)) return null;

		if (!prwc.updateDValueDManifestMeasure ("PV", 1.)) return null;

		return prwc;
	}

	/**
	 * Generate the Calibratable Linearized Predictor/Response Constraint Weights for the Non-merged Forward
	 *  Factor Latent State from the Component's Cash Flows. The Constraints here typically correspond to
	 *  Date/Cash Flow pairs and the corresponding leading PV.
	 * 
	 * @param valParams Valuation Parameters
	 * @param pricerParams Pricer Parameters
	 * @param csqs Component Market Parameters
	 * @param vcp Valuation Customization Parameters
	 * @param calibForwardLabel The Funding Latent State Label for which the Loadings need to be generated
	 * @param sqs The Stream Calibration Inputs
	 * 
	 * @return The Calibratable Linearized Predictor/Response Constraints (Date/Cash Flow pairs and the
	 * 	corresponding PV)
	 */

	public org.drip.state.estimator.PredictorResponseWeightConstraint forwardPRWC (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.pricer.PricerParams pricerParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams vcp,
		final org.drip.state.identifier.ForwardLabel calibForwardLabel,
		final org.drip.product.calib.StreamQuoteSet sqs)
	{
		if (null == valParams || null == calibForwardLabel || null == sqs || (null != csqs && null !=
			csqs.forwardCurve (calibForwardLabel)))
			return null;

		double dblValueDate = valParams.valueDate();

		if (dblValueDate >= _dblMaturity) return null;

		org.drip.analytics.rates.DiscountCurve dcFunding = csqs.fundingCurve
			(org.drip.state.identifier.FundingLabel.Standard (_strPayCurrency));

		if (null == dcFunding) return null;

		double dblPV = 0.;
		double dblBasis = null != _fri ? _dblCouponSpread : 0.;
		double dblBaseCoupon = null == _fri ? _dblCouponSpread : 0.;

		try {
			if (sqs.containsPV()) dblPV = sqs.pv();

			if (sqs.containsCouponSpread()) {
				if (null == _fri)
					dblBaseCoupon = sqs.couponSpread();
				else
					dblBasis = sqs.couponSpread();
			}

			if (sqs.containsBasis()) dblBasis += sqs.basis();
		} catch (java.lang.Exception e) {
			e.printStackTrace();

			return null;
		}

		org.drip.state.estimator.PredictorResponseWeightConstraint prwc = new
			org.drip.state.estimator.PredictorResponseWeightConstraint();

		for (org.drip.analytics.period.CashflowPeriod period : _lsCouponPeriod) {
			double dblPeriodEndDate = period.endDate();

			if (dblPeriodEndDate < dblValueDate) continue;

			try {
				double dblAccrued = period.contains (dblValueDate) ? period.accrualDCF (dblValueDate) : 0.;

				double dblPeriodPayDate = period.payDate();

				double dblPeriodDCDCF = _dblInitialNotional * notionalFactor (dblPeriodEndDate) *
					(period.couponDCF() - dblAccrued) * dcFunding.df (dblPeriodPayDate);

				double dblPeriodBaseCoupon = dblBaseCoupon;

				if (null != _fri) {
					if (!_fri.match (calibForwardLabel)) {
						org.drip.analytics.output.PeriodCouponMeasures pcm = floatingRate (dblPeriodEndDate,
							valParams, csqs);

						if (null == pcm) return null;

						dblPeriodBaseCoupon = pcm.convexityAdjusted();
					} else {
						if (!prwc.addPredictorResponseWeight (dblPeriodPayDate, dblPeriodDCDCF)) return null;

						if (!prwc.addDResponseWeightDManifestMeasure ("PV", dblPeriodPayDate,
							dblPeriodDCDCF))
							return null;
					}
				}

				dblPV -= dblPeriodDCDCF * (dblPeriodBaseCoupon + dblBasis);
			} catch (java.lang.Exception e) {
				e.printStackTrace();

				return null;
			}
		}

		if (!prwc.updateValue (dblPV)) return null;

		if (!prwc.updateDValueDManifestMeasure ("PV", 1.)) return null;

		return prwc;
	}

	/**
	 * Retrieve the Floating Rate Index
	 * 
	 * @return The Floating Rate Index
	 */

	public org.drip.state.identifier.ForwardLabel fri()
	{
		return _fri;
	}
}
