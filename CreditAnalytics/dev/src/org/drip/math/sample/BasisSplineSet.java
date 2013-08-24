
package org.drip.math.sample;

import org.drip.math.calculus.WengertJacobian;
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
 * BasisSplineSet implements Samples for the Construction and the usage of various basis spline functions. It
 *  demonstrates the following:
 * 	- Construction of segment control parameters - polynomial (regular/Bernstein) segment control,
 * 		exponential/hyperbolic tension segment control, Kaklis-Pandelis tension segment control, and C1
 * 		Hermite.
 * 	- Control the segment using the rational shape controller, and the appropriate Ck.
 * 	- Interpolate the node value and the node value Jacobian with the segment, as well as at the boundaries.
 * 	- Calculate the segment monotonicity.

 * @author Lakshmi Krishnamurthy
 */

public class BasisSplineSet {

	/*
	 * Sample demonstrating the creation of the polynomial basis spline set
	 * 
	 *  	USE WITH CARE: This sample ignores errors and does not handle exceptions.
	 */

	private static final AbstractUnivariate[] CreatePolynomialSpline()
		throws Exception
	{
		int iNumBasis = 4;

		/*
		 * Create the basis parameter set from the number of basis functions, and construct the basis
		 */

		PolynomialBasisSetParams polybsbp = new PolynomialBasisSetParams (iNumBasis);

		return SegmentBasisSetBuilder.PolynomialBasisSet (polybsbp);
	}

	/*
	 * Sample demonstrating the creation of the Bernstein polynomial basis spline set
	 * 
	 *  	USE WITH CARE: This sample ignores errors and does not handle exceptions.
	 */

	private static final AbstractUnivariate[] CreateBernsteinPolynomialSpline()
		throws Exception
	{
		int iNumBasis = 4;

		/*
		 * Create the basis parameter set from the number of basis functions, and construct the basis
		 */

		PolynomialBasisSetParams polybsbp = new PolynomialBasisSetParams (iNumBasis);

		return SegmentBasisSetBuilder.BernsteinPolynomialBasisSet (polybsbp);
	}

	/*
	 * Sample demonstrating the creation of the exponential tension basis spline set
	 * 
	 *  	USE WITH CARE: This sample ignores errors and does not handle exceptions.
	 */

	private static final AbstractUnivariate[] CreateExponentialTensionSpline()
		throws Exception
	{
		double dblTension = 1.;

		/*
		 * Create the basis parameter set from the segment tension parameter, and construct the basis
		 */

		ExponentialTensionBasisSetParams etbsbp = new ExponentialTensionBasisSetParams (dblTension);

		return SegmentBasisSetBuilder.ExponentialTensionBasisSet (etbsbp);
	}

	/*
	 * Sample demonstrating the creation of the hyperbolic tension basis spline set
	 * 
	 *  	USE WITH CARE: This sample ignores errors and does not handle exceptions.
	 */

	private static final AbstractUnivariate[] CreateHyperbolicTensionSpline()
		throws Exception
	{
		double dblTension = 1.;

		/*
		 * Create the basis parameter set from the segment tension parameter, and construct the basis
		 */

		ExponentialTensionBasisSetParams etbsbp = new ExponentialTensionBasisSetParams (dblTension);

		return SegmentBasisSetBuilder.HyperbolicTensionBasisSet (etbsbp);
	}

	/*
	 * Sample demonstrating the creation of the Kaklis Pandelis basis spline set
	 * 
	 *  	USE WITH CARE: This sample ignores errors and does not handle exceptions.
	 */

	private static final AbstractUnivariate[] CreateKaklisPandelisSpline()
		throws Exception
	{
		int iPolynomialTensionDegree = 2;

		/*
		 * Create the basis parameter set from the segment polynomial tension control, and construct the basis
		 */

		KaklisPandelisBasisSetParams kpbpsp = new KaklisPandelisBasisSetParams (iPolynomialTensionDegree);

		return SegmentBasisSetBuilder.KaklisPandelisBasisSet (kpbpsp);
	}

