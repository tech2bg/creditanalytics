
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

			@Override public double calcDerivative (
				final double dblX,
				final int iOrder)
				throws java.lang.Exception
			{
				if (!org.drip.quant.common.NumberUtil.IsValid (dblX))
					throw new java.lang.Exception
						("KLKF::FromHyperbolicPrimitive.Phy::calcDerivative => Invalid Inputs!");

				double dblTension = etsp.tension();

				if (1 == iOrder)
					return (java.lang.Math.cosh (dblTension * dblX) - 1.) / (dblTension * java.lang.Math.sinh
						(dblTension));

				if (2 == iOrder)
					return java.lang.Math.sinh (dblTension * dblX) / java.lang.Math.sinh (dblTension);

				return calcDerivative (dblX, iOrder);
			}

			@Override public double integrate (
				final double dblBegin,
				final double dblEnd)
				throws java.lang.Exception
			{
				if (!org.drip.quant.common.NumberUtil.IsValid (dblBegin) ||
					!org.drip.quant.common.NumberUtil.IsValid (dblEnd))
					throw new java.lang.Exception
						("KLKF::FromHyperbolicPrimitive.Phy::integrate => Invalid Inputs");

				double dblTension = etsp.tension();

				return (java.lang.Math.cosh (dblTension * dblEnd) - java.lang.Math.cosh (dblTension *
					dblBegin) - 0.5 * dblTension * (dblEnd * dblEnd - dblBegin * dblBegin)) / (dblTension *
						dblTension * dblTension * java.lang.Math.sinh (dblTension));
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

			@Override public double calcDerivative (
				final double dblX,
				final int iOrder)
				throws java.lang.Exception
			{
				if (!org.drip.quant.common.NumberUtil.IsValid (dblX))
					throw new java.lang.Exception
						("KLKF::FromHyperbolicPrimitive.Psy::calcDerivative => Invalid Inputs!");

				double dblTension = etsp.tension();

				if (1 == iOrder)
					return (1. - java.lang.Math.cosh (dblTension * (1. - dblX))) / (dblTension *
						java.lang.Math.cosh (dblTension));

				if (2 == iOrder)
					return java.lang.Math.sinh (dblTension * (1. - dblX)) / java.lang.Math.sinh (dblTension);

				return calcDerivative (dblX, iOrder);
			}

			@Override public double integrate (
				final double dblBegin,
				final double dblEnd)
				throws java.lang.Exception
			{
				if (!org.drip.quant.common.NumberUtil.IsValid (dblBegin) ||
					!org.drip.quant.common.NumberUtil.IsValid (dblEnd))
					throw new java.lang.Exception
						("KLKF::FromHyperbolicPrimitive.Psy::integrate => Invalid Inputs");

				double dblTension = etsp.tension();

				return -1. * (java.lang.Math.sinh (dblTension * (1. - dblEnd)) - java.lang.Math.sinh 
					(dblTension * (1. - dblBegin)) - 0.5 * dblTension * ((1. - dblEnd) * (1. - dblEnd) - (1.
						- dblBegin) * (1. - dblBegin))) / (dblTension * dblTension * dblTension *
							java.lang.Math.sinh (dblTension));
			}
		};

		try {
			return new CkBasisFunctionSet (2, etsp.tension(), new
				org.drip.quant.function1D.AbstractUnivariate[] {auPhy, auPsy});
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

			@Override public double calcDerivative (
				final double dblX,
				final int iOrder)
				throws java.lang.Exception
			{
				if (!org.drip.quant.common.NumberUtil.IsValid (dblX))
					throw new java.lang.Exception
						("KLKF::FromRationalLinearPrimitive.Phy::calcDerivative => Invalid Inputs!");

				double dblTension = etsp.tension();

				if (1 == iOrder) {
					double dblDLDX = -1. * dblTension;
					double dblL = 1. + dblTension * (1. - dblX);

					return 1. / (dblL * dblL * (6. + 8. * dblTension)) * (3. * dblL * dblX * dblX - dblDLDX *
						dblX * dblX * dblX);
				}

				if (2 == iOrder) {
					double dblD2LDX2 = 0.;
					double dblDLDX = -1. * dblTension;
					double dblL = 1. + dblTension * (1. - dblX);

					return 1. / (dblL * dblL * (6. + 8. * dblTension)) * (6. * dblL * dblX - dblD2LDX2 * dblX
						* dblX * dblX) - 2. / (dblL * dblL * dblL * (6. + 8. * dblTension)) *
							(3. * dblL * dblX * dblX - dblDLDX * dblX * dblX * dblX);
				}

				return calcDerivative (dblX, iOrder);
			}

			@Override public double integrate (
				final double dblBegin,
				final double dblEnd)
				throws java.lang.Exception
			{
				return org.drip.quant.calculus.Integrator.Boole (this, dblBegin, dblEnd);
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

			@Override public double calcDerivative (
				final double dblX,
				final int iOrder)
				throws java.lang.Exception
			{
				if (!org.drip.quant.common.NumberUtil.IsValid (dblX))
					throw new java.lang.Exception
						("KLKF::FromRationalLinearPrimitive.Psy::calcDerivative => Invalid Inputs!");

				double dblTension = etsp.tension();

				if (1 == iOrder) {
					double dblDLDX = dblTension;
					double dblL = 1. + dblTension * dblX;

					return -1. / (dblL * dblL * (6. + 8. * dblTension)) * (3. * dblL * (1. - dblX) *
						(1. - dblX) + dblDLDX * (1. - dblX) * (1. - dblX) * (1. - dblX));
				}

				if (2 == iOrder) {
					double dblD2LDX2 = 0.;
					double dblDLDX = dblTension;
					double dblL = 1. + dblTension * dblX;

					return 1. / (dblL * dblL * (6. + 8. * dblTension)) * (6. * dblL * (1. - dblX) - dblD2LDX2
						* (1. - dblX) * (1. - dblX) * (1. - dblX)) - 2. / (dblL * dblL * dblL *
							(6. + 8. * dblTension)) * (3. * dblL * (1. - dblX) * (1. - dblX) + dblDLDX *
								(1. - dblX) * (1. - dblX) * (1. - dblX));
				}

				return calcDerivative (dblX, iOrder);
			}

			@Override public double integrate (
				final double dblBegin,
				final double dblEnd)
				throws java.lang.Exception
			{
				return org.drip.quant.calculus.Integrator.Boole (this, dblBegin, dblEnd);
			}
		};

		try {
			return new CkBasisFunctionSet (2, etsp.tension(), new
				org.drip.quant.function1D.AbstractUnivariate[] {auPhy, auPsy});
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

			@Override public double calcDerivative (
				final double dblX,
				final int iOrder)
				throws java.lang.Exception
			{
				if (!org.drip.quant.common.NumberUtil.IsValid (dblX))
					throw new java.lang.Exception
						("KLKF::FromRationalQuadraticPrimitive.Phy::calcDerivative => Invalid Inputs!");

				double dblTension = etsp.tension();

				if (1 == iOrder) {
					double dblDLDX = dblTension * (1. - 2. * dblX);
					double dblL = 1. + dblTension * dblX * (1. - dblX);

					return 1. / (dblL * dblL * (6. + 8. * dblTension)) * (3. * dblL * dblX * dblX - dblDLDX *
						dblX * dblX * dblX);
				}

				if (2 == iOrder) {
					double dblD2LDX2 = -2. * dblTension;
					double dblDLDX = dblTension * (1. - 2. * dblX);
					double dblL = 1. + dblTension * dblX * (1. - dblX);

					return 1. / (dblL * dblL * (6. + 8. * dblTension)) * (6. * dblL * dblX - dblD2LDX2 * dblX
						* dblX * dblX) - 2. / (dblL * dblL * dblL * (6. + 8. * dblTension)) *
							(3. * dblL * dblX * dblX - dblDLDX * dblX * dblX * dblX);
				}

				return calcDerivative (dblX, iOrder);
			}

			@Override public double integrate (
				final double dblBegin,
				final double dblEnd)
				throws java.lang.Exception
			{
				return org.drip.quant.calculus.Integrator.Boole (this, dblBegin, dblEnd);
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

			@Override public double calcDerivative (
				final double dblX,
				final int iOrder)
				throws java.lang.Exception
			{
				if (!org.drip.quant.common.NumberUtil.IsValid (dblX))
					throw new java.lang.Exception
						("KLKF::FromRationalQuadraticPrimitive.Psy::calcDerivative => Invalid Inputs!");

				double dblTension = etsp.tension();

				if (1 == iOrder) {
					double dblDLDX = dblTension * (1. - 2. * dblX);
					double dblL = 1. + dblTension * dblX * (1. - dblX);

					return -1. / (dblL * dblL * (6. + 8. * dblTension)) * (3. * dblL * (1. - dblX) *
						(1. - dblX) + dblDLDX * (1. - dblX) * (1. - dblX) * (1. - dblX));
				}

				if (2 == iOrder) {
					double dblD2LDX2 = -2. * dblTension * dblX;
					double dblDLDX = dblTension * (1. - 2. * dblX);
					double dblL = 1. + dblTension * dblX * (1. - dblX);

					return 1. / (dblL * dblL * (6. + 8. * dblTension)) * (6. * dblL * (1. - dblX) - dblD2LDX2
						* (1. - dblX) * (1. - dblX) * (1. - dblX)) - 2. / (dblL * dblL * dblL *
							(6. + 8. * dblTension)) * (3. * dblL * (1. - dblX) * (1. - dblX) + dblDLDX *
								(1. - dblX) * (1. - dblX) * (1. - dblX));
				}

				return calcDerivative (dblX, iOrder);
			}

			@Override public double integrate (
				final double dblBegin,
				final double dblEnd)
				throws java.lang.Exception
			{
				return org.drip.quant.calculus.Integrator.Boole (this, dblBegin, dblEnd);
			}
		};

		try {
			return new CkBasisFunctionSet (2, etsp.tension(), new
				org.drip.quant.function1D.AbstractUnivariate[] {auPhy, auPsy});
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

				return dblX * dblX * dblX * java.lang.Math.exp (-1. * dblTension * (1. - dblX)) / (6. + 7. *
					dblTension);
			}

			@Override public double calcDerivative (
				final double dblX,
				final int iOrder)
				throws java.lang.Exception
			{
				if (!org.drip.quant.common.NumberUtil.IsValid (dblX))
					throw new java.lang.Exception
						("KLKF::FromExponentialPrimitive.Phy::calcDerivative => Invalid Inputs!");

				double dblTension = etsp.tension();

				if (1 == iOrder)
					return (3. + dblTension * dblX) / (6. + 7. * dblTension) * dblX * dblX *
						java.lang.Math.exp (-1. * dblTension * (1. - dblX));

				if (2 == iOrder)
					return (dblTension * dblTension * dblX * dblX + 6. * dblTension * dblX + 6.) / (6. + 7. *
						dblTension) * dblX * java.lang.Math.exp (-1. * dblTension * (1. - dblX));

				return calcDerivative (dblX, iOrder);
			}

			@Override public double integrate (
				final double dblBegin,
				final double dblEnd)
				throws java.lang.Exception
			{
				return org.drip.quant.calculus.Integrator.Boole (this, dblBegin, dblEnd);
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

			@Override public double calcDerivative (
				final double dblX,
				final int iOrder)
				throws java.lang.Exception
			{
				if (!org.drip.quant.common.NumberUtil.IsValid (dblX))
					throw new java.lang.Exception
						("KLKF::FromExponentialPrimitive.Psy::calcDerivative => Invalid Inputs!");

				double dblTension = etsp.tension();

				if (1 == iOrder)
					return -1. * (3. + dblTension * (1. - dblX)) / (6. + 7. * dblTension) * (1. - dblX) *
						(1. - dblX) * java.lang.Math.exp (-1. * dblTension * dblX);

				if (2 == iOrder)
					return (dblTension * dblTension * (1. - dblX) * (1. - dblX) + 6. * dblTension *
						(1. - dblX) + 6.) / (6. + 7. * dblTension) * (1. - dblX) * java.lang.Math.exp (-1. *
							dblTension * dblX);

				return calcDerivative (dblX, iOrder);
			}

			@Override public double integrate (
				final double dblBegin,
				final double dblEnd)
				throws java.lang.Exception
			{
				return org.drip.quant.calculus.Integrator.Boole (this, dblBegin, dblEnd);
			}
		};

		try {
			return new CkBasisFunctionSet (2, etsp.tension(), new
				org.drip.quant.function1D.AbstractUnivariate[] {auPhy, auPsy});
		} catch (java.lang.Exception e) {
			e.printStackTrace();
		}

		return null;
	}
}
