
package org.drip.sample.xccy;

import java.util.*;

import org.drip.analytics.date.JulianDate;
import org.drip.analytics.period.CouponPeriod;
import org.drip.analytics.support.CaseInsensitiveTreeMap;
import org.drip.analytics.support.PeriodHelper;
import org.drip.param.creator.ScenarioForwardCurveBuilder;
import org.drip.param.market.CurveSurfaceQuoteSet;
import org.drip.param.valuation.*;
import org.drip.product.cashflow.*;
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
		final CurrencyPair cp,
		final String strFixedCurrency,
		final boolean bFixMTMOn,
		final String strFloatCurrency,
		final String strTenor,
		final int iTenorInMonths)
		throws Exception
	{
		/*
		 * The Fixed Leg
		 */

		List<CouponPeriod> lsFixPeriods = PeriodHelper.RegularPeriodSingleReset (
			dtEffective.julian(),
			strTenor,
			null,
			2,
			"Act/360",
			false,
			false,
			strFixedCurrency,
			strFixedCurrency,
			null,
			null
		);

		FixedStream fixStream = new FixedStream (
			strFixedCurrency,
			new FXMTMSetting (cp, bFixMTMOn),
			0.02,
			-1.,
			null,
			lsFixPeriods
		);

		fixStream.setPrimaryCode (strFixedCurrency + "_" + (null == cp ? "" : cp.numCcy()) + "::FIXED::" + strTenor);

		/*
		 * The Derived Leg
		 */

		List<CouponPeriod> lsDerivedFloatPeriods = PeriodHelper.RegularPeriodSingleReset (
			dtEffective.julian(),
			strTenor,
			null,
			12 / iTenorInMonths,
			"Act/360",
			false,
			false,
			strFloatCurrency,
			strFloatCurrency,
			ForwardLabel.Standard (strFloatCurrency + "-LIBOR-" + iTenorInMonths + "M"),
			null
		);

		FloatingStream floatStream = new FloatingStream (
			strFloatCurrency,
			null,
			0.,
			1.,
			null,
			lsDerivedFloatPeriods,
			ForwardLabel.Standard (strFloatCurrency + "-LIBOR-" + iTenorInMonths + "M"),
			false
		);

		floatStream.setPrimaryCode (strFloatCurrency + "_" + strFloatCurrency + "::FIXED::" + iTenorInMonths + "M::" + strTenor);

		/*
		 * The fix-float swap instance
		 */

		FixFloatComponent fixFloat = new FixFloatComponent (fixStream, floatStream);

		fixFloat.setPrimaryCode (fixStream.primaryCode() + "__" + floatStream.primaryCode());

		return fixFloat;
	}

	private static final void SetMarketParams (
		final CurveSurfaceQuoteSet mktParams,
		final ForwardLabel fri3MUSD,
		final ForwardLabel fri3MEUR,
		final FXLabel fxLabel,
		final double dblUSDFundingVol,
		final double dblEURFundingVol,
		final double dblUSD3MForwardVol,
		final double dblEUR3MForwardVol,
		final double dblUSDEURFXVol,
		final double dblUSDFundingUSD3MForwardCorr,
		final double dblEURFundingUSDEURFXCorr,
		final double dblEURFundingEUR3MForwardCorr,
		final double dblEUR3MForwardUSDEURFXCorr)
		throws Exception
	{
		FundingLabel fundingLabelUSD = FundingLabel.Standard ("USD");

		FundingLabel fundingLabelEUR = FundingLabel.Standard ("EUR");

		mktParams.setFundingCurveVolSurface (fundingLabelUSD, new FlatUnivariate (dblUSDFundingVol));

		mktParams.setFundingCurveVolSurface (fundingLabelEUR, new FlatUnivariate (dblEURFundingVol));

		mktParams.setForwardCurveVolSurface (fri3MUSD, new FlatUnivariate (dblUSD3MForwardVol));

		mktParams.setForwardCurveVolSurface (fri3MEUR, new FlatUnivariate (dblEUR3MForwardVol));

		mktParams.setFXCurveVolSurface (fxLabel, new FlatUnivariate (dblUSDEURFXVol));

		mktParams.setForwardFundingCorrSurface (fri3MUSD, fundingLabelUSD, new FlatUnivariate (dblUSDFundingUSD3MForwardCorr));

		mktParams.setFundingFXCorrSurface (fundingLabelEUR, fxLabel, new FlatUnivariate (dblEURFundingUSDEURFXCorr));

		mktParams.setForwardFundingCorrSurface (fri3MEUR, fundingLabelEUR, new FlatUnivariate (dblEURFundingEUR3MForwardCorr));

		mktParams.setForwardFXCorrSurface (fri3MEUR, fxLabel, new FlatUnivariate (dblEUR3MForwardUSDEURFXCorr));
	}

	private static final void VolCorrScenario (
		final ComponentPair[] aCP,
		final ValuationParams valParams,
		final CurveSurfaceQuoteSet mktParams,
		final ForwardLabel fri3MUSD,
		final ForwardLabel fri3MEUR,
		final FXLabel fxLabel,
		final double dblUSDFundingVol,
		final double dblEURFundingVol,
		final double dblUSD3MForwardVol,
		final double dblEUR3MForwardVol,
		final double dblUSDEURFXVol,
		final double dblUSDFundingUSD3MForwardCorr,
		final double dblEURFundingUSDEURFXCorr,
		final double dblEURFundingEUR3MForwardCorr,
		final double dblEUR3MForwardUSDEURFXCorr)
		throws Exception
	{
		SetMarketParams (
			mktParams,
			fri3MUSD,
			fri3MEUR,
			fxLabel,
			dblUSDFundingVol,
			dblEURFundingVol,
			dblUSD3MForwardVol,
			dblEUR3MForwardVol,
			dblUSDEURFXVol,
			dblUSDFundingUSD3MForwardCorr,
			dblEURFundingUSDEURFXCorr,
			dblEURFundingEUR3MForwardCorr,
			dblEUR3MForwardUSDEURFXCorr
		);

		String strDump = "\t[" +
			FormatUtil.FormatDouble (dblUSDFundingVol, 2, 0, 100.) + "%," +
			FormatUtil.FormatDouble (dblEURFundingVol, 2, 0, 100.) + "%," +
			FormatUtil.FormatDouble (dblUSD3MForwardVol, 2, 0, 100.) + "%," +
			FormatUtil.FormatDouble (dblEUR3MForwardVol, 2, 0, 100.) + "%," +
			FormatUtil.FormatDouble (dblUSDEURFXVol, 2, 0, 100.) + "%," +
			FormatUtil.FormatDouble (dblUSDFundingUSD3MForwardCorr, 2, 0, 100.) + "%," +
			FormatUtil.FormatDouble (dblEURFundingUSDEURFXCorr, 2, 0, 100.) + "%," +
			FormatUtil.FormatDouble (dblEURFundingEUR3MForwardCorr, 2, 0, 100.) + "%," +
			FormatUtil.FormatDouble (dblEUR3MForwardUSDEURFXCorr, 2, 0, 100.) + "%] = ";

		for (int i = 0; i < aCP.length; ++i) {
			CaseInsensitiveTreeMap<Double> mapOutput = aCP[i].value (valParams, null, mktParams, null);

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
		double dblUSDFundingRate = 0.03;
		double dblEURFundingRate = 0.02;
		double dblUSD3MForwardRate = 0.0275;
		double dblEUR3MForwardRate = 0.0175;
		double dblUSDEURFXRate = 1. / 1.34;

		/*
		 * Initialize the Credit Analytics Library
		 */

		CreditAnalytics.Init ("");

		JulianDate dtToday = JulianDate.Today();

		ValuationParams valParams = new ValuationParams (dtToday, dtToday, "USD");

		ForwardLabel fri3MUSD = ForwardLabel.Create ("USD", "LIBOR", "3M");

		FixFloatComponent fixFloatUSD = MakeFixFloatSwap (
			dtToday,
			null,
			"USD",
			false,
			"USD",
			"2Y",
			3);

		CurrencyPair cp = CurrencyPair.FromCode ("USD/EUR");

		ForwardLabel fri3MEUR = ForwardLabel.Create ("EUR", "LIBOR", "3M");

		FixFloatComponent fixFloatEURMTM = MakeFixFloatSwap (
			dtToday,
			cp,
			"EUR",
			true,
			"EUR",
			"2Y",
			3);

		ComponentPair cpMTM = new ComponentPair (
			"FFFF_MTM",
			fixFloatUSD,
			fixFloatEURMTM
		);

		FixFloatComponent fixFloatEURNonMTM = MakeFixFloatSwap (
			dtToday,
			cp,
			"EUR",
			false,
			"EUR",
			"2Y",
			3);

		ComponentPair cpNonMTM = new ComponentPair (
			"FFFF_Non_MTM",
			fixFloatUSD,
			fixFloatEURNonMTM
		);

		FXLabel fxLabel = FXLabel.Standard (cp);

		CurveSurfaceQuoteSet mktParams = new CurveSurfaceQuoteSet();

		mktParams.setFundingCurve (
			DiscountCurveBuilder.CreateFromFlatRate (
				dtToday,
				"USD",
				new CollateralizationParams ("OVERNIGHT_INDEX", "USD"),
				dblUSDFundingRate
			)
		);

		mktParams.setFundingCurve (
			DiscountCurveBuilder.CreateFromFlatRate (
				dtToday,
				"EUR",
				new CollateralizationParams ("OVERNIGHT_INDEX", "EUR"),
				dblEURFundingRate
			)
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

		mktParams.setFXCurve (
			fxLabel,
			new FlatUnivariate (dblUSDEURFXRate)
		);

		double[] adblUSDFundingVol = new double[] {0.1, 0.4};
		double[] adblEURFundingVol = new double[] {0.1, 0.4};
		double[] adblUSD3MForwardVol = new double[] {0.1, 0.4};
		double[] adblEUR3MForwardVol = new double[] {0.1, 0.4};
		double[] adblUSDEURFXVol = new double[] {0.1, 0.4};
		double[] adblUSDFundingUSD3MForwardCorr = new double[] {-0.1, 0.2};
		double[] adblEURFundingEUR3MForwardCorr = new double[] {-0.1, 0.2};
		double[] adblEURFundingUSDEURFXCorr = new double[] {-0.1, 0.2};
		double[] adblEUR3MForwardUSDEURFXCorr = new double[] {-0.1, 0.2};

		for (double dblUSDFundingVol : adblUSDFundingVol) {
			for (double dblEURFundingVol : adblEURFundingVol) {
				for (double dblUSD3MForwardVol : adblUSD3MForwardVol) {
					for (double dblEUR3MForwardVol : adblEUR3MForwardVol) {
						for (double dblUSDEURFXVol : adblUSDEURFXVol) {
							for (double dblUSDFundingUSD3MForwardCorr : adblUSDFundingUSD3MForwardCorr) {
								for (double dblEURFundingEUR3MForwardCorr : adblEURFundingEUR3MForwardCorr) {
									for (double dblEURFundingUSDEURFXCorr : adblEURFundingUSDEURFXCorr) {
										for (double dblEUR3MForwardUSDEURFXCorr : adblEUR3MForwardUSDEURFXCorr)
											VolCorrScenario (
												new ComponentPair[] {cpMTM, cpNonMTM},
												valParams,
												mktParams,
												fri3MUSD,
												fri3MEUR,
												fxLabel,
												dblUSDFundingVol,
												dblEURFundingVol,
												dblUSD3MForwardVol,
												dblEUR3MForwardVol,
												dblUSDEURFXVol,
												dblUSDFundingUSD3MForwardCorr,
												dblEURFundingUSDEURFXCorr,
												dblEURFundingEUR3MForwardCorr,
												dblEUR3MForwardUSDEURFXCorr
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
}
