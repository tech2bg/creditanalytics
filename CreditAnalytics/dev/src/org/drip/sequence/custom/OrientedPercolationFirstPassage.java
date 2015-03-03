
package org.drip.sequence.custom;

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
 * OrientedPercolationFirstPassage contains Variance Bounds on the Critical Measures of the Standard Problem
 * 	of First Passage Time in Oriented Percolation.
 *
 * @author Lakshmi Krishnamurthy
 */

public class OrientedPercolationFirstPassage extends org.drip.sequence.functional.BoundedMultivariateRandom {
	private double _dblMaxLength = java.lang.Double.NaN;
	private double _dblEdgeWeightVariance = java.lang.Double.NaN;

	/**
	 * OrientedPercolationFirstPassage Constructor
	 * 
	 * @param dblEdgeWeightVariance Variance of Edge Weight
	 * @param dblMaxLength Length of the Maximal Path
	 * 
	 * @throws java.lang.Exception Thrown if the Inputs are Invalid
	 */

	public OrientedPercolationFirstPassage (
		final double dblEdgeWeightVariance,
		final double dblMaxLength)
		throws java.lang.Exception
	{
		if (!org.drip.quant.common.NumberUtil.IsValid (_dblEdgeWeightVariance = dblEdgeWeightVariance) ||
			!org.drip.quant.common.NumberUtil.IsValid (_dblMaxLength = dblMaxLength))
			throw new java.lang.Exception ("OrientedPercolationFirstPassage ctr => Invalid Inputs");
	}

	/**
	 * Retrieve the Edge Width Variance
	 * 
	 * @return The Edge Width Variance
	 */

	public double edgeWeightVariance()
	{
		return _dblEdgeWeightVariance;
	}

	/**
	 * Retrieve the Length of the Maximal Path
	 * 
	 * @return Length of the Maximal Path
	 */

	public double maxLength()
	{
		return _dblMaxLength;
	}

	@Override public double evaluate (
		final double[] adblVariate)
		throws java.lang.Exception
	{
		return _dblMaxLength;
	}

	@Override public double targetVariateVarianceBound (
		final int iTargetVariateIndex)
		throws java.lang.Exception
	{
		return _dblMaxLength * _dblEdgeWeightVariance;
	}
}
