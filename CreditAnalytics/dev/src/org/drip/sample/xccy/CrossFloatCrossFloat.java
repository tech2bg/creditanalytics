
package org.drip.sample.xccy;

import java.util.*;

import org.drip.analytics.date.JulianDate;
import org.drip.analytics.rates.*;
import org.drip.analytics.support.*;
import org.drip.param.creator.*;
import org.drip.param.market.CurveSurfaceQuoteSet;
import org.drip.param.valuation.*;
import org.drip.product.cashflow.FloatingStream;
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
 * CrossFloatCrossFloat demonstrates the construction, usage, and eventual valuation of the Mark-to-market
 *  float-float swap with a 3M EUR Floater leg that pays in USD, and a 6M EUR Floater leg that pays in USD.
 *  Comparison is done across MTM and non-MTM fixed Leg Counterparts.
 * 
 * @author Lakshmi Krishnamurthy
 */

public class CrossFloatCrossFloat {

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

	public static final void main (
		final String[] astrArgs)
		throws Exception
	{
		double dblEURFundingRate = 0.02;
		double dblEUR3MForwardRate = 0.02;
		double dblEUR6MForwardRate = 0.025;
		double dblUSDEURFXRate = 1. / 1.35;

		double dblEURFundingVol = 0.3;
		double dblEURForward3MVol = 0.3;
		double dblEURForward6MVol = 0.3;
		double dblUSDEURFXVol = 0.3;

		double dblEUR3MUSDEURFXCorr = 0.1;
		double dblEUR6MUSDEURFXCorr = 0.1;
		double dblEURFundingEUR3MCorr = 0.1;
		double dblEURFundingEUR6MCorr = 0.1;
		double dblEURFundingUSDEURFXCorr = 0.1;

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

		FundingLabel fundingLabelEUR = org.drip.state.identifier.FundingLabel.Standard ("EUR");

		CurveSurfaceQuoteSet mktParams = new CurveSurfaceQuoteSet();

		mktParams.setForwardCurve (fcEUR3M);

		mktParams.setForwardCurve (fcEUR6M);

		mktParams.setFundingCurve (dcEURFunding);

		mktParams.setFXCurve (fxLabel, new FlatUnivariate (dblUSDEURFXRate));

		mktParams.setForwardCurveVolSurface (friEUR3M, new FlatUnivariate (dblEURForward3MVol));

		mktParams.setForwardCurveVolSurface (friEUR6M, new FlatUnivariate (dblEURForward6MVol));

		mktParams.setFundingCurveVolSurface (fundingLabelEUR, new FlatUnivariate (dblEURFundingVol));

		mktParams.setFXCurveVolSurface (fxLabel, new FlatUnivariate (dblUSDEURFXVol));

		mktParams.setForwardFundingCorrSurface (friEUR3M, fundingLabelEUR, new FlatUnivariate (dblEURFundingEUR3MCorr));

		mktParams.setForwardFundingCorrSurface (friEUR6M, fundingLabelEUR, new FlatUnivariate (dblEURFundingEUR6MCorr));

		mktParams.setForwardFXCorrSurface (friEUR3M, fxLabel, new FlatUnivariate (dblEUR3MUSDEURFXCorr));

		mktParams.setForwardFXCorrSurface (friEUR6M, fxLabel, new FlatUnivariate (dblEUR6MUSDEURFXCorr));

		mktParams.setFundingFXCorrSurface (fundingLabelEUR, fxLabel, new FlatUnivariate (dblEURFundingUSDEURFXCorr));

		CaseInsensitiveTreeMap<Double> mapMTMOutput = floatFloatMTM.value (valParams, null, mktParams, null);

		CaseInsensitiveTreeMap<Double> mapNonMTMOutput = floatFloatNonMTM.value (valParams, null, mktParams, null);

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
