
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
 * ComponentPair contains the implementation of the dual cross currency components. It is composed of two
 *  different Rates Components - one each for each currency.
 * 
 * @author Lakshmi Krishnamurthy
 */

public class ComponentPair extends org.drip.product.definition.BasketProduct {
	private java.lang.String _strName = "";
	private org.drip.product.definition.CalibratableFixedIncomeComponent _rcDerived = null;
	private org.drip.product.definition.CalibratableFixedIncomeComponent _rcReference = null;

	/**
	 * ComponentPair constructor
	 * 
	 * @param strName The ComponentPair Instance Name
	 * @param rcReference The Reference Component
	 * @param rcDerived The Derived Component
	 * 
	 * @throws java.lang.Exception Thrown if the Inputs are Invalid
	 */

	public ComponentPair (
		final java.lang.String strName,
		final org.drip.product.definition.CalibratableFixedIncomeComponent rcReference,
		final org.drip.product.definition.CalibratableFixedIncomeComponent rcDerived)
		throws java.lang.Exception
	{
		if (null == (_strName = strName) || _strName.isEmpty() || null == (_rcDerived = rcDerived) || null ==
			(_rcReference = rcReference))
			throw new java.lang.Exception ("ComponentPair ctr: Invalid Inputs!");
	}

	/**
	 * Retrieve the Reference Component
	 * 
	 * @return The Reference Component
	 */

	public org.drip.product.definition.CalibratableFixedIncomeComponent referenceComponent()
	{
		return _rcReference;
	}

	/**
	 * Retrieve the Derived Component
	 * 
	 * @return The Derived Component
	 */

	public org.drip.product.definition.CalibratableFixedIncomeComponent derivedComponent()
	{
		return _rcDerived;
	}

	/**
	 * Retrieve the FX Code
	 * 
	 * @return The FX Code
	 */

	public java.lang.String fxCode()
	{
		java.lang.String strDerivedComponentCouponCurrency = _rcDerived.couponCurrency()[0];

		java.lang.String strReferenceComponentCouponCurrency = _rcReference.couponCurrency()[0];

		return strDerivedComponentCouponCurrency.equalsIgnoreCase (strReferenceComponentCouponCurrency) ?
			null : strReferenceComponentCouponCurrency + "/" + strDerivedComponentCouponCurrency;
	}

	@Override public java.lang.String name()
	{
		return _strName;
	}

	@Override public org.drip.state.identifier.FXLabel[] fxLabel()
	{
		java.lang.String strReferenceCurrency = _rcReference.couponCurrency()[0];

		java.lang.String strDerivedCurrency = _rcDerived.couponCurrency()[0];

		return new org.drip.state.identifier.FXLabel[] {org.drip.state.identifier.FXLabel.Standard
			(strReferenceCurrency + "/" + strDerivedCurrency), org.drip.state.identifier.FXLabel.Standard
				(strDerivedCurrency + "/" + strReferenceCurrency)};
	}

	@Override public java.util.Set<java.lang.String> cashflowCurrencySet()
	{
		java.util.Set<java.lang.String> setstrCurrency = new java.util.TreeSet<java.lang.String>();

		setstrCurrency.addAll (_rcReference.cashflowCurrencySet());

		setstrCurrency.addAll (_rcDerived.cashflowCurrencySet());

		return setstrCurrency;
	}

	@Override public org.drip.product.definition.FixedIncomeComponent[] components()
	{
		return new org.drip.product.definition.FixedIncomeComponent[] {_rcReference, _rcDerived};
	}

	@Override public int measureAggregationType (
		final java.lang.String strMeasureName)
	{
		return org.drip.product.definition.BasketProduct.MEASURE_AGGREGATION_TYPE_UNIT_ACCUMULATE;
	}

