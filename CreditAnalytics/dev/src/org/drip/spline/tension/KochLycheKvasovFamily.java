
package org.drip.spline.tension;

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
 * This class implements the basic framework and the family of C2 Tension Splines outlined in Koch and Lyche
 * 	(1989), Koch and Lyche (1993), and Kvasov (2000) Papers.
 * 
 * @author Lakshmi Krishnamurthy
 */

public class KochLycheKvasovFamily {

	/**
	 * Implement the Basis Function Set from the Hyperbolic Hat Primitive Set
	 * 
	 * @param etsp The Tension Function Set Parameters
	 * 
	 * @return Instance of the Basis Function Set
	 */

	public static final org.drip.spline.basis.FunctionSet FromHyperbolicPrimitive (
		final org.drip.spline.basis.ExponentialTensionSetParams etsp)
	{
		if (null == etsp) return null;

		org.drip.quant.function1D.AbstractUnivariate auPhy = new org.drip.quant.function1D.AbstractUnivariate
			(null) {
			@Override public double evaluate (
				final double dblX)
				throws java.lang.Exception
			{
				if (!org.drip.quant.common.NumberUtil.IsValid (dblX))
					throw new java.lang.Exception
						("KLKF::FromHyperbolicPrimitive.Phy::evaluate => Invalid Inputs!");

				double dblTension = etsp.tension();

				return (java.lang.Math.sinh (dblTension * dblX) - dblTension * dblX) / (dblTension *
					dblTension * java.lang.Math.sinh (dblTension));
			}
		};

		org.drip.quant.function1D.AbstractUnivariate auPsy = new org.drip.quant.function1D.AbstractUnivariate
			(null) {
			@Override public double evaluate (
				final double dblX)
				throws java.lang.Exception
			{
				if (!org.drip.quant.common.NumberUtil.IsValid (dblX))
					throw new java.lang.Exception
						("KLKF.Psy::FromHyperbolicPrimitive::evaluate => Invalid Inputs!");

				double dblTension = etsp.tension();

				return (java.lang.Math.sinh (dblTension * (1. - dblX)) - dblTension * (1. - dblX)) /
					(dblTension * dblTension * java.lang.Math.sinh (dblTension));
			}
		};

		try {
			return new C2BasisFunctionSet (etsp.tension(), new org.drip.quant.function1D.AbstractUnivariate[]
				{auPhy, auPsy});
		} catch (java.lang.Exception e) {
			e.printStackTrace();
		}

		return null;
	}

	/**
	 * Implement the Basis Function Set from the Cubic Polynomial Numerator and Linear Rational Denominator
	 * 
	 * @param etsp The Tension Function Set Parameters
	 * 
	 * @return Instance of the Basis Function Set
	 */

	public static final org.drip.spline.basis.FunctionSet FromRationalLinearPrimitive (
		final org.drip.spline.basis.ExponentialTensionSetParams etsp)
	{
		if (null == etsp) return null;

		org.drip.quant.function1D.AbstractUnivariate auPhy = new org.drip.quant.function1D.AbstractUnivariate
			(null) {
			@Override public double evaluate (
				final double dblX)
				throws java.lang.Exception
			{
				if (!org.drip.quant.common.NumberUtil.IsValid (dblX))
					throw new java.lang.Exception
						("KLKF::FromRationalLinearPrimitive.Phy::evaluate => Invalid Inputs!");

				double dblTension = etsp.tension();

				return dblX * dblX * dblX / (1. + dblTension * (1. - dblX)) / (6. + 8. * dblTension);
			}
		};

		org.drip.quant.function1D.AbstractUnivariate auPsy = new org.drip.quant.function1D.AbstractUnivariate
			(null) {
			@Override public double evaluate (
				final double dblX)
				throws java.lang.Exception
			{
				if (!org.drip.quant.common.NumberUtil.IsValid (dblX))
					throw new java.lang.Exception
						("KLKF::FromRationalLinearPrimitive.Psy::evaluate => Invalid Inputs!");

				double dblTension = etsp.tension();

				return (1. - dblX) * (1. - dblX) * (1. - dblX) / (1. + dblTension * dblX) / (6. + 8. *
					dblTension);
			}
		};

		try {
			return new C2BasisFunctionSet (etsp.tension(), new org.drip.quant.function1D.AbstractUnivariate[]
				{auPhy, auPsy});
		} catch (java.lang.Exception e) {
			e.printStackTrace();
		}

		return null;
	}

