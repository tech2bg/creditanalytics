
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
 *  the forward curve, the treasury curve, the credit curve, the component quote, the treasury quote map, and
 *  the fixings map.
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
	 * Retrieve the Discount Curve associated with the Pay Cash-flow Collateralized using a different
	 * 	Collateral Currency Numeraire 
	 * 
	 * @param strPayCurrency The Pay Currency
	 * @param strCollateralCurrency The Collateral Currency
	 * 
	 * @return The Discount Curve associated with the Pay Cash-flow Collateralized using a different
	 * 	Collateral Currency Numeraire
	 */

	public abstract org.drip.analytics.rates.DiscountCurve payCurrencyCollateralCurrencyCurve (
		final java.lang.String strPayCurrency,
		final java.lang.String strCollateralCurrency);

	/**
	 * Set the Discount Curve associated with the Pay Cash-flow Collateralized using a different
	 * 	Collateral Currency Numeraire
	 * 
	 * @param strPayCurrency The Pay Currency
	 * @param strCollateralCurrency The Collateral Currency
	 * @param dcPayCurrencyCollateralCurrency The Discount Curve associated with the Pay Cash-flow
	 *  Collateralized using a different Collateral Currency Numeraire
	 * 
	 * @return TRUE => Successfully set
	 */

	public abstract boolean setPayCurrencyCollateralCurrencyCurve (
		final java.lang.String strPayCurrency,
		final java.lang.String strCollateralCurrency,
		final org.drip.analytics.rates.DiscountCurve dcPayCurrencyCollateralCurrency);

	/**
	 * Retrieve the Collateral Choice Discount Curve for the specified Pay Currency
	 * 
	 * @param strPayCurrency The Pay Currency
	 * 
	 * @return Component's Collateral Choice Discount Curve
	 */

	public abstract org.drip.analytics.rates.DiscountCurve collateralChoiceDiscountCurve (
		final java.lang.String strPayCurrency);

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

	public abstract org.drip.analytics.rates.DiscountCurve govvieFundingCurve();

	/**
	 * (Re)-set the Component Government Discount Curve
	 * 
	 * @param dcTSY Component Government Discount Curve
	 * 
	 * @return TRUE => Successfully set
	 */

	public abstract boolean setGovvieFundingCurve (
		final org.drip.analytics.rates.DiscountCurve dcTSY);

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
			benchmarkTSYQuotes();

	/**
	 * (Re)-set the Benchmark TSY Quotes
	 * 
	 * @param mapBenchmarkTSYQuotes The Benchmark TSY Quotes
	 * 
	 * @return TRUE => Successfully set
	 */

	public abstract boolean setBenchmarkTSYQuotes (
		final org.drip.analytics.support.CaseInsensitiveTreeMap<org.drip.param.definition.ComponentQuote>
			mapBenchmarkTSYQuotes);

	/**
	 * Retrieve the Fixings
	 * 
	 * @return The Fixings Object
	 */

	public abstract java.util.Map<org.drip.analytics.date.JulianDate,
		org.drip.analytics.support.CaseInsensitiveTreeMap<java.lang.Double>> fixings();

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
	 * Retrieve the Volatility Surface for the specified Collateral Curve
	 * 
	 * @param strCurrency The Collateral Currency
	 * 
	 * @return The Volatility Surface for the Collateral Currency
	 */

	public abstract org.drip.quant.function1D.AbstractUnivariate collateralCurveVolSurface (
		final java.lang.String strCurrency);

	/**
	 * (Re)-set the Volatility Surface for the specified Collateral Curve
	 * 
	 * @param strCurrency The Funding Currency
	 * @param auVolatility The Volatility Surface
	 * 
	 * @return TRUE => Successfully set
	 */

	public abstract boolean setCollateralCurveVolSurface (
		final java.lang.String strCurrency,
		final org.drip.quant.function1D.AbstractUnivariate auVolatility);

	/**
	 * Retrieve the Volatility Surface for the specified Forward Curve
	 * 
	 * @param fri The Forward Rate Index identifying the Forward Curve
	 * 
	 * @return The Volatility Surface for the Forward Curve
	 */

	public abstract org.drip.quant.function1D.AbstractUnivariate forwardCurveVolSurface (
		final org.drip.product.params.FloatingRateIndex fri);

	/**
	 * (Re)-set the Volatility Surface for the specified Forward Curve
	 * 
	 * @param fri The Forward Rate Index identifying the Forward Curve
	 * @param auVolatility The Volatility Surface
	 * 
	 * @return TRUE => Successfully set
	 */

	public abstract boolean setForwardCurveVolSurface (
		final org.drip.product.params.FloatingRateIndex fri,
		final org.drip.quant.function1D.AbstractUnivariate auVolatility);

	/**
	 * Retrieve the Volatility Surface for the specified Funding Curve
	 * 
	 * @param strCurrency The Funding Currency
	 * 
	 * @return The Volatility Surface for the Funding Currency
	 */

	public abstract org.drip.quant.function1D.AbstractUnivariate fundingCurveVolSurface (
		final java.lang.String strCurrency);

	/**
	 * (Re)-set the Volatility Surface for the specified Funding Curve
	 * 
	 * @param strCurrency The Funding Currency
	 * @param auVolatility The Volatility Surface
	 * 
	 * @return TRUE => Successfully set
	 */

	public abstract boolean setFundingCurveVolSurface (
		final java.lang.String strCurrency,
		final org.drip.quant.function1D.AbstractUnivariate auVolatility);

	/**
	 * Retrieve the Custom Metric Volatility Surface for the given Forward Date
	 * 
	 * @param strCustomMetric The Custom Metric Name
	 * @param dtForward The Forward Date 
	 * 
	 * @return The Latent State Volatility Surface
	 */

	public abstract org.drip.quant.function1D.AbstractUnivariate customMetricVolSurface (
		final java.lang.String strCustomMetric,
		final org.drip.analytics.date.JulianDate dtForward);

	/**
	 * (Re)-set the Custom Metric Volatility Surface for the given Forward Date
	 * 
	 * @param strCustomMetric The Custom Metric Name
	 * @param dtForward The Forward Date 
	 * @param auVolatility The Custom Metric Volatility Surface
	 * 
	 * @return TRUE => Successfully set
	 */

	public abstract boolean setCustomMetricVolSurface (
		final java.lang.String strCustomMetric,
		final org.drip.analytics.date.JulianDate dtForward,
		final org.drip.quant.function1D.AbstractUnivariate auVolatility);

	/**
	 * Retrieve the Correlation Surface for the specified Collateral Currency Pair
	 * 
	 * @param strCurrency1 Collateral Currency #1
	 * @param strCurrency2 Collateral Currency #2
	 * 
	 * @return The Correlation Surface for the specified Collateral Currency Pair
	 */

	public abstract org.drip.quant.function1D.AbstractUnivariate collateralCollateralCorrSurface (
		final java.lang.String strCurrency1,
		final java.lang.String strCurrency2);

	/**
	 * (Re)-set the Correlation Surface for the specified Collateral Currency Pair
	 * 
	 * @param strCurrency1 Collateral Currency #1
	 * @param strCurrency2 Collateral Currency #2
	 * @param auCorrelation The Correlation Surface
	 * 
	 * @return TRUE => Successfully set
	 */

	public abstract boolean setCollateralCollateralCorrSurface (
		final java.lang.String strCurrency1,
		final java.lang.String strCurrency2,
		final org.drip.quant.function1D.AbstractUnivariate auCorrelation);

	/**
	 * Retrieve the Correlation Surface for the specified Custom Metric Pair
	 * 
	 * @param strCustomMetric1 Custom Metric #1
	 * @param strCustomMetric2 Custom Metric #2
	 * 
	 * @return The Correlation Surface for the specified Custom Metric Pair
	 */

	public abstract org.drip.quant.function1D.AbstractUnivariate customMetricCustomMetricCorrSurface (
		final java.lang.String strCustomMetric1,
		final java.lang.String strCustomMetric2);

	/**
	 * (Re)-set the Correlation Surface for the specified Custom Metric Pair
	 * 
	 * @param strCustomMetric1 Custom Metric #1
	 * @param strCustomMetric2 Custom Metric #2
	 * @param auCorrelation The Correlation Surface
	 * 
	 * @return TRUE => Successfully set
	 */

	public abstract boolean setCustomMetricCustomMetricCorrSurface (
		final java.lang.String strCustomMetric1,
		final java.lang.String strCustomMetric2,
		final org.drip.quant.function1D.AbstractUnivariate auCorrelation);

	/**
	 * Retrieve the Correlation Surface for the specified FRI Pair
	 * 
	 * @param fri1 FRI #1
	 * @param fri2 FRI #2
	 * 
	 * @return The Correlation Surface for the specified FRI Pair
	 */

	public abstract org.drip.quant.function1D.AbstractUnivariate forwardForwardCorrSurface (
		final org.drip.product.params.FloatingRateIndex fri1,
		final org.drip.product.params.FloatingRateIndex fri2);

	/**
	 * (Re)-set the Correlation Surface for the specified FRI Pair
	 * 
	 * @param fri1 FRI #1
	 * @param fri2 FRI #2
	 * @param auCorrelation The Correlation Surface
	 * 
	 * @return TRUE => Successfully set
	 */

	public abstract boolean setForwardForwardCorrSurface (
		final org.drip.product.params.FloatingRateIndex fri1,
		final org.drip.product.params.FloatingRateIndex fri2,
		final org.drip.quant.function1D.AbstractUnivariate auCorrelation);

	/**
	 * Retrieve the Correlation Surface for the specified Funding Currency Pair
	 * 
	 * @param strCurrency1 Funding Currency #1
	 * @param strCurrency2 Funding Currency #2
	 * 
	 * @return The Correlation Surface for the specified Funding Currency Pair
	 */

	public abstract org.drip.quant.function1D.AbstractUnivariate fundingFundingCorrSurface (
		final java.lang.String strCurrency1,
		final java.lang.String strCurrency2);

	/**
	 * (Re)-set the Correlation Surface for the specified Funding Currency Pair
	 * 
	 * @param strCurrency1 Funding Currency #1
	 * @param strCurrency2 Funding Currency #2
	 * @param auCorrelation The Correlation Surface
	 * 
	 * @return TRUE => Successfully set
	 */

	public abstract boolean setFundingFundingCorrSurface (
		final java.lang.String strCurrency1,
		final java.lang.String strCurrency2,
		final org.drip.quant.function1D.AbstractUnivariate auCorrelation);

	/**
	 * Retrieve the Correlation Surface for the specified Collateral Currency and the FRI
	 * 
	 * @param strCollateralCurrency The Collateral Currency
	 * @param fri The Floating Rate Index
	 * 
	 * @return The Correlation Surface for the specified Collateral Currency and the FRI
	 */

	public abstract org.drip.quant.function1D.AbstractUnivariate collateralForwardCorrSurface (
		final java.lang.String strCollateralCurrency,
		final org.drip.product.params.FloatingRateIndex fri);

	/**
	 * (Re)-set the Correlation Surface for the specified Collateral Currency and the FRI
	 * 
	 * @param strCollateralCurrency The Collateral Currency
	 * @param fri The Floating Rate Index
	 * @param auCorrelation The Correlation Surface
	 * 
	 * @return TRUE => Successfully set
	 */

	public abstract boolean setCollateralForwardCorrSurface (
		final java.lang.String strCollateralCurrency,
		final org.drip.product.params.FloatingRateIndex fri,
		final org.drip.quant.function1D.AbstractUnivariate auCorrelation);

	/**
	 * Retrieve the Correlation Surface for the specified Collateral Currency and the Custom Metric
	 * 
	 * @param strCollateralCurrency The Collateral Currency
	 * @param strCustomMetric The Custom Metric
	 * 
	 * @return The Correlation Surface for the specified Collateral Currency and the Custom Metric
	 */

	public abstract org.drip.quant.function1D.AbstractUnivariate collateralCustomMetricCorrSurface (
		final java.lang.String strCollateralCurrency,
		final java.lang.String strCustomMetric);

	/**
	 * (Re)-set the Correlation Surface for the specified Collateral Currency and the Custom Metric
	 * 
	 * @param strCollateralCurrency The Collateral Currency
	 * @param strCustomMetric The Custom Metric
	 * @param auCorrelation The Correlation Surface
	 * 
	 * @return TRUE => Successfully set
	 */

	public abstract boolean setCollateralCustomMetricCorrSurface (
		final java.lang.String strCollateralCurrency,
		final java.lang.String strCustomMetric,
		final org.drip.quant.function1D.AbstractUnivariate auCorrelation);

	/**
	 * Retrieve the Correlation Surface for the specified Collateral and the Funding Currencies
	 * 
	 * @param strCollateralCurrency The Collateral Currency
	 * @param strFundingCurrency The Funding Currency
	 * 
	 * @return The Correlation Surface for the specified Collateral and the Funding Curves
	 */

	public abstract org.drip.quant.function1D.AbstractUnivariate collateralFundingCorrSurface (
		final java.lang.String strCollateralCurrency,
		final java.lang.String strFundingCurrency);

	/**
	 * (Re)-set the Correlation Surface for the specified Collateral and the Funding Currencies
	 * 
	 * @param strCollateralCurrency The Collateral Currency
	 * @param strFundingCurrency The Funding Currency
	 * @param auCorrelation The Correlation Surface
	 * 
	 * @return TRUE => Successfully set
	 */

	public abstract boolean setCollateralFundingCorrSurface (
		final java.lang.String strCollateralCurrency,
		final java.lang.String strFundingCurrency,
		final org.drip.quant.function1D.AbstractUnivariate auCorrelation);

	/**
	 * Retrieve the Correlation Surface for the specified Custom Metric and the FRI
	 * 
	 * @param strCustomMetric The Custom Metric
	 * @param fri The FRI
	 * 
	 * @return The Correlation Surface for the specified Custom Metric and the FRI
	 */

	public abstract org.drip.quant.function1D.AbstractUnivariate customMetricForwardCorrSurface (
		final java.lang.String strCustomMetric,
		final org.drip.product.params.FloatingRateIndex fri);

	/**
	 * (Re)-set the Correlation Surface for the specified Custom Metric and the FRI
	 * 
	 * @param strCustomMetric The Custom Metric
	 * @param fri The FRI
	 * @param auCorrelation The Correlation Surface
	 * 
	 * @return TRUE => Successfully set
	 */

	public abstract boolean setCustomMetricForwardCorrSurface (
		final java.lang.String strCustomMetric,
		final org.drip.product.params.FloatingRateIndex fri,
		final org.drip.quant.function1D.AbstractUnivariate auCorrelation);

	/**
	 * Retrieve the Correlation Surface for the specified Custom Metric and the Funding Currency
	 * 
	 * @param strCustomMetric The Custom Metric
	 * @param strFundingCurrency The Funding Currency
	 * 
	 * @return The Correlation Surface for the specified Custom Metric and the Funding Currency
	 */

	public abstract org.drip.quant.function1D.AbstractUnivariate customMetricFundingCorrSurface (
		final java.lang.String strCustomMetric,
		final java.lang.String strFundingCurrency);

	/**
	 * (Re)-set the Correlation Surface for the specified Custom Metric and the Funding Currency
	 * 
	 * @param strCustomMetric The Custom Metric
	 * @param strFundingCurrency The Funding Currency
	 * @param auCorrelation The Correlation Surface
	 * 
	 * @return TRUE => Successfully set
	 */

	public abstract boolean setCustomMetricFundingCorrSurface (
		final java.lang.String strCustomMetric,
		final java.lang.String strFundingCurrency,
		final org.drip.quant.function1D.AbstractUnivariate auCorrelation);

	/**
	 * Retrieve the Correlation Surface for the specified FRI and the Funding Currency
	 * 
	 * @param fri The FRI
	 * @param strFundingCurrency The Funding Currency
	 * 
	 * @return The Correlation Surface for the specified FRI and the Funding Currency
	 */

	public abstract org.drip.quant.function1D.AbstractUnivariate forwardFundingCorrSurface (
		final org.drip.product.params.FloatingRateIndex fri,
		final java.lang.String strFundingCurrency);

	/**
	 * (Re)-set the Correlation Surface for the specified FRI and the Funding Currency
	 * 
	 * @param fri The FRI
	 * @param strFundingCurrency The Funding Currency
	 * @param auCorrelation The Correlation Surface
	 * 
	 * @return TRUE => Successfully set
	 */

	public abstract boolean setForwardFundingCorrSurface (
		final org.drip.product.params.FloatingRateIndex fri,
		final java.lang.String strFundingCurrency,
		final org.drip.quant.function1D.AbstractUnivariate auCorrelation);

	/**
	 * Retrieve the Component's FX Curve for the specified currency Pair
	 * 
	 * @param cp The Currency Pair
	 * 
	 * @return Component's FX Curve
	 */

	public abstract org.drip.quant.function1D.AbstractUnivariate fxCurve (
		final org.drip.product.params.CurrencyPair cp);

	/**
	 * (Re)-set the Component's FX Curve for the specified Currency Pair
	 * 
	 * @param cp The Currency Pair
	 * @param auFX The FX Curve
	 * 
	 * @return TRUE => Successfully set
	 */

	public abstract boolean setFXCurve (
		final org.drip.product.params.CurrencyPair cp,
		final org.drip.quant.function1D.AbstractUnivariate auFX);
}
