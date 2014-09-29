
package org.drip.product.rates;

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
 * FixFloatComponent contains the implementation of the Fix-Float Index Basis Swap product
 *  contract/valuation details. It is made off one Reference Fixed stream and one Derived floating stream.
 *  It exports the following functionality:
 *  - Standard/Custom Constructor for the FixFloatComponent
 *  - Dates: Effective, Maturity, Coupon dates and Product settlement Parameters
 *  - Coupon/Notional Outstanding as well as schedules
 *  - Retrieve the constituent floating streams
 *  - Market Parameters: Discount, Forward, Credit, Treasury Curves
 *  - Cash Flow Periods: Coupon flows and (Optionally) Loss Flows
 *  - Valuation: Named Measure Generation
 *  - Calibration: The codes and constraints generation
 *  - Jacobians: Quote/DF and PV/DF micro-Jacobian generation
 *  - Serialization into and de-serialization out of byte arrays
 * 
 * @author Lakshmi Krishnamurthy
 */

public class FixFloatComponent extends org.drip.product.rates.DualStreamComponent {
	private java.lang.String _strCode = "";
	private org.drip.product.rates.Stream _fixReference = null;
	private org.drip.product.rates.Stream _floatDerived = null;
	private org.drip.param.valuation.CashSettleParams _csp = null;

	@Override protected org.drip.analytics.support.CaseInsensitiveTreeMap<java.lang.Double> calibMeasures (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.pricer.PricerParams pricerParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams quotingParams)
	{
		return null;
	}

	/**
	 * Construct the FixFloatComponent from the Reference Fixed and the Derived Floating Streams.
	 * 
	 * @param fixReference The Reference Fixed Stream
	 * @param floatDerived The Derived Floating Stream
	 * @param csp Cash Settle Parameters Instance
	 * 
	 * @throws java.lang.Exception Thrown if the inputs are invalid
	 */

	public FixFloatComponent (
		final org.drip.product.rates.Stream fixReference,
		final org.drip.product.rates.Stream floatDerived,
		final org.drip.param.valuation.CashSettleParams csp)
		throws java.lang.Exception
	{
		if (null == (_fixReference = fixReference) || null == (_floatDerived = floatDerived))
			throw new java.lang.Exception ("FixFloatComponent ctr: Invalid Inputs");

		_csp = csp;
	}

	@Override public void setPrimaryCode (
		final java.lang.String strCode)
	{
		_strCode = strCode;
	}

	@Override public java.lang.String primaryCode()
	{
		return _strCode;
	}

	@Override public java.lang.String name()
	{
		return _strCode;
	}

	@Override public java.util.Set<java.lang.String> cashflowCurrencySet()
	{
		java.util.Set<java.lang.String> setCcy = new java.util.HashSet<java.lang.String>();

		setCcy.addAll (_fixReference.cashflowCurrencySet());

		setCcy.addAll (_floatDerived.cashflowCurrencySet());

		return setCcy;
	}

	@Override public java.lang.String[] payCurrency()
	{
		return new java.lang.String[] {_fixReference.payCurrency(), _floatDerived.payCurrency()};
	}

	@Override public java.lang.String[] principalCurrency()
	{
		java.lang.String[] astrReferencePrincipalCurrency = _fixReference.principalCurrency();

		java.lang.String[] astrDerivedPrincipalCurrency = _floatDerived.principalCurrency();

		int iNumReferencePrincipalCurrency = null == astrReferencePrincipalCurrency ? 0 :
			astrReferencePrincipalCurrency.length;
		int iNumDerivedPrincipalCurrency = null == astrDerivedPrincipalCurrency ? 0 :
			astrDerivedPrincipalCurrency.length;
		int iNumPrincipalCurrency = iNumReferencePrincipalCurrency + iNumDerivedPrincipalCurrency;

		if (0 == iNumPrincipalCurrency) return null;

		java.lang.String[] astrPrincipalCurrency = new java.lang.String[iNumPrincipalCurrency];

		for (int i = 0; i < iNumReferencePrincipalCurrency; ++i)
			astrPrincipalCurrency[i] = astrReferencePrincipalCurrency[i];

		for (int i = iNumReferencePrincipalCurrency; i < iNumPrincipalCurrency; ++i)
			astrPrincipalCurrency[i] = astrDerivedPrincipalCurrency[i - iNumReferencePrincipalCurrency];

		return astrPrincipalCurrency;
	}

