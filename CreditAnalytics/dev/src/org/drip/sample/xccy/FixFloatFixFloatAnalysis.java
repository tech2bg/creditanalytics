
package org.drip.sample.xccy;

import org.drip.analytics.date.JulianDate;
import org.drip.analytics.support.*;
import org.drip.param.creator.ScenarioForwardCurveBuilder;
import org.drip.param.market.CurveSurfaceQuoteSet;
import org.drip.param.valuation.*;
import org.drip.product.fx.ComponentPair;
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
 * FixFloatFixFloat demonstrates the Funding Volatility, Forward Volatility, FX Volatility, Funding/Forward
 *  Correlation, Funding/FX Correlation, and Forward/FX Correlation across the 2 currencies (USD and EUR) on
 *  the Valuation of the Cross Currency Basis Swap built out of a pair of fix-float swaps.
 * 
 * @author Lakshmi Krishnamurthy
 */

public class FixFloatFixFloatAnalysis {

	private static final FixFloatComponent MakeFixFloatSwap (
		final JulianDate dtEffective,
		final boolean bFXMTM,
		final String strPayCurrency,
		final String strCouponCurrency,
		final String strTenor,
		final int iTenorInMonths)
		throws Exception
	{
		/*
		 * The Fixed Leg
		 */

		GenericStream fixStream = new GenericStream (
			PeriodBuilder.RegularPeriodSingleReset (
				dtEffective.julian(),
				strTenor,
				bFXMTM ? Double.NaN : dtEffective.julian(),
				null,
				null,
				null,
				null,
				null,
				null,
				null,
				null,
				2,
				"Act/360",
				false,
				"Act/360",
				false,
				false,
				strCouponCurrency,
				-1.,
				null,
				0.02,
				strPayCurrency,
				strCouponCurrency,
				null,
				null
			)
		);

		/*
		 * The Derived Leg
		 */

		GenericStream floatStream = new GenericStream (
			PeriodBuilder.RegularPeriodSingleReset (
				dtEffective.julian(),
				strTenor,
				bFXMTM ? Double.NaN : dtEffective.julian(),
				null,
				null,
				null,
				null,
				null,
				null,
				null,
				null,
				12 / iTenorInMonths,
				"Act/360",
				false,
				"Act/360",
				false,
				false,
				strCouponCurrency,
				1.,
				null,
				0.,
				strPayCurrency,
				strCouponCurrency,
				ForwardLabel.Standard (strCouponCurrency + "-LIBOR-" + iTenorInMonths + "M"),
				null
			)
		);

		/*
		 * The fix-float swap instance
		 */

		FixFloatComponent fixFloat = new FixFloatComponent (
			fixStream,
			floatStream,
			new CashSettleParams (0, strCouponCurrency, 0)
		);

		return fixFloat;
	}

	private static final void SetMarketParams (
		final CurveSurfaceQuoteSet mktParams,
		final ForwardLabel forwardLabel1,
		final ForwardLabel forwardLabel2,
		final FundingLabel fundingLabel,
		final FXLabel fxLabel,
		final double dblForward1Vol,
		final double dblForward2Vol,
		final double dblFundingVol,
		final double dblFXVol,
		final double dblForward1FundingCorr,
		final double dblForward2FundingCorr,
		final double dblForward2FXCorr,
		final double dblFundingFXCorr)
		throws Exception
	{
		mktParams.setForwardCurveVolSurface (forwardLabel1, new FlatUnivariate (dblForward1Vol));

		mktParams.setForwardCurveVolSurface (forwardLabel2, new FlatUnivariate (dblForward2Vol));

		mktParams.setFundingCurveVolSurface (fundingLabel, new FlatUnivariate (dblFundingVol));

		mktParams.setFXCurveVolSurface (fxLabel, new FlatUnivariate (dblFXVol));

		mktParams.setForwardFundingCorrSurface (forwardLabel1, fundingLabel, new FlatUnivariate (dblForward1FundingCorr));

		mktParams.setForwardFundingCorrSurface (forwardLabel2, fundingLabel, new FlatUnivariate (dblForward2FundingCorr));

		mktParams.setForwardFXCorrSurface (forwardLabel2, fxLabel, new FlatUnivariate (dblForward2FXCorr));

		mktParams.setFundingFXCorrSurface (fundingLabel, fxLabel, new FlatUnivariate (dblFundingFXCorr));
	}

