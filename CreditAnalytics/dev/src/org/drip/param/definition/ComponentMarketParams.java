
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

	public abstract org.drip.analytics.definition.CreditCurve creditCurve();

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
	 * Retrieve the Component's Funding Curve
	 * 
	 * @return Component's Funding Curve
	 */

	public abstract org.drip.analytics.rates.DiscountCurve fundingCurve();

	/**
	 * (Re)-set the Component's Funding Curve
	 * 
	 * @param dcFunding Component's Funding Curve
	 * 
	 * @return TRUE => Successfully set
	 */

	public abstract boolean setFundingCurve (
		final org.drip.analytics.rates.DiscountCurve dcFunding);

	/**
	 * Retrieve the Component's Domestic Collateral Curve
	 * 
	 * @return Component's Domestic Collateral Curve
	 */

	public abstract org.drip.analytics.rates.DiscountCurve domesticCollateralCurve();

	/**
	 * (Re)-set the Component's Domestic Collateral Curve
	 * 
	 * @param dcDomesticCollateral Component's Domestic Collateral Curve
	 * 
	 * @return TRUE => Successfully set
	 */

	public abstract boolean setDomesticCollateralCurve (
		final org.drip.analytics.rates.DiscountCurve dcDomesticCollateral);

	/**
	 * Retrieve the Component's Foreign Collateral Curve
	 * 
	 * @return Component's Foreign Collateral Curve
	 */

	public abstract org.drip.analytics.rates.DiscountCurve foreignCollateralCurve (
		final java.lang.String strCurrency);

	/**
	 * (Re)-set the Component's Foreign Collateral Curve
	 * 
	 * @param strCurrency Component's Foreign Collateral Currency
	 * @param dcForeignCollateral Component's Foreign Collateral Curve
	 * 
	 * @return TRUE => Successfully set
	 */

	public abstract boolean setForeignCollateralCurve (
		final java.lang.String strCurrency,
		final org.drip.analytics.rates.DiscountCurve dcForeignCollateral);

	/**
	 * Retrieve the Component's Domestic Currency Collateralized in Foreign Collateral Curve
	 * 
	 * @return Component's Domestic Currency Collateralized in Foreign Collateral Curve
	 */

	public abstract org.drip.analytics.rates.DiscountCurve domesticCurrencyForeignCollateralCurve (
		final java.lang.String strCurrency);

	/**
	 * (Re)-set the Component's Domestic Currency Collateralized in Foreign Collateral Curve
	 * 
	 * @param strCurrency Component's Foreign Collateral Currency
	 * @param dcDomesticCurrencyForeignCollateral Component's Domestic Currency Collateralized in Foreign
	 * 	Collateral Curve
	 * 
	 * @return TRUE => Successfully set
	 */

	public abstract boolean setDomesticCurrencyForeignCollateralCurve (
		final java.lang.String strCurrency,
		final org.drip.analytics.rates.DiscountCurve dcDomesticCurrencyForeignCollateral);

	/**
	 * Retrieve the Component's Foreign Currency Collateralized in Domestic Collateral Curve
	 * 
	 * @return Component's Foreign Currency Collateralized in Domestic Collateral Curve
	 */

	public abstract org.drip.analytics.rates.DiscountCurve foreignCurrencyDomesticCollateralCurve (
		final java.lang.String strCurrency);

	/**
	 * (Re)-set the Component's Foreign Currency Collateralized in Domestic Collateral Curve
	 * 
	 * @param strCurrency Component's Foreign Collateral Currency
	 * @param dcForeignCurrencyDomesticCollateral Component's Foreign Currency Collateralized in Domestic
	 * 	Collateral Curve
	 * 
	 * @return TRUE => Successfully set
	 */

	public abstract boolean setForeignCurrencyDomesticCollateralCurve (
		final java.lang.String strCurrency,
		final org.drip.analytics.rates.DiscountCurve dcForeignCurrencyDomesticCollateral);

	/**
	 * Retrieve the Component's Collateral Choice Discount Curve
	 * 
	 * @return Component's Collateral Choice Discount Curve
	 */

	public abstract org.drip.analytics.rates.DiscountCurve collateralChoiceDiscountCurve();

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
	 * @param fri Floating Rate Index
	 * 
	 * @return Component Forward Curve
	 */

	public abstract org.drip.analytics.rates.ForwardCurve forwardCurve (
		final org.drip.product.params.FloatingRateIndex fri);

	/**
	 * Retrieve the Component Government Funding Curve
	 * 
	 * @return Component Government Funding Curve
	 */

	public abstract org.drip.analytics.rates.DiscountCurve fundingCurveGovvie();

	/**
	 * (Re)-set the Component Government Discount Curve
	 * 
	 * @param dcTSY Component Government Discount Curve
	 * 
	 * @return TRUE => Successfully set
	 */

	public abstract boolean setfundingCurveGovvie (
		final org.drip.analytics.rates.DiscountCurve dcTSY);

	/**
	 * Retrieve the Component Futures Funding Curve
	 * 
	 * @return Component Futures Funding Curve
	 */

	public abstract org.drip.analytics.rates.DiscountCurve futuresFundingCurve();

	/**
	 * (Re)-set the Component Futures Funding Curve
	 * 
	 * @param dcFutures Component Futures Funding Curve
	 * 
	 * @return TRUE => Successfully set
	 */

	public abstract boolean setFuturesFundingCurve (
		final org.drip.analytics.rates.DiscountCurve dcFutures);

	/**
	 * Retrieve the Component Quote
	 * 
	 * @return Component Quote
	 */

	public abstract org.drip.param.definition.ComponentQuote componentQuote();

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

	/**
	 * Retrieve the Component's FX Curve
	 * 
	 * @return Component's FX Curve
	 */

	public abstract org.drip.quant.function1D.AbstractUnivariate getFXCurve (
		final java.lang.String strFXCode);

	/**
	 * (Re)-set the Component's Foreign Collateral Curve
	 * 
	 * @param strFXCode Code for the FX Currency Pair
	 * @param auFX The FX Curve
	 * 
	 * @return TRUE => Successfully set
	 */

	public abstract boolean setFXCurve (
		final java.lang.String strFXCode,
		final org.drip.quant.function1D.AbstractUnivariate auFX);
}
