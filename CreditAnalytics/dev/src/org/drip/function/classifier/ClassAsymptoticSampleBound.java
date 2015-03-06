
package org.drip.function.classifier;

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
 * ClassAsymptoticSampleBound provides the Asymptotic Sample Behavior of the Empirical Loss of the given
 *  Classifier Class. This is expressed as ~ C * n^a, where n is the Size of the Sample, and 'C' and 'a' are
 *  Constants specific to the Classifier Class.
 *
 * @author Lakshmi Krishnamurthy
 */

public class ClassAsymptoticSampleBound {
	private double _dblConstant = java.lang.Double.NaN;
	private double _dblExponent = java.lang.Double.NaN;

	/**
	 * ClassAsymptoticSampleBound Constructor
	 * 
	 * @param dblConstant Asymptote Constant
	 * @param dblExponent Asymptote Exponent
	 * 
	 * @throws java.lang.Exception Thrown if the Constant and/or Exponent is Invalid
	 */

	public ClassAsymptoticSampleBound (
		final double dblConstant,
		final double dblExponent)
		throws java.lang.Exception
	{
		if (!org.drip.quant.common.NumberUtil.IsValid (_dblConstant = dblConstant) ||
			!org.drip.quant.common.NumberUtil.IsValid (_dblExponent = dblExponent))
			throw new java.lang.Exception ("ClassAsymptoticSampleBound ctr: Invalid Inputs!");
	}

	/**
	 * Retrieve the Asymptote Constant
	 * 
	 * @return The Asymptote Constant
	 */

	public double constant()
	{
		return _dblConstant;
	}

	/**
	 * Retrieve the Asymptote Exponent
	 * 
	 * @return The Asymptote Exponent
	 */

	public double exponent()
	{
		return _dblExponent;
	}
}
