
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
 * RegimeBuilder exports Regime creation/calibration methods to generate customized basis splines, with
 *  customized segment behavior using the segment control.
 *
 * @author Lakshmi Krishnamurthy
 */

public class RegimeBuilder {

	/**
	 * Polynomial Spline
	 */

	public static final java.lang.String BASIS_SPLINE_POLYNOMIAL = "Polynomial";

	/**
	 * Bernstein Polynomial Spline
	 */

	public static final java.lang.String BASIS_SPLINE_BERNSTEIN_POLYNOMIAL = "BernsteinPolynomial";

	/**
	 * Hyperbolic Tension Spline
	 */

	public static final java.lang.String BASIS_SPLINE_HYPERBOLIC_TENSION = "HyperbolicTension";

	/**
	 * Exponential Tension Spline
	 */

	public static final java.lang.String BASIS_SPLINE_EXPONENTIAL_TENSION = "ExponentialTension";

	/**
	 * Kaklis Pandelis Spline
	 */

	public static final java.lang.String BASIS_SPLINE_KAKLIS_PANDELIS = "KaklisPandelis";

	private static final double[] MonotoneSlope (
		final double[] adblPredictorOrdinate,
		final double[] adblResponseValue)
	{
		int iNumSegment = adblResponseValue.length - 1;
		double[] adblMonotoneSlope = new double[iNumSegment];

		for (int i = 0; i < iNumSegment; ++i)
			adblMonotoneSlope[i] = (adblResponseValue[i + 1] - adblResponseValue[i]) /
				(adblPredictorOrdinate[i + 1] - adblPredictorOrdinate[i]);

		return adblMonotoneSlope;
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

		double[] adblMonotoneSlope = MonotoneSlope (adblPredictorOrdinate, adblResponseValue);

		for (int i = 0; i < iNumResponse; ++i) {
			if (i < 2 || i >= iNumResponse - 2)
				adblEdgeSlope[i] = adblBesselC1[i];
			else {
				double dMuMinus = (adblMonotoneSlope[i - 1] * (2. * (adblPredictorOrdinate[i] -
					adblPredictorOrdinate[i - 1]) + adblPredictorOrdinate[i - 1] -
						adblPredictorOrdinate[i - 2]) - adblMonotoneSlope[i - 2] * (adblPredictorOrdinate[i]
							- adblPredictorOrdinate[i - 1])) / (adblPredictorOrdinate[i] -
								adblPredictorOrdinate[i - 2]);
				double dMu0 = (adblMonotoneSlope[i - 1] * (adblPredictorOrdinate[i + 1] -
					adblPredictorOrdinate[i]) + adblMonotoneSlope[i] * (adblPredictorOrdinate[i] -
						adblPredictorOrdinate[i - 1])) / (adblPredictorOrdinate[i + 1] -
							adblPredictorOrdinate[i - 1]);
				double dMuPlus = (adblMonotoneSlope[i] * (2. * (adblPredictorOrdinate[i + 1] -
					adblPredictorOrdinate[i]) + adblPredictorOrdinate[i + 2] - adblPredictorOrdinate[i + 1])
						- adblMonotoneSlope[i + 1] * (adblPredictorOrdinate[i + 1] -
							adblPredictorOrdinate[i])) / (adblPredictorOrdinate[i + 2] -
								adblPredictorOrdinate[i]);

				try {
					double dblM = 3 * org.drip.math.common.NumberUtil.Minimum (new double[]
						{java.lang.Math.abs (adblMonotoneSlope[i - 1]), java.lang.Math.abs
							(adblMonotoneSlope[i]), java.lang.Math.abs (dMu0), java.lang.Math.abs
								(dMuPlus)});

					if (!org.drip.math.common.NumberUtil.SameSign (new double[] {dMu0, dMuMinus,
						adblMonotoneSlope[i - 1] - adblMonotoneSlope[i - 2], adblMonotoneSlope[i] -
							adblMonotoneSlope[i - 1]}))
						dblM = java.lang.Math.max (dblM, 1.5 * java.lang.Math.min (java.lang.Math.abs (dMu0),
							java.lang.Math.abs (dMuMinus)));
					else if (!org.drip.math.common.NumberUtil.SameSign (new double[] {-dMu0, -dMuPlus,
						adblMonotoneSlope[i] - adblMonotoneSlope[i - 1], adblMonotoneSlope[i + 1] -
							adblMonotoneSlope[i]}))
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

	/**
	 * Create an uncalibrated Regime instance over the specified Predictor Ordinate Array using the specified
	 * 	Basis Spline Parameters for the Segment.
	 * 
	 * @param strName Name of the Regime
	 * @param adblPredictorOrdinate Predictor Ordinate Array
	 * @param aPRBP Array of Segment Builder Parameters
	 * 
	 * @return Regime instance
	 */

	public static final org.drip.math.regime.MultiSegmentRegime CreateUncalibratedRegimeEstimator (
		final java.lang.String strName,
		final double[] adblPredictorOrdinate,
		final org.drip.math.segment.PredictorResponseBuilderParams[] aPRBP)
	{
		if (null == adblPredictorOrdinate || 0 == adblPredictorOrdinate.length || null == aPRBP) return null;

		int iNumSegment = adblPredictorOrdinate.length - 1;
		org.drip.math.segment.PredictorResponse[] aSegment = new
			org.drip.math.segment.PredictorResponse[iNumSegment];

		if (1 > iNumSegment || iNumSegment != aPRBP.length) return null;

		for (int i = 0; i < iNumSegment; ++i) {
			if (null == aPRBP[i]) return null;

			java.lang.String strBasisSpline = aPRBP[i].getBasisSpline();

			if (null == strBasisSpline || (!BASIS_SPLINE_POLYNOMIAL.equalsIgnoreCase (strBasisSpline) &&
				!BASIS_SPLINE_BERNSTEIN_POLYNOMIAL.equalsIgnoreCase (strBasisSpline) &&
					!BASIS_SPLINE_HYPERBOLIC_TENSION.equalsIgnoreCase (strBasisSpline) &&
						!BASIS_SPLINE_EXPONENTIAL_TENSION.equalsIgnoreCase (strBasisSpline) &&
							!BASIS_SPLINE_KAKLIS_PANDELIS.equalsIgnoreCase (strBasisSpline)))
				return null;

			if (BASIS_SPLINE_POLYNOMIAL.equalsIgnoreCase (strBasisSpline)) {
				if (null == (aSegment[i] = org.drip.math.segment.LocalBasisPredictorResponse.Create
					(adblPredictorOrdinate[i], adblPredictorOrdinate[i + 1],
						org.drip.math.spline.BasisSetBuilder.PolynomialBasisSet
							((org.drip.math.spline.PolynomialBasisSetParams) aPRBP[i].getBasisSetParams()),
								aPRBP[i].getShapeController(), aPRBP[i].getSegmentElasticParams())))
					return null;
			} else if (BASIS_SPLINE_BERNSTEIN_POLYNOMIAL.equalsIgnoreCase (strBasisSpline)) {
				if (null == (aSegment[i] = org.drip.math.segment.LocalBasisPredictorResponse.Create
					(adblPredictorOrdinate[i], adblPredictorOrdinate[i + 1],
						org.drip.math.spline.BasisSetBuilder.BernsteinPolynomialBasisSet
							((org.drip.math.spline.PolynomialBasisSetParams) aPRBP[i].getBasisSetParams()),
								aPRBP[i].getShapeController(), aPRBP[i].getSegmentElasticParams())))
					return null;
			} else if (org.drip.math.regime.RegimeBuilder.BASIS_SPLINE_HYPERBOLIC_TENSION.equalsIgnoreCase
				(strBasisSpline)) {
				if (null == (aSegment[i] = org.drip.math.segment.LocalBasisPredictorResponse.Create
					(adblPredictorOrdinate[i], adblPredictorOrdinate[i + 1],
						org.drip.math.spline.BasisSetBuilder.HyperbolicTensionBasisSet
							((org.drip.math.spline.ExponentialTensionBasisSetParams)
								aPRBP[i].getBasisSetParams()), aPRBP[i].getShapeController(),
									aPRBP[i].getSegmentElasticParams())))
					return null;
			} else if (org.drip.math.regime.RegimeBuilder.BASIS_SPLINE_EXPONENTIAL_TENSION.equalsIgnoreCase
				(strBasisSpline)) {
				if (null == (aSegment[i] = org.drip.math.segment.LocalBasisPredictorResponse.Create
					(adblPredictorOrdinate[i], adblPredictorOrdinate[i + 1],
						org.drip.math.spline.BasisSetBuilder.ExponentialTensionBasisSet
							((org.drip.math.spline.ExponentialTensionBasisSetParams)
								aPRBP[i].getBasisSetParams()), aPRBP[i].getShapeController(),
									aPRBP[i].getSegmentElasticParams())))
					return null;
			} else if (org.drip.math.regime.RegimeBuilder.BASIS_SPLINE_KAKLIS_PANDELIS.equalsIgnoreCase
				(strBasisSpline)) {
				if (null == (aSegment[i] = org.drip.math.segment.LocalBasisPredictorResponse.Create
					(adblPredictorOrdinate[i], adblPredictorOrdinate[i + 1],
						org.drip.math.spline.BasisSetBuilder.KaklisPandelisBasisSet
							((org.drip.math.spline.KaklisPandelisBasisSetParams)
								aPRBP[i].getBasisSetParams()), aPRBP[i].getShapeController(),
									aPRBP[i].getSegmentElasticParams())))
					return null;
			}
		}

		try {
			return new org.drip.math.regime.MultiSegmentCalibratableRegime (strName, aSegment, aPRBP);
		} catch (java.lang.Exception e) {
			e.printStackTrace();
		}

		return null;
	}

	/**
	 * Create a calibrated Regime Instance over the specified array of Predictor Ordinates and Response
	 *  Values using the specified Basis Splines.
	 * 
	 * @param strName Name of the Regime
	 * @param adblPredictorOrdinate Predictor Ordinate Array
	 * @param adblResponseValue Response Value Array
	 * @param aSBP Array of Segment Builder Parameters
	 * @param rcs Regime Calibration Parameters
	 * 
	 * @return Regime instance
	 */

	public static final org.drip.math.regime.MultiSegmentRegime CreateCalibratedRegimeEstimator (
		final java.lang.String strName,
		final double[] adblPredictorOrdinate,
		final double[] adblResponseValue,
		final org.drip.math.segment.PredictorResponseBuilderParams[] aSBP,
		final org.drip.math.regime.RegimeCalibrationSetting rcs)
	{
		org.drip.math.regime.MultiSegmentRegime regime = CreateUncalibratedRegimeEstimator (strName,
			adblPredictorOrdinate, aSBP);

		if (null == regime || null == adblResponseValue) return null;

		int iNumRightNode = adblResponseValue.length - 1;
		double[] adblResponseValueRight = new double[iNumRightNode];

		if (0 == iNumRightNode) return null;

		for (int i = 0; i < iNumRightNode; ++i)
			adblResponseValueRight[i] = adblResponseValue[i + 1];

		return regime.setup (adblResponseValue[0], adblResponseValueRight, rcs) ? regime : null;
	}

	/**
	 * Create a calibrated Regime Instance over the specified Predictor Ordinates, Response Values, and their
	 * 	Constraints, using the specified Segment Builder Parameters.
	 * 
	 * @param strName Name of the Regime
	 * @param adblPredictorOrdinate Predictor Ordinate Array
	 * @param dblRegimeLeftResponseValue Left-most Y Point
	 * @param aRVC Array of Response Value Constraints - One per Segment
	 * @param aPRBP Array of Segment Builder Parameters - One per Segment
	 * @param rcs Regime Calibration Parameters
	 * 
	 * @return Regime Instance
	 */

	public static final org.drip.math.regime.MultiSegmentRegime CreateCalibratedRegimeEstimator (
		final java.lang.String strName,
		final double[] adblPredictorOrdinate,
		final double dblRegimeLeftResponseValue,
		final org.drip.math.segment.ResponseValueConstraint[] aRVC,
		final org.drip.math.segment.PredictorResponseBuilderParams[] aPRBP,
		final org.drip.math.regime.RegimeCalibrationSetting rcs)
	{
		org.drip.math.regime.MultiSegmentRegime regime = CreateUncalibratedRegimeEstimator (strName,
			adblPredictorOrdinate, aPRBP);

		return null == regime ? null : regime.setup (dblRegimeLeftResponseValue, aRVC, rcs) ? regime : null;
	}

	/**
	 * Create a calibrated Regime Instance over the specified Predictor Ordinates and the Response Value
	 * 	Constraints, with the Segment Builder Parameters.
	 * 
	 * @param strName Name of the Regime
	 * @param adblPredictorOrdinate Predictor Ordinate Array
	 * @param rvcRegimeLeft Regime Left Constraint
	 * @param aRVC Array of Segment Constraints - One per Segment
	 * @param aPRBP Array of Segment Builder Parameters - One per Segment
	 * @param rcs Regime Calibration Parameters
	 * 
	 * @return Regime Instance
	 */

	public static final org.drip.math.regime.MultiSegmentRegime CreateCalibratedRegimeEstimator (
		final java.lang.String strName,
		final double[] adblPredictorOrdinate,
		final org.drip.math.segment.ResponseValueConstraint rvcRegimeLeft,
		final org.drip.math.segment.ResponseValueConstraint[] aRVC,
		final org.drip.math.segment.PredictorResponseBuilderParams[] aPRBP,
		final org.drip.math.regime.RegimeCalibrationSetting rcs)
	{
		org.drip.math.regime.MultiSegmentRegime regime = CreateUncalibratedRegimeEstimator (strName,
			adblPredictorOrdinate, aPRBP);

		return null == regime ? null : regime.setup (rvcRegimeLeft, aRVC, rcs) ? regime : null;
	}

	/**
	 * Create a Calibrated Regime Instance from the Array of Predictor Ordinates and a flat Response Value
	 * 
	 * @param strName Name of the Regime
	 * @param adblPredictorOrdinate Predictor Ordinate Array
	 * @param dblResponseValue Response Value
	 * @param prbp Segment Builder Parameters - One per Segment
	 * @param rcs Regime Calibration Parameters
	 * 
	 * @return Regime Instance
	 */

	public static final org.drip.math.regime.MultiSegmentRegime CreateCalibratedRegimeEstimator (
		final java.lang.String strName,
		final double[] adblPredictorOrdinate,
		final double dblResponseValue,
		final org.drip.math.segment.PredictorResponseBuilderParams prbp,
		final org.drip.math.regime.RegimeCalibrationSetting rcs)
	{
		if (!org.drip.math.common.NumberUtil.IsValid (dblResponseValue) || null == adblPredictorOrdinate ||
			null == prbp)
			return null;

		int iNumPredictorOrdinate = adblPredictorOrdinate.length;

		if (1 >= iNumPredictorOrdinate) return null;

		double[] adblResponseValue = new double[iNumPredictorOrdinate];
		org.drip.math.segment.PredictorResponseBuilderParams[] aPRBP = new
			org.drip.math.segment.PredictorResponseBuilderParams[iNumPredictorOrdinate - 1];

		for (int i = 0; i < iNumPredictorOrdinate; ++i) {
			adblResponseValue[i] = dblResponseValue;

			if (0 != i) aPRBP[i - 1] = prbp;
		}

		return CreateCalibratedRegimeEstimator (strName, adblPredictorOrdinate, adblResponseValue, aPRBP,
			rcs);
	}

	/**
	 * Create Hermite/Bessel C1 Cubic Spline Regime
	 * 
	 * @param strName Regime Name
	 * @param adblPredictorOrdinate Array of Predictor Ordinates
	 * @param adblResponseValue Array of Response Values
	 * @param aPRBP Array of Segment Builder Parameters
	 * @param iSetupMode Segment Setup Mode
	 * 
	 * @return Hermite/Bessel C1 Cubic Spline Regime
	 */

	public static final org.drip.math.regime.MultiSegmentRegime CreateBesselCubicSplineRegime (
		final java.lang.String strName,
		final double[] adblPredictorOrdinate,
		final double[] adblResponseValue,
		final org.drip.math.segment.PredictorResponseBuilderParams[] aPRBP,
		final int iSetupMode)
	{
		org.drip.math.regime.MultiSegmentRegime regime = CreateUncalibratedRegimeEstimator (strName,
			adblPredictorOrdinate, aPRBP);

		if (null == regime || null == adblResponseValue) return null;

		int iNumResponseValue = adblResponseValue.length;
		org.drip.math.segment.PredictorOrdinateResponseDerivative[] aPORDLeft = new
			org.drip.math.segment.PredictorOrdinateResponseDerivative[iNumResponseValue - 1];
		org.drip.math.segment.PredictorOrdinateResponseDerivative[] aPORDRight = new
			org.drip.math.segment.PredictorOrdinateResponseDerivative[iNumResponseValue - 1];

		if (1 >= iNumResponseValue) return null;

		double[] adblBesselSlope = BesselC1 (adblPredictorOrdinate, adblResponseValue);

		if (null == adblBesselSlope || adblBesselSlope.length != iNumResponseValue) return null;

		for (int i = 0; i < iNumResponseValue; ++i) {
			org.drip.math.segment.PredictorOrdinateResponseDerivative pord = null;

			try {
				pord = new org.drip.math.segment.PredictorOrdinateResponseDerivative (adblResponseValue[i],
					new double[] {adblBesselSlope[i]});
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

		return regime.setupHermite (aPORDLeft, aPORDRight, null, iSetupMode) ? regime : null;
	}

	/**
	 * Create Hyman (1983) Monotone Preserving Regime. The reference is:
	 * 
	 * 	Accurate Monotonicity Preserving Cubic Interpolation - SIAM J on Numerical Analysis 4 (4), 645-654.
	 * 
	 * @param strName Regime Name
	 * @param adblPredictorOrdinate Array of Predictor Ordinates
	 * @param adblResponseValue Array of Response Values
	 * @param aPRBP Array of Segment Builder Parameters
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
		final int iSetupMode,
		final boolean bEliminateSpuriousExtrema)
	{
		org.drip.math.regime.MultiSegmentRegime regime = CreateUncalibratedRegimeEstimator (strName,
			adblPredictorOrdinate, aPRBP);

		if (null == regime || null == adblResponseValue) return null;

		int iNumResponseValue = adblResponseValue.length;
		org.drip.math.segment.PredictorOrdinateResponseDerivative[] aPORDLeft = new
			org.drip.math.segment.PredictorOrdinateResponseDerivative[iNumResponseValue - 1];
		org.drip.math.segment.PredictorOrdinateResponseDerivative[] aPORDRight = new
			org.drip.math.segment.PredictorOrdinateResponseDerivative[iNumResponseValue - 1];

		if (1 >= iNumResponseValue) return null;

		double[] adblHyman83Slope = Hyman83C1 (adblPredictorOrdinate, adblResponseValue,
			bEliminateSpuriousExtrema);

		if (null == adblHyman83Slope || adblHyman83Slope.length != iNumResponseValue) return null;

		for (int i = 0; i < iNumResponseValue; ++i) {
			org.drip.math.segment.PredictorOrdinateResponseDerivative pord = null;

			try {
				pord = new org.drip.math.segment.PredictorOrdinateResponseDerivative (adblResponseValue[i],
					new double[] {adblHyman83Slope[i]});
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

		return regime.setupHermite (aPORDLeft, aPORDRight, null, iSetupMode) ? regime : null;
	}

	/**
	 * Create Hyman (1989) enhancement to the Hyman (1983) Monotone Preserving Regime. The reference is:
	 * 
	 * 	Non-negative, monotonic, or convexity preserving cubic and quintic Hermite interpolation -
	 * 		Mathematics of Computation 52 (186), 471-494.
	 * 
	 * @param strName Regime Name
	 * @param adblPredictorOrdinate Array of Predictor Ordinates
	 * @param adblResponseValue Array of Response Values
	 * @param aPRBP Array of Segment Builder Parameters
	 * @param iSetupMode Segment Setup Mode
	 * 
	 * @return Hyman (1989) Monotone Preserving Regime
	 */

	public static final org.drip.math.regime.MultiSegmentRegime CreateHyman89MonotoneRegime (
		final java.lang.String strName,
		final double[] adblPredictorOrdinate,
		final double[] adblResponseValue,
		final org.drip.math.segment.PredictorResponseBuilderParams[] aPRBP,
		final int iSetupMode)
	{
		org.drip.math.regime.MultiSegmentRegime regime = CreateUncalibratedRegimeEstimator (strName,
			adblPredictorOrdinate, aPRBP);

		if (null == regime || null == adblResponseValue) return null;

		int iNumResponseValue = adblResponseValue.length;
		org.drip.math.segment.PredictorOrdinateResponseDerivative[] aPORDLeft = new
			org.drip.math.segment.PredictorOrdinateResponseDerivative[iNumResponseValue - 1];
		org.drip.math.segment.PredictorOrdinateResponseDerivative[] aPORDRight = new
			org.drip.math.segment.PredictorOrdinateResponseDerivative[iNumResponseValue - 1];

		if (1 >= iNumResponseValue) return null;

		double[] adblHyman89Slope = Hyman89C1 (adblPredictorOrdinate, adblResponseValue);

		if (null == adblHyman89Slope || adblHyman89Slope.length != iNumResponseValue) return null;

		for (int i = 0; i < iNumResponseValue; ++i) {
			org.drip.math.segment.PredictorOrdinateResponseDerivative pord = null;

			try {
				pord = new org.drip.math.segment.PredictorOrdinateResponseDerivative (adblResponseValue[i],
					new double[] {adblHyman89Slope[i]});
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

		return regime.setupHermite (aPORDLeft, aPORDRight, null, iSetupMode) ? regime : null;
	}
}