	private static final void VolCorrScenario (
		final ComponentPair[] aCP,
		final ValuationParams valParams,
		final CurveSurfaceQuoteSet mktParams,
		final ForwardLabel forwardLabel1,
		final ForwardLabel forwardLabel2,
		final FundingLabel fundingLabel,
		final FXLabel fxLabel,
		final double dblForward1Vol,
		final double dblForward2Vol,
		final double dblFundingVol,
		final double dblFXVol,
		final double dblForward1FundingCorr,
		final double dblForward2FundingCorr,
		final double dblForward2FXCorr,
		final double dblFundingFXCorr)
		throws Exception
	{
		SetMarketParams (
			mktParams,
			forwardLabel1,
			forwardLabel2,
			fundingLabel,
			fxLabel,
			dblForward1Vol,
			dblForward2Vol,
			dblFundingVol,
			dblFXVol,
			dblForward1FundingCorr,
			dblForward2FundingCorr,
			dblForward2FXCorr,
			dblFundingFXCorr
		);

		String strDump = "\t[" +
			FormatUtil.FormatDouble (dblForward1Vol, 2, 0, 100.) + "%," +
			FormatUtil.FormatDouble (dblForward2Vol, 2, 0, 100.) + "%," +
			FormatUtil.FormatDouble (dblFundingVol, 2, 0, 100.) + "%," +
			FormatUtil.FormatDouble (dblFXVol, 2, 0, 100.) + "%," +
			FormatUtil.FormatDouble (dblForward1FundingCorr, 2, 0, 100.) + "%," +
			FormatUtil.FormatDouble (dblForward2FundingCorr, 2, 0, 100.) + "%," +
			FormatUtil.FormatDouble (dblForward2FXCorr, 2, 0, 100.) + "%," +
			FormatUtil.FormatDouble (dblFundingFXCorr, 2, 0, 100.) + "%] = ";

		for (int i = 0; i < aCP.length; ++i) {
			CaseInsensitiveTreeMap<Double> mapOutput = aCP[i].value (valParams, null, mktParams, null);

			if (0 != i) strDump += " || ";

			strDump +=
				FormatUtil.FormatDouble (mapOutput.get ("ReferenceCumulativeConvexityAdjustmentPremium"), 2, 0, 10000.) + " | " +
				FormatUtil.FormatDouble (mapOutput.get ("DerivedCumulativeConvexityAdjustmentPremium"), 2, 0, 10000.) + " | " +
				FormatUtil.FormatDouble (mapOutput.get ("CumulativeConvexityAdjustmentPremium"), 2, 0, 10000.);
		}

		System.out.println (strDump);
	}