	/*
	 * This sample demonstrates the following:
	 * 
	 * 	- Construction of two segments, 1 and 2.
	 *  - Calibration of the segments to the left and the right node values
	 *  - Extraction of the segment Jacobians and segment monotonicity
	 *  - Interpolate point value and the Jacobian
	 * 
	 *  	USE WITH CARE: This sample ignores errors and does not handle exceptions.
	 */

	private static final void TestSpline (
		final AbstractUnivariate[] aAU,
		final AbstractUnivariate rsc,
		final SegmentInelasticParams segParams)
		throws Exception
	{
		/*
		 * Construct the left and the right segments
		 */

		Segment seg1 = SegmentBasisSetBuilder.CreateCk (1.0, 1.5, aAU, rsc, segParams);

		Segment seg2 = SegmentBasisSetBuilder.CreateCk (1.5, 2.0, aAU, rsc, segParams);

		/*
		 * Calibrate the left segment using the node values, and compute the segment Jacobian
		 */

		WengertJacobian wj1 = seg1.calibrateJacobian (25., 0., 20.25);

		System.out.println ("\tY[" + 1.0 + "]: " + seg1.calcValue (1.));

		System.out.println ("\tY[" + 1.5 + "]: " + seg1.calcValue (1.5));

		System.out.println ("Segment 1 Jacobian: " + wj1.displayString());

		System.out.println ("Segment 1 Head: " + seg1.calcJacobian().displayString());

		System.out.println ("Segment 1 Monotone Type: " + seg1.monotoneType());

		/*
		 * Calibrate the right segment using the node values, and compute the segment Jacobian
		 */

		WengertJacobian wj2 = seg2.calibrateJacobian (seg1, 16.);

		System.out.println ("\tY[" + 1.5 + "]: " + seg2.calcValue (1.5));

		System.out.println ("\tY[" + 2. + "]: " + seg2.calcValue (2.));

		System.out.println ("Segment 2 Jacobian: " + wj2.displayString());

		System.out.println ("Segment 2 Regular Jacobian: " + seg2.calcJacobian().displayString());

		System.out.println ("Segment 2 Monotone Type: " + seg2.monotoneType());

		seg2.calibrate (seg1, 14.);

		/*
		 * Interpolate the segment value at the given variate, and compute the corresponding Jacobian
		 */

		double dblX = 2.0;

		System.out.println ("\t\tValue[" + dblX + "]: " + seg2.calcValue (dblX));

		System.out.println ("\t\tValue Jacobian[" + dblX + "]: " + seg2.calcValueJacobian (dblX).displayString());
	}

	/*
	 * This sample demonstrates the following specifically for the C1 Hermite Splines, which are calibrated
	 *  using left and right node values, along with their derivatives:
	 * 
	 * 	- Construction of two segments, 1 and 2.
	 *  - Calibration of the segments to the left and the right node values
	 *  - Extraction of the segment Jacobians and segment monotonicity
	 *  - Interpolate point value and the Jacobian
	 * 
	 *  	USE WITH CARE: This sample ignores errors and does not handle exceptions.
	 */

