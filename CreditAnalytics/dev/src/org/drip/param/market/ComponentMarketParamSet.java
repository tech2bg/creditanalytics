
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
 * ComponentMarketParamSet provides implementation of the ComponentMarketParamsRef interface. It serves as a
 *  place holder for the market parameters needed to value the component object – discount curve, forward
 *  curve, treasury curve, credit curve, component quote, treasury quote map, and fixings map.
 *
 * @author Lakshmi Krishnamurthy
 */

public class ComponentMarketParamSet extends org.drip.param.definition.ComponentMarketParams {
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
		_mapDCFunding = new
			org.drip.analytics.support.CaseInsensitiveTreeMap<org.drip.analytics.rates.DiscountCurve>();

	private org.drip.analytics.support.CaseInsensitiveTreeMap<org.drip.quant.function1D.AbstractUnivariate>
		_mapAUFX = new
			org.drip.analytics.support.CaseInsensitiveTreeMap<org.drip.quant.function1D.AbstractUnivariate>();

	private org.drip.analytics.support.CaseInsensitiveTreeMap<org.drip.analytics.rates.DiscountCurve>
		_mapDCGovvie = new
			org.drip.analytics.support.CaseInsensitiveTreeMap<org.drip.analytics.rates.DiscountCurve>();

	private org.drip.analytics.support.CaseInsensitiveTreeMap<org.drip.quant.function1D.AbstractUnivariate>
		_mapAUCollateralVolatility = new
			org.drip.analytics.support.CaseInsensitiveTreeMap<org.drip.quant.function1D.AbstractUnivariate>();

	private org.drip.analytics.support.CaseInsensitiveTreeMap<org.drip.quant.function1D.AbstractUnivariate>
		_mapAUCreditVolatility = new
			org.drip.analytics.support.CaseInsensitiveTreeMap<org.drip.quant.function1D.AbstractUnivariate>();

	private
		org.drip.analytics.support.CaseInsensitiveTreeMap<java.util.Map<org.drip.analytics.date.JulianDate,
			org.drip.quant.function1D.AbstractUnivariate>> _mapCustomMetricVolatility = new
				org.drip.analytics.support.CaseInsensitiveTreeMap<java.util.Map<org.drip.analytics.date.JulianDate,
		org.drip.quant.function1D.AbstractUnivariate>>();

	private org.drip.analytics.support.CaseInsensitiveTreeMap<org.drip.quant.function1D.AbstractUnivariate>
		_mapAUForwardVolatility = new
			org.drip.analytics.support.CaseInsensitiveTreeMap<org.drip.quant.function1D.AbstractUnivariate>();

	private org.drip.analytics.support.CaseInsensitiveTreeMap<org.drip.quant.function1D.AbstractUnivariate>
		_mapAUFundingVolatility = new
			org.drip.analytics.support.CaseInsensitiveTreeMap<org.drip.quant.function1D.AbstractUnivariate>();

	private org.drip.analytics.support.CaseInsensitiveTreeMap<org.drip.quant.function1D.AbstractUnivariate>
		_mapAUFXVolatility = new
			org.drip.analytics.support.CaseInsensitiveTreeMap<org.drip.quant.function1D.AbstractUnivariate>();

	private org.drip.analytics.support.CaseInsensitiveTreeMap<org.drip.quant.function1D.AbstractUnivariate>
		_mapAUGovvieVolatility = new
			org.drip.analytics.support.CaseInsensitiveTreeMap<org.drip.quant.function1D.AbstractUnivariate>();

	private org.drip.analytics.support.CaseInsensitiveTreeMap<org.drip.quant.function1D.AbstractUnivariate>
		_mapAUCollateralCollateralCorrelation = new
			org.drip.analytics.support.CaseInsensitiveTreeMap<org.drip.quant.function1D.AbstractUnivariate>();

	private org.drip.analytics.support.CaseInsensitiveTreeMap<org.drip.quant.function1D.AbstractUnivariate>
		_mapAUCollateralCreditCorrelation = new
			org.drip.analytics.support.CaseInsensitiveTreeMap<org.drip.quant.function1D.AbstractUnivariate>();

	private org.drip.analytics.support.CaseInsensitiveTreeMap<org.drip.quant.function1D.AbstractUnivariate>
		_mapAUCollateralCustomMetricCorrelation = new
			org.drip.analytics.support.CaseInsensitiveTreeMap<org.drip.quant.function1D.AbstractUnivariate>();

	private org.drip.analytics.support.CaseInsensitiveTreeMap<org.drip.quant.function1D.AbstractUnivariate>
		_mapAUCollateralForwardCorrelation = new
			org.drip.analytics.support.CaseInsensitiveTreeMap<org.drip.quant.function1D.AbstractUnivariate>();

	private org.drip.analytics.support.CaseInsensitiveTreeMap<org.drip.quant.function1D.AbstractUnivariate>
		_mapAUCollateralFundingCorrelation = new
			org.drip.analytics.support.CaseInsensitiveTreeMap<org.drip.quant.function1D.AbstractUnivariate>();

	private org.drip.analytics.support.CaseInsensitiveTreeMap<org.drip.quant.function1D.AbstractUnivariate>
		_mapAUCollateralFXCorrelation = new
			org.drip.analytics.support.CaseInsensitiveTreeMap<org.drip.quant.function1D.AbstractUnivariate>();

	private org.drip.analytics.support.CaseInsensitiveTreeMap<org.drip.quant.function1D.AbstractUnivariate>
		_mapAUCollateralGovvieCorrelation = new
			org.drip.analytics.support.CaseInsensitiveTreeMap<org.drip.quant.function1D.AbstractUnivariate>();

	private org.drip.analytics.support.CaseInsensitiveTreeMap<org.drip.quant.function1D.AbstractUnivariate>
		_mapAUCreditCreditCorrelation = new
			org.drip.analytics.support.CaseInsensitiveTreeMap<org.drip.quant.function1D.AbstractUnivariate>();

	private org.drip.analytics.support.CaseInsensitiveTreeMap<org.drip.quant.function1D.AbstractUnivariate>
		_mapAUCreditCustomMetricCorrelation = new
			org.drip.analytics.support.CaseInsensitiveTreeMap<org.drip.quant.function1D.AbstractUnivariate>();

	private org.drip.analytics.support.CaseInsensitiveTreeMap<org.drip.quant.function1D.AbstractUnivariate>
		_mapAUCreditForwardCorrelation = new
			org.drip.analytics.support.CaseInsensitiveTreeMap<org.drip.quant.function1D.AbstractUnivariate>();

	private org.drip.analytics.support.CaseInsensitiveTreeMap<org.drip.quant.function1D.AbstractUnivariate>
		_mapAUCreditFundingCorrelation = new
			org.drip.analytics.support.CaseInsensitiveTreeMap<org.drip.quant.function1D.AbstractUnivariate>();

	private org.drip.analytics.support.CaseInsensitiveTreeMap<org.drip.quant.function1D.AbstractUnivariate>
		_mapAUCreditFXCorrelation = new
			org.drip.analytics.support.CaseInsensitiveTreeMap<org.drip.quant.function1D.AbstractUnivariate>();

	private org.drip.analytics.support.CaseInsensitiveTreeMap<org.drip.quant.function1D.AbstractUnivariate>
		_mapAUCreditGovvieCorrelation = new
			org.drip.analytics.support.CaseInsensitiveTreeMap<org.drip.quant.function1D.AbstractUnivariate>();

	private org.drip.analytics.support.CaseInsensitiveTreeMap<org.drip.quant.function1D.AbstractUnivariate>
		_mapAUCustomMetricCustomMetricCorrelation = new
			org.drip.analytics.support.CaseInsensitiveTreeMap<org.drip.quant.function1D.AbstractUnivariate>();

