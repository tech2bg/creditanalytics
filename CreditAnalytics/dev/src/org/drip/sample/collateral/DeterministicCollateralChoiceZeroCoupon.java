
package org.drip.sample.collateral;

import org.drip.analytics.date.DateUtil;
import org.drip.analytics.date.JulianDate;
import org.drip.analytics.rates.DiscountCurve;
import org.drip.function.deterministic.R1ToR1;
import org.drip.function.deterministic1D.*;
import org.drip.param.valuation.CollateralizationParams;
import org.drip.quant.common.FormatUtil;
import org.drip.service.api.CreditAnalytics;
import org.drip.state.creator.DiscountCurveBuilder;
import org.drip.state.curve.*;

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
 * DeterministicCollateralChoiceZeroCoupon contains an analysis of the impact on the single cash flow
 * 	discount factor of a Zero Coupon collateralized using a deterministic choice of collaterals.
 * 
 * @author Lakshmi Krishnamurthy
 */

public class DeterministicCollateralChoiceZeroCoupon {
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
		String strForeignCurrency = "JPY";
		double dblDomesticCollateralRate = 0.03;
		double dblForeignCollateralRate = 0.02;
		double dblCollateralizedFXRate = 0.01;
		double dblForeignRatesVolatility = 0.20;
		double dblFXVolatility = 0.10;
		double dblFXForeignRatesCorrelation = 0.30;
		int iDiscreteCollateralizationIncrement = 30; // 30 Days
		String strCollateralizationCheckTenor = "5Y";

		DiscountCurve dcCcyDomesticCollatDomestic = DiscountCurveBuilder.CreateFromFlatRate (
			dtToday,
			strDomesticCurrency,
			new CollateralizationParams ("OVERNIGHT_INDEX", strDomesticCurrency),
			dblDomesticCollateralRate
		);

		DiscountCurve dcCcyForeignCollatForeign = DiscountCurveBuilder.CreateFromFlatRate (
			dtToday,
			strForeignCurrency,
			new CollateralizationParams ("OVERNIGHT_INDEX", strForeignCurrency),
			dblForeignCollateralRate
		);

		R1ToR1 auFX = new ExponentialDecay (dtToday.julian(), dblCollateralizedFXRate / 365.25);

		ForeignCollateralizedDiscountCurve dcCcyDomesticCollatForeign = new ForeignCollateralizedDiscountCurve (
			strDomesticCurrency,
			dcCcyForeignCollatForeign,
			auFX,
			new FlatUnivariate (dblForeignRatesVolatility),
			new FlatUnivariate (dblFXVolatility),
			new FlatUnivariate (dblFXForeignRatesCorrelation)
		);

		DeterministicCollateralChoiceDiscountCurve dccdc = new DeterministicCollateralChoiceDiscountCurve (
			dcCcyDomesticCollatDomestic,
			new org.drip.state.curve.ForeignCollateralizedDiscountCurve[] {dcCcyDomesticCollatForeign},
			iDiscreteCollateralizationIncrement
		);

		double dblStart = dtToday.julian() + iDiscreteCollateralizationIncrement;

		double dblCollateralizationCheckDate = dtToday.addTenor (strCollateralizationCheckTenor).julian();

		System.out.println ("\tPrinting the Zero Coupon Bond Price in Order (Left -> Right):");

		System.out.println ("\t\tDate");

		System.out.println ("\t\tDomestic Collateral Price (Par = 100)");

		System.out.println ("\t\tForeign Collateral Price (Par = 100)");

		System.out.println ("\t\tChoice Collateral Price (Par = 100)");

		System.out.println ("\t-------------------------------------------------------------");

		System.out.println ("\t-------------------------------------------------------------");

		for (double dblDate = dblStart; dblDate <= dblCollateralizationCheckDate; dblDate += iDiscreteCollateralizationIncrement) {
			double dblDomesticCollateralDF = dcCcyDomesticCollatDomestic.df (dblDate);

			double dblForeignCollateralDF = dcCcyDomesticCollatForeign.df (dblDate);

			double dblChoiceCollateralDF = dccdc.df (dblDate);

			System.out.println (
				new JulianDate (dblDate) + " => " +
				FormatUtil.FormatDouble (dblDomesticCollateralDF, 2, 2, 100.) + " | " +
				FormatUtil.FormatDouble (dblForeignCollateralDF, 2, 2, 100.) + " | " +
				FormatUtil.FormatDouble (dblChoiceCollateralDF, 2, 2, 100.)
			);
		}
	}
}
