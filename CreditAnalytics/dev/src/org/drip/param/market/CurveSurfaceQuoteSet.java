
package org.drip.param.market;

/*
 * -*- mode: java; tab-width: 4; indent-tabs-mode: nil; c-basic-offset: 4 -*-
 */

/*!
 * Copyright (C) 2014 Lakshmi Krishnamurthy
 * Copyright (C) 2013 Lakshmi Krishnamurthy
 * Copyright (C) 2012 Lakshmi Krishnamurthy
 * Copyright (C) 2011 Lakshmi Krishnamurthy
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
 * CurveSurfaceQuoteSet provides implementation of the set of the market curve parameters. It serves as a
 *  place holder for the market parameters needed to value the product – discount curve, forward curve,
 *  treasury curve, credit curve, product quote, treasury quote map, and fixings map.
 *
 * @author Lakshmi Krishnamurthy
 */

public class CurveSurfaceQuoteSet extends org.drip.service.stream.Serializer {
	private
		org.drip.analytics.support.CaseInsensitiveTreeMap<org.drip.analytics.support.CaseInsensitiveTreeMap<org.drip.analytics.rates.DiscountCurve>>
			_mapPayCurrencyForeignCollateralDC = new
				org.drip.analytics.support.CaseInsensitiveTreeMap<org.drip.analytics.support.CaseInsensitiveTreeMap<org.drip.analytics.rates.DiscountCurve>>();

	private org.drip.analytics.support.CaseInsensitiveTreeMap<org.drip.analytics.definition.CreditCurve>
		_mapCreditCurve = new
			org.drip.analytics.support.CaseInsensitiveTreeMap<org.drip.analytics.definition.CreditCurve>();

	private org.drip.analytics.support.CaseInsensitiveTreeMap<org.drip.analytics.rates.ForwardCurve>
		_mapForwardCurve = new
			org.drip.analytics.support.CaseInsensitiveTreeMap<org.drip.analytics.rates.ForwardCurve>();

	private org.drip.analytics.support.CaseInsensitiveTreeMap<org.drip.analytics.rates.DiscountCurve>
		_mapFundingCurve = new
			org.drip.analytics.support.CaseInsensitiveTreeMap<org.drip.analytics.rates.DiscountCurve>();

	private org.drip.analytics.support.CaseInsensitiveTreeMap<org.drip.quant.function1D.AbstractUnivariate>
		_mapFXCurve = new
			org.drip.analytics.support.CaseInsensitiveTreeMap<org.drip.quant.function1D.AbstractUnivariate>();

	private org.drip.analytics.support.CaseInsensitiveTreeMap<org.drip.analytics.rates.DiscountCurve>
		_mapGovvieCurve = new
			org.drip.analytics.support.CaseInsensitiveTreeMap<org.drip.analytics.rates.DiscountCurve>();

	private org.drip.analytics.support.CaseInsensitiveTreeMap<org.drip.quant.function1D.AbstractUnivariate>
		_mapCollateralVolatilitySurface = new
			org.drip.analytics.support.CaseInsensitiveTreeMap<org.drip.quant.function1D.AbstractUnivariate>();

	private org.drip.analytics.support.CaseInsensitiveTreeMap<org.drip.quant.function1D.AbstractUnivariate>
		_mapCreditVolatilitySurface = new
			org.drip.analytics.support.CaseInsensitiveTreeMap<org.drip.quant.function1D.AbstractUnivariate>();

	private
		org.drip.analytics.support.CaseInsensitiveTreeMap<java.util.Map<org.drip.analytics.date.JulianDate,
			org.drip.quant.function1D.AbstractUnivariate>> _mapCustomMetricVolatilitySurface = new
				org.drip.analytics.support.CaseInsensitiveTreeMap<java.util.Map<org.drip.analytics.date.JulianDate,
		org.drip.quant.function1D.AbstractUnivariate>>();

	private org.drip.analytics.support.CaseInsensitiveTreeMap<org.drip.quant.function1D.AbstractUnivariate>
		_mapForwardVolatilitySurface = new
			org.drip.analytics.support.CaseInsensitiveTreeMap<org.drip.quant.function1D.AbstractUnivariate>();

	private org.drip.analytics.support.CaseInsensitiveTreeMap<org.drip.quant.function1D.AbstractUnivariate>
		_mapFundingVolatilitySurface = new
			org.drip.analytics.support.CaseInsensitiveTreeMap<org.drip.quant.function1D.AbstractUnivariate>();

	private org.drip.analytics.support.CaseInsensitiveTreeMap<org.drip.quant.function1D.AbstractUnivariate>
		_mapFXVolatilitySurface = new
			org.drip.analytics.support.CaseInsensitiveTreeMap<org.drip.quant.function1D.AbstractUnivariate>();

	private org.drip.analytics.support.CaseInsensitiveTreeMap<org.drip.quant.function1D.AbstractUnivariate>
		_mapGovvieVolatilitySurface = new
			org.drip.analytics.support.CaseInsensitiveTreeMap<org.drip.quant.function1D.AbstractUnivariate>();

	private org.drip.analytics.support.CaseInsensitiveTreeMap<org.drip.quant.function1D.AbstractUnivariate>
		_mapCollateralCollateralCorrelationSurface = new
			org.drip.analytics.support.CaseInsensitiveTreeMap<org.drip.quant.function1D.AbstractUnivariate>();

	private org.drip.analytics.support.CaseInsensitiveTreeMap<org.drip.quant.function1D.AbstractUnivariate>
		_mapCollateralCreditCorrelationSurface = new
			org.drip.analytics.support.CaseInsensitiveTreeMap<org.drip.quant.function1D.AbstractUnivariate>();

	private org.drip.analytics.support.CaseInsensitiveTreeMap<org.drip.quant.function1D.AbstractUnivariate>
		_mapCollateralCustomMetricCorrelationSurface = new
			org.drip.analytics.support.CaseInsensitiveTreeMap<org.drip.quant.function1D.AbstractUnivariate>();

	private org.drip.analytics.support.CaseInsensitiveTreeMap<org.drip.quant.function1D.AbstractUnivariate>
		_mapCollateralForwardCorrelationSurface = new
			org.drip.analytics.support.CaseInsensitiveTreeMap<org.drip.quant.function1D.AbstractUnivariate>();

	private org.drip.analytics.support.CaseInsensitiveTreeMap<org.drip.quant.function1D.AbstractUnivariate>
		_mapCollateralFundingCorrelationSurface = new
			org.drip.analytics.support.CaseInsensitiveTreeMap<org.drip.quant.function1D.AbstractUnivariate>();

	private org.drip.analytics.support.CaseInsensitiveTreeMap<org.drip.quant.function1D.AbstractUnivariate>
		_mapCollateralFXCorrelationSurface = new
			org.drip.analytics.support.CaseInsensitiveTreeMap<org.drip.quant.function1D.AbstractUnivariate>();

	private org.drip.analytics.support.CaseInsensitiveTreeMap<org.drip.quant.function1D.AbstractUnivariate>
		_mapCollateralGovvieCorrelationSurface = new
			org.drip.analytics.support.CaseInsensitiveTreeMap<org.drip.quant.function1D.AbstractUnivariate>();

	private org.drip.analytics.support.CaseInsensitiveTreeMap<org.drip.quant.function1D.AbstractUnivariate>
		_mapCreditCreditCorrelationSurface = new
			org.drip.analytics.support.CaseInsensitiveTreeMap<org.drip.quant.function1D.AbstractUnivariate>();

	private org.drip.analytics.support.CaseInsensitiveTreeMap<org.drip.quant.function1D.AbstractUnivariate>
		_mapCreditCustomMetricCorrelationSurface = new
			org.drip.analytics.support.CaseInsensitiveTreeMap<org.drip.quant.function1D.AbstractUnivariate>();

	private org.drip.analytics.support.CaseInsensitiveTreeMap<org.drip.quant.function1D.AbstractUnivariate>
		_mapCreditForwardCorrelationSurface = new
			org.drip.analytics.support.CaseInsensitiveTreeMap<org.drip.quant.function1D.AbstractUnivariate>();

	private org.drip.analytics.support.CaseInsensitiveTreeMap<org.drip.quant.function1D.AbstractUnivariate>
		_mapCreditFundingCorrelationSurface = new
			org.drip.analytics.support.CaseInsensitiveTreeMap<org.drip.quant.function1D.AbstractUnivariate>();

	private org.drip.analytics.support.CaseInsensitiveTreeMap<org.drip.quant.function1D.AbstractUnivariate>
		_mapCreditFXCorrelationSurface = new
			org.drip.analytics.support.CaseInsensitiveTreeMap<org.drip.quant.function1D.AbstractUnivariate>();

	private org.drip.analytics.support.CaseInsensitiveTreeMap<org.drip.quant.function1D.AbstractUnivariate>
		_mapCreditGovvieCorrelationSurface = new
			org.drip.analytics.support.CaseInsensitiveTreeMap<org.drip.quant.function1D.AbstractUnivariate>();

	private org.drip.analytics.support.CaseInsensitiveTreeMap<org.drip.quant.function1D.AbstractUnivariate>
		_mapCustomMetricCustomMetricCorrelationSurface = new
			org.drip.analytics.support.CaseInsensitiveTreeMap<org.drip.quant.function1D.AbstractUnivariate>();

	private org.drip.analytics.support.CaseInsensitiveTreeMap<org.drip.quant.function1D.AbstractUnivariate>
		_mapCustomMetricForwardCorrelationSurface = new
			org.drip.analytics.support.CaseInsensitiveTreeMap<org.drip.quant.function1D.AbstractUnivariate>();

	private org.drip.analytics.support.CaseInsensitiveTreeMap<org.drip.quant.function1D.AbstractUnivariate>
		_mapCustomMetricFundingCorrelationSurface = new
			org.drip.analytics.support.CaseInsensitiveTreeMap<org.drip.quant.function1D.AbstractUnivariate>();

	private org.drip.analytics.support.CaseInsensitiveTreeMap<org.drip.quant.function1D.AbstractUnivariate>
		_mapCustomMetricFXCorrelationSurface = new
			org.drip.analytics.support.CaseInsensitiveTreeMap<org.drip.quant.function1D.AbstractUnivariate>();

	private org.drip.analytics.support.CaseInsensitiveTreeMap<org.drip.quant.function1D.AbstractUnivariate>
		_mapCustomMetricGovvieCorrelationSurface = new
			org.drip.analytics.support.CaseInsensitiveTreeMap<org.drip.quant.function1D.AbstractUnivariate>();

	private org.drip.analytics.support.CaseInsensitiveTreeMap<org.drip.quant.function1D.AbstractUnivariate>
		_mapForwardForwardCorrelationSurface = new
			org.drip.analytics.support.CaseInsensitiveTreeMap<org.drip.quant.function1D.AbstractUnivariate>();

	private org.drip.analytics.support.CaseInsensitiveTreeMap<org.drip.quant.function1D.AbstractUnivariate>
		_mapForwardFundingCorrelationSurface = new
			org.drip.analytics.support.CaseInsensitiveTreeMap<org.drip.quant.function1D.AbstractUnivariate>();

	private org.drip.analytics.support.CaseInsensitiveTreeMap<org.drip.quant.function1D.AbstractUnivariate>
		_mapForwardFXCorrelationSurface = new
			org.drip.analytics.support.CaseInsensitiveTreeMap<org.drip.quant.function1D.AbstractUnivariate>();

	private org.drip.analytics.support.CaseInsensitiveTreeMap<org.drip.quant.function1D.AbstractUnivariate>
		_mapForwardGovvieCorrelationSurface = new
			org.drip.analytics.support.CaseInsensitiveTreeMap<org.drip.quant.function1D.AbstractUnivariate>();

	private org.drip.analytics.support.CaseInsensitiveTreeMap<org.drip.quant.function1D.AbstractUnivariate>
		_mapFundingFundingCorrelationSurface = new
			org.drip.analytics.support.CaseInsensitiveTreeMap<org.drip.quant.function1D.AbstractUnivariate>();

	private org.drip.analytics.support.CaseInsensitiveTreeMap<org.drip.quant.function1D.AbstractUnivariate>
		_mapFundingFXCorrelationSurface = new
			org.drip.analytics.support.CaseInsensitiveTreeMap<org.drip.quant.function1D.AbstractUnivariate>();

