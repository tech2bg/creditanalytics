	
package org.drip.sample.regime;

import org.drip.quant.common.FormatUtil;
import org.drip.quant.function1D.*;
import org.drip.spline.basis.*;
import org.drip.spline.params.*;
import org.drip.spline.pchip.*;
import org.drip.spline.regime.*;

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
 * RegimeEstimation demonstrates the Regime builder and usage API. It shows the following:
 * 	- Construction of segment control parameters - polynomial (regular/Bernstein) segment control,
 * 		exponential/hyperbolic tension segment control, Kaklis-Pandelis tension segment control.
 * 	- Control the segment using the rational shape controller, and the appropriate Ck
 * 	- Construct a calibrated regime Estimator.
 * 	- Insert a knot into the regime
 * 	- Estimate the node value and the node value Jacobian
 * 	- Calculate the segment/regime monotonicity
 *
 * @author Lakshmi Krishnamurthy
 */

public class RegimeEstimation {

	/**
	 * Build Polynomial Segment Control Parameters
	 * 
	 * @param iNumBasis Number of Polynomial Basis Functions
	 * @param segParams Inelastic Segment Parameters
	 * @param rssc Shape Controller
	 * 
	 * 	WARNING: Insufficient Error Checking, so use caution
	 * 
	 * @return Polynomial Segment Control Parameters
	 */

	public static final SegmentCustomBuilderControl PolynomialSegmentControlParams (
		final int iNumBasis,
		final SegmentDesignInelasticControl segParams,
		final ResponseScalingShapeControl rssc)
		throws Exception
	{
		return new SegmentCustomBuilderControl (
			MultiSegmentSequenceBuilder.BASIS_SPLINE_POLYNOMIAL,
			new PolynomialFunctionSetParams (iNumBasis),
			segParams,
			rssc);
	}

	/**
	 * Build Bernstein Polynomial Segment Control Parameters
	 * 
	 * @param iNumBasis Number of Polynomial Basis Functions
	 * @param segParams Inelastic Segment Parameters
	 * @param rssc Shape Controller
	 * 
	 * 	WARNING: Insufficient Error Checking, so use caution
	 * 
	 * @return Bernstein Polynomial Segment Control Parameters
	 */

	public static final SegmentCustomBuilderControl BernsteinPolynomialSegmentControlParams (
		final int iNumBasis,
		final SegmentDesignInelasticControl segParams,
		final ResponseScalingShapeControl rssc)
		throws Exception
	{
		return new SegmentCustomBuilderControl (
			MultiSegmentSequenceBuilder.BASIS_SPLINE_BERNSTEIN_POLYNOMIAL,
			new PolynomialFunctionSetParams (iNumBasis),
			segParams,
			rssc);
	}

	/**
	 * Build Exponential Tension Segment Control Parameters
	 * 
	 * @param dblTension Segment Tension
	 * @param segParams Inelastic Segment Parameters
	 * @param rssc Shape Controller
	 * 
	 * 	WARNING: Insufficient Error Checking, so use caution
	 * 
	 * @return Exponential Tension Segment Control Parameters
	 */

	public static final SegmentCustomBuilderControl ExponentialTensionSegmentControlParams (
		final double dblTension,
		final SegmentDesignInelasticControl segParams,
		final ResponseScalingShapeControl rssc)
		throws Exception
	{
		return new SegmentCustomBuilderControl (
			MultiSegmentSequenceBuilder.BASIS_SPLINE_EXPONENTIAL_TENSION,
			new ExponentialTensionSetParams (dblTension),
			segParams,
			rssc);
	}

	/**
	 * Build Hyperbolic Tension Segment Control Parameters
	 * 
	 * @param dblTension Segment Tension
	 * @param segParams Inelastic Segment Parameters
	 * @param rssc Shape Controller
	 * 
	 * 	WARNING: Insufficient Error Checking, so use caution
	 * 
	 * @return Hyperbolic Tension Segment Control Parameters
	 */

