
package org.drip.sample.xccy;

import java.util.List;

import org.drip.analytics.date.JulianDate;
import org.drip.analytics.period.CashflowPeriod;
import org.drip.analytics.support.CaseInsensitiveTreeMap;
import org.drip.analytics.support.PeriodBuilder;
import org.drip.param.creator.ScenarioForwardCurveBuilder;
import org.drip.param.market.CurveSurfaceQuoteSet;
import org.drip.param.valuation.*;
import org.drip.product.cashflow.FloatingStream;
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
 * FloatFloatFloatFloatAnalysis demonstrates the Funding Volatility, Forward Volatility, FX Volatility,
 *  Funding/Forward Correlation, Funding/FX Correlation, and Forward/FX Correlation of the Cross Currency
 *  Basis Swap built out of a pair of float-float swaps.
 * 
 * @author Lakshmi Krishnamurthy
 */

public class FloatFloatFloatFloatAnalysis {

	private static final FloatFloatComponent MakeFloatFloatSwap (
		final JulianDate dtEffective,
		final CurrencyPair cp,
		final boolean bFixMTMOn,
		final String strCurrency,
		final String strMaturityTenor,
		final int iTenorInMonthsReference,
		final int iTenorInMonthsDerived)
		throws Exception
	{
			/*
			 * The Reference Leg
			 */

		List<CashflowPeriod> lsReferenceFloatPeriods = PeriodBuilder.GeneratePeriodsRegular (
			dtEffective.julian(),
			strMaturityTenor,
			null,
			12 / iTenorInMonthsReference,
			"Act/360",
			false,
			false,
			strCurrency,
			strCurrency
		);

		FloatingStream floatStreamReference = new FloatingStream (
			strCurrency,
			null == cp ? null : new FXMTMSetting (cp, bFixMTMOn),
			0.,
			-1.,
			null,
			lsReferenceFloatPeriods,
			ForwardLabel.Create (strCurrency + "-LIBOR-" + iTenorInMonthsReference + "M"),
			false
		);

		floatStreamReference.setPrimaryCode (strCurrency + "::FLOAT::" + iTenorInMonthsReference + "M::" + strMaturityTenor);

		/*
		 * The Derived Leg
		 */

		List<CashflowPeriod> lsDerivedFloatPeriods = PeriodBuilder.GeneratePeriodsRegular (
			dtEffective.julian(),
			strMaturityTenor,
			null,
			12 / iTenorInMonthsDerived,
			"Act/360",
			false,
			false,
			strCurrency,
			strCurrency
		);

		FloatingStream floatStreamDerived = new FloatingStream (
			strCurrency,
			new FXMTMSetting (cp, bFixMTMOn),
			0.,
			1.,
			null,
			lsDerivedFloatPeriods,
			ForwardLabel.Create (strCurrency + "-LIBOR-" + iTenorInMonthsDerived + "M"),
			false
		);

		floatStreamDerived.setPrimaryCode (strCurrency + "::FLOAT::" + iTenorInMonthsDerived + "M::" + strMaturityTenor);

		/*
		 * The float-float swap instance
		 */

		return new FloatFloatComponent (floatStreamReference, floatStreamDerived);
	}

