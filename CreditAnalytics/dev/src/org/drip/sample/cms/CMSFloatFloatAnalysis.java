
package org.drip.sample.cms;

import java.util.List;

import org.drip.analytics.date.*;
import org.drip.analytics.rates.DiscountCurve;
import org.drip.analytics.support.*;
import org.drip.function.deterministic1D.FlatUnivariate;
import org.drip.market.otc.*;
import org.drip.param.creator.ScenarioDiscountCurveBuilder;
import org.drip.param.market.CurveSurfaceQuoteSet;
import org.drip.param.period.*;
import org.drip.param.valuation.*;
import org.drip.product.creator.SingleStreamComponentBuilder;
import org.drip.product.definition.CalibratableFixedIncomeComponent;
import org.drip.product.rates.*;
import org.drip.quant.common.FormatUtil;
import org.drip.service.api.CreditAnalytics;
import org.drip.state.identifier.*;

/*
 * -*- mode: java; tab-width: 4; indent-tabs-mode: nil; c-basic-offset: 4 -*-
 */

/*!
 * Copyright (C) 2015 Lakshmi Krishnamurthy
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
 * CMSFloatFloatAnalysis demonstrates the Construction and Valuation Impact of Volatility and Correlation on
 * 	the CMS Float-Float Swap.
 * 
 * @author Lakshmi Krishnamurthy
 */

public class CMSFloatFloatAnalysis {

	private static final FixFloatComponent OTCIRS (
		final JulianDate dtSpot,
		final String strCurrency,
		final String strMaturityTenor,
		final double dblCoupon)
	{
		FixedFloatSwapConvention ffConv = IBORFixedFloatContainer.ConventionFromJurisdiction (
			strCurrency,
			"ALL",
			strMaturityTenor,
			"MAIN"
		);

		return ffConv.createFixFloatComponent (
			dtSpot,
			strMaturityTenor,
			dblCoupon,
			0.,
			1.
		);
	}

	private static final CalibratableFixedIncomeComponent[] DepositInstrumentsFromMaturityDays (
		final JulianDate dtEffective,
		final int[] aiDay,
		final int iNumFutures,
		final String strCurrency)
		throws Exception
	{
		CalibratableFixedIncomeComponent[] aCalibComp = new CalibratableFixedIncomeComponent[aiDay.length + iNumFutures];

		for (int i = 0; i < aiDay.length; ++i)
			aCalibComp[i] = SingleStreamComponentBuilder.Deposit (
				dtEffective,
				dtEffective.addBusDays (aiDay[i], strCurrency),
				ForwardLabel.Create (strCurrency, "3M")
			);

		CalibratableFixedIncomeComponent[] aEDF = SingleStreamComponentBuilder.FuturesPack (
			dtEffective,
			iNumFutures,
			strCurrency
		);

		for (int i = aiDay.length; i < aiDay.length + iNumFutures; ++i)
			aCalibComp[i] = aEDF[i - aiDay.length];

		return aCalibComp;
	}

	private static final FixFloatComponent[] SwapInstrumentsFromMaturityTenor (
		final JulianDate dtSpot,
		final String strCurrency,
		final String[] astrMaturityTenor,
		final double[] adblCoupon)
		throws Exception
	{
		FixFloatComponent[] aIRS = new FixFloatComponent[astrMaturityTenor.length];

		for (int i = 0; i < astrMaturityTenor.length; ++i) {
			FixFloatComponent irs = OTCIRS (
				dtSpot,
				strCurrency,
				astrMaturityTenor[i],
				adblCoupon[i]
			);

			irs.setPrimaryCode ("IRS." + astrMaturityTenor[i] + "." + strCurrency);

			aIRS[i] = irs;
		}

		return aIRS;
	}