	/**
	 * Implement the Basis Function Set from the Cubic Polynomial Numerator and Quadratic Rational
	 *  Denominator
	 * 
	 * @param etsp The Tension Function Set Parameters
	 * 
	 * @return Instance of the Basis Function Set
	 */

	public static final org.drip.spline.basis.FunctionSet FromRationalQuadraticPrimitive (
		final org.drip.spline.basis.ExponentialTensionSetParams etsp)
	{
		if (null == etsp) return null;

		org.drip.quant.function1D.AbstractUnivariate auPhy = new org.drip.quant.function1D.AbstractUnivariate
			(null) {
			@Override public double evaluate (
				final double dblX)
				throws java.lang.Exception
			{
				if (!org.drip.quant.common.NumberUtil.IsValid (dblX))
					throw new java.lang.Exception
						("KLKF::FromRationalQuadraticPrimitive.Phy::evaluate => Invalid Inputs!");

				double dblTension = etsp.tension();

				return dblX * dblX * dblX / (1. + dblTension * dblX * (1. - dblX)) / (6. + 8. * dblTension);
			}
		};

		org.drip.quant.function1D.AbstractUnivariate auPsy = new org.drip.quant.function1D.AbstractUnivariate
			(null) {
			@Override public double evaluate (
				final double dblX)
				throws java.lang.Exception
			{
				if (!org.drip.quant.common.NumberUtil.IsValid (dblX))
					throw new java.lang.Exception
						("KLKF::FromRationalQuadraticPrimitive.Psy::evaluate => Invalid Inputs!");

				double dblTension = etsp.tension();

				return (1. - dblX) * (1. - dblX) * (1. - dblX) / (1. + dblTension * dblX * (1. - dblX)) / (6.
					+ 8. * dblTension);
			}
		};

		try {
			return new C2BasisFunctionSet (etsp.tension(), new org.drip.quant.function1D.AbstractUnivariate[]
				{auPhy, auPsy});
		} catch (java.lang.Exception e) {
			e.printStackTrace();
		}

		return null;
	}

	/**
	 * Implement the Basis Function Set from the Cubic Polynomial Numerator and Exponential Denominator
	 * 
	 * @param etsp The Tension Function Set Parameters
	 * 
	 * @return Instance of the Basis Function Set
	 */

	public static final org.drip.spline.basis.FunctionSet FromExponentialPrimitive (
		final org.drip.spline.basis.ExponentialTensionSetParams etsp)
	{
		if (null == etsp) return null;

		org.drip.quant.function1D.AbstractUnivariate auPhy = new org.drip.quant.function1D.AbstractUnivariate
			(null) {
			@Override public double evaluate (
				final double dblX)
				throws java.lang.Exception
			{
				if (!org.drip.quant.common.NumberUtil.IsValid (dblX))
					throw new java.lang.Exception
						("KLKF::FromExponentialPrimitive.Phy::evaluate => Invalid Inputs!");

				double dblTension = etsp.tension();

				return dblX * dblX * dblX * java.lang.Math.exp (-1. * dblTension * (1. - dblX))/ (6. + 7. *
					dblTension);
			}
		};

		org.drip.quant.function1D.AbstractUnivariate auPsy = new org.drip.quant.function1D.AbstractUnivariate
			(null) {
			@Override public double evaluate (
				final double dblX)
				throws java.lang.Exception
			{
				if (!org.drip.quant.common.NumberUtil.IsValid (dblX))
					throw new java.lang.Exception
						("KLKF::FromExponentialPrimitive.Psy::evaluate => Invalid Inputs!");

				double dblTension = etsp.tension();

				return (1. - dblX) * (1. - dblX) * (1. - dblX) * java.lang.Math.exp (-1. * dblTension * dblX)
					/ (6. + 7. * dblTension);
			}
		};

		try {
			return new C2BasisFunctionSet (etsp.tension(), new org.drip.quant.function1D.AbstractUnivariate[]
				{auPhy, auPsy});
		} catch (java.lang.Exception e) {
			e.printStackTrace();
		}

		return null;
	}
}
