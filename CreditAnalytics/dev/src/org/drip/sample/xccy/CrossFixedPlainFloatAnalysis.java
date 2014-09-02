
package org.drip.sample.xccy;

import java.util.List;

import org.drip.analytics.date.JulianDate;
import org.drip.analytics.period.CouponPeriod;
import org.drip.analytics.rates.*;
import org.drip.analytics.support.CaseInsensitiveTreeMap;
import org.drip.analytics.support.PeriodHelper;
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

		fixStream.setPrimaryCode (strFixedCurrency + "_" + cp.numCcy() + "::FIXED::" + strTenor);

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
		final FXLabel fxLabel,
		final String strFixedCurrency,
		final String strFloatCurrency,
		final double dblUSDFundingVol,
		final double dblUSD3MVol,
		final double dblUSD3MUSDFundingCorr,
		final double dblEURFundingVol,
		final double dblUSDEURFXVol,
		final double dblEURFundingUSDEURFXCorr)
		throws Exception
	{
		FundingLabel fundingLabelFixed = org.drip.state.identifier.FundingLabel.Standard (strFixedCurrency);

		FundingLabel fundingLabelFloat = org.drip.state.identifier.FundingLabel.Standard (strFloatCurrency);

		mktParams.setFundingCurveVolSurface (fundingLabelFloat, new FlatUnivariate (dblUSDFundingVol));

		mktParams.setForwardCurveVolSurface (fri3MUSD, new FlatUnivariate (dblUSD3MVol));

		mktParams.setForwardFundingCorrSurface (fri3MUSD, fundingLabelFloat, new FlatUnivariate (dblUSD3MUSDFundingCorr));

		mktParams.setFundingCurveVolSurface (fundingLabelFixed, new FlatUnivariate (dblEURFundingVol));

		mktParams.setFXCurveVolSurface (fxLabel, new FlatUnivariate (dblUSDEURFXVol));

		mktParams.setFundingFXCorrSurface (fundingLabelFixed, fxLabel, new FlatUnivariate (dblEURFundingUSDEURFXCorr));
	}

	private static final void VolCorrScenario (
		final FixFloatComponent[] aFixFloat,
		final ForwardLabel fri3MUSD,
		final FXLabel fxLabel,
		final String strFixedCurrency,
		final String strFloatCurrency,
		final ValuationParams valParams,
		final CurveSurfaceQuoteSet mktParams,
		final double dblUSDFundingVol,
		final double dblUSD3MVol,
		final double dblUSD3MUSDFundingCorr,
		final double dblEURFundingVol,
		final double dblUSDEURFXVol,
		final double dblEURFundingUSDEURFXCorr)
		throws Exception
	{
		SetMarketParams (
			mktParams,
			fri3MUSD,
			fxLabel,
			strFixedCurrency,
			strFloatCurrency,
			dblUSDFundingVol,
			dblUSD3MVol,
			dblUSD3MUSDFundingCorr,
			dblEURFundingVol,
			dblUSDEURFXVol,
			dblEURFundingUSDEURFXCorr
		);

		String strDump = "\t[" +
			FormatUtil.FormatDouble (dblUSDFundingVol, 2, 0, 100.) + "%," +
			FormatUtil.FormatDouble (dblUSD3MVol, 2, 0, 100.) + "%," +
			FormatUtil.FormatDouble (dblUSD3MUSDFundingCorr, 2, 0, 100.) + "%," +
			FormatUtil.FormatDouble (dblEURFundingVol, 2, 0, 100.) + "%," +
			FormatUtil.FormatDouble (dblUSDEURFXVol, 2, 0, 100.) + "%," +
			FormatUtil.FormatDouble (dblEURFundingUSDEURFXCorr, 2, 0, 100.) + "%] = ";

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
			cp,
			"EUR",
			true,
			"USD",
			"2Y",
			3
		);

		FixFloatComponent fixNonMTMFloat = MakeFixFloatSwap (
			dtToday,
			cp,
			"EUR",
			false,
			"USD",
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

		double[] adblUSDFundingVol = new double[] {0.1, 0.35, 0.60};

		double[] adblUSD3MVol = new double[] {0.1, 0.35, 0.60};

		double[] adblUSD3MUSDFundingCorr = new double[] {-0.1, 0.35};

		double[] adblEURFundingVol = new double[] {0.1, 0.35, 0.60};

		double[] adblUSDEURFXVol = new double[] {0.1, 0.35, 0.60};

		double[] adblEURFundingUSDEURFXCorr = new double[] {-0.1, 0.35};

		for (double dblUSDFundingVol : adblUSDFundingVol) {
			for (double dblUSD3MVol : adblUSD3MVol) {
				for (double dblUSD3MUSDFundingCorr : adblUSD3MUSDFundingCorr) {
					for (double dblEURFundingVol : adblEURFundingVol) {
						for (double dblUSDEURFXVol : adblUSDEURFXVol) {
							for (double dblEURFundingUSDEURFXCorr : adblEURFundingUSDEURFXCorr)
								VolCorrScenario (
									new FixFloatComponent[] {fixMTMFloat, fixNonMTMFloat},
									fri3M,
									fxLabel,
									"EUR",
									"USD",
									valParams,
									mktParams,
									dblUSDFundingVol,
									dblUSD3MVol,
									dblUSD3MUSDFundingCorr,
									dblEURFundingVol,
									dblUSDEURFXVol,
									dblEURFundingUSDEURFXCorr
								);
						}
					}
				}
			}
		}
	}
}
