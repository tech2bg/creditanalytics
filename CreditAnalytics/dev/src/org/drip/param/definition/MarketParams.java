
package org.drip.param.definition;

/*
 * -*- mode: java; tab-width: 4; indent-tabs-mode: nil; c-basic-offset: 4 -*-
 */

/*!
 * Copyright (C) 2014 Lakshmi Krishnamurthy
 * Copyright (C) 2013 Lakshmi Krishnamurthy
 * Copyright (C) 2012 Lakshmi Krishnamurthy
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
 * MarketParams is the place holder for the comprehensive suite of the market set of curves for the given
 * 	date. It exports the following functionality:
 * 	- add/remove/retrieve scenario discount curve
 * 	- add/remove/retrieve scenario forward curve
 * 	- add/remove/retrieve scenario zero curve
 * 	- add/remove/retrieve scenario credit curve
 * 	- add/remove/retrieve scenario recovery curve
 * 	- add/remove/retrieve scenario FXForward curve
 * 	- add/remove/retrieve scenario FXBasis curve
 * 	- add/remove/retrieve scenario fixings
 * 	- add/remove/retrieve Treasury/component quotes
 * 	- retrieve scenario CMP/BMP
 * 	- retrieve map of flat rates/credit/recovery CMP/BMP
 * 	- retrieve double map of tenor rates/credit/recovery CMP/BMP
 *  - retrieve rates/credit scenario generator
 *
 * @author Lakshmi Krishnamurthy
 */

public abstract class MarketParams {

	/**
	 * Add the named scenario DC
	 * 
	 * @param strName Name
	 * @param irsg Corresponding IRCurveScenarioContainer instance
	 * 
	 * @return Added successfully (true)
	 */

	public abstract boolean addScenDC (
		final java.lang.String strName,
		final org.drip.param.definition.ScenarioDiscountCurve irsg);

	/**
	 * Remove the named scenario DC
	 * 
	 * @param strName Name
	 * 
	 * @return Removed successfully (true)
	 */

	public abstract boolean removeScenDC (
		final java.lang.String strName);

	/**
	 * Add Named Scenario Forward Curve
	 * 
	 * @param strName Name
	 * @param sfc Scenario Forward Curve Instance
	 * 
	 * @return Added successfully (true)
	 */

	public abstract boolean addScenFC (
		final java.lang.String strName,
		final org.drip.param.definition.ScenarioForwardCurve sfc);

	/**
	 * Remove the Named Scenario Forward Curve
	 * 
	 * @param strName Name
	 * 
	 * @return Removed successfully (true)
	 */

	public abstract boolean removeScenFC (
		final java.lang.String strName);

	/**
	 * Add the named scenario CC
	 * 
	 * @param strName Name
	 * @param ccsg Corresponding org.drip.param.definition.CreditScenarioCurve instance
	 * 
	 * @return Added successfully (true)
	 */

	public abstract boolean addScenCC (
		final java.lang.String strName,
		final org.drip.param.definition.ScenarioCreditCurve ccsg);

	/**
	 * Removes the named scenario CC
	 * 
	 * @param strName Name
	 * 
	 * @return Removed successfully (true)
	 */

	public abstract boolean removeScenCC (
		final java.lang.String strName);

	/**
	 * Add the named Treasury Quote
	 * 
	 * @param strBenchmark Name
	 * @param cqTSY Treasury Quote
	 * 
	 * @return Added successfully (true)
	 */

	public abstract boolean addTSYQuote (
		final java.lang.String strBenchmark,
		final org.drip.param.definition.ComponentQuote cqTSY);

	/**
	 * Remove the named Treasury Quote
	 * 
	 * @param strBenchmark Name
	 * 
	 * @return Removed successfully (true)
	 */

	public abstract boolean removeTSYQuote (
		final java.lang.String strBenchmark);

	/**
	 * Set the full set of named Treasury Quote Map
	 * 
	 * @param mapCQTSY Named Treasury Quote Map
	 * 
	 * @return Set successfully (true)
	 */

	public abstract boolean setTSYQuotes (
		final org.drip.analytics.support.CaseInsensitiveTreeMap<org.drip.param.definition.ComponentQuote>
			mapCQTSY);

	/**
	 * Get the named Treasury Quote Map corresponding to the desired benchmark
	 * 
	 * @param strBenchmark The treasury benchmark
	 * 
	 * @return Treasury Quote
	 */

	public abstract org.drip.param.definition.ComponentQuote getTSYQuote (
		final java.lang.String strBenchmark);

	/**
	 * Get the full set of named Treasury Quote Map
	 * 
	 * @return Named Treasury Quote Map
	 */

	public abstract org.drip.analytics.support.CaseInsensitiveTreeMap<org.drip.param.definition.ComponentQuote>
		getTSYQuotes();

	/**
	 * Add the fixing for the given rate index and the given date
	 * 
	 * @param dtFix The fixing date
	 * @param strIndex The Rate Index
	 * @param dblFixing The fixing
	 * 
	 * @return Added successfully (true)
	 */

