
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
 * BasketMarketParams class extends the BaketMarketParamsRef for a specific scenario. It provides access to
 *  maps holding named discount curves, named forward curves, named credit curves, named treasury quotes,
 *  named component quotes, and fixings objects, and retrieve the component market parameters from the
 *  specified reference.
 *
 * @author Lakshmi Krishnamurthy
 */

public abstract class BasketMarketParams extends org.drip.service.stream.Serializer {

	/**
	 * Add a named discount curve
	 * 
	 * @param strName Name
	 * @param dc Discount Curve
	 * 
	 * @return Success (true) or Failure (false)
	 */

	public abstract boolean addDiscountCurve (
		final java.lang.String strName,
		final org.drip.analytics.rates.DiscountCurve dc);

	/**
	 * Add a named Forward curve
	 * 
	 * @param strName Name
	 * @param fc Forward Curve
	 * 
	 * @return Success (true) or Failure (false)
	 */

	public abstract boolean addForwardCurve (
		final java.lang.String strName,
		final org.drip.analytics.rates.ForwardCurve fc);

	/**
	 * Add a named credit curve
	 * 
	 * @param strName Name
	 * @param cc Credit Curve
	 * 
	 * @return Success (true) or Failure (false)
	 */

	public abstract boolean addCreditCurve (
		final java.lang.String strName,
		final org.drip.analytics.definition.CreditCurve cc);

	/**
	 * Retrieve a named discount curve
	 * 
	 * @param strName Name
	 * 
	 * @return Discount Curve
	 */

	public abstract org.drip.analytics.rates.DiscountCurve getDiscountCurve (
		final java.lang.String strName);

	/**
	 * Retrieve the Named Forward Curve
	 * 
	 * @param strName Name
	 * 
	 * @return The Forward Curve
	 */

	public abstract org.drip.analytics.rates.ForwardCurve getForwardCurve (
		final java.lang.String strName);

	/**
	 * Retrieve a named credit curve
	 * 
	 * @param strName Name
	 * 
	 * @return Credit Curve
	 */

	public abstract org.drip.analytics.definition.CreditCurve getCreditCurve (
		final java.lang.String strName);

	/**
	 * Add a named Component Quote
	 * 
	 * @param strName Component Name
	 * @param cq Component Quote
	 * 
	 * @return TRUE => Successfully added
	 */

	public abstract boolean addComponentQuote (
		final java.lang.String strName,
		final org.drip.param.definition.ComponentQuote cq);

	/**
	 * Retrieve the Named Component Quote
	 * 
	 * @param strName Component Name
	 * 
	 * @return Component Quote
	 */

	public abstract org.drip.param.definition.ComponentQuote getComponentQuote (
		final java.lang.String strName);

	/**
	 * Retrieve the basket component's market parameters
	 * 
	 * @param compRef The component's ComponentMarketParamRef
	 * 
	 * @return The ComponentMarketParam object
	 */

	public abstract org.drip.param.definition.ComponentMarketParams getComponentMarketParams (
		final org.drip.product.definition.ComponentMarketParamRef compRef);
}
