
package org.drip.math.regime;

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
 * LocalControlRegimeBuilder exports Regime creation/calibration methods to generate customized basis splines, with
 *  customized segment behavior using the segment control.
 *
 * @author Lakshmi Krishnamurthy
 */

public class LocalControlRegimeBuilder {
	private static final double[] NodeC1 (
		final double[] adblPredictorOrdinate,
		final double[] adblResponseValue)
	{
		int iNumSegment = adblResponseValue.length - 1;
		double[] adblNodeC1 = new double[iNumSegment];

		for (int i = 0; i < iNumSegment; ++i)
			adblNodeC1[i] = (adblResponseValue[i + 1] - adblResponseValue[i]) / (adblPredictorOrdinate[i + 1]
				- adblPredictorOrdinate[i]);

		return adblNodeC1;
	}

	private static final double[] BesselC1 (
		final double[] adblPredictorOrdinate,
		final double[] adblResponseValue)
	{
		int iNumResponse = adblResponseValue.length;
		double[] adblEdgeSlope = new double[iNumResponse];

		for (int i = 0; i < iNumResponse; ++i) {
			if (0 == i) {
				adblEdgeSlope[i] = (adblPredictorOrdinate[2] + adblPredictorOrdinate[1] - 2. *
					adblPredictorOrdinate[0]) * (adblResponseValue[1] - adblResponseValue[0]) /
						(adblPredictorOrdinate[1] - adblPredictorOrdinate[0]);
				adblEdgeSlope[i] -= (adblPredictorOrdinate[1] - adblPredictorOrdinate[0]) *
					(adblResponseValue[2] - adblResponseValue[1]) / (adblPredictorOrdinate[2] -
						adblPredictorOrdinate[1]);
				adblEdgeSlope[i] /= (adblPredictorOrdinate[2] - adblPredictorOrdinate[0]);
			} else if (iNumResponse - 1 == i) {
				adblEdgeSlope[i] = (adblPredictorOrdinate[iNumResponse - 1] -
					adblPredictorOrdinate[iNumResponse - 2]) * (adblResponseValue[iNumResponse - 2] -
						adblResponseValue[iNumResponse - 3]) / (adblPredictorOrdinate[iNumResponse - 2] -
							adblPredictorOrdinate[iNumResponse - 3]);
				adblEdgeSlope[i] -= (2. * adblPredictorOrdinate[iNumResponse - 1] -
					adblPredictorOrdinate[iNumResponse - 2] - adblPredictorOrdinate[iNumResponse - 3]) *
						(adblResponseValue[iNumResponse - 1] - adblResponseValue[iNumResponse - 2]) /
							(adblPredictorOrdinate[iNumResponse - 1] - adblPredictorOrdinate[iNumResponse - 2]);
				adblEdgeSlope[i] /= (adblPredictorOrdinate[iNumResponse - 1] -
					adblPredictorOrdinate[iNumResponse - 3]);
			} else {
				adblEdgeSlope[i] = (adblPredictorOrdinate[i + 1] - adblPredictorOrdinate[i]) *
					(adblResponseValue[i] - adblResponseValue[i - 1]) / (adblPredictorOrdinate[i] -
						adblPredictorOrdinate[i - 1]);
				adblEdgeSlope[i] += (adblPredictorOrdinate[i] - adblPredictorOrdinate[i - 1]) *
					(adblResponseValue[i + 1] - adblResponseValue[i]) / (adblPredictorOrdinate[i + 1] -
						adblPredictorOrdinate[i]);
				adblEdgeSlope[i] /= (adblPredictorOrdinate[iNumResponse - 1] -
					adblPredictorOrdinate[iNumResponse - 3]);
			}
		}

		return adblEdgeSlope;
	}

	private static final double[] Hyman83C1 (
		final double[] adblPredictorOrdinate,
		final double[] adblResponseValue,
		final boolean bEliminateSpuriousExtrema)
	{
		int iNumResponse = adblResponseValue.length;
		double[] adblEdgeSlope = new double[iNumResponse];
		double dblMonotoneSlopePrev = java.lang.Double.NaN;

		for (int i = 0; i < iNumResponse; ++i) {
			adblEdgeSlope[i] = 0.;
			double dblMonotoneSlope = iNumResponse - 1 != i ? (adblResponseValue[i + 1] -
				adblResponseValue[i]) / (adblPredictorOrdinate[i + 1] - adblPredictorOrdinate[i]) :
					java.lang.Double.NaN;

			if (0 != i && iNumResponse - 1 != i) {
				double dblMonotoneIndicator = dblMonotoneSlopePrev * dblMonotoneSlope;

				if (0. <= dblMonotoneIndicator) {
					adblEdgeSlope[i] = 3. * dblMonotoneIndicator / (java.lang.Math.max (dblMonotoneSlope,
						dblMonotoneSlopePrev) + 2. * java.lang.Math.min (dblMonotoneSlope,
							dblMonotoneSlopePrev));

					if (bEliminateSpuriousExtrema) {
						if (0. < dblMonotoneSlope)
							adblEdgeSlope[i] = java.lang.Math.min (java.lang.Math.max (0., adblEdgeSlope[i]),
								java.lang.Math.min (dblMonotoneSlope, dblMonotoneSlopePrev));
						else
							adblEdgeSlope[i] = java.lang.Math.max (java.lang.Math.min (0., adblEdgeSlope[i]),
								java.lang.Math.max (dblMonotoneSlope, dblMonotoneSlopePrev));
					}
				}
			}

			dblMonotoneSlopePrev = dblMonotoneSlope;
		}

		return adblEdgeSlope;
	}

