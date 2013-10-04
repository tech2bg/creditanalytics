
package org.drip.state.curve;

/*
 * -*- mode: java; tab-width: 4; indent-tabs-mode: nil; c-basic-offset: 4 -*-
 */

/*!
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
 * ForwardRateDiscountCurve manages the Discounting Latent State, using the Constant Forward Rate as the
 *  State Response Representation. It exports the following functionality:
 *  - Calculate discount factor / discount factor Jacobian
 *  - Calculate implied forward rate / implied forward rate Jacobian
 *  - Construct tweaked curve instances (parallel/tenor/custom tweaks)
 *  - Optionally provide the calibration instruments and quotes used to build the curve.
 *
 * @author Lakshmi Krishnamurthy
 */

public class ForwardRateDiscountCurve extends org.drip.analytics.definition.ExplicitBootDiscountCurve {
	private double _adblDate[] = null;
	private double _adblRate[] = null;

	private ForwardRateDiscountCurve shiftManifestMeasure (
		final double[] adblShift)
	{
		if (!org.drip.math.common.NumberUtil.IsValid (adblShift) || null == _ccis) return null;

		org.drip.product.definition.CalibratableComponent[] aCalibInst = _ccis.getComponent();

		org.drip.param.valuation.ValuationParams valParam = _ccis.getValuationParameter();

		org.drip.param.valuation.QuotingParams quotingParam = _ccis.getQuotingParameter();

		org.drip.analytics.support.CaseInsensitiveTreeMap<java.lang.Double> mapQuote = _ccis.getQuote();

		org.drip.analytics.support.CaseInsensitiveTreeMap<java.lang.String> mapMeasure = _ccis.getMeasure();

		java.util.Map<org.drip.analytics.date.JulianDate,
			org.drip.analytics.support.CaseInsensitiveTreeMap<java.lang.Double>> mmFixing =
				_ccis.getFixing();

		org.drip.state.estimator.NonlinearCurveCalibrator calibrator = new
			org.drip.state.estimator.NonlinearCurveCalibrator();

		int iNumComp = aCalibInst.length;
		double[] adblCalibQuoteShifted = new double[iNumComp];
		java.lang.String[] astrCalibMeasure = new java.lang.String[iNumComp];

		if (adblShift.length != iNumComp) return null;

		try {
			ForwardRateDiscountCurve frdc = new ForwardRateDiscountCurve (new
				org.drip.analytics.date.JulianDate (_dblEpochDate), _strCurrency, _adblDate, _adblRate);

			for (int i = 0; i < iNumComp; ++i) {
				java.lang.String strInstrumentCode = aCalibInst[i].getPrimaryCode();

				calibrator.calibrateIRNode (frdc, null, null, aCalibInst[i], i, valParam, astrCalibMeasure[i]
					= mapMeasure.get (strInstrumentCode), adblCalibQuoteShifted[i] = mapQuote.get
						(strInstrumentCode) + adblShift[i], mmFixing, quotingParam, false,
							java.lang.Double.NaN);
			}

			return frdc.setCCIS (new org.drip.analytics.definition.BootCurveConstructionInput (valParam,
				quotingParam, aCalibInst, mapQuote, mapMeasure, mmFixing)) ? frdc : null;
		} catch (java.lang.Exception e) {
			e.printStackTrace();
		}

		return null;
	}

	/**
	 * Boot-straps a constant forward discount curve from an array of dates and discount rates
	 * 
	 * @param dtStart Epoch Date
	 * @param strCurrency Currency
	 * @param adblDate Array of Dates
	 * @param adblRate Array of Rates
	 * 
	 * @throws java.lang.Exception Thrown if the curve cannot be created
	 */

	public ForwardRateDiscountCurve (
		final org.drip.analytics.date.JulianDate dtStart,
		final java.lang.String strCurrency,
		final double[] adblDate,
		final double[] adblRate)
		throws java.lang.Exception
	{
		super (dtStart.getJulian(), strCurrency);

		if (null == adblDate || null == adblRate)
			throw new java.lang.Exception ("ForwardRateDiscountCurve ctr: Invalid inputs");

		int iNumDate = adblDate.length;

		if (0 == iNumDate || iNumDate != adblRate.length)
			throw new java.lang.Exception ("ForwardRateDiscountCurve ctr: Invalid inputs");

		_adblDate = new double[iNumDate];
		_adblRate = new double[iNumDate];

		for (int i = 0; i < iNumDate; ++i) {
			_adblDate[i] = adblDate[i];
			_adblRate[i] = adblRate[i];
		}
	}

