
package org.drip.state.identifier;

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
 * VolatilityLabel contains the Identifier Parameters referencing the Latent State of the named Volatility
 *  Curve. Currently it only contains the label of the underlying Latent State.
 *  
 * @author Lakshmi Krishnamurthy
 */

public class VolatilityLabel implements org.drip.state.identifier.LatentStateLabel {
	private org.drip.state.identifier.LatentStateLabel _lslUnderlyingState = null;

	/**
	 * Make a Standard Volatility Latent State Label from the Underlying Latent State Label
	 * 
	 * @param lslUnderlyingState Underlying Latent State Label
	 * 
	 * @return The Volatility Label
	 */

	public static final VolatilityLabel Standard (
		final org.drip.state.identifier.LatentStateLabel lslUnderlyingState)
	{
		try {
			return new VolatilityLabel (lslUnderlyingState);
		} catch (java.lang.Exception e) {
			e.printStackTrace();
		}

		return null;
	}

	/**
	 * VolatilityLabel constructor
	 * 
	 * @param lslUnderlyingState Underlying Latent State Label
	 * 
	 * @throws java.lang.Exception Thrown if the inputs are invalid
	 */

	private VolatilityLabel (
		final org.drip.state.identifier.LatentStateLabel lslUnderlyingState)
		throws java.lang.Exception
	{
		if (null == (_lslUnderlyingState = lslUnderlyingState))
			throw new java.lang.Exception ("VolatilityLabel ctr: Invalid Inputs");
	}

	@Override public java.lang.String fullyQualifiedName()
	{
		return _lslUnderlyingState.fullyQualifiedName() + "::VOL";
	}

	@Override public boolean match (
		final org.drip.state.identifier.LatentStateLabel lslOther)
	{
		return null == lslOther || !(lslOther instanceof org.drip.state.identifier.VolatilityLabel) ? false :
			fullyQualifiedName().equalsIgnoreCase (lslOther.fullyQualifiedName());
	}
}