	@Override public org.drip.analytics.support.CaseInsensitiveTreeMap<java.lang.Double> value (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.pricer.PricerParams pricerParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams quotingParams)
	{
		long lStart = System.nanoTime();

		org.drip.analytics.support.CaseInsensitiveTreeMap<java.lang.Double> mapOutput = super.value
			(valParams, pricerParams, csqs, quotingParams);

		if (null == mapOutput) return null;

		org.drip.product.definition.CalibratableFixedIncomeComponent rcReference = referenceComponent();

		org.drip.product.definition.CalibratableFixedIncomeComponent rcDerived = derivedComponent();

		java.lang.String strReferenceCompName = rcReference.name();

		java.lang.String strDerivedCompName = rcDerived.name();

		java.lang.String strDerivedCompPV = strDerivedCompName + "[PV]";
		java.lang.String strReferenceCompPV = strReferenceCompName + "[PV]";
		java.lang.String strDerivedCompDerivedDV01 = strDerivedCompName + "[DerivedCleanDV01]";
		java.lang.String strReferenceCompDerivedDV01 = strReferenceCompName + "[DerivedCleanDV01]";
		java.lang.String strDerivedCompReferenceDV01 = strDerivedCompName + "[ReferenceCleanDV01]";
		java.lang.String strReferenceCompReferenceDV01 = strReferenceCompName + "[ReferenceCleanDV01]";
		java.lang.String strDerivedCompConvexityPremium = strDerivedCompName +
			"[ConvexityAdjustmentPremium]";
		java.lang.String strDerivedCompConvexityAdjustment = strDerivedCompName +
			"[ConvexityAdjustmentFactor]";
		java.lang.String strReferenceCompConvexityPremium = strReferenceCompName +
			"[ConvexityAdjustmentPremium]";
		java.lang.String strReferenceCompConvexityAdjustment = strReferenceCompName +
			"[QuantoAdjustmentFactor]";

		if (!mapOutput.containsKey (strDerivedCompPV) || !mapOutput.containsKey (strReferenceCompPV) ||
			!mapOutput.containsKey (strReferenceCompReferenceDV01) || !mapOutput.containsKey
				(strReferenceCompDerivedDV01) || !mapOutput.containsKey (strDerivedCompReferenceDV01) ||
					!mapOutput.containsKey (strDerivedCompDerivedDV01) || !mapOutput.containsKey
						(strDerivedCompConvexityPremium) || !mapOutput.containsKey
							(strReferenceCompConvexityPremium)) {
			mapOutput.put ("CalcTime", (System.nanoTime() - lStart) * 1.e-09);

			return mapOutput;
		}

		double dblDerivedCompPV = mapOutput.get (strDerivedCompPV);

		double dblReferenceCompPV = mapOutput.get (strReferenceCompPV);

		double dblDerivedCompDerivedDV01 = mapOutput.get (strDerivedCompDerivedDV01);

		double dblDerivedCompReferenceDV01 = mapOutput.get (strDerivedCompReferenceDV01);

		double dblReferenceCompDerivedDV01 = mapOutput.get (strReferenceCompDerivedDV01);

		double dblReferenceCompReferenceDV01 = mapOutput.get (strReferenceCompReferenceDV01);

		mapOutput.put ("ReferenceCompReferenceBasis", -1. * (dblDerivedCompPV + dblReferenceCompPV) /
			dblReferenceCompReferenceDV01);

		mapOutput.put ("ReferenceCompDerivedBasis", -1. * (dblDerivedCompPV + dblReferenceCompPV) /
			dblReferenceCompDerivedDV01);

		mapOutput.put ("DerivedCompReferenceBasis", -1. * (dblDerivedCompPV + dblReferenceCompPV) /
			dblDerivedCompReferenceDV01);

		mapOutput.put ("DerivedCompDerivedBasis", -1. * (dblDerivedCompPV + dblReferenceCompPV) /
			dblDerivedCompDerivedDV01);

		if (mapOutput.containsKey (strReferenceCompConvexityAdjustment))
			mapOutput.put ("ReferenceConvexityAdjustmentFactor", mapOutput.get
				(strReferenceCompConvexityAdjustment));

		double dblReferenceConvexityAdjustmentPremium = mapOutput.get (strReferenceCompConvexityPremium);

		mapOutput.put ("ReferenceConvexityAdjustmentPremium", dblReferenceConvexityAdjustmentPremium);

		if (mapOutput.containsKey (strDerivedCompConvexityAdjustment))
			mapOutput.put ("DerivedConvexityAdjustmentFactor", mapOutput.get
				(strDerivedCompConvexityAdjustment));

		double dblDerivedConvexityAdjustmentPremium = mapOutput.get (strDerivedCompConvexityPremium);

		mapOutput.put ("DerivedConvexityAdjustmentPremium", dblDerivedConvexityAdjustmentPremium);

		try {
			mapOutput.put ("ConvexityAdjustmentPremium", _rcReference.initialNotional() *
				dblReferenceConvexityAdjustmentPremium + _rcDerived.initialNotional() *
					dblDerivedConvexityAdjustmentPremium);
		} catch (java.lang.Exception e) {
			e.printStackTrace();

			return null;
		}

		mapOutput.put ("CalcTime", (System.nanoTime() - lStart) * 1.e-09);

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
