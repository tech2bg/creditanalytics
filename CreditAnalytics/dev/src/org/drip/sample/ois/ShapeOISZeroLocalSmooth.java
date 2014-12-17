
package org.drip.sample.ois;

import java.util.List;

import org.drip.analytics.date.JulianDate;
import org.drip.analytics.definition.LatentStateStatic;
import org.drip.analytics.rates.*;
import org.drip.analytics.support.*;
import org.drip.market.definition.OvernightIndexContainer;
import org.drip.param.creator.*;
import org.drip.param.period.*;
import org.drip.param.valuation.*;
import org.drip.product.calib.*;
import org.drip.product.creator.*;
import org.drip.product.definition.CalibratableFixedIncomeComponent;
import org.drip.product.rates.*;
import org.drip.quant.common.FormatUtil;
import org.drip.quant.function1D.QuadraticRationalShapeControl;
import org.drip.service.api.CreditAnalytics;
import org.drip.spline.basis.*;
import org.drip.spline.params.*;
import org.drip.spline.pchip.LocalMonotoneCkGenerator;
import org.drip.spline.stretch.*;
import org.drip.state.estimator.*;
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
 * ShapeOISZeroLocalSmooth demonstrates the usage of different local smoothing techniques involved in the
 * 	discount curve creation. It shows the following:
 * 	- Construct the Array of Deposit/OIS Instruments and their Quotes from the given set of parameters.
 * 	- Construct the Deposit/OIS Instrument Set Stretch Builder.
 * 	- Set up the Linear Curve Calibrator using the following parameters:
 * 		- Cubic Exponential Mixture Basis Spline Set
 * 		- Ck = 2, Segment Curvature Penalty = 2
 * 		- Quadratic Rational Shape Controller
 * 		- Natural Boundary Setting
 * 	- Set up the Akima Local Curve Control parameters as follows:
 * 		- C1 Akima Monotone Smoothener with spurious extrema elimination and monotone filtering applied
 * 		- Zero Rate Quantification Metric
 * 		- Cubic Polynomial Basis Spline Set
 * 		- Ck = 2, Segment Curvature Penalty = 2
 * 		- Quadratic Rational Shape Controller
 * 		- Natural Boundary Setting
 * 	- Set up the Harmonic Local Curve Control parameters as follows:
 * 		- C1 Harmonic Monotone Smoothener with spurious extrema elimination and monotone filtering applied
 * 		- Zero Rate Quantification Metric
 * 		- Cubic Polynomial Basis Spline Set
 * 		- Ck = 2, Segment Curvature Penalty = 2
 * 		- Quadratic Rational Shape Controller
 * 		- Natural Boundary Setting
 * 	- Set up the Hyman 1983 Local Curve Control parameters as follows:
 * 		- C1 Hyman 1983 Monotone Smoothener with spurious extrema elimination and monotone filtering applied
 * 		- Zero Rate Quantification Metric
 * 		- Cubic Polynomial Basis Spline Set
 * 		- Ck = 2, Segment Curvature Penalty = 2
 * 		- Quadratic Rational Shape Controller
 * 		- Natural Boundary Setting
 * 	- Set up the Hyman 1989 Local Curve Control parameters as follows:
 * 		- C1 Akima Monotone Smoothener with spurious extrema elimination and monotone filtering applied
 * 		- Zero Rate Quantification Metric
 * 		- Cubic Polynomial Basis Spline Set
 * 		- Ck = 2, Segment Curvature Penalty = 2
 * 		- Quadratic Rational Shape Controller
 * 		- Natural Boundary Setting
 * 	- Set up the Huynh-Le Floch Delimited Local Curve Control parameters as follows:
 * 		- C1 Huynh-Le Floch Delimited Monotone Smoothener with spurious extrema elimination and monotone filtering applied
 * 		- Zero Rate Quantification Metric
 * 		- Cubic Polynomial Basis Spline Set
 * 		- Ck = 2, Segment Curvature Penalty = 2
 * 		- Quadratic Rational Shape Controller
 * 		- Natural Boundary Setting
 * 	- Set up the Kruger Local Curve Control parameters as follows:
 * 		- C1 Kruger Monotone Smoothener with spurious extrema elimination and monotone filtering applied
 * 		- Zero Rate Quantification Metric
 * 		- Cubic Polynomial Basis Spline Set
 * 		- Ck = 2, Segment Curvature Penalty = 2
 * 		- Quadratic Rational Shape Controller
 * 		- Natural Boundary Setting
 * 	- Construct the Shape Preserving OIS Discount Curve by applying the linear curve calibrator to the array
 * 		of Deposit and OIS Stretches.
 * 	- Construct the Akima Locally Smoothened OIS Discount Curve by applying the linear curve calibrator and
 * 		the Local Curve Control parameters to the array of Deposit and OIS Stretches and the shape
 * 		preserving discount curve.
 * 	- Construct the Harmonic Locally Smoothened OIS Discount Curve by applying the linear curve calibrator
 * 		and the Local Curve Control parameters to the array of Deposit and OIS Stretches and the shape
 * 		preserving discount curve.
 * 	- Construct the Hyman 1983 Locally Smoothened OIS Discount Curve by applying the linear curve calibrator
 * 		and the Local Curve Control parameters to the array of Deposit and OIS Stretches and the shape
 * 		preserving discount curve.
 * 	- Construct the Hyman 1989 Locally Smoothened OIS Discount Curve by applying the linear curve calibrator
 * 		and the Local Curve Control parameters to the array of Deposit and OIS Stretches and the shape
 * 		preserving discount curve.
 * 	- Construct the Huynh-Le Floch Delimiter Locally Smoothened OIS Discount Curve by applying the linear
 * 		curve calibrator and the Local Curve Control parameters to the array of Deposit and OIS Stretches
 * 		and the shape preserving discount curve.
 * 	- Construct the Kruger Locally Smoothened OIS Discount Curve by applying the linear curve calibrator and
 * 		the Local Curve Control parameters to the array of Deposit and OIS Stretches and the shape
 * 		preserving discount curve.
 * 	- Cross-Comparison of the Deposit/OIS Calibration Instrument "Rate" metric across the different curve
 * 		construction methodologies.
 *  - Cross-Comparison of the OIS Calibration Instrument "Rate" metric across the different curve
 *  	construction methodologies for a sequence of bespoke swap instruments.
 * 
 * @author Lakshmi Krishnamurthy
 */