	public static final SegmentCustomBuilderControl HyperbolicTensionSegmentControlParams (
		final double dblTension,
		final SegmentDesignInelasticControl segParams,
		final ResponseScalingShapeControl rssc)
		throws Exception
	{
		return new SegmentCustomBuilderControl (
			MultiSegmentSequenceBuilder.BASIS_SPLINE_HYPERBOLIC_TENSION,
			new ExponentialTensionSetParams (dblTension),
			segParams,
			rssc);
	}

	/**
	 * Build Kaklis-Pandelis Segment Control Parameters
	 * 
	 * @param iKPTensionDegree KP Polynomial Tension Degree
	 * @param segParams Inelastic Segment Parameters
	 * @param rssc Shape Controller
	 * 
	 * 	WARNING: Insufficient Error Checking, so use caution
	 * 
	 * @return Kaklis-Pandelis Segment Control Parameters
	 */

	public static final SegmentCustomBuilderControl KaklisPandelisSegmentControlParams (
		final int iKPTensionDegree,
		final SegmentDesignInelasticControl segParams,
		final ResponseScalingShapeControl rssc)
		throws Exception
	{
		return new SegmentCustomBuilderControl (
			MultiSegmentSequenceBuilder.BASIS_SPLINE_KAKLIS_PANDELIS,
			new KaklisPandelisSetParams (iKPTensionDegree),
			segParams,
			rssc);
	}

	/**
	 * Perform the following sequence of tests for a given segment control for a predictor/response range
	 * 	- Estimate
	 *  - Compute the segment-by-segment monotonicity
	 *  - Regime Jacobian
	 *  - Regime knot insertion
	 * 
	 * @param adblX The Predictor Array
	 * @param adblY The Response Array
	 * @param sbp The Segment Builder Parameters
	 * 
	 * 	WARNING: Insufficient Error Checking, so use caution
	 */

	public static final void BasisSplineRegimeTest (
		final double[] adblX,
		final double[] adblY,
		final SegmentCustomBuilderControl sbp)
		throws Exception
	{
		double dblX = 1.;
		double dblXMax = 10.;

		/*
		 * Array of Segment Builder Parameters - one per segment
		 */

		SegmentCustomBuilderControl[] aSBP = new SegmentCustomBuilderControl[adblX.length - 1]; 

		for (int i = 0; i < adblX.length - 1; ++i)
			aSBP[i] = sbp;

		/*
		 * Construct a Regime instance 
		 */

		MultiSegmentSequence regime = MultiSegmentSequenceBuilder.CreateCalibratedRegimeEstimator (
			"SPLINE_REGIME",
			adblX, // predictors
			adblY, // responses
			aSBP, // Basis Segment Builder parameters
			null, 
			MultiSegmentSequence.BOUNDARY_CONDITION_NATURAL, // Boundary Condition - Natural
			MultiSegmentSequence.CALIBRATE); // Calibrate the Regime predictors to the responses

		/*
		 * Estimate, compute the segment-by-segment monotonicity and the Regime Jacobian
		 */

		while (dblX <= dblXMax) {
			System.out.println ("Y[" + dblX + "] " + FormatUtil.FormatDouble (regime.responseValue (dblX), 1, 2, 1.) + " | " +
				regime.monotoneType (dblX));

			System.out.println ("Jacobian Y[" + dblX + "]=" + regime.jackDResponseDResponseInput (dblX).displayString());

			dblX += 1.;
		}

		System.out.println ("SPLINE_REGIME DCPE: " + regime.dcpe());

		/*
		 * Construct a new Regime instance by inserting a pair of of predictor/response knots
		 */

		MultiSegmentSequence regimeInsert = MultiSegmentSequenceModifier.InsertKnot (regime,
			9.,
			10.,
			MultiSegmentSequence.BOUNDARY_CONDITION_NATURAL, // Boundary Condition - Natural
			MultiSegmentSequence.CALIBRATE); // Calibrate the Regime predictors to the responses

		dblX = 1.;

		/*
		 * Estimate, compute the sgement-by-segment monotonicty and the Regime Jacobian
		 */

		while (dblX <= dblXMax) {
			System.out.println ("Inserted Y[" + dblX + "] " + FormatUtil.FormatDouble (regimeInsert.responseValue (dblX), 1, 2, 1.)
				+ " | " + regimeInsert.monotoneType (dblX));

			dblX += 1.;
		}

		System.out.println ("SPLINE_REGIME_INSERT DCPE: " + regimeInsert.dcpe());
	}

