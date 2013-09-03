	
package org.drip.math.sample;

import org.drip.math.common.FormatUtil;
import org.drip.math.function.*;
import org.drip.math.grid.*;
import org.drip.math.spline.*;

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
 * SpanInterpolator demonstrates the Span builder and usage API. It shows the following:
 * 	- Construction of segment control parameters - polynomial (regular/Bernstein) segment control,
 * 		exponential/hyperbolic tension segment control, Kaklis-Pandelis tension segment control.
 * 	- Control the segment using the rational shape controller, and the appropriate Ck
 * 	- Construct a calibrated span interpolator
 * 	- Insert a knot into the Span
 * 	- Interpolate the node value and the node value Jacobian
 * 	- Calculate the segment/span monotonicity
 *
 * @author Lakshmi Krishnamurthy
 */

public class SpanInterpolator {

	/**
	 * Build Polynomial Segment Control Parameters
	 * 
	 * @param iNumBasis Number of Polynomial Basis Functions
	 * @param segParams Inelastic Segment Parameters
	 * @param rsc Shape Controller
	 * 
	 * 	WARNING: Insufficient Error Checking, so use caution
	 * 
	 * @return Polynomial Segment Control Parameters
	 */

	public static final SegmentBuilderParams PolynomialSegmentControlParams (
		final int iNumBasis,
		final SegmentInelasticParams segParams,
		final AbstractUnivariate rsc)
		throws Exception
	{
		return new SegmentBuilderParams (SpanBuilder.BASIS_SPLINE_POLYNOMIAL, new PolynomialBasisSetParams (iNumBasis), segParams, rsc);
	}

	/**
	 * Build Bernstein Polynomial Segment Control Parameters
	 * 
	 * @param iNumBasis Number of Polynomial Basis Functions
	 * @param segParams Inelastic Segment Parameters
	 * @param rsc Shape Controller
	 * 
	 * 	WARNING: Insufficient Error Checking, so use caution
	 * 
	 * @return Bernstein Polynomial Segment Control Parameters
	 */

	public static final SegmentBuilderParams BernsteinPolynomialSegmentControlParams (
		final int iNumBasis,
		final SegmentInelasticParams segParams,
		final AbstractUnivariate rsc)
		throws Exception
	{
		return new SegmentBuilderParams (SpanBuilder.BASIS_SPLINE_BERNSTEIN_POLYNOMIAL, new PolynomialBasisSetParams (iNumBasis), segParams, rsc);
	}

	/**
	 * Build Exponential Tension Segment Control Parameters
	 * 
	 * @param dblTension Segment Tension
	 * @param segParams Inelastic Segment Parameters
	 * @param rsc Shape Controller
	 * 
	 * 	WARNING: Insufficient Error Checking, so use caution
	 * 
	 * @return Exponential Tension Segment Control Parameters
	 */

	public static final SegmentBuilderParams ExponentialTensionSegmentControlParams (
		final double dblTension,
		final SegmentInelasticParams segParams,
		final AbstractUnivariate rsc)
		throws Exception
	{
		return new SegmentBuilderParams (SpanBuilder.BASIS_SPLINE_EXPONENTIAL_TENSION, new ExponentialTensionBasisSetParams (dblTension), segParams, rsc);
	}

	/**
	 * Build Hyperbolic Tension Segment Control Parameters
	 * 
	 * @param dblTension Segment Tension
	 * @param segParams Inelastic Segment Parameters
	 * @param rsc Shape Controller
	 * 
	 * 	WARNING: Insufficient Error Checking, so use caution
	 * 
	 * @return Hyperbolic Tension Segment Control Parameters
	 */

	public static final SegmentBuilderParams HyperbolicTensionSegmentControlParams (
		final double dblTension,
		final SegmentInelasticParams segParams,
		final AbstractUnivariate rsc)
		throws Exception
	{
		return new SegmentBuilderParams (SpanBuilder.BASIS_SPLINE_HYPERBOLIC_TENSION, new ExponentialTensionBasisSetParams (dblTension), segParams, rsc);
	}

	/**
	 * Build Kaklis-Pandelis Segment Control Parameters
	 * 
	 * @param iKPTensionDegree KP Polynomial Tension Degree
	 * @param segParams Inelastic Segment Parameters
	 * @param rsc Shape Controller
	 * 
	 * 	WARNING: Insufficient Error Checking, so use caution
	 * 
	 * @return Kaklis-Pandelis Segment Control Parameters
	 */

