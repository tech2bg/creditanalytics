
package org.drip.sample.mtm;

import java.util.List;

import org.drip.analytics.date.JulianDate;
import org.drip.analytics.daycount.*;
import org.drip.analytics.period.CashflowPeriod;
import org.drip.analytics.rates.*;
import org.drip.analytics.support.CaseInsensitiveTreeMap;
import org.drip.param.creator.ScenarioForwardCurveBuilder;
import org.drip.param.market.CurveSurfaceQuoteSet;
import org.drip.param.valuation.*;
import org.drip.product.params.FloatingRateIndex;
import org.drip.product.rates.*;
import org.drip.quant.common.FormatUtil;
import org.drip.quant.function1D.FlatUnivariate;
import org.drip.service.api.CreditAnalytics;
import org.drip.state.creator.DiscountCurveBuilder;

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
 * FixFloatMTMVolAnalysis demonstrates the impact of Funding Volatility, Forward Volatility, and
 *  Funding/Forward Correlation on the Valuation of a fix-float swap.
 * 
 * @author Lakshmi Krishnamurthy
 */

public class FixFloatMTMVolAnalysis {

	private static final FixFloatComponent MakeFixFloatSwap (
		final JulianDate dtEffective,
		final String strCurrency,
		final String strTenor,
		final int iTenorInMonths)
		throws Exception
	{
		DateAdjustParams dap = new DateAdjustParams (Convention.DR_FOLL, strCurrency);

			/*
			 * The Fixed Leg
			 */

		List<CashflowPeriod> lsFixPeriods = CashflowPeriod.GeneratePeriodsRegular (
			dtEffective.julian(),
			strTenor,
			dap,
			2,
			"Act/360",
			false,
			false,
			strCurrency,
			strCurrency
		);

		FixedStream fixStream = new FixedStream (
			strCurrency,
			null,
			0.,
			-1.,
			null,
			lsFixPeriods
		);

		fixStream.setPrimaryCode ("USD::FIXED::" + strTenor);

		/*
		 * The Derived Leg
		 */

		List<CashflowPeriod> lsDerivedFloatPeriods = CashflowPeriod.GeneratePeriodsRegular (
			dtEffective.julian(),
			strTenor,
			dap,
			12 / iTenorInMonths,
			"Act/360",
			false,
			false,
			strCurrency,
			strCurrency
		);

		FloatingStream floatStream = new FloatingStream (
			strCurrency,
			null,
			0.,
			1.,
			null,
			lsDerivedFloatPeriods,
			FloatingRateIndex.Create (strCurrency + "-LIBOR-" + iTenorInMonths + "M"),
			false
		);

		floatStream.setPrimaryCode ("USD::" + iTenorInMonths + "M::" + strTenor);

		/*
		 * The fix-float swap instance
		 */

		return new FixFloatComponent (fixStream, floatStream);
	}

	private static final void SetMarketParams (
		final CurveSurfaceQuoteSet mktParams,
		final FloatingRateIndex fri,
		final String strCurrency,
		final double dblFundingVol,
		final double dblForwardVol,
		final double dblFundingForwardCorr)
		throws Exception
	{
		mktParams.setFundingCurveVolSurface (strCurrency, new FlatUnivariate (dblFundingVol));

		mktParams.setForwardCurveVolSurface (fri, new FlatUnivariate (dblForwardVol));

		mktParams.setForwardFundingCorrSurface (fri, strCurrency, new FlatUnivariate (dblFundingForwardCorr));
	}

	private static final void VolCorrScenario (
		final FixFloatComponent fixFloat,
		final FloatingRateIndex fri,
		final String strCurrency,
		final ValuationParams valParams,
		final CurveSurfaceQuoteSet mktParams,
		final double dblFundingVol,
		final double dblForwardVol,
		final double dblFundingForwardCorr)
		throws Exception
	{
		SetMarketParams (mktParams, fri, strCurrency, dblFundingVol, dblForwardVol, dblFundingForwardCorr);

		CaseInsensitiveTreeMap<Double> mapMTMOutput = fixFloat.value (valParams, null, mktParams, null);

		System.out.println ("\t[" +
			FormatUtil.FormatDouble (dblFundingVol, 2, 0, 100.) + "%," +
			FormatUtil.FormatDouble (dblForwardVol, 2, 0, 100.) + "%," +
			FormatUtil.FormatDouble (dblFundingForwardCorr, 2, 0, 100.) + "%] = " +
			FormatUtil.FormatDouble (mapMTMOutput.get ("ReferenceQuantoAdjustmentPremium"), 2, 0, 10000.) + " | " +
			FormatUtil.FormatDouble (mapMTMOutput.get ("DerivedQuantoAdjustmentPremium"), 2, 0, 10000.) + " | " +
			FormatUtil.FormatDouble (mapMTMOutput.get ("QuantoAdjustmentPremium"), 2, 0, 10000.)
		);
	}

	public static final void main (
		final String[] astrArgs)
		throws Exception
	{
		double dblUSDCollateralRate = 0.02;
		double dblUSD3MForwardRate = 0.02;

		/*
		 * Initialize the Credit Analytics Library
		 */

		CreditAnalytics.Init ("");

		JulianDate dtToday = JulianDate.Today();

		ValuationParams valParams = new ValuationParams (dtToday, dtToday, "USD");

		FloatingRateIndex fri3M = FloatingRateIndex.Create ("USD", "LIBOR", "3M");

		DiscountCurve dcUSDCollatDomestic = DiscountCurveBuilder.CreateFromFlatRate (
			dtToday,
			"USD",
			new CollateralizationParams ("OVERNIGHT_INDEX", "USD"),
			dblUSDCollateralRate);

		ForwardCurve fc3MUSD = ScenarioForwardCurveBuilder.FlatForwardForwardCurve (
			dtToday,
			fri3M,
			dblUSD3MForwardRate,
			new CollateralizationParams ("OVERNIGHT_INDEX", "USD"));

		FixFloatComponent fixFloatUSD = MakeFixFloatSwap (
			dtToday,
			"USD",
			"2Y",
			3);

		fixFloatUSD.setPrimaryCode ("USD_IRS::3M::2Y");

		CurveSurfaceQuoteSet mktParams = new CurveSurfaceQuoteSet();

		mktParams.setFundingCurve (dcUSDCollatDomestic);

		mktParams.setForwardCurve (fc3MUSD);

		double[] adblFundingVol = new double[] {0.1, 0.2, 0.3, 0.4};

		double[] adblForwardVol = new double[] {0.1, 0.2, 0.3, 0.4};

		double[] adblFundingForwardCorr = new double[] {-0.4, -0.1, 0.1, 0.4};

		for (double dblFundingVol : adblFundingVol) {
			for (double dblForwardVol : adblForwardVol) {
				for (double dblFundingForwardCorr : adblFundingForwardCorr)
					VolCorrScenario (
						fixFloatUSD,
						fri3M,
						"USD",
						valParams,
						mktParams,
						dblFundingVol,
						dblForwardVol,
						dblFundingForwardCorr
					);
			}
		}
	}
}
