
package org.drip.sample.mtm;

import java.util.List;

import org.drip.analytics.date.JulianDate;
import org.drip.analytics.daycount.*;
import org.drip.analytics.period.CashflowPeriod;
import org.drip.analytics.rates.*;
import org.drip.analytics.support.CaseInsensitiveTreeMap;
import org.drip.param.creator.ScenarioForwardCurveBuilder;
import org.drip.param.market.CurveSurfaceQuoteSet;
import org.drip.param.pricer.JointStatePricerParams;
import org.drip.param.valuation.*;
import org.drip.product.fx.ComponentPair;
import org.drip.product.mtm.ComponentPairMTM;
import org.drip.product.params.FloatingRateIndex;
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
 * FloatFloatMTMVolAnalysis demonstrates the impact of Funding Volatility, Forward Volatility, and
 *  Funding/Forward Correlation for each of the FRI's on the Valuation of a float-float swap.
 * 
 * @author Lakshmi Krishnamurthy
 */

public class FloatFloatMTMVolAnalysis {

	private static final FloatFloatComponent MakeFloatFloatSwap (
		final JulianDate dtEffective,
		final String strCurrency,
		final String strTenor,
		final int iTenorInMonths)
		throws Exception
	{
		DateAdjustParams dap = new DateAdjustParams (Convention.DR_FOLL, strCurrency);

			/*
			 * The Reference Leg
			 */

		List<CashflowPeriod> lsReferencePeriods = CashflowPeriod.GeneratePeriodsRegular (
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

		FloatingStream floatReferenceStream = new FloatingStream (
			strCurrency,
			0.,
			-1.,
			null,
			lsReferencePeriods,
			FloatingRateIndex.Create (strCurrency + "-LIBOR-6M"),
			true
		);

		floatReferenceStream.setPrimaryCode ("USD::6M::" + strTenor);

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

		FloatingStream floatDerivedStream = new FloatingStream (
			strCurrency,
			0.,
			1.,
			null,
			lsDerivedFloatPeriods,
			FloatingRateIndex.Create (strCurrency + "-LIBOR-" + iTenorInMonths + "M"),
			false
		);

		floatDerivedStream.setPrimaryCode ("USD::" + iTenorInMonths + "M::" + strTenor);

		/*
		 * The float-float swap instance
		 */

		return new FloatFloatComponent (floatReferenceStream, floatDerivedStream);
	}

	private static final void SetMarketParams (
		final CurveSurfaceQuoteSet mktParams,
		final FloatingRateIndex fri3M,
		final FloatingRateIndex fri6M,
		final String strCurrency,
		final double dblFundingVol,
		final double dblForward3MVol,
		final double dblFundingForward3MCorr,
		final double dblForward6MVol,
		final double dblFundingForward6MCorr)
		throws Exception
	{
		mktParams.setFundingCurveVolSurface (strCurrency, new FlatUnivariate (dblFundingVol));

		mktParams.setForwardCurveVolSurface (fri3M, new FlatUnivariate (dblForward3MVol));

		mktParams.setForwardFundingCorrSurface (fri3M, strCurrency, new FlatUnivariate (dblFundingForward3MCorr));

		mktParams.setForwardCurveVolSurface (fri6M, new FlatUnivariate (dblForward6MVol));

		mktParams.setForwardFundingCorrSurface (fri6M, strCurrency, new FlatUnivariate (dblFundingForward6MCorr));
	}

	private static final void VolCorrScenario (
		final ComponentPairMTM[] aCCBSMTM,
		final FloatingRateIndex fri3M,
		final FloatingRateIndex fri6M,
		final String strCurrency,
		final ValuationParams valParams,
		final CurveSurfaceQuoteSet mktParams,
		final double dblFundingVol,
		final double dblForward3MVol,
		final double dblFundingForward3MCorr,
		final double dblForward6MVol,
		final double dblFundingForward6MCorr,
		final JointStatePricerParams jspp)
		throws Exception
	{
		SetMarketParams (
			mktParams,
			fri3M,
			fri6M,
			strCurrency,
			dblFundingVol,
			dblForward3MVol,
			dblFundingForward3MCorr,
			dblForward6MVol,
			dblFundingForward6MCorr
		);

		String strDump = "\t[" +
				org.drip.quant.common.FormatUtil.FormatDouble (dblFundingVol, 2, 0, 100.) + "%," +
				org.drip.quant.common.FormatUtil.FormatDouble (dblForward3MVol, 2, 0, 100.) + "%," +
				org.drip.quant.common.FormatUtil.FormatDouble (dblFundingForward3MCorr, 2, 0, 100.) + "%," +
				org.drip.quant.common.FormatUtil.FormatDouble (dblForward6MVol, 2, 0, 100.) + "%," +
				org.drip.quant.common.FormatUtil.FormatDouble (dblFundingForward6MCorr, 2, 0, 100.) + "%] = ";

		for (int i = 0; i < aCCBSMTM.length; ++i) {
			CaseInsensitiveTreeMap<Double> mapMTMOutput = aCCBSMTM[i].value (valParams, jspp, mktParams, null);

			if (0 != i) strDump += "  ||  ";

			strDump += 
				org.drip.quant.common.FormatUtil.FormatDouble (mapMTMOutput.get ("ReferenceMTMAdditiveAdjustment"), 2, 2, 100.) + "% | " +
				org.drip.quant.common.FormatUtil.FormatDouble (mapMTMOutput.get ("DerivedMTMAdditiveAdjustment"), 2, 2, 100.) + "%";
		}

		System.out.println (strDump);
	}

	public static final void main (
		final String[] astrArgs)
		throws Exception
	{
		double dblUSDCollateralRate = 0.02;
		double dblUSD3MForwardRate = 0.02;
		double dblUSD6MForwardRate = 0.025;

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

		FloatingRateIndex fri3M = FloatingRateIndex.Create ("USD", "LIBOR", "3M");

		ForwardCurve fc3MUSD = ScenarioForwardCurveBuilder.FlatForwardForwardCurve (
			dtToday,
			fri3M,
			dblUSD3MForwardRate,
			new CollateralizationParams ("OVERNIGHT_INDEX", "USD"));

		FloatingRateIndex fri6M = FloatingRateIndex.Create ("USD", "LIBOR", "6M");

		ForwardCurve fc6MUSD = ScenarioForwardCurveBuilder.FlatForwardForwardCurve (
			dtToday,
			fri6M,
			dblUSD6MForwardRate,
			new CollateralizationParams ("OVERNIGHT_INDEX", "USD"));

		FloatFloatComponent floatFloatUSD = MakeFloatFloatSwap (
			dtToday,
			"USD",
			"2Y",
			3);

		floatFloatUSD.setPrimaryCode ("USD_IRS::3M::6M::2Y");

		ComponentPairMTM floatFloatAbsolute = new ComponentPairMTM (
			new ComponentPair (
				"USD_3M_6M_ABSOLUTE",
				floatFloatUSD.referenceStream(),
				floatFloatUSD.derivedStream()),
			true
		);

		ComponentPairMTM floatFloatRelative = new ComponentPairMTM (
			new ComponentPair (
				"USD_3M_6M_RELATIVE",
				floatFloatUSD.referenceStream(),
				floatFloatUSD.derivedStream()),
			false
		);

		CurveSurfaceQuoteSet mktParams = new CurveSurfaceQuoteSet();

		mktParams.setFundingCurve (dcUSDCollatDomestic);

		mktParams.setForwardCurve (fc3MUSD);

		mktParams.setForwardCurve (fc6MUSD);

		double[] adblFundingVol = new double[] {0.1, 0.3, 0.5};

		double[] adblForward3MVol = new double[] {0.1, 0.3, 0.5};

		double[] adblFundingForward3MCorr = new double[] {-0.6, 0.1, 0.8};

		double[] adblForward6MVol = new double[] {0.1, 0.3, 0.5};

		double[] adblFundingForward6MCorr = new double[] {-0.6, 0.1, 0.8};

		JointStatePricerParams jspp = JointStatePricerParams.Make (JointStatePricerParams.QUANTO_ADJUSTMENT_FORWARD_FUNDING_FX);

		for (double dblFundingVol : adblFundingVol) {
			for (double dblForward3MVol : adblForward3MVol) {
				for (double dblFundingForward3MCorr : adblFundingForward3MCorr) {
					for (double dblForward6MVol : adblForward6MVol) {
						for (double dblFundingForward6MCorr : adblFundingForward6MCorr)
							VolCorrScenario (
								new ComponentPairMTM[] {floatFloatRelative, floatFloatAbsolute},
								fri3M,
								fri6M,
								"USD",
								valParams,
								mktParams,
								dblFundingVol,
								dblForward3MVol,
								dblFundingForward3MCorr,
								dblForward6MVol,
								dblFundingForward6MCorr,
								jspp
							);
					}
				}
			}
		}
	}
}
