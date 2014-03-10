
package org.drip.state.estimator;

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
 * RatesSegmentSequenceBuilder holds the logic behind building the bootstrap segments contained in the given
 * 	Stretch.
 * 
 * It extends SegmentSequenceBuilder by implementing/customizing the calibration of the starting as well as
 *  the subsequent segments.
 *
 * @author Lakshmi Krishnamurthy
 */

public class RatesSegmentSequenceBuilder implements org.drip.spline.stretch.SegmentSequenceBuilder {
	private double _dblEpochResponse = java.lang.Double.NaN;
	private org.drip.spline.stretch.BoundarySettings _bs = null;
	private org.drip.state.estimator.CurveStretch _stretch = null;
	private org.drip.param.pricer.PricerParams _pricerParams = null;
	private org.drip.param.valuation.ValuationParams _valParams = null;
	private org.drip.spline.params.StretchBestFitResponse _sbfr = null;
	private org.drip.param.definition.ComponentMarketParams _cmp = null;
	private org.drip.param.valuation.ValuationCustomizationParams _quotingParams = null;
	private org.drip.spline.stretch.MultiSegmentSequence _mssPrev = null;
	private org.drip.state.estimator.StretchRepresentationSpec _srs = null;
	private org.drip.spline.params.StretchBestFitResponse _sbfrQuoteSensitivity = null;
	private
		org.drip.analytics.support.CaseInsensitiveHashMap<org.drip.spline.params.PreceedingManifestSensitivityControl>
			_mapPMSC = null;

	private java.util.Map<java.lang.Double, org.drip.spline.params.ResponseValueSensitivityConstraint>
		_mapRVSC = new
			java.util.HashMap<java.lang.Double, org.drip.spline.params.ResponseValueSensitivityConstraint>();

	/**
	 * Construct a RatesSegmentSequenceBuilder instance from the Parameters
	 * 
	 * @param dblEpochResponse Segment Sequence Left-most Response Value
	 * @param srs Stretch Representation
	 * @param valParams Valuation Parameter
	 * @param pricerParams Pricer Parameter
	 * @param cmp Component Market Parameter
	 * @param quotingParams Quoting Parameter
	 * @param mssPrev The Previous Stretch Used to value cash flows that fall in those segments
	 * @param sbfr Stretch Fitness Weighted Response
	 * @param pmsc Preceeding Manifest Sensitivity Control Parameters
	 * @param sbfrQuoteSensitivity Stretch Fitness Weighted Response Quote Sensitivity
	 * @param bs The Calibration Boundary Condition
	 * 
	 * @return The RatesSegmentSequenceBuilder instance
	 */

	public static final RatesSegmentSequenceBuilder Create (
		final double dblEpochResponse,
		final org.drip.state.estimator.StretchRepresentationSpec srs,
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.pricer.PricerParams pricerParams,
		final org.drip.param.definition.ComponentMarketParams cmp,
		final org.drip.param.valuation.ValuationCustomizationParams quotingParams,
		final org.drip.spline.stretch.MultiSegmentSequence mssPrev,
		final org.drip.spline.params.StretchBestFitResponse sbfr,
		final org.drip.spline.params.PreceedingManifestSensitivityControl pmsc,
		final org.drip.spline.params.StretchBestFitResponse sbfrQuoteSensitivity,
		final org.drip.spline.stretch.BoundarySettings bs)
	{
		if (null == srs) return null;

		org.drip.analytics.support.CaseInsensitiveHashMap<org.drip.spline.params.PreceedingManifestSensitivityControl>
			mapPMSC = new
				org.drip.analytics.support.CaseInsensitiveHashMap<org.drip.spline.params.PreceedingManifestSensitivityControl>();

		if (null != pmsc) {
			for (int iSegment = 0; iSegment < srs.getCalibComp().length; ++iSegment) {
				for (java.lang.String strManifestMeasure : srs.getLSMM (iSegment).getManifestMeasures())
					mapPMSC.put (strManifestMeasure, pmsc);
			}
		}

		try {
			return new RatesSegmentSequenceBuilder (dblEpochResponse, srs, valParams, pricerParams, cmp,
				quotingParams, mssPrev, sbfr, mapPMSC, sbfrQuoteSensitivity, bs);
		} catch (java.lang.Exception e) {
			e.printStackTrace();
		}

		return null;
	}

	private org.drip.spline.params.PreceedingManifestSensitivityControl getPMSC (
		final java.lang.String strManifestMeasure)
	{
		return _mapPMSC.containsKey (strManifestMeasure) ? _mapPMSC.get (strManifestMeasure) : null;
	}

