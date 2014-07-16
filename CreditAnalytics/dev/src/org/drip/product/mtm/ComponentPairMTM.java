
package org.drip.product.mtm;

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
 * ComponentPairMTM implements the valuation for the MTM-adjusted component pair. It provides the default
 *  implementation of dual currency swap.
 * 
 * @author Lakshmi Krishnamurthy
 */

public class ComponentPairMTM extends org.drip.product.definition.BasketProduct {

	private org.drip.product.fx.ComponentPair _dcpBase = null;
	private org.drip.product.definition.RatesComponent[] _aRCDerivedForward = null;
	private org.drip.product.definition.RatesComponent[] _aRCReferenceForward = null;

	private final double ForwardFundingQuanto (
		final org.drip.product.definition.RatesComponent rcForward,
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs)
	{
		if (!(rcForward instanceof org.drip.product.rates.FloatingStream)) return 1.;

		org.drip.product.params.FloatingRateIndex fri = ((org.drip.product.rates.FloatingStream)
			rcForward).fri();

		java.lang.String strCurrency = rcForward.couponCurrency()[0];

		try {
			return java.lang.Math.exp (org.drip.analytics.support.OptionHelper.IntegratedCrossVolQuanto
				(csqs.fundingCurveVolSurface (strCurrency), csqs.forwardCurveVolSurface (fri),
					csqs.forwardFundingCorrSurface (fri, strCurrency), valParams.valueDate(),
						rcForward.maturity().julian()));
		} catch (java.lang.Exception e) {
		}

		return 1.;
	}

	private final double ForwardFXQuanto (
		final org.drip.product.definition.RatesComponent rcForward,
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs)
	{
		if (!(rcForward instanceof org.drip.product.rates.FloatingStream)) return 1.;

		org.drip.product.params.FloatingRateIndex fri = ((org.drip.product.rates.FloatingStream)
			rcForward).fri();

		org.drip.product.params.CurrencyPair cp = org.drip.product.params.CurrencyPair.FromCode
			(_dcpBase.fxCode());

		try {
			return null == cp ? 1. : java.lang.Math.exp
				(org.drip.analytics.support.OptionHelper.IntegratedCrossVolQuanto
					(csqs.forwardCurveVolSurface (fri), csqs.fxCurveVolSurface (cp),
						csqs.forwardFXCorrSurface (fri, cp), valParams.valueDate(),
							rcForward.maturity().julian()));
		} catch (java.lang.Exception e) {
		}

		return 1.;
	}

	private final double FundingFXQuanto (
		final org.drip.product.definition.RatesComponent rcForward,
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs)
	{
		org.drip.product.params.CurrencyPair cp = org.drip.product.params.CurrencyPair.FromCode
			(_dcpBase.fxCode());

		java.lang.String strCurrency = rcForward.couponCurrency()[0];

		try {
			return null == cp ? 1. : java.lang.Math.exp
				(org.drip.analytics.support.OptionHelper.IntegratedCrossVolQuanto
					(csqs.fundingCurveVolSurface (strCurrency), csqs.fxCurveVolSurface (cp),
						csqs.fundingFXCorrSurface (strCurrency, cp), valParams.valueDate(),
							rcForward.maturity().julian()));
		} catch (java.lang.Exception e) {
		}

		return 1.;
	}