	public abstract boolean addFixings (
		final org.drip.analytics.date.JulianDate dtFix,
		final java.lang.String strIndex,
		final double dblFixing);

	/**
	 * Remove the fixing corresponding to the given date and index
	 * 
	 * @param dtFix Fixing date
	 * @param strIndex Rate Index
	 * 
	 * @return Successfully removed (true)
	 */

	public abstract boolean removeFixings (
		final org.drip.analytics.date.JulianDate dtFix,
		final java.lang.String strIndex);

	/**
	 * Retrieve the fixings double map
	 * 
	 * @return The fixings Map
	 */

	public abstract java.util.Map<org.drip.analytics.date.JulianDate,
		org.drip.analytics.support.CaseInsensitiveTreeMap<java.lang.Double>>
			getFixings();

	/**
	 * Add the component quote
	 * 
	 * @param strCompID Component ID
	 * @param cqComp Component Quote
	 * 
	 * @return Added successfully (true)
	 */

	public abstract boolean addCompQuote (
		final java.lang.String strCompID,
		final org.drip.param.definition.ComponentQuote cqComp);

	/**
	 * Remove the component quote
	 * 
	 * @param strCompID Component ID
	 * 
	 * @return Removed successfully (true)
	 */

	public abstract boolean removeCompQuote (
		final java.lang.String strCompID);

	/**
	 * Add the full map of component quotes
	 * 
	 * @param mCompQuotes Map of Component Quotes
	 * 
	 * @return Added successfully (true)
	 */

	public abstract boolean addCompQuotes (
		final org.drip.analytics.support.CaseInsensitiveTreeMap<org.drip.param.definition.ComponentQuote>
			mCompQuotes);

	/**
	 * Retrieve the quote for the given component
	 * 
	 * @param strCompID Component ID
	 * 
	 * @return ComponentQuote
	 */

	public abstract org.drip.param.definition.ComponentQuote getCompQuote (
		final java.lang.String strCompID);

	/**
	 * Retrieve the full map of component quotes
	 * 
	 * @return mCompQuotes Map of Component Quotes
	 */

	public abstract org.drip.analytics.support.CaseInsensitiveTreeMap<org.drip.param.definition.ComponentQuote>
		getCompQuotes();

	/**
	 * Add the named scenario BMP
	 * 
	 * @param strScenarioName Scenario Name
	 * @param bmp BasketMarketParams
	 * 
	 * @return True => Added successfully
	 */

	public abstract boolean addScenBMP (
		final java.lang.String strScenarioName,
		final org.drip.param.definition.BasketMarketParams bmp);

	/**
	 * Retrieve the Named Scenario BMP
	 * 
	 * @param strScenarioName Scenario Name
	 * 
	 * @return Named BMP
	 */

	public abstract org.drip.param.definition.BasketMarketParams getScenBMP (
		final java.lang.String strScenarioName);

	/**
	 * Add the named scenario CMP
	 * 
	 * @param strScenarioName Scenario Name
	 * @param cmp BasketMarketParams
	 * 
	 * @return True => Added successfully
	 */

	public abstract boolean addScenCMP (
		final java.lang.String strScenarioName,
		final org.drip.param.definition.ComponentMarketParams cmp);

	/**
	 * Retrieve the Named Scenario CMP
	 * 
	 * @param strScenarioName Scenario Name
	 * 
	 * @return Named CMP
	 */

	public abstract org.drip.param.definition.ComponentMarketParams getScenCMP (
		final java.lang.String strScenarioName);

	/**
	 * Get the ComponentMarketParams corresponding to the component and the scenario
	 * 
	 * @param comp Component
	 * @param strScen Scenario
	 * 
	 * @return ComponentMarketParams
	 */

	public abstract org.drip.param.definition.ComponentMarketParams getScenCMP (
		final org.drip.product.definition.FixedIncomeComponent comp,
		final java.lang.String strScen);

	/**
	 * Get the map of tenor IR bumped ComponentMarketParams corresponding to the component
	 * 
	 * @param comp Component
	 * @param bBumpUp Bump up (True)
	 * 
	 * @return Map of the tenor IR bumped ComponentMarketParams
	 */

	public abstract
		org.drip.analytics.support.CaseInsensitiveTreeMap<org.drip.param.definition.ComponentMarketParams>
			getIRTenorCMP (
				final org.drip.product.definition.FixedIncomeComponent comp,
				final boolean bBumpUp);

	/**
	 * Get the Map of Tenor Forward Rate bumped ComponentMarketParams corresponding to the component
	 * 
	 * @param comp Component
	 * @param bBumpUp Bump up (True)
	 * 
	 * @return Map of the Tenor Forward Rate bumped ComponentMarketParams
	 */

	public abstract
		org.drip.analytics.support.CaseInsensitiveTreeMap<org.drip.param.definition.ComponentMarketParams>
			getForwardTenorCMP (
				final org.drip.product.definition.FixedIncomeComponent comp,
				final boolean bBumpUp);

