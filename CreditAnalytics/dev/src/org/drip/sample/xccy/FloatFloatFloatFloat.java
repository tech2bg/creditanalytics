
package org.drip.sample.xccy;

import java.util.*;

import org.drip.analytics.date.JulianDate;
import org.drip.analytics.support.*;
import org.drip.param.creator.*;
import org.drip.param.market.CurveSurfaceQuoteSet;
import org.drip.param.valuation.*;
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
		final boolean bFXMTM,
		final String strPayCurrency,
		final String strCouponCurrency,
		final String strMaturityTenor,
		final int iTenorInMonthsReference,
		final int iTenorInMonthsDerived)
		throws Exception
	{
			/*
			 * The Reference Leg
			 */

		Stream floatStreamReference = new Stream (
			PeriodBuilder.RegularPeriodSingleReset (
				dtEffective.julian(),
				strMaturityTenor,
				bFXMTM ? Double.NaN : dtEffective.julian(),
				null,
				null,
				null,
				null,
				null,
				null,
				null,
				null,
				12 / iTenorInMonthsReference,
				"Act/360",
				false,
				"Act/360",
				false,
				false,
				strCouponCurrency,
				-1.,
				null,
				0.,
				strPayCurrency,
				strCouponCurrency,
				ForwardLabel.Standard (strCouponCurrency + "-LIBOR-" + iTenorInMonthsReference + "M"),
				null
			)
		);

		/*
		 * The Derived Leg
		 */

		Stream floatStreamDerived = new Stream (
			PeriodBuilder.RegularPeriodSingleReset (
				dtEffective.julian(),
				strMaturityTenor,
				bFXMTM ? Double.NaN : dtEffective.julian(),
				null,
				null,
				null,
				null,
				null,
				null,
				null,
				null,
				12 / iTenorInMonthsDerived,
				"Act/360",
				false,
				"Act/360",
				false,
				false,
				strCouponCurrency,
				1.,
				null,
				0.,
				strPayCurrency,
				strCouponCurrency,
				ForwardLabel.Standard (strCouponCurrency + "-LIBOR-" + iTenorInMonthsDerived + "M"),
				null
			)
		);

		/*
		 * The float-float swap instance
		 */

		return new FloatFloatComponent (
			floatStreamReference,
			floatStreamDerived,
			new CashSettleParams (0, strCouponCurrency, 0)
		);
	}

	public static final void main (
		final String[] astrArgs)
		throws Exception
	{
		String strReferenceCurrency = "USD";
		String strDerivedCurrency = "EUR";

		double dblReference3MForwardRate = 0.00750;
		double dblReference6MForwardRate = 0.01000;
		double dblDerived3MForwardRate = 0.00375;
		double dblDerived6MForwardRate = 0.00625;
		double dblReferenceFundingRate = 0.02;
		double dblReferenceDerivedFXRate = 1. / 1.28;

		double dblReference3MForwardVol = 0.3;
		double dblReference6MForwardVol = 0.3;
		double dblDerived3MForwardVol = 0.3;
		double dblDerived6MForwardVol = 0.3;
		double dblReferenceFundingVol = 0.3;
		double dblReferenceDerivedFXVol = 0.3;

		double dblReference3MForwardFundingCorr = 0.15;
		double dblReference6MForwardFundingCorr = 0.15;
		double dblDerived3MForwardFundingCorr = 0.15;
		double dblDerived6MForwardFundingCorr = 0.15;

		double dblReference3MForwardFXCorr = 0.15;
		double dblReference6MForwardFXCorr = 0.15;
		double dblDerived3MForwardFXCorr = 0.15;
		double dblDerived6MForwardFXCorr = 0.15;

		double dblFundingFXCorr = 0.15;

		/*
		 * Initialize the Credit Analytics Library
		 */

		CreditAnalytics.Init ("");

		JulianDate dtToday = JulianDate.Today();

		ValuationParams valParams = new ValuationParams (dtToday, dtToday, strReferenceCurrency);

		ForwardLabel fri3MReference = ForwardLabel.Create (strReferenceCurrency, "LIBOR", "3M");

		ForwardLabel fri6MReference = ForwardLabel.Create (strReferenceCurrency, "LIBOR", "6M");

		ForwardLabel fri3MDerived = ForwardLabel.Create (strDerivedCurrency, "LIBOR", "3M");

		ForwardLabel fri6MDerived = ForwardLabel.Create (strDerivedCurrency, "LIBOR", "6M");

		FundingLabel fundingLabelReference = FundingLabel.Standard (strReferenceCurrency);

		FXLabel fxLabel = FXLabel.Standard (CurrencyPair.FromCode (strReferenceCurrency + "/" + strDerivedCurrency));

		FloatFloatComponent floatFloatReference = MakeFloatFloatSwap (
			dtToday,
			false,
			strReferenceCurrency,
			strReferenceCurrency,
			"2Y",
			6,
			3
		);

		floatFloatReference.setPrimaryCode (
			"FLOAT::FLOAT::" + strReferenceCurrency + "::" + strReferenceCurrency + "_3M::" + strReferenceCurrency + "_6M::2Y"
		);

		FloatFloatComponent floatFloatDerivedMTM = MakeFloatFloatSwap (
			dtToday,
			true,
			strReferenceCurrency,
			strDerivedCurrency,
			"2Y",
			6,
			3
		);

		floatFloatDerivedMTM.setPrimaryCode (
			"FLOAT::FLOAT::MTM::" + strReferenceCurrency + "::" + strDerivedCurrency + "_3M::" + strDerivedCurrency + "_6M::2Y"
		);

		ComponentPair cpMTM = new ComponentPair (
			"FFFF_MTM",
			floatFloatReference,
			floatFloatDerivedMTM
		);

		FloatFloatComponent floatFloatDerivedNonMTM = MakeFloatFloatSwap (
			dtToday,
			false,
			strReferenceCurrency,
			strDerivedCurrency,
			"2Y",
			6,
			3
		);

		floatFloatDerivedNonMTM.setPrimaryCode (
			"FLOAT::FLOAT::NONMTM::" + strReferenceCurrency + "::" + strDerivedCurrency + "_3M::" + strDerivedCurrency + "_6M::2Y"
		);

		ComponentPair cpNonMTM = new ComponentPair (
			"FFFF_NonMTM",
			floatFloatReference,
			floatFloatDerivedNonMTM
		);

		CurveSurfaceQuoteSet mktParams = new CurveSurfaceQuoteSet();

		mktParams.setFixing (
			dtToday,
			fxLabel,
			dblReferenceDerivedFXRate
		);

		mktParams.setForwardCurve (
			ScenarioForwardCurveBuilder.FlatForwardForwardCurve (
				dtToday,
				fri3MReference,
				dblReference3MForwardRate,
				new CollateralizationParams ("OVERNIGHT_INDEX", strReferenceCurrency)
			)
		);

		mktParams.setForwardCurve (
			ScenarioForwardCurveBuilder.FlatForwardForwardCurve (
				dtToday,
				fri6MReference,
				dblReference6MForwardRate,
				new CollateralizationParams ("OVERNIGHT_INDEX", strReferenceCurrency)
			)
		);

		mktParams.setForwardCurve (
			ScenarioForwardCurveBuilder.FlatForwardForwardCurve (
				dtToday,
				fri3MDerived,
				dblDerived3MForwardRate,
				new CollateralizationParams ("OVERNIGHT_INDEX", strReferenceCurrency)
			)
		);

		mktParams.setForwardCurve (
			ScenarioForwardCurveBuilder.FlatForwardForwardCurve (
				dtToday,
				fri6MDerived,
				dblDerived6MForwardRate,
				new CollateralizationParams ("OVERNIGHT_INDEX", strReferenceCurrency)
			)
		);

		mktParams.setFundingCurve (
			DiscountCurveBuilder.CreateFromFlatRate (
				dtToday,
				strReferenceCurrency,
				new CollateralizationParams ("OVERNIGHT_INDEX", strReferenceCurrency),
				dblReferenceFundingRate
			)
		);

		mktParams.setFXCurve (
			fxLabel,
			new FlatUnivariate (dblReferenceDerivedFXRate)
		);

		mktParams.setForwardCurveVolSurface (fri3MReference, new FlatUnivariate (dblReference3MForwardVol));

		mktParams.setForwardCurveVolSurface (fri6MReference, new FlatUnivariate (dblReference6MForwardVol));

		mktParams.setForwardCurveVolSurface (fri3MDerived, new FlatUnivariate (dblDerived3MForwardVol));

		mktParams.setForwardCurveVolSurface (fri6MDerived, new FlatUnivariate (dblDerived6MForwardVol));

		mktParams.setFundingCurveVolSurface (fundingLabelReference, new FlatUnivariate (dblReferenceFundingVol));

		mktParams.setFXCurveVolSurface (fxLabel, new FlatUnivariate (dblReferenceDerivedFXVol));

		mktParams.setForwardFundingCorrSurface (fri3MReference, fundingLabelReference, new FlatUnivariate (dblReference3MForwardFundingCorr));

		mktParams.setForwardFundingCorrSurface (fri6MReference, fundingLabelReference, new FlatUnivariate (dblReference6MForwardFundingCorr));

		mktParams.setForwardFundingCorrSurface (fri3MDerived, fundingLabelReference, new FlatUnivariate (dblDerived3MForwardFundingCorr));

		mktParams.setForwardFundingCorrSurface (fri6MDerived, fundingLabelReference, new FlatUnivariate (dblDerived6MForwardFundingCorr));

		mktParams.setForwardFXCorrSurface (fri3MReference, fxLabel, new FlatUnivariate (dblReference3MForwardFXCorr));

		mktParams.setForwardFXCorrSurface (fri6MReference, fxLabel, new FlatUnivariate (dblReference6MForwardFXCorr));

		mktParams.setForwardFXCorrSurface (fri3MDerived, fxLabel, new FlatUnivariate (dblDerived3MForwardFXCorr));

		mktParams.setForwardFXCorrSurface (fri6MDerived, fxLabel, new FlatUnivariate (dblDerived6MForwardFXCorr));

		mktParams.setFundingFXCorrSurface (fundingLabelReference, fxLabel, new FlatUnivariate (dblFundingFXCorr));

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
