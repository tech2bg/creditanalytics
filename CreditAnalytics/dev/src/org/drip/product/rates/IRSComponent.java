
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
 * IRSComponent contains the implementation of the Interest Rate Swap product contract/valuation details. It
 *  exports the following functionality:
 *  - Standard/Custom Constructor for the IRSComponent
 *  - Dates: Effective, Maturity, Coupon dates and Product settlement Parameters
 *  - Coupon/Notional Outstanding as well as schedules
 *  - Retrieve the constituent fixed and floating streams
 *  - Market Parameters: Discount, Forward, Credit, Treasury Curves
 *  - Cash Flow Periods: Coupon flows and (Optionally) Loss Flows
 *  - Valuation: Named Measure Generation
 *  - Calibration: The codes and constraints generation
 *  - Jacobians: Quote/DF and PV/DF micro-Jacobian generation
 *  - Serialization into and de-serialization out of byte arrays
 * 
 * @author Lakshmi Krishnamurthy
 */

public class IRSComponent extends org.drip.product.definition.RatesComponent {
	private java.lang.String _strCode = "";
	private org.drip.product.stream.FixedStream _fixStream = null;
	private org.drip.product.stream.FloatingStream _floatStream = null;

	@Override protected org.drip.analytics.support.CaseInsensitiveTreeMap<java.lang.Double> calibMeasures (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.pricer.PricerParams pricerParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams quotingParams)
	{
		return null;
	}

	/**
	 * Construct the IRSComponent from the fixed and the floating streams
	 * 
	 * @param fixStream Fixed Stream
	 * @param floatStream Floating Stream
	 * 
	 * @throws java.lang.Exception Thrown if the inputs are invalid
	 */

	public IRSComponent (
		final org.drip.product.stream.FixedStream fixStream,
		final org.drip.product.stream.FloatingStream floatStream)
		throws java.lang.Exception
	{
		if (null == (_fixStream = fixStream) || null == (_floatStream = floatStream))
			throw new java.lang.Exception ("IRSComponent ctr: Invalid Inputs");
	}

	/**
	 * De-serialize the IRSComponent from the byte array
	 * 
	 * @param ab Byte Array
	 * 
	 * @throws java.lang.Exception Thrown if the IRSComponent cannot be de-serialized from the byte array
	 */

	public IRSComponent (
		final byte[] ab)
		throws java.lang.Exception
	{
		if (null == ab || 0 == ab.length)
			throw new java.lang.Exception ("InterestRateSwap de-serializer: Invalid input Byte array");

		java.lang.String strRawString = new java.lang.String (ab);

		if (null == strRawString || strRawString.isEmpty())
			throw new java.lang.Exception ("InterestRateSwap de-serializer: Empty state");

		java.lang.String strSerializedInterestRateSwap = strRawString.substring (0, strRawString.indexOf
			(objectTrailer()));

		if (null == strSerializedInterestRateSwap || strSerializedInterestRateSwap.isEmpty())
			throw new java.lang.Exception ("InterestRateSwap de-serializer: Cannot locate state");

		java.lang.String[] astrField = org.drip.quant.common.StringUtil.Split (strSerializedInterestRateSwap,
			fieldDelimiter());

		if (null == astrField || 3 > astrField.length)
			throw new java.lang.Exception ("InterestRateSwap de-serializer: Invalid reqd field set");

		// double dblVersion = new java.lang.Double (astrField[0]).doubleValue();

		if (null == astrField[1] || astrField[1].isEmpty())
			throw new java.lang.Exception ("InterestRateSwap de-serializer: Cannot locate fixed stream");

		if (org.drip.service.stream.Serializer.NULL_SER_STRING.equalsIgnoreCase (astrField[1]))
			_fixStream = null;
		else
			_fixStream = new org.drip.product.stream.FixedStream (astrField[1].getBytes());

		if (null == astrField[2] || astrField[2].isEmpty())
			throw new java.lang.Exception ("InterestRateSwap de-serializer: Cannot locate floating stream");

		if (org.drip.service.stream.Serializer.NULL_SER_STRING.equalsIgnoreCase (astrField[2]))
			_floatStream = null;
		else
			_floatStream = new org.drip.product.stream.FloatingStream (astrField[2].getBytes());
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
		return "IRS=" + maturity();
	}