	private static final void SetMarketParams (
		final CurveSurfaceQuoteSet mktParams,
		final ForwardLabel fri3MUSD,
		final ForwardLabel fri6MUSD,
		final ForwardLabel fri3MEUR,
		final ForwardLabel fri6MEUR,
		final FXLabel fxLabel,
		final double dblUSDFundingVol,
		final double dblUSD3MForwardVol,
		final double dblUSD6MForwardVol,
		final double dblEURFundingVol,
		final double dblEUR3MForwardVol,
		final double dblEUR6MForwardVol,
		final double dblEURUSDFXVol,
		final double dblUSDFundingUSD3MForwardCorr,
		final double dblUSDFundingUSD6MForwardCorr,
		final double dblEURFundingEUR3MForwardCorr,
		final double dblEURFundingEUR6MForwardCorr,
		final double dblUSD3MForwardEURUSDFXCorr,
		final double dblUSD6MForwardEURUSDFXCorr,
		final double dblUSDFundingUSDEURFXCorr)
		throws Exception
	{
		FundingLabel fundingLabelUSD = FundingLabel.Standard ("USD");

		FundingLabel fundingLabelEUR = FundingLabel.Standard ("EUR");

		mktParams.setFundingCurveVolSurface (fundingLabelUSD, new FlatUnivariate (dblUSDFundingVol));

		mktParams.setForwardCurveVolSurface (fri3MUSD, new FlatUnivariate (dblUSD3MForwardVol));

		mktParams.setForwardCurveVolSurface (fri6MUSD, new FlatUnivariate (dblUSD6MForwardVol));

		mktParams.setFundingCurveVolSurface (fundingLabelEUR, new FlatUnivariate (dblEURFundingVol));

		mktParams.setForwardCurveVolSurface (fri3MEUR, new FlatUnivariate (dblEUR3MForwardVol));

		mktParams.setForwardCurveVolSurface (fri6MEUR, new FlatUnivariate (dblEUR6MForwardVol));

		mktParams.setFXCurveVolSurface (fxLabel, new FlatUnivariate (dblEURUSDFXVol));

		mktParams.setForwardFundingCorrSurface (fri3MUSD, fundingLabelUSD, new FlatUnivariate (dblUSDFundingUSD3MForwardCorr));

		mktParams.setForwardFundingCorrSurface (fri6MUSD, fundingLabelUSD, new FlatUnivariate (dblUSDFundingUSD6MForwardCorr));

		mktParams.setForwardFundingCorrSurface (fri3MEUR, fundingLabelEUR, new FlatUnivariate (dblEURFundingEUR3MForwardCorr));

		mktParams.setForwardFundingCorrSurface (fri6MEUR, fundingLabelEUR, new FlatUnivariate (dblEURFundingEUR6MForwardCorr));

		mktParams.setForwardFXCorrSurface (fri3MUSD, fxLabel, new FlatUnivariate (dblUSD3MForwardEURUSDFXCorr));

		mktParams.setForwardFXCorrSurface (fri6MUSD, fxLabel, new FlatUnivariate (dblUSD6MForwardEURUSDFXCorr));

		mktParams.setFundingFXCorrSurface (fundingLabelUSD, fxLabel, new FlatUnivariate (dblUSDFundingUSDEURFXCorr));
	}

	private static final void VolCorrScenario (
		final ComponentPair[] aCP,
		final ValuationParams valParams,
		final CurveSurfaceQuoteSet mktParams,
		final ForwardLabel fri3MUSD,
		final ForwardLabel fri6MUSD,
		final ForwardLabel fri3MEUR,
		final ForwardLabel fri6MEUR,
		final FXLabel fxLabel,
		final double dblUSDFundingVol,
		final double dblUSD3MForwardVol,
		final double dblUSD6MForwardVol,
		final double dblEURFundingVol,
		final double dblEUR3MForwardVol,
		final double dblEUR6MForwardVol,
		final double dblEURUSDFXVol,
		final double dblUSDFundingUSD3MForwardCorr,
		final double dblUSDFundingUSD6MForwardCorr,
		final double dblEURFundingEUR3MForwardCorr,
		final double dblEURFundingEUR6MForwardCorr,
		final double dblUSD3MForwardEURUSDFXCorr,
		final double dblUSD6MForwardEURUSDFXCorr,
		final double dblUSDFundingUSDEURFXCorr)
		throws Exception
	{
		SetMarketParams (
			mktParams,
			fri3MUSD,
			fri6MUSD,
			fri3MEUR,
			fri6MEUR,
			fxLabel,
			dblUSDFundingVol,
			dblUSD3MForwardVol,
			dblUSD6MForwardVol,
			dblEURFundingVol,
			dblEUR3MForwardVol,
			dblEUR6MForwardVol,
			dblEURUSDFXVol,
			dblUSDFundingUSD3MForwardCorr,
			dblUSDFundingUSD6MForwardCorr,
			dblEURFundingEUR3MForwardCorr,
			dblEURFundingEUR6MForwardCorr,
			dblUSD3MForwardEURUSDFXCorr,
			dblUSD6MForwardEURUSDFXCorr,
			dblUSDFundingUSDEURFXCorr
		);

		String strDump = "\t[" +
			FormatUtil.FormatDouble (dblUSDFundingVol, 2, 0, 100.) + "%," +
			FormatUtil.FormatDouble (dblUSD3MForwardVol, 2, 0, 100.) + "%," +
			FormatUtil.FormatDouble (dblUSD6MForwardVol, 2, 0, 100.) + "%," +
			FormatUtil.FormatDouble (dblEURFundingVol, 2, 0, 100.) + "%," +
			FormatUtil.FormatDouble (dblEUR3MForwardVol, 2, 0, 100.) + "%," +
			FormatUtil.FormatDouble (dblEUR6MForwardVol, 2, 0, 100.) + "%," +
			FormatUtil.FormatDouble (dblEURUSDFXVol, 2, 0, 100.) + "%," +
			FormatUtil.FormatDouble (dblUSDFundingUSD3MForwardCorr, 2, 0, 100.) + "%," +
			FormatUtil.FormatDouble (dblUSDFundingUSD6MForwardCorr, 2, 0, 100.) + "%," +
			FormatUtil.FormatDouble (dblEURFundingEUR3MForwardCorr, 2, 0, 100.) + "%," +
			FormatUtil.FormatDouble (dblEURFundingEUR6MForwardCorr, 2, 0, 100.) + "%," +
			FormatUtil.FormatDouble (dblUSD3MForwardEURUSDFXCorr, 2, 0, 100.) + "%," +
			FormatUtil.FormatDouble (dblUSD6MForwardEURUSDFXCorr, 2, 0, 100.) + "%," +
			FormatUtil.FormatDouble (dblUSDFundingUSDEURFXCorr, 2, 0, 100.) + "%] = ";

		for (int i = 0; i < aCP.length; ++i) {
			CaseInsensitiveTreeMap<Double> mapOutput = aCP[i].value (valParams, null, mktParams, null);

			if (0 != i) strDump += " || ";

			strDump +=
				FormatUtil.FormatDouble (mapOutput.get ("ReferenceQuantoAdjustmentPremium"), 2, 0, 10000.) + " | " +
				FormatUtil.FormatDouble (mapOutput.get ("DerivedQuantoAdjustmentPremium"), 2, 0, 10000.) + " | " +
				FormatUtil.FormatDouble (mapOutput.get ("QuantoAdjustmentPremium"), 2, 0, 10000.);
		}

		System.out.println (strDump);
	}