	/**
	 * This function demonstrates the construction, the calibration, and the usage of Local Control Segment Spline.
	 * 	It does the following:
	 * 	- Set up the predictor/variates, the shape controller, and the basis spline (in this case polynomial)
	 *  - Create the left and the right segment edge parameters for each segment
	 *  - Construct the Regime Estimator
	 *  - Verify the Estimated Value/Jacobian
	 *  - Insert a Hermite local knot, a Cardinal knot, and a Catmull-Rom knot and examine the Estimated output/Jacobian
	 * 
	 * @throws java.lang.Exception Thrown if the test does not succeed
	 */

	private static final void TestHermiteCatmullRomCardinal()
		throws java.lang.Exception
	{
		/*
		 * X predictors
		 */

		double[] adblX = new double[] {0.00, 1.00,  2.00,  3.00,  4.00};

		/*
		 * Y responses
		 */

		double[] adblY = new double[] {1.00, 4.00, 15.00, 40.00, 85.00};

		/*
		 * DY/DX explicit local shape control for the responses
		 */

		double[] adblDYDX = new double[] {1.00, 6.00, 17.00, 34.00, 57.00};

		/*
		 * Construct a rational shape controller with the shape controller tension of 1.
		 */

		double dblShapeControllerTension = 1.;

		ResponseScalingShapeControl rssc = new ResponseScalingShapeControl (
			true,
			new QuadraticRationalShapeControl (dblShapeControllerTension));

		/*
		 * Construct the segment inelastic parameter that is C2 (iK = 2 sets it to C2), with 2nd order
		 * 	roughness penalty derivative, and without constraint
		 */

		int iK = 1;
		int iRoughnessPenaltyDerivativeOrder = 2;

		SegmentDesignInelasticControl segParams = SegmentDesignInelasticControl.Create (iK, iRoughnessPenaltyDerivativeOrder);

		/* 
		 * Construct the C1 Hermite Polynomial Spline based Regime Estimator by using the following steps:
		 * 
		 * - 1) Set up the Regime Builder Parameter
		 */

		int iNumBasis = 4;

		SegmentCustomBuilderControl sbp = new SegmentCustomBuilderControl (
			MultiSegmentSequenceBuilder.BASIS_SPLINE_POLYNOMIAL,
			new PolynomialFunctionSetParams (iNumBasis),
			segParams,
			rssc);

		/*
		 *	- 2a) Set the array of Segment Builder Parameters - one per segment
		 */

		SegmentCustomBuilderControl[] aSBP = new SegmentCustomBuilderControl[adblX.length - 1]; 

		for (int i = 0; i < adblX.length - 1; ++i)
			aSBP[i] = sbp;

		/* 
		 * - 2b) Construct the Regime
		 */

		MultiSegmentSequence regime = MultiSegmentSequenceBuilder.CreateUncalibratedRegimeEstimator ("SPLINE_REGIME", adblX, aSBP);

		SegmentPredictorResponseDerivative[] aSEPLeft = new SegmentPredictorResponseDerivative[adblY.length - 1];
		SegmentPredictorResponseDerivative[] aSEPRight = new SegmentPredictorResponseDerivative[adblY.length - 1];

		 /* 
		  * - 3) Set up the left and the local control Parameters
		  */

		for (int i = 0; i < adblY.length - 1; ++i) {
			aSEPLeft[i] = new SegmentPredictorResponseDerivative (adblY[i], new double[] {adblDYDX[i]});

			aSEPRight[i] = new SegmentPredictorResponseDerivative (adblY[i + 1], new double[] {adblDYDX[i + 1]});
		}

		/* 
		 * - 4) Calibrate the Regime and compute the Jacobian
		 */

		System.out.println ("Regime Setup Succeeded: " + regime.setupHermite (aSEPLeft, aSEPRight, null, null, MultiSegmentSequence.CALIBRATE));

		double dblX = 0.;
		double dblXMax = 4.;

		/* 
		 * - 5) Display the Estimated Y and the Regime Jacobian across the variates
		 */

		while (dblX <= dblXMax) {
			System.out.println ("Y[" + dblX + "] " + FormatUtil.FormatDouble (regime.responseValue (dblX), 1, 2, 1.) + " | " +
				regime.monotoneType (dblX));

			System.out.println ("Jacobian Y[" + dblX + "]=" + regime.jackDResponseDResponseInput (dblX).displayString());

			dblX += 0.5;
		}

		System.out.println ("SPLINE_REGIME DCPE: " + regime.dcpe());

		/* 
		 * We now insert a Hermite local control knot. The following are the steps:
		 * 
		 * - 1) Set up the left and the right segment edge parameters
		 * - 2) Insert the pair of SEP's at the chosen variate node.
		 * - 3) Compute the Estimated segment value and the motonicity across a suitable variate range.
		 */

		SegmentPredictorResponseDerivative sepLeftSegmentRightNode = new SegmentPredictorResponseDerivative (27.5, new double[] {25.5});

		SegmentPredictorResponseDerivative sepRightSegmentLeftNode = new SegmentPredictorResponseDerivative (27.5, new double[] {25.5});

		MultiSegmentSequence regimeInsert = MultiSegmentSequenceModifier.InsertKnot (regime, 2.5, sepLeftSegmentRightNode, sepRightSegmentLeftNode);

		dblX = 1.;

		while (dblX <= dblXMax) {
			System.out.println ("Inserted Y[" + dblX + "] " + FormatUtil.FormatDouble (regimeInsert.responseValue (dblX), 1, 2, 1.)
				+ " | " + regimeInsert.monotoneType (dblX));

			dblX += 0.5;
		}

		System.out.println ("SPLINE_REGIME_INSERT DCPE: " + regimeInsert.dcpe());

		/* 
		 * We now insert a Cardinal local control knot. The following are the steps:
		 * 
		 * - 1) Set up the left and the right segment edge parameters
		 * - 2) Insert the pair of SEP's at the chosen variate node.
		 * - 3) Compute the Estimated segment value and the motonicity across a suitable variate range.
		 */

		MultiSegmentSequence regimeCardinalInsert = MultiSegmentSequenceModifier.InsertCardinalKnot (regime, 2.5, 0.);

		dblX = 1.;

		while (dblX <= dblXMax) {
			System.out.println ("Cardinal Inserted Y[" + dblX + "] " + FormatUtil.FormatDouble
				(regimeCardinalInsert.responseValue (dblX), 1, 2, 1.) + " | " + regimeInsert.monotoneType (dblX));

			dblX += 0.5;
		}

		System.out.println ("SPLINE_REGIME_CARDINAL_INSERT DCPE: " + regimeCardinalInsert.dcpe());

		/* 
		 * We now insert a Catmull-Rom local control knot. The following are the steps:
		 * 
		 * - 1) Set up the left and the right segment edge parameters
		 * - 2) Insert the pair of SEP's at the chosen variate node.
		 * - 3) Compute the Estimated segment value and the motonicity across a suitable variate range.
		 */

		MultiSegmentSequence regimeCatmullRomInsert = MultiSegmentSequenceModifier.InsertCatmullRomKnot (regime, 2.5);

		dblX = 1.;

		while (dblX <= dblXMax) {
			System.out.println ("Catmull-Rom Inserted Y[" + dblX + "] " + FormatUtil.FormatDouble
				(regimeCatmullRomInsert.responseValue (dblX), 1, 2, 1.) + " | " + regimeInsert.monotoneType (dblX));

			dblX += 0.5;
		}

		System.out.println ("SPLINE_REGIME_CATMULL_ROM_INSERT DCPE: " + regimeCatmullRomInsert.dcpe());
	}

