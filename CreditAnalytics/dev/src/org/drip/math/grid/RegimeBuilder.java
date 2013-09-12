
package org.drip.math.grid;

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

	public static final org.drip.math.grid.MultiSegmentRegime CreateUncalibratedRegimeInterpolator (
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
				if (null == (aSegment[i] = org.drip.math.segment.PredictorResponseBasisSpline.Create
					(adblPredictorOrdinate[i], adblPredictorOrdinate[i + 1],
						org.drip.math.spline.BasisSetBuilder.PolynomialBasisSet
							((org.drip.math.spline.PolynomialBasisSetParams) aPRBP[i].getBasisSetParams()),
								aPRBP[i].getShapeController(), aPRBP[i].getSegmentElasticParams())))
					return null;
			} else if (BASIS_SPLINE_BERNSTEIN_POLYNOMIAL.equalsIgnoreCase (strBasisSpline)) {
				if (null == (aSegment[i] = org.drip.math.segment.PredictorResponseBasisSpline.Create
					(adblPredictorOrdinate[i], adblPredictorOrdinate[i + 1],
						org.drip.math.spline.BasisSetBuilder.BernsteinPolynomialBasisSet
							((org.drip.math.spline.PolynomialBasisSetParams) aPRBP[i].getBasisSetParams()),
								aPRBP[i].getShapeController(), aPRBP[i].getSegmentElasticParams())))
					return null;
			} else if (org.drip.math.grid.RegimeBuilder.BASIS_SPLINE_HYPERBOLIC_TENSION.equalsIgnoreCase
				(strBasisSpline)) {
				if (null == (aSegment[i] = org.drip.math.segment.PredictorResponseBasisSpline.Create
					(adblPredictorOrdinate[i], adblPredictorOrdinate[i + 1],
						org.drip.math.spline.BasisSetBuilder.HyperbolicTensionBasisSet
							((org.drip.math.spline.ExponentialTensionBasisSetParams)
								aPRBP[i].getBasisSetParams()), aPRBP[i].getShapeController(),
									aPRBP[i].getSegmentElasticParams())))
					return null;
			} else if (org.drip.math.grid.RegimeBuilder.BASIS_SPLINE_EXPONENTIAL_TENSION.equalsIgnoreCase
				(strBasisSpline)) {
				if (null == (aSegment[i] = org.drip.math.segment.PredictorResponseBasisSpline.Create
					(adblPredictorOrdinate[i], adblPredictorOrdinate[i + 1],
						org.drip.math.spline.BasisSetBuilder.ExponentialTensionBasisSet
							((org.drip.math.spline.ExponentialTensionBasisSetParams)
								aPRBP[i].getBasisSetParams()), aPRBP[i].getShapeController(),
									aPRBP[i].getSegmentElasticParams())))
					return null;
			} else if (org.drip.math.grid.RegimeBuilder.BASIS_SPLINE_KAKLIS_PANDELIS.equalsIgnoreCase
				(strBasisSpline)) {
				if (null == (aSegment[i] = org.drip.math.segment.PredictorResponseBasisSpline.Create
					(adblPredictorOrdinate[i], adblPredictorOrdinate[i + 1],
						org.drip.math.spline.BasisSetBuilder.KaklisPandelisBasisSet
							((org.drip.math.spline.KaklisPandelisBasisSetParams)
								aPRBP[i].getBasisSetParams()), aPRBP[i].getShapeController(),
									aPRBP[i].getSegmentElasticParams())))
					return null;
			}
		}

		try {
			return new org.drip.math.grid.MultiSegmentCalibratableRegime (strName, aSegment, aPRBP);
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

	public static final org.drip.math.grid.MultiSegmentRegime CreateCalibratedRegimeInterpolator (
		final java.lang.String strName,
		final double[] adblPredictorOrdinate,
		final double[] adblResponseValue,
		final org.drip.math.segment.PredictorResponseBuilderParams[] aSBP,
		final org.drip.math.grid.RegimeCalibrationSetting rcs)
	{
		org.drip.math.grid.MultiSegmentRegime regime = CreateUncalibratedRegimeInterpolator (strName,
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

	public static final org.drip.math.grid.MultiSegmentRegime CreateCalibratedRegimeInterpolator (
		final java.lang.String strName,
		final double[] adblPredictorOrdinate,
		final double dblRegimeLeftResponseValue,
		final org.drip.math.segment.ResponseValueConstraint[] aRVC,
		final org.drip.math.segment.PredictorResponseBuilderParams[] aPRBP,
		final org.drip.math.grid.RegimeCalibrationSetting rcs)
	{
		org.drip.math.grid.MultiSegmentRegime regime = CreateUncalibratedRegimeInterpolator (strName,
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

	public static final org.drip.math.grid.MultiSegmentRegime CreateCalibratedRegimeInterpolator (
		final java.lang.String strName,
		final double[] adblPredictorOrdinate,
		final org.drip.math.segment.ResponseValueConstraint rvcRegimeLeft,
		final org.drip.math.segment.ResponseValueConstraint[] aRVC,
		final org.drip.math.segment.PredictorResponseBuilderParams[] aPRBP,
		final org.drip.math.grid.RegimeCalibrationSetting rcs)
	{
		org.drip.math.grid.MultiSegmentRegime regime = CreateUncalibratedRegimeInterpolator (strName,
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

	public static final org.drip.math.grid.MultiSegmentRegime CreateCalibratedRegimeInterpolator (
		final java.lang.String strName,
		final double[] adblPredictorOrdinate,
		final double dblResponseValue,
		final org.drip.math.segment.PredictorResponseBuilderParams prbp,
		final org.drip.math.grid.RegimeCalibrationSetting rcs)
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

		return CreateCalibratedRegimeInterpolator (strName, adblPredictorOrdinate, adblResponseValue, aPRBP,
			rcs);
	}
}
