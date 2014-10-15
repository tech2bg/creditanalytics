
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
 * ScenarioMarketParams is the place holder for the comprehensive suite of the market set of curves for the
 * 	given date. It exports the following functionality:
 * 	- add/remove/retrieve scenario discount curve
 * 	- add/remove/retrieve scenario forward curve
 * 	- add/remove/retrieve scenario zero curve
 * 	- add/remove/retrieve scenario credit curve
 * 	- add/remove/retrieve scenario recovery curve
 * 	- add/remove/retrieve scenario FXForward curve
 * 	- add/remove/retrieve scenario FXBasis curve
 * 	- add/remove/retrieve scenario fixings
 * 	- add/remove/retrieve Treasury/component quotes
 * 	- retrieve scenario Market Parameters
 * 	- retrieve map of flat rates/credit/recovery Market Parameters
 * 	- retrieve double map of tenor rates/credit/recovery Market Parameters
 *  - retrieve rates/credit scenario generator
 *
 * @author Lakshmi Krishnamurthy
 */

public abstract class ScenarioMarketParams {

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
		final org.drip.param.definition.ProductQuote cqTSY);

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
		final org.drip.analytics.support.CaseInsensitiveTreeMap<org.drip.param.definition.ProductQuote>
			mapCQTSY);

	/**
	 * Get the named Treasury Quote Map corresponding to the desired benchmark
	 * 
	 * @param strBenchmark The treasury benchmark
	 * 
	 * @return Treasury Quote
	 */

	public abstract org.drip.param.definition.ProductQuote getTSYQuote (
		final java.lang.String strBenchmark);

	/**
	 * Get the full set of named Treasury Quote Map
	 * 
	 * @return Named Treasury Quote Map
	 */

	public abstract org.drip.analytics.support.CaseInsensitiveTreeMap<org.drip.param.definition.ProductQuote>
		getTSYQuotes();

	/**
	 * Add the fixing for the given Latent State Label and the given date
	 * 
	 * @param dtFix The fixing date
	 * @param lsl The Latent State Label
	 * @param dblFixing The fixing
	 * 
	 * @return Added successfully (true)
	 */

	public abstract boolean addFixings (
		final org.drip.analytics.date.JulianDate dtFix,
		final org.drip.state.identifier.LatentStateLabel lsl,
		final double dblFixing);

	/**
	 * Remove the fixing corresponding to the given date and the Latent State Label
	 * 
	 * @param dtFix Fixing date
	 * @param lsl The Latent State label
	 * 
	 * @return Successfully removed (true)
	 */

	public abstract boolean removeFixings (
		final org.drip.analytics.date.JulianDate dtFix,
		final org.drip.state.identifier.LatentStateLabel lsl);

	/**
	 * Retrieve the Latent State Fixings Container
	 * 
	 * @return The Latent State Fixings Container
	 */

	public abstract org.drip.param.market.LatentStateFixingsContainer fixings();

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
		final org.drip.param.definition.ProductQuote cqComp);

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
		final org.drip.analytics.support.CaseInsensitiveTreeMap<org.drip.param.definition.ProductQuote>
			mCompQuotes);

	/**
	 * Retrieve the quote for the given component
	 * 
	 * @param strCompID Component ID
	 * 
	 * @return ComponentQuote
	 */

	public abstract org.drip.param.definition.ProductQuote getCompQuote (
		final java.lang.String strCompID);

	/**
	 * Retrieve the full map of component quotes
	 * 
	 * @return mCompQuotes Map of Component Quotes
	 */

	public abstract
		org.drip.analytics.support.CaseInsensitiveTreeMap<org.drip.param.definition.ProductQuote>
			getCompQuotes();

	/**
	 * Add the named scenario Market Parameters
	 * 
	 * @param strScenarioName Scenario Name
	 * @param csqs Market Parameters
	 * 
	 * @return True => Added successfully
	 */

	public abstract boolean addScenMarketParams (
		final java.lang.String strScenarioName,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs);

	/**
	 * Retrieve the Named Scenario Market Parameters
	 * 
	 * @param strScenarioName Scenario Name
	 * 
	 * @return Named Market Parameters
	 */

	public abstract org.drip.param.market.CurveSurfaceQuoteSet getScenMarketParams (
		final java.lang.String strScenarioName);

	/**
	 * Get the Market Parameters corresponding to the component and the scenario
	 * 
	 * @param comp Component
	 * @param strScen Scenario
	 * 
	 * @return The Market Parameters
	 */

	public abstract org.drip.param.market.CurveSurfaceQuoteSet getScenMarketParams (
		final org.drip.product.definition.FixedIncomeComponent comp,
		final java.lang.String strScen);

	/**
	 * Get the map of tenor IR bumped Market Parameters corresponding to the component
	 * 
	 * @param comp Component
	 * @param bBumpUp Bump up (True)
	 * 
	 * @return Map of the tenor IR bumped Market Parameters
	 */

	public abstract
		org.drip.analytics.support.CaseInsensitiveTreeMap<org.drip.param.market.CurveSurfaceQuoteSet>
			getIRTenorMarketParams (
				final org.drip.product.definition.FixedIncomeComponent comp,
				final boolean bBumpUp);

	/**
	 * Get the Map of Tenor Forward Rate bumped Market Parameters corresponding to the component
	 * 
	 * @param comp Component
	 * @param bBumpUp Bump up (True)
	 * 
	 * @return Map of the Tenor Forward Rate bumped Market Parameters
	 */

	public abstract
		org.drip.analytics.support.CaseInsensitiveTreeMap<org.drip.param.market.CurveSurfaceQuoteSet>
			getForwardTenorMarketParams (
				final org.drip.product.definition.FixedIncomeComponent comp,
				final boolean bBumpUp);

	/**
	 * Get the map of tenor credit bumped Market Parameters corresponding to the component
	 *  
	 * @param comp Component
	 * @param bBumpUp Bump up (True)
	 * 
	 * @return Map of the tenor credit bumped Market Parameters
	 */

	public abstract
		org.drip.analytics.support.CaseInsensitiveTreeMap<org.drip.param.market.CurveSurfaceQuoteSet>
			getCreditTenorMarketParams (
				final org.drip.product.definition.FixedIncomeComponent comp,
				final boolean bBumpUp);

	/**
	 * Get the Market Parameters for the given basket product and the scenario
	 * 
	 * @param bp BasketProduct
	 * @param strScen Scenario
	 * 
	 * @return Market Parameters Instance
	 */

	public abstract org.drip.param.market.CurveSurfaceQuoteSet getScenMarketParams (
		final org.drip.product.definition.BasketProduct bp,
		final java.lang.String strScen);

	/**
	 * Get the map of IR Parallel bumped curves for the given Basket Product
	 * 
	 * @param bp BasketProduct
	 * @param bBump True (Bump Up), False (Bump Down)
	 * 
	 * @return Map of the IR Tenor bumped curves
	 */

	public abstract
		org.drip.analytics.support.CaseInsensitiveTreeMap<org.drip.param.market.CurveSurfaceQuoteSet>
			dcFlatBump (
				final org.drip.product.definition.BasketProduct bp,
				final boolean bBump);

	/**
	 * Get the Map of Forward Rate Flat Bumped Curves for the given Basket Product
	 * 
	 * @param bp BasketProduct
	 * @param bBump True (Bump Up), False (Bump Down)
	 * 
	 * @return Map of the IR Tenor bumped curves
	 */

	public abstract
		org.drip.analytics.support.CaseInsensitiveTreeMap<org.drip.param.market.CurveSurfaceQuoteSet>
			forwardFlatBump (
				final org.drip.product.definition.BasketProduct bp,
				final boolean bBump);

	/**
	 * Get the map of credit Flat Flat curves for the given Basket Product
	 * 
	 * @param bp BasketProduct
	 * @param bBump True (Bump Up), False (Bump Down)
	 * 
	 * @return Map of the credit Flat bumped curves
	 */

	public abstract
		org.drip.analytics.support.CaseInsensitiveTreeMap<org.drip.param.market.CurveSurfaceQuoteSet>
			creditFlatBump (
				final org.drip.product.definition.BasketProduct bp,
				final boolean bBump);

	/**
	 * Get the map of Recovery Flat bumped curves for the given Basket Product
	 * 
	 * @param bp BasketProduct
	 * @param bBump True (Bump Up), False (Bump Down)
	 * 
	 * @return Map of the Recovery Tenor bumped curves
	 */

	public abstract
		org.drip.analytics.support.CaseInsensitiveTreeMap<org.drip.param.market.CurveSurfaceQuoteSet>
			recoveryFlatBump (
				final org.drip.product.definition.BasketProduct bp,
				final boolean bBump);

	/**
	 * Get the double map of DC Tenor bumped curves for each DC curve for the given Basket Product
	 * 
	 * @param bp BasketProduct
	 * @param bBump True (Bump Up), False (Bump Down)
	 * 
	 * @return Double Map of the IR Tenor bumped Market Parameters
	 */

	public abstract
		org.drip.analytics.support.CaseInsensitiveTreeMap<org.drip.analytics.support.CaseInsensitiveTreeMap<org.drip.param.market.CurveSurfaceQuoteSet>>
			dcTenorBump (
				final org.drip.product.definition.BasketProduct bp,
				final boolean bBump);

	/**
	 * Get the double map of credit Tenor bumped curves for each credit curve for the given Basket Product
	 * 
	 * @param bp BasketProduct
	 * @param bBump True (Bump Up), False (Bump Down)
	 * 
	 * @return Double Map of the credit Tenor bumped Market Parameters
	 */

	public abstract
		org.drip.analytics.support.CaseInsensitiveTreeMap<org.drip.analytics.support.CaseInsensitiveTreeMap<org.drip.param.market.CurveSurfaceQuoteSet>>
			creditTenorBump (
				final org.drip.product.definition.BasketProduct bp,
				final boolean bBump);

	/**
	 * Retrieve the map of RatesScenarioCurve
	 * 
	 * @return Map of RatesScenarioCurve
	 */

	public abstract
		org.drip.analytics.support.CaseInsensitiveTreeMap<org.drip.param.definition.ScenarioDiscountCurve>
			irsg();

	/**
	 * Retrieve the map of org.drip.param.definition.CreditScenarioCurve
	 * 
	 * @return Map of org.drip.param.definition.CreditScenarioCurve
	 */

	public abstract
		org.drip.analytics.support.CaseInsensitiveTreeMap<org.drip.param.definition.ScenarioCreditCurve>
			ccsg();
}
