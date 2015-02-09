
package org.drip.market.exchange;

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
 * BondFuturesConventionContainer holds the Details of the Bond Futures Contracts.
 *
 * @author Lakshmi Krishnamurthy
 */

public class BondFuturesConventionContainer {
	private static final java.util.Map<java.lang.String, org.drip.market.exchange.BondFuturesConvention>
		_mapFutures = new
			java.util.TreeMap<java.lang.String, org.drip.market.exchange.BondFuturesConvention>();

	/**
	 * Initialize the Bond Futures Convention Container with the Conventions
	 * 
	 * @return TRUE => The Bond Futures Convention Container successfully initialized with the Conventions
	 */

	public static final boolean Init()
	{
		try {
			_mapFutures.put ("AUD-BANK-BILLS-3M", new org.drip.market.exchange.BondFuturesConvention
				("AUD-BANKBILLS-3M", new java.lang.String[] {"AUD-BANKBILLS-3M"}, "AUD", "3M", 1000000.,
					100000., new java.lang.String[] {"ASX"}, "BANK", "BILLS", new
						org.drip.analytics.eventday.DateInMonth
							(org.drip.analytics.eventday.DateInMonth.INSTANCE_GENERATOR_RULE_WEEK_DAY, false,
								-1, org.drip.analytics.date.DateUtil.FRIDAY, 2), new
									org.drip.market.exchange.BondFuturesEligibility ("85D", "95D", new
										java.lang.String[]
											{"Australia and New Zealand Banking Group Limited",
												"Commonwealth Bank of Australia",
													"National Australia Bank Limited",
														"Westpac Banking Corporation"}, 0.), new
															org.drip.market.exchange.BondFuturesSettle (1, 1,
																org.drip.market.exchange.BondFuturesSettle.SETTLE_TYPE_PHYSICAL_DELIVERY,
				org.drip.market.exchange.BondFuturesSettle.QUOTE_REFERENCE_INDEX_FLAT, false,
					java.lang.Double.NaN, java.lang.Double.NaN)));

			_mapFutures.put ("USD-TREASURY-BOND-ULTRA", new org.drip.market.exchange.BondFuturesConvention
				("ULTRA T-BOND", new java.lang.String[] {"UB", "UL", "UBE"}, "USD", "ULTRA", 100000., 0., new
					java.lang.String[] {"CBOT"}, "TREASURY", "BOND", new
						org.drip.analytics.eventday.DateInMonth
							(org.drip.analytics.eventday.DateInMonth.INSTANCE_GENERATOR_RULE_EDGE_LAG, true,
								7, -1, -1), new org.drip.market.exchange.BondFuturesEligibility ("25Y",
									"MAX", new java.lang.String[] {"US Government Bonds"}, 0.), new
										org.drip.market.exchange.BondFuturesSettle (1, 1,
											org.drip.market.exchange.BondFuturesSettle.SETTLE_TYPE_PHYSICAL_DELIVERY,
				org.drip.market.exchange.BondFuturesSettle.QUOTE_REFERENCE_INDEX_CONVERSION_FACTOR, false,
					0.06, 0.06)));

			_mapFutures.put ("USD-TREASURY-BOND-30Y", new org.drip.market.exchange.BondFuturesConvention
				("USD 30-YR BOND", new java.lang.String[] {"ZB", "US"}, "USD", "30Y", 100000., 0., new
					java.lang.String[] {"CBOT"}, "TREASURY", "BOND", new
						org.drip.analytics.eventday.DateInMonth
							(org.drip.analytics.eventday.DateInMonth.INSTANCE_GENERATOR_RULE_EDGE_LAG, true,
								7, -1, -1), new org.drip.market.exchange.BondFuturesEligibility ("15Y",
									"25Y", new java.lang.String[] {"US Government Bonds"}, 0.), new
										org.drip.market.exchange.BondFuturesSettle (1, 1,
											org.drip.market.exchange.BondFuturesSettle.SETTLE_TYPE_PHYSICAL_DELIVERY,
				org.drip.market.exchange.BondFuturesSettle.QUOTE_REFERENCE_INDEX_CONVERSION_FACTOR, false,
					0.06, 0.06)));

			_mapFutures.put ("USD-TREASURY-NOTE-10Y", new org.drip.market.exchange.BondFuturesConvention
				("USD 10-YR NOTE", new java.lang.String[] {"ZN", "TY"}, "USD", "10Y", 100000., 0., new
					java.lang.String[] {"CBOT"}, "TREASURY", "NOTE", new
						org.drip.analytics.eventday.DateInMonth
							(org.drip.analytics.eventday.DateInMonth.INSTANCE_GENERATOR_RULE_EDGE_LAG, true,
								7, -1, -1), new org.drip.market.exchange.BondFuturesEligibility ("78M",
									"10Y", new java.lang.String[] {"US Government Bonds"}, 0.), new
										org.drip.market.exchange.BondFuturesSettle (1, 1,
											org.drip.market.exchange.BondFuturesSettle.SETTLE_TYPE_PHYSICAL_DELIVERY,
				org.drip.market.exchange.BondFuturesSettle.QUOTE_REFERENCE_INDEX_CONVERSION_FACTOR, false,
					0.06, 0.06)));

			_mapFutures.put ("USD-TREASURY-NOTE-5Y", new org.drip.market.exchange.BondFuturesConvention
				("USD 5-YR NOTE", new java.lang.String[] {"ZF", "FV"}, "USD", "5Y", 100000., 0., new
					java.lang.String[] {"CBOT"}, "TREASURY", "NOTE", new
						org.drip.analytics.eventday.DateInMonth
							(org.drip.analytics.eventday.DateInMonth.INSTANCE_GENERATOR_RULE_EDGE_LAG, true,
								7, -1, -1), new org.drip.market.exchange.BondFuturesEligibility ("50M",
									"63M", new java.lang.String[] {"US Government Bonds"}, 0.), new
										org.drip.market.exchange.BondFuturesSettle (1, 1,
											org.drip.market.exchange.BondFuturesSettle.SETTLE_TYPE_PHYSICAL_DELIVERY,
				org.drip.market.exchange.BondFuturesSettle.QUOTE_REFERENCE_INDEX_CONVERSION_FACTOR, false,
					0.06, 0.06)));

			_mapFutures.put ("USD-TREASURY-NOTE-3Y", new org.drip.market.exchange.BondFuturesConvention
				("USD 3-YR NOTE", new java.lang.String[] {"Z3N", "3YR"}, "USD", "3Y", 200000., 0., new
					java.lang.String[] {"CBOT"}, "TREASURY", "NOTE", new
						org.drip.analytics.eventday.DateInMonth
							(org.drip.analytics.eventday.DateInMonth.INSTANCE_GENERATOR_RULE_EDGE_LAG, true,
								7, -1, -1), new org.drip.market.exchange.BondFuturesEligibility ("33M", "3Y",
									new java.lang.String[] {"US Government Bonds"}, 0.), new
										org.drip.market.exchange.BondFuturesSettle (1, 1,
											org.drip.market.exchange.BondFuturesSettle.SETTLE_TYPE_PHYSICAL_DELIVERY,
				org.drip.market.exchange.BondFuturesSettle.QUOTE_REFERENCE_INDEX_CONVERSION_FACTOR, false,
					0.06, 0.06)));

			_mapFutures.put ("USD-TREASURY-NOTE-2Y", new org.drip.market.exchange.BondFuturesConvention
				("USD 2-YR NOTE", new java.lang.String[] {"ZT", "TU"}, "USD", "2Y", 200000., 0., new
					java.lang.String[] {"CBOT"}, "TREASURY", "NOTE", new
						org.drip.analytics.eventday.DateInMonth
							(org.drip.analytics.eventday.DateInMonth.INSTANCE_GENERATOR_RULE_EDGE_LAG, true,
								7, -1, -1), new org.drip.market.exchange.BondFuturesEligibility ("21M", "2Y",
									new java.lang.String[] {"US Government Bonds"}, 0.), new
										org.drip.market.exchange.BondFuturesSettle (1, 1,
											org.drip.market.exchange.BondFuturesSettle.SETTLE_TYPE_PHYSICAL_DELIVERY,
				org.drip.market.exchange.BondFuturesSettle.QUOTE_REFERENCE_INDEX_CONVERSION_FACTOR, false,
					0.06, 0.06)));

			_mapFutures.put ("EUR-EURO-BUXL-30Y", new org.drip.market.exchange.BondFuturesConvention
				("EUR EURO-BUXL", new java.lang.String[] {"BUXL"}, "EUR", "30Y", 100000., 0., new
					java.lang.String[] {"EUREX"}, "EURO", "BUXL", new org.drip.analytics.eventday.DateInMonth
						(org.drip.analytics.eventday.DateInMonth.INSTANCE_GENERATOR_RULE_EDGE_LAG, true, 10,
							-1, -1), new org.drip.market.exchange.BondFuturesEligibility ("24Y", "35Y", new
								java.lang.String[] {"EUR-Germany BUXL Bonds"}, 5000000000.), new
									org.drip.market.exchange.BondFuturesSettle (1, 1,
										org.drip.market.exchange.BondFuturesSettle.SETTLE_TYPE_PHYSICAL_DELIVERY,
				org.drip.market.exchange.BondFuturesSettle.QUOTE_REFERENCE_INDEX_CONVERSION_FACTOR, false,
					0.04, 0.04)));
		} catch (java.lang.Exception e) {
			e.printStackTrace();

			return false;
		}

		return true;
	}

	/**
	 * Retrieve the Bond Futures Convention from the Currency, the Type, the Sub-type, and the Maturity Tenor
	 * 
	 * @param strCurrency The Currency
	 * @param strUnderlierType The Underlier Type
	 * @param strUnderlierSubtype The Underlier Sub-type
	 * @param strMaturityTenor The Maturity Tenor
	 * 
	 * @return The Deliverable Swap Futures Instance
	 */

	public static final org.drip.market.exchange.BondFuturesConvention FromJuristictionTypeMaturity (
		final java.lang.String strCurrency,
		final java.lang.String strUnderlierType,
		final java.lang.String strUnderlierSubtype,
		final java.lang.String strMaturityTenor)
	{
		if (null == strCurrency || strCurrency.isEmpty() || null == strUnderlierType ||
			strUnderlierType.isEmpty() || null == strUnderlierSubtype || strUnderlierSubtype.isEmpty() ||
				null == strMaturityTenor || strMaturityTenor.isEmpty())
			return null;

		java.lang.String strKey = strCurrency + "-" + strUnderlierType + "-" + strUnderlierSubtype + "-" +
			strMaturityTenor;

		return _mapFutures.containsKey (strKey) ? _mapFutures.get (strKey) : null;
	}
}
