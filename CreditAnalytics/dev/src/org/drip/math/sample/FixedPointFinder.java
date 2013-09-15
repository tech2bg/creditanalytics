
package org.drip.math.sample;

import org.drip.math.calculus.Differential;
import org.drip.math.common.*;
import org.drip.math.function.AbstractUnivariate;
import org.drip.math.solver1D.*;

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
 * FixedPointFinder contains a sample illustration of usage of the Root Finder Library. It demonstrates the
 * 	fixed point extraction using the following techniques:
 * 	- Newton-Raphson method
 * 	- Bisection Method
 * 	- False Position
 * 	- Quadratic Interpolation
 * 	- Inverse Quadratic Interpolation
 * 	- Ridder's method
 * 	- Brent's method
 * 	- Zheng's method
 *
 * @author Lakshmi Krishnamurthy
 */

public class FixedPointFinder {

	/*
	 * Sample illustrating the Invocation of the Newton-Raphson Open Method
	 * 
	 * 	WARNING: Insufficient Error Checking, so use caution
	 */

	private static final void InvokeNewton (
		final AbstractUnivariate func)
	{
		try {
			FixedPointFinderOutput fpop = new FixedPointFinderNewton (0., func).findRoot();

			System.out.println ("--------\nNEWTON START\n-------");

			if (null != fpop && fpop.containsRoot()) {
				System.out.println ("Root: " + FormatUtil.FormatDouble (fpop.getRoot(), 1, 4, 1.));

				System.out.println (fpop.displayString());
			} else
				System.out.println ("Root searched failed!");

			System.out.println ("--------\nNEWTON FINISH\n-------\n");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/*
	 * Sample illustrating the Invocation of the Bisection Bracketing Method
	 * 
	 * 	WARNING: Insufficient Error Checking, so use caution
	 */

	private static final void InvokeBisection (
		final AbstractUnivariate func)
	{
		try {
			FixedPointFinderOutput fpop = new FixedPointFinderBracketing (0., func, null,
				VariateIteratorPrimitive.BISECTION).findRoot();

			System.out.println ("--------\nBISECTION START\n-------");

			if (null != fpop && fpop.containsRoot()) {
				System.out.println ("Root: " + FormatUtil.FormatDouble (fpop.getRoot(), 1, 4, 1.));

				System.out.println (fpop.displayString());
			} else
				System.out.println ("Root searched failed!");

			System.out.println ("--------\nBISECTION FINISH\n-------\n");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/*
	 * Sample illustrating the Invocation of the False Position Method
	 * 
	 * 	WARNING: Insufficient Error Checking, so use caution
	 */

	private static final void InvokeFalsePosition (
		final AbstractUnivariate func)
	{
		try {
			FixedPointFinderOutput fpop = new FixedPointFinderBracketing (0., func, null,
				VariateIteratorPrimitive.FALSE_POSITION).findRoot();

			System.out.println ("--------\nFALSE POSITION START\n-------");

			if (null != fpop && fpop.containsRoot()) {
				System.out.println ("Root: " + FormatUtil.FormatDouble (fpop.getRoot(), 1, 4, 1.));

				System.out.println (fpop.displayString());
			} else
				System.out.println ("Root searched failed!");

			System.out.println ("--------\nFALSE POSITION FINISH\n-------\n");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/*
	 * Sample illustrating the Invocation of the Quadratic Interpolation Bracketing Method
	 * 
	 * 	WARNING: Insufficient Error Checking, so use caution
	 */

	private static final void InvokeQuadraticInterpolation (
		final AbstractUnivariate func)
	{
		try {
			FixedPointFinderOutput fpop = new FixedPointFinderBracketing (0., func, null,
				VariateIteratorPrimitive.QUADRATIC_INTERPOLATION).findRoot();

			System.out.println ("--------\nQUADRATIC INTERPOLATION START\n-------");

			if (null != fpop && fpop.containsRoot()) {
				System.out.println ("Root: " + FormatUtil.FormatDouble (fpop.getRoot(), 1, 4, 1.));

				System.out.println (fpop.displayString());
			} else
				System.out.println ("Root searched failed!");

			System.out.println ("--------\nQUADRATIC INTERPOLATION FINISH\n-------\n");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/*
	 * Sample illustrating the Invocation of the Inverse Quadratic Interpolation Bracketing Method
	 * 
	 * 	WARNING: Insufficient Error Checking, so use caution
	 */

	private static final void InvokeInverseQuadraticInterpolation (
		final AbstractUnivariate func)
	{
		try {
			FixedPointFinderOutput fpop = new FixedPointFinderBracketing (0., func, null,
				VariateIteratorPrimitive.INVERSE_QUADRATIC_INTERPOLATION).findRoot();

			System.out.println ("--------\nINVERSE QUADRATIC INTERPOLATION START\n-------");

			if (null != fpop && fpop.containsRoot()) {
				System.out.println ("Root: " + FormatUtil.FormatDouble (fpop.getRoot(), 1, 4, 1.));

				System.out.println (fpop.displayString());
			} else
				System.out.println ("Root searched failed!");

			System.out.println ("--------\nINVERSE QUADRATIC INTERPOLATION FINISH\n-------\n");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/*
	 * Sample illustrating the Invocation of the Ridder Bracketing Method
	 * 
	 * 	WARNING: Insufficient Error Checking, so use caution
	 */

	private static final void InvokeRidder (
		final AbstractUnivariate func)
	{
		try {
			FixedPointFinderOutput fpop = new FixedPointFinderBracketing (0., func, null,
				VariateIteratorPrimitive.RIDDER).findRoot();

			System.out.println ("--------\nRIDDER START\n-------");

			if (null != fpop && fpop.containsRoot()) {
				System.out.println ("Root: " + FormatUtil.FormatDouble (fpop.getRoot(), 1, 4, 1.));

				System.out.println (fpop.displayString());
			} else
				System.out.println ("Root searched failed!");

			System.out.println ("--------\nRIDDER FINISH\n-------\n");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/*
	 * Sample illustrating the Invocation of the Brent's Bracketing Method
	 * 
	 * 	WARNING: Insufficient Error Checking, so use caution
	 */

	private static final void InvokeBrent (
		final AbstractUnivariate func)
	{
		try {
			FixedPointFinderOutput fpop = new FixedPointFinderBrent (0., func).findRoot();

			System.out.println ("--------\nBRENT START\n-------");

			if (null != fpop && fpop.containsRoot()) {
				System.out.println ("Root: " + FormatUtil.FormatDouble (fpop.getRoot(), 1, 4, 1.));

				System.out.println (fpop.displayString());
			} else
				System.out.println ("Root searched failed!");

			System.out.println ("--------\nBRENT FINISH\n-------\n");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/*
	 * Sample illustrating the Invocation of the Zheng's Bracketing Method
	 * 
	 * 	WARNING: Insufficient Error Checking, so use caution
	 */

	private static final void InvokeZheng (
		final AbstractUnivariate func)
	{
		try {
			FixedPointFinderOutput fpop = new FixedPointFinderZheng (0., func).findRoot();

			System.out.println ("--------\nZHENG START\n-------");

			if (null != fpop && fpop.containsRoot()) {
				System.out.println ("Root: " + FormatUtil.FormatDouble (fpop.getRoot(), 1, 4, 1.));

				System.out.println (fpop.displayString());
			} else
				System.out.println ("Root searched failed!");

			System.out.println ("--------\nZHENG FINISH\n-------\n");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static final void main (
		final String[] astrArgs)
	{
		/*
		 * Define and implement the objective function
		 */

		AbstractUnivariate func = new AbstractUnivariate (null) {
			@Override public double evaluate (
				final double dblVariate)
				throws Exception
			{
				return Math.cos (dblVariate) - dblVariate * dblVariate * dblVariate;

				/* return dblVariate * dblVariate * dblVariate - 3. * dblVariate * dblVariate + 2. *
					dblVariate;

				return dblVariate * dblVariate * dblVariate + 4. * dblVariate + 4.;

				return 32. * dblVariate * dblVariate * dblVariate * dblVariate * dblVariate * dblVariate
					- 48. * dblVariate * dblVariate * dblVariate * dblVariate + 18. * dblVariate *
						dblVariate - 1.;

				return 1. + 3. * dblVariate - 2. * java.lang.Math.sin (dblVariate); */
			}

			@Override public Differential calcDifferential (
				final double dblVariate,
				final double dblOFBase,
				final int iOrder)
			{
				if (0 >= iOrder || 2 < iOrder) return null;

				double dblVariateInfinitesimal = Double.NaN;

				try {
					dblVariateInfinitesimal = _dc.getVariateInfinitesimal (dblVariate);
				} catch (Exception e) {
					e.printStackTrace();

					return null;
				}

				if (1 != iOrder) {
					try {
						return new Differential (dblVariateInfinitesimal, (-1. * Math.cos (dblVariate) - 6. * dblVariate)
							* dblVariateInfinitesimal);

						/* return new Differential (dblVariateInfinitesimal, (6. * dblVariate - 6.) * dblVariateInfinitesimal);

						return new Differential (dblVariateInfinitesimal, (6. * dblVariate) * dblVariateInfinitesimal);

						return new Differential (dblVariateInfinitesimal, (960. * dblVariate * dblVariate * dblVariate *
						 	dblVariate - 576. * dblVariate * dblVariate + 36.) * dblVariateInfinitesimal);

						return new Differential (dblVariateInfinitesimal, (2. * Math.sin (dblVariate)) * dblVariateInfinitesimal); */
					} catch (Exception e) {
						e.printStackTrace();
					}

					return null;
				}

				try {
					return new Differential (dblVariateInfinitesimal, (-1. * Math.sin (dblVariate) - 3. * dblVariate * dblVariate) *
				 		dblVariateInfinitesimal);

					/* return new Differential (dblVariateInfinitesimal, (3. * dblVariate * dblVariate - 6. * dblVariate + 2.) *
					 	dblVariateInfinitesimal);

					return new Differential (dblVariateInfinitesimal, (3. * dblVariate * dblVariate + 4.) * dblVariateInfinitesimal);

					return new Differential (dblVariateInfinitesimal, (192. * dblVariate * dblVariate * dblVariate * dblVariate *
						dblVariate - 192. * dblVariate * dblVariate * dblVariate + 36. * dblVariate) * dblVariateInfinitesimal);

					return new Differential (dblVariateInfinitesimal, (3. - 2. * Math.cos (dblVariate)) * dblVariateInfinitesimal); */
				} catch (Exception e) {
					e.printStackTrace();
				}

				return null;
			}
		};

		InvokeNewton (func);

		InvokeBisection (func);

		InvokeFalsePosition (func);

		InvokeQuadraticInterpolation (func);

		InvokeInverseQuadraticInterpolation (func);

		InvokeRidder (func);

		InvokeBrent (func);

		InvokeZheng (func);
	}
}