	public static final SegmentBuilderParams KaklisPandelisSegmentControlParams (
		final int iKPTensionDegree,
		final SegmentInelasticParams segParams,
		final AbstractUnivariate rsc)
		throws Exception
	{
		return new SegmentBuilderParams (SpanBuilder.BASIS_SPLINE_KAKLIS_PANDELIS, new KaklisPandelisBasisSetParams (iKPTensionDegree), segParams, rsc);
	}

	/**
	 * Perform the following sequence of tests for a given segment control for a predictor/response range
	 * 	- Interpolate
	 *  - Compute the segment-by-segment monotonicity
	 *  - Span Jacobian
	 *  - Span knot insertion
	 * 
	 * @param adblX The Predictor Array
	 * @param adblY The Response Array
	 * @param sbp The Segment Builder Parameters
	 * 
	 * 	WARNING: Insufficient Error Checking, so use caution
	 */

	public static final void BasisSplineSpanTest (
		final double[] adblX,
		final double[] adblY,
		final SegmentBuilderParams sbp)
		throws Exception
	{
		double dblX = 1.;
		double dblXMax = 10.;

		/*
		 * Array of Segment Builder Parameters - one per segment
		 */

		SegmentBuilderParams[] aSBP = new SegmentBuilderParams[adblX.length - 1]; 

		for (int i = 0; i < adblX.length - 1; ++i)
			aSBP[i] = sbp;

		/*
		 * Construct a Span instance 
		 */

		MultiSegmentSpan span = SpanBuilder.CreateCalibratedSpanInterpolator (
			adblX, // predictors
			adblY, // responses
			MultiSegmentSpan.SPLINE_BOUNDARY_MODE_NATURAL, // Boundary Condition - Natural
			aSBP, // Basis Segment Builder parameters
			SingleSegmentSpan.CALIBRATE_SPAN); // Calibrate the Span predictors to the responses

		/*
		 * Interpolate, compute the segment-by-segment monotonicity and the Span Jacobian
		 */

		while (dblX <= dblXMax) {
			System.out.println ("Y[" + dblX + "] " + FormatUtil.FormatDouble (span.calcValue (dblX), 1, 2, 1.) + " | " + span.monotoneType (dblX));

			System.out.println ("Jacobian Y[" + dblX + "]=" + span.calcValueJacobian (dblX).displayString());

			dblX += 1.;
		}

		/*
		 * Construct a new Span instance by inserting a pair of of predictor/response knots
		 */

		MultiSegmentSpan spanInsert = SpanBuilder.InsertKnot (span, 9., 10.);

		dblX = 1.;

		/*
		 * Interpolate, compute the sgement-by-segment monotonicty and the Span Jacobian
		 */

		while (dblX <= dblXMax) {
			System.out.println ("Inserted Y[" + dblX + "] " + FormatUtil.FormatDouble (spanInsert.calcValue (dblX), 1, 2, 1.)
				+ " | " + spanInsert.monotoneType (dblX));

			dblX += 1.;
		}
	}

	/**
	 * This function demonstrates the construction, the calibration, and the usage of Local Control Segment Spline.
	 * 	It does the following:
	 * 	- Set up the predictor/variates, the shape controller, and the basis spline (in this case polynomial)
	 *  - Create the left and the right segment edge parameters for each segment
	 *  - Construct the span interpolator
	 *  - Verify the Interpolated Value/Jacobian
	 *  - Insert a Hermite local knot, a Cardinal knot, and a Catmull-Rom knot and examine the interpolated output/Jacobian
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

		AbstractUnivariate rsc = new RationalShapeControl (dblShapeControllerTension);

		/*
		 * Construct the segment inelastic parameter that is C2 (iK = 2 sets it to C2), with 2nd order
		 * 	roughness penalty derivative, and without constraint
		 */

		int iK = 1;
		int iRoughnessPenaltyDerivativeOrder = 2;

		SegmentInelasticParams segParams = new SegmentInelasticParams (iK, iRoughnessPenaltyDerivativeOrder, null);