	@Override public double initialNotional()
		throws java.lang.Exception
	{
		return _fixReference.initialNotional();
	}

	@Override public double notional (
		final double dblDate)
		throws java.lang.Exception
	{
		return _fixReference.notional (dblDate);
	}

	@Override public double notional (
		final double dblDate1,
		final double dblDate2)
		throws java.lang.Exception
	{
		return _fixReference.notional (dblDate1, dblDate2);
	}

	@Override public org.drip.analytics.output.GenericCouponPeriodMetrics coupon (
		final double dblAccrualEndDate,
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs)
	{
		return _fixReference.coupon (dblAccrualEndDate, valParams, csqs);
	}

	@Override public int freq()
	{
		return _fixReference.freq();
	}

	@Override public org.drip.state.identifier.CreditLabel[] creditLabel()
	{
		return null;
	}

	@Override public org.drip.state.identifier.ForwardLabel[] forwardLabel()
	{
		return new org.drip.state.identifier.ForwardLabel[] {_floatDerived.forwardLabel()};
	}

	@Override public org.drip.state.identifier.FundingLabel[] fundingLabel()
	{
		return new org.drip.state.identifier.FundingLabel[] {_fixReference.fundingLabel(),
			_floatDerived.fundingLabel()};
	}

	@Override public org.drip.state.identifier.FXLabel[] fxLabel()
	{
		return null;
	}

	@Override public org.drip.product.rates.Stream referenceStream()
	{
		return _fixReference;
	}

	@Override public org.drip.product.rates.Stream derivedStream()
	{
		return _floatDerived;
	}

	@Override public org.drip.analytics.date.JulianDate effective()
	{
		org.drip.analytics.date.JulianDate dtFloatReferenceEffective = _fixReference.effective();

		org.drip.analytics.date.JulianDate dtFloatDerivedEffective = _floatDerived.effective();

		if (null == dtFloatReferenceEffective || null == dtFloatDerivedEffective) return null;

		return dtFloatReferenceEffective.julian() < dtFloatDerivedEffective.julian() ?
			dtFloatReferenceEffective : dtFloatDerivedEffective;
	}

	@Override public org.drip.analytics.date.JulianDate maturity()
	{
		org.drip.analytics.date.JulianDate dtFloatReferenceMaturity = _fixReference.maturity();

		org.drip.analytics.date.JulianDate dtFloatDerivedMaturity = _floatDerived.maturity();

		if (null == dtFloatReferenceMaturity || null == dtFloatDerivedMaturity) return null;

		return dtFloatReferenceMaturity.julian() > dtFloatDerivedMaturity.julian() ?
			dtFloatReferenceMaturity : dtFloatDerivedMaturity;
	}

	@Override public org.drip.analytics.date.JulianDate firstCouponDate()
	{
		org.drip.analytics.date.JulianDate dtFloatReferenceFirstCoupon = _fixReference.firstCouponDate();

		org.drip.analytics.date.JulianDate dtFloatDerivedFirstCoupon = _floatDerived.firstCouponDate();

		if (null == dtFloatReferenceFirstCoupon || null == dtFloatDerivedFirstCoupon) return null;

		return dtFloatReferenceFirstCoupon.julian() < dtFloatDerivedFirstCoupon.julian() ?
			dtFloatReferenceFirstCoupon : dtFloatDerivedFirstCoupon;
	}

	@Override public java.util.List<org.drip.analytics.cashflow.GenericCouponPeriod> cashFlowPeriod()
	{
		return org.drip.analytics.support.AnalyticsHelper.MergePeriodLists (_fixReference.cashFlowPeriod(),
			_floatDerived.cashFlowPeriod());
	}

	@Override public org.drip.param.valuation.CashSettleParams cashSettleParams()
	{
		return _csp;
	}

