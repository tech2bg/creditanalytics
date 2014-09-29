
package org.drip.product.rates;

/*
 * -*- mode: java; tab-width: 4; indent-tabs-mode: nil; c-basic-offset: 4 -*-
 */

/*!
 * Copyright (C) 2014 Lakshmi Krishnamurthy
 * Copyright (C) 2013 Lakshmi Krishnamurthy
 * Copyright (C) 2012 Lakshmi Krishnamurthy
 * Copyright (C) 2011 Lakshmi Krishnamurthy
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
 * DepositComponent contains the implementation of the Deposit IR product and its contract/valuation details.
 * 	It exports the following functionality:
 *  - Standard/Custom Constructor for the Deposit Component
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

public class DepositComponent extends org.drip.product.definition.CalibratableFixedIncomeComponent {
	private double _dblNotional = 100.;
	private java.lang.String _strCode = "";
	private java.lang.String _strCalendar = "";
	private java.lang.String _strCurrency = "";
	private java.lang.String _strDayCount = "Act/360";
	private double _dblMaturity = java.lang.Double.NaN;
	private double _dblEffective = java.lang.Double.NaN;
	private org.drip.state.identifier.ForwardLabel _fri = null;
	private org.drip.param.valuation.CashSettleParams _settleParams = null;

	private org.drip.state.estimator.PredictorResponseWeightConstraint discountFactorPRWC (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.pricer.PricerParams pricerParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams quotingParams,
		final org.drip.product.calib.ProductQuoteSet pqs)
	{
		double dblValueDate = valParams.valueDate();

		if (dblValueDate > _dblEffective) return null;

		org.drip.product.calib.DepositComponentQuoteSet dcqs =
			(org.drip.product.calib.DepositComponentQuoteSet) pqs;

		if (!dcqs.containsPV() && !dcqs.containsRate()) return null;

		double dblForwardDF = java.lang.Double.NaN;

		try {
			if (dcqs.containsPV())
				dblForwardDF = dcqs.pv();
			else if (dcqs.containsRate())
				dblForwardDF = 1. / (1. + org.drip.analytics.daycount.Convention.YearFraction (_dblEffective,
					_dblMaturity, _strDayCount, false, null, _strCalendar) * dcqs.rate());
		} catch (java.lang.Exception e) {
			e.printStackTrace();

			return null;
		}

		org.drip.state.estimator.PredictorResponseWeightConstraint prwc = new
			org.drip.state.estimator.PredictorResponseWeightConstraint();

		if (!prwc.addPredictorResponseWeight (_dblEffective, _dblNotional * dblForwardDF)) return null;

		if (!prwc.addDResponseWeightDManifestMeasure ("PV", _dblEffective, _dblNotional * dblForwardDF))
			return null;

		if (!prwc.addPredictorResponseWeight (_dblMaturity, -1. * _dblNotional)) return null;

		if (!prwc.addDResponseWeightDManifestMeasure ("PV", _dblMaturity, -1. * _dblNotional)) return null;

		if (!prwc.updateValue (0.)) return null;

		if (!prwc.updateDValueDManifestMeasure ("PV", 1.)) return null;

		return prwc;
	}

	/**
	 * Construct a DepositComponent instance
	 * 
	 * @param dtEffective Effective Date
	 * @param dtMaturity Maturity Date
	 * @param strCurrency Pay Currency
	 * @param strDayCount Day Count
	 * @param strCalendar Calendar
	 * 
	 * @throws java.lang.Exception Thrown if the inputs are invalid
	 */

	public DepositComponent (
		final org.drip.analytics.date.JulianDate dtEffective,
		final org.drip.analytics.date.JulianDate dtMaturity,
		final org.drip.state.identifier.ForwardLabel fri,
		final java.lang.String strCurrency,
		final java.lang.String strDayCount,
		final java.lang.String strCalendar)
		throws java.lang.Exception
	{
		if (null == dtEffective || null == dtMaturity || null == (_strCurrency = strCurrency) ||
			_strCurrency.isEmpty() || (_dblMaturity = dtMaturity.julian()) <= (_dblEffective =
				dtEffective.julian()))
			throw new java.lang.Exception ("DepositComponent ctr: Invalid Inputs!");

		_fri = fri;
		_strCalendar = strCalendar;
		_strDayCount = strDayCount;
	}

	@Override protected org.drip.analytics.support.CaseInsensitiveTreeMap<java.lang.Double> calibMeasures (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.pricer.PricerParams pricerParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams quotingParams)
	{
		return null;
	}

	@Override public java.lang.String primaryCode()
	{
		return _strCode;
	}

	@Override public void setPrimaryCode (
		final java.lang.String strCode)
	{
		_strCode = strCode;
	}

	@Override public java.lang.String name()
	{
		return "CD=" + org.drip.analytics.date.JulianDate.fromJulian (_dblMaturity);
	}

	@Override public java.util.Set<java.lang.String> cashflowCurrencySet()
	{
		java.util.Set<java.lang.String> setCcy = new java.util.HashSet<java.lang.String>();

		setCcy.add (_strCurrency);

		return setCcy;
	}

	@Override public java.lang.String[] payCurrency()
	{
		return new java.lang.String[] {_strCurrency};
	}

	@Override public java.lang.String[] principalCurrency()
	{
		return new java.lang.String[] {_strCurrency};
	}

	@Override public double initialNotional()
	{
		return _dblNotional;
	}

	@Override public double notional (
		final double dblDate)
		throws java.lang.Exception
	{
		if (!org.drip.quant.common.NumberUtil.IsValid (dblDate))
			throw new java.lang.Exception ("DepositComponent::notional => Bad date into getNotional");

		return 1.;
	}

	@Override public double notional (
		final double dblDate1,
		final double dblDate2)
		throws java.lang.Exception
	{
		if (!org.drip.quant.common.NumberUtil.IsValid (dblDate1) || !org.drip.quant.common.NumberUtil.IsValid
			(dblDate2))
			throw new java.lang.Exception ("DepositComponent::notional => Bad date into getNotional");

		return 1.;
	}

	@Override public org.drip.analytics.output.GenericCouponPeriodMetrics coupon (
		final double dblAccrualEndDate,
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs)
	{
		try {
			java.util.List<org.drip.analytics.output.ResetPeriodMetrics> lsRPM = new
				java.util.ArrayList<org.drip.analytics.output.ResetPeriodMetrics>();

			lsRPM.add (new org.drip.analytics.output.ResetPeriodMetrics (_dblEffective, _dblMaturity,
				_dblEffective, 0., org.drip.analytics.daycount.Convention.YearFraction (_dblEffective,
					_dblMaturity, _strDayCount, false, null, _strCalendar)));

			return org.drip.analytics.output.GenericCouponPeriodMetrics.Create (_dblEffective, _dblMaturity,
				_dblMaturity, notional (_dblMaturity),
					org.drip.analytics.support.ResetUtil.ACCRUAL_COMPOUNDING_RULE_ARITHMETIC, lsRPM, 1., 1.,
						1., null, null, _fri, org.drip.state.identifier.FundingLabel.Standard (_strCurrency),
							null);
		} catch (java.lang.Exception e) {
			e.printStackTrace();
		}

		return null;
	}

	@Override public int freq()
	{
		return cashFlowPeriod().get (0).freq();
	}

	@Override public org.drip.state.identifier.CreditLabel[] creditLabel()
	{
		return null;
	}

	@Override public org.drip.state.identifier.ForwardLabel[] forwardLabel()
	{
		return null == _fri ? null : new org.drip.state.identifier.ForwardLabel[] {_fri};
	}

	@Override public org.drip.state.identifier.FundingLabel[] fundingLabel()
	{
		return new org.drip.state.identifier.FundingLabel[] {org.drip.state.identifier.FundingLabel.Standard
			(_strCurrency)};
	}

	@Override public org.drip.state.identifier.FXLabel[] fxLabel()
	{
		return null;
	}

	@Override public org.drip.analytics.date.JulianDate effective()
	{
		try {
			return new org.drip.analytics.date.JulianDate (_dblEffective);
		} catch (java.lang.Exception e) {
			e.printStackTrace();
		}

		return null;
	}

	@Override public org.drip.analytics.date.JulianDate maturity()
	{
		try {
			return new org.drip.analytics.date.JulianDate (_dblMaturity);
		} catch (java.lang.Exception e) {
			e.printStackTrace();
		}

		return null;
	}

	@Override public org.drip.analytics.date.JulianDate firstCouponDate()
	{
		return null;
	}

	@Override public java.util.List<org.drip.analytics.cashflow.GenericCouponPeriod> cashFlowPeriod()
	{
		return org.drip.analytics.support.PeriodBuilder.SinglePeriodSingleReset (_dblEffective, _dblMaturity,
			java.lang.Double.NaN, _strDayCount, _strCalendar, _dblNotional, null, 0., _strCurrency,
				_strCurrency, _fri, null);
	}

	@Override public org.drip.param.valuation.CashSettleParams cashSettleParams()
	{
		return _settleParams;
	}

	@Override public org.drip.analytics.support.CaseInsensitiveTreeMap<java.lang.Double> value (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.pricer.PricerParams pricerParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams quotingParams)
	{
		if (null == valParams || valParams.valueDate() >= _dblMaturity || null == csqs) return null;

		long lStart = System.nanoTime();

		org.drip.analytics.support.CaseInsensitiveTreeMap<java.lang.Double> mapResult = new
			org.drip.analytics.support.CaseInsensitiveTreeMap<java.lang.Double>();

		org.drip.analytics.rates.ForwardCurve fc = csqs.forwardCurve (_fri);

		if (null != fc && null != _fri && fc.label().match (_fri)) {
			try {
				double dblForwardRate = fc.forward (_dblMaturity);

				mapResult.put ("forward", dblForwardRate);

				mapResult.put ("forwardrate", dblForwardRate);
			} catch (java.lang.Exception e) {
				e.printStackTrace();
			}
		}

		org.drip.analytics.rates.DiscountCurve dc = csqs.fundingCurve (fundingLabel()[0]);

		if (null == dc) {
			mapResult.put ("calctime", (System.nanoTime() - lStart) * 1.e-09);

			return mapResult;
		}

		try {
			double dblCashSettle = null == _settleParams ? valParams.cashPayDate() :
				_settleParams.cashSettleDate (valParams.valueDate());

			double dblUnadjustedAnnuity = dc.df (_dblMaturity) / dc.df (_dblEffective) / dc.df
				(dblCashSettle);

			double dblAdjustedAnnuity = dblUnadjustedAnnuity / dc.df (dblCashSettle);

			mapResult.put ("pv", dblAdjustedAnnuity * _dblNotional * 0.01 * notional (_dblEffective,
				_dblMaturity));

			mapResult.put ("price", 100. * dblAdjustedAnnuity);

			mapResult.put ("rate", ((1. / dblUnadjustedAnnuity) - 1.) /
				org.drip.analytics.daycount.Convention.YearFraction (_dblEffective, _dblMaturity,
					_strDayCount, false, null, _strCalendar));
		} catch (java.lang.Exception e) {
			e.printStackTrace();
		}

		mapResult.put ("calctime", (System.nanoTime() - lStart) * 1.e-09);

		return mapResult;
	}

	@Override public java.util.Set<java.lang.String> measureNames()
	{
		java.util.Set<java.lang.String> setstrMeasureNames = new java.util.TreeSet<java.lang.String>();

		setstrMeasureNames.add ("CalcTime");

		setstrMeasureNames.add ("Forward");

		setstrMeasureNames.add ("ForwardRate");

		setstrMeasureNames.add ("Price");

		setstrMeasureNames.add ("PV");

		setstrMeasureNames.add ("Rate");

		return setstrMeasureNames;
	}

	@Override public org.drip.quant.calculus.WengertJacobian jackDDirtyPVDManifestMeasure (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.pricer.PricerParams pricerParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams quotingParams)
	{
		if (null == valParams || valParams.valueDate() >= _dblMaturity || null == csqs) return null;

		org.drip.analytics.rates.DiscountCurve dcFunding = csqs.fundingCurve (fundingLabel()[0]);

		if (null == dcFunding) return null;

		try {
			org.drip.analytics.support.CaseInsensitiveTreeMap<java.lang.Double> mapMeasures = value
				(valParams, pricerParams, csqs, quotingParams);

			if (null == mapMeasures) return null;

			org.drip.quant.calculus.WengertJacobian wjDFDF = dcFunding.jackDDFDManifestMeasure (_dblMaturity,
				"Rate");

			if (null == wjDFDF) return null;

			org.drip.quant.calculus.WengertJacobian wjPVDFMicroJack = new
				org.drip.quant.calculus.WengertJacobian (1, wjDFDF.numParameters());

			for (int k = 0; k < wjDFDF.numParameters(); ++k) {
				if (!wjPVDFMicroJack.accumulatePartialFirstDerivative (0, k, wjDFDF.getFirstDerivative (0,
					k)))
					return null;
			}

			return wjPVDFMicroJack;
		} catch (java.lang.Exception e) {
			e.printStackTrace();
		}

		return null;
	}

	@Override public org.drip.quant.calculus.WengertJacobian manifestMeasureDFMicroJack (
		final java.lang.String strManifestMeasure,
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.pricer.PricerParams pricerParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams quotingParams)
	{
		if (null == valParams || valParams.valueDate() >= _dblMaturity || null == strManifestMeasure)
			return null;

		org.drip.analytics.rates.DiscountCurve dcFunding = csqs.fundingCurve (fundingLabel()[0]);

		if (null == dcFunding) return null;

		if ("Rate".equalsIgnoreCase (strManifestMeasure)) {
			try {
				org.drip.quant.calculus.WengertJacobian wjDF = dcFunding.jackDDFDManifestMeasure
					(_dblMaturity, "Rate");

				if (null == wjDF) return null;

				org.drip.quant.calculus.WengertJacobian wjDFMicroJack = new
					org.drip.quant.calculus.WengertJacobian (1, wjDF.numParameters());

				for (int k = 0; k < wjDF.numParameters(); ++k) {
					if (!wjDFMicroJack.accumulatePartialFirstDerivative (0, k, -365.25 / (_dblMaturity -
						_dblEffective) / dcFunding.df (_dblMaturity) * wjDF.getFirstDerivative (0, k)))
						return null;
				}

				return wjDFMicroJack;
			} catch (java.lang.Exception e) {
				e.printStackTrace();
			}
		}

		return null;
	}

	@Override public org.drip.product.calib.ProductQuoteSet calibQuoteSet (
		final org.drip.state.representation.LatentStateSpecification[] aLSS)
	{
		try {
			return new org.drip.product.calib.DepositComponentQuoteSet (aLSS);
		} catch (java.lang.Exception e) {
			e.printStackTrace();
		}

		return null;
	}

	@Override public org.drip.state.estimator.PredictorResponseWeightConstraint fundingPRWC (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.pricer.PricerParams pricerParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams quotingParams,
		final org.drip.product.calib.ProductQuoteSet pqs)
	{
		return null == valParams || null == pqs || !(pqs instanceof
			org.drip.product.calib.DepositComponentQuoteSet) || !pqs.contains
				(org.drip.analytics.definition.LatentStateStatic.LATENT_STATE_FUNDING,
					org.drip.analytics.definition.LatentStateStatic.DISCOUNT_QM_DISCOUNT_FACTOR,
						org.drip.state.identifier.FundingLabel.Standard (_strCurrency)) ? null :
							discountFactorPRWC (valParams, pricerParams, csqs, quotingParams, pqs);
	}

	@Override public org.drip.state.estimator.PredictorResponseWeightConstraint forwardPRWC (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.pricer.PricerParams pricerParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams quotingParams,
		final org.drip.product.calib.ProductQuoteSet pqs)
	{
		if (null == _fri || null == valParams || null == pqs || !(pqs instanceof
			org.drip.product.calib.DepositComponentQuoteSet) || !pqs.contains
				(org.drip.analytics.definition.LatentStateStatic.LATENT_STATE_FORWARD,
					org.drip.analytics.definition.LatentStateStatic.FORWARD_QM_FORWARD_RATE, _fri))
			return null;

		double dblValueDate = valParams.valueDate();

		if (dblValueDate > _dblEffective) return null;

		org.drip.product.calib.DepositComponentQuoteSet dcqs =
			(org.drip.product.calib.DepositComponentQuoteSet) pqs;

		if (!dcqs.containsPV() && !dcqs.containsRate() && !dcqs.containsForwardRate()) return null;

		double dblForwardRate = java.lang.Double.NaN;

		try {
			if (dcqs.containsRate())
				dblForwardRate = dcqs.rate();
			else if (dcqs.containsForwardRate())
				dblForwardRate = dcqs.forwardRate();
			else if (dcqs.containsPV())
				dblForwardRate = ((1. / dcqs.pv()) - 1.) /
					org.drip.analytics.daycount.Convention.YearFraction (_dblEffective, _dblMaturity,
						_strDayCount, false, null, _strCalendar);
		} catch (java.lang.Exception e) {
			e.printStackTrace();

			return null;
		}

		org.drip.state.estimator.PredictorResponseWeightConstraint prwc = new
			org.drip.state.estimator.PredictorResponseWeightConstraint();

		if (!prwc.addPredictorResponseWeight (_dblMaturity, 1.)) return null;

		if (!prwc.addDResponseWeightDManifestMeasure ("Rate", _dblMaturity, 1.)) return null;

		if (!prwc.updateValue (dblForwardRate)) return null;

		if (!prwc.updateDValueDManifestMeasure ("Rate", 1.)) return null;

		return prwc;
	}

	@Override public org.drip.state.estimator.PredictorResponseWeightConstraint fundingForwardPRWC (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.pricer.PricerParams pricerParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams quotingParams,
		final org.drip.product.calib.ProductQuoteSet pqs)
	{
		if (null == valParams || null == pqs || !(pqs instanceof
			org.drip.product.calib.DepositComponentQuoteSet) || !pqs.contains
				(org.drip.analytics.definition.LatentStateStatic.LATENT_STATE_FUNDING,
					org.drip.analytics.definition.LatentStateStatic.DISCOUNT_QM_DISCOUNT_FACTOR,
						org.drip.state.identifier.FundingLabel.Standard (_strCurrency)))
			return null;

		org.drip.state.estimator.PredictorResponseWeightConstraint prwc = discountFactorPRWC (valParams,
			pricerParams, csqs, quotingParams, pqs);

		if (null == prwc) return null;

		if (null != _fri && !prwc.addMergeLabel (_fri)) return null;

		return prwc;
	}
}
