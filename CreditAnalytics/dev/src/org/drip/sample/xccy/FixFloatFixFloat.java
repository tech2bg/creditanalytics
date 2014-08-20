
package org.drip.sample.xccy;

import java.util.*;

import org.drip.analytics.date.JulianDate;
import org.drip.analytics.period.CashflowPeriod;
import org.drip.analytics.support.CaseInsensitiveTreeMap;
import org.drip.analytics.support.PeriodHelper;
import org.drip.param.creator.ScenarioForwardCurveBuilder;
import org.drip.param.market.CurveSurfaceQuoteSet;
import org.drip.param.valuation.*;
import org.drip.product.cashflow.*;
import org.drip.product.fx.ComponentPair;
import org.drip.product.params.*;
import org.drip.product.rates.*;
import org.drip.quant.common.*;
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
 * FixFloatFixFloat demonstrates the construction, the usage, and the eventual valuation of the Cross
 *  Currency Basis Swap built out of a pair of fix-float swaps.
 * 
 * @author Lakshmi Krishnamurthy
 */

public class FixFloatFixFloat {

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

		List<CashflowPeriod> lsFixPeriods = PeriodHelper.RegularPeriodSingleReset (
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

		List<CashflowPeriod> lsDerivedFloatPeriods = PeriodHelper.RegularPeriodSingleReset (
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

	public static final void main (
		final String[] astrArgs)
		throws Exception
	{
		double dblUSDFundingRate = 0.03;
		double dblEURFundingRate = 0.02;
		double dblUSD3MForwardRate = 0.0275;
		double dblEUR3MForwardRate = 0.0175;
		double dblUSDEURFXRate = 1. / 1.34;

		double dblUSDFundingVol = 0.3;
		double dblEURFundingVol = 0.3;
		double dblUSD3MForwardVol = 0.3;
		double dblEUR3MForwardVol = 0.3;
		double dblUSDEURFXVol = 0.3;

		double dblUSDFundingUSD3MForwardCorr = 0.15;
		double dblEURFundingUSDEURFXCorr = 0.15;
		double dblEURFundingEUR3MForwardCorr = 0.15;
		double dblEUR3MForwardUSDEURFXCorr = 0.15;

		/*
		 * Initialize the Credit Analytics Library
		 */

		CreditAnalytics.Init ("");

		JulianDate dtToday = JulianDate.Today();

		ValuationParams valParams = new ValuationParams (dtToday, dtToday, "USD");

		ForwardLabel fri3MUSD = ForwardLabel.Create ("USD", "LIBOR", "3M");

		FundingLabel fundingLabelUSD = FundingLabel.Standard ("USD");

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

		FundingLabel fundingLabelEUR = FundingLabel.Standard ("EUR");

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

		mktParams.setFundingCurveVolSurface (
			fundingLabelUSD,
			new FlatUnivariate (dblUSDFundingVol)
		);

		mktParams.setFundingCurveVolSurface (
			fundingLabelEUR,
			new FlatUnivariate (dblEURFundingVol)
		);

		mktParams.setForwardCurveVolSurface (
			fri3MUSD,
			new FlatUnivariate (dblUSD3MForwardVol)
		);

		mktParams.setForwardCurveVolSurface (
			fri3MEUR,
			new FlatUnivariate (dblEUR3MForwardVol)
		);

		mktParams.setFXCurveVolSurface (
			fxLabel,
			new FlatUnivariate (dblUSDEURFXVol)
		);

		mktParams.setForwardFundingCorrSurface (
			fri3MUSD,
			fundingLabelUSD,
			new FlatUnivariate (dblUSDFundingUSD3MForwardCorr)
		);

		mktParams.setFundingFXCorrSurface (
			fundingLabelEUR,
			fxLabel,
			new FlatUnivariate (dblEURFundingUSDEURFXCorr)
		);

		mktParams.setForwardFundingCorrSurface (
			fri3MEUR,
			fundingLabelEUR,
			new FlatUnivariate (dblEURFundingEUR3MForwardCorr)
		);

		mktParams.setForwardFXCorrSurface (
			fri3MEUR,
			fxLabel,
			new FlatUnivariate (dblEUR3MForwardUSDEURFXCorr)
		);

		CaseInsensitiveTreeMap<Double> mapMTMOutput = cpMTM.value (valParams, null, mktParams, null);

		CaseInsensitiveTreeMap<Double> mapNonMTMOutput = cpNonMTM.value (valParams, null, mktParams, null);

		for (Map.Entry<String, Double> me : mapMTMOutput.entrySet()) {
			String strKey = me.getKey();

			if (null != me.getValue() && null != mapNonMTMOutput.get (strKey)) {
				double dblMTMMeasure = me.getValue();

				double dblNonMTMMeasure = mapNonMTMOutput.get (strKey);

				String strReconcile = NumberUtil.WithinTolerance (dblMTMMeasure, dblNonMTMMeasure, 1.e-08, 1.e-04) ?
					"RECONCILES" :
					"DOES NOT RECONCILE";

				System.out.println ("\t" +
					FormatUtil.FormatDouble (dblMTMMeasure, 1, 8, 1.) + " | " +
					FormatUtil.FormatDouble (dblNonMTMMeasure, 1, 8, 1.) + " | " +
					strReconcile + " <= " + strKey);
			}
		}
	}
}
