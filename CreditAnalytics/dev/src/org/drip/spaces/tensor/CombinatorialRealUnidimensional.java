
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
 * CombinatorialRealUnidimensional exposes the normed/non-normed Discrete Spaces with Real-valued
 *  Combinatorial Elements.
 *
 * @author Lakshmi Krishnamurthy
 */

public class CombinatorialRealUnidimensional implements org.drip.spaces.tensor.GeneralizedVectorSpace {
	private java.util.Set<java.lang.Double> _setElementSpace = new java.util.HashSet<java.lang.Double>();

	protected CombinatorialRealUnidimensional (
		final java.util.Set<java.lang.Double> setElementSpace)
		throws java.lang.Exception
	{
		if (null == (_setElementSpace = setElementSpace) || 0 == _setElementSpace.size())
			throw new java.lang.Exception ("CombinatorialRealUnidimensional ctr: Invalid Inputs");
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
	 * @param objInstance The Input Instance
	 * 
	 * @return TRUE => Instance is a Valid Entry in the Space
	 */

	public boolean validateInstance (
		final java.lang.Object objInstance)
	{
		return _setElementSpace.contains (objInstance);
	}

	@Override public org.drip.spaces.tensor.Cardinality cardinality()
	{
		return org.drip.spaces.tensor.Cardinality.CountablyFinite (_setElementSpace.size());
	}
}
