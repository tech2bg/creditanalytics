
package org.drip.state.curve;

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
 * DerivedFXForward manages the constant forward based FX Forward Curve holder object. It exports the
 *  following functionality:
 *  - Extract currency, currency pair, spot epoch and spot FX
 *  - Compute Zero/boot-strap Basis, as well as boot-strap basis DC
 *  - Compute the spot implied rate/implied rate nodes
 *  - Retrieve Array of the Calibration Components and their LatentStateMetricMeasure's
 *  - Retrieve the Curve Construction Input Set
 *  - Synthesize scenario Latent State by parallel shifting/custom tweaking the quantification metric
 *  - Synthesize scenario Latent State by parallel/custom shifting/custom tweaking the manifest measure
 *  - Serialize into and de-serialize out of byte array
 *
 * @author Lakshmi Krishnamurthy
 */

public class DerivedFXForward extends org.drip.analytics.definition.FXForwardCurve {
	private boolean[] _abIsPIP = null;
	private double[] _adblDate = null;
	private double[] _adblFXFwd = null;
	private double _dblFXSpot = java.lang.Double.NaN;
	private double _dblSpotDate = java.lang.Double.NaN;
	private org.drip.product.params.CurrencyPair _cp = null;

	private double calcNodeBasis (
		final int iNode,
		final org.drip.param.valuation.ValuationParams valParam,
		final org.drip.analytics.rates.DiscountCurve dcNum,
		final org.drip.analytics.rates.DiscountCurve dcDenom,
		final boolean bBasisOnDenom)
		throws java.lang.Exception
	{
		if (iNode >= _abIsPIP.length || null == valParam || null == dcNum || null == dcDenom)
			throw new java.lang.Exception
				("DerivedFXForward::calcNodeBasis => Bad inputs into calcNodeBasis!");

		double dblFXFwd = _adblFXFwd[iNode];

		if (_abIsPIP[iNode]) dblFXFwd = _dblFXSpot + (_adblFXFwd[iNode] / _cp.getPIPFactor());

		org.drip.product.definition.FXForward fxfwd =
			org.drip.product.creator.FXForwardBuilder.CreateFXForward (_cp, new
				org.drip.analytics.date.JulianDate (_dblSpotDate), new org.drip.analytics.date.JulianDate
					(_adblDate[iNode]));

		if (null == fxfwd)
			throw new java.lang.Exception
				("DerivedFXForward::calcNodeBasis => Cannot make fxfwd at maturity " +
					org.drip.analytics.date.JulianDate.fromJulian (_adblDate[iNode]));

		return fxfwd.calcDCBasis (valParam, dcNum, dcDenom, _dblFXSpot, dblFXFwd, bBasisOnDenom);
	}

	/**
	 * DerivedFXForward from the CurrencyPair, FX Spot, and the FX Forward parameters
	 * 
	 * @param cp CurrencyPair
	 * @param dtSpot Spot Date
	 * @param dblFXSpot FX Spot Rate
	 * @param adblDate Array of dates
	 * @param adblFXFwd Array of FX Forwards
	 * @param abIsPIP Array of PIP indicators
	 * 
	 * @throws java.lang.Exception Creates the FXCurve instance
	 */

	public DerivedFXForward (
		final org.drip.product.params.CurrencyPair cp,
		final org.drip.analytics.date.JulianDate dtSpot,
		final double dblFXSpot,
		final double[] adblDate,
		final double[] adblFXFwd,
		final boolean[] abIsPIP)
		throws java.lang.Exception
	{
		if (null == cp || null == dtSpot || !org.drip.quant.common.NumberUtil.IsValid (dblFXSpot) || null ==
			adblDate || 0 == adblDate.length || null == adblFXFwd || 0 == adblFXFwd.length || null == abIsPIP
				|| 0 == abIsPIP.length || adblDate.length != adblFXFwd.length || adblDate.length !=
					abIsPIP.length)
			throw new java.lang.Exception ("DerivedFXForward ctr => Invalid params!");

		_dblSpotDate = dtSpot.getJulian();

		_cp = cp;
		_dblFXSpot = dblFXSpot;
		_abIsPIP = new boolean[abIsPIP.length];
		_adblDate = new double[adblDate.length];
		_adblFXFwd = new double[adblFXFwd.length];

		for (int i = 0; i < abIsPIP.length; ++i) {
			if (!org.drip.quant.common.NumberUtil.IsValid (adblDate[i]) || adblDate[i] <= _dblSpotDate)
				throw new java.lang.Exception ("DerivedFXForward ctr: Node date " +
					org.drip.analytics.date.JulianDate.fromJulian (adblDate[i]) + " before spot " + dtSpot);

			_abIsPIP[i] = abIsPIP[i];
			_adblDate[i] = adblDate[i];
			_adblFXFwd[i] = adblFXFwd[i];
		}
	}

