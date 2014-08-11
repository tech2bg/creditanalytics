
package org.drip.sample.xccy;

import java.util.*;

import org.drip.analytics.date.JulianDate;
import org.drip.analytics.period.CashflowPeriod;
import org.drip.analytics.support.CaseInsensitiveTreeMap;
import org.drip.analytics.support.PeriodBuilder;
import org.drip.param.creator.*;
import org.drip.param.market.CurveSurfaceQuoteSet;
import org.drip.param.valuation.*;
import org.drip.product.cashflow.FloatingStream;
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
 * FloatFloatFloatFloat demonstrates the construction, the usage, and the eventual valuation of the Cross
 *  Currency Basis Swap built out of a pair of float-float swaps.
 * 
 * @author Lakshmi Krishnamurthy
 */

public class FloatFloatFloatFloat {

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

		double dblUSDFundingVol = 0.3;
		double dblUSD3MForwardVol = 0.3;
		double dblUSD6MForwardVol = 0.3;
		double dblEURFundingVol = 0.3;
		double dblEUR3MForwardVol = 0.3;
		double dblEUR6MForwardVol = 0.3;
		double dblEURUSDFXVol = 0.3;

		double dblUSDFundingUSD3MForwardCorr = 0.15;
		double dblUSDFundingUSD6MForwardCorr = 0.15;
		double dblEURFundingEUR3MForwardCorr = 0.15;
		double dblEURFundingEUR6MForwardCorr = 0.15;
		double dblUSD3MForwardEURUSDFXCorr = 0.15;
		double dblUSD6MForwardEURUSDFXCorr = 0.15;
		double dblUSDFundingUSDEURFXCorr = 0.15;

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

		FundingLabel fundingLabelUSD = FundingLabel.Standard ("USD");

		FundingLabel fundingLabelEUR = FundingLabel.Standard ("EUR");

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
