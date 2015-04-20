
package org.drip.function.stochastic;

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
 * R1R1ToR1 interface exposes the stubs for the evaluation of the objective function and its derivatives for
 *  a R^1 Deterministic + R^1 Random -> R^1 Stochastic Function with one Random Component.
 *
 * @author Lakshmi Krishnamurthy
 */

public interface R1R1ToR1 {

	/**
	 * Evaluate a Single Realization for the given variate
	 * 
	 * @param dblVariate Variate
	 *  
	 * @return Return the Single Realization for the given variate
	 * 
	 * @throws java.lang.Exception Thrown if evaluation cannot be done
	 */

	public abstract double evaluateRealization (
		final double dblVariate)
		throws java.lang.Exception;

	/**
	 * Evaluate the Expectation for the given variate
	 * 
	 * @param dblVariate Variate
	 *  
	 * @return Return the Expectation for the given variate
	 * 
	 * @throws java.lang.Exception Thrown if evaluation cannot be done
	 */

	public abstract double evaluateExpectation (
		final double dblVariate)
		throws java.lang.Exception;

	/**
	 * Evaluate the Derivative for a Single Realization for the given variate
	 * 
	 * @param dblVariate Variate at which the derivative is to be calculated
	 * @param iOrder Order of the derivative to be computed
	 *  
	 * @return Return the Derivative for a Single Realization for the given variate
	 * 
	 * @throws java.lang.Exception Thrown if evaluation cannot be done
	 */

	public abstract double derivativeRealization (
		final double dblVariate,
		final int iOrder)
		throws java.lang.Exception;

	/**
	 * Evaluate the Derivative Expectation at the given variate
	 * 
	 * @param dblVariate Variate at which the derivative is to be calculated
	 * @param iOrder Order of the derivative to be computed
	 *  
	 * @return Return the Derivative Expectation at the given variate
	 * 
	 * @throws java.lang.Exception Thrown if evaluation cannot be done
	 */

	public abstract double derivativeExpectation (
		final double dblVariate,
		final int iOrder)
		throws java.lang.Exception;

	/**
	 * Evaluate a Path-wise Integral between the Vriates
	 * 
	 * @param dblStart Variate Start
	 * @param dblEnd Variate End
	 *  
	 * @return The Path-wise Integral between the Variates
	 * 
	 * @throws java.lang.Exception Thrown if evaluation cannot be done
	 */

	public abstract double integralRealization (
		final double dblStart,
		final double dblEnd)
		throws java.lang.Exception;

	/**
	 * Evaluate the Expected Path-wise Integral between the Vriates
	 * 
	 * @param dblStart Variate Start
	 * @param dblEnd Variate End
	 *  
	 * @return The Expected Path-wise Integral between the Variates
	 * 
	 * @throws java.lang.Exception Thrown if evaluation cannot be done
	 */

	public abstract double integralExpectation (
		final double dblStart,
		final double dblEnd)
		throws java.lang.Exception;
}
