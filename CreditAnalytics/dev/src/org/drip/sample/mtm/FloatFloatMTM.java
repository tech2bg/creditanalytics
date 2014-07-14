
package org.drip.sample.mtm;

import java.util.*;

import org.drip.analytics.date.JulianDate;
import org.drip.analytics.daycount.*;
import org.drip.analytics.period.CashflowPeriod;
import org.drip.analytics.rates.*;
import org.drip.analytics.support.CaseInsensitiveTreeMap;
import org.drip.param.creator.*;
import org.drip.param.market.CurveSurfaceQuoteSet;
import org.drip.param.valuation.*;
import org.drip.product.fx.*;
import org.drip.product.mtm.ComponentPairMTM;
import org.drip.product.params.*;
import org.drip.product.rates.*;
import org.drip.quant.common.*;
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
 * FloatFloatMTM demonstrates the construction, usage, and eventual valuation of the Mark-to-market float-float swap.
 * 
 * @author Lakshmi Krishnamurthy
 */

public class FloatFloatMTM {

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
			dtEffective.getJulian(),
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
			dtEffective.getJulian(),
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

		ForwardCurve fc3MUSD = ScenarioForwardCurveBuilder.FlatForwardForwardCurve (
			dtToday,
			FloatingRateIndex.Create ("USD", "LIBOR", "3M"),
			dblUSD3MForwardRate,
			new CollateralizationParams ("OVERNIGHT_INDEX", "USD"));

		ForwardCurve fc6MUSD = ScenarioForwardCurveBuilder.FlatForwardForwardCurve (
			dtToday,
			FloatingRateIndex.Create ("USD", "LIBOR", "6M"),
			dblUSD6MForwardRate,
			new CollateralizationParams ("OVERNIGHT_INDEX", "USD"));

		FloatFloatComponent floatFloatUSD = MakeFloatFloatSwap (
			dtToday,
			"USD",
			"2Y",
			3);

		floatFloatUSD.setPrimaryCode ("USD_IRS::3M::6M::2Y");

		ComponentPairMTM ccbsUSDJPYAbsolute = new ComponentPairMTM (
			new ComponentPair (
				"USD_3M_6M_ABSOLUTE",
				floatFloatUSD.referenceStream(),
				floatFloatUSD.derivedStream()),
			true,
			ComponentPairMTM.MTM_QUANTO_ADJUSTMENT_FUNDING_FX,
			ComponentPairMTM.MTM_QUANTO_ADJUSTMENT_NONE
		);

		ComponentPairMTM ccbsUSDJPYRelative = new ComponentPairMTM (
			new ComponentPair (
				"USD_3M_6M_RELATIVE",
				floatFloatUSD.referenceStream(),
				floatFloatUSD.derivedStream()),
			false,
			ComponentPairMTM.MTM_QUANTO_ADJUSTMENT_FUNDING_FX,
			ComponentPairMTM.MTM_QUANTO_ADJUSTMENT_NONE
		);

		CurveSurfaceQuoteSet mktParams = new CurveSurfaceQuoteSet();

		mktParams.setFundingCurve (dcUSDCollatDomestic);

		mktParams.setForwardCurve (fc3MUSD);

		mktParams.setForwardCurve (fc6MUSD);

		mktParams.setFundingCurveVolSurface ("USD", new FlatUnivariate (0.3));

		CaseInsensitiveTreeMap<Double> mapAbsoluteMTMOutput = ccbsUSDJPYAbsolute.value (valParams, null, mktParams, null);

		CaseInsensitiveTreeMap<Double> mapRelativeMTMOutput = ccbsUSDJPYRelative.value (valParams, null, mktParams, null);

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