		/* 
		 * Construct the C1 Hermite Polynomial Spline based Span Interpolator by using the following steps:
		 * 
		 * - 1) Set up the Span Builder Parameter
		 */

		int iNumBasis = 4;

		SegmentBuilderParams sbp = new SegmentBuilderParams (
			SpanBuilder.BASIS_SPLINE_POLYNOMIAL,
			new PolynomialBasisSetParams (iNumBasis),
			segParams,
			rsc);

		/*
		 *	- 2a) Set the array of Segment Builder Parameters - one per segment
		 */

		SegmentBuilderParams[] aSBP = new SegmentBuilderParams[adblX.length - 1]; 

		for (int i = 0; i < adblX.length - 1; ++i)
			aSBP[i] = sbp;

		/* 
		 * - 2b) Construct the Span
		 */

		MultiSegmentSpan span = SpanBuilder.CreateUncalibratedSpanInterpolator (adblX, aSBP);

		SegmentEdgeParams[] aSEPLeft = new SegmentEdgeParams[adblY.length - 1];
		SegmentEdgeParams[] aSEPRight = new SegmentEdgeParams[adblY.length - 1];

		 /* 
		  * - 3) Set up the left and the local control Parameters
		  */

		for (int i = 0; i < adblY.length - 1; ++i) {
			aSEPLeft[i] = new SegmentEdgeParams (adblY[i], new double[] {adblDYDX[i]});

			aSEPRight[i] = new SegmentEdgeParams (adblY[i + 1], new double[] {adblDYDX[i + 1]});
		}

		/* 
		 * - 4) Calibrate the Span and compute the Jacobian
		 */

		System.out.println ("Span Setup Succeeded: " + span.setup (aSEPLeft, aSEPRight, null,
			SingleSegmentSpan.CALIBRATE_SPAN | SingleSegmentSpan.CALIBRATE_JACOBIAN));

		double dblX = 0.;
		double dblXMax = 4.;

		/* 
		 * - 5) Display the interpolated Y and the Span Jacobian across the variates
		 */

		while (dblX <= dblXMax) {
			System.out.println ("Y[" + dblX + "] " + FormatUtil.FormatDouble (span.calcValue (dblX), 1, 2, 1.) + " | " +
				span.monotoneType (dblX));

			System.out.println ("Jacobian Y[" + dblX + "]=" + span.calcValueJacobian (dblX).displayString());

			dblX += 0.5;
		}

		/* 
		 * We now insert a Hermite local control knot. The following are the steps:
		 * 
		 * - 1) Set up the left and the right segment edge parameters
		 * - 2) Insert the pair of SEP's at the chosen variate node.
		 * - 3) Compute the interpolated segment value and the motonicity across a suitable variate range.
		 */

		SegmentEdgeParams sepLeftSegmentRightNode = new SegmentEdgeParams (27.5, new double[] {25.5});

		SegmentEdgeParams sepRightSegmentLeftNode = new SegmentEdgeParams (27.5, new double[] {25.5});

		MultiSegmentSpan spanInsert = SpanBuilder.InsertKnot (span, 2.5, sepLeftSegmentRightNode, sepRightSegmentLeftNode);

		dblX = 1.;

		while (dblX <= dblXMax) {
			System.out.println ("Inserted Y[" + dblX + "] " + FormatUtil.FormatDouble (spanInsert.calcValue (dblX), 1, 2, 1.)
				+ " | " + spanInsert.monotoneType (dblX));

			dblX += 0.5;
		}

		/* 
		 * We now insert a Cardinal local control knot. The following are the steps:
		 * 
		 * - 1) Set up the left and the right segment edge parameters
		 * - 2) Insert the pair of SEP's at the chosen variate node.
		 * - 3) Compute the interpolated segment value and the motonicity across a suitable variate range.
		 */

		MultiSegmentSpan spanCardinalInsert = SpanBuilder.InsertCardinalKnot (span, 2.5, 0.);

		dblX = 1.;

		while (dblX <= dblXMax) {
			System.out.println ("Cardinal Inserted Y[" + dblX + "] " + FormatUtil.FormatDouble
				(spanCardinalInsert.calcValue (dblX), 1, 2, 1.) + " | " + spanInsert.monotoneType (dblX));

			dblX += 0.5;
		}

