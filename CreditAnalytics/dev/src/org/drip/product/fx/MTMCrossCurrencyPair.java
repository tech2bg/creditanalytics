
package org.drip.product.fx;

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
 * MTMCrossCurrencyPair contains the implementation of the MTM-adjusted dual cross currency components. It is
 *  composed of two different Rates Components - one each for each currency.
 * 
 * @author Lakshmi Krishnamurthy
 */

public class MTMCrossCurrencyPair extends org.drip.product.fx.CrossCurrencyComponentPair {
	private org.drip.product.rates.FloatFloatComponent[] _aFFPForward = null;

	private static final org.drip.product.rates.FloatingStream[] SinglePeriodStreamDecompose (
		final org.drip.product.rates.FloatingStream fs,
		final int iNumPeriodsToAccumulate)
	{
		if (null == fs) return null;

		java.util.List<org.drip.analytics.period.CashflowPeriod> lsCouponFlow = fs.cashFlowPeriod();

		org.drip.product.params.FloatingRateIndex fri = fs.fri();

		java.lang.String strCurrency = fs.couponCurrency()[0];

		double dblInitialNotional = fs.initialNotional();

		int iNumPeriods = lsCouponFlow.size();

		boolean bIsReference = fs.reference();

		double dblSpread = fs.spread();

		int iCFPIndex = 0;
		int iNumPeriodsAccumulated = 0;
		int iNumForward = iNumPeriods / iNumPeriodsToAccumulate;
		double dblNotional = bIsReference ? -1. * dblInitialNotional : dblInitialNotional;
		org.drip.product.rates.FloatingStream[] aFS = new org.drip.product.rates.FloatingStream[iNumForward];

		java.util.List<java.util.List<org.drip.analytics.period.CashflowPeriod>> lslsCouponPeriod = new
			java.util.ArrayList<java.util.List<org.drip.analytics.period.CashflowPeriod>>();

		for (int i = 0; i < iNumForward; ++i)
			lslsCouponPeriod.add (new java.util.ArrayList<org.drip.analytics.period.CashflowPeriod>());

		for (org.drip.analytics.period.CashflowPeriod cfp : lsCouponFlow) {
			java.util.List<org.drip.analytics.period.CashflowPeriod> lsCouponPeriod = lslsCouponPeriod.get
				(iCFPIndex);

			lsCouponPeriod.add (cfp);

			if (++iNumPeriodsAccumulated != iNumPeriodsToAccumulate) continue;

			iNumPeriodsAccumulated = 0;

			try {
				aFS[iCFPIndex++] = new org.drip.product.rates.FloatingStream (strCurrency, dblSpread,
					dblNotional, null, lsCouponPeriod, fri, bIsReference);
			} catch (java.lang.Exception e) {
				e.printStackTrace();

				return null;
			}
		}

		return aFS;
	}

	private static final org.drip.product.rates.FloatFloatComponent[] FloatFloatForwardArray (
		final org.drip.product.rates.FloatFloatComponent ffc)
	{
		if (null == ffc) return null;

		org.drip.product.rates.FloatingStream fsDerived = ffc.getDerivedStream();

		org.drip.product.rates.FloatingStream fsReference = ffc.getReferenceStream();

		int iNumForward = 0;
		int iDerivedStreamTenorMonths = -1;
		int iReferenceStreamTenorMonths = -1;
		org.drip.product.rates.FloatingStream[] aFSDerivedForward = null;
		org.drip.product.rates.FloatingStream[] aFSReferenceForward = null;

		try {
			iDerivedStreamTenorMonths = org.drip.analytics.support.AnalyticsHelper.TenorToMonths
				(fsDerived.fri().tenor());

			iReferenceStreamTenorMonths = org.drip.analytics.support.AnalyticsHelper.TenorToMonths
				(fsReference.fri().tenor());
		} catch (java.lang.Exception e) {
			e.printStackTrace();

			return null;
		}

		if (iReferenceStreamTenorMonths > iDerivedStreamTenorMonths) {
			if (null == (aFSReferenceForward = SinglePeriodStreamDecompose (fsReference, 1))) return null;

			iNumForward = aFSReferenceForward.length;

			if (0 == iNumForward) return null;

			if (null == (aFSDerivedForward = SinglePeriodStreamDecompose (fsDerived,
				iReferenceStreamTenorMonths / iDerivedStreamTenorMonths)) || iNumForward !=
					aFSDerivedForward.length)
				return null;
		} else {
			if (null == (aFSDerivedForward = SinglePeriodStreamDecompose (fsDerived, 1))) return null;

			iNumForward = aFSDerivedForward.length;

			if (0 == iNumForward) return null;

			if (null == (aFSReferenceForward = SinglePeriodStreamDecompose (fsReference,
				iDerivedStreamTenorMonths / iReferenceStreamTenorMonths)) || iNumForward !=
					aFSReferenceForward.length)
				return null;
		}

		org.drip.product.rates.FloatFloatComponent[] aFFP = new
			org.drip.product.rates.FloatFloatComponent[iNumForward];

		for (int i = 0; i < iNumForward; ++i) {
			try {
				(aFFP[i] = new org.drip.product.rates.FloatFloatComponent (aFSReferenceForward[i],
					aFSDerivedForward[i])).setPrimaryCode (fsReference.name() + "::" + fsDerived.name() + "_"
						+ i);
			} catch (java.lang.Exception e) {
				e.printStackTrace();

				return null;
			}
		}

		return aFFP;
	}

