
package org.drip.market.otc;

/*
 * -*- mode: java; tab-width: 4; indent-tabs-mode: nil; c-basic-offset: 4 -*-
 */

/*!
 * Copyright (C) 2015 Lakshmi Krishnamurthy
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
 * FloatFloatConvention contains the Details of the Float-Float Component of an OTC contact.
 *
 * @author Lakshmi Krishnamurthy
 */

public class FloatFloatConvention {
	private int _iSpotLag = -1;
	private boolean _bIsComponentPair = false;
	private java.lang.String _strCurrency = "";
	private boolean _bBasisOnDerivedSide = true;
	private java.lang.String _strReferenceTenor = "";
	private boolean _bIsDerivedCompoundedToReference = true;

	private org.drip.product.rates.FixFloatComponent fixFloatComponent (
		final org.drip.analytics.date.JulianDate dtEffective,
		final java.lang.String strFloaterTenor,
		final java.lang.String strMaturityTenor,
		final double dblFixedCoupon,
		final double dblFloaterBasis,
		final double dblNotional)
	{
		org.drip.market.otc.FixFloatConvention ffConv =
			org.drip.market.otc.FixFloatConventionContainer.ConventionFromJurisdiction (_strCurrency);

		if (null == ffConv) return null;

		org.drip.market.otc.FixFloatFixedConvention fixedConv = ffConv.fixedStreamConvention();

		org.drip.product.rates.Stream streamFixed = fixedConv.createStream (dtEffective, strMaturityTenor,
			dblFixedCoupon, dblNotional);

		org.drip.state.identifier.ForwardLabel forwardLabel = org.drip.state.identifier.ForwardLabel.Create
			(org.drip.market.definition.IBORIndexContainer.IndexFromJurisdiction (_strCurrency),
				strFloaterTenor);

		/* java.lang.String strFloaterTenorComposite = _bIsDerivedCompoundedToReference ?
			fixedConv.compositePeriodTenor() : strFloaterTenor; */

		java.lang.String strFloaterTenorComposite = strFloaterTenor;

		try {
			org.drip.param.period.ComposableFloatingUnitSetting cfus = new
				org.drip.param.period.ComposableFloatingUnitSetting (strFloaterTenor,
					org.drip.analytics.support.CompositePeriodBuilder.EDGE_DATE_SEQUENCE_REGULAR, null,
						forwardLabel,
							org.drip.analytics.support.CompositePeriodBuilder.REFERENCE_PERIOD_IN_ADVANCE,
								dblFloaterBasis);

			org.drip.param.period.CompositePeriodSetting cps = new
				org.drip.param.period.CompositePeriodSetting
					(org.drip.analytics.support.AnalyticsHelper.TenorToFreq (strFloaterTenorComposite),
						strFloaterTenorComposite, _strCurrency, null, -1. * dblNotional, null, null, null,
							null);

			java.util.List<java.lang.Double> lsEdgeDate =
				org.drip.analytics.support.CompositePeriodBuilder.RegularEdgeDates (dtEffective,
					strFloaterTenorComposite, strMaturityTenor, null);

			org.drip.product.rates.Stream streamFloater = new org.drip.product.rates.Stream
				(org.drip.analytics.support.CompositePeriodBuilder.FloatingCompositeUnit (lsEdgeDate, cps,
					cfus));

			org.drip.product.rates.FixFloatComponent ffc = new org.drip.product.rates.FixFloatComponent
				(streamFixed, streamFloater, null);

			ffc.setPrimaryCode ("IRS::" + ffc.forwardLabel().get ("DERIVED").fullyQualifiedName() + "." +
				strMaturityTenor);

			return ffc;
		} catch (java.lang.Exception e) {
			e.printStackTrace();
		}

		return null;
	}

	/**
	 * FloatFloatConvention Constructor
	 * 
	 * @param strCurrency The Currency
	 * @param strReferenceTenor The Reference Tenor
	 * @param bBasisOnDerivedSide TRUE => Apply the Basis to the Derived Side
	 * @param bIsDerivedCompoundedToReference TRUE => The Derived Periods are Compounded onto the Reference
	 * @param bIsComponentPair TRUE => The Float-Float Swap is a Component Pair of 2 Fix-Float Swaps
	 * @param iSpotLag Spot Lag
	 * 
	 * @throws java.lang.Exception Thrown if the Inputs are Invalid
	 */

