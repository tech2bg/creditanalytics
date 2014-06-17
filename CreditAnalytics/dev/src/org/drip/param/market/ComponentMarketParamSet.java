
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
	private org.drip.analytics.definition.CreditCurve _cc = null;
	private org.drip.analytics.rates.DiscountCurve _dcTSY = null;
	private org.drip.analytics.rates.DiscountCurve _dcFunding = null;
	private org.drip.param.definition.ComponentQuote _compQuote = null;
	private org.drip.analytics.support.CaseInsensitiveTreeMap<org.drip.param.definition.ComponentQuote>
		_mTSYQuotes = null;
	private java.util.Map<org.drip.analytics.date.JulianDate,
		org.drip.analytics.support.CaseInsensitiveTreeMap<java.lang.Double>> _mmFixings = null;

	private org.drip.analytics.support.CaseInsensitiveTreeMap<org.drip.quant.function1D.AbstractUnivariate>
		_mapAUFX = new
			org.drip.analytics.support.CaseInsensitiveTreeMap<org.drip.quant.function1D.AbstractUnivariate>();

	private org.drip.analytics.support.CaseInsensitiveTreeMap<org.drip.analytics.rates.ForwardCurve>
		_mapForward = new
			org.drip.analytics.support.CaseInsensitiveTreeMap<org.drip.analytics.rates.ForwardCurve>();

	private org.drip.analytics.support.CaseInsensitiveTreeMap<org.drip.quant.function1D.AbstractUnivariate>
		_mapAUCollateralVolatity = new
			org.drip.analytics.support.CaseInsensitiveTreeMap<org.drip.quant.function1D.AbstractUnivariate>();

	private org.drip.analytics.support.CaseInsensitiveTreeMap<org.drip.quant.function1D.AbstractUnivariate>
		_mapAUForwardVolatity = new
			org.drip.analytics.support.CaseInsensitiveTreeMap<org.drip.quant.function1D.AbstractUnivariate>();

	private org.drip.analytics.support.CaseInsensitiveTreeMap<org.drip.quant.function1D.AbstractUnivariate>
		_mapAUFundingVolatity = new
			org.drip.analytics.support.CaseInsensitiveTreeMap<org.drip.quant.function1D.AbstractUnivariate>();

	private org.drip.analytics.support.CaseInsensitiveTreeMap<org.drip.quant.function1D.AbstractUnivariate>
		_mapAUCollateralCollateralCorrelation = new
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
		_mapAUCustomMetricCustomMetricCorrelation = new
			org.drip.analytics.support.CaseInsensitiveTreeMap<org.drip.quant.function1D.AbstractUnivariate>();

	private org.drip.analytics.support.CaseInsensitiveTreeMap<org.drip.quant.function1D.AbstractUnivariate>
		_mapAUCustomMetricForwardCorrelation = new
			org.drip.analytics.support.CaseInsensitiveTreeMap<org.drip.quant.function1D.AbstractUnivariate>();

	private org.drip.analytics.support.CaseInsensitiveTreeMap<org.drip.quant.function1D.AbstractUnivariate>
		_mapAUCustomMetricFundingCorrelation = new
			org.drip.analytics.support.CaseInsensitiveTreeMap<org.drip.quant.function1D.AbstractUnivariate>();

	private org.drip.analytics.support.CaseInsensitiveTreeMap<org.drip.quant.function1D.AbstractUnivariate>
		_mapAUForwardForwardCorrelation = new
			org.drip.analytics.support.CaseInsensitiveTreeMap<org.drip.quant.function1D.AbstractUnivariate>();

	private org.drip.analytics.support.CaseInsensitiveTreeMap<org.drip.quant.function1D.AbstractUnivariate>
		_mapAUForwardFundingCorrelation = new
			org.drip.analytics.support.CaseInsensitiveTreeMap<org.drip.quant.function1D.AbstractUnivariate>();

	private org.drip.analytics.support.CaseInsensitiveTreeMap<org.drip.quant.function1D.AbstractUnivariate>
		_mapAUFundingFundingCorrelation = new
			org.drip.analytics.support.CaseInsensitiveTreeMap<org.drip.quant.function1D.AbstractUnivariate>();

	private
		org.drip.analytics.support.CaseInsensitiveTreeMap<java.util.Map<org.drip.analytics.date.JulianDate,
			org.drip.quant.function1D.AbstractUnivariate>> _mapCustomMetricVolatility = new
				org.drip.analytics.support.CaseInsensitiveTreeMap<java.util.Map<org.drip.analytics.date.JulianDate,
		org.drip.quant.function1D.AbstractUnivariate>>();

	private
		org.drip.analytics.support.CaseInsensitiveTreeMap<org.drip.analytics.support.CaseInsensitiveTreeMap<org.drip.analytics.rates.DiscountCurve>>
			_mapPayCurrencyForeignCollateralDC = new
				org.drip.analytics.support.CaseInsensitiveTreeMap<org.drip.analytics.support.CaseInsensitiveTreeMap<org.drip.analytics.rates.DiscountCurve>>();

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
	 * @throws java.lang.Exception Thrown if CreditCurve cannot be properly de-serialized
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

		if (null == astrField || 8 > astrField.length)
			throw new java.lang.Exception ("ComponentMarketParamSet de-serializer: Invalid reqd field set");

		// double dblVersion = new java.lang.Double (astrField[0]);

		if (null == astrField[1] || astrField[1].isEmpty())
			throw new java.lang.Exception
				("ComponentMarketParamSet de-serializer: Cannot locate credit curve");

		if (!org.drip.service.stream.Serializer.NULL_SER_STRING.equalsIgnoreCase (astrField[1]))
			_cc = org.drip.state.creator.CreditCurveBuilder.FromByteArray (astrField[1].getBytes());

		if (null == astrField[2] || astrField[2].isEmpty())
			throw new java.lang.Exception
				("ComponentMarketParamSet de-serializer: Cannot locate discount curve");

		if (!org.drip.service.stream.Serializer.NULL_SER_STRING.equalsIgnoreCase (astrField[2]))
			_dcFunding = org.drip.state.creator.DiscountCurveBuilder.FromByteArray (astrField[2].getBytes(),
				org.drip.state.creator.DiscountCurveBuilder.BOOTSTRAP_MODE_CONSTANT_FORWARD);

		if (null == astrField[3] || astrField[3].isEmpty())
			throw new java.lang.Exception
				("ComponentMarketParamSet de-serializer: Cannot locate forward curve");

		if (!org.drip.service.stream.Serializer.NULL_SER_STRING.equalsIgnoreCase (astrField[3]))
			_mapForward = null;

		if (null == astrField[4] || astrField[4].isEmpty())
			throw new java.lang.Exception
				("ComponentMarketParamSet de-serializer: Cannot locate TSY discount curve");

		if (!org.drip.service.stream.Serializer.NULL_SER_STRING.equalsIgnoreCase (astrField[4]))
			_dcTSY = org.drip.state.creator.DiscountCurveBuilder.FromByteArray (astrField[4].getBytes(),
				org.drip.state.creator.DiscountCurveBuilder.BOOTSTRAP_MODE_CONSTANT_FORWARD);

		if (null == astrField[5] || astrField[5].isEmpty())
			throw new java.lang.Exception
				("ComponentMarketParamSet de-serializer: Cannot locate component quote");

		if (!org.drip.service.stream.Serializer.NULL_SER_STRING.equalsIgnoreCase (astrField[5]))
			_compQuote = new org.drip.param.market.ComponentMultiMeasureQuote (astrField[5].getBytes());

		if (null == astrField[6] || astrField[6].isEmpty())
			throw new java.lang.Exception ("ComponentMarketParamSet de-serializer: Cannot locate fixings");

		if (!org.drip.service.stream.Serializer.NULL_SER_STRING.equalsIgnoreCase (astrField[6])) {
			java.lang.String[] astrRecord = org.drip.quant.common.StringUtil.Split (astrField[6],
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

		if (null == astrField[7] || astrField[7].isEmpty())
			throw new java.lang.Exception
				("ComponentMarketParamSet de-serializer: Cannot locate TSY quotes");

		if (!org.drip.service.stream.Serializer.NULL_SER_STRING.equalsIgnoreCase (astrField[7])) {
			java.lang.String[] astrRecord = org.drip.quant.common.StringUtil.Split (astrField[7],
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

					if (null == _mTSYQuotes)
						_mTSYQuotes = new
							org.drip.analytics.support.CaseInsensitiveTreeMap<org.drip.param.definition.ComponentQuote>();

					_mTSYQuotes.put (astrKVPair[0], new org.drip.param.market.ComponentMultiMeasureQuote
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

	@Override public org.drip.analytics.rates.DiscountCurve fundingCurve()
	{
		return _dcFunding;
	}

	@Override public boolean setFundingCurve (
		final org.drip.analytics.rates.DiscountCurve dcFunding)
	{
		if (null == dcFunding) return false;

		_dcFunding = dcFunding;
		return true;
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

	@Override public org.drip.analytics.definition.CreditCurve creditCurve()
	{
		return _cc;
	}

	@Override public boolean setCreditCurve (
		final org.drip.analytics.definition.CreditCurve cc)
	{
		if (null == cc) return false;

		_cc = cc;
		return true;
	}

	@Override public org.drip.analytics.rates.ForwardCurve forwardCurve (
		final org.drip.product.params.FloatingRateIndex fri)
	{
		if (null == fri) return null;

		java.lang.String strFullyQualifiedName = fri.fullyQualifiedName();

		return _mapForward.containsKey (strFullyQualifiedName) ? _mapForward.get (strFullyQualifiedName) :
			null;
	}

	@Override public boolean setForwardCurve (
		final org.drip.analytics.rates.ForwardCurve fc)
	{
		if (null == fc) return false;

		_mapForward.put (fc.index().fullyQualifiedName(), fc);

		return true;
	}

	@Override public org.drip.analytics.rates.DiscountCurve govvieFundingCurve()
	{
		return _dcTSY;
	}

	@Override public boolean setGovvieFundingCurve (
		final org.drip.analytics.rates.DiscountCurve dcTSY)
	{
		if (null == dcTSY) return false;

		_dcTSY = dcTSY;
		return true;
	}

	@Override public org.drip.param.definition.ComponentQuote componentQuote()
	{
		return _compQuote;
	}

	@Override public boolean setComponentQuote (
		final org.drip.param.definition.ComponentQuote compQuote)
	{
		_compQuote = compQuote;
		return true;
	}

	@Override public
		org.drip.analytics.support.CaseInsensitiveTreeMap<org.drip.param.definition.ComponentQuote>
			benchmarkTSYQuotes()
	{
		return _mTSYQuotes;
	}

	@Override public boolean setBenchmarkTSYQuotes (
		final org.drip.analytics.support.CaseInsensitiveTreeMap<org.drip.param.definition.ComponentQuote>
			mTSYQuotes)
	{
		_mTSYQuotes = mTSYQuotes;
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

	@Override public org.drip.quant.function1D.AbstractUnivariate collateralCurveVolSurface (
		final java.lang.String strCurrency)
	{
		if (null == strCurrency || strCurrency.isEmpty() || !_mapAUCollateralVolatity.containsKey
			(strCurrency))
			return null;

		return _mapAUCollateralVolatity.get (strCurrency);
	}

	@Override public boolean setCollateralCurveVolSurface (
		final java.lang.String strCurrency,
		final org.drip.quant.function1D.AbstractUnivariate auVolatility)
	{
		if (null == strCurrency || strCurrency.isEmpty() || null == auVolatility) return false;

		_mapAUCollateralVolatity.put (strCurrency, auVolatility);

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

		return _mapAUForwardVolatity.containsKey (strFRI) ? _mapAUCollateralVolatity.get (strFRI) : null;
	}

	@Override public boolean setForwardCurveVolSurface (
		final org.drip.product.params.FloatingRateIndex fri,
		final org.drip.quant.function1D.AbstractUnivariate auVolatility)
	{
		if (null == fri || null == auVolatility) return false;

		_mapAUCollateralVolatity.put (fri.fullyQualifiedName(), auVolatility);

		return true;
	}

	@Override public org.drip.quant.function1D.AbstractUnivariate fundingCurveVolSurface (
		final java.lang.String strCurrency)
	{
		if (null == strCurrency || strCurrency.isEmpty() || !_mapAUFundingVolatity.containsKey (strCurrency))
			return null;

		return _mapAUFundingVolatity.get (strCurrency);
	}

	@Override public boolean setFundingCurveVolSurface (
		final java.lang.String strCurrency,
		final org.drip.quant.function1D.AbstractUnivariate auVolatility)
	{
		if (null == strCurrency || strCurrency.isEmpty() || null == auVolatility) return false;

		_mapAUFundingVolatity.put (strCurrency, auVolatility);

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

	@Override public org.drip.quant.function1D.AbstractUnivariate fxCurve (
		final org.drip.product.params.CurrencyPair cp)
	{
		if (null == cp) return null;

		java.lang.String strFXCode = cp.getCode();

		return _mapAUFX.containsKey (strFXCode) ? _mapAUFX.get (strFXCode) : null;
	}

	@Override public boolean setFXCurve (
		final org.drip.product.params.CurrencyPair cp,
		final org.drip.quant.function1D.AbstractUnivariate auFX)
	{
		if (null == cp || null == auFX) return false;

		_mapAUFX.put (cp.getCode(), auFX);

		try {
			_mapAUFX.put (cp.inverseCode(), new org.drip.quant.function1D.UnivariateReciprocal (auFX));
		} catch (java.lang.Exception e) {
			e.printStackTrace();

			return false;
		}

		return true;
	}

	@Override public byte[] serialize()
	{
		java.lang.StringBuffer sb = new java.lang.StringBuffer();

		sb.append (org.drip.service.stream.Serializer.VERSION + fieldDelimiter());

		if (null == _cc)
			sb.append (org.drip.service.stream.Serializer.NULL_SER_STRING + fieldDelimiter());
		else
			sb.append (new java.lang.String (_cc.serialize()) + fieldDelimiter());

		if (null == _dcFunding)
			sb.append (org.drip.service.stream.Serializer.NULL_SER_STRING + fieldDelimiter());
		else
			sb.append (new java.lang.String (_dcFunding.serialize()) + fieldDelimiter());

		sb.append (org.drip.service.stream.Serializer.NULL_SER_STRING + fieldDelimiter());

		if (null == _dcTSY)
			sb.append (org.drip.service.stream.Serializer.NULL_SER_STRING + fieldDelimiter());
		else
			sb.append (new java.lang.String (_dcTSY.serialize()) + fieldDelimiter());

		if (null == _compQuote)
			sb.append (org.drip.service.stream.Serializer.NULL_SER_STRING + fieldDelimiter());
		else
			sb.append (new java.lang.String (_compQuote.serialize()) + fieldDelimiter());

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

		if (null == _mTSYQuotes || 0 == _mTSYQuotes.size())
			sb.append (org.drip.service.stream.Serializer.NULL_SER_STRING);
		else {
			boolean bFirstEntry = true;

			java.lang.StringBuffer sbMapTSYQuotes = new java.lang.StringBuffer();

			for (java.util.Map.Entry<java.lang.String, org.drip.param.definition.ComponentQuote> me :
				_mTSYQuotes.entrySet()) {
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

		cmp.setGovvieFundingCurve (dcTSY);

		cmp.setComponentQuote (cq);

		cmp.setFundingCurve (dc);

		cmp.setFixings (mmFixings);

		cmp.setBenchmarkTSYQuotes (mapTSYQuotes);

		byte[] abCMP = cmp.serialize();

		System.out.println (new java.lang.String (abCMP));

		ComponentMarketParamSet cmpDeser = new ComponentMarketParamSet (abCMP);

		System.out.println (new java.lang.String (cmpDeser.serialize()));
	}
}