	private static final double[] Hyman89C1 (
		final double[] adblPredictorOrdinate,
		final double[] adblResponseValue)
	{
		int iNumResponse = adblResponseValue.length;
		double[] adblEdgeSlope = new double[iNumResponse];

		double[] adblBesselC1 = BesselC1 (adblPredictorOrdinate, adblResponseValue);

		double[] adblNodeC1 = NodeC1 (adblPredictorOrdinate, adblResponseValue);

		for (int i = 0; i < iNumResponse; ++i) {
			if (i < 2 || i >= iNumResponse - 2)
				adblEdgeSlope[i] = adblBesselC1[i];
			else {
				double dMuMinus = (adblNodeC1[i - 1] * (2. * (adblPredictorOrdinate[i] -
					adblPredictorOrdinate[i - 1]) + adblPredictorOrdinate[i - 1] -
						adblPredictorOrdinate[i - 2]) - adblNodeC1[i - 2] * (adblPredictorOrdinate[i] -
							adblPredictorOrdinate[i - 1])) / (adblPredictorOrdinate[i] -
								adblPredictorOrdinate[i - 2]);
				double dMu0 = (adblNodeC1[i - 1] * (adblPredictorOrdinate[i + 1] - adblPredictorOrdinate[i])
					+ adblNodeC1[i] * (adblPredictorOrdinate[i] - adblPredictorOrdinate[i - 1])) /
						(adblPredictorOrdinate[i + 1] - adblPredictorOrdinate[i - 1]);
				double dMuPlus = (adblNodeC1[i] * (2. * (adblPredictorOrdinate[i + 1] -
					adblPredictorOrdinate[i]) + adblPredictorOrdinate[i + 2] - adblPredictorOrdinate[i + 1])
						- adblNodeC1[i + 1] * (adblPredictorOrdinate[i + 1] - adblPredictorOrdinate[i])) /
							(adblPredictorOrdinate[i + 2] - adblPredictorOrdinate[i]);

				try {
					double dblM = 3 * org.drip.math.common.NumberUtil.Minimum (new double[]
						{java.lang.Math.abs (adblNodeC1[i - 1]), java.lang.Math.abs (adblNodeC1[i]),
							java.lang.Math.abs (dMu0), java.lang.Math.abs (dMuPlus)});

					if (!org.drip.math.common.NumberUtil.SameSign (new double[] {dMu0, dMuMinus,
							adblNodeC1[i - 1] - adblNodeC1[i - 2], adblNodeC1[i] - adblNodeC1[i - 1]}))
						dblM = java.lang.Math.max (dblM, 1.5 * java.lang.Math.min (java.lang.Math.abs (dMu0),
							java.lang.Math.abs (dMuMinus)));
					else if (!org.drip.math.common.NumberUtil.SameSign (new double[] {-dMu0, -dMuPlus,
							adblNodeC1[i] - adblNodeC1[i - 1], adblNodeC1[i + 1] - adblNodeC1[i]}))
						dblM = java.lang.Math.max (dblM, 1.5 * java.lang.Math.min (java.lang.Math.abs (dMu0),
							java.lang.Math.abs (dMuPlus)));

					adblEdgeSlope[i] = 0.;

					if (adblBesselC1[i] * dMu0 > 0.)
						adblEdgeSlope[i] = adblBesselC1[i] / java.lang.Math.abs (adblBesselC1[i]) *
							java.lang.Math.min (java.lang.Math.abs (adblBesselC1[i]), dblM);
				} catch (java.lang.Exception e) {
					e.printStackTrace();

					return null;
				}
			}
		}

		return adblEdgeSlope;
	}