	@Override public org.drip.analytics.support.CaseInsensitiveTreeMap<java.lang.Double> value (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.pricer.PricerParams pricerParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams quotingParams)
	{
		long lStart = System.nanoTime();

		double dblValueDate = valParams.valueDate();

		org.drip.analytics.support.CaseInsensitiveTreeMap<java.lang.Double> mapFixedReferenceStreamResult =
			_fixReference.value (valParams, pricerParams, csqs, quotingParams);

		org.drip.analytics.support.CaseInsensitiveTreeMap<java.lang.Double> mapFloatDerivedStreamResult =
			_floatDerived.value (valParams, pricerParams, csqs, quotingParams);

		if (null == mapFixedReferenceStreamResult || 0 == mapFixedReferenceStreamResult.size() || null ==
			mapFloatDerivedStreamResult || 0 == mapFloatDerivedStreamResult.size())
			return null;

		org.drip.analytics.support.CaseInsensitiveTreeMap<java.lang.Double> mapResult = new
			org.drip.analytics.support.CaseInsensitiveTreeMap<java.lang.Double>();

		if (!org.drip.analytics.support.AnalyticsHelper.AccumulateMeasures (mapResult, _fixReference.name(),
			mapFixedReferenceStreamResult))
			return null;

		if (!org.drip.analytics.support.AnalyticsHelper.AccumulateMeasures (mapResult, _floatDerived.name(),
			mapFloatDerivedStreamResult))
			return null;

		double dblDerivedAccrued = mapFloatDerivedStreamResult.get ("Accrued");

		double dblDerivedAccrued01 = mapFloatDerivedStreamResult.get ("Accrued01");

		double dblDerivedCleanDV01 = mapFloatDerivedStreamResult.get ("CleanDV01");

		double dblDerivedCleanPV = mapFloatDerivedStreamResult.get ("CleanPV");

		double dblDerivedDirtyDV01 = mapFloatDerivedStreamResult.get ("DirtyDV01");

		double dblDerivedDirtyPV = mapFloatDerivedStreamResult.get ("DirtyPV");

		double dblDerivedPV = mapFloatDerivedStreamResult.get ("PV");

		double dblDerivedCumulativeConvexityAdjustmentPremium = mapFloatDerivedStreamResult.get
			("CumulativeConvexityAdjustmentPremiumUpfront");

		double dblFixing01 = mapFloatDerivedStreamResult.get ("Fixing01");

		double dblReferenceAccrued = mapFixedReferenceStreamResult.get ("Accrued");

		double dblReferenceAccrued01 = mapFixedReferenceStreamResult.get ("Accrued01");

		double dblReferenceCleanDV01 = mapFixedReferenceStreamResult.get ("CleanDV01");

		double dblReferenceCleanPV = mapFixedReferenceStreamResult.get ("CleanPV");

		double dblReferenceDirtyPV = mapFixedReferenceStreamResult.get ("DirtyPV");

		double dblReferenceCumulativeConvexityAdjustmentPremium = mapFixedReferenceStreamResult.get
			("CumulativeConvexityAdjustmentPremium");

		double dblValueNotional = java.lang.Double.NaN;
		double dblAccrued = dblDerivedAccrued + dblReferenceAccrued;
		double dblCleanPV = dblReferenceCleanPV + dblDerivedCleanPV;
		double dblParFixedCoupon = -0.0001 * dblDerivedCleanPV / dblReferenceCleanDV01;

		mapResult.put ("Accrued", dblAccrued);

		mapResult.put ("CleanFixedDV01", dblReferenceCleanDV01);

		mapResult.put ("CleanFloatingDV01", dblDerivedCleanDV01);

		mapResult.put ("CleanFloatingPV", dblDerivedCleanPV);

		mapResult.put ("CleanPV", dblCleanPV);

		mapResult.put ("CumulativeConvexityAdjustmentPremium", _fixReference.initialNotional() *
			dblReferenceCumulativeConvexityAdjustmentPremium + _floatDerived.initialNotional() *
				dblDerivedCumulativeConvexityAdjustmentPremium);

		mapResult.put ("DerivedAccrued", dblDerivedAccrued);

		mapResult.put ("DerivedAccrued01", dblDerivedAccrued01);

		mapResult.put ("DerivedCleanDV01", dblDerivedCleanDV01);

		mapResult.put ("DerivedCleanPV", dblDerivedCleanPV);

		mapResult.put ("DerivedDirtyDV01", dblDerivedDirtyDV01);

		mapResult.put ("DerivedDirtyPV", dblDerivedDirtyPV);

		mapResult.put ("DerivedDV01", dblDerivedCleanDV01);

		mapResult.put ("DerivedFixing01", dblFixing01);

		mapResult.put ("DerivedParBasisSpread", -1. * dblCleanPV / dblDerivedCleanDV01);

		mapResult.put ("DerivedPV", dblDerivedPV);

		mapResult.put ("DerivedCumulativeConvexityAdjustmentFactor", mapFloatDerivedStreamResult.get
			("CumulativeConvexityAdjustmentFactor"));

		mapResult.put ("DerivedCumulativeConvexityAdjustmentPremium",
			dblDerivedCumulativeConvexityAdjustmentPremium);

		mapResult.put ("DerivedResetDate", mapFloatDerivedStreamResult.get ("ResetDate"));

		mapResult.put ("DerivedResetRate", mapFloatDerivedStreamResult.get ("ResetRate"));

		mapResult.put ("DirtyFixedDV01", mapFixedReferenceStreamResult.get ("DirtyDV01"));

		mapResult.put ("DirtyFixedPV", dblReferenceDirtyPV);

		mapResult.put ("DirtyFloatingDV01", dblDerivedDirtyDV01);

		mapResult.put ("DirtyFloatingPV", dblDerivedDirtyPV);

		mapResult.put ("DirtyPV", dblDerivedDirtyPV + dblReferenceDirtyPV);

		mapResult.put ("FairPremium", dblParFixedCoupon);

		mapResult.put ("FixedAccrued", dblReferenceAccrued);

		mapResult.put ("FixedAccrued01", dblReferenceAccrued01);

		mapResult.put ("FixedDV01", dblReferenceCleanDV01);

		mapResult.put ("FloatAccrued", dblDerivedAccrued);

		mapResult.put ("FloatAccrued01", dblDerivedAccrued01);

		mapResult.put ("FloatDV01", dblDerivedCleanDV01);

		mapResult.put ("Fixing01", dblFixing01);

		mapResult.put ("ParFixedCoupon", dblParFixedCoupon);

		mapResult.put ("ParRate", dblParFixedCoupon);

		mapResult.put ("ParSwapRate", dblParFixedCoupon);

		mapResult.put ("PV", dblCleanPV);

		mapResult.put ("Rate", dblParFixedCoupon);

		mapResult.put ("ReferenceAccrued", dblReferenceAccrued);

		mapResult.put ("ReferenceAccrued01", dblReferenceAccrued01);

		mapResult.put ("ReferenceCleanDV01", dblReferenceCleanDV01);

		mapResult.put ("ReferenceCleanPV", dblReferenceCleanPV);

		mapResult.put ("ReferenceDirtyDV01", mapFixedReferenceStreamResult.get ("DirtyDV01"));

		mapResult.put ("ReferenceDirtyPV", dblReferenceDirtyPV);

		mapResult.put ("ReferenceDV01", dblReferenceCleanDV01);

		mapResult.put ("ReferenceParBasisSpread", -1. * dblCleanPV / dblReferenceCleanDV01);

		mapResult.put ("ReferencePV", dblReferenceCleanPV);

		mapResult.put ("ReferenceCumulativeConvexityAdjustmentFactor", mapFixedReferenceStreamResult.get
			("CumulativeConvexityAdjustmentFactor"));

		mapResult.put ("ReferenceCumulativeConvexityAdjustmentPremium",
			dblReferenceCumulativeConvexityAdjustmentPremium);

		mapResult.put ("ResetDate", mapFloatDerivedStreamResult.get ("ResetDate"));

		mapResult.put ("ResetRate", mapFloatDerivedStreamResult.get ("ResetRate"));

		mapResult.put ("SwapRate", dblParFixedCoupon);

		mapResult.put ("Upfront", mapFixedReferenceStreamResult.get ("Upfront") +
			mapFloatDerivedStreamResult.get ("Upfront"));

		try {
			dblValueNotional = notional (dblValueDate);
		} catch (java.lang.Exception e) {
			e.printStackTrace();
		}

		try {
			if (org.drip.quant.common.NumberUtil.IsValid (dblValueNotional)) {
				double dblCleanPrice = 100. * (1. + (dblCleanPV / initialNotional() / dblValueNotional));

				org.drip.analytics.rates.DiscountCurve dcFunding = csqs.fundingCurve (fundingLabel()[0]);

				if (null == dcFunding) return null;

				double dblStartDate = effective().julian();

				double dblTelescopedFloatingPV = dcFunding.df (dblStartDate > dblValueDate ? dblStartDate :
					dblValueDate) - dcFunding.df (maturity());

				mapResult.put ("CalibFloatingPV", dblTelescopedFloatingPV);

				mapResult.put ("CalibSwapRate", java.lang.Math.abs (0.0001 * dblTelescopedFloatingPV /
					dblReferenceCleanDV01 * notional (dblValueDate)));

				mapResult.put ("CleanPrice", dblCleanPrice);

				mapResult.put ("DirtyPrice", dblCleanPrice + dblAccrued);

				mapResult.put ("Price", dblCleanPrice);
			}
		} catch (java.lang.Exception e) {
			e.printStackTrace();

			return null;
		}

		mapResult.put ("CalcTime", (System.nanoTime() - lStart) * 1.e-09);

		return mapResult;
	}

