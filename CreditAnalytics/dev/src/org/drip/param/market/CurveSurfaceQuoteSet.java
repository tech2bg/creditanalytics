
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

					org.drip.analytics.support.CaseInsensitiveTreeMap<java.lang.Double> map2D =
						_mmFixings.get (astrKeySet[0]);

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
	 * Retrieve the Credit Latent State from the Label
	 * 
	 * @param creditLabel The Credit Latent State Label
	 * 
	 * @return The Credit Latent State from the Label
	 */

	public org.drip.analytics.definition.CreditCurve creditCurve (
		final org.drip.state.identifier.CreditLabel creditLabel)
	{
		if (null == creditLabel) return null;

		java.lang.String strCreditLabel = creditLabel.fullyQualifiedName();

		return !_mapCreditCurve.containsKey (strCreditLabel) ? null : _mapCreditCurve.get (strCreditLabel);
	}

	/**
	 * (Re)-set the Credit Curve
	 * 
	 * @param cc The Credit Curve
	 * 
	 * @return TRUE => Successfully set
	 */

	public boolean setCreditCurve (
		final org.drip.analytics.definition.CreditCurve cc)
	{
		if (null == cc) return false;

		_mapCreditCurve.put (cc.label().fullyQualifiedName(), cc);

		return true;
	}

	/**
	 * Retrieve the Forward Curve corresponding to the Label
	 * 
	 * @param forwardLabel Forward Latent State Label
	 * 
	 * @return Forward Curve
	 */

	public org.drip.analytics.rates.ForwardCurve forwardCurve (
		final org.drip.state.identifier.ForwardLabel forwardLabel)
	{
		if (null == forwardLabel) return null;

		java.lang.String strForwardLabel = forwardLabel.fullyQualifiedName();

		return _mapForwardCurve.containsKey (strForwardLabel) ? _mapForwardCurve.get (strForwardLabel) :
			null;
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

		_mapForwardCurve.put (fc.label().fullyQualifiedName(), fc);

		return true;
	}

	/**
	 * Retrieve the Funding Latent State Corresponding to the Label
	 * 
	 * @param fundingLabel Funding Latent State Label
	 * 
	 * @return The Funding Latent State
	 */

	public org.drip.analytics.rates.DiscountCurve fundingCurve (
		final org.drip.state.identifier.FundingLabel fundingLabel)
	{
		if (null == fundingLabel) return null;

		java.lang.String strFundingLabel = fundingLabel.fullyQualifiedName();

		return _mapFundingCurve.containsKey (strFundingLabel) ? _mapFundingCurve.get (strFundingLabel) :
			null;
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

		_mapFundingCurve.put (dc.label().fullyQualifiedName(), dc);

		return true;
	}

	/**
	 * Retrieve the FX Curve for the specified FX Latent State Label
	 * 
	 * @param fxLabel The FX Latent State Label
	 * 
	 * @return FX Curve
	 */

	public org.drip.quant.function1D.AbstractUnivariate fxCurve (
		final org.drip.state.identifier.FXLabel fxLabel)
	{
		if (null == fxLabel) return null;

		java.lang.String strCode = fxLabel.fullyQualifiedName();

		return _mapFXCurve.containsKey (strCode) ? _mapFXCurve.get (strCode) : null;
	}

	/**
	 * (Re)-set the FX Curve for the specified FX Latent State Label
	 * 
	 * @param fxLabel The FX Latent State Label
	 * @param auFX The FX Curve
	 * 
	 * @return TRUE => Successfully set
	 */

	public boolean setFXCurve (
		final org.drip.state.identifier.FXLabel fxLabel,
		final org.drip.quant.function1D.AbstractUnivariate auFX)
	{
		if (null == fxLabel || null == auFX) return false;

		_mapFXCurve.put (fxLabel.fullyQualifiedName(), auFX);

		try {
			_mapFXCurve.put (fxLabel.inverse().fullyQualifiedName(), new
				org.drip.quant.function1D.UnivariateReciprocal (auFX));
		} catch (java.lang.Exception e) {
			e.printStackTrace();

			return false;
		}

		return true;
	}

	/**
	 * Retrieve the Government Curve for the specified Label
	 * 
	 * @param lslGovvie Govvie Curve Latent State Label
	 * 
	 * @return Government Curve for the specified Label
	 */

	public org.drip.analytics.rates.DiscountCurve govvieCurve (
		final org.drip.state.identifier.GovvieLabel govvieLabel)
	{
		if (null == govvieLabel) return null;

		java.lang.String strGovvieLabel = govvieLabel.fullyQualifiedName();

		return !_mapGovvieCurve.containsKey (strGovvieLabel) ? null : _mapGovvieCurve.get (strGovvieLabel);
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

		_mapGovvieCurve.put (dcGovvie.label().fullyQualifiedName(), dcGovvie);

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
	 * Retrieve the Volatility Surface for the Credit Latent State
	 * 
	 * @param creditLabel The Credit Curve Latent State Label
	 * 
	 * @return The Volatility Surface for the Credit Latent State
	 */

	public org.drip.quant.function1D.AbstractUnivariate creditCurveVolSurface (
		final org.drip.state.identifier.CreditLabel creditLabel)
	{
		if (null == creditLabel) return null;

		java.lang.String strCreditLabel = creditLabel.fullyQualifiedName();

		return  !_mapCreditVolatilitySurface.containsKey (strCreditLabel) ? null :
			_mapCreditVolatilitySurface.get (strCreditLabel);
	}

	/**
	 * (Re)-set the Volatility Surface for the Credit Latent State
	 * 
	 * @param creditLabel The Credit Curve Latent State Label
	 * @param auVolatility The Volatility Surface
	 * 
	 * @return TRUE => Successfully set
	 */

	public boolean setCreditCurveVolSurface (
		final org.drip.state.identifier.CreditLabel creditLabel,
		final org.drip.quant.function1D.AbstractUnivariate auVolatility)
	{
		if (null == creditLabel || null == auVolatility) return false;

		_mapCreditVolatilitySurface.put (creditLabel.fullyQualifiedName(), auVolatility);

		return true;
	}

	/**
	 * Retrieve the Volatility Surface for the Custom Metric Latent State for the Forward Date
	 * 
	 * @param customMetricLabel The Custom Metric Latent State Label
	 * @param dtForward The Forward Date 
	 * 
	 * @return The Volatility Surface for the Custom Metric Latent State for the Forward Date
	 */

	public org.drip.quant.function1D.AbstractUnivariate customMetricVolSurface (
		final org.drip.state.identifier.CustomMetricLabel customMetricLabel,
		final org.drip.analytics.date.JulianDate dtForward)
	{
		if (null == customMetricLabel) return null;

		java.lang.String strCustomMetricLabel = customMetricLabel.fullyQualifiedName();

		if (!_mapCustomMetricVolatilitySurface.containsKey (strCustomMetricLabel)) return null;

		java.util.Map<org.drip.analytics.date.JulianDate, org.drip.quant.function1D.AbstractUnivariate>
			mapForwardVolatility = _mapCustomMetricVolatilitySurface.get (strCustomMetricLabel);

		return null == mapForwardVolatility || !mapForwardVolatility.containsKey (dtForward) ? null :
			mapForwardVolatility.get (dtForward);
	}

	/**
	 * (Re)-set the Custom Metric Volatility Surface for the given Forward Date
	 * 
	 * @param customMetricLabel The Custom Metric Latent State Label
	 * @param dtForward The Forward Date 
	 * @param auVolatility The Custom Metric Volatility Surface
	 * 
	 * @return TRUE => Successfully set
	 */

	public boolean setCustomMetricVolSurface (
		final org.drip.state.identifier.CustomMetricLabel customMetricLabel,
		final org.drip.analytics.date.JulianDate dtForward,
		final org.drip.quant.function1D.AbstractUnivariate auVolatility)
	{
		if (null == customMetricLabel || null == dtForward || null == auVolatility) return false;

		java.lang.String strCustomMetricLabel = customMetricLabel.fullyQualifiedName();

		java.util.Map<org.drip.analytics.date.JulianDate, org.drip.quant.function1D.AbstractUnivariate>
			mapForwardVolatility = _mapCustomMetricVolatilitySurface.get (strCustomMetricLabel);

		if (null == mapForwardVolatility) {
			mapForwardVolatility = new java.util.HashMap<org.drip.analytics.date.JulianDate,
				org.drip.quant.function1D.AbstractUnivariate>();

			mapForwardVolatility.put (dtForward, auVolatility);

			_mapCustomMetricVolatilitySurface.put (strCustomMetricLabel, mapForwardVolatility);
		} else
			mapForwardVolatility.put (dtForward, auVolatility);

		return true;
	}

	/**
	 * Retrieve the Volatility Surface for the specified Forward Latent State Label
	 * 
	 * @param forwardLabel The Forward Latent State Label
	 * 
	 * @return The Volatility Surface for the Forward Curve
	 */

	public org.drip.quant.function1D.AbstractUnivariate forwardCurveVolSurface (
		final org.drip.state.identifier.ForwardLabel forwardLabel)
	{
		if (null == forwardLabel) return null;

		java.lang.String strForwardLabel = forwardLabel.fullyQualifiedName();

		return _mapForwardVolatilitySurface.containsKey (strForwardLabel) ? _mapForwardVolatilitySurface.get
			(strForwardLabel) : null;
	}

	/**
	 * (Re)-set the Volatility Surface for the specified Forward Latent State Label
	 * 
	 * @param forwardLabel The Forward Latent State Label
	 * @param auVolatility The Volatility Surface
	 * 
	 * @return TRUE => Successfully set
	 */

	public boolean setForwardCurveVolSurface (
		final org.drip.state.identifier.ForwardLabel forwardLabel,
		final org.drip.quant.function1D.AbstractUnivariate auVolatility)
	{
		if (null == forwardLabel || null == auVolatility) return false;

		_mapForwardVolatilitySurface.put (forwardLabel.fullyQualifiedName(), auVolatility);

		return true;
	}

	/**
	 * Retrieve the Volatility Surface for the Funding Latent State Label
	 * 
	 * @param fundingLabel The Funding Latent State Label
	 * 
	 * @return The Volatility Surface for the Funding Currency
	 */

	public org.drip.quant.function1D.AbstractUnivariate fundingCurveVolSurface (
		final org.drip.state.identifier.FundingLabel fundingLabel)
	{
		if (null == fundingLabel) return null;

		java.lang.String strFundingLabel = fundingLabel.fullyQualifiedName();

		return _mapFundingVolatilitySurface.containsKey (strFundingLabel) ? _mapFundingVolatilitySurface.get
			(strFundingLabel) : null;
	}

	/**
	 * (Re)-set the Volatility Surface for the Funding Latent State Label
	 * 
	 * @param fundingLabel The Funding Latent State Label
	 * @param auVolatility The Volatility Surface
	 * 
	 * @return TRUE => Successfully set
	 */

	public boolean setFundingCurveVolSurface (
		final org.drip.state.identifier.FundingLabel fundingLabel,
		final org.drip.quant.function1D.AbstractUnivariate auVolatility)
	{
		if (null == fundingLabel || null == auVolatility) return false;

		_mapFundingVolatilitySurface.put (fundingLabel.fullyQualifiedName(), auVolatility);

		return true;
	}

	/**
	 * Retrieve the Volatility Surface for the specified FX Latent State Label
	 * 
	 * @param fxLabel The FX Latent State Label
	 * 
	 * @return The Volatility Surface for the FX Latent State Label
	 */

	public org.drip.quant.function1D.AbstractUnivariate fxCurveVolSurface (
		final org.drip.state.identifier.FXLabel fxLabel)
	{
		if (null == fxLabel) return null;

		java.lang.String strCode = fxLabel.fullyQualifiedName();

		return !_mapFXVolatilitySurface.containsKey (strCode) ? null : _mapFXVolatilitySurface.get
			(strCode);
	}

	/**
	 * (Re)-set the Volatility Surface for the specified FX Latent State
	 * 
	 * @param fxLabel The FX Latent State Label
	 * @param auVolatility The Volatility Surface
	 * 
	 * @return TRUE => Successfully set
	 */

	public boolean setFXCurveVolSurface (
		final org.drip.state.identifier.FXLabel fxLabel,
		final org.drip.quant.function1D.AbstractUnivariate auVolatility)
	{
		if (null == fxLabel || null == auVolatility) return false;

		_mapFXVolatilitySurface.put (fxLabel.fullyQualifiedName(), auVolatility);

		return true;
	}

	/**
	 * Retrieve the Volatility Surface for the specified Govvie Latent State
	 * 
	 * @param govvieLabel The Govvie Latent State Label
	 * 
	 * @return The Volatility Surface for the Govvie Latent State
	 */

	public org.drip.quant.function1D.AbstractUnivariate govvieCurveVolSurface (
		final org.drip.state.identifier.GovvieLabel govvieLabel)
	{
		if (null == govvieLabel) return null;

		java.lang.String strGovvieLabel = govvieLabel.fullyQualifiedName();

		return !_mapGovvieVolatilitySurface.containsKey (strGovvieLabel) ? null :
			_mapGovvieVolatilitySurface.get (strGovvieLabel);
	}

	/**
	 * (Re)-set the Volatility Surface for the Govvie Latent State
	 * 
	 * @param govvieLabel The Govvie Latent State Label
	 * @param auVolatility The Volatility Surface
	 * 
	 * @return TRUE => Successfully set
	 */

	public boolean setGovvieCurveVolSurface (
		final org.drip.state.identifier.GovvieLabel govvieLabel,
		final org.drip.quant.function1D.AbstractUnivariate auVolatility)
	{
		if (null == govvieLabel || null == auVolatility) return false;

		_mapGovvieVolatilitySurface.put (govvieLabel.fullyQualifiedName(), auVolatility);

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
	 * Retrieve the Correlation Surface between the Pair of Credit Latent States
	 * 
	 * @param creditLabel1 The Credit Curve Latent State Label #1
	 * @param creditLabel2 The Credit Curve Latent State Label #2
	 * 
	 * @return The Correlation Surface between the Pair of Credit Latent States
	 */

	public org.drip.quant.function1D.AbstractUnivariate creditCreditCorrSurface (
		final org.drip.state.identifier.CreditLabel creditLabel1,
		final org.drip.state.identifier.CreditLabel creditLabel2)
	{
		if (null == creditLabel1 || null == creditLabel2) return null;

		java.lang.String strCode12 = creditLabel1.fullyQualifiedName() + "@#" +
			creditLabel2.fullyQualifiedName();

		if (_mapCreditCreditCorrelationSurface.containsKey (strCode12))
			return _mapCreditCreditCorrelationSurface.get (strCode12);

		java.lang.String strCode21 = creditLabel2.fullyQualifiedName() + "@#" +
			creditLabel1.fullyQualifiedName();

		return !_mapCreditCreditCorrelationSurface.containsKey (strCode21) ? null :
			_mapCreditCreditCorrelationSurface.get (strCode21);
	}

	/**
	 * (Re)-set the Correlation Surface between the Pair of Credit Latent States
	 * 
	 * @param creditLabel1 The Credit Curve Latent State Label #1
	 * @param creditLabel2 The Credit Curve Latent State Label #2
	 * @param auCorrelation The Correlation Surface
	 * 
	 * @return TRUE => Successfully set
	 */

	public boolean setCreditCreditCorrSurface (
		final org.drip.state.identifier.CreditLabel creditLabel1,
		final org.drip.state.identifier.CreditLabel creditLabel2,
		final org.drip.quant.function1D.AbstractUnivariate auCorrelation)
	{
		if (null == creditLabel1 || null == creditLabel2 || null == auCorrelation) return false;

		java.lang.String strCreditLabel1 = creditLabel1.fullyQualifiedName();

		java.lang.String strCreditLabel2 = creditLabel2.fullyQualifiedName();

		_mapCreditCreditCorrelationSurface.put (strCreditLabel1 + "@#" + strCreditLabel2, auCorrelation);

		_mapCreditCreditCorrelationSurface.put (strCreditLabel2 + "@#" + strCreditLabel1, auCorrelation);

		return true;
	}

	/**
	 * Retrieve the Correlation Surface between the Custom Metric Latent State Pair
	 * 
	 * @param customMetricLabel1 The Custom Metric Latent State Label #1
	 * @param customMetricLabel2 The Custom Metric Latent State Label #2
	 * 
	 * @return The Correlation Surface between the Custom Metric Latent State Pair
	 */

	public org.drip.quant.function1D.AbstractUnivariate customMetricCustomMetricCorrSurface (
		final org.drip.state.identifier.CustomMetricLabel customMetricLabel1,
		final org.drip.state.identifier.CustomMetricLabel customMetricLabel2)
	{
		if (null == customMetricLabel1 || null == customMetricLabel2) return null;

		java.lang.String strCode12 = customMetricLabel1.fullyQualifiedName() + "@#" +
			customMetricLabel2.fullyQualifiedName();

		if (_mapCustomMetricCustomMetricCorrelationSurface.containsKey (strCode12))
			return _mapCustomMetricCustomMetricCorrelationSurface.get (strCode12);

		java.lang.String strCode21 = customMetricLabel2.fullyQualifiedName() + "@#" +
			customMetricLabel1.fullyQualifiedName();

		return _mapCustomMetricCustomMetricCorrelationSurface.containsKey (strCode21) ?
			_mapCustomMetricCustomMetricCorrelationSurface.get (strCode21) : null;
	}

	/**
	 * (Re)-set the Correlation Surface between the Custom Metric Latent State Pair
	 * 
	 * @param customMetricLabel1 The Custom Metric Latent State Label #1
	 * @param customMetricLabel2 The Custom Metric Latent State Label #2
	 * @param auCorrelation The Correlation Surface
	 * 
	 * @return TRUE => Successfully set
	 */

	public boolean setCustomMetricCustomMetricCorrSurface (
		final org.drip.state.identifier.CustomMetricLabel customMetricLabel1,
		final org.drip.state.identifier.CustomMetricLabel customMetricLabel2,
		final org.drip.quant.function1D.AbstractUnivariate auCorrelation)
	{
		if (null == customMetricLabel1 || null == customMetricLabel2 || customMetricLabel1.match
			(customMetricLabel2) || null == auCorrelation)
			return false;

		_mapCustomMetricCustomMetricCorrelationSurface.put (customMetricLabel1.fullyQualifiedName() + "@#" +
			customMetricLabel2.fullyQualifiedName(), auCorrelation);

		_mapCustomMetricCustomMetricCorrelationSurface.put (customMetricLabel2.fullyQualifiedName() + "@#" +
			customMetricLabel1.fullyQualifiedName(), auCorrelation);

		return true;
	}

	/**
	 * Retrieve the Correlation Surface between the Pair of Forward Latent States
	 * 
	 * @param forwardLabel1 Forward Curve Latent State Label #1
	 * @param forwardLabel2 Forward Curve Latent State Label #2
	 * 
	 * @return The Correlation Surface between the Pair of Forward Latent States
	 */

	public org.drip.quant.function1D.AbstractUnivariate forwardForwardCorrSurface (
		final org.drip.state.identifier.ForwardLabel forwardLabel1,
		final org.drip.state.identifier.ForwardLabel forwardLabel2)
	{
		if (null == forwardLabel1 || null == forwardLabel2) return null;

		java.lang.String strCode = forwardLabel1.fullyQualifiedName() + "@#" +
			forwardLabel2.fullyQualifiedName();

		return _mapForwardForwardCorrelationSurface.containsKey (strCode) ?
			_mapForwardForwardCorrelationSurface.get (strCode) : null;
	}

	/**
	 * (Re)-set the Correlation Surface between the Pair of Forward Latent States
	 * 
	 * @param forwardLabel1 Forward Curve Latent State Label #1
	 * @param forwardLabel2 Forward Curve Latent State Label #2
	 * @param auCorrelation The Correlation Surface
	 * 
	 * @return TRUE => Successfully set
	 */

	public boolean setForwardForwardCorrSurface (
		final org.drip.state.identifier.ForwardLabel forwardLabel1,
		final org.drip.state.identifier.ForwardLabel forwardLabel2,
		final org.drip.quant.function1D.AbstractUnivariate auCorrelation)
	{
		if (null == forwardLabel1 || null == forwardLabel2 || forwardLabel1.match (forwardLabel2) || null ==
			auCorrelation)
			return false;

		java.lang.String strForwardLabel1 = forwardLabel1.fullyQualifiedName();

		java.lang.String strForwardLabel2 = forwardLabel2.fullyQualifiedName();

		_mapForwardForwardCorrelationSurface.put (strForwardLabel1 + "@#" + strForwardLabel2, auCorrelation);

		_mapForwardForwardCorrelationSurface.put (strForwardLabel2 + "@#" + strForwardLabel1, auCorrelation);

		return true;
	}

	/**
	 * Retrieve the Correlation Surface between the Pair of Funding Latent States
	 * 
	 * @param fundingLabel1 Funding Latent State Label #1
	 * @param fundingLabel2 Funding Latent State Label #2
	 * 
	 * @return The Correlation Surface between the Pair of Funding Latent States
	 */

	public org.drip.quant.function1D.AbstractUnivariate fundingFundingCorrSurface (
		final org.drip.state.identifier.FundingLabel fundingLabel1,
		final org.drip.state.identifier.FundingLabel fundingLabel2)
	{
		if (null == fundingLabel1 || null == fundingLabel2 || fundingLabel1.match (fundingLabel2))
			return null;

		java.lang.String strCode = fundingLabel1.fullyQualifiedName() + "@#" +
			fundingLabel2.fullyQualifiedName();

		return _mapFundingFundingCorrelationSurface.containsKey (strCode) ?
			_mapFundingFundingCorrelationSurface.get (strCode) : null;
	}

	/**
	 * (Re)-set the Correlation Surface between the Pair of Funding Latent States
	 * 
	 * @param fundingLabel1 Funding Latent State Label #1
	 * @param fundingLabel2 Funding Latent State Label #2
	 * @param auCorrelation The Correlation Surface
	 * 
	 * @return TRUE => Successfully set
	 */

	public boolean setFundingFundingCorrSurface (
		final org.drip.state.identifier.FundingLabel fundingLabel1,
		final org.drip.state.identifier.FundingLabel fundingLabel2,
		final org.drip.quant.function1D.AbstractUnivariate auCorrelation)
	{
		if (null == fundingLabel1 || null == fundingLabel2 || fundingLabel1.match (fundingLabel2) || null ==
			auCorrelation)
			return false;

		java.lang.String strFundingLabel1 = fundingLabel1.fullyQualifiedName();

		java.lang.String strFundingLabel2 = fundingLabel2.fullyQualifiedName();

		_mapFundingFundingCorrelationSurface.put (strFundingLabel1 + "@#" + strFundingLabel2, auCorrelation);

		_mapFundingFundingCorrelationSurface.put (strFundingLabel2 + "@#" + strFundingLabel1, auCorrelation);

		return true;
	}

	/**
	 * Retrieve the Correlation Surface for the specified FX Latent State Label Set
	 * 
	 * @param fxLabel1 The FX Latent State Label #1
	 * @param fxLabel2 The FX Latent State Label #2
	 * 
	 * @return The Correlation Surface for the specified FX Latent State Label Set
	 */

	public org.drip.quant.function1D.AbstractUnivariate fxFXCorrSurface (
		final org.drip.state.identifier.FXLabel fxLabel1,
		final org.drip.state.identifier.FXLabel fxLabel2)
	{
		if (null == fxLabel1 || null == fxLabel2 || fxLabel1.match (fxLabel2)) return null;

		java.lang.String strCode = fxLabel1.fullyQualifiedName() + "@#" + fxLabel2.fullyQualifiedName();

		return !_mapFXFXCorrelationSurface.containsKey (strCode) ? null : _mapFXFXCorrelationSurface.get
			(strCode);
	}

	/**
	 * (Re)-set the Correlation Surface for the specified FX Latent State Label Set
	 * 
	 * @param fxLabel1 The FX Latent State Label #1
	 * @param fxLabel2 The FX Latent State Label #2
	 * @param auCorrelation The Correlation Surface
	 * 
	 * @return TRUE => Successfully set
	 */

	public boolean setFXFXCorrSurface (
		final org.drip.state.identifier.FXLabel fxLabel1,
		final org.drip.state.identifier.FXLabel fxLabel2,
		final org.drip.quant.function1D.AbstractUnivariate auCorrelation)
	{
		if (null == fxLabel1 || null == fxLabel2 || fxLabel1.match (fxLabel2) || null == auCorrelation)
			return false;

		java.lang.String strCode1 = fxLabel1.fullyQualifiedName();

		java.lang.String strCode2 = fxLabel2.fullyQualifiedName();

		_mapFXFXCorrelationSurface.put (strCode1 + "@#" + strCode2, auCorrelation);

		_mapFXFXCorrelationSurface.put (strCode2 + "@#" + strCode1, auCorrelation);

		return true;
	}

	/**
	 * Retrieve the Correlation Surface for the specified Govvie Latent State Pair
	 * 
	 * @param govvieLabel1 The Govvie Curve Latent State Label #1
	 * @param govvieLabel2 The Govvie Curve Latent State Label #2
	 * 
	 * @return The Correlation Surface for the specified Govvie Latent State Pair
	 */

	public org.drip.quant.function1D.AbstractUnivariate govvieGovvieCorrSurface (
		final org.drip.state.identifier.GovvieLabel govvieLabel1,
		final org.drip.state.identifier.GovvieLabel govvieLabel2)
	{
		if (null == govvieLabel1 || null == govvieLabel2 || govvieLabel1.match (govvieLabel2)) return null;

		java.lang.String strCode12 = govvieLabel1.fullyQualifiedName() + "@#" +
			govvieLabel2.fullyQualifiedName();

		if (_mapGovvieGovvieCorrelationSurface.containsKey (strCode12))
			return _mapGovvieGovvieCorrelationSurface.get (strCode12);

		java.lang.String strCode21 = govvieLabel2.fullyQualifiedName() + "@#" +
			govvieLabel1.fullyQualifiedName();

		return _mapGovvieGovvieCorrelationSurface.containsKey (strCode21) ?
			_mapGovvieGovvieCorrelationSurface.get (strCode21) : null;
	}

	/**
	 * (Re)-set the Correlation Surface for the Govvie Latent State Pair
	 * 
	 * @param govvieLabel1 The Govvie Curve Latent State Label #1
	 * @param govvieLabel2 The Govvie Curve Latent State Label #2
	 * @param auCorrelation The Correlation Surface
	 * 
	 * @return TRUE => Successfully set
	 */

	public boolean setGovvieGovvieCorrSurface (
		final org.drip.state.identifier.GovvieLabel govvieLabel1,
		final org.drip.state.identifier.GovvieLabel govvieLabel2,
		final org.drip.quant.function1D.AbstractUnivariate auCorrelation)
	{
		if (null == govvieLabel1 || null == govvieLabel2 || govvieLabel1.match (govvieLabel2) || null ==
			auCorrelation)
			return false;

		java.lang.String strGovvieLabel1 = govvieLabel1.fullyQualifiedName();

		java.lang.String strGovvieLabel2 = govvieLabel2.fullyQualifiedName();

		_mapGovvieGovvieCorrelationSurface.put (strGovvieLabel1 + "@#" + strGovvieLabel2, auCorrelation);

		_mapGovvieGovvieCorrelationSurface.put (strGovvieLabel2 + "@#" + strGovvieLabel1, auCorrelation);

		return true;
	}

	/**
	 * Retrieve the Correlation Surface between the Collateral and the Credit Latent States
	 * 
	 * @param strCollateralCurrency The Collateral Currency
	 * @param creditLabel The Credit Curve Latent State Label
	 * 
	 * @return The Correlation Surface between the Collateral and the Credit Latent States
	 */

	public org.drip.quant.function1D.AbstractUnivariate collateralCreditCorrSurface (
		final java.lang.String strCollateralCurrency,
		final org.drip.state.identifier.CreditLabel creditLabel)
	{
		if (null == strCollateralCurrency || strCollateralCurrency.isEmpty() || null == creditLabel)
			return null;

		java.lang.String strCode = strCollateralCurrency + "@#" + creditLabel.fullyQualifiedName();

		return _mapCollateralCreditCorrelationSurface.containsKey (strCode) ? null :
			_mapCollateralCreditCorrelationSurface.get (strCode);
	}

	/**
	 * (Re)-set the Correlation Surface between the Collateral and the Credit Latent States
	 * 
	 * @param strCollateralCurrency The Collateral Currency
	 * @param creditLabel The Credit Curve Latent State Label
	 * @param auCorrelation The Correlation Surface
	 * 
	 * @return TRUE => Successfully set
	 */

	public boolean setCollateralCreditCorrSurface (
		final java.lang.String strCollateralCurrency,
		final org.drip.state.identifier.CreditLabel creditLabel,
		final org.drip.quant.function1D.AbstractUnivariate auCorrelation)
	{
		if (null == strCollateralCurrency || strCollateralCurrency.isEmpty() || null == creditLabel)
			return false;

		_mapCollateralCreditCorrelationSurface.put (strCollateralCurrency + "@#" +
			creditLabel.fullyQualifiedName(), auCorrelation);

		return true;
	}

	/**
	 * Retrieve the Correlation Surface between the Collateral and the Custom Metric Latent States
	 * 
	 * @param strCollateralCurrency The Collateral Currency
	 * @param customMetricLabel The Custom Metric Latent State Label
	 * 
	 * @return The Correlation Surface between the Collateral and the Custom Metric Latent States
	 */

	public org.drip.quant.function1D.AbstractUnivariate collateralCustomMetricCorrSurface (
		final java.lang.String strCollateralCurrency,
		final org.drip.state.identifier.CustomMetricLabel customMetricLabel)
	{
		if (null == strCollateralCurrency || strCollateralCurrency.isEmpty() || null == customMetricLabel)
			return null;

		java.lang.String strCode = strCollateralCurrency + "@#" + customMetricLabel.fullyQualifiedName();

		return _mapCollateralCustomMetricCorrelationSurface.containsKey (strCode) ?
			_mapCollateralCustomMetricCorrelationSurface.get (strCode) : null;
	}

	/**
	 * (Re)-set the Correlation Surface between the Collateral and the Custom Metric Latent States
	 * 
	 * @param strCollateralCurrency The Collateral Currency
	 * @param customMetricLabel The Custom Metric Latent State Label
	 * @param auCorrelation The Correlation Surface
	 * 
	 * @return TRUE => Successfully set
	 */

	public boolean setCollateralCustomMetricCorrSurface (
		final java.lang.String strCollateralCurrency,
		final org.drip.state.identifier.CustomMetricLabel customMetricLabel,
		final org.drip.quant.function1D.AbstractUnivariate auCorrelation)
	{
		if (null == strCollateralCurrency || strCollateralCurrency.isEmpty() || null == customMetricLabel)
			return false;

		_mapCollateralCustomMetricCorrelationSurface.put (strCollateralCurrency + "@#" +
			customMetricLabel.fullyQualifiedName(), auCorrelation);

		return true;
	}

	/**
	 * Retrieve the Correlation Surface between the Collateral and the Forward Latent States
	 * 
	 * @param strCollateralCurrency The Collateral Currency
	 * @param forwardLabel The Forward Latent State Label
	 * 
	 * @return The Correlation Surface between the Collateral and the Forward Latent States
	 */

	public org.drip.quant.function1D.AbstractUnivariate collateralForwardCorrSurface (
		final java.lang.String strCollateralCurrency,
		final org.drip.state.identifier.ForwardLabel forwardLabel)
	{
		if (null == strCollateralCurrency || strCollateralCurrency.isEmpty() || null == forwardLabel)
			return null;

		java.lang.String strCode = strCollateralCurrency + "@#" + forwardLabel.fullyQualifiedName();

		return _mapCollateralForwardCorrelationSurface.containsKey (strCode) ?
			_mapCollateralForwardCorrelationSurface.get (strCode) : null;
	}

	/**
	 * (Re)-set the Correlation Surface between the Collateral and the Forward Latent States
	 * 
	 * @param strCollateralCurrency The Collateral Currency
	 * @param forwardLabel The Forward Latent State Label
	 * @param auCorrelation The Correlation Surface
	 * 
	 * @return TRUE => Successfully set
	 */

	public boolean setCollateralForwardCorrSurface (
		final java.lang.String strCollateralCurrency,
		final org.drip.state.identifier.ForwardLabel forwardLabel,
		final org.drip.quant.function1D.AbstractUnivariate auCorrelation)
	{
		if (null == strCollateralCurrency || strCollateralCurrency.isEmpty() || null == forwardLabel || null
			== auCorrelation)
			return false;

		_mapCollateralForwardCorrelationSurface.put (strCollateralCurrency + "@#" +
			forwardLabel.fullyQualifiedName(), auCorrelation);

		return true;
	}

	/**
	 * Retrieve the Correlation Surface between the Collateral and the Funding Latent States
	 * 
	 * @param strCollateralCurrency The Collateral Currency
	 * @param fundingLabel The Funding Latent State Label
	 * 
	 * @return The Correlation Surface between the Collateral and the Funding Latent States
	 */

	public org.drip.quant.function1D.AbstractUnivariate collateralFundingCorrSurface (
		final java.lang.String strCollateralCurrency,
		final org.drip.state.identifier.FundingLabel fundingLabel)
	{
		if (null == strCollateralCurrency || strCollateralCurrency.isEmpty() || null == fundingLabel)
			return null;

		java.lang.String strCode = strCollateralCurrency + "@#" + fundingLabel.fullyQualifiedName();

		return _mapCollateralFundingCorrelationSurface.containsKey (strCode) ?
			_mapCollateralFundingCorrelationSurface.get (strCode) : null;
	}

	/**
	 * (Re)-set the Correlation Surface between the Collateral and the Funding Latent States
	 * 
	 * @param strCollateralCurrency The Collateral Currency
	 * @param fundingLabel The Funding Latent State Label
	 * @param auCorrelation The Correlation Surface
	 * 
	 * @return TRUE => Successfully set
	 */

	public boolean setCollateralFundingCorrSurface (
		final java.lang.String strCollateralCurrency,
		final org.drip.state.identifier.FundingLabel fundingLabel,
		final org.drip.quant.function1D.AbstractUnivariate auCorrelation)
	{
		if (null == strCollateralCurrency || strCollateralCurrency.isEmpty() || null == fundingLabel || null
			== auCorrelation)
			return false;

		_mapCollateralFundingCorrelationSurface.put (strCollateralCurrency + "@#" +
			fundingLabel.fullyQualifiedName(), auCorrelation);

		return true;
	}

	/**
	 * Retrieve the Correlation Surface for the specified Collateral and the FX Latent State Label
	 * 
	 * @param strCollateralCurrency The Collateral Currency
	 * @param fxLabel The FX Latent State Label
	 * 
	 * @return The Correlation Surface for the specified Collateral and the FX Latent State Label
	 */

	public org.drip.quant.function1D.AbstractUnivariate collateralFXCorrSurface (
		final java.lang.String strCollateralCurrency,
		final org.drip.state.identifier.FXLabel fxLabel)
	{
		if (null == strCollateralCurrency || strCollateralCurrency.isEmpty() || null == fxLabel) return null;

		java.lang.String strCode = strCollateralCurrency + "@#" + fxLabel.fullyQualifiedName();

		return _mapCollateralFXCorrelationSurface.containsKey (strCode) ?
			_mapCollateralFXCorrelationSurface.get (strCode) : null;
	}

	/**
	 * (Re)-set the Correlation Surface for the specified Collateral and FX Latent States
	 * 
	 * @param strCollateralCurrency The Collateral Currency
	 * @param fxLabel The FX Latent State Label
	 * @param auCorrelation The Correlation Surface
	 * 
	 * @return TRUE => Successfully set
	 */

	public boolean setCollateralFXCorrSurface (
		final java.lang.String strCollateralCurrency,
		final org.drip.state.identifier.FXLabel fxLabel,
		final org.drip.quant.function1D.AbstractUnivariate auCorrelation)
	{
		if (null == strCollateralCurrency || strCollateralCurrency.isEmpty() || null == fxLabel || null ==
			auCorrelation)
			return false;

		_mapCollateralFXCorrelationSurface.put (strCollateralCurrency + "@#" + fxLabel.fullyQualifiedName(),
			auCorrelation);

		return true;
	}

	/**
	 * Retrieve the Correlation Surface for the specified Collateral and Govvie Latent State Labels
	 * 
	 * @param strCollateralCurrency The Collateral Currency
	 * @param govvieLabel The Govvie Latent State Label
	 * 
	 * @return The Correlation Surface for the specified Collateral and Govvie Latent State Labels
	 */

	public org.drip.quant.function1D.AbstractUnivariate collateralGovvieCorrSurface (
		final java.lang.String strCollateralCurrency,
		final org.drip.state.identifier.GovvieLabel govvieLabel)
	{
		if (null == strCollateralCurrency || strCollateralCurrency.isEmpty() || null == govvieLabel)
			return null;

		java.lang.String strCode = strCollateralCurrency + "@#" + govvieLabel.fullyQualifiedName();

		return _mapCollateralGovvieCorrelationSurface.containsKey (strCode) ?
			_mapCollateralGovvieCorrelationSurface.get (strCode) : null;
	}

	/**
	 * (Re)-set the Correlation Surface for the specified Collateral and Govvie Latent State Labels
	 * 
	 * @param strCollateralCurrency The Collateral Currency
	 * @param govvieLabel The Govvie Latent State Label
	 * @param auCorrelation The Correlation Surface
	 * 
	 * @return TRUE => Successfully set
	 */

	public boolean setCollateralGovvieCorrSurface (
		final java.lang.String strCollateralCurrency,
		final org.drip.state.identifier.GovvieLabel govvieLabel,
		final org.drip.quant.function1D.AbstractUnivariate auCorrelation)
	{
		if (null == strCollateralCurrency || strCollateralCurrency.isEmpty() || null == govvieLabel || null
			== auCorrelation)
			return false;

		_mapCollateralGovvieCorrelationSurface.put (strCollateralCurrency + "@#" +
			govvieLabel.fullyQualifiedName(), auCorrelation);

		return true;
	}

	/**
	 * Retrieve the Correlation Surface between the Credit and the Custom Metric Latent States
	 * 
	 * @param creditLabel The Credit Latent State Label
	 * @param customMetricLabel The Custom Metric Latent State Label
	 * 
	 * @return The Correlation Surface between the Credit and the Custom Metric Latent States
	 */

	public org.drip.quant.function1D.AbstractUnivariate creditCustomMetricCorrSurface (
		final org.drip.state.identifier.CreditLabel creditLabel,
		final org.drip.state.identifier.CustomMetricLabel customMetricLabel)
	{
		if (null == creditLabel || null == customMetricLabel) return null;

		java.lang.String strCode = creditLabel.fullyQualifiedName() + "@#" +
			customMetricLabel.fullyQualifiedName();

		return _mapCreditCustomMetricCorrelationSurface.containsKey (strCode) ?
			_mapCreditCustomMetricCorrelationSurface.get (strCode) : null;
	}

	/**
	 * (Re)-set the Correlation Surface between the Credit and the Custom Metric Latent States
	 * 
	 * @param creditLabel The Credit Latent State Label
	 * @param customMetricLabel The Custom Metric Latent State Label
	 * @param auCorrelation The Correlation Surface
	 * 
	 * @return TRUE => Successfully set
	 */

	public boolean setCreditCustomMetricCorrSurface (
		final org.drip.state.identifier.CreditLabel creditLabel,
		final org.drip.state.identifier.CustomMetricLabel customMetricLabel,
		final org.drip.quant.function1D.AbstractUnivariate auCorrelation)
	{
		if (null == creditLabel || null == customMetricLabel || null == auCorrelation) return false;

		_mapCreditCustomMetricCorrelationSurface.put (creditLabel.fullyQualifiedName() + "@#" +
			customMetricLabel.fullyQualifiedName(), auCorrelation);

		return true;
	}

	/**
	 * Retrieve the Correlation Surface between the Credit and the Forward Latent States
	 * 
	 * @param creditLabel The Credit Curve Label
	 * @param forwardLabel The Forward Latent State Label
	 * 
	 * @return The Correlation Surface between the Credit and the Forward Lsatent States
	 */

	public org.drip.quant.function1D.AbstractUnivariate creditForwardCorrSurface (
		final org.drip.state.identifier.CreditLabel creditLabel,
		final org.drip.state.identifier.ForwardLabel forwardLabel)
	{
		if (null == creditLabel || null == forwardLabel) return null;

		java.lang.String strCode = creditLabel.fullyQualifiedName() + "@#" +
			forwardLabel.fullyQualifiedName();

		return _mapCreditForwardCorrelationSurface.containsKey (strCode) ?
			_mapCreditForwardCorrelationSurface.get (strCode) : null;
	}

	/**
	 * (Re)-set the Correlation Surface between the Credit and the Forward Latent States
	 * 
	 * @param creditLabel The Credit Curve Label
	 * @param forwardLabel The Forward Latent State Label
	 * @param auCorrelation The Correlation Surface
	 * 
	 * @return TRUE => Successfully set
	 */

	public boolean setCreditForwardCorrSurface (
		final org.drip.state.identifier.CreditLabel creditLabel,
		final org.drip.state.identifier.ForwardLabel forwardLabel,
		final org.drip.quant.function1D.AbstractUnivariate auCorrelation)
	{
		if (null == creditLabel || null == forwardLabel || null == auCorrelation) return false;

		_mapCreditForwardCorrelationSurface.put (creditLabel.fullyQualifiedName() + "@#" +
			forwardLabel.fullyQualifiedName(), auCorrelation);

		return true;
	}

	/**
	 * Retrieve the Correlation Surface between the Credit and the Funding Latent States
	 * 
	 * @param creditLabel The Credit Curve Latent State Label
	 * @param fundingLabel The Funding Latent State Label
	 * 
	 * @return The Correlation Surface between the Credit and the Funding Latent States
	 */

	public org.drip.quant.function1D.AbstractUnivariate creditFundingCorrSurface (
		final org.drip.state.identifier.CreditLabel creditLabel,
		final org.drip.state.identifier.FundingLabel fundingLabel)
	{
		if (null == creditLabel || null == fundingLabel) return null;

		java.lang.String strCode = creditLabel.fullyQualifiedName() + "@#" +
			fundingLabel.fullyQualifiedName();

		return _mapCreditFundingCorrelationSurface.containsKey (strCode) ?
			_mapCreditFundingCorrelationSurface.get (strCode) : null;
	}

	/**
	 * (Re)-set the Correlation Surface between the Credit and the Funding Latent States
	 * 
	 * @param creditLabel The Credit Curve Label
	 * @param fundingLabel The Funding Latent State Label
	 * @param auCorrelation The Correlation Surface
	 * 
	 * @return TRUE => Successfully set
	 */

	public boolean setCreditFundingCorrSurface (
		final org.drip.state.identifier.CreditLabel creditLabel,
		final org.drip.state.identifier.FundingLabel fundingLabel,
		final org.drip.quant.function1D.AbstractUnivariate auCorrelation)
	{
		if (null == creditLabel || null == fundingLabel || null == auCorrelation) return false;

		_mapCreditFundingCorrelationSurface.put (creditLabel.fullyQualifiedName() + "@#" +
			fundingLabel.fullyQualifiedName(), auCorrelation);

		return true;
	}

	/**
	 * Retrieve the Correlation Surface between the Credit and the FX Latent State Labels
	 * 
	 * @param creditLabel The Credit Curve Label
	 * @param fxLabel The FX Latent State Label
	 * 
	 * @return The Correlation Surface between the Credit and the FX Latent State Labels
	 */

	public org.drip.quant.function1D.AbstractUnivariate creditFXCorrSurface (
		final org.drip.state.identifier.CreditLabel creditLabel,
		final org.drip.state.identifier.FXLabel fxLabel)
	{
		if (null == creditLabel || null == fxLabel) return null;

		java.lang.String strCode = creditLabel.fullyQualifiedName() + "@#" + fxLabel.fullyQualifiedName();

		return _mapCreditFXCorrelationSurface.containsKey (strCode) ? _mapCreditFXCorrelationSurface.get
			(strCode) : null;
	}

	/**
	 * (Re)-set the Correlation Surface between the Credit and the FX Latent States
	 * 
	 * @param creditLabel The Credit Curve Label
	 * @param fxLabel The FX Latent State Label
	 * @param auCorrelation The Correlation Surface
	 * 
	 * @return TRUE => Successfully set
	 */

	public boolean setCreditFXCorrSurface (
		final org.drip.state.identifier.CreditLabel creditLabel,
		final org.drip.state.identifier.FXLabel fxLabel,
		final org.drip.quant.function1D.AbstractUnivariate auCorrelation)
	{
		if (null == creditLabel || null == fxLabel || null == auCorrelation) return false;

		_mapCreditFXCorrelationSurface.get (creditLabel.fullyQualifiedName() + "@#" +
			fxLabel.fullyQualifiedName());

		return true;
	}

	/**
	 * Retrieve the Correlation Surface between the Credit and the Govvie Latent State Labels
	 * 
	 * @param creditLabel The Credit Curve Label
	 * @param govvieLabel The Govvie Latent State Label
	 * 
	 * @return The Correlation Surface between the Credit and the Govvie Latent State Labels
	 */

	public org.drip.quant.function1D.AbstractUnivariate creditGovvieCorrSurface (
		final org.drip.state.identifier.CreditLabel creditLabel,
		final org.drip.state.identifier.GovvieLabel govvieLabel)
	{
		if (null == creditLabel || null == govvieLabel) return null;

		java.lang.String strCode = creditLabel.fullyQualifiedName() + "@#" +
			govvieLabel.fullyQualifiedName();

		return _mapCreditGovvieCorrelationSurface.containsKey (strCode) ?
			_mapCreditGovvieCorrelationSurface.get (strCode) : null;
	}

	/**
	 * (Re)-set the Correlation Surface between the Credit and the Govvie Latent States
	 * 
	 * @param creditLabel The Credit Curve Latent State Label
	 * @param govvieLabel The Govvie Latent State Label
	 * @param auCorrelation The Correlation Surface
	 * 
	 * @return TRUE => Successfully set
	 */

	public boolean setCreditGovvieCorrSurface (
		final org.drip.state.identifier.CreditLabel creditLabel,
		final org.drip.state.identifier.GovvieLabel govvieLabel,
		final org.drip.quant.function1D.AbstractUnivariate auCorrelation)
	{
		if (null == creditLabel || null == govvieLabel || null == auCorrelation) return false;

		_mapCreditGovvieCorrelationSurface.put (creditLabel.fullyQualifiedName() + "@#" +
			govvieLabel.fullyQualifiedName(), auCorrelation);

		return true;
	}

	/**
	 * Retrieve the Correlation Surface between the Custom Metric and the Forward Latent States
	 * 
	 * @param customMetricLabel The Custom Metric Latent State Label
	 * @param forwardLabel The Forward Latent State Label
	 * 
	 * @return The Correlation Surface between the Custom Metric and the Forward Latent States
	 */

	public org.drip.quant.function1D.AbstractUnivariate customMetricForwardCorrSurface (
		final org.drip.state.identifier.CustomMetricLabel customMetricLabel,
		final org.drip.state.identifier.ForwardLabel forwardLabel)
	{
		if (null == customMetricLabel || null == forwardLabel) return null;

		java.lang.String strCode = customMetricLabel.fullyQualifiedName() + "@#" +
			forwardLabel.fullyQualifiedName();

		return _mapCustomMetricForwardCorrelationSurface.containsKey (strCode) ?
			_mapCustomMetricForwardCorrelationSurface.get (strCode) : null;
	}

	/**
	 * (Re)-set the Correlation Surface between the Custom Metric and the Forward Latent States
	 * 
	 * @param customMetricLabel The Custom Metric Label
	 * @param forwardLabel The Forward Latent State Label
	 * @param auCorrelation The Correlation Surface
	 * 
	 * @return TRUE => Successfully set
	 */

	public boolean setCustomMetricForwardCorrSurface (
		final org.drip.state.identifier.CustomMetricLabel customMetricLabel,
		final org.drip.state.identifier.ForwardLabel forwardLabel,
		final org.drip.quant.function1D.AbstractUnivariate auCorrelation)
	{
		if (null == customMetricLabel || null == forwardLabel || null == auCorrelation) return false;

		_mapCustomMetricForwardCorrelationSurface.put (customMetricLabel.fullyQualifiedName() + "@#" +
			forwardLabel.fullyQualifiedName(), auCorrelation);

		return true;
	}

	/**
	 * Retrieve the Correlation Surface between Custom Metric and the Funding Latent States
	 * 
	 * @param customMetricLabel The Custom Metric Latent State Label
	 * @param fundingLabel The Funding Latent State Label
	 * 
	 * @return The Correlation Surface between the Custom Metric and the Funding Latent States
	 */

	public org.drip.quant.function1D.AbstractUnivariate customMetricFundingCorrSurface (
		final org.drip.state.identifier.CustomMetricLabel customMetricLabel,
		final org.drip.state.identifier.FundingLabel fundingLabel)
	{
		if (null == customMetricLabel || null == fundingLabel) return null;

		java.lang.String strCode = customMetricLabel.fullyQualifiedName() + "@#" +
			fundingLabel.fullyQualifiedName();

		return _mapCustomMetricFundingCorrelationSurface.containsKey (strCode) ?
			_mapCustomMetricFundingCorrelationSurface.get (strCode) : null;
	}

	/**
	 * (Re)-set the Correlation Surface between the Custom Metric and the Funding Latent States
	 * 
	 * @param customMetricLabel The Custom Metric Latent State Label
	 * @param fundingLabel The Funding Latent State Label
	 * @param auCorrelation The Correlation Surface
	 * 
	 * @return TRUE => Successfully set
	 */

	public boolean setCustomMetricFundingCorrSurface (
		final org.drip.state.identifier.CustomMetricLabel customMetricLabel,
		final org.drip.state.identifier.FundingLabel fundingLabel,
		final org.drip.quant.function1D.AbstractUnivariate auCorrelation)
	{
		if (null == customMetricLabel || null == fundingLabel) return false;

		_mapCustomMetricFundingCorrelationSurface.put (customMetricLabel.fullyQualifiedName() + "@#" +
			fundingLabel.fullyQualifiedName(), auCorrelation);

		return true;
	}

	/**
	 * Retrieve the Correlation Surface between the Custom Metric and the FX Latent States
	 * 
	 * @param customMetricLabel The Custom Metric Latent State Label
	 * @param fxLabel The FX Latent State Label
	 * 
	 * @return The Correlation Surface between the Custom Metric and the FX Latent States
	 */

	public org.drip.quant.function1D.AbstractUnivariate customMetricFXCorrSurface (
		final org.drip.state.identifier.CustomMetricLabel customMetricLabel,
		final org.drip.state.identifier.FXLabel fxLabel)
	{
		if (null == customMetricLabel || null == fxLabel) return null;

		java.lang.String strCode = customMetricLabel.fullyQualifiedName() + "@#" +
			fxLabel.fullyQualifiedName();

		return _mapCustomMetricFXCorrelationSurface.containsKey (strCode) ?
			_mapCustomMetricFXCorrelationSurface.get (strCode) : null;
	}

	/**
	 * (Re)-set the Correlation Surface between the Custom Metric and the FX Latent States
	 * 
	 * @param customMetricLabel The Custom Metric Latent State Label
	 * @param fxLabel The FX Latent State Label
	 * @param auCorrelation The Correlation Surface
	 * 
	 * @return TRUE => Successfully set
	 */

	public boolean setCustomMetricFXCorrSurface (
		final org.drip.state.identifier.CustomMetricLabel customMetricLabel,
		final org.drip.state.identifier.FXLabel fxLabel,
		final org.drip.quant.function1D.AbstractUnivariate auCorrelation)
	{
		if (null == customMetricLabel || null == fxLabel || null == auCorrelation) return false;

		_mapCustomMetricFXCorrelationSurface.get (customMetricLabel.fullyQualifiedName() + "@#" +
			fxLabel.fullyQualifiedName());

		return true;
	}

	/**
	 * Retrieve the Correlation Surface between the Custom Metric and the Govvie Latent States
	 * 
	 * @param customMetricLabel The Custom Metric Latent State Label
	 * @param govvieLabel The Govvie Latent State Label
	 * 
	 * @return The Correlation Surface between the Custom Metric and the Govvie Latent States
	 */

	public org.drip.quant.function1D.AbstractUnivariate customMetricGovvieCorrSurface (
		final org.drip.state.identifier.CustomMetricLabel customMetricLabel,
		final org.drip.state.identifier.GovvieLabel govvieLabel)
	{
		if (null == customMetricLabel || null == govvieLabel) return null;

		java.lang.String strCode = customMetricLabel.fullyQualifiedName() + "@#" +
			govvieLabel.fullyQualifiedName();

		return _mapCustomMetricGovvieCorrelationSurface.containsKey (strCode) ?
			_mapCustomMetricGovvieCorrelationSurface.get (strCode) : null;
	}

	/**
	 * (Re)-set the Correlation Surface between the Custom Metric and the Govvie Latent States
	 * 
	 * @param customMetricLabel The Custom Metric Latent State Label
	 * @param govvieLabel The Govvie Latent State Label
	 * @param auCorrelation The Correlation Surface
	 * 
	 * @return TRUE => Successfully set
	 */

	public boolean setCustomMetricGovvieCorrSurface (
		final org.drip.state.identifier.CustomMetricLabel customMetricLabel,
		final org.drip.state.identifier.GovvieLabel govvieLabel,
		final org.drip.quant.function1D.AbstractUnivariate auCorrelation)
	{
		if (null == customMetricLabel || null == govvieLabel) return false;

		_mapCustomMetricGovvieCorrelationSurface.put (customMetricLabel.fullyQualifiedName() + "@#" +
			govvieLabel.fullyQualifiedName(), auCorrelation);

		return true;
	}

	/**
	 * Retrieve the Correlation Surface between the Forward and the Funding Latent States
	 * 
	 * @param forwardLabel The Forward Latent State Label
	 * @param fundingLabel The Funding Latent State Label
	 * 
	 * @return The Correlation Surface between the Forward and the Funding Latent States
	 */

	public org.drip.quant.function1D.AbstractUnivariate forwardFundingCorrSurface (
		final org.drip.state.identifier.ForwardLabel forwardLabel,
		final org.drip.state.identifier.FundingLabel fundingLabel)
	{
		if (null == forwardLabel || null == fundingLabel) return null;

		java.lang.String strCode = forwardLabel.fullyQualifiedName() + "@#" +
			fundingLabel.fullyQualifiedName();

		return _mapForwardFundingCorrelationSurface.containsKey (strCode) ?
			_mapForwardFundingCorrelationSurface.get (strCode) : null;
	}

	/**
	 * (Re)-set the Correlation Surface between the Forward and the Funding Latent States
	 * 
	 * @param forwardLabel The Forward Curve Latent State Label
	 * @param fundingLabel The Funding Latent State Label
	 * @param auCorrelation The Correlation Surface
	 * 
	 * @return TRUE => Successfully set
	 */

	public boolean setForwardFundingCorrSurface (
		final org.drip.state.identifier.ForwardLabel forwardLabel,
		final org.drip.state.identifier.FundingLabel fundingLabel,
		final org.drip.quant.function1D.AbstractUnivariate auCorrelation)
	{
		if (null == forwardLabel || null == fundingLabel || null == auCorrelation) return false;

		_mapForwardFundingCorrelationSurface.put (forwardLabel.fullyQualifiedName() + "@#" +
			fundingLabel.fullyQualifiedName(), auCorrelation);

		return true;
	}

	/**
	 * Retrieve the Correlation Surface between the Forward and the FX Latent State Labels
	 * 
	 * @param forwardLabel The Forward Curve Latent State Label
	 * @param fxLabel The FX Latent State Label
	 * 
	 * @return The Correlation Surface between the Forward and the FX Latent State Labels
	 */

	public org.drip.quant.function1D.AbstractUnivariate forwardFXCorrSurface (
		final org.drip.state.identifier.ForwardLabel forwardLabel,
		final org.drip.state.identifier.FXLabel fxLabel)
	{
		if (null == forwardLabel || null == fxLabel) return null;

		java.lang.String strCode = forwardLabel.fullyQualifiedName() + "@#" + fxLabel.fullyQualifiedName();

		return _mapForwardFXCorrelationSurface.containsKey (strCode) ? _mapForwardFXCorrelationSurface.get
			(strCode) : null;
	}

	/**
	 * (Re)-set the Correlation Surface between the Forward and the FX Latent State Labels
	 * 
	 * @param forwardLabel The Forward Curve Latent State Label
	 * @param fxLabel The FX Latent State Label
	 * @param auCorrelation The Correlation Surface
	 * 
	 * @return TRUE => Successfully set
	 */

	public boolean setForwardFXCorrSurface (
		final org.drip.state.identifier.ForwardLabel forwardLabel,
		final org.drip.state.identifier.FXLabel fxLabel,
		final org.drip.quant.function1D.AbstractUnivariate auCorrelation)
	{
		if (null == forwardLabel || null == fxLabel || null == auCorrelation) return false;

		_mapForwardFXCorrelationSurface.get (forwardLabel.fullyQualifiedName() + "@#" +
			fxLabel.fullyQualifiedName());

		return true;
	}

	/**
	 * Retrieve the Correlation Surface between the Forward and the Govvie Latent States
	 * 
	 * @param forwardLabel The Forward Curve Latent State Label
	 * @param govvieLabel The Govvie Latent State Label
	 * 
	 * @return The Correlation Surface between the Forward and the Govvie Latent States
	 */

	public org.drip.quant.function1D.AbstractUnivariate forwardGovvieCorrSurface (
		final org.drip.state.identifier.ForwardLabel forwardLabel,
		final org.drip.state.identifier.GovvieLabel govvieLabel)
	{
		if (null == forwardLabel || null == govvieLabel) return null;

		java.lang.String strCode = forwardLabel.fullyQualifiedName() + "@#" +
			govvieLabel.fullyQualifiedName();

		return _mapForwardGovvieCorrelationSurface.containsKey (strCode) ?
			_mapForwardGovvieCorrelationSurface.get (strCode) : null;
	}

	/**
	 * (Re)-set the Correlation Surface between the Forward and the Govvie Latent States
	 * 
	 * @param forwardLabel The Forward Curve Latent State Label
	 * @param govvieLabel The Govvie Latent State Label
	 * @param auCorrelation The Correlation Surface
	 * 
	 * @return TRUE => Successfully set
	 */

	public boolean setForwardGovvieCorrSurface (
		final org.drip.state.identifier.ForwardLabel forwardLabel,
		final org.drip.state.identifier.GovvieLabel govvieLabel,
		final org.drip.quant.function1D.AbstractUnivariate auCorrelation)
	{
		if (null == forwardLabel || null == govvieLabel || null == auCorrelation) return false;

		_mapForwardGovvieCorrelationSurface.put (forwardLabel.fullyQualifiedName() + "@#" + 
			govvieLabel.fullyQualifiedName(), auCorrelation);

		return true;
	}

	/**
	 * Retrieve the Correlation Surface between the Funding and the FX Latent States
	 * 
	 * @param fundingLabel The Funding Latent State Label
	 * @param fxLabel The FX Latent State Label
	 * 
	 * @return The Correlation Surface between the Funding and the FX Latent States
	 */

	public org.drip.quant.function1D.AbstractUnivariate fundingFXCorrSurface (
		final org.drip.state.identifier.FundingLabel fundingLabel,
		final org.drip.state.identifier.FXLabel fxLabel)
	{
		if (null == fundingLabel || null == fxLabel) return null;

		java.lang.String strCode = fundingLabel.fullyQualifiedName() + "@#" + fxLabel.fullyQualifiedName();

		return _mapFundingFXCorrelationSurface.containsKey (strCode) ? _mapFundingFXCorrelationSurface.get
			(strCode) : null;
	}

	/**
	 * (Re)-set the Correlation Surface between the Funding and the FX Latent States
	 * 
	 * @param fundingLabel The Funding Latent State Label
	 * @param fxLabel The FX Latent State Label
	 * @param auCorrelation The Correlation Surface
	 * 
	 * @return TRUE => Successfully set
	 */

	public boolean setFundingFXCorrSurface (
		final org.drip.state.identifier.FundingLabel fundingLabel,
		final org.drip.state.identifier.FXLabel fxLabel,
		final org.drip.quant.function1D.AbstractUnivariate auCorrelation)
	{
		if (null == fundingLabel || null == fxLabel || null == auCorrelation) return false;

		_mapFundingFXCorrelationSurface.put (fundingLabel.fullyQualifiedName() + "@#" +
			fxLabel.fullyQualifiedName(), auCorrelation);

		return true;
	}

	/**
	 * Retrieve the Correlation Surface between the Funding and the Govvie Latent States
	 * 
	 * @param fundingLabel The Funding Latent State Label
	 * @param govvieLabel The Govvie Latent State Label
	 * 
	 * @return The Correlation Surface between the Funding and the Govvie Latent States
	 */

	public org.drip.quant.function1D.AbstractUnivariate fundingGovvieCorrSurface (
		final org.drip.state.identifier.FundingLabel fundingLabel,
		final org.drip.state.identifier.GovvieLabel govvieLabel)
	{
		if (null == fundingLabel || null == govvieLabel) return null;

		java.lang.String strCode = fundingLabel.fullyQualifiedName() + "@#" +
			govvieLabel.fullyQualifiedName();

		return _mapFundingGovvieCorrelationSurface.containsKey (strCode) ?
			_mapFundingGovvieCorrelationSurface.get (strCode) : null;
	}

	/**
	 * (Re)-set the Correlation Surface between the Funding and the Govvie Latent States
	 * 
	 * @param fundingLabel The Funding Latent State Label
	 * @param govvieLabel The Govvie Latent State Label
	 * @param auCorrelation The Correlation Surface
	 * 
	 * @return TRUE => Successfully set
	 */

	public boolean setFundingGovvieCorrSurface (
		final org.drip.state.identifier.FundingLabel fundingLabel,
		final org.drip.state.identifier.GovvieLabel govvieLabel,
		final org.drip.quant.function1D.AbstractUnivariate auCorrelation)
	{
		if (null == fundingLabel || null == govvieLabel || null == auCorrelation) return false;

		_mapFundingGovvieCorrelationSurface.put (fundingLabel.fullyQualifiedName() + "@#" +
			govvieLabel.fullyQualifiedName(), auCorrelation);

		return true;
	}

	/**
	 * Retrieve the Correlation Surface for the specified FX and the Govvie Latent States
	 * 
	 * @param fxLabel The FX Latent State Label
	 * @param govvieLabel The Govvie Latent State Label
	 * 
	 * @return The Correlation Surface for the specified FX and the Govvie Latent States
	 */

	public org.drip.quant.function1D.AbstractUnivariate fxGovvieCorrSurface (
		final org.drip.state.identifier.FXLabel fxLabel,
		final org.drip.state.identifier.GovvieLabel govvieLabel)
	{
		if (null == fxLabel || null == govvieLabel) return null;

		java.lang.String strCode = fxLabel.fullyQualifiedName() + "@#" + govvieLabel.fullyQualifiedName();

		return _mapFXGovvieCorrelationSurface.containsKey (strCode) ? _mapFXGovvieCorrelationSurface.get
			(strCode) : null;
	}

	/**
	 * (Re)-set the Correlation Surface for the specified FX and the Govvie Latent States
	 * 
	 * @param fxLabel The FX Latent State Label
	 * @param govvieLabel The Govvie Latent State Label
	 * @param auCorrelation The Correlation Surface
	 * 
	 * @return TRUE => Successfully set
	 */

	public boolean setFXGovvieCorrSurface (
		final org.drip.state.identifier.FXLabel fxLabel,
		final org.drip.state.identifier.GovvieLabel govvieLabel,
		final org.drip.quant.function1D.AbstractUnivariate auCorrelation)
	{
		if (null == fxLabel || null == govvieLabel || null == auCorrelation) return false;

		_mapFXGovvieCorrelationSurface.put (fxLabel.fullyQualifiedName() + "@#" +
			govvieLabel.fullyQualifiedName(), auCorrelation);

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

					sbFixings.append (meOut.getKey().julian() + collectionMultiLevelKeyDelimiter() +
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
		double dblStart = org.drip.analytics.date.JulianDate.Today().julian();

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
