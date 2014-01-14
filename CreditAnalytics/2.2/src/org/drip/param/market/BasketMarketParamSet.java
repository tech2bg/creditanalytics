
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
 * This file is part of CreditAnalytics, a free-software/open-source library for fixed income analysts and
 * 		developers - http://www.credit-trader.org
 * 
 * CreditAnalytics is a free, full featured, fixed income credit analytics library, developed with a special
 * 		focus towards the needs of the bonds and credit products community.
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
 * BasketMarketParamSet provides an implementation of BasketMarketParamsRef for a specific scenario. It
 *  contains maps holding named discount curves, named forward curves, named credit curves, named component
 *  quotes, and fixings object. Further, BasketMarketParamSet implements the component market parameters
 *  corresponding to a particulat reference.
 *
 * @author Lakshmi Krishnamurthy
 */

public class BasketMarketParamSet extends org.drip.param.definition.BasketMarketParams {
	private org.drip.analytics.support.CaseInsensitiveTreeMap<org.drip.analytics.rates.DiscountCurve>
		_mapDC = null;
	private org.drip.analytics.support.CaseInsensitiveTreeMap<org.drip.analytics.rates.ForwardCurve>
		_mapFC = null;
	private org.drip.analytics.support.CaseInsensitiveTreeMap<org.drip.analytics.definition.CreditCurve>
		_mapCC = null;
	private java.util.Map<org.drip.analytics.date.JulianDate,
		org.drip.analytics.support.CaseInsensitiveTreeMap<java.lang.Double>> _mmFixings = null;

	private org.drip.analytics.support.CaseInsensitiveTreeMap<org.drip.param.definition.ComponentQuote>
		_mapCQComp = new
			org.drip.analytics.support.CaseInsensitiveTreeMap<org.drip.param.definition.ComponentQuote>();

	/**
	 * Construct the BasketMarketParamSet object from the map of discount curve, the map of forward curve,
	 *  the map of credit curve, a double map of date/rate index and fixings, and a map of the component
	 *  quotes.
	 * 
	 * @param mapDC Map of discount curve
	 * @param mapFC Map of Forward curve
	 * @param mapCC Map of Credit curve
	 * @param mapCQComp Map of component quotes
	 * @param mmFixings Double map of date/rate index and fixings
	 */

	public BasketMarketParamSet (
		final org.drip.analytics.support.CaseInsensitiveTreeMap<org.drip.analytics.rates.DiscountCurve>
			mapDC,
		final org.drip.analytics.support.CaseInsensitiveTreeMap<org.drip.analytics.rates.ForwardCurve>
			mapFC,
		final org.drip.analytics.support.CaseInsensitiveTreeMap<org.drip.analytics.definition.CreditCurve>
			mapCC,
		final org.drip.analytics.support.CaseInsensitiveTreeMap<org.drip.param.definition.ComponentQuote>
			mapCQComp,
		final java.util.Map<org.drip.analytics.date.JulianDate,
			org.drip.analytics.support.CaseInsensitiveTreeMap<java.lang.Double>> mmFixings)
	{
		if (null == (_mapDC = mapDC))
			_mapDC = new
				org.drip.analytics.support.CaseInsensitiveTreeMap<org.drip.analytics.rates.DiscountCurve>();

		if (null == (_mapFC = mapFC))
			_mapFC = new
				org.drip.analytics.support.CaseInsensitiveTreeMap<org.drip.analytics.rates.ForwardCurve>();

		if (null == (_mapCC = mapCC))
			_mapCC = new
				org.drip.analytics.support.CaseInsensitiveTreeMap<org.drip.analytics.definition.CreditCurve>();

		if (null == (_mapCQComp = mapCQComp))
			_mapCQComp = new
				org.drip.analytics.support.CaseInsensitiveTreeMap<org.drip.param.definition.ComponentQuote>();

		_mmFixings = mmFixings;
	}

