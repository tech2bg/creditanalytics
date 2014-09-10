
package org.drip.sample.xccy;

import org.drip.analytics.date.JulianDate;
import org.drip.analytics.rates.*;
import org.drip.analytics.support.*;
import org.drip.param.creator.ScenarioForwardCurveBuilder;
import org.drip.param.market.CurveSurfaceQuoteSet;
import org.drip.param.valuation.*;
import org.drip.product.cashflow.FloatingStream;
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
 * FloatFloatMTMVolAnalysis demonstrates the impact of Funding Volatility, Forward Volatility, and
 *  Funding/Forward, Funding/FX, and Forward/FX Correlation for each of the FRI's on the Valuation of a
 *  float-float swap with a 3M EUR Floater leg that pays in USD, and a 6M EUR Floater leg that pays in USD.
 *  Comparison is done across MTM and non-MTM fixed Leg Counterparts.
 * 
 * @author Lakshmi Krishnamurthy
 */

public class CrossFloatCrossFloatAnalysis {

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

		FloatingStream floatStreamReference = new FloatingStream (
			PeriodBuilder.RegularPeriodSingleReset (
					dtEffective.julian(),
					strMaturityTenor,
					bFixMTMOn ? Double.NaN : dtEffective.julian(),
					null,
					12 / iTenorInMonthsReference,
					"Act/360",
					false,
					false,
					strCurrency,
					-1.,
					null,
					0.,
					strCurrency,
					ForwardLabel.Standard (strCurrency + "-LIBOR-" + iTenorInMonthsReference + "M"),
					null
				)
		);

		floatStreamReference.setPrimaryCode (strCurrency + "::FLOAT::" + iTenorInMonthsReference + "M::" + strMaturityTenor);

		/*
		 * The Derived Leg
		 */

		FloatingStream floatStreamDerived = new FloatingStream (
			PeriodBuilder.RegularPeriodSingleReset (
				dtEffective.julian(),
				strMaturityTenor,
				bFixMTMOn ? Double.NaN : dtEffective.julian(),
				null,
				12 / iTenorInMonthsDerived,
				"Act/360",
				false,
				false,
				strCurrency,
				1.,
				null,
				0.,
				strCurrency,
				ForwardLabel.Standard (strCurrency + "-LIBOR-" + iTenorInMonthsDerived + "M"),
				null
			)
		);

		floatStreamDerived.setPrimaryCode (strCurrency + "::FLOAT::" + iTenorInMonthsDerived + "M::" + strMaturityTenor);

		/*
		 * The float-float swap instance
		 */