	public FloatFloatConvention (
		final java.lang.String strCurrency,
		final java.lang.String strReferenceTenor,
		final boolean bBasisOnDerivedSide,
		final boolean bIsDerivedCompoundedToReference,
		final boolean bIsComponentPair,
		final int iSpotLag)
		throws java.lang.Exception
	{
		if (null == (_strCurrency = strCurrency) || _strCurrency.isEmpty() || null == (_strReferenceTenor =
			strReferenceTenor) || _strReferenceTenor.isEmpty() || 0 > (_iSpotLag = iSpotLag))
			throw new java.lang.Exception ("FloatFloatConvention ctr: Invalid Inputs");

		_bIsComponentPair = bIsComponentPair;
		_bBasisOnDerivedSide = bBasisOnDerivedSide;
		_bIsDerivedCompoundedToReference = bIsDerivedCompoundedToReference;
	}

	/**
	 * Retrieve the Currency
	 * 
	 * @return The Currency
	 */

	public java.lang.String currency()
	{
		return _strCurrency;
	}

	/**
	 * Retrieve the Reference Tenor
	 * 
	 * @return The Reference Tenor
	 */

	public java.lang.String referenceTenor()
	{
		return _strReferenceTenor;
	}

	/**
	 * Retrieve the Flag indicating whether the Basis is to be applied to the Derived or the Reference Side
	 * 
	 * @return TRUE => The Basis is applied to the Derived Side
	 */

	public boolean basisOnDerivedSide()
	{
		return _bBasisOnDerivedSide;
	}

	/**
	 * Retrieve the Flag indicating whether the Derived Periods are to be compounded onto the Reference
	 *  Period
	 * 
	 * @return TRUE => The Derived Periods are Compounded onto the Reference
	 */

	public boolean derivedCompoundedToReference()
	{
		return _bIsDerivedCompoundedToReference;
	}

	/**
	 * Retrieve the Flag indicating whether the Float-Float Swap is a Component Pair of 2 Fix-Float Swaps
	 * 
	 * @return TRUE => The Float-Float Swap is a Component Pair of 2 Fix-Float Swaps
	 */

	public boolean componentPair()
	{
		return _bIsComponentPair;
	}

	/**
	 * Retrieve the Spot Lag
	 * 
	 * @return The Spot Lag
	 */

	public int spotLag()
	{
		return _iSpotLag;
	}

	/**
	 * Create an Instance of the Float-Float Component
	 * 
	 * @param dtSpot Spot Date
	 * @param strDerivedTenor The Derived Tenor
	 * @param strMaturityTenor The Maturity Tenor
	 * @param dblBasis Basis
	 * @param dblNotional Notional
	 * 
	 * @return Instance of the Float-Float Component
	 */