	private static final double[] HarmonicC1 (
		final double[] adblPredictorOrdinate,
		final double[] adblResponseValue,
		final boolean bApplyMonotoneFilter)
	{
		int iNumResponse = adblResponseValue.length;
		double[] adblEdgeSlope = new double[iNumResponse];

		double[] adblNodeC1 = NodeC1 (adblPredictorOrdinate, adblResponseValue);

		for (int i = 0; i < iNumResponse; ++i) {
			if (0 == i) {
				adblEdgeSlope[i] = (adblPredictorOrdinate[2] + adblPredictorOrdinate[1] - 2. *
					adblPredictorOrdinate[0]) * adblNodeC1[0] / (adblPredictorOrdinate[2] -
						adblPredictorOrdinate[0]);
				adblEdgeSlope[i] -= (adblPredictorOrdinate[1] - adblPredictorOrdinate[0]) * adblNodeC1[1] /
					(adblPredictorOrdinate[2] - adblPredictorOrdinate[0]);

				if (bApplyMonotoneFilter) {
					if (adblEdgeSlope[0] * adblNodeC1[0] > 0. && adblNodeC1[0] * adblNodeC1[1] > 0. &&
						java.lang.Math.abs (adblEdgeSlope[0]) < 3. * java.lang.Math.abs (adblNodeC1[0]))
						adblEdgeSlope[0] = 3. * adblNodeC1[0];
					else if (adblEdgeSlope[0] * adblNodeC1[0] <= 0.)
						adblEdgeSlope[0] = 0.;
				}
			} else if (iNumResponse - 1 == i) {
				adblEdgeSlope[i] = -(adblPredictorOrdinate[i] - adblPredictorOrdinate[i - 1]) *
					adblNodeC1[i - 2] / (adblPredictorOrdinate[i] - adblPredictorOrdinate[i - 2]);
				adblEdgeSlope[i] += (2. * adblPredictorOrdinate[i] - adblPredictorOrdinate[i - 1] -
					adblPredictorOrdinate[i - 2]) * adblNodeC1[i - 1] / (adblPredictorOrdinate[i] -
						adblPredictorOrdinate[i - 2]);

				if (bApplyMonotoneFilter) {
					if (adblEdgeSlope[i] * adblNodeC1[i - 1] > 0. && adblNodeC1[i - 1] * adblNodeC1[i - 2] >
						0. && java.lang.Math.abs (adblEdgeSlope[i]) < 3. * java.lang.Math.abs
							(adblNodeC1[i - 1]))
						adblEdgeSlope[i] = 3. * adblNodeC1[i - 1];
					else if (adblEdgeSlope[i] * adblNodeC1[i - 1] <= 0.)
						adblEdgeSlope[i] = 0.;
				}
			} else {
				if (adblNodeC1[i - 1] * adblNodeC1[i] <= 0.)
					adblEdgeSlope[i] = 0.;
				else {
					adblEdgeSlope[i] = (adblPredictorOrdinate[i] - adblPredictorOrdinate[i - 1] + 2. *
						(adblPredictorOrdinate[i + 1] - adblPredictorOrdinate[i])) / (3. *
							(adblPredictorOrdinate[i + 1] - adblPredictorOrdinate[i])) / adblNodeC1[i - 1];
					adblEdgeSlope[i] += (adblPredictorOrdinate[i + 1] - adblPredictorOrdinate[i] + 2. *
						(adblPredictorOrdinate[i] - adblPredictorOrdinate[i - 1])) / (3. *
							(adblPredictorOrdinate[i + 1] - adblPredictorOrdinate[i])) / adblNodeC1[i];
					adblEdgeSlope[i] = 1. / adblEdgeSlope[i];
				}
			}
		}

		return adblEdgeSlope;
	}

	private static final double[] VanLeerLimiterC1 (
		final double[] adblPredictorOrdinate,
		final double[] adblResponseValue,
		final boolean bApplyMonotoneFilter)
	{
		int iNumResponse = adblResponseValue.length;
		double[] adblEdgeSlope = new double[iNumResponse];

		double[] adblNodeC1 = NodeC1 (adblPredictorOrdinate, adblResponseValue);

		for (int i = 0; i < iNumResponse; ++i) {
			if (0 == i) {
				adblEdgeSlope[i] = (adblPredictorOrdinate[2] + adblPredictorOrdinate[1] - 2. *
					adblPredictorOrdinate[0]) * adblNodeC1[0] / (adblPredictorOrdinate[2] -
						adblPredictorOrdinate[0]);
				adblEdgeSlope[i] -= (adblPredictorOrdinate[1] - adblPredictorOrdinate[0]) * adblNodeC1[1] /
					(adblPredictorOrdinate[2] - adblPredictorOrdinate[0]);

				if (bApplyMonotoneFilter) {
					if (adblEdgeSlope[0] * adblNodeC1[0] > 0. && adblNodeC1[0] * adblNodeC1[1] > 0. &&
						java.lang.Math.abs (adblEdgeSlope[0]) < 3. * java.lang.Math.abs (adblNodeC1[0]))
						adblEdgeSlope[0] = 3. * adblNodeC1[0];
					else if (adblEdgeSlope[0] * adblNodeC1[0] <= 0.)
						adblEdgeSlope[0] = 0.;
				}
			} else if (iNumResponse - 1 == i) {
				adblEdgeSlope[i] = -(adblPredictorOrdinate[i] - adblPredictorOrdinate[i - 1]) *
					adblNodeC1[i - 2] / (adblPredictorOrdinate[i] - adblPredictorOrdinate[i - 2]);
				adblEdgeSlope[i] += (2. * adblPredictorOrdinate[i] - adblPredictorOrdinate[i - 1] -
					adblPredictorOrdinate[i - 2]) * adblNodeC1[i - 1] / (adblPredictorOrdinate[i] -
						adblPredictorOrdinate[i - 2]);

				if (bApplyMonotoneFilter) {
					if (adblEdgeSlope[i] * adblNodeC1[i - 1] > 0. && adblNodeC1[i - 1] * adblNodeC1[i - 2] >
						0. && java.lang.Math.abs (adblEdgeSlope[i]) < 3. * java.lang.Math.abs
							(adblNodeC1[i - 1]))
						adblEdgeSlope[i] = 3. * adblNodeC1[i - 1];
					else if (adblEdgeSlope[i] * adblNodeC1[i - 1] <= 0.)
						adblEdgeSlope[i] = 0.;
				}
			} else {
				if (0. != adblNodeC1[i - 1]) {
					double dblR = adblNodeC1[i] / adblNodeC1[i - 1];

					double dblRAbsolute = java.lang.Math.abs (dblR);

					adblEdgeSlope[i] = adblNodeC1[i] * (dblR + dblRAbsolute) / (1. + dblRAbsolute);
				} else if (0. >= adblNodeC1[i])
					adblEdgeSlope[i] = 0.;
				else if (0. < adblNodeC1[i])
					adblEdgeSlope[i] = 2. * adblNodeC1[i];
			}
		}

		return adblEdgeSlope;
	}

