
package org.drip.sample.xccy;

import org.drip.analytics.date.JulianDate;
import org.drip.analytics.rates.*;
import org.drip.analytics.support.*;
import org.drip.param.creator.ScenarioForwardCurveBuilder;
import org.drip.param.market.CurveSurfaceQuoteSet;
import org.drip.param.valuation.*;
import org.drip.product.cashflow.*;
import org.drip.product.params.*;
import org.drip.product.rates.*;
import org.drip.quant.common.FormatUtil;
import org.drip.quant.function1D.FlatUnivariate;
import org.drip.service.api.CreditAnalytics;
import org.drip.state.creator.DiscountCurveBuilder;
import org.drip.state.identifier.*;

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
 * CrossFixedPlainFloatAnalysis demonstrates the impact of Funding Volatility, Forward Volatility, and
 *  Funding/Forward Correlation on the Valuation of a fix-float swap with a EUR Fixed leg that pays in USD,
 *  and a USD Floating Leg. Comparison is done across MTM and non-MTM fixed Leg Counterparts.
 * 
 * @author Lakshmi Krishnamurthy
 */

public class CrossFixedPlainFloatAnalysis {

	private static final FixFloatComponent MakeFixFloatSwap (
		final JulianDate dtEffective,
		final boolean bFXMTM,
		final String strPayCurrency,
		final String strFixedCouponCurrency,
		final String strTenor,
		final int iTenorInMonths)
		throws Exception
	{
		/*
		 * The Fixed Leg
		 */

		FixedStream fixStream = new FixedStream (
			PeriodBuilder.RegularPeriodSingleReset (
				dtEffective.julian(),
				strTenor,
				bFXMTM ? Double.NaN : dtEffective.julian(),
				null,
				2,
				"Act/360",
				false,
				false,
				strFixedCouponCurrency,
				-1.,
				null,
				0.02,
				strPayCurrency,
				strFixedCouponCurrency,
				null,
				null
			)
		);

		fixStream.setPrimaryCode (strPayCurrency + "_" + strFixedCouponCurrency + "::FIXED::" + strTenor);

		/*
		 * The Derived Leg
		 */

		FloatingStream floatStream = new FloatingStream (
			PeriodBuilder.RegularPeriodSingleReset (
				dtEffective.julian(),
				strTenor,
				Double.NaN,
				null,
				12 / iTenorInMonths,
				"Act/360",
				false,
				false,
				strPayCurrency,
				1.,
				null,
				0.,
				strPayCurrency,
				strPayCurrency,
				ForwardLabel.Standard (strPayCurrency + "-LIBOR-" + iTenorInMonths + "M"),
				null
			)
		);

		floatStream.setPrimaryCode (strPayCurrency + "_" + strPayCurrency + "::FIXED::" + iTenorInMonths + "M::" + strTenor);

		/*
		 * The fix-float swap instance
		 */

		FixFloatComponent fixFloat = new FixFloatComponent (
			fixStream,
			floatStream,
			new CashSettleParams (0, strPayCurrency, 0)
		);

		fixFloat.setPrimaryCode (fixStream.primaryCode() + "__" + floatStream.primaryCode());

		return fixFloat;
	}

	private static final void SetMarketParams (
		final CurveSurfaceQuoteSet mktParams,
		final ForwardLabel forwardLabel,
		final FundingLabel fundingLabel,
		final FXLabel fxLabel,
		final double dblForwardVol,
		final double dblFundingVol,
		final double dblFXVol,
		final double dblForwardFundingCorr,
		final double dblForwardFXCorr,
		final double dblFundingFXCorr)
		throws Exception
	{
		mktParams.setForwardCurveVolSurface (forwardLabel, new FlatUnivariate (dblForwardVol));

		mktParams.setFundingCurveVolSurface (fundingLabel, new FlatUnivariate (dblFundingVol));

		mktParams.setFXCurveVolSurface (fxLabel, new FlatUnivariate (dblFXVol));

		mktParams.setForwardFundingCorrSurface (forwardLabel, fundingLabel, new FlatUnivariate (dblForwardFundingCorr));

		mktParams.setForwardFXCorrSurface (forwardLabel, fxLabel, new FlatUnivariate (dblForwardFXCorr));

		mktParams.setFundingFXCorrSurface (fundingLabel, fxLabel, new FlatUnivariate (dblFundingFXCorr));
	}

