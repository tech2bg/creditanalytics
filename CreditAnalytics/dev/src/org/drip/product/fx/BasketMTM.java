
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
 * BasketMTM is the abstract class that contains the implementation of the MTM-adjusted basket products.
 * 
 * @author Lakshmi Krishnamurthy
 */

public class BasketMTM extends org.drip.product.definition.BasketProduct {
	private org.drip.product.fx.CrossCurrencyComponentPair _bpBase = null;
	private org.drip.product.definition.RatesComponent[] _aRCForward = null;

	protected double forwardMTMPVAdjustment (
		final int i,
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs)
		throws java.lang.Exception
	{
		if (null == valParams || null == csqs)
			throw new java.lang.Exception ("BasketMTM::forwardMTMPVAdjustment => Invalid Inputs");

		org.drip.product.params.CurrencyPair cp = org.drip.product.params.CurrencyPair.FromCode
			(_bpBase.fxCode());

		if (null == cp)
			throw new java.lang.Exception ("BasketMTM::forwardMTMPVAdjustment => Cannot get Currency Pair");

		java.lang.String strCurrency = _aRCForward[i].couponCurrency()[0];

		return java.lang.Math.exp (org.drip.analytics.support.OptionHelper.IntegratedCrossVolQuanto
			(csqs.fundingCurveVolSurface (strCurrency), csqs.fxCurveVolSurface (cp),
				csqs.fundingFXCorrSurface (strCurrency, cp), valParams.valueDate(),
					_aRCForward[i].maturity().getJulian()));
	}

	/**
	 * BasketMTM constructor - Construct a BasketMTM instance from the Base Dual Dual Stream Basket Product
	 * 	Instance
	 * 
	 * @param bpBase the Base Dual Dual Stream Basket Product
	 * 
	 * @throws java.lang.Exception Thrown if the Inputs are invalid
	 */

	public BasketMTM (
		final org.drip.product.fx.CrossCurrencyComponentPair bpBase)
		throws java.lang.Exception
	{
		if (null == (_bpBase = bpBase)) throw new java.lang.Exception ("BasketMTM ctr: Invalid Inputs");

		if (null == (_aRCForward = org.drip.product.mtm.ForwardDecompositionUtil.DualStreamForwardArray
			((org.drip.product.rates.DualStreamComponent) _bpBase.referenceComponent())))
			throw new java.lang.Exception ("BasketMTM ctr: Cannot construct the forward Dual Stream Strip");
	}

	@Override public java.lang.String name()
	{
		return "MTM::" + _bpBase.name();
	}

	@Override public java.lang.String[] currencyPairCode()
	{
		return _bpBase.currencyPairCode();
	}

	@Override public int measureAggregationType (
		final java.lang.String strMeasureName)
	{
		return _bpBase.measureAggregationType (strMeasureName);
	}

	@Override public java.util.Set<java.lang.String> cashflowCurrencySet()
	{
		java.util.Set<java.lang.String> setstrCurrency = new java.util.TreeSet<java.lang.String>();

		for (org.drip.product.definition.RatesComponent rc : _aRCForward)
			setstrCurrency.addAll (rc.cashflowCurrencySet());

		return setstrCurrency;
	}

	@Override public org.drip.product.definition.FixedIncomeComponent[] components()
	{
		return _aRCForward;
	}

	@Override public org.drip.analytics.support.CaseInsensitiveTreeMap<java.lang.Double> value (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.pricer.PricerParams pricerParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams quotingParams)
	{
		org.drip.analytics.support.CaseInsensitiveTreeMap<java.lang.Double> mapOutput = _bpBase.value
			(valParams, pricerParams, csqs, quotingParams);

		if (null == mapOutput) return null;

		double dblMTMPV = 0.;
		double dblMTMCorrectionAdjust = 1.;

		for (int i = 0; i < _aRCForward.length; ++i) {
			org.drip.analytics.support.CaseInsensitiveTreeMap<java.lang.Double> mapFwdOutput =
				_aRCForward[i].value (valParams, pricerParams, csqs, quotingParams);

			try {
				dblMTMCorrectionAdjust = forwardMTMPVAdjustment (i, valParams, csqs);
			} catch (java.lang.Exception e) {
				e.printStackTrace();

				return null;
			}

			dblMTMPV += mapFwdOutput.get ("PV") * dblMTMCorrectionAdjust;

			mapOutput.put ("MTMAdditiveAdjustment_" + i, (dblMTMCorrectionAdjust - 1.));

			mapOutput.put ("MTMMultiplicativeAdjustment_" + i, dblMTMCorrectionAdjust);
		}

		double dblBasePV = mapOutput.get (_bpBase.referenceComponent().name() + "[PV]");

		double dblMultiplicativeAdjustment = dblMTMPV / dblBasePV;

		mapOutput.put ("MTMAdditiveAdjustment", (dblMultiplicativeAdjustment - 1.));

		mapOutput.put ("MTMMultiplicativeAdjustment", dblMultiplicativeAdjustment);

		mapOutput.put ("MTMPV", dblMTMPV);

		return mapOutput;
	}

	/**
	 * Retrieve the Base MTM Basket Product Instance
	 * 
	 * @return The Base MTM Basket Product Instance
	 */

	public org.drip.product.definition.BasketProduct base()
	{
		return _bpBase;
	}

	/**
	 * Retrieve the FX Code
	 * 
	 * @return The FX Code
	 */

	public java.lang.String fxCode()
	{
		return _bpBase.fxCode();
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
