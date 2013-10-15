
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
 * CurvaturePenaltyParams implements basis per-segment Curvature Penalty Parameter Set. Currently it contains
 *  the Curvature Penalty Derivative Order and the Roughness Coefficient Weight.
 *
 * @author Lakshmi Krishnamurthy
 */

public class CurvaturePenaltyParams {
	private int _iDerivativeOrder = -1;
	private double _dblCoefficient = java.lang.Double.NaN;

	/**
	 * CurvaturePenaltyParams constructor
	 * 
	 * @param iDerivativeOrder Roughness Penalty Derivative Order
	 * @param dblCoefficient Roughness Coefficient
	 * 
	 * @throws java.lang.Exception Thrown if the inputs are invalid
	 */

	public CurvaturePenaltyParams (
		final int iDerivativeOrder,
		final double dblCoefficient)
		throws java.lang.Exception
	{
		if (0 >= (_iDerivativeOrder = iDerivativeOrder) || !org.drip.math.common.NumberUtil.IsValid
			(_dblCoefficient = dblCoefficient))
			throw new java.lang.Exception ("CurvaturePenaltyParams ctr: Invalid Inputs");
	}

	/**
	 * Retrieve the Derivative Order
	 * 
	 * @return The Derivative Order
	 */

	public int derivativeOrder()
	{
		return _iDerivativeOrder;
	}

	/**
	 * Retrieve the Roughness Coefficient
	 * 
	 * @return The Roughness Coefficient
	 */

	public double coefficient()
	{
		return _dblCoefficient;
	}
}
