
package org.drip.state.curve;

/*
 * -*- mode: java; tab-width: 4; indent-tabs-mode: nil; c-basic-offset: 4 -*-
 */

/*!
 * Copyright (C) 2014 Lakshmi Krishnamurthy
 * Copyright (C) 2013 Lakshmi Krishnamurthy
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
 * NonlinearDiscountFactorDiscountCurve manages the Discounting Latent State, using the Forward Rate as the
 *  State Response Representation. It exports the following functionality:
 *  - Boot Methods - Set/Bump Specific Node Quantification Metric, or Set Flat Value
 *  - Boot Calibration - Initialize Run, Compute Calibration Metric
 *  - Compute the discount factor, forward rate, or the zero rate from the Forward Rate Latent State
 *  - Create a ForwardRateEstimator instance for the given Index
 *  - Retrieve Array of the Calibration Components
 *  - Retrieve the Curve Construction Input Set
 *  - Compute the Jacobian of the Discount Factor Latent State to the input Quote
 *  - Synthesize scenario Latent State by parallel shifting/custom tweaking the quantification metric
 *  - Synthesize scenario Latent State by parallel/custom shifting/custom tweaking the manifest measure
 *  - Serialize into and de-serialize out of byte array
 *
 * @author Lakshmi Krishnamurthy
 */

