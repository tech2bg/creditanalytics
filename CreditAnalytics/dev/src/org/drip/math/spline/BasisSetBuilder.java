
package org.drip.math.spline;

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
 * This class implements the basis set and spline builder for the following types of splines:
 * 
 * 	- Exponential basis tension splines
 * 	- Hyperbolic basis tension splines
 * 	- Polynomial basis splines
 *  - Bernstein Polynomial basis splines
 *  - Kaklis Pandelis basis tension splines
 * 
 * This elastic coefficients for the segment using Ck basis splines inside [0,...,1) - Globally
 *  [x_0,...,x_1) are extracted for:
 * 
 * 			y = Estimator (Ck, x) * ShapeControl (x)
 * 
 *		where x is the normalized ordinate mapped as
 * 
 * 			x => (x - x_i-1) / (x_i - x_i-1)
 * 
 * The inverse quadratic/rational spline is a typical shape controller spline used.
 * 
 * @author Lakshmi Krishnamurthy
 */

public class BasisSetBuilder {

	/**
	 * This function implements the elastic coefficients for the segment using tension exponential basis
	 * 	splines inside - [0,...,1) - Globally [x_0,...,x_1). The segment equation is
	 * 
	 * 		y = A + B * x + C * exp (Tension * x / (x_i - x_i-1)) + D * exp (-Tension * x / (x_i - x_i-1))
	 * 
	 *	where x is the normalized ordinate mapped as
	 * 
	 * 		x => (x - x_i-1) / (x_i - x_i-1)
	 * 
	 * @param etbsbp Exponential Tension Basis set Builder Parameters
	 * 
	 * @return Exponential Tension Basis Functions
	 */

	public static final org.drip.math.function.AbstractUnivariate[] ExponentialTensionBasisSet (
		final org.drip.math.spline.ExponentialTensionBasisSetParams etbsbp)
	{
		if (null == etbsbp) return null;

		double dblTension = etbsbp.tension();

		try {
			return new org.drip.math.function.AbstractUnivariate[] {new org.drip.math.function.Polynomial
				(0), new org.drip.math.function.Polynomial (1), new
					org.drip.math.function.ExponentialTension (java.lang.Math.E, dblTension), new
						org.drip.math.function.ExponentialTension (java.lang.Math.E, -dblTension)};
		} catch (java.lang.Exception e) {
			e.printStackTrace();
		}

		return null;
	}

	/**
	 * This function implements the elastic coefficients for the segment using tension hyperbolic basis
	 * 	splines inside - [0,...,1) - Globally [x_0,...,x_1). The segment equation is
	 * 
	 * 		y = A + B * x + C * sinh (Tension * x / (x_i - x_i-1)) + D * cosh (Tension * x / (x_i - x_i-1))
	 * 
	 *	where x is the normalized ordinate mapped as
	 * 
	 * 		x => (x - x_i-1) / (x_i - x_i-1)
	 * 
	 * @param etbsbp Exponential Tension Basis set Builder Parameters
	 * 
	 * @return Hyperbolic Tension Basis Set
	 */

	public static final org.drip.math.function.AbstractUnivariate[] HyperbolicTensionBasisSet (
		final org.drip.math.spline.ExponentialTensionBasisSetParams etbsbp)
	{
		if (null == etbsbp) return null;

		double dblTension = etbsbp.tension();

		try {
			return new org.drip.math.function.AbstractUnivariate[] {new org.drip.math.function.Polynomial
				(0), new org.drip.math.function.Polynomial (1), new org.drip.math.function.HyperbolicTension
					(org.drip.math.function.HyperbolicTension.COSH, dblTension), new
						org.drip.math.function.HyperbolicTension
							(org.drip.math.function.HyperbolicTension.SINH, dblTension)};
		} catch (java.lang.Exception e) {
			e.printStackTrace();
		}

		return null;
	}