	private boolean generateSegmentConstraintSet (
		final double dblSegmentRight,
		final org.drip.state.estimator.PredictorResponseWeightConstraint prlc,
		final java.lang.String strManifestMeasure)
	{
		org.drip.spline.params.SegmentResponseValueConstraint srvcBase = segmentCalibResponseConstraint
			(prlc);

		if (null == srvcBase) return false;

		org.drip.spline.params.SegmentResponseValueConstraint srvcSensitivity = segmentSensResponseConstraint
			(prlc, strManifestMeasure);

		if (null == srvcSensitivity) return false;

		org.drip.spline.params.ResponseValueSensitivityConstraint rvsc = null;

		try {
			if (!(rvsc = new org.drip.spline.params.ResponseValueSensitivityConstraint
				(srvcBase)).addManifestMeasureSensitivity (strManifestMeasure, srvcSensitivity))
				return false;
		} catch (java.lang.Exception e) {
			e.printStackTrace();

			return false;
		}

		_mapRVSC.put (dblSegmentRight, rvsc);

		return true;
	}

	protected org.drip.spline.params.SegmentResponseValueConstraint segmentCalibResponseConstraint (
		final org.drip.state.estimator.PredictorResponseWeightConstraint prlc)
	{
		java.util.TreeMap<java.lang.Double, java.lang.Double> mapPredictorResponseWeight =
			prlc.getPredictorResponseWeight();

		if (null == mapPredictorResponseWeight || 0 == mapPredictorResponseWeight.size()) return null;

		java.util.Set<java.util.Map.Entry<java.lang.Double, java.lang.Double>> setPredictorResponseWeight =
			mapPredictorResponseWeight.entrySet();

		if (null == setPredictorResponseWeight || 0 == setPredictorResponseWeight.size()) return null;

		double dblValue = 0.;

		java.util.List<java.lang.Double> lsPredictor = new java.util.ArrayList<java.lang.Double>();

		java.util.List<java.lang.Double> lsResponseWeight = new java.util.ArrayList<java.lang.Double>();

		for (java.util.Map.Entry<java.lang.Double, java.lang.Double> me : setPredictorResponseWeight) {
			if (null == me) return null;

			double dblPredictorDate = me.getKey();

			try {
				if (null != _mssPrev && _mssPrev.in (dblPredictorDate))
					dblValue -= _mssPrev.responseValue (dblPredictorDate) * me.getValue();
				else if (null != _stretch && _stretch.inBuiltRange (dblPredictorDate))
					dblValue -= _stretch.responseValue (dblPredictorDate) * me.getValue();
				else {
					lsPredictor.add (dblPredictorDate);

					lsResponseWeight.add (me.getValue());
				}
			} catch (java.lang.Exception e) {
				e.printStackTrace();

				return null;
			}
		}

		int iSize = lsPredictor.size();

		double[] adblPredictor = new double[iSize];
		double[] adblResponseWeight = new double[iSize];

		for (int i = 0; i < iSize; ++i) {
			adblPredictor[i] = lsPredictor.get (i);

			adblResponseWeight[i] = lsResponseWeight.get (i);
		}

		try {
			return new org.drip.spline.params.SegmentResponseValueConstraint (adblPredictor,
				adblResponseWeight, (prlc.getValue()) + dblValue);
		} catch (java.lang.Exception e) {
			e.printStackTrace();
		}

		return null;
	}

