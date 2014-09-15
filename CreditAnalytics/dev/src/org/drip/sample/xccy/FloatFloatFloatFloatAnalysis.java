
package org.drip.sample.xccy;

import org.drip.analytics.date.JulianDate;
import org.drip.analytics.support.*;
import org.drip.param.creator.ScenarioForwardCurveBuilder;
import org.drip.param.market.CurveSurfaceQuoteSet;
import org.drip.param.valuation.*;
import org.drip.product.cashflow.Stream;
import org.drip.product.fx.ComponentPair;
import org.drip.product.params.*;
import org.drip.product.rates.*;
import org.drip.quant.common.FormatUtil;
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
 * FloatFloatFloatFloatAnalysis demonstrates the Funding Volatility, Forward Volatility, FX Volatility,
 *  Funding/Forward Correlation, Funding/FX Correlation, and Forward/FX Correlation of the Cross Currency
 *  Basis Swap built out of a pair of float-float swaps.
 * 
 * @author Lakshmi Krishnamurthy
 */

public class FloatFloatFloatFloatAnalysis {

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
				12 / iTenorInMonthsReference,
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
				12 / iTenorInMonthsDerived,
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

	private static final void SetMarketParams (
		final CurveSurfaceQuoteSet mktParams,
		final ForwardLabel forwardReferenceLabel1,
		final ForwardLabel forwardReferenceLabel2,
		final ForwardLabel forwardDerivedLabel1,
		final ForwardLabel forwardDerivedLabel2,
		final FundingLabel fundingLabel,
		final FXLabel fxLabel,
		final double dblForwardReference1Vol,
		final double dblForwardReference2Vol,
		final double dblForwardDerived1Vol,
		final double dblForwardDerived2Vol,
		final double dblFundingVol,
		final double dblFXVol,
		final double dblForwardReference1FundingCorr,
		final double dblForwardReference2FundingCorr,
		final double dblForwardDerived1FundingCorr,
		final double dblForwardDerived2FundingCorr,
		final double dblForwardReference1FXCorr,
		final double dblForwardReference2FXCorr,
		final double dblForwardDerived1FXCorr,
		final double dblForwardDerived2FXCorr,
		final double dblFundingFXCorr)
		throws Exception
	{
		mktParams.setForwardCurveVolSurface (forwardReferenceLabel1, new FlatUnivariate (dblForwardReference1Vol));

		mktParams.setForwardCurveVolSurface (forwardReferenceLabel2, new FlatUnivariate (dblForwardReference2Vol));

		mktParams.setForwardCurveVolSurface (forwardDerivedLabel1, new FlatUnivariate (dblForwardDerived1Vol));

		mktParams.setForwardCurveVolSurface (forwardDerivedLabel2, new FlatUnivariate (dblForwardDerived2Vol));

		mktParams.setFundingCurveVolSurface (fundingLabel, new FlatUnivariate (dblFundingVol));

		mktParams.setFXCurveVolSurface (fxLabel, new FlatUnivariate (dblFXVol));

		mktParams.setForwardFundingCorrSurface (forwardReferenceLabel1, fundingLabel, new FlatUnivariate (dblForwardReference1FundingCorr));

		mktParams.setForwardFundingCorrSurface (forwardReferenceLabel2, fundingLabel, new FlatUnivariate (dblForwardReference2FundingCorr));

		mktParams.setForwardFundingCorrSurface (forwardDerivedLabel1, fundingLabel, new FlatUnivariate (dblForwardDerived1FundingCorr));

		mktParams.setForwardFundingCorrSurface (forwardDerivedLabel2, fundingLabel, new FlatUnivariate (dblForwardDerived2FundingCorr));

		mktParams.setForwardFXCorrSurface (forwardReferenceLabel1, fxLabel, new FlatUnivariate (dblForwardReference1FXCorr));

		mktParams.setForwardFXCorrSurface (forwardReferenceLabel2, fxLabel, new FlatUnivariate (dblForwardReference2FXCorr));

		mktParams.setForwardFXCorrSurface (forwardDerivedLabel1, fxLabel, new FlatUnivariate (dblForwardDerived1FXCorr));

		mktParams.setForwardFXCorrSurface (forwardDerivedLabel2, fxLabel, new FlatUnivariate (dblForwardDerived2FXCorr));

		mktParams.setFundingFXCorrSurface (fundingLabel, fxLabel, new FlatUnivariate (dblFundingFXCorr));
	}

