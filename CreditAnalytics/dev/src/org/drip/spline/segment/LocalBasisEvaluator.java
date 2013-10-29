
package org.drip.spline.segment;

/*
 * -*- mode: java; tab-width: 4; indent-tabs-mode: nil; c-basic-offset: 4 -*-
 */

/*!
 * Copyright (C) 2013 Lakshmi Krishnamurthy
 * 
 * This file is part of CreditAnalytics, a free-software/open-source library for fixed income analysts and
 * 		developers - http://www.credit-trader.org
 * 
 * CreditAnalytics is a free, full featured, fixed income credit analytics library, developed with a special
 * 		focus towards the needs of the bonds and credit products community.
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
 * This Interface implements the Segment's Local Basis Evaluator Functions.
 * 
 * @author Lakshmi Krishnamurthy
 */

public interface LocalBasisEvaluator {

	/**
	 * Compute the Ordered Derivative of the Response Value off of the Basis Function Set at the specified
	 *  Local Predictor Ordinate
	 * 
	 * @param dblLocalPredictorOrdinate The specified Local Predictor Ordinate
	 * @param iOrder Order of the Derivative
	 * 
	 * @return The Ordered Derivative of the Response Value off of the Basis Function Set
	 * 
	 * @throws java.lang.Exception Thrown if the Ordered Derivative of the Basis Function Set cannot be
	 * 	computed
	 */

	public abstract double basisFunctionResponseDerivative (
		final double dblLocalPredictorOrdinate,
		final int iOrder)
		throws java.lang.Exception;

	/**
	 * Compute the Basis Function Value at the specified Local Predictor Ordinate
	 * 
	 * @param dblLocalPredictorOrdinate The specified Local Predictor Ordinate
	 * 
	 * @return The Basis Function Value
	 * 
	 * @throws java.lang.Exception Thrown if the Basis Function Value cannot be computed
	 */

	public abstract double basisFunctionResponseValue (
		final double dblLocalPredictorOrdinate)
		throws java.lang.Exception;

	/**
	 * Compute the Response Value of the indexed Basis Function at the specified Local Predictor Ordinate
	 * 
	 * @param dblLocalPredictorOrdinate The specified Local Predictor Ordinate
	 * @param iBasisFunctionIndex Index representing the Basis Function in the Basis Function Set
	 *  
	 * @return The Response Value of the indexed Basis Function at the specified Local Predictor Ordinate
	 * 
	 * @throws java.lang.Exception Thrown if the Ordered Derivative cannot be computed
	 */

	public abstract double localSpecificBasisResponse (
		final double dblLocalPredictorOrdinate,
		final int iBasisFunctionIndex)
		throws java.lang.Exception;

	/**
	 * Compute the Ordered Derivative of the Response Value off of the indexed Basis Function at the
	 *	specified Local Predictor Ordinate
	 * 
	 * @param dblLocalPredictorOrdinate The specified Local Predictor Ordinate
	 * @param iOrder Order of the Derivative
	 * @param iBasisFunctionIndex Index representing the Basis Function in the Basis Function Set
	 *  
	 * @return The Ordered Derivative of the Response Value off of the Indexed Basis Function
	 * 
	 * @throws java.lang.Exception Thrown if the Ordered Derivative cannot be computed
	 */

	public abstract double localSpecificBasisDerivative (
		final double dblLocalPredictorOrdinate,
		final int iOrder,
		final int iBasisFunctionIndex)
		throws java.lang.Exception;
}
