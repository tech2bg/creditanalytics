
package org.drip.function.deterministic1D;

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
 * LinearRationalTensionExponential provides the evaluation of the Convolution of the Linear Rational and the
 * 	Tension Exponential Functons and its derivatives for a specified variate.
 *
 * @author Lakshmi Krishnamurthy
 */

public class LinearRationalTensionExponential extends org.drip.function.deterministic1D.UnivariateConvolution {

	/**
	 * Construct a LinearRationalTensionExponential instance
	 * 
	 * @param dblExponentialTension Exponential Tension Parameter
	 * @param dblRationalTension Rational Tension Parameter
	 * 
	 * @throws java.lang.Exception Thrown if the inputs are invalid
	 */

	public LinearRationalTensionExponential (
		final double dblExponentialTension,
		final double dblRationalTension)
		throws java.lang.Exception
	{
		super (new org.drip.function.deterministic1D.ExponentialTension (java.lang.Math.E, dblExponentialTension), new
			org.drip.function.deterministic1D.LinearRationalShapeControl (dblRationalTension));
	}

	public static final void main (
		final java.lang.String[] astrArgs)
		throws java.lang.Exception
	{
		LinearRationalTensionExponential lret = new LinearRationalTensionExponential (-1., 1.);

		System.out.println ("LRET[0.00] = " + lret.evaluate (0.00));

		System.out.println ("LRETDeriv[0.00] = " + lret.calcDerivative (0.00, 1));
	}
}