	/**
	 * This function implements the elastic coefficients for the segment using polynomial basis splines
	 * 		inside [0,...,1) - Globally [x_0,...,x_1):
	 * 
	 * 			y = Sum (A_i*x^i) i = 0,...,n (0 and n inclusive)
	 * 
	 *		where x is the normalized ordinate mapped as
	 * 
	 * 			x => (x - x_i-1) / (x_i - x_i-1)
	 * 
	 * @param polybsbp Polynomial Basis set Builder Parameters
	 * 
	 * @return The Polynomial Basis Spline Set
	 */

	public static final org.drip.math.function.AbstractUnivariate[] PolynomialBasisSet (
		final org.drip.math.spline.PolynomialBasisSetParams polybsbp)
	{
		if (null == polybsbp) return null;

		int iNumBasis = polybsbp.numBasis();

		org.drip.math.function.AbstractUnivariate[] aAU = new
			org.drip.math.function.AbstractUnivariate[iNumBasis];

		try {
			for (int i = 0; i < iNumBasis; ++i)
				aAU[i] = new org.drip.math.function.Polynomial (i);

			return aAU;
		} catch (java.lang.Exception e) {
			e.printStackTrace();
		}

		return null;
	}

	/**
	 * This function implements the elastic coefficients for the segment using Bernstein polynomial basis
	 * 	splines inside - [0,...,1) - Globally [x_0,...,x_1):
	 * 
	 * 			y = Sum (A_i*B^i(x)) i = 0,...,n (0 and n inclusive)
	 * 
	 *		where x is the normalized ordinate mapped as
	 * 
	 * 			x => (x - x_i-1) / (x_i - x_i-1)
	 * 
	 * 		and B^i(x) is the Bernstein basis polynomial of order i.
	 * 
	 * @param polybsbp Polynomial Basis set Builder Parameters
	 * 
	 * @return The Bernstein polynomial basis
	 */

	public static final org.drip.math.function.AbstractUnivariate[] BernsteinPolynomialBasisSet (
		final org.drip.math.spline.PolynomialBasisSetParams polybsbp)
	{
		if (null == polybsbp) return null;

		int iNumBasis = polybsbp.numBasis();

		org.drip.math.function.AbstractUnivariate[] aAU = new
			org.drip.math.function.AbstractUnivariate[iNumBasis];

		try {
			for (int i = 0; i < iNumBasis; ++i)
				aAU[i] = new org.drip.math.function.BernsteinPolynomial (i, iNumBasis - 1 - i);

			return aAU;
		} catch (java.lang.Exception e) {
			e.printStackTrace();
		}

		return null;
	}

	/**
	 * Construct KaklisPandelis from the polynomial tension basis function set
	 * 
	 * 		y = A * (1-x) + B * x + C * x * (1-x)^m + D * x^m * (1-x)
	 * 
	 * @param kpbsbp Kaklis Pandelis Basis set Builder Parameters
	 * 
	 * @return The KaklisPandelis Basis Set
	 */

	public static final org.drip.math.function.AbstractUnivariate[] KaklisPandelisBasisSet (
		final org.drip.math.spline.KaklisPandelisBasisSetParams kpbsbp)
	{
		if (null == kpbsbp) return null;

		try {
			org.drip.math.function.AbstractUnivariate auLinearPoly = new org.drip.math.function.Polynomial
				(1);

			org.drip.math.function.AbstractUnivariate auReflectedLinearPoly = new
				org.drip.math.function.UnivariateReflection (auLinearPoly);

			org.drip.math.function.AbstractUnivariate auKaklisPandelisPolynomial = new
				org.drip.math.function.Polynomial (kpbsbp.polynomialTensionDegree());

			return new org.drip.math.function.AbstractUnivariate[] {auReflectedLinearPoly, auLinearPoly, new
				org.drip.math.function.UnivariateConvolution (auLinearPoly, new
					org.drip.math.function.UnivariateReflection (auKaklisPandelisPolynomial)), new
						org.drip.math.function.UnivariateConvolution (auKaklisPandelisPolynomial,
							auReflectedLinearPoly)};
		} catch (java.lang.Exception e) {
			e.printStackTrace();
		}

		return null;
	}
}
