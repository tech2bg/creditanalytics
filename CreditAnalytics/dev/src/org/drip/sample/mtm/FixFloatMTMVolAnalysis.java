
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
		final ComponentPairMTM[] aCCBSMTM,
		final FloatingRateIndex fri,
		final String strCurrency,
		final ValuationParams valParams,
		final CurveSurfaceQuoteSet mktParams,
		final double dblFundingVol,
		final double dblForwardVol,
		final double dblFundingForwardCorr,
		final JointStatePricerParams jspp)
		throws Exception
	{
		SetMarketParams (mktParams, fri, strCurrency, dblFundingVol, dblForwardVol, dblFundingForwardCorr);

		String strDump = "\t[" +
				org.drip.quant.common.FormatUtil.FormatDouble (dblFundingVol, 2, 0, 100.) + "%," +
				org.drip.quant.common.FormatUtil.FormatDouble (dblForwardVol, 2, 0, 100.) + "%," +
				org.drip.quant.common.FormatUtil.FormatDouble (dblFundingForwardCorr, 2, 0, 100.) + "%] = ";

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

		ComponentPairMTM fixFloatAbsolute = new ComponentPairMTM (
			new ComponentPair (
				"USD_IRS",
				fixFloatUSD.referenceStream(),
				fixFloatUSD.derivedStream()),
			true
		);

		ComponentPairMTM fixFloatRelative = new ComponentPairMTM (
			new ComponentPair (
				"USD_IRS",
				fixFloatUSD.referenceStream(),
				fixFloatUSD.derivedStream()),
			false
		);

		CurveSurfaceQuoteSet mktParams = new CurveSurfaceQuoteSet();

		mktParams.setFundingCurve (dcUSDCollatDomestic);

		mktParams.setForwardCurve (fc3MUSD);

		double[] adblFundingVol = new double[] {0.1, 0.2, 0.3, 0.4};

		double[] adblForwardVol = new double[] {0.1, 0.2, 0.3, 0.4};

		double[] adblFundingForwardCorr = new double[] {-0.4, -0.1, 0.1, 0.4};

		JointStatePricerParams jspp = JointStatePricerParams.Make (JointStatePricerParams.QUANTO_ADJUSTMENT_FORWARD_FUNDING_FX);

		for (double dblFundingVol : adblFundingVol) {
			for (double dblForwardVol : adblForwardVol) {
				for (double dblFundingForwardCorr : adblFundingForwardCorr)
					VolCorrScenario (
						new ComponentPairMTM[] {fixFloatRelative, fixFloatAbsolute},
						fri3M,
						"USD",
						valParams,
						mktParams,
						dblFundingVol,
						dblForwardVol,
						dblFundingForwardCorr,
						jspp
					);
			}
		}
	}
}