	private static final DiscountCurve MakeDC (
		final JulianDate dtSpot,
		final String strCurrency)
		throws Exception
	{

		CalibratableFixedIncomeComponent[] aDepositComp = DepositInstrumentsFromMaturityDays (
			dtSpot,
			new int[] {
				1, 2, 3, 7, 14, 21, 30, 60
			},
			0,
			strCurrency
		);

		double[] adblDepositQuote = new double[] {
			0.01200, 0.01200, 0.01200, 0.01450, 0.01550, 0.01600, 0.01660, 0.01850
		};

		String[] astrDepositManifestMeasure = new String[] {
			"ForwardRate", "ForwardRate", "ForwardRate", "ForwardRate", "ForwardRate", "ForwardRate", "ForwardRate", "ForwardRate"
		};

		double[] adblSwapQuote = new double[] {
			0.02604,    //  4Y
			0.02808,    //  5Y
			0.02983,    //  6Y
			0.03136,    //  7Y
			0.03268,    //  8Y
			0.03383,    //  9Y
			0.03488,    // 10Y
			0.03583,    // 11Y
			0.03668,    // 12Y
			0.03833,    // 15Y
			0.03854,    // 20Y
			0.03672,    // 25Y
			0.03510,    // 30Y
			0.03266,    // 40Y
			0.03145     // 50Y
		};

		String[] astrSwapManifestMeasure = new String[] {
			"SwapRate",    //  4Y
			"SwapRate",    //  5Y
			"SwapRate",    //  6Y
			"SwapRate",    //  7Y
			"SwapRate",    //  8Y
			"SwapRate",    //  9Y
			"SwapRate",    // 10Y
			"SwapRate",    // 11Y
			"SwapRate",    // 12Y
			"SwapRate",    // 15Y
			"SwapRate",    // 20Y
			"SwapRate",    // 25Y
			"SwapRate",    // 30Y
			"SwapRate",    // 40Y
			"SwapRate"     // 50Y
		};

		CalibratableFixedIncomeComponent[] aSwapComp = SwapInstrumentsFromMaturityTenor (
			dtSpot,
			strCurrency,
			new java.lang.String[] {
				"4Y", "5Y", "6Y", "7Y", "8Y", "9Y", "10Y", "11Y", "12Y", "15Y", "20Y", "25Y", "30Y", "40Y", "50Y"
			},
			adblSwapQuote
		);

		return ScenarioDiscountCurveBuilder.CubicKLKHyperbolicDFRateShapePreserver (
			"KLK_HYPERBOLIC_SHAPE_TEMPLATE",
			new ValuationParams (
				dtSpot,
				dtSpot,
				strCurrency
			),
			aDepositComp,
			adblDepositQuote,
			astrDepositManifestMeasure,
			aSwapComp,
			adblSwapQuote,
			astrSwapManifestMeasure,
			true
		);
	}

	private static final FloatFloatComponent MakeFloatFloatSwap (
		final JulianDate dtEffective,
		final String strCurrency,
		final ForwardLabel forwardLabel,
		final String strMaturityTenor,
		final boolean bInArrears)
		throws Exception
	{
		ComposableFloatingUnitSetting cfusReference = new ComposableFloatingUnitSetting (
			"3M",
			CompositePeriodBuilder.EDGE_DATE_SEQUENCE_REGULAR,
			null,
			ForwardLabel.Create (
				strCurrency,
				"3M"
			),
			CompositePeriodBuilder.REFERENCE_PERIOD_IN_ADVANCE,
			0.
		);

		ComposableFloatingUnitSetting cfusDerived = new ComposableFloatingUnitSetting (
			"3M",
			CompositePeriodBuilder.EDGE_DATE_SEQUENCE_REGULAR,
			null,
			forwardLabel,
			bInArrears ? CompositePeriodBuilder.REFERENCE_PERIOD_IN_ARREARS : CompositePeriodBuilder.REFERENCE_PERIOD_IN_ADVANCE,
			0.
		);

		CompositePeriodSetting cpsReference = new CompositePeriodSetting (
			4,
			"3M",
			strCurrency,
			null,
			1.,
			null,
			null,
			null,
			null
		);

		CompositePeriodSetting cpsDerived = new CompositePeriodSetting (
			4,
			"3M",
			strCurrency,
			null,
			-1.,
			null,
			null,
			null,
			null
		);

		List<Double> lsReferenceStreamEdgeDate = CompositePeriodBuilder.RegularEdgeDates (
			dtEffective,
			"3M",
			strMaturityTenor,
			null
		);

		List<Double> lsDerivedStreamEdgeDate = CompositePeriodBuilder.RegularEdgeDates (
			dtEffective,
			"3M",
			strMaturityTenor,
			null
		);

		Stream referenceStream = new Stream (
			CompositePeriodBuilder.FloatingCompositeUnit (
				lsReferenceStreamEdgeDate,
				cpsReference,
				cfusReference
			)
		);

		Stream derivedStream = new Stream (
			CompositePeriodBuilder.FloatingCompositeUnit (
				lsDerivedStreamEdgeDate,
				cpsDerived,
				cfusDerived
			)
		);

		FloatFloatComponent floatFloat = new FloatFloatComponent (
			referenceStream,
			derivedStream,
			new CashSettleParams (0, strCurrency, 0)
		);

		return floatFloat;
	}

	private static final void SetMarketParams (
		final CurveSurfaceQuoteSet mktParams,
		final ForwardLabel forwardLabel,
		final FundingLabel fundingLabel,
		final double dblFundingVol,
		final double dblForwardVol,
		final double dblForwardFundingCorr)
		throws Exception
	{
		mktParams.setForwardCurveVolSurface (
			forwardLabel,
			new FlatUnivariate (dblForwardVol)
		);

		mktParams.setFundingCurveVolSurface (
			fundingLabel,
			new FlatUnivariate (dblFundingVol)
		);

		mktParams.setForwardFundingCorrSurface (
			forwardLabel,
			fundingLabel,
			new FlatUnivariate (dblForwardFundingCorr)
		);
	}

