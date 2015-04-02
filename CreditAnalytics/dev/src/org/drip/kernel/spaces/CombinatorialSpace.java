
package org.drip.kernel.spaces;

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
 * CombinatorialSpace exposes the normed/non-normed Discrete Spaces with Combinatorial Elements.
 *
 * @author Lakshmi Krishnamurthy
 */

public class CombinatorialSpace {
	private java.util.Set<java.lang.Object> _setElementSpace = new java.util.HashSet<java.lang.Object>();

	protected CombinatorialSpace (
		final java.util.Set<java.lang.Object> setElementSpace)
		throws java.lang.Exception
	{
		if (null == (_setElementSpace = setElementSpace) || 0 == _setElementSpace.size())
			throw new java.lang.Exception ("CombinatorialSpace ctr: Invalid Inputs");
	}

	/**
	 * Retrieve the Full Space of Elements
	 * 
	 * @return The Full Space of Elements
	 */

	public java.util.Set<java.lang.Object> elementSpace()
	{
		return _setElementSpace;
	}

	/**
	 * Validate the Input Element
	 * 
	 * @param objElem The Input Element
	 * 
	 * @return TRUE => Element is a Valid Entry in the Space
	 */

	public boolean validateElement (
		final java.lang.Object objElem)
	{
		return null != objElem && _setElementSpace.contains (objElem);
	}
}
