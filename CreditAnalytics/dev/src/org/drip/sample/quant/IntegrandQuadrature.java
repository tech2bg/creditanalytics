
package org.drip.sample.quant;

import org.drip.function.deterministic.R1ToR1;
import org.drip.function.deterministic1D.*;
import org.drip.quant.calculus.Integrator;
import org.drip.quant.common.*;

/*
 * -*- mode: java; tab-width: 4; indent-tabs-mode: nil; c-basic-offset: 4 -*-
 */

/*!
 * Copyright (C) 2015 Lakshmi Krishnamurthy
 * Copyright (C) 2014 Lakshmi Krishnamurthy
 * Copyright (C) 2013 Lakshmi Krishnamurthy
 * 
 *  This file is part of DRIP, a free-software/open-source library for fixed income analysts and developers -
 * 		http://www.credit-trader.org/Begin.html
 * 
 *  DRIP is a free, full featured, fixed income rates, credit, and FX analytics library with a focus towards
 *  	pricing/valuation, risk, and market making.
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
 * IntegrandQuadrature shows samples for the following routines for integrating the objective function:
 * 	- Mid-Point Scheme
 * 	- Trapezoidal Scheme
 * 	- Simpson/Simpson38 schemes
 * 	- Boole Scheme
 * 
 * @author Lakshmi Krishnamurthy
 */

public class IntegrandQuadrature {

	/*
	 * Compute the Integrand Quadrature for the specified Univariate Function using the various methods.
	 * 
	 * 	WARNING: Insufficient Error Checking, so use caution
	 */

	private static void ComputeQuadrature (
		final R1ToR1 au,
		final double dblActual,
		final double dblStart,
		final double dblEnd)
		throws Exception
	{
		int iRightDecimal = 8;

		System.out.println ("\t\tActual      : " +
			FormatUtil.FormatDouble (dblActual,
			1, iRightDecimal, 1.));

		System.out.println ("\t\tLinear      : " +
			FormatUtil.FormatDouble (Integrator.LinearQuadrature (au, dblStart, dblEnd),
			1, iRightDecimal, 1.));

		System.out.println ("\t\tMidPoint    : " +
			FormatUtil.FormatDouble (Integrator.MidPoint (au, dblStart, dblEnd),
			1, iRightDecimal, 1.));

		System.out.println ("\t\tTrapezoidal : " +
			FormatUtil.FormatDouble (Integrator.Trapezoidal (au, dblStart, dblEnd),
			1, iRightDecimal, 1.));

		System.out.println ("\t\tSimpson     : " +
			FormatUtil.FormatDouble (Integrator.Simpson (au, dblStart, dblEnd),
			1, iRightDecimal, 1.));

		System.out.println ("\t\tSimpson 38  : " +
			FormatUtil.FormatDouble (Integrator.Simpson (au, dblStart, dblEnd),
			1, iRightDecimal, 1.));

		System.out.println ("\t\tBoole       : " +
			FormatUtil.FormatDouble (Integrator.Boole (au, dblStart, dblEnd),
			1, iRightDecimal, 1.));
	}

	/*
	 * Compute the Integrand Quadrature for the various Univariate Functions using the different methods.
	 * 
	 * 	WARNING: Insufficient Error Checking, so use caution
	 */

	private static void IntegrandQuadratureSample()
		throws Exception
	{
		double dblStart = 0.;
		double dblEnd = 1.;

		R1ToR1 auExp = new ExponentialTension (Math.E, 1.);

		System.out.println ("\n\t-------------------------------------\n");

		ComputeQuadrature (
			auExp,
			auExp.evaluate (dblEnd) - auExp.evaluate (dblStart),
			dblStart,
			dblEnd);

		System.out.println ("\n\t-------------------------------------\n");

		R1ToR1 au1 = new R1ToR1 (null) {
			@Override public double evaluate (
				final double dblVariate)
				throws Exception
			{
				return Math.cos (dblVariate) - dblVariate * dblVariate * dblVariate;
			}
		};

		ComputeQuadrature (
			au1,
			Math.sin (dblEnd) - Math.sin (dblStart) - 0.25 * (dblEnd * dblEnd * dblEnd * dblEnd - dblStart * dblStart * dblStart * dblStart),
			dblStart,
			dblEnd);

		System.out.println ("\n\t-------------------------------------\n");

		R1ToR1 au2 = new R1ToR1 (null) {
			@Override public double evaluate (
				final double dblVariate)
				throws Exception
			{
				return dblVariate * dblVariate * dblVariate - 3. * dblVariate * dblVariate + 2. * dblVariate;
			}
		};

		ComputeQuadrature (
			au2,
			0.25 * (dblEnd * dblEnd * dblEnd * dblEnd - dblStart * dblStart * dblStart * dblStart) -
				(dblEnd * dblEnd * dblEnd - dblStart * dblStart * dblStart) +
				(dblEnd * dblEnd - dblStart * dblStart),
			dblStart,
			dblEnd);

		System.out.println ("\n\t-------------------------------------\n");
	}

	public static void main (
		final String astrArgs[])
		throws Exception
	{
		IntegrandQuadratureSample();
	}
}