	private static final void VolCorrScenario (
		final FloatFloatComponent[] aCMSFloatFloat,
		final ValuationParams valParams,
		final CurveSurfaceQuoteSet mktParams,
		final ForwardLabel forwardLabel,
		final FundingLabel fundingLabel,
		final double dblForwardVol,
		final double dblFundingVol,
		final double dblForwardFundingCorr,
		final double dblBaseReferenceParBasisSpread)
		throws Exception
	{
		SetMarketParams (
			mktParams,
			forwardLabel,
			fundingLabel,
			dblForwardVol,
			dblFundingVol,
			dblForwardFundingCorr
		);

		String strDump = "\t[" +
			FormatUtil.FormatDouble (dblForwardVol, 2, 0, 100.) + "%," +
			FormatUtil.FormatDouble (dblFundingVol, 2, 0, 100.) + "%," +
			FormatUtil.FormatDouble (dblForwardFundingCorr, 2, 0, 100.) + "%] = ";

		for (int i = 0; i < aCMSFloatFloat.length; ++i) {
			CaseInsensitiveTreeMap<Double> mapOutput = aCMSFloatFloat[i].value (
				valParams,
				null,
				mktParams,
				null
			);

			if (0 != i) strDump += " || ";

			double dblReferenceParBasisSpread = mapOutput.get ("ReferenceParBasisSpread");

			strDump +=
				FormatUtil.FormatDouble (dblReferenceParBasisSpread, 2, 1, 1.) + " | " +
				FormatUtil.FormatDouble (dblReferenceParBasisSpread - dblBaseReferenceParBasisSpread, 2, 1, 1.);
		}

		System.out.println (strDump + "  |");
	}

	public static final void main (
		final String[] astrArgs)
		throws Exception
	{
		CreditAnalytics.Init ("");

		String strTenor = "6M";
		String strCurrency = "USD";
		String strMaturityTenor = "5Y";

		JulianDate dtSpot = DateUtil.CreateFromYMD (
			2012,
			DateUtil.DECEMBER,
			11
		);

		DiscountCurve dc = MakeDC (
			dtSpot,
			strCurrency
		);

		ForwardLabel forwardLabel = ForwardLabel.Create (
			strCurrency,
			strTenor
		);

		CurveSurfaceQuoteSet mktParams = new CurveSurfaceQuoteSet();

		mktParams.setFundingCurve (dc);

		FloatFloatComponent cmsInAdvance = MakeFloatFloatSwap (
			dtSpot,
			strCurrency,
			forwardLabel,
			strMaturityTenor,
			false
		);

		FloatFloatComponent cmsInArrears = MakeFloatFloatSwap (
			dtSpot,
			strCurrency,
			forwardLabel,
			strMaturityTenor,
			true
		);

		ValuationParams valParams = new ValuationParams (
			dtSpot,
			dtSpot,
			strCurrency
		);

		double dblBaseReferenceParBasisSpread = cmsInAdvance.value (
			valParams,
			null,
			mktParams,
			null
		).get ("ReferenceParBasisSpread");

		double[] adblForwardVol = new double[] {0.10, 0.30, 0.50};

		double[] adblFundingVol = new double[] {0.10, 0.30, 0.50};

		double[] adblForwardFundingCorr = new double[] {-0.10, 0.25};

		System.out.println ("\n\n\t|--------------------------------------------------|");

		System.out.println ("\t| CMS FLOAT-FLOAT IN-ADVANCE & IN-ARREARS ANALYSIS |");

		System.out.println ("\t|--------------------------------------------------|");

		System.out.println ("\t| INPUTS: L -> R                                   |");

		System.out.println ("\t|                                                  |");

		System.out.println ("\t|   Forward State Volatility                       |");

		System.out.println ("\t|   Funding State Volatility                       |");

		System.out.println ("\t|   Forward-Funding Correlation                    |");

		System.out.println ("\t|                                                  |");

		System.out.println ("\t|--------------------------------------------------|");

		System.out.println ("\t| OUTPUTS: L -> R                                  |");

		System.out.println ("\t|                                                  |");

		System.out.println ("\t|   In Advance Reference Par Basis Spread          |");

		System.out.println ("\t|   In Advance Reference Par Basis Spread Change   |");

		System.out.println ("\t|   In Arrears Reference Par Basis Spread          |");

		System.out.println ("\t|   In Arrears Reference Par Basis Spread Change   |");

		System.out.println ("\t|                                                  |");

		System.out.println ("\t|--------------------------------------------------|");

		for (double dblForwardVol : adblForwardVol) {
			for (double dblFundingVol : adblFundingVol) {
				for (double dblForwardFundingCorr : adblForwardFundingCorr) {
					VolCorrScenario (
						new FloatFloatComponent[] {cmsInAdvance, cmsInArrears},
						valParams,
						mktParams,
						forwardLabel,
						FundingLabel.Standard (strCurrency),
						dblForwardVol,
						dblFundingVol,
						dblForwardFundingCorr,
						dblBaseReferenceParBasisSpread
					);
				}
			}
		}

		System.out.println ("\t|--------------------------------------------------|");
	}
}