	/**
	 * This function demonstrates the construction, the calibration, and the usage of Lagrange Polynomial Regime.
	 * 	It does the following:
	 * 	- Set up the predictors and the Lagrange Polynomial Regime.
	 *  - Calibrate to a target Y array.
	 *  - Calibrate the value to a target X.
	 *  - Calibrate the value Jacobian to a target X.
	 *  - Verify the local monotonicity and convexity (both the co- and the local versions).
	 * 
	 * @throws java.lang.Exception Thrown if the test does not succeed
	 */

	private static final void TestLagrangePolynomialRegime()
		throws java.lang.Exception
	{
		SingleSegmentSequence lps = new SingleSegmentLagrangePolynomial (new double[] {-2., -1., 2., 5.});

		System.out.println ("Setup: " + lps.setup (
			0.25,
			new double[] {0.25, 0.25, 12.25, 42.25},
			null, // Fitness Weighted Response
			MultiSegmentSequence.BOUNDARY_CONDITION_NATURAL, // Boundary Condition - Natural
			MultiSegmentSequence.CALIBRATE)); // Calibrate the Regime predictors to the responses

		System.out.println ("Value = " + lps.responseValue (2.16));

		System.out.println ("Value Jacobian = " + lps.jackDResponseDResponseInput (2.16).displayString());

		System.out.println ("Value Monotone Type: " + lps.monotoneType (2.16));

		System.out.println ("Is Locally Monotone: " + lps.isLocallyMonotone());
	}

