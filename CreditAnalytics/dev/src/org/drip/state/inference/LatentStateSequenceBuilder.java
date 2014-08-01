
package org.drip.state.inference;

/*
 * -*- mode: java; tab-width: 4; indent-tabs-mode: nil; c-basic-offset: 4 -*-
 */

/*!
 * Copyright (C) 2014 Lakshmi Krishnamurthy
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
 * LatentStateSequenceBuilder holds the logic behind building the bootstrap segments contained in the given
 * 	Stretch.
 * 
 * It extends SegmentSequenceBuilder by implementing/customizing the calibration of the starting as well as
 *  the subsequent segments.
 *
 * @author Lakshmi Krishnamurthy
 */

public class LatentStateSequenceBuilder implements org.drip.spline.stretch.SegmentSequenceBuilder {
	private org.drip.spline.grid.Span _span = null;
	private org.drip.spline.stretch.BoundarySettings _bs = null;
	private org.drip.state.estimator.CurveStretch _stretch = null;
	private org.drip.param.pricer.PricerParams _pricerParams = null;
	private org.drip.param.market.CurveSurfaceQuoteSet _csqs = null;
	private org.drip.param.valuation.ValuationParams _valParams = null;
	private org.drip.param.valuation.ValuationCustomizationParams _vcp = null;
	private org.drip.state.inference.LatentStateStretchSpec _stretchSpec = null;
	private
		org.drip.analytics.support.CaseInsensitiveHashMap<org.drip.spline.params.PreceedingManifestSensitivityControl>
			_mapPMSC = null;

	private java.util.Map<java.lang.Double, org.drip.spline.params.ResponseValueSensitivityConstraint>
		_mapRVSC = new
			java.util.HashMap<java.lang.Double, org.drip.spline.params.ResponseValueSensitivityConstraint>();

	private org.drip.spline.params.PreceedingManifestSensitivityControl getPMSC (
		final java.lang.String strManifestMeasure)
	{
		return _mapPMSC.containsKey (strManifestMeasure) ? _mapPMSC.get (strManifestMeasure) : null;
	}

	private org.drip.spline.params.SegmentResponseValueConstraint segmentCalibResponseConstraint (
		final org.drip.state.estimator.PredictorResponseWeightConstraint prwc)
	{
		java.util.TreeMap<java.lang.Double, java.lang.Double> mapPredictorLSQMLoading =
			prwc.getPredictorResponseWeight();

		if (null == mapPredictorLSQMLoading || 0 == mapPredictorLSQMLoading.size()) return null;

		java.util.Set<java.util.Map.Entry<java.lang.Double, java.lang.Double>> esPredictorLSQMLoading =
			mapPredictorLSQMLoading.entrySet();

		if (null == esPredictorLSQMLoading || 0 == esPredictorLSQMLoading.size()) return null;

		double dblConstraint = 0.;

		java.util.List<java.lang.Double> lsPredictor = new java.util.ArrayList<java.lang.Double>();

		java.util.List<java.lang.Double> lsResponseLSQMLoading = new java.util.ArrayList<java.lang.Double>();

		for (java.util.Map.Entry<java.lang.Double, java.lang.Double> me : esPredictorLSQMLoading) {
			if (null == me) return null;

			double dblPredictorDate = me.getKey();

			try {
				if (null != _span && _span.in (dblPredictorDate))
					dblConstraint -= _span.calcResponseValue (dblPredictorDate) * me.getValue();
				else if (_stretch.inBuiltRange (dblPredictorDate))
					dblConstraint -= _stretch.responseValue (dblPredictorDate) * me.getValue();
				else {
					lsPredictor.add (dblPredictorDate);

					lsResponseLSQMLoading.add (me.getValue());
				}
			} catch (java.lang.Exception e) {
				e.printStackTrace();

				return null;
			}
		}

		int iSize = lsPredictor.size();

		double[] adblPredictor = new double[iSize];
		double[] adblResponseLSQMLoading = new double[iSize];

		for (int i = 0; i < iSize; ++i) {
			adblPredictor[i] = lsPredictor.get (i);

			adblResponseLSQMLoading[i] = lsResponseLSQMLoading.get (i);
		}

		try {
			return new org.drip.spline.params.SegmentResponseValueConstraint (adblPredictor,
				adblResponseLSQMLoading, (prwc.getValue()) + dblConstraint);
		} catch (java.lang.Exception e) {
			e.printStackTrace();
		}

		return null;
	}