	private static final void VolCorrScenario (
		final FixFloatComponent[] aFixFloat,
		final ValuationParams valParams,
		final CurveSurfaceQuoteSet mktParams,
		final ForwardLabel forwardLabel,
		final FundingLabel fundingLabel,
		final FXLabel fxLabel,
		final double dblForwardVol,
		final double dblFundingVol,
		final double dblFXVol,
		final double dblForwardFundingCorr,
		final double dblForwardFXCorr,
		final double dblFundingFXCorr)
		throws Exception
	{
		SetMarketParams (
			mktParams,
			forwardLabel,
			fundingLabel,
			fxLabel,
			dblForwardVol,
			dblFundingVol,
			dblFXVol,
			dblForwardFundingCorr,
			dblForwardFXCorr,
			dblFundingFXCorr
		);

		String strDump = "\t[" +
			FormatUtil.FormatDouble (dblForwardVol, 2, 0, 100.) + "%," +
			FormatUtil.FormatDouble (dblFundingVol, 2, 0, 100.) + "%," +
			FormatUtil.FormatDouble (dblFXVol, 2, 0, 100.) + "%," +
			FormatUtil.FormatDouble (dblForwardFundingCorr, 2, 0, 100.) + "%," +
			FormatUtil.FormatDouble (dblForwardFXCorr, 2, 0, 100.) + "%," +
			FormatUtil.FormatDouble (dblFundingFXCorr, 2, 0, 100.) + "%] = ";

		for (int i = 0; i < aFixFloat.length; ++i) {
			CaseInsensitiveTreeMap<Double> mapOutput = aFixFloat[i].value (valParams, null, mktParams, null);

			if (0 != i) strDump += " || ";

			strDump +=
				FormatUtil.FormatDouble (mapOutput.get ("ReferenceConvexityAdjustmentPremium"), 2, 0, 10000.) + " | " +
				FormatUtil.FormatDouble (mapOutput.get ("DerivedConvexityAdjustmentPremium"), 2, 0, 10000.) + " | " +
				FormatUtil.FormatDouble (mapOutput.get ("ConvexityAdjustmentPremium"), 2, 0, 10000.);
		}

		System.out.println (strDump);
	}

	public static final void main (
		final String[] astrArgs)
		throws Exception
	{
		double dblUSDCollateralRate = 0.02;
		double dblEURCollateralRate = 0.02;
		double dblUSD3MForwardRate = 0.02;
		double dblUSDEURFXRate = 1. / 1.35;

		/*
		 * Initialize the Credit Analytics Library
		 */

		CreditAnalytics.Init ("");

		JulianDate dtToday = JulianDate.Today();

		ValuationParams valParams = new ValuationParams (dtToday, dtToday, "USD");

		ForwardLabel fri3M = ForwardLabel.Create ("USD", "LIBOR", "3M");

		DiscountCurve dcUSDCollatDomestic = DiscountCurveBuilder.CreateFromFlatRate (
			dtToday,
			"USD",
			new CollateralizationParams ("OVERNIGHT_INDEX", "USD"),
			dblUSDCollateralRate
		);

		DiscountCurve dcEURCollatDomestic = DiscountCurveBuilder.CreateFromFlatRate (
			dtToday,
			"EUR",
			new CollateralizationParams ("OVERNIGHT_INDEX", "EUR"),
			dblEURCollateralRate
		);

		ForwardCurve fc3MUSD = ScenarioForwardCurveBuilder.FlatForwardForwardCurve (
			dtToday,
			fri3M,
			dblUSD3MForwardRate,
			new CollateralizationParams ("OVERNIGHT_INDEX", "USD")
		);

		CurrencyPair cp = CurrencyPair.FromCode ("USD/EUR");

		FixFloatComponent fixMTMFloat = MakeFixFloatSwap (
			dtToday,
			true,
			"USD",
			"EUR",
			"2Y",
			3
		);

		FixFloatComponent fixNonMTMFloat = MakeFixFloatSwap (
			dtToday,
			false,
			"USD",
			"EUR",
			"2Y",
			3
		);

		FXLabel fxLabel = FXLabel.Standard (cp);

		CurveSurfaceQuoteSet mktParams = new CurveSurfaceQuoteSet();

		mktParams.setFundingCurve (dcUSDCollatDomestic);

		mktParams.setForwardCurve (fc3MUSD);

		mktParams.setFundingCurve (dcEURCollatDomestic);

		mktParams.setFXCurve (fxLabel, new FlatUnivariate (dblUSDEURFXRate));

		mktParams.setFixing (dtToday, fxLabel, dblUSDEURFXRate);

		double[] adblForwardVol = new double[] {0.1, 0.35, 0.60};

		double[] adblFundingVol = new double[] {0.1, 0.35, 0.60};

		double[] adblFXVol = new double[] {0.1, 0.35, 0.60};

		double[] adblForwardFundingCorr = new double[] {-0.1, 0.35};

		double[] adblForwardFXCorr = new double[] {-0.1, 0.35};

		double[] adblFundingFXCorr = new double[] {-0.1, 0.35};

		for (double dblForwardVol : adblForwardVol) {
			for (double dblFundingVol : adblFundingVol) {
				for (double dblFXVol : adblFXVol) {
					for (double dblForwardFundingCorr : adblForwardFundingCorr) {
						for (double dblForwardFXCorr : adblForwardFXCorr) {
							for (double dblFundingFXCorr : adblFundingFXCorr)
								VolCorrScenario (
									new FixFloatComponent[] {fixMTMFloat, fixNonMTMFloat},
									valParams,
									mktParams,
									fri3M,
									FundingLabel.Standard ("USD"),
									fxLabel,
									dblForwardVol,
									dblFundingVol,
									dblFXVol,
									dblForwardFundingCorr,
									dblForwardFXCorr,
									dblFundingFXCorr
								);
						}
					}
				}
			}
		}
	}
}
