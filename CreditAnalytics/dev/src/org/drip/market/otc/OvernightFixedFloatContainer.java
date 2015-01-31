
package org.drip.market.otc;

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
 * OvernightFixedFloatContainer holds the settings of the standard OTC Overnight Fix-Float Swap Contract
 *  Conventions.
 *
 * @author Lakshmi Krishnamurthy
 */

public class OvernightFixedFloatContainer {
	private static final java.util.Map<java.lang.String, org.drip.market.otc.FixedFloatSwapConvention>
		_mapConvention = new java.util.TreeMap<java.lang.String,
			org.drip.market.otc.FixedFloatSwapConvention>();

	/**
	 * Initialize the Fix-Float Conventions Container with the pre-set Fix-Float Contracts
	 * 
	 * @return TRUE => The Fix-Float Conventions Container successfully initialized with the pre-set
	 *  Fix-Float Contracts
	 */

	public static final boolean Init()
	{
		try {
			_mapConvention.put ("AUD", new org.drip.market.otc.FixedFloatSwapConvention (new
				org.drip.market.otc.FixedStreamConvention ("AUD", "Act/365", "AUD", "1Y", "1Y",
					org.drip.analytics.support.CompositePeriodBuilder.ACCRUAL_COMPOUNDING_RULE_GEOMETRIC),
						new org.drip.market.otc.FloatStreamConvention (
							org.drip.state.identifier.ForwardLabel.Create (
								org.drip.market.definition.OvernightIndexContainer.IndexFromJurisdiction
									("AUD"), "ON"), "ON"), 1));

			_mapConvention.put ("CAD", new org.drip.market.otc.FixedFloatSwapConvention (new
				org.drip.market.otc.FixedStreamConvention ("CAD", "Act/365", "CAD", "1Y", "1Y",
					org.drip.analytics.support.CompositePeriodBuilder.ACCRUAL_COMPOUNDING_RULE_GEOMETRIC),
						new org.drip.market.otc.FloatStreamConvention (
							org.drip.state.identifier.ForwardLabel.Create (
								org.drip.market.definition.OvernightIndexContainer.IndexFromJurisdiction
									("CAD"), "ON"), "ON"), 0));

			_mapConvention.put ("EUR", new org.drip.market.otc.FixedFloatSwapConvention (new
				org.drip.market.otc.FixedStreamConvention ("EUR", "Act/360", "EUR", "1Y", "1Y",
					org.drip.analytics.support.CompositePeriodBuilder.ACCRUAL_COMPOUNDING_RULE_GEOMETRIC),
						new org.drip.market.otc.FloatStreamConvention (
							org.drip.state.identifier.ForwardLabel.Create (
								org.drip.market.definition.OvernightIndexContainer.IndexFromJurisdiction
									("EUR"), "ON"), "ON"), 2));

			_mapConvention.put ("GBP", new org.drip.market.otc.FixedFloatSwapConvention (new
				org.drip.market.otc.FixedStreamConvention ("GBP", "Act/365", "GBP", "1Y", "1Y",
					org.drip.analytics.support.CompositePeriodBuilder.ACCRUAL_COMPOUNDING_RULE_GEOMETRIC),
						new org.drip.market.otc.FloatStreamConvention (
							org.drip.state.identifier.ForwardLabel.Create (
								org.drip.market.definition.OvernightIndexContainer.IndexFromJurisdiction
									("GBP"), "ON"), "ON"), 1));

			_mapConvention.put ("INR", new org.drip.market.otc.FixedFloatSwapConvention (new
				org.drip.market.otc.FixedStreamConvention ("INR", "Act/365", "INR", "1Y", "1Y",
					org.drip.analytics.support.CompositePeriodBuilder.ACCRUAL_COMPOUNDING_RULE_GEOMETRIC),
						new org.drip.market.otc.FloatStreamConvention (
							org.drip.state.identifier.ForwardLabel.Create (
								org.drip.market.definition.OvernightIndexContainer.IndexFromJurisdiction
									("INR"), "ON"), "ON"), 1));

			_mapConvention.put ("JPY", new org.drip.market.otc.FixedFloatSwapConvention (new
				org.drip.market.otc.FixedStreamConvention ("JPY", "Act/365", "JPY", "1Y", "1Y",
					org.drip.analytics.support.CompositePeriodBuilder.ACCRUAL_COMPOUNDING_RULE_GEOMETRIC),
						new org.drip.market.otc.FloatStreamConvention (
							org.drip.state.identifier.ForwardLabel.Create (
								org.drip.market.definition.OvernightIndexContainer.IndexFromJurisdiction
									("JPY"), "ON"), "ON"), 2));

			_mapConvention.put ("SGD", new org.drip.market.otc.FixedFloatSwapConvention (new
				org.drip.market.otc.FixedStreamConvention ("SGD", "Act/365", "SGD", "1Y", "1Y",
					org.drip.analytics.support.CompositePeriodBuilder.ACCRUAL_COMPOUNDING_RULE_GEOMETRIC),
						new org.drip.market.otc.FloatStreamConvention (
							org.drip.state.identifier.ForwardLabel.Create (
								org.drip.market.definition.OvernightIndexContainer.IndexFromJurisdiction
									("SGD"), "ON"), "ON"), 2));

			_mapConvention.put ("USD", new org.drip.market.otc.FixedFloatSwapConvention (new
				org.drip.market.otc.FixedStreamConvention ("USD", "Act/360", "USD", "1Y", "1Y",
					org.drip.analytics.support.CompositePeriodBuilder.ACCRUAL_COMPOUNDING_RULE_GEOMETRIC),
						new org.drip.market.otc.FloatStreamConvention (
							org.drip.state.identifier.ForwardLabel.Create (
								org.drip.market.definition.OvernightIndexContainer.IndexFromJurisdiction
									("USD"), "ON"), "ON"), 2));
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

	public static final org.drip.market.otc.FixedFloatSwapConvention ConventionFromJurisdiction (
		final java.lang.String strJurisdictionName)
	{
		return null == strJurisdictionName || strJurisdictionName.isEmpty() || !_mapConvention.containsKey
			(strJurisdictionName) ? null : _mapConvention.get (strJurisdictionName);
	}
}