	private static final double[] HuynhLeFlochLimiterC1 (
		final double[] adblPredictorOrdinate,
		final double[] adblResponseValue,
		final boolean bApplyMonotoneFilter)
	{
		int iNumResponse = adblResponseValue.length;
		double[] adblEdgeSlope = new double[iNumResponse];

		double[] adblNodeC1 = NodeC1 (adblPredictorOrdinate, adblResponseValue);

		for (int i = 0; i < iNumResponse; ++i) {
			if (0 == i) {
				adblEdgeSlope[i] = (adblPredictorOrdinate[2] + adblPredictorOrdinate[1] - 2. *
					adblPredictorOrdinate[0]) * adblNodeC1[0] / (adblPredictorOrdinate[2] -
						adblPredictorOrdinate[0]);
				adblEdgeSlope[i] -= (adblPredictorOrdinate[1] - adblPredictorOrdinate[0]) * adblNodeC1[1] /
					(adblPredictorOrdinate[2] - adblPredictorOrdinate[0]);

				if (bApplyMonotoneFilter) {
					if (adblEdgeSlope[0] * adblNodeC1[0] > 0. && adblNodeC1[0] * adblNodeC1[1] > 0. &&
						java.lang.Math.abs (adblEdgeSlope[0]) < 3. * java.lang.Math.abs (adblNodeC1[0]))
						adblEdgeSlope[0] = 3. * adblNodeC1[0];
					else if (adblEdgeSlope[0] * adblNodeC1[0] <= 0.)
						adblEdgeSlope[0] = 0.;
				}
			} else if (iNumResponse - 1 == i) {
				adblEdgeSlope[i] = -(adblPredictorOrdinate[i] - adblPredictorOrdinate[i - 1]) *
					adblNodeC1[i - 2] / (adblPredictorOrdinate[i] - adblPredictorOrdinate[i - 2]);
				adblEdgeSlope[i] += (2. * adblPredictorOrdinate[i] - adblPredictorOrdinate[i - 1] -
					adblPredictorOrdinate[i - 2]) * adblNodeC1[i - 1] / (adblPredictorOrdinate[i] -
						adblPredictorOrdinate[i - 2]);

				if (bApplyMonotoneFilter) {
					if (adblEdgeSlope[i] * adblNodeC1[i - 1] > 0. && adblNodeC1[i - 1] * adblNodeC1[i - 2] >
						0. && java.lang.Math.abs (adblEdgeSlope[i]) < 3. * java.lang.Math.abs
							(adblNodeC1[i - 1]))
						adblEdgeSlope[i] = 3. * adblNodeC1[i - 1];
					else if (adblEdgeSlope[i] * adblNodeC1[i - 1] <= 0.)
						adblEdgeSlope[i] = 0.;
				}
			} else {
				double dblMonotoneIndicator = adblNodeC1[i] * adblNodeC1[i - 1];

				if (0. < dblMonotoneIndicator)
					adblEdgeSlope[i] = 3. * dblMonotoneIndicator * (adblNodeC1[i] + adblNodeC1[i - 1]) /
						(adblNodeC1[i] * adblNodeC1[i] + adblNodeC1[i - 1] * adblNodeC1[i - 1] * 4. *
							dblMonotoneIndicator);
				else
					adblEdgeSlope[i] = 0.;
			}
		}

		return adblEdgeSlope;
	}

	private static final double[] KrugerC1 (
		final double[] adblPredictorOrdinate,
		final double[] adblResponseValue)
	{
		int iNumResponse = adblResponseValue.length;
		double[] adblKrugerSlope = new double[iNumResponse];

		double[] adblSlopeC1 = NodeC1 (adblPredictorOrdinate, adblResponseValue);

		if (null == adblSlopeC1 || adblSlopeC1.length != iNumResponse - 1) return null;

		for (int i = 0; i < iNumResponse; ++i) {
			if (0 != i && iNumResponse - 1 != i) {
				if (adblSlopeC1[i - 1] * adblSlopeC1[i] <= 0.)
					adblKrugerSlope[i] = 0.;
				else
					adblKrugerSlope[i] = 2. / ((1. / adblSlopeC1[i - 1]) + (1. / adblSlopeC1[i]));
			}
		}

		adblKrugerSlope[0] = 3.5 * adblSlopeC1[0] - 0.5 * adblKrugerSlope[1];
		adblKrugerSlope[iNumResponse - 1] = 3.5 * adblSlopeC1[iNumResponse - 2] - 0.5 *
			adblKrugerSlope[iNumResponse - 2];
		return adblKrugerSlope;
	}

	/**
	 * Create a Regime off of Hermite Splines from the specified the Predictor Ordinates, the Response
	 *  Values, the Custom Slopes, and the Segment Builder Parameters.
	 * 
	 * @param strName Regime Name
	 * @param adblPredictorOrdinate Array of Predictor Ordinates
	 * @param adblResponseValue Array of Response Values
	 * @param adblCustomSlope Array of Custom Slopes
	 * @param aPRBP Array of Segment Builder Parameters
	 * @param fwr Fitness Weighted Response
	 * @param iSetupMode Calibration Set up Mode NATURAL | FINANCIAL | FLOATING | NOTAKNOT
	 * 
	 * @return The Instance of the Hermite Spline Regime
	 */

