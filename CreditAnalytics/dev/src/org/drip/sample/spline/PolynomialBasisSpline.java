
package org.drip.sample.spline;

import org.drip.math.calculus.WengertJacobian;
import org.drip.math.function.*;
import org.drip.math.segment.*;
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
 * PolynomialBasisSpline implements Samples for the Construction and the usage of polynomial (both regular
 * 	and Hermite) basis spline functions. It demonstrates the following:
 * 	- Control the polynomial segment using the rational shape controller, the appropriate Ck, and the basis
 * 		function.
 * 	- Demonstrate the variational shape optimization behavior.
 * 	- Estimate the node value and the node value Jacobian with the segment, as well as at the boundaries.
 * 	- Calculate the segment monotonicity.
 *
 * @author Lakshmi Krishnamurthy
 */

public class PolynomialBasisSpline {

	/*
	 * This sample demonstrates the following:
	 * 
	 * 	- Construction of two segments, 1 and 2.
	 *  - Calibration of the segments to the left and the right node values
	 *  - Extraction of the segment Jacobians and segment monotonicity
	 *  - Estimate point value and the Jacobian
	 * 
	 *  	USE WITH CARE: This sample ignores errors and does not handle exceptions.
	 */

	private static final void TestPolynomialSpline (
		final int iNumBasis,
		final int iCk,
		final int iRoughnessPenaltyDerivativeOrder,
		final ResponseScalingShapeController rssc)
		throws Exception
	{
		System.out.println (" ------------------------------ \n     POLYNOMIAL n = " + iNumBasis +
			"; Ck = " + iCk + "\n ------------------------------ \n");

		/*
		 * Construct the segment inelastic parameter that is C2 (iCk = 2 sets it to C2), without constraint
		 */

		DesignInelasticParams segParams = DesignInelasticParams.Create (iCk, iRoughnessPenaltyDerivativeOrder);

		/*
		 * Create the basis parameter set from the number of basis functions, and construct the basis
		 */

		PolynomialBasisSetParams polybsbp = new PolynomialBasisSetParams (iNumBasis);

		AbstractUnivariate[] aAU = BasisSetBuilder.PolynomialBasisSet (polybsbp);

		/*
		 * Construct the left and the right segments
		 */

		PredictorResponse seg1 = LocalBasisPredictorResponse.Create (1.0, 1.5, aAU, rssc, segParams);

		PredictorResponse seg2 = LocalBasisPredictorResponse.Create (1.5, 2.0, aAU, rssc, segParams);

		/*
		 * Calibrate the left segment using the node values, and compute the segment Jacobian
		 */

		WengertJacobian wj1 = seg1.jackDCoeffDEdgeParams (25., 0., 20.25, null);

		System.out.println ("\tY[" + 1.0 + "]: " + seg1.responseValue (1.));

		System.out.println ("\tY[" + 1.5 + "]: " + seg1.responseValue (1.5));

		System.out.println ("Segment 1 Jacobian: " + wj1.displayString());

		System.out.println ("Segment 1 Head: " + seg1.jackDCoeffDEdgeParams().displayString());

		System.out.println ("Segment 1 Monotone Type: " + seg1.monotoneType());

		System.out.println ("Segment 1 DCPE: " + seg1.dcpe());

		/*
		 * Calibrate the right segment using the node values, and compute the segment Jacobian
		 */

		WengertJacobian wj2 = seg2.jackDCoeffDEdgeParams (seg1, 16., null);

		System.out.println ("\tY[" + 1.5 + "]: " + seg2.responseValue (1.5));

		System.out.println ("\tY[" + 2. + "]: " + seg2.responseValue (2.));

		System.out.println ("Segment 2 Jacobian: " + wj2.displayString());

		System.out.println ("Segment 2 Regular Jacobian: " + seg2.jackDCoeffDEdgeParams().displayString());

		System.out.println ("Segment 2 Monotone Type: " + seg2.monotoneType());

		System.out.println ("Segment 2 DCPE: " + seg2.dcpe());

		seg2.calibrate (seg1, 14., null);

		/*
		 * Estimate the segment value at the given variate, and compute the corresponding Jacobian
		 */

		double dblX = 2.0;

		System.out.println ("\t\tValue[" + dblX + "]: " + seg2.responseValue (dblX));

		System.out.println ("\t\tValue Jacobian[" + dblX + "]: " + seg2.jackDResponseDEdgeParams (dblX).displayString());

		System.out.println ("\t\tSegment 2 DCPE: " + seg2.dcpe());
	}

