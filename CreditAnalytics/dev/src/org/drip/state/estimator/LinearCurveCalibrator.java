
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
 * LinearCurveCalibrator creates the discount curve span from the instrument cash flows.
 *
 * @author Lakshmi Krishnamurthy
 */

public class LinearCurveCalibrator {
	private org.drip.math.regime.RegimeCalibrationSetting _rcs = null;
	private org.drip.math.segment.PredictorResponseBuilderParams _prbp = null;

	private static final org.drip.math.segment.ResponseValueConstraint GenerateSegmentConstraint (
		final org.drip.state.estimator.PredictorResponseLinearConstraint prlc,
		final org.drip.math.regime.MultiSegmentRegime regimeCurrent,
		final org.drip.math.regime.MultiSegmentRegime regimePrev)
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
				if (null != regimePrev && regimePrev.in (dblDate)) {
					try {
						dblValue -= regimePrev.response (dblDate) * me.getValue();
					} catch (java.lang.Exception e) {
						e.printStackTrace();

						return null;
					}
				} else if (null != regimeCurrent && regimeCurrent.in (dblDate)) {
					try {
						dblValue -= regimeCurrent.response (dblDate) * me.getValue();
					} catch (java.lang.Exception e) {
						e.printStackTrace();

						return null;
					}
				} else {
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
	 * LinearCurveCalibrator constructor
	 * 
	 * @param prbp Segment Builder Parameters
	 * @param rcs Regime Calibrator Setting
	 * 
	 * @throws java.lang.Exception Thrown if the inputs are invalid
	 */

	public LinearCurveCalibrator (
		final org.drip.math.segment.PredictorResponseBuilderParams prbp,
		final org.drip.math.regime.RegimeCalibrationSetting rcs)
		throws java.lang.Exception
	{
		if (null == (_rcs = rcs) || null == (_prbp = prbp))
			throw new java.lang.Exception ("LinearCurveCalibrator ctr: Invalid Inputs");
	}

	/**
	 * Calibrate the Span from the instruments in the regimes, and their cash flows.
	 * 
	 * @param aRBS Array of the Regime Builder Parameters
	 * @param valParams Valuation Parameter
	 * @param pricerParams Pricer Parameter
	 * @param cmp Component Market Parameter
	 * @param quotingParams Quoting Parameter
	 * 
	 * @return Instance of the the DIscopunt CUrve Span
	 */

	public org.drip.math.grid.OverlappingRegimeSpan calibrateSpan (
		final org.drip.state.estimator.RegimeBuilderSet[] aRBS,
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.pricer.PricerParams pricerParams,
		final org.drip.param.definition.ComponentMarketParams cmp,
		final org.drip.param.valuation.QuotingParams quotingParams)
	{
		if (null == aRBS || null == valParams) return null;

		int iNumRegime = aRBS.length;
		org.drip.math.grid.OverlappingRegimeSpan span = null;
		org.drip.math.regime.MultiSegmentRegime regimePrev = null;

		if (0 == iNumRegime) return null;

		for (org.drip.state.estimator.RegimeBuilderSet rbs : aRBS) {
			if (null == rbs) return null;

			org.drip.state.estimator.LatentStateMetricMeasure[] aLSMM = rbs.getLSMM();

			org.drip.product.definition.CalibratableComponent[] aCalibComp = rbs.getCalibComp();

			int iNumCalibComp = aCalibComp.length;
			org.drip.math.regime.MultiSegmentRegime regime = null;

			for (int i = 0; i < iNumCalibComp; ++i) {
				if (null == aCalibComp[i]) return null;

				org.drip.state.estimator.PredictorResponseLinearConstraint prlc =
					aCalibComp[i].generateCalibPRLC (valParams, pricerParams, cmp, quotingParams, aLSMM[i]);

				if (null == prlc) return null;

				org.drip.math.segment.ResponseValueConstraint rvc = GenerateSegmentConstraint (prlc, regime,
					regimePrev);

				if (null == rvc) return null;

				double dblTerminalDate = aCalibComp[i].terminalDate().getJulian();

				try {
					if (null == regime) {
						if (null == (regime =
							org.drip.math.regime.RegimeBuilder.CreateUncalibratedRegimeEstimator
								(rbs.getName(), new double[] {valParams._dblValue, dblTerminalDate}, new
									org.drip.math.segment.PredictorResponseBuilderParams[] {_prbp})) ||
										!regime.setup (1., new
											org.drip.math.segment.ResponseValueConstraint[] {rvc}, _rcs))
							return null;
					} else {
						if (null == (regime = org.drip.math.regime.RegimeModifier.AppendSegment (regime,
							dblTerminalDate, rvc, _prbp, _rcs)))
							return null;
					}
				} catch (java.lang.Exception e) {
					e.printStackTrace();

					return null;
				}
			}

			if (null == span) {
				try {
					span = new org.drip.math.grid.OverlappingRegimeSpan (regime);
				} catch (java.lang.Exception e) {
					e.printStackTrace();

					return null;
				}
			} else {
				if (!span.addRegime (regime)) return null;
			}

			regimePrev = regime;
		}

		return span;
	}
}