public class NonlinearDiscountFactorDiscountCurve extends
	org.drip.analytics.rates.ExplicitBootDiscountCurve {
	private double[] _adblDate = null;
	private double _dblLeftNodeDF = java.lang.Double.NaN;
	private double _dblLeftNodeDFSlope = java.lang.Double.NaN;
	private double _dblLeftFlatForwardRate = java.lang.Double.NaN;
	private double _dblRightFlatForwardRate = java.lang.Double.NaN;
	private org.drip.spline.stretch.MultiSegmentSequence _msr = null;

	private NonlinearDiscountFactorDiscountCurve shiftManifestMeasure (
		final double[] adblShiftedManifestMeasure)
	{
		if (!org.drip.quant.common.NumberUtil.IsValid (adblShiftedManifestMeasure) || null == _ccis)
			return null;

		org.drip.param.valuation.ValuationParams valParam = _ccis.valuationParameter();

		org.drip.param.valuation.ValuationCustomizationParams quotingParam = _ccis.quotingParameter();

		org.drip.product.definition.CalibratableFixedIncomeComponent[] aCalibInst = _ccis.components();

		org.drip.analytics.support.CaseInsensitiveTreeMap<java.lang.String[]> mapMeasures =
			_ccis.measures();

		org.drip.param.market.LatentStateFixingsContainer lsfc = _ccis.fixing();

		org.drip.state.estimator.NonlinearCurveCalibrator calibrator = new
			org.drip.state.estimator.NonlinearCurveCalibrator();

		int iNumComp = aCalibInst.length;
		java.lang.String[] astrCalibMeasure = new java.lang.String[iNumComp];

		try {
			NonlinearDiscountFactorDiscountCurve nldfdc = new NonlinearDiscountFactorDiscountCurve (new
				org.drip.analytics.date.JulianDate (_dblEpochDate), _strCurrency, collateralParams(),
					_adblDate, adblShiftedManifestMeasure);

			for (int i = 0; i < iNumComp; ++i) {
				java.lang.String strInstrumentCode = aCalibInst[i].primaryCode();

				calibrator.calibrateIRNode (nldfdc, null, aCalibInst[i], i, valParam, astrCalibMeasure[i] =
					mapMeasures.get (strInstrumentCode)[0], adblShiftedManifestMeasure[i], lsfc,
						quotingParam, false, java.lang.Double.NaN);
			}

			return nldfdc.setCCIS (org.drip.analytics.input.BootCurveConstructionInput.Create (valParam,
				quotingParam, aCalibInst, adblShiftedManifestMeasure, astrCalibMeasure, lsfc)) ? nldfdc :
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
	 * @param collatParams Collateralization Parameters
	 * @param adblDate Array of Dates
	 * @param adblRate Array of Forward Rates
	 * 
	 * @throws java.lang.Exception Thrown if the curve cannot be created
	 */

	public NonlinearDiscountFactorDiscountCurve (
		final org.drip.analytics.date.JulianDate dtStart,
		final java.lang.String strCurrency,
		final org.drip.param.valuation.CollateralizationParams collatParams,
		final double[] adblDate,
		final double[] adblRate)
		throws java.lang.Exception
	{
		super (dtStart.julian(), strCurrency, collatParams);

		if (null == adblDate || 0 == adblDate.length || null == adblRate || adblDate.length !=
			adblRate.length || null == dtStart)
			throw new java.lang.Exception ("NonlinearDiscountFactorDiscountCurve ctr: Invalid inputs");

		_dblEpochDate = dtStart.julian();

		org.drip.spline.params.SegmentCustomBuilderControl sbp = new
			org.drip.spline.params.SegmentCustomBuilderControl
				(org.drip.spline.stretch.MultiSegmentSequenceBuilder.BASIS_SPLINE_POLYNOMIAL, new
					org.drip.spline.basis.PolynomialFunctionSetParams (2),
						org.drip.spline.params.SegmentInelasticDesignControl.Create (0, 2), null, null);

		int iNumSegment = adblDate.length;
		_adblDate = new double[iNumSegment];
		double[] adblDF = new double[iNumSegment];
		org.drip.spline.params.SegmentCustomBuilderControl[] aSBP = new
			org.drip.spline.params.SegmentCustomBuilderControl[adblDate.length - 1];

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

		_msr = org.drip.spline.stretch.MultiSegmentSequenceBuilder.CreateCalibratedStretchEstimator
			("POLY_SPLINE_DF_REGIME", adblDate, adblDF, aSBP, null,
				org.drip.spline.stretch.BoundarySettings.NaturalStandard(),
					org.drip.spline.stretch.MultiSegmentSequence.CALIBRATE);
	}

	@Override public org.drip.param.valuation.CollateralizationParams collateralParams()
	{
		return null;
	}

	@Override public double df (
		final double dblDate)
		throws java.lang.Exception
	{
		if (!org.drip.quant.common.NumberUtil.IsValid (dblDate))
			throw new java.lang.Exception ("NonlinearDiscountFactorDiscountCurve::df => Invalid Inputs");

		if (dblDate <= _dblEpochDate) return 1.;

		if (dblDate <= _adblDate[0])
			return java.lang.Math.exp (-1. * _dblLeftFlatForwardRate * (dblDate - _dblEpochDate) / 365.25);

		return (dblDate <= _adblDate[_adblDate.length - 1] ? _msr.responseValue (dblDate) :
			java.lang.Math.exp (-1. * _dblRightFlatForwardRate * (dblDate - _dblEpochDate) / 365.25))  *
				turnAdjust (epoch().julian(), dblDate);
	}

	@Override public double forward (
		final double dblDate1,
		final double dblDate2)
		throws java.lang.Exception
	{
		if (!org.drip.quant.common.NumberUtil.IsValid (dblDate1) || !org.drip.quant.common.NumberUtil.IsValid
			(dblDate2))
			throw new java.lang.Exception ("NonlinearDiscountFactorDiscountCurve::forward => Invalid input");

		double dblStartDate = epoch().julian();

		if (dblDate1 < dblStartDate || dblDate2 < dblStartDate) return 0.;

		return 365.25 / (dblDate2 - dblDate1) * java.lang.Math.log (df (dblDate1) / df (dblDate2));
	}

	@Override public double zero (
		final double dblDate)
		throws java.lang.Exception
	{
		if (!org.drip.quant.common.NumberUtil.IsValid (dblDate))
			throw new java.lang.Exception ("NonlinearDiscountFactorDiscountCurve::zero => Invalid Date");

		double dblStartDate = epoch().julian();

		if (dblDate < dblStartDate) return 0.;

		return -365.25 / (dblDate - dblStartDate) * java.lang.Math.log (df (dblDate));
	}

	@Override public org.drip.analytics.rates.ForwardRateEstimator forwardRateEstimator (
		final double dblDate,
		final org.drip.state.identifier.ForwardLabel fri)
	{
		return null;
	}

	@Override public NonlinearDiscountFactorDiscountCurve parallelShiftManifestMeasure (
		final java.lang.String strManifestMeasure,
		final double dblShift)
	{
		if (!org.drip.quant.common.NumberUtil.IsValid (dblShift) || null == _ccis) return null;

		org.drip.product.definition.CalibratableFixedIncomeComponent[] aCalibInst = _ccis.components();

		org.drip.analytics.support.CaseInsensitiveTreeMap<org.drip.analytics.support.CaseInsensitiveTreeMap<java.lang.Double>>
			mapQuote = _ccis.quoteMap();

		int iNumComp = aCalibInst.length;
		double[] adblShiftedManifestMeasure = new double[iNumComp];

		for (int i = 0; i < iNumComp; ++i)
			adblShiftedManifestMeasure[i] = mapQuote.get (aCalibInst[i].primaryCode()).get
				(strManifestMeasure) + dblShift;

		return shiftManifestMeasure (adblShiftedManifestMeasure);
	}

	@Override public NonlinearDiscountFactorDiscountCurve shiftManifestMeasure (
		final int iSpanIndex,
		final java.lang.String strManifestMeasure,
		final double dblShift)
	{
		if (!org.drip.quant.common.NumberUtil.IsValid (dblShift) || null == _ccis) return null;

		org.drip.product.definition.CalibratableFixedIncomeComponent[] aCalibInst = _ccis.components();

		org.drip.analytics.support.CaseInsensitiveTreeMap<org.drip.analytics.support.CaseInsensitiveTreeMap<java.lang.Double>>
			mapQuote = _ccis.quoteMap();

		int iNumComp = aCalibInst.length;
		double[] adblShiftedManifestMeasure = new double[iNumComp];

		if (iSpanIndex >= iNumComp) return null;

		for (int i = 0; i < iNumComp; ++i)
			adblShiftedManifestMeasure[i] = mapQuote.get (aCalibInst[i].primaryCode()).get
				(strManifestMeasure) + (iSpanIndex == i ? dblShift : 0.);

		return shiftManifestMeasure (adblShiftedManifestMeasure);
	}

	@Override public org.drip.analytics.rates.ExplicitBootDiscountCurve customTweakManifestMeasure (
		final java.lang.String strManifestMeasure,
		final org.drip.param.definition.ResponseValueTweakParams rvtp)
	{
		if (null == rvtp) return null;

		org.drip.product.definition.CalibratableFixedIncomeComponent[] aCalibInst = _ccis.components();

		org.drip.analytics.support.CaseInsensitiveTreeMap<org.drip.analytics.support.CaseInsensitiveTreeMap<java.lang.Double>>
			mapQuote = _ccis.quoteMap();

		int iNumComp = aCalibInst.length;
		double[] adblManifestMeasure = new double[iNumComp];

		for (int i = 0; i < iNumComp; ++i)
			adblManifestMeasure[i] = mapQuote.get (aCalibInst[i].primaryCode()).get (strManifestMeasure);

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
				(_dblEpochDate), _strCurrency, collateralParams(), adblDate, adblCDFRate);
		} catch (java.lang.Exception e) {
			e.printStackTrace();
		}

		return null;
	}

	@Override public java.lang.String latentStateQuantificationMetric()
	{
		return org.drip.analytics.definition.LatentStateStatic.DISCOUNT_QM_ZERO_RATE;
	}

	@Override public org.drip.quant.calculus.WengertJacobian jackDDFDManifestMeasure (
		final double dblDate,
		final java.lang.String strManifestMeasure)
	{
		if (!org.drip.quant.common.NumberUtil.IsValid (dblDate)) return null;

		org.drip.quant.calculus.WengertJacobian wj = null;

		try {
			wj = new org.drip.quant.calculus.WengertJacobian (1, _adblDate.length);
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

		if (dblDate <= _adblDate[_adblDate.length - 1])
			return _msr.jackDResponseDCalibrationInput (dblDate, 1);

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
		if (!org.drip.quant.common.NumberUtil.IsValid (dblNodeDF)) return false;

		int iNumDate = _adblDate.length;

		if (iNodeIndex > iNumDate) return false;

		if (0 == iNodeIndex) {
			_dblLeftFlatForwardRate = -365.25 * java.lang.Math.log (_dblLeftNodeDF = dblNodeDF) /
				(_adblDate[0] - _dblEpochDate);

			return true;
		}

		if (1 == iNodeIndex) return _msr.setLeftNode (_dblLeftNodeDF, _dblLeftNodeDFSlope, dblNodeDF, null);

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
		return org.drip.quant.common.NumberUtil.IsValid (_dblLeftNodeDFSlope = dblLeftSlope);
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
}
