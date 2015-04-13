
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
 * This class provides the evaluation of the Convolution au1 * au2 and its derivatives for a specified
 * 	variate.
 *
 * @author Lakshmi Krishnamurthy
 */

public class UnivariateConvolution extends org.drip.function.deterministic.R1ToR1 {
	private org.drip.function.deterministic.R1ToR1 _au1 = null;
	private org.drip.function.deterministic.R1ToR1 _au2 = null;

	/**
	 * Construct a PolynomialMirrorCross instance
	 * 
	 * @param au1 Univariate Function #1
	 * @param au2 Univariate Function #2
	 * 
	 * @throws java.lang.Exception Thrown if the inputs are invalid
	 */

	public UnivariateConvolution (
		final org.drip.function.deterministic.R1ToR1 au1,
		final org.drip.function.deterministic.R1ToR1 au2)
		throws java.lang.Exception
	{
		super (null);

		if (null == (_au1 = au1) || null == (_au2 = au2))
			throw new java.lang.Exception ("Convolution ctr: Invalid Inputs");
	}

	@Override public double evaluate (
		final double dblVariate)
		throws java.lang.Exception
	{
		if (!org.drip.quant.common.NumberUtil.IsValid (dblVariate))
			throw new java.lang.Exception ("Convolution::evaluate => Invalid Input");

		return _au1.evaluate (dblVariate) * _au2.evaluate (dblVariate);
	}

	@Override public double derivative (
		final double dblVariate,
		final int iOrder)
		throws java.lang.Exception
	{
		double dblDerivative = _au1.evaluate (dblVariate) * _au2.derivative (dblVariate, iOrder);

		for (int i = 1; i < iOrder; ++i)
			dblDerivative += org.drip.quant.common.NumberUtil.NCK (iOrder, i) * _au1.derivative (dblVariate,
				i) * _au2.derivative (dblVariate, iOrder - i);

		return dblDerivative + _au1.derivative (dblVariate, iOrder) * _au2.evaluate (dblVariate);
	}

	@Override public double integrate (
		final double dblBegin,
		final double dblEnd)
		throws java.lang.Exception
	{
		if (!org.drip.quant.common.NumberUtil.IsValid (dblBegin) || !org.drip.quant.common.NumberUtil.IsValid
			(dblEnd))
			throw new java.lang.Exception ("HyperbolicTension::integrate => Invalid Inputs");

		return org.drip.quant.calculus.Integrator.Boole (this, dblBegin, dblEnd);
	}
}
