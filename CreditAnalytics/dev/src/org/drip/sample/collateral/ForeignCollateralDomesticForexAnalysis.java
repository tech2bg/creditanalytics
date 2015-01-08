
package org.drip.sample.collateral;

import org.drip.analytics.date.DateUtil;
import org.drip.analytics.date.JulianDate;
import org.drip.analytics.rates.DiscountCurve;
import org.drip.analytics.support.CaseInsensitiveTreeMap;
import org.drip.param.creator.MarketParamsBuilder;
import org.drip.param.market.CurveSurfaceQuoteSet;
import org.drip.param.valuation.*;
import org.drip.product.fx.ForeignCollateralizedDomesticForward;
import org.drip.product.params.CurrencyPair;
import org.drip.quant.function1D.*;
import org.drip.service.api.CreditAnalytics;
import org.drip.state.creator.DiscountCurveBuilder;
import org.drip.state.curve.ForeignCollateralizedDiscountCurve;
import org.drip.state.identifier.FXLabel;

/*
 * -*- mode: java; tab-width: 4; indent-tabs-mode: nil; c-basic-offset: 4 -*-
 */

/*!
 * Copyright (C) 2015 Lakshmi Krishnamurthy
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
 * ForeignCollateralDomesticForexAnalysis contains an analysis of the correlation and volatility impact on the
 * 	price of a Foreign Collateralized Domestic Pay-out Forex Contract.
 * 
 * @author Lakshmi Krishnamurthy
 */

public class ForeignCollateralDomesticForexAnalysis {
	public static final void main (
		final String[] astrArgs)
		throws Exception
	{
		/*
		 * Initialize the Credit Analytics Library
		 */

		CreditAnalytics.Init ("");

		JulianDate dtToday = DateUtil.Today();

		String strDomesticCurrency = "USD";
		String strForeignCurrency = "EUR";
		String strMaturity = "1Y";
		double dblFXFwdStrike = 1.016;
		double dblForeignCollateralRate = 0.02;
		double dblCollateralizedFXRate = 0.01;

		CurrencyPair cp = CurrencyPair.FromCode (strForeignCurrency + "/" + strDomesticCurrency);

		DiscountCurve dcCcyForeignCollatForeign = DiscountCurveBuilder.CreateFromFlatRate (
			dtToday,
			strForeignCurrency,
			new CollateralizationParams ("OVERNIGHT_INDEX", strForeignCurrency),
			dblForeignCollateralRate
		);

		AbstractUnivariate auFX = new ExponentialDecay (
			dtToday.julian(),
			dblCollateralizedFXRate / 365.25
		);

		ValuationParams valParams = new ValuationParams (
			dtToday,
			dtToday,
			strDomesticCurrency
		);

		DiscountCurve dcCcyDomesticCollatForeign = new ForeignCollateralizedDiscountCurve (
			strDomesticCurrency,
			dcCcyForeignCollatForeign,
			auFX,
			new FlatUnivariate (0.),
			new FlatUnivariate (0.),
			new FlatUnivariate (0.)
		);

		CurveSurfaceQuoteSet mktParams = MarketParamsBuilder.Create (null, null, null, null, null, null, null);

		mktParams.setPayCurrencyCollateralCurrencyCurve (
			strDomesticCurrency,
			strForeignCurrency,
			dcCcyDomesticCollatForeign
		);

		mktParams.setPayCurrencyCollateralCurrencyCurve (
			strForeignCurrency,
			strForeignCurrency,
			dcCcyForeignCollatForeign
		);

		mktParams.setFXCurve (
			FXLabel.Standard (cp),
			auFX
		);

		ForeignCollateralizedDomesticForward fcff = new ForeignCollateralizedDomesticForward (
			cp,
			dblFXFwdStrike,
			dtToday.addTenor (strMaturity)
		);

		CaseInsensitiveTreeMap<Double> mapBaseValue = fcff.value (
			new ValuationParams (dtToday, dtToday, strDomesticCurrency),
			null,
			mktParams,
			null
		);

		double dblBaselinePrice = mapBaseValue.get ("Price");

		double dblBaselineParForward = mapBaseValue.get ("ParForward");

		double[] adblForeignRatesVolatility = new double[] {0.1, 0.2, 0.3, 0.4, 0.5};
		double[] adblFXVolatility = new double[] {0.10, 0.15, 0.20, 0.25, 0.30};
		double[] adblFXForeignRatesCorrelation = new double[] {-0.99, -0.50, 0.00, 0.50, 0.99};

		System.out.println ("\tPrinting the Foreign Collateralized Forex Output in Order (Left -> Right):");

		System.out.println ("\t\tPrice (%)");

		System.out.println ("\t\tPrice Difference (%)");

		System.out.println ("\t\tPar Forward (abs)");

		System.out.println ("\t\tPar Forward Difference (abs)");

		System.out.println ("\t-------------------------------------------------------------");

		System.out.println ("\t-------------------------------------------------------------");

		for (double dblForeignRatesVolatility : adblForeignRatesVolatility) {
			for (double dblFXVolatility : adblFXVolatility) {
				for (double dblFXForeignRatesCorrelation : adblFXForeignRatesCorrelation) {
					dcCcyDomesticCollatForeign = new ForeignCollateralizedDiscountCurve (
						strDomesticCurrency,
						dcCcyForeignCollatForeign,
						auFX,
						new FlatUnivariate (dblForeignRatesVolatility),
						new FlatUnivariate (dblFXVolatility),
						new FlatUnivariate (dblFXForeignRatesCorrelation)
					);

					mktParams.setPayCurrencyCollateralCurrencyCurve (
						strDomesticCurrency,
						strForeignCurrency,
						dcCcyDomesticCollatForeign
					);

					CaseInsensitiveTreeMap<Double> mapFCFF = fcff.value (
						valParams,
						null,
						mktParams,
						null
					);

					double dblPrice = mapFCFF.get ("Price");

					double dblParForward = mapFCFF.get ("ParForward");

					System.out.println ("\t[" +
						org.drip.quant.common.FormatUtil.FormatDouble (dblForeignRatesVolatility, 2, 0, 100.) + "%," +
						org.drip.quant.common.FormatUtil.FormatDouble (dblFXVolatility, 2, 0, 100.) + "%," +
						org.drip.quant.common.FormatUtil.FormatDouble (dblFXForeignRatesCorrelation, 2, 0, 100.) + "%] = " +
						org.drip.quant.common.FormatUtil.FormatDouble (dblPrice, 2, 2, 100.) + " | " +
						org.drip.quant.common.FormatUtil.FormatDouble (dblPrice - dblBaselinePrice, 1, 2, 100.) + " | " +
						org.drip.quant.common.FormatUtil.FormatDouble (dblParForward, 1, 4, 1.) + " | " +
						org.drip.quant.common.FormatUtil.FormatDouble (dblParForward - dblBaselineParForward, 1, 4, 1.)
					);
				}
			}
		}
	}
}
