
package org.drip.sample.mtm;

import java.util.List;

import org.drip.analytics.date.JulianDate;
import org.drip.analytics.daycount.*;
import org.drip.analytics.period.CashflowPeriod;
import org.drip.analytics.rates.*;
import org.drip.analytics.support.CaseInsensitiveTreeMap;
import org.drip.param.creator.*;
import org.drip.param.market.CurveSurfaceQuoteSet;
import org.drip.param.pricer.JointStatePricerParams;
import org.drip.param.valuation.*;
import org.drip.product.fx.*;
import org.drip.product.mtm.ComponentPairMTM;
import org.drip.product.params.*;
import org.drip.product.rates.*;
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
 * FixFloatFixFloatMTMVolAnalysis demonstrates the impact of Funding Volatility, FX Volatility, and Funding/FX
 * 	Correlation on the Valuation of an MTM Cross Currency Swap built out of a pair of fix-float swaps.
 * 
 * @author Lakshmi Krishnamurthy
 */

public class FixFloatFixFloatMTMVolAnalysis {

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

		/*
		 * The fix-float swap instance
		 */

		return new FixFloatComponent (fixStream, floatStream);
	}

	private static final void SetMarketParams (
		final CurveSurfaceQuoteSet mktParams,
		final CurrencyPair cp,
		final FloatingRateIndex friUSD3M,
		final FloatingRateIndex friJPY3M,
		final String strCurrency,
		final double dblForwardUSD3MVol,
		final double dblForwardJPY3MVol,
		final double dblFundingUSDVol,
		final double dblFundingJPYVol,
		final double dblForwardUSD3MFundingUSDCorr,
		final double dblForwardJPY3MFundingJPYCorr,
		final double dblJPYUSDFXVol,
		final double dblFundingUSDJPYUSDFXCorr)
		throws Exception
	{
		mktParams.setForwardCurveVolSurface (friUSD3M, new FlatUnivariate (dblForwardUSD3MVol));

		mktParams.setForwardCurveVolSurface (friJPY3M, new FlatUnivariate (dblForwardJPY3MVol));

		mktParams.setFundingCurveVolSurface ("USD", new FlatUnivariate (dblFundingUSDVol));

		mktParams.setFundingCurveVolSurface ("JPY", new FlatUnivariate (dblFundingJPYVol));

		mktParams.setForwardFundingCorrSurface (friUSD3M, "USD", new FlatUnivariate (dblForwardUSD3MFundingUSDCorr));

		mktParams.setForwardFundingCorrSurface (friJPY3M, "JPY", new FlatUnivariate (dblForwardJPY3MFundingJPYCorr));

		mktParams.setFXCurveVolSurface (cp, new FlatUnivariate (dblJPYUSDFXVol));

		mktParams.setFundingFXCorrSurface ("USD", cp, new FlatUnivariate (dblFundingUSDJPYUSDFXCorr));
	}

	private static final void VolCorrScenario (
		final ComponentPairMTM[] aCCBSMTM,
		final CurrencyPair cp,
		final FloatingRateIndex friUSD3M,
		final FloatingRateIndex friJPY3M,
		final String strCurrency,
		final ValuationParams valParams,
		final CurveSurfaceQuoteSet mktParams,
		final double dblForwardUSD3MVol,
		final double dblForwardJPY3MVol,
		final double dblFundingUSDVol,
		final double dblFundingJPYVol,
		final double dblForwardUSD3MFundingUSDCorr,
		final double dblForwardJPY3MFundingJPYCorr,
		final double dblJPYUSDFXVol,
		final double dblFundingUSDJPYUSDFXCorr,
		final JointStatePricerParams jspp)
		throws Exception
	{
		SetMarketParams (
			mktParams,
			cp,
			friUSD3M,
			friJPY3M,
			strCurrency,
			dblForwardUSD3MVol,
			dblForwardJPY3MVol,
			dblFundingUSDVol,
			dblFundingJPYVol,
			dblForwardUSD3MFundingUSDCorr,
			dblForwardJPY3MFundingJPYCorr,
			dblJPYUSDFXVol,
			dblFundingUSDJPYUSDFXCorr
		);

		String strDump = "\t[" +
				org.drip.quant.common.FormatUtil.FormatDouble (dblForwardUSD3MVol, 2, 0, 100.) + "%," +
				org.drip.quant.common.FormatUtil.FormatDouble (dblForwardJPY3MVol, 2, 0, 100.) + "%," +
				org.drip.quant.common.FormatUtil.FormatDouble (dblFundingUSDVol, 2, 0, 100.) + "%," +
				org.drip.quant.common.FormatUtil.FormatDouble (dblFundingJPYVol, 2, 0, 100.) + "%," +
				org.drip.quant.common.FormatUtil.FormatDouble (dblForwardUSD3MFundingUSDCorr, 2, 0, 100.) + "%," +
				org.drip.quant.common.FormatUtil.FormatDouble (dblForwardJPY3MFundingJPYCorr, 2, 0, 100.) + "%," +
				org.drip.quant.common.FormatUtil.FormatDouble (dblJPYUSDFXVol, 2, 0, 100.) + "%," +
				org.drip.quant.common.FormatUtil.FormatDouble (dblFundingUSDJPYUSDFXCorr, 2, 0, 100.) + "%] = ";

		for (int i = 0; i < aCCBSMTM.length; ++i) {
			CaseInsensitiveTreeMap<Double> mapMTMOutput = aCCBSMTM[i].value (valParams, jspp, mktParams, null);

			if (0 != i) strDump += "  ||  ";

			strDump += 
				org.drip.quant.common.FormatUtil.FormatDouble (mapMTMOutput.get ("ReferenceMTMAdditiveAdjustment"), 1, 2, 100.) + "% | " +
				org.drip.quant.common.FormatUtil.FormatDouble (mapMTMOutput.get ("DerivedMTMAdditiveAdjustment"), 1, 2, 100.) + "%";
		}

		System.out.println (strDump);
	}

	public static final void main (
		final String[] astrArgs)
		throws Exception
	{
		double dblUSDCollateralRate = 0.02;
		double dblUSD3MForwardRate = 0.02;
		double dblJPYCollateralRate = 0.02;
		double dblJPY3MForwardRate = 0.02;

		/*
		 * Initialize the Credit Analytics Library
		 */

		CreditAnalytics.Init ("");

		JulianDate dtToday = JulianDate.Today();

		ValuationParams valParams = new ValuationParams (dtToday, dtToday, "USD");

		DiscountCurve dcUSDCollatDomestic = DiscountCurveBuilder.CreateFromFlatRate (
			dtToday,
			"USD",
			new CollateralizationParams ("OVERNIGHT_INDEX", "USD"),
			dblUSDCollateralRate);

		FloatingRateIndex friUSD3M = FloatingRateIndex.Create ("USD", "LIBOR", "3M");

		ForwardCurve fc3MUSD = ScenarioForwardCurveBuilder.FlatForwardForwardCurve (
			dtToday,
			friUSD3M,
			dblUSD3MForwardRate,
			new CollateralizationParams ("OVERNIGHT_INDEX", "USD"));

		FixFloatComponent fixFloatUSD = MakeFixFloatSwap (
			dtToday,
			"USD",
			"2Y",
			3);

		fixFloatUSD.setPrimaryCode ("USD_IRS::3M::2Y");

		DiscountCurve dcJPYCollatDomestic = DiscountCurveBuilder.CreateFromFlatRate (
			dtToday,
			"JPY",
			new CollateralizationParams ("OVERNIGHT_INDEX", "JPY"),
			dblJPYCollateralRate);

		FloatingRateIndex friJPY3M = FloatingRateIndex.Create ("JPY", "LIBOR", "3M");

		ForwardCurve fc3MJPY = ScenarioForwardCurveBuilder.FlatForwardForwardCurve (
			dtToday,
			friJPY3M,
			dblJPY3MForwardRate,
			new CollateralizationParams ("OVERNIGHT_INDEX", "JPY"));

		FixFloatComponent fixFloatJPY = MakeFixFloatSwap (
			dtToday,
			"JPY",
			"2Y",
			3);

		fixFloatJPY.setPrimaryCode ("JPY_IRS::3M::2Y");

		ComponentPairMTM ccbsUSDJPYRelative = new ComponentPairMTM (
			new ComponentPair (
				"USDJPY_CCBS",
				fixFloatUSD,
				fixFloatJPY),
			false
		);

		ComponentPairMTM ccbsUSDJPYAbsolute = new ComponentPairMTM (
			new ComponentPair (
				"USDJPY_CCBS",
				fixFloatUSD,
				fixFloatJPY),
			true
		);

		CurveSurfaceQuoteSet mktParams = new CurveSurfaceQuoteSet();

		mktParams.setFundingCurve (dcUSDCollatDomestic);

		mktParams.setFundingCurve (dcJPYCollatDomestic);

		mktParams.setForwardCurve (fc3MUSD);

		mktParams.setForwardCurve (fc3MJPY);

		CurrencyPair cp = CurrencyPair.FromCode (ccbsUSDJPYRelative.fxCode());

		double[] adblForwardUSD3MVol = new double[] {0.1, 0.3, 0.5};
		double[] adblForwardJPY3MVol = new double[] {0.1, 0.3, 0.5};
		double[] adblFundingUSDVol = new double[] {0.1, 0.3, 0.5};
		double[] adblFundingJPYVol = new double[] {0.1, 0.3, 0.5};
		double[] adblForwardUSD3MFundingUSDCorr = new double[] {-0.1, 0.1, 0.3};
		double[] adblForwardJPY3MFundingJPYCorr = new double[] {-0.1, 0.1, 0.3};
		double[] adblJPYUSDFXVol = new double[] {0.1, 0.3, 0.5};
		double[] adblFundingUSDJPYUSDFXVol = new double[] {-0.1, 0.1, 0.3};

		JointStatePricerParams jspp = JointStatePricerParams.Make (JointStatePricerParams.QUANTO_ADJUSTMENT_FORWARD_FUNDING_FX);

		for (double dblForwardUSD3MVol : adblForwardUSD3MVol) {
			for (double dblForwardJPY3MVol : adblForwardJPY3MVol) {
				for (double dblFundingUSDVol : adblFundingUSDVol) {
					for (double dblFundingJPYVol : adblFundingJPYVol) {
						for (double dblForwardUSD3MFundingUSDCorr : adblForwardUSD3MFundingUSDCorr) {
							for (double dblForwardJPY3MFundingJPYCorr : adblForwardJPY3MFundingJPYCorr) {
								for (double dblJPYUSDFXVol : adblJPYUSDFXVol) {
									for (double dblFundingUSDJPYUSDFXVol : adblFundingUSDJPYUSDFXVol) {
										VolCorrScenario (
											new ComponentPairMTM[] {ccbsUSDJPYRelative, ccbsUSDJPYAbsolute},
											cp,
											friUSD3M,
											friJPY3M,
											"USD",
											valParams,
											mktParams,
											dblForwardUSD3MVol,
											dblForwardJPY3MVol,
											dblFundingUSDVol,
											dblFundingJPYVol,
											dblForwardUSD3MFundingUSDCorr,
											dblForwardJPY3MFundingJPYCorr,
											dblJPYUSDFXVol,
											dblFundingUSDJPYUSDFXVol,
											jspp
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