	private org.drip.analytics.support.CaseInsensitiveTreeMap<org.drip.quant.function1D.AbstractUnivariate>
		_mapFundingGovvieCorrelationSurface = new
			org.drip.analytics.support.CaseInsensitiveTreeMap<org.drip.quant.function1D.AbstractUnivariate>();

	private org.drip.analytics.support.CaseInsensitiveTreeMap<org.drip.quant.function1D.AbstractUnivariate>
		_mapFXFXCorrelationSurface = new
			org.drip.analytics.support.CaseInsensitiveTreeMap<org.drip.quant.function1D.AbstractUnivariate>();

	private org.drip.analytics.support.CaseInsensitiveTreeMap<org.drip.quant.function1D.AbstractUnivariate>
		_mapFXGovvieCorrelationSurface = new
			org.drip.analytics.support.CaseInsensitiveTreeMap<org.drip.quant.function1D.AbstractUnivariate>();

	private org.drip.analytics.support.CaseInsensitiveTreeMap<org.drip.quant.function1D.AbstractUnivariate>
		_mapGovvieGovvieCorrelationSurface = new
			org.drip.analytics.support.CaseInsensitiveTreeMap<org.drip.quant.function1D.AbstractUnivariate>();

	private org.drip.analytics.support.CaseInsensitiveTreeMap<org.drip.param.definition.ProductQuote>
		_mapProductQuote = new
			org.drip.analytics.support.CaseInsensitiveTreeMap<org.drip.param.definition.ProductQuote>();

	private java.util.Map<org.drip.analytics.date.JulianDate,
		org.drip.analytics.support.CaseInsensitiveTreeMap<java.lang.Double>> _mmFixings = null;

	/**
	 * Empty CurveSurfaceQuoteSet Constructor
	 */

	public CurveSurfaceQuoteSet()
	{
	}

	/**
	 * CurveSurfaceQuoteSet de-serialization from input byte array
	 * 
	 * @param ab Byte Array
	 * 
	 * @throws java.lang.Exception Thrown if CurveSurfaceQuoteSet cannot be properly de-serialized
	 */

	public CurveSurfaceQuoteSet (
		final byte[] ab)
		throws java.lang.Exception
	{
		if (null == ab || 0 == ab.length)
			throw new java.lang.Exception ("CurveSurfaceQuoteSet de-serializer: Invalid input Byte array");

		java.lang.String strRawString = new java.lang.String (ab);

		if (null == strRawString || strRawString.isEmpty())
			throw new java.lang.Exception ("CurveSurfaceQuoteSet de-serializer: Empty state");

		java.lang.String strSerializedCurveSurfaceQuoteSet = strRawString.substring (0, strRawString.indexOf
			(objectTrailer()));

		if (null == strSerializedCurveSurfaceQuoteSet || strSerializedCurveSurfaceQuoteSet.isEmpty())
			throw new java.lang.Exception ("CurveSurfaceQuoteSet de-serializer: Cannot locate state");

		java.lang.String[] astrField = org.drip.quant.common.StringUtil.Split
			(strSerializedCurveSurfaceQuoteSet, fieldDelimiter());

		if (null == astrField || 4 > astrField.length)
			throw new java.lang.Exception ("CurveSurfaceQuoteSet de-serializer: Invalid reqd field set");

		// double dblVersion = new java.lang.Double (astrField[0]);

		if (null == astrField[1] || astrField[1].isEmpty())
			throw new java.lang.Exception
				("CurveSurfaceQuoteSet de-serializer: Cannot locate forward curve");

		if (!org.drip.service.stream.Serializer.NULL_SER_STRING.equalsIgnoreCase (astrField[1]))
			_mapForwardCurve = null;

		if (null == astrField[2] || astrField[2].isEmpty())
			throw new java.lang.Exception ("CurveSurfaceQuoteSet de-serializer: Cannot locate fixings");

		if (!org.drip.service.stream.Serializer.NULL_SER_STRING.equalsIgnoreCase (astrField[2])) {
			java.lang.String[] astrRecord = org.drip.quant.common.StringUtil.Split (astrField[2],
				collectionRecordDelimiter());

			if (null != astrRecord && 0 != astrRecord.length) {
				for (int i = 0; i < astrRecord.length; ++i) {
					if (null == astrRecord[i] || astrRecord[i].isEmpty()) continue;

					java.lang.String[] astrKVPair = org.drip.quant.common.StringUtil.Split (astrRecord[i],
						collectionKeyValueDelimiter());
					
					if (null == astrKVPair || 2 != astrKVPair.length || null == astrKVPair[0] ||
						astrKVPair[0].isEmpty() ||
							org.drip.service.stream.Serializer.NULL_SER_STRING.equalsIgnoreCase
								(astrKVPair[0]) || null == astrKVPair[1] || astrKVPair[1].isEmpty() ||
									org.drip.service.stream.Serializer.NULL_SER_STRING.equalsIgnoreCase
										(astrKVPair[1]))
						continue;

					java.lang.String[] astrKeySet = org.drip.quant.common.StringUtil.Split (astrKVPair[0],
						collectionMultiLevelKeyDelimiter());

					if (null == astrKeySet || 2 != astrKeySet.length || null == astrKeySet[0] ||
						astrKeySet[0].isEmpty() ||
							org.drip.service.stream.Serializer.NULL_SER_STRING.equalsIgnoreCase
								(astrKeySet[0]) || null == astrKeySet[1] || astrKeySet[1].isEmpty() ||
									org.drip.service.stream.Serializer.NULL_SER_STRING.equalsIgnoreCase
										(astrKeySet[1]))
						continue;

					if (null == _mmFixings)
						_mmFixings = new java.util.HashMap<org.drip.analytics.date.JulianDate,
							org.drip.analytics.support.CaseInsensitiveTreeMap<java.lang.Double>>();

					org.drip.analytics.support.CaseInsensitiveTreeMap<java.lang.Double> map2D = _mmFixings.get
						(astrKeySet[0]);

					if (null == map2D)
						map2D = new org.drip.analytics.support.CaseInsensitiveTreeMap<java.lang.Double>();

					map2D.put (astrKeySet[1], new java.lang.Double (astrKVPair[1]));

					_mmFixings.put (new org.drip.analytics.date.JulianDate (new java.lang.Double
						(astrKeySet[0])), map2D);
				}
			}
		}

		if (null == astrField[3] || astrField[3].isEmpty())
			throw new java.lang.Exception ("CurveSurfaceQuoteSet de-serializer: Cannot locate TSY quotes");

		if (!org.drip.service.stream.Serializer.NULL_SER_STRING.equalsIgnoreCase (astrField[3])) {
			java.lang.String[] astrRecord = org.drip.quant.common.StringUtil.Split (astrField[3],
				collectionRecordDelimiter());

			if (null != astrRecord && 0 != astrRecord.length) {
				for (int i = 0; i < astrRecord.length; ++i) {
					if (null == astrRecord[i] || astrRecord[i].isEmpty() ||
						org.drip.service.stream.Serializer.NULL_SER_STRING.equalsIgnoreCase (astrRecord[i]))
						continue;

					java.lang.String[] astrKVPair = org.drip.quant.common.StringUtil.Split (astrRecord[i],
						collectionKeyValueDelimiter());
				
					if (null == astrKVPair || 2 != astrKVPair.length || null == astrKVPair[0] ||
						astrKVPair[0].isEmpty() ||
							org.drip.service.stream.Serializer.NULL_SER_STRING.equalsIgnoreCase
								(astrKVPair[0]) || null == astrKVPair[1] || astrKVPair[1].isEmpty() ||
									org.drip.service.stream.Serializer.NULL_SER_STRING.equalsIgnoreCase
										(astrKVPair[1]))
						continue;

					if (null == _mapProductQuote)
						_mapProductQuote = new
							org.drip.analytics.support.CaseInsensitiveTreeMap<org.drip.param.definition.ProductQuote>();

					_mapProductQuote.put (astrKVPair[0], new org.drip.param.market.ProductMultiMeasureQuote
						(astrKVPair[1].getBytes()));
				}
			}
		}
	}

	@Override public java.lang.String collectionKeyValueDelimiter()
	{
		return "]";
	}

	@Override public java.lang.String fieldDelimiter()
	{
		return "[";
	}

	@Override public java.lang.String objectTrailer()
	{
		return "~";
	}

	/**
	 * Retrieve the Discount Curve associated with the Pay Cash-flow Collateralized using a different
	 * 	Collateral Currency Numeraire
	 * 
	 * @param strPayCurrency The Pay Currency
	 * @param strCollateralCurrency The Collateral Currency
	 * 
	 * @return The Discount Curve associated with the Pay Cash-flow Collateralized using a different
	 * 	Collateral Currency Numeraire
	 */

	public org.drip.analytics.rates.DiscountCurve payCurrencyCollateralCurrencyCurve (
		final java.lang.String strPayCurrency,
		final java.lang.String strCollateralCurrency)
	{
		if (null == strPayCurrency || !_mapPayCurrencyForeignCollateralDC.containsKey (strPayCurrency) ||
			null == strCollateralCurrency)
			return null;

		return _mapPayCurrencyForeignCollateralDC.get (strPayCurrency).get (strCollateralCurrency);
	}

	/**
	 * Set the Discount Curve associated with the Pay Cash-flow Collateralized using a different
	 * 	Collateral Currency Numeraire
	 * 
	 * @param strPayCurrency The Pay Currency
	 * @param strCollateralCurrency The Collateral Currency
	 * @param dcPayCurrencyCollateralCurrency The Discount Curve associated with the Pay Cash-flow
	 *  Collateralized using a different Collateral Currency Numeraire
	 * 
	 * @return TRUE => Successfully set
	 */

	public boolean setPayCurrencyCollateralCurrencyCurve (
		final java.lang.String strPayCurrency,
		final java.lang.String strCollateralCurrency,
		final org.drip.analytics.rates.DiscountCurve dcPayCurrencyCollateralCurrency)
	{
		if (null == strPayCurrency || strPayCurrency.isEmpty() || null == strCollateralCurrency ||
			strCollateralCurrency.isEmpty() || null == dcPayCurrencyCollateralCurrency)
			return false;

		org.drip.analytics.support.CaseInsensitiveTreeMap<org.drip.analytics.rates.DiscountCurve>
			mapCollateralCurrencyDC = new
				org.drip.analytics.support.CaseInsensitiveTreeMap<org.drip.analytics.rates.DiscountCurve>();

		mapCollateralCurrencyDC.put (strCollateralCurrency, dcPayCurrencyCollateralCurrency);

		_mapPayCurrencyForeignCollateralDC.put (strPayCurrency, mapCollateralCurrencyDC);

		return true;
	}

	/**
	 * Retrieve the Collateral Choice Discount Curve for the specified Pay Currency
	 * 
	 * @param strPayCurrency The Pay Currency
	 * 
	 * @return Collateral Choice Discount Curve
	 */

	public org.drip.analytics.rates.DiscountCurve collateralChoiceDiscountCurve (
		final java.lang.String strPayCurrency)
	{
		if (null == strPayCurrency || !_mapPayCurrencyForeignCollateralDC.containsKey (strPayCurrency))
			return null;

		org.drip.analytics.support.CaseInsensitiveTreeMap<org.drip.analytics.rates.DiscountCurve>
			mapCollateralCurrencyDC = _mapPayCurrencyForeignCollateralDC.get (strPayCurrency);

		int iNumCollateralizer = mapCollateralCurrencyDC.size();

		org.drip.state.curve.ForeignCollateralizedDiscountCurve[] aFCDC = new
			org.drip.state.curve.ForeignCollateralizedDiscountCurve[iNumCollateralizer];

		int i = 0;

		for (java.util.Map.Entry<java.lang.String, org.drip.analytics.rates.DiscountCurve> me :
			mapCollateralCurrencyDC.entrySet()) {
			org.drip.analytics.rates.DiscountCurve fcdc = me.getValue();

			if (!(fcdc instanceof org.drip.state.curve.ForeignCollateralizedDiscountCurve)) return null;

			aFCDC[i++] = (org.drip.state.curve.ForeignCollateralizedDiscountCurve) fcdc;
		}

		try {
			return new org.drip.state.curve.DeterministicCollateralChoiceDiscountCurve
				(mapCollateralCurrencyDC.get (strPayCurrency), aFCDC, 30);
		} catch (java.lang.Exception e) {
			e.printStackTrace();
		}

		return null;
	}

