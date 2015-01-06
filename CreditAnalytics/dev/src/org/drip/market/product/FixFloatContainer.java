
package org.drip.market.product;

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
 * FixFloatContainer holds the settings of the standard OTC fix-float swap contracts.
 *
 * @author Lakshmi Krishnamurthy
 */

public class FixFloatContainer {
	private static final java.util.Map<java.lang.String, org.drip.market.product.FixFloatConvention>
		_mapConvention = new java.util.TreeMap<java.lang.String,
			org.drip.market.product.FixFloatConvention>();

	private static final java.lang.String TenorSubKey (
		final java.lang.String strCurrency,
		final java.lang.String strMaturityTenor)
	{
		if (null == strCurrency) return null;

		try {
			if ("AUD".equalsIgnoreCase (strCurrency))
				return 36 >= org.drip.analytics.support.AnalyticsHelper.TenorToMonths (strMaturityTenor) ?
					"36M" : "MAX";

			if ("CAD".equalsIgnoreCase (strCurrency) || "CHF".equalsIgnoreCase (strCurrency) ||
				"EUR".equalsIgnoreCase (strCurrency) || "GBP".equalsIgnoreCase (strCurrency) ||
					"INR".equalsIgnoreCase (strCurrency))
				return 12 >= org.drip.analytics.support.AnalyticsHelper.TenorToMonths (strMaturityTenor) ?
					"12M" : "MAX";
		} catch (java.lang.Exception e) {
			e.printStackTrace();

			return null;
		}

		return "MAX";
	}

	/**
	 * Initialize the Fix-Float Conventions Container with the pre-set Fix-Float Contracts
	 * 
	 * @return TRUE => The Fix-Float Conventions Container successfully initialized with the pre-set
	 *  Fix-Float Contracts
	 */

