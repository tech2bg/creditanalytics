
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
 * CrossCurrencyComponentPair contains the implementation of the dual cross currency components. It is
 *  componsed of two different Rates Components - one each for each currency.
 * 
 * @author Lakshmi Krishnamurthy
 */

public class CrossCurrencyComponentPair extends org.drip.product.definition.BasketProduct {
	private java.lang.String _strName = "";
	private org.drip.product.definition.RatesComponent _rcDerived = null;
	private org.drip.product.definition.RatesComponent _rcReference = null;

	protected int measureAggregationType (
		final java.lang.String strMeasureName)
	{
		return org.drip.product.definition.BasketProduct.MEASURE_AGGREGATION_TYPE_UNIT_ACCUMULATE;
	}

	/**
	 * CrossCurrencyComponentPair constructor
	 * 
	 * @param strName The CrossCurrencyBasisSwap Instance Name
	 * @param rcReference The Reference Component
	 * @param rcDerived The Derived Component
	 * 
	 * @throws java.lang.Exception Thrown if the Inputs are Invalid
	 */

	public CrossCurrencyComponentPair (
		final java.lang.String strName,
		final org.drip.product.definition.RatesComponent rcReference,
		final org.drip.product.definition.RatesComponent rcDerived)
		throws java.lang.Exception
	{
		if (null == (_strName = strName) || _strName.isEmpty() || null == (_rcDerived = rcDerived) || null ==
			(_rcReference = rcReference))
			throw new java.lang.Exception ("CrossCurrencyComponentPair ctr: Invalid Inputs!");
	}

	/**
	 * Retrieve the Reference Component
	 * 
	 * @return The Reference Component
	 */

	public org.drip.product.definition.RatesComponent referenceComponent()
	{
		return _rcReference;
	}

	/**
	 * Retrieve the Derived Component
	 * 
	 * @return The Derived Component
	 */

	public org.drip.product.definition.RatesComponent derivedComponent()
	{
		return _rcDerived;
	}

	@Override public java.lang.String getName()
	{
		return _strName;
	}

	@Override public java.util.Set<java.lang.String> cashflowCurrencySet()
	{
		java.util.Set<java.lang.String> setstrCurrency = new java.util.TreeSet<java.lang.String>();

		setstrCurrency.addAll (_rcReference.cashflowCurrencySet());

		setstrCurrency.addAll (_rcDerived.cashflowCurrencySet());

		return setstrCurrency;
	}

	@Override public java.util.Set<java.lang.String> getComponentCreditCurveNames()
	{
		java.util.Set<java.lang.String> setstrCredit = new java.util.TreeSet<java.lang.String>();

		setstrCredit.add (_rcReference.creditCurveName());

		setstrCredit.add (_rcDerived.creditCurveName());

		return setstrCredit;
	}

	@Override public org.drip.analytics.support.CaseInsensitiveTreeMap<java.lang.Double> value (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.pricer.PricerParams pricerParams,
		final org.drip.param.definition.BasketMarketParams bmp,
		final org.drip.param.valuation.ValuationCustomizationParams quotingParams)
	{
		long lStart = System.nanoTime();

		org.drip.analytics.support.CaseInsensitiveTreeMap<java.lang.Double> mapOutput = super.value
			(valParams, pricerParams, bmp, quotingParams);

		if (null == mapOutput) return mapOutput;

		java.lang.String strRefCompName = _rcDerived.componentName();

		double dblFX = 1.;
		java.lang.String strDerivedCleanDV01 = strRefCompName + "[DerivedCleanDV01]";
		java.lang.String strDerivedParBasisSpread = strRefCompName + "[DerivedParBasisSpread]";

		if (!mapOutput.containsKey (strDerivedCleanDV01) || !mapOutput.containsKey
			(strDerivedParBasisSpread)) {
			mapOutput.put ("CalcTime", (System.nanoTime() - lStart) * 1.e-09);

			return mapOutput;
		}

		double dblDerivedCleanDV01 = mapOutput.get (strDerivedCleanDV01);

		double dblDerivedParBasisSpread = mapOutput.get (strDerivedParBasisSpread);

		double dblReferencePV = mapOutput.get (_rcReference.componentName() + "[PV]");

		org.drip.quant.function1D.AbstractUnivariate auFX = bmp.fxCurve (_rcDerived.couponCurrency()[0] +
			"/" + _rcReference.couponCurrency()[0]);

		if (null != auFX) {
			try {
				dblFX = auFX.evaluate (getEffectiveDate().getJulian());
			} catch (java.lang.Exception e) {
				e.printStackTrace();

				return null;
			}
		}

		mapOutput.put ("DerivedParCurrencyBasis", dblDerivedParBasisSpread + (dblFX * dblReferencePV /
			dblDerivedCleanDV01));

		mapOutput.put ("CalcTime", (System.nanoTime() - lStart) * 1.e-09);

		return mapOutput;
	}

	@Override public org.drip.product.definition.FixedIncomeComponent[] getComponents()
	{
		return new org.drip.product.definition.FixedIncomeComponent[] {_rcReference, _rcDerived};
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