	public static final org.drip.math.regime.MultiSegmentRegime CustomSlopeHermiteSpline (
		final java.lang.String strName,
		final double[] adblPredictorOrdinate,
		final double[] adblResponseValue,
		final double[] adblCustomSlope,
		final org.drip.math.segment.PredictorResponseBuilderParams[] aPRBP,
		final org.drip.math.segment.BestFitWeightedResponse fwr,
		final int iSetupMode)
	{
		org.drip.math.regime.MultiSegmentRegime regime =
			org.drip.math.regime.RegimeBuilder.CreateUncalibratedRegimeEstimator (strName,
				adblPredictorOrdinate, aPRBP);

		if (null == regime || null == adblResponseValue || null == adblCustomSlope) return null;

		int iNumResponseValue = adblResponseValue.length;
		org.drip.math.segment.PredictorOrdinateResponseDerivative[] aPORDLeft = new
			org.drip.math.segment.PredictorOrdinateResponseDerivative[iNumResponseValue - 1];
		org.drip.math.segment.PredictorOrdinateResponseDerivative[] aPORDRight = new
			org.drip.math.segment.PredictorOrdinateResponseDerivative[iNumResponseValue - 1];

		if (1 >= iNumResponseValue || adblPredictorOrdinate.length != iNumResponseValue ||
			adblCustomSlope.length != iNumResponseValue)
			return null;

		for (int i = 0; i < iNumResponseValue; ++i) {
			org.drip.math.segment.PredictorOrdinateResponseDerivative pord = null;

			try {
				pord = new org.drip.math.segment.PredictorOrdinateResponseDerivative (adblResponseValue[i],
					new double[] {adblCustomSlope[i]});
			} catch (java.lang.Exception e) {
				e.printStackTrace();

				return null;
			}

			if (0 == i)
				aPORDLeft[i] = pord;
			else if (iNumResponseValue - 1 == i)
				aPORDRight[i - 1] = pord;
			else {
				aPORDLeft[i] = pord;
				aPORDRight[i - 1] = pord;
			}
		}

		return regime.setupHermite (aPORDLeft, aPORDRight, null, fwr, iSetupMode) ? regime : null;
	}

	/**
	 * Create Hermite/Bessel C1 Cubic Spline Regime
	 * 
	 * @param strName Regime Name
	 * @param adblPredictorOrdinate Array of Predictor Ordinates
	 * @param adblResponseValue Array of Response Values
	 * @param aPRBP Array of Segment Builder Parameters
	 * @param fwr Fitness Weighted Response
	 * @param iSetupMode Segment Setup Mode
	 * 
	 * @return Hermite/Bessel C1 Cubic Spline Regime
	 */

	public static final org.drip.math.regime.MultiSegmentRegime CreateBesselCubicSplineRegime (
		final java.lang.String strName,
		final double[] adblPredictorOrdinate,
		final double[] adblResponseValue,
		final org.drip.math.segment.PredictorResponseBuilderParams[] aPRBP,
		final org.drip.math.segment.BestFitWeightedResponse fwr,
		final int iSetupMode)
	{
		return CustomSlopeHermiteSpline (strName, adblPredictorOrdinate, adblResponseValue, BesselC1
			(adblPredictorOrdinate, adblResponseValue), aPRBP, fwr, iSetupMode);
	}

	/**
	 * Create Hyman (1983) Monotone Preserving Regime. The reference is:
	 * 
	 * 	Hyman (1983) Accurate Monotonicity Preserving Cubic Interpolation -
	 *  	SIAM J on Numerical Analysis 4 (4), 645-654.
	 * 
	 * @param strName Regime Name
	 * @param adblPredictorOrdinate Array of Predictor Ordinates
	 * @param adblResponseValue Array of Response Values
	 * @param aPRBP Array of Segment Builder Parameters
	 * @param fwr Fitness Weighted Response
	 * @param iSetupMode Segment Setup Mode
	 * @param bEliminateSpuriousExtrema TRUE => Eliminate Spurious Extrema
	 * 
	 * @return Hyman (1983) Monotone Preserving Regime
	 */

	public static final org.drip.math.regime.MultiSegmentRegime CreateHyman83MonotoneRegime (
		final java.lang.String strName,
		final double[] adblPredictorOrdinate,
		final double[] adblResponseValue,
		final org.drip.math.segment.PredictorResponseBuilderParams[] aPRBP,
		final org.drip.math.segment.BestFitWeightedResponse fwr,
		final int iSetupMode,
		final boolean bEliminateSpuriousExtrema)
	{
		return CustomSlopeHermiteSpline (strName, adblPredictorOrdinate, adblResponseValue, Hyman83C1
			(adblPredictorOrdinate, adblResponseValue, bEliminateSpuriousExtrema), aPRBP, fwr, iSetupMode);
	}

