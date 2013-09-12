
package org.drip.regression.spline;

/*
 * -*- mode: java; tab-width: 4; indent-tabs-mode: nil; c-basic-offset: 4 -*-
 */

/*!
 * Copyright (C) 2013 Lakshmi Krishnamurthy
 * Copyright (C) 2012 Lakshmi Krishnamurthy
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
 * BasisSplineRegressorSet carries out regression testing for the following series of basis splines:
 * - #1: Polynomial Basis Spline, n = 2 basis functions, and Ck = 0.
 * - #2: Polynomial Basis Spline, n = 3 basis functions, and Ck = 1.
 * - #3: Polynomial Basis Spline, n = 4 basis functions, and Ck = 1.
 * - #4: Polynomial Basis Spline, n = 4 basis functions, and Ck = 2.
 * - #5: Polynomial Basis Spline, n = 5 basis functions, and Ck = 1.
 * - #6: Polynomial Basis Spline, n = 5 basis functions, and Ck = 2.
 * - #7: Polynomial Basis Spline, n = 5 basis functions, and Ck = 3.
 * - #8: Polynomial Basis Spline, n = 6 basis functions, and Ck = 1.
 * - #9: Polynomial Basis Spline, n = 6 basis functions, and Ck = 2.
 * - #10: Polynomial Basis Spline, n = 6 basis functions, and Ck = 3.
 * - #11: Polynomial Basis Spline, n = 6 basis functions, and Ck = 4.
 * - #12: Polynomial Basis Spline, n = 7 basis functions, and Ck = 1.
 * - #13: Polynomial Basis Spline, n = 7 basis functions, and Ck = 2.
 * - #14: Polynomial Basis Spline, n = 7 basis functions, and Ck = 3.
 * - #15: Polynomial Basis Spline, n = 7 basis functions, and Ck = 4.
 * - #16: Polynomial Basis Spline, n = 7 basis functions, and Ck = 5.
 * - #17: Bernstein Polynomial Basis Spline, n = 4 basis functions, and Ck = 2.
 * - #18: Exponential Tension Spline, n = 4 basis functions, Tension = 1., and Ck = 2.
 * - #19: Hyperbolic Tension Spline, n = 4 basis functions, Tension = 1., and Ck = 2.
 * - #20: Kaklis-Pandelis Tension Spline, n = 4 basis functions, KP = 2, and Ck = 2.
 * - #21: C1 Hermite Local Spline, n = 4 basis functions, and Ck = 1.
 * - #21: Hermite Local Spline with Local, Catmull-Rom, and Cardinal Knots, n = 4 basis functions, and Ck = 1.
 *
 * @author Lakshmi Krishnamurthy
 */

public class BasisSplineRegressorSet implements org.drip.regression.core.RegressorSet {
	private java.lang.String _strRegressionScenario = "PolynomialSplineRegressor";

	private java.util.List<org.drip.regression.core.UnitRegressor> _setRegressors = new
		java.util.ArrayList<org.drip.regression.core.UnitRegressor>();

	/**
	 * BasisSplineRegressorSet constructor - Creates the base spline parameter and initializes the
	 *	regression objects
	 */

	public BasisSplineRegressorSet()
		throws java.lang.Exception
	{
	}

	/*
	 * Setting up of the set of individual regressors
	 */