	protected ForwardRateDiscountCurve (
		final ForwardRateDiscountCurve dc)
		throws java.lang.Exception
	{
		super (dc.epoch().getJulian(), dc.currency());

		_adblDate = dc._adblDate;
		_adblRate = dc._adblRate;
		_strCurrency = dc._strCurrency;
		_dblEpochDate = dc._dblEpochDate;
	}

	/**
	 * ForwardRateDiscountCurve de-serialization from input byte array
	 * 
	 * @param ab Byte Array
	 * 
	 * @throws java.lang.Exception Thrown if ForwardRateDiscountCurve cannot be properly de-serialized
	 */

	public ForwardRateDiscountCurve (
		final byte[] ab)
		throws java.lang.Exception
	{
		super (org.drip.analytics.date.JulianDate.Today().getJulian(), "DEF_INIT");

		if (null == ab || 0 == ab.length)
			throw new java.lang.Exception
				("ForwardRateDiscountCurve de-serializer: Invalid input Byte array");

		java.lang.String strRawString = new java.lang.String (ab);

		if (null == strRawString || strRawString.isEmpty())
			throw new java.lang.Exception ("ForwardRateDiscountCurve de-serializer: Empty state");

		java.lang.String strSerializedConstantForwardDiscountCurve = strRawString.substring (0,
			strRawString.indexOf (getObjectTrailer()));

		if (null == strSerializedConstantForwardDiscountCurve ||
			strSerializedConstantForwardDiscountCurve.isEmpty())
			throw new java.lang.Exception ("ForwardRateDiscountCurve de-serializer: Cannot locate state");

		java.lang.String[] astrField = org.drip.math.common.StringUtil.Split
			(strSerializedConstantForwardDiscountCurve, getFieldDelimiter());

		if (null == astrField || 4 > astrField.length)
			throw new java.lang.Exception ("ForwardRateDiscountCurve de-serializer: Invalid reqd field set");

		// double dblVersion = new java.lang.Double (astrField[0]);

		if (null == astrField[1] || astrField[1].isEmpty() ||
			org.drip.service.stream.Serializer.NULL_SER_STRING.equalsIgnoreCase (astrField[1]))
			throw new java.lang.Exception
				("ForwardRateDiscountCurve de-serializer: Cannot locate start state");

		_dblEpochDate = new java.lang.Double (astrField[1]);

		if (null == astrField[2] || astrField[2].isEmpty() ||
			org.drip.service.stream.Serializer.NULL_SER_STRING.equalsIgnoreCase (astrField[2]))
			throw new java.lang.Exception ("ForwardRateDiscountCurve de-serializer: Cannot locate currency");

		_strCurrency = astrField[2];

		java.util.List<java.lang.Double> lsdblDate = new java.util.ArrayList<java.lang.Double>();

		java.util.List<java.lang.Double> lsdblRate = new java.util.ArrayList<java.lang.Double>();

		if (null == astrField[3] || astrField[3].isEmpty() ||
			org.drip.service.stream.Serializer.NULL_SER_STRING.equalsIgnoreCase (astrField[3]))
			throw new java.lang.Exception ("ForwardRateDiscountCurve de-serializer: Cannot decode state");

		if (!org.drip.math.common.StringUtil.KeyValueListFromStringArray (lsdblDate, lsdblRate, astrField[3],
			getCollectionRecordDelimiter(), getCollectionKeyValueDelimiter()))
			throw new java.lang.Exception ("ForwardRateDiscountCurve de-serializer: Cannot decode state");

		if (0 == lsdblDate.size() || 0 == lsdblRate.size() || lsdblDate.size() != lsdblRate.size())
			throw new java.lang.Exception ("ForwardRateDiscountCurve de-serializer: Cannot decode state");

		_adblDate = new double[lsdblDate.size()];

		_adblRate = new double[lsdblRate.size()];

		for (int i = 0; i < _adblDate.length; ++i) {
			_adblDate[i] = lsdblDate.get (i);

			_adblRate[i] = lsdblRate.get (i);
		}
	}