	protected org.drip.spline.params.SegmentResponseValueConstraint segmentSensResponseConstraint (
		final org.drip.state.estimator.PredictorResponseWeightConstraint prlc,
		final java.lang.String strManifestMeasure)
	{
		java.util.TreeMap<java.lang.Double, java.lang.Double> mapPredictorResponseWeight =
			prlc.getDResponseWeightDManifestMeasure (strManifestMeasure);

		if (null == mapPredictorResponseWeight || 0 == mapPredictorResponseWeight.size()) return null;

		java.util.Set<java.util.Map.Entry<java.lang.Double, java.lang.Double>> setPredictorResponseWeight =
			mapPredictorResponseWeight.entrySet();

		if (null == setPredictorResponseWeight || 0 == setPredictorResponseWeight.size()) return null;

		double dblValue = 0.;

		java.util.List<java.lang.Double> lsPredictor = new java.util.ArrayList<java.lang.Double>();

		java.util.List<java.lang.Double> lsResponseWeight = new java.util.ArrayList<java.lang.Double>();

		for (java.util.Map.Entry<java.lang.Double, java.lang.Double> me : setPredictorResponseWeight) {
			if (null == me) return null;

			double dblPredictorDate = me.getKey();

			try {
				if (null != _mssPrev && _mssPrev.in (dblPredictorDate))
					dblValue -= _mssPrev.responseValue (dblPredictorDate) * me.getValue();
				else if (null != _stretch && _stretch.inBuiltRange (dblPredictorDate))
					dblValue -= _stretch.responseValue (dblPredictorDate) * me.getValue();
				else {
					lsPredictor.add (dblPredictorDate);

					lsResponseWeight.add (me.getValue());
				}
			} catch (java.lang.Exception e) {
				e.printStackTrace();

				return null;
			}
		}

		int iSize = lsPredictor.size();

		double[] adblPredictor = new double[iSize];
		double[] adblResponseWeight = new double[iSize];

		for (int i = 0; i < iSize; ++i) {
			adblPredictor[i] = lsPredictor.get (i);

			adblResponseWeight[i] = lsResponseWeight.get (i);
		}

		try {
			return new org.drip.spline.params.SegmentResponseValueConstraint (adblPredictor,
				adblResponseWeight, prlc.getDValueDManifestMeasure (strManifestMeasure) + dblValue);
		} catch (java.lang.Exception e) {
			e.printStackTrace();
		}

		return null;
	}

	/**
	 * RatesSegmentSequenceBuilder constructor
	 * 
	 * @param dblEpochResponse Segment Sequence Left-most Response Value
	 * @param srs Stretch Representation
	 * @param valParams Valuation Parameter
	 * @param pricerParams Pricer Parameter
	 * @param cmp Component Market Parameter
	 * @param quotingParams Quoting Parameter
	 * @param mssPrev The Previous Stretch Used to value cash flows that fall in those segments
	 * @param sbfr Stretch Fitness Weighted Response
	 * @param pmsc Preceeding Manifest Sensitivity Control Parameters
	 * @param sbfrQuoteSensitivity Stretch Fitness Weighted Response Quote Sensitivity
	 * @param bs The Calibration Boundary Condition
	 * 
	 * @throws java.lang.Exception Thrown if the Inputs are invalid
	 */

	public RatesSegmentSequenceBuilder (
		final double dblEpochResponse,
		final org.drip.state.estimator.StretchRepresentationSpec srs,
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.pricer.PricerParams pricerParams,
		final org.drip.param.definition.ComponentMarketParams cmp,
		final org.drip.param.valuation.ValuationCustomizationParams quotingParams,
		final org.drip.spline.stretch.MultiSegmentSequence mssPrev,
		final org.drip.spline.params.StretchBestFitResponse sbfr,
		final
			org.drip.analytics.support.CaseInsensitiveHashMap<org.drip.spline.params.PreceedingManifestSensitivityControl>
				mapPMSC,
		final org.drip.spline.params.StretchBestFitResponse sbfrQuoteSensitivity,
		final org.drip.spline.stretch.BoundarySettings bs)
		throws java.lang.Exception
	{
		if (!org.drip.quant.common.NumberUtil.IsValid (_dblEpochResponse = dblEpochResponse) || null == (_srs
			= srs) || null == (_valParams = valParams) || null == (_bs = bs) || null == (_mapPMSC = mapPMSC))
			throw new java.lang.Exception ("RatesSegmentSequenceBuilder ctr: Invalid Inputs");

		_cmp = cmp;
		_sbfr = sbfr;
		_mssPrev = mssPrev;
		_pricerParams = pricerParams;
		_quotingParams = quotingParams;
		_sbfrQuoteSensitivity = sbfrQuoteSensitivity;
	}

	@Override public boolean setStretch (
		final org.drip.spline.stretch.MultiSegmentSequence mss)
	{
		if (null == mss || !(mss instanceof org.drip.state.estimator.CurveStretch)) return false;

		_stretch = (org.drip.state.estimator.CurveStretch) mss;

		org.drip.spline.segment.LatentStateResponseModel[] aCS = _stretch.segments();

		if (null == aCS || aCS.length != _srs.getCalibComp().length) return false;

		return true;
	}

	@Override public org.drip.spline.stretch.BoundarySettings getCalibrationBoundaryCondition()
	{
		return _bs;
	}