	@Override public java.util.Set<java.lang.String> measureNames()
	{
		java.util.Set<java.lang.String> setstrMeasureNames = new java.util.TreeSet<java.lang.String>();

		setstrMeasureNames.add ("Accrued");

		setstrMeasureNames.add ("CalcTime");

		setstrMeasureNames.add ("CalibFloatingPV");

		setstrMeasureNames.add ("CalibSwapRate");

		setstrMeasureNames.add ("CleanFixedDV01");

		setstrMeasureNames.add ("CleanFloatingDV01");

		setstrMeasureNames.add ("CleanFloatingPV");

		setstrMeasureNames.add ("CleanPrice");

		setstrMeasureNames.add ("CleanPV");

		setstrMeasureNames.add ("DerivedAccrued01");

		setstrMeasureNames.add ("DerivedAccrued");

		setstrMeasureNames.add ("DerivedCleanDV01");

		setstrMeasureNames.add ("DerivedCleanPV");

		setstrMeasureNames.add ("DerivedDirtyDV01");

		setstrMeasureNames.add ("DerivedDirtyPV");

		setstrMeasureNames.add ("DerivedDV01");

		setstrMeasureNames.add ("DerivedFixing01");

		setstrMeasureNames.add ("DerivedParBasisSpread");

		setstrMeasureNames.add ("DerivedPV");

		setstrMeasureNames.add ("DerivedCumulativeConvexityAdjustmentFactor");

		setstrMeasureNames.add ("DerivedCumulativeConvexityAdjustmentPremium");

		setstrMeasureNames.add ("DerivedResetDate");

		setstrMeasureNames.add ("DerivedResetRate");

		setstrMeasureNames.add ("DirtyFixedDV01");

		setstrMeasureNames.add ("DirtyFixedPV");

		setstrMeasureNames.add ("DirtyFloatingDV01");

		setstrMeasureNames.add ("DirtyFloatingPV");

		setstrMeasureNames.add ("DirtyPrice");

		setstrMeasureNames.add ("DirtyPV");

		setstrMeasureNames.add ("FairPremium");

		setstrMeasureNames.add ("FixedAccrued");

		setstrMeasureNames.add ("FixedAccrued01");

		setstrMeasureNames.add ("FixedDV01");

		setstrMeasureNames.add ("FloatAccrued");

		setstrMeasureNames.add ("FloatAccrued01");

		setstrMeasureNames.add ("FloatDV01");

		setstrMeasureNames.add ("Fixing01");

		setstrMeasureNames.add ("ParFixedCoupon");

		setstrMeasureNames.add ("ParRate");

		setstrMeasureNames.add ("ParSwapRate");

		setstrMeasureNames.add ("Price");

		setstrMeasureNames.add ("PV");

		setstrMeasureNames.add ("CumulativeConvexityAdjustmentPremium");

		setstrMeasureNames.add ("Rate");

		setstrMeasureNames.add ("ReferenceAccrued");

		setstrMeasureNames.add ("ReferenceAccrued01");

		setstrMeasureNames.add ("ReferenceCleanDV01");

		setstrMeasureNames.add ("ReferenceCleanPV");

		setstrMeasureNames.add ("ReferenceDirtyDV01");

		setstrMeasureNames.add ("ReferenceDirtyPV");

		setstrMeasureNames.add ("ReferenceDV01");

		setstrMeasureNames.add ("ReferenceParBasisSpread");

		setstrMeasureNames.add ("ReferencePV");

		setstrMeasureNames.add ("ReferenceCumulativeConvexityAdjustmentFactor");

		setstrMeasureNames.add ("ReferenceCumulativeConvexityAdjustmentPremium");

		setstrMeasureNames.add ("ResetDate");

		setstrMeasureNames.add ("ResetRate");

		setstrMeasureNames.add ("SwapRate");

		setstrMeasureNames.add ("Upfront");

		return setstrMeasureNames;
	}