public class ShapeOISZeroLocalSmooth {

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
			aDeposit[i] = SingleStreamComponentBuilder.CreateDeposit (
				dtEffective,
				dtEffective.addBusDays (
					aiDay[i],
					strCurrency
				),
				OvernightIndexContainer.IndexFromJurisdiction (strCurrency).ForwardStateLabel(),
				strCurrency
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
			"   DEPOSIT   ",
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

		UnitCouponAccrualSetting ucasFloating = new UnitCouponAccrualSetting (
			360,
			"Act/360",
			false,
			"Act/360",
			false,
			strCurrency,
			false
		);

		UnitCouponAccrualSetting ucasFixed = new UnitCouponAccrualSetting (
			2,
			"Act/360",
			false,
			"Act/360",
			false,
			strCurrency,
			false
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
				OvernightIndexContainer.IndexFromJurisdiction (strCurrency).ForwardStateLabel(),
				CompositePeriodBuilder.REFERENCE_PERIOD_IN_ADVANCE,
				null,
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
				CompositePeriodBuilder.ACCRUAL_COMPOUNDING_RULE_GEOMETRIC,
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
				CompositePeriodBuilder.ACCRUAL_COMPOUNDING_RULE_GEOMETRIC,
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
					ucasFloating,
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

			UnitCouponAccrualSetting ucasFloating = new UnitCouponAccrualSetting (
				360,
				"Act/360",
				false,
				"Act/360",
				false,
				strCurrency,
				false
			);

			UnitCouponAccrualSetting ucasFixed = new UnitCouponAccrualSetting (
				2,
				"Act/360",
				false,
				"Act/360",
				false,
				strCurrency,
				false
			);

			ComposableFloatingUnitSetting cfusFloating = new ComposableFloatingUnitSetting (
				"ON",
				CompositePeriodBuilder.EDGE_DATE_SEQUENCE_OVERNIGHT,
				null,
				OvernightIndexContainer.IndexFromJurisdiction (strCurrency).ForwardStateLabel(),
				CompositePeriodBuilder.REFERENCE_PERIOD_IN_ADVANCE,
				null,
				0.
			);

			CompositePeriodSetting cpsFloating = new CompositePeriodSetting (
				4,
				strFloatingTenor,
				strCurrency,
				null,
				CompositePeriodBuilder.ACCRUAL_COMPOUNDING_RULE_GEOMETRIC,
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
				CompositePeriodBuilder.ACCRUAL_COMPOUNDING_RULE_GEOMETRIC,
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
					ucasFloating,
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

	/*
	 * This sample demonstrates the usage of different local smoothing techniques involved in the OIS discount
	 * 	curve creation. It shows the following:
	 * 	- Construct the Array of Deposit/OIS Instruments and their Quotes from the given set of parameters.
	 * 	- Construct the Deposit/OIS Instrument Set Stretch Builder.
	 * 	- Set up the Linear Curve Calibrator using the following parameters:
	 * 		- Cubic Exponential Mixture Basis Spline Set
	 * 		- Ck = 2, Segment Curvature Penalty = 2
	 * 		- Quadratic Rational Shape Controller
	 * 		- Natural Boundary Setting
	 * 	- Set up the Akima Local Curve Control parameters as follows:
	 * 		- C1 Akima Monotone Smoothener with spurious extrema elimination and monotone filtering applied
	 * 		- Zero Rate Quantification Metric
	 * 		- Cubic Polynomial Basis Spline Set
	 * 		- Ck = 2, Segment Curvature Penalty = 2
	 * 		- Quadratic Rational Shape Controller
	 * 		- Natural Boundary Setting
	 * 	- Set up the Harmonic Local Curve Control parameters as follows:
	 * 		- C1 Harmonic Monotone Smoothener with spurious extrema elimination and monotone filtering applied
	 * 		- Zero Rate Quantification Metric
	 * 		- Cubic Polynomial Basis Spline Set
	 * 		- Ck = 2, Segment Curvature Penalty = 2
	 * 		- Quadratic Rational Shape Controller
	 * 		- Natural Boundary Setting
	 * 	- Set up the Hyman 1983 Local Curve Control parameters as follows:
	 * 		- C1 Hyman 1983 Monotone Smoothener with spurious extrema elimination and monotone filtering applied
	 * 		- Zero Rate Quantification Metric
	 * 		- Cubic Polynomial Basis Spline Set
	 * 		- Ck = 2, Segment Curvature Penalty = 2
	 * 		- Quadratic Rational Shape Controller
	 * 		- Natural Boundary Setting
	 * 	- Set up the Hyman 1989 Local Curve Control parameters as follows:
	 * 		- C1 Akima Monotone Smoothener with spurious extrema elimination and monotone filtering applied
	 * 		- Zero Rate Quantification Metric
	 * 		- Cubic Polynomial Basis Spline Set
	 * 		- Ck = 2, Segment Curvature Penalty = 2
	 * 		- Quadratic Rational Shape Controller
	 * 		- Natural Boundary Setting
	 * 	- Set up the Huynh-Le Floch Delimited Local Curve Control parameters as follows:
	 * 		- C1 Huynh-Le Floch Delimited Monotone Smoothener with spurious extrema elimination and monotone filtering applied
	 * 		- Zero Rate Quantification Metric
	 * 		- Cubic Polynomial Basis Spline Set
	 * 		- Ck = 2, Segment Curvature Penalty = 2
	 * 		- Quadratic Rational Shape Controller
	 * 		- Natural Boundary Setting
	 * 	- Set up the Kruger Local Curve Control parameters as follows:
	 * 		- C1 Kruger Monotone Smoothener with spurious extrema elimination and monotone filtering applied
	 * 		- Zero Rate Quantification Metric
	 * 		- Cubic Polynomial Basis Spline Set
	 * 		- Ck = 2, Segment Curvature Penalty = 2
	 * 		- Quadratic Rational Shape Controller
	 * 		- Natural Boundary Setting
	 * 	- Construct the Shape Preserving OIS Discount Curve by applying the linear curve calibrator to the array
	 * 		of Deposit and OIS Stretches.
	 * 	- Construct the Akima Locally Smoothened OIS Discount Curve by applying the linear curve calibrator and
	 * 		the Local Curve Control parameters to the array of Deposit and OIS Stretches and the shape
	 * 		preserving discount curve.
	 * 	- Construct the Harmonic Locally Smoothened OIS Discount Curve by applying the linear curve calibrator
	 * 		and the Local Curve Control parameters to the array of Deposit and OIS Stretches and the shape
	 * 		preserving discount curve.
	 * 	- Construct the Hyman 1983 Locally Smoothened OIS Discount Curve by applying the linear curve calibrator
	 * 		and the Local Curve Control parameters to the array of Deposit and OIS Stretches and the shape
	 * 		preserving discount curve.
	 * 	- Construct the Hyman 1989 Locally Smoothened OIS Discount Curve by applying the linear curve calibrator
	 * 		and the Local Curve Control parameters to the array of Deposit and OIS Stretches and the shape
	 * 		preserving discount curve.
	 * 	- Construct the Huynh-Le Floch Delimiter Locally Smoothened OIS Discount Curve by applying the linear
	 * 		curve calibrator and the Local Curve Control parameters to the array of Deposit and OIS Stretches
	 * 		and the shape preserving discount curve.
	 * 	- Construct the Kruger Locally Smoothened OIS Discount Curve by applying the linear curve calibrator and
	 * 		the Local Curve Control parameters to the array of Deposit and OIS Stretches and the shape
	 * 		preserving discount curve.
	 * 	- Cross-Comparison of the Deposit/OIS Calibration Instrument "Rate" metric across the different curve
	 * 		construction methodologies.
	 *  - Cross-Comparison of the OIS Calibration Instrument "Rate" metric across the different curve
	 *  	construction methodologies for a sequence of bespoke OIS instruments.
	 * 
	 *  	USE WITH CARE: This sample ignores errors and does not handle exceptions.
	 */

	private static final void ShapeOISDFZeroLocalSmoothSample (
		final JulianDate dtSpot,
		final String strHeaderComment,
		final String strCurrency)
		throws Exception
	{
		System.out.println ("\n\t----------------------------------------------------------------");

		System.out.println ("\t" + strHeaderComment);

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
				MultiSegmentSequenceBuilder.BASIS_SPLINE_EXPONENTIAL_MIXTURE,
				new ExponentialMixtureSetParams (new double[] {0.01, 0.05, 0.25}),
				SegmentInelasticDesignControl.Create (2, 2),
				new ResponseScalingShapeControl (true, new QuadraticRationalShapeControl (0.)),
				null),
			BoundarySettings.NaturalStandard(),
			MultiSegmentSequence.CALIBRATE,
			null,
			null
		);

		/*
		 * Set up the Akima Local Curve Control parameters as follows:
		 * 	- C1 Akima Monotone Smoothener with spurious extrema elimination and monotone filtering applied
		 * 	- Zero Rate Quantification Metric
		 * 	- Cubic Polynomial Basis Spline Set
		 * 	- Ck = 2, Segment Curvature Penalty = 2
		 * 	- Quadratic Rational Shape Controller
		 * 	- Natural Boundary Setting
		 */

		LocalControlCurveParams lccpAkima = new LocalControlCurveParams (
			LocalMonotoneCkGenerator.C1_AKIMA,
			LatentStateStatic.DISCOUNT_QM_ZERO_RATE,
			new SegmentCustomBuilderControl (
				MultiSegmentSequenceBuilder.BASIS_SPLINE_POLYNOMIAL,
				new PolynomialFunctionSetParams (4),
				SegmentInelasticDesignControl.Create (2, 2),
				new ResponseScalingShapeControl (true, new QuadraticRationalShapeControl (0.)),
				null),
			MultiSegmentSequence.CALIBRATE,
			null,
			null,
			true,
			true
		);

		/*
		 * Set up the Harmonic Local Curve Control parameters as follows:
		 * 	- C1 Harmonic Monotone Smoothener with spurious extrema elimination and monotone filtering
		 * 		applied
		 * 	- Zero Rate Quantification Metric
		 * 	- Cubic Polynomial Basis Spline Set
		 * 	- Ck = 2, Segment Curvature Penalty = 2
		 * 	- Quadratic Rational Shape Controller
		 * 	- Natural Boundary Setting
		 */

		LocalControlCurveParams lccpHarmonic = new LocalControlCurveParams (
			LocalMonotoneCkGenerator.C1_HARMONIC,
			LatentStateStatic.DISCOUNT_QM_ZERO_RATE,
			new SegmentCustomBuilderControl (
				MultiSegmentSequenceBuilder.BASIS_SPLINE_POLYNOMIAL,
				new PolynomialFunctionSetParams (4),
				SegmentInelasticDesignControl.Create (2, 2),
				new ResponseScalingShapeControl (true, new QuadraticRationalShapeControl (0.)),
				null),
			MultiSegmentSequence.CALIBRATE,
			null,
			null,
			true,
			true
		);

		/*
		 * Set up the Hyman 1983 Local Curve Control parameters as follows:
		 * 	- C1 Hyman 1983 Monotone Smoothener with spurious extrema elimination and monotone filtering
		 * 		applied
		 * 	- Zero Rate Quantification Metric
		 * 	- Cubic Polynomial Basis Spline Set
		 * 	- Ck = 2, Segment Curvature Penalty = 2
		 * 	- Quadratic Rational Shape Controller
		 * 	- Natural Boundary Setting
		 */

		LocalControlCurveParams lccpHyman83 = new LocalControlCurveParams (
			LocalMonotoneCkGenerator.C1_HYMAN83,
			LatentStateStatic.DISCOUNT_QM_ZERO_RATE,
			new SegmentCustomBuilderControl (
				MultiSegmentSequenceBuilder.BASIS_SPLINE_POLYNOMIAL,
				new PolynomialFunctionSetParams (4),
				SegmentInelasticDesignControl.Create (2, 2),
				new ResponseScalingShapeControl (true, new QuadraticRationalShapeControl (0.)),
				null),
			MultiSegmentSequence.CALIBRATE,
			null,
			null,
			true,
			true
		);

		/*
		 * Set up the Hyman 1989 Local Curve Control parameters as follows:
		 * 	- C1 Hyman 1989 Monotone Smoothener with spurious extrema elimination and monotone filtering
		 * 		applied
		 * 	- Zero Rate Quantification Metric
		 * 	- Cubic Polynomial Basis Spline Set
		 * 	- Ck = 2, Segment Curvature Penalty = 2
		 * 	- Quadratic Rational Shape Controller
		 * 	- Natural Boundary Setting
		 */

		LocalControlCurveParams lccpHyman89 = new LocalControlCurveParams (
			LocalMonotoneCkGenerator.C1_HYMAN89,
			LatentStateStatic.DISCOUNT_QM_ZERO_RATE,
			new SegmentCustomBuilderControl (
				MultiSegmentSequenceBuilder.BASIS_SPLINE_POLYNOMIAL,
				new PolynomialFunctionSetParams (4),
				SegmentInelasticDesignControl.Create (2, 2),
				new ResponseScalingShapeControl (true, new QuadraticRationalShapeControl (0.)),
				null),
			MultiSegmentSequence.CALIBRATE,
			null,
			null,
			true,
			true
		);

		/*
		 * Set up the Huynh-LeFloch Limiter Local Curve Control parameters as follows:
		 * 	- C1 Huynh-LeFloch Limiter Monotone Smoothener with spurious extrema elimination and monotone
		 * 		filtering applied
		 * 	- Zero Rate Quantification Metric
		 * 	- Cubic Polynomial Basis Spline Set
		 * 	- Ck = 2, Segment Curvature Penalty = 2
		 * 	- Quadratic Rational Shape Controller
		 * 	- Natural Boundary Setting
		 */

		LocalControlCurveParams lccpHuynhLeFloch = new LocalControlCurveParams (
			LocalMonotoneCkGenerator.C1_HUYNH_LE_FLOCH,
			LatentStateStatic.DISCOUNT_QM_ZERO_RATE,
			new SegmentCustomBuilderControl (
				MultiSegmentSequenceBuilder.BASIS_SPLINE_POLYNOMIAL,
				new PolynomialFunctionSetParams (4),
				SegmentInelasticDesignControl.Create (2, 2),
				new ResponseScalingShapeControl (true, new QuadraticRationalShapeControl (0.)),
				null),
			MultiSegmentSequence.CALIBRATE,
			null,
			null,
			true,
			true
		);

		/*
		 * Set up the Kruger Local Curve Control parameters as follows:
		 * 	- C1 Kruger Monotone Smoothener with spurious extrema elimination and monotone filtering applied
		 * 	- Zero Rate Quantification Metric
		 * 	- Cubic Polynomial Basis Spline Set
		 * 	- Ck = 2, Segment Curvature Penalty = 2
		 * 	- Quadratic Rational Shape Controller
		 * 	- Natural Boundary Setting
		 */

		LocalControlCurveParams lccpKruger = new LocalControlCurveParams (
			LocalMonotoneCkGenerator.C1_KRUGER,
			LatentStateStatic.DISCOUNT_QM_ZERO_RATE,
			new SegmentCustomBuilderControl (
				MultiSegmentSequenceBuilder.BASIS_SPLINE_POLYNOMIAL,
				new PolynomialFunctionSetParams (4),
				SegmentInelasticDesignControl.Create (2, 2),
				new ResponseScalingShapeControl (true, new QuadraticRationalShapeControl (0.)),
				null),
			MultiSegmentSequence.CALIBRATE,
			null,
			null,
			true,
			true
		);

		ValuationParams valParams = new ValuationParams (dtSpot, dtSpot, strCurrency);

		/*
		 * Construct the Shape Preserving Discount Curve by applying the linear curve calibrator to the array
		 *  of Deposit and OIS Stretches.
		 */

		DiscountCurve dcShapePreserving = ScenarioDiscountCurveBuilder.ShapePreservingDFBuild (
			lcc,
			aStretchSpec,
			valParams,
			null,
			null,
			null,
			1.
		);

		/*
		 * Construct the Akima Locally Smoothened Discount Curve by applying the linear curve calibrator and
		 * 	the Local Curve Control parameters to the array of Deposit and OIS Stretches and the shape
		 * 	preserving discount curve.
		 */

		DiscountCurve dcLocalAkima = ScenarioDiscountCurveBuilder.SmoothingLocalControlBuild (
			dcShapePreserving,
			lcc,
			lccpAkima,
			valParams,
			null,
			null,
			null
		);

		/*
		 * Construct the Harmonic Locally Smoothened Discount Curve by applying the linear curve calibrator
		 * 	and the Local Curve Control parameters to the array of Deposit and OIS Stretches and the shape
		 * 	preserving discount curve.
		 */

		DiscountCurve dcLocalHarmonic = ScenarioDiscountCurveBuilder.SmoothingLocalControlBuild (
			dcShapePreserving,
			lcc,
			lccpHarmonic,
			valParams,
			null,
			null,
			null
		);

		/*
		 * Construct the Hyman 1983 Locally Smoothened Discount Curve by applying the linear curve calibrator
		 * 	and the Local Curve Control parameters to the array of Deposit and OIS Stretches and the shape
		 * 	preserving discount curve.
		 */

		DiscountCurve dcLocalHyman83 = ScenarioDiscountCurveBuilder.SmoothingLocalControlBuild (
			dcShapePreserving,
			lcc,
			lccpHyman83,
			valParams,
			null,
			null,
			null
		);

		/*
		 * Construct the Hyman 1989 Locally Smoothened OIS Discount Curve by applying the linear curve calibrator
		 * 	and the Local Curve Control parameters to the array of Deposit and OIS Stretches and the shape
		 * 	preserving discount curve.
		 */

		DiscountCurve dcLocalHyman89 = ScenarioDiscountCurveBuilder.SmoothingLocalControlBuild (
			dcShapePreserving,
			lcc,
			lccpHyman89,
			valParams,
			null,
			null,
			null
		);

		/*
		 * Construct the Huynh-Le Floch delimited Locally Smoothened OIS Discount Curve by applying the linear
		 * 	curve calibrator and the Local Curve Control parameters to the array of Deposit and OIS Stretches
		 * 	and the shape preserving discount curve.
		 */

		DiscountCurve dcLocalHuynhLeFloch = ScenarioDiscountCurveBuilder.SmoothingLocalControlBuild (
			dcShapePreserving,
			lcc,
			lccpHuynhLeFloch,
			valParams,
			null,
			null,
			null
		);

		/*
		 * Construct the Kruger Locally Smoothened OIS Discount Curve by applying the linear curve calibrator and
		 *  the Local Curve Control parameters to the array of Deposit and OIS Stretches and the shape
		 * 	preserving discount curve.
		 */

		DiscountCurve dcLocalKruger = ScenarioDiscountCurveBuilder.SmoothingLocalControlBuild (
			dcShapePreserving,
			lcc,
			lccpKruger,
			valParams,
			null,
			null,
			null
		);

		/*
		 * Cross-Comparison of the Deposit Calibration Instrument "Rate" metric across the different curve
		 * 	construction methodologies.
		 */

		System.out.println ("\n\t-------------------------------------------------------------------------------------------------------------------------------------------");

		System.out.println ("\t--------------------------------------------------------------------------------------------------------------------------------------------");

		System.out.println ("\t                                                DEPOSIT INSTRUMENTS CALIBRATION RECOVERY");

		System.out.println ("\t--------------------------------------------------------------------------------------------------------------------------------------------");

		System.out.println ("\t        SHAPE PRESERVING   |  LOCAL AKIMA  | LOCAL HARMONIC | LOCAL HYMAN83 | LOCAL HYMAN89 | LOCAL HUYNHLF | LOCAL KRUGER  |  INPUT QUOTE  ");

		System.out.println ("\t--------------------------------------------------------------------------------------------------------------------------------------------");

		System.out.println ("\t--------------------------------------------------------------------------------------------------------------------------------------------");

		for (int i = 0; i < aDeposit.length; ++i)
			System.out.println ("\t[" + aDeposit[i].maturityDate() + "] = " +
				FormatUtil.FormatDouble (
					aDeposit[i].measureValue (
						valParams, null,
						MarketParamsBuilder.Create (dcShapePreserving, null, null, null, null, null, null),
						null,
						"Rate"),
					1, 6, 1.) + "   |   " +
				FormatUtil.FormatDouble (
					aDeposit[i].measureValue (
						valParams, null,
						MarketParamsBuilder.Create (dcLocalAkima, null, null, null, null, null, null),
						null,
						"Rate"),
					1, 6, 1.) + "   |   " +
				FormatUtil.FormatDouble (
					aDeposit[i].measureValue (
						valParams, null,
						MarketParamsBuilder.Create (dcLocalHarmonic, null, null, null, null, null, null),
						null,
						"Rate"),
					1, 6, 1.) + "    |   " +
				FormatUtil.FormatDouble (
					aDeposit[i].measureValue (
						valParams, null,
						MarketParamsBuilder.Create (dcLocalHyman83, null, null, null, null, null, null),
						null,
						"Rate"),
					1, 6, 1.) + "   |   " +
				FormatUtil.FormatDouble (
					aDeposit[i].measureValue (
						valParams, null,
						MarketParamsBuilder.Create (dcLocalHyman89, null, null, null, null, null, null),
						null,
						"Rate"),
					1, 6, 1.) + "   |   " +
				FormatUtil.FormatDouble (
					aDeposit[i].measureValue (
						valParams, null,
						MarketParamsBuilder.Create (dcLocalHuynhLeFloch, null, null, null, null, null, null),
						null,
						"Rate"),
					1, 6, 1.) + "   |   " +
				FormatUtil.FormatDouble (
					aDeposit[i].measureValue (
						valParams, null,
						MarketParamsBuilder.Create (dcLocalKruger, null, null, null, null, null, null),
						null,
						"Rate"),
					1, 6, 1.) + "   |   " +
				FormatUtil.FormatDouble (adblDepositQuote[i], 1, 6, 1.)
			);

		/*
		 * Cross-Comparison of the Short End OIS Calibration Instrument "Rate" metric across the different curve
		 * 	construction methodologies.
		 */

		System.out.println ("\n\t--------------------------------------------------------------------------------------------------------------------------------------------");

		System.out.println ("\t--------------------------------------------------------------------------------------------------------------------------------------------");

		System.out.println ("\t                                                 SHORT END OIS INSTRUMENTS CALIBRATION RECOVERY");

		System.out.println ("\t--------------------------------------------------------------------------------------------------------------------------------------------");

		System.out.println ("\t        SHAPE PRESERVING   |  LOCAL AKIMA  | LOCAL HARMONIC | LOCAL HYMAN83 | LOCAL HYMAN89 | LOCAL HUYNHLF | LOCAL KRUGER  |  INPUT QUOTE  ");

		System.out.println ("\t--------------------------------------------------------------------------------------------------------------------------------------------");

		System.out.println ("\t--------------------------------------------------------------------------------------------------------------------------------------------");

		for (int i = 0; i < aShortEndOISComp.length; ++i)
			System.out.println ("\t[" + aShortEndOISComp[i].maturityDate() + "] = " +
				FormatUtil.FormatDouble (
					aShortEndOISComp[i].measureValue (
						valParams, null,
						MarketParamsBuilder.Create (dcShapePreserving, null, null, null, null, null, null),
						null,
						"CalibSwapRate"),
					1, 6, 1.) + "   |   " +
				FormatUtil.FormatDouble (
					aShortEndOISComp[i].measureValue (
						valParams, null,
						MarketParamsBuilder.Create (dcLocalAkima, null, null, null, null, null, null),
						null,
						"CalibSwapRate"),
					1, 6, 1.) + "   |   " +
				FormatUtil.FormatDouble (
					aShortEndOISComp[i].measureValue (
						valParams, null,
						MarketParamsBuilder.Create (dcLocalHarmonic, null, null, null, null, null, null),
						null,
						"CalibSwapRate"),
					1, 6, 1.) + "    |   " +
				FormatUtil.FormatDouble (
					aShortEndOISComp[i].measureValue (
						valParams, null,
						MarketParamsBuilder.Create (dcLocalHyman83, null, null, null, null, null, null),
						null,
						"CalibSwapRate"),
					1, 6, 1.) + "   |   " +
				FormatUtil.FormatDouble (
					aShortEndOISComp[i].measureValue (
						valParams, null,
						MarketParamsBuilder.Create (dcLocalHyman89, null, null, null, null, null, null),
						null,
						"CalibSwapRate"),
					1, 6, 1.) + "   |   " +
				FormatUtil.FormatDouble (
					aShortEndOISComp[i].measureValue (
						valParams, null,
						MarketParamsBuilder.Create (dcLocalHuynhLeFloch, null, null, null, null, null, null),
						null,
						"CalibSwapRate"),
					1, 6, 1.) + "   |   " +
				FormatUtil.FormatDouble (
					aShortEndOISComp[i].measureValue (
						valParams, null,
						MarketParamsBuilder.Create (dcLocalKruger, null, null, null, null, null, null),
						null,
						"CalibSwapRate"),
					1, 6, 1.) + "   |   " +
				FormatUtil.FormatDouble (adblShortEndOISQuote[i], 1, 6, 1.)
			);

		/*
		 * Cross-Comparison of the OIS Future Calibration Instrument "Rate" metric across the different curve
		 * 	construction methodologies.
		 */

		System.out.println ("\n\t--------------------------------------------------------------------------------------------------------------------------------------------");

		System.out.println ("\t--------------------------------------------------------------------------------------------------------------------------------------------");

		System.out.println ("\t                                                 OIS FUTURE INSTRUMENTS CALIBRATION RECOVERY");

		System.out.println ("\t--------------------------------------------------------------------------------------------------------------------------------------------");

		System.out.println ("\t        SHAPE PRESERVING   |  LOCAL AKIMA  | LOCAL HARMONIC | LOCAL HYMAN83 | LOCAL HYMAN89 | LOCAL HUYNHLF | LOCAL KRUGER  |  INPUT QUOTE  ");

		System.out.println ("\t--------------------------------------------------------------------------------------------------------------------------------------------");

		System.out.println ("\t--------------------------------------------------------------------------------------------------------------------------------------------");

		for (int i = 0; i < aOISFutureComp.length; ++i)
			System.out.println ("\t[" + aOISFutureComp[i].maturityDate() + "] = " +
				FormatUtil.FormatDouble (
					aOISFutureComp[i].measureValue (
						valParams, null,
						MarketParamsBuilder.Create (dcShapePreserving, null, null, null, null, null, null),
						null,
						"SwapRate"),
					1, 6, 1.) + "   |   " +
				FormatUtil.FormatDouble (
					aOISFutureComp[i].measureValue (
						valParams, null,
						MarketParamsBuilder.Create (dcLocalAkima, null, null, null, null, null, null),
						null,
						"SwapRate"),
					1, 6, 1.) + "   |   " +
				FormatUtil.FormatDouble (
					aOISFutureComp[i].measureValue (
						valParams, null,
						MarketParamsBuilder.Create (dcLocalHarmonic, null, null, null, null, null, null),
						null,
						"SwapRate"),
					1, 6, 1.) + "    |   " +
				FormatUtil.FormatDouble (
					aOISFutureComp[i].measureValue (
						valParams, null,
						MarketParamsBuilder.Create (dcLocalHyman83, null, null, null, null, null, null),
						null,
						"SwapRate"),
					1, 6, 1.) + "   |   " +
				FormatUtil.FormatDouble (
					aOISFutureComp[i].measureValue (
						valParams, null,
						MarketParamsBuilder.Create (dcLocalHyman89, null, null, null, null, null, null),
						null,
						"SwapRate"),
					1, 6, 1.) + "   |   " +
				FormatUtil.FormatDouble (
					aOISFutureComp[i].measureValue (
						valParams, null,
						MarketParamsBuilder.Create (dcLocalHuynhLeFloch, null, null, null, null, null, null),
						null,
						"SwapRate"),
					1, 6, 1.) + "   |   " +
				FormatUtil.FormatDouble (
					aOISFutureComp[i].measureValue (
						valParams, null,
						MarketParamsBuilder.Create (dcLocalKruger, null, null, null, null, null, null),
						null,
						"SwapRate"),
					1, 6, 1.) + "   |   " +
				FormatUtil.FormatDouble (adblOISFutureQuote[i], 1, 6, 1.)
			);

		System.out.println ("\n\t--------------------------------------------------------------------------------------------------------------------------------------------");

		System.out.println ("\t--------------------------------------------------------------------------------------------------------------------------------------------");

		System.out.println ("\t                                                 LONG END OIS INSTRUMENTS CALIBRATION RECOVERY");

		System.out.println ("\t--------------------------------------------------------------------------------------------------------------------------------------------");

		System.out.println ("\t        SHAPE PRESERVING   |  LOCAL AKIMA  | LOCAL HARMONIC | LOCAL HYMAN83 | LOCAL HYMAN89 | LOCAL HUYNHLF | LOCAL KRUGER  |  INPUT QUOTE  ");

		System.out.println ("\t--------------------------------------------------------------------------------------------------------------------------------------------");

		System.out.println ("\t--------------------------------------------------------------------------------------------------------------------------------------------");

		for (int i = 0; i < aLongEndOISComp.length; ++i)
			System.out.println ("\t[" + aLongEndOISComp[i].maturityDate() + "] = " +
				FormatUtil.FormatDouble (
					aLongEndOISComp[i].measureValue (
						valParams, null,
						MarketParamsBuilder.Create (dcShapePreserving, null, null, null, null, null, null),
						null,
						"CalibSwapRate"),
					1, 6, 1.) + "   |   " +
				FormatUtil.FormatDouble (
					aLongEndOISComp[i].measureValue (
						valParams, null,
						MarketParamsBuilder.Create (dcLocalAkima, null, null, null, null, null, null),
						null,
						"CalibSwapRate"),
					1, 6, 1.) + "   |   " +
				FormatUtil.FormatDouble (
					aLongEndOISComp[i].measureValue (
						valParams, null,
						MarketParamsBuilder.Create (dcLocalHarmonic, null, null, null, null, null, null),
						null,
						"CalibSwapRate"),
					1, 6, 1.) + "    |   " +
				FormatUtil.FormatDouble (
					aLongEndOISComp[i].measureValue (
						valParams, null,
						MarketParamsBuilder.Create (dcLocalHyman83, null, null, null, null, null, null),
						null,
						"CalibSwapRate"),
					1, 6, 1.) + "   |   " +
				FormatUtil.FormatDouble (
					aLongEndOISComp[i].measureValue (
						valParams, null,
						MarketParamsBuilder.Create (dcLocalHyman89, null, null, null, null, null, null),
						null,
						"CalibSwapRate"),
					1, 6, 1.) + "   |   " +
				FormatUtil.FormatDouble (
					aLongEndOISComp[i].measureValue (
						valParams, null,
						MarketParamsBuilder.Create (dcLocalHuynhLeFloch, null, null, null, null, null, null),
						null,
						"CalibSwapRate"),
					1, 6, 1.) + "   |   " +
				FormatUtil.FormatDouble (
					aLongEndOISComp[i].measureValue (
						valParams, null,
						MarketParamsBuilder.Create (dcLocalKruger, null, null, null, null, null, null),
						null,
						"CalibSwapRate"),
					1, 6, 1.) + "   |   " +
				FormatUtil.FormatDouble (adblLongEndOISQuote[i], 1, 6, 1.)
			);

		/*
		 * Cross-Comparison of the OIS Calibration Instrument "Rate" metric across the different curve
		 * 	construction methodologies for a sequence of bespoke OIS instruments.
		 */

		CalibratableFixedIncomeComponent[] aCC = OvernightIndexFromMaturityTenor (
			dtSpot,
			new java.lang.String[] {"3Y", "6Y", "9Y", "12Y", "15Y", "18Y", "21Y", "24Y", "27Y", "30Y"},
			new double[] {0.01, 0.01, 0.01, 0.01, 0.01, 0.01, 0.01, 0.01, 0.01, 0.01},
			strCurrency
		);

		System.out.println ("\n\t--------------------------------------------------------------------------------------------------------------------------------");

		System.out.println ("\t--------------------------------------------------------------------------------------------------------------------------------");

		System.out.println ("\t                                                BESPOKE OIS PAR RATE");

		System.out.println ("\t--------------------------------------------------------------------------------------------------------------------------------");

		System.out.println ("\t        SHAPE PRESERVING   |  LOCAL AKIMA  | LOCAL HARMONIC | LOCAL HYMAN83  | LOCAL HYMAN89  | LOCAL HUYNHLF  | LOCAL KRUGER ");

		System.out.println ("\t--------------------------------------------------------------------------------------------------------------------------------");

		System.out.println ("\t--------------------------------------------------------------------------------------------------------------------------------");

		for (int i = 0; i < aCC.length; ++i)
			System.out.println ("\t[" + aCC[i].maturityDate() + "] = " +
				FormatUtil.FormatDouble (
					aCC[i].measureValue (valParams, null,
					MarketParamsBuilder.Create (dcShapePreserving, null, null, null, null, null, null),
					null,
					"CalibSwapRate"),
				1, 6, 1.) + "   |   " +
				FormatUtil.FormatDouble (
					aCC[i].measureValue (valParams, null,
					MarketParamsBuilder.Create (dcLocalAkima, null, null, null, null, null, null),
					null,
					"CalibSwapRate"),
				1, 6, 1.) + "   |   " +
				FormatUtil.FormatDouble (
					aCC[i].measureValue (valParams, null,
					MarketParamsBuilder.Create (dcLocalHarmonic, null, null, null, null, null, null),
					null,
					"CalibSwapRate"),
				1, 6, 1.) + "    |   " +
				FormatUtil.FormatDouble (
					aCC[i].measureValue (valParams, null,
					MarketParamsBuilder.Create (dcLocalHyman83, null, null, null, null, null, null),
					null,
					"CalibSwapRate"),
					1, 6, 1.) + "    |   " +
				FormatUtil.FormatDouble (
					aCC[i].measureValue (valParams, null,
					MarketParamsBuilder.Create (dcLocalHyman89, null, null, null, null, null, null),
					null,
					"CalibSwapRate"),
					1, 6, 1.) + "    |   " +
				FormatUtil.FormatDouble (
					aCC[i].measureValue (valParams, null,
					MarketParamsBuilder.Create (dcLocalHuynhLeFloch, null, null, null, null, null, null),
					null,
					"CalibSwapRate"),
					1, 6, 1.) + "    |   " +
				FormatUtil.FormatDouble (
					aCC[i].measureValue (valParams, null,
					MarketParamsBuilder.Create (dcLocalKruger, null, null, null, null, null, null),
					null,
					"CalibSwapRate"),
				1, 6, 1.)
			);
	}

	public static final void main (
		final String[] astrArgs)
		throws Exception
	{
		/*
		 * Initialize the Credit Analytics Library
		 */

		CreditAnalytics.Init ("");

		JulianDate dtSpot = JulianDate.Today();

		ShapeOISDFZeroLocalSmoothSample (
			dtSpot,
			"---- DISCOUNT CURVE WITH OVERNIGHT INDEX ---",
			"EUR"
		);
	}
}
