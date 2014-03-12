
package org.drip.param.creator;

/*
 * -*- mode: java; tab-width: 4; indent-tabs-mode: nil; c-basic-offset: 4 -*-
 */

/*!
 * Copyright (C) 2014 Lakshmi Krishnamurthy
 * Copyright (C) 2013 Lakshmi Krishnamurthy
 * Copyright (C) 2012 Lakshmi Krishnamurthy
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
 * ComponentMarketParamsBuilder implements the various ways of constructing, de-serializing, and building the
 *  Component Market Parameters.
 * 
 * @author Lakshmi Krishnamurthy
 */

public class ComponentMarketParamsBuilder {

	/**
	 * Create a CMP with the rates discount curve alone
	 * 
	 * @param dc Rates Discount Curve
	 * 
	 * @return CMP
	 */

	public static final org.drip.param.definition.ComponentMarketParams MakeDiscountCMP (
		final org.drip.analytics.rates.DiscountCurve dc)
	{
		return new org.drip.param.market.ComponentMarketParamSet (dc, null, null, null, null, null, null,
			null, null);
	}

	/**
	 * Create a CMP with the discount curve and the forward Curve
	 * 
	 * @param dc Discount Curve
	 * @param fc Forward Curve
	 * 
	 * @return CMP
	 */

	public static final org.drip.param.definition.ComponentMarketParams MakeFloaterDiscountCMP (
		final org.drip.analytics.rates.DiscountCurve dc,
		final org.drip.analytics.rates.ForwardCurve fc)
	{
		return new org.drip.param.market.ComponentMarketParamSet (dc, null, fc, null, null, null, null, null,
			null);
	}

	/**
	 * Create a CMP with the rates discount curve and the treasury discount curve alone
	 * 
	 * @param dc Rates Discount Curve
	 * @param dcTSY Treasury Discount Curve
	 * 
	 * @return CMP
	 */

	public static final org.drip.param.definition.ComponentMarketParams MakeDiscountCMP (
		final org.drip.analytics.rates.DiscountCurve dc,
		final org.drip.analytics.rates.DiscountCurve dcTSY)
	{
		return new org.drip.param.market.ComponentMarketParamSet (dc, null, null, dcTSY, null, null, null,
			null, null);
	}

	/**
	 * Create a CMP with the rates discount curve, the treasury discount curve, and the EDSF discount curve
	 * 
	 * @param dc Rates Discount Curve
	 * @param dcTSY Treasury Discount Curve
	 * @param dcEDSF EDSF Discount Curve
	 * 
	 * @return CMP
	 */

	public static final org.drip.param.definition.ComponentMarketParams MakeDiscountCMP (
		final org.drip.analytics.rates.DiscountCurve dc,
		final org.drip.analytics.rates.DiscountCurve dcTSY,
		final org.drip.analytics.rates.DiscountCurve dcEDSF)
	{
		return new org.drip.param.market.ComponentMarketParamSet (dc, null, null, dcTSY, dcEDSF, null, null,
			null, null);
	}

	/**
	 * Create a CMP with the discount curve and the credit curve
	 * 
	 * @param dc Discount Curve
	 * @param cc Credit Curve
	 * 
	 * @return CMP
	 */

	public static final org.drip.param.definition.ComponentMarketParams MakeCreditCMP (
		final org.drip.analytics.rates.DiscountCurve dc,
		final org.drip.analytics.definition.CreditCurve cc)
	{
		return new org.drip.param.market.ComponentMarketParamSet (dc, null, null, null, null, cc, null, null,
			null);
	}

	/**
	 * Create a CMP with the rates discount curve, the treasury discount curve, the EDSF discount curve, the
	 * 	credit curve, the component quote, the map of treasury benchmark quotes, and the double map of
	 * 	date/rate index and fixings
	 * 
	 * @param dc Rates Discount Curve
	 * @param dcTSY Treasury Discount Curve
	 * @param dcEDSF EDSF Discount Curve
	 * @param cc Credit Curve
	 * @param compQuote Component quote
	 * @param mTSYQuotes Map of Treasury Benchmark Quotes
	 * @param mmFixings Double map of date/rate index and fixings
	 */

	public static final org.drip.param.definition.ComponentMarketParams CreateComponentMarketParams (
		final org.drip.analytics.rates.DiscountCurve dc,
		final org.drip.analytics.rates.DiscountCurve dcTSY,
		final org.drip.analytics.rates.DiscountCurve dcEDSF,
		final org.drip.analytics.definition.CreditCurve cc,
		final org.drip.param.definition.ComponentQuote compQuote,
		final org.drip.analytics.support.CaseInsensitiveTreeMap<org.drip.param.definition.ComponentQuote>
			mTSYQuotes,
		final java.util.Map<org.drip.analytics.date.JulianDate,
			org.drip.analytics.support.CaseInsensitiveTreeMap<java.lang.Double>> mmFixings)
	{
		try {
			return new org.drip.param.market.ComponentMarketParamSet (dc, null, null, dcTSY, dcEDSF, cc,
				compQuote, mTSYQuotes, mmFixings);
		} catch (java.lang.Exception e) {
			e.printStackTrace();
		}

		return null;
	}

	/**
	 * Create a CMP with the rates discount curve, the forward discount curve, the treasury discount curve,
	 *  the EDSF discount curve, the credit curve, the component quote, the map of treasury benchmark quotes,
	 *  and the double map of date/rate index and fixings
	 * 
	 * @param dc Discount Curve
	 * @param fc Forward Curve
	 * @param dcTSY Treasury Discount Curve
	 * @param dcEDSF EDSF Discount Curve
	 * @param cc Credit Curve
	 * @param compQuote Component quote
	 * @param mTSYQuotes Map of Treasury Benchmark Quotes
	 * @param mmFixings Double map of date/rate index and fixings
	 */

	public static final org.drip.param.definition.ComponentMarketParams CreateComponentMarketParams (
		final org.drip.analytics.rates.DiscountCurve dc,
		final org.drip.analytics.rates.ForwardCurve fc,
		final org.drip.analytics.rates.DiscountCurve dcTSY,
		final org.drip.analytics.rates.DiscountCurve dcEDSF,
		final org.drip.analytics.definition.CreditCurve cc,
		final org.drip.param.definition.ComponentQuote compQuote,
		final org.drip.analytics.support.CaseInsensitiveTreeMap<org.drip.param.definition.ComponentQuote>
			mTSYQuotes,
		final java.util.Map<org.drip.analytics.date.JulianDate,
			org.drip.analytics.support.CaseInsensitiveTreeMap<java.lang.Double>> mmFixings)
	{
		try {
			return new org.drip.param.market.ComponentMarketParamSet (dc, null, fc, dcTSY, dcEDSF, cc,
				compQuote, mTSYQuotes, mmFixings);
		} catch (java.lang.Exception e) {
			e.printStackTrace();
		}

		return null;
	}

	/**
	 * Create a Component Market Parameter Instance from the byte array
	 * 
	 * @param ab Byte Array
	 * 
	 * @return Component Market Parameter Instance
	 */

	public static final org.drip.param.definition.ComponentMarketParams FromByteArray (
		final byte[] ab)
	{
		if (null == ab || 0 == ab.length) return null;

		try {
			return new org.drip.param.market.ComponentMarketParamSet (ab);
		} catch (java.lang.Exception e) {
			e.printStackTrace();
		}

		return null;
	}
}
