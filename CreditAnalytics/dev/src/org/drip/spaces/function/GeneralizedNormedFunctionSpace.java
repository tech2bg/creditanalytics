
package org.drip.spaces.function;

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
 * GeneralizedNormedFunctionSpace is the abstract Class that exposes f : R^d -> R^d along with the Function
 *  Space Level P-Norm.
 * 
 * The Reference we've used is:
 * 
 * 	- Carl, B., and I. Stephani (1990): Entropy, Compactness, and Approximation of Operators, Cambridge
 * 		University Press, Cambridge UK.
 *
 * @author Lakshmi Krishnamurthy
 */

public abstract class GeneralizedNormedFunctionSpace {
	private int _iPNorm = -1;

	protected GeneralizedNormedFunctionSpace (
		final int iPNorm)
		throws java.lang.Exception
	{
		if (0 > (_iPNorm = iPNorm))
			throw new java.lang.Exception ("GeneralizedNormedFunctionSpace ctr: Invalid Inputs");
	}

	/**
	 * Retrieve the Input Vector Space
	 * 
	 * @return The Input Vector Space
	 */

	public abstract org.drip.spaces.tensor.GeneralizedVectorSpace input();

	/**
	 * Retrieve the Output Vector Space
	 * 
	 * @return The Output Vector Space
	 */

	public abstract org.drip.spaces.tensor.GeneralizedVectorSpace output();

	/**
	 * Retrieve the P-Norm Index
	 * 
	 * @return The P-Norm Index
	 */

	public int pNorm()
	{
		return _iPNorm;
	}

	/**
	 * Retrieve the Sample Supremum Norm
	 * 
	 * @param gvviInstance The Validated Vector Space Instance
	 * 
	 * @return The Sample Supremum Norm
	 * 
	 * @throws java.lang.Exception Thrown if the Supremum Norm cannot be computed
	 */

	public abstract double sampleSupremumNorm (
		final org.drip.spaces.instance.GeneralizedValidatedVectorInstance gvviInstance)
		throws java.lang.Exception;

	/**
	 * Retrieve the Sample Metric Norm
	 * 
	 * @param gvviInstance The Validated Vector Space Instance
	 * 
	 * @return The Sample Metric Norm
	 * 
	 * @throws java.lang.Exception Thrown if the Sample Metric Norm cannot be computed
	 */

	public abstract double sampleMetricNorm (
		final org.drip.spaces.instance.GeneralizedValidatedVectorInstance gvviInstance)
		throws java.lang.Exception;

	/**
	 * Retrieve the Population ESS (Essential Spectrum)
	 * 
	 * @return The Population ESS (Essential Spectrum)
	 * 
	 * @throws java.lang.Exception Thrown if the Population ESS (Essential Spectrum) cannot be computed
	 */

	public abstract double populationESS()
		throws java.lang.Exception;

	/**
	 * Retrieve the Population Metric Norm
	 * 
	 * @return The Population Metric Norm
	 * 
	 * @throws java.lang.Exception Thrown if the Population Metric Norm cannot be computed
	 */

	public abstract double populationMetricNorm()
		throws java.lang.Exception;
}
