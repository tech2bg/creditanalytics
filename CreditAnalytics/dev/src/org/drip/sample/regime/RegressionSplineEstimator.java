
package org.drip.sample.regime;

import org.drip.quant.common.FormatUtil;
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
 * RegressionSplineEstimator shows the sample construction and usage of RegressionSplines.
 *
 * @author Lakshmi Krishnamurthy
 */

public class RegressionSplineEstimator {

	/**
	 * Build Polynomial Segment Control Parameters
	 * 
	 * @param iNumBasis Number of Polynomial Basis Functions
	 * @param segParams Inelastic Segment Parameters
	 * 
	 * 	WARNING: Insufficient Error Checking, so use caution
	 * 
	 * @return Polynomial Segment Control Parameters
	 */

	public static final SegmentCustomBuilderControl PolynomialSegmentControlParams (
		final int iNumBasis,
		final SegmentDesignInelasticControl segParams)
		throws Exception
	{
		return new SegmentCustomBuilderControl (
			MultiSegmentSequenceBuilder.BASIS_SPLINE_POLYNOMIAL,
			new PolynomialFunctionSetParams (iNumBasis),
			segParams,
			null);
	}

	/**
	 * Perform the following sequence of tests for a given segment control for a predictor/response range
	 * 	- Estimate
	 *  - Compute the segment-by-segment monotonicity
	 *  - Regime Jacobian
	 *  - Regime knot insertion
	 * 
	 * @param adblX The Predictor Array
	 * @param sbp The Segment Builder Parameters
	 * @param sbfr The Fitness Weighted Response Instance
	 * 
	 * 	WARNING: Insufficient Error Checking, so use caution
	 */

	public static final void BasisSplineRegimeTest (
		final double[] adblX,
		final SegmentCustomBuilderControl sbp,
		final RegimeBestFitResponse rbfr)
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

		MultiSegmentSequence regime = MultiSegmentSequenceBuilder.CreateRegressionSplineEstimator (
			"SPLINE_REGIME",
			adblX, // predictors
			aSBP, // Basis Segment Builder parameters
			rbfr, 
			BoundarySettings.NaturalStandard(), // Boundary Condition - Natural
			MultiSegmentSequence.CALIBRATE); // Calibrate the Regime predictors to the responses

		/*
		 * Estimate, compute the segment-by-segment monotonicity and the Regime Jacobian
		 */

		while (dblX <= dblXMax) {
			System.out.println ("Y[" + dblX + "] " + FormatUtil.FormatDouble (regime.responseValue (dblX), 1, 2, 1.) + " | " +
				regime.monotoneType (dblX));

			System.out.println ("\t\tJacobian Y[" + dblX + "]=" + regime.jackDResponseDResponseInput (dblX).displayString());

			dblX += 1.;
		}

		System.out.println ("\tSPLINE_REGIME DPE: " + regime.curvatureDPE());
	}

	public static final void main (
		final String[] astrArgs)
		throws Exception
	{
		/*
		 * Set the Knot Points
		 */

		double[] adblX = new double[] { 1.00,  5.00, 10.00};

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
		 * Construct the segment inelastic parameter that is C2 (iK = 2 sets it to C2), with 2nd order
		 * 	roughness penalty derivative, and without constraint
		 */

		int iK = 2;
		int iRoughnessPenaltyDerivativeOrder = 2;

		SegmentDesignInelasticControl segParams = SegmentDesignInelasticControl.Create (iK, iRoughnessPenaltyDerivativeOrder);

		int iPolyNumBasis = 4;

		BasisSplineRegimeTest (adblX, PolynomialSegmentControlParams (iPolyNumBasis, segParams), rbfr);
	}
}