	public static final MultiSegmentSequence ConstructSpecifiedC1Regime (
		final double[] adblX,
		final double[] adblY,
		final java.lang.String strGeneratorType,
		final SegmentCustomBuilderControl scbc,
		final boolean bEliminateSpuriousExtrema,
		final boolean bApplyMonotoneFilter)
	{
		LocalMonotoneCkGenerator lmcg = LocalMonotoneCkGenerator.Create (
			adblX,
			adblY,
			strGeneratorType,
			bEliminateSpuriousExtrema,
			bApplyMonotoneFilter);

		/*
		 * Array of Segment Builder Parameters - one per segment
		 */

		SegmentCustomBuilderControl[] aSCBC = new SegmentCustomBuilderControl[adblX.length - 1]; 

		for (int i = 0; i < adblX.length - 1; ++i)
			aSCBC[i] = scbc;

		/*
		 * Construct the Local Control Regime instance 
		 */

		return LocalControlRegimeBuilder.CustomSlopeHermiteSpline (
			strGeneratorType + "_LOCAL_REGIME",
			adblX,
			adblY,
			lmcg.C1(),
			aSCBC,
			null,
			MultiSegmentSequence.CALIBRATE);
	}

	/**
	 * Perform the following sequence of tests for a given segment control for a predictor/response range
	 * 	- Estimate
	 *  - Compute the segment-by-segment monotonicity
	 *  - Regime Jacobian
	 *  - Regime knot insertion
	 * 
	 * @param adblX The Predictor Array
	 * @param adblY The Response Array
	 * @param sbp The Segment Builder Parameters
	 * @param bEliminateSpuriousExtrema TRUE => Eliminate Spurious Extrema
	 * @param bApplyMonotoneFilter TRUE => Apply Monotone Filter
	 * 
	 * 	WARNING: Insufficient Error Checking, so use caution
	 */