	private org.drip.analytics.support.CaseInsensitiveTreeMap<org.drip.quant.function1D.AbstractUnivariate>
		_mapAUCustomMetricForwardCorrelation = new
			org.drip.analytics.support.CaseInsensitiveTreeMap<org.drip.quant.function1D.AbstractUnivariate>();

	private org.drip.analytics.support.CaseInsensitiveTreeMap<org.drip.quant.function1D.AbstractUnivariate>
		_mapAUCustomMetricFundingCorrelation = new
			org.drip.analytics.support.CaseInsensitiveTreeMap<org.drip.quant.function1D.AbstractUnivariate>();

	private org.drip.analytics.support.CaseInsensitiveTreeMap<org.drip.quant.function1D.AbstractUnivariate>
		_mapAUCustomMetricFXCorrelation = new
			org.drip.analytics.support.CaseInsensitiveTreeMap<org.drip.quant.function1D.AbstractUnivariate>();

	private org.drip.analytics.support.CaseInsensitiveTreeMap<org.drip.quant.function1D.AbstractUnivariate>
		_mapAUCustomMetricGovvieCorrelation = new
			org.drip.analytics.support.CaseInsensitiveTreeMap<org.drip.quant.function1D.AbstractUnivariate>();

	private org.drip.analytics.support.CaseInsensitiveTreeMap<org.drip.quant.function1D.AbstractUnivariate>
		_mapAUForwardForwardCorrelation = new
			org.drip.analytics.support.CaseInsensitiveTreeMap<org.drip.quant.function1D.AbstractUnivariate>();

	private org.drip.analytics.support.CaseInsensitiveTreeMap<org.drip.quant.function1D.AbstractUnivariate>
		_mapAUForwardFundingCorrelation = new
			org.drip.analytics.support.CaseInsensitiveTreeMap<org.drip.quant.function1D.AbstractUnivariate>();

	private org.drip.analytics.support.CaseInsensitiveTreeMap<org.drip.quant.function1D.AbstractUnivariate>
		_mapAUForwardFXCorrelation = new
			org.drip.analytics.support.CaseInsensitiveTreeMap<org.drip.quant.function1D.AbstractUnivariate>();

	private org.drip.analytics.support.CaseInsensitiveTreeMap<org.drip.quant.function1D.AbstractUnivariate>
		_mapAUForwardGovvieCorrelation = new
			org.drip.analytics.support.CaseInsensitiveTreeMap<org.drip.quant.function1D.AbstractUnivariate>();

	private org.drip.analytics.support.CaseInsensitiveTreeMap<org.drip.quant.function1D.AbstractUnivariate>
		_mapAUFundingFundingCorrelation = new
			org.drip.analytics.support.CaseInsensitiveTreeMap<org.drip.quant.function1D.AbstractUnivariate>();

	private org.drip.analytics.support.CaseInsensitiveTreeMap<org.drip.quant.function1D.AbstractUnivariate>
		_mapAUFundingFXCorrelation = new
			org.drip.analytics.support.CaseInsensitiveTreeMap<org.drip.quant.function1D.AbstractUnivariate>();

	private org.drip.analytics.support.CaseInsensitiveTreeMap<org.drip.quant.function1D.AbstractUnivariate>
		_mapAUFundingGovvieCorrelation = new
			org.drip.analytics.support.CaseInsensitiveTreeMap<org.drip.quant.function1D.AbstractUnivariate>();

	private org.drip.analytics.support.CaseInsensitiveTreeMap<org.drip.quant.function1D.AbstractUnivariate>
		_mapAUFXFXCorrelation = new
			org.drip.analytics.support.CaseInsensitiveTreeMap<org.drip.quant.function1D.AbstractUnivariate>();

	private org.drip.analytics.support.CaseInsensitiveTreeMap<org.drip.quant.function1D.AbstractUnivariate>
		_mapAUFXGovvieCorrelation = new
			org.drip.analytics.support.CaseInsensitiveTreeMap<org.drip.quant.function1D.AbstractUnivariate>();

	private org.drip.analytics.support.CaseInsensitiveTreeMap<org.drip.quant.function1D.AbstractUnivariate>
		_mapAUGovvieGovvieCorrelation = new
			org.drip.analytics.support.CaseInsensitiveTreeMap<org.drip.quant.function1D.AbstractUnivariate>();

	private org.drip.analytics.support.CaseInsensitiveTreeMap<org.drip.param.definition.ComponentQuote>
		_mapComponentQuote = new
			org.drip.analytics.support.CaseInsensitiveTreeMap<org.drip.param.definition.ComponentQuote>();

	private java.util.Map<org.drip.analytics.date.JulianDate,
		org.drip.analytics.support.CaseInsensitiveTreeMap<java.lang.Double>> _mmFixings = null;

	/**
	 * Empty ComponentMarketParamSet Constructor
	 */

	public ComponentMarketParamSet()
	{
	}

	/**
	 * ComponentMarketParamSet de-serialization from input byte array
	 * 
	 * @param ab Byte Array
	 * 
	 * @throws java.lang.Exception Thrown if ComponentMarketParamSet cannot be properly de-serialized
	 */

