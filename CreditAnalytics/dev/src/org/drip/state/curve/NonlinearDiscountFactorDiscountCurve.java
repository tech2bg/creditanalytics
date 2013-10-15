
package org.drip.state.curve;

/*
 * -*- mode: java; tab-width: 4; indent-tabs-mode: nil; c-basic-offset: 4 -*-
 */

/*!
 * Copyright (C) 2013 Lakshmi Krishnamurthy
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
 * NonlinearDiscountFactorDiscountCurve manages the Discounting Latent State, using the Discount Factor as
 *  the State Response Representation. The class constructs the discount curve using generic polynomial
 *  splines (arbitrary degree, variable shape control, custom segment knot constraints, user specified
 *  variational penalty optimization, and segment tension). It exports the following functionality:
 *  - Calculate discount factor / discount factor Jacobian
 *  - Calculate implied forward rate / implied forward rate Jacobian
 *  - Construct tweaked curve instances (parallel/tenor/custom tweaks)
 *  - Optionally provide the calibration instruments and quotes used to build the curve.
 *
 * @author Lakshmi Krishnamurthy
 */

public class NonlinearDiscountFactorDiscountCurve extends
	org.drip.analytics.definition.ExplicitBootDiscountCurve {
	private double[] _adblDate = null;
	private double _dblLeftNodeDF = java.lang.Double.NaN;
	private double _dblLeftNodeDFSlope = java.lang.Double.NaN;
	private org.drip.math.regime.MultiSegmentRegime _msr = null;
	private double _dblLeftFlatForwardRate = java.lang.Double.NaN;
	private double _dblRightFlatForwardRate = java.lang.Double.NaN;

	private NonlinearDiscountFactorDiscountCurve shiftManifestMeasure (
		final double[] adblShiftedManifestMeasure)
	{
		if (!org.drip.math.common.NumberUtil.IsValid (adblShiftedManifestMeasure) || null == _ccis)
			return null;

		org.drip.param.valuation.ValuationParams valParam = _ccis.getValuationParameter();

		org.drip.param.valuation.QuotingParams quotingParam = _ccis.getQuotingParameter();

		org.drip.product.definition.CalibratableComponent[] aCalibInst = _ccis.getComponent();

		org.drip.analytics.support.CaseInsensitiveTreeMap<java.lang.String> mapMeasure = _ccis.getMeasure();

		java.util.Map<org.drip.analytics.date.JulianDate,
			org.drip.analytics.support.CaseInsensitiveTreeMap<java.lang.Double>> mmFixing =
				_ccis.getFixing();

		org.drip.state.estimator.NonlinearCurveCalibrator calibrator = new
			org.drip.state.estimator.NonlinearCurveCalibrator();

		int iNumComp = aCalibInst.length;
		java.lang.String[] astrCalibMeasure = new java.lang.String[iNumComp];

		try {
			NonlinearDiscountFactorDiscountCurve nldfdc = new NonlinearDiscountFactorDiscountCurve (new
				org.drip.analytics.date.JulianDate (_dblEpochDate), _strCurrency, _adblDate,
					adblShiftedManifestMeasure);

			for (int i = 0; i < iNumComp; ++i) {
				java.lang.String strInstrumentCode = aCalibInst[i].getPrimaryCode();

				calibrator.calibrateIRNode (nldfdc, null, null, aCalibInst[i], i, valParam,
					astrCalibMeasure[i] = mapMeasure.get (strInstrumentCode), adblShiftedManifestMeasure[i],
						mmFixing, quotingParam, false, java.lang.Double.NaN);
			}

			return nldfdc.setCCIS (org.drip.analytics.definition.BootCurveConstructionInput.Create (valParam,
				quotingParam, aCalibInst, adblShiftedManifestMeasure, astrCalibMeasure, mmFixing)) ? nldfdc :
					null;
		} catch (java.lang.Exception e) {
			e.printStackTrace();
		}

		return null;
	}

	/**
	 * Construct NonlinearDiscountFactorDiscountCurve instance from an array of dates and forward rates
	 * 
	 * @param dtStart Epoch Date
	 * @param strCurrency Currency
	 * @param adblDate Array of Dates
	 * @param adblRate Array of Forward Rates
	 * 
	 * @throws java.lang.Exception Thrown if the curve cannot be created
	 */

	public NonlinearDiscountFactorDiscountCurve (
		final org.drip.analytics.date.JulianDate dtStart,
		final java.lang.String strCurrency,
		final double[] adblDate,
		final double[] adblRate)
		throws java.lang.Exception
	{
		super (dtStart.getJulian(), strCurrency);

		if (null == adblDate || 0 == adblDate.length || null == adblRate || adblDate.length !=
			adblRate.length || null == dtStart)
			throw new java.lang.Exception ("NonlinearDiscountFactorDiscountCurve ctr: Invalid inputs");

		_dblEpochDate = dtStart.getJulian();

		org.drip.math.segment.PredictorResponseBuilderParams sbp = new
			org.drip.math.segment.PredictorResponseBuilderParams
				(org.drip.math.regime.RegimeBuilder.BASIS_SPLINE_POLYNOMIAL, new
					org.drip.math.spline.PolynomialBasisSetParams (2),
						org.drip.math.segment.DesignInelasticParams.Create (0, 2), null);

		int iNumSegment = adblDate.length;
		_adblDate = new double[iNumSegment];
		double[] adblDF = new double[iNumSegment];
		org.drip.math.segment.PredictorResponseBuilderParams[] aSBP = new
			org.drip.math.segment.PredictorResponseBuilderParams[adblDate.length - 1];

		for (int i = 0; i < iNumSegment; ++i) {
			_adblDate[i] = adblDate[i];

			if (0 == i)
				adblDF[0] = java.lang.Math.exp (adblRate[0] * (_dblEpochDate - _adblDate[0]) / 365.25);
			else {
				aSBP[i - 1] = sbp;

				adblDF[i] = java.lang.Math.exp (adblRate[i] * (_adblDate[i - 1] - _adblDate[i]) / 365.25) *
					adblDF[i - 1];
			}
		}

		_dblLeftFlatForwardRate = -365.25 * java.lang.Math.log (adblDF[0]) / (_adblDate[0] - _dblEpochDate);

		_dblRightFlatForwardRate = -365.25 * java.lang.Math.log (adblDF[iNumSegment - 1]) /
			(_adblDate[iNumSegment - 1] - _dblEpochDate);

		_msr = org.drip.math.regime.RegimeBuilder.CreateCalibratedRegimeEstimator ("POLY_SPLINE_DF_REGIME",
			adblDate, adblDF, aSBP, org.drip.math.regime.MultiSegmentRegime.BOUNDARY_CONDITION_NATURAL,
				org.drip.math.regime.MultiSegmentRegime.CALIBRATE);
	}

	/**
	 * NonlinearDiscountFactorDiscountCurve de-serialization from input byte array
	 * 
	 * @param ab Byte Array
	 * 
	 * @throws java.lang.Exception Thrown if NonlinearDiscountFactorDiscountCurve cannot be properly
	 * 	de-serialized
	 */

	public NonlinearDiscountFactorDiscountCurve (
		final byte[] ab)
		throws java.lang.Exception
	{
		super (org.drip.analytics.date.JulianDate.Today().getJulian(), "DEF_INIT");

		if (null == ab || 0 == ab.length)
			throw new java.lang.Exception
				("NonlinearDiscountFactorDiscountCurve de-serializer: Invalid input Byte array");

		java.lang.String strRawString = new java.lang.String (ab);

		if (null == strRawString || strRawString.isEmpty())
			throw new java.lang.Exception
				("NonlinearDiscountFactorDiscountCurve de-serializer: Empty state");

		java.lang.String strSerializedPolynomialSplineDF = strRawString.substring (0, strRawString.indexOf
			(getObjectTrailer()));

		if (null == strSerializedPolynomialSplineDF || strSerializedPolynomialSplineDF.isEmpty())
			throw new java.lang.Exception
				("NonlinearDiscountFactorDiscountCurve de-serializer: Cannot locate state");

		java.lang.String[] astrField = org.drip.math.common.StringUtil.Split
			(strSerializedPolynomialSplineDF, getFieldDelimiter());

		if (null == astrField || 4 > astrField.length)
			throw new java.lang.Exception
				("NonlinearDiscountFactorDiscountCurve de-serializer: Invalid reqd field set");

		// double dblVersion = new java.lang.Double (astrField[0]);

		if (null == astrField[1] || astrField[1].isEmpty() ||
			org.drip.service.stream.Serializer.NULL_SER_STRING.equalsIgnoreCase (astrField[1]))
			throw new java.lang.Exception
				("NonlinearDiscountFactorDiscountCurve de-serializer: Cannot locate start state");

		_dblEpochDate = new java.lang.Double (astrField[1]);

		if (null == astrField[2] || astrField[2].isEmpty() ||
			org.drip.service.stream.Serializer.NULL_SER_STRING.equalsIgnoreCase (astrField[2]))
			throw new java.lang.Exception
				("NonlinearDiscountFactorDiscountCurve de-serializer: Cannot locate currency");

		_strCurrency = astrField[2];

		java.util.List<java.lang.Double> lsdblDate = new java.util.ArrayList<java.lang.Double>();

		java.util.List<java.lang.Double> lsdblRate = new java.util.ArrayList<java.lang.Double>();

		if (null == astrField[3] || astrField[3].isEmpty() ||
			org.drip.service.stream.Serializer.NULL_SER_STRING.equalsIgnoreCase (astrField[3]))
			throw new java.lang.Exception
				("NonlinearDiscountFactorDiscountCurve de-serializer: Cannot decode state");

		if (!org.drip.math.common.StringUtil.KeyValueListFromStringArray (lsdblDate, lsdblRate, astrField[3],
			getCollectionRecordDelimiter(), getCollectionKeyValueDelimiter()))
			throw new java.lang.Exception
				("NonlinearDiscountFactorDiscountCurve de-serializer: Cannot decode state");

		if (0 == lsdblDate.size() || 0 == lsdblRate.size() || lsdblDate.size() != lsdblRate.size())
			throw new java.lang.Exception
				("NonlinearDiscountFactorDiscountCurve de-serializer: Cannot decode state");

		_adblDate = new double[lsdblDate.size()];

		// _adblEndRate = new double[lsdblRate.size()];

		for (int i = 0; i < _adblDate.length; ++i) {
			_adblDate[i] = lsdblDate.get (i);

			// _adblEndRate[i] = lsdblRate.get (i);
		}
	}

	@Override public double df (
		final double dblDate)
		throws java.lang.Exception
	{
		if (!org.drip.math.common.NumberUtil.IsValid (dblDate))
			throw new java.lang.Exception ("NonlinearDiscountFactorDiscountCurve::df => Invalid Inputs");

		if (dblDate <= _dblEpochDate) return 1.;

		if (dblDate <= _adblDate[0])
			return java.lang.Math.exp (-1. * _dblLeftFlatForwardRate * (dblDate - _dblEpochDate) / 365.25);

		return dblDate <= _adblDate[_adblDate.length - 1] ? _msr.responseValue (dblDate) : java.lang.Math.exp (-1.
			* _dblRightFlatForwardRate * (dblDate - _dblEpochDate) / 365.25);
	}

	@Override public NonlinearDiscountFactorDiscountCurve parallelShiftManifestMeasure (
		final double dblShift)
	{
		if (!org.drip.math.common.NumberUtil.IsValid (dblShift) || null == _ccis) return null;

		org.drip.product.definition.CalibratableComponent[] aCalibInst = _ccis.getComponent();

		org.drip.analytics.support.CaseInsensitiveTreeMap<java.lang.Double> mapQuote = _ccis.getQuote();

		int iNumComp = aCalibInst.length;
		double[] adblShiftedManifestMeasure = new double[iNumComp];

		for (int i = 0; i < iNumComp; ++i)
			adblShiftedManifestMeasure[i] = mapQuote.get (aCalibInst[i].getPrimaryCode()) + dblShift;

		return shiftManifestMeasure (adblShiftedManifestMeasure);
	}

	@Override public NonlinearDiscountFactorDiscountCurve shiftManifestMeasure (
		final int iSpanIndex,
		final double dblShift)
	{
		if (!org.drip.math.common.NumberUtil.IsValid (dblShift) || null == _ccis) return null;

		org.drip.product.definition.CalibratableComponent[] aCalibInst = _ccis.getComponent();

		org.drip.analytics.support.CaseInsensitiveTreeMap<java.lang.Double> mapQuote = _ccis.getQuote();

		int iNumComp = aCalibInst.length;
		double[] adblShiftedManifestMeasure = new double[iNumComp];

		if (iSpanIndex >= iNumComp) return null;

		for (int i = 0; i < iNumComp; ++i)
			adblShiftedManifestMeasure[i] = mapQuote.get (aCalibInst[i].getPrimaryCode()) + (iSpanIndex == i
				? dblShift : 0.);

		return shiftManifestMeasure (adblShiftedManifestMeasure);
	}

	@Override public org.drip.analytics.definition.ExplicitBootDiscountCurve customTweakManifestMeasure (
		final org.drip.param.definition.ResponseValueTweakParams rvtp)
	{
		if (null == rvtp) return null;

		org.drip.product.definition.CalibratableComponent[] aCalibInst = _ccis.getComponent();

		org.drip.analytics.support.CaseInsensitiveTreeMap<java.lang.Double> mapQuote = _ccis.getQuote();

		int iNumComp = aCalibInst.length;
		double[] adblManifestMeasure = new double[iNumComp];

		for (int i = 0; i < iNumComp; ++i)
			adblManifestMeasure[i] = mapQuote.get (aCalibInst[i].getPrimaryCode());

		return shiftManifestMeasure (org.drip.analytics.support.AnalyticsHelper.TweakManifestMeasure
			(adblManifestMeasure, rvtp));
	}

	@Override public NonlinearDiscountFactorDiscountCurve parallelShiftQuantificationMetric (
		final double dblShift)
	{
		return null;
	}

	@Override public org.drip.analytics.definition.Curve customTweakQuantificationMetric (
		final org.drip.param.definition.ResponseValueTweakParams rvtp)
	{
		return null;
	}

	@Override public NonlinearDiscountFactorDiscountCurve createBasisRateShiftedCurve (
		final double[] adblDate,
		final double[] adblBasis)
	{
		if (null == adblDate || 0 == adblDate.length || null == adblBasis || 0 == adblBasis.length ||
			adblDate.length != adblBasis.length)
			return null;

		try {
			double[] adblCDFRate = new double[adblBasis.length];

			for (int i = 0; i < adblDate.length; ++i)
				adblCDFRate[i] = zero (adblDate[i]) + adblBasis[i];

			return new NonlinearDiscountFactorDiscountCurve (new org.drip.analytics.date.JulianDate
				(_dblEpochDate), _strCurrency, adblDate, adblCDFRate);
		} catch (java.lang.Exception e) {
			e.printStackTrace();
		}

		return null;
	}

	@Override public java.lang.String latentStateQuantificationMetric()
	{
		return org.drip.analytics.definition.DiscountCurve.QUANTIFICATION_METRIC_DISCOUNT_FACTOR;
	}

	@Override public org.drip.math.calculus.WengertJacobian dfJack (
		final double dblDate)
	{
		if (!org.drip.math.common.NumberUtil.IsValid (dblDate)) return null;

		org.drip.math.calculus.WengertJacobian wj = null;

		try {
			wj = new org.drip.math.calculus.WengertJacobian (1, _adblDate.length);
		} catch (java.lang.Exception e) {
			e.printStackTrace();

			return null;
		}

		for (int i = 0; i < _adblDate.length; ++i) {
			if (!wj.accumulatePartialFirstDerivative (0, i, 0.)) return null;
		}

		if (dblDate <= _dblEpochDate) return wj;

		if (dblDate <= _adblDate[0]) {
			try {
				return wj.accumulatePartialFirstDerivative (0, 0, (dblDate - _dblEpochDate) / (_adblDate[0] -
					_dblEpochDate) * java.lang.Math.exp (_dblLeftFlatForwardRate * (_adblDate[0] - dblDate) /
						365.25)) ? wj : null;
			} catch (java.lang.Exception e) {
				e.printStackTrace();

				return null;
			}
		}

		if (dblDate <= _adblDate[_adblDate.length - 1]) return _msr.jackDResponseDResponseInput (dblDate);

		try {
			return wj.accumulatePartialFirstDerivative (0, _adblDate.length - 1, (dblDate - _dblEpochDate) /
				(_adblDate[_adblDate.length - 1] - _dblEpochDate) * java.lang.Math.exp
					(_dblRightFlatForwardRate * (_adblDate[_adblDate.length - 1] - dblDate) / 365.25)) ? wj :
						null;
		} catch (java.lang.Exception e) {
			e.printStackTrace();
		}

		return null;
	}

	@Override public boolean setNodeValue (
		final int iNodeIndex,
		final double dblNodeDF)
	{
		if (!org.drip.math.common.NumberUtil.IsValid (dblNodeDF)) return false;

		int iNumDate = _adblDate.length;

		if (iNodeIndex > iNumDate) return false;

		if (0 == iNodeIndex) {
			_dblLeftFlatForwardRate = -365.25 * java.lang.Math.log (_dblLeftNodeDF = dblNodeDF) /
				(_adblDate[0] - _dblEpochDate);

			return true;
		}

		if (1 == iNodeIndex) return _msr.setLeftNode (_dblLeftNodeDF, _dblLeftNodeDFSlope, dblNodeDF);

		if (iNumDate - 1 == iNodeIndex) {
			try {
				_dblRightFlatForwardRate = -365.25 * java.lang.Math.log (_msr.responseValue
					(_adblDate[iNodeIndex])) / (_adblDate[iNodeIndex] - _dblEpochDate);
			} catch (java.lang.Exception e) {
				e.printStackTrace();

				return false;
			}
		}

		return _msr.resetNode (iNodeIndex, dblNodeDF);
	}

	@Override public boolean bumpNodeValue (
		final int iNodeIndex,
		final double dblValue)
	{
		return false;
	}

	@Override public boolean setFlatValue (
		final double dblValue)
	{
		return false;
	}

	public boolean initializeCalibrationRun (
		final double dblLeftSlope)
	{
		return org.drip.math.common.NumberUtil.IsValid (_dblLeftNodeDFSlope = dblLeftSlope);
	}

	/**
	 * Calculate the calibration metric for the node
	 * 
	 * @return Calibration Metric
	 * 
	 * @throws java.lang.Exception
	 */

	public double getCalibrationMetric()
		throws java.lang.Exception
	{
		return _msr.calcRightEdgeDerivative (2);
	}

	@Override public byte[] serialize()
	{
		java.lang.StringBuffer sb = new java.lang.StringBuffer();

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
}