	public static final void C1GeneratedRegimeTest (
		final double[] adblX,
		final double[] adblY,
		final MultiSegmentSequence regime)
		throws Exception
	{
		double dblX = 1.;
		double dblXMax = 10.;

		/*
		 * Estimate, compute the segment-by-segment monotonicity and the Regime Jacobian
		 */

		while (dblX <= dblXMax) {
			System.out.println (
				"Y[" + dblX + "] => " + FormatUtil.FormatDouble (regime.responseValue (dblX), 1, 2, 1.) + " | " +
				regime.monotoneType (dblX));

			System.out.println ("Jacobian Y[" + dblX + "]=" + regime.jackDResponseDResponseInput (dblX).displayString());

			dblX += 1.;
		}

		System.out.println ("\tSPLINE_REGIME DCPE: " + regime.dcpe());

		/*
		 * Construct a new Regime instance by inserting a pair of of predictor/response knots
		 */

		MultiSegmentSequence regimeInsert = MultiSegmentSequenceModifier.InsertKnot (regime,
			9.,
			10.,
			MultiSegmentSequence.BOUNDARY_CONDITION_NATURAL, // Boundary Condition - Natural
			MultiSegmentSequence.CALIBRATE); // Calibrate the Regime predictors to the responses

		dblX = 1.;

		/*
		 * Estimate, compute the sgement-by-segment monotonicty and the Regime Jacobian
		 */

		while (dblX <= dblXMax) {
			System.out.println ("Inserted Y[" + dblX + "] " + FormatUtil.FormatDouble (regimeInsert.responseValue (dblX), 1, 2, 1.)
				+ " | " + regimeInsert.monotoneType (dblX));

			dblX += 1.;
		}

		System.out.println ("\tSPLINE_REGIME_INSERT DCPE: " + regimeInsert.dcpe());
	}