	public ComponentMarketParamSet (
		final byte[] ab)
		throws java.lang.Exception
	{
		if (null == ab || 0 == ab.length)
			throw new java.lang.Exception
				("ComponentMarketParamSet de-serializer: Invalid input Byte array");

		java.lang.String strRawString = new java.lang.String (ab);

		if (null == strRawString || strRawString.isEmpty())
			throw new java.lang.Exception ("ComponentMarketParamSet de-serializer: Empty state");

		java.lang.String strSerializedComponentMarketParams = strRawString.substring (0, strRawString.indexOf
			(objectTrailer()));

		if (null == strSerializedComponentMarketParams || strSerializedComponentMarketParams.isEmpty())
			throw new java.lang.Exception ("ComponentMarketParamSet de-serializer: Cannot locate state");

		java.lang.String[] astrField = org.drip.quant.common.StringUtil.Split
			(strSerializedComponentMarketParams, fieldDelimiter());

		if (null == astrField || 4 > astrField.length)
			throw new java.lang.Exception ("ComponentMarketParamSet de-serializer: Invalid reqd field set");

		// double dblVersion = new java.lang.Double (astrField[0]);

		if (null == astrField[1] || astrField[1].isEmpty())
			throw new java.lang.Exception
				("ComponentMarketParamSet de-serializer: Cannot locate forward curve");

		if (!org.drip.service.stream.Serializer.NULL_SER_STRING.equalsIgnoreCase (astrField[1]))
			_mapForwardCurve = null;

		if (null == astrField[2] || astrField[2].isEmpty())
			throw new java.lang.Exception ("ComponentMarketParamSet de-serializer: Cannot locate fixings");

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
			throw new java.lang.Exception
				("ComponentMarketParamSet de-serializer: Cannot locate TSY quotes");

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

					if (null == _mapComponentQuote)
						_mapComponentQuote = new
							org.drip.analytics.support.CaseInsensitiveTreeMap<org.drip.param.definition.ComponentQuote>();

					_mapComponentQuote.put (astrKVPair[0], new org.drip.param.market.ComponentMultiMeasureQuote
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

	@Override public org.drip.analytics.rates.DiscountCurve payCurrencyCollateralCurrencyCurve (
		final java.lang.String strPayCurrency,
		final java.lang.String strCollateralCurrency)
	{
		if (null == strPayCurrency || !_mapPayCurrencyForeignCollateralDC.containsKey (strPayCurrency) ||
			null == strCollateralCurrency)
			return null;

		return _mapPayCurrencyForeignCollateralDC.get (strPayCurrency).get (strCollateralCurrency);
	}

	@Override public boolean setPayCurrencyCollateralCurrencyCurve (
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

	@Override public org.drip.analytics.rates.DiscountCurve collateralChoiceDiscountCurve (
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

	@Override public org.drip.analytics.definition.CreditCurve creditCurve (
		final java.lang.String strCreditCurveName)
	{
		if (null == strCreditCurveName || strCreditCurveName.isEmpty() || !_mapCreditCurve.containsKey
			(strCreditCurveName))
			return null;

		return _mapCreditCurve.get (strCreditCurveName);
	}

	@Override public boolean setCreditCurve (
		final org.drip.analytics.definition.CreditCurve cc)
	{
		if (null == cc) return false;

		_mapCreditCurve.put (cc.name(), cc);

		return true;
	}

	@Override public org.drip.analytics.rates.ForwardCurve forwardCurve (
		final org.drip.product.params.FloatingRateIndex fri)
	{
		if (null == fri) return null;

		java.lang.String strFullyQualifiedName = fri.fullyQualifiedName();

		return _mapForwardCurve.containsKey (strFullyQualifiedName) ? _mapForwardCurve.get
			(strFullyQualifiedName) : null;
	}

	@Override public boolean setForwardCurve (
		final org.drip.analytics.rates.ForwardCurve fc)
	{
		if (null == fc) return false;

		_mapForwardCurve.put (fc.index().fullyQualifiedName(), fc);

		return true;
	}

	@Override public org.drip.analytics.rates.DiscountCurve fundingCurve (
		final java.lang.String strCurrency)
	{
		return null == strCurrency || strCurrency.isEmpty() || !_mapDCFunding.containsKey (strCurrency) ?
			null : _mapDCFunding.get (strCurrency);
	}

	@Override public boolean setFundingCurve (
		final org.drip.analytics.rates.DiscountCurve dc)
	{
		if (null == dc) return false;

		_mapDCFunding.put (dc.currency(), dc);

		return true;
	}

	@Override public org.drip.quant.function1D.AbstractUnivariate fxCurve (
		final org.drip.product.params.CurrencyPair cp)
	{
		if (null == cp) return null;

		java.lang.String strFXCode = cp.code();

		return _mapAUFX.containsKey (strFXCode) ? _mapAUFX.get (strFXCode) : null;
	}

	@Override public boolean setFXCurve (
		final org.drip.product.params.CurrencyPair cp,
		final org.drip.quant.function1D.AbstractUnivariate auFX)
	{
		if (null == cp || null == auFX) return false;

		_mapAUFX.put (cp.code(), auFX);

		try {
			_mapAUFX.put (cp.inverseCode(), new org.drip.quant.function1D.UnivariateReciprocal (auFX));
		} catch (java.lang.Exception e) {
			e.printStackTrace();

			return false;
		}

		return true;
	}

	@Override public org.drip.analytics.rates.DiscountCurve govvieCurve (
		final java.lang.String strCurrency)
	{
		return null == strCurrency || strCurrency.isEmpty() || !_mapDCGovvie.containsKey (strCurrency) ? null
			: _mapDCGovvie.get (strCurrency);
	}

	@Override public boolean setGovvieCurve (
		final org.drip.analytics.rates.DiscountCurve dcGovvie)
	{
		if (null == dcGovvie) return false;

		_mapDCGovvie.put (dcGovvie.currency(), dcGovvie);

		return true;
	}

	@Override public org.drip.quant.function1D.AbstractUnivariate collateralCurveVolSurface (
		final java.lang.String strCurrency)
	{
		if (null == strCurrency || strCurrency.isEmpty() || !_mapAUCollateralVolatility.containsKey
			(strCurrency))
			return null;

		return _mapAUCollateralVolatility.get (strCurrency);
	}

	@Override public boolean setCollateralCurveVolSurface (
		final java.lang.String strCurrency,
		final org.drip.quant.function1D.AbstractUnivariate auVolatility)
	{
		if (null == strCurrency || strCurrency.isEmpty() || null == auVolatility) return false;

		_mapAUCollateralVolatility.put (strCurrency, auVolatility);

		return true;
	}

	@Override public org.drip.quant.function1D.AbstractUnivariate creditCurveVolSurface (
		final java.lang.String strCreditCurveName)
	{
		return null == strCreditCurveName || strCreditCurveName.isEmpty() ||
			!_mapAUCreditVolatility.containsKey (strCreditCurveName) ? null : _mapAUCreditVolatility.get
				(strCreditCurveName);
	}

	@Override public boolean setCreditCurveVolSurface (
		final java.lang.String strCreditCurveName,
		final org.drip.quant.function1D.AbstractUnivariate auVolatility)
	{
		if (null == strCreditCurveName || strCreditCurveName.isEmpty() || null == auVolatility) return false;

		_mapAUCreditVolatility.put (strCreditCurveName, auVolatility);

		return true;
	}

	@Override public org.drip.quant.function1D.AbstractUnivariate customMetricVolSurface (
		final java.lang.String strCustomMetric,
		final org.drip.analytics.date.JulianDate dtForward)
	{
		if (null == strCustomMetric || !_mapCustomMetricVolatility.containsKey (strCustomMetric))
			return null;

		java.util.Map<org.drip.analytics.date.JulianDate, org.drip.quant.function1D.AbstractUnivariate>
			mapForwardVolatility = _mapCustomMetricVolatility.get (strCustomMetric);

		if (null == mapForwardVolatility || !mapForwardVolatility.containsKey (dtForward)) return null;

		return mapForwardVolatility.get (dtForward);
	}

	@Override public boolean setCustomMetricVolSurface (
		final java.lang.String strCustomMetric,
		final org.drip.analytics.date.JulianDate dtForward,
		final org.drip.quant.function1D.AbstractUnivariate auVolatility)
	{
		if (null == strCustomMetric || strCustomMetric.isEmpty() || null == dtForward || null ==
			auVolatility)
			return false;

		java.util.Map<org.drip.analytics.date.JulianDate, org.drip.quant.function1D.AbstractUnivariate>
			mapForwardVolatility = _mapCustomMetricVolatility.get (strCustomMetric);

		if (null == mapForwardVolatility) {
			mapForwardVolatility = new java.util.HashMap<org.drip.analytics.date.JulianDate,
				org.drip.quant.function1D.AbstractUnivariate>();

			mapForwardVolatility.put (dtForward, auVolatility);

			_mapCustomMetricVolatility.put (strCustomMetric, mapForwardVolatility);
		} else
			mapForwardVolatility.put (dtForward, auVolatility);

		return true;
	}

	@Override public org.drip.quant.function1D.AbstractUnivariate forwardCurveVolSurface (
		final org.drip.product.params.FloatingRateIndex fri)
	{
		if (null == fri) return null;

		java.lang.String strFRI = fri.fullyQualifiedName();

		return _mapAUForwardVolatility.containsKey (strFRI) ? _mapAUForwardVolatility.get (strFRI) : null;
	}

	@Override public boolean setForwardCurveVolSurface (
		final org.drip.product.params.FloatingRateIndex fri,
		final org.drip.quant.function1D.AbstractUnivariate auVolatility)
	{
		if (null == fri || null == auVolatility) return false;

		_mapAUForwardVolatility.put (fri.fullyQualifiedName(), auVolatility);

		return true;
	}

	@Override public org.drip.quant.function1D.AbstractUnivariate fundingCurveVolSurface (
		final java.lang.String strCurrency)
	{
		if (null == strCurrency || strCurrency.isEmpty() || !_mapAUFundingVolatility.containsKey
			(strCurrency))
			return null;

		return _mapAUFundingVolatility.get (strCurrency);
	}

	@Override public boolean setFundingCurveVolSurface (
		final java.lang.String strCurrency,
		final org.drip.quant.function1D.AbstractUnivariate auVolatility)
	{
		if (null == strCurrency || strCurrency.isEmpty() || null == auVolatility) return false;

		_mapAUFundingVolatility.put (strCurrency, auVolatility);

		return true;
	}

	@Override public org.drip.quant.function1D.AbstractUnivariate fxCurveVolSurface (
		final org.drip.product.params.CurrencyPair cp)
	{
		if (null == cp) return null;

		java.lang.String strCode = cp.code();

		return !_mapAUFXVolatility.containsKey (strCode) ? null : _mapAUFXVolatility.get (strCode);
	}

	@Override public boolean setFXCurveVolSurface (
		final org.drip.product.params.CurrencyPair cp,
		final org.drip.quant.function1D.AbstractUnivariate auVolatility)
	{
		if (null == cp || null == auVolatility) return false;

		_mapAUFXVolatility.put (cp.code(), auVolatility);

		return true;
	}

	@Override public org.drip.quant.function1D.AbstractUnivariate govvieCurveVolSurface (
		final java.lang.String strCurrency)
	{
		return null == strCurrency || strCurrency.isEmpty() ||
			!_mapAUGovvieVolatility.containsKey (strCurrency) ? null : _mapAUGovvieVolatility.get
				(strCurrency);
	}

	@Override public boolean setGovvieCurveVolSurface (
		final java.lang.String strCurrency,
		final org.drip.quant.function1D.AbstractUnivariate auVolatility)
	{
		if (null == strCurrency || strCurrency.isEmpty() || null == auVolatility) return false;

		_mapAUGovvieVolatility.put (strCurrency, auVolatility);

		return true;
	}

	@Override public org.drip.quant.function1D.AbstractUnivariate collateralCollateralCorrSurface (
		final java.lang.String strCurrency1,
		final java.lang.String strCurrency2)
	{
		if (null == strCurrency1 || strCurrency1.isEmpty() || null == strCurrency2 || strCurrency2.isEmpty())
			return null;

		java.lang.String strCode = strCurrency1 + "@#" + strCurrency2;

		if (!_mapAUCollateralCollateralCorrelation.containsKey (strCode)) return null;

		return _mapAUCollateralCollateralCorrelation.get (strCode);
	}

	@Override public boolean setCollateralCollateralCorrSurface (
		final java.lang.String strCurrency1,
		final java.lang.String strCurrency2,
		final org.drip.quant.function1D.AbstractUnivariate auCorrelation)
	{
		if (null == strCurrency1 || strCurrency1.isEmpty() || null == strCurrency2 || strCurrency2.isEmpty()
			|| null == auCorrelation)
			return false;

		_mapAUCollateralFundingCorrelation.put (strCurrency1 + "@#" + strCurrency2, auCorrelation);

		_mapAUCollateralFundingCorrelation.put (strCurrency2 + "@#" + strCurrency1, auCorrelation);

		return true;
	}

	@Override public org.drip.quant.function1D.AbstractUnivariate creditCreditCorrSurface (
		final java.lang.String strCreditCurveName1,
		final java.lang.String strCreditCurveName2)
	{
		if (null == strCreditCurveName1 || strCreditCurveName1.isEmpty() || null == strCreditCurveName2 ||
			strCreditCurveName2.isEmpty())
			return null;

		java.lang.String strCode = strCreditCurveName1 + "@#" + strCreditCurveName2;

		return !_mapAUCreditCreditCorrelation.containsKey (strCode) ? null :
			_mapAUCreditCreditCorrelation.get (strCode);
	}

	@Override public boolean setCreditCreditCorrSurface (
		final java.lang.String strCreditCurveName1,
		final java.lang.String strCreditCurveName2,
		final org.drip.quant.function1D.AbstractUnivariate auCorrelation)
	{
		if (null == strCreditCurveName1 || strCreditCurveName1.isEmpty() || null == strCreditCurveName2 ||
			strCreditCurveName2.isEmpty() || null == auCorrelation)
			return false;

		_mapAUCreditCreditCorrelation.put (strCreditCurveName1 + "@#" + strCreditCurveName2, auCorrelation);

		_mapAUCreditCreditCorrelation.put (strCreditCurveName2 + "@#" + strCreditCurveName1, auCorrelation);

		return true;
	}

	@Override public org.drip.quant.function1D.AbstractUnivariate customMetricCustomMetricCorrSurface (
		final java.lang.String strCustomMetric1,
		final java.lang.String strCustomMetric2)
	{
		if (null == strCustomMetric1 || strCustomMetric1.isEmpty() || null == strCustomMetric2 ||
			strCustomMetric2.isEmpty())
			return null;

		java.lang.String strCode = strCustomMetric1 + "@#" + strCustomMetric2;

		if (!_mapAUCustomMetricCustomMetricCorrelation.containsKey (strCode)) return null;

		return _mapAUCustomMetricCustomMetricCorrelation.get (strCode);
	}

	@Override public boolean setCustomMetricCustomMetricCorrSurface (
		final java.lang.String strCustomMetric1,
		final java.lang.String strCustomMetric2,
		final org.drip.quant.function1D.AbstractUnivariate auCorrelation)
	{
		if (null == strCustomMetric1 || strCustomMetric1.isEmpty() || null == strCustomMetric2 ||
			strCustomMetric2.isEmpty() || null == auCorrelation)
			return false;

		_mapAUCustomMetricCustomMetricCorrelation.put (strCustomMetric1 + "@#" + strCustomMetric2,
			auCorrelation);

		_mapAUCustomMetricCustomMetricCorrelation.put (strCustomMetric2 + "@#" + strCustomMetric1,
			auCorrelation);

		return true;
	}

	@Override public org.drip.quant.function1D.AbstractUnivariate forwardForwardCorrSurface (
		final org.drip.product.params.FloatingRateIndex fri1,
		final org.drip.product.params.FloatingRateIndex fri2)
	{
		if (null == fri1 || null == fri2) return null;

		java.lang.String strCode = fri1.fullyQualifiedName() + "@#" + fri2.fullyQualifiedName();

		if (!_mapAUForwardForwardCorrelation.containsKey (strCode)) return null;

		return _mapAUForwardForwardCorrelation.get (strCode);
	}

	@Override public boolean setForwardForwardCorrSurface (
		final org.drip.product.params.FloatingRateIndex fri1,
		final org.drip.product.params.FloatingRateIndex fri2,
		final org.drip.quant.function1D.AbstractUnivariate auCorrelation)
	{
		if (null == fri1 || null == fri2 || null == auCorrelation) return false;

		_mapAUForwardForwardCorrelation.put (fri1.fullyQualifiedName() + "@#" + fri2.fullyQualifiedName(),
			auCorrelation);

		_mapAUForwardForwardCorrelation.put (fri2.fullyQualifiedName() + "@#" + fri1.fullyQualifiedName(),
			auCorrelation);

		return true;
	}

	@Override public org.drip.quant.function1D.AbstractUnivariate fundingFundingCorrSurface (
		final java.lang.String strCurrency1,
		final java.lang.String strCurrency2)
	{
		if (null == strCurrency1 || strCurrency1.isEmpty() || null == strCurrency2 || strCurrency2.isEmpty())
			return null;

		java.lang.String strCode = strCurrency1 + "@#" + strCurrency2;

		if (!_mapAUFundingFundingCorrelation.containsKey (strCode)) return null;

		return _mapAUFundingFundingCorrelation.get (strCode);
	}

	@Override public boolean setFundingFundingCorrSurface (
		final java.lang.String strCurrency1,
		final java.lang.String strCurrency2,
		final org.drip.quant.function1D.AbstractUnivariate auCorrelation)
	{
		if (null == strCurrency1 || strCurrency1.isEmpty() || null == strCurrency2 || strCurrency2.isEmpty()
			|| null == auCorrelation)
			return false;

		_mapAUFundingFundingCorrelation.put (strCurrency1 + "@#" + strCurrency2, auCorrelation);

		_mapAUFundingFundingCorrelation.put (strCurrency2 + "@#" + strCurrency1, auCorrelation);

		return true;
	}

	@Override public org.drip.quant.function1D.AbstractUnivariate fxFXCorrSurface (
		final org.drip.product.params.CurrencyPair cp1,
		final org.drip.product.params.CurrencyPair cp2)
	{
		if (null == cp1 || null == cp2) return null;

		java.lang.String strCode = cp1.code() + "@#" + cp2.code();

		return !_mapAUFXFXCorrelation.containsKey (strCode) ? null : _mapAUFXFXCorrelation.get (strCode);
	}

	@Override public boolean setFXFXCorrSurface (
		final org.drip.product.params.CurrencyPair cp1,
		final org.drip.product.params.CurrencyPair cp2,
		final org.drip.quant.function1D.AbstractUnivariate auCorrelation)
	{
		if (null == cp1 || null == cp2 || null == auCorrelation) return false;

		_mapAUFXFXCorrelation.put (cp1.code() + "@#" + cp2.code(), auCorrelation);

		_mapAUFXFXCorrelation.put (cp2.code() + "@#" + cp1.code(), auCorrelation);

		return true;
	}

	@Override public org.drip.quant.function1D.AbstractUnivariate govvieGovvieCorrSurface (
		final java.lang.String strCurrency1,
		final java.lang.String strCurrency2)
	{
		if (null == strCurrency1 || strCurrency1.isEmpty() || null == strCurrency2 || strCurrency2.isEmpty())
			return null;

		java.lang.String strCode = strCurrency1 + "@#" + strCurrency2;

		if (!_mapAUGovvieGovvieCorrelation.containsKey (strCode)) return null;

		return _mapAUGovvieGovvieCorrelation.get (strCode);
	}

	@Override public boolean setGovvieGovvieCorrSurface (
		final java.lang.String strCurrency1,
		final java.lang.String strCurrency2,
		final org.drip.quant.function1D.AbstractUnivariate auCorrelation)
	{
		if (null == strCurrency1 || strCurrency1.isEmpty() || null == strCurrency2 || strCurrency2.isEmpty()
			|| null == auCorrelation)
			return false;

		_mapAUGovvieGovvieCorrelation.put (strCurrency1 + "@#" + strCurrency2, auCorrelation);

		_mapAUGovvieGovvieCorrelation.put (strCurrency2 + "@#" + strCurrency1, auCorrelation);

		return true;
	}

	@Override public org.drip.quant.function1D.AbstractUnivariate collateralCreditCorrSurface (
		final java.lang.String strCollateralCurrency,
		final java.lang.String strCreditCurveName)
	{
		if (null == strCollateralCurrency || strCollateralCurrency.isEmpty() || null == strCreditCurveName ||
			strCreditCurveName.isEmpty())
			return null;

		java.lang.String strCode = strCollateralCurrency + "@#" + strCreditCurveName;

		if (!_mapAUCollateralCreditCorrelation.containsKey (strCode)) return null;

		return _mapAUCollateralCreditCorrelation.get (strCode);
	}

	@Override public boolean setCollateralCreditCorrSurface (
		final java.lang.String strCollateralCurrency,
		final java.lang.String strCreditCurveName,
		final org.drip.quant.function1D.AbstractUnivariate auCorrelation)
	{
		if (null == strCollateralCurrency || strCollateralCurrency.isEmpty() || null == strCreditCurveName ||
			strCreditCurveName.isEmpty())
			return false;

		_mapAUCollateralCreditCorrelation.put (strCollateralCurrency + "@#" + strCreditCurveName,
			auCorrelation);

		return true;
	}

	@Override public org.drip.quant.function1D.AbstractUnivariate collateralCustomMetricCorrSurface (
		final java.lang.String strCollateralCurrency,
		final java.lang.String strCustomMetric)
	{
		if (null == strCollateralCurrency || strCollateralCurrency.isEmpty() || null == strCustomMetric ||
			strCustomMetric.isEmpty())
			return null;

		java.lang.String strCode = strCollateralCurrency + "@#" + strCustomMetric;

		if (!_mapAUCollateralCustomMetricCorrelation.containsKey (strCode)) return null;

		return _mapAUCollateralCustomMetricCorrelation.get (strCode);
	}

	@Override public boolean setCollateralCustomMetricCorrSurface (
		final java.lang.String strCollateralCurrency,
		final java.lang.String strCustomMetric,
		final org.drip.quant.function1D.AbstractUnivariate auCorrelation)
	{
		if (null == strCollateralCurrency || strCollateralCurrency.isEmpty() || null == strCustomMetric ||
			strCustomMetric.isEmpty())
			return false;

		_mapAUCollateralCustomMetricCorrelation.put (strCollateralCurrency + "@#" + strCustomMetric,
			auCorrelation);

		return true;
	}

	@Override public org.drip.quant.function1D.AbstractUnivariate collateralForwardCorrSurface (
		final java.lang.String strCollateralCurrency,
		final org.drip.product.params.FloatingRateIndex fri)
	{
		if (null == strCollateralCurrency || strCollateralCurrency.isEmpty() || null == fri) return null;

		java.lang.String strCollateralForwardCorrelationCode = strCollateralCurrency + "@#" +
			fri.fullyQualifiedName();

		if (!_mapAUCollateralForwardCorrelation.containsKey (strCollateralForwardCorrelationCode))
			return null;

		return _mapAUCollateralForwardCorrelation.get (strCollateralForwardCorrelationCode);
	}

	@Override public boolean setCollateralForwardCorrSurface (
		final java.lang.String strCollateralCurrency,
		final org.drip.product.params.FloatingRateIndex fri,
		final org.drip.quant.function1D.AbstractUnivariate auCorrelation)
	{
		if (null == strCollateralCurrency || strCollateralCurrency.isEmpty() || null == fri || null ==
			auCorrelation)
			return false;

		_mapAUCollateralFundingCorrelation.put (strCollateralCurrency + "@#" + fri.fullyQualifiedName(),
			auCorrelation);

		return true;
	}

	@Override public org.drip.quant.function1D.AbstractUnivariate collateralFundingCorrSurface (
		final java.lang.String strCollateralCurrency,
		final java.lang.String strFundingCurrency)
	{
		if (null == strCollateralCurrency || strCollateralCurrency.isEmpty() || null == strFundingCurrency ||
			strFundingCurrency.isEmpty())
			return null;

		java.lang.String strCode = strCollateralCurrency + "@#" + strFundingCurrency;

		if (!_mapAUCollateralFundingCorrelation.containsKey (strCode)) return null;

		return _mapAUCollateralFundingCorrelation.get (strCode);
	}

	@Override public boolean setCollateralFundingCorrSurface (
		final java.lang.String strCollateralCurrency,
		final java.lang.String strFundingCurrency,
		final org.drip.quant.function1D.AbstractUnivariate auCorrelation)
	{
		if (null == strCollateralCurrency || strCollateralCurrency.isEmpty() || null == strFundingCurrency ||
			strFundingCurrency.isEmpty() || null == auCorrelation)
			return false;

		_mapAUCollateralFundingCorrelation.put (strCollateralCurrency + "@#" + strFundingCurrency,
			auCorrelation);

		return true;
	}

	@Override public org.drip.quant.function1D.AbstractUnivariate collateralFXCorrSurface (
		final java.lang.String strCollateralCurrency,
		final org.drip.product.params.CurrencyPair cp)
	{
		if (null == strCollateralCurrency || strCollateralCurrency.isEmpty() || null == cp) return null;

		java.lang.String strCode = strCollateralCurrency + "@#" + cp.code();

		return _mapAUCollateralFXCorrelation.containsKey (strCode) ? _mapAUCustomMetricForwardCorrelation.get
			(strCode) : null;
	}

	@Override public boolean setCollateralFXCorrSurface (
		final java.lang.String strCollateralCurrency,
		final org.drip.product.params.CurrencyPair cp,
		final org.drip.quant.function1D.AbstractUnivariate auCorrelation)
	{
		if (null == strCollateralCurrency || strCollateralCurrency.isEmpty() || null == cp || null ==
			auCorrelation)
			return false;

		_mapAUCollateralFXCorrelation.put (strCollateralCurrency + "@#" + cp.code(), auCorrelation);

		return true;
	}

	@Override public org.drip.quant.function1D.AbstractUnivariate collateralGovvieCorrSurface (
		final java.lang.String strCollateralCurrency,
		final java.lang.String strGovvieCurrency)
	{
		if (null == strCollateralCurrency || strCollateralCurrency.isEmpty() || null == strGovvieCurrency ||
			strGovvieCurrency.isEmpty())
			return null;

		java.lang.String strCode = strCollateralCurrency + "@#" + strGovvieCurrency;

		return _mapAUCollateralGovvieCorrelation.containsKey (strCode) ?
			_mapAUCollateralGovvieCorrelation.get (strCode) : null;
	}

	@Override public boolean setCollateralGovvieCorrSurface (
		final java.lang.String strCollateralCurrency,
		final java.lang.String strGovvieCurrency,
		final org.drip.quant.function1D.AbstractUnivariate auCorrelation)
	{
		if (null == strCollateralCurrency || strCollateralCurrency.isEmpty() || null == strGovvieCurrency ||
			strGovvieCurrency.isEmpty() || null == auCorrelation)
			return false;

		_mapAUCollateralGovvieCorrelation.put (strCollateralCurrency + "@#" + strGovvieCurrency,
			auCorrelation);

		return true;
	}

	@Override public org.drip.quant.function1D.AbstractUnivariate creditCustomMetricCorrSurface (
		final java.lang.String strCreditCurveName,
		final java.lang.String strCustomMetric)
	{
		if (null == strCreditCurveName || strCreditCurveName.isEmpty() || null == strCustomMetric ||
			strCustomMetric.isEmpty())
			return null;

		java.lang.String strCode = strCreditCurveName + "@#" + strCustomMetric;

		return _mapAUCreditCustomMetricCorrelation.containsKey (strCode) ?
			_mapAUCreditCustomMetricCorrelation.get (strCode) : null;
	}

	@Override public boolean setCreditCustomMetricCorrSurface (
		final java.lang.String strCreditCurveName,
		final java.lang.String strCustomMetric,
		final org.drip.quant.function1D.AbstractUnivariate auCorrelation)
	{
		if (null == strCreditCurveName || strCreditCurveName.isEmpty() || null == strCustomMetric ||
			strCustomMetric.isEmpty() || null == auCorrelation)
			return false;

		_mapAUCreditCustomMetricCorrelation.put (strCreditCurveName + "@#" + strCustomMetric, auCorrelation);

		return true;
	}

	@Override public org.drip.quant.function1D.AbstractUnivariate creditForwardCorrSurface (
		final java.lang.String strCreditCurveName,
		final org.drip.product.params.FloatingRateIndex fri)
	{
		if (null == strCreditCurveName || strCreditCurveName.isEmpty() || null == fri) return null;

		java.lang.String strCode = strCreditCurveName + "@#" + fri.fullyQualifiedName();

		return _mapAUCreditForwardCorrelation.containsKey (strCode) ? _mapAUCreditForwardCorrelation.get
			(strCode) : null;
	}

	@Override public boolean setCreditForwardCorrSurface (
		final java.lang.String strCreditCurveName,
		final org.drip.product.params.FloatingRateIndex fri,
		final org.drip.quant.function1D.AbstractUnivariate auCorrelation)
	{
		if (null == strCreditCurveName || strCreditCurveName.isEmpty() || null == fri || null ==
			auCorrelation)
			return false;

		_mapAUCreditForwardCorrelation.put (strCreditCurveName + "@#" + fri.fullyQualifiedName(),
			auCorrelation);

		return true;
	}

	@Override public org.drip.quant.function1D.AbstractUnivariate creditFundingCorrSurface (
		final java.lang.String strCreditCurveName,
		final java.lang.String strFundingCurrency)
	{
		if (null == strCreditCurveName || strCreditCurveName.isEmpty() || null == strFundingCurrency ||
			strFundingCurrency.isEmpty())
			return null;

		java.lang.String strCode = strCreditCurveName + "@#" + strFundingCurrency;

		return _mapAUCreditFundingCorrelation.containsKey (strCode) ? _mapAUCreditFundingCorrelation.get
			(strCode) : null;
	}

	@Override public boolean setCreditFundingCorrSurface (
		final java.lang.String strCreditCurveName,
		final java.lang.String strFundingCurrency,
		final org.drip.quant.function1D.AbstractUnivariate auCorrelation)
	{
		if (null == strCreditCurveName || strCreditCurveName.isEmpty() || null == strFundingCurrency ||
			strFundingCurrency.isEmpty() || null == auCorrelation)
			return false;

		_mapAUCreditFundingCorrelation.put (strCreditCurveName + "@#" + strFundingCurrency, auCorrelation);

		return true;
	}

	@Override public org.drip.quant.function1D.AbstractUnivariate creditFXCorrSurface (
		final java.lang.String strCreditCurveName,
		final org.drip.product.params.CurrencyPair cp)
	{
		if (null == strCreditCurveName || strCreditCurveName.isEmpty() || null == cp) return null;

		java.lang.String strCode = strCreditCurveName + "@#" + cp.code();

		return _mapAUCreditFXCorrelation.containsKey (strCode) ? _mapAUCreditFXCorrelation.get (strCode) :
			null;
	}

	@Override public boolean setCreditFXCorrSurface (
		final java.lang.String strCreditCurveName,
		final org.drip.product.params.CurrencyPair cp,
		final org.drip.quant.function1D.AbstractUnivariate auCorrelation)
	{
		if (null == strCreditCurveName || strCreditCurveName.isEmpty() || null == cp || null ==
			auCorrelation)
			return false;

		_mapAUCreditFXCorrelation.get (strCreditCurveName + "@#" + cp.code());

		return true;
	}

	@Override public org.drip.quant.function1D.AbstractUnivariate creditGovvieCorrSurface (
		final java.lang.String strCreditCurveName,
		final java.lang.String strGovvieCurrency)
	{
		if (null == strCreditCurveName || strCreditCurveName.isEmpty() || null == strGovvieCurrency ||
			strGovvieCurrency.isEmpty())
			return null;

		java.lang.String strCode = strCreditCurveName + "@#" + strGovvieCurrency;

		return _mapAUCreditGovvieCorrelation.containsKey (strCode) ? _mapAUCreditGovvieCorrelation.get
			(strCode) : null;
	}

	@Override public boolean setCreditGovvieCorrSurface (
		final java.lang.String strCreditCurveName,
		final java.lang.String strGovvieCurrency,
		final org.drip.quant.function1D.AbstractUnivariate auCorrelation)
	{
		if (null == strCreditCurveName || strCreditCurveName.isEmpty() || null == strGovvieCurrency ||
			strGovvieCurrency.isEmpty() || null == auCorrelation)
			return false;

		_mapAUCreditGovvieCorrelation.put (strCreditCurveName + "@#" + strGovvieCurrency, auCorrelation);

		return true;
	}

	@Override public org.drip.quant.function1D.AbstractUnivariate customMetricForwardCorrSurface (
		final java.lang.String strCustomMetric,
		final org.drip.product.params.FloatingRateIndex fri)
	{
		if (null == strCustomMetric || strCustomMetric.isEmpty() || null == fri) return null;

		if (!_mapAUCustomMetricForwardCorrelation.containsKey (strCustomMetric + "@#" +
			fri.fullyQualifiedName()))
			return null;

		return _mapAUCustomMetricForwardCorrelation.get (strCustomMetric + "@#" + fri.fullyQualifiedName());
	}

	@Override public boolean setCustomMetricForwardCorrSurface (
		final java.lang.String strCustomMetric,
		final org.drip.product.params.FloatingRateIndex fri,
		final org.drip.quant.function1D.AbstractUnivariate auCorrelation)
	{
		if (null == strCustomMetric || strCustomMetric.isEmpty() || null == fri || null == auCorrelation)
			return false;

		_mapAUCustomMetricForwardCorrelation.put (strCustomMetric + "@#" + fri.fullyQualifiedName(),
			auCorrelation);

		return true;
	}

	@Override public org.drip.quant.function1D.AbstractUnivariate customMetricFundingCorrSurface (
		final java.lang.String strCustomMetric,
		final java.lang.String strFundingCurrency)
	{
		if (null == strCustomMetric || strCustomMetric.isEmpty() || null == strFundingCurrency ||
			strFundingCurrency.isEmpty())
			return null;

		java.lang.String strCode = strCustomMetric + "@#" + strFundingCurrency;

		if (!_mapAUCustomMetricFundingCorrelation.containsKey (strCode)) return null;

		return _mapAUCustomMetricFundingCorrelation.get (strCode);
	}

	@Override public boolean setCustomMetricFundingCorrSurface (
		final java.lang.String strCustomMetric,
		final java.lang.String strFundingCurrency,
		final org.drip.quant.function1D.AbstractUnivariate auCorrelation)
	{
		if (null == strCustomMetric || strCustomMetric.isEmpty() || null == strFundingCurrency ||
			strFundingCurrency.isEmpty())
			return false;

		_mapAUCustomMetricFundingCorrelation.put (strCustomMetric + "@#" + strFundingCurrency,
			auCorrelation);

		return true;
	}

	@Override public org.drip.quant.function1D.AbstractUnivariate customMetricFXCorrSurface (
		final java.lang.String strCustomMetric,
		final org.drip.product.params.CurrencyPair cp)
	{
		if (null == strCustomMetric || strCustomMetric.isEmpty() || null == cp) return null;

		java.lang.String strCode = strCustomMetric + "@#" + cp.code();

		return _mapAUCustomMetricFXCorrelation.containsKey (strCode) ? _mapAUCustomMetricFXCorrelation.get
			(strCode) : null;
	}

	@Override public boolean setCustomMetricFXCorrSurface (
		final java.lang.String strCustomMetric,
		final org.drip.product.params.CurrencyPair cp,
		final org.drip.quant.function1D.AbstractUnivariate auCorrelation)
	{
		if (null == strCustomMetric || strCustomMetric.isEmpty() || null == cp || null == auCorrelation)
			return false;

		_mapAUCustomMetricFXCorrelation.get (strCustomMetric + "@#" + cp.code());

		return true;
	}

	@Override public org.drip.quant.function1D.AbstractUnivariate customMetricGovvieCorrSurface (
		final java.lang.String strCustomMetric,
		final java.lang.String strGovvieCurrency)
	{
		if (null == strCustomMetric || strCustomMetric.isEmpty() || null == strGovvieCurrency ||
			strGovvieCurrency.isEmpty())
			return null;

		java.lang.String strCode = strCustomMetric + "@#" + strGovvieCurrency;

		return _mapAUCustomMetricGovvieCorrelation.containsKey (strCode) ?
			_mapAUCustomMetricGovvieCorrelation.get (strCode) : null;
	}

	@Override public boolean setCustomMetricGovvieCorrSurface (
		final java.lang.String strCustomMetric,
		final java.lang.String strGovvieCurrency,
		final org.drip.quant.function1D.AbstractUnivariate auCorrelation)
	{
		if (null == strCustomMetric || strCustomMetric.isEmpty() || null == strGovvieCurrency ||
			strGovvieCurrency.isEmpty())
			return false;

		_mapAUCustomMetricGovvieCorrelation.put (strCustomMetric + "@#" + strGovvieCurrency, auCorrelation);

		return true;
	}

	@Override public org.drip.quant.function1D.AbstractUnivariate forwardFundingCorrSurface (
		final org.drip.product.params.FloatingRateIndex fri,
		final java.lang.String strFundingCurrency)
	{
		if (null == fri || null == strFundingCurrency || strFundingCurrency.isEmpty()) return null;

		java.lang.String strCode = fri.fullyQualifiedName() + "@#" + strFundingCurrency;

		if (!_mapAUForwardFundingCorrelation.containsKey (strCode)) return null;

		return _mapAUForwardFundingCorrelation.get (strCode);
	}

	@Override public boolean setForwardFundingCorrSurface (
		final org.drip.product.params.FloatingRateIndex fri,
		final java.lang.String strFundingCurrency,
		final org.drip.quant.function1D.AbstractUnivariate auCorrelation)
	{
		if (null == fri || null == strFundingCurrency || strFundingCurrency.isEmpty() || null ==
			auCorrelation)
			return false;

		_mapAUForwardFundingCorrelation.put (fri.fullyQualifiedName() + "@#" + strFundingCurrency,
			auCorrelation);

		return true;
	}

	@Override public org.drip.quant.function1D.AbstractUnivariate forwardFXCorrSurface (
		final org.drip.product.params.FloatingRateIndex fri,
		final org.drip.product.params.CurrencyPair cp)
	{
		if (null == fri || null == cp) return null;

		java.lang.String strCode = fri.fullyQualifiedName() + "@#" + cp.code();

		return _mapAUForwardFXCorrelation.containsKey (strCode) ? _mapAUForwardFXCorrelation.get (strCode) :
			null;
	}

	@Override public boolean setForwardFXCorrSurface (
		final org.drip.product.params.FloatingRateIndex fri,
		final org.drip.product.params.CurrencyPair cp,
		final org.drip.quant.function1D.AbstractUnivariate auCorrelation)
	{
		if (null == fri || null == cp || null == auCorrelation) return false;

		_mapAUForwardFXCorrelation.get (fri.fullyQualifiedName() + "@#" + cp.code());

		return true;
	}

	@Override public org.drip.quant.function1D.AbstractUnivariate forwardGovvieCorrSurface (
		final org.drip.product.params.FloatingRateIndex fri,
		final java.lang.String strGovvieCurrency)
	{
		if (null == fri || null == strGovvieCurrency || strGovvieCurrency.isEmpty()) return null;

		java.lang.String strCode = fri.fullyQualifiedName() + "@#" + strGovvieCurrency;

		return _mapAUForwardGovvieCorrelation.containsKey (strCode) ? _mapAUForwardGovvieCorrelation.get
			(strCode) : null;
	}

	@Override public boolean setForwardGovvieCorrSurface (
		final org.drip.product.params.FloatingRateIndex fri,
		final java.lang.String strGovvieCurrency,
		final org.drip.quant.function1D.AbstractUnivariate auCorrelation)
	{
		if (null == fri || null == strGovvieCurrency || strGovvieCurrency.isEmpty() || null == auCorrelation)
			return false;

		_mapAUForwardGovvieCorrelation.put (fri.fullyQualifiedName() + "@#" + strGovvieCurrency,
			auCorrelation);

		return true;
	}

	@Override public org.drip.quant.function1D.AbstractUnivariate fundingFXCorrSurface (
		final java.lang.String strFundingCurrency,
		final org.drip.product.params.CurrencyPair cp)
	{
		if (null == strFundingCurrency || strFundingCurrency.isEmpty() || null == cp) return null;

		java.lang.String strCode = strFundingCurrency + "@#" + cp.code();

		return _mapAUFundingFXCorrelation.containsKey (strCode) ? _mapAUFundingFXCorrelation.get (strCode) :
			null;
	}

	@Override public boolean setFundingFXCorrSurface (
		final java.lang.String strFundingCurrency,
		final org.drip.product.params.CurrencyPair cp,
		final org.drip.quant.function1D.AbstractUnivariate auCorrelation)
	{
		if (null == strFundingCurrency || strFundingCurrency.isEmpty() || null == cp || null ==
			auCorrelation)
			return false;

		_mapAUFundingFXCorrelation.get (strFundingCurrency + "@#" + cp.code());

		return true;
	}

	@Override public org.drip.quant.function1D.AbstractUnivariate fundingGovvieCorrSurface (
		final java.lang.String strFundingCurrency,
		final java.lang.String strGovvieCurrency)
	{
		if (null == strFundingCurrency || strFundingCurrency.isEmpty() || null == strGovvieCurrency ||
			strGovvieCurrency.isEmpty())
			return null;

		java.lang.String strCode = strFundingCurrency + "@#" + strGovvieCurrency;

		return _mapAUFundingGovvieCorrelation.containsKey (strCode) ? _mapAUFundingGovvieCorrelation.get
			(strCode) : null;
	}

	@Override public boolean setFundingGovvieCorrSurface (
		final java.lang.String strFundingCurrency,
		final java.lang.String strGovvieCurrency,
		final org.drip.quant.function1D.AbstractUnivariate auCorrelation)
	{
		if (null == strFundingCurrency || strFundingCurrency.isEmpty() || null == strGovvieCurrency ||
			strGovvieCurrency.isEmpty() || null == auCorrelation)
			return false;

		_mapAUFundingGovvieCorrelation.put (strFundingCurrency + "@#" + strGovvieCurrency, auCorrelation);

		return true;
	}

	@Override public org.drip.quant.function1D.AbstractUnivariate fundingGovvieCorrSurface (
		final org.drip.product.params.CurrencyPair cp,
		final java.lang.String strGovvieCurrency)
	{
		if (null == cp || null == strGovvieCurrency || strGovvieCurrency.isEmpty()) return null;

		java.lang.String strCode = cp.code() + "@#" + strGovvieCurrency;

		return _mapAUFXGovvieCorrelation.containsKey (strCode) ? _mapAUFXGovvieCorrelation.get (strCode) :
			null;
	}

	@Override public boolean setFundingGovvieCorrSurface (
		final org.drip.product.params.CurrencyPair cp,
		final java.lang.String strGovvieCurrency,
		final org.drip.quant.function1D.AbstractUnivariate auCorrelation)
	{
		if (null == cp || null == strGovvieCurrency || strGovvieCurrency.isEmpty() || null == auCorrelation)
			return false;

		_mapAUFundingGovvieCorrelation.put (cp.code() + "@#" + strGovvieCurrency, auCorrelation);

		return true;
	}

	@Override public org.drip.param.definition.ComponentQuote componentQuote (
		final java.lang.String strComponentCode)
	{
		if (null == strComponentCode || strComponentCode.isEmpty() || !_mapComponentQuote.containsKey
			(strComponentCode))
			return null;

		return _mapComponentQuote.get (strComponentCode);
	}

	@Override public boolean setComponentQuote (
		final java.lang.String strComponentCode,
		final org.drip.param.definition.ComponentQuote compQuote)
	{
		if (null == strComponentCode || strComponentCode.isEmpty() || null == compQuote) return false;

		_mapComponentQuote.put (strComponentCode, compQuote);

		return true;
	}

	@Override public
		org.drip.analytics.support.CaseInsensitiveTreeMap<org.drip.param.definition.ComponentQuote>
			componentQuoteMap()
	{
		return _mapComponentQuote;
	}

	@Override public boolean setComponentQuoteMap (
		final org.drip.analytics.support.CaseInsensitiveTreeMap<org.drip.param.definition.ComponentQuote>
			mapComponentQuote)
	{
		_mapComponentQuote = mapComponentQuote;
		return true;
	}

	@Override public java.util.Map<org.drip.analytics.date.JulianDate,
		org.drip.analytics.support.CaseInsensitiveTreeMap<java.lang.Double>> fixings()
	{
		return _mmFixings;
	}

	@Override public boolean setFixings (
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

		if (null == _mapComponentQuote || 0 == _mapComponentQuote.size())
			sb.append (org.drip.service.stream.Serializer.NULL_SER_STRING);
		else {
			boolean bFirstEntry = true;

			java.lang.StringBuffer sbMapTSYQuotes = new java.lang.StringBuffer();

			for (java.util.Map.Entry<java.lang.String, org.drip.param.definition.ComponentQuote> me :
				_mapComponentQuote.entrySet()) {
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
			return new ComponentMarketParamSet (ab);
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

		org.drip.param.market.ComponentMultiMeasureQuote cq = new
			org.drip.param.market.ComponentMultiMeasureQuote();

		cq.addQuote ("Price", new org.drip.param.market.MultiSidedQuote ("ASK", 103., 100000.), false);

		cq.setMarketQuote ("SpreadToTsyBmk", new org.drip.param.market.MultiSidedQuote ("MID", 210.,
			100000.));

		org.drip.analytics.support.CaseInsensitiveTreeMap<org.drip.param.definition.ComponentQuote>
			mapTSYQuotes = new
				org.drip.analytics.support.CaseInsensitiveTreeMap<org.drip.param.definition.ComponentQuote>();

		mapTSYQuotes.put ("TSY2ON", cq);

		org.drip.analytics.support.CaseInsensitiveTreeMap<java.lang.Double> mIndexFixings = new
			org.drip.analytics.support.CaseInsensitiveTreeMap<java.lang.Double>();

		mIndexFixings.put ("USD-LIBOR-6M", 0.0042);

		java.util.Map<org.drip.analytics.date.JulianDate,
			org.drip.analytics.support.CaseInsensitiveTreeMap<java.lang.Double>> mmFixings = new
				java.util.HashMap<org.drip.analytics.date.JulianDate,
					org.drip.analytics.support.CaseInsensitiveTreeMap<java.lang.Double>>();

		mmFixings.put (org.drip.analytics.date.JulianDate.Today().addDays (2), mIndexFixings);

		org.drip.param.market.ComponentMarketParamSet cmp = new
			org.drip.param.market.ComponentMarketParamSet();

		cmp.setCreditCurve (cc);

		cmp.setGovvieCurve (dcTSY);

		cmp.setComponentQuote ("IRSSWAP", cq);

		cmp.setFundingCurve (dc);

		cmp.setFixings (mmFixings);

		cmp.setComponentQuoteMap (mapTSYQuotes);

		byte[] abCMP = cmp.serialize();

		System.out.println (new java.lang.String (abCMP));

		ComponentMarketParamSet cmpDeser = new ComponentMarketParamSet (abCMP);

		System.out.println (new java.lang.String (cmpDeser.serialize()));
	}
}
