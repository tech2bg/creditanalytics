
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
	private org.drip.product.definition.RatesComponent[] _aRCForward = null;

	/**
	 * MTMCrossCurrencyPair constructor
	 * 
	 * @param strName The MTMCrossCurrencyPair Instance Name
	 * @param dscReference The Reference Dual Stream Component
	 * @param dscDerived The Derived Dual Stream Component
	 * 
	 * @throws java.lang.Exception Thrown if the Inputs are Invalid
	 */

	public MTMCrossCurrencyPair (
		final java.lang.String strName,
		final org.drip.product.rates.DualStreamComponent dscReference,
		final org.drip.product.rates.DualStreamComponent dscDerived)
		throws java.lang.Exception
	{
		super (strName, dscReference, dscDerived);

		if (null == (_aRCForward = org.drip.product.mtm.ForwardDecompositionUtil.DualStreamForwardArray
			(dscReference)))
			throw new java.lang.Exception
				("MTMCrossCurrencyPair ctr: Cannot construct the forward Dual Stream Strip");
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

		for (int i = 0; i < _aRCForward.length; ++i) {
			org.drip.analytics.support.CaseInsensitiveTreeMap<java.lang.Double> mapFwdOutput =
				_aRCForward[i].value (valParams, pricerParams, csqs, quotingParams);

			java.lang.String strCurrency = _aRCForward[i].couponCurrency()[0];

			try {
				dblMTMCorrectionAdjust = java.lang.Math.exp
					(org.drip.analytics.support.OptionHelper.IntegratedCrossVolQuanto
						(csqs.fundingCurveVolSurface (strCurrency), csqs.fxCurveVolSurface (cp),
							csqs.fundingFXCorrSurface (strCurrency, cp), dblValueDate,
							_aRCForward[i].maturity().getJulian()));
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
