
package org.drip.sample.spline;

import org.drip.spline.basis.*;
import org.drip.spline.bspline.*;
import org.drip.spline.params.*;
import org.drip.spline.segment.ConstitutiveState;

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
 * BasisSplineSet implements Samples for the Construction and the usage of various basis spline functions. It
 *  demonstrates the following:
 * 	- Construction of segment control parameters - polynomial (regular/Bernstein) segment control,
 * 		exponential/hyperbolic tension segment control, Kaklis-Pandelis tension segment control, and C1
 * 		Hermite.
 * 	- Control the segment using the rational shape controller, and the appropriate Ck.
 * 	- Estimate the node value and the node value Jacobian with the segment, as well as at the boundaries.
 * 	- Calculate the segment monotonicity.

 * @author Lakshmi Krishnamurthy
 */

public class BasisBSplineSet {
	private static final void TestSpline (
		final FunctionSet fs,
		final ResponseScalingShapeControl rssc,
		final SegmentDesignInelasticControl segParams)
		throws Exception
	{
		/*
		 * Construct the left and the right segments
		 */

		ConstitutiveState seg1 = ConstitutiveState.Create (1.0, 1.5, fs, rssc, segParams);

		/*
		 * Calibrate the left segment using the node values, and compute the segment Jacobian
		 */

		System.out.println (seg1.calibrate (25., 0., 20.25, null));

		System.out.println ("\tY[" + 1.0 + "]: " + seg1.responseValue (1.));

		System.out.println ("\tY[" + 1.5 + "]: " + seg1.responseValue (1.5));
	}

	public static final void main (
		final String[] astrArgs)
		throws Exception
	{
		BSplineSequenceParams bssp = new BSplineSequenceParams (
			BasisHatPairGenerator.PROCESSED_CUBIC_RATIONAL,
			BasisHatShapeControl.SHAPE_CONTROL_RATIONAL_LINEAR,
			2,
			4,
			1.,
			2);

		FunctionSet fsBSS = FunctionSetBuilder.BSplineBasisSet (bssp);

		/* for (double dbl = 0.; dbl <= 1.; dbl += 0.25)
			System.out.println ("\tFunc[" + fsBSS.indexedBasisFunction (3) + "] => " + fsBSS.indexedBasisFunction (3).calcDerivative (1., 2));
		*/

		/*
		 * Construct the segment inelastic parameter that is C2 (iK = 2 sets it to C2), with second order
		 *  curvature penalty, and without constraint
		 */

		int iK = 2;
		int iCurvaturePenaltyDerivativeOrder = 2;

		SegmentDesignInelasticControl segParams = SegmentDesignInelasticControl.Create (
			iK,
			iCurvaturePenaltyDerivativeOrder);

		TestSpline (fsBSS, null, segParams);
	}
}
