
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
 * 	regime.
 *
 * @author Lakshmi Krishnamurthy
 */

public class RatesSegmentSequenceBuilder implements org.drip.math.regime.SegmentSequenceBuilder {
	private int _iCalibrationBoundaryCondition = -1;
	private org.drip.state.estimator.CurveRegime _cr = null;
	private org.drip.state.estimator.RegimeBuilderSet _rbs = null;
	private org.drip.param.pricer.PricerParams _pricerParams = null;
	private org.drip.math.regime.MultiSegmentRegime _regimePrev = null;
	private org.drip.param.valuation.ValuationParams _valParams = null;
	private org.drip.param.definition.ComponentMarketParams _cmp = null;
	private org.drip.param.valuation.QuotingParams _quotingParams = null;

	private org.drip.math.segment.ResponseValueConstraint GenerateSegmentConstraint (
		final org.drip.state.estimator.PredictorResponseLinearConstraint prlc)
	{
		java.util.TreeMap<java.lang.Double, java.lang.Double> mapResponsePredictorWeight =
			prlc.getResponsePredictorWeight();

		if (null == mapResponsePredictorWeight || 0 == mapResponsePredictorWeight.size()) return null;

		java.util.Set<java.util.Map.Entry<java.lang.Double, java.lang.Double>> setRP =
			mapResponsePredictorWeight.entrySet();

		if (null == setRP) return null;

		double dblValue = 0.;

		java.util.List<java.lang.Double> lsPredictor = new java.util.ArrayList<java.lang.Double>();

		java.util.List<java.lang.Double> lsResponseWeight = new java.util.ArrayList<java.lang.Double>();

		for (java.util.Map.Entry<java.lang.Double, java.lang.Double> me : setRP) {
			if (null == me) return null;

			double dblDate = me.getKey();

			try {
				if (null != _regimePrev && _regimePrev.in (dblDate))
					dblValue -= _regimePrev.response (dblDate) * me.getValue();
				else if (null != _cr && _cr.inBuiltRange (dblDate))
					dblValue -= _cr.response (dblDate) * me.getValue();
				else {
					lsPredictor.add (dblDate);

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
			return new org.drip.math.segment.ResponseValueConstraint (adblPredictor, adblResponseWeight,
				prlc.getValue() + dblValue);
		} catch (java.lang.Exception e) {
			e.printStackTrace();
		}

		return null;
	}

	/**
	 * RatesSegmentSequenceBuilder constructor
	 * 
	 * @param RBS Regime Builder Parameters
	 * @param valParams Valuation Parameter
	 * @param pricerParams Pricer Parameter
	 * @param cmp Component Market Parameter
	 * @param quotingParams Quoting Parameter
	 * @param regimePrev The Previous Regime Used to value cash flows that fall in those segments
	 * @param iCalibrationBoundaryCondition The Calibration Boundary Condition
	 * 
	 * @throws java.lang.Exception Thrown if the Inputs are invalid
	 */

	public RatesSegmentSequenceBuilder (
		final org.drip.state.estimator.RegimeBuilderSet rbs,
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.pricer.PricerParams pricerParams,
		final org.drip.param.definition.ComponentMarketParams cmp,
		final org.drip.param.valuation.QuotingParams quotingParams,
		final org.drip.math.regime.MultiSegmentRegime regimePrev,
		final int iCalibrationBoundaryCondition)
		throws java.lang.Exception
	{
		if (null == (_rbs = rbs) || null == (_valParams = valParams))
			throw new java.lang.Exception ("RatesSegmentSequenceBuilder ctr: Invalid Inputs");

		_cmp = cmp;
		_regimePrev = regimePrev;
		_pricerParams = pricerParams;
		_quotingParams = quotingParams;
		_iCalibrationBoundaryCondition = iCalibrationBoundaryCondition;
	}

	@Override public boolean setRegime (
		final org.drip.math.regime.MultiSegmentRegime msr)
	{
		if (null == msr || !(msr instanceof org.drip.state.estimator.CurveRegime)) return false;

		org.drip.state.estimator.CurveRegime cr = (org.drip.state.estimator.CurveRegime) msr;

		org.drip.math.segment.PredictorResponse[] aPR = cr.getSegments();

		if (null == aPR || aPR.length != _rbs.getCalibComp().length) return false;

		_cr = cr;
		return true;
	}

	@Override public int getCalibrationBoundaryCondition()
	{
		return _iCalibrationBoundaryCondition;
	}

	@Override public boolean calibStartingSegment (
		final double dblLeftSlope)
	{
		if (null == _cr || !_cr.setClearBuiltRange()) return false;

		org.drip.math.segment.PredictorResponse[] aPR = _cr.getSegments();

		org.drip.product.definition.CalibratableComponent cc = _rbs.getCalibComp (0);

		org.drip.state.estimator.LatentStateMetricMeasure lsmm = _rbs.getLSMM (0);

		org.drip.math.segment.ResponseValueConstraint rvcLeading =
			org.drip.math.segment.ResponseValueConstraint.FromPredictorResponse (_valParams._dblValue, 1.);

		if (null == aPR || 1 > aPR.length || null == cc | null == lsmm || null == rvcLeading) return false;

		org.drip.state.estimator.PredictorResponseLinearConstraint prlc = cc.generateCalibPRLC (_valParams,
			_pricerParams, _cmp, _quotingParams, lsmm);

		if (null == prlc) return false;

		org.drip.math.segment.ResponseValueConstraint rvc = GenerateSegmentConstraint (prlc);

		if (null == rvc) return false;

		return aPR[0].calibrate (rvcLeading, dblLeftSlope, rvc) && _cr.setSegmentBuilt (0);
	}

	@Override public boolean calibSegmentSequence (
		final int iStartingSegment)
	{
		if (null == _cr) return false;

		org.drip.math.segment.PredictorResponse[] aPR = _cr.getSegments();

		int iNumSegment = aPR.length;

		for (int iSegment = iStartingSegment; iSegment < iNumSegment; ++iSegment) {
			org.drip.product.definition.CalibratableComponent cc = _rbs.getCalibComp (iSegment);

			org.drip.state.estimator.LatentStateMetricMeasure lsmm = _rbs.getLSMM (iSegment);

			if (null == aPR || 1 > aPR.length || null == cc | null == lsmm) return false;

			org.drip.state.estimator.PredictorResponseLinearConstraint prlc = cc.generateCalibPRLC
				(_valParams, _pricerParams, _cmp, _quotingParams, lsmm);

			if (null == prlc) return false;

			org.drip.math.segment.ResponseValueConstraint rvc = GenerateSegmentConstraint (prlc);

			if (null == rvc) return false;

			if (!aPR[iSegment].calibrate (0 == iSegment ? null : aPR[iSegment - 1], rvc) ||
				!_cr.setSegmentBuilt (iSegment))
				return false;
		}

		return true;
	}
}
