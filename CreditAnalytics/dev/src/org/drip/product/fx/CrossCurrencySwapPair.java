
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
 * CrossCurrencyBasisSwap contains the implementation of the dual currency cross swaps. It is composed of two
 * 	different Rates Components - one each for each currency.
 * 
 * @author Lakshmi Krishnamurthy
 */

public class CrossCurrencySwapPair extends org.drip.product.definition.BasketProduct {
	private java.lang.String _strName = "";
	private org.drip.product.rates.FloatFloatComponent _ffcDerived = null;
	private org.drip.product.rates.FloatFloatComponent _ffcReference = null;

	protected int measureAggregationType (
		final java.lang.String strMeasureName)
	{
		return org.drip.product.definition.BasketProduct.MEASURE_AGGREGATION_TYPE_UNIT_ACCUMULATE;
	}

	/**
	 * CrossCurrencyBasisSwap constructor
	 * 
	 * @param strName The CrossCurrencyBasisSwap Instance Name
	 * @param ffcReference The Reference Swap
	 * @param ffcDerived The Derived Swap
	 * 
	 * @throws java.lang.Exception Thrown if the Inputs are Invalid
	 */

	public CrossCurrencySwapPair (
		final java.lang.String strName,
		final org.drip.product.rates.FloatFloatComponent ffcReference,
		final org.drip.product.rates.FloatFloatComponent ffcDerived)
		throws java.lang.Exception
	{
		if (null == (_strName = strName) || _strName.isEmpty() || null == (_ffcDerived = ffcDerived) || null
			== (_ffcReference = ffcReference))
			throw new java.lang.Exception ("CrossCurrencyBasisSwap ctr: Invalid Inputs!");
	}

	/**
	 * Retrieve the Reference Swap
	 * 
	 * @return The Reference Swap
	 */

	public org.drip.product.rates.FloatFloatComponent getReferenceSwap()
	{
		return _ffcReference;
	}

	/**
	 * Retrieve the Derived Swap
	 * 
	 * @return The Derived Swap
	 */

	public org.drip.product.rates.FloatFloatComponent getDerivedSwap()
	{
		return _ffcDerived;
	}

	@Override public java.lang.String getName()
	{
		return _strName;
	}

	@Override public java.util.Set<java.lang.String> cashflowCurrencySet()
	{
		java.util.Set<java.lang.String> setstrCurrency = new java.util.TreeSet<java.lang.String>();

		setstrCurrency.addAll (_ffcReference.cashflowCurrencySet());

		setstrCurrency.addAll (_ffcDerived.cashflowCurrencySet());

		return setstrCurrency;
	}

	@Override public java.util.Set<java.lang.String> getComponentCreditCurveNames()
	{
		java.util.Set<java.lang.String> setstrCredit = new java.util.TreeSet<java.lang.String>();

		setstrCredit.add (_ffcReference.creditCurveName());

		setstrCredit.add (_ffcDerived.creditCurveName());

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

		mapOutput.put ("CalcTime", (System.nanoTime() - lStart) * 1.e-09);

		return mapOutput;
	}

	@Override public org.drip.product.definition.FixedIncomeComponent[] getComponents()
	{
		return new org.drip.product.definition.FixedIncomeComponent[] {_ffcReference, _ffcDerived};
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
