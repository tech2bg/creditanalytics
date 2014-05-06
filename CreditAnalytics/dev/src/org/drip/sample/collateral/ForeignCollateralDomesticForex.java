
package org.drip.sample.collateral;

import java.util.Map;

import org.drip.analytics.date.JulianDate;
import org.drip.analytics.rates.DiscountCurve;
import org.drip.analytics.support.CaseInsensitiveTreeMap;
import org.drip.param.creator.ComponentMarketParamsBuilder;
import org.drip.param.definition.ComponentMarketParams;
import org.drip.param.valuation.*;
import org.drip.product.fx.ForeignCollateralizedDomesticForward;
import org.drip.product.params.CurrencyPair;
import org.drip.quant.function1D.*;
import org.drip.service.api.CreditAnalytics;
import org.drip.state.creator.DiscountCurveBuilder;
import org.drip.state.curve.ForeignCollateralizedDiscountCurve;

/*
 * -*- mode: java; tab-width: 4; indent-tabs-mode: nil; c-basic-offset: 4 -*-
 */

/*!
 * Copyright (C) 2014 Lakshmi Krishnamurthy
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
 * ForeignCollateralDomesticForex demonstrates the construction and the usage of Foreign Currency
 * 	Collateralized Domestic Pay-out FX forward product, and generation of its measures.
 *  
 * @author Lakshmi Krishnamurthy
 */

public class ForeignCollateralDomesticForex {
	public static final void main (
		final String[] astrArgs)
		throws Exception
	{
		/*
		 * Initialize the Credit Analytics Library
		 */

		CreditAnalytics.Init ("");

		JulianDate dtToday = JulianDate.Today();

		String strDomesticCurrency = "USD";
		String strForeignCurrency = "EUR";
		String strMaturity = "1Y";
		double dblFXFwdStrike = 1.016;
		double dblForeignCollateralRate = 0.02;
		double dblCollateralizedFXRate = 0.01;
		double dblForeignRatesVolatility = 0.30;
		double dblFXVolatility = 0.10;
		double dblFXForeignRatesCorrelation = 0.20;

		CurrencyPair cp = CurrencyPair.FromCode (strForeignCurrency + "/" + strDomesticCurrency);

		DiscountCurve dcCcyForeignCollatForeign = DiscountCurveBuilder.CreateFromFlatRate (
			dtToday,
			strForeignCurrency,
			new CollateralizationParams ("OVERNIGHT_INDEX", strForeignCurrency),
			dblForeignCollateralRate);

		AbstractUnivariate auFX = new ExponentialDecay (dtToday.getJulian(), dblCollateralizedFXRate / 365.25);

		DiscountCurve dcCcyDomesticCollatForeign = new ForeignCollateralizedDiscountCurve (
			strDomesticCurrency,
			dcCcyForeignCollatForeign,
			auFX,
			new FlatUnivariate (dblForeignRatesVolatility),
			new FlatUnivariate (dblFXVolatility),
			new FlatUnivariate (dblFXForeignRatesCorrelation));

		ComponentMarketParams cmp = ComponentMarketParamsBuilder.CreateComponentMarketParams
			(null, null, null, null, null, null, null);

		cmp.setPayCurrencyCollateralCurrencyCurve (strDomesticCurrency, strForeignCurrency, dcCcyDomesticCollatForeign);

		cmp.setPayCurrencyCollateralCurrencyCurve (strForeignCurrency, strForeignCurrency, dcCcyForeignCollatForeign);

		cmp.setFXCurve (cp.getCode(), auFX);

		ForeignCollateralizedDomesticForward fcff = new ForeignCollateralizedDomesticForward (
			cp,
			dblFXFwdStrike,
			dtToday.addTenor (strMaturity));

		CaseInsensitiveTreeMap<Double> mapFCFF = fcff.value (
			new ValuationParams (dtToday, dtToday, strDomesticCurrency),
			null,
			cmp,
			null);

		for (Map.Entry<String, Double> me : mapFCFF.entrySet())
			System.out.println ("\t" + me.getKey() + " => " + me.getValue());
	}
}
