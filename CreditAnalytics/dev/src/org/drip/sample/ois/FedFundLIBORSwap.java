
package org.drip.sample.ois;

import java.util.*;

import org.drip.analytics.date.DateUtil;
import org.drip.analytics.date.JulianDate;
import org.drip.analytics.definition.LatentStateStatic;
import org.drip.analytics.rates.*;
import org.drip.analytics.support.*;
import org.drip.param.creator.*;
import org.drip.param.market.CurveSurfaceQuoteSet;
import org.drip.param.period.*;
import org.drip.param.valuation.*;
import org.drip.product.calib.*;
import org.drip.product.creator.SingleStreamComponentBuilder;
import org.drip.product.rates.*;
import org.drip.quant.common.FormatUtil;
import org.drip.quant.function1D.QuadraticRationalShapeControl;
import org.drip.service.api.CreditAnalytics;
import org.drip.spline.basis.PolynomialFunctionSetParams;
import org.drip.spline.params.*;
import org.drip.spline.stretch.*;
import org.drip.state.identifier.*;
import org.drip.state.inference.*;
import org.drip.state.representation.LatentStateSpecification;

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
 * FedFundLIBORSwap demonstrates the construction, the valuation, and Bloomberg analysis metrics for Fed Fund
 * 	vs. LIBOR Basis Swaps.
 * 
 * @author Lakshmi Krishnamurthy
 */

public class FedFundLIBORSwap {

	/*
	 * Construct the Array of Deposit Instruments from the given set of parameters
	 * 
	 *  	USE WITH CARE: This sample ignores errors and does not handle exceptions.
	 */

	private static final SingleStreamComponent[] DepositInstrumentsFromMaturityDays (
		final JulianDate dtEffective,
		final String strCurrency,
		final int[] aiDay)
		throws Exception
	{
		SingleStreamComponent[] aDeposit = new SingleStreamComponent[aiDay.length];

		for (int i = 0; i < aiDay.length; ++i)
			aDeposit[i] = SingleStreamComponentBuilder.Deposit (
				dtEffective,
				dtEffective.addBusDays (
					aiDay[i],
					strCurrency
				),
				ForwardLabel.Create (strCurrency, "ON")
			);

		return aDeposit;
	}

	private static final LatentStateStretchSpec DepositStretch (
		final SingleStreamComponent[] aDeposit,
		final double[] adblQuote)
		throws Exception
	{
		LatentStateSegmentSpec[] aSegmentSpec = new LatentStateSegmentSpec[aDeposit.length];

		String strCurrency = aDeposit[0].payCurrency();

		for (int i = 0; i < aDeposit.length; ++i) {
			FloatingStreamQuoteSet depositQuote = new FloatingStreamQuoteSet (
				new LatentStateSpecification[] {
					new LatentStateSpecification (
						LatentStateStatic.LATENT_STATE_FUNDING,
						LatentStateStatic.DISCOUNT_QM_DISCOUNT_FACTOR,
						FundingLabel.Standard (strCurrency)
					),
					new LatentStateSpecification (
						LatentStateStatic.LATENT_STATE_FORWARD,
						LatentStateStatic.FORWARD_QM_FORWARD_RATE,
						aDeposit[i].forwardLabel().get ("DERIVED")
					)
				}
			);

			depositQuote.set ("ForwardRate", adblQuote[i]);

			aSegmentSpec[i] = new LatentStateSegmentSpec (
				aDeposit[i],
				depositQuote
			);
		}

		return new LatentStateStretchSpec (
			"DEPOSIT",
			aSegmentSpec
		);
	}

	/*
	 * Construct the Array of Overnight Index Instruments from the given set of parameters
	 * 
	 *  	USE WITH CARE: This sample ignores errors and does not handle exceptions.
	 */

