
package org.drip.quant.function;

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
 * AbstractMultivariate provides the evaluation of the objective function and its derivatives for a specified
 * 	set of variates. Default implementation of the derivatives are for non-analytical black box objective
 * 	functions.
 *
 * @author Lakshmi Krishnamurthy
 */

public abstract class AbstractMultivariate {
	private int _iNumVariable = -1;

	/**
	 * Multi-variate Objective Function constructor
	 * 
	 * @param iNumVariable Number of Variables
	 * 
	 * @throws java.lang.Exception Thrown if the Inputs are invalid
	 */

	public AbstractMultivariate (
		final int iNumVariable)
		throws java.lang.Exception
	{
		if (0 >= (_iNumVariable = iNumVariable))
			throw new java.lang.Exception ("AbstractMultivariate ctr: Invalid Inputs");
	}

	/**
	 * Retrieve the Number of Input Variables
	 * 
	 * @return The Number of Input Variables
	 */

	public int numVariables()
	{
		return _iNumVariable;
	}

	/**
	 * Verify if the Number/Range of the Inputs is valid for Functional Evaluation
	 * 
	 * @param adblVariable Input Variable Array
	 * 
	 * @return TRUE => Number/Range of the Inputs is valid for Functional Evaluation
	 */

	public boolean verifyVariables (
		final double[] adblVariable)
	{
		if (null == adblVariable) return false;

		int iNumVariable = adblVariable.length;

		if (iNumVariable != _iNumVariable) return false;

		for (int i = 0; i < iNumVariable; ++i) {
			if (!org.drip.quant.common.NumberUtil.IsValid (adblVariable[i])) return false;
		}

		return true;
	}

	/**
	 * Evaluate for the given input variate
	 * 
	 * @param adblVariate Array of Input Variates
	 *  
	 * @return Returns the calculated value
	 * 
	 * @throws java.lang.Exception Thrown if evaluation cannot be done
	 */

	public abstract double evaluate (
		final double[] adblVariate)
		throws java.lang.Exception;
}
