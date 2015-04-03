
package org.drip.quant.distribution;

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
 * Multivariate implements the base abstract class behind Multivariate distributions. It exports methods for
 * 	incremental, cumulative, and inverse cumulative distribution densities.
 *
 * @author Lakshmi Krishnamurthy
 */

public abstract class Multivariate {

	/**
	 * Compute the Cumulative under the Distribution to the given Variaate Array
	 * 
	 * @param adblX Variate Array to which the Cumulative is to be computed
	 * 
	 * @return The Cumulative
	 * 
	 * @throws java.lang.Exception Thrown if the inputs are invalid
	 */

	public abstract double cumulative (
		final double[] adblX)
		throws java.lang.Exception;

	/**
	 * Compute the Incremental under the Distribution between the 2 Variate Arrays
	 * 
	 * @param adblXLeft Left Variate Array to which the Cumulative is to be computed
	 * @param adblXRight Right Variate Array to which the Cumulative is to be computed
	 * 
	 * @return The Incremental
	 * 
	 * @throws java.lang.Exception Thrown if the inputs are invalid
	 */

	public abstract double incremental (
		final double[] adblXLeft,
		final double[] adblXRight)
		throws java.lang.Exception;

	/**
	 * Compute the Inverse Cumulative under the Distribution corresponding to the given Variate Array
	 * 
	 * @param adblX Variate Array corresponding to which the Inverse Cumulative needs to be computed
	 * 
	 * @return The Inverse Cumulative
	 * 
	 * @throws java.lang.Exception Thrown if the input is invalid
	 */

	public abstract double invCumulative (
		final double[] adblX)
		throws java.lang.Exception;

	/**
	 * Compute the Density under the Distribution at the given Variate Array
	 * 
	 * @param adblX Variate Array at which the Density needs to be computed
	 * 
	 * @return The Density
	 * 
	 * @throws java.lang.Exception Thrown if the input is invalid
	 */

	public abstract double density (
		final double[] adblX)
		throws java.lang.Exception;
}