	/**
	 * Get the map of tenor credit bumped ComponentMarketParams corresponding to the component
	 *  
	 * @param comp Component
	 * @param bBumpUp Bump up (True)
	 * 
	 * @return Map of the tenor credit bumped ComponentMarketParams
	 */

	public abstract
		org.drip.analytics.support.CaseInsensitiveTreeMap<org.drip.param.definition.ComponentMarketParams>
			getCreditTenorCMP (
				final org.drip.product.definition.FixedIncomeComponent comp,
				final boolean bBumpUp);

	/**
	 * Get the BasketMarketParams for the given basket product and the scenario
	 * 
	 * @param bp BasketProduct
	 * @param strScen Scenario
	 * 
	 * @return BasketMarketParams object
	 */

	public abstract org.drip.param.definition.BasketMarketParams getScenBMP (
		final org.drip.product.definition.BasketProduct bp,
		final java.lang.String strScen);

	/**
	 * Get the map of IR Tenor bumped curves for the given BasketProduct
	 * 
	 * @param bp BasketProduct
	 * @param bBump True (Bump Up), False (Bump Down)
	 * 
	 * @return Map of the IR Tenor bumped curves
	 */

	public abstract
		org.drip.analytics.support.CaseInsensitiveTreeMap<org.drip.param.definition.BasketMarketParams>
			getIRBumpBMP (
				final org.drip.product.definition.BasketProduct bp,
				final boolean bBump);

	/**
	 * Get the Map of Forward Rate Tenor Bumped Curves for the given Basket Product
	 * 
	 * @param bp BasketProduct
	 * @param bBump True (Bump Up), False (Bump Down)
	 * 
	 * @return Map of the IR Tenor bumped curves
	 */

	public abstract
		org.drip.analytics.support.CaseInsensitiveTreeMap<org.drip.param.definition.BasketMarketParams>
			getForwardBumpBMP (
				final org.drip.product.definition.BasketProduct bp,
				final boolean bBump);

	/**
	 * Get the map of credit Tenor bumped curves for the given BasketProduct
	 * 
	 * @param bp BasketProduct
	 * @param bBump True (Bump Up), False (Bump Down)
	 * 
	 * @return Map of the credit Tenor bumped curves
	 */

	public abstract
		org.drip.analytics.support.CaseInsensitiveTreeMap<org.drip.param.definition.BasketMarketParams>
			getCreditBumpBMP (
				final org.drip.product.definition.BasketProduct bp,
				final boolean bBump);

	/**
	 * Get the map of Recovery Tenor bumped curves for the given BasketProduct
	 * 
	 * @param bp BasketProduct
	 * @param bBump True (Bump Up), False (Bump Down)
	 * 
	 * @return Map of the Recovery Tenor bumped curves
	 */

	public abstract
		org.drip.analytics.support.CaseInsensitiveTreeMap<org.drip.param.definition.BasketMarketParams>
			getRecoveryBumpBMP (
				final org.drip.product.definition.BasketProduct bp,
				final boolean bBump);

	/**
	 * Get the double map of IR Tenor bumped curves for each IR curve for the given BasketProduct
	 * 
	 * @param bp BasketProduct
	 * @param bBump True (Bump Up), False (Bump Down)
	 * 
	 * @return Double Map of the IR Tenor bumped BasketMarketParams
	 */

	public abstract
		org.drip.analytics.support.CaseInsensitiveTreeMap<org.drip.analytics.support.CaseInsensitiveTreeMap<org.drip.param.definition.BasketMarketParams>>
			getIRTenorBumpBMP (
				final org.drip.product.definition.BasketProduct bp,
				final boolean bBump);

	/**
	 * Get the double map of credit Tenor bumped curves for each credit curve for the given BasketProduct
	 * 
	 * @param bp BasketProduct
	 * @param bBump True (Bump Up), False (Bump Down)
	 * 
	 * @return Double Map of the credit Tenor bumped BasketMarketParams
	 */

	public abstract
		org.drip.analytics.support.CaseInsensitiveTreeMap<org.drip.analytics.support.CaseInsensitiveTreeMap<org.drip.param.definition.BasketMarketParams>>
			getCreditTenorBumpBMP (
				final org.drip.product.definition.BasketProduct bp,
				final boolean bBump);

	/**
	 * Retrieve the map of RatesScenarioCurve
	 * 
	 * @return Map of RatesScenarioCurve
	 */

	public abstract
		org.drip.analytics.support.CaseInsensitiveTreeMap<org.drip.param.definition.ScenarioDiscountCurve>
			getIRSG();

	/**
	 * Retrieve the map of org.drip.param.definition.CreditScenarioCurve
	 * 
	 * @return Map of org.drip.param.definition.CreditScenarioCurve
	 */

	public abstract
		org.drip.analytics.support.CaseInsensitiveTreeMap<org.drip.param.definition.ScenarioCreditCurve>
			getCCSG();
}
