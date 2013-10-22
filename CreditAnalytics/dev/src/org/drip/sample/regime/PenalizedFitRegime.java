
package org.drip.sample.regime;

import org.drip.math.common.FormatUtil;
import org.drip.math.function.QuadraticRationalShapeControl;
import org.drip.math.regime.*;
import org.drip.math.segment.*;
import org.drip.math.spline.PolynomialBasisSetParams;

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
 * PenalizedFitRegime demonstrates the setting up and the usage of the curvature and closeness of fit
 * 	penalizing spline.
 *
 * @author Lakshmi Krishnamurthy
 */

public class PenalizedFitRegime {

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

	public static final PredictorResponseBuilderParams PolynomialSegmentControlParams (
		final int iNumBasis,
		final DesignInelasticParams segParams,
		final ResponseScalingShapeController rssc)
		throws Exception
	{
		return new PredictorResponseBuilderParams (
			RegimeBuilder.BASIS_SPLINE_POLYNOMIAL,
			new PolynomialBasisSetParams (iNumBasis),
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
	 * @param fwr The Fitness Weighted Response Instance
	 * 
	 * 	WARNING: Insufficient Error Checking, so use caution
	 */

	public static final MultiSegmentRegime BasisSplineRegimeTest (
		final double[] adblX,
		final double[] adblY,
		final PredictorResponseBuilderParams sbp,
		final BestFitWeightedResponse fwr)
		throws Exception
	{
		/*
		 * Array of Segment Builder Parameters - one per segment
		 */

		PredictorResponseBuilderParams[] aSBP = new PredictorResponseBuilderParams[adblX.length - 1]; 

		for (int i = 0; i < adblX.length - 1; ++i)
			aSBP[i] = sbp;

		/*
		 * Construct a Regime instance 
		 */

		MultiSegmentRegime regime = RegimeBuilder.CreateCalibratedRegimeEstimator (
			"SPLINE_REGIME",
			adblX, // predictors
			adblY, // responses
			aSBP, // Basis Segment Builder parameters
			fwr, // Fitness Weighted Response
			MultiSegmentRegime.BOUNDARY_CONDITION_NATURAL, // Boundary Condition - Natural
			MultiSegmentRegime.CALIBRATE); // Calibrate the Regime predictors to the responses

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

		BestFitWeightedResponse fwr = BestFitWeightedResponse.Create (
			new double[] { 2.28,  2.52,  2.73, 3.00,  5.50, 8.44,  8.76,  9.08,  9.80,  9.92},
			new double[] {14.27, 12.36, 10.61, 9.25, -0.50, 7.92, 10.07, 12.23, 15.51, 16.36},
			new double[] { 1.09,  0.82,  1.34, 1.10,  0.50, 0.79,  0.65,  0.49,  0.24,  0.21}
		);

		/*
		 * Construct a rational shape controller with the shape controller tension of 1, and Global Scaling.
		 */

		double dblShapeControllerTension = 1.;

		ResponseScalingShapeController rssc = new ResponseScalingShapeController (
			false,
			new QuadraticRationalShapeControl (dblShapeControllerTension));

		/*
		 * Construct the segment inelastic parameter that is C2 (iK = 2 sets it to C2), with 2nd order
		 * 	roughness penalty derivative, and without constraint
		 */

		int iK = 2;
		int iRoughnessPenaltyDerivativeOrder = 2;

		DesignInelasticParams dip = DesignInelasticParams.Create (iK, iRoughnessPenaltyDerivativeOrder);

		System.out.println (" \n--------------------------------------------------------------------------------------------------");

		System.out.println (" \n         == ORIGINAL #1 ==      $$   == ORIGINAL #2 ==    $$   == BEST FIT ==    ");

		System.out.println (" \n--------------------------------------------------------------------------------------------------");

		int iPolyNumBasis = 4;

		PredictorResponseBuilderParams prbp1 = PolynomialSegmentControlParams (iPolyNumBasis, dip, rssc);

		PredictorResponseBuilderParams prbp2 = PolynomialSegmentControlParams (iPolyNumBasis + 1, dip, rssc);

		MultiSegmentRegime regimeBase1 = BasisSplineRegimeTest (adblX, adblY, prbp1, null);

		MultiSegmentRegime regimeBase2 = BasisSplineRegimeTest (adblX, adblY, prbp2, null);

		MultiSegmentRegime regimeBestFit = BasisSplineRegimeTest (adblX, adblY, prbp2, fwr);

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

		System.out.println ("\tBASE #1  DCPE: " + FormatUtil.FormatDouble (regimeBase1.dcpe(), 10, 0, 1.));

		System.out.println ("\tBASE #2  DCPE: " + FormatUtil.FormatDouble (regimeBase2.dcpe(), 10, 0, 1.));

		System.out.println ("\tBEST FIT DCPE: " + FormatUtil.FormatDouble (regimeBestFit.dcpe(), 10, 0, 1.));
	}
}
