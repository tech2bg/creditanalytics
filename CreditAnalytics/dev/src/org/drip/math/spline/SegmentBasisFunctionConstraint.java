
package org.drip.math.spline;

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
 * SegmentBasisFunctionConstraint holds the following set of fields that characterize a single local linear
 * 	constraint, expressed linearly across the nodal basis function realizations. Constraints are expressed as
 * 
 * 			C = Sigma_i [W_i * B_i] = V where
 * 
 * 	B_i => The Coefficient for the Basis Function i
 * 	W_i => Weight applied for the Basis Function i
 * 	V => Value of the Constraint
 * 
 * SegmentBasisFunctionConstraint can be viewed as the localized basis function transpose of
 *  SegmentNodeWeightConstraint.
 * 
 * @author Lakshmi Krishnamurthy
 */

public class SegmentBasisFunctionConstraint {
	private double[] _adblCoeffWeight = null;
	private double _dblValue = java.lang.Double.NaN;

	/**
	 * SegmentBasisFunctionConstraint constructor
	 * 
	 * @param adblCoeffWeight The constraint Weight for each of the coefficients in the basis function set
	 * @param dblValue The constraint value
	 *
	 * @throws java.lang.Exception Thrown if the inputs are invalid
	 */

	public SegmentBasisFunctionConstraint (
		double dblValue,
		double[] adblCoeffWeight)
		throws java.lang.Exception
	{
		if (!org.drip.math.common.NumberUtil.IsValid (_dblValue = dblValue) || null == (_adblCoeffWeight =
			adblCoeffWeight) || 0 == _adblCoeffWeight.length)
			throw new java.lang.Exception ("SegmentBasisFunctionConstraint ctr: Invalid Inputs");
	}

	/**
	 * Retrieve the Array of the Coefficient Weights
	 * 
	 * @return The Array of the Coefficient Weights
	 */

	public double[] getCoeffWeight()
	{
		return _adblCoeffWeight;
	}

	/**
	 * Retrieve the Constraint Value
	 * 
	 * @return The Constraint Value
	 */

	public double getValue()
	{
		return _dblValue;
	}
}
