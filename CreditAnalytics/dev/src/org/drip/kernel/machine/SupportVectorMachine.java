
package org.drip.kernel.machine;

/*
 * -*- mode: java; tab-width: 4; indent-tabs-mode: nil; c-basic-offset: 4 -*-
 */

/*!
 * Copyright (C) 2015 Lakshmi Krishnamurthy
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
 * SupportVectorMachine implements the SVM Functionality for Classification and Regression.
 *
 * @author Lakshmi Krishnamurthy
 */

public class SupportVectorMachine extends org.drip.function.deterministic.AbstractMultivariate {
	private double[] _adblW = null;
	private double _dblB = java.lang.Double.NaN;
	private org.drip.kernel.spaces.MultidimensionalRealValuedSpace _mrvs = null;

	/**
	 * SupportVectorMachine Constructor
	 * 
	 * @param adblW Array of Inverse Margin Weights
	 * @param dblB The Offset
	 * @param mrvs The Multidimensional Real-Valued Input Space
	 * 
	 * @throws java.lang.Exception Thrown if the Inputs are Invalid
	 */

	public SupportVectorMachine (
		final double[] adblW,
		final double dblB,
		final org.drip.kernel.spaces.MultidimensionalRealValuedSpace mrvs)
		throws java.lang.Exception
	{
		if (null == (_adblW = adblW) || null == mrvs || mrvs.dimension() != _adblW.length ||
			!org.drip.quant.common.NumberUtil.IsValid (_dblB = dblB))
			throw new java.lang.Exception ("SupportVectorMachine ctr: Invalid Inputs");
	}

	/**
	 * Retrieve the Inverse Margin Array
	 * 
	 * @return The Inverse Margin Array
	 */

	public double[] inverseMargin()
	{
		return _adblW;
	}

	/**
	 * Retrieve the Offset
	 * 
	 * @return The Offset
	 */

	public double offset()
	{
		return _dblB;
	}

	@Override public double evaluate (
		final double[] adblX)
		throws java.lang.Exception
	{
		if (!_mrvs.validate (adblX))
			throw new java.lang.Exception ("SupportVectorMachine::evaluate => Invalid Inputs");

		double dblDotProduct = 0.;
		int iDimension = adblX.length;

		for (int i = 0; i < iDimension; ++i)
			dblDotProduct += _adblW[i] * adblX[i];

		return dblDotProduct + _dblB;
	}

	/**
	 * Classify the Specified Multi-dimensional Point
	 * 
	 * @param adblX The Multi-dimensional Input Point
	 * 
	 * @return +1/-1 Boolean Space Output Equivalents
	 * 
	 * @throws java.lang.Exception Thrown if the Inputs are Invalid
	 */

	public short classify (
		final double[] adblX)
		throws java.lang.Exception
	{
		return evaluate (adblX) > 0. ? org.drip.kernel.spaces.BooleanSpace.BS_UP :
			org.drip.kernel.spaces.BooleanSpace.BS_DOWN;
	}

	/**
	 * Regress on the Specified Multi-dimensional Point
	 * 
	 * @param adblX The Multi-dimensional Input Point
	 * 
	 * @return The Regression Output
	 * 
	 * @throws java.lang.Exception Thrown if the Inputs are Invalid
	 */

	public double regress (
		final double[] adblX)
		throws java.lang.Exception
	{
		return evaluate (adblX);
	}
}
