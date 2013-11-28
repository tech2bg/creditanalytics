
package org.drip.sample.regime;

import org.drip.quant.common.FormatUtil;
import org.drip.quant.function1D.QuadraticRationalShapeControl;
import org.drip.spline.basis.PolynomialFunctionSetParams;
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
 * PenalizedCurvatureFit demonstrates the setting up and the usage of the curvature and closeness of fit
 * 	penalizing spline.
 *
 * @author Lakshmi Krishnamurthy
 */

public class PenalizedCurvatureFit {

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
	 *  - Regime Jacobian
	 *  - Regime knot insertion
	 * 
	 * @param adblX The Predictor Array
	 * @param adblY The Response Array
	 * @param scbc The Segment Builder Parameters
	 * @param sbfr The Fitness Weighted Response Instance
	 * 
	 * 	WARNING: Insufficient Error Checking, so use caution
	 */

	public static final MultiSegmentSequence BasisSplineRegimeTest (
		final double[] adblX,
		final double[] adblY,
		final SegmentCustomBuilderControl scbc,
		final RegimeBestFitResponse rbfr)
		throws Exception
	{
		/*
		 * Array of Segment Builder Parameters - one per segment
		 */

		SegmentCustomBuilderControl[] aSCBC = new SegmentCustomBuilderControl[adblX.length - 1]; 

		for (int i = 0; i < adblX.length - 1; ++i)
			aSCBC[i] = scbc;

		/*
		 * Construct a Regime instance 
		 */

		MultiSegmentSequence regime = MultiSegmentSequenceBuilder.CreateCalibratedRegimeEstimator (
			"SPLINE_REGIME",
			adblX, // predictors
			adblY, // responses
			aSCBC, // Basis Segment Builder parameters
			rbfr, // Regime Fitness Weighted Response
			BoundarySettings.NaturalStandard(), // Boundary Condition - Natural
			MultiSegmentSequence.CALIBRATE); // Calibrate the Regime predictors to the responses

		return regime;
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

		RegimeBestFitResponse rbfr = RegimeBestFitResponse.Create (
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
		 * Construct the segment inelastic parameter that is C2 (iK = 2 sets it to C2), with 2nd order
		 * 	roughness penalty derivative, and without constraint
		 */

		int iK = 2;
		int iRoughnessPenaltyDerivativeOrder = 2;

		SegmentDesignInelasticControl sdic = SegmentDesignInelasticControl.Create (
			iK,
			iRoughnessPenaltyDerivativeOrder);

		System.out.println (" \n--------------------------------------------------------------------------------------------------");

		System.out.println (" \n         == ORIGINAL #1 ==      $$   == ORIGINAL #2 ==    $$   == BEST FIT ==    ");

		System.out.println (" \n--------------------------------------------------------------------------------------------------");

		int iPolyNumBasis = 4;

		SegmentCustomBuilderControl scbc1 = PolynomialSegmentControlParams (iPolyNumBasis, sdic, rssc);

		SegmentCustomBuilderControl scbc2 = PolynomialSegmentControlParams (iPolyNumBasis + 1, sdic, rssc);

		MultiSegmentSequence regimeBase1 = BasisSplineRegimeTest (adblX, adblY, scbc1, null);

		MultiSegmentSequence regimeBase2 = BasisSplineRegimeTest (adblX, adblY, scbc2, null);

		MultiSegmentSequence regimeBestFit = BasisSplineRegimeTest (adblX, adblY, scbc2, rbfr);

		/*
		 * Compute the segment-by-segment monotonicity
		 */

		double dblX = regimeBase1.getLeftPredictorOrdinateEdge();

		double dblXMax = regimeBase1.getRightPredictorOrdinateEdge();

		while (dblX <= dblXMax) {
			System.out.println (
				"Y[" + FormatUtil.FormatDouble (dblX, 1, 2, 1.) + "] " +
				FormatUtil.FormatDouble (regimeBase1.responseValue (dblX), 2, 2, 1.) + " | "
					+ regimeBase1.monotoneType (dblX) + " $$ "
				+ FormatUtil.FormatDouble (regimeBase2.responseValue (dblX), 2, 2, 1.) + " | "
					+ regimeBase2.monotoneType (dblX) + " $$ "
				+ FormatUtil.FormatDouble (regimeBestFit.responseValue (dblX), 2, 2, 1.) + " | "
					+ regimeBestFit.monotoneType (dblX));

			dblX += 0.25;
		}

		/*
		 * Compute the Regime Jacobian
		 */

		dblX = regimeBase1.getLeftPredictorOrdinateEdge();

		while (dblX <= dblXMax) {
			System.out.println (
				"\t\tJacobian Y[" + FormatUtil.FormatDouble (dblX, 2, 2, 1.) + "] => " +
					regimeBase1.jackDResponseDResponseInput (dblX).displayString());

			System.out.println (
				"\t\tJacobian Y[" + FormatUtil.FormatDouble (dblX, 2, 2, 1.) + "] => " +
					regimeBase2.jackDResponseDResponseInput (dblX).displayString());

			System.out.println (
				"\t\tJacobian Y[" + FormatUtil.FormatDouble (dblX, 2, 2, 1.) + "] => " +
					regimeBestFit.jackDResponseDResponseInput (dblX).displayString());

			System.out.println ("\t\t----\n\t\t----");

			dblX += 0.25;
		}

		System.out.println ("\tBASE #1  DPE: " + FormatUtil.FormatDouble (regimeBase1.curvatureDPE(), 10, 0, 1.));

		System.out.println ("\tBASE #2  DPE: " + FormatUtil.FormatDouble (regimeBase2.curvatureDPE(), 10, 0, 1.));

		System.out.println ("\tBEST FIT DPE: " + FormatUtil.FormatDouble (regimeBestFit.curvatureDPE(), 10, 0, 1.));
	}
}