	@Override public double df (
		final double dblDate)
		throws java.lang.Exception
	{
		if (!org.drip.math.common.NumberUtil.IsValid (dblDate))
			throw new java.lang.Exception ("ForwardRateDiscountCurve::df => Got NaN for date");

		if (dblDate <= _dblEpochDate) return 1.;

		int i = 0;
		double dblExpArg = 0.;
		int iNumDate = _adblDate.length;
		double dblStartDate = _dblEpochDate;

		while (i < iNumDate && (int) dblDate >= (int) _adblDate[i]) {
			dblExpArg -= _adblRate[i] * (_adblDate[i] - dblStartDate);
			dblStartDate = _adblDate[i++];
		}

		if (i >= iNumDate) i = iNumDate - 1;

		dblExpArg -= _adblRate[i] * (dblDate - dblStartDate);

		return java.lang.Math.exp (dblExpArg / 365.25);
	}

	@Override public ForwardRateDiscountCurve parallelShiftManifestMeasure (
		final double dblShift)
	{
		System.out.println ("In here: " + _ccis);

		if (!org.drip.math.common.NumberUtil.IsValid (dblShift) || null == _ccis) return null;

		org.drip.product.definition.CalibratableComponent[] aCalibInst = _ccis.getComponent();

		int iNumComp = aCalibInst.length;
		double[] adblShift = new double[iNumComp];

		for (int i = 0; i < iNumComp; ++i)
			adblShift[i] = dblShift;

		return shiftManifestMeasure (adblShift);
	}

	@Override public ForwardRateDiscountCurve shiftManifestMeasure (
		final int iSpanIndex,
		final double dblShift)
	{
		if (!org.drip.math.common.NumberUtil.IsValid (dblShift) || null == _ccis) return null;

		org.drip.product.definition.CalibratableComponent[] aCalibInst = _ccis.getComponent();

		int iNumComp = aCalibInst.length;
		double[] adblShift = new double[iNumComp];

		if (iSpanIndex >= iNumComp) return null;

		for (int i = 0; i < iNumComp; ++i)
			adblShift[i] = i == iSpanIndex ? dblShift : 0.;

		return shiftManifestMeasure (adblShift);
	}

	@Override public org.drip.analytics.definition.ExplicitBootDiscountCurve customTweakManifestMeasure (
		final org.drip.param.definition.ResponseValueTweakParams rvtp)
	{
		return shiftManifestMeasure (org.drip.analytics.support.AnalyticsHelper.TweakManifestMeasure
			(_adblRate, rvtp));
	}

	@Override public ForwardRateDiscountCurve parallelShiftQuantificationMetric (
		final double dblShift)
	{
		if (!org.drip.math.common.NumberUtil.IsValid (dblShift)) return null;

		int iNumDate = _adblRate.length;
		double[] adblRate = new double[iNumDate];

		for (int i = 0; i < iNumDate; ++i)
			adblRate[i] = _adblRate[i] + dblShift;

		try {
			return new ForwardRateDiscountCurve (new org.drip.analytics.date.JulianDate (_dblEpochDate),
				_strCurrency, _adblDate, adblRate);
		} catch (java.lang.Exception e) {
			e.printStackTrace();
		}

		return null;
	}

	@Override public org.drip.analytics.definition.Curve customTweakQuantificationMetric (
		final org.drip.param.definition.ResponseValueTweakParams rvtp)
	{
		try {
			return new ForwardRateDiscountCurve (new org.drip.analytics.date.JulianDate (_dblEpochDate),
				_strCurrency, _adblDate, org.drip.analytics.support.AnalyticsHelper.TweakManifestMeasure
					(_adblRate, rvtp));
		} catch (java.lang.Exception e) {
			e.printStackTrace();
		}

		return null;
	}

	@Override public ForwardRateDiscountCurve createBasisRateShiftedCurve (
		final double[] adblDate,
		final double[] adblBasis)
	{
		if (null == adblDate || null == adblBasis) return null;

		int iNumDate = adblDate.length;

		if (0 == iNumDate || iNumDate != adblBasis.length) return null;

		double[] adblShiftedRate = new double[iNumDate];

		try {
			for (int i = 0; i < adblDate.length; ++i)
				adblShiftedRate[i] = rate (adblDate[i]) + adblBasis[i];

			return new ForwardRateDiscountCurve (new org.drip.analytics.date.JulianDate (_dblEpochDate),
				_strCurrency, adblDate, adblShiftedRate);
		} catch (java.lang.Exception e) {
			e.printStackTrace();
		}

		return null;
	}

	@Override public java.lang.String latentStateQuantificationMetric()
	{
		return org.drip.analytics.definition.DiscountCurve.QUANTIFICATION_METRIC_FORWARD_RATE;
	}

