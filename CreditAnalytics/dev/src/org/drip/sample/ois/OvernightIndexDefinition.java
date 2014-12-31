
package org.drip.sample.ois;

import org.drip.analytics.support.CompositePeriodBuilder;
import org.drip.market.definition.*;
import org.drip.service.api.CreditAnalytics;

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
 * OvernightIndexDefinition demonstrates the functionality to retrieve the Overnight Index settings for the
 * 	various Jurisdictions.
 * 
 * @author Lakshmi Krishnamurthy
 */

public class OvernightIndexDefinition {
	private static final String AccrualType (
		final int iAccrualCompounding)
	{
		return CompositePeriodBuilder.ACCRUAL_COMPOUNDING_RULE_ARITHMETIC == iAccrualCompounding ? "ARITHMETIC" : " GEOMETRIC";
	}

	private static final void DisplayJurisdictionOvernightSetting (
		final String strJurisdiction)
	{
		OvernightIndex index = OvernightIndexContainer.IndexFromJurisdiction (strJurisdiction);

		System.out.println ("\t[" +
			index.currency() + "] => " +
			index.dayCount() + " | " +
			AccrualType (index.accrualCompoundingRule()) + " | " +
			index.referenceLag() + " | " +
			index.publicationLag() + " | " + 
			index.name()
		);
	}

	public static final void main (
		String[] args)
	{
		CreditAnalytics.Init ("");

		System.out.println ("\n\t---------------\n\t---------------\n");

		DisplayJurisdictionOvernightSetting ("CHF");

		DisplayJurisdictionOvernightSetting ("EUR");

		DisplayJurisdictionOvernightSetting ("GBP");

		DisplayJurisdictionOvernightSetting ("JPY");

		DisplayJurisdictionOvernightSetting ("USD");

		System.out.println ("\n\t---------------\n\t---------------\n");

		DisplayJurisdictionOvernightSetting ("AUD");

		DisplayJurisdictionOvernightSetting ("BRL");

		DisplayJurisdictionOvernightSetting ("CAD");

		DisplayJurisdictionOvernightSetting ("CZK");

		DisplayJurisdictionOvernightSetting ("DKK");

		DisplayJurisdictionOvernightSetting ("HKD");

		DisplayJurisdictionOvernightSetting ("HUF");

		DisplayJurisdictionOvernightSetting ("INR");

		DisplayJurisdictionOvernightSetting ("NZD");

		DisplayJurisdictionOvernightSetting ("PLN");

		DisplayJurisdictionOvernightSetting ("SEK");

		DisplayJurisdictionOvernightSetting ("SGD");

		DisplayJurisdictionOvernightSetting ("ZAR");

		DisplayJurisdictionOvernightSetting ("INR2");

		DisplayJurisdictionOvernightSetting ("ZAR2");
	}
}
