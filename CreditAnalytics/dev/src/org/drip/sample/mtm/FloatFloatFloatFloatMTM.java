
package org.drip.sample.mtm;

import java.util.*;

import org.drip.analytics.date.JulianDate;
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
import org.drip.quant.common.FormatUtil;
import org.drip.quant.common.NumberUtil;
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
 * FloatFloatFloatFloatMTM demonstrates the construction, the usage, and the eventual valuation of the
 *  Mark-to-market Cross Currency Basis Swap built out of a pair of float-float swaps.
 * 
 * @author Lakshmi Krishnamurthy
 */

public class FloatFloatFloatFloatMTM {
	private static final FloatFloatComponent MakexM6MBasisSwap (
		final JulianDate dtEffective,
		final String strCurrency,
		final String strTenor,
		final int iTenorInMonths)
		throws Exception
	{
		/*
		 * The Reference 6M Leg
		 */

		List<CashflowPeriod> lsFloatPeriods = CashflowPeriod.GeneratePeriodsRegular (
			dtEffective.julian(),
			strTenor,
			null,
			2,
			"Act/360",
			false,
			false,
			strCurrency,
			strCurrency
		);

		FloatingStream fsReference = new FloatingStream (
			strCurrency,
			null,
			0.,
			-1.,
			null,
			lsFloatPeriods,
			FloatingRateIndex.Create (strCurrency + "-LIBOR-6M"),
			false
		);

		/*
		 * The Derived Leg
		 */

		List<CashflowPeriod> lsDerivedFloatPeriods = CashflowPeriod.GeneratePeriodsRegular (
			dtEffective.julian(),
			strTenor,
			null,
			12 / iTenorInMonths,
			"Act/360",
			false,
			false,
			strCurrency,
			strCurrency
		);

		FloatingStream fsDerived = new FloatingStream (
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
		 * The float-float swap instance
		 */

		return new FloatFloatComponent (fsReference, fsDerived);
	}

	public static final void main (
		final String[] astrArgs)
		throws Exception
	{
		double dblUSDCollateralRate = 0.02;
		double dblUSD3MForwardRate = 0.02;
		double dblJPYCollateralRate = 0.02;
		double dblJPY3MForwardRate = 0.02;

		double dblForwardUSD3MVol = 0.3;
		double dblForwardUSD6MVol = 0.3;
		double dblForwardJPY3MVol = 0.3;
		double dblForwardJPY6MVol = 0.3;
		double dblFundingUSDVol = 0.3;
		double dblFundingJPYVol = 0.3;
		double dblForwardUSD3MFundingUSDCorr = 0.3;
		double dblForwardUSD6MFundingUSDCorr = 0.3;
		double dblForwardJPY3MFundingJPYCorr = 0.3;
		double dblForwardJPY6MFundingJPYCorr = 0.3;
		double dblJPYUSDFXVol = 0.3;
		double dblFundingUSDJPYUSDFXCorr = 0.3;
		double dblForwardUSD3MJPYUSDFXCorr = 0.3;
		double dblForwardUSD6MJPYUSDFXCorr = 0.3;

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

		FloatingRateIndex friUSD6M = FloatingRateIndex.Create ("USD", "LIBOR", "6M");

		ForwardCurve fc3MUSD = ScenarioForwardCurveBuilder.FlatForwardForwardCurve (
			dtToday,
			friUSD3M,
			dblUSD3MForwardRate,
			new CollateralizationParams ("OVERNIGHT_INDEX", "USD"));

		FloatFloatComponent ffcReferenceUSD = MakexM6MBasisSwap (
			dtToday,
			"USD",
			"2Y",
			3);

		ffcReferenceUSD.setPrimaryCode ("USD_6M::3M::2Y");

		DiscountCurve dcJPYCollatDomestic = DiscountCurveBuilder.CreateFromFlatRate (
			dtToday,
			"JPY",
			new CollateralizationParams ("OVERNIGHT_INDEX", "JPY"),
			dblJPYCollateralRate);

		FloatingRateIndex friJPY3M = FloatingRateIndex.Create ("JPY", "LIBOR", "3M");

		FloatingRateIndex friJPY6M = FloatingRateIndex.Create ("JPY", "LIBOR", "6M");

		ForwardCurve fc3MJPY = ScenarioForwardCurveBuilder.FlatForwardForwardCurve (
			dtToday,
			friJPY3M,
			dblJPY3MForwardRate,
			new CollateralizationParams ("OVERNIGHT_INDEX", "JPY"));

		FloatFloatComponent ffcDerivedJPY = MakexM6MBasisSwap (
			dtToday,
			"JPY",
			"2Y",
			3);

		ffcDerivedJPY.setPrimaryCode ("JPY_6M::3M::2Y");

		ComponentPairMTM ccbsUSDJPYAbsolute = new ComponentPairMTM (
			new ComponentPair (
				"USDJPY_CCBS",
				ffcReferenceUSD,
				ffcDerivedJPY),
			true
		);

		ComponentPairMTM ccbsUSDJPYRelative = new ComponentPairMTM (
			new ComponentPair (
				"USDJPY_CCBS",
				ffcReferenceUSD,
				ffcDerivedJPY),
			false
		);

		CurveSurfaceQuoteSet mktParams = new CurveSurfaceQuoteSet();

		mktParams.setFundingCurve (dcUSDCollatDomestic);

		mktParams.setFundingCurve (dcJPYCollatDomestic);

		mktParams.setForwardCurve (fc3MUSD);

		mktParams.setForwardCurve (fc3MJPY);

		CurrencyPair cp = CurrencyPair.FromCode ("USD/JPY");

		mktParams.setFundingCurveVolSurface ("USD", new FlatUnivariate (dblFundingUSDVol));

		mktParams.setFundingCurveVolSurface ("JPY", new FlatUnivariate (dblFundingJPYVol));

		mktParams.setForwardCurveVolSurface (friUSD3M, new FlatUnivariate (dblForwardUSD3MVol));

		mktParams.setForwardCurveVolSurface (friUSD6M, new FlatUnivariate (dblForwardUSD6MVol));

		mktParams.setForwardCurveVolSurface (friJPY3M, new FlatUnivariate (dblForwardJPY3MVol));

		mktParams.setForwardCurveVolSurface (friJPY6M, new FlatUnivariate (dblForwardJPY6MVol));

		mktParams.setForwardFundingCorrSurface (friUSD3M, "USD", new FlatUnivariate (dblForwardUSD3MFundingUSDCorr));

		mktParams.setForwardFundingCorrSurface (friUSD6M, "USD", new FlatUnivariate (dblForwardUSD6MFundingUSDCorr));

		mktParams.setForwardFundingCorrSurface (friJPY3M, "JPY", new FlatUnivariate (dblForwardJPY3MFundingJPYCorr));

		mktParams.setForwardFundingCorrSurface (friJPY6M, "JPY", new FlatUnivariate (dblForwardJPY6MFundingJPYCorr));

		mktParams.setFXCurveVolSurface (cp, new FlatUnivariate (dblJPYUSDFXVol));

		mktParams.setFundingFXCorrSurface ("USD", cp, new FlatUnivariate (dblFundingUSDJPYUSDFXCorr));

		mktParams.setForwardFXCorrSurface (friUSD3M, cp, new FlatUnivariate (dblForwardUSD3MJPYUSDFXCorr));

		mktParams.setForwardFXCorrSurface (friUSD6M, cp, new FlatUnivariate (dblForwardUSD6MJPYUSDFXCorr));

		JointStatePricerParams jspp = JointStatePricerParams.Make (JointStatePricerParams.QUANTO_ADJUSTMENT_FORWARD_FUNDING_FX);

		CaseInsensitiveTreeMap<Double> mapAbsoluteMTMOutput = ccbsUSDJPYAbsolute.value (valParams, jspp, mktParams, null);

		CaseInsensitiveTreeMap<Double> mapRelativeMTMOutput = ccbsUSDJPYRelative.value (valParams, jspp, mktParams, null);

		for (Map.Entry<String, Double> me : mapRelativeMTMOutput.entrySet()) {
			String strKey = me.getKey();

			double dblAbsoluteMeasure = mapAbsoluteMTMOutput.get (strKey);

			double dblRelativeMeasure = mapRelativeMTMOutput.get (strKey);

			String strReconcile = NumberUtil.WithinTolerance (dblAbsoluteMeasure, dblRelativeMeasure, 1.e-08, 1.e-04) ?
				"RECONCILES" :
				"DOES NOT RECONCILE";

			System.out.println ("\t" +
				FormatUtil.FormatDouble (dblAbsoluteMeasure, 1, 8, 1.) + " | " +
				FormatUtil.FormatDouble (dblRelativeMeasure, 1, 8, 1.) + " | " +
				strReconcile + " <= " + strKey);
		}
	}
}