	@Override public boolean setupRegressors()
	{
		try {
			_setRegressors.add
				(org.drip.regression.spline.BasisSplineRegressor.CreatePolynomialSplineRegressor ("N2Ck0",
					_strRegressionScenario, 2, 0));

			_setRegressors.add
				(org.drip.regression.spline.BasisSplineRegressor.CreatePolynomialSplineRegressor ("N3Ck0",
					_strRegressionScenario, 3, 0));

			_setRegressors.add
				(org.drip.regression.spline.BasisSplineRegressor.CreatePolynomialSplineRegressor ("N3Ck1",
					_strRegressionScenario, 3, 1));

			_setRegressors.add
				(org.drip.regression.spline.BasisSplineRegressor.CreatePolynomialSplineRegressor ("N4Ck0",
					_strRegressionScenario, 4, 0));

			_setRegressors.add
				(org.drip.regression.spline.BasisSplineRegressor.CreatePolynomialSplineRegressor ("N4Ck1",
					_strRegressionScenario, 4, 1));

			_setRegressors.add
				(org.drip.regression.spline.BasisSplineRegressor.CreatePolynomialSplineRegressor ("N4Ck2",
					_strRegressionScenario, 4, 2));

			_setRegressors.add
				(org.drip.regression.spline.BasisSplineRegressor.CreatePolynomialSplineRegressor ("N5Ck0",
					_strRegressionScenario, 5, 0));

			_setRegressors.add
				(org.drip.regression.spline.BasisSplineRegressor.CreatePolynomialSplineRegressor ("N5Ck1",
					_strRegressionScenario, 5, 1));

			_setRegressors.add
				(org.drip.regression.spline.BasisSplineRegressor.CreatePolynomialSplineRegressor ("N5Ck2",
					_strRegressionScenario, 5, 2));

			_setRegressors.add
				(org.drip.regression.spline.BasisSplineRegressor.CreatePolynomialSplineRegressor ("N5Ck3",
					_strRegressionScenario, 5, 3));

			_setRegressors.add
				(org.drip.regression.spline.BasisSplineRegressor.CreatePolynomialSplineRegressor ("N6Ck0",
					_strRegressionScenario, 6, 0));

			_setRegressors.add
				(org.drip.regression.spline.BasisSplineRegressor.CreatePolynomialSplineRegressor ("N6Ck1",
					_strRegressionScenario, 6, 1));

			_setRegressors.add
				(org.drip.regression.spline.BasisSplineRegressor.CreatePolynomialSplineRegressor ("N6Ck2",
					_strRegressionScenario, 6, 2));

			_setRegressors.add
				(org.drip.regression.spline.BasisSplineRegressor.CreatePolynomialSplineRegressor ("N6Ck3",
					_strRegressionScenario, 6, 3));

			_setRegressors.add
				(org.drip.regression.spline.BasisSplineRegressor.CreatePolynomialSplineRegressor ("N6Ck4",
					_strRegressionScenario, 6, 4));

			_setRegressors.add
				(org.drip.regression.spline.BasisSplineRegressor.CreatePolynomialSplineRegressor ("N7Ck0",
					_strRegressionScenario, 7, 0));

			_setRegressors.add
				(org.drip.regression.spline.BasisSplineRegressor.CreatePolynomialSplineRegressor ("N7Ck1",
					_strRegressionScenario, 7, 1));

			_setRegressors.add
				(org.drip.regression.spline.BasisSplineRegressor.CreatePolynomialSplineRegressor ("N7Ck2",
					_strRegressionScenario, 7, 2));

			_setRegressors.add
				(org.drip.regression.spline.BasisSplineRegressor.CreatePolynomialSplineRegressor ("N7Ck3",
					_strRegressionScenario, 7, 3));

			_setRegressors.add
				(org.drip.regression.spline.BasisSplineRegressor.CreatePolynomialSplineRegressor ("N7Ck4",
					_strRegressionScenario, 7, 4));

			_setRegressors.add
				(org.drip.regression.spline.BasisSplineRegressor.CreatePolynomialSplineRegressor ("N7Ck5",
					_strRegressionScenario, 7, 5));

			_setRegressors.add
				(org.drip.regression.spline.BasisSplineRegressor.CreateExponentialTensionSplineRegressor
					("ExpTension", _strRegressionScenario, 1.));

			_setRegressors.add
				(org.drip.regression.spline.BasisSplineRegressor.CreateHyperbolicTensionSplineRegressor
					("HyperTension", _strRegressionScenario, 1.));

			_setRegressors.add
				(org.drip.regression.spline.BasisSplineRegressor.CreateKaklisPandelisSplineRegressor ("KP",
					_strRegressionScenario, 2));

			_setRegressors.add
				(org.drip.regression.spline.BasisSplineRegressor.CreateBernsteinPolynomialSplineRegressor
					("Bern_N4Ck2", _strRegressionScenario, 4, 2));

			_setRegressors.add
				(org.drip.regression.spline.HermiteBasisSplineRegressor.CreateHermiteSplineRegressor
					("Hermite_N4Ck1", _strRegressionScenario, 4, 1));

			_setRegressors.add (new org.drip.regression.spline.LocalControlBasisSplineRegressor
				("Hermite_CatmullRom_Cardinal_N4Ck1", _strRegressionScenario,
					org.drip.math.grid.RegimeBuilder.BASIS_SPLINE_POLYNOMIAL, new
						org.drip.math.spline.PolynomialBasisSetParams (4), 1));

			_setRegressors.add (new org.drip.regression.spline.LagrangePolynomialSpanRegressor
				("Lagrange_Polynomial_Span", _strRegressionScenario));
		} catch (java.lang.Exception e) {
			e.printStackTrace();

			return false;
		}

		return true;
	}

	@Override public java.util.List<org.drip.regression.core.UnitRegressor> getRegressorSet()
	{
		return _setRegressors;
	}

	@Override public java.lang.String getSetName()
	{
		return _strRegressionScenario;
	}
}