	/**
	 * Retrieve the  Credit Curve
	 * 
	 * @param strCreditCurveName Name of the Credit Curve
	 * 
	 * @return  Credit Curve
	 */

	public org.drip.analytics.definition.CreditCurve creditCurve (
		final java.lang.String strCreditCurveName)
	{
		if (null == strCreditCurveName || strCreditCurveName.isEmpty() || !_mapCreditCurve.containsKey
			(strCreditCurveName))
			return null;

		return _mapCreditCurve.get (strCreditCurveName);
	}

	/**
	 * (Re)-set the  Credit Curve
	 * 
	 * @param cc  Credit Curve
	 * 
	 * @return TRUE => Successfully set
	 */

	public boolean setCreditCurve (
		final org.drip.analytics.definition.CreditCurve cc)
	{
		if (null == cc) return false;

		_mapCreditCurve.put (cc.name(), cc);

		return true;
	}

	/**
	 * Retrieve the Forward Curve
	 * 
	 * @param fri Floating Rate Index
	 * 
	 * @return  Forward Curve
	 */

	public org.drip.analytics.rates.ForwardCurve forwardCurve (
		final org.drip.product.params.FloatingRateIndex fri)
	{
		if (null == fri) return null;

		java.lang.String strFullyQualifiedName = fri.fullyQualifiedName();

		return _mapForwardCurve.containsKey (strFullyQualifiedName) ? _mapForwardCurve.get
			(strFullyQualifiedName) : null;
	}

	/**
	 * (Re)-set the Forward Curve
	 * 
	 * @param fc Forward Curve
	 * 
	 * @return TRUE => Successfully set
	 */

	public boolean setForwardCurve (
		final org.drip.analytics.rates.ForwardCurve fc)
	{
		if (null == fc) return false;

		_mapForwardCurve.put (fc.index().fullyQualifiedName(), fc);

		return true;
	}

	/**
	 * Retrieve the Funding Curve Corresponding to the specified Currency
	 * 
	 * @param strCurrency The Currency
	 * 
	 * @return Funding Curve
	 */

	public org.drip.analytics.rates.DiscountCurve fundingCurve (
		final java.lang.String strCurrency)
	{
		return null == strCurrency || strCurrency.isEmpty() || !_mapFundingCurve.containsKey (strCurrency) ?
			null : _mapFundingCurve.get (strCurrency);
	}

	/**
	 * (Re)-set the Funding Curve
	 * 
	 * @param dcFunding Funding Curve
	 * 
	 * @return TRUE => Successfully set
	 */

	public boolean setFundingCurve (
		final org.drip.analytics.rates.DiscountCurve dc)
	{
		if (null == dc) return false;

		_mapFundingCurve.put (dc.currency(), dc);

		return true;
	}

	/**
	 * Retrieve the FX Curve for the specified currency Pair
	 * 
	 * @param cp The Currency Pair
	 * 
	 * @return FX Curve
	 */

	public org.drip.quant.function1D.AbstractUnivariate fxCurve (
		final org.drip.product.params.CurrencyPair cp)
	{
		if (null == cp) return null;

		java.lang.String strFXCode = cp.code();

		return _mapFXCurve.containsKey (strFXCode) ? _mapFXCurve.get (strFXCode) : null;
	}

	/**
	 * (Re)-set the FX Curve for the specified Currency Pair
	 * 
	 * @param cp The Currency Pair
	 * @param auFX The FX Curve
	 * 
	 * @return TRUE => Successfully set
	 */

	public boolean setFXCurve (
		final org.drip.product.params.CurrencyPair cp,
		final org.drip.quant.function1D.AbstractUnivariate auFX)
	{
		if (null == cp || null == auFX) return false;

		_mapFXCurve.put (cp.code(), auFX);

		try {
			_mapFXCurve.put (cp.inverseCode(), new org.drip.quant.function1D.UnivariateReciprocal (auFX));
		} catch (java.lang.Exception e) {
			e.printStackTrace();

			return false;
		}

		return true;
	}

	/**
	 * Retrieve the Government Curve for the specified Currency
	 * 
	 * @return Government Curve for the specified Currency
	 */

	public org.drip.analytics.rates.DiscountCurve govvieCurve (
		final java.lang.String strCurrency)
	{
		return null == strCurrency || strCurrency.isEmpty() || !_mapGovvieCurve.containsKey (strCurrency) ?
			null : _mapGovvieCurve.get (strCurrency);
	}

	/**
	 * (Re)-set the Government Discount Curve
	 * 
	 * @param dcGovvie Government Discount Curve
	 * 
	 * @return TRUE => Successfully set
	 */

	public boolean setGovvieCurve (
		final org.drip.analytics.rates.DiscountCurve dcGovvie)
	{
		if (null == dcGovvie) return false;

		_mapGovvieCurve.put (dcGovvie.currency(), dcGovvie);

		return true;
	}

	/**
	 * Retrieve the Volatility Surface for the specified Collateral Curve
	 * 
	 * @param strCurrency The Collateral Currency
	 * 
	 * @return The Volatility Surface for the Collateral Currency
	 */

	public org.drip.quant.function1D.AbstractUnivariate collateralCurveVolSurface (
		final java.lang.String strCurrency)
	{
		if (null == strCurrency || strCurrency.isEmpty() || !_mapCollateralVolatilitySurface.containsKey
			(strCurrency))
			return null;

		return _mapCollateralVolatilitySurface.get (strCurrency);
	}

	/**
	 * (Re)-set the Volatility Surface for the specified Collateral Curve
	 * 
	 * @param strCurrency The Collateral Currency
	 * @param auVolatility The Volatility Surface
	 * 
	 * @return TRUE => Successfully set
	 */

	public boolean setCollateralCurveVolSurface (
		final java.lang.String strCurrency,
		final org.drip.quant.function1D.AbstractUnivariate auVolatility)
	{
		if (null == strCurrency || strCurrency.isEmpty() || null == auVolatility) return false;

		_mapCollateralVolatilitySurface.put (strCurrency, auVolatility);

		return true;
	}

	/**
	 * Retrieve the Volatility Surface for the specified Credit Curve
	 * 
	 * @param strCreditCurveName The Credit Curve Name
	 * 
	 * @return The Volatility Surface for the specified Credit Curve
	 */

	public org.drip.quant.function1D.AbstractUnivariate creditCurveVolSurface (
		final java.lang.String strCreditCurveName)
	{
		return null == strCreditCurveName || strCreditCurveName.isEmpty() ||
			!_mapCreditVolatilitySurface.containsKey (strCreditCurveName) ? null :
				_mapCreditVolatilitySurface.get (strCreditCurveName);
	}

	/**
	 * (Re)-set the Volatility Surface for the specified Credit Curve
	 * 
	 * @param strCreditCurveName The Credit Curve Name
	 * @param auVolatility The Volatility Surface
	 * 
	 * @return TRUE => Successfully set
	 */

	public boolean setCreditCurveVolSurface (
		final java.lang.String strCreditCurveName,
		final org.drip.quant.function1D.AbstractUnivariate auVolatility)
	{
		if (null == strCreditCurveName || strCreditCurveName.isEmpty() || null == auVolatility) return false;

		_mapCreditVolatilitySurface.put (strCreditCurveName, auVolatility);

		return true;
	}

	/**
	 * Retrieve the Custom Metric Volatility Surface for the given Forward Date
	 * 
	 * @param strCustomMetric The Custom Metric Name
	 * @param dtForward The Forward Date 
	 * 
	 * @return The Latent State Volatility Surface
	 */

	public org.drip.quant.function1D.AbstractUnivariate customMetricVolSurface (
		final java.lang.String strCustomMetric,
		final org.drip.analytics.date.JulianDate dtForward)
	{
		if (null == strCustomMetric || !_mapCustomMetricVolatilitySurface.containsKey (strCustomMetric))
			return null;

		java.util.Map<org.drip.analytics.date.JulianDate, org.drip.quant.function1D.AbstractUnivariate>
			mapForwardVolatility = _mapCustomMetricVolatilitySurface.get (strCustomMetric);

		if (null == mapForwardVolatility || !mapForwardVolatility.containsKey (dtForward)) return null;

		return mapForwardVolatility.get (dtForward);
	}

	/**
	 * (Re)-set the Custom Metric Volatility Surface for the given Forward Date
	 * 
	 * @param strCustomMetric The Custom Metric Name
	 * @param dtForward The Forward Date 
	 * @param auVolatility The Custom Metric Volatility Surface
	 * 
	 * @return TRUE => Successfully set
	 */

	public boolean setCustomMetricVolSurface (
		final java.lang.String strCustomMetric,
		final org.drip.analytics.date.JulianDate dtForward,
		final org.drip.quant.function1D.AbstractUnivariate auVolatility)
	{
		if (null == strCustomMetric || strCustomMetric.isEmpty() || null == dtForward || null ==
			auVolatility)
			return false;

		java.util.Map<org.drip.analytics.date.JulianDate, org.drip.quant.function1D.AbstractUnivariate>
			mapForwardVolatility = _mapCustomMetricVolatilitySurface.get (strCustomMetric);

		if (null == mapForwardVolatility) {
			mapForwardVolatility = new java.util.HashMap<org.drip.analytics.date.JulianDate,
				org.drip.quant.function1D.AbstractUnivariate>();

			mapForwardVolatility.put (dtForward, auVolatility);

			_mapCustomMetricVolatilitySurface.put (strCustomMetric, mapForwardVolatility);
		} else
			mapForwardVolatility.put (dtForward, auVolatility);

		return true;
	}

	/**
	 * Retrieve the Volatility Surface for the specified Forward Curve
	 * 
	 * @param fri The Forward Rate Index identifying the Forward Curve
	 * 
	 * @return The Volatility Surface for the Forward Curve
	 */

	public org.drip.quant.function1D.AbstractUnivariate forwardCurveVolSurface (
		final org.drip.product.params.FloatingRateIndex fri)
	{
		if (null == fri) return null;

		java.lang.String strFRI = fri.fullyQualifiedName();

		return _mapForwardVolatilitySurface.containsKey (strFRI) ? _mapForwardVolatilitySurface.get (strFRI)
			: null;
	}

	/**
	 * (Re)-set the Volatility Surface for the specified Forward Curve
	 * 
	 * @param fri The Forward Rate Index identifying the Forward Curve
	 * @param auVolatility The Volatility Surface
	 * 
	 * @return TRUE => Successfully set
	 */

	public boolean setForwardCurveVolSurface (
		final org.drip.product.params.FloatingRateIndex fri,
		final org.drip.quant.function1D.AbstractUnivariate auVolatility)
	{
		if (null == fri || null == auVolatility) return false;

		_mapForwardVolatilitySurface.put (fri.fullyQualifiedName(), auVolatility);

		return true;
	}

	/**
	 * Retrieve the Volatility Surface for the specified Funding Curve
	 * 
	 * @param strCurrency The Funding Currency
	 * 
	 * @return The Volatility Surface for the Funding Currency
	 */

	public org.drip.quant.function1D.AbstractUnivariate fundingCurveVolSurface (
		final java.lang.String strCurrency)
	{
		if (null == strCurrency || strCurrency.isEmpty() || !_mapFundingVolatilitySurface.containsKey
			(strCurrency))
			return null;

		return _mapFundingVolatilitySurface.get (strCurrency);
	}

	/**
	 * (Re)-set the Volatility Surface for the specified Funding Curve
	 * 
	 * @param strCurrency The Funding Currency
	 * @param auVolatility The Volatility Surface
	 * 
	 * @return TRUE => Successfully set
	 */

	public boolean setFundingCurveVolSurface (
		final java.lang.String strCurrency,
		final org.drip.quant.function1D.AbstractUnivariate auVolatility)
	{
		if (null == strCurrency || strCurrency.isEmpty() || null == auVolatility) return false;

		_mapFundingVolatilitySurface.put (strCurrency, auVolatility);

		return true;
	}

	/**
	 * Retrieve the FX Volatility Surface for the specified Currency Pair
	 * 
	 * @param cp The Currency Pair
	 * 
	 * @return The FX Volatility Surface for the Currency Pair
	 */