		/* 
		 * We now insert a Catnull-Rom local control knot. The following are the steps:
		 * 
		 * - 1) Set up the left and the right segment edge parameters
		 * - 2) Insert the pair of SEP's at the chosen variate node.
		 * - 3) Compute the interpolated segment value and the motonicity across a suitable variate range.
		 */

		MultiSegmentSpan spanCatmullRomInsert = SpanBuilder.InsertCatmullRomKnot (span, 2.5);

		dblX = 1.;

		while (dblX <= dblXMax) {
			System.out.println ("Catmull-Rom Inserted Y[" + dblX + "] " + FormatUtil.FormatDouble
				(spanCatmullRomInsert.calcValue (dblX), 1, 2, 1.) + " | " + spanInsert.monotoneType (dblX));

			dblX += 0.5;
		}
	}

	/**
	 * This function demonstrates the construction, the calibration, and the usage of Lagrange Polynomial Span.
	 * 	It does the following:
	 * 	- Set up the predictors and the Lagrange Polynomial Span.
	 *  - Calibrate to a target Y array.
	 *  - Calibrate the value to a target X.
	 *  - Calibrate the value Jacobian to a target X.
	 *  - Verify the local monotonicity and convexity (both the co- and the local versions).
	 * 
	 * @throws java.lang.Exception Thrown if the test does not succeed
	 */

	private static final void TestLagrangePolynomialSpan()
		throws java.lang.Exception
	{
		SingleSegmentSpan lps = new LagrangePolynomialSpan (new double[] {-2., -1., 2., 5.});

		System.out.println ("Setup: " + lps.setup (0.25, new double[] {0.25, 0.25, 12.25, 42.25}, "", SingleSegmentSpan.CALIBRATE_SPAN));

		System.out.println ("Value = " + lps.calcValue (2.16));

		System.out.println ("Value Jacobian = " + lps.calcValueJacobian (2.16).displayString());

		System.out.println ("Value Monotone Type: " + lps.monotoneType (2.16));

		System.out.println ("Is Locally Monotone: " + lps.isLocallyMonotone());
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

		double dblShapeControllerTension = 1.;

		AbstractUnivariate rsc = new RationalShapeControl (dblShapeControllerTension);

		/*
		 * Construct the segment inelastic parameter that is C2 (iK = 2 sets it to C2), with 2nd order
		 * 	roughness penalty derivative, and without constraint
		 */

		int iK = 2;
		int iRoughnessPenaltyDerivativeOrder= 2;

		SegmentInelasticParams segParams = new SegmentInelasticParams (iK, iRoughnessPenaltyDerivativeOrder, null);

		System.out.println (" \n---------- \n BERNSTEIN POLYNOMIAL \n ---------- \n");

		int iBernPolyNumBasis = 4;

		BasisSplineSpanTest (adblX, adblY, BernsteinPolynomialSegmentControlParams (iBernPolyNumBasis, segParams, rsc));

		System.out.println (" \n---------- \n POLYNOMIAL \n ---------- \n");

		int iPolyNumBasis = 4;

		BasisSplineSpanTest (adblX, adblY, PolynomialSegmentControlParams (iPolyNumBasis, segParams, rsc));

		System.out.println (" \n---------- \n EXPONENTIAL TENSION \n ---------- \n");

		double dblTension = 1.;

		BasisSplineSpanTest (adblX, adblY, ExponentialTensionSegmentControlParams (dblTension, segParams, rsc));

		System.out.println (" \n---------- \n HYPERBOLIC TENSION \n ---------- \n");

		BasisSplineSpanTest (adblX, adblY, HyperbolicTensionSegmentControlParams (dblTension, segParams, rsc));

		System.out.println (" \n---------- \n KAKLIS PANDELIS \n ---------- \n");

		int iKPTensionDegree = 2;

		BasisSplineSpanTest (adblX, adblY, KaklisPandelisSegmentControlParams (iKPTensionDegree, segParams, rsc));

		System.out.println (" \n---------- \n HERMITE - CATMULL ROM - CARDINAL \n ---------- \n");

		TestHermiteCatmullRomCardinal();

		System.out.println (" \n---------- \n LAGRANGE POLYNOMIAL SPAN \n ---------- \n");

		TestLagrangePolynomialSpan();
	}
}
