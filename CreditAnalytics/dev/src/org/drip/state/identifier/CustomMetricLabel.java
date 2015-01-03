
package org.drip.state.identifier;

/*
 * -*- mode: java; tab-width: 4; indent-tabs-mode: nil; c-basic-offset: 4 -*-
 */

/*!
 * Copyright (C) 2015 Lakshmi Krishnamurthy
 * Copyright (C) 2014 Lakshmi Krishnamurthy
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
 * CustomMetricLabel contains the Identifier Parameters referencing the Latent State of the named Custom
 *  Metric. Currently it only contains the Arbitrarily Assigned Label.
 *  
 * @author Lakshmi Krishnamurthy
 */

public class CustomMetricLabel implements org.drip.state.identifier.LatentStateLabel {
	private java.lang.String _strLabel = "";

	/**
	 * Make a Standard Custom Metric Label from the Reference Entity Name
	 * 
	 * @param strLabel The Custom Metric Label
	 * 
	 * @return The Custom Metric Label
	 */

	public static final CustomMetricLabel Standard (
		final java.lang.String strLabel)
	{
		try {
			return new CustomMetricLabel (strLabel);
		} catch (java.lang.Exception e) {
			e.printStackTrace();
		}

		return null;
	}

	/**
	 * CustomMetricLabel constructor
	 * 
	 * @param strLabel The Custom Metric Label
	 * 
	 * @throws java.lang.Exception Thrown if the inputs are invalid
	 */

	private CustomMetricLabel (
		final java.lang.String strLabel)
		throws java.lang.Exception
	{
		if (null == (_strLabel = strLabel) || _strLabel.isEmpty())
			throw new java.lang.Exception ("CustomMetricLabel ctr: Invalid Inputs");
	}

	@Override public java.lang.String fullyQualifiedName()
	{
		return _strLabel;
	}

	@Override public boolean match (
		final org.drip.state.identifier.LatentStateLabel lslOther)
	{
		return null == lslOther || !(lslOther instanceof org.drip.state.identifier.CustomMetricLabel) ? false
			: _strLabel.equalsIgnoreCase (lslOther.fullyQualifiedName());
	}
}
