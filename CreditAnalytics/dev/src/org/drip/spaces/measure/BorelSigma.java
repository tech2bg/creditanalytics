
package org.drip.spaces.measure;

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
 * BorelSigma exposes the basic Properties of the Borel Sigma Measure Space.
 *
 * @author Lakshmi Krishnamurthy
 */

public interface BorelSigma {

	/**
	 * Compute the Population ESS (i.e., the Essential Spectrum) of the Spanning Space
	 * 
	 * @return The Population ESS
	 * 
	 * @throws java.lang.Exception Thrown if the Inputs are Invalid
	 */

	public abstract double populationESS()
		throws java.lang.Exception;

	/**
	 * Compute the P-Norm of the Spanning Population Space
	 * 
	 * @return The P-Norm
	 * 
	 * @throws java.lang.Exception Thrown if the p-Norm cannot be computed
	 */

	public abstract double populationMetricNorm()
		throws java.lang.Exception;
}