	public org.drip.quant.function1D.AbstractUnivariate fxCurveVolSurface (
		final org.drip.product.params.CurrencyPair cp)
	{
		if (null == cp) return null;

		java.lang.String strCode = cp.code();

		return !_mapFXVolatilitySurface.containsKey (strCode) ? null : _mapFXVolatilitySurface.get
			(strCode);
	}

	/**
	 * (Re)-set the FX Volatility Surface for the specified Currency Pair
	 * 
	 * @param cp The Currency Pair
	 * @param auVolatility The Volatility Surface
	 * 
	 * @return TRUE => Successfully set
	 */

	public boolean setFXCurveVolSurface (
		final org.drip.product.params.CurrencyPair cp,
		final org.drip.quant.function1D.AbstractUnivariate auVolatility)
	{
		if (null == cp || null == auVolatility) return false;

		_mapFXVolatilitySurface.put (cp.code(), auVolatility);

		return true;
	}

	/**
	 * Retrieve the Volatility Surface for the specified Govvie Curve
	 * 
	 * @param strCurrency The Govvie Currency
	 * 
	 * @return The Volatility Surface for the Govvie Curve
	 */

	public org.drip.quant.function1D.AbstractUnivariate govvieCurveVolSurface (
		final java.lang.String strCurrency)
	{
		return null == strCurrency || strCurrency.isEmpty() ||
			!_mapGovvieVolatilitySurface.containsKey (strCurrency) ? null : _mapGovvieVolatilitySurface.get
				(strCurrency);
	}

	/**
	 * (Re)-set the Volatility Surface for the specified Govvie Curve
	 * 
	 * @param strCurrency The Govvie Currency
	 * @param auVolatility The Volatility Surface
	 * 
	 * @return TRUE => Successfully set
	 */

	public boolean setGovvieCurveVolSurface (
		final java.lang.String strCurrency,
		final org.drip.quant.function1D.AbstractUnivariate auVolatility)
	{
		if (null == strCurrency || strCurrency.isEmpty() || null == auVolatility) return false;

		_mapGovvieVolatilitySurface.put (strCurrency, auVolatility);

		return true;
	}

	/**
	 * Retrieve the Correlation Surface for the specified Collateral Currency Pair
	 * 
	 * @param strCurrency1 Collateral Currency #1
	 * @param strCurrency2 Collateral Currency #2
	 * 
	 * @return The Correlation Surface for the specified Collateral Currency Pair
	 */

	public org.drip.quant.function1D.AbstractUnivariate collateralCollateralCorrSurface (
		final java.lang.String strCurrency1,
		final java.lang.String strCurrency2)
	{
		if (null == strCurrency1 || strCurrency1.isEmpty() || null == strCurrency2 || strCurrency2.isEmpty())
			return null;

		java.lang.String strCode = strCurrency1 + "@#" + strCurrency2;

		if (!_mapCollateralCollateralCorrelationSurface.containsKey (strCode)) return null;

		return _mapCollateralCollateralCorrelationSurface.get (strCode);
	}

	/**
	 * (Re)-set the Correlation Surface for the specified Collateral Currency Pair
	 * 
	 * @param strCurrency1 Collateral Currency #1
	 * @param strCurrency2 Collateral Currency #2
	 * @param auCorrelation The Correlation Surface
	 * 
	 * @return TRUE => Successfully set
	 */

	public boolean setCollateralCollateralCorrSurface (
		final java.lang.String strCurrency1,
		final java.lang.String strCurrency2,
		final org.drip.quant.function1D.AbstractUnivariate auCorrelation)
	{
		if (null == strCurrency1 || strCurrency1.isEmpty() || null == strCurrency2 || strCurrency2.isEmpty()
			|| null == auCorrelation)
			return false;

		_mapCollateralCollateralCorrelationSurface.put (strCurrency1 + "@#" + strCurrency2, auCorrelation);

		_mapCollateralCollateralCorrelationSurface.put (strCurrency2 + "@#" + strCurrency1, auCorrelation);

		return true;
	}

	/**
	 * Retrieve the Correlation Surface for the specified Credit Pair
	 * 
	 * @param strCreditCurveName1 Credit Curve Name #1
	 * @param strCreditCurveName2 Credit Curve Name #2
	 * 
	 * @return The Correlation Surface for the specified Credit Pair
	 */

	public org.drip.quant.function1D.AbstractUnivariate creditCreditCorrSurface (
		final java.lang.String strCreditCurveName1,
		final java.lang.String strCreditCurveName2)
	{
		if (null == strCreditCurveName1 || strCreditCurveName1.isEmpty() || null == strCreditCurveName2 ||
			strCreditCurveName2.isEmpty())
			return null;

		java.lang.String strCode = strCreditCurveName1 + "@#" + strCreditCurveName2;

		return !_mapCreditCreditCorrelationSurface.containsKey (strCode) ? null :
			_mapCreditCreditCorrelationSurface.get (strCode);
	}

	/**
	 * (Re)-set the Correlation Surface for the specified Credit Pair
	 * 
	 * @param strCreditCurveName1 Credit Curve Name #1
	 * @param strCreditCurveName2 Credit Curve Name #2
	 * @param auCorrelation The Correlation Surface
	 * 
	 * @return TRUE => Successfully set
	 */

	public boolean setCreditCreditCorrSurface (
		final java.lang.String strCreditCurveName1,
		final java.lang.String strCreditCurveName2,
		final org.drip.quant.function1D.AbstractUnivariate auCorrelation)
	{
		if (null == strCreditCurveName1 || strCreditCurveName1.isEmpty() || null == strCreditCurveName2 ||
			strCreditCurveName2.isEmpty() || null == auCorrelation)
			return false;

		_mapCreditCreditCorrelationSurface.put (strCreditCurveName1 + "@#" + strCreditCurveName2,
			auCorrelation);

		_mapCreditCreditCorrelationSurface.put (strCreditCurveName2 + "@#" + strCreditCurveName1,
			auCorrelation);

		return true;
	}

	/**
	 * Retrieve the Correlation Surface for the specified Custom Metric Pair
	 * 
	 * @param strCustomMetric1 Custom Metric #1
	 * @param strCustomMetric2 Custom Metric #2
	 * 
	 * @return The Correlation Surface for the specified Custom Metric Pair
	 */

	public org.drip.quant.function1D.AbstractUnivariate customMetricCustomMetricCorrSurface (
		final java.lang.String strCustomMetric1,
		final java.lang.String strCustomMetric2)
	{
		if (null == strCustomMetric1 || strCustomMetric1.isEmpty() || null == strCustomMetric2 ||
			strCustomMetric2.isEmpty())
			return null;

		java.lang.String strCode = strCustomMetric1 + "@#" + strCustomMetric2;

		if (!_mapCustomMetricCustomMetricCorrelationSurface.containsKey (strCode)) return null;

		return _mapCustomMetricCustomMetricCorrelationSurface.get (strCode);
	}

	/**
	 * (Re)-set the Correlation Surface for the specified Custom Metric Pair
	 * 
	 * @param strCustomMetric1 Custom Metric #1
	 * @param strCustomMetric2 Custom Metric #2
	 * @param auCorrelation The Correlation Surface
	 * 
	 * @return TRUE => Successfully set
	 */

	public boolean setCustomMetricCustomMetricCorrSurface (
		final java.lang.String strCustomMetric1,
		final java.lang.String strCustomMetric2,
		final org.drip.quant.function1D.AbstractUnivariate auCorrelation)
	{
		if (null == strCustomMetric1 || strCustomMetric1.isEmpty() || null == strCustomMetric2 ||
			strCustomMetric2.isEmpty() || null == auCorrelation)
			return false;

		_mapCustomMetricCustomMetricCorrelationSurface.put (strCustomMetric1 + "@#" + strCustomMetric2,
			auCorrelation);

		_mapCustomMetricCustomMetricCorrelationSurface.put (strCustomMetric2 + "@#" + strCustomMetric1,
			auCorrelation);

		return true;
	}

	/**
	 * Retrieve the Correlation Surface for the specified FRI Pair
	 * 
	 * @param fri1 FRI #1
	 * @param fri2 FRI #2
	 * 
	 * @return The Correlation Surface for the specified FRI Pair
	 */

	public org.drip.quant.function1D.AbstractUnivariate forwardForwardCorrSurface (
		final org.drip.product.params.FloatingRateIndex fri1,
		final org.drip.product.params.FloatingRateIndex fri2)
	{
		if (null == fri1 || null == fri2) return null;

		java.lang.String strCode = fri1.fullyQualifiedName() + "@#" + fri2.fullyQualifiedName();

		if (!_mapForwardForwardCorrelationSurface.containsKey (strCode)) return null;

		return _mapForwardForwardCorrelationSurface.get (strCode);
	}

	/**
	 * (Re)-set the Correlation Surface for the specified FRI Pair
	 * 
	 * @param fri1 FRI #1
	 * @param fri2 FRI #2
	 * @param auCorrelation The Correlation Surface
	 * 
	 * @return TRUE => Successfully set
	 */

	public boolean setForwardForwardCorrSurface (
		final org.drip.product.params.FloatingRateIndex fri1,
		final org.drip.product.params.FloatingRateIndex fri2,
		final org.drip.quant.function1D.AbstractUnivariate auCorrelation)
	{
		if (null == fri1 || null == fri2 || null == auCorrelation) return false;

		java.lang.String strFRI1 = fri1.fullyQualifiedName();

		java.lang.String strFRI2 = fri2.fullyQualifiedName();

		_mapForwardForwardCorrelationSurface.put (strFRI1 + "@#" + strFRI2, auCorrelation);

		_mapForwardForwardCorrelationSurface.put (strFRI2 + "@#" + strFRI1, auCorrelation);

		return true;
	}

	/**
	 * Retrieve the Correlation Surface for the specified Funding Currency Pair
	 * 
	 * @param strCurrency1 Funding Currency #1
	 * @param strCurrency2 Funding Currency #2
	 * 
	 * @return The Correlation Surface for the specified Funding Currency Pair
	 */

	public org.drip.quant.function1D.AbstractUnivariate fundingFundingCorrSurface (
		final java.lang.String strCurrency1,
		final java.lang.String strCurrency2)
	{
		if (null == strCurrency1 || strCurrency1.isEmpty() || null == strCurrency2 || strCurrency2.isEmpty())
			return null;

		java.lang.String strCode = strCurrency1 + "@#" + strCurrency2;

		if (!_mapFundingFundingCorrelationSurface.containsKey (strCode)) return null;

		return _mapFundingFundingCorrelationSurface.get (strCode);
	}

	/**
	 * (Re)-set the Correlation Surface for the specified Funding Currency Pair
	 * 
	 * @param strCurrency1 Funding Currency #1
	 * @param strCurrency2 Funding Currency #2
	 * @param auCorrelation The Correlation Surface
	 * 
	 * @return TRUE => Successfully set
	 */

	public boolean setFundingFundingCorrSurface (
		final java.lang.String strCurrency1,
		final java.lang.String strCurrency2,
		final org.drip.quant.function1D.AbstractUnivariate auCorrelation)
	{
		if (null == strCurrency1 || strCurrency1.isEmpty() || null == strCurrency2 || strCurrency2.isEmpty()
			|| null == auCorrelation)
			return false;

		_mapFundingFundingCorrelationSurface.put (strCurrency1 + "@#" + strCurrency2, auCorrelation);

		_mapFundingFundingCorrelationSurface.put (strCurrency2 + "@#" + strCurrency1, auCorrelation);

		return true;
	}

	/**
	 * Retrieve the FX Correlation Surface for the specified Currency Pair Set
	 * 
	 * @param cp1 Currency Pair #1
	 * @param cp2 Currency Pair #2
	 * 
	 * @return The FX Correlation Surface for the specified Currency Pair Set
	 */

	public org.drip.quant.function1D.AbstractUnivariate fxFXCorrSurface (
		final org.drip.product.params.CurrencyPair cp1,
		final org.drip.product.params.CurrencyPair cp2)
	{
		if (null == cp1 || null == cp2) return null;

		java.lang.String strCode = cp1.code() + "@#" + cp2.code();

		return !_mapFXFXCorrelationSurface.containsKey (strCode) ? null : _mapFXFXCorrelationSurface.get
			(strCode);
	}

