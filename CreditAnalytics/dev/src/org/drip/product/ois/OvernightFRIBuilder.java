
package org.drip.product.ois;

/*
 * -*- mode: java; tab-width: 4; indent-tabs-mode: nil; c-basic-offset: 4 -*-
 */

/*!
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
 * OvernightFRIBuilder contains the functionality to build the Jurisdiction-Specific FRI, its retrieval, and
 * 	verification.
 * 
 * @author Lakshmi Krishnamurthy
 */

public class OvernightFRIBuilder {
	private static final
		org.drip.analytics.support.CaseInsensitiveTreeMap<org.drip.product.params.FloatingRateIndex>
			_mapOvernightFRI = new
				org.drip.analytics.support.CaseInsensitiveTreeMap<org.drip.product.params.FloatingRateIndex>();

	private static final
		org.drip.analytics.support.CaseInsensitiveTreeMap<org.drip.product.params.FloatingRateIndex>
			_mapJurisdictionFRI = new
				org.drip.analytics.support.CaseInsensitiveTreeMap<org.drip.product.params.FloatingRateIndex>();

	static {
		org.drip.product.params.FloatingRateIndex friEUR = org.drip.product.params.FloatingRateIndex.Create
			("EUR-EONIA-ON");

		_mapOvernightFRI.put ("EUR-EONIA-ON", friEUR);

		_mapJurisdictionFRI.put ("EUR", friEUR);

		org.drip.product.params.FloatingRateIndex friUSD = org.drip.product.params.FloatingRateIndex.Create
			("USD-OIS-ON");

		_mapOvernightFRI.put ("USD-OIS-ON", friUSD);

		_mapJurisdictionFRI.put ("USD", friUSD);
	};

	/**
	 * Add the FRI identified by its fully qualified name
	 * 
	 * @param strFRIFullName The FRI Fully Qualified Name
	 * 
	 * @return TRUE => Successfully created and Added.
	 */

	public static final boolean Add (
		final java.lang.String strFRIFullName)
	{
		org.drip.product.params.FloatingRateIndex fri = org.drip.product.params.FloatingRateIndex.Create
			(strFRIFullName);

		if (null == fri) return false;

		_mapOvernightFRI.put (strFRIFullName, fri);

		_mapJurisdictionFRI.put (fri.currency(), fri);

		return true;
	}

	/**
	 * Add the specified FRI
	 * 
	 * @param fri The FRI
	 * 
	 * @return TRUE => Successfully Added.
	 */

	public static final boolean Add (
		final org.drip.product.params.FloatingRateIndex fri)
	{
		if (null == fri) return false;

		_mapOvernightFRI.put (fri.fullyQualifiedName(), fri);

		_mapJurisdictionFRI.put (fri.currency(), fri);

		return true;
	}

	/**
	 * Is the Specified FRI an Overnight Index?
	 * 
	 * @param fri The FRI
	 * 
	 * @return TRUE => The Specified FRI corresponds to an Overnight Index
	 */

	public static final boolean IsOvernight (
		final org.drip.product.params.FloatingRateIndex fri)
	{
		if (null == fri) return false;

		return _mapOvernightFRI.containsKey (fri.fullyQualifiedName());
	}

	/**
	 * Is the Specified FRI Name correspond to an Overnight Index?
	 * 
	 * @param strFRIFullName The FRI Fully Qualified Name
	 * 
	 * @return TRUE => The Specified FRI Name corresponds to an Overnight Index
	 */

	public static final boolean IsOvernight (
		final java.lang.String strFRIFullName)
	{
		if (null == strFRIFullName || strFRIFullName.isEmpty()) return false;

		return _mapOvernightFRI.containsKey (strFRIFullName);
	}

	/**
	 * Retrieve the FRI Associated with the Jurisdiction
	 * 
	 * @param strJurisdiction The Jurisdiction Name
	 * 
	 * @return The Jurisdiction FRI
	 */

	public static final org.drip.product.params.FloatingRateIndex JurisdictionFRI (
		final java.lang.String strJurisdiction)
	{
		if (null == strJurisdiction || strJurisdiction.isEmpty()) return null;

		return _mapJurisdictionFRI.get (strJurisdiction);
	}
}