	public static final boolean Init()
	{
		try {
			_mapConvention.put ("AUD|ALL|36M|MAIN", new org.drip.market.product.FixFloatConvention (new
				org.drip.market.product.FixedStreamConvention ("AUD", "Act/365", "AUD", "3M", "3M",
					org.drip.analytics.support.CompositePeriodBuilder.ACCRUAL_COMPOUNDING_RULE_GEOMETRIC),
						new org.drip.market.product.FloatStreamConvention (
							org.drip.state.identifier.ForwardLabel.Create (
								org.drip.market.definition.IBORIndexContainer.IndexFromJurisdiction ("AUD"),
									"3M"), "3M"), 1));

			_mapConvention.put ("AUD|ALL|MAX|MAIN", new org.drip.market.product.FixFloatConvention (new
				org.drip.market.product.FixedStreamConvention ("AUD", "Act/365", "AUD", "6M", "6M",
					org.drip.analytics.support.CompositePeriodBuilder.ACCRUAL_COMPOUNDING_RULE_GEOMETRIC),
						new org.drip.market.product.FloatStreamConvention (
							org.drip.state.identifier.ForwardLabel.Create (
								org.drip.market.definition.IBORIndexContainer.IndexFromJurisdiction ("AUD"),
									"6M"), "6M"), 1));

			_mapConvention.put ("CAD|ALL|12M|MAIN", new org.drip.market.product.FixFloatConvention (new
				org.drip.market.product.FixedStreamConvention ("CAD", "Act/365", "CAD", "1Y", "1Y",
					org.drip.analytics.support.CompositePeriodBuilder.ACCRUAL_COMPOUNDING_RULE_GEOMETRIC),
						new org.drip.market.product.FloatStreamConvention (
							org.drip.state.identifier.ForwardLabel.Create (
								org.drip.market.definition.IBORIndexContainer.IndexFromJurisdiction ("CAD"),
									"3M"), "1Y"), 0));

			_mapConvention.put ("CAD|ALL|MAX|MAIN", new org.drip.market.product.FixFloatConvention (new
				org.drip.market.product.FixedStreamConvention ("CAD", "Act/365", "CAD", "6M", "6M",
					org.drip.analytics.support.CompositePeriodBuilder.ACCRUAL_COMPOUNDING_RULE_GEOMETRIC),
						new org.drip.market.product.FloatStreamConvention (
							org.drip.state.identifier.ForwardLabel.Create (
								org.drip.market.definition.IBORIndexContainer.IndexFromJurisdiction ("CAD"),
									"3M"), "6M"), 0));

			_mapConvention.put ("CHF|ALL|12M|MAIN", new org.drip.market.product.FixFloatConvention (new
				org.drip.market.product.FixedStreamConvention ("CHF", "30/360", "CHF", "1Y", "1Y",
					org.drip.analytics.support.CompositePeriodBuilder.ACCRUAL_COMPOUNDING_RULE_GEOMETRIC),
						new org.drip.market.product.FloatStreamConvention (
							org.drip.state.identifier.ForwardLabel.Create (
								org.drip.market.definition.IBORIndexContainer.IndexFromJurisdiction ("CHF"),
									"3M"), "3M"), 2));

			_mapConvention.put ("CHF|ALL|MAX|MAIN", new org.drip.market.product.FixFloatConvention (new
				org.drip.market.product.FixedStreamConvention ("CHF", "30/360", "CHF", "1Y", "1Y",
					org.drip.analytics.support.CompositePeriodBuilder.ACCRUAL_COMPOUNDING_RULE_GEOMETRIC),
						new org.drip.market.product.FloatStreamConvention (
							org.drip.state.identifier.ForwardLabel.Create (
								org.drip.market.definition.IBORIndexContainer.IndexFromJurisdiction ("CHF"),
									"6M"), "6M"), 2));

			_mapConvention.put ("CNY|ALL|MAX|MAIN", new org.drip.market.product.FixFloatConvention (new
				org.drip.market.product.FixedStreamConvention ("CNY", "Act/365", "CNY", "3M", "3M",
					org.drip.analytics.support.CompositePeriodBuilder.ACCRUAL_COMPOUNDING_RULE_GEOMETRIC),
						new org.drip.market.product.FloatStreamConvention (
							org.drip.state.identifier.ForwardLabel.Create (
								org.drip.market.definition.IBORIndexContainer.IndexFromJurisdiction ("CNY"),
									"1W"), "3M"), 2));

			_mapConvention.put ("DKK|ALL|MAX|MAIN", new org.drip.market.product.FixFloatConvention (new
				org.drip.market.product.FixedStreamConvention ("DKK", "30/360", "DKK", "1Y", "1Y",
					org.drip.analytics.support.CompositePeriodBuilder.ACCRUAL_COMPOUNDING_RULE_GEOMETRIC),
						new org.drip.market.product.FloatStreamConvention (
							org.drip.state.identifier.ForwardLabel.Create (
								org.drip.market.definition.IBORIndexContainer.IndexFromJurisdiction ("DKK"),
									"6M"), "6M"), 2));

			_mapConvention.put ("EUR|ALL|12M|MAIN", new org.drip.market.product.FixFloatConvention (new
				org.drip.market.product.FixedStreamConvention ("EUR", "30/360", "EUR", "1Y", "1Y",
					org.drip.analytics.support.CompositePeriodBuilder.ACCRUAL_COMPOUNDING_RULE_GEOMETRIC),
						new org.drip.market.product.FloatStreamConvention (
							org.drip.state.identifier.ForwardLabel.Create (
								org.drip.market.definition.IBORIndexContainer.IndexFromJurisdiction ("EUR"),
									"3M"), "3M"), 2));

			_mapConvention.put ("EUR|ALL|MAX|MAIN", new org.drip.market.product.FixFloatConvention (new
				org.drip.market.product.FixedStreamConvention ("EUR", "30/360", "EUR", "1Y", "1Y",
					org.drip.analytics.support.CompositePeriodBuilder.ACCRUAL_COMPOUNDING_RULE_GEOMETRIC),
						new org.drip.market.product.FloatStreamConvention (
							org.drip.state.identifier.ForwardLabel.Create (
								org.drip.market.definition.IBORIndexContainer.IndexFromJurisdiction ("EUR"),
									"6M"), "6M"), 2));

			_mapConvention.put ("GBP|ALL|12M|MAIN", new org.drip.market.product.FixFloatConvention (new
				org.drip.market.product.FixedStreamConvention ("GBP", "Act/365", "GBP", "1Y", "1Y",
					org.drip.analytics.support.CompositePeriodBuilder.ACCRUAL_COMPOUNDING_RULE_GEOMETRIC),
						new org.drip.market.product.FloatStreamConvention (
							org.drip.state.identifier.ForwardLabel.Create (
								org.drip.market.definition.IBORIndexContainer.IndexFromJurisdiction ("GBP"),
									"3M"), "3M"), 0));

			_mapConvention.put ("GBP|ALL|MAX|MAIN", new org.drip.market.product.FixFloatConvention (new
				org.drip.market.product.FixedStreamConvention ("GBP", "Act/365", "GBP", "6M", "6M",
					org.drip.analytics.support.CompositePeriodBuilder.ACCRUAL_COMPOUNDING_RULE_GEOMETRIC),
						new org.drip.market.product.FloatStreamConvention (
							org.drip.state.identifier.ForwardLabel.Create (
								org.drip.market.definition.IBORIndexContainer.IndexFromJurisdiction ("GBP"),
									"6M"), "6M"), 0));

			_mapConvention.put ("HKD|ALL|MAX|MAIN", new org.drip.market.product.FixFloatConvention (new
				org.drip.market.product.FixedStreamConvention ("HKD", "Act/365", "HKD", "3M", "3M",
					org.drip.analytics.support.CompositePeriodBuilder.ACCRUAL_COMPOUNDING_RULE_GEOMETRIC),
						new org.drip.market.product.FloatStreamConvention (
							org.drip.state.identifier.ForwardLabel.Create (
								org.drip.market.definition.IBORIndexContainer.IndexFromJurisdiction ("HKD"),
									"3M"), "3M"), 0));

			_mapConvention.put ("INR|ALL|12M|MAIN", new org.drip.market.product.FixFloatConvention (new
				org.drip.market.product.FixedStreamConvention ("INR", "Act/365", "INR", "1Y", "1Y",
					org.drip.analytics.support.CompositePeriodBuilder.ACCRUAL_COMPOUNDING_RULE_GEOMETRIC),
						new org.drip.market.product.FloatStreamConvention (
							org.drip.state.identifier.ForwardLabel.Create (
								org.drip.market.definition.IBORIndexContainer.IndexFromJurisdiction ("INR"),
									"3M"), "3M"), 2));

			_mapConvention.put ("INR|ALL|MAX|MAIN", new org.drip.market.product.FixFloatConvention (new
				org.drip.market.product.FixedStreamConvention ("INR", "Act/365", "INR", "6M", "6M",
					org.drip.analytics.support.CompositePeriodBuilder.ACCRUAL_COMPOUNDING_RULE_GEOMETRIC),
						new org.drip.market.product.FloatStreamConvention (
							org.drip.state.identifier.ForwardLabel.Create (
								org.drip.market.definition.IBORIndexContainer.IndexFromJurisdiction ("INR"),
									"6M"), "6M"), 2));

			_mapConvention.put ("JPY|ALL|MAX|MAIN", new org.drip.market.product.FixFloatConvention (new
				org.drip.market.product.FixedStreamConvention ("JPY", "Act/365", "JPY", "6M", "6M",
					org.drip.analytics.support.CompositePeriodBuilder.ACCRUAL_COMPOUNDING_RULE_GEOMETRIC),
						new org.drip.market.product.FloatStreamConvention (
							org.drip.state.identifier.ForwardLabel.Create (
								org.drip.market.definition.IBORIndexContainer.IndexFromJurisdiction ("JPY"),
									"6M"), "6M"), 2));

			_mapConvention.put ("JPY|ALL|MAX|TIBOR", new org.drip.market.product.FixFloatConvention (new
				org.drip.market.product.FixedStreamConvention ("JPY", "Act/365", "JPY", "6M", "6M",
					org.drip.analytics.support.CompositePeriodBuilder.ACCRUAL_COMPOUNDING_RULE_GEOMETRIC),
						new org.drip.market.product.FloatStreamConvention (
							org.drip.state.identifier.ForwardLabel.Create (
								org.drip.market.definition.IBORIndexContainer.IndexFromName ("JPY-TIBOR"),
									"3M"), "3M"), 2));

			_mapConvention.put ("NOK|ALL|MAX|MAIN", new org.drip.market.product.FixFloatConvention (new
				org.drip.market.product.FixedStreamConvention ("NOK", "30/360", "NOK", "1Y", "1Y",
					org.drip.analytics.support.CompositePeriodBuilder.ACCRUAL_COMPOUNDING_RULE_GEOMETRIC),
						new org.drip.market.product.FloatStreamConvention (
							org.drip.state.identifier.ForwardLabel.Create (
								org.drip.market.definition.IBORIndexContainer.IndexFromJurisdiction ("NOK"),
									"6M"), "6M"), 2));

			_mapConvention.put ("NZD|ALL|MAX|MAIN", new org.drip.market.product.FixFloatConvention (new
				org.drip.market.product.FixedStreamConvention ("NZD", "Act/365", "NZD", "6M", "6M",
					org.drip.analytics.support.CompositePeriodBuilder.ACCRUAL_COMPOUNDING_RULE_GEOMETRIC),
						new org.drip.market.product.FloatStreamConvention (
							org.drip.state.identifier.ForwardLabel.Create (
								org.drip.market.definition.IBORIndexContainer.IndexFromJurisdiction ("NZD"),
									"3M"), "3M"), 0));

			_mapConvention.put ("PLN|ALL|MAX|MAIN", new org.drip.market.product.FixFloatConvention (new
				org.drip.market.product.FixedStreamConvention ("PLN", "Act/Act ISDA", "PLN", "1Y", "1Y",
					org.drip.analytics.support.CompositePeriodBuilder.ACCRUAL_COMPOUNDING_RULE_GEOMETRIC),
						new org.drip.market.product.FloatStreamConvention (
							org.drip.state.identifier.ForwardLabel.Create (
								org.drip.market.definition.IBORIndexContainer.IndexFromJurisdiction ("PLN"),
									"6M"), "6M"), 2));

			_mapConvention.put ("SEK|ALL|MAX|MAIN", new org.drip.market.product.FixFloatConvention (new
				org.drip.market.product.FixedStreamConvention ("SEK", "30/360", "SEK", "1Y", "1Y",
					org.drip.analytics.support.CompositePeriodBuilder.ACCRUAL_COMPOUNDING_RULE_GEOMETRIC),
						new org.drip.market.product.FloatStreamConvention (
							org.drip.state.identifier.ForwardLabel.Create (
								org.drip.market.definition.IBORIndexContainer.IndexFromJurisdiction ("SEK"),
									"6M"), "6M"), 2));

			_mapConvention.put ("SGD|ALL|MAX|MAIN", new org.drip.market.product.FixFloatConvention (new
				org.drip.market.product.FixedStreamConvention ("SGD", "Act/365", "SGD", "6M", "6M",
					org.drip.analytics.support.CompositePeriodBuilder.ACCRUAL_COMPOUNDING_RULE_GEOMETRIC),
						new org.drip.market.product.FloatStreamConvention (
							org.drip.state.identifier.ForwardLabel.Create (
								org.drip.market.definition.IBORIndexContainer.IndexFromJurisdiction ("SGD"),
									"6M"), "6M"), 2));

			_mapConvention.put ("USD|ALL|MAX|MAIN", new org.drip.market.product.FixFloatConvention (new
				org.drip.market.product.FixedStreamConvention ("USD", "30/360", "USD", "6M", "6M",
					org.drip.analytics.support.CompositePeriodBuilder.ACCRUAL_COMPOUNDING_RULE_GEOMETRIC),
						new org.drip.market.product.FloatStreamConvention (
							org.drip.state.identifier.ForwardLabel.Create (
								org.drip.market.definition.IBORIndexContainer.IndexFromJurisdiction ("USD"),
									"3M"), "3M"), 2));

			_mapConvention.put ("USD|LON|MAX|MAIN", new org.drip.market.product.FixFloatConvention (new
				org.drip.market.product.FixedStreamConvention ("USD", "Act/360", "USD", "1Y", "1Y",
					org.drip.analytics.support.CompositePeriodBuilder.ACCRUAL_COMPOUNDING_RULE_GEOMETRIC),
						new org.drip.market.product.FloatStreamConvention (
							org.drip.state.identifier.ForwardLabel.Create (
								org.drip.market.definition.IBORIndexContainer.IndexFromJurisdiction ("USD"),
									"3M"), "3M"), 2));

			_mapConvention.put ("USD|NYC|MAX|MAIN", new org.drip.market.product.FixFloatConvention (new
				org.drip.market.product.FixedStreamConvention ("USD", "30/360", "USD", "6M", "6M",
					org.drip.analytics.support.CompositePeriodBuilder.ACCRUAL_COMPOUNDING_RULE_GEOMETRIC),
						new org.drip.market.product.FloatStreamConvention (
							org.drip.state.identifier.ForwardLabel.Create (
								org.drip.market.definition.IBORIndexContainer.IndexFromJurisdiction ("USD"),
									"3M"), "3M"), 2));

			_mapConvention.put ("ZAR|ALL|MAX|MAIN", new org.drip.market.product.FixFloatConvention (new
				org.drip.market.product.FixedStreamConvention ("ZAR", "Act/365", "ZAR", "3M", "3M",
					org.drip.analytics.support.CompositePeriodBuilder.ACCRUAL_COMPOUNDING_RULE_GEOMETRIC),
						new org.drip.market.product.FloatStreamConvention (
							org.drip.state.identifier.ForwardLabel.Create (
								org.drip.market.definition.IBORIndexContainer.IndexFromJurisdiction ("ZAR"),
									"3M"), "3M"), 0));
		} catch (java.lang.Exception e) {
			e.printStackTrace();

			return false;
		}

		return true;
	}