	protected double forwardComponentPVAdjustment (
		final org.drip.product.definition.RatesComponent rcForward,
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.pricer.PricerParams pricerParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs)
		throws java.lang.Exception
	{
		if (null == pricerParams || !(pricerParams instanceof org.drip.param.pricer.JointStatePricerParams))
			return 1.;

		org.drip.param.pricer.JointStatePricerParams jspp = (org.drip.param.pricer.JointStatePricerParams)
			pricerParams;

		int iQuantoAdjustment = jspp.quantoAdjustment();

		if (org.drip.param.pricer.JointStatePricerParams.QUANTO_ADJUSTMENT_NONE == iQuantoAdjustment)
			return 1.;

		if (null == rcForward || null == valParams || null == csqs)
			throw new java.lang.Exception
				("ComponentPairMTM::forwardComponentPVAdjustment => Invalid Inputs");

		if (org.drip.param.pricer.JointStatePricerParams.QUANTO_ADJUSTMENT_FORWARD_FUNDING ==
			iQuantoAdjustment)
			return ForwardFundingQuanto (rcForward, valParams, csqs);

		if (org.drip.param.pricer.JointStatePricerParams.QUANTO_ADJUSTMENT_FORWARD_FX == iQuantoAdjustment)
			return ForwardFXQuanto (rcForward, valParams, csqs);

		if (org.drip.param.pricer.JointStatePricerParams.QUANTO_ADJUSTMENT_FUNDING_FX == iQuantoAdjustment)
			return FundingFXQuanto (rcForward, valParams, csqs);

		if (org.drip.param.pricer.JointStatePricerParams.QUANTO_ADJUSTMENT_FORWARD_FUNDING_FX ==
			iQuantoAdjustment)
			return ForwardFundingQuanto (rcForward, valParams, csqs) * ForwardFXQuanto (rcForward, valParams,
				csqs) * FundingFXQuanto (rcForward, valParams, csqs);

		return 1.;
	}

	/**
	 * ComponentPairMTM constructor - Make a ComponentPairMTM instance from the Base Component Pair Instance
	 * 
	 * @param dcpBase The Base Component Pair
	 * @param bIsMTMAbsolute TRUE => The MTM is computed on an Absolute Basis
	 * 
	 * @throws java.lang.Exception Thrown if the Inputs are invalid
	 */

	public ComponentPairMTM (
		final org.drip.product.fx.ComponentPair dcpBase,
		final boolean bIsMTMAbsolute)
		throws java.lang.Exception
	{
		if (null == (_dcpBase = dcpBase))
			throw new java.lang.Exception ("ComponentPairMTM ctr: Invalid Inputs");

		if (null == (_aRCReferenceForward =
			org.drip.product.mtm.ForwardDecompositionUtil.RatesComponentForwardArray
				(_dcpBase.referenceComponent())) || 0 == _aRCReferenceForward.length)
			throw new java.lang.Exception
				("ComponentPairMTM ctr: Cannot construct Reference Forward Component Strip");

		if (bIsMTMAbsolute && (null == (_aRCDerivedForward =
			org.drip.product.mtm.ForwardDecompositionUtil.RatesComponentForwardArray
				(_dcpBase.derivedComponent())) || 0 == _aRCDerivedForward.length))
			throw new java.lang.Exception
				("ComponentPairMTM ctr: Cannot construct Derived Forward Component Strip");
	}

	@Override public java.lang.String name()
	{
		return "MTM::" + _dcpBase.name();
	}

	@Override public java.lang.String[] currencyPairCode()
	{
		return _dcpBase.currencyPairCode();
	}

	@Override public int measureAggregationType (
		final java.lang.String strMeasureName)
	{
		return _dcpBase.measureAggregationType (strMeasureName);
	}

	@Override public java.util.Set<java.lang.String> cashflowCurrencySet()
	{
		java.util.Set<java.lang.String> setstrCurrency = new java.util.TreeSet<java.lang.String>();

		for (org.drip.product.definition.RatesComponent rc : _aRCReferenceForward)
			setstrCurrency.addAll (rc.cashflowCurrencySet());

		return setstrCurrency;
	}

	@Override public org.drip.product.definition.FixedIncomeComponent[] components()
	{
		return _aRCReferenceForward;
	}

