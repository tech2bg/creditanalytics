
package org.drip.param.definition;

/*
 * -*- mode: java; tab-width: 4; indent-tabs-mode: nil; c-basic-offset: 4 -*-
 */

/*!
 * Copyright (C) 2014 Lakshmi Krishnamurthy
 * Copyright (C) 2013 Lakshmi Krishnamurthy
 * Copyright (C) 2012 Lakshmi Krishnamurthy
 * Copyright (C) 2011 Lakshmi Krishnamurthy
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
 * ComponentMarketParams abstract class provides stub for the ComponentMarketParamsRef interface. It serves
 *  as a place holder for the market parameters needed to value the component object – the discount curve,
 *  the forward curve, the treasury curve, the EDSF curve, the credit curve, the component quote, the
 *  treasury quote map, and the fixings map.
 *
 * @author Lakshmi Krishnamurthy
 */

public abstract class ComponentMarketParams extends org.drip.service.stream.Serializer {

	/**
	 * Retrieve the Component Credit Curve
	 * 
	 * @return Component Credit Curve
	 */

	public abstract org.drip.analytics.definition.CreditCurve getCreditCurve();

	/**
	 * (Re)-set the Component Credit Curve
	 * 
	 * @param cc Component Credit Curve
	 * 
	 * @return TRUE => Successfully set
	 */

	public abstract boolean setCreditCurve (
		final org.drip.analytics.definition.CreditCurve cc);

	/**
	 * Retrieve the Component Discount Curve
	 * 
	 * @return Component Discount Curve
	 */

	public abstract org.drip.analytics.rates.DiscountCurve getDiscountCurve();

	/**
	 * (Re)-set the Component Forward Curve
	 * 
	 * @param fc Component Forward Curve
	 * 
	 * @return TRUE => Successfully set
	 */

	public abstract boolean setForwardCurve (
		final org.drip.analytics.rates.ForwardCurve fc);

	/**
	 * Retrieve the Component Forward Curve
	 * 
	 * @return Component Forward Curve
	 */

	public abstract org.drip.analytics.rates.ForwardCurve getForwardCurve();

	/**
	 * (Re)-set the Component Discount Curve
	 * 
	 * @param dc Component Discount Curve
	 * 
	 * @return TRUE => Successfully set
	 */

	public abstract boolean setDiscountCurve (
		final org.drip.analytics.rates.DiscountCurve dc);

	/**
	 * Retrieve the Component TSY Discount Curve
	 * 
	 * @return Component TSY Discount Curve
	 */

	public abstract org.drip.analytics.rates.DiscountCurve getTSYDiscountCurve();

	/**
	 * (Re)-set the Component TSY Discount Curve
	 * 
	 * @param dcTSY Component TSY Discount Curve
	 * 
	 * @return TRUE => Successfully set
	 */

	public abstract boolean setTSYDiscountCurve (
		final org.drip.analytics.rates.DiscountCurve dcTSY);

	/**
	 * Retrieve the Component EDSF Discount Curve
	 * 
	 * @return Component EDSF Discount Curve
	 */

	public abstract org.drip.analytics.rates.DiscountCurve getEDSFDiscountCurve();

	/**
	 * (Re)-set the Component EDSF Discount Curve
	 * 
	 * @param dcEDSF Component EDSF Discount Curve
	 * 
	 * @return TRUE => Successfully set
	 */

	public abstract boolean setEDSFDiscountCurve (
		final org.drip.analytics.rates.DiscountCurve dcEDSF);

	/**
	 * Retrieve the Component Quote
	 * 
	 * @return Component Quote
	 */

	public abstract org.drip.param.definition.ComponentQuote getComponentQuote();

	/**
	 * (Re)-set the Component Quote
	 * 
	 * @param compQuote Component Quote
	 * 
	 * @return TRUE => Successfully set
	 */

	public abstract boolean setComponentQuote (
		final org.drip.param.definition.ComponentQuote compQuote);

	/**
	 * Retrieve the TSY Benchmark Quotes
	 * 
	 * @return TSY Benchmark Quotes
	 */

	public abstract
		org.drip.analytics.support.CaseInsensitiveTreeMap<org.drip.param.definition.ComponentQuote>
			getTSYBenchmarkQuotes();

	/**
	 * Retrieve the Fixings
	 * 
	 * @return The Fixings Object
	 */

	public abstract java.util.Map<org.drip.analytics.date.JulianDate,
		org.drip.analytics.support.CaseInsensitiveTreeMap<java.lang.Double>> getFixings();

	/**
	 * (Re)-set the Fixings
	 * 
	 * @param mmFixings Fixings
	 * 
	 * @return TRUE => Successfully set
	 */

	public abstract boolean setFixings (
		final java.util.Map<org.drip.analytics.date.JulianDate,
			org.drip.analytics.support.CaseInsensitiveTreeMap<java.lang.Double>> mmFixings);

	/**
	 * Retrieve the Latent State Volatility Surface for the given Forward Date
	 * 
	 * @param strLatentState The Latent State
	 * @param dtForward The Forward Date 
	 * 
	 * @return The Latent State Volatility Surface
	 */

	public abstract org.drip.quant.function1D.AbstractUnivariate getLatentStateVolSurface (
		final java.lang.String strFRI,
		final org.drip.analytics.date.JulianDate dtForward);

	/**
	 * (Re)-set the Latent State Volatility Surface for the given Forward Date
	 * 
	 * @param strLatentState The Latent State
	 * @param dtForward The Forward Date 
	 * @param auVolatility The Latent State Volatility Surface
	 * 
	 * @return TRUE => Successfully set
	 */

	public abstract boolean setLatentStateVolSurface (
		final java.lang.String strLatentState,
		final org.drip.analytics.date.JulianDate dtForward,
		final org.drip.quant.function1D.AbstractUnivariate auVolatility);
}
