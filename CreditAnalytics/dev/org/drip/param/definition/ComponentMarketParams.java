
package org.drip.param.definition;

/*
 * -*- mode: java; tab-width: 4; indent-tabs-mode: nil; c-basic-offset: 4 -*-
 */

/*!
 * Copyright (C) 2013 Lakshmi Krishnamurthy
 * Copyright (C) 2012 Lakshmi Krishnamurthy
 * Copyright (C) 2011 Lakshmi Krishnamurthy
 * 
 * This file is part of CreditAnalytics, a free-software/open-source library for fixed income analysts and
 * 		developers - http://www.credit-trader.org
 * 
 * CreditAnalytics is a free, full featured, fixed income credit analytics library, developed with a special
 * 		focus towards the needs of the bonds and credit products community.
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
 * ComponentMarketParams abstract class provides stub for the ComponentMarketParamsRef interface. It serves
 *  as a place holder for the market parameters needed to value the component object – the discount curve,
 *  the forward curve, the treasury curve, the EDSF curve, the credit curve, the component quote, the
 *  treasury quote map, and the fixings map.
 *
 * @author Lakshmi Krishnamurthy
 */

public abstract class ComponentMarketParams extends org.drip.service.stream.Serializer {

	/**
	 * Retrieves the Component Credit Curve
	 * 
	 * @return Component Credit Curve
	 */

	public abstract org.drip.analytics.definition.CreditCurve getCreditCurve();

	/**
	 * (Re)-sets the Component Credit Curve
	 * 
	 * @param cc Component Credit Curve
	 * 
	 * @return TRUE => Successfully set
	 */

	public abstract boolean setCreditCurve (
		final org.drip.analytics.definition.CreditCurve cc);

	/**
	 * Retrieves the Component Discount Curve
	 * 
	 * @return Component Discount Curve
	 */

	public abstract org.drip.analytics.definition.DiscountCurve getDiscountCurve();

	/**
	 * (Re)-sets the Component Forward Discount Curve
	 * 
	 * @param dcForward Component Forward Discount Curve
	 * 
	 * @return TRUE => Successfully set
	 */

	public abstract boolean setForwardDiscountCurve (
		final org.drip.analytics.definition.DiscountCurve dcForward);

	/**
	 * Retrieves the Component Forward Discount Curve
	 * 
	 * @return Component Forward Discount Curve
	 */

	public abstract org.drip.analytics.definition.DiscountCurve getForwardDiscountCurve();

	/**
	 * (Re)-sets the Component Discount Curve
	 * 
	 * @param dc Component Discount Curve
	 * 
	 * @return TRUE => Successfully set
	 */

	public abstract boolean setDiscountCurve (
		final org.drip.analytics.definition.DiscountCurve dc);

	/**
	 * Retrieves the Component TSY Discount Curve
	 * 
	 * @return Component TSY Discount Curve
	 */

	public abstract org.drip.analytics.definition.DiscountCurve getTSYDiscountCurve();

	/**
	 * (Re)-sets the Component TSY Discount Curve
	 * 
	 * @param dcTSY Component TSY Discount Curve
	 * 
	 * @return TRUE => Successfully set
	 */

	public abstract boolean setTSYDiscountCurve (
		final org.drip.analytics.definition.DiscountCurve dcTSY);

	/**
	 * Retrieves the Component EDSF Discount Curve
	 * 
	 * @return Component EDSF Discount Curve
	 */

	public abstract org.drip.analytics.definition.DiscountCurve getEDSFDiscountCurve();

	/**
	 * (Re)-sets the Component EDSF Discount Curve
	 * 
	 * @param dcEDSF Component EDSF Discount Curve
	 * 
	 * @return TRUE => Successfully set
	 */

	public abstract boolean setEDSFDiscountCurve (
		final org.drip.analytics.definition.DiscountCurve dcEDSF);

	/**
	 * Retrieves the Component Quote
	 * 
	 * @return Component Quote
	 */

	public abstract org.drip.param.definition.ComponentQuote getComponentQuote();

	/**
	 * (Re)-sets the Component Quote
	 * 
	 * @param compQuote Component Quote
	 * 
	 * @return TRUE => Successfully set
	 */

	public abstract boolean setComponentQuote (
		final org.drip.param.definition.ComponentQuote compQuote);

	/**
	 * Retrieves the TSY Benchmark Quotes
	 * 
	 * @return TSY Benchmark Quotes
	 */

	public abstract org.drip.analytics.support.CaseInsensitiveTreeMap<org.drip.param.definition.ComponentQuote>
		getTSYBenchmarkQuotes();

	/**
	 * Retrieves the Fixings
	 * 
	 * @return The Fixings Object
	 */

	public abstract java.util.Map<org.drip.analytics.date.JulianDate,
		org.drip.analytics.support.CaseInsensitiveTreeMap<java.lang.Double>> getFixings();

	/**
	 * (Re)-sets the Fixings
	 * 
	 * @param mmFixings Fixings
	 * 
	 * @return TRUE => Successfully set
	 */

	public abstract boolean setFixings (
		final java.util.Map<org.drip.analytics.date.JulianDate,
			org.drip.analytics.support.CaseInsensitiveTreeMap<java.lang.Double>> mmFixings);
}