	public static final void main (
		final String[] astrArgs)
		throws Exception
	{
		/*
		 * X predictors
		 */

		double[] adblX = new double[] { 1.00,  1.50,  2.00, 3.00, 4.00, 5.00, 6.50, 8.00, 10.00};

		/*
		 * Y responses
		 */

		double[] adblY = new double[] {25.00, 20.25, 16.00, 9.00, 4.00, 1.00, 0.25, 4.00, 16.00};

		/*
		 * Construct a rational shape controller with the shape controller tension of 1.
		 */

		double dblShapeControllerTension = 0.;

		ResponseScalingShapeControl rssc = new ResponseScalingShapeControl (true, new QuadraticRationalShapeControl (dblShapeControllerTension));

		/*
		 * Construct the segment inelastic parameter that is C2 (iK = 2 sets it to C2), with 2nd order
		 * 	roughness penalty derivative, and without constraint
		 */

		int iK = 2;
		int iRoughnessPenaltyDerivativeOrder= 2;

		SegmentDesignInelasticControl segParams = SegmentDesignInelasticControl.Create (iK, iRoughnessPenaltyDerivativeOrder);

		System.out.println (" \n---------- \n BERNSTEIN POLYNOMIAL \n ---------- \n");

		int iBernPolyNumBasis = 4;

		BasisSplineRegimeTest (adblX, adblY, BernsteinPolynomialSegmentControlParams (iBernPolyNumBasis, segParams, rssc));

		System.out.println (" \n---------- \n POLYNOMIAL \n ---------- \n");

		int iPolyNumBasis = 4;

		BasisSplineRegimeTest (adblX, adblY, PolynomialSegmentControlParams (iPolyNumBasis, segParams, rssc));

		System.out.println (" \n---------- \n EXPONENTIAL TENSION \n ---------- \n");

		double dblTension = 1.;

		BasisSplineRegimeTest (adblX, adblY, ExponentialTensionSegmentControlParams (dblTension, segParams, rssc));

		System.out.println (" \n---------- \n HYPERBOLIC TENSION \n ---------- \n");

		BasisSplineRegimeTest (adblX, adblY, HyperbolicTensionSegmentControlParams (dblTension, segParams, rssc));

		System.out.println (" \n---------- \n KAKLIS PANDELIS \n ---------- \n");

		int iKPTensionDegree = 2;

		BasisSplineRegimeTest (adblX, adblY, KaklisPandelisSegmentControlParams (iKPTensionDegree, segParams, rssc));

		System.out.println (" \n---------- \n HERMITE - CATMULL ROM - CARDINAL \n ---------- \n");

		TestHermiteCatmullRomCardinal();

		System.out.println (" \n---------- \n LAGRANGE POLYNOMIAL REGIME \n ---------- \n");

		TestLagrangePolynomialRegime();

		System.out.println (" \n---------- \n C1 AKIMA REGIME \n ---------- \n");

		C1GeneratedRegimeTest (
			adblX,
			adblY,
			ConstructSpecifiedC1Regime (
				adblX,
				adblY,
				LocalMonotoneCkGenerator.C1_AKIMA,
				PolynomialSegmentControlParams (iPolyNumBasis, segParams, rssc),
				true,
				true)
			);

		System.out.println (" \n---------- \n C1 BESSEL/HERMITE \n ---------- \n");

		C1GeneratedRegimeTest (
			adblX,
			adblY,
			ConstructSpecifiedC1Regime (
				adblX,
				adblY,
				LocalMonotoneCkGenerator.C1_BESSEL,
				PolynomialSegmentControlParams (iPolyNumBasis, segParams, rssc),
				true,
				true)
			);

		System.out.println (" \n---------- \n C1 HARMONIC MONOTONE WITH FILTER \n ---------- \n");

		C1GeneratedRegimeTest (
			adblX,
			adblY,
			ConstructSpecifiedC1Regime (
				adblX,
				adblY,
				LocalMonotoneCkGenerator.C1_HARMONIC,
				PolynomialSegmentControlParams (iPolyNumBasis, segParams, rssc),
				true,
				true)
			);

		System.out.println (" \n---------- \n C1 HARMONIC MONOTONE WITHOUT FILTER \n ---------- \n");

		C1GeneratedRegimeTest (
			adblX,
			adblY,
			ConstructSpecifiedC1Regime (
				adblX,
				adblY,
				LocalMonotoneCkGenerator.C1_HARMONIC,
				PolynomialSegmentControlParams (iPolyNumBasis, segParams, rssc),
				true,
				false)
			);

		System.out.println (" \n---------- \n C1 HUYNH LE-FLOCH LIMITER REGIME WITHOUT FILTER \n ---------- \n");

		C1GeneratedRegimeTest (
			adblX,
			adblY,
			ConstructSpecifiedC1Regime (
				adblX,
				adblY,
				LocalMonotoneCkGenerator.C1_HUYNH_LE_FLOCH,
				PolynomialSegmentControlParams (iPolyNumBasis, segParams, rssc),
				true,
				true)
			);

		System.out.println (" \n---------- \n C1 HYMAN 1983 MONOTONE \n ---------- \n");

		C1GeneratedRegimeTest (
			adblX,
			adblY,
			ConstructSpecifiedC1Regime (
				adblX,
				adblY,
				LocalMonotoneCkGenerator.C1_HYMAN83,
				PolynomialSegmentControlParams (iPolyNumBasis, segParams, rssc),
				true,
				true)
			);

		System.out.println (" \n---------- \n C1 HYMAN 1989 MONOTONE \n ---------- \n");

		C1GeneratedRegimeTest (
			adblX,
			adblY,
			ConstructSpecifiedC1Regime (
				adblX,
				adblY,
				LocalMonotoneCkGenerator.C1_HYMAN89,
				PolynomialSegmentControlParams (iPolyNumBasis, segParams, rssc),
				true,
				true)
			);

		System.out.println (" \n---------- \n C1 KRUGER REGIME \n ---------- \n");

		C1GeneratedRegimeTest (
			adblX,
			adblY,
			ConstructSpecifiedC1Regime (
				adblX,
				adblY,
				LocalMonotoneCkGenerator.C1_KRUGER,
				PolynomialSegmentControlParams (iPolyNumBasis, segParams, rssc),
				true,
				true)
			);

		System.out.println (" \n---------- \n C1 VAN LEER LIMITER REGIME WITHOUT FILTER \n ---------- \n");

		C1GeneratedRegimeTest (
			adblX,
			adblY,
			ConstructSpecifiedC1Regime (
				adblX,
				adblY,
				LocalMonotoneCkGenerator.C1_VAN_LEER,
				PolynomialSegmentControlParams (iPolyNumBasis, segParams, rssc),
				true,
				false)
			);
	}
}