	/**
	 * BasketMarketParamSet de-serialization from input byte array
	 * 
	 * @param ab Byte Array
	 * 
	 * @throws java.lang.Exception Thrown if BasketMarketParamSet cannot be properly de-serialized
	 */

	public BasketMarketParamSet (
		final byte[] ab)
		throws java.lang.Exception
	{
		if (null == ab || 0 == ab.length)
			throw new java.lang.Exception ("BasketMarketParamSet de-serializer: Invalid input Byte array");

		java.lang.String strRawString = new java.lang.String (ab);

		if (null == strRawString || strRawString.isEmpty())
			throw new java.lang.Exception ("BasketMarketParamSet de-serializer: Empty state");

		java.lang.String strSerializedBasketMarketParams = strRawString.substring (0, strRawString.indexOf
			(getObjectTrailer()));

		if (null == strSerializedBasketMarketParams || strSerializedBasketMarketParams.isEmpty())
			throw new java.lang.Exception ("BasketMarketParamSet de-serializer: Cannot locate state");

		java.lang.String[] astrField = org.drip.quant.common.StringUtil.Split
			(strSerializedBasketMarketParams, getFieldDelimiter());

		if (null == astrField || 6 > astrField.length)
			throw new java.lang.Exception ("BasketMarketParamSet de-serializer: Invalid reqd field set");

		// double dblVersion = new java.lang.Double (astrField[0]);

		if (null == astrField[1] || astrField[1].isEmpty())
			throw new java.lang.Exception
				("BasketMarketParamSet de-serializer: Cannot locate credit curve map");

		if (!org.drip.service.stream.Serializer.NULL_SER_STRING.equalsIgnoreCase (astrField[1])) {
			java.lang.String[] astrRecord = org.drip.quant.common.StringUtil.Split (astrField[1],
				getCollectionRecordDelimiter());

			if (null != astrRecord && 0 != astrRecord.length) {
				for (int i = 0; i < astrRecord.length; ++i) {
					if (null == astrRecord[i] || astrRecord[i].isEmpty() ||
						org.drip.service.stream.Serializer.NULL_SER_STRING.equalsIgnoreCase (astrRecord[i]))
						continue;

					java.lang.String[] astrKVPair = org.drip.quant.common.StringUtil.Split (astrRecord[i],
						getCollectionKeyValueDelimiter());
				
					if (null == astrKVPair || 2 != astrKVPair.length || null == astrKVPair[0] ||
						astrKVPair[0].isEmpty() ||
							org.drip.service.stream.Serializer.NULL_SER_STRING.equalsIgnoreCase
								(astrKVPair[0]) || null == astrKVPair[1] || astrKVPair[1].isEmpty() ||
									org.drip.service.stream.Serializer.NULL_SER_STRING.equalsIgnoreCase
										(astrKVPair[1]))
						continue;

					org.drip.analytics.definition.CreditCurve cc =
						org.drip.state.creator.CreditCurveBuilder.FromByteArray
							(astrKVPair[1].getBytes());

					if (null != cc) {
						if (null == _mapCC)
							_mapCC = new
								org.drip.analytics.support.CaseInsensitiveTreeMap<org.drip.analytics.definition.CreditCurve>();

						_mapCC.put (astrKVPair[0], cc);
					}
				}
			}
		}

		if (null == astrField[2] || astrField[2].isEmpty())
			throw new java.lang.Exception
				("BasketMarketParamSet de-serializer: Cannot locate discount curve map");

		if (!org.drip.service.stream.Serializer.NULL_SER_STRING.equalsIgnoreCase (astrField[2])) {
			java.lang.String[] astrRecord = org.drip.quant.common.StringUtil.Split (astrField[2],
				getCollectionRecordDelimiter());

			if (null != astrRecord && 0 != astrRecord.length) {
				for (int i = 0; i < astrRecord.length; ++i) {
					if (null == astrRecord[i] || astrRecord[i].isEmpty() ||
						org.drip.service.stream.Serializer.NULL_SER_STRING.equalsIgnoreCase (astrRecord[i]))
						continue;

					java.lang.String[] astrKVPair = org.drip.quant.common.StringUtil.Split (astrRecord[i],
						getCollectionKeyValueDelimiter());
				
					if (null == astrKVPair || 2 != astrKVPair.length || null == astrKVPair[0] ||
						astrKVPair[0].isEmpty() ||
							org.drip.service.stream.Serializer.NULL_SER_STRING.equalsIgnoreCase
								(astrKVPair[0]) || null == astrKVPair[1] || astrKVPair[1].isEmpty() ||
									org.drip.service.stream.Serializer.NULL_SER_STRING.equalsIgnoreCase
										(astrKVPair[1]))
						continue;

					org.drip.analytics.rates.DiscountCurve dc =
						org.drip.state.creator.DiscountCurveBuilder.FromByteArray
							(astrKVPair[1].getBytes(),
								org.drip.state.creator.DiscountCurveBuilder.BOOTSTRAP_MODE_CONSTANT_FORWARD);

					if (null != dc) {
						if (null == _mapDC)
							_mapDC = new
								org.drip.analytics.support.CaseInsensitiveTreeMap<org.drip.analytics.rates.DiscountCurve>();

						_mapDC.put (astrKVPair[0], dc);
					}
				}
			}
		}

		if (null == astrField[3] || astrField[3].isEmpty())
			throw new java.lang.Exception
				("BasketMarketParamSet de-serializer: Cannot locate Forward curve map");

		if (!org.drip.service.stream.Serializer.NULL_SER_STRING.equalsIgnoreCase (astrField[3])) {
			java.lang.String[] astrRecord = org.drip.quant.common.StringUtil.Split (astrField[3],
				getCollectionRecordDelimiter());

			if (null != astrRecord && 0 != astrRecord.length) {
				for (int i = 0; i < astrRecord.length; ++i) {
					if (null == astrRecord[i] || astrRecord[i].isEmpty() ||
						org.drip.service.stream.Serializer.NULL_SER_STRING.equalsIgnoreCase (astrRecord[i]))
						continue;

					java.lang.String[] astrKVPair = org.drip.quant.common.StringUtil.Split (astrRecord[i],
						getCollectionKeyValueDelimiter());
				
					if (null == astrKVPair || 2 != astrKVPair.length || null == astrKVPair[0] ||
						astrKVPair[0].isEmpty() ||
							org.drip.service.stream.Serializer.NULL_SER_STRING.equalsIgnoreCase
								(astrKVPair[0]) || null == astrKVPair[1] || astrKVPair[1].isEmpty() ||
									org.drip.service.stream.Serializer.NULL_SER_STRING.equalsIgnoreCase
										(astrKVPair[1]))
						continue;

					/* org.drip.analytics.rates.ForwardCurve fc = null;

					if (null != fc) {
						if (null == _mapFC)
							_mapFC = new
								org.drip.analytics.support.CaseInsensitiveTreeMap<org.drip.analytics.rates.ForwardCurve>();

						_mapFC.put (astrKVPair[0], fc);
					} */
				}
			}
		}

		if (null == astrField[4] || astrField[4].isEmpty())
			throw new java.lang.Exception ("BasketMarketParamSet de-serializer: Cannot locate fixings");

		if (!org.drip.service.stream.Serializer.NULL_SER_STRING.equalsIgnoreCase (astrField[4])) {
			java.lang.String[] astrRecord = org.drip.quant.common.StringUtil.Split (astrField[4],
				getCollectionRecordDelimiter());

			if (null != astrRecord && 0 != astrRecord.length) {
				for (int i = 0; i < astrRecord.length; ++i) {
					if (null == astrRecord[i] || astrRecord[i].isEmpty()) continue;

					java.lang.String[] astrKVPair = org.drip.quant.common.StringUtil.Split (astrRecord[i],
						getCollectionKeyValueDelimiter());
					
					if (null == astrKVPair || 2 != astrKVPair.length || null == astrKVPair[0] ||
						astrKVPair[0].isEmpty() ||
							org.drip.service.stream.Serializer.NULL_SER_STRING.equalsIgnoreCase
								(astrKVPair[0]) || null == astrKVPair[1] || astrKVPair[1].isEmpty() ||
									org.drip.service.stream.Serializer.NULL_SER_STRING.equalsIgnoreCase
										(astrKVPair[1]))
						continue;

					java.lang.String[] astrKeySet = org.drip.quant.common.StringUtil.Split (astrKVPair[0],
						getCollectionMultiLevelKeyDelimiter());

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

		if (null == astrField[5] || astrField[5].isEmpty())
			throw new java.lang.Exception
				("BasketMarketParamSet de-serializer: Cannot locate component quote map");

		if (!org.drip.service.stream.Serializer.NULL_SER_STRING.equalsIgnoreCase (astrField[5])) {
			java.lang.String[] astrRecord = org.drip.quant.common.StringUtil.Split (astrField[5],
				getCollectionRecordDelimiter());

			if (null != astrRecord && 0 != astrRecord.length) {
				for (int i = 0; i < astrRecord.length; ++i) {
					if (null == astrRecord[i] || astrRecord[i].isEmpty() ||
						org.drip.service.stream.Serializer.NULL_SER_STRING.equalsIgnoreCase (astrRecord[i]))
						continue;

					java.lang.String[] astrKVPair = org.drip.quant.common.StringUtil.Split (astrRecord[i],
						getCollectionKeyValueDelimiter());
				
					if (null == astrKVPair || 2 != astrKVPair.length || null == astrKVPair[0] ||
						astrKVPair[0].isEmpty() ||
							org.drip.service.stream.Serializer.NULL_SER_STRING.equalsIgnoreCase
								(astrKVPair[0]) || null == astrKVPair[1] || astrKVPair[1].isEmpty() ||
									org.drip.service.stream.Serializer.NULL_SER_STRING.equalsIgnoreCase
										(astrKVPair[1]))
						continue;

					org.drip.param.definition.ComponentQuote cq = new
						org.drip.param.market.ComponentMultiMeasureQuote (astrKVPair[1].getBytes());

					if (null != cq) {
						if (null == _mapCQComp)
							_mapCQComp = new
								org.drip.analytics.support.CaseInsensitiveTreeMap<org.drip.param.definition.ComponentQuote>();

						_mapCQComp.put (astrKVPair[0], cq);
					}
				}
			}
		}
	}

	/**
	 * Empty BasketMarketParamSet object
	 */

	public BasketMarketParamSet()
	{
		_mapDC = new
			org.drip.analytics.support.CaseInsensitiveTreeMap<org.drip.analytics.rates.DiscountCurve>();

		_mapFC = new
			org.drip.analytics.support.CaseInsensitiveTreeMap<org.drip.analytics.rates.ForwardCurve>();

		_mapCC = new
			org.drip.analytics.support.CaseInsensitiveTreeMap<org.drip.analytics.definition.CreditCurve>();

		_mapCQComp = new
			org.drip.analytics.support.CaseInsensitiveTreeMap<org.drip.param.definition.ComponentQuote>();
	}

	@Override public boolean addDiscountCurve (
		final java.lang.String strName,
		final org.drip.analytics.rates.DiscountCurve dc)
	{
		if (null == strName || strName.isEmpty() || null == dc) return false;

		_mapDC.put (strName, dc);

		return true;
	}

	@Override public boolean addForwardCurve (
		final java.lang.String strName,
		final org.drip.analytics.rates.ForwardCurve fc)
	{
		if (null == strName || strName.isEmpty() || null == fc) return false;

		_mapFC.put (strName, fc);

		return true;
	}

	@Override public boolean addCreditCurve (
		final java.lang.String strName,
		final org.drip.analytics.definition.CreditCurve cc)
	{
		if (null == strName || strName.isEmpty() || null == cc) return false;

		_mapCC.put (strName, cc);

		return true;
	}

	@Override public org.drip.analytics.rates.DiscountCurve getDiscountCurve (
		final java.lang.String strName)
	{
		if (null == strName || strName.isEmpty()) return null;

		return _mapDC.get (strName);
	}

	@Override public org.drip.analytics.rates.ForwardCurve getForwardCurve (
		final java.lang.String strName)
	{
		if (null == strName || strName.isEmpty()) return null;

		return _mapFC.get (strName);
	}

	@Override public org.drip.analytics.definition.CreditCurve getCreditCurve (
		final java.lang.String strName)
	{
		if (null == strName || strName.isEmpty()) return null;

		return _mapCC.get (strName);
	}

	@Override public boolean addComponentQuote (
		final java.lang.String strName,
		final org.drip.param.definition.ComponentQuote cq)
	{
		_mapCQComp.put (strName, cq);

		return true;
	}

	@Override public org.drip.param.definition.ComponentQuote getComponentQuote (
		final java.lang.String strName)
	{
		if (null == strName || strName.isEmpty()) return null;

		return _mapCQComp.get (strName);
	}

	@Override public org.drip.param.definition.ComponentMarketParams getComponentMarketParams (
		final org.drip.product.definition.ComponentMarketParamRef compRef)
	{
		if (null == compRef) return null;

		return new ComponentMarketParamSet (_mapDC.get (compRef.getIRCurveName()), _mapFC.get
			(compRef.getForwardCurveName()), _mapDC.get (compRef.getTreasuryCurveName()), _mapDC.get
				(compRef.getEDSFCurveName()), _mapCC.get (compRef.getCreditCurveName()), _mapCQComp.get
					(compRef.getComponentName()), _mapCQComp, _mmFixings);
	}

	@Override public java.lang.String getCollectionKeyValueDelimiter()
	{
		return "]";
	}

	@Override public java.lang.String getFieldDelimiter()
	{
		return "[";
	}

	@Override public java.lang.String getObjectTrailer()
	{
		return "~";
	}

	@Override public java.lang.String getCollectionRecordDelimiter()
	{
		return "`";
	}

	@Override public byte[] serialize()
	{
		java.lang.StringBuffer sb = new java.lang.StringBuffer();

		sb.append (VERSION + getFieldDelimiter());

		if (null == _mapCC || null == _mapCC.entrySet())
			sb.append (org.drip.service.stream.Serializer.NULL_SER_STRING + getFieldDelimiter());
		else {
			boolean bFirstEntry = true;

			java.lang.StringBuffer sbMapCC = new java.lang.StringBuffer();

			for (java.util.Map.Entry<java.lang.String, org.drip.analytics.definition.CreditCurve> me :
				_mapCC.entrySet()) {
				if (null == me || null == me.getKey() || me.getKey().isEmpty()) continue;

				if (bFirstEntry)
					bFirstEntry = false;
				else
					sbMapCC.append (getCollectionRecordDelimiter());

				sbMapCC.append (me.getKey() + getCollectionKeyValueDelimiter() + new java.lang.String
					(me.getValue().serialize()));
			}

			if (sbMapCC.toString().isEmpty())
				sb.append (org.drip.service.stream.Serializer.NULL_SER_STRING + getFieldDelimiter());
			else
				sb.append (sbMapCC.toString() + getFieldDelimiter());
		}

		if (null == _mapDC || null == _mapDC.entrySet())
			sb.append (org.drip.service.stream.Serializer.NULL_SER_STRING + getFieldDelimiter());
		else {
			boolean bFirstEntry = true;

			java.lang.StringBuffer sbMapDC = new java.lang.StringBuffer();

			for (java.util.Map.Entry<java.lang.String, org.drip.analytics.rates.DiscountCurve> me :
				_mapDC.entrySet()) {
				if (null == me || null == me.getKey() || me.getKey().isEmpty()) continue;

				if (bFirstEntry)
					bFirstEntry = false;
				else
					sbMapDC.append (getCollectionRecordDelimiter());

				sbMapDC.append (me.getKey() + getCollectionKeyValueDelimiter() + new java.lang.String
					(me.getValue().serialize()));
			}

			if (sbMapDC.toString().isEmpty())
				sb.append (org.drip.service.stream.Serializer.NULL_SER_STRING + getFieldDelimiter());
			else
				sb.append (sbMapDC.toString() + getFieldDelimiter());
		}

		if (null == _mapFC || null == _mapFC.entrySet())
			sb.append (org.drip.service.stream.Serializer.NULL_SER_STRING + getFieldDelimiter());
		else {
			boolean bFirstEntry = true;

			java.lang.StringBuffer sbMapFC = new java.lang.StringBuffer();

			for (java.util.Map.Entry<java.lang.String, org.drip.analytics.rates.ForwardCurve> me :
				_mapFC.entrySet()) {
				if (null == me || null == me.getKey() || me.getKey().isEmpty()) continue;

				if (bFirstEntry)
					bFirstEntry = false;
				else
					sbMapFC.append (getCollectionRecordDelimiter());

				sbMapFC.append (me.getKey() + getCollectionKeyValueDelimiter() + new java.lang.String
					(me.getValue().serialize()));
			}

			if (sbMapFC.toString().isEmpty())
				sb.append (org.drip.service.stream.Serializer.NULL_SER_STRING + getFieldDelimiter());
			else
				sb.append (sbMapFC.toString() + getFieldDelimiter());
		}

		if (null == _mmFixings || null == _mmFixings.entrySet())
			sb.append (org.drip.service.stream.Serializer.NULL_SER_STRING + getFieldDelimiter());
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
						sb.append (getCollectionRecordDelimiter());

					sbFixings.append (meOut.getKey().getJulian() + getCollectionMultiLevelKeyDelimiter() +
						meIn.getKey() + getCollectionKeyValueDelimiter() + meIn.getValue());
				}
			}

			if (sbFixings.toString().isEmpty())
				sb.append (org.drip.service.stream.Serializer.NULL_SER_STRING + getFieldDelimiter());
			else
				sb.append (sbFixings.toString() + getFieldDelimiter());
		}

		if (null == _mapCQComp || null == _mapCQComp.entrySet())
			sb.append (org.drip.service.stream.Serializer.NULL_SER_STRING);
		else {
			boolean bFirstEntry = true;

			java.lang.StringBuffer sbMapCQComp = new java.lang.StringBuffer();

			for (java.util.Map.Entry<java.lang.String, org.drip.param.definition.ComponentQuote> me :
				_mapCQComp.entrySet()) {
				if (null == me || null == me.getKey() || me.getKey().isEmpty()) continue;

				if (bFirstEntry)
					bFirstEntry = false;
				else
					sbMapCQComp.append (getCollectionRecordDelimiter());

				sbMapCQComp.append (me.getKey() + getCollectionKeyValueDelimiter() + new java.lang.String
					(me.getValue().serialize()));
			}

			if (sbMapCQComp.toString().isEmpty())
				sb.append (org.drip.service.stream.Serializer.NULL_SER_STRING);
			else
				sb.append (sbMapCQComp.toString());
		}

		return sb.append (getObjectTrailer()).toString().getBytes();
	}

