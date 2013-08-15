
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
 * SegmentConstraint holds the segment coefficient constraints and their values.
 * 
 * @author Lakshmi Krishnamurthy
 */

public class SegmentConstraint {
	private double[] _adblValue = null;
	private double[][] _aadblBasisFunction = null;

	/**
	 * SegmentConstraint constructor
	 * 
	 * @param aadblBasisFunction Basis Function Realizations
	 * @param adblValue Constraint Values
	 * 
	 * @throws java.lang.Exception
	 */

	public SegmentConstraint (
		final double[][] aadblBasisFunction,
		final double[] adblValue)
		throws java.lang.Exception
	{
		if (null == (_adblValue = adblValue) || null == (_aadblBasisFunction = aadblBasisFunction) || 0 ==
			_adblValue.length || _aadblBasisFunction.length != _adblValue.length || null ==
				_aadblBasisFunction[0] || 0 == _aadblBasisFunction[0].length)
			throw new java.lang.Exception ("SegmentConstraint ctr: Invalid Inputs");
	}

	/**
	 * Size of the Constraint
	 * 
	 * @return Constraint Size
	 */

	public int size()
	{
		return _adblValue.length;
	}

	/**
	 * Number of Basis Functions
	 * 
	 * @return Number of Basis Functions
	 */

	public int numBasis()
	{
		return _aadblBasisFunction[0].length;
	}

	/**
	 * Retrieve the Indexed Constraint Value
	 * 
	 * @param iConstraint The Constraint Index
	 * 
	 * @return The Indexed Constraint Value
	 */

	public double getValue (
		final int iConstraint)
	{
		return _adblValue[iConstraint];
	}

	/**
	 * Retrieve the Indexed Basis Function Constraint Coefficient
	 * 
	 * @param iConstraint The Constraint Index
	 * @param iBasis The Basis Function Index
	 * 
	 * @return The Basis Function Constraint Coefficient
	 */

	public double getValue (
		final int iConstraint,
		final int iBasis)
	{
		return _aadblBasisFunction[iConstraint][iBasis];
	}
}
