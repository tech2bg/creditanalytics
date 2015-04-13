
package org.drip.spaces.metric;

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
 * RealUnidimensionalNormedSpace abstract Class implements the normed, bounded/unbounded
 *  Continuous/Combinatorial l^p R^1 Spaces.
 * 
 * The Reference we've used is:
 * 
 * 	- Carl, B., and I. Stephani (1990): Entropy, Compactness, and Approximation of Operators, Cambridge
 * 		University Press, Cambridge UK.
 *
 * @author Lakshmi Krishnamurthy
 */

public interface RealUnidimensionalNormedSpace extends org.drip.spaces.metric.GeneralizedMetricSpace {

	/**
	 * Retrieve the Borel Sigma Univariate Probability Measure
	 * 
	 * @return The Borel Sigma Univariate Probability Measure
	 */

	public abstract org.drip.measure.continuous.UnivariateDistribution borelSigmaMeasure();

	/**
	 * Compute the Metric Norm of the Sample
	 * 
	 * @param dblX The Sample
	 * 
	 * @return The Metric Norm of the Sample
	 * 
	 * @throws java.lang.Exception Thrown if the Inputs are Invalid
	 */

	public abstract double sampleMetricNorm (
		final double dblX)
		throws java.lang.Exception;

	/**
	 * Retrieve the Population Mode
	 * 
	 * @return The Population Mode
	 * 
	 * @throws java.lang.Exception Thrown if the Population Mode cannot be calculated
	 */

	public double populationMode()
		throws java.lang.Exception;
}