	@Override public org.drip.quant.calculus.WengertJacobian jackDDirtyPVDManifestMeasure (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.pricer.PricerParams pricerParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams quotingParams)
	{
		org.drip.analytics.support.CaseInsensitiveTreeMap<java.lang.Double> mapMeasures = value (valParams,
			pricerParams, csqs, quotingParams);

		if (null == mapMeasures || !mapMeasures.containsKey ("SwapRate")) return null;

		double dblParSwapRate = mapMeasures.get ("SwapRate");

		org.drip.quant.calculus.WengertJacobian jackDDirtyPVDManifestMeasureFloating =
			_floatDerived.jackDDirtyPVDManifestMeasure (valParams, pricerParams, csqs, quotingParams);

		if (null == jackDDirtyPVDManifestMeasureFloating) return null;

		int iNumQuote = jackDDirtyPVDManifestMeasureFloating.numParameters();

		if (0 == iNumQuote) return null;

		org.drip.quant.calculus.WengertJacobian jackDDirtyPVDManifestMeasureFixed =
			_fixReference.jackDDirtyPVDManifestMeasure (valParams, pricerParams, csqs, quotingParams);

		if (null == jackDDirtyPVDManifestMeasureFixed || iNumQuote !=
			jackDDirtyPVDManifestMeasureFixed.numParameters())
			return null;

		double dblNotionalScaleDown = java.lang.Double.NaN;
		org.drip.quant.calculus.WengertJacobian jackDDirtyPVDManifestMeasureIRS = null;

		if (null == jackDDirtyPVDManifestMeasureIRS) {
			try {
				dblNotionalScaleDown = 1. / initialNotional();

				jackDDirtyPVDManifestMeasureIRS = new org.drip.quant.calculus.WengertJacobian (1, iNumQuote);
			} catch (java.lang.Exception e) {
				e.printStackTrace();

				return null;
			}
		}

		for (int i = 0; i < iNumQuote; ++i) {
			if (!jackDDirtyPVDManifestMeasureIRS.accumulatePartialFirstDerivative (0, i, dblNotionalScaleDown
				* (dblParSwapRate * jackDDirtyPVDManifestMeasureFixed.getFirstDerivative (0, i) +
					jackDDirtyPVDManifestMeasureFloating.getFirstDerivative (0, i))))
				return null;
		}

		return jackDDirtyPVDManifestMeasureIRS;
	}