	/**
	 * (Re)-set the FX Correlation Surface for the specified Funding Currency Pair Set
	 * 
	 * @param cp1 Currency Pair #1
	 * @param cp2 Currency Pair #2
	 * @param auCorrelation The Correlation Surface
	 * 
	 * @return TRUE => Successfully set
	 */

	public boolean setFXFXCorrSurface (
		final org.drip.product.params.CurrencyPair cp1,
		final org.drip.product.params.CurrencyPair cp2,
		final org.drip.quant.function1D.AbstractUnivariate auCorrelation)
	{
		if (null == cp1 || null == cp2 || null == auCorrelation) return false;

		java.lang.String strCurrencyPair1 = cp1.code();

		java.lang.String strCurrencyPair2 = cp2.code();

		_mapFXFXCorrelationSurface.put (strCurrencyPair1 + "@#" + strCurrencyPair2, auCorrelation);

		_mapFXFXCorrelationSurface.put (strCurrencyPair2 + "@#" + strCurrencyPair1, auCorrelation);

		return true;
	}

	/**
	 * Retrieve the Correlation Surface for the specified Govvie Currency Pair
	 * 
	 * @param strCurrency1 Govvie Currency #1
	 * @param strCurrency2 Govvie Currency #2
	 * 
	 * @return The Correlation Surface for the specified Govvie Currency Pair
	 */

	public org.drip.quant.function1D.AbstractUnivariate govvieGovvieCorrSurface (
		final java.lang.String strCurrency1,
		final java.lang.String strCurrency2)
	{
		if (null == strCurrency1 || strCurrency1.isEmpty() || null == strCurrency2 || strCurrency2.isEmpty())
			return null;

		java.lang.String strCode = strCurrency1 + "@#" + strCurrency2;

		if (!_mapGovvieGovvieCorrelationSurface.containsKey (strCode)) return null;

		return _mapGovvieGovvieCorrelationSurface.get (strCode);
	}

	/**
	 * (Re)-set the Correlation Surface for the specified Govvie Currency Pair
	 * 
	 * @param strCurrency1 Govvie Currency #1
	 * @param strCurrency2 Govvie Currency #2
	 * @param auCorrelation The Correlation Surface
	 * 
	 * @return TRUE => Successfully set
	 */

	public boolean setGovvieGovvieCorrSurface (
		final java.lang.String strCurrency1,
		final java.lang.String strCurrency2,
		final org.drip.quant.function1D.AbstractUnivariate auCorrelation)
	{
		if (null == strCurrency1 || strCurrency1.isEmpty() || null == strCurrency2 || strCurrency2.isEmpty()
			|| null == auCorrelation)
			return false;

		_mapGovvieGovvieCorrelationSurface.put (strCurrency1 + "@#" + strCurrency2, auCorrelation);

		_mapGovvieGovvieCorrelationSurface.put (strCurrency2 + "@#" + strCurrency1, auCorrelation);

		return true;
	}

	/**
	 * Retrieve the Correlation Surface for the specified Collateral Currency and Credit
	 * 
	 * @param strCollateralCurrency The Collateral Currency
	 * @param strCreditCurveName The Credit Curve Name
	 * 
	 * @return The Correlation Surface for the specified Collateral Currency and Credit
	 */

	public org.drip.quant.function1D.AbstractUnivariate collateralCreditCorrSurface (
		final java.lang.String strCollateralCurrency,
		final java.lang.String strCreditCurveName)
	{
		if (null == strCollateralCurrency || strCollateralCurrency.isEmpty() || null == strCreditCurveName ||
			strCreditCurveName.isEmpty())
			return null;

		java.lang.String strCode = strCollateralCurrency + "@#" + strCreditCurveName;

		if (!_mapCollateralCreditCorrelationSurface.containsKey (strCode)) return null;

		return _mapCollateralCreditCorrelationSurface.get (strCode);
	}

	/**
	 * (Re)-set the Correlation Surface for the specified Collateral Currency and Credit
	 * 
	 * @param strCollateralCurrency The Collateral Currency
	 * @param strCreditCurveName The Credit Curve Name
	 * @param auCorrelation The Correlation Surface
	 * 
	 * @return TRUE => Successfully set
	 */

	public boolean setCollateralCreditCorrSurface (
		final java.lang.String strCollateralCurrency,
		final java.lang.String strCreditCurveName,
		final org.drip.quant.function1D.AbstractUnivariate auCorrelation)
	{
		if (null == strCollateralCurrency || strCollateralCurrency.isEmpty() || null == strCreditCurveName ||
			strCreditCurveName.isEmpty())
			return false;

		_mapCollateralCreditCorrelationSurface.put (strCollateralCurrency + "@#" + strCreditCurveName,
			auCorrelation);

		return true;
	}

	/**
	 * Retrieve the Correlation Surface for the specified Collateral Currency and the Custom Metric
	 * 
	 * @param strCollateralCurrency The Collateral Currency
	 * @param strCustomMetric The Custom Metric
	 * 
	 * @return The Correlation Surface for the specified Collateral Currency and the Custom Metric
	 */

	public org.drip.quant.function1D.AbstractUnivariate collateralCustomMetricCorrSurface (
		final java.lang.String strCollateralCurrency,
		final java.lang.String strCustomMetric)
	{
		if (null == strCollateralCurrency || strCollateralCurrency.isEmpty() || null == strCustomMetric ||
			strCustomMetric.isEmpty())
			return null;

		java.lang.String strCode = strCollateralCurrency + "@#" + strCustomMetric;

		if (!_mapCollateralCustomMetricCorrelationSurface.containsKey (strCode)) return null;

		return _mapCollateralCustomMetricCorrelationSurface.get (strCode);
	}

	/**
	 * (Re)-set the Correlation Surface for the specified Collateral Currency and the Custom Metric
	 * 
	 * @param strCollateralCurrency The Collateral Currency
	 * @param strCustomMetric The Custom Metric
	 * @param auCorrelation The Correlation Surface
	 * 
	 * @return TRUE => Successfully set
	 */

	public boolean setCollateralCustomMetricCorrSurface (
		final java.lang.String strCollateralCurrency,
		final java.lang.String strCustomMetric,
		final org.drip.quant.function1D.AbstractUnivariate auCorrelation)
	{
		if (null == strCollateralCurrency || strCollateralCurrency.isEmpty() || null == strCustomMetric ||
			strCustomMetric.isEmpty())
			return false;

		_mapCollateralCustomMetricCorrelationSurface.put (strCollateralCurrency + "@#" + strCustomMetric,
			auCorrelation);

		return true;
	}

	/**
	 * Retrieve the Correlation Surface for the specified Collateral Currency and the FRI
	 * 
	 * @param strCollateralCurrency The Collateral Currency
	 * @param fri The Floating Rate Index
	 * 
	 * @return The Correlation Surface for the specified Collateral Currency and the FRI
	 */

	public org.drip.quant.function1D.AbstractUnivariate collateralForwardCorrSurface (
		final java.lang.String strCollateralCurrency,
		final org.drip.product.params.FloatingRateIndex fri)
	{
		if (null == strCollateralCurrency || strCollateralCurrency.isEmpty() || null == fri) return null;

		java.lang.String strCollateralForwardCorrelationCode = strCollateralCurrency + "@#" +
			fri.fullyQualifiedName();

		if (!_mapCollateralForwardCorrelationSurface.containsKey (strCollateralForwardCorrelationCode))
			return null;

		return _mapCollateralForwardCorrelationSurface.get (strCollateralForwardCorrelationCode);
	}

	/**
	 * (Re)-set the Correlation Surface for the specified Collateral Currency and the FRI
	 * 
	 * @param strCollateralCurrency The Collateral Currency
	 * @param fri The Floating Rate Index
	 * @param auCorrelation The Correlation Surface
	 * 
	 * @return TRUE => Successfully set
	 */

	public boolean setCollateralForwardCorrSurface (
		final java.lang.String strCollateralCurrency,
		final org.drip.product.params.FloatingRateIndex fri,
		final org.drip.quant.function1D.AbstractUnivariate auCorrelation)
	{
		if (null == strCollateralCurrency || strCollateralCurrency.isEmpty() || null == fri || null ==
			auCorrelation)
			return false;

		_mapCollateralForwardCorrelationSurface.put (strCollateralCurrency + "@#" + fri.fullyQualifiedName(),
			auCorrelation);

		return true;
	}

	/**
	 * Retrieve the Correlation Surface for the specified Collateral and the Funding Currencies
	 * 
	 * @param strCollateralCurrency The Collateral Currency
	 * @param strFundingCurrency The Funding Currency
	 * 
	 * @return The Correlation Surface for the specified Collateral and the Funding Curves
	 */

	public org.drip.quant.function1D.AbstractUnivariate collateralFundingCorrSurface (
		final java.lang.String strCollateralCurrency,
		final java.lang.String strFundingCurrency)
	{
		if (null == strCollateralCurrency || strCollateralCurrency.isEmpty() || null == strFundingCurrency ||
			strFundingCurrency.isEmpty())
			return null;

		java.lang.String strCode = strCollateralCurrency + "@#" + strFundingCurrency;

		if (!_mapCollateralFundingCorrelationSurface.containsKey (strCode)) return null;

		return _mapCollateralFundingCorrelationSurface.get (strCode);
	}

	/**
	 * (Re)-set the Correlation Surface for the specified Collateral and the Funding Currencies
	 * 
	 * @param strCollateralCurrency The Collateral Currency
	 * @param strFundingCurrency The Funding Currency
	 * @param auCorrelation The Correlation Surface
	 * 
	 * @return TRUE => Successfully set
	 */

	public boolean setCollateralFundingCorrSurface (
		final java.lang.String strCollateralCurrency,
		final java.lang.String strFundingCurrency,
		final org.drip.quant.function1D.AbstractUnivariate auCorrelation)
	{
		if (null == strCollateralCurrency || strCollateralCurrency.isEmpty() || null == strFundingCurrency ||
			strFundingCurrency.isEmpty() || null == auCorrelation)
			return false;

		_mapCollateralFundingCorrelationSurface.put (strCollateralCurrency + "@#" + strFundingCurrency,
			auCorrelation);

		return true;
	}

	/**
	 * Retrieve the Correlation Surface for the specified Collateral and Currency Pair FX Combination
	 * 
	 * @param strCollateralCurrency The Collateral Currency
	 * @param cp The Currency Pair
	 * 
	 * @return The Correlation Surface for the specified Collateral and Currency Pair FX Combination
	 */

	public org.drip.quant.function1D.AbstractUnivariate collateralFXCorrSurface (
		final java.lang.String strCollateralCurrency,
		final org.drip.product.params.CurrencyPair cp)
	{
		if (null == strCollateralCurrency || strCollateralCurrency.isEmpty() || null == cp) return null;

		java.lang.String strCode = strCollateralCurrency + "@#" + cp.code();

		return _mapCollateralFXCorrelationSurface.containsKey (strCode) ?
			_mapCollateralFXCorrelationSurface.get (strCode) : null;
	}

	/**
	 * (Re)-set the Correlation Surface for the specified Collateral and Currency Pair FX Combination
	 * 
	 * @param strCollateralCurrency The Collateral Currency
	 * @param cp The Currency Pair
	 * @param auCorrelation The Correlation Surface
	 * 
	 * @return TRUE => Successfully set
	 */

	public boolean setCollateralFXCorrSurface (
		final java.lang.String strCollateralCurrency,
		final org.drip.product.params.CurrencyPair cp,
		final org.drip.quant.function1D.AbstractUnivariate auCorrelation)
	{
		if (null == strCollateralCurrency || strCollateralCurrency.isEmpty() || null == cp || null ==
			auCorrelation)
			return false;

		_mapCollateralFXCorrelationSurface.put (strCollateralCurrency + "@#" + cp.code(), auCorrelation);

		return true;
	}

	/**
	 * Retrieve the Correlation Surface for the specified Collateral and Govvie Currencies
	 * 
	 * @param strCollateralCurrency The Collateral Currency
	 * @param strGovvieCurrency The Govvie Currency
	 * 
	 * @return The Correlation Surface for the specified Collateral and Govvie Curves
	 */