	private static final void TestC1HermiteSpline (
		final AbstractUnivariate[] aAU,
		final AbstractUnivariate rsc,
		final SegmentInelasticParams segParams)
		throws Exception
	{
		/*
		 * Construct the left and the right segments
		 */

		Segment seg1 = SegmentBasisSetBuilder.CreateCk (0.0, 1.0, aAU, rsc, segParams);

		Segment seg2 = SegmentBasisSetBuilder.CreateCk (1.0, 2.0, aAU, rsc, segParams);

		/*
		 * Calibrate the left segment using the node values, and compute the segment Jacobian
		 */

		WengertJacobian wj1 = seg1.calibrateJacobian (
			new SegmentEdgeParams (1., new double[] {1.}), // SEP Left
			new SegmentEdgeParams (4., new double[] {6.}), // SEP Right
			false); // Non-local Calibration

		System.out.println ("\tY[" + 0.0 + "]: " + seg1.calcValue (0.0));

		System.out.println ("\tY[" + 1.0 + "]: " + seg1.calcValue (1.0));

		System.out.println ("Segment 1 Jacobian: " + wj1.displayString());

		System.out.println ("Segment 1 Head: " + seg1.calcJacobian().displayString());

		System.out.println ("Segment 1 Monotone Type: " + seg1.monotoneType());

		/*
		 * Calibrate the right segment using the node values, and compute the segment Jacobian
		 */

		WengertJacobian wj2 = seg2.calibrateJacobian (
			new SegmentEdgeParams (4., new double[] {6.}), // SEP Left
			new SegmentEdgeParams (15., new double[] {17.}), // SEP Right
			false); // Non-local Calibration

		System.out.println ("\tY[" + 1.0 + "]: " + seg2.calcValue (1.0));

		System.out.println ("\tY[" + 2.0 + "]: " + seg2.calcValue (2.0));

		System.out.println ("Segment 2 Jacobian: " + wj2.displayString());

		System.out.println ("Segment 2 Regular Jacobian: " + seg2.calcJacobian().displayString());

		System.out.println ("Segment 2 Monotone Type: " + seg2.monotoneType());

		seg2.calibrate (seg1, 14.);

		/*
		 * Interpolate the segment value at the given variate, and compute the corresponding Jacobian
		 */

		double dblX = 2.0;

		System.out.println ("\t\tValue[" + dblX + "]: " + seg2.calcValue (dblX));

		System.out.println ("\t\tValue Jacobian[" + dblX + "]: " + seg2.calcValueJacobian (dblX).displayString());
	}

	public static final void main (
		final String[] astrArgs)
		throws Exception
	{
		/*
		 * Construct a rational shape controller with the shape controller tension of 1.
		 */

		double dblShapeControllerTension = 1.;

		AbstractUnivariate rsc = new RationalShapeControl (dblShapeControllerTension);

		/*
		 * Construct the segment inelastic parameter that is C2 (iK = 2 sets it to C2), with second order
		 * 	roughness penalty, and without constraint
		 */

		int iK = 2;
		int iRoughnessPenaltyDerivativeOrder = 2;

		SegmentInelasticParams segParams = new SegmentInelasticParams (iK, iRoughnessPenaltyDerivativeOrder, null);

		/*
		 * Test the polynomial spline
		 */

		System.out.println (" ---------- \n POLYNOMIAL \n ---------- \n");

		TestSpline (CreatePolynomialSpline(), rsc, segParams);

		/*
		 * Test the Bernstein polynomial spline
		 */

		System.out.println (" -------------------- \n BERNSTEINPOLYNOMIAL \n -------------------- \n");

		TestSpline (CreateBernsteinPolynomialSpline(), rsc, segParams);

		/*
		 * Test the exponential tension spline
		 */

		System.out.println ( " ----------- \n EXPONENTIAL \n ----------- \n");

		TestSpline (CreateExponentialTensionSpline(), rsc, segParams);

		/*
		 * Test the hyperbolic tension spline
		 */

		System.out.println (" ---------- \n HYPERBOLIC \n ---------- \n");

		TestSpline (CreateHyperbolicTensionSpline(), rsc, segParams);

		/*
		 * Test the Kaklis-Pandelis spline
		 */

		System.out.println (" -------------------- \n KAKLISPANDELIS \n -------------------- \n");

		TestSpline (CreateKaklisPandelisSpline(), rsc, segParams);

		/*
		 * Test the C1 Hermite spline
		 */

		System.out.println (" -------------------- \n C1 HERMITE \n -------------------- \n");

		TestC1HermiteSpline (CreatePolynomialSpline(), rsc, new SegmentInelasticParams (1, iRoughnessPenaltyDerivativeOrder, null));
	}
}