	/**
	 * Retrieve the Fix-Float Convention for the specified Jurisdiction
	 * 
	 * @param strJurisdictionName The Jurisdiction Name
	 * 
	 * @return The Fix-Float Convention
	 */

	public static final org.drip.market.product.FixFloatConvention ConventionFromJurisdiction (
		final java.lang.String strJurisdictionName)
	{
		if (null == strJurisdictionName) return null;

		java.lang.String strKey = strJurisdictionName + "|ALL|MAX|MAIN";

		return _mapConvention.containsKey (strKey) ? _mapConvention.get (strKey) : null;
	}

	/**
	 * Retrieve the Fix-Float Convention for the specified Jurisdiction for the specified Maturity Tenor
	 * 
	 * @param strJurisdictionName The Jurisdiction Name
	 * @param strMaturityTenor The Maturity Tenor
	 * 
	 * @return The Fix-Float Convention
	 */

	public static final org.drip.market.product.FixFloatConvention ConventionFromJurisdictionMaturity (
		final java.lang.String strJurisdictionName,
		final java.lang.String strMaturityTenor)
	{
		if (null == strJurisdictionName || null == strMaturityTenor) return null;

		java.lang.String strKey = strJurisdictionName + "|ALL|" + TenorSubKey (strJurisdictionName,
			strMaturityTenor) + "|MAIN";

		return _mapConvention.containsKey (strKey) ? _mapConvention.get (strKey) : null;
	}