	/**
	 * DerivedFXForward de-serialization from input byte array
	 * 
	 * @param ab Byte Array
	 * 
	 * @throws java.lang.Exception Thrown if DerivedFXForward cannot be properly de-serialized
	 */

	public DerivedFXForward (
		final byte[] ab)
		throws java.lang.Exception
	{
		if (null == ab || 0 == ab.length)
			throw new java.lang.Exception ("DerivedFXForward de-serializer: Invalid input Byte array");

		java.lang.String strRawString = new java.lang.String (ab);

		if (null == strRawString || strRawString.isEmpty())
			throw new java.lang.Exception ("DerivedFXForward de-serializer: Empty state");

		java.lang.String strFXCurve = strRawString.substring (0, strRawString.indexOf (getObjectTrailer()));

		if (null == strFXCurve || strFXCurve.isEmpty())
			throw new java.lang.Exception ("DerivedFXForward de-serializer: Cannot locate state");

		java.lang.String[] astrField = org.drip.quant.common.StringUtil.Split (strFXCurve,
			getFieldDelimiter());

		if (null == astrField || 6 > astrField.length)
			throw new java.lang.Exception ("DerivedFXForward de-serializer: Invalid reqd field set");

		// double dblVersion = new java.lang.Double (astrField[0]);

		if (null == astrField[1] || astrField[1].isEmpty() ||
			org.drip.service.stream.Serializer.NULL_SER_STRING.equalsIgnoreCase (astrField[1]))
			throw new java.lang.Exception ("DerivedFXForward de-serializer: Cannot locate spot date");

		_dblSpotDate = new java.lang.Double (astrField[1]);

		if (null == astrField[2] || astrField[2].isEmpty() ||
			org.drip.service.stream.Serializer.NULL_SER_STRING.equalsIgnoreCase (astrField[2]))
			throw new java.lang.Exception ("DerivedFXForward de-serializer: Cannot locate FX spot");

		_dblFXSpot = new java.lang.Double (astrField[2]);

		java.util.List<java.lang.Double> lsdblDate = new java.util.ArrayList<java.lang.Double>();

		java.util.List<java.lang.Double> lsdblBasis = new java.util.ArrayList<java.lang.Double>();

		if (null == astrField[3] || astrField[3].isEmpty() ||
			org.drip.service.stream.Serializer.NULL_SER_STRING.equalsIgnoreCase (astrField[3]))
			throw new java.lang.Exception ("DerivedFXForward de-serializer: Cannot decode state");

		if (!org.drip.quant.common.StringUtil.KeyValueListFromStringArray (lsdblDate, lsdblBasis,
			astrField[3], getCollectionRecordDelimiter(), getCollectionKeyValueDelimiter()))
			throw new java.lang.Exception ("DerivedFXForward de-serializer: Cannot decode state");

		if (0 == lsdblDate.size() || 0 == lsdblBasis.size() || lsdblDate.size() != lsdblBasis.size())
			throw new java.lang.Exception ("DerivedFXForward de-serializer: Cannot decode state");

		_adblDate = new double[lsdblDate.size()];

		_adblFXFwd = new double[lsdblBasis.size()];

		for (int i = 0; i < _adblDate.length; ++i) {
			_adblDate[i] = lsdblDate.get (i);

			_adblFXFwd[i] = lsdblBasis.get (i);
		}

		if (null == astrField[4] || astrField[4].isEmpty() ||
			org.drip.service.stream.Serializer.NULL_SER_STRING.equalsIgnoreCase (astrField[4]))
			throw new java.lang.Exception ("DerivedFXForward de-serializer: Cannot decode state");

		java.util.List<java.lang.Boolean> lsb = new java.util.ArrayList<java.lang.Boolean>();

		if (!org.drip.quant.common.StringUtil.BooleanListFromString (lsb, astrField[4],
			getCollectionRecordDelimiter()))
			throw new java.lang.Exception ("DerivedFXForward de-serializer: Cannot decode state");

		_abIsPIP = new boolean[lsb.size()];

		for (int i = 0; i < _abIsPIP.length; ++i)
			_abIsPIP[i] = lsb.get (i);

		if (null == astrField[5] || astrField[5].isEmpty() ||
			org.drip.service.stream.Serializer.NULL_SER_STRING.equalsIgnoreCase (astrField[5]))
			throw new java.lang.Exception ("DerivedFXForward de-serializer: Cannot decode currency pair");

		_cp = new org.drip.product.params.CurrencyPair (astrField[5].getBytes());
	}