	/**
	 * MTMCrossCurrencyPair constructor
	 * 
	 * @param strName The MTMCrossCurrencyPair Instance Name
	 * @param ffcReference The Reference Float-Float Component
	 * @param ffcDerived The Derived Float-Float Component
	 * 
	 * @throws java.lang.Exception Thrown if the Inputs are Invalid
	 */

	public MTMCrossCurrencyPair (
		final java.lang.String strName,
		final org.drip.product.rates.FloatFloatComponent ffcReference,
		final org.drip.product.rates.FloatFloatComponent ffcDerived)
		throws java.lang.Exception
	{
		super (strName, ffcReference, ffcDerived);

		if (null == (_aFFPForward = FloatFloatForwardArray (ffcReference)))
			throw new java.lang.Exception
				("MTMCrossCurrencyPair ctr: Cannot construct the forward FFC strip");
	}

	@Override public org.drip.analytics.support.CaseInsensitiveTreeMap<java.lang.Double> value (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.pricer.PricerParams pricerParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams quotingParams)
	{
		org.drip.analytics.support.CaseInsensitiveTreeMap<java.lang.Double> mapOutput = super.value
			(valParams, pricerParams, csqs, quotingParams);

		if (null == mapOutput) return null;

		double dblMTMPV = 0.;
		double dblMTMCorrectionAdjust = 1.;

		double dblValueDate = valParams.valueDate();

		org.drip.product.params.CurrencyPair cp = org.drip.product.params.CurrencyPair.FromCode (fxCode());

		for (int i = 0; i < _aFFPForward.length; ++i) {
			org.drip.analytics.support.CaseInsensitiveTreeMap<java.lang.Double> mapFwdOutput =
				_aFFPForward[i].value (valParams, pricerParams, csqs, quotingParams);

			java.lang.String strCurrency = _aFFPForward[i].couponCurrency()[0];

			try {
				dblMTMCorrectionAdjust = java.lang.Math.exp
					(org.drip.analytics.support.OptionHelper.IntegratedCrossVolQuanto
						(csqs.fundingCurveVolSurface (strCurrency), csqs.fxCurveVolSurface (cp),
							csqs.fundingFXCorrSurface (strCurrency, cp), dblValueDate,
								_aFFPForward[i].maturity().getJulian()));
			} catch (java.lang.Exception e) {
				e.printStackTrace();

				return null;
			}

			dblMTMPV += mapFwdOutput.get ("PV") * dblMTMCorrectionAdjust;

			mapOutput.put ("MTMAdditiveAdjustment_" + i, (dblMTMCorrectionAdjust - 1.));

			mapOutput.put ("MTMMultiplicativeAdjustment_" + i, dblMTMCorrectionAdjust);
		}

		double dblBasePV = mapOutput.get (referenceComponent().name() + "[PV]");

		double dblMultiplicativeAdjustment = dblMTMPV / dblBasePV;

		mapOutput.put ("MTMAdditiveAdjustment", (dblMultiplicativeAdjustment - 1.));

		mapOutput.put ("MTMMultiplicativeAdjustment", dblMultiplicativeAdjustment);

		mapOutput.put ("MTMPV", dblMTMPV);

		return mapOutput;
	}

	@Override public byte[] serialize()
	{
		return null;
	}

	@Override public org.drip.service.stream.Serializer deserialize (
		final byte[] ab)
	{
		return null;
	}
}