		return new FloatFloatComponent (
			floatStreamReference,
			floatStreamDerived,
			new CashSettleParams (0, strCurrency, 0));
	}

	private static final void SetMarketParams (
		final CurveSurfaceQuoteSet mktParams,
		final ForwardLabel friEUR3M,
		final ForwardLabel friEUR6M,
		final FXLabel fxLabel,
		final String strCurrency,
		final double dblEURFundingVol,
		final double dblEURForward3MVol,
		final double dblEURForward6MVol,
		final double dblUSDEURFXVol,
		final double dblEUR3MUSDEURFXCorr,
		final double dblEUR6MUSDEURFXCorr,
		final double dblEURFundingEUR3MCorr,
		final double dblEURFundingEUR6MCorr,
		final double dblEURFundingUSDEURFXCorr)
		throws Exception
	{
		FundingLabel fundingLabel = FundingLabel.Standard (strCurrency);

		mktParams.setForwardCurveVolSurface (friEUR3M, new FlatUnivariate (dblEURForward3MVol));

		mktParams.setForwardCurveVolSurface (friEUR6M, new FlatUnivariate (dblEURForward6MVol));

		mktParams.setFundingCurveVolSurface (fundingLabel, new FlatUnivariate (dblEURFundingVol));

		mktParams.setFXCurveVolSurface (fxLabel, new FlatUnivariate (dblUSDEURFXVol));

		mktParams.setForwardFundingCorrSurface (friEUR3M, fundingLabel, new FlatUnivariate (dblEURFundingEUR3MCorr));

		mktParams.setForwardFundingCorrSurface (friEUR6M, fundingLabel, new FlatUnivariate (dblEURFundingEUR6MCorr));

		mktParams.setForwardFXCorrSurface (friEUR3M, fxLabel, new FlatUnivariate (dblEUR3MUSDEURFXCorr));

		mktParams.setForwardFXCorrSurface (friEUR6M, fxLabel, new FlatUnivariate (dblEUR6MUSDEURFXCorr));

		mktParams.setFundingFXCorrSurface (fundingLabel, fxLabel, new FlatUnivariate (dblEURFundingUSDEURFXCorr));
	}

	private static final void VolCorrScenario (
		final FloatFloatComponent[] aFloatFloat,
		final ForwardLabel friEUR3M,
		final ForwardLabel friEUR6M,
		final FXLabel fxLabel,
		final String strCurrency,
		final ValuationParams valParams,
		final CurveSurfaceQuoteSet mktParams,
		final double dblEURFundingVol,
		final double dblEURForward3MVol,
		final double dblEURForward6MVol,
		final double dblUSDEURFXVol,
		final double dblEUR3MUSDEURFXCorr,
		final double dblEUR6MUSDEURFXCorr,
		final double dblEURFundingEUR3MCorr,
		final double dblEURFundingEUR6MCorr,
		final double dblEURFundingUSDEURFXCorr)
		throws Exception
	{
		SetMarketParams (
			mktParams,
			friEUR3M,
			friEUR6M,
			fxLabel,
			strCurrency,
			dblEURFundingVol,
			dblEURForward3MVol,
			dblEURForward6MVol,
			dblUSDEURFXVol,
			dblEUR3MUSDEURFXCorr,
			dblEUR6MUSDEURFXCorr,
			dblEURFundingEUR3MCorr,
			dblEURFundingEUR6MCorr,
			dblEURFundingUSDEURFXCorr
		);

		String strDump = "\t[" +
			FormatUtil.FormatDouble (dblEURFundingVol, 2, 0, 100.) + "%," +
			FormatUtil.FormatDouble (dblEURForward3MVol, 2, 0, 100.) + "%," +
			FormatUtil.FormatDouble (dblEURForward6MVol, 2, 0, 100.) + "%," +
			FormatUtil.FormatDouble (dblUSDEURFXVol, 2, 0, 100.) + "%," +
			FormatUtil.FormatDouble (dblEUR3MUSDEURFXCorr, 2, 0, 100.) + "%," +
			FormatUtil.FormatDouble (dblEUR6MUSDEURFXCorr, 2, 0, 100.) + "%," +
			FormatUtil.FormatDouble (dblEURFundingEUR3MCorr, 2, 0, 100.) + "%," +
			FormatUtil.FormatDouble (dblEURFundingEUR6MCorr, 2, 0, 100.) + "%," +
			FormatUtil.FormatDouble (dblEURFundingUSDEURFXCorr, 2, 0, 100.) + "%] = ";

		for (int i = 0; i < aFloatFloat.length; ++i) {
			CaseInsensitiveTreeMap<Double> mapOutput = aFloatFloat[i].value (valParams, null, mktParams, null);

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
		double dblEURFundingRate = 0.02;
		double dblEUR3MForwardRate = 0.02;
		double dblEUR6MForwardRate = 0.025;
		double dblUSDEURFXRate = 1. / 1.35;

		/*
		 * Initialize the Credit Analytics Library
		 */

		CreditAnalytics.Init ("");

		JulianDate dtToday = JulianDate.Today();

		ValuationParams valParams = new ValuationParams (dtToday, dtToday, "EUR");

		DiscountCurve dcEURFunding = DiscountCurveBuilder.CreateFromFlatRate (
			dtToday,
			"EUR",
			new CollateralizationParams ("OVERNIGHT_INDEX", "EUR"),
			dblEURFundingRate
		);

		ForwardLabel friEUR3M = ForwardLabel.Create ("EUR", "LIBOR", "3M");

		ForwardCurve fcEUR3M = ScenarioForwardCurveBuilder.FlatForwardForwardCurve (
			dtToday,
			friEUR3M,
			dblEUR3MForwardRate,
			new CollateralizationParams (
				"OVERNIGHT_INDEX",
				"EUR"
			)
		);

		ForwardLabel friEUR6M = ForwardLabel.Create ("EUR", "LIBOR", "6M");

		ForwardCurve fcEUR6M = ScenarioForwardCurveBuilder.FlatForwardForwardCurve (
			dtToday,
			friEUR6M,
			dblEUR6MForwardRate,
			new CollateralizationParams (
				"OVERNIGHT_INDEX",
				"EUR"
			)
		);

		CurrencyPair cp = CurrencyPair.FromCode ("USD/EUR");

		FloatFloatComponent floatFloatMTM = MakeFloatFloatSwap (
			dtToday,
			cp,
			true,
			"EUR",
			"2Y",
			6,
			3);

		floatFloatMTM.setPrimaryCode ("EUR__USD__MTM::FLOAT::3M::6M::2Y");

		FloatFloatComponent floatFloatNonMTM = MakeFloatFloatSwap (
			dtToday,
			cp,
			false,
			"EUR",
			"2Y",
			6,
			3);

		floatFloatNonMTM.setPrimaryCode ("EUR__USD__NONMTM::FLOAT::3M::6M::2Y");

		FXLabel fxLabel = FXLabel.Standard (cp);

		CurveSurfaceQuoteSet mktParams = new CurveSurfaceQuoteSet();

		mktParams.setForwardCurve (fcEUR3M);

		mktParams.setForwardCurve (fcEUR6M);

		mktParams.setFundingCurve (dcEURFunding);

		mktParams.setFXCurve (fxLabel, new FlatUnivariate (dblUSDEURFXRate));

		double[] adblEURFundingVol = new double[] {0.1, 0.3, 0.5};

		double[] adblEURForward3MVol = new double[] {0.1, 0.3, 0.5};

		double[] adblEURForward6MVol = new double[] {0.1, 0.3, 0.5};

		double[] adblUSDEURFXVol = new double[] {0.1, 0.3, 0.5};

		double[] adblEUR3MUSDEURFXCorr = new double[] {-0.2, 0.25};

		double[] adblEUR6MUSDEURFXCorr = new double[] {-0.2, 0.25};

		double[] adblEURFundingEUR3MCorr = new double[] {-0.2, 0.25};

		double[] adblEURFundingEUR6MCorr = new double[] {-0.2, 0.25};

		double[] adblEURFundingUSDEURFXCorr = new double[] {-0.2, 0.25};

		for (double dblEURFundingVol : adblEURFundingVol) {
			for (double dblEURForward3MVol : adblEURForward3MVol) {
				for (double dblEURForward6MVol : adblEURForward6MVol) {
					for (double dblUSDEURFXVol : adblUSDEURFXVol) {
						for (double dblEUR3MUSDEURFXCorr : adblEUR3MUSDEURFXCorr) {
							for (double dblEUR6MUSDEURFXCorr : adblEUR6MUSDEURFXCorr) {
								for (double dblEURFundingEUR3MCorr : adblEURFundingEUR3MCorr) {
									for (double dblEURFundingEUR6MCorr : adblEURFundingEUR6MCorr) {
										for (double dblEURFundingUSDEURFXCorr : adblEURFundingUSDEURFXCorr)
											VolCorrScenario (
												new FloatFloatComponent[] {floatFloatMTM, floatFloatNonMTM},
												friEUR3M,
												friEUR6M,
												fxLabel,
												"EUR",
												valParams,
												mktParams,
												dblEURFundingVol,
												dblEURForward3MVol,
												dblEURForward6MVol,
												dblUSDEURFXVol,
												dblEUR3MUSDEURFXCorr,
												dblEUR6MUSDEURFXCorr,
												dblEURFundingEUR3MCorr,
												dblEURFundingEUR6MCorr,
												dblEURFundingUSDEURFXCorr
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