	@Override public boolean calibStartingSegment (
		final double dblLeftSlope)
	{
		if (null == _stretch || !_stretch.setClearBuiltRange()) return false;

		org.drip.product.definition.CalibratableFixedIncomeComponent cc = _srs.getCalibComp (0);

		if (null == cc) return false;

		org.drip.spline.segment.LatentStateResponseModel[] aCS = _stretch.segments();

		if (null == aCS || 0 == aCS.length) return false;

		org.drip.state.estimator.PredictorResponseWeightConstraint prlc = cc.generateCalibPRLC (_valParams,
			_pricerParams, _cmp, _quotingParams, _srs.getLSMM (0));

		double dblSegmentRight = aCS[0].right();

		if (null == prlc || !generateSegmentConstraintSet (dblSegmentRight, prlc, _srs.getLSMM
			(0).getManifestMeasures()[0]))
			return false;

		org.drip.spline.params.SegmentResponseValueConstraint rvcLeading =
			org.drip.spline.params.SegmentResponseValueConstraint.FromPredictorResponsePair
				(_valParams.valueDate(), _dblEpochResponse);

		if (null == rvcLeading) return false;

		return aCS[0].calibrate (rvcLeading, dblLeftSlope, _mapRVSC.get (dblSegmentRight).base(), null ==
			_sbfr ? null : _sbfr.sizeToSegment (aCS[0])) && _stretch.setSegmentBuilt (0,
				org.drip.product.params.FloatingRateIndex.Create (cc.getForwardCurveName()));
	}

	@Override public boolean calibSegmentSequence (
		final int iStartingSegment)
	{
		org.drip.spline.segment.LatentStateResponseModel[] aCS = _stretch.segments();

		int iNumSegment = aCS.length;

		for (int iSegment = iStartingSegment; iSegment < iNumSegment; ++iSegment) {
			org.drip.product.definition.CalibratableFixedIncomeComponent cc = _srs.getCalibComp (iSegment);

			if (null == cc) return false;

			org.drip.state.estimator.PredictorResponseWeightConstraint prlc = cc.generateCalibPRLC
				(_valParams, _pricerParams, _cmp, _quotingParams, _srs.getLSMM (iSegment));

			double dblSegmentRight = aCS[iSegment].right();

			if (null == prlc || !generateSegmentConstraintSet (dblSegmentRight, prlc,
				_srs.getLSMM (iSegment).getManifestMeasures()[0]))
				return false;

			if (!aCS[iSegment].calibrate (0 == iSegment ? null : aCS[iSegment - 1], _mapRVSC.get
				(dblSegmentRight).base(), null == _sbfr ? null : _sbfr.sizeToSegment (aCS[iSegment])) ||
					!_stretch.setSegmentBuilt (iSegment, org.drip.product.params.FloatingRateIndex.Create
						(cc.getForwardCurveName())))
				return false;
		}

		return true;
	}

	@Override public boolean manifestMeasureSensitivity (
		final double dblLeftSlopeSensitivity)
	{
		org.drip.spline.segment.LatentStateResponseModel[] aCS = _stretch.segments();

		int iNumSegment = aCS.length;

		for (int iSegment = 0; iSegment < iNumSegment; ++iSegment) {
			double dblSegmentRight = aCS[iSegment].right();

			for (java.lang.String strManifestMeasure : _srs.getLSMM (iSegment).getManifestMeasures()) {
				if (!aCS[iSegment].setPreceedingManifestSensitivityControl (strManifestMeasure, getPMSC
					(strManifestMeasure)))
					return false;

				if (0 == iSegment) {
					if (!aCS[0].manifestMeasureSensitivity (strManifestMeasure,
						org.drip.spline.params.SegmentResponseValueConstraint.FromPredictorResponsePair
							(_valParams.valueDate(), _dblEpochResponse), _mapRVSC.get
								(dblSegmentRight).base(), dblLeftSlopeSensitivity,
									org.drip.spline.params.SegmentResponseValueConstraint.FromPredictorResponsePair
						(_valParams.valueDate(), 0.), _mapRVSC.get
							(dblSegmentRight).manifestMeasureSensitivity (strManifestMeasure), null ==
								_sbfrQuoteSensitivity ? null : _sbfrQuoteSensitivity.sizeToSegment (aCS[0])))
						return false;
				} else {
					if (!aCS[iSegment].manifestMeasureSensitivity (aCS[iSegment - 1], strManifestMeasure,
						_mapRVSC.get (dblSegmentRight).base(), _mapRVSC.get
							(dblSegmentRight).manifestMeasureSensitivity (strManifestMeasure), null ==
								_sbfrQuoteSensitivity ? null : _sbfrQuoteSensitivity.sizeToSegment
									(aCS[iSegment])))
						return false;
				}
			}
		}

		return true;
	}
}