	public static final void main (
		final String[] astrArgs)
		throws Exception
	{
		double dblUSDFundingRate = 0.03;
		double dblUSD3MForwardRate = 0.0275;
		double dblUSD6MForwardRate = 0.0325;
		double dblEURFundingRate = 0.02;
		double dblEUR3MForwardRate = 0.0175;
		double dblEUR6MForwardRate = 0.0225;
		double dblEURUSDFXRate = 1. / 1.34;

		/*
		 * Initialize the Credit Analytics Library
		 */

		CreditAnalytics.Init ("");

		JulianDate dtToday = JulianDate.Today();

		ValuationParams valParams = new ValuationParams (dtToday, dtToday, "USD");

		CurrencyPair cp = CurrencyPair.FromCode ("EUR/USD");

		ForwardLabel fri3MUSD = ForwardLabel.Create ("USD", "LIBOR", "3M");

		ForwardLabel fri6MUSD = ForwardLabel.Create ("USD", "LIBOR", "6M");

		ForwardLabel fri3MEUR = ForwardLabel.Create ("EUR", "LIBOR", "3M");

		ForwardLabel fri6MEUR = ForwardLabel.Create ("EUR", "LIBOR", "6M");

		FloatFloatComponent floatFloatDerivedEUR = MakeFloatFloatSwap (
			dtToday,
			null,
			false,
			"EUR",
			"2Y",
			6,
			3);

		floatFloatDerivedEUR.setPrimaryCode ("EUR::FLOAT::3M::6M::2Y");

		FloatFloatComponent floatFloatReferenceUSDMTM = MakeFloatFloatSwap (
			dtToday,
			cp,
			true,
			"USD",
			"2Y",
			6,
			3);

		floatFloatReferenceUSDMTM.setPrimaryCode ("USD__EUR::FLOAT::3M::6M::2Y");

		ComponentPair cpMTM = new ComponentPair (
			"FFFF_MTM",
			floatFloatReferenceUSDMTM,
			floatFloatDerivedEUR
		);

		FloatFloatComponent floatFloatReferenceUSDNonMTM = MakeFloatFloatSwap (
			dtToday,
			cp,
			false,
			"USD",
			"2Y",
			6,
			3);

		floatFloatReferenceUSDNonMTM.setPrimaryCode ("USD__EUR::FLOAT::3M::6M::2Y");

		ComponentPair cpNonMTM = new ComponentPair (
			"FFFF_NonMTM",
			floatFloatReferenceUSDNonMTM,
			floatFloatDerivedEUR
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
				fri6MUSD,
				dblUSD6MForwardRate,
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

		mktParams.setForwardCurve (
			ScenarioForwardCurveBuilder.FlatForwardForwardCurve (
				dtToday,
				fri6MEUR,
				dblEUR6MForwardRate,
				new CollateralizationParams ("OVERNIGHT_INDEX", "EUR")
			)
		);

		mktParams.setFXCurve (
			fxLabel,
			new FlatUnivariate (dblEURUSDFXRate)
		);

		double[] adblUSDFundingVol = new double[] {0.1, 0.4};
		double[] adblUSD3MForwardVol = new double[] {0.1, 0.4};
		double[] adblUSD6MForwardVol = new double[] {0.1, 0.4};
		double[] adblEURFundingVol = new double[] {0.1, 0.4};
		double[] adblEUR3MForwardVol = new double[] {0.1, 0.4};
		double[] adblEUR6MForwardVol = new double[] {0.1, 0.4};
		double[] adblEURUSDFXVol = new double[] {0.1, 0.4};

		double[] adblUSDFundingUSD3MForwardCorr = new double[] {-0.1, 0.2};
		double[] adblUSDFundingUSD6MForwardCorr = new double[] {-0.1, 0.2};
		double[] adblEURFundingEUR3MForwardCorr = new double[] {-0.1, 0.2};
		double[] adblEURFundingEUR6MForwardCorr = new double[] {-0.1, 0.2};
		double[] adblUSD3MForwardEURUSDFXCorr = new double[] {-0.1, 0.2};
		double[] adblUSD6MForwardEURUSDFXCorr = new double[] {-0.1, 0.2};
		double[] adblUSDFundingUSDEURFXCorr = new double[] {-0.1, 0.2};

		for (double dblUSDFundingVol : adblUSDFundingVol) {
			for (double dblUSD3MForwardVol : adblUSD3MForwardVol) {
				for (double dblUSD6MForwardVol : adblUSD6MForwardVol) {
					for (double dblEURFundingVol : adblEURFundingVol) {
						for (double dblEUR3MForwardVol : adblEUR3MForwardVol) {
							for (double dblEUR6MForwardVol : adblEUR6MForwardVol) {
								for (double dblEURUSDFXVol : adblEURUSDFXVol) {
									for (double dblUSDFundingUSD3MForwardCorr : adblUSDFundingUSD3MForwardCorr) {
										for (double dblUSDFundingUSD6MForwardCorr : adblUSDFundingUSD6MForwardCorr) {
											for (double dblEURFundingEUR3MForwardCorr : adblEURFundingEUR3MForwardCorr) {
												for (double dblEURFundingEUR6MForwardCorr : adblEURFundingEUR6MForwardCorr) {
													for (double dblUSD3MForwardEURUSDFXCorr : adblUSD3MForwardEURUSDFXCorr) {
														for (double dblUSD6MForwardEURUSDFXCorr : adblUSD6MForwardEURUSDFXCorr) {
															for (double dblUSDFundingUSDEURFXCorr : adblUSDFundingUSDEURFXCorr)
																VolCorrScenario (
																	new ComponentPair[] {cpMTM, cpNonMTM},
																	valParams,
																	mktParams,
																	fri3MUSD,
																	fri6MUSD,
																	fri3MEUR,
																	fri6MEUR,
																	fxLabel,
																	dblUSDFundingVol,
																	dblUSD3MForwardVol,
																	dblUSD6MForwardVol,
																	dblEURFundingVol,
																	dblEUR3MForwardVol,
																	dblEUR6MForwardVol,
																	dblEURUSDFXVol,
																	dblUSDFundingUSD3MForwardCorr,
																	dblUSDFundingUSD6MForwardCorr,
																	dblEURFundingEUR3MForwardCorr,
																	dblEURFundingEUR6MForwardCorr,
																	dblUSD3MForwardEURUSDFXCorr,
																	dblUSD6MForwardEURUSDFXCorr,
																	dblUSDFundingUSDEURFXCorr);
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
				}
			}
		}
	}
}