	private static final FixFloatComponent[] OvernightIndexFromMaturityTenor (
		final JulianDate dtEffective,
		final String[] astrMaturityTenor,
		final double[] adblCoupon,
		final String strCurrency)
		throws Exception
	{
		FixFloatComponent[] aOIS = new FixFloatComponent[astrMaturityTenor.length];

		UnitCouponAccrualSetting ucasFixed = new UnitCouponAccrualSetting (
			2,
			"Act/360",
			false,
			"Act/360",
			false,
			strCurrency,
			false,
			CompositePeriodBuilder.ACCRUAL_COMPOUNDING_RULE_GEOMETRIC
		);

		CashSettleParams csp = new CashSettleParams (
			0,
			strCurrency,
			0
		);

		for (int i = 0; i < astrMaturityTenor.length; ++i) {
			java.lang.String strFixedTenor = AnalyticsHelper.LEFT_TENOR_LESSER == AnalyticsHelper.TenorCompare (
				astrMaturityTenor[i],
				"6M"
			) ? astrMaturityTenor[i] : "6M";

			java.lang.String strFloatingTenor = AnalyticsHelper.LEFT_TENOR_LESSER == AnalyticsHelper.TenorCompare (
				astrMaturityTenor[i],
				"3M"
			) ? astrMaturityTenor[i] : "3M";

			ComposableFloatingUnitSetting cfusFloating = new ComposableFloatingUnitSetting (
				"ON",
				CompositePeriodBuilder.EDGE_DATE_SEQUENCE_OVERNIGHT,
				null,
				ForwardLabel.Create (strCurrency, "ON"),
				CompositePeriodBuilder.REFERENCE_PERIOD_IN_ADVANCE,
				0.
			);

			ComposableFixedUnitSetting cfusFixed = new ComposableFixedUnitSetting (
				strFixedTenor,
				CompositePeriodBuilder.EDGE_DATE_SEQUENCE_REGULAR,
				null,
				adblCoupon[i],
				0.,
				strCurrency
			);

			CompositePeriodSetting cpsFloating = new CompositePeriodSetting (
				4,
				strFloatingTenor,
				strCurrency,
				null,
				-1.,
				null,
				null,
				null,
				null
			);

			CompositePeriodSetting cpsFixed = new CompositePeriodSetting (
				2,
				strFixedTenor,
				strCurrency,
				null,
				1.,
				null,
				null,
				null,
				null
			);

			List<Double> lsFixedStreamEdgeDate = CompositePeriodBuilder.RegularEdgeDates (
				dtEffective,
				strFixedTenor,
				astrMaturityTenor[i],
				null
			);

			List<Double> lsFloatingStreamEdgeDate = CompositePeriodBuilder.RegularEdgeDates (
				dtEffective,
				strFloatingTenor,
				astrMaturityTenor[i],
				null
			);

			Stream floatingStream = new Stream (
				CompositePeriodBuilder.FloatingCompositeUnit (
					lsFloatingStreamEdgeDate,
					cpsFloating,
					cfusFloating
				)
			);

			Stream fixedStream = new Stream (
				CompositePeriodBuilder.FixedCompositeUnit (
					lsFixedStreamEdgeDate,
					cpsFixed,
					ucasFixed,
					cfusFixed
				)
			);

			FixFloatComponent ois = new FixFloatComponent (
				fixedStream,
				floatingStream,
				csp
			);

			ois.setPrimaryCode ("OIS." + astrMaturityTenor[i] + "." + strCurrency);

			aOIS[i] = ois;
		}

		return aOIS;
	}

	/*
	 * Construct the Array of Overnight Index Future Instruments from the given set of parameters
	 * 
	 *  	USE WITH CARE: This sample ignores errors and does not handle exceptions.
	 */

	private static final FixFloatComponent[] OvernightIndexFutureFromMaturityTenor (
		final JulianDate dtSpot,
		final String[] astrStartTenor,
		final String[] astrMaturityTenor,
		final double[] adblCoupon,
		final String strCurrency)
		throws Exception
	{
		FixFloatComponent[] aOIS = new FixFloatComponent[astrStartTenor.length];

		CashSettleParams csp = new CashSettleParams (
			0,
			strCurrency,
			0
		);

		for (int i = 0; i < astrStartTenor.length; ++i) {
			JulianDate dtEffective = dtSpot.addTenor (astrStartTenor[i]);

			java.lang.String strFixedTenor = AnalyticsHelper.LEFT_TENOR_LESSER == AnalyticsHelper.TenorCompare (
				astrMaturityTenor[i],
				"6M"
			) ? astrMaturityTenor[i] : "6M";

			java.lang.String strFloatingTenor = AnalyticsHelper.LEFT_TENOR_LESSER == AnalyticsHelper.TenorCompare (
				astrMaturityTenor[i],
				"3M"
			) ? astrMaturityTenor[i] : "3M";

			ComposableFixedUnitSetting cfusFixed = new ComposableFixedUnitSetting (
				strFixedTenor,
				CompositePeriodBuilder.EDGE_DATE_SEQUENCE_REGULAR,
				null,
				adblCoupon[i],
				0.,
				strCurrency
			);

			UnitCouponAccrualSetting ucasFixed = new UnitCouponAccrualSetting (
				2,
				"Act/360",
				false,
				"Act/360",
				false,
				strCurrency,
				false,
				CompositePeriodBuilder.ACCRUAL_COMPOUNDING_RULE_GEOMETRIC
			);

			ComposableFloatingUnitSetting cfusFloating = new ComposableFloatingUnitSetting (
				"ON",
				CompositePeriodBuilder.EDGE_DATE_SEQUENCE_OVERNIGHT,
				null,
				ForwardLabel.Create (strCurrency, "ON"),
				CompositePeriodBuilder.REFERENCE_PERIOD_IN_ADVANCE,
				0.
			);

			CompositePeriodSetting cpsFloating = new CompositePeriodSetting (
				4,
				strFloatingTenor,
				strCurrency,
				null,
				-1.,
				null,
				null,
				null,
				null
			);

			CompositePeriodSetting cpsFixed = new CompositePeriodSetting (
				2,
				strFixedTenor,
				strCurrency,
				null,
				1.,
				null,
				null,
				null,
				null
			);

			List<Double> lsFixedStreamEdgeDate = CompositePeriodBuilder.RegularEdgeDates (
				dtEffective,
				"6M",
				astrMaturityTenor[i],
				null
			);

			List<Double> lsFloatingStreamEdgeDate = CompositePeriodBuilder.RegularEdgeDates (
				dtEffective,
				"3M",
				astrMaturityTenor[i],
				null
			);

			Stream floatingStream = new Stream (
				CompositePeriodBuilder.FloatingCompositeUnit (
					lsFloatingStreamEdgeDate,
					cpsFloating,
					cfusFloating
				)
			);

			Stream fixedStream = new Stream (
				CompositePeriodBuilder.FixedCompositeUnit (
					lsFixedStreamEdgeDate,
					cpsFixed,
					ucasFixed,
					cfusFixed
				)
			);

			FixFloatComponent ois = new FixFloatComponent (
				fixedStream,
				floatingStream,
				csp
			);

			ois.setPrimaryCode ("OIS." + astrMaturityTenor[i] + "." + strCurrency);

			aOIS[i] = ois;
		}

		return aOIS;
	}

