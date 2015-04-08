
package org.drip.spaces.tensor;

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
 * CombinatorialRealUnidimensionalVector exposes the normed/non-normed Discrete Spaces with Unidimensional
 * 	Real-valued Combinatorial Vector Elements.
 *
 * @author Lakshmi Krishnamurthy
 */

public class CombinatorialRealUnidimensionalVector implements org.drip.spaces.tensor.GeneralizedVectorSpace {
	private java.util.Set<java.lang.Double> _setElementSpace = new java.util.HashSet<java.lang.Double>();

	/**
	 * CombinatorialRealUnidimensionalVector Constructor
	 * 
	 * @param setElementSpace The Set Space of Elements
	 * 
	 * @throws java.lang.Exception Thrown if the Inputs are Invalid
	 */

	public CombinatorialRealUnidimensionalVector (
		final java.util.Set<java.lang.Double> setElementSpace)
		throws java.lang.Exception
	{
		if (null == (_setElementSpace = setElementSpace) || 0 == _setElementSpace.size())
			throw new java.lang.Exception ("CombinatorialRealUnidimensionalVector ctr: Invalid Inputs");
	}

	/**
	 * Retrieve the Full Candidate Set of Elements
	 * 
	 * @return The Full Candidate Set of Elements
	 */

	public java.util.Set<java.lang.Double> elementSpace()
	{
		return _setElementSpace;
	}

	/**
	 * Validate the Input Instance
	 * 
	 * @param dblXInstance The Input Instance
	 * 
	 * @return TRUE => Instance is a Valid Entry in the Space
	 */

	public boolean validateInstance (
		final double dblXInstance)
	{
		return _setElementSpace.contains (dblXInstance);
	}

	@Override public org.drip.spaces.tensor.Cardinality cardinality()
	{
		return org.drip.spaces.tensor.Cardinality.CountablyFinite (_setElementSpace.size());
	}

	@Override public boolean match (
		final org.drip.spaces.tensor.GeneralizedVectorSpace gvsOther)
	{
		if (null == gvsOther || !(gvsOther instanceof CombinatorialRealUnidimensionalVector)) return false;

		CombinatorialRealUnidimensionalVector cruvOther = (CombinatorialRealUnidimensionalVector) gvsOther;

		if (!cardinality().match (cruvOther.cardinality())) return false;

		java.util.Set<java.lang.Double> setElementOther = cruvOther.elementSpace();

		for (double dblElement : _setElementSpace) {
			if (!setElementOther.contains (dblElement)) return false;
		}

		return true;
	}
}
