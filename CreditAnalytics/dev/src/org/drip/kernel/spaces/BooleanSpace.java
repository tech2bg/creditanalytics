
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
 * BooleanSpace implements the normed/non-normed Boolean Spaces.
 *
 * @author Lakshmi Krishnamurthy
 */

public class BooleanSpace extends org.drip.kernel.spaces.CombinatorialSpace {

	/**
	 * Boolean Space "UP"
	 */

	public static final short BS_UP = (short) +1;

	/**
	 * Boolean Space "DOWN"
	 */

	public static final short BS_DOWN = (short) -1;

	public static final BooleanSpace Standard()
	{
		java.util.Set<java.lang.Object> setElementSpace = new java.util.HashSet<java.lang.Object>();

		setElementSpace.add (BS_UP);

		setElementSpace.add (BS_DOWN);

		try {
			return new BooleanSpace (setElementSpace);
		} catch (java.lang.Exception e) {
			e.printStackTrace();
		}

		return null;
	}

	private BooleanSpace (
		final java.util.Set<java.lang.Object> setElementSpace)
		throws java.lang.Exception
	{
		super (setElementSpace);
	}
}
