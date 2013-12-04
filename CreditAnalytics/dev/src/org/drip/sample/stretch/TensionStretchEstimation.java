	
package org.drip.sample.stretch;

import org.drip.quant.common.FormatUtil;
import org.drip.quant.function1D.*;
import org.drip.spline.basis.*;
import org.drip.spline.params.*;
import org.drip.spline.stretch.*;

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
 * StretchEstimation demonstrates the Stretch builder and usage API. It shows the following:
 * 	- Construction of segment control parameters - polynomial (regular/Bernstein) segment control,
 * 		exponential/hyperbolic tension segment control, Kaklis-Pandelis tension segment control.
 * 	- Control the segment using the rational shape controller, and the appropriate Ck
 * 	- Construct a calibrated Stretch Estimator.
 * 	- Insert a knot into the Stretch
 * 	- Estimate the node value and the node value Jacobian
 * 	- Calculate the segment/Stretch monotonicity
 *
 * @author Lakshmi Krishnamurthy
 */

public class TensionStretchEstimation {

	/**
	 * Build KLK Exponential Tension Segment Control Parameters
	 * 
	 * @param dblTension Segment Tension
	 * @param sdic Inelastic Segment Parameters
	 * @param rssc Shape Controller
	 * 
	 * 	WARNING: Insufficient Error Checking, so use caution
	 * 
	 * @return KLK Exponential Tension Segment Control Parameters
	 */

	public static final SegmentCustomBuilderControl KLKExponentialTensionSegmentControlParams (
		final double dblTension,
		final SegmentDesignInelasticControl sdic,
		final ResponseScalingShapeControl rssc)
		throws Exception
	{
		return new SegmentCustomBuilderControl (
			MultiSegmentSequenceBuilder.BASIS_SPLINE_KLK_EXPONENTIAL_TENSION,
			new ExponentialTensionSetParams (dblTension),
			sdic,
			rssc);
	}

	/**
	 * Build KLK Hyperbolic Tension Segment Control Parameters
	 * 
	 * @param dblTension Segment Tension
	 * @param sdic Inelastic Segment Parameters
	 * @param rssc Shape Controller
	 * 
	 * 	WARNING: Insufficient Error Checking, so use caution
	 * 
	 * @return KLK Hyperbolic Tension Segment Control Parameters
	 */

	public static final SegmentCustomBuilderControl KLKHyperbolicTensionSegmentControlParams (
		final double dblTension,
		final SegmentDesignInelasticControl sdic,
		final ResponseScalingShapeControl rssc)
		throws Exception
	{
		return new SegmentCustomBuilderControl (
			MultiSegmentSequenceBuilder.BASIS_SPLINE_KLK_HYPERBOLIC_TENSION,
			new ExponentialTensionSetParams (dblTension),
			sdic,
			rssc);
	}

	/**
	 * Build KLK Rational Linear Tension Segment Control Parameters
	 * 
	 * @param dblTension Segment Tension
	 * @param sdic Inelastic Segment Parameters
	 * @param rssc Shape Controller
	 * 
	 * 	WARNING: Insufficient Error Checking, so use caution
	 * 
	 * @return KLK Rational Linear Tension Segment Control Parameters
	 */

	public static final SegmentCustomBuilderControl KLKRationalLinearTensionSegmentControlParams (
		final double dblTension,
		final SegmentDesignInelasticControl sdic,
		final ResponseScalingShapeControl rssc)
		throws Exception
	{
		return new SegmentCustomBuilderControl (
			MultiSegmentSequenceBuilder.BASIS_SPLINE_KLK_RATIONAL_LINEAR_TENSION,
			new ExponentialTensionSetParams (dblTension),
			sdic,
			rssc);
	}

	/**
	 * Build KLK Rational Quadratic Tension Segment Control Parameters
	 * 
	 * @param dblTension Segment Tension
	 * @param sdic Inelastic Segment Parameters
	 * @param rssc Shape Controller
	 * 
	 * 	WARNING: Insufficient Error Checking, so use caution
	 * 
	 * @return KLK Rational Quadratic Tension Segment Control Parameters
	 */