	private static final LatentStateStretchSpec OISStretch (
		final String strName,
		final FixFloatComponent[] aOIS,
		final double[] adblQuote)
		throws Exception
	{
		LatentStateSegmentSpec[] aSegmentSpec = new LatentStateSegmentSpec[aOIS.length];

		String strCurrency = aOIS[0].payCurrency();

		for (int i = 0; i < aOIS.length; ++i) {
			FixFloatQuoteSet oisQuote = new FixFloatQuoteSet (
				new LatentStateSpecification[] {
					new LatentStateSpecification (
						LatentStateStatic.LATENT_STATE_FUNDING,
						LatentStateStatic.DISCOUNT_QM_DISCOUNT_FACTOR,
						FundingLabel.Standard (strCurrency)
					),
					new LatentStateSpecification (
						LatentStateStatic.LATENT_STATE_FORWARD,
						LatentStateStatic.FORWARD_QM_FORWARD_RATE,
						aOIS[i].forwardLabel().get ("DERIVED")
					)
				}
			);

			oisQuote.setSwapRate (adblQuote[i]);

			aSegmentSpec[i] = new LatentStateSegmentSpec (
				aOIS[i],
				oisQuote
			);
		}

		return new LatentStateStretchSpec (
			strName,
			aSegmentSpec
		);
	}

