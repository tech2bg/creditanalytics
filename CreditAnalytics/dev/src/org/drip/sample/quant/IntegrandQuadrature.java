
package org.drip.sample.quant;

import org.drip.quant.calculus.Integrator;
import org.drip.quant.common.*;
import org.drip.quant.function1D.*;
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
 * IntegrandQuadrature shows samples for the following routines for integrating the objective function:
 * 	- Mid-Point Scheme
 * 	- Trapezoidal Scheme
 * 	- Simpson/Simpson38 schemes
 * 	- Boole Scheme
 * 
 * @author Lakshmi Krishnamurthy
 */

public class IntegrandQuadrature {
	public static void ComputeQuadrature (
		final AbstractUnivariate au,
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

	public static void main (
		final String astrArgs[])
		throws Exception
	{
		double dblStart = 0.;
		double dblEnd = 1.;

		AbstractUnivariate auExp = new ExponentialTension (Math.E, 1.);

		System.out.println ("\n\t-------------------------------------\n");

		ComputeQuadrature (
			auExp,
			auExp.evaluate (dblEnd) - auExp.evaluate (dblStart),
			dblStart,
			dblEnd);

		System.out.println ("\n\t-------------------------------------\n");

		AbstractUnivariate au1 = new AbstractUnivariate (null) {
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

		AbstractUnivariate au2 = new AbstractUnivariate (null) {
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
}
