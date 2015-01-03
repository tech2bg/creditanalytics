
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
 * CreditLabel contains the Identifier Parameters referencing the Latent State of the named Credit Curve.
 *  Currently it only contains the Reference Entity Name.
 *  
 * @author Lakshmi Krishnamurthy
 */

public class CreditLabel implements org.drip.state.identifier.LatentStateLabel {
	private java.lang.String _strReferenceEntity = "";

	/**
	 * Make a Standard Credit Label from the Reference Entity Name
	 * 
	 * @param strReferenceEntity The Reference Entity Name
	 * 
	 * @return The Credit Label
	 */

	public static final CreditLabel Standard (
		final java.lang.String strReferenceEntity)
	{
		try {
			return new CreditLabel (strReferenceEntity);
		} catch (java.lang.Exception e) {
			e.printStackTrace();
		}

		return null;
	}

	/**
	 * CreditLabel constructor
	 * 
	 * @param strReferenceEntity The Reference Entity Name
	 * 
	 * @throws java.lang.Exception Thrown if the inputs are invalid
	 */

	private CreditLabel (
		final java.lang.String strReferenceEntity)
		throws java.lang.Exception
	{
		if (null == (_strReferenceEntity = strReferenceEntity) || _strReferenceEntity.isEmpty())
			throw new java.lang.Exception ("CreditLabel ctr: Invalid Inputs");
	}

	@Override public java.lang.String fullyQualifiedName()
	{
		return _strReferenceEntity;
	}

	@Override public boolean match (
		final org.drip.state.identifier.LatentStateLabel lslOther)
	{
		return null == lslOther || !(lslOther instanceof org.drip.state.identifier.CreditLabel) ? false :
			_strReferenceEntity.equalsIgnoreCase (lslOther.fullyQualifiedName());
	}
}