	@Override public org.drip.service.stream.Serializer deserialize (
		final byte[] ab)
	{
		try {
			return new BasketMarketParamSet (ab);
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
		double[] adblRateTSY = new double[3];
		double[] adblRateEDSF = new double[3];
		double[] adblHazardRate = new double[3];

		for (int i = 0; i < 3; ++i) {
			adblDate[i] = dblStart + 365. * (i + 1);
			adblRate[i] = 0.015 * (i + 1);
			adblRateTSY[i] = 0.01 * (i + 1);
			adblRateEDSF[i] = 0.0125 * (i + 1);
			adblHazardRate[i] = 0.01 * (i + 1);
		}

		org.drip.analytics.rates.DiscountCurve dc =
			org.drip.state.creator.DiscountCurveBuilder.CreateDC
				(org.drip.analytics.date.JulianDate.Today(), "ABC", adblDate, adblRate,
					org.drip.state.creator.DiscountCurveBuilder.BOOTSTRAP_MODE_CONSTANT_FORWARD);

		org.drip.analytics.rates.DiscountCurve dcTSY =
			org.drip.state.creator.DiscountCurveBuilder.CreateDC
				(org.drip.analytics.date.JulianDate.Today(), "ABCTSY", adblDate, adblRateTSY,
					org.drip.state.creator.DiscountCurveBuilder.BOOTSTRAP_MODE_CONSTANT_FORWARD);

		org.drip.analytics.rates.DiscountCurve dcEDSF =
			org.drip.state.creator.DiscountCurveBuilder.CreateDC
				(org.drip.analytics.date.JulianDate.Today(), "ABCEDSF", adblDate, adblRateEDSF,
					org.drip.state.creator.DiscountCurveBuilder.BOOTSTRAP_MODE_CONSTANT_FORWARD);

		org.drip.analytics.support.CaseInsensitiveTreeMap<org.drip.analytics.rates.DiscountCurve> mapDC =
			new org.drip.analytics.support.CaseInsensitiveTreeMap<org.drip.analytics.rates.DiscountCurve>();

		mapDC.put ("ABC", dc);

		mapDC.put ("ABCTSY", dcTSY);

		mapDC.put ("ABCEDSF", dcEDSF);

		org.drip.analytics.definition.CreditCurve cc =
			org.drip.state.creator.CreditCurveBuilder.CreateCreditCurve
				(org.drip.analytics.date.JulianDate.Today(), "ABCSOV", "USD", adblDate, adblHazardRate,
					0.40);

		org.drip.analytics.support.CaseInsensitiveTreeMap<org.drip.analytics.definition.CreditCurve> mapCC = new
			org.drip.analytics.support.CaseInsensitiveTreeMap<org.drip.analytics.definition.CreditCurve>();

		mapCC.put ("ABCSOV", cc);

		org.drip.param.definition.ComponentQuote cq = new org.drip.param.market.ComponentMultiMeasureQuote();

		cq.addQuote ("Price", new org.drip.param.market.MultiSidedQuote ("ASK", 103., 100000.), false);

		cq.setMarketQuote ("SpreadToTsyBmk", new org.drip.param.market.MultiSidedQuote ("MID", 210.,
			100000.));

		org.drip.analytics.support.CaseInsensitiveTreeMap<org.drip.param.definition.ComponentQuote> mapTSYQuotes
			= new org.drip.analytics.support.CaseInsensitiveTreeMap<org.drip.param.definition.ComponentQuote>();

		mapTSYQuotes.put ("TSY2ON", cq);

		org.drip.analytics.support.CaseInsensitiveTreeMap<java.lang.Double> mIndexFixings = new
			org.drip.analytics.support.CaseInsensitiveTreeMap<java.lang.Double>();

		mIndexFixings.put ("USD-LIBOR-6M", 0.0042);

		java.util.Map<org.drip.analytics.date.JulianDate,
			org.drip.analytics.support.CaseInsensitiveTreeMap<java.lang.Double>> mmFixings = new
				java.util.HashMap<org.drip.analytics.date.JulianDate,
					org.drip.analytics.support.CaseInsensitiveTreeMap<java.lang.Double>>();

		mmFixings.put (org.drip.analytics.date.JulianDate.Today().addDays (2), mIndexFixings);

		BasketMarketParamSet bmp = new BasketMarketParamSet (mapDC, null, mapCC, mapTSYQuotes, mmFixings);

		byte[] abBMP = bmp.serialize();

		System.out.println (new java.lang.String (abBMP));

		BasketMarketParamSet bmpDeser = new BasketMarketParamSet (abBMP);

		System.out.println (new java.lang.String (bmpDeser.serialize()));
	}
}
