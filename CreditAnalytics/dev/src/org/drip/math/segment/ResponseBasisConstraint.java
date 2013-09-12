
package org.drip.math.segment;

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
 * ResponseBasisConstraint holds the set of fields needed to characterize a single local linear
 * 	Constraint, expressed linearly as a combination of the local Predictor Ordinates and their corresponding
 *  Response Basis Function Realizations. Constraints are expressed as
 * 
 * 			C := Sigma_(i,j) [W_i * B_i(x_j)] = V where
 * 
 * 	x_j => The Predictor Ordinate at Node j
 * 	B_i => The Coefficient for the Response Basis Function i
 * 	W_i => Weight applied for the Response Basis Function i
 * 	V => Value of the Constraint
 * 
 * ResponseBasisConstraint can be viewed as the localized basis function transpose of
 *  PredictorResponseConstraint.
 * 
 * @author Lakshmi Krishnamurthy
 */

public class ResponseBasisConstraint {
	private double[] _adblResponseBasisCoeffWeight = null;
	private double _dblConstraintValue = java.lang.Double.NaN;

	/**
	 * ResponseBasisConstraint constructor
	 * 
	 * @param adblResponseBasisCoeffWeight The Weight for each of the Coefficients in the Basis Function Set
	 * @param dblConstraintValue The Constraint Value
	 *
	 * @throws java.lang.Exception Thrown if the inputs are invalid
	 */

	public ResponseBasisConstraint (
		double[] adblResponseBasisCoeffWeight,
		double dblConstraintValue)
		throws java.lang.Exception
	{
		if (!org.drip.math.common.NumberUtil.IsValid (_dblConstraintValue = dblConstraintValue) || null ==
			(_adblResponseBasisCoeffWeight = adblResponseBasisCoeffWeight) || 0 ==
				_adblResponseBasisCoeffWeight.length)
			throw new java.lang.Exception ("ResponseBasisConstraint ctr: Invalid Inputs");
	}

	/**
	 * Retrieve the Array of the Response Basis Coefficient Weights
	 * 
	 * @return The Array of the Response Basis Coefficient Weights
	 */

	public double[] responseBasisCoeffWeights()
	{
		return _adblResponseBasisCoeffWeight;
	}

	/**
	 * Retrieve the Constraint Value
	 * 
	 * @return The Constraint Value
	 */

	public double contraintValue()
	{
		return _dblConstraintValue;
	}
}