	/*
	 * This sample demonstrates the following specifically for the Ck Hermite Splines, which are calibrated
	 *  using left and right node values, along with their derivatives:
	 * 
	 * 	- Construction of two segments, 1 and 2.
	 *  - Calibration of the segments to the left and the right node values
	 *  - Extraction of the segment Jacobians and segment monotonicity
	 *  - Estimate point value and the Jacobian
	 * 
	 *  	USE WITH CARE: This sample ignores errors and does not handle exceptions.
	 */

	private static final void TestC1HermiteSpline (
		final int iNumBasis,
		final int iCk,
		final int iRoughnessPenaltyDerivativeOrder,
		final ResponseScalingShapeController sc)
		throws Exception
	{
		System.out.println (" ------------------------------ \n     HERMITE POLYNOMIAL n = " + iNumBasis +
			"; Ck = " + iCk + "\n ------------------------------ \n");

		/*
		 * Construct the segment inelastic parameter that is C2 (iCk = 2 sets it to C2), without constraint
		 */

		DesignInelasticParams segParams = DesignInelasticParams.Create (iCk, iRoughnessPenaltyDerivativeOrder);

		/*
		 * Create the basis parameter set from the number of basis functions, and construct the basis
		 */

		PolynomialBasisSetParams polybsbp = new PolynomialBasisSetParams (iNumBasis);

		AbstractUnivariate[] aAU = BasisSetBuilder.PolynomialBasisSet (polybsbp);

		/*
		 * Construct the left and the right segments
		 */

		PredictorResponse seg1 = LocalBasisPredictorResponse.Create (0.0, 1.0, aAU, sc, segParams);

		PredictorResponse seg2 = LocalBasisPredictorResponse.Create (1.0, 2.0, aAU, sc, segParams);

		/*
		 * Calibrate the left segment using the node values, and compute the segment Jacobian
		 */

		seg1.calibrate (new CalibrationParams (
			new double[] {0., 1.}, // Segment Calibration Nodes
			new double[] {1., 4.}, // Segment Calibration Values
			new double[] {1.}, // Segment Left Derivative
			new double[] {6.}, // Segment Left Derivative
			null, null)); // Segment Constraint AND Fitness Penalty Response

		System.out.println ("\tY[" + 0.0 + "]: " + seg1.responseValue (0.0));

		System.out.println ("\tY[" + 1.0 + "]: " + seg1.responseValue (1.0));

		System.out.println ("Segment 1 Head: " + seg1.jackDCoeffDEdgeParams().displayString());

		System.out.println ("Segment 1 Monotone Type: " + seg1.monotoneType());

		System.out.println ("Segment 1 DCPE: " + seg1.dcpe());

		/*
		 * Calibrate the right segment using the node values, and compute the segment Jacobian
		 */

		seg2.calibrate (new CalibrationParams (
			new double[] {0., 1.}, // Segment Calibration Nodes
			new double[] {4., 15.}, // Segment Calibration Values
			new double[] {6.}, // Segment Left Derivative
			new double[] {17.}, // Segment Left Derivative
			null, null)); // Segment Constraint AND Fitness Penalty Response

		System.out.println ("\tY[" + 1.0 + "]: " + seg2.responseValue (1.0));

		System.out.println ("\tY[" + 2.0 + "]: " + seg2.responseValue (2.0));

		System.out.println ("Segment 2 Regular Jacobian: " + seg2.jackDCoeffDEdgeParams().displayString());

		System.out.println ("Segment 2 Monotone Type: " + seg2.monotoneType());

		System.out.println ("Segment 2 DCPE: " + seg2.dcpe());

		seg2.calibrate (seg1, 14., null);

		/*
		 * Estimate the segment value at the given variate, and compute the corresponding Jacobian
		 */

		double dblX = 2.0;

		System.out.println ("\t\tValue[" + dblX + "]: " + seg2.responseValue (dblX));

		System.out.println ("\t\tValue Jacobian[" + dblX + "]: " + seg2.jackDResponseDEdgeParams (dblX).displayString());

		System.out.println ("\t\tSegment 2 DCPE: " + seg2.dcpe());
	}

