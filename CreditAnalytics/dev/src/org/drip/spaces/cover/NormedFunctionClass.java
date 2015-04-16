
package org.drip.spaces.cover;

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
 * NormedFunctionClass implements the Class F of f : R^x -> R^x Normed Function Spaces.
 * 
 * The Reference we've used is:
 * 
 * 	- Carl, B., and I. Stephani (1990): Entropy, Compactness, and Approximation of Operators, Cambridge
 * 		University Press, Cambridge UK.
 *
 * @author Lakshmi Krishnamurthy
 */

public class NormedFunctionClass {
	private org.drip.spaces.function.GeneralizedNormedFunctionSpace[] _aGNFS = null;

	private NormedFunctionClass (
		final org.drip.spaces.function.GeneralizedNormedFunctionSpace[] aGNFS)
		throws java.lang.Exception
	{
		if (null == (_aGNFS = aGNFS))
			throw new java.lang.Exception ("NormedFunctionClass ctr: Invalid Inputs");

		int iClassSize = _aGNFS.length;

		if (0 == iClassSize) throw new java.lang.Exception ("NormedFunctionClass ctr: Invalid Inputs");

		for (int i = 0; i < iClassSize; ++i) {
			if (null == _aGNFS[i]) throw new java.lang.Exception ("NormedFunctionClass ctr: Invalid Inputs");
		}
	}

	/**
	 * Retrieve the Array of Function Spaces in the Class
	 * 
	 * @return The Array of Function Spaces in the Class
	 */

	public org.drip.spaces.function.GeneralizedNormedFunctionSpace[] functionSpaces()
	{
		return _aGNFS;
	}

	/**
	 * Retrieve the Input Vector Space
	 * 
	 * @return The Input Vector Space
	 */

	public org.drip.spaces.tensor.GeneralizedVectorSpace input()
	{
		return _aGNFS[0].input();
	}

	/**
	 * Retrieve the Output Vector Space
	 * 
	 * @return The Output Vector Space
	 */

	public org.drip.spaces.tensor.GeneralizedVectorSpace output()
	{
		return _aGNFS[0].output();
	}

	/**
	 * Retrieve the P-Norm Index
	 * 
	 * @return The P-Norm Index
	 */

	public int pNorm()
	{
		return _aGNFS[0].pNorm();
	}

	/**
	 * Compute the Operator Population Norm
	 * 
	 * @return The Operator Population Norm
	 * 
	 * @throws java.lang.Exception Thrown if the Operator Population Norm cannot be computed
	 */

	public double operatorPopulationNorm()
		throws java.lang.Exception
	{
		int iNumFunction = _aGNFS.length;

		double dblOperatorPopulationNorm = _aGNFS[0].populationMetricNorm();

		if (!org.drip.quant.common.NumberUtil.IsValid (dblOperatorPopulationNorm))
			throw new java.lang.Exception
				("NormedFunctionClass::operatorPopulationNorm => Cannot compute population Norm for Function "
					+ "#" + 0);

		for (int i = 1; i < iNumFunction; ++i) {
			double dblPopulationNorm = _aGNFS[i].populationMetricNorm();

			if (!org.drip.quant.common.NumberUtil.IsValid (dblPopulationNorm))
				throw new java.lang.Exception
					("NormedFunctionClass::operatorPopulationNorm => Cannot compute population Norm for Function "
						+ "#" + i);

			if (dblOperatorPopulationNorm > dblPopulationNorm) dblOperatorPopulationNorm = dblPopulationNorm;
		}

		return dblOperatorPopulationNorm;
	}

	/**
	 * Compute the Operator Sample Norm
	 * 
	 * @param gvviInstance The Validated Vector Space Instance
	 * 
	 * @return The Operator Sample Norm
	 * 
	 * @throws java.lang.Exception Thrown if the Operator Sample Norm cannot be computed
	 */

	public double operatorSampleNorm (
		final org.drip.spaces.instance.GeneralizedValidatedVectorInstance gvviInstance)
		throws java.lang.Exception
	{
		int iNumFunction = _aGNFS.length;

		double dblOperatorSampleNorm = _aGNFS[0].sampleMetricNorm (gvviInstance);

		if (!org.drip.quant.common.NumberUtil.IsValid (dblOperatorSampleNorm))
			throw new java.lang.Exception
				("NormedFunctionClass::operatorSampleNorm => Cannot compute Sample Norm for Function " + "#"
					+ 0);

		for (int i = 1; i < iNumFunction; ++i) {
			double dblSampleNorm = _aGNFS[i].populationMetricNorm();

			if (!org.drip.quant.common.NumberUtil.IsValid (dblSampleNorm))
				throw new java.lang.Exception
					("NormedFunctionClass::operatorSampleNorm => Cannot compute Sample Norm for Function " +
						"#" + i);

			if (dblOperatorSampleNorm > dblSampleNorm) dblOperatorSampleNorm = dblSampleNorm;
		}

		return dblOperatorSampleNorm;
	}
}