	private org.drip.spline.params.SegmentResponseValueConstraint segmentSensResponseConstraint (
		final org.drip.state.estimator.PredictorResponseWeightConstraint prwc,
		final java.lang.String strManifestMeasure)
	{
		java.util.TreeMap<java.lang.Double, java.lang.Double> mapPredictorSensLoading =
			prwc.getDResponseWeightDManifestMeasure (strManifestMeasure);

		if (null == mapPredictorSensLoading || 0 == mapPredictorSensLoading.size()) return null;

		java.util.Set<java.util.Map.Entry<java.lang.Double, java.lang.Double>> esPredictorSensLoading =
			mapPredictorSensLoading.entrySet();

		if (null == esPredictorSensLoading || 0 == esPredictorSensLoading.size()) return null;

		double dblSensLoadingConstraint = 0.;

		java.util.List<java.lang.Double> lsPredictor = new java.util.ArrayList<java.lang.Double>();

		java.util.List<java.lang.Double> lsSensLoading = new java.util.ArrayList<java.lang.Double>();

		for (java.util.Map.Entry<java.lang.Double, java.lang.Double> me : esPredictorSensLoading) {
			if (null == me) return null;

			double dblPredictorDate = me.getKey();

			try {
				if (null != _span && _span.in (dblPredictorDate))
					dblSensLoadingConstraint -= _span.calcResponseValue (dblPredictorDate) * me.getValue();
				else if (_stretch.inBuiltRange (dblPredictorDate))
					dblSensLoadingConstraint -= _stretch.responseValue (dblPredictorDate) * me.getValue();
				else {
					lsPredictor.add (dblPredictorDate);

					lsSensLoading.add (me.getValue());
				}
			} catch (java.lang.Exception e) {
				e.printStackTrace();

				return null;
			}
		}

		int iSize = lsPredictor.size();

		double[] adblPredictor = new double[iSize];
		double[] adblSensLoading = new double[iSize];

		for (int i = 0; i < iSize; ++i) {
			adblPredictor[i] = lsPredictor.get (i);

			adblSensLoading[i] = lsSensLoading.get (i);
		}

		try {
			return new org.drip.spline.params.SegmentResponseValueConstraint (adblPredictor, adblSensLoading,
				prwc.getDValueDManifestMeasure (strManifestMeasure) + dblSensLoadingConstraint);
		} catch (java.lang.Exception e) {
			e.printStackTrace();
		}

		return null;
	}

	private boolean generateSegmentConstraintSet (
		final double dblSegmentRight,
		final org.drip.state.estimator.PredictorResponseWeightConstraint prwc,
		final java.lang.String strManifestMeasure)
	{
		org.drip.spline.params.SegmentResponseValueConstraint srvcBase = segmentCalibResponseConstraint
			(prwc);

		if (null == srvcBase) return false;

		org.drip.spline.params.SegmentResponseValueConstraint srvcSensitivity = segmentSensResponseConstraint
			(prwc, strManifestMeasure);

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

	@Override public boolean setStretch (
		final org.drip.spline.stretch.MultiSegmentSequence mss)
	{
		if (null == mss || !(mss instanceof org.drip.state.estimator.CurveStretch)) return false;

		_stretch = (org.drip.state.estimator.CurveStretch) mss;

		org.drip.spline.segment.LatentStateResponseModel[] aLSRM = _stretch.segments();

		if (null == aLSRM || aLSRM.length != _stretchSpec.segmentSpec().length) return false;

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

		org.drip.product.definition.CalibratableFixedIncomeComponent cfic =
			_stretchSpec.segmentSpec()[0].component();

		if (null == cfic) return false;

		org.drip.spline.segment.LatentStateResponseModel[] aLSRM = _stretch.segments();

		if (null == aLSRM || 0 == aLSRM.length) return false;

		/* org.drip.state.estimator.PredictorResponseWeightConstraint prwc = cfic.calibPRWC (_valParams,
			_pricerParams, _csqs, _vcp, _stretchSpec.segmentSpec()[0].manifestMeasures());

		double dblSegmentRight = aLSRM[0].right();

		if (null == prwc || !generateSegmentConstraintSet (dblSegmentRight, prwc, _srs.getLSMM
			(0).manifestMeasures()[0]))
			return false;

		org.drip.spline.params.SegmentResponseValueConstraint rvcLeading =
			org.drip.spline.params.SegmentResponseValueConstraint.FromPredictorResponsePair
				(_valParams.valueDate(), _dblEpochResponse);

		if (null == rvcLeading) return false;

		return aCS[0].calibrate (rvcLeading, dblLeftSlope, _mapRVSC.get (dblSegmentRight).base(), null ==
			_sbfr ? null : _sbfr.sizeToSegment (aCS[0])) && _stretch.setSegmentBuilt (0,
				prlc.mergeLabelSet()); */

		return false;
	}

	@Override public boolean calibSegmentSequence (
		final int iStartingSegment)
	{
		/* org.drip.spline.segment.LatentStateResponseModel[] aCS = _stretch.segments();

		int iNumSegment = aCS.length;

		for (int iSegment = iStartingSegment; iSegment < iNumSegment; ++iSegment) {
			org.drip.product.definition.CalibratableFixedIncomeComponent cc = _srs.getCalibComp (iSegment);

			if (null == cc) return false;

			org.drip.state.estimator.PredictorResponseWeightConstraint prlc = cc.generateCalibPRWC
				(_valParams, _pricerParams, _mktParams, _quotingParams, _srs.getLSMM (iSegment));

			double dblSegmentRight = aCS[iSegment].right();

			if (null == prlc || !generateSegmentConstraintSet (dblSegmentRight, prlc, _srs.getLSMM
				(iSegment).manifestMeasures()[0]))
				return false;

			if (!aCS[iSegment].calibrate (0 == iSegment ? null : aCS[iSegment - 1], _mapRVSC.get
				(dblSegmentRight).base(), null == _sbfr ? null : _sbfr.sizeToSegment (aCS[iSegment])) ||
					!_stretch.setSegmentBuilt (iSegment, prlc.mergeLabelSet()))
				return false;
		}

		return true; */

		return false;
	}

	@Override public boolean manifestMeasureSensitivity (
		final double dblLeftSlopeSensitivity)
	{
		/* org.drip.spline.segment.LatentStateResponseModel[] aCS = _stretch.segments();

		int iNumSegment = aCS.length;

		for (int iSegment = 0; iSegment < iNumSegment; ++iSegment) {
			double dblSegmentRight = aCS[iSegment].right();

			for (java.lang.String strManifestMeasure : _srs.getLSMM (iSegment).manifestMeasures()) {
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

		return true; */

		return false;
	}
}
