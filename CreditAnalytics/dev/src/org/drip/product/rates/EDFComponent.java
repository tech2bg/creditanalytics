
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
 * EDFComponent contains the implementation of the Euro-dollar future contract/valuation (EDF). It exports
 *  the following functionality:
 *  - Standard/Custom Constructor for the EDFComponent
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

public class EDFComponent extends org.drip.product.definition.CalibratableFixedIncomeComponent {
	private double _dblNotional = 100.;
	private java.lang.String _strEDCode = "";
	private java.lang.String _strCurrency = "";
	private java.lang.String _strDC = "Act/360";
	private java.lang.String _strCalendar = "USD";
	private double _dblMaturity = java.lang.Double.NaN;
	private double _dblEffective = java.lang.Double.NaN;
	private org.drip.state.identifier.ForwardLabel _fri = null;
	private org.drip.product.params.FactorSchedule _notlSchedule = null;
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

		org.drip.product.calib.EDFComponentQuoteSet ecqs = (org.drip.product.calib.EDFComponentQuoteSet) pqs;

		if (!ecqs.containsPrice() && !ecqs.containsRate()) return null;

		double dblForwardDF = java.lang.Double.NaN;

		try {
			if (ecqs.containsPrice())
				dblForwardDF = ecqs.price();
			else if (ecqs.containsRate())
				dblForwardDF = 1. / (1. + org.drip.analytics.daycount.Convention.YearFraction (_dblEffective,
					_dblMaturity, _strDC, false, null, _strCalendar) * ecqs.rate());
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

	@Override protected org.drip.analytics.support.CaseInsensitiveTreeMap<java.lang.Double> calibMeasures (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.pricer.PricerParams pricerParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams quotingParams)
	{
		return null;
	}

	/**
	 * Construct an EDFComponent Instance
	 * 
	 * @param dtEffective Effective Date
	 * @param dtMaturity Maturity Date
	 * @param fri The Forward Latent State Label (i.e., the Floating Rate Index)
	 * @param strCurrency The Currency
	 * @param strDC Day Count
	 * @param strCalendar Calendar
	 * 
	 * @throws java.lang.Exception Thrown if the inputs are invalid
	 */

	public EDFComponent (
		final org.drip.analytics.date.JulianDate dtEffective,
		final org.drip.analytics.date.JulianDate dtMaturity,
		final org.drip.state.identifier.ForwardLabel fri,
		final java.lang.String strCurrency,
		final java.lang.String strDC,
		final java.lang.String strCalendar)
		throws java.lang.Exception
	{
		if (null == dtEffective || null == dtMaturity || null == (_strCurrency = strCurrency) ||
			_strCurrency.isEmpty() || null == (_fri = fri) || (_dblMaturity = dtMaturity.julian()) <=
				(_dblEffective = dtEffective.julian()))
			throw new java.lang.Exception ("EDFComponent ctr:: Invalid Params!");

		_strDC = strDC;
		_strCalendar = strCalendar;

		_notlSchedule = org.drip.product.params.FactorSchedule.CreateBulletSchedule();
	}

	/**
	 * Construct an EDFComponent Component
	 * 
	 * @param strFullEDCode EDF Component Code
	 * @param dt Start Date
	 * @param strCurrency EDF Currency
	 * 
	 * @throws java.lang.Exception Thrown if the inputs are invalid
	 */

	public EDFComponent (
		final java.lang.String strFullEDCode,
		final org.drip.analytics.date.JulianDate dt,
		final java.lang.String strCurrency)
		throws java.lang.Exception
	{
		if (null == dt || null == (_strCurrency = strCurrency) || _strCurrency.isEmpty())
			throw new java.lang.Exception ("EDFComponent ctr:: Invalid Params!");

		java.lang.String strEDCode = strFullEDCode;

		if (4 != strEDCode.length() || !strEDCode.toUpperCase().startsWith ("ED"))
			 throw new java.lang.Exception ("EDFComponent ctr:: Unknown EDF Code " + strEDCode);

		_strCalendar = _strCurrency;
		int iEffectiveMonth = -1;

		int iYearDigit = new java.lang.Integer ("" + strEDCode.charAt (3)).intValue();

		if (10 <= iYearDigit)
			throw new java.lang.Exception ("EDFComponent ctr:: Invalid ED year in " + strEDCode);

		char chMonth = strEDCode.charAt (2);

		if ('H' == chMonth)
			 iEffectiveMonth = org.drip.analytics.date.JulianDate.MARCH;
		else if ('M' == chMonth)
			 iEffectiveMonth = org.drip.analytics.date.JulianDate.JUNE;
		else if ('U' == chMonth)
			iEffectiveMonth = org.drip.analytics.date.JulianDate.SEPTEMBER;
		else if ('Z' == chMonth)
			 iEffectiveMonth = org.drip.analytics.date.JulianDate.DECEMBER;
		else
			 throw new java.lang.Exception ("EDFComponent ctr:: Unknown Month in " + strEDCode);

		org.drip.analytics.date.JulianDate dtEDEffective = dt.firstEDFStartDate (3);

		while (iYearDigit != (org.drip.analytics.date.JulianDate.Year (dtEDEffective.julian()) % 10))
			 dtEDEffective = dtEDEffective.addYears (1);

		org.drip.analytics.date.JulianDate dtEffective = org.drip.analytics.date.JulianDate.CreateFromYMD
			 (org.drip.analytics.date.JulianDate.Year (dtEDEffective.julian()), iEffectiveMonth, 15);

		_dblEffective = dtEffective.julian();

		_dblMaturity = dtEffective.addMonths (3).julian();

		_notlSchedule = org.drip.product.params.FactorSchedule.CreateBulletSchedule();

		_fri = org.drip.state.identifier.ForwardLabel.Create (strCurrency, "LIBOR", "3M");
	}

	@Override public java.lang.String primaryCode()
	{
		return _strEDCode + "." + _strCurrency;
	}

	@Override public void setPrimaryCode (
		final java.lang.String strCode)
	{
		_strEDCode = strCode;
	}

	@Override public java.lang.String[] secondaryCode()
	{
		java.lang.String strPrimaryCode = primaryCode();

		int iNumTokens = 0;
		java.lang.String astrCodeTokens[] = new java.lang.String[2];

		java.util.StringTokenizer stCodeTokens = new java.util.StringTokenizer (strPrimaryCode, ".");

		while (stCodeTokens.hasMoreTokens())
			astrCodeTokens[iNumTokens++] = stCodeTokens.nextToken();

		return new java.lang.String[] {astrCodeTokens[0]};
	}

	@Override public java.lang.String name()
	{
		return _strEDCode;
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
		if (null == _notlSchedule || !org.drip.quant.common.NumberUtil.IsValid (dblDate))
			throw new java.lang.Exception ("EDFComponent::notional => Got NaN date");

		return _notlSchedule.getFactor (dblDate);
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
					_dblMaturity, _strDC, false, null, _strCalendar)));

			return org.drip.analytics.output.GenericCouponPeriodMetrics.Create (_dblEffective, _dblMaturity,
				_dblMaturity, notional (_dblMaturity),
					org.drip.analytics.support.CompositePeriodUtil.ACCRUAL_COMPOUNDING_RULE_ARITHMETIC, lsRPM, 1., 1.,
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

	@Override public double notional (
		final double dblDate1,
		final double dblDate2)
		throws java.lang.Exception
	{
		if (null == _notlSchedule || !org.drip.quant.common.NumberUtil.IsValid (dblDate1) ||
			!org.drip.quant.common.NumberUtil.IsValid (dblDate2))
			throw new java.lang.Exception ("EDFComponent::notional => Got NaN date");

		return _notlSchedule.getFactor (dblDate1, dblDate2);
	}

	@Override public org.drip.state.identifier.CreditLabel[] creditLabel()
	{
		return null;
	}

	@Override public org.drip.state.identifier.ForwardLabel[] forwardLabel()
	{
		return new org.drip.state.identifier.ForwardLabel[] {_fri};
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
			java.lang.Double.NaN, _strDC, _strCalendar, _dblNotional, null, 0., _strCurrency, _strCurrency,
				_fri, null);
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
		if (null == valParams || null == csqs || valParams.valueDate() >= _dblMaturity) return null;

		org.drip.analytics.rates.DiscountCurve dcFunding = csqs.fundingCurve (fundingLabel()[0]);

		if (null == dcFunding) return null;

		long lStart = System.nanoTime();

		org.drip.analytics.support.CaseInsensitiveTreeMap<java.lang.Double> mapResult = new
			org.drip.analytics.support.CaseInsensitiveTreeMap<java.lang.Double>();

		try {
			double dblCashSettle = null == _settleParams ? valParams.cashPayDate() :
				_settleParams.cashSettleDate (valParams.valueDate());

			double dblUnadjustedAnnuity = dcFunding.df (_dblMaturity) / dcFunding.df (_dblEffective) /
				dcFunding.df (dblCashSettle);

			double dblAdjustedAnnuity = dblUnadjustedAnnuity / dcFunding.df (dblCashSettle);

			mapResult.put ("PV", dblAdjustedAnnuity * _dblNotional * 0.01 * notional (_dblEffective,
				_dblMaturity));

			mapResult.put ("Price", 100. * dblAdjustedAnnuity);

			mapResult.put ("Rate", ((1. / dblUnadjustedAnnuity) - 1.) /
				org.drip.analytics.daycount.Convention.YearFraction (_dblEffective, _dblMaturity, _strDC,
					false, null, _strCalendar));
		} catch (java.lang.Exception e) {
			e.printStackTrace();
		}

		mapResult.put ("CalcTime", (System.nanoTime() - lStart) * 1.e-09);

		return mapResult;
	}

	@Override public java.util.Set<java.lang.String> measureNames()
	{
		java.util.Set<java.lang.String> setstrMeasureNames = new java.util.TreeSet<java.lang.String>();

		setstrMeasureNames.add ("CalcTime");

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
		if (null == valParams || valParams.valueDate() >= maturity().julian() || null == csqs)
			return null;

		org.drip.analytics.rates.DiscountCurve dcFunding = csqs.fundingCurve (fundingLabel()[0]);

		if (null == dcFunding) return null;

		try {
			org.drip.analytics.support.CaseInsensitiveTreeMap<java.lang.Double> mapMeasures = value
				(valParams, pricerParams, csqs, quotingParams);

			if (null == mapMeasures) return null;

			double dblDFEffective = dcFunding.df (_dblEffective);

			double dblDFMaturity = dcFunding.df (maturity().julian());

			org.drip.quant.calculus.WengertJacobian wjDFEffective = dcFunding.jackDDFDManifestMeasure
				(_dblEffective, "Rate");

			org.drip.quant.calculus.WengertJacobian wjDFMaturity = dcFunding.jackDDFDManifestMeasure
				(maturity().julian(), "Rate");

			if (null == wjDFEffective || null == wjDFMaturity) return null;

			org.drip.quant.calculus.WengertJacobian wjPVDFMicroJack = new
				org.drip.quant.calculus.WengertJacobian (1, wjDFMaturity.numParameters());

			for (int i = 0; i < wjDFMaturity.numParameters(); ++i) {
				if (!wjPVDFMicroJack.accumulatePartialFirstDerivative (0, i, wjDFMaturity.getFirstDerivative
					(0, i) / dblDFEffective))
					return null;

				if (!wjPVDFMicroJack.accumulatePartialFirstDerivative (0, i,
					-wjDFEffective.getFirstDerivative (0, i) * dblDFMaturity / dblDFEffective /
						dblDFEffective))
					return null;
			}

			return wjPVDFMicroJack;

			/* return adjustPVDFMicroJackForCashSettle (valParams.cashPayDate(), dblPV, dc, wjPVDFMicroJack) ?
				wjPVDFMicroJack : null; */
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
		if (null == valParams || valParams.valueDate() >= maturity().julian() || null ==
			strManifestMeasure || strManifestMeasure.isEmpty() || null == csqs)
			return null;

		org.drip.analytics.rates.DiscountCurve dcFunding = csqs.fundingCurve (fundingLabel()[0]);

		if (null == dcFunding) return null;

		if ("Rate".equalsIgnoreCase (strManifestMeasure)) {
			try {
				double dblDFEffective = dcFunding.df (_dblEffective);

				double dblDFMaturity = dcFunding.df (maturity().julian());

				org.drip.quant.calculus.WengertJacobian wjDFEffective = dcFunding.jackDDFDManifestMeasure
					(_dblEffective, "Rate");

				org.drip.quant.calculus.WengertJacobian wjDFMaturity = dcFunding.jackDDFDManifestMeasure
					(maturity().julian(), "Rate");

				if (null == wjDFEffective || null == wjDFMaturity) return null;

				org.drip.quant.calculus.WengertJacobian wjDFMicroJack = new
					org.drip.quant.calculus.WengertJacobian (1, wjDFMaturity.numParameters());

				for (int i = 0; i < wjDFMaturity.numParameters(); ++i) {
					if (!wjDFMicroJack.accumulatePartialFirstDerivative (0, i,
						wjDFMaturity.getFirstDerivative (0, i) / dblDFEffective))
						return null;

					if (!wjDFMicroJack.accumulatePartialFirstDerivative (0, i, -1. *
						wjDFEffective.getFirstDerivative (0, i) * dblDFMaturity / dblDFEffective /
							dblDFEffective))
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
			return new org.drip.product.calib.EDFComponentQuoteSet (aLSS);
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
		if (null == valParams || null == pqs || !(pqs instanceof org.drip.product.calib.EDFComponentQuoteSet)
			|| !pqs.contains (org.drip.analytics.definition.LatentStateStatic.LATENT_STATE_FUNDING,
				org.drip.analytics.definition.LatentStateStatic.DISCOUNT_QM_DISCOUNT_FACTOR,
					org.drip.state.identifier.FundingLabel.Standard (_strCurrency)))
			return null;

		org.drip.state.estimator.PredictorResponseWeightConstraint prwc = discountFactorPRWC (valParams,
			pricerParams, csqs, quotingParams, pqs);

		return null != prwc && prwc.addMergeLabel (_fri) ? prwc : null;
	}

	@Override public org.drip.state.estimator.PredictorResponseWeightConstraint forwardPRWC (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.pricer.PricerParams pricerParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams quotingParams,
		final org.drip.product.calib.ProductQuoteSet pqs)
	{
		if (null == valParams || null == pqs || !(pqs instanceof
			org.drip.product.calib.EDFComponentQuoteSet))
			return null;

		double dblValueDate = valParams.valueDate();

		if (dblValueDate >= _dblEffective) return null;

		org.drip.product.calib.EDFComponentQuoteSet ecqs = (org.drip.product.calib.EDFComponentQuoteSet) pqs;

		if (!ecqs.containsPrice() && !ecqs.containsRate()) return null;

		double dblForwardRate = java.lang.Double.NaN;

		try {
			if (ecqs.containsRate())
				dblForwardRate = ecqs.rate();
			else if (ecqs.containsPrice())
				dblForwardRate = ((1. / ecqs.price()) - 1.) /
					org.drip.analytics.daycount.Convention.YearFraction (_dblEffective, _dblMaturity, _strDC,
						false, null, _strCalendar);
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
		if (null == valParams || null == pqs || !(pqs instanceof org.drip.product.calib.EDFComponentQuoteSet)
			|| !pqs.contains (org.drip.analytics.definition.LatentStateStatic.LATENT_STATE_FUNDING,
				org.drip.analytics.definition.LatentStateStatic.DISCOUNT_QM_DISCOUNT_FACTOR,
					org.drip.state.identifier.FundingLabel.Standard (_strCurrency)) || !pqs.contains
						(org.drip.analytics.definition.LatentStateStatic.LATENT_STATE_FORWARD,
							org.drip.analytics.definition.LatentStateStatic.FORWARD_QM_FORWARD_RATE, _fri))
			return null;

		org.drip.state.estimator.PredictorResponseWeightConstraint prwc = discountFactorPRWC (valParams,
			pricerParams, csqs, quotingParams, pqs);

		return null != prwc && prwc.addMergeLabel (_fri) ? prwc : null;
	}
}
