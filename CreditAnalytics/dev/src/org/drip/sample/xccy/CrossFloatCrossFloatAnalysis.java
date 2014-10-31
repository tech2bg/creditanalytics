
package org.drip.sample.xccy;

import java.util.List;

import org.drip.analytics.date.JulianDate;
import org.drip.analytics.rates.*;
import org.drip.analytics.support.*;
import org.drip.param.creator.ScenarioForwardCurveBuilder;
import org.drip.param.market.CurveSurfaceQuoteSet;
import org.drip.param.period.ComposableFloatingUnitSetting;
import org.drip.param.period.CompositePeriodSetting;
import org.drip.param.period.FixingSetting;
import org.drip.param.period.UnitCouponAccrualSetting;
import org.drip.param.valuation.*;
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
 * FloatFloatMTMVolAnalysis demonstrates the impact of Funding Volatility, Forward Volatility, and
 *  Funding/Forward, Funding/FX, and Forward/FX Correlation for each of the FRI's on the Valuation of a
 *  float-float swap with a 3M EUR Floater leg that pays in USD, and a 6M EUR Floater leg that pays in USD.
 *  Comparison is done across MTM and non-MTM fixed Leg Counterparts.
 * 
 * @author Lakshmi Krishnamurthy
 */

public class CrossFloatCrossFloatAnalysis {

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
		UnitCouponAccrualSetting ucasReference = new UnitCouponAccrualSetting (
			12 / iTenorInMonthsReference,
			"Act/360",
			false,
			"Act/360",
			false,
			strCouponCurrency,
			false
		);

		UnitCouponAccrualSetting ucasDerived = new UnitCouponAccrualSetting (
			12 / iTenorInMonthsDerived,
			"Act/360",
			false,
			"Act/360",
			false,
			strCouponCurrency,
			false
		);

		ComposableFloatingUnitSetting cfusReference = new ComposableFloatingUnitSetting (
			iTenorInMonthsReference + "M",
			CompositePeriodBuilder.EDGE_DATE_SEQUENCE_SINGLE,
			null,
			ForwardLabel.Standard (strCouponCurrency + "-LIBOR-" + iTenorInMonthsReference + "M"),
			CompositePeriodBuilder.REFERENCE_PERIOD_IN_ADVANCE,
			null,
			0.
		);

		ComposableFloatingUnitSetting cfusDerived = new ComposableFloatingUnitSetting (
			iTenorInMonthsDerived + "M",
			CompositePeriodBuilder.EDGE_DATE_SEQUENCE_SINGLE,
			null,
			ForwardLabel.Standard (strCouponCurrency + "-LIBOR-" + iTenorInMonthsDerived + "M"),
			CompositePeriodBuilder.REFERENCE_PERIOD_IN_ADVANCE,
			null,
			0.
		);

		CompositePeriodSetting cpsReference = new CompositePeriodSetting (
			12 / iTenorInMonthsReference,
			iTenorInMonthsReference + "M",
			strPayCurrency,
			null,
			CompositePeriodBuilder.ACCRUAL_COMPOUNDING_RULE_GEOMETRIC,
			-1.,
			null,
			null,
			bFXMTM ? null : new FixingSetting (
				FixingSetting.FIXING_PRESET_STATIC,
				null,
				dtEffective.julian()
			),
			null
		);

		CompositePeriodSetting cpsDerived = new CompositePeriodSetting (
			12 / iTenorInMonthsDerived,
			iTenorInMonthsDerived + "M",
			strPayCurrency,
			null,
			CompositePeriodBuilder.ACCRUAL_COMPOUNDING_RULE_GEOMETRIC,
			1.,
			null,
			null,
			bFXMTM ? null : new FixingSetting (
				FixingSetting.FIXING_PRESET_STATIC,
				null,
				dtEffective.julian()
			),
			null
		);

		List<Double> lsReferenceStreamEdgeDate = CompositePeriodBuilder.RegularEdgeDates (
			dtEffective,
			iTenorInMonthsReference + "M",
			strMaturityTenor,
			null
		);

		List<Double> lsDerivedStreamEdgeDate = CompositePeriodBuilder.RegularEdgeDates (
			dtEffective,
			iTenorInMonthsDerived + "M",
			strMaturityTenor,
			null
		);