	/**
	 * Retrieve the Fix-Float Convention for the specified Jurisdiction for the specified Location
	 * 
	 * @param strJurisdictionName The Jurisdiction Name
	 * @param strLocation The Location
	 * 
	 * @return The Fix-Float Convention
	 */

	public static final org.drip.market.product.FixFloatConvention ConventionFromJurisdictionLocation (
		final java.lang.String strJurisdictionName,
		final java.lang.String strLocation)
	{
		if (null == strJurisdictionName || null == strLocation) return null;

		java.lang.String strKey = strJurisdictionName + "|" + strLocation + "|MAX|MAIN";

		return _mapConvention.containsKey (strKey) ? _mapConvention.get (strKey) : null;
	}

	/**
	 * Retrieve the Fix-Float Convention for the specified Jurisdiction for the specified Index
	 * 
	 * @param strJurisdictionName The Jurisdiction Name
	 * @param strIndexName The Index Name
	 * 
	 * @return The Fix-Float Convention
	 */

	public static final org.drip.market.product.FixFloatConvention ConventionFromJurisdictionIndex (
		final java.lang.String strJurisdictionName,
		final java.lang.String strIndexName)
	{
		if (null == strJurisdictionName || null == strIndexName) return null;

		java.lang.String strKey = strJurisdictionName + "|ALL|MAX|" + strIndexName;

		return _mapConvention.containsKey (strKey) ? _mapConvention.get (strKey) : null;
	}

	/**
	 * Retrieve the Fix-Float Convention for the specified Jurisdiction for the specified Index, Location,
	 * 	and Maturity Tenor
	 * 
	 * @param strJurisdictionName The Jurisdiction Name
	 * @param strLocation The Location
	 * @param strMaturityTenor Maturity Tenor
	 * @param strIndexName The Index Name
	 * 
	 * @return The Fix-Float Convention
	 */

	public static final org.drip.market.product.FixFloatConvention ConventionFromJurisdiction (
		final java.lang.String strJurisdictionName,
		final java.lang.String strLocation,
		final java.lang.String strMaturityTenor,
		final java.lang.String strIndexName)
	{
		if (null == strJurisdictionName || null == strLocation || null == strMaturityTenor || null ==
			strIndexName)
			return null;

		java.lang.String strKey = strJurisdictionName + "|" + strLocation + "|" + TenorSubKey
			(strJurisdictionName, strMaturityTenor) + "|" + strIndexName;

		return _mapConvention.containsKey (strKey) ? _mapConvention.get (strKey) : null;
	}
}
