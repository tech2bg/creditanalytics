
package org.drip.market.definition;

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
 * OvernightIndexContainer holds the definitions of the overnight index definitions corresponding to
 *  different jurisdictions.
 *
 * @author Lakshmi Krishnamurthy
 */

public class OvernightIndexContainer {
	private static final
		org.drip.analytics.support.CaseInsensitiveTreeMap<org.drip.market.definition.OvernightIndex>
			_mapJurisdictionOvernightIndex = new
				org.drip.analytics.support.CaseInsensitiveTreeMap<org.drip.market.definition.OvernightIndex>();

	private static final
		org.drip.analytics.support.CaseInsensitiveTreeMap<org.drip.market.definition.OvernightIndex>
			_mapNamedOvernightIndex = new
				org.drip.analytics.support.CaseInsensitiveTreeMap<org.drip.market.definition.OvernightIndex>();

	/**
	 * Initialize the Overnight Index Container with the Overnight Indexes
	 * 
	 * @return TRUE => The Overnight Index Container successfully initialized with the indexes
	 */

	public static final boolean Init()
	{
		try {
			org.drip.market.definition.OvernightIndex oiAUD = new org.drip.market.definition.OvernightIndex
				("RBA ON AONIA", "AUD", "Act/365", "ON", 0);

			_mapJurisdictionOvernightIndex.put ("AUD", oiAUD);

			_mapNamedOvernightIndex.put ("AONIA", oiAUD);

			_mapNamedOvernightIndex.put ("RBA ON AONIA", oiAUD);

			org.drip.market.definition.OvernightIndex oiCAD = new org.drip.market.definition.OvernightIndex
				("CORRA", "CAD", "Act/365", "ON", 1);

			_mapJurisdictionOvernightIndex.put ("CAD", oiCAD);

			_mapNamedOvernightIndex.put ("CORRA", oiCAD);

			org.drip.market.definition.OvernightIndex oiCHF = new org.drip.market.definition.OvernightIndex
				("TOIS", "CHF", "Act/360", "TN", -1);

			_mapJurisdictionOvernightIndex.put ("CHF", oiCHF);

			_mapNamedOvernightIndex.put ("TOIS", oiCHF);

			org.drip.market.definition.OvernightIndex oiCZK = new org.drip.market.definition.OvernightIndex
				("CZEONIA", "CZK", "Act/360", "ON", 0);

			_mapJurisdictionOvernightIndex.put ("CZK", oiCZK);

			_mapNamedOvernightIndex.put ("CZEONIA", oiCZK);

			org.drip.market.definition.OvernightIndex oiDKK = new org.drip.market.definition.OvernightIndex
				("DNB TN", "DKK", "Act/360", "TN", -1);

			_mapJurisdictionOvernightIndex.put ("DKK", oiDKK);

			_mapNamedOvernightIndex.put ("DNBTN", oiDKK);

			_mapNamedOvernightIndex.put ("DNB TN", oiDKK);

			org.drip.market.definition.OvernightIndex oiEUR = new org.drip.market.definition.OvernightIndex
				("EONIA", "EUR", "Act/360", "ON", 0);

			_mapJurisdictionOvernightIndex.put ("EUR", oiEUR);

			_mapNamedOvernightIndex.put ("EONIA", oiEUR);

			org.drip.market.definition.OvernightIndex oiGBP = new org.drip.market.definition.OvernightIndex
				("SONIA", "GBP", "Act/365", "ON", 0);

			_mapJurisdictionOvernightIndex.put ("GBP", oiGBP);

			_mapNamedOvernightIndex.put ("SONIA", oiGBP);

			org.drip.market.definition.OvernightIndex oiHKD = new org.drip.market.definition.OvernightIndex
				("HONIX", "HKD", "Act/365", "ON", 0);

			_mapJurisdictionOvernightIndex.put ("HKD", oiHKD);

			_mapNamedOvernightIndex.put ("HONIX", oiHKD);

			org.drip.market.definition.OvernightIndex oiHUF = new org.drip.market.definition.OvernightIndex
				("HUFONIA", "HUF", "Act/360", "ON", 0);

			_mapJurisdictionOvernightIndex.put ("HUF", oiHUF);

			_mapNamedOvernightIndex.put ("HUFONIA", oiHUF);

			org.drip.market.definition.OvernightIndex oiINR = new org.drip.market.definition.OvernightIndex
				("ON MIBOR", "INR", "Act/365", "ON", 0);

			_mapJurisdictionOvernightIndex.put ("INR", oiINR);

			_mapNamedOvernightIndex.put ("MIBOR", oiINR);

			_mapNamedOvernightIndex.put ("ON MIBOR", oiINR);

			org.drip.market.definition.OvernightIndex oiINR2 = new org.drip.market.definition.OvernightIndex
				("MITOR", "INR", "Act/365", "TN", 0);

			_mapJurisdictionOvernightIndex.put ("INR2", oiINR2);

			_mapNamedOvernightIndex.put ("MITOR", oiINR2);

			org.drip.market.definition.OvernightIndex oiJPY = new org.drip.market.definition.OvernightIndex
				("TONAR", "JPY", "Act/365", "ON", 1);

			_mapJurisdictionOvernightIndex.put ("JPY", oiJPY);

			_mapNamedOvernightIndex.put ("TONAR", oiJPY);

			org.drip.market.definition.OvernightIndex oiNZD = new org.drip.market.definition.OvernightIndex
				("NZIONA", "NZD", "Act/365", "ON", 0);

			_mapJurisdictionOvernightIndex.put ("NZD", oiNZD);

			_mapNamedOvernightIndex.put ("NZIONA", oiNZD);

			org.drip.market.definition.OvernightIndex oiPLN = new org.drip.market.definition.OvernightIndex
				("POLONIA", "PLN", "Act/365", "ON", 0);

			_mapJurisdictionOvernightIndex.put ("PLN", oiPLN);

			_mapNamedOvernightIndex.put ("POLONIA", oiPLN);

			org.drip.market.definition.OvernightIndex oiSEK = new org.drip.market.definition.OvernightIndex
				("SIOR TN STIBOR", "SEK", "Act/360", "1N", -1);

			_mapJurisdictionOvernightIndex.put ("SEK", oiSEK);

			_mapNamedOvernightIndex.put ("SIOR", oiSEK);

			_mapNamedOvernightIndex.put ("SIOR TN STIBOR", oiSEK);

			_mapNamedOvernightIndex.put ("STIBOR", oiSEK);

			org.drip.market.definition.OvernightIndex oiSGD = new org.drip.market.definition.OvernightIndex
				("SONAR", "SGD", "Act/365", "ON", 0);

			_mapJurisdictionOvernightIndex.put ("SGD", oiSGD);

			_mapNamedOvernightIndex.put ("SONAR", oiSGD);

			org.drip.market.definition.OvernightIndex oiUSD = new org.drip.market.definition.OvernightIndex
				("Fed Fund", "USD", "Act/360", "ON", 1);

			_mapJurisdictionOvernightIndex.put ("USD", oiUSD);

			_mapNamedOvernightIndex.put ("Fed Fund", oiUSD);

			_mapNamedOvernightIndex.put ("FedFund", oiUSD);

			org.drip.market.definition.OvernightIndex oiZAR = new org.drip.market.definition.OvernightIndex
				("SAFEX ON Dep Rate", "ZAR", "Act/365", "ON", 0);

			_mapJurisdictionOvernightIndex.put ("ZAR", oiZAR);

			_mapNamedOvernightIndex.put ("SAFEX", oiZAR);

			_mapNamedOvernightIndex.put ("SAFEX ON Dep Rate", oiZAR);

			org.drip.market.definition.OvernightIndex oiZAR2 = new org.drip.market.definition.OvernightIndex
				("SAONIA", "ZAR", "Act/365", "ON", 0);

			_mapJurisdictionOvernightIndex.put ("ZAR2", oiZAR2);

			_mapNamedOvernightIndex.put ("SAONIA", oiZAR2);
		} catch (java.lang.Exception e) {
			e.printStackTrace();

			return false;
		}

		return true;
	}

	/**
	 * Retrieve the Overnight Index from the Jurisdiction Name
	 * 
	 * @param strName The Overnight Index Jurisdiction Name
	 * 
	 * @return The Overnight Index
	 */

	public static final org.drip.market.definition.OvernightIndex IndexFromJurisdiction (
		final java.lang.String strName)
	{
		return _mapJurisdictionOvernightIndex.containsKey (strName) ? _mapJurisdictionOvernightIndex.get
			(strName) : null;
	}

	/**
	 * Retrieve the Overnight Index from the Index Name
	 * 
	 * @param strName The Overnight Index Index Name
	 * 
	 * @return The Overnight Index
	 */

	public static final org.drip.market.definition.OvernightIndex IndexFromName (
		final java.lang.String strName)
	{
		return _mapNamedOvernightIndex.containsKey (strName) ? _mapNamedOvernightIndex.get (strName) : null;
	}
}