		Stream referenceStream = new Stream (
			CompositePeriodBuilder.FloatingCompositeUnit (
				lsReferenceStreamEdgeDate,
				cpsReference,
				ucasReference,
				cfusReference
			)
		);

		Stream derivedStream = new Stream (
			CompositePeriodBuilder.FloatingCompositeUnit (
				lsDerivedStreamEdgeDate,
				cpsDerived,
				ucasDerived,
				cfusDerived
			)
		);

		CashSettleParams csp = new CashSettleParams (
			0,
			strPayCurrency,
			0
		);

		return new FloatFloatComponent (
			referenceStream,
			derivedStream,
			csp
		);
	}

	private static final void SetMarketParams (
		final CurveSurfaceQuoteSet mktParams,
		final ForwardLabel forwardLabel1,
		final ForwardLabel forwardLabel2,
		final FundingLabel fundingLabel,
		final FXLabel fxLabel,
		final double dblForward1Vol,
		final double dblForward2Vol,
		final double dblFundingVol,
		final double dblFXVol,
		final double dblForward1FundingCorr,
		final double dblForward2FundingCorr,
		final double dblForward1FXCorr,
		final double dblForward2FXCorr,
		final double dblFundingFXCorr)
		throws Exception
	{
		mktParams.setForwardCurveVolSurface (forwardLabel1, new FlatUnivariate (dblForward1Vol));

		mktParams.setForwardCurveVolSurface (forwardLabel2, new FlatUnivariate (dblForward2Vol));

		mktParams.setFundingCurveVolSurface (fundingLabel, new FlatUnivariate (dblFundingVol));

		mktParams.setFXCurveVolSurface (fxLabel, new FlatUnivariate (dblFXVol));

		mktParams.setForwardFundingCorrSurface (forwardLabel1, fundingLabel, new FlatUnivariate (dblForward1FundingCorr));

		mktParams.setForwardFundingCorrSurface (forwardLabel2, fundingLabel, new FlatUnivariate (dblForward2FundingCorr));

		mktParams.setForwardFXCorrSurface (forwardLabel1, fxLabel, new FlatUnivariate (dblForward1FXCorr));

		mktParams.setForwardFXCorrSurface (forwardLabel2, fxLabel, new FlatUnivariate (dblForward2FXCorr));

		mktParams.setFundingFXCorrSurface (fundingLabel, fxLabel, new FlatUnivariate (dblFundingFXCorr));
	}

	private static final void VolCorrScenario (
		final FloatFloatComponent[] aFloatFloat,
		final ValuationParams valParams,
		final CurveSurfaceQuoteSet mktParams,
		final ForwardLabel forwardLabel1,
		final ForwardLabel forwardLabel2,
		final FundingLabel fundingLabel,
		final FXLabel fxLabel,
		final double dblForward1Vol,
		final double dblForward2Vol,
		final double dblFundingVol,
		final double dblFXVol,
		final double dblForward1FundingCorr,
		final double dblForward2FundingCorr,
		final double dblForward1FXCorr,
		final double dblForward2FXCorr,
		final double dblFundingFXCorr)
		throws Exception
	{
		SetMarketParams (
			mktParams,
			forwardLabel1,
			forwardLabel2,
			fundingLabel,
			fxLabel,
			dblForward1Vol,
			dblForward2Vol,
			dblFundingVol,
			dblFXVol,
			dblForward1FundingCorr,
			dblForward2FundingCorr,
			dblForward1FXCorr,
			dblForward2FXCorr,
			dblFundingFXCorr
		);

		String strDump = "\t[" +
			FormatUtil.FormatDouble (dblForward1Vol, 2, 0, 100.) + "%," +
			FormatUtil.FormatDouble (dblForward2Vol, 2, 0, 100.) + "%," +
			FormatUtil.FormatDouble (dblFundingVol, 2, 0, 100.) + "%," +
			FormatUtil.FormatDouble (dblFXVol, 2, 0, 100.) + "%," +
			FormatUtil.FormatDouble (dblForward1FundingCorr, 2, 0, 100.) + "%," +
			FormatUtil.FormatDouble (dblForward2FundingCorr, 2, 0, 100.) + "%," +
			FormatUtil.FormatDouble (dblForward1FXCorr, 2, 0, 100.) + "%," +
			FormatUtil.FormatDouble (dblForward2FXCorr, 2, 0, 100.) + "%," +
			FormatUtil.FormatDouble (dblFundingFXCorr, 2, 0, 100.) + "%] = ";

		for (int i = 0; i < aFloatFloat.length; ++i) {
			CaseInsensitiveTreeMap<Double> mapOutput = aFloatFloat[i].value (valParams, null, mktParams, null);

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
		double dblEUR3MForwardRate = 0.02;
		double dblEUR6MForwardRate = 0.025;
		double dblUSDFundingRate = 0.02;
		double dblUSDEURFXRate = 1. / 1.35;

		/*
		 * Initialize the Credit Analytics Library
		 */

		CreditAnalytics.Init ("");

		JulianDate dtToday = JulianDate.Today();

		ValuationParams valParams = new ValuationParams (dtToday, dtToday, "EUR");

		DiscountCurve dcUSDFunding = DiscountCurveBuilder.CreateFromFlatRate (
			dtToday,
			"USD",
			new CollateralizationParams ("OVERNIGHT_INDEX", "USD"),
			dblUSDFundingRate
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
			true,
			"USD",
			"EUR",
			"2Y",
			6,
			3
		);

		floatFloatMTM.setPrimaryCode ("EUR__USD__MTM::FLOAT::3M::6M::2Y");

		FloatFloatComponent floatFloatNonMTM = MakeFloatFloatSwap (
			dtToday,
			false,
			"USD",
			"EUR",
			"2Y",
			6,
			3
		);

		floatFloatNonMTM.setPrimaryCode ("EUR__USD__NONMTM::FLOAT::3M::6M::2Y");

		FXLabel fxLabel = FXLabel.Standard (cp);

		CurveSurfaceQuoteSet mktParams = new CurveSurfaceQuoteSet();

		mktParams.setFixing (
			dtToday,
			fxLabel,
			dblUSDEURFXRate
		);

		mktParams.setForwardCurve (fcEUR3M);

		mktParams.setForwardCurve (fcEUR6M);

		mktParams.setFundingCurve (dcUSDFunding);

		mktParams.setFXCurve (fxLabel, new FlatUnivariate (dblUSDEURFXRate));

		double[] adblEURForward3MVol = new double[] {0.1, 0.3, 0.5};

		double[] adblEURForward6MVol = new double[] {0.1, 0.3, 0.5};

		double[] adblUSDFundingVol = new double[] {0.1, 0.3, 0.5};

		double[] adblUSDEURFXVol = new double[] {0.1, 0.3, 0.5};

		double[] adblEUR3MUSDFundingCorr = new double[] {-0.2, 0.25};

		double[] adblEUR6MUSDFundingCorr = new double[] {-0.2, 0.25};

		double[] adblEUR3MUSDEURFXCorr = new double[] {-0.2, 0.25};

		double[] adblEUR6MUSDEURFXCorr = new double[] {-0.2, 0.25};

		double[] adblUSDFundingUSDEURFXCorr = new double[] {-0.2, 0.25};

		for (double dblEURForward3MVol : adblEURForward3MVol) {
			for (double dblEURForward6MVol : adblEURForward6MVol) {
				for (double dblUSDFundingVol : adblUSDFundingVol) {
					for (double dblUSDEURFXVol : adblUSDEURFXVol) {
						for (double dblEUR3MUSDFundingCorr : adblEUR3MUSDFundingCorr) {
							for (double dblEUR6MUSDFundingCorr : adblEUR6MUSDFundingCorr) {
								for (double dblEUR3MUSDEURFXCorr : adblEUR3MUSDEURFXCorr) {
									for (double dblEUR6MUSDEURFXCorr : adblEUR6MUSDEURFXCorr) {
										for (double dblUSDFundingUSDEURFXCorr : adblUSDFundingUSDEURFXCorr)
											VolCorrScenario (
												new FloatFloatComponent[] {floatFloatMTM, floatFloatNonMTM},
												valParams,
												mktParams,
												friEUR3M,
												friEUR6M,
												FundingLabel.Standard ("USD"),
												fxLabel,
												dblEURForward3MVol,
												dblEURForward6MVol,
												dblUSDFundingVol,
												dblUSDEURFXVol,
												dblEUR3MUSDFundingCorr,
												dblEUR6MUSDFundingCorr,
												dblEUR3MUSDEURFXCorr,
												dblEUR6MUSDEURFXCorr,
												dblUSDFundingUSDEURFXCorr
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