	/**
	 * Create Hyman (1989) enhancement to the Hyman (1983) Monotone Preserving Regime. The reference is:
	 * 
	 * 	Doherty, Edelman, and Hyman (1989) Non-negative, monotonic, or convexity preserving cubic and quintic
	 *  	Hermite interpolation - Mathematics of Computation 52 (186), 471-494.
	 * 
	 * @param strName Regime Name
	 * @param adblPredictorOrdinate Array of Predictor Ordinates
	 * @param adblResponseValue Array of Response Values
	 * @param aPRBP Array of Segment Builder Parameters
	 * @param fwr Fitness Weighted Response
	 * @param iSetupMode Segment Setup Mode
	 * 
	 * @return Hyman (1989) Monotone Preserving Regime
	 */

	public static final org.drip.math.regime.MultiSegmentRegime CreateHyman89MonotoneRegime (
		final java.lang.String strName,
		final double[] adblPredictorOrdinate,
		final double[] adblResponseValue,
		final org.drip.math.segment.PredictorResponseBuilderParams[] aPRBP,
		final org.drip.math.segment.BestFitWeightedResponse fwr,
		final int iSetupMode)
	{
		return CustomSlopeHermiteSpline (strName, adblPredictorOrdinate, adblResponseValue, Hyman89C1
			(adblPredictorOrdinate, adblResponseValue), aPRBP, fwr, iSetupMode);
	}

	/**
	 * Create the Harmonic Monotone Preserving Regime. The reference is:
	 * 
	 * 	Fritcsh and Butland (1984) A Method for constructing local monotonic piece-wise cubic interpolants -
	 *  	SIAM J on Scientific and Statistical Computing 5, 300-304.
	 * 
	 * @param strName Regime Name
	 * @param adblPredictorOrdinate Array of Predictor Ordinates
	 * @param adblResponseValue Array of Response Values
	 * @param aPRBP Array of Segment Builder Parameters
	 * @param fwr Fitness Weighted Response
	 * @param iSetupMode Segment Setup Mode
	 * @param bApplyMonotoneFilter TRUE => Apply the Monotone Filter
	 * 
	 * @return Harmonic Monotone Preserving Regime
	 */

	public static final org.drip.math.regime.MultiSegmentRegime CreateHarmonicMonotoneRegime (
		final java.lang.String strName,
		final double[] adblPredictorOrdinate,
		final double[] adblResponseValue,
		final org.drip.math.segment.PredictorResponseBuilderParams[] aPRBP,
		final org.drip.math.segment.BestFitWeightedResponse fwr,
		final int iSetupMode,
		final boolean bApplyMonotoneFilter)
	{
		return CustomSlopeHermiteSpline (strName, adblPredictorOrdinate, adblResponseValue, HarmonicC1
			(adblPredictorOrdinate, adblResponseValue, bApplyMonotoneFilter), aPRBP, fwr, iSetupMode);
	}

	/**
	 * Create the Van Leer Limiter Regime. The reference is:
	 * 
	 * 	Van Leer (1974) Towards the Ultimate Conservative Difference Scheme. II - Monotonicity and
	 * 		Conservation combined in a Second-Order Scheme, Journal of Computational Physics 14 (4), 361-370.
	 * 
	 * @param strName Regime Name
	 * @param adblPredictorOrdinate Array of Predictor Ordinates
	 * @param adblResponseValue Array of Response Values
	 * @param aPRBP Array of Segment Builder Parameters
	 * @param fwr Fitness Weighted Response
	 * @param iSetupMode Segment Setup Mode
	 * @param bApplyMonotoneFilter TRUE => Apply the Monotone Filter
	 * 
	 * @return The Van Leer Limiter Regime
	 */

	public static final org.drip.math.regime.MultiSegmentRegime CreateVanLeerLimiterRegime (
		final java.lang.String strName,
		final double[] adblPredictorOrdinate,
		final double[] adblResponseValue,
		final org.drip.math.segment.PredictorResponseBuilderParams[] aPRBP,
		final org.drip.math.segment.BestFitWeightedResponse fwr,
		final int iSetupMode,
		final boolean bApplyMonotoneFilter)
	{
		return CustomSlopeHermiteSpline (strName, adblPredictorOrdinate, adblResponseValue, VanLeerLimiterC1
			(adblPredictorOrdinate, adblResponseValue, bApplyMonotoneFilter), aPRBP, fwr, iSetupMode);
	}

	/**
	 * Create the Huynh Le Floch Limiter Regime. The reference is:
	 * 
	 * 	Huynh (1993) Accurate Monotone Cubic Interpolation, SIAM J on Numerical Analysis 30 (1), 57-100.
	 * 
	 * @param strName Regime Name
	 * @param adblPredictorOrdinate Array of Predictor Ordinates
	 * @param adblResponseValue Array of Response Values
	 * @param aPRBP Array of Segment Builder Parameters
	 * @param fwr Fitness Weighted Response
	 * @param iSetupMode Segment Setup Mode
	 * @param bApplyMonotoneFilter TRUE => Apply the Monotone Filter
	 * 
	 * @return The Huynh Le Floch Limiter Regime
	 */