	@Override public org.drip.product.params.CurrencyPair currencyPair()
	{
		return _cp;
	}

	@Override public org.drip.analytics.date.JulianDate spotDate()
	{
		try {
			return new org.drip.analytics.date.JulianDate (_dblSpotDate);
		} catch (java.lang.Exception e) {
			e.printStackTrace();
		}

		return null;
	}

	@Override public double fxSpot()
	{
		return _dblFXSpot;
	}

	@Override public java.lang.String currency()
	{
		return _cp.getDenomCcy();
	}

	@Override public org.drip.param.valuation.CollateralizationParams collateralParams()
	{
		return null;
	}

	@Override public double[] zeroBasis (
		final org.drip.param.valuation.ValuationParams valParam,
		final org.drip.analytics.rates.DiscountCurve dcNum,
		final org.drip.analytics.rates.DiscountCurve dcDenom,
		final boolean bBasisOnDenom)
	{
		double[] adblBasis = new double[_abIsPIP.length];

		for (int i = 0; i < _abIsPIP.length; ++i) {
			try {
				adblBasis[i] = calcNodeBasis (i, valParam, dcNum, dcDenom, bBasisOnDenom);
			} catch (java.lang.Exception e) {
				e.printStackTrace();

				return null;
			}
		}

		return adblBasis;
	}

	@Override public double[] bootstrapBasis (
		final org.drip.param.valuation.ValuationParams valParam,
		final org.drip.analytics.rates.DiscountCurve dcNum,
		final org.drip.analytics.rates.DiscountCurve dcDenom,
		final boolean bBasisOnDenom)
	{
		double[] adblBasis = new double[_abIsPIP.length];
		org.drip.analytics.rates.ExplicitBootDiscountCurve dcBasis = null;

		try {
			dcBasis = (org.drip.analytics.rates.ExplicitBootDiscountCurve) (bBasisOnDenom ?
				dcDenom.parallelShiftQuantificationMetric (0.) : dcNum.parallelShiftQuantificationMetric
					(0.));
		} catch (java.lang.Exception e) {
			e.printStackTrace();

			return null;
		}

		if (null == dcBasis) return null;

		for (int i = 0; i < _abIsPIP.length; ++i) {
			try {
				if (bBasisOnDenom)
					adblBasis[i] = calcNodeBasis (i, valParam, dcNum, dcBasis, true);
				else
					adblBasis[i] = calcNodeBasis (i, valParam, dcBasis, dcDenom, false);

				dcBasis.bumpNodeValue (i, adblBasis[i]);
			} catch (java.lang.Exception e) {
				e.printStackTrace();

				return null;
			}
		}

		return adblBasis;
	}

	@Override public org.drip.analytics.rates.DiscountCurve bootstrapBasisDC (
		final org.drip.param.valuation.ValuationParams valParam,
		final org.drip.analytics.rates.DiscountCurve dcNum,
		final org.drip.analytics.rates.DiscountCurve dcDenom,
		final boolean bBasisOnDenom)
	{
		org.drip.analytics.rates.ExplicitBootDiscountCurve dcBasis = null;

		try {
			if (bBasisOnDenom)
				dcBasis = (org.drip.analytics.rates.ExplicitBootDiscountCurve)
					dcDenom.parallelShiftManifestMeasure ("Rate", 0.);
			else
				dcBasis = (org.drip.analytics.rates.ExplicitBootDiscountCurve)
					dcNum.parallelShiftManifestMeasure ("Rate", 0.);
		} catch (java.lang.Exception e) {
			e.printStackTrace();

			return null;
		}

		if (null == dcBasis) return null;

		for (int i = 0; i < _abIsPIP.length; ++i) {
			double dblBasis = java.lang.Double.NaN;

			try {
				if (bBasisOnDenom)
					dblBasis = calcNodeBasis (i, valParam, dcNum, dcBasis, true);
				else
					dblBasis = calcNodeBasis (i, valParam, dcBasis, dcDenom, false);

				dcBasis.bumpNodeValue (i, dblBasis);
			} catch (java.lang.Exception e) {
				e.printStackTrace();

				return null;
			}
		}

		return dcBasis;
	}

