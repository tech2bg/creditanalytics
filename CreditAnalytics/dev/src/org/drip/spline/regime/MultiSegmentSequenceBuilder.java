
package org.drip.spline.regime;

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
 * MultiSegmentSequenceBuilder exports Regime creation/calibration methods to generate customized basis splines, with
 *  customized segment behavior using the segment control.
 *
 * @author Lakshmi Krishnamurthy
 */

public class MultiSegmentSequenceBuilder {

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
	 * Exponential Rational Basis Spline
	 */

	public static final java.lang.String BASIS_SPLINE_EXPONENTIAL_RATIONAL = "ExponentialRational";

	/**
	 * Exponential Mixture Basis Spline
	 */

	public static final java.lang.String BASIS_SPLINE_EXPONENTIAL_MIXTURE = "ExponentialMixture";

	/**
	 * Koch-Lyche-Kvasov Exponential Tension Spline
	 */

	public static final java.lang.String BASIS_SPLINE_KLK_EXPONENTIAL_TENSION = "KLKExponentialTension";

	/**
	 * Koch-Lyche-Kvasov Hyperbolic Tension Spline
	 */

	public static final java.lang.String BASIS_SPLINE_KLK_HYPERBOLIC_TENSION = "KLKHyperbolicTension";

	/**
	 * Koch-Lyche-Kvasov Rational Linear Tension Spline
	 */

	public static final java.lang.String BASIS_SPLINE_KLK_RATIONAL_LINEAR_TENSION = "KLKRationalLinearTension";

	/**
	 * Koch-Lyche-Kvasov Rational Quadratic Tension Spline
	 */

	public static final java.lang.String BASIS_SPLINE_KLK_RATIONAL_QUADRATIC_TENSION = "KLKRationalQuadraticTension";

	/**
	 * Create an uncalibrated Regime instance over the specified Predictor Ordinate Array using the specified
	 * 	Basis Spline Parameters for the Segment.
	 * 
	 * @param strName Name of the Regime
	 * @param adblPredictorOrdinate Predictor Ordinate Array
	 * @param aSCBC Array of Segment Builder Parameters
	 * 
	 * @return Regime instance
	 */