	@Override public java.util.Set<java.lang.String> cashflowCurrencySet()
	{
		java.util.Set<java.lang.String> setCcy = new java.util.HashSet<java.lang.String>();

		setCcy.addAll (_fixStream.cashflowCurrencySet());

		setCcy.addAll (_floatStream.cashflowCurrencySet());

		return setCcy;
	}

	@Override public java.lang.String[] couponCurrency()
	{
		java.lang.String[] astrReferenceCouponCurrency = _fixStream.couponCurrency();

		java.lang.String[] astrDerivedCouponCurrency = _floatStream.couponCurrency();

		int iNumReferenceCouponCurrency = null == astrReferenceCouponCurrency ? 0 :
			astrReferenceCouponCurrency.length;
		int iNumDerivedCouponCurrency = null == astrDerivedCouponCurrency ? 0 :
			astrDerivedCouponCurrency.length;
		int iNumCouponCurrency = iNumReferenceCouponCurrency + iNumDerivedCouponCurrency;

		if (0 == iNumCouponCurrency) return null;

		java.lang.String[] astrCouponCurrency = new java.lang.String[iNumCouponCurrency];

		for (int i = 0; i < iNumReferenceCouponCurrency; ++i)
			astrCouponCurrency[i] = astrReferenceCouponCurrency[i];

		for (int i = iNumReferenceCouponCurrency; i < iNumCouponCurrency; ++i)
			astrCouponCurrency[i] = astrDerivedCouponCurrency[i - iNumReferenceCouponCurrency];

		return astrCouponCurrency;
	}

	@Override public java.lang.String[] principalCurrency()
	{
		java.lang.String[] astrReferencePrincipalCurrency = _fixStream.principalCurrency();

		java.lang.String[] astrDerivedPrincipalCurrency = _floatStream.principalCurrency();

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
		return _fixStream.initialNotional();
	}

	@Override public double notional (
		final double dblDate)
		throws java.lang.Exception
	{
		return _fixStream.notional (dblDate);
	}

	@Override public double notional (
		final double dblDate1,
		final double dblDate2)
		throws java.lang.Exception
	{
		return _fixStream.notional (dblDate1, dblDate2);
	}

	@Override public org.drip.analytics.output.PeriodCouponMeasures coupon (
		final double dblAccrualEndDate,
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs)
	{
		return _fixStream.coupon (dblAccrualEndDate, valParams, csqs);
	}

	@Override public int freq()
	{
		return _fixStream.freq();
	}

	@Override public java.lang.String[] forwardCurveName()
	{
		return _floatStream.forwardCurveName();
	}

	@Override public java.lang.String[] creditCurveName()
	{
		return null;
	}

	@Override public java.lang.String[] currencyPairCode()
	{
		return null;
	}

	@Override public org.drip.analytics.date.JulianDate effective()
	{
		org.drip.analytics.date.JulianDate dtFixEffective = _fixStream.effective();

		org.drip.analytics.date.JulianDate dtFloatEffective = _floatStream.effective();

		if (null == dtFixEffective || null == dtFloatEffective) return null;

		return dtFixEffective.julian() < dtFloatEffective.julian() ? dtFixEffective : dtFloatEffective;
	}

	@Override public org.drip.analytics.date.JulianDate maturity()
	{
		org.drip.analytics.date.JulianDate dtFixMaturity = _fixStream.maturity();

		org.drip.analytics.date.JulianDate dtFloatMaturity = _floatStream.maturity();

		if (null == dtFixMaturity || null == dtFloatMaturity) return null;

		return dtFixMaturity.julian() > dtFloatMaturity.julian() ? dtFixMaturity : dtFloatMaturity;
	}