	public org.drip.quant.function1D.AbstractUnivariate collateralGovvieCorrSurface (
		final java.lang.String strCollateralCurrency,
		final java.lang.String strGovvieCurrency)
	{
		if (null == strCollateralCurrency || strCollateralCurrency.isEmpty() || null == strGovvieCurrency ||
			strGovvieCurrency.isEmpty())
			return null;

		java.lang.String strCode = strCollateralCurrency + "@#" + strGovvieCurrency;

		return _mapCollateralGovvieCorrelationSurface.containsKey (strCode) ?
			_mapCollateralGovvieCorrelationSurface.get (strCode) : null;
	}

	/**
	 * (Re)-set the Correlation Surface for the specified Collateral and Govvie Currencies
	 * 
	 * @param strCollateralCurrency The Collateral Currency
	 * @param strGovvieCurrency The Govvie Currency
	 * @param auCorrelation The Correlation Surface
	 * 
	 * @return TRUE => Successfully set
	 */

	public boolean setCollateralGovvieCorrSurface (
		final java.lang.String strCollateralCurrency,
		final java.lang.String strGovvieCurrency,
		final org.drip.quant.function1D.AbstractUnivariate auCorrelation)
	{
		if (null == strCollateralCurrency || strCollateralCurrency.isEmpty() || null == strGovvieCurrency ||
			strGovvieCurrency.isEmpty() || null == auCorrelation)
			return false;

		_mapCollateralGovvieCorrelationSurface.put (strCollateralCurrency + "@#" + strGovvieCurrency,
			auCorrelation);

		return true;
	}

	/**
	 * Retrieve the Correlation Surface for the specified Credit and the Custom Metric
	 * 
	 * @param strCreditCurveName The Credit Curve Name
	 * @param strCustomMetric The Custom Metric
	 * 
	 * @return The Correlation Surface for the specified Credit and the Custom Metric
	 */

	public org.drip.quant.function1D.AbstractUnivariate creditCustomMetricCorrSurface (
		final java.lang.String strCreditCurveName,
		final java.lang.String strCustomMetric)
	{
		if (null == strCreditCurveName || strCreditCurveName.isEmpty() || null == strCustomMetric ||
			strCustomMetric.isEmpty())
			return null;

		java.lang.String strCode = strCreditCurveName + "@#" + strCustomMetric;

		return _mapCreditCustomMetricCorrelationSurface.containsKey (strCode) ?
			_mapCreditCustomMetricCorrelationSurface.get (strCode) : null;
	}

	/**
	 * (Re)-set the Correlation Surface for the specified Credit and the Custom Metric
	 * 
	 * @param strCreditCurveName The Credit Curve Name
	 * @param strCustomMetric The Custom Metric
	 * @param auCorrelation The Correlation Surface
	 * 
	 * @return TRUE => Successfully set
	 */

	public boolean setCreditCustomMetricCorrSurface (
		final java.lang.String strCreditCurveName,
		final java.lang.String strCustomMetric,
		final org.drip.quant.function1D.AbstractUnivariate auCorrelation)
	{
		if (null == strCreditCurveName || strCreditCurveName.isEmpty() || null == strCustomMetric ||
			strCustomMetric.isEmpty() || null == auCorrelation)
			return false;

		_mapCreditCustomMetricCorrelationSurface.put (strCreditCurveName + "@#" + strCustomMetric,
			auCorrelation);

		return true;
	}

	/**
	 * Retrieve the Correlation Surface for the specified Credit and the FRI
	 * 
	 * @param strCreditCurveName The Credit Curve Name
	 * @param fri The FRI
	 * 
	 * @return The Correlation Surface for the specified Credit and the FRI
	 */

	public org.drip.quant.function1D.AbstractUnivariate creditForwardCorrSurface (
		final java.lang.String strCreditCurveName,
		final org.drip.product.params.FloatingRateIndex fri)
	{
		if (null == strCreditCurveName || strCreditCurveName.isEmpty() || null == fri) return null;

		java.lang.String strCode = strCreditCurveName + "@#" + fri.fullyQualifiedName();

		return _mapCreditForwardCorrelationSurface.containsKey (strCode) ?
			_mapCreditForwardCorrelationSurface.get (strCode) : null;
	}

	/**
	 * (Re)-set the Correlation Surface for the specified Credit and the FRI
	 * 
	 * @param strCreditCurveName The Credit Curve Name
	 * @param fri The FRI
	 * @param auCorrelation The Correlation Surface
	 * 
	 * @return TRUE => Successfully set
	 */

	public boolean setCreditForwardCorrSurface (
		final java.lang.String strCreditCurveName,
		final org.drip.product.params.FloatingRateIndex fri,
		final org.drip.quant.function1D.AbstractUnivariate auCorrelation)
	{
		if (null == strCreditCurveName || strCreditCurveName.isEmpty() || null == fri || null ==
			auCorrelation)
			return false;

		_mapCreditForwardCorrelationSurface.put (strCreditCurveName + "@#" + fri.fullyQualifiedName(),
			auCorrelation);

		return true;
	}

	/**
	 * Retrieve the Correlation Surface for the specified Credit and the Funding Currency
	 * 
	 * @param strCreditCurveName The Credit Curve Name
	 * @param strFundingCurrency The Funding Currency
	 * 
	 * @return The Correlation Surface for the specified Credit and the Funding Currency
	 */

	public org.drip.quant.function1D.AbstractUnivariate creditFundingCorrSurface (
		final java.lang.String strCreditCurveName,
		final java.lang.String strFundingCurrency)
	{
		if (null == strCreditCurveName || strCreditCurveName.isEmpty() || null == strFundingCurrency ||
			strFundingCurrency.isEmpty())
			return null;

		java.lang.String strCode = strCreditCurveName + "@#" + strFundingCurrency;

		return _mapCreditFundingCorrelationSurface.containsKey (strCode) ?
			_mapCreditFundingCorrelationSurface.get (strCode) : null;
	}

	/**
	 * (Re)-set the Correlation Surface for the specified Credit and the Funding Currency
	 * 
	 * @param strCreditCurveName The Credit Curve Name
	 * @param strFundingCurrency The Funding Currency
	 * @param auCorrelation The Correlation Surface
	 * 
	 * @return TRUE => Successfully set
	 */

	public boolean setCreditFundingCorrSurface (
		final java.lang.String strCreditCurveName,
		final java.lang.String strFundingCurrency,
		final org.drip.quant.function1D.AbstractUnivariate auCorrelation)
	{
		if (null == strCreditCurveName || strCreditCurveName.isEmpty() || null == strFundingCurrency ||
			strFundingCurrency.isEmpty() || null == auCorrelation)
			return false;

		_mapCreditFundingCorrelationSurface.put (strCreditCurveName + "@#" + strFundingCurrency,
			auCorrelation);

		return true;
	}

	/**
	 * Retrieve the Correlation Surface for the specified Credit and the Currency Pair FX
	 * 
	 * @param strCreditCurveName The Credit Curve Name
	 * @param cp The Currency Pair
	 * 
	 * @return The Correlation Surface for the specified Credit and the Currency Pair FX
	 */

	public org.drip.quant.function1D.AbstractUnivariate creditFXCorrSurface (
		final java.lang.String strCreditCurveName,
		final org.drip.product.params.CurrencyPair cp)
	{
		if (null == strCreditCurveName || strCreditCurveName.isEmpty() || null == cp) return null;

		java.lang.String strCode = strCreditCurveName + "@#" + cp.code();

		return _mapCreditFXCorrelationSurface.containsKey (strCode) ? _mapCreditFXCorrelationSurface.get
			(strCode) : null;
	}

	/**
	 * (Re)-set the Correlation Surface for the specified Credit and the Currency Pair FX
	 * 
	 * @param strCreditCurveName The Credit Curve Name
	 * @param cp The Currency Pair
	 * @param auCorrelation The Correlation Surface
	 * 
	 * @return TRUE => Successfully set
	 */

	public boolean setCreditFXCorrSurface (
		final java.lang.String strCreditCurveName,
		final org.drip.product.params.CurrencyPair cp,
		final org.drip.quant.function1D.AbstractUnivariate auCorrelation)
	{
		if (null == strCreditCurveName || strCreditCurveName.isEmpty() || null == cp || null ==
			auCorrelation)
			return false;

		_mapCreditFXCorrelationSurface.get (strCreditCurveName + "@#" + cp.code());

		return true;
	}

	/**
	 * Retrieve the Correlation Surface for the specified Credit and the Govvie Currencies
	 * 
	 * @param strCreditCurveName The Credit Curve Name
	 * @param strGovvieCurrency The Govvie Currency
	 * 
	 * @return The Correlation Surface for the specified Credit and the Govvie Currencies
	 */

	public org.drip.quant.function1D.AbstractUnivariate creditGovvieCorrSurface (
		final java.lang.String strCreditCurveName,
		final java.lang.String strGovvieCurrency)
	{
		if (null == strCreditCurveName || strCreditCurveName.isEmpty() || null == strGovvieCurrency ||
			strGovvieCurrency.isEmpty())
			return null;

		java.lang.String strCode = strCreditCurveName + "@#" + strGovvieCurrency;

		return _mapCreditGovvieCorrelationSurface.containsKey (strCode) ?
			_mapCreditGovvieCorrelationSurface.get (strCode) : null;
	}

	/**
	 * (Re)-set the Correlation Surface for the specified Credit and the Govvie Currencies
	 * 
	 * @param strCreditCurveName The Credit Curve Name
	 * @param strGovvieCurrency The Govvie Currency
	 * @param auCorrelation The Correlation Surface
	 * 
	 * @return TRUE => Successfully set
	 */

	public boolean setCreditGovvieCorrSurface (
		final java.lang.String strCreditCurveName,
		final java.lang.String strGovvieCurrency,
		final org.drip.quant.function1D.AbstractUnivariate auCorrelation)
	{
		if (null == strCreditCurveName || strCreditCurveName.isEmpty() || null == strGovvieCurrency ||
			strGovvieCurrency.isEmpty() || null == auCorrelation)
			return false;

		_mapCreditGovvieCorrelationSurface.put (strCreditCurveName + "@#" + strGovvieCurrency,
			auCorrelation);

		return true;
	}

	/**
	 * Retrieve the Correlation Surface for the specified Custom Metric and the FRI
	 * 
	 * @param strCustomMetric The Custom Metric
	 * @param fri The FRI
	 * 
	 * @return The Correlation Surface for the specified Custom Metric and the FRI
	 */

	public org.drip.quant.function1D.AbstractUnivariate customMetricForwardCorrSurface (
		final java.lang.String strCustomMetric,
		final org.drip.product.params.FloatingRateIndex fri)
	{
		if (null == strCustomMetric || strCustomMetric.isEmpty() || null == fri) return null;

		java.lang.String strFRI = fri.fullyQualifiedName();

		if (!_mapCustomMetricForwardCorrelationSurface.containsKey (strCustomMetric + "@#" + strFRI))
			return null;

		return _mapCustomMetricForwardCorrelationSurface.get (strCustomMetric + "@#" + strFRI);
	}

	/**
	 * (Re)-set the Correlation Surface for the specified Custom Metric and the FRI
	 * 
	 * @param strCustomMetric The Custom Metric
	 * @param fri The FRI
	 * @param auCorrelation The Correlation Surface
	 * 
	 * @return TRUE => Successfully set
	 */

	public boolean setCustomMetricForwardCorrSurface (
		final java.lang.String strCustomMetric,
		final org.drip.product.params.FloatingRateIndex fri,
		final org.drip.quant.function1D.AbstractUnivariate auCorrelation)
	{
		if (null == strCustomMetric || strCustomMetric.isEmpty() || null == fri || null == auCorrelation)
			return false;

		_mapCustomMetricForwardCorrelationSurface.put (strCustomMetric + "@#" + fri.fullyQualifiedName(),
			auCorrelation);

		return true;
	}

	/**
	 * Retrieve the Correlation Surface for the specified Custom Metric and the Funding Currency
	 * 
	 * @param strCustomMetric The Custom Metric
	 * @param strFundingCurrency The Funding Currency
	 * 
	 * @return The Correlation Surface for the specified Custom Metric and the Funding Currency
	 */

