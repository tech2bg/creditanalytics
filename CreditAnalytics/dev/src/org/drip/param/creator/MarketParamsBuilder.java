
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
 * MarketParamsBuilder implements the various ways of constructing, de-serializing, and building the Market
 *  Parameters.
 * 
 * @author Lakshmi Krishnamurthy
 */

public class MarketParamsBuilder {

	/**
	 * Create a Market Parameters instance with the funding discount curve, the forward discount curve, the
	 *  treasury discount curve, the credit curve, the component quote, the map of treasury benchmark quotes,
	 *  and the Latent State Fixings Instance.
	 * 
	 * @param dcFunding Funding Discount Curve
	 * @param fc Forward Curve
	 * @param dcTSY Treasury Discount Curve
	 * @param cc Credit Curve
	 * @param strComponentCode Component Code
	 * @param compQuote Component quote
	 * @param mTSYQuotes Map of Treasury Benchmark Quotes
	 * @param lsfc The Latent State Fixings Instance
	 * 
	 * @return Market Parameters Instance
	 */

	public static final org.drip.param.market.CurveSurfaceQuoteSet Create (
		final org.drip.analytics.rates.DiscountCurve dcFunding,
		final org.drip.analytics.rates.ForwardCurve fc,
		final org.drip.analytics.rates.DiscountCurve dcTSY,
		final org.drip.analytics.definition.CreditCurve cc,
		final java.lang.String strComponentCode,
		final org.drip.param.definition.ProductQuote compQuote,
		final org.drip.analytics.support.CaseInsensitiveTreeMap<org.drip.param.definition.ProductQuote>
			mTSYQuotes,
		final org.drip.param.market.LatentStateFixingsContainer lsfc)
	{
		org.drip.param.market.CurveSurfaceQuoteSet csqs = new
			org.drip.param.market.CurveSurfaceQuoteSet();

		if (null != cc && !csqs.setCreditCurve (cc)) return null;

		if (null != dcTSY && !csqs.setGovvieCurve (dcTSY)) return null;

		if (null != lsfc && !csqs.setFixings (lsfc)) return null;

		if (null != dcFunding && !csqs.setFundingCurve (dcFunding)) return null;

		if (null != mTSYQuotes && !csqs.setQuoteMap (mTSYQuotes)) return null;

		if (null != compQuote && null != strComponentCode && !strComponentCode.isEmpty() &&
			!csqs.setProductQuote (strComponentCode, compQuote))
			return null;

		if (null != fc && !csqs.setForwardCurve (fc)) return null;

		return csqs;
	}

	/**
	 * Create a Market Parameters instance with the rates discount curve alone
	 * 
	 * @param dc Rates Discount Curve
	 * 
	 * @return Market Parameters instance
	 */

	public static final org.drip.param.market.CurveSurfaceQuoteSet Discount (
		final org.drip.analytics.rates.DiscountCurve dc)
	{
		return Create (dc, null, null, null, "", null, null, null);
	}

	/**
	 * Create a Market Parameters instance with the discount curve and the forward Curve
	 * 
	 * @param dc Discount Curve
	 * @param fc Forward Curve
	 * 
	 * @return Market Parameters instance
	 */

	public static final org.drip.param.market.CurveSurfaceQuoteSet DiscountForward (
		final org.drip.analytics.rates.DiscountCurve dc,
		final org.drip.analytics.rates.ForwardCurve fc)
	{
		return Create (dc, fc, null, null, "", null, null, null);
	}

	/**
	 * Create a Market Parameters instance with the rates discount curve and the treasury discount curve alone
	 * 
	 * @param dc Rates Discount Curve
	 * @param dcTSY Treasury Discount Curve
	 * 
	 * @return Market Parameters instance
	 */

	public static final org.drip.param.market.CurveSurfaceQuoteSet Govvie (
		final org.drip.analytics.rates.DiscountCurve dc,
		final org.drip.analytics.rates.DiscountCurve dcTSY)
	{
		return Create (dc, null, dcTSY, null, "", null, null, null);
	}

	/**
	 * Create a Market Parameters Instance with the discount curve and the credit curve
	 * 
	 * @param dc Discount Curve
	 * @param cc Credit Curve
	 * 
	 * @return The Market Parameters Instance
	 */

	public static final org.drip.param.market.CurveSurfaceQuoteSet Credit (
		final org.drip.analytics.rates.DiscountCurve dc,
		final org.drip.analytics.definition.CreditCurve cc)
	{
		return Create (dc, null, null, cc, "", null, null, null);
	}

	/**
	 * Create a Market Parameters Instance with the rates discount curve, the treasury discount curve, the
	 *  credit curve, the component quote, the map of treasury benchmark quotes, and the Latent State Fixings
	 *  Container
	 * 
	 * @param dc Rates Discount Curve
	 * @param dcTSY Treasury Discount Curve
	 * @param cc Credit Curve
	 * @param strComponentCode Component Code
	 * @param compQuote Component quote
	 * @param mTSYQuotes Map of Treasury Benchmark Quotes
	 * @param lsfc Latent State Fixings Container
	 * 
	 * @return Market Parameters Instance
	 */

	public static final org.drip.param.market.CurveSurfaceQuoteSet Create (
		final org.drip.analytics.rates.DiscountCurve dc,
		final org.drip.analytics.rates.DiscountCurve dcTSY,
		final org.drip.analytics.definition.CreditCurve cc,
		final java.lang.String strComponentCode,
		final org.drip.param.definition.ProductQuote compQuote,
		final org.drip.analytics.support.CaseInsensitiveTreeMap<org.drip.param.definition.ProductQuote>
			mTSYQuotes,
		final org.drip.param.market.LatentStateFixingsContainer lsfc)
	{
		return Create (dc, null, dcTSY, cc, strComponentCode, compQuote, mTSYQuotes, lsfc);
	}

	/**
	 * Create MarketParams from the array of calibration instruments
	 * 
	 * @return MarketParams object
	 */

	public static final org.drip.param.definition.ScenarioMarketParams CreateMarketParams()
	{
		try {
			return new org.drip.param.market.ScenarioMarketParamsContainer();
		} catch (java.lang.Exception e) {
			e.printStackTrace();
		}

		return null;
	}

	/**
	 * Create a Market Parameter Instance from the byte array
	 * 
	 * @param ab Byte Array
	 * 
	 * @return Market Parameter Instance
	 */

	public static final org.drip.param.market.CurveSurfaceQuoteSet FromByteArray (
		final byte[] ab)
	{
		if (null == ab || 0 == ab.length) return null;

		try {
			return new org.drip.param.market.CurveSurfaceQuoteSet (ab);
		} catch (java.lang.Exception e) {
			e.printStackTrace();
		}

		return null;
	}
}