	private static final DiscountCurve OISDiscountCurve (
		final JulianDate dtSpot,
		final String strCurrency,
		final String strHeaderComment)
		throws Exception
	{
		System.out.println ("\n\t----------------------------------------------------------------");

		System.out.println ("\t     " + strHeaderComment);

		System.out.println ("\t----------------------------------------------------------------");

		/*
		 * Construct the Array of Deposit Instruments and their Quotes from the given set of parameters
		 */

		SingleStreamComponent[] aDeposit = DepositInstrumentsFromMaturityDays (
			dtSpot,
			strCurrency,
			new int[] {
				1, 2, 3
			}
		);

		double[] adblDepositQuote = new double[] {
			0.0004, 0.0004, 0.0004		 // Deposit
		};

		/*
		 * Construct the Deposit Instrument Set Stretch Builder
		 */

		LatentStateStretchSpec depositStretch = DepositStretch (
			aDeposit,
			adblDepositQuote
		);

		/*
		 * Construct the Array of Short End OIS Instruments and their Quotes from the given set of parameters
		 */

		double[] adblShortEndOISQuote = new double[] {
			0.00070,    //   1W
			0.00069,    //   2W
			0.00078,    //   3W
			0.00074     //   1M
		};

		FixFloatComponent[] aShortEndOISComp = OvernightIndexFromMaturityTenor (
			dtSpot,
			new java.lang.String[] {
				"1W", "2W", "3W", "1M"
			},
			adblShortEndOISQuote,
			strCurrency
		);

		/*
		 * Construct the Short End OIS Instrument Set Stretch Builder
		 */

		LatentStateStretchSpec oisShortEndStretch = OISStretch (
			"SHORT_END_OIS",
			aShortEndOISComp,
			adblShortEndOISQuote
		);

		/*
		 * Construct the Array of OIS Futures Instruments and their Quotes from the given set of parameters
		 */

		double[] adblOISFutureQuote = new double[] {
			 0.00046,    //   1M x 1M
			 0.00016,    //   2M x 1M
			-0.00007,    //   3M x 1M
			-0.00013,    //   4M x 1M
			-0.00014     //   5M x 1M
		};

		FixFloatComponent[] aOISFutureComp = OvernightIndexFutureFromMaturityTenor (
			dtSpot,
			new java.lang.String[] {
				"1M", "2M", "3M", "4M", "5M"
			},
			new java.lang.String[] {
				"1M", "1M", "1M", "1M", "1M"
			},
			adblOISFutureQuote,
			strCurrency
		);

		/*
		 * Construct the OIS Future Instrument Set Stretch Builder
		 */

		LatentStateStretchSpec oisFutureStretch = OISStretch (
			"OIS_FUTURE",
			aOISFutureComp,
			adblOISFutureQuote
		);

		/*
		 * Construct the Array of Long End OIS Instruments and their Quotes from the given set of parameters
		 */

		double[] adblLongEndOISQuote = new double[] {
			0.00002,    //  15M
			0.00008,    //  18M
			0.00021,    //  21M
			0.00036,    //   2Y
			0.00127,    //   3Y
			0.00274,    //   4Y
			0.00456,    //   5Y
			0.00647,    //   6Y
			0.00827,    //   7Y
			0.00996,    //   8Y
			0.01147,    //   9Y
			0.01280,    //  10Y
			0.01404,    //  11Y
			0.01516,    //  12Y
			0.01764,    //  15Y
			0.01939,    //  20Y
			0.02003,    //  25Y
			0.02038     //  30Y
		};

		FixFloatComponent[] aLongEndOISComp = OvernightIndexFromMaturityTenor (
			dtSpot,
			new java.lang.String[] {
				"15M", "18M", "21M", "2Y", "3Y", "4Y", "5Y", "6Y", "7Y", "8Y", "9Y", "10Y", "11Y", "12Y", "15Y", "20Y", "25Y", "30Y"
			},
			adblLongEndOISQuote,
			strCurrency
		);

		/*
		 * Construct the Long End OIS Instrument Set Stretch Builder
		 */

		LatentStateStretchSpec oisLongEndStretch = OISStretch (
			"LONG_END_OIS",
			aLongEndOISComp,
			adblLongEndOISQuote
		);

		LatentStateStretchSpec[] aStretchSpec = new LatentStateStretchSpec[] {
			depositStretch,
			oisShortEndStretch,
			oisFutureStretch,
			oisLongEndStretch
		};

		/*
		 * Set up the Linear Curve Calibrator using the following parameters:
		 * 	- Cubic Exponential Mixture Basis Spline Set
		 * 	- Ck = 2, Segment Curvature Penalty = 2
		 * 	- Quadratic Rational Shape Controller
		 * 	- Natural Boundary Setting
		 */

		LinearLatentStateCalibrator lcc = new LinearLatentStateCalibrator (
			new SegmentCustomBuilderControl (
				MultiSegmentSequenceBuilder.BASIS_SPLINE_POLYNOMIAL,
				new PolynomialFunctionSetParams (4),
				SegmentInelasticDesignControl.Create (2, 2),
				new ResponseScalingShapeControl (true, new QuadraticRationalShapeControl (0.)),
				null),
			BoundarySettings.NaturalStandard(),
			MultiSegmentSequence.CALIBRATE,
			null,
			null
		);

		/*
		 * Construct the Shape Preserving Discount Curve by applying the linear curve calibrator to the array
		 *  of Deposit and Swap Stretches.
		 */

		ValuationParams valParams = new ValuationParams (dtSpot, dtSpot, strCurrency);

		DiscountCurve dc = ScenarioDiscountCurveBuilder.ShapePreservingDFBuild (
			lcc,
			aStretchSpec,
			valParams,
			null,
			null,
			null,
			1.
		);

		/*
		 * Cross-Comparison of the Deposit Calibration Instrument "Rate" metric across the different curve
		 * 	construction methodologies.
		 */

		System.out.println ("\t----------------------------------------------------------------");

		System.out.println ("\t     DEPOSIT INSTRUMENTS CALIBRATION RECOVERY");

		System.out.println ("\t----------------------------------------------------------------");

		for (int i = 0; i < aDeposit.length; ++i)
			System.out.println ("\t[" + aDeposit[i].effectiveDate() + " => " + aDeposit[i].maturityDate() + "] = " +
				FormatUtil.FormatDouble (aDeposit[i].measureValue (valParams, null,
					MarketParamsBuilder.Create (dc, null, null, null, null, null, null),
						null, "Rate"), 1, 6, 1.) + " | " + FormatUtil.FormatDouble (adblDepositQuote[i], 1, 6, 1.));

		/*
		 * Cross-Comparison of the Short End OIS Calibration Instrument "Rate" metric across the different curve
		 * 	construction methodologies.
		 */

		System.out.println ("\n\t----------------------------------------------------------------");

		System.out.println ("\t     OIS SHORT END INSTRUMENTS CALIBRATION RECOVERY");

		System.out.println ("\t----------------------------------------------------------------");

		for (int i = 0; i < aShortEndOISComp.length; ++i)
			System.out.println ("\t[" + aShortEndOISComp[i].effectiveDate() + " => " + aShortEndOISComp[i].maturityDate() + "] = " +
				FormatUtil.FormatDouble (aShortEndOISComp[i].measureValue (valParams, null,
					MarketParamsBuilder.Create (dc, null, null, null, null, null, null),
						null, "CalibSwapRate"), 1, 6, 1.) + " | " + FormatUtil.FormatDouble (adblShortEndOISQuote[i], 1, 6, 1.) + " | " +
							FormatUtil.FormatDouble (aShortEndOISComp[i].measureValue (valParams, null,
								MarketParamsBuilder.Create (dc, null, null, null, null, null, null),
									null, "FairPremium"), 1, 6, 1.));

		/*
		 * Cross-Comparison of the OIS Future Calibration Instrument "Rate" metric across the different curve
		 * 	construction methodologies.
		 */

		System.out.println ("\n\t----------------------------------------------------------------");

		System.out.println ("\t     OIS FUTURE INSTRUMENTS CALIBRATION RECOVERY");

		System.out.println ("\t----------------------------------------------------------------");

		for (int i = 0; i < aOISFutureComp.length; ++i)
			System.out.println ("\t[" + aOISFutureComp[i].effectiveDate() + " => " + aOISFutureComp[i].maturityDate() + "] = " +
				FormatUtil.FormatDouble (aOISFutureComp[i].measureValue (valParams, null,
					MarketParamsBuilder.Create (dc, null, null, null, null, null, null),
						null, "SwapRate"), 1, 6, 1.) + " | " + FormatUtil.FormatDouble (adblOISFutureQuote[i], 1, 6, 1.) + " | " +
							FormatUtil.FormatDouble (aOISFutureComp[i].measureValue (valParams, null,
								MarketParamsBuilder.Create (dc, null, null, null, null, null, null),
									null, "FairPremium"), 1, 6, 1.));

		/*
		 * Cross-Comparison of the Long End OIS Calibration Instrument "Rate" metric across the different curve
		 * 	construction methodologies.
		 */

		System.out.println ("\n\t----------------------------------------------------------------");

		System.out.println ("\t     OIS LONG END INSTRUMENTS CALIBRATION RECOVERY");

		System.out.println ("\t----------------------------------------------------------------");

		for (int i = 0; i < aLongEndOISComp.length; ++i)
			System.out.println ("\t[" + aLongEndOISComp[i].effectiveDate() + " => " + aLongEndOISComp[i].maturityDate() + "] = " +
				FormatUtil.FormatDouble (aLongEndOISComp[i].measureValue (valParams, null,
					MarketParamsBuilder.Create (dc, null, null, null, null, null, null),
						null, "CalibSwapRate"), 1, 6, 1.) + " | " + FormatUtil.FormatDouble (adblLongEndOISQuote[i], 1, 6, 1.) + " | " +
							FormatUtil.FormatDouble (aLongEndOISComp[i].measureValue (valParams, null,
								MarketParamsBuilder.Create (dc, null, null, null, null, null, null),
									null, "FairPremium"), 1, 6, 1.));

		return dc;
	}

