
package org.drip.state.curve;

/*
 * -*- mode: java; tab-width: 4; indent-tabs-mode: nil; c-basic-offset: 4 -*-
 */

/*!
 * Copyright (C) 2015 Lakshmi Krishnamurthy
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
 * FlatForwardDiscountCurve manages the Discounting Latent State, using the Forward Rate as the State
 *  Response Representation. It exports the following functionality:
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

public class FlatForwardDiscountCurve extends org.drip.analytics.rates.ExplicitBootDiscountCurve {
	private double _adblDate[] = null;
	private int _iCompoundingFreq = -1;
	private double _adblForwardRate[] = null;
	private boolean _bDiscreteCompounding = false;
	private java.lang.String _strCompoundingDayCount = "";

	private double yearFract (
		final double dblStartDate,
		final double dblEndDate)
		throws java.lang.Exception
	{
		return _bDiscreteCompounding ? org.drip.analytics.daycount.Convention.YearFraction (dblStartDate,
			dblEndDate, _strCompoundingDayCount, false, null, currency()) : (dblEndDate - dblStartDate) /
				365.25;
	}

	private FlatForwardDiscountCurve shiftManifestMeasure (
		final double[] adblShift)
	{
		if (!org.drip.quant.common.NumberUtil.IsValid (adblShift) || null == _ccis) return null;

		org.drip.product.definition.CalibratableFixedIncomeComponent[] aCalibInst = _ccis.components();

		org.drip.param.valuation.ValuationParams valParam = _ccis.valuationParameter();

		org.drip.param.valuation.ValuationCustomizationParams quotingParam = _ccis.quotingParameter();

		org.drip.analytics.support.CaseInsensitiveTreeMap<org.drip.analytics.support.CaseInsensitiveTreeMap<java.lang.Double>>
			mapQuote = _ccis.quoteMap();

		org.drip.analytics.support.CaseInsensitiveTreeMap<java.lang.String[]> mapMeasures =
			_ccis.measures();

		org.drip.param.market.LatentStateFixingsContainer lsfc = _ccis.fixing();

		org.drip.state.estimator.NonlinearCurveCalibrator calibrator = new
			org.drip.state.estimator.NonlinearCurveCalibrator();

		int iNumComp = aCalibInst.length;
		double[] adblCalibQuoteShifted = new double[iNumComp];
		java.lang.String[] astrCalibMeasure = new java.lang.String[iNumComp];

		if (adblShift.length != iNumComp) return null;

		try {
			FlatForwardDiscountCurve frdc = new FlatForwardDiscountCurve (new
				org.drip.analytics.date.JulianDate (_dblEpochDate), _strCurrency, collateralParams(),
					_adblDate, _adblForwardRate, _bDiscreteCompounding, _strCompoundingDayCount,
						_iCompoundingFreq);

			for (int i = 0; i < iNumComp; ++i) {
				java.lang.String strInstrumentCode = aCalibInst[i].primaryCode();

				calibrator.calibrateIRNode (frdc, null, aCalibInst[i], i, valParam, astrCalibMeasure[i] =
					mapMeasures.get (strInstrumentCode)[0], adblCalibQuoteShifted[i] = mapQuote.get
						(strInstrumentCode).get (astrCalibMeasure[i]) + adblShift[i], lsfc, quotingParam,
							false, java.lang.Double.NaN);
			}

			return frdc.setCCIS (new org.drip.analytics.input.BootCurveConstructionInput (valParam,
				quotingParam, aCalibInst, mapQuote, mapMeasures, lsfc)) ? frdc : null;
		} catch (java.lang.Exception e) {
			e.printStackTrace();
		}

		return null;
	}

	/**
	 * Boot-strap a constant forward discount curve from an array of dates and discount rates
	 * 
	 * @param dtStart Epoch Date
	 * @param strCurrency Currency
	 * @param collatParams Collateralization Parameters
	 * @param adblDate Array of Dates
	 * @param adblForwardRate Array of Forward Rates
	 * @param bDiscreteCompounding TRUE => Compounding is Discrete
	 * @param strCompoundingDayCount Day Count Convention to be used for Discrete Compounding
	 * @param iCompoundingFreq Frequency to be used for Discrete Compounding
	 * 
	 * @throws java.lang.Exception Thrown if the curve cannot be created
	 */

	public FlatForwardDiscountCurve (
		final org.drip.analytics.date.JulianDate dtStart,
		final java.lang.String strCurrency,
		final org.drip.param.valuation.CollateralizationParams collatParams,
		final double[] adblDate,
		final double[] adblForwardRate,
		final boolean bDiscreteCompounding,
		final java.lang.String strCompoundingDayCount,
		final int iCompoundingFreq)
		throws java.lang.Exception
	{
		super (dtStart.julian(), strCurrency, collatParams);

		if (null == adblDate || null == adblForwardRate)
			throw new java.lang.Exception ("FlatForwardDiscountCurve ctr: Invalid inputs");

		int iNumDate = adblDate.length;

		if (0 == iNumDate || iNumDate != adblForwardRate.length)
			throw new java.lang.Exception ("FlatForwardDiscountCurve ctr: Invalid inputs");

		_adblDate = new double[iNumDate];
		_iCompoundingFreq = iCompoundingFreq;
		_adblForwardRate = new double[iNumDate];
		_bDiscreteCompounding = bDiscreteCompounding;
		_strCompoundingDayCount = strCompoundingDayCount;

		for (int i = 0; i < iNumDate; ++i) {
			_adblDate[i] = adblDate[i];
			_adblForwardRate[i] = adblForwardRate[i];
		}
	}

	protected FlatForwardDiscountCurve (
		final FlatForwardDiscountCurve dc)
		throws java.lang.Exception
	{
		super (dc.epoch().julian(), dc.currency(), dc.collateralParams());

		_adblDate = dc._adblDate;
		_strCurrency = dc._strCurrency;
		_dblEpochDate = dc._dblEpochDate;
		_adblForwardRate = dc._adblForwardRate;
		_iCompoundingFreq = dc._iCompoundingFreq;
		_bDiscreteCompounding = dc._bDiscreteCompounding;
		_strCompoundingDayCount = dc._strCompoundingDayCount;
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
			throw new java.lang.Exception ("FlatForwardDiscountCurve::df => Got NaN for date");

		if (dblDate <= _dblEpochDate) return 1.;

		int i = 0;
		double dblDF = 1.;
		double dblExpArg = 0.;
		int iNumDate = _adblDate.length;
		double dblStartDate = _dblEpochDate;

		while (i < iNumDate && (int) dblDate >= (int) _adblDate[i]) {
			if (_bDiscreteCompounding)
				dblDF *= java.lang.Math.pow (1. + (_adblForwardRate[i] / _iCompoundingFreq), yearFract
					(dblStartDate, _adblDate[i]) * _iCompoundingFreq);
			else
				dblExpArg -= _adblForwardRate[i] * yearFract (dblStartDate, _adblDate[i]);

			dblStartDate = _adblDate[i++];
		}

		if (i >= iNumDate) i = iNumDate - 1;

		if (_bDiscreteCompounding)
			dblDF *= java.lang.Math.pow (1. + (_adblForwardRate[i] / _iCompoundingFreq), yearFract
				(dblStartDate, dblDate) * _iCompoundingFreq);
		else
			dblExpArg -= _adblForwardRate[i] * yearFract (dblStartDate, dblDate);

		return (_bDiscreteCompounding ? dblDF : java.lang.Math.exp (dblExpArg)) * turnAdjust (_dblEpochDate,
			dblDate);
	}

	@Override public double forward (
		final double dblDate1,
		final double dblDate2)
		throws java.lang.Exception
	{
		if (!org.drip.quant.common.NumberUtil.IsValid (dblDate1) || !org.drip.quant.common.NumberUtil.IsValid
			(dblDate2))
			throw new java.lang.Exception ("FlatForwardDiscountCurve::forward => Invalid input");

		double dblStartDate = epoch().julian();

		if (dblDate1 < dblStartDate || dblDate2 < dblStartDate) return 0.;

		return 365.25 / (dblDate2 - dblDate1) * java.lang.Math.log (df (dblDate1) / df (dblDate2));
	}

	@Override public double zero (
		final double dblDate)
		throws java.lang.Exception
	{
		if (!org.drip.quant.common.NumberUtil.IsValid (dblDate))
			throw new java.lang.Exception ("FlatForwardDiscountCurve::zero => Invalid Date");

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

	@Override public java.util.Map<java.lang.Double, java.lang.Double> canonicalTruthness (
		final java.lang.String strLatentQuantificationMetric)
	{
		return null;
	}

	@Override public FlatForwardDiscountCurve parallelShiftManifestMeasure (
		final java.lang.String strManifestMeasure,
		final double dblShift)
	{
		if (!org.drip.quant.common.NumberUtil.IsValid (dblShift) || null == _ccis) return null;

		org.drip.product.definition.CalibratableFixedIncomeComponent[] aCalibInst = _ccis.components();

		int iNumComp = aCalibInst.length;
		double[] adblShift = new double[iNumComp];

		for (int i = 0; i < iNumComp; ++i)
			adblShift[i] = dblShift;

		return shiftManifestMeasure (adblShift);
	}

	@Override public FlatForwardDiscountCurve shiftManifestMeasure (
		final int iSpanIndex,
		final java.lang.String strManifestMeasure,
		final double dblShift)
	{
		if (!org.drip.quant.common.NumberUtil.IsValid (dblShift) || null == _ccis) return null;

		org.drip.product.definition.CalibratableFixedIncomeComponent[] aCalibInst = _ccis.components();

		int iNumComp = aCalibInst.length;
		double[] adblShift = new double[iNumComp];

		if (iSpanIndex >= iNumComp) return null;

		for (int i = 0; i < iNumComp; ++i)
			adblShift[i] = i == iSpanIndex ? dblShift : 0.;

		return shiftManifestMeasure (adblShift);
	}

	@Override public org.drip.analytics.rates.ExplicitBootDiscountCurve customTweakManifestMeasure (
		final java.lang.String strManifestMeasure,
		final org.drip.param.definition.ResponseValueTweakParams rvtp)
	{
		return shiftManifestMeasure (org.drip.analytics.support.AnalyticsHelper.TweakManifestMeasure
			(_adblForwardRate, rvtp));
	}

	@Override public FlatForwardDiscountCurve parallelShiftQuantificationMetric (
		final double dblShift)
	{
		if (!org.drip.quant.common.NumberUtil.IsValid (dblShift)) return null;

		int iNumDate = _adblForwardRate.length;
		double[] adblForwardRate = new double[iNumDate];

		for (int i = 0; i < iNumDate; ++i)
			adblForwardRate[i] = _adblForwardRate[i] + dblShift;

		try {
			return new FlatForwardDiscountCurve (new org.drip.analytics.date.JulianDate (_dblEpochDate),
				_strCurrency, collateralParams(), _adblDate, adblForwardRate, _bDiscreteCompounding,
					_strCompoundingDayCount, _iCompoundingFreq);
		} catch (java.lang.Exception e) {
			e.printStackTrace();
		}

		return null;
	}

	@Override public org.drip.analytics.definition.Curve customTweakQuantificationMetric (
		final org.drip.param.definition.ResponseValueTweakParams rvtp)
	{
		try {
			return new FlatForwardDiscountCurve (new org.drip.analytics.date.JulianDate (_dblEpochDate),
				_strCurrency, collateralParams(), _adblDate,
					org.drip.analytics.support.AnalyticsHelper.TweakManifestMeasure (_adblForwardRate,
						rvtp), _bDiscreteCompounding, _strCompoundingDayCount, _iCompoundingFreq);
		} catch (java.lang.Exception e) {
			e.printStackTrace();
		}

		return null;
	}

	@Override public FlatForwardDiscountCurve createBasisRateShiftedCurve (
		final double[] adblDate,
		final double[] adblBasis)
	{
		if (null == adblDate || null == adblBasis) return null;

		int iNumDate = adblDate.length;

		if (0 == iNumDate || iNumDate != adblBasis.length) return null;

		double[] adblShiftedRate = new double[iNumDate];

		try {
			for (int i = 0; i < adblDate.length; ++i)
				adblShiftedRate[i] = zero (adblDate[i]) + adblBasis[i];

			return new FlatForwardDiscountCurve (new org.drip.analytics.date.JulianDate (_dblEpochDate),
				_strCurrency, collateralParams(), adblDate, adblShiftedRate, _bDiscreteCompounding,
					_strCompoundingDayCount, _iCompoundingFreq);
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

		int i = 0;
		double dblDF = java.lang.Double.NaN;
		double dblStartDate = _dblEpochDate;
		org.drip.quant.calculus.WengertJacobian wj = null;

		try {
			wj = new org.drip.quant.calculus.WengertJacobian (1, _adblForwardRate.length);
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

		while (i < _adblForwardRate.length && (int) dblDate >= (int) _adblDate[i]) {
			if (!wj.accumulatePartialFirstDerivative (0, i, dblDF * (dblStartDate - _adblDate[i]) / 365.25))
				return null;

			dblStartDate = _adblDate[i++];
		}

		if (i >= _adblForwardRate.length) i = _adblForwardRate.length - 1;

		return wj.accumulatePartialFirstDerivative (0, i, dblDF * (dblStartDate - dblDate) / 365.25) ? wj :
			null;
	}

	@Override public boolean setNodeValue (
		final int iNodeIndex,
		final double dblValue)
	{
		if (!org.drip.quant.common.NumberUtil.IsValid (dblValue) || iNodeIndex > _adblForwardRate.length)
			return false;

		for (int i = iNodeIndex; i < _adblForwardRate.length; ++i)
			_adblForwardRate[i] = dblValue;

		return true;
	}

	@Override public boolean bumpNodeValue (
		final int iNodeIndex,
		final double dblValue)
	{
		if (!org.drip.quant.common.NumberUtil.IsValid (dblValue) || iNodeIndex > _adblForwardRate.length)
			return false;

		for (int i = iNodeIndex; i < _adblForwardRate.length; ++i)
			_adblForwardRate[i] += dblValue;

		return true;
	}

	@Override public boolean setFlatValue (
		final double dblValue)
	{
		if (!org.drip.quant.common.NumberUtil.IsValid (dblValue)) return false;

		for (int i = 0; i < _adblForwardRate.length; ++i)
			_adblForwardRate[i] = dblValue;

		return true;
	}
}