	public static final SegmentCustomBuilderControl KLKRationalQuadraticTensionSegmentControlParams (
		final double dblTension,
		final SegmentDesignInelasticControl sdic,
		final ResponseScalingShapeControl rssc)
		throws Exception
	{
		return new SegmentCustomBuilderControl (
			MultiSegmentSequenceBuilder.BASIS_SPLINE_KLK_RATIONAL_QUADRATIC_TENSION,
			new ExponentialTensionSetParams (dblTension),
			sdic,
			rssc);
	}

	/**
	 * Perform the following sequence of tests for a given segment control for a predictor/response range
	 * 	- Estimate
	 *  - Compute the segment-by-segment monotonicity
	 *  - Stretch Jacobian
	 *  - Stretch knot insertion
	 * 
	 * @param adblX The Predictor Array
	 * @param adblY The Response Array
	 * @param scbc The Segment Builder Parameters
	 * 
	 * 	WARNING: Insufficient Error Checking, so use caution
	 */

	public static final void BasisSplineStretchTest (
		final double[] adblX,
		final double[] adblY,
		final SegmentCustomBuilderControl scbc)
		throws Exception
	{
		double dblX = 1.;
		double dblXMax = 10.;

		/*
		 * Array of Segment Builder Parameters - one per segment
		 */

		SegmentCustomBuilderControl[] aSCBC = new SegmentCustomBuilderControl[adblX.length - 1]; 

		for (int i = 0; i < adblX.length - 1; ++i)
			aSCBC[i] = scbc;

		/*
		 * Construct a Stretch instance 
		 */

		MultiSegmentSequence mss = MultiSegmentSequenceBuilder.CreateCalibratedStretchEstimator (
			"SPLINE_STRETCH",
			adblX, // predictors
			adblY, // responses
			aSCBC, // Basis Segment Builder parameters
			null, 
			BoundarySettings.NaturalStandard(), // Boundary Condition - Natural
			MultiSegmentSequence.CALIBRATE); // Calibrate the Stretch predictors to the responses

		/*
		 * Estimate, compute the segment-by-segment monotonicity and the Stretch Jacobian
		 */

		while (dblX <= dblXMax) {
			System.out.println ("Y[" + dblX + "] " + FormatUtil.FormatDouble (mss.responseValue (dblX), 1, 2, 1.) + " | " +
				mss.monotoneType (dblX));

			System.out.println ("\tJacobian Y[" + dblX + "]=" + mss.jackDResponseDResponseInput (dblX).displayString());

			dblX += 1.;
		}

		System.out.println ("\t\tSPLINE_STRETCH DPE: " + mss.curvatureDPE());

		/*
		 * Construct a new Stretch instance by inserting a pair of of predictor/response knots
		 */

		MultiSegmentSequence mssInsert = MultiSegmentSequenceModifier.InsertKnot (mss,
			9.,
			10.,
			BoundarySettings.NaturalStandard(), // Boundary Condition - Natural
			MultiSegmentSequence.CALIBRATE); // Calibrate the Stretch predictors to the responses

		dblX = 1.;

		/*
		 * Estimate, compute the sgement-by-segment monotonicty and the Stretch Jacobian
		 */

		while (dblX <= dblXMax) {
			System.out.println ("Inserted Y[" + dblX + "] " + FormatUtil.FormatDouble (mssInsert.responseValue (dblX), 1, 2, 1.)
				+ " | " + mssInsert.monotoneType (dblX));

			dblX += 1.;
		}

		System.out.println ("\t\tSPLINE_STRETCH_INSERT DPE: " + mssInsert.curvatureDPE());
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

		BasisSplineStretchTest (adblX, adblY, KLKHyperbolicTensionSegmentControlParams (dblKLKTension, segParams, rssc));

		System.out.println (" \n---------- \n KLK EXPONENTIAL TENSION \n ---------- \n");

		BasisSplineStretchTest (adblX, adblY, KLKExponentialTensionSegmentControlParams (dblKLKTension, segParams, rssc));

		System.out.println (" \n---------- \n KLK RATIONAL LINEAR TENSION \n ---------- \n");

		BasisSplineStretchTest (adblX, adblY, KLKRationalLinearTensionSegmentControlParams (dblKLKTension, segParams, rssc));

		System.out.println (" \n---------- \n KLK RATIONAL QUADRATIC TENSION \n ---------- \n");

		BasisSplineStretchTest (adblX, adblY, KLKRationalQuadraticTensionSegmentControlParams (dblKLKTension, segParams, rssc));
	}
}
