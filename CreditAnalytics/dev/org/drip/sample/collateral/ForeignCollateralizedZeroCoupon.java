
package org.drip.sample.collateral;

import org.drip.analytics.date.JulianDate;
import org.drip.analytics.rates.DiscountCurve;
import org.drip.param.valuation.CollateralizationParams;
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
 * ForeignCollateralizedZeroCoupon contains an analysis of the correlation and volatility impact on the
 * 	single cash flow discount factor of a Foreign Collateralized Zero Coupon.
 * 
 * @author Lakshmi Krishnamurthy
 */

public class ForeignCollateralizedZeroCoupon {
	private static final double ZeroCouponVolCorr (
		final String strDomesticCurrency,
		final DiscountCurve dcCcyForeignCollatForeign,
		final AbstractUnivariate auFX,
		final double dblForeignRatesVolatility,
		final double dblFXVolatility,
		final double dblFXForeignRatesCorrelation,
		final JulianDate dtMaturity,
		final double dblBaselinePrice)
		throws Exception
	{
		DiscountCurve dcCcyDomesticCollatForeign = new ForeignCollateralizedDiscountCurve (
			strDomesticCurrency,
			dcCcyForeignCollatForeign,
			auFX,
			new FlatUnivariate (dblForeignRatesVolatility),
			new FlatUnivariate (dblFXVolatility),
			new FlatUnivariate (dblFXForeignRatesCorrelation));

		double dblPrice = dcCcyDomesticCollatForeign.df (dtMaturity);

		System.out.println ("\t[" +
			org.drip.quant.common.FormatUtil.FormatDouble (dblForeignRatesVolatility, 2, 0, 100.) + "%," +
			org.drip.quant.common.FormatUtil.FormatDouble (dblFXVolatility, 2, 0, 100.) + "%," +
			org.drip.quant.common.FormatUtil.FormatDouble (dblFXForeignRatesCorrelation, 2, 0, 100.) + "%] =" +
			org.drip.quant.common.FormatUtil.FormatDouble (dblPrice, 1, 2, 100.) + " | " +
			org.drip.quant.common.FormatUtil.FormatDouble (dblPrice - dblBaselinePrice, 2, 0, 100.));

		return dblPrice;
	}

	public static final void main (
		final String[] astrArgs)
		throws Exception
	{
		/*
		 * Initialize the Credit Analytics Library
		 */

		CreditAnalytics.Init ("");

		JulianDate dtToday = JulianDate.Today();

		String strMaturityTenor = "5Y";
		String strDomesticCurrency = "USD";
		String strForeignCurrency = "JPY";
		double dblForeignCollateralRate = 0.02;
		double dblCollateralizedFXRate = 0.01;

		JulianDate dtZeroCouponMaturity = dtToday.addTenor (strMaturityTenor);

		DiscountCurve dcCcyForeignCollatForeign = DiscountCurveBuilder.CreateFromFlatRate (
			dtToday,
			strForeignCurrency,
			new CollateralizationParams ("OVERNIGHT_INDEX", strForeignCurrency),
			dblForeignCollateralRate);

		AbstractUnivariate auFX = new ExponentialDecay (dtToday.julian(), dblCollateralizedFXRate / 365.25);

		double dblBaselinePrice = ZeroCouponVolCorr (
			strDomesticCurrency,
			dcCcyForeignCollatForeign,
			auFX,
			0.,
			0.,
			0.,
			dtZeroCouponMaturity,
			0.);

		double[] adblForeignRatesVol = new double[] {0.1, 0.2, 0.3, 0.4, 0.5};
		double[] adblFXVol = new double[] {0.10, 0.15, 0.20, 0.25, 0.30};
		double[] adblForeignRatesFXCorr = new double[] {-0.99, -0.50, 0.00, 0.50, 0.99};

		System.out.println ("\tPrinting the Zero Coupon Bond Price in Order (Left -> Right):");

		System.out.println ("\t\tPrice (%)");

		System.out.println ("\t\tDifference from Baseline (pt)");

		System.out.println ("\t-------------------------------------------------------------");

		System.out.println ("\t-------------------------------------------------------------");

		for (double dblForeignRatesVol : adblForeignRatesVol) {
			for (double dblFXVol : adblFXVol) {
				for (double dblForeignRatesFXCorr : adblForeignRatesFXCorr)
					ZeroCouponVolCorr (
						strDomesticCurrency,
						dcCcyForeignCollatForeign,
						auFX,
						dblForeignRatesVol,
						dblFXVol,
						dblForeignRatesFXCorr,
						dtZeroCouponMaturity,
						dblBaselinePrice);
			}
		}
	}
}