	public double rate (
		final org.drip.param.valuation.ValuationParams valParam,
		final org.drip.analytics.rates.DiscountCurve dcNum,
		final org.drip.analytics.rates.DiscountCurve dcDenom,
		final double dblDate,
		final boolean bBasisOnDenom)
		throws java.lang.Exception
	{
		if (!org.drip.quant.common.NumberUtil.IsValid (dblDate))
			throw new java.lang.Exception ("FXForwardCurve.rate: Invalid input date!");

		org.drip.analytics.rates.DiscountCurve dcImplied = bootstrapBasisDC (valParam, dcNum, dcDenom,
			bBasisOnDenom);

		if (null == dcImplied)
			throw new java.lang.Exception ("FXForwardCurve.rate: Cannot imply basis DC!");

		return dcImplied.zero (dblDate);
	}

	@Override public double[] impliedNodeRates (
		final org.drip.param.valuation.ValuationParams valParam,
		final org.drip.analytics.rates.DiscountCurve dcNum,
		final org.drip.analytics.rates.DiscountCurve dcDenom,
		final boolean bBasisOnDenom)
	{
		double[] adblImpliedNodeRate = new double[_abIsPIP.length];

		for (int i = 0; i < _abIsPIP.length; ++i) {
			try {
				double dblBaseImpliedRate = java.lang.Double.NaN;

				if (bBasisOnDenom)
					dblBaseImpliedRate = dcNum.zero (_adblDate[i]);
				else
					dblBaseImpliedRate = dcDenom.zero (_adblDate[i]);

				adblImpliedNodeRate[i] = dblBaseImpliedRate + calcNodeBasis (i,	valParam, dcNum, dcDenom,
					bBasisOnDenom);
			} catch (java.lang.Exception e) {
				e.printStackTrace();
			}
		}

		return adblImpliedNodeRate;
	}

	@Override public org.drip.state.representation.LatentStateMetricMeasure[] lsmm()
	{
		return null;
	}

	@Override public org.drip.analytics.support.CaseInsensitiveTreeMap<java.lang.Double> manifestMeasure (
		final java.lang.String strInstr)
	{
		return null;
	}

	@Override public boolean setCCIS (
		final org.drip.analytics.definition.CurveConstructionInputSet ccis)
	{
		return false;
	}

	@Override public org.drip.product.definition.CalibratableFixedIncomeComponent[] calibComp()
	{
		return null;
	}

	@Override public java.lang.String name()
	{
		java.lang.StringBuffer sb = new java.lang.StringBuffer();

		sb.append ("FXFWDBASIS[" + _cp.getCode() + "]=");

		for (int i = 0; i < _adblDate.length; ++i) {
			if (0 != i) sb.append (";");

			sb.append (_adblDate[i] + ":" + _adblFXFwd[i]);
		}

		return sb.toString();
	}

	@Override public org.drip.analytics.definition.Curve parallelShiftQuantificationMetric (
		final double dblShift)
	{
		return null;
	}

	@Override public org.drip.analytics.definition.Curve customTweakQuantificationMetric (
		final org.drip.param.definition.ResponseValueTweakParams rvtp)
	{
		return null;
	}

	@Override public org.drip.analytics.definition.Curve parallelShiftManifestMeasure (
		final java.lang.String strManifestMeasure,
		final double dblShift)
	{
		double[] adblFXForwardBumped = new double[_adblFXFwd.length];

		for (int i = 0; i < _adblFXFwd.length; ++i)
			adblFXForwardBumped[i] = _adblFXFwd[i] + dblShift;

		try {
			return new DerivedFXForward (_cp, new org.drip.analytics.date.JulianDate (_dblSpotDate),
				_dblFXSpot, _adblDate, adblFXForwardBumped, _abIsPIP);
		} catch (java.lang.Exception e) {
			e.printStackTrace();
		}

		return null;
	}