	@Override public org.drip.analytics.support.CaseInsensitiveTreeMap<java.lang.Double> value (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.pricer.PricerParams pricerParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams quotingParams)
	{
		org.drip.analytics.support.CaseInsensitiveTreeMap<java.lang.Double> mapOutput = _dcpBase.value
			(valParams, pricerParams, csqs, quotingParams);

		if (null == mapOutput) return null;

		double dblMTMDerivedPV = 0.;
		double dblMTMReferencePV = 0.;
		double dblMTMDerivedCorrectionAdjust = 1.;
		double dblMTMReferenceCorrectionAdjust = 1.;

		for (int i = 0; i < _aRCReferenceForward.length; ++i) {
			org.drip.analytics.support.CaseInsensitiveTreeMap<java.lang.Double> mapFwdOutput =
				_aRCReferenceForward[i].value (valParams, pricerParams, csqs, quotingParams);

			try {
				dblMTMReferenceCorrectionAdjust = forwardComponentPVAdjustment (_aRCReferenceForward[i],
					valParams, pricerParams, csqs);
			} catch (java.lang.Exception e) {
				e.printStackTrace();

				dblMTMReferenceCorrectionAdjust = 1.;
			}

			dblMTMReferencePV += mapFwdOutput.get ("PV") * dblMTMReferenceCorrectionAdjust;

			mapOutput.put ("ReferenceMTMAdditiveAdjustment_" + i, (dblMTMReferenceCorrectionAdjust - 1.));

			mapOutput.put ("ReferenceMTMMultiplicativeAdjustment_" + i, dblMTMReferenceCorrectionAdjust);
		}

		if (null != _aRCDerivedForward) {
			for (int i = 0; i < _aRCDerivedForward.length; ++i) {
				org.drip.analytics.support.CaseInsensitiveTreeMap<java.lang.Double> mapFwdOutput =
					_aRCDerivedForward[i].value (valParams, pricerParams, csqs, quotingParams);

				try {
					dblMTMDerivedCorrectionAdjust = forwardComponentPVAdjustment (_aRCDerivedForward[i],
						valParams, pricerParams, csqs);
				} catch (java.lang.Exception e) {
					e.printStackTrace();

					dblMTMDerivedCorrectionAdjust = 1.;
				}

				dblMTMDerivedPV += mapFwdOutput.get ("PV") * dblMTMDerivedCorrectionAdjust;

				mapOutput.put ("DerivedMTMAdditiveAdjustment_" + i, (dblMTMDerivedCorrectionAdjust - 1.));

				mapOutput.put ("DerivedMTMMultiplicativeAdjustment_" + i, dblMTMDerivedCorrectionAdjust);
			}
		} else
			dblMTMDerivedPV = mapOutput.get (_dcpBase.derivedComponent().name() + "[PV]");

		double dblBaseReferencePV = mapOutput.get (_dcpBase.referenceComponent().name() + "[PV]");

		double dblBaseDerivedPV = mapOutput.get (_dcpBase.derivedComponent().name() + "[PV]");

		double dblReferenceMultiplicativeAdjustment = (0. == dblBaseReferencePV && dblMTMReferencePV ==
			dblBaseReferencePV) ? 1. : dblMTMReferencePV / dblBaseReferencePV;
		double dblDerivedMultiplicativeAdjustment = null == _aRCDerivedForward || (0. == dblBaseDerivedPV &&
			dblMTMDerivedPV == dblBaseDerivedPV) ? 1. : dblMTMDerivedPV / dblBaseDerivedPV;

		mapOutput.put ("ReferenceMTMAdditiveAdjustment", (dblReferenceMultiplicativeAdjustment - 1.));

		mapOutput.put ("ReferenceMTMMultiplicativeAdjustment", dblReferenceMultiplicativeAdjustment);

		mapOutput.put ("DerivedMTMAdditiveAdjustment", (dblDerivedMultiplicativeAdjustment - 1.));

		mapOutput.put ("DerivedMTMMultiplicativeAdjustment", dblDerivedMultiplicativeAdjustment);

		mapOutput.put ("MTMPV", dblMTMReferencePV + dblMTMDerivedPV);

		return mapOutput;
	}

	/**
	 * Retrieve the Base MTM Basket Product Instance
	 * 
	 * @return The Base MTM Basket Product Instance
	 */

	public org.drip.product.definition.BasketProduct base()
	{
		return _dcpBase;
	}

	/**
	 * Retrieve the FX Code
	 * 
	 * @return The FX Code
	 */

	public java.lang.String fxCode()
	{
		return _dcpBase.fxCode();
	}

	/**
	 * Is the MTM Valuation off of Leg Absolute?
	 * 
	 * @return TRUE => MTM Valuation is off of Leg Absolute
	 */

	public boolean isAbsolute()
	{
		return null != _aRCDerivedForward;
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