	public static final org.drip.math.regime.MultiSegmentRegime CreateHuynhLeFlochLimiterRegime (
		final java.lang.String strName,
		final double[] adblPredictorOrdinate,
		final double[] adblResponseValue,
		final org.drip.math.segment.PredictorResponseBuilderParams[] aPRBP,
		final org.drip.math.segment.BestFitWeightedResponse fwr,
		final int iSetupMode,
		final boolean bApplyMonotoneFilter)
	{
		return CustomSlopeHermiteSpline (strName, adblPredictorOrdinate, adblResponseValue,
			HuynhLeFlochLimiterC1 (adblPredictorOrdinate, adblResponseValue, bApplyMonotoneFilter), aPRBP,
				fwr, iSetupMode);
	}

	/**
	 * Generate the local control C1 Slope using the Akima Cubic Algorithm. The reference is:
	 * 
	 * 	Akima (1970): A New Method of Interpolation and Smooth Curve Fitting based on Local Procedures,
	 * 		Journal of the Association for the Computing Machinery 17 (4), 589-602.
	 * 
	 * @param strName Regime Name
	 * @param adblPredictorOrdinate Array of Predictor Ordinates
	 * @param adblResponseValue Array of Response Values
	 * @param aPRBP Array of Segment Builder Parameters
	 * @param fwr Fitness Weighted Response
	 * @param iSetupMode Segment Setup Mode
	 * 
	 * @return The Akima Local Control Regime Instance
	 */

	public static final org.drip.math.regime.MultiSegmentRegime CreateAkimaRegime (
		final java.lang.String strName,
		final double[] adblPredictorOrdinate,
		final double[] adblResponseValue,
		final org.drip.math.segment.PredictorResponseBuilderParams[] aPRBP,
		final org.drip.math.segment.BestFitWeightedResponse fwr,
		final int iSetupMode)
	{
		org.drip.math.regime.AkimaLocalControlRegime alcr =
			org.drip.math.regime.AkimaLocalControlRegime.Create (adblPredictorOrdinate, adblResponseValue);

		if (null == alcr) return null;

		return CustomSlopeHermiteSpline (strName, adblPredictorOrdinate, adblResponseValue, alcr.slopeC1(),
			aPRBP, fwr, iSetupMode);
	}

	/**
	 * Create the Kruger Regime. The reference is:
	 * 
	 * 	Kruger (2002) Constrained Cubic Spline Interpolations for Chemical Engineering Application,
	 *  	http://www.korf.co.uk/spline.pdf
	 * 
	 * @param strName Regime Name
	 * @param adblPredictorOrdinate Array of Predictor Ordinates
	 * @param adblResponseValue Array of Response Values
	 * @param aPRBP Array of Segment Builder Parameters
	 * @param fwr Fitness Weighted Response
	 * @param iSetupMode Segment Setup Mode
	 * 
	 * @return The Kruger Regime
	 */

	public static final org.drip.math.regime.MultiSegmentRegime CreateKrugerRegime (
		final java.lang.String strName,
		final double[] adblPredictorOrdinate,
		final double[] adblResponseValue,
		final org.drip.math.segment.PredictorResponseBuilderParams[] aPRBP,
		final org.drip.math.segment.BestFitWeightedResponse fwr,
		final int iSetupMode)
	{
		return CustomSlopeHermiteSpline (strName, adblPredictorOrdinate, adblResponseValue, KrugerC1
			(adblPredictorOrdinate, adblResponseValue), aPRBP, fwr, iSetupMode);
	}

	/**
	 * Verify if the given Quintic Polynomial is Monotone using the Hyman89 Algorithm
	 * 
	 * 	Doherty, Edelman, and Hyman (1989) Non-negative, monotonic, or convexity preserving cubic and quintic
	 *  	Hermite interpolation - Mathematics of Computation 52 (186), 471-494.
	 * 
	 * @param adblPredictorOrdinate Array of Predictor Ordinates
	 * @param adblResponseValue Array of Response Values
	 * @param adblFirstDerivative Array of First Derivatives
	 * @param adblSecondDerivative Array of Second Derivatives
	 * 
	 * @return TRUE => The given Quintic Polynomial is Monotone
	 * 
	 * @throws java.lang.Exception Thrown if the Monotonicity cannot be determined
	 */

	public static final boolean VerifyHyman89QuinticMonotonicity (
		final double[] adblPredictorOrdinate,
		final double[] adblResponseValue,
		final double[] adblFirstDerivative,
		final double[] adblSecondDerivative)
		throws java.lang.Exception
	{
		if (null == adblPredictorOrdinate || null == adblResponseValue || null == adblFirstDerivative || null
			== adblSecondDerivative)
			throw new java.lang.Exception
				("LocalControlRegimeBuilder::VerifyHyman89QuinticMonotonicity => Invalid Inputs");

		int iNumPredictor = adblPredictorOrdinate.length;

		if (1 >= iNumPredictor || iNumPredictor != adblResponseValue.length || iNumPredictor !=
			adblResponseValue.length || iNumPredictor != adblResponseValue.length)
			throw new java.lang.Exception
				("LocalControlRegimeBuilder::VerifyHyman89QuinticMonotonicity => Invalid Inputs");

		for (int i = 1; i < iNumPredictor - 1; ++i) {
			double dblAbsoluteResponseValue = java.lang.Math.abs (adblResponseValue[i]);

			double dblResponseValueSign = adblResponseValue[i] > 0. ? 1. : -1.;
			double dblHMinus = (adblPredictorOrdinate[i] - adblPredictorOrdinate[i - 1]);
			double dblHPlus = (adblPredictorOrdinate[i + 1] - adblPredictorOrdinate[i]);

			if (-5. * dblAbsoluteResponseValue / dblHPlus > dblResponseValueSign * adblFirstDerivative[i] ||
				5. * dblAbsoluteResponseValue / dblHMinus < dblResponseValueSign * adblFirstDerivative[i])
				return false;

			if (dblResponseValueSign * adblSecondDerivative[i] < dblResponseValueSign * java.lang.Math.max
				(8. * adblFirstDerivative[i] / dblHMinus - 20. * adblResponseValue[i] / dblHMinus /
					dblHMinus, -8. * adblFirstDerivative[i] / dblHPlus - 20. * adblResponseValue[i] /
						dblHPlus / dblHPlus))
				return false;
		}

		return true;
	}