	@Override public org.drip.quant.calculus.WengertJacobian manifestMeasureDFMicroJack (
		final java.lang.String strManifestMeasure,
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.pricer.PricerParams pricerParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams quotingParams)
	{
		if (null == valParams || valParams.valueDate() >= maturity().julian() || null ==
			strManifestMeasure || null == csqs)
			return null;

		if ("Rate".equalsIgnoreCase (strManifestMeasure) || "SwapRate".equalsIgnoreCase (strManifestMeasure))
		{
			org.drip.analytics.support.CaseInsensitiveTreeMap<java.lang.Double> mapMeasures = value
				(valParams, pricerParams, csqs, quotingParams);

			if (null == mapMeasures) return null;

			double dblDirtyDV01 = mapMeasures.get ("DirtyDV01");

			double dblParSwapRate = mapMeasures.get ("SwapRate");

			try {
				org.drip.quant.calculus.WengertJacobian wjSwapRateDFMicroJack = null;

				org.drip.analytics.rates.DiscountCurve dcFunding = csqs.fundingCurve (fundingLabel()[0]);

				if (null == dcFunding) return null;

				for (org.drip.analytics.cashflow.GenericCouponPeriod p : cashFlowPeriod()) {
					double dblPeriodPayDate = p.payDate();

					if (dblPeriodPayDate < valParams.valueDate()) continue;

					org.drip.quant.calculus.WengertJacobian wjPeriodFwdRateDF =
						dcFunding.jackDForwardDManifestMeasure (p.startDate(), p.endDate(), "Rate",
							p.couponDCF());

					org.drip.quant.calculus.WengertJacobian wjPeriodPayDFDF =
						dcFunding.jackDDFDManifestMeasure (dblPeriodPayDate, "Rate");

					if (null == wjPeriodFwdRateDF || null == wjPeriodPayDFDF) continue;

					double dblForwardRate = dcFunding.libor (p.startDate(), p.endDate());

					double dblPeriodPayDF = dcFunding.df (dblPeriodPayDate);

					if (null == wjSwapRateDFMicroJack)
						wjSwapRateDFMicroJack = new org.drip.quant.calculus.WengertJacobian (1,
							wjPeriodFwdRateDF.numParameters());

					double dblPeriodNotional = notional (p.startDate(), p.endDate());

					double dblPeriodDCF = p.couponDCF();

					for (int k = 0; k < wjPeriodFwdRateDF.numParameters(); ++k) {
						double dblPeriodMicroJack = (dblForwardRate - dblParSwapRate) *
							wjPeriodPayDFDF.getFirstDerivative (0, k) + dblPeriodPayDF *
								wjPeriodFwdRateDF.getFirstDerivative (0, k);

						if (!wjSwapRateDFMicroJack.accumulatePartialFirstDerivative (0, k, dblPeriodNotional
							* dblPeriodDCF * dblPeriodMicroJack / dblDirtyDV01))
							return null;
					}
				}

				return wjSwapRateDFMicroJack;
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
			return new org.drip.product.calib.FixFloatQuoteSet (aLSS);
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
		if (null == valParams || valParams.valueDate() >= maturity().julian() || null == pqs || !(pqs
			instanceof org.drip.product.calib.FixFloatQuoteSet))
			return null;

		double dblPV = 0.;
		org.drip.product.calib.FixedStreamQuoteSet fsqsReference = null;
		org.drip.product.calib.FloatingStreamQuoteSet fsqsDerived = null;
		org.drip.product.calib.FixFloatQuoteSet ffqs = (org.drip.product.calib.FixFloatQuoteSet) pqs;

		if (!ffqs.containsPV() && !ffqs.containsSwapRate() && !ffqs.containsDerivedParBasisSpread() &&
			!ffqs.containsReferenceParBasisSpread())
			return null;

		org.drip.state.representation.LatentStateSpecification[] aLSS = pqs.lss();

		try {
			fsqsDerived = new org.drip.product.calib.FloatingStreamQuoteSet (aLSS);

			fsqsReference = new org.drip.product.calib.FixedStreamQuoteSet (aLSS);

			if (ffqs.containsPV()) dblPV = ffqs.pv();

			if (ffqs.containsSwapRate()) fsqsReference.setCoupon (ffqs.swapRate());

			if (ffqs.containsDerivedParBasisSpread()) fsqsDerived.setSpread (ffqs.derivedParBasisSpread());

			if (ffqs.containsReferenceParBasisSpread())
				fsqsReference.setCouponBasis (ffqs.referenceParBasisSpread());
		} catch (java.lang.Exception e) {
			e.printStackTrace();

			return null;
		}

		org.drip.state.estimator.PredictorResponseWeightConstraint prwcDerived = _floatDerived.fundingPRWC
			(valParams, pricerParams, csqs, quotingParams, fsqsDerived);

		org.drip.state.estimator.PredictorResponseWeightConstraint prwcReference = _fixReference.fundingPRWC
			(valParams, pricerParams, csqs, quotingParams, fsqsReference);

		if (null == prwcDerived && null == prwcReference) return null;

		org.drip.state.estimator.PredictorResponseWeightConstraint prwc = new
			org.drip.state.estimator.PredictorResponseWeightConstraint();

		if (!prwc.absorb (prwcDerived)) return null;

		if (!prwc.absorb (prwcReference)) return null;

		return !prwc.updateValue (dblPV) ? null : prwc;
	}

	@Override public org.drip.state.estimator.PredictorResponseWeightConstraint forwardPRWC (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.pricer.PricerParams pricerParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams quotingParams,
		final org.drip.product.calib.ProductQuoteSet pqs)
	{
		if (null == valParams || valParams.valueDate() >= maturity().julian() || null == pqs || !(pqs
			instanceof org.drip.product.calib.FixFloatQuoteSet))
			return null;

		double dblPV = 0.;
		org.drip.product.calib.FixedStreamQuoteSet fsqsReference = null;
		org.drip.product.calib.FloatingStreamQuoteSet fsqsDerived = null;
		org.drip.product.calib.FixFloatQuoteSet ffqs = (org.drip.product.calib.FixFloatQuoteSet) pqs;

		if (!ffqs.containsPV() && !ffqs.containsSwapRate() && !ffqs.containsDerivedParBasisSpread() &&
			!ffqs.containsReferenceParBasisSpread())
			return null;

		org.drip.state.representation.LatentStateSpecification[] aLSS = pqs.lss();

		try {
			fsqsDerived = new org.drip.product.calib.FloatingStreamQuoteSet (aLSS);

			fsqsReference = new org.drip.product.calib.FixedStreamQuoteSet (aLSS);

			if (ffqs.containsPV()) dblPV = ffqs.pv();

			if (ffqs.containsSwapRate()) fsqsReference.setCoupon (ffqs.swapRate());

			if (ffqs.containsDerivedParBasisSpread()) fsqsDerived.setSpread (ffqs.derivedParBasisSpread());

			if (ffqs.containsReferenceParBasisSpread())
				fsqsReference.setCouponBasis (ffqs.referenceParBasisSpread());
		} catch (java.lang.Exception e) {
			e.printStackTrace();

			return null;
		}

		org.drip.state.estimator.PredictorResponseWeightConstraint prwcDerived = _floatDerived.forwardPRWC
			(valParams, pricerParams, csqs, quotingParams, fsqsDerived);

		org.drip.state.estimator.PredictorResponseWeightConstraint prwcReference = _fixReference.forwardPRWC
			(valParams, pricerParams, csqs, quotingParams, fsqsReference);

		if (null == prwcDerived && null == prwcReference) return null;

		org.drip.state.estimator.PredictorResponseWeightConstraint prwc = new
			org.drip.state.estimator.PredictorResponseWeightConstraint();

		if (!prwc.absorb (prwcDerived)) return null;

		if (!prwc.absorb (prwcReference)) return null;

		return !prwc.updateValue (dblPV) ? null : prwc;
	}

	@Override public org.drip.state.estimator.PredictorResponseWeightConstraint fundingForwardPRWC (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.pricer.PricerParams pricerParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams quotingParams,
		final org.drip.product.calib.ProductQuoteSet pqs)
	{
		if (null == valParams || valParams.valueDate() >= maturity().julian() || null == pqs || !(pqs
			instanceof org.drip.product.calib.FixFloatQuoteSet))
			return null;

		double dblPV = 0.;
		org.drip.product.calib.FixedStreamQuoteSet fsqsReference = null;
		org.drip.product.calib.FloatingStreamQuoteSet fsqsDerived = null;
		org.drip.product.calib.FixFloatQuoteSet ffqs = (org.drip.product.calib.FixFloatQuoteSet) pqs;

		if (!ffqs.containsPV() && !ffqs.containsSwapRate() && !ffqs.containsDerivedParBasisSpread() &&
			!ffqs.containsReferenceParBasisSpread())
			return null;

		org.drip.state.representation.LatentStateSpecification[] aLSS = pqs.lss();

		try {
			fsqsDerived = new org.drip.product.calib.FloatingStreamQuoteSet (aLSS);

			fsqsReference = new org.drip.product.calib.FixedStreamQuoteSet (aLSS);

			if (ffqs.containsPV()) dblPV = ffqs.pv();

			if (ffqs.containsSwapRate()) fsqsReference.setCoupon (ffqs.swapRate());

			if (ffqs.containsDerivedParBasisSpread()) fsqsDerived.setSpread (ffqs.derivedParBasisSpread());

			if (ffqs.containsReferenceParBasisSpread())
				fsqsReference.setCouponBasis (ffqs.referenceParBasisSpread());
		} catch (java.lang.Exception e) {
			e.printStackTrace();

			return null;
		}

		org.drip.state.estimator.PredictorResponseWeightConstraint prwcDerived =
			_floatDerived.fundingForwardPRWC (valParams, pricerParams, csqs, quotingParams, fsqsDerived);

		org.drip.state.estimator.PredictorResponseWeightConstraint prwcReference =
			_fixReference.fundingForwardPRWC (valParams, pricerParams, csqs, quotingParams, fsqsReference);

		if (null == prwcDerived && null == prwcReference) return null;

		org.drip.state.estimator.PredictorResponseWeightConstraint prwc = new
			org.drip.state.estimator.PredictorResponseWeightConstraint();

		if (!prwc.absorb (prwcDerived)) return null;

		if (!prwc.absorb (prwcReference)) return null;

		return !prwc.updateValue (dblPV) ? null : prwc;
	}
}