	public static final void main (
		final String[] astrArgs)
		throws Exception
	{
		double dblUSD3MForwardRate = 0.0275;
		double dblEUR3MForwardRate = 0.0175;
		double dblUSDFundingRate = 0.03;
		double dblUSDEURFXRate = 1. / 1.34;

		/*
		 * Initialize the Credit Analytics Library
		 */

		CreditAnalytics.Init ("");

		JulianDate dtToday = JulianDate.Today();

		ValuationParams valParams = new ValuationParams (dtToday, dtToday, "USD");

		ForwardLabel fri3MUSD = ForwardLabel.Create ("USD", "LIBOR", "3M");

		ForwardLabel fri3MEUR = ForwardLabel.Create ("EUR", "LIBOR", "3M");

		FundingLabel fundingLabel = FundingLabel.Standard ("USD");

		FXLabel fxLabel = FXLabel.Standard (CurrencyPair.FromCode ("USD/EUR"));

		FixFloatComponent fixFloatUSD = MakeFixFloatSwap (
			dtToday,
			false,
			"USD",
			"USD",
			"2Y",
			3
		);

		FixFloatComponent fixFloatEURMTM = MakeFixFloatSwap (
			dtToday,
			true,
			"USD",
			"EUR",
			"2Y",
			3
		);

		ComponentPair cpMTM = new ComponentPair (
			"FFFF_MTM",
			fixFloatUSD,
			fixFloatEURMTM
		);

		FixFloatComponent fixFloatEURNonMTM = MakeFixFloatSwap (
			dtToday,
			false,
			"USD",
			"EUR",
			"2Y",
			3
		);

		ComponentPair cpNonMTM = new ComponentPair (
			"FFFF_Non_MTM",
			fixFloatUSD,
			fixFloatEURNonMTM
		);

		CurveSurfaceQuoteSet mktParams = new CurveSurfaceQuoteSet();

		mktParams.setFixing (
			dtToday,
			fxLabel,
			dblUSDEURFXRate
		);

		mktParams.setForwardCurve (
			ScenarioForwardCurveBuilder.FlatForwardForwardCurve (
				dtToday,
				fri3MUSD,
				dblUSD3MForwardRate,
				new CollateralizationParams ("OVERNIGHT_INDEX", "USD")
			)
		);

		mktParams.setForwardCurve (
			ScenarioForwardCurveBuilder.FlatForwardForwardCurve (
				dtToday,
				fri3MEUR,
				dblEUR3MForwardRate,
				new CollateralizationParams ("OVERNIGHT_INDEX", "EUR")
			)
		);

		mktParams.setFundingCurve (
			DiscountCurveBuilder.CreateFromFlatRate (
				dtToday,
				"USD",
				new CollateralizationParams ("OVERNIGHT_INDEX", "USD"),
				dblUSDFundingRate
			)
		);

		mktParams.setFXCurve (
			fxLabel,
			new FlatUnivariate (dblUSDEURFXRate)
		);

		double[] adblUSD3MForwardVol = new double[] {0.1, 0.4};
		double[] adblEUR3MForwardVol = new double[] {0.1, 0.4};
		double[] adblUSDFundingVol = new double[] {0.1, 0.4};
		double[] adblUSDEURFXVol = new double[] {0.1, 0.4};
		double[] adblUSD3MForwardUSDFundingCorr = new double[] {-0.1, 0.2};
		double[] adblEUR3MForwardUSDFundingCorr = new double[] {-0.1, 0.2};
		double[] adblEUR3MForwardUSDEURFXCorr = new double[] {-0.1, 0.2};
		double[] adblUSDFundingUSDEURFXCorr = new double[] {-0.1, 0.2};

		for (double dblUSD3MForwardVol : adblUSD3MForwardVol) {
			for (double dblEUR3MForwardVol : adblEUR3MForwardVol) {
				for (double dblUSDFundingVol : adblUSDFundingVol) {
					for (double dblUSDEURFXVol : adblUSDEURFXVol) {
						for (double dblUSD3MForwardUSDFundingCorr : adblUSD3MForwardUSDFundingCorr) {
							for (double dblEUR3MForwardUSDFundingCorr : adblEUR3MForwardUSDFundingCorr) {
								for (double dblEUR3MForwardUSDEURFXCorr : adblEUR3MForwardUSDEURFXCorr) {
									for (double dblUSDFundingUSDEURFXCorr : adblUSDFundingUSDEURFXCorr)
										VolCorrScenario (
											new ComponentPair[] {cpMTM, cpNonMTM},
											valParams,
											mktParams,
											fri3MUSD,
											fri3MEUR,
											fundingLabel,
											fxLabel,
											dblUSD3MForwardVol,
											dblEUR3MForwardVol,
											dblUSDFundingVol,
											dblUSDEURFXVol,
											dblUSD3MForwardUSDFundingCorr,
											dblEUR3MForwardUSDFundingCorr,
											dblEUR3MForwardUSDEURFXCorr,
											dblUSDFundingUSDEURFXCorr
										);
								}
							}
						}
					}
				}
			}
		}
	}
}
