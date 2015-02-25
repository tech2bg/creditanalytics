
package org.drip.function.deterministic;

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

	/**
	 * Validate the Input Double Array
	 * 
	 * @param adblVariate The Input Double Array
	 * 
	 * @return The Input Double Array consists of valid Values
	 */

	public static final boolean ValidateInput (
		final double[] adblVariate)
	{
		if (null == adblVariate) return false;

		int iNumVariate = adblVariate.length;

		if (0 == iNumVariate) return false;

		for (int i = 0; i < iNumVariate; ++i) {
			if (!org.drip.quant.common.NumberUtil.IsValid (adblVariate[i])) return false;
		}

		return true;
	}

	/**
	 * Evaluate for the given input variate
	 * 
	 * @param adblVariate Array of Input Variates
	 *  
	 * @return The calculated value
	 * 
	 * @throws java.lang.Exception Thrown if evaluation cannot be done
	 */

	public abstract double evaluate (
		final double[] adblVariate)
		throws java.lang.Exception;
}