	public org.drip.quant.function1D.AbstractUnivariate customMetricFundingCorrSurface (
		final java.lang.String strCustomMetric,
		final java.lang.String strFundingCurrency)
	{
		if (null == strCustomMetric || strCustomMetric.isEmpty() || null == strFundingCurrency ||
			strFundingCurrency.isEmpty())
			return null;

		java.lang.String strCode = strCustomMetric + "@#" + strFundingCurrency;

		if (!_mapCustomMetricFundingCorrelationSurface.containsKey (strCode)) return null;

		return _mapCustomMetricFundingCorrelationSurface.get (strCode);
	}

	/**
	 * (Re)-set the Correlation Surface for the specified Custom Metric and the Funding Currency
	 * 
	 * @param strCustomMetric The Custom Metric
	 * @param strFundingCurrency The Funding Currency
	 * @param auCorrelation The Correlation Surface
	 * 
	 * @return TRUE => Successfully set
	 */

	public boolean setCustomMetricFundingCorrSurface (
		final java.lang.String strCustomMetric,
		final java.lang.String strFundingCurrency,
		final org.drip.quant.function1D.AbstractUnivariate auCorrelation)
	{
		if (null == strCustomMetric || strCustomMetric.isEmpty() || null == strFundingCurrency ||
			strFundingCurrency.isEmpty())
			return false;

		_mapCustomMetricFundingCorrelationSurface.put (strCustomMetric + "@#" + strFundingCurrency,
			auCorrelation);

		return true;
	}

	/**
	 * Retrieve the Correlation Surface for the specified Custom Metric and the Currency Pair FX
	 * 
	 * @param strCustomMetric The Custom Metric
	 * @param cp The Currency Pair
	 * 
	 * @return The Correlation Surface for the specified Custom Metric and the Currency Pair FX
	 */

	public org.drip.quant.function1D.AbstractUnivariate customMetricFXCorrSurface (
		final java.lang.String strCustomMetric,
		final org.drip.product.params.CurrencyPair cp)
	{
		if (null == strCustomMetric || strCustomMetric.isEmpty() || null == cp) return null;

		java.lang.String strCode = strCustomMetric + "@#" + cp.code();

		return _mapCustomMetricFXCorrelationSurface.containsKey (strCode) ?
			_mapCustomMetricFXCorrelationSurface.get (strCode) : null;
	}

	/**
	 * (Re)-set the Correlation Surface for the specified Custom Metric and the Currency Pair FX
	 * 
	 * @param strCustomMetric The Custom Metric
	 * @param cp The Currency Pair
	 * @param auCorrelation The Correlation Surface
	 * 
	 * @return TRUE => Successfully set
	 */

	public boolean setCustomMetricFXCorrSurface (
		final java.lang.String strCustomMetric,
		final org.drip.product.params.CurrencyPair cp,
		final org.drip.quant.function1D.AbstractUnivariate auCorrelation)
	{
		if (null == strCustomMetric || strCustomMetric.isEmpty() || null == cp || null == auCorrelation)
			return false;

		_mapCustomMetricFXCorrelationSurface.get (strCustomMetric + "@#" + cp.code());

		return true;
	}

	/**
	 * Retrieve the Correlation Surface for the specified Custom Metric and the Govvie Currency
	 * 
	 * @param strCustomMetric The Custom Metric
	 * @param strGovvieCurrency The Govvie Currency
	 * 
	 * @return The Correlation Surface for the specified Custom Metric and the Govvie Currency
	 */

	public org.drip.quant.function1D.AbstractUnivariate customMetricGovvieCorrSurface (
		final java.lang.String strCustomMetric,
		final java.lang.String strGovvieCurrency)
	{
		if (null == strCustomMetric || strCustomMetric.isEmpty() || null == strGovvieCurrency ||
			strGovvieCurrency.isEmpty())
			return null;

		java.lang.String strCode = strCustomMetric + "@#" + strGovvieCurrency;

		return _mapCustomMetricGovvieCorrelationSurface.containsKey (strCode) ?
			_mapCustomMetricGovvieCorrelationSurface.get (strCode) : null;
	}

	/**
	 * (Re)-set the Correlation Surface for the specified Custom Metric and the Govvie Currency
	 * 
	 * @param strCustomMetric The Custom Metric
	 * @param strGovvieCurrency The Govvie Currency
	 * @param auCorrelation The Correlation Surface
	 * 
	 * @return TRUE => Successfully set
	 */

	public boolean setCustomMetricGovvieCorrSurface (
		final java.lang.String strCustomMetric,
		final java.lang.String strGovvieCurrency,
		final org.drip.quant.function1D.AbstractUnivariate auCorrelation)
	{
		if (null == strCustomMetric || strCustomMetric.isEmpty() || null == strGovvieCurrency ||
			strGovvieCurrency.isEmpty())
			return false;

		_mapCustomMetricGovvieCorrelationSurface.put (strCustomMetric + "@#" + strGovvieCurrency,
			auCorrelation);

		return true;
	}

	/**
	 * Retrieve the Correlation Surface for the specified FRI and the Funding Currency
	 * 
	 * @param fri The FRI
	 * @param strFundingCurrency The Funding Currency
	 * 
	 * @return The Correlation Surface for the specified FRI and the Funding Currency
	 */

	public org.drip.quant.function1D.AbstractUnivariate forwardFundingCorrSurface (
		final org.drip.product.params.FloatingRateIndex fri,
		final java.lang.String strFundingCurrency)
	{
		if (null == fri || null == strFundingCurrency || strFundingCurrency.isEmpty()) return null;

		java.lang.String strCode = fri.fullyQualifiedName() + "@#" + strFundingCurrency;

		if (!_mapForwardFundingCorrelationSurface.containsKey (strCode)) return null;

		return _mapForwardFundingCorrelationSurface.get (strCode);
	}

	/**
	 * (Re)-set the Correlation Surface for the specified FRI and the Funding Currency
	 * 
	 * @param fri The FRI
	 * @param strFundingCurrency The Funding Currency
	 * @param auCorrelation The Correlation Surface
	 * 
	 * @return TRUE => Successfully set
	 */

	public boolean setForwardFundingCorrSurface (
		final org.drip.product.params.FloatingRateIndex fri,
		final java.lang.String strFundingCurrency,
		final org.drip.quant.function1D.AbstractUnivariate auCorrelation)
	{
		if (null == fri || null == strFundingCurrency || strFundingCurrency.isEmpty() || null ==
			auCorrelation)
			return false;

		_mapForwardFundingCorrelationSurface.put (fri.fullyQualifiedName() + "@#" + strFundingCurrency,
			auCorrelation);

		return true;
	}

	/**
	 * Retrieve the Correlation Surface for the specified FRI and the FX Currency Pair
	 * 
	 * @param fri The FRI
	 * @param cp The FX Currency Pair
	 * 
	 * @return The Correlation Surface for the specified FRI and the FX Currency Pair
	 */

	public org.drip.quant.function1D.AbstractUnivariate forwardFXCorrSurface (
		final org.drip.product.params.FloatingRateIndex fri,
		final org.drip.product.params.CurrencyPair cp)
	{
		if (null == fri || null == cp) return null;

		java.lang.String strCode = fri.fullyQualifiedName() + "@#" + cp.code();

		return _mapForwardFXCorrelationSurface.containsKey (strCode) ? _mapForwardFXCorrelationSurface.get
			(strCode) : null;
	}

	/**
	 * (Re)-set the Correlation Surface for the specified FRI and the FX Currency Pair
	 * 
	 * @param fri The FRI
	 * @param cp The FX Currency Pair
	 * @param auCorrelation The Correlation Surface
	 * 
	 * @return TRUE => Successfully set
	 */

	public boolean setForwardFXCorrSurface (
		final org.drip.product.params.FloatingRateIndex fri,
		final org.drip.product.params.CurrencyPair cp,
		final org.drip.quant.function1D.AbstractUnivariate auCorrelation)
	{
		if (null == fri || null == cp || null == auCorrelation) return false;

		_mapForwardFXCorrelationSurface.get (fri.fullyQualifiedName() + "@#" + cp.code());

		return true;
	}

	/**
	 * Retrieve the Correlation Surface for the specified FRI and the Govvie Currency
	 * 
	 * @param fri The FRI
	 * @param strGovvieCurrency The Govvie Currency
	 * 
	 * @return The Correlation Surface for the specified FRI and the Govvie Currency
	 */

	public org.drip.quant.function1D.AbstractUnivariate forwardGovvieCorrSurface (
		final org.drip.product.params.FloatingRateIndex fri,
		final java.lang.String strGovvieCurrency)
	{
		if (null == fri || null == strGovvieCurrency || strGovvieCurrency.isEmpty()) return null;

		java.lang.String strCode = fri.fullyQualifiedName() + "@#" + strGovvieCurrency;

		return _mapForwardGovvieCorrelationSurface.containsKey (strCode) ?
			_mapForwardGovvieCorrelationSurface.get (strCode) : null;
	}

	/**
	 * (Re)-set the Correlation Surface for the specified FRI and the Govvie Currency
	 * 
	 * @param fri The FRI
	 * @param strGovvieCurrency The Govvie Currency
	 * @param auCorrelation The Correlation Surface
	 * 
	 * @return TRUE => Successfully set
	 */

	public boolean setForwardGovvieCorrSurface (
		final org.drip.product.params.FloatingRateIndex fri,
		final java.lang.String strGovvieCurrency,
		final org.drip.quant.function1D.AbstractUnivariate auCorrelation)
	{
		if (null == fri || null == strGovvieCurrency || strGovvieCurrency.isEmpty() || null == auCorrelation)
			return false;

		_mapForwardGovvieCorrelationSurface.put (fri.fullyQualifiedName() + "@#" + strGovvieCurrency,
			auCorrelation);

		return true;
	}

	/**
	 * Retrieve the Correlation Surface for the specified Funding Currency and the FX Currency Pair
	 * 
	 * @param strFundingCurrency The Funding Currency
	 * @param cp The FX Currency Pair
	 * 
	 * @return The Correlation Surface for the specified Funding Currency and the FX Currency Pair
	 */

	public org.drip.quant.function1D.AbstractUnivariate fundingFXCorrSurface (
		final java.lang.String strFundingCurrency,
		final org.drip.product.params.CurrencyPair cp)
	{
		if (null == strFundingCurrency || strFundingCurrency.isEmpty() || null == cp) return null;

		java.lang.String strCode = strFundingCurrency + "@#" + cp.code();

		return _mapFundingFXCorrelationSurface.containsKey (strCode) ? _mapFundingFXCorrelationSurface.get
			(strCode) : null;
	}

	/**
	 * (Re)-set the Correlation Surface for the specified Funding Currency and the FX Currency Pair
	 * 
	 * @param strFundingCurrency The Funding Currency
	 * @param cp The FX Currency Pair
	 * @param auCorrelation The Correlation Surface
	 * 
	 * @return TRUE => Successfully set
	 */

	public boolean setFundingFXCorrSurface (
		final java.lang.String strFundingCurrency,
		final org.drip.product.params.CurrencyPair cp,
		final org.drip.quant.function1D.AbstractUnivariate auCorrelation)
	{
		if (null == strFundingCurrency || strFundingCurrency.isEmpty() || null == cp || null ==
			auCorrelation)
			return false;

		_mapFundingFXCorrelationSurface.put (strFundingCurrency + "@#" + cp.code(), auCorrelation);

		return true;
	}

	/**
	 * Retrieve the Correlation Surface for the specified Funding and the Govvie Currencies
	 * 
	 * @param strFundingCurrency The Funding Currency
	 * @param strGovvieCurrency The Govvie Currency
	 * 
	 * @return The Correlation Surface for the specified Funding and the Govvie Currencies
	 */

	public org.drip.quant.function1D.AbstractUnivariate fundingGovvieCorrSurface (
		final java.lang.String strFundingCurrency,
		final java.lang.String strGovvieCurrency)
	{
		if (null == strFundingCurrency || strFundingCurrency.isEmpty() || null == strGovvieCurrency ||
			strGovvieCurrency.isEmpty())
			return null;

		java.lang.String strCode = strFundingCurrency + "@#" + strGovvieCurrency;

		return _mapFundingGovvieCorrelationSurface.containsKey (strCode) ?
			_mapFundingGovvieCorrelationSurface.get (strCode) : null;
	}

