	
package org.drip.sample.regime;

import org.drip.quant.common.FormatUtil;
import org.drip.quant.function1D.*;
import org.drip.spline.basis.*;
import org.drip.spline.params.*;
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

public class TensionRegimeEstimation {

	/**
	 * Build KLK Exponential Tension Segment Control Parameters
	 * 
	 * @param dblTension Segment Tension
	 * @param segParams Inelastic Segment Parameters
	 * @param rssc Shape Controller
	 * 
	 * 	WARNING: Insufficient Error Checking, so use caution
	 * 
	 * @return KLK Exponential Tension Segment Control Parameters
	 */

	public static final SegmentCustomBuilderControl KLKExponentialTensionSegmentControlParams (
		final double dblTension,
		final SegmentDesignInelasticControl segParams,
		final ResponseScalingShapeControl rssc)
		throws Exception
	{
		return new SegmentCustomBuilderControl (
			MultiSegmentSequenceBuilder.BASIS_SPLINE_KLK_EXPONENTIAL_TENSION,
			new ExponentialTensionSetParams (dblTension),
			segParams,
			rssc);
	}

	/**
	 * Build KLK Hyperbolic Tension Segment Control Parameters
	 * 
	 * @param dblTension Segment Tension
	 * @param segParams Inelastic Segment Parameters
	 * @param rssc Shape Controller
	 * 
	 * 	WARNING: Insufficient Error Checking, so use caution
	 * 
	 * @return KLK Hyperbolic Tension Segment Control Parameters
	 */

	public static final SegmentCustomBuilderControl KLKHyperbolicTensionSegmentControlParams (
		final double dblTension,
		final SegmentDesignInelasticControl segParams,
		final ResponseScalingShapeControl rssc)
		throws Exception
	{
		return new SegmentCustomBuilderControl (
			MultiSegmentSequenceBuilder.BASIS_SPLINE_KLK_HYPERBOLIC_TENSION,
			new ExponentialTensionSetParams (dblTension),
			segParams,
			rssc);
	}

	/**
	 * Build KLK Rational Linear Tension Segment Control Parameters
	 * 
	 * @param dblTension Segment Tension
	 * @param segParams Inelastic Segment Parameters
	 * @param rssc Shape Controller
	 * 
	 * 	WARNING: Insufficient Error Checking, so use caution
	 * 
	 * @return KLK Rational Linear Tension Segment Control Parameters
	 */

	public static final SegmentCustomBuilderControl KLKRationalLinearTensionSegmentControlParams (
		final double dblTension,
		final SegmentDesignInelasticControl segParams,
		final ResponseScalingShapeControl rssc)
		throws Exception
	{
		return new SegmentCustomBuilderControl (
			MultiSegmentSequenceBuilder.BASIS_SPLINE_KLK_RATIONAL_LINEAR_TENSION,
			new ExponentialTensionSetParams (dblTension),
			segParams,
			rssc);
	}

	/**
	 * Build KLK Rational Quadratic Tension Segment Control Parameters
	 * 
	 * @param dblTension Segment Tension
	 * @param segParams Inelastic Segment Parameters
	 * @param rssc Shape Controller
	 * 
	 * 	WARNING: Insufficient Error Checking, so use caution
	 * 
	 * @return KLK Rational Quadratic Tension Segment Control Parameters
	 */

	public static final SegmentCustomBuilderControl KLKRationalQuadraticTensionSegmentControlParams (
		final double dblTension,
		final SegmentDesignInelasticControl segParams,
		final ResponseScalingShapeControl rssc)
		throws Exception
	{
		return new SegmentCustomBuilderControl (
			MultiSegmentSequenceBuilder.BASIS_SPLINE_KLK_RATIONAL_QUADRATIC_TENSION,
			new ExponentialTensionSetParams (dblTension),
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

		System.out.println ("\t\tregime = " + regime);

		/*
		 * Estimate, compute the segment-by-segment monotonicity and the Regime Jacobian
		 */

		while (dblX <= dblXMax) {
			System.out.println ("Y[" + dblX + "] " + FormatUtil.FormatDouble (regime.responseValue (dblX), 1, 2, 1.) + " | " +
				regime.monotoneType (dblX));

			System.out.println ("\tJacobian Y[" + dblX + "]=" + regime.jackDResponseDResponseInput (dblX).displayString());

			dblX += 1.;
		}

		System.out.println ("\t\tSPLINE_REGIME DCPE: " + regime.dcpe());

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

		System.out.println ("\t\tSPLINE_REGIME_INSERT DCPE: " + regimeInsert.dcpe());
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

		ResponseScalingShapeControl rssc = new ResponseScalingShapeControl (
			false,
			new LinearRationalShapeControl (dblShapeControllerTension));

		/*
		 * Construct the Segment Inelastic Parameter that is C2 (iK = 2 sets it to C2), with Second Order
		 * 	Curvature Penalty Derivative, and without constraint
		 */

		int iK = 2;
		int iCurvaturePenaltyDerivativeOrder= 2;

		SegmentDesignInelasticControl segParams = SegmentDesignInelasticControl.Create (
			iK,
			iCurvaturePenaltyDerivativeOrder);

		double dblKLKTension = 1.;

		System.out.println (" \n---------- \n KLK HYPERBOLIC TENSION \n ---------- \n");

		BasisSplineRegimeTest (adblX, adblY, KLKHyperbolicTensionSegmentControlParams (dblKLKTension, segParams, rssc));

		System.out.println (" \n---------- \n KLK EXPONENTIAL TENSION \n ---------- \n");

		BasisSplineRegimeTest (adblX, adblY, KLKExponentialTensionSegmentControlParams (dblKLKTension, segParams, rssc));

		System.out.println (" \n---------- \n KLK RATIONAL LINEAR TENSION \n ---------- \n");

		BasisSplineRegimeTest (adblX, adblY, KLKRationalLinearTensionSegmentControlParams (dblKLKTension, segParams, rssc));

		System.out.println (" \n---------- \n KLK RATIONAL QUADRATIC TENSION \n ---------- \n");

		BasisSplineRegimeTest (adblX, adblY, KLKRationalQuadraticTensionSegmentControlParams (dblKLKTension, segParams, rssc));
	}
}
