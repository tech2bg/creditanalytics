
package org.drip.state.estimator;

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
 * RatesSegmentSequenceBuilder holds the logic behind building the bootstrap segments contained in the given
 * 	Stretch.
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
	private org.drip.param.valuation.QuotingParams _quotingParams = null;
	private org.drip.spline.stretch.MultiSegmentSequence _mssPrev = null;
	private org.drip.state.estimator.StretchRepresentationSpec _srs = null;

	protected org.drip.spline.params.SegmentResponseValueConstraint GenerateSegmentConstraint (
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
				adblResponseWeight, prlc.getValue() + dblValue);
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
		final org.drip.param.valuation.QuotingParams quotingParams,
		final org.drip.spline.stretch.MultiSegmentSequence mssPrev,
		final org.drip.spline.params.StretchBestFitResponse sbfr,
		final org.drip.spline.stretch.BoundarySettings bs)
		throws java.lang.Exception
	{
		if (!org.drip.quant.common.NumberUtil.IsValid (_dblEpochResponse = dblEpochResponse) || null == (_srs
			= srs) || null == (_valParams = valParams) || null == (_bs = bs))
			throw new java.lang.Exception ("RatesSegmentSequenceBuilder ctr: Invalid Inputs");

		_cmp = cmp;
		_sbfr = sbfr;
		_mssPrev = mssPrev;
		_pricerParams = pricerParams;
		_quotingParams = quotingParams;
	}

	@Override public boolean setStretch (
		final org.drip.spline.stretch.MultiSegmentSequence mss)
	{
		if (null == mss || !(mss instanceof org.drip.state.estimator.CurveStretch)) return false;

		org.drip.state.estimator.CurveStretch stretch = (org.drip.state.estimator.CurveStretch) mss;

		org.drip.spline.segment.ConstitutiveState[] aCS = stretch.segments();

		if (null == aCS || aCS.length != _srs.getCalibComp().length) return false;

		_stretch = stretch;
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

		org.drip.spline.segment.ConstitutiveState[] aCS = _stretch.segments();

		org.drip.product.definition.CalibratableComponent cc = _srs.getCalibComp (0);

		org.drip.state.representation.LatentStateMetricMeasure lsmm = _srs.getLSMM (0);

		org.drip.spline.params.SegmentResponseValueConstraint rvcLeading =
			org.drip.spline.params.SegmentResponseValueConstraint.FromPredictorResponsePair
				(_valParams._dblValue, _dblEpochResponse);

		if (null == aCS || 1 > aCS.length || null == cc | null == lsmm || null == rvcLeading) return false;

		org.drip.state.estimator.PredictorResponseWeightConstraint prlc = cc.generateCalibPRLC (_valParams,
			_pricerParams, _cmp, _quotingParams, lsmm);

		if (null == prlc) return false;

		return aCS[0].calibrate (rvcLeading, dblLeftSlope, GenerateSegmentConstraint (prlc), null == _sbfr ?
			null : _sbfr.sizeToSegment (aCS[0])) && _stretch.setSegmentBuilt (0,
				org.drip.product.params.FloatingRateIndex.Create (cc.getForwardCurveName()));
	}

	@Override public boolean calibSegmentSequence (
		final int iStartingSegment)
	{
		if (null == _stretch) return false;

		org.drip.spline.segment.ConstitutiveState[] aCS = _stretch.segments();

		int iNumSegment = aCS.length;

		for (int iSegment = iStartingSegment; iSegment < iNumSegment; ++iSegment) {
			org.drip.product.definition.CalibratableComponent cc = _srs.getCalibComp (iSegment);

			org.drip.state.representation.LatentStateMetricMeasure lsmm = _srs.getLSMM (iSegment);

			if (null == aCS || 1 > aCS.length || null == cc | null == lsmm) return false;

			org.drip.state.estimator.PredictorResponseWeightConstraint prlc = cc.generateCalibPRLC
				(_valParams, _pricerParams, _cmp, _quotingParams, lsmm);

			if (null == prlc) return false;

			org.drip.spline.params.SegmentResponseValueConstraint srvc = GenerateSegmentConstraint (prlc);

			if (null == srvc) return false;

			if (!aCS[iSegment].calibrate (0 == iSegment ? null : aCS[iSegment - 1], srvc, null == _sbfr ?
				null : _sbfr.sizeToSegment (aCS[iSegment])) || !_stretch.setSegmentBuilt (iSegment,
					org.drip.product.params.FloatingRateIndex.Create (cc.getForwardCurveName())))
				return false;
		}

		return true;
	}
}
