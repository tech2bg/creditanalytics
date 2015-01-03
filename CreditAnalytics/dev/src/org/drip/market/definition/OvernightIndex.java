
package org.drip.market.definition;

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
 * OvernightIndex contains the definitions of the overnight indexes of different jurisdictions.
 *
 * @author Lakshmi Krishnamurthy
 */

public class OvernightIndex extends org.drip.market.definition.FloaterIndex {
	private int _iPublicationLag = 0;
	private java.lang.String _strReferenceLag = "";

	/**
	 * OvernightIndex Constructor
	 * 
	 * @param strName Index Name
	 * @param strFamily Index Family
	 * @param strCurrency Index Currency
	 * @param strDayCount Index Day Count
	 * @param strCalendar Index Holiday Calendar
	 * @param strReferenceLag Index Reference Lag
	 * @param iPublicationLag Index Publication Lag
	 * @param iAccrualCompoundingRule Accrual Compounding Rule
	 * 
	 * @throws java.lang.Exception Thrown if Inputs are Invalid
	 */

	public OvernightIndex (
		final java.lang.String strName,
		final java.lang.String strFamily,
		final java.lang.String strCurrency,
		final java.lang.String strDayCount,
		final java.lang.String strCalendar,
		final java.lang.String strReferenceLag,
		final int iPublicationLag,
		final int iAccrualCompoundingRule)
		throws java.lang.Exception
	{
		super (strName, strFamily, strCurrency, strDayCount, strCalendar, 0, iAccrualCompoundingRule);

		if (null == (_strReferenceLag = strReferenceLag) || _strReferenceLag.isEmpty())
			throw new java.lang.Exception ("OvernightIndex ctr => Invalid Inputs!");

		_iPublicationLag = iPublicationLag;
	}

	@Override public int spotLag()
	{
		return "ON".equalsIgnoreCase (_strReferenceLag) ? 0 : 1;
	}

	/**
	 * Retrieve the Index Reference Lag
	 * 
	 * @return The Index Reference Lag
	 */

	public java.lang.String referenceLag()
	{
		return _strReferenceLag;
	}

	/**
	 * Retrieve the Index Publication Lag
	 * 
	 * @return The Index Publication Lag
	 */

	public int publicationLag()
	{
		return _iPublicationLag;
	}
}