	public static final void main (
		final String[] astrArgs)
		throws Exception
	{
		/*
		 * Construct a rational shape controller with the shape controller tension of 1.
		 */

		double dblShapeControllerTension = 1.;

		ResponseScalingShapeController rssc = new ResponseScalingShapeController (true, new RationalShapeControl (dblShapeControllerTension));

		/*
		 * Set to 2nd order Roughness Penalty Derivative Order.
		 */

		int iRoughnessPenaltyDerivativeOrder = 2;

		/*
		 * Test the polynomial spline across different polynomial degrees and Ck's
		 */

		TestPolynomialSpline (2, 0, iRoughnessPenaltyDerivativeOrder, rssc);

		TestPolynomialSpline (3, 0, iRoughnessPenaltyDerivativeOrder, rssc);

		TestPolynomialSpline (3, 1, iRoughnessPenaltyDerivativeOrder, rssc);

		TestPolynomialSpline (4, 0, iRoughnessPenaltyDerivativeOrder, rssc);

		TestPolynomialSpline (4, 1, iRoughnessPenaltyDerivativeOrder, rssc);

		TestPolynomialSpline (4, 2, iRoughnessPenaltyDerivativeOrder, rssc);

		TestPolynomialSpline (5, 0, iRoughnessPenaltyDerivativeOrder, rssc);

		TestPolynomialSpline (5, 1, iRoughnessPenaltyDerivativeOrder, rssc);

		TestPolynomialSpline (5, 2, iRoughnessPenaltyDerivativeOrder, rssc);

		TestPolynomialSpline (5, 3, iRoughnessPenaltyDerivativeOrder, rssc);

		TestPolynomialSpline (6, 0, iRoughnessPenaltyDerivativeOrder, rssc);

		TestPolynomialSpline (6, 1, iRoughnessPenaltyDerivativeOrder, rssc);

		TestPolynomialSpline (6, 2, iRoughnessPenaltyDerivativeOrder, rssc);

		TestPolynomialSpline (6, 3, iRoughnessPenaltyDerivativeOrder, rssc);

		TestPolynomialSpline (6, 4, iRoughnessPenaltyDerivativeOrder, rssc);

		TestPolynomialSpline (7, 0, iRoughnessPenaltyDerivativeOrder, rssc);

		TestPolynomialSpline (7, 1, iRoughnessPenaltyDerivativeOrder, rssc);

		TestPolynomialSpline (7, 2, iRoughnessPenaltyDerivativeOrder, rssc);

		TestPolynomialSpline (7, 3, iRoughnessPenaltyDerivativeOrder, rssc);

		TestPolynomialSpline (7, 4, iRoughnessPenaltyDerivativeOrder, rssc);

		TestPolynomialSpline (7, 5, iRoughnessPenaltyDerivativeOrder, rssc);

		/*
		 * Test the C1 Hermite spline
		 */

		System.out.println (" -------------------- \n Ck HERMITE \n -------------------- \n");

		TestC1HermiteSpline (4, 1, iRoughnessPenaltyDerivativeOrder, rssc);
	}
}