	@Override public org.drip.analytics.date.JulianDate firstCouponDate()
	{
		org.drip.analytics.date.JulianDate dtFixFirstCoupon = _fixStream.firstCouponDate();

		org.drip.analytics.date.JulianDate dtFloatFirstCoupon = _floatStream.firstCouponDate();

		if (null == dtFixFirstCoupon || null == dtFloatFirstCoupon) return null;

		return dtFixFirstCoupon.julian() < dtFloatFirstCoupon.julian() ? dtFixFirstCoupon :
			dtFloatFirstCoupon;
	}

	@Override public java.util.List<org.drip.analytics.period.CashflowPeriod> cashFlowPeriod()
	{
		return org.drip.analytics.support.AnalyticsHelper.MergePeriodLists (_fixStream.cashFlowPeriod(),
			_floatStream.cashFlowPeriod());
	}

	@Override public org.drip.param.valuation.CashSettleParams cashSettleParams()
	{
		return _fixStream.cashSettleParams();
	}

	@Override public org.drip.analytics.support.CaseInsensitiveTreeMap<java.lang.Double> value (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.pricer.PricerParams pricerParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams quotingParams)
	{
		long lStart = System.nanoTime();

		org.drip.analytics.support.CaseInsensitiveTreeMap<java.lang.Double> mapFixStreamResult =
			_fixStream.value (valParams, pricerParams, csqs, quotingParams);

		org.drip.analytics.support.CaseInsensitiveTreeMap<java.lang.Double> mapFloatStreamResult =
			_floatStream.value (valParams, pricerParams, csqs, quotingParams);

		if (null == mapFixStreamResult || 0 == mapFixStreamResult.size() || null == mapFloatStreamResult || 0
			== mapFloatStreamResult.size())
			return null;

		org.drip.analytics.support.CaseInsensitiveTreeMap<java.lang.Double> mapResult = new
			org.drip.analytics.support.CaseInsensitiveTreeMap<java.lang.Double>();

		if (!org.drip.analytics.support.AnalyticsHelper.AccumulateMeasures (mapResult, _fixStream.name(),
			mapFixStreamResult) || !org.drip.analytics.support.AnalyticsHelper.AccumulateMeasures (mapResult,
				_floatStream.name(), mapFloatStreamResult))
			return null;

		double dblFixedCleanDV01 = mapFixStreamResult.get ("CleanDV01");

		double dblFloatingCleanPV = mapFloatStreamResult.get ("CleanPV");

		double dblCleanPV = mapFixStreamResult.get ("CleanPV") + dblFloatingCleanPV;

		double dblFairPremium = java.lang.Math.abs (0.0001 * dblFloatingCleanPV / dblFixedCleanDV01);

		mapResult.put ("CleanFixedDV01", dblFixedCleanDV01);

		mapResult.put ("CleanFixedPV", mapFixStreamResult.get ("CleanPV"));

		mapResult.put ("CleanFloatingDV01", mapFloatStreamResult.get ("CleanDV01"));

		mapResult.put ("CleanFloatingPV", dblFloatingCleanPV);

		mapResult.put ("CleanPV", dblCleanPV);

		mapResult.put ("DirtyFixedDV01", mapFixStreamResult.get ("DirtyDV01"));

		mapResult.put ("DirtyFixedPV", mapFixStreamResult.get ("DirtyPV"));

		mapResult.put ("DirtyFloatingDV01", mapFloatStreamResult.get ("DirtyDV01"));

		mapResult.put ("DirtyFloatingPV", mapFloatStreamResult.get ("DirtyPV"));

		mapResult.put ("DirtyPV", mapFixStreamResult.get ("DirtyPV") + mapFloatStreamResult.get ("DirtyPV"));

		mapResult.put ("FairPremium", dblFairPremium);

		mapResult.put ("FixedAccrued", mapFixStreamResult.get ("Accrued"));

		mapResult.put ("FixedAccrued01", mapFixStreamResult.get ("Accrued01"));

		mapResult.put ("FixedDV01", mapFixStreamResult.get ("DV01"));

		mapResult.put ("FloatAccrued", mapFloatStreamResult.get ("Accrued"));

		mapResult.put ("FloatAccrued01", mapFloatStreamResult.get ("Accrued01"));

		mapResult.put ("FloatDV01", mapFloatStreamResult.get ("DV01"));

		mapResult.put ("Fixing01", mapFloatStreamResult.get ("Fixing01"));

		mapResult.put ("ParRate", dblFairPremium);

		mapResult.put ("PV", mapFixStreamResult.get ("PV") + mapFloatStreamResult.get ("PV"));

		mapResult.put ("Rate", dblFairPremium);

		mapResult.put ("ResetDate", mapFloatStreamResult.get ("ResetDate"));

		mapResult.put ("ResetRate", mapFloatStreamResult.get ("ResetRate"));

		mapResult.put ("SwapRate", dblFairPremium);

		mapResult.put ("Upfront", mapFixStreamResult.get ("Upfront") + mapFloatStreamResult.get ("Upfront"));

		double dblValueDate = valParams.valueDate();

		double dblValueNotional = java.lang.Double.NaN;

		try {
			dblValueNotional = notional (dblValueDate);
		} catch (java.lang.Exception e) {
			e.printStackTrace();
		}

		try {
			if (org.drip.quant.common.NumberUtil.IsValid (dblValueNotional)) {
				double dblCleanPrice = 100. * (1. + (dblCleanPV / initialNotional() / dblValueNotional));

				org.drip.analytics.rates.DiscountCurve dcFunding = csqs.fundingCurve
					(couponCurrency()[0]);

				if (null == dcFunding) return null;

				mapResult.put ("Price", dblCleanPrice);

				mapResult.put ("CleanPrice", dblCleanPrice);

				double dblStartDate = effective().julian();

				mapResult.put ("CalibSwapRatePV", (dcFunding.df (dblStartDate > dblValueDate ? dblStartDate :
					dblValueDate) - dcFunding.df (maturity())));

				mapResult.put ("CalibSwapRate", (dcFunding.df (dblStartDate > dblValueDate ? dblStartDate :
					dblValueDate) - dcFunding.df (maturity())) / dblFixedCleanDV01 * notional (dblValueDate)
						* 0.0001);
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

		setstrMeasureNames.add ("CalcTime");

		setstrMeasureNames.add ("CleanFixedDV01");

		setstrMeasureNames.add ("CleanFixedPV");

		setstrMeasureNames.add ("CleanFloatingDV01");

		setstrMeasureNames.add ("CleanFloatingPV");

		setstrMeasureNames.add ("CleanPrice");

		setstrMeasureNames.add ("CleanPV");

		setstrMeasureNames.add ("DirtyFixedDV01");

		setstrMeasureNames.add ("DirtyFixedPV");

		setstrMeasureNames.add ("DirtyFloatingDV01");

		setstrMeasureNames.add ("DirtyFloatingPV");

		setstrMeasureNames.add ("DirtyPV");

		setstrMeasureNames.add ("FairPremium");

		setstrMeasureNames.add ("FixAccrued");

		setstrMeasureNames.add ("FixAccrued01");

		setstrMeasureNames.add ("FixedDV01");

		setstrMeasureNames.add ("Fixing01");

		setstrMeasureNames.add ("FloatAccrued");

		setstrMeasureNames.add ("FloatAccrued01");

		setstrMeasureNames.add ("FloatDV01");

		setstrMeasureNames.add ("ParRate");

		setstrMeasureNames.add ("Price");

		setstrMeasureNames.add ("PV");

		setstrMeasureNames.add ("Rate");

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
			_floatStream.jackDDirtyPVDManifestMeasure (valParams, pricerParams, csqs, quotingParams);

		if (null == jackDDirtyPVDManifestMeasureFloating) return null;

		int iNumQuote = jackDDirtyPVDManifestMeasureFloating.numParameters();

		if (0 == iNumQuote) return null;

		org.drip.quant.calculus.WengertJacobian jackDDirtyPVDManifestMeasureFixed =
			_fixStream.jackDDirtyPVDManifestMeasure (valParams, pricerParams, csqs, quotingParams);

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

				org.drip.analytics.rates.DiscountCurve dcFunding = csqs.fundingCurve
					(couponCurrency()[0]);

				if (null == dcFunding) return null;

				for (org.drip.analytics.period.Period p : cashFlowPeriod()) {
					double dblPeriodPayDate = p.pay();

					if (dblPeriodPayDate < valParams.valueDate()) continue;

					org.drip.quant.calculus.WengertJacobian wjPeriodFwdRateDF =
						dcFunding.jackDForwardDManifestMeasure (p.start(), p.end(), "Rate", p.couponDCF());

					org.drip.quant.calculus.WengertJacobian wjPeriodPayDFDF =
						dcFunding.jackDDFDManifestMeasure (dblPeriodPayDate, "Rate");

					if (null == wjPeriodFwdRateDF || null == wjPeriodPayDFDF) continue;

					double dblForwardRate = dcFunding.libor (p.start(), p.end());

					double dblPeriodPayDF = dcFunding.df (dblPeriodPayDate);

					if (null == wjSwapRateDFMicroJack)
						wjSwapRateDFMicroJack = new org.drip.quant.calculus.WengertJacobian (1,
							wjPeriodFwdRateDF.numParameters());

					double dblPeriodNotional = notional (p.start(), p.end());

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

	private boolean generateFixedLegPRWC (
		final double dblValueDate,
		final double dblInitialDate,
		final org.drip.analytics.rates.RatesLSMM ratesLSMM,
		final org.drip.state.estimator.PredictorResponseWeightConstraint prwc)
	{
		boolean bFirstPeriod = true;

		org.drip.analytics.rates.TurnListDiscountFactor tldf = ratesLSMM.turnsDiscount();

		try {
			for (org.drip.analytics.period.CashflowPeriod period : _fixStream.cashFlowPeriod()) {
				if (null == period) continue;

				double dblPayDate = period.pay();

				if (dblValueDate > dblPayDate) continue;

				double dblPeriodTurnDF = null == tldf ? 1. : tldf.turnAdjust (dblValueDate,
					dblPayDate);

				double dblPeriodDCF = period.couponDCF();

				if (bFirstPeriod) {
					bFirstPeriod = false;

					if (dblValueDate > period.start()) dblPeriodDCF -= period.accrualDCF (dblValueDate);
				}

				double dblPay01 = dblPeriodDCF * dblPeriodTurnDF;

				if (!prwc.addPredictorResponseWeight (dblPayDate, ratesLSMM.measureQuoteValue ("Rate") *
					dblPay01) || !prwc.addDResponseWeightDManifestMeasure ("Rate", dblPayDate, dblPay01))
					return false;
			}

			return true;
		} catch (java.lang.Exception e) {
			e.printStackTrace();
		}

		return false;
	}

	private boolean generateFloatingLegPRWC (
		final double dblValueDate,
		final double dblInitialDate,
		final org.drip.analytics.rates.RatesLSMM ratesLSMM,
		final org.drip.state.estimator.PredictorResponseWeightConstraint prwc)
	{
		double dblMaturityDate = maturity().julian();

		org.drip.analytics.rates.TurnListDiscountFactor tldf = ratesLSMM.turnsDiscount();

		try {
			double dblPeriodMaturityDF = null == tldf ? 1. : tldf.turnAdjust (dblValueDate,
				dblMaturityDate);

			return prwc.addPredictorResponseWeight (dblInitialDate, -1.) &&
				prwc.addPredictorResponseWeight (dblMaturityDate, dblPeriodMaturityDF) &&
					prwc.addDResponseWeightDManifestMeasure ("Rate", dblInitialDate, 0.) &&
						prwc.addDResponseWeightDManifestMeasure ("Rate", dblMaturityDate, 0.);
		} catch (java.lang.Exception e) {
			e.printStackTrace();
		}

		return false;
	}

	@Override public org.drip.state.estimator.PredictorResponseWeightConstraint discountPRWC (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.pricer.PricerParams pricerParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams quotingParams,
		final org.drip.product.calib.ProductQuoteSet pqs)
	{
		if (null == valParams || null == pqs || !(pqs instanceof org.drip.product.calib.FixFloatQuoteSet))
			return null;

		if (valParams.valueDate() >= maturity().julian()) return null;

		double dblPV = 0.;
		org.drip.product.calib.FixFloatQuoteSet ffqs = (org.drip.product.calib.FixFloatQuoteSet) pqs;

		if (!ffqs.containsPV() && !ffqs.containsDerivedBasis() && !ffqs.containsSwapRate()) return null;

		org.drip.product.calib.FloatingStreamQuoteSet fsqsDerived = new
			org.drip.product.calib.FloatingStreamQuoteSet();

		org.drip.product.calib.FixedStreamQuoteSet fsqsReference = new
			org.drip.product.calib.FixedStreamQuoteSet();

		try {
			if (ffqs.containsPV()) dblPV = ffqs.pv();

			if (ffqs.containsDerivedBasis()) fsqsDerived.setSpread (ffqs.derivedBasis());

			if (ffqs.containsSwapRate()) fsqsReference.setCoupon (ffqs.swapRate());
		} catch (java.lang.Exception e) {
			e.printStackTrace();

			return null;
		}

		org.drip.state.estimator.PredictorResponseWeightConstraint prwc = _floatStream.discountPRWC
			(valParams, pricerParams, csqs, quotingParams, fsqsDerived);

		if (null == prwc) return null;

		org.drip.state.estimator.PredictorResponseWeightConstraint prwcReference = _fixStream.discountPRWC
			(valParams, pricerParams, csqs, quotingParams, fsqsReference);

		return null == prwcReference || !prwc.absorb (prwcReference) || !prwc.updateValue (dblPV) ? null :
			prwc;
	}

	@Override public org.drip.state.estimator.PredictorResponseWeightConstraint generateCalibPRWC (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.pricer.PricerParams pricerParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams quotingParams,
		final org.drip.state.representation.LatentStateMetricMeasure lsmm)
	{
		if (null == valParams  || null == lsmm || !(lsmm instanceof org.drip.analytics.rates.RatesLSMM) ||
			!org.drip.analytics.rates.DiscountCurve.LATENT_STATE_DISCOUNT.equalsIgnoreCase (lsmm.id()))
			return null;

		double dblValueDate = valParams.valueDate();

		double dblMaturityDate = maturity().julian();

		if (dblValueDate >= dblMaturityDate) return null;

		double dblEffectiveDate = effective().julian();

		double dblUpfront = 0.;
		org.drip.analytics.rates.RatesLSMM ratesLSMM = (org.drip.analytics.rates.RatesLSMM) lsmm;
		double dblInitialDate = dblEffectiveDate > dblValueDate ? dblEffectiveDate : dblValueDate;

		java.lang.String[] astrManifestMeasure = ratesLSMM.manifestMeasures();

		if (org.drip.quant.common.StringUtil.MatchInStringArray (astrManifestMeasure, new java.lang.String[]
			{"Upfront"}, false)) {
			try {
				dblUpfront = ratesLSMM.measureQuoteValue ("Upfront");
			} catch (java.lang.Exception e) {
				e.printStackTrace();

				return null;
			}
		}

		org.drip.state.estimator.PredictorResponseWeightConstraint prwc = new
			org.drip.state.estimator.PredictorResponseWeightConstraint();

		if (org.drip.analytics.rates.DiscountCurve.QUANTIFICATION_METRIC_DISCOUNT_FACTOR.equalsIgnoreCase
			(ratesLSMM.quantificationMetric())) {
			if (org.drip.quant.common.StringUtil.MatchInStringArray (astrManifestMeasure, new
				java.lang.String[] {"Rate", "SwapRate", "ParRate", "ParSpread", "FairPremium"}, false)) {
				if (!generateFixedLegPRWC (dblValueDate, dblInitialDate, ratesLSMM, prwc)) return null;

				if (!generateFloatingLegPRWC (dblValueDate, dblInitialDate, ratesLSMM, prwc)) return null;
			}
		}

		return prwc.updateValue (dblUpfront) ? prwc : null;
	}

	@Override public java.lang.String fieldDelimiter()
	{
		return "{";
	}

	@Override public java.lang.String objectTrailer()
	{
		return "^";
	}

	@Override public byte[] serialize()
	{
		java.lang.StringBuffer sb = new java.lang.StringBuffer();

		sb.append (org.drip.service.stream.Serializer.VERSION + fieldDelimiter());

		if (null == _fixStream)
			sb.append (org.drip.service.stream.Serializer.NULL_SER_STRING + fieldDelimiter());
		else
			sb.append (new java.lang.String (_fixStream.serialize()) + fieldDelimiter());

		if (null == _floatStream)
			sb.append (org.drip.service.stream.Serializer.NULL_SER_STRING + fieldDelimiter());
		else
			sb.append (new java.lang.String (_floatStream.serialize()));

		return sb.append (objectTrailer()).toString().getBytes();
	}

	@Override public org.drip.service.stream.Serializer deserialize (
		final byte[] ab)
	{
		try {
			return new IRSComponent (ab);
		} catch (java.lang.Exception e) {
			e.printStackTrace();
		}

		return null;
	}

	/**
	 * Retrieve the Fixed Stream
	 * 
	 * @return The Fixed Stream
	 */

	public org.drip.product.stream.FixedStream getFixedStream()
	{
		return _fixStream;
	}

	/**
	 * Retrieve the Floating Stream
	 * 
	 * @return The Floating Stream
	 */

	public org.drip.product.stream.FloatingStream getFloatStream()
	{
		return _floatStream;
	}

	public static final void main (
		final java.lang.String[] astrArgs)
		throws java.lang.Exception
	{
		org.drip.analytics.date.JulianDate dtEffective = org.drip.analytics.date.JulianDate.Today();

		java.util.List<org.drip.analytics.period.CashflowPeriod> lsFixedCouponPeriod =
			org.drip.analytics.period.CashflowPeriod.GeneratePeriodsRegular (dtEffective.julian(), "4Y",
				null, 2, "30/360", false, true, "JPY", "JPY");

		org.drip.product.stream.FixedStream fixStream = new org.drip.product.stream.FixedStream ("JPY", null,
			0.05, 7., null, lsFixedCouponPeriod);

		java.util.List<org.drip.analytics.period.CashflowPeriod> lsFloatCouponPeriod =
			org.drip.analytics.period.CashflowPeriod.GeneratePeriodsRegular (dtEffective.julian(), "4Y",
				null, 4, "Act/360", false, true, "JPY", "JPY");

		org.drip.product.stream.FloatingStream floatStream = new org.drip.product.stream.FloatingStream ("JPY",
			null, 0.01, -7., null, lsFloatCouponPeriod, org.drip.product.params.FloatingRateIndex.Create
				("JPY-LIBOR-3M"), false);

		IRSComponent irs = new org.drip.product.rates.IRSComponent (fixStream, floatStream);

		byte[] abIRS = irs.serialize();

		System.out.println (new java.lang.String (abIRS));

		IRSComponent irsDeser = new IRSComponent (abIRS);

		System.out.println (new java.lang.String (irsDeser.serialize()));
	}
}
