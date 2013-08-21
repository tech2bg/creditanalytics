	
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

	public static final SegmentControlParams PolynomialSegmentControlParams (
		final int iNumBasis,
		final SegmentInelasticParams segParams,
		final AbstractUnivariate rsc)
		throws Exception
	{
		return new SegmentControlParams (Span.BASIS_SPLINE_POLYNOMIAL, new PolynomialBasisSetParams (iNumBasis), segParams, rsc);
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

	public static final SegmentControlParams BernsteinPolynomialSegmentControlParams (
		final int iNumBasis,
		final SegmentInelasticParams segParams,
		final AbstractUnivariate rsc)
		throws Exception
	{
		return new SegmentControlParams (Span.BASIS_SPLINE_BERNSTEIN_POLYNOMIAL, new PolynomialBasisSetParams (iNumBasis), segParams, rsc);
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

	public static final SegmentControlParams ExponentialTensionSegmentControlParams (
		final double dblTension,
		final SegmentInelasticParams segParams,
		final AbstractUnivariate rsc)
		throws Exception
	{
		return new SegmentControlParams (Span.BASIS_SPLINE_EXPONENTIAL_TENSION, new ExponentialTensionBasisSetParams (dblTension), segParams, rsc);
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

	public static final SegmentControlParams HyperbolicTensionSegmentControlParams (
		final double dblTension,
		final SegmentInelasticParams segParams,
		final AbstractUnivariate rsc)
		throws Exception
	{
		return new SegmentControlParams (Span.BASIS_SPLINE_HYPERBOLIC_TENSION, new ExponentialTensionBasisSetParams (dblTension), segParams, rsc);
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

	public static final SegmentControlParams KaklisPandelisSegmentControlParams (
		final int iKPTensionDegree,
		final SegmentInelasticParams segParams,
		final AbstractUnivariate rsc)
		throws Exception
	{
		return new SegmentControlParams (Span.BASIS_SPLINE_KAKLIS_PANDELIS, new KaklisPandelisBasisSetParams (iKPTensionDegree), segParams, rsc);
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
	 * @param segControlParams The Segment Control Parameters
	 * 
	 * 	WARNING: Insufficient Error Checking, so use caution
	 */

	public static final void BasisSplineSpanTest (
		final double[] adblX,
		final double[] adblY,
		final SegmentControlParams segControlParams)
		throws Exception
	{
		double dblX = 1.;
		double dblXMax = 10.;

		/*
		 * Construct a Span instance 
		 */

		Span span = Span.CreateCalibratedSpanInterpolator (
			adblX, // predictors
			adblY, // responses
			Span.SPLINE_BOUNDARY_MODE_NATURAL, // Boundary Condition - Natural
			segControlParams, // Basis Spline construction control parameters
			Span.SET_ITEP | Span.CALIBRATE_SPAN); // Set up + calibrate the Span predictors to the responses

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

		Span spanInsert = span.insertKnot (9., 10.);

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
	}
}
