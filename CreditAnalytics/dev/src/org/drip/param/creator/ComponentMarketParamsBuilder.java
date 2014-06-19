
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
	 * Create a CMP with the funding discount curve, the forward discount curve, the treasury discount curve,
	 *  the credit curve, the component quote, the map of treasury benchmark quotes, and the double map of
	 *  date/rate index and fixings.
	 * 
	 * @param dcFunding Funding Discount Curve
	 * @param fc Forward Curve
	 * @param dcTSY Treasury Discount Curve
	 * @param cc Credit Curve
	 * @param strComponentCode Component Code
	 * @param compQuote Component quote
	 * @param mTSYQuotes Map of Treasury Benchmark Quotes
	 * @param mmFixings Double map of date/rate index and fixings
	 * 
	 * @return Instance of ComponentMarketParams
	 */

	public static final org.drip.param.definition.ComponentMarketParams Create (
		final org.drip.analytics.rates.DiscountCurve dcFunding,
		final org.drip.analytics.rates.ForwardCurve fc,
		final org.drip.analytics.rates.DiscountCurve dcTSY,
		final org.drip.analytics.definition.CreditCurve cc,
		final java.lang.String strComponentCode,
		final org.drip.param.definition.ComponentQuote compQuote,
		final org.drip.analytics.support.CaseInsensitiveTreeMap<org.drip.param.definition.ComponentQuote>
			mTSYQuotes,
		final java.util.Map<org.drip.analytics.date.JulianDate,
			org.drip.analytics.support.CaseInsensitiveTreeMap<java.lang.Double>> mmFixings)
	{
		org.drip.param.market.ComponentMarketParamSet cmp = new
			org.drip.param.market.ComponentMarketParamSet();

		if (null != cc && !cmp.setCreditCurve (cc)) return null;

		if (null != dcTSY && !cmp.setGovvieCurve (dcTSY)) return null;

		if (null != compQuote && null != strComponentCode && !strComponentCode.isEmpty() &&
			!cmp.setComponentQuote (strComponentCode, compQuote))
			return null;

		if (null != dcFunding && !cmp.setFundingCurve (dcFunding)) return null;

		if (null != mmFixings && !cmp.setFixings (mmFixings)) return null;

		if (null != mTSYQuotes && !cmp.setComponentQuoteMap (mTSYQuotes)) return null;

		if (null != fc && !cmp.setForwardCurve (fc)) return null;

		return cmp;
	}

	/**
	 * Create a CMP with the rates discount curve alone
	 * 
	 * @param dc Rates Discount Curve
	 * 
	 * @return CMP Instance
	 */

	public static final org.drip.param.definition.ComponentMarketParams MakeDiscountCMP (
		final org.drip.analytics.rates.DiscountCurve dc)
	{
		return Create (dc, null, null, null, "", null, null, null);
	}

	/**
	 * Create a CMP with the discount curve and the forward Curve
	 * 
	 * @param dc Discount Curve
	 * @param fc Forward Curve
	 * 
	 * @return CMP Instance
	 */

	public static final org.drip.param.definition.ComponentMarketParams MakeFloaterDiscountCMP (
		final org.drip.analytics.rates.DiscountCurve dc,
		final org.drip.analytics.rates.ForwardCurve fc)
	{
		return Create (dc, fc, null, null, "", null, null, null);
	}

	/**
	 * Create a CMP with the rates discount curve and the treasury discount curve alone
	 * 
	 * @param dc Rates Discount Curve
	 * @param dcTSY Treasury Discount Curve
	 * 
	 * @return The CMP Instance
	 */

	public static final org.drip.param.definition.ComponentMarketParams MakeDiscountCMP (
		final org.drip.analytics.rates.DiscountCurve dc,
		final org.drip.analytics.rates.DiscountCurve dcTSY)
	{
		return Create (dc, null, dcTSY, null, "", null, null, null);
	}

	/**
	 * Create a CMP with the discount curve and the credit curve
	 * 
	 * @param dc Discount Curve
	 * @param cc Credit Curve
	 * 
	 * @return The CMP Instance
	 */

	public static final org.drip.param.definition.ComponentMarketParams MakeCreditCMP (
		final org.drip.analytics.rates.DiscountCurve dc,
		final org.drip.analytics.definition.CreditCurve cc)
	{
		return Create (dc, null, null, cc, "", null, null, null);
	}

	/**
	 * Create a CMP with the rates discount curve, the treasury discount curve, the credit curve, the
	 *  component quote, the map of treasury benchmark quotes, and the double map of date/rate index and
	 *  fixings
	 * 
	 * @param dc Rates Discount Curve
	 * @param dcTSY Treasury Discount Curve
	 * @param cc Credit Curve
	 * @param strComponentCode Component Code
	 * @param compQuote Component quote
	 * @param mTSYQuotes Map of Treasury Benchmark Quotes
	 * @param mmFixings Double map of date/rate index and fixings
	 * 
	 * @return The CMP Instance
	 */

	public static final org.drip.param.definition.ComponentMarketParams CreateComponentMarketParams (
		final org.drip.analytics.rates.DiscountCurve dc,
		final org.drip.analytics.rates.DiscountCurve dcTSY,
		final org.drip.analytics.definition.CreditCurve cc,
		final java.lang.String strComponentCode,
		final org.drip.param.definition.ComponentQuote compQuote,
		final org.drip.analytics.support.CaseInsensitiveTreeMap<org.drip.param.definition.ComponentQuote>
			mTSYQuotes,
		final java.util.Map<org.drip.analytics.date.JulianDate,
			org.drip.analytics.support.CaseInsensitiveTreeMap<java.lang.Double>> mmFixings)
	{
		return Create (dc, null, dcTSY, cc, strComponentCode, compQuote, mTSYQuotes, mmFixings);
	}

	/**
	 * Create a CMP with the rates discount curve, the forward discount curve, the treasury discount curve,
	 *  the credit curve, the component quote, the map of treasury benchmark quotes, and the double map of
	 *  date/rate index and fixings
	 * 
	 * @param dc Discount Curve
	 * @param fc Forward Curve
	 * @param dcTSY Treasury Discount Curve
	 * @param cc Credit Curve
	 * @param strComponentCode Component Code
	 * @param compQuote Component quote
	 * @param mTSYQuotes Map of Treasury Benchmark Quotes
	 * @param mmFixings Double map of date/rate index and fixings
	 * 
	 * @return Instance of ComponentMarketParams
	 */

	public static final org.drip.param.definition.ComponentMarketParams CreateComponentMarketParams (
		final org.drip.analytics.rates.DiscountCurve dc,
		final org.drip.analytics.rates.ForwardCurve fc,
		final org.drip.analytics.rates.DiscountCurve dcTSY,
		final org.drip.analytics.definition.CreditCurve cc,
		final java.lang.String strComponentCode,
		final org.drip.param.definition.ComponentQuote compQuote,
		final org.drip.analytics.support.CaseInsensitiveTreeMap<org.drip.param.definition.ComponentQuote>
			mTSYQuotes,
		final java.util.Map<org.drip.analytics.date.JulianDate,
			org.drip.analytics.support.CaseInsensitiveTreeMap<java.lang.Double>> mmFixings)
	{
		return Create (dc, fc, dcTSY, cc, strComponentCode, compQuote, mTSYQuotes, mmFixings);
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
