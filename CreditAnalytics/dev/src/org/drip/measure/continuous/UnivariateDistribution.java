
package org.drip.measure.continuous;

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
 * UnivariateDistribution implements the base abstract class behind univariate distributions. It exports
 *  methods for incremental, cumulative, and inverse cumulative distribution densities.
 *
 * @author Lakshmi Krishnamurthy
 */

public abstract class UnivariateDistribution {

	/**
	 * Compute the cumulative under the distribution to the given value
	 * 
	 * @param dblX Variate to which the cumulative is to be computed
	 * 
	 * @return The cumulative
	 * 
	 * @throws java.lang.Exception Thrown if the inputs are invalid
	 */

	public abstract double cumulative (
		final double dblX)
		throws java.lang.Exception;

	/**
	 * Compute the incremental under the distribution between the 2 variates
	 * 
	 * @param dblXLeft Left Variate to which the cumulative is to be computed
	 * @param dblXRight Right Variate to which the cumulative is to be computed
	 * 
	 * @return The incremental
	 * 
	 * @throws java.lang.Exception Thrown if the inputs are invalid
	 */

	public abstract double incremental (
		final double dblXLeft,
		final double dblXRight)
		throws java.lang.Exception;

	/**
	 * Compute the inverse cumulative under the distribution corresponding to the given value
	 * 
	 * @param dblX Value corresponding to which the inverse cumulative is to be computed
	 * 
	 * @return The inverse cumulative
	 * 
	 * @throws java.lang.Exception Thrown if the input is invalid
	 */

	public abstract double invCumulative (
		final double dblX)
		throws java.lang.Exception;

	/**
	 * Compute the Density under the Distribution at the given Variate
	 * 
	 * @param dblX Variate at which the Density needs to be computed
	 * 
	 * @return The Density
	 * 
	 * @throws java.lang.Exception Thrown if the input is invalid
	 */

	public abstract double density (
		final double dblX)
		throws java.lang.Exception;

	/**
	 * Retrieve the Mean of the Distribution
	 * 
	 * @return The Mean of the Distribution
	 */

	public abstract double mean();

	/**
	 * Retrieve the Variance of the Distribution
	 * 
	 * @return The Variance of the Distribution
	 */

	public abstract double variance();

	/**
	 * Retrieve the Univariate Weighted Histogram
	 * 
	 * @return The Univariate Weighted Histogram
	 */

	public abstract org.drip.quant.common.Array2D histogram();
}