	@Override public org.drip.math.calculus.WengertJacobian dfJack (
		final double dblDate)
	{
		if (!org.drip.math.common.NumberUtil.IsValid (dblDate)) return null;

		int i = 0;
		double dblDF = java.lang.Double.NaN;
		double dblStartDate = _dblEpochDate;
		org.drip.math.calculus.WengertJacobian wj = null;

		try {
			wj = new org.drip.math.calculus.WengertJacobian (1, _adblRate.length);
		} catch (java.lang.Exception e) {
			e.printStackTrace();

			return null;
		}

		if (dblDate <= _dblEpochDate) {
			if (!wj.setWengert (0, 0.)) return null;

			return wj;
		}

		try {
			if (!wj.setWengert (0, dblDF = df (dblDate))) return null;
		} catch (java.lang.Exception e) {
			e.printStackTrace();

			return null;
		}

		while (i < _adblRate.length && (int) dblDate >= (int) _adblDate[i]) {
			if (!wj.accumulatePartialFirstDerivative (0, i, dblDF * (dblStartDate - _adblDate[i]) / 365.25))
				return null;

			dblStartDate = _adblDate[i++];
		}

		if (i >= _adblRate.length) i = _adblRate.length - 1;

		return wj.accumulatePartialFirstDerivative (0, i, dblDF * (dblStartDate - dblDate) / 365.25) ? wj :
			null;
	}

	@Override public boolean setNodeValue (
		final int iNodeIndex,
		final double dblValue)
	{
		if (!org.drip.math.common.NumberUtil.IsValid (dblValue) || iNodeIndex > _adblRate.length)
			return false;

		for (int i = iNodeIndex; i < _adblRate.length; ++i)
			_adblRate[i] = dblValue;

		return true;
	}

	@Override public boolean bumpNodeValue (
		final int iNodeIndex,
		final double dblValue)
	{
		if (!org.drip.math.common.NumberUtil.IsValid (dblValue) || iNodeIndex > _adblRate.length)
			return false;

		for (int i = iNodeIndex; i < _adblRate.length; ++i)
			_adblRate[i] += dblValue;

		return true;
	}

	@Override public boolean setFlatValue (
		final double dblValue)
	{
		if (!org.drip.math.common.NumberUtil.IsValid (dblValue)) return false;

		for (int i = 0; i < _adblRate.length; ++i)
			_adblRate[i] = dblValue;

		return true;
	}

	@Override public byte[] serialize()
	{
		java.lang.StringBuffer sb = new java.lang.StringBuffer();

		sb.append (org.drip.service.stream.Serializer.VERSION + getFieldDelimiter() + _dblEpochDate +
			getFieldDelimiter() + _strCurrency + getFieldDelimiter());

		if (null == _adblRate || 0 == _adblRate.length)
			sb.append (org.drip.service.stream.Serializer.NULL_SER_STRING);
		else {
			for (int i = 0; i < _adblRate.length; ++i) {
				if (0 != i) sb.append (getCollectionRecordDelimiter());

				sb.append (_adblDate[i] + getCollectionKeyValueDelimiter() + _adblRate[i]);
			}
		}

		return sb.append (getObjectTrailer()).toString().getBytes();
	}

	@Override public org.drip.service.stream.Serializer deserialize (
		final byte[] ab)
	{
		try {
			return new ForwardRateDiscountCurve (ab);
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

		for (int i = 0; i < 3; ++i) {
			adblDate[i] = dblStart + 365. * (i + 1);
			adblRate[i] = 0.01 * (i + 1);
		}

		ForwardRateDiscountCurve dc = new ForwardRateDiscountCurve
			(org.drip.analytics.date.JulianDate.Today(), "ABC", adblDate, adblRate);

		byte[] abDC = dc.serialize();

		System.out.println ("Input: " + new java.lang.String (abDC));

		System.out.println ("DF[12/12/20]=" + dc.df
			(org.drip.analytics.date.JulianDate.CreateFromDDMMMYYYY ("12-DEC-2020")));

		ForwardRateDiscountCurve dcDeser = (ForwardRateDiscountCurve) dc.deserialize (abDC);

		System.out.println ("Output: " + new java.lang.String (dcDeser.serialize()));

		System.out.println ("DF[12/12/20]=" + dcDeser.df
			(org.drip.analytics.date.JulianDate.CreateFromDDMMMYYYY ("12-DEC-2020")));
	}
}