	public org.drip.product.rates.FloatFloatComponent createFloatFloatComponent (
		final org.drip.analytics.date.JulianDate dtSpot,
		final java.lang.String strDerivedTenor,
		final java.lang.String strMaturityTenor,
		final double dblBasis,
		final double dblNotional)
	{
		if (_bIsComponentPair || null == dtSpot) return null;

		org.drip.state.identifier.ForwardLabel forwardLabelReference =
			org.drip.state.identifier.ForwardLabel.Create
				(org.drip.market.definition.IBORIndexContainer.IndexFromJurisdiction (_strCurrency),
					_strReferenceTenor);

		if (null == forwardLabelReference) return null;

		org.drip.analytics.date.JulianDate dtEffective = dtSpot.addBusDays (_iSpotLag,
			forwardLabelReference.floaterIndex().calendar());

		org.drip.state.identifier.ForwardLabel forwardLabelDerived =
			org.drip.state.identifier.ForwardLabel.Create
				(org.drip.market.definition.IBORIndexContainer.IndexFromJurisdiction (_strCurrency),
					strDerivedTenor);

		if (null == forwardLabelDerived) return null;

		java.lang.String strDerivedTenorComposite = _bIsDerivedCompoundedToReference ? _strReferenceTenor :
			strDerivedTenor;

		try {
			org.drip.param.period.ComposableFloatingUnitSetting cfusReference = new
				org.drip.param.period.ComposableFloatingUnitSetting (_strReferenceTenor,
					org.drip.analytics.support.CompositePeriodBuilder.EDGE_DATE_SEQUENCE_REGULAR, null,
						forwardLabelReference,
							org.drip.analytics.support.CompositePeriodBuilder.REFERENCE_PERIOD_IN_ADVANCE,
								!_bBasisOnDerivedSide ? dblBasis : 0.);

			org.drip.param.period.CompositePeriodSetting cpsReference = new
				org.drip.param.period.CompositePeriodSetting
					(org.drip.analytics.support.AnalyticsHelper.TenorToFreq (_strReferenceTenor),
						_strReferenceTenor, _strCurrency, null, dblNotional, null, null, null, null);

			java.util.List<java.lang.Double> lsReferenceEdgeDate =
				org.drip.analytics.support.CompositePeriodBuilder.RegularEdgeDates (dtEffective,
					_strReferenceTenor, strMaturityTenor, null);

			org.drip.product.rates.Stream streamReference = new org.drip.product.rates.Stream
				(org.drip.analytics.support.CompositePeriodBuilder.FloatingCompositeUnit
					(lsReferenceEdgeDate, cpsReference, cfusReference));

			org.drip.param.period.ComposableFloatingUnitSetting cfusDerived = new
				org.drip.param.period.ComposableFloatingUnitSetting (strDerivedTenor,
					org.drip.analytics.support.CompositePeriodBuilder.EDGE_DATE_SEQUENCE_REGULAR, null,
						forwardLabelDerived,
							org.drip.analytics.support.CompositePeriodBuilder.REFERENCE_PERIOD_IN_ADVANCE,
								_bBasisOnDerivedSide ? dblBasis : 0.);

			org.drip.param.period.CompositePeriodSetting cpsDerived = new
				org.drip.param.period.CompositePeriodSetting
					(org.drip.analytics.support.AnalyticsHelper.TenorToFreq (strDerivedTenorComposite),
						strDerivedTenorComposite, _strCurrency, null, -1. * dblNotional, null, null, null,
							null);

			java.util.List<java.lang.Double> lsDerivedEdgeDate =
				org.drip.analytics.support.CompositePeriodBuilder.RegularEdgeDates (dtEffective,
					strDerivedTenor, strMaturityTenor, null);

			org.drip.product.rates.Stream streamDerived = new org.drip.product.rates.Stream
				(org.drip.analytics.support.CompositePeriodBuilder.FloatingCompositeUnit (lsDerivedEdgeDate,
					cpsDerived, cfusDerived));

			return new org.drip.product.rates.FloatFloatComponent (streamReference, streamDerived, null);
		} catch (java.lang.Exception e) {
			e.printStackTrace();
		}

		return null;
	}

	/**
	 * Create an Instance of the Fix-Float Component Pair
	 * 
	 * @param dtSpot Spot Date
	 * @param strDerivedTenor The Derived Tenor
	 * @param strMaturityTenor The Maturity Tenor
	 * @param dblReferenceFixedCoupon Fixed Coupon Rate for the Reference Component
	 * @param dblDerivedFixedCoupon Fixed Coupon Rate for the Derived Component
	 * @param dblBasis Basis
	 * @param dblNotional Notional
	 * 
	 * @return Instance of the Fix-Float Component Pair
	 */

	public org.drip.product.fx.ComponentPair createFixFloatComponentPair (
		final org.drip.analytics.date.JulianDate dtSpot,
		final java.lang.String strDerivedTenor,
		final java.lang.String strMaturityTenor,
		final double dblReferenceFixedCoupon,
		final double dblDerivedFixedCoupon,
		final double dblBasis,
		final double dblNotional)
	{
		if (!_bIsComponentPair || null == dtSpot) return null;

		org.drip.market.definition.IBORIndex floaterIndex =
			org.drip.market.definition.IBORIndexContainer.IndexFromJurisdiction (_strCurrency);

		if (null == floaterIndex) return null;

		org.drip.analytics.date.JulianDate dtEffective = dtSpot.addBusDays (_iSpotLag,
			floaterIndex.calendar());

		org.drip.product.rates.FixFloatComponent ffcReference = fixFloatComponent (dtEffective,
			_strReferenceTenor, strMaturityTenor, dblReferenceFixedCoupon, !_bBasisOnDerivedSide ? dblBasis :
				0., dblNotional);

		org.drip.product.rates.FixFloatComponent ffcDerived = fixFloatComponent (dtEffective,
			strDerivedTenor, strMaturityTenor, dblDerivedFixedCoupon, _bBasisOnDerivedSide ? dblBasis : 0.,
				-1. * dblNotional);

		try {
			return new org.drip.product.fx.ComponentPair (_strCurrency + "::" + _strReferenceTenor + "/" +
				strDerivedTenor + "_" + strMaturityTenor, ffcReference, ffcDerived, null);
		} catch (java.lang.Exception e) {
			e.printStackTrace();
		}

		return null;
	}
}