	/**
	 * (Re)-set the Correlation Surface for the specified Funding and the Govvie Currencies
	 * 
	 * @param strFundingCurrency The Funding Currency
	 * @param strGovvieCurrency The Govvie Currency
	 * @param auCorrelation The Correlation Surface
	 * 
	 * @return TRUE => Successfully set
	 */

	public boolean setFundingGovvieCorrSurface (
		final java.lang.String strFundingCurrency,
		final java.lang.String strGovvieCurrency,
		final org.drip.quant.function1D.AbstractUnivariate auCorrelation)
	{
		if (null == strFundingCurrency || strFundingCurrency.isEmpty() || null == strGovvieCurrency ||
			strGovvieCurrency.isEmpty() || null == auCorrelation)
			return false;

		_mapFundingGovvieCorrelationSurface.put (strFundingCurrency + "@#" + strGovvieCurrency,
			auCorrelation);

		return true;
	}

	/**
	 * Retrieve the Correlation Surface for the specified FX Currency Pair and the Govvie Currency
	 * 
	 * @param cp The Currency Pair
	 * @param strGovvieCurrency The Govvie Currency
	 * 
	 * @return The Correlation Surface for the specified FX Currency Pair and the Govvie Currency
	 */

	public org.drip.quant.function1D.AbstractUnivariate fxGovvieCorrSurface (
		final org.drip.product.params.CurrencyPair cp,
		final java.lang.String strGovvieCurrency)
	{
		if (null == cp || null == strGovvieCurrency || strGovvieCurrency.isEmpty()) return null;

		java.lang.String strCode = cp.code() + "@#" + strGovvieCurrency;

		return _mapFXGovvieCorrelationSurface.containsKey (strCode) ? _mapFXGovvieCorrelationSurface.get
			(strCode) : null;
	}

	/**
	 * (Re)-set the Correlation Surface for the specified FX Currency Pair and the Govvie Currency
	 * 
	 * @param cp The Currency Pair
	 * @param strGovvieCurrency The Govvie Currency
	 * @param auCorrelation The Correlation Surface
	 * 
	 * @return TRUE => Successfully set
	 */

	public boolean setFXGovvieCorrSurface (
		final org.drip.product.params.CurrencyPair cp,
		final java.lang.String strGovvieCurrency,
		final org.drip.quant.function1D.AbstractUnivariate auCorrelation)
	{
		if (null == cp || null == strGovvieCurrency || strGovvieCurrency.isEmpty() || null == auCorrelation)
			return false;

		_mapFXGovvieCorrelationSurface.put (cp.code() + "@#" + strGovvieCurrency, auCorrelation);

		return true;
	}

	/**
	 * Retrieve the Product Quote
	 * 
	 * @param strProductCode Product Code
	 * 
	 * @return Product Quote
	 */

	public org.drip.param.definition.ProductQuote productQuote (
		final java.lang.String strProductCode)
	{
		if (null == strProductCode || strProductCode.isEmpty() || !_mapProductQuote.containsKey
			(strProductCode))
			return null;

		return _mapProductQuote.get (strProductCode);
	}

	/**
	 * (Re)-set the Product Quote
	 * 
	 * @param strProductCode Product Code
	 * @param pq Product Quote
	 * 
	 * @return TRUE => Successfully set
	 */

	public boolean setProductQuote (
		final java.lang.String strProductCode,
		final org.drip.param.definition.ProductQuote pq)
	{
		if (null == strProductCode || strProductCode.isEmpty() || null == pq) return false;

		_mapProductQuote.put (strProductCode, pq);

		return true;
	}

	/**
	 * Retrieve the Full Set of Quotes
	 * 
	 * @return The Full Set of Quotes
	 */

	public
		org.drip.analytics.support.CaseInsensitiveTreeMap<org.drip.param.definition.ProductQuote>
			quoteMap()
	{
		return _mapProductQuote;
	}

	/**
	 * (Re)-set the Map of Quote
	 * 
	 * @param mapQuote Map of Quotes
	 * 
	 * @return TRUE => Successfully set
	 */

	public boolean setQuoteMap (
		final org.drip.analytics.support.CaseInsensitiveTreeMap<org.drip.param.definition.ProductQuote>
			mapQuote)
	{
		if (null == mapQuote || 0 == mapQuote.size()) return false;

		for (java.util.Map.Entry<java.lang.String, org.drip.param.definition.ProductQuote> meCQ :
			mapQuote.entrySet()) {
			if (null == meCQ) continue;

			java.lang.String strKey = meCQ.getKey();

			org.drip.param.definition.ProductQuote cq = meCQ.getValue();

			if (null == strKey || strKey.isEmpty() || null == cq) continue;

			_mapProductQuote.put (strKey, cq);
		}

		return true;
	}

	/**
	 * Retrieve the Fixings
	 * 
	 * @return The Fixings Object
	 */

	public java.util.Map<org.drip.analytics.date.JulianDate,
		org.drip.analytics.support.CaseInsensitiveTreeMap<java.lang.Double>> fixings()
	{
		return _mmFixings;
	}

	/**
	 * (Re)-set the Fixings
	 * 
	 * @param mmFixings Fixings
	 * 
	 * @return TRUE => Successfully set
	 */

	public boolean setFixings (
		final java.util.Map<org.drip.analytics.date.JulianDate,
			org.drip.analytics.support.CaseInsensitiveTreeMap<java.lang.Double>> mmFixings)
	{
		_mmFixings = mmFixings;
		return true;
	}

	@Override public byte[] serialize()
	{
		java.lang.StringBuffer sb = new java.lang.StringBuffer();

		sb.append (org.drip.service.stream.Serializer.VERSION + fieldDelimiter());

		sb.append (org.drip.service.stream.Serializer.NULL_SER_STRING + fieldDelimiter());

		if (null == _mmFixings || null == _mmFixings.entrySet())
			sb.append (org.drip.service.stream.Serializer.NULL_SER_STRING + fieldDelimiter());
		else {
			boolean bFirstEntry = true;

			java.lang.StringBuffer sbFixings = new java.lang.StringBuffer();

			for (java.util.Map.Entry<org.drip.analytics.date.JulianDate,
				org.drip.analytics.support.CaseInsensitiveTreeMap<java.lang.Double>> meOut :
					_mmFixings.entrySet()) {
				if (null == meOut || null == meOut.getValue() || null == meOut.getValue().entrySet())
					continue;

				for (java.util.Map.Entry<java.lang.String, java.lang.Double> meIn :
					meOut.getValue().entrySet()) {
					if (null == meIn || null == meIn.getKey() || meIn.getKey().isEmpty()) continue;

					if (bFirstEntry)
						bFirstEntry = false;
					else
						sb.append (collectionRecordDelimiter());

					sbFixings.append (meOut.getKey().getJulian() + collectionMultiLevelKeyDelimiter() +
						meIn.getKey() + collectionKeyValueDelimiter() + meIn.getValue());
				}
			}

			if (sbFixings.toString().isEmpty())
				sb.append (org.drip.service.stream.Serializer.NULL_SER_STRING + fieldDelimiter());
			else
				sb.append (sbFixings.toString() + fieldDelimiter());
		}

		if (null == _mapProductQuote || 0 == _mapProductQuote.size())
			sb.append (org.drip.service.stream.Serializer.NULL_SER_STRING);
		else {
			boolean bFirstEntry = true;

			java.lang.StringBuffer sbMapTSYQuotes = new java.lang.StringBuffer();

			for (java.util.Map.Entry<java.lang.String, org.drip.param.definition.ProductQuote> me :
				_mapProductQuote.entrySet()) {
				if (null == me || null == me.getKey() || me.getKey().isEmpty()) continue;

				if (bFirstEntry)
					bFirstEntry = false;
				else
					sbMapTSYQuotes.append (collectionRecordDelimiter());

				sbMapTSYQuotes.append (me.getKey() + collectionKeyValueDelimiter() + new java.lang.String
					(me.getValue().serialize()));
			}

			if (!sbMapTSYQuotes.toString().isEmpty()) sb.append (sbMapTSYQuotes);
		}

		return sb.append (objectTrailer()).toString().getBytes();
	}

	@Override public org.drip.service.stream.Serializer deserialize (
		final byte[] ab)
	{
		try {
			return new CurveSurfaceQuoteSet (ab);
		} catch (java.lang.Exception e) {
			e.printStackTrace();
		}

		return null;
	}

	public static void main (
		final java.lang.String[] astrArgs)
		throws java.lang.Exception
	{
		double dblStart = org.drip.analytics.date.JulianDate.Today().getJulian();

		double[] adblDate = new double[3];
		double[] adblRate = new double[3];
		double[] adblForward = new double[3];
		double[] adblRateTSY = new double[3];
		double[] adblHazardRate = new double[3];

		for (int i = 0; i < 3; ++i) {
			adblDate[i] = dblStart + 365. * (i + 1);
			adblRate[i] = 0.015 * (i + 1);
			adblForward[i] = 0.02 * (i + 1);
			adblRateTSY[i] = 0.01 * (i + 1);
			adblHazardRate[i] = 0.01 * (i + 1);
		}

		org.drip.analytics.rates.ExplicitBootDiscountCurve dc =
			org.drip.state.creator.DiscountCurveBuilder.CreateDC
				(org.drip.analytics.date.JulianDate.Today(), "ABC", null, adblDate, adblRate,
					org.drip.state.creator.DiscountCurveBuilder.BOOTSTRAP_MODE_CONSTANT_FORWARD);

		org.drip.analytics.rates.ExplicitBootDiscountCurve dcTSY =
			org.drip.state.creator.DiscountCurveBuilder.CreateDC
				(org.drip.analytics.date.JulianDate.Today(), "ABCTSY", null, adblDate, adblRateTSY,
					org.drip.state.creator.DiscountCurveBuilder.BOOTSTRAP_MODE_CONSTANT_FORWARD);

		org.drip.analytics.definition.ExplicitBootCreditCurve cc =
			org.drip.state.creator.CreditCurveBuilder.CreateCreditCurve
				(org.drip.analytics.date.JulianDate.Today(), "ABCSOV", "USD", adblDate, adblHazardRate,
					0.40);

		org.drip.param.market.ProductMultiMeasureQuote cq = new
			org.drip.param.market.ProductMultiMeasureQuote();

		cq.addQuote ("Price", new org.drip.param.market.MultiSidedQuote ("ASK", 103., 100000.), false);

		cq.setMarketQuote ("SpreadToTsyBmk", new org.drip.param.market.MultiSidedQuote ("MID", 210.,
			100000.));

		org.drip.analytics.support.CaseInsensitiveTreeMap<org.drip.param.definition.ProductQuote>
			mapTSYQuotes = new
				org.drip.analytics.support.CaseInsensitiveTreeMap<org.drip.param.definition.ProductQuote>();

		mapTSYQuotes.put ("TSY2ON", cq);

		org.drip.analytics.support.CaseInsensitiveTreeMap<java.lang.Double> mIndexFixings = new
			org.drip.analytics.support.CaseInsensitiveTreeMap<java.lang.Double>();

		mIndexFixings.put ("USD-LIBOR-6M", 0.0042);

		java.util.Map<org.drip.analytics.date.JulianDate,
			org.drip.analytics.support.CaseInsensitiveTreeMap<java.lang.Double>> mmFixings = new
				java.util.HashMap<org.drip.analytics.date.JulianDate,
					org.drip.analytics.support.CaseInsensitiveTreeMap<java.lang.Double>>();

		mmFixings.put (org.drip.analytics.date.JulianDate.Today().addDays (2), mIndexFixings);

		org.drip.param.market.CurveSurfaceQuoteSet csqs = new
			org.drip.param.market.CurveSurfaceQuoteSet();

		csqs.setCreditCurve (cc);

		csqs.setGovvieCurve (dcTSY);

		csqs.setProductQuote ("IRSSWAP", cq);

		csqs.setFundingCurve (dc);

		csqs.setFixings (mmFixings);

		csqs.setQuoteMap (mapTSYQuotes);

		byte[] abCSQS = csqs.serialize();

		System.out.println (new java.lang.String (abCSQS));

		CurveSurfaceQuoteSet csqsDeser = new CurveSurfaceQuoteSet (abCSQS);

		System.out.println (new java.lang.String (csqsDeser.serialize()));
	}
}