	/*
	 * Construct an array of float-float swaps from the corresponding reference (6M) and the derived legs.
	 * 
	 *  	USE WITH CARE: This sample ignores errors and does not handle exceptions.
	 */

	private static final FloatFloatComponent[] MakexM6MBasisSwap (
		final JulianDate dtEffective,
		final String strCurrency,
		final String[] astrMaturityTenor,
		final int iTenorInMonths)
		throws Exception
	{
		FloatFloatComponent[] aFFC = new FloatFloatComponent[astrMaturityTenor.length];

		ComposableFloatingUnitSetting cfusReference = new ComposableFloatingUnitSetting (
			"6M",
			CompositePeriodBuilder.EDGE_DATE_SEQUENCE_REGULAR,
			null,
			ForwardLabel.Standard (strCurrency + "-6M"),
			CompositePeriodBuilder.REFERENCE_PERIOD_IN_ADVANCE,
			0.
		);

		ComposableFloatingUnitSetting cfusDerived = new ComposableFloatingUnitSetting (
			iTenorInMonths + "M",
			CompositePeriodBuilder.EDGE_DATE_SEQUENCE_REGULAR,
			null,
			ForwardLabel.Standard (strCurrency + "-" + iTenorInMonths + "M"),
			CompositePeriodBuilder.REFERENCE_PERIOD_IN_ADVANCE,
			0.
		);

		CompositePeriodSetting cpsReference = new CompositePeriodSetting (
			2,
			"6M",
			strCurrency,
			null,
			-1.,
			null,
			null,
			null,
			null
		);

		CompositePeriodSetting cpsDerived = new CompositePeriodSetting (
			12 / iTenorInMonths,
			iTenorInMonths + "M",
			strCurrency,
			null,
			1.,
			null,
			null,
			null,
			null
		);

		CashSettleParams csp = new CashSettleParams (
			0,
			strCurrency,
			0
		);

		for (int i = 0; i < astrMaturityTenor.length; ++i) {
			List<Double> lsReferenceStreamEdgeDate = CompositePeriodBuilder.RegularEdgeDates (
				dtEffective,
				"6M",
				astrMaturityTenor[i],
				null
			);

			List<Double> lsDerivedStreamEdgeDate = CompositePeriodBuilder.RegularEdgeDates (
				dtEffective,
				iTenorInMonths + "M",
				astrMaturityTenor[i],
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

			aFFC[i] = new FloatFloatComponent (
				referenceStream,
				derivedStream,
				csp
			);
		}

		return aFFC;
	}

	private static final ForwardCurve MakexMForwardCurve (
		final JulianDate dtSpot,
		final String strCurrency,
		final DiscountCurve dc,
		final int iTenorInMonths,
		final String[] astrxM6MFwdTenor,
		final String strManifestMeasure,
		final double[] adblxM6MBasisSwapQuote)
		throws Exception
	{
		/*
		 * Construct the 6M-xM float-float basis swap.
		 */

		FloatFloatComponent[] aFFC = MakexM6MBasisSwap (dtSpot, strCurrency, astrxM6MFwdTenor, iTenorInMonths);

		String strBasisTenor = iTenorInMonths + "M";

		ValuationParams valParams = new ValuationParams (dtSpot, dtSpot, strCurrency);

		/*
		 * Calculate the starting forward rate off of the discount curve.
		 */

		double dblStartingFwd = dc.forward (dtSpot.julian(), dtSpot.addTenor (strBasisTenor).julian());

		/*
		 * Set the discount curve based component market parameters.
		 */

		CurveSurfaceQuoteSet mktParams = MarketParamsBuilder.Create (dc, null, null, null, null, null, null);

		/*
		 * Construct the shape preserving forward curve off of Cubic Polynomial Basis Spline.
		 */

		return ScenarioForwardCurveBuilder.ShapePreservingForwardCurve (
			"CUBIC_FWD" + strBasisTenor,
			ForwardLabel.Create (strCurrency, strBasisTenor),
			valParams,
			null,
			mktParams,
			null,
			MultiSegmentSequenceBuilder.BASIS_SPLINE_POLYNOMIAL,
			new PolynomialFunctionSetParams (4),
			aFFC,
			strManifestMeasure,
			adblxM6MBasisSwapQuote,
			dblStartingFwd
		);
	}

	private static final FloatFloatComponent[] FedFundLIBORBasisSwap (
		final JulianDate dtEffective,
		final String strCurrency,
		final String[] astrMaturityTenor)
		throws Exception
	{
		FloatFloatComponent[] aFFC = new FloatFloatComponent[astrMaturityTenor.length];

		ComposableFloatingUnitSetting cfusReference = new ComposableFloatingUnitSetting (
			"3M",
			CompositePeriodBuilder.EDGE_DATE_SEQUENCE_REGULAR,
			null,
			ForwardLabel.Standard (strCurrency + "-3M"),
			CompositePeriodBuilder.REFERENCE_PERIOD_IN_ADVANCE,
			0.
		);

		ComposableFloatingUnitSetting cfusDerived = new ComposableFloatingUnitSetting (
			"ON",
			CompositePeriodBuilder.EDGE_DATE_SEQUENCE_OVERNIGHT,
			null,
			ForwardLabel.Create (strCurrency, "ON"),
			CompositePeriodBuilder.REFERENCE_PERIOD_IN_ADVANCE,
			0.
		);

		CompositePeriodSetting cpsReference = new CompositePeriodSetting (
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

		CompositePeriodSetting cpsDerived = new CompositePeriodSetting (
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

		CashSettleParams csp = new CashSettleParams (
			0,
			strCurrency,
			0
		);

		for (int i = 0; i < astrMaturityTenor.length; ++i) {
			List<Double> lsReferenceStreamEdgeDate = CompositePeriodBuilder.RegularEdgeDates (
				dtEffective,
				"3M",
				astrMaturityTenor[i],
				null
			);

			List<Double> lsDerivedStreamEdgeDate = CompositePeriodBuilder.RegularEdgeDates (
				dtEffective,
				"3M",
				astrMaturityTenor[i],
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

			aFFC[i] = new FloatFloatComponent (
				referenceStream,
				derivedStream,
				csp
			);
		}

		return aFFC;
	}

	/*
	 * Construct the Array of Overnight Index Instruments from the given set of parameters
	 * 
	 *  	USE WITH CARE: This sample ignores errors and does not handle exceptions.
	 */

	private static final FixFloatComponent[] SwapInstrumentsFromMaturityTenor (
		final JulianDate dtEffective,
		final String[] astrMaturityTenor,
		final double[] adblCoupon,
		final String strCurrency)
		throws Exception
	{
		FixFloatComponent[] aOIS = new FixFloatComponent[astrMaturityTenor.length];

		UnitCouponAccrualSetting ucasFixed = new UnitCouponAccrualSetting (
			2,
			"Act/360",
			false,
			"Act/360",
			false,
			strCurrency,
			false,
			CompositePeriodBuilder.ACCRUAL_COMPOUNDING_RULE_GEOMETRIC
		);

		CashSettleParams csp = new CashSettleParams (
			0,
			strCurrency,
			0
		);

		for (int i = 0; i < astrMaturityTenor.length; ++i) {
			java.lang.String strFixedTenor = AnalyticsHelper.LEFT_TENOR_LESSER == AnalyticsHelper.TenorCompare (
				astrMaturityTenor[i],
				"6M"
			) ? astrMaturityTenor[i] : "6M";

			java.lang.String strFloatingTenor = AnalyticsHelper.LEFT_TENOR_LESSER == AnalyticsHelper.TenorCompare (
				astrMaturityTenor[i],
				"3M"
			) ? astrMaturityTenor[i] : "3M";

			ComposableFloatingUnitSetting cfusFloating = new ComposableFloatingUnitSetting (
				"3M",
				CompositePeriodBuilder.EDGE_DATE_SEQUENCE_REGULAR,
				null,
				ForwardLabel.Create (strCurrency, "3M"),
				CompositePeriodBuilder.REFERENCE_PERIOD_IN_ADVANCE,
				0.
			);

			ComposableFixedUnitSetting cfusFixed = new ComposableFixedUnitSetting (
				strFixedTenor,
				CompositePeriodBuilder.EDGE_DATE_SEQUENCE_REGULAR,
				null,
				adblCoupon[i],
				0.,
				strCurrency
			);

			CompositePeriodSetting cpsFloating = new CompositePeriodSetting (
				4,
				strFloatingTenor,
				strCurrency,
				null,
				-1.,
				null,
				null,
				null,
				null
			);

			CompositePeriodSetting cpsFixed = new CompositePeriodSetting (
				2,
				strFixedTenor,
				strCurrency,
				null,
				1.,
				null,
				null,
				null,
				null
			);

			List<Double> lsFixedStreamEdgeDate = CompositePeriodBuilder.RegularEdgeDates (
				dtEffective,
				strFixedTenor,
				astrMaturityTenor[i],
				null
			);

			List<Double> lsFloatingStreamEdgeDate = CompositePeriodBuilder.RegularEdgeDates (
				dtEffective,
				strFloatingTenor,
				astrMaturityTenor[i],
				null
			);

			Stream floatingStream = new Stream (
				CompositePeriodBuilder.FloatingCompositeUnit (
					lsFloatingStreamEdgeDate,
					cpsFloating,
					cfusFloating
				)
			);

			Stream fixedStream = new Stream (
				CompositePeriodBuilder.FixedCompositeUnit (
					lsFixedStreamEdgeDate,
					cpsFixed,
					ucasFixed,
					cfusFixed
				)
			);

			FixFloatComponent ois = new FixFloatComponent (
				fixedStream,
				floatingStream,
				csp
			);

			ois.setPrimaryCode ("OIS." + astrMaturityTenor[i] + "." + strCurrency);

			aOIS[i] = ois;
		}

		return aOIS;
	}

	public static final void main (
		final String[] astrArgs)
		throws Exception
	{
		/*
		 * Initialize the Credit Analytics Library
		 */

		CreditAnalytics.Init ("");

		String strCurrency = "USD";

		JulianDate dtToday = DateUtil.CreateFromYMD (2012, DateUtil.DECEMBER, 11);

		DiscountCurve dcOIS = OISDiscountCurve (
			dtToday,
			strCurrency,
			"OVERNIGHT INDEX RUN RECONCILIATION"
		);

		ForwardCurve fc = MakexMForwardCurve (
			dtToday,
			strCurrency,
			dcOIS,
			3,
			new String[] {
				"1Y", "2Y", "3Y", "4Y", "5Y", "6Y", "7Y", "8Y", "9Y", "10Y", "11Y", "12Y", "15Y", "20Y", "25Y", "30Y"
			},
			"ReferenceParBasisSpread",
			new double[] {
				0.00186,    //  1Y
				0.00127,    //  2Y
				0.00097,    //  3Y
				0.00080,    //  4Y
				0.00067,    //  5Y
				0.00058,    //  6Y
				0.00051,    //  7Y
				0.00046,    //  8Y
				0.00042,    //  9Y
				0.00038,    // 10Y
				0.00035,    // 11Y
				0.00033,    // 12Y
				0.00028,    // 15Y
				0.00022,    // 20Y
				0.00020,    // 25Y
				0.00018     // 30Y
			}
		);

		CurveSurfaceQuoteSet mktParams = MarketParamsBuilder.Create (
			dcOIS,
			null,
			null,
			null,
			null,
			null,
			null
		);

		mktParams.setForwardCurve (fc);

		String[] astrMaturityTenor = new String[] {
			"1Y", "2Y", "3Y", "4Y", "5Y", "6Y", "7Y", "8Y", "9Y", "10Y", "11Y", "12Y", "15Y", "20Y", "25Y", "30Y"
		};

		FloatFloatComponent[] aFedFundLIBORSwap = FedFundLIBORBasisSwap (
			dtToday,
			strCurrency,
			astrMaturityTenor
		);

		FixFloatComponent[] aOIS = OvernightIndexFromMaturityTenor (
			dtToday,
			astrMaturityTenor,
			new double[] {
				0.00002,
				0.00036,
				0.00127,
				0.00274,
				0.00456,
				0.00647,
				0.00827,
				0.00996,
				0.01147,
				0.01280,
				0.01404,
				0.01516,
				0.01764,
				0.01939,
				0.02003,
				0.02038
			},
			strCurrency
		);

		FixFloatComponent[] aIRS = SwapInstrumentsFromMaturityTenor (
			dtToday,
			astrMaturityTenor,
			new double[] {
				0.00002,
				0.00036,
				0.00127,
				0.00274,
				0.00456,
				0.00647,
				0.00827,
				0.00996,
				0.01147,
				0.01280,
				0.01404,
				0.01516,
				0.01764,
				0.01939,
				0.02003,
				0.02038
			},
			strCurrency
		);

		ValuationParams valParams = new ValuationParams (
			dtToday,
			dtToday,
			strCurrency
		);

		System.out.println ("\n\t--------------------------------------------------------------------------");

		System.out.println ("\t                    FED FUND OIS BASIS COMPARISON");

		System.out.println ("\t--------------------------------------------------------------------------");

		System.out.println ("\t\tOutput Order[Effective Date - Maturity Date]");

		System.out.println ("\t\t\t IRS Rate (%)");

		System.out.println ("\t\t\t Fed Fund LIBOR Basis (bp)");

		System.out.println ("\t\t\t OIS Rate Uncompounded (%) (Bloomberg 2010 Methodology)");

		System.out.println ("\t\t\t OIS Rate Daily Compounded (%) (Bloomberg 2010 Methodology)");

		System.out.println ("\t\t\t OIS Rate (%) From Full Calibration\n");

		for (int i = 0; i < aFedFundLIBORSwap.length; ++i) {
			Map<String, Double> mapOIS = aOIS[i].value (
				valParams,
				null,
				mktParams,
				null
			);

			Map<String, Double> mapIRS = aIRS[i].value (
				valParams,
				null,
				mktParams,
				null
			);

			double dblOISRate = mapOIS.get ("SwapRate");

			double dblIRSRate = mapIRS.get ("SwapRate");

			double dblLIBORFedFundBasis = dblIRSRate - dblOISRate;

			System.out.println ("\t[" +
				aFedFundLIBORSwap[i].effectiveDate() + " - " +
				aFedFundLIBORSwap[i].maturityDate() + "] => " +
				FormatUtil.FormatDouble (dblIRSRate, 1, 4, 100.) + "% | " +
				FormatUtil.FormatDouble (dblLIBORFedFundBasis, 1, 1, 10000.) + " | " +
				FormatUtil.FormatDouble (AnalyticsHelper.OISFromLIBORSwapFedFundBasis (dblIRSRate, -dblLIBORFedFundBasis), 1, 4, 100.) + "% | " +
				FormatUtil.FormatDouble (AnalyticsHelper.OISFromLIBORSwapFedFundBasis2 (dblIRSRate, -dblLIBORFedFundBasis), 1, 4, 100.) + "% | " +
				FormatUtil.FormatDouble (dblOISRate, 1, 4, 100.) + "%"
			);
		}
	}
}