	private static final void VolCorrScenario (
		final ComponentPair[] aCP,
		final ValuationParams valParams,
		final CurveSurfaceQuoteSet mktParams,
		final ForwardLabel forwardReferenceLabel1,
		final ForwardLabel forwardReferenceLabel2,
		final ForwardLabel forwardDerivedLabel1,
		final ForwardLabel forwardDerivedLabel2,
		final FundingLabel fundingLabel,
		final FXLabel fxLabel,
		final double dblForwardReference1Vol,
		final double dblForwardReference2Vol,
		final double dblForwardDerived1Vol,
		final double dblForwardDerived2Vol,
		final double dblFundingVol,
		final double dblFXVol,
		final double dblForwardReference1FundingCorr,
		final double dblForwardReference2FundingCorr,
		final double dblForwardDerived1FundingCorr,
		final double dblForwardDerived2FundingCorr,
		final double dblForwardReference1FXCorr,
		final double dblForwardReference2FXCorr,
		final double dblForwardDerived1FXCorr,
		final double dblForwardDerived2FXCorr,
		final double dblFundingFXCorr)
		throws Exception
	{
		SetMarketParams (
			mktParams,
			forwardReferenceLabel1,
			forwardReferenceLabel2,
			forwardDerivedLabel1,
			forwardDerivedLabel2,
			fundingLabel,
			fxLabel,
			dblForwardReference1Vol,
			dblForwardReference2Vol,
			dblForwardDerived1Vol,
			dblForwardDerived2Vol,
			dblFundingVol,
			dblFXVol,
			dblForwardReference1FundingCorr,
			dblForwardReference2FundingCorr,
			dblForwardDerived1FundingCorr,
			dblForwardDerived2FundingCorr,
			dblForwardReference1FXCorr,
			dblForwardReference2FXCorr,
			dblForwardDerived1FXCorr,
			dblForwardDerived2FXCorr,
			dblFundingFXCorr
		);

		String strDump = "\t[" +
			FormatUtil.FormatDouble (dblForwardReference1Vol, 2, 0, 100.) + "%," +
			FormatUtil.FormatDouble (dblForwardReference2Vol, 2, 0, 100.) + "%," +
			FormatUtil.FormatDouble (dblForwardDerived1Vol, 2, 0, 100.) + "%," +
			FormatUtil.FormatDouble (dblForwardDerived2Vol, 2, 0, 100.) + "%," +
			FormatUtil.FormatDouble (dblFundingVol, 2, 0, 100.) + "%," +
			FormatUtil.FormatDouble (dblFXVol, 2, 0, 100.) + "%," +
			FormatUtil.FormatDouble (dblForwardReference1FundingCorr, 2, 0, 100.) + "%," +
			FormatUtil.FormatDouble (dblForwardReference2FundingCorr, 2, 0, 100.) + "%," +
			FormatUtil.FormatDouble (dblForwardDerived1FundingCorr, 2, 0, 100.) + "%," +
			FormatUtil.FormatDouble (dblForwardDerived2FundingCorr, 2, 0, 100.) + "%," +
			FormatUtil.FormatDouble (dblForwardReference1FXCorr, 2, 0, 100.) + "%," +
			FormatUtil.FormatDouble (dblForwardReference2FXCorr, 2, 0, 100.) + "%," +
			FormatUtil.FormatDouble (dblForwardDerived1FXCorr, 2, 0, 100.) + "%," +
			FormatUtil.FormatDouble (dblForwardDerived2FXCorr, 2, 0, 100.) + "%," +
			FormatUtil.FormatDouble (dblFundingFXCorr, 2, 0, 100.) + "%] = ";

		for (int i = 0; i < aCP.length; ++i) {
			CaseInsensitiveTreeMap<Double> mapOutput = aCP[i].value (valParams, null, mktParams, null);

			if (0 != i) strDump += " || ";

			strDump +=
				FormatUtil.FormatDouble (mapOutput.get ("ReferenceCumulativeConvexityAdjustmentPremium"), 2, 0, 10000.) + " | " +
				FormatUtil.FormatDouble (mapOutput.get ("DerivedCumulativeConvexityAdjustmentPremium"), 2, 0, 10000.) + " | " +
				FormatUtil.FormatDouble (mapOutput.get ("CumulativeConvexityAdjustmentPremium"), 2, 0, 10000.);
		}

		System.out.println (strDump);
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

		/*
		 * Initialize the Credit Analytics Library
		 */

		CreditAnalytics.Init ("");

		JulianDate dtToday = JulianDate.Today();

		ValuationParams valParams = new ValuationParams (dtToday, dtToday, "USD");

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

		double[] adblReference3MForwardVol = new double[] {0.1, 0.4};
		double[] adblReference6MForwardVol = new double[] {0.1, 0.4};
		double[] adblDerived3MForwardVol = new double[] {0.1, 0.4};
		double[] adblDerived6MForwardVol = new double[] {0.1, 0.4};
		double[] adblReferenceFundingVol = new double[] {0.1, 0.4};
		double[] adblReferenceDerivedFXVol = new double[] {0.1, 0.4};

		double[] adblReference3MForwardFundingCorr = new double[] {-0.1, 0.2};
		double[] adblReference6MForwardFundingCorr = new double[] {-0.1, 0.2};
		double[] adblDerived3MForwardFundingCorr = new double[] {-0.1, 0.2};
		double[] adblDerived6MForwardFundingCorr = new double[] {-0.1, 0.2};

		double[] adblReference3MForwardFXCorr = new double[] {-0.1, 0.2};
		double[] adblReference6MForwardFXCorr = new double[] {-0.1, 0.2};
		double[] adblDerived3MForwardFXCorr = new double[] {-0.1, 0.2};
		double[] adblDerived6MForwardFXCorr = new double[] {-0.1, 0.2};

		double[] adblFundingFXCorr = new double[] {-0.1, 0.2};

		for (double dblReference3MForwardVol : adblReference3MForwardVol) {
			for (double dblReference6MForwardVol : adblReference6MForwardVol) {
				for (double dblDerived3MForwardVol : adblDerived3MForwardVol) {
					for (double dblDerived6MForwardVol : adblDerived6MForwardVol) {
						for (double dblReferenceFundingVol : adblReferenceFundingVol) {
							for (double dblReferenceDerivedFXVol : adblReferenceDerivedFXVol) {
								for (double dblReference3MForwardFundingCorr : adblReference3MForwardFundingCorr) {
									for (double dblReference6MForwardFundingCorr : adblReference6MForwardFundingCorr) {
										for (double dblDerived3MForwardFundingCorr : adblDerived3MForwardFundingCorr) {
											for (double dblDerived6MForwardFundingCorr : adblDerived6MForwardFundingCorr) {
												for (double dblReference3MForwardFXCorr : adblReference3MForwardFXCorr) {
													for (double dblReference6MForwardFXCorr : adblReference6MForwardFXCorr) {
														for (double dblDerived3MForwardFXCorr : adblDerived3MForwardFXCorr) {
															for (double dblDerived6MForwardFXCorr : adblDerived6MForwardFXCorr) {
																for (double dblFundingFXCorr : adblFundingFXCorr)
																	VolCorrScenario (
																		new ComponentPair[] {cpMTM, cpNonMTM},
																		valParams,
																		mktParams,
																		fri3MReference,
																		fri6MReference,
																		fri3MDerived,
																		fri6MDerived,
																		fundingLabelReference,
																		fxLabel,
																		dblReference3MForwardVol,
																		dblReference6MForwardVol,
																		dblDerived3MForwardVol,
																		dblDerived6MForwardVol,
																		dblReferenceFundingVol,
																		dblReferenceDerivedFXVol,
																		dblReference3MForwardFundingCorr,
																		dblReference6MForwardFundingCorr,
																		dblDerived3MForwardFundingCorr,
																		dblDerived6MForwardFundingCorr,
																		dblReference3MForwardFXCorr,
																		dblReference6MForwardFXCorr,
																		dblDerived3MForwardFXCorr,
																		dblDerived6MForwardFXCorr,
																		dblFundingFXCorr
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
					}
				}
			}
		}
	}
}