	public static final org.drip.spline.segment.ConstitutiveState[] CreateSegmentSet (
		final double[] adblPredictorOrdinate,
		final org.drip.spline.params.SegmentCustomBuilderControl[] aSCBC)
	{
		if (null == adblPredictorOrdinate || null == aSCBC) return null;

		int iNumSegment = adblPredictorOrdinate.length - 1;

		if (1 > iNumSegment || iNumSegment != aSCBC.length) return null;

		org.drip.spline.segment.ConstitutiveState[] aCS = new
			org.drip.spline.segment.ConstitutiveState[iNumSegment];

		for (int i = 0; i < iNumSegment; ++i) {
			if (null == aSCBC[i]) return null;

			java.lang.String strBasisSpline = aSCBC[i].basisSpline();

			if (null == strBasisSpline || (!BASIS_SPLINE_POLYNOMIAL.equalsIgnoreCase (strBasisSpline) &&
				!BASIS_SPLINE_BERNSTEIN_POLYNOMIAL.equalsIgnoreCase (strBasisSpline) &&
					!BASIS_SPLINE_HYPERBOLIC_TENSION.equalsIgnoreCase (strBasisSpline) &&
						!BASIS_SPLINE_EXPONENTIAL_TENSION.equalsIgnoreCase (strBasisSpline) &&
							!BASIS_SPLINE_KAKLIS_PANDELIS.equalsIgnoreCase (strBasisSpline) &&
								!BASIS_SPLINE_EXPONENTIAL_RATIONAL.equalsIgnoreCase (strBasisSpline) &&
									!BASIS_SPLINE_EXPONENTIAL_MIXTURE.equalsIgnoreCase (strBasisSpline) &&
										!BASIS_SPLINE_KLK_EXPONENTIAL_TENSION.equalsIgnoreCase
											(strBasisSpline) &&
												!BASIS_SPLINE_KLK_HYPERBOLIC_TENSION.equalsIgnoreCase
													(strBasisSpline) &&
														!BASIS_SPLINE_KLK_RATIONAL_LINEAR_TENSION.equalsIgnoreCase
				(strBasisSpline) && !BASIS_SPLINE_KLK_RATIONAL_QUADRATIC_TENSION.equalsIgnoreCase
					(strBasisSpline)))
				return null;

			if (BASIS_SPLINE_POLYNOMIAL.equalsIgnoreCase (strBasisSpline)) {
				if (null == (aCS[i] = org.drip.spline.segment.ConstitutiveState.Create
					(adblPredictorOrdinate[i], adblPredictorOrdinate[i + 1],
						org.drip.spline.basis.FunctionSetBuilder.PolynomialBasisSet
							((org.drip.spline.basis.PolynomialFunctionSetParams) aSCBC[i].basisSetParams()),
								aSCBC[i].shapeController(), aSCBC[i].inelasticParams())))
					return null;
			} else if (BASIS_SPLINE_BERNSTEIN_POLYNOMIAL.equalsIgnoreCase (strBasisSpline)) {
				if (null == (aCS[i] = org.drip.spline.segment.ConstitutiveState.Create
					(adblPredictorOrdinate[i], adblPredictorOrdinate[i + 1],
						org.drip.spline.basis.FunctionSetBuilder.BernsteinPolynomialBasisSet
							((org.drip.spline.basis.PolynomialFunctionSetParams) aSCBC[i].basisSetParams()),
								aSCBC[i].shapeController(), aSCBC[i].inelasticParams())))
					return null;
			} else if (BASIS_SPLINE_HYPERBOLIC_TENSION.equalsIgnoreCase (strBasisSpline)) {
				if (null == (aCS[i] = org.drip.spline.segment.ConstitutiveState.Create
					(adblPredictorOrdinate[i], adblPredictorOrdinate[i + 1],
						org.drip.spline.basis.FunctionSetBuilder.HyperbolicTensionBasisSet
							((org.drip.spline.basis.ExponentialTensionSetParams) aSCBC[i].basisSetParams()),
								aSCBC[i].shapeController(), aSCBC[i].inelasticParams())))
					return null;
			} else if (BASIS_SPLINE_EXPONENTIAL_TENSION.equalsIgnoreCase (strBasisSpline)) {
				if (null == (aCS[i] = org.drip.spline.segment.ConstitutiveState.Create
					(adblPredictorOrdinate[i], adblPredictorOrdinate[i + 1],
						org.drip.spline.basis.FunctionSetBuilder.ExponentialTensionBasisSet
							((org.drip.spline.basis.ExponentialTensionSetParams) aSCBC[i].basisSetParams()),
								aSCBC[i].shapeController(), aSCBC[i].inelasticParams())))
					return null;
			} else if (BASIS_SPLINE_KAKLIS_PANDELIS.equalsIgnoreCase (strBasisSpline)) {
				if (null == (aCS[i] = org.drip.spline.segment.ConstitutiveState.Create
					(adblPredictorOrdinate[i], adblPredictorOrdinate[i + 1],
						org.drip.spline.basis.FunctionSetBuilder.KaklisPandelisBasisSet
							((org.drip.spline.basis.KaklisPandelisSetParams) aSCBC[i].basisSetParams()),
								aSCBC[i].shapeController(), aSCBC[i].inelasticParams())))
					return null;
			} else if (BASIS_SPLINE_EXPONENTIAL_RATIONAL.equalsIgnoreCase (strBasisSpline)) {
				if (null == (aCS[i] = org.drip.spline.segment.ConstitutiveState.Create
					(adblPredictorOrdinate[i], adblPredictorOrdinate[i + 1],
						org.drip.spline.basis.FunctionSetBuilder.ExponentialRationalBasisSet
							((org.drip.spline.basis.ExponentialRationalSetParams) aSCBC[i].basisSetParams()),
								aSCBC[i].shapeController(), aSCBC[i].inelasticParams())))
					return null;
			} else if (BASIS_SPLINE_EXPONENTIAL_MIXTURE.equalsIgnoreCase (strBasisSpline)) {
				if (null == (aCS[i] = org.drip.spline.segment.ConstitutiveState.Create
					(adblPredictorOrdinate[i], adblPredictorOrdinate[i + 1],
						org.drip.spline.basis.FunctionSetBuilder.ExponentialMixtureBasisSet
							((org.drip.spline.basis.ExponentialMixtureSetParams) aSCBC[i].basisSetParams()),
								aSCBC[i].shapeController(), aSCBC[i].inelasticParams())))
					return null;
			} else if (BASIS_SPLINE_KLK_EXPONENTIAL_TENSION.equalsIgnoreCase (strBasisSpline)) {
				if (null == (aCS[i] = org.drip.spline.segment.ConstitutiveState.Create
					(adblPredictorOrdinate[i], adblPredictorOrdinate[i + 1],
						org.drip.spline.tension.KochLycheKvasovFamily.FromExponentialPrimitive
							((org.drip.spline.basis.ExponentialTensionSetParams) aSCBC[i].basisSetParams()),
								aSCBC[i].shapeController(), aSCBC[i].inelasticParams())))
					return null;
			} else if (BASIS_SPLINE_KLK_HYPERBOLIC_TENSION.equalsIgnoreCase (strBasisSpline)) {
				if (null == (aCS[i] = org.drip.spline.segment.ConstitutiveState.Create
					(adblPredictorOrdinate[i], adblPredictorOrdinate[i + 1],
						org.drip.spline.tension.KochLycheKvasovFamily.FromHyperbolicPrimitive
							((org.drip.spline.basis.ExponentialTensionSetParams) aSCBC[i].basisSetParams()),
								aSCBC[i].shapeController(), aSCBC[i].inelasticParams())))
					return null;
			} else if (BASIS_SPLINE_KLK_RATIONAL_LINEAR_TENSION.equalsIgnoreCase (strBasisSpline)) {
				if (null == (aCS[i] = org.drip.spline.segment.ConstitutiveState.Create
					(adblPredictorOrdinate[i], adblPredictorOrdinate[i + 1],
						org.drip.spline.tension.KochLycheKvasovFamily.FromRationalLinearPrimitive
							((org.drip.spline.basis.ExponentialTensionSetParams) aSCBC[i].basisSetParams()),
								aSCBC[i].shapeController(), aSCBC[i].inelasticParams())))
					return null;
			} else if (BASIS_SPLINE_KLK_RATIONAL_QUADRATIC_TENSION.equalsIgnoreCase (strBasisSpline)) {
				if (null == (aCS[i] = org.drip.spline.segment.ConstitutiveState.Create
					(adblPredictorOrdinate[i], adblPredictorOrdinate[i + 1],
						org.drip.spline.tension.KochLycheKvasovFamily.FromRationalQuadraticPrimitive
							((org.drip.spline.basis.ExponentialTensionSetParams) aSCBC[i].basisSetParams()),
								aSCBC[i].shapeController(), aSCBC[i].inelasticParams())))
					return null;
			}
		}

		return aCS;
	}