	@Override public org.drip.analytics.definition.Curve shiftManifestMeasure (
		final int iSpanIndex,
		final java.lang.String strManifestMeasure,
		final double dblShift)
	{
		int iNumForward = _adblFXFwd.length;

		if (iSpanIndex >= iNumForward || !org.drip.quant.common.NumberUtil.IsValid (dblShift)) return null;

		double[] adblFXForwardBumped = new double[iNumForward];

		for (int i = 0; i < iNumForward; ++i)
			adblFXForwardBumped[i] = _adblFXFwd[i] + (i == iSpanIndex ? dblShift : 0.);

		try {
			return new DerivedFXForward (_cp, new org.drip.analytics.date.JulianDate (_dblSpotDate),
				_dblFXSpot, _adblDate, adblFXForwardBumped, _abIsPIP);
		} catch (java.lang.Exception e) {
			e.printStackTrace();
		}

		return null;
	}

	@Override public org.drip.analytics.definition.Curve customTweakManifestMeasure (
		final java.lang.String strManifestMeasure,
		final org.drip.param.definition.ResponseValueTweakParams mmtp)
	{
		if (null == mmtp) return null;

		double[] adblFXBasisBumped = org.drip.analytics.support.AnalyticsHelper.TweakManifestMeasure
			(_adblFXFwd, mmtp);

		if (null == adblFXBasisBumped || 0 == adblFXBasisBumped.length) return null;

		try {
			return new DerivedFXForward (_cp, new org.drip.analytics.date.JulianDate (_dblSpotDate),
				_dblFXSpot, _adblDate, adblFXBasisBumped, _abIsPIP);
		} catch (java.lang.Exception e) {
			e.printStackTrace();
		}

		return null;
	}

	@Override public org.drip.analytics.date.JulianDate epoch()
	{
		try {
			return new org.drip.analytics.date.JulianDate (_dblSpotDate);
		} catch (java.lang.Exception e) {
			e.printStackTrace();
		}

		return null;
	}

	@Override public byte[] serialize()
	{
		java.lang.StringBuffer sb = new java.lang.StringBuffer();

		sb.append (org.drip.service.stream.Serializer.VERSION + getFieldDelimiter() + _dblSpotDate +
			getFieldDelimiter() + _dblFXSpot + getFieldDelimiter());

		for (int i = 0; i < _adblDate.length; ++i) {
			if (0 != i) sb.append (getCollectionRecordDelimiter());

			sb.append (_adblDate[i] + getCollectionKeyValueDelimiter() + _adblFXFwd[i]);
		}

		sb.append (getFieldDelimiter());

		for (int i = 0; i < _abIsPIP.length; ++i) {
			if (0 != i) sb.append (getCollectionRecordDelimiter());

			sb.append (_abIsPIP[i]);
		}

		sb.append (getFieldDelimiter() + new java.lang.String (_cp.serialize()));

		return sb.append (getObjectTrailer()).toString().getBytes();
	}

	@Override public org.drip.service.stream.Serializer deserialize (
		final byte[] ab)
	{
		try {
			return new DerivedFXForward (ab);
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
		double[] adblFXFwd = new double[3];
		boolean[] abIsPIP = new boolean[3];

		for (int i = 0; i < 3; ++i) {
			adblDate[i] = dblStart + 365. * (i + 1);
			adblFXFwd[i] = 53.51 + i;
			abIsPIP[i] = false;
		}

		DerivedFXForward fxCurve = new DerivedFXForward (new org.drip.product.params.CurrencyPair
			("USD", "INR", "INR", 1.), org.drip.analytics.date.JulianDate.Today(), 53.51, adblDate,
				adblFXFwd, abIsPIP);

		byte[] abFXCurve = fxCurve.serialize();

		System.out.println ("Input: " + new java.lang.String (abFXCurve));

		DerivedFXForward fxCurveDeser = new DerivedFXForward (abFXCurve);

		System.out.println ("Output: " + new java.lang.String (fxCurveDeser.serialize()));
	}
}