	/**
	 * Generate C1 Slope Quintic Polynomial is Monotone using the Hyman89 Algorithm
	 * 
	 * 	Doherty, Edelman, and Hyman (1989) Non-negative, monotonic, or convexity preserving cubic and quintic
	 *  	Hermite interpolation - Mathematics of Computation 52 (186), 471-494.
	 * 
	 * @param adblPredictorOrdinate Array of Predictor Ordinates
	 * @param adblResponseValue Array of Response Values
	 * @param adblFirstDerivative Array of First Derivatives
	 * @param adblSecondDerivative Array of Second Derivatives
	 * 
	 * @return The C1 Slope Quintic Regime
	 */

	public static final double[] Hyman89QuinticMonotoneC1 (
		final double[] adblPredictorOrdinate,
		final double[] adblResponseValue,
		final double[] adblFirstDerivative,
		final double[] adblSecondDerivative)
	{
		if (null == adblPredictorOrdinate || null == adblResponseValue || null == adblFirstDerivative || null
			== adblSecondDerivative)
			return null;

		int iNumPredictor = adblPredictorOrdinate.length;

		if (1 >= iNumPredictor || iNumPredictor != adblResponseValue.length || iNumPredictor !=
			adblResponseValue.length || iNumPredictor != adblResponseValue.length)
			return null;

		double[] adblAdjFirstDerivative = new double[iNumPredictor];

		double[] adblNodeC1 = NodeC1 (adblPredictorOrdinate, adblResponseValue);

		double[] adblBesselC1 = BesselC1 (adblPredictorOrdinate, adblResponseValue);

		for (int i = 0; i < iNumPredictor; ++i) {
			if (i < 2 || i >= iNumPredictor - 2)
				adblAdjFirstDerivative[i] = adblBesselC1[i];
			else {
				double dblSign = 0.;
				double dblHMinus = (adblPredictorOrdinate[i] - adblPredictorOrdinate[i - 1]);
				double dblHPlus = (adblPredictorOrdinate[i + 1] - adblPredictorOrdinate[i]);

				if (adblFirstDerivative[i - 1] * adblFirstDerivative[i] < 0.)
					dblSign = adblResponseValue[i] > 0. ? 1. : -1.;

				double dblMinSlope = java.lang.Math.min (java.lang.Math.abs (adblFirstDerivative[i - 1]),
					java.lang.Math.abs (adblFirstDerivative[i]));

				if (dblSign >= 0.)
					adblAdjFirstDerivative[i] = java.lang.Math.min (java.lang.Math.max (0.,
						adblFirstDerivative[i]), 5. * dblMinSlope);
				else
					adblAdjFirstDerivative[i] = java.lang.Math.max (java.lang.Math.min (0.,
						adblFirstDerivative[i]), -5. * dblMinSlope);

				double dblA = java.lang.Math.max (0., adblAdjFirstDerivative[i] / adblNodeC1[i - 1]);

				double dblB = java.lang.Math.max (0., adblAdjFirstDerivative[i + 1] / adblNodeC1[i]);

				double dblDPlus = adblAdjFirstDerivative[i] * adblNodeC1[i] > 0. ? adblAdjFirstDerivative[i]
					: 0.;
				double dblDMinus = adblAdjFirstDerivative[i] * adblNodeC1[i - 1] > 0. ?
					adblAdjFirstDerivative[i] : 0.;
				double dblALeft = (-7.9 * dblDPlus - 0.26 * dblDPlus * dblB) / dblHPlus;
				double dblARight = ((20. - 2. * dblB) * adblNodeC1[i] - 8. * dblDPlus - 0.48 * dblDPlus *
					dblB) / dblHPlus;
				double dblBLeft = ((2. * dblA - 20.) * adblNodeC1[i - 1] + 8. * dblDMinus - 0.48 * dblDMinus
					* dblA) / dblHMinus;
				double dblBRight = (7.9 * dblDMinus + 0.26 * dblDMinus * dblA) / dblHMinus;

				if (dblARight <= dblBLeft || dblALeft >= dblBRight) {
					double dblDenom = ((8. + 0.48 * dblB) / dblHPlus) + ((8. + 0.48 * dblA) / dblHMinus);
					adblAdjFirstDerivative[i] = (20. - 2. * dblB) * adblNodeC1[i] / dblHPlus;
					adblAdjFirstDerivative[i] += (20. - 2. * dblA) * adblNodeC1[i - 1] / dblHMinus;
					adblAdjFirstDerivative[i] /= dblDenom;
				}
			}
		}

		return adblAdjFirstDerivative;
	}
}
