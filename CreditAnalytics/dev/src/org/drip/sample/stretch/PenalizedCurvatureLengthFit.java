
package org.drip.sample.stretch;

import org.drip.quant.common.FormatUtil;
import org.drip.quant.function1D.QuadraticRationalShapeControl;
import org.drip.spline.basis.PolynomialFunctionSetParams;
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
 * PenalizedCurvatureLengthFit demonstrates the setting up and the usage of the curvature, the length, and
 * 	the closeness of fit penalizing spline.
 *
 * @author Lakshmi Krishnamurthy
 */

public class PenalizedCurvatureLengthFit {

	/**
	 * Build Polynomial Segment Control Parameters
	 * 
	 * @param iNumBasis Number of Polynomial Basis Functions
	 * @param sdic Inelastic Segment Parameters
	 * @param rssc Shape Controller
	 * 
	 * 	WARNING: Insufficient Error Checking, so use caution
	 * 
	 * @return Polynomial Segment Control Parameters
	 */

	public static final SegmentCustomBuilderControl PolynomialSegmentControlParams (
		final int iNumBasis,
		final SegmentDesignInelasticControl sdic,
		final ResponseScalingShapeControl rssc)
		throws Exception
	{
		return new SegmentCustomBuilderControl (
			MultiSegmentSequenceBuilder.BASIS_SPLINE_POLYNOMIAL,
			new PolynomialFunctionSetParams (iNumBasis),
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
	 * @param rbfr The Fitness Weighted Response Instance
	 * 
	 * 	WARNING: Insufficient Error Checking, so use caution
	 */

	public static final MultiSegmentSequence BasisSplineStretchTest (
		final double[] adblX,
		final double[] adblY,
		final SegmentCustomBuilderControl scbc,
		final StretchBestFitResponse rbfr)
		throws Exception
	{
		/*
		 * Array of Segment Builder Parameters - one per segment
		 */

		SegmentCustomBuilderControl[] aSCBC = new SegmentCustomBuilderControl[adblX.length - 1]; 

		for (int i = 0; i < adblX.length - 1; ++i)
			aSCBC[i] = scbc;

		/*
		 * Construct a Stretch instance 
		 */

		return MultiSegmentSequenceBuilder.CreateCalibratedStretchEstimator (
			"SPLINE_STRETCH",
			adblX, // predictors
			adblY, // responses
			aSCBC, // Basis Segment Builder parameters
			rbfr, // Fitness Weighted Response
			BoundarySettings.NaturalStandard(), // Boundary Condition - Natural
			MultiSegmentSequence.CALIBRATE); // Calibrate the Stretch predictors to the responses
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
		 * Construct a set of Predictor Ordinates, their Responses, and corresponding Weights to serve as
		 *  weighted closeness of fit.
		 */

		StretchBestFitResponse rbfr = StretchBestFitResponse.Create (
			new double[] { 2.28,  2.52,  2.73, 3.00,  5.50, 8.44,  8.76,  9.08,  9.80,  9.92},
			new double[] {14.27, 12.36, 10.61, 9.25, -0.50, 7.92, 10.07, 12.23, 15.51, 16.36},
			new double[] { 1.09,  0.82,  1.34, 1.10,  0.50, 0.79,  0.65,  0.49,  0.24,  0.21}
		);

		/*
		 * Construct a rational shape controller with the shape controller tension of 1, and Global Scaling.
		 */

		double dblShapeControllerTension = 1.;

		ResponseScalingShapeControl rssc = new ResponseScalingShapeControl (
			false,
			new QuadraticRationalShapeControl (dblShapeControllerTension));

		/*
		 * Construct the Segment Inelastic Parameter that is C2 (iK = 2 sets it to C2), with First Order
		 * 	Segment Length Penalty Derivative, Second Order Segment Curvature Penalty Derivative, their
		 *  Amplitudes, and without Constraint
		 */

		int iK = 2;
		double dblLengthPenaltyAmplitude = 1.;
		double dblCurvaturePenaltyAmplitude = 1.;
		int iLengthPenaltyDerivativeOrder = 1;
		int iCurvaturePenaltyDerivativeOrder = 2;

		SegmentDesignInelasticControl sdic = new SegmentDesignInelasticControl (
			iK,
			new org.drip.spline.params.SegmentFlexurePenaltyControl (
				iLengthPenaltyDerivativeOrder,
				dblLengthPenaltyAmplitude),
			new org.drip.spline.params.SegmentFlexurePenaltyControl (
				iCurvaturePenaltyDerivativeOrder,
				dblCurvaturePenaltyAmplitude)
			);

		System.out.println (" \n--------------------------------------------------------------------------------------------------");

		System.out.println (" \n         == ORIGINAL #1 ==      $$   == ORIGINAL #2 ==    $$   == BEST FIT ==    ");

		System.out.println (" \n--------------------------------------------------------------------------------------------------");

		int iPolyNumBasis = 4;

		SegmentCustomBuilderControl scbc1 = PolynomialSegmentControlParams (iPolyNumBasis, sdic, rssc);

		SegmentCustomBuilderControl scbc2 = PolynomialSegmentControlParams (iPolyNumBasis + 1, sdic, rssc);

		MultiSegmentSequence mssBase1 = BasisSplineStretchTest (adblX, adblY, scbc1, null);

		MultiSegmentSequence mssBase2 = BasisSplineStretchTest (adblX, adblY, scbc2, null);

		MultiSegmentSequence mssBestFit = BasisSplineStretchTest (adblX, adblY, scbc2, rbfr);

		/*
		 * Compute the segment-by-segment monotonicity
		 */

		double dblX = mssBase1.getLeftPredictorOrdinateEdge();

		double dblXMax = mssBase1.getRightPredictorOrdinateEdge();

		while (dblX <= dblXMax) {
			System.out.println (
				"Y[" + FormatUtil.FormatDouble (dblX, 1, 2, 1.) + "] " +
				FormatUtil.FormatDouble (mssBase1.responseValue (dblX), 2, 2, 1.) + " | "
					+ mssBase1.monotoneType (dblX) + " $$ "
				+ FormatUtil.FormatDouble (mssBase2.responseValue (dblX), 2, 2, 1.) + " | "
					+ mssBase2.monotoneType (dblX) + " $$ "
				+ FormatUtil.FormatDouble (mssBestFit.responseValue (dblX), 2, 2, 1.) + " | "
					+ mssBestFit.monotoneType (dblX));

			dblX += 0.25;
		}

		/*
		 * Compute the Stretch Jacobian
		 */

		dblX = mssBase1.getLeftPredictorOrdinateEdge();

		while (dblX <= dblXMax) {
			System.out.println (
				"\t\tJacobian Y[" + FormatUtil.FormatDouble (dblX, 2, 2, 1.) + "] => " +
					mssBase1.jackDResponseDResponseInput (dblX).displayString());

			System.out.println (
				"\t\tJacobian Y[" + FormatUtil.FormatDouble (dblX, 2, 2, 1.) + "] => " +
					mssBase2.jackDResponseDResponseInput (dblX).displayString());

			System.out.println (
				"\t\tJacobian Y[" + FormatUtil.FormatDouble (dblX, 2, 2, 1.) + "] => " +
					mssBestFit.jackDResponseDResponseInput (dblX).displayString());

			System.out.println ("\t\t----\n\t\t----");

			dblX += 0.25;
		}

		System.out.println ("\n\t\t----STRETCH #1----\n\t\t-----------------");

		System.out.println ("\tCURVATURE DPE         => " +
			FormatUtil.FormatDouble (mssBase1.curvatureDPE(), 10, 0, 1.));

		System.out.println ("\tLENGTH DPE            => " +
			FormatUtil.FormatDouble (mssBase1.lengthDPE(), 10, 0, 1.));

		System.out.println ("\tBEST FIT DPE          => " +
			FormatUtil.FormatDouble (mssBase1.bestFitDPE (rbfr), 10, 0, 1.));

		System.out.println ("\n\t\t----STRETCH #2----\n\t\t-----------------");

		System.out.println ("\tCURVATURE DPE         => " +
			FormatUtil.FormatDouble (mssBase2.curvatureDPE(), 10, 0, 1.));

		System.out.println ("\tLENGTH DPE            => " +
			FormatUtil.FormatDouble (mssBase2.lengthDPE(), 10, 0, 1.));

		System.out.println ("\tBEST FIT DPE          => " +
			FormatUtil.FormatDouble (mssBase2.bestFitDPE (rbfr), 10, 0, 1.));

		System.out.println ("\n\t\t----STRETCH BEST FIT----\n\t\t-----------------------");

		System.out.println ("\tCURVATURE DPE         => " +
			FormatUtil.FormatDouble (mssBestFit.curvatureDPE(), 10, 0, 1.));

		System.out.println ("\tLENGTH DPE            => " +
			FormatUtil.FormatDouble (mssBestFit.lengthDPE(), 10, 0, 1.));

		System.out.println ("\tBEST FIT DPE          => " +
			FormatUtil.FormatDouble (mssBestFit.bestFitDPE (rbfr), 10, 0, 1.));
	}
}
