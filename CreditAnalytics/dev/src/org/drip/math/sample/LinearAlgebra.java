
package org.drip.math.sample;

import org.drip.math.common.*;
import org.drip.math.linearalgebra.*;

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
 * LinearAlgebra implements Samples for Linear Algebra and Matrix Manipulations. It demonstrates the
 * 	following:
 * 	- Compute the inverse of a matrix, and multiply with the original to recover the unit matrix
 * 	- Solves system of linear equations using one the exposed techniques
 *
 * @author Lakshmi Krishnamurthy
 */

public class LinearAlgebra {

	/*
	 * Sample illustrating the Invocation of Base Matrix Manipulation Functionality
	 * 
	 * 	WARNING: Insufficient Error Checking, so use caution
	 */

	public static final void MatrixManipulation()
	{
		double[][] aadblA = new double[][] {{1, 2, 3}, {4, 5, 6}, {7, 8, 9.01}};

		double[][] aadblAInv = Matrix.InvertUsingGaussianElimination (aadblA);

		NumberUtil.Print2DArray ("AINV", aadblAInv, false);

		double[][] aadblProduct = Matrix.Product (aadblA, aadblAInv);

		NumberUtil.Print2DArray ("PROD", aadblProduct, false);
	}

	/*
	 * Sample illustrating the Invocation of Linear System Solver Functionality
	 * 
	 * 	WARNING: Insufficient Error Checking, so use caution
	 */

	public static final void LinearSystemSolver()
	{
		/* double[][] aadblA = new double[][] {{1., 2., 3.}, {2., 3., 4.}, {3., 5., 8.}};
		double[] adblB = new double[] {63., 19., 121.};
		double[][] aadblA = new double[][] {{0., 2., 0.}, {5., 0., 0.}, {0., 0., 1.}};
		double[] adblB = new double[] {14., 5., 22. };
		double[][] aadblA = new double[][] {{5., 1., 1.}, {1., 5., 1.}, {1., 1., 5.}};
		double[] adblB = new double[] {12., 17., 22.}; */
		double[][] aadblA = new double[][] {{1., 0., 0., 0.}, {1., 1., 2.72, 0.37}, {0., 0., 3.19, 0.37}, {0., 0., 1., 0.43}};
		double[] adblB = new double[] {1., 1., 7., 0.};

		/*
		 * Solve the Linear System using Gaussian Elimination
		 */

		LinearizationOutput lssGaussianElimination = LinearSystemSolver.SolveUsingGaussianElimination (aadblA, adblB);

		for (int i = 0; i < lssGaussianElimination.getTransformedRHS().length; ++i)
			System.out.println ("GaussianElimination[" + i + "] = " + FormatUtil.FormatDouble
				(lssGaussianElimination.getTransformedRHS()[i], 0, 2, 1.));

		/*
		 * Solve the Linear System using the Gauss-Seidel method
		 */

		/* LinearSystemSolution lssGaussSeidel = LinearSystemSolver.SolveUsingGaussSeidel (aadblA, adblB);

		for (int i = 0; i < lssGaussSeidel.getSolution().length; ++i)
			System.out.println ("GaussSeidel[" + i + "] = " + FormatUtil.FormatDouble (lssGaussSeidel.getSolution()[i], 0, 2, 1.)); */
	}

	public static final void main (
		final String[] astrArgs)
	{
		MatrixManipulation();

		LinearSystemSolver();
	}
}