	/**
	 * Create an uncalibrated Regime instance over the specified Predictor Ordinate Array using the specified
	 * 	Basis Spline Parameters for the Segment.
	 * 
	 * @param strName Name of the Regime
	 * @param adblPredictorOrdinate Predictor Ordinate Array
	 * @param aSCBC Array of Segment Builder Parameters
	 * 
	 * @return Regime instance
	 */

	public static final org.drip.spline.regime.MultiSegmentSequence CreateUncalibratedRegimeEstimator (
		final java.lang.String strName,
		final double[] adblPredictorOrdinate,
		final org.drip.spline.params.SegmentCustomBuilderControl[] aSCBC)
	{
		try {
			return new org.drip.spline.regime.CalibratableMultiSegmentSequence (strName, CreateSegmentSet
				(adblPredictorOrdinate, aSCBC), aSCBC);
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
	 * @param aSCBC Array of Segment Builder Parameters
	 * @param rbfr Regime Fitness Weighted Response
	 * @param iCalibrationBoundaryCondition The Calibration Boundary Condition
	 * @param iCalibrationDetail The Calibration Detail
	 * 
	 * @return Regime instance
	 */

	public static final org.drip.spline.regime.MultiSegmentSequence CreateCalibratedRegimeEstimator (
		final java.lang.String strName,
		final double[] adblPredictorOrdinate,
		final double[] adblResponseValue,
		final org.drip.spline.params.SegmentCustomBuilderControl[] aSCBC,
		final org.drip.spline.params.RegimeBestFitResponse rbfr,
		final int iCalibrationBoundaryCondition,
		final int iCalibrationDetail)
	{
		org.drip.spline.regime.MultiSegmentSequence mss = CreateUncalibratedRegimeEstimator (strName,
			adblPredictorOrdinate, aSCBC);

		if (null == mss || null == adblResponseValue) return null;

		int iNumRightNode = adblResponseValue.length - 1;
		double[] adblResponseValueRight = new double[iNumRightNode];

		if (0 == iNumRightNode) return null;

		for (int i = 0; i < iNumRightNode; ++i)
			adblResponseValueRight[i] = adblResponseValue[i + 1];

		return mss.setup (adblResponseValue[0], adblResponseValueRight, rbfr, iCalibrationBoundaryCondition,
			iCalibrationDetail) ? mss : null;
	}

	/**
	 * Create a calibrated Regime Instance over the specified Predictor Ordinates, Response Values, and their
	 * 	Constraints, using the specified Segment Builder Parameters.
	 * 
	 * @param strName Name of the Regime
	 * @param adblPredictorOrdinate Predictor Ordinate Array
	 * @param dblRegimeLeftResponseValue Left-most Y Point
	 * @param aSRVC Array of Response Value Constraints - One per Segment
	 * @param aSCBC Array of Segment Builder Parameters - One per Segment
	 * @param rbfr Regime Fitness Weighted Response
	 * @param iCalibrationBoundaryCondition The Calibration Boundary Condition
	 * @param iCalibrationDetail The Calibration Detail
	 * 
	 * @return Regime Instance
	 */

	public static final org.drip.spline.regime.MultiSegmentSequence CreateCalibratedRegimeEstimator (
		final java.lang.String strName,
		final double[] adblPredictorOrdinate,
		final double dblRegimeLeftResponseValue,
		final org.drip.spline.params.SegmentResponseValueConstraint[] aSRVC,
		final org.drip.spline.params.SegmentCustomBuilderControl[] aSCBC,
		final org.drip.spline.params.RegimeBestFitResponse rbfr,
		final int iCalibrationBoundaryCondition,
		final int iCalibrationDetail)
	{
		org.drip.spline.regime.MultiSegmentSequence mss = CreateUncalibratedRegimeEstimator (strName,
			adblPredictorOrdinate, aSCBC);

		return null == mss ? null : mss.setup (dblRegimeLeftResponseValue, aSRVC, rbfr,
			iCalibrationBoundaryCondition, iCalibrationDetail) ? mss : null;
	}

	/**
	 * Create a calibrated Regime Instance over the specified Predictor Ordinates and the Response Value
	 * 	Constraints, with the Segment Builder Parameters.
	 * 
	 * @param strName Name of the Regime
	 * @param adblPredictorOrdinate Predictor Ordinate Array
	 * @param srvcRegimeLeft Regime Left Constraint
	 * @param aSRVC Array of Segment Constraints - One per Segment
	 * @param aSCBC Array of Segment Builder Parameters - One per Segment
	 * @param rbfr Regime Fitness Weighted Response
	 * @param iCalibrationBoundaryCondition The Calibration Boundary Condition
	 * @param iCalibrationDetail The Calibration Detail
	 * 
	 * @return Regime Instance
	 */

	public static final org.drip.spline.regime.MultiSegmentSequence CreateCalibratedRegimeEstimator (
		final java.lang.String strName,
		final double[] adblPredictorOrdinate,
		final org.drip.spline.params.SegmentResponseValueConstraint srvcRegimeLeft,
		final org.drip.spline.params.SegmentResponseValueConstraint[] aSRVC,
		final org.drip.spline.params.SegmentCustomBuilderControl[] aSCBC,
		final org.drip.spline.params.RegimeBestFitResponse rbfr,
		final int iCalibrationBoundaryCondition,
		final int iCalibrationDetail)
	{
		org.drip.spline.regime.MultiSegmentSequence mss = CreateUncalibratedRegimeEstimator (strName,
			adblPredictorOrdinate, aSCBC);

		return null == mss ? null : mss.setup (srvcRegimeLeft, aSRVC, rbfr, iCalibrationBoundaryCondition,
			iCalibrationDetail) ? mss : null;
	}

	/**
	 * Create a Calibrated Regime Instance from the Array of Predictor Ordinates and a flat Response Value
	 * 
	 * @param strName Name of the Regime
	 * @param adblPredictorOrdinate Predictor Ordinate Array
	 * @param dblResponseValue Response Value
	 * @param scbc Segment Builder Parameters - One per Segment
	 * @param rbfr Regime Fitness Weighted Response
	 * @param iCalibrationBoundaryCondition The Calibration Boundary Condition
	 * @param iCalibrationDetail The Calibration Detail
	 * 
	 * @return Regime Instance
	 */

	public static final org.drip.spline.regime.MultiSegmentSequence CreateCalibratedRegimeEstimator (
		final java.lang.String strName,
		final double[] adblPredictorOrdinate,
		final double dblResponseValue,
		final org.drip.spline.params.SegmentCustomBuilderControl scbc,
		final org.drip.spline.params.RegimeBestFitResponse rbfr,
		final int iCalibrationBoundaryCondition,
		final int iCalibrationDetail)
	{
		if (!org.drip.quant.common.NumberUtil.IsValid (dblResponseValue) || null == adblPredictorOrdinate ||
			null == scbc)
			return null;

		int iNumPredictorOrdinate = adblPredictorOrdinate.length;

		if (1 >= iNumPredictorOrdinate) return null;

		double[] adblResponseValue = new double[iNumPredictorOrdinate];
		org.drip.spline.params.SegmentCustomBuilderControl[] aSCBC = new
			org.drip.spline.params.SegmentCustomBuilderControl[iNumPredictorOrdinate - 1];

		for (int i = 0; i < iNumPredictorOrdinate; ++i) {
			adblResponseValue[i] = dblResponseValue;

			if (0 != i) aSCBC[i - 1] = scbc;
		}

		return CreateCalibratedRegimeEstimator (strName, adblPredictorOrdinate, adblResponseValue, aSCBC,
			rbfr, iCalibrationBoundaryCondition, iCalibrationDetail);
	}

	/**
	 * Create a Regression Spline Instance over the specified array of Predictor Ordinate Knot Points and the
	 *  Set of the Points to be Best Fit.
	 * 
	 * @param strName Name of the Regime
	 * @param adblKnotPredictorOrdinate Array of the Predictor Ordinate Knots
	 * @param aSCBC Array of Segment Builder Parameters
	 * @param rbfr Regime Fitness Weighted Response
	 * @param iCalibrationBoundaryCondition The Calibration Boundary Condition
	 * @param iCalibrationDetail The Calibration Detail
	 * 
	 * @return Regime instance
	 */

	public static final org.drip.spline.regime.MultiSegmentSequence CreateRegressionSplineEstimator (
		final java.lang.String strName,
		final double[] adblKnotPredictorOrdinate,
		final org.drip.spline.params.SegmentCustomBuilderControl[] aSCBC,
		final org.drip.spline.params.RegimeBestFitResponse rbfr,
		final int iCalibrationBoundaryCondition,
		final int iCalibrationDetail)
	{
		org.drip.spline.regime.MultiSegmentSequence mss = CreateUncalibratedRegimeEstimator (strName,
			adblKnotPredictorOrdinate, aSCBC);

		if (null == mss) return null;

		return mss.setup (null, null, rbfr, iCalibrationBoundaryCondition, iCalibrationDetail) ? mss : null;
	}
}
