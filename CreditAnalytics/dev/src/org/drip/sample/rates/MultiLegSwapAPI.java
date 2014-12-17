
package org.drip.sample.rates;

/*
 * Credit Analytics Imports
 */

import java.util.List;

import org.drip.analytics.date.JulianDate;
import org.drip.analytics.daycount.*;
import org.drip.analytics.rates.DiscountCurve;
import org.drip.analytics.support.*;
import org.drip.market.definition.IBORIndexContainer;
import org.drip.param.creator.*;
import org.drip.param.market.CurveSurfaceQuoteSet;
import org.drip.param.period.*;
import org.drip.param.valuation.*;
import org.drip.product.definition.CalibratableFixedIncomeComponent;
import org.drip.product.params.CurrencyPair;
import org.drip.product.rates.*;
import org.drip.quant.function1D.FlatUnivariate;
import org.drip.service.api.CreditAnalytics;
import org.drip.state.creator.DiscountCurveBuilder;
import org.drip.state.identifier.*;

/*!
 * Copyright (C) 2014 Lakshmi Krishnamurthy
 * Copyright (C) 2013 Lakshmi Krishnamurthy
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

/**
 * MultiLegSwapAPI illustrates the creation, invocation, and usage of the MultiLegSwap. It shows how to:
 * 	- Create the Discount Curve from the rates instruments.
 *  - Set up the valuation and the market parameters.
 * 	- Create the Rates Basket from the fixed/float streams.
 * 	- Value the Rates Basket.
 * 
 * @author Lakshmi Krishnamurthy
 */

public class MultiLegSwapAPI {

	/*
	 * Sample demonstrating building of rates curve from deposit/future/swaps
	 * 
	 *  	USE WITH CARE: This sample ignores errors and does not handle exceptions.
	 */

	private static DiscountCurve BuildRatesCurveFromInstruments (
		final JulianDate dtStart,
		final String[] astrDepositTenor,
		final double[] adblDepositRate,
		final String[] astrIRSTenor,
		final double[] adblIRSRate,
		final double dblBump,
		final String strCurrency)
		throws Exception
	{
		int iNumDCInstruments = astrDepositTenor.length + adblIRSRate.length;
		double adblDate[] = new double[iNumDCInstruments];
		double adblRate[] = new double[iNumDCInstruments];
		String astrCalibMeasure[] = new String[iNumDCInstruments];
		double adblCompCalibValue[] = new double[iNumDCInstruments];
		CalibratableFixedIncomeComponent aCompCalib[] = new CalibratableFixedIncomeComponent[iNumDCInstruments];

		// Deposit Calibration

		UnitCouponAccrualSetting ucasDeposit = new UnitCouponAccrualSetting (
			4,
			"Act/360",
			false,
			"Act/360",
			false,
			strCurrency,
			false
		);

		ComposableFloatingUnitSetting cfusDeposit = new ComposableFloatingUnitSetting (
			"3M",
			CompositePeriodBuilder.EDGE_DATE_SEQUENCE_SINGLE,
			null,
			IBORIndexContainer.IndexFromJurisdiction (strCurrency).ForwardStateLabel ("3M"),
			CompositePeriodBuilder.REFERENCE_PERIOD_IN_ADVANCE,
			null,
			0.
		);

		CompositePeriodSetting cpsDeposit = new CompositePeriodSetting (
			4,
			"3M",
			strCurrency,
			null,
			CompositePeriodBuilder.ACCRUAL_COMPOUNDING_RULE_GEOMETRIC,
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

		for (int i = 0; i < astrDepositTenor.length; ++i) {
			astrCalibMeasure[i] = "Rate";
			adblRate[i] = java.lang.Double.NaN;
			adblCompCalibValue[i] = adblDepositRate[i] + dblBump;

			aCompCalib[i] = new SingleStreamComponent (
				"DEPOSIT_" + astrDepositTenor[i],
				new Stream (
					CompositePeriodBuilder.FloatingCompositeUnit (
						CompositePeriodBuilder.EdgePair (
							dtStart,
							new JulianDate (adblDate[i] = dtStart.addTenor (astrDepositTenor[i]).julian())
						),
						cpsDeposit,
						ucasDeposit,
						cfusDeposit
					)
				),
				csp
			);

			aCompCalib[i].setPrimaryCode (astrDepositTenor[i]);
		}

		// IRS Calibration

		UnitCouponAccrualSetting ucasFloating = new UnitCouponAccrualSetting (
			2,
			"Act/360",
			false,
			"Act/360",
			false,
			strCurrency,
			true
		);

		UnitCouponAccrualSetting ucasFixed = new UnitCouponAccrualSetting (
			2,
			"Act/360",
			false,
			"Act/360",
			false,
			strCurrency,
			true
		);

		ComposableFloatingUnitSetting cfusFloating = new ComposableFloatingUnitSetting (
			"6M",
			CompositePeriodBuilder.EDGE_DATE_SEQUENCE_REGULAR,
			null,
			IBORIndexContainer.IndexFromJurisdiction (strCurrency).ForwardStateLabel ("6M"),
			CompositePeriodBuilder.REFERENCE_PERIOD_IN_ADVANCE,
			null,
			0.
		);

		ComposableFixedUnitSetting cfusFixed = new ComposableFixedUnitSetting (
			"6M",
			CompositePeriodBuilder.EDGE_DATE_SEQUENCE_REGULAR,
			null,
			0.,
			0.,
			strCurrency
		);

		CompositePeriodSetting cpsFloating = new CompositePeriodSetting (
			2,
			"6M",
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
			"6M",
			strCurrency,
			null,
			CompositePeriodBuilder.ACCRUAL_COMPOUNDING_RULE_GEOMETRIC,
			1.,
			null,
			null,
			null,
			null
		);

		for (int i = 0; i < astrIRSTenor.length; ++i) {
			astrCalibMeasure[i + astrDepositTenor.length] = "Rate";
			adblRate[i + astrDepositTenor.length] = java.lang.Double.NaN;
			adblCompCalibValue[i + astrDepositTenor.length] = adblIRSRate[i] + dblBump;

			List<Double> lsFixedStreamEdgeDate = CompositePeriodBuilder.RegularEdgeDates (
				dtStart,
				"6M",
				astrIRSTenor[i],
				null
			);

			List<Double> lsFloatingStreamEdgeDate = CompositePeriodBuilder.RegularEdgeDates (
				dtStart,
				"6M",
				astrIRSTenor[i],
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

			FixFloatComponent irs = new FixFloatComponent (
				fixedStream,
				floatingStream,
				csp
			);

			irs.setPrimaryCode ("IRS." + astrIRSTenor[i] + "." + strCurrency);

			aCompCalib[i + astrDepositTenor.length] = irs;
		}

		/*
		 * Build the IR curve from the components, their calibration measures, and their calibration quotes.
		 */

		return ScenarioDiscountCurveBuilder.NonlinearBuild (
			dtStart,
			strCurrency,
			DiscountCurveBuilder.BOOTSTRAP_MODE_CONSTANT_FORWARD,
			aCompCalib,
			adblCompCalibValue,
			astrCalibMeasure,
			null
		);
	}

	/*
	 * Sample demonstrating creation of a rates basket instance from component fixed and floating streams
	 * 
	 *  	USE WITH CARE: This sample ignores errors and does not handle exceptions.
	 */

	private static final RatesBasket MakeRatesBasket (
		final JulianDate dtEffective)
		throws Exception
	{
		/*
		 * Create a sequence of Fixed Streams
		 */

		Stream[] aFixedStream = new Stream[3];

		UnitCouponAccrualSetting ucasFixed = new UnitCouponAccrualSetting (
			2,
			"Act/360",
			false,
			"Act/360",
			false,
			"USD",
			false
		);

		ComposableFixedUnitSetting cfusFixed3Y = new ComposableFixedUnitSetting (
			"6M",
			CompositePeriodBuilder.EDGE_DATE_SEQUENCE_REGULAR,
			null,
			0.03,
			0.,
			"USD"
		);

		ComposableFixedUnitSetting cfusFixed5Y = new ComposableFixedUnitSetting (
			"6M",
			CompositePeriodBuilder.EDGE_DATE_SEQUENCE_REGULAR,
			null,
			0.05,
			0.,
			"USD"
		);

		ComposableFixedUnitSetting cfusFixed7Y = new ComposableFixedUnitSetting (
			"6M",
			CompositePeriodBuilder.EDGE_DATE_SEQUENCE_REGULAR,
			null,
			0.07,
			0.,
			"USD"
		);

		CompositePeriodSetting cpsFixed = new CompositePeriodSetting (
			2,
			"6M",
			"USD",
			null,
			CompositePeriodBuilder.ACCRUAL_COMPOUNDING_RULE_GEOMETRIC,
			1.,
			null,
			null,
			null,
			null
		);

		aFixedStream[0] = new Stream (
			CompositePeriodBuilder.FixedCompositeUnit (
				CompositePeriodBuilder.RegularEdgeDates (
					dtEffective,
					"6M",
					"3Y",
					null
				),
				cpsFixed,
				ucasFixed,
				cfusFixed3Y
			)
		);

		aFixedStream[1] = new Stream (
			CompositePeriodBuilder.FixedCompositeUnit (
				CompositePeriodBuilder.RegularEdgeDates (
					dtEffective,
					"6M",
					"5Y",
					null
				),
				cpsFixed,
				ucasFixed,
				cfusFixed5Y
			)
		);

		aFixedStream[2] = new Stream (
			CompositePeriodBuilder.FixedCompositeUnit (
				CompositePeriodBuilder.RegularEdgeDates (
					dtEffective,
					"6M",
					"7Y",
					null
				),
				cpsFixed,
				ucasFixed,
				cfusFixed7Y
			)
		);

		/*
		 * Create a sequence of Float Streams
		 */

		Stream[] aFloatStream = new Stream[3];

		UnitCouponAccrualSetting ucasFloat = new UnitCouponAccrualSetting (
			4,
			"Act/360",
			false,
			"Act/360",
			false,
			"USD",
			false
		);

		ComposableFloatingUnitSetting cfusFloat3Y = new ComposableFloatingUnitSetting (
			"3M",
			CompositePeriodBuilder.EDGE_DATE_SEQUENCE_SINGLE,
			null,
			IBORIndexContainer.IndexFromJurisdiction ("USD").ForwardStateLabel ("3M"),
			CompositePeriodBuilder.REFERENCE_PERIOD_IN_ADVANCE,
			null,
			0.03
		);

		ComposableFloatingUnitSetting cfusFloat5Y = new ComposableFloatingUnitSetting (
			"3M",
			CompositePeriodBuilder.EDGE_DATE_SEQUENCE_SINGLE,
			null,
			IBORIndexContainer.IndexFromJurisdiction ("USD").ForwardStateLabel ("3M"),
			CompositePeriodBuilder.REFERENCE_PERIOD_IN_ADVANCE,
			null,
			0.05
		);

		ComposableFloatingUnitSetting cfusFloat7Y = new ComposableFloatingUnitSetting (
			"3M",
			CompositePeriodBuilder.EDGE_DATE_SEQUENCE_SINGLE,
			null,
			IBORIndexContainer.IndexFromJurisdiction ("USD").ForwardStateLabel ("3M"),
			CompositePeriodBuilder.REFERENCE_PERIOD_IN_ADVANCE,
			null,
			0.07
		);

		CompositePeriodSetting cpsFloat = new CompositePeriodSetting (
			4,
			"3M",
			"USD",
			null,
			CompositePeriodBuilder.ACCRUAL_COMPOUNDING_RULE_GEOMETRIC,
			1.,
			null,
			null,
			null,
			null
		);

		aFloatStream[0] = new Stream (
			CompositePeriodBuilder.FloatingCompositeUnit (
				CompositePeriodBuilder.RegularEdgeDates (
					dtEffective,
					"6M",
					"3Y",
					null
				),
				cpsFloat,
				ucasFloat,
				cfusFloat3Y
			)
		);

		aFloatStream[1] = new Stream (
			CompositePeriodBuilder.FloatingCompositeUnit (
				CompositePeriodBuilder.RegularEdgeDates (
					dtEffective,
					"6M",
					"5Y",
					null
				),
				cpsFloat,
				ucasFloat,
				cfusFloat5Y
			)
		);

		aFloatStream[2] = new Stream (
			CompositePeriodBuilder.FloatingCompositeUnit (
				CompositePeriodBuilder.RegularEdgeDates (
					dtEffective,
					"6M",
					"7Y",
					null
				),
				cpsFloat,
				ucasFloat,
				cfusFloat7Y
			)
		);

		/*
		 * Create a Rates Basket instance containing the fixed and floating streams
		 */

		return new RatesBasket ("RATESBASKET", aFixedStream, aFloatStream);
	}

	/*
	 * Sample demonstrating creation of discount curve from cash/futures/swaps
	 * 
	 *  	USE WITH CARE: This sample ignores errors and does not handle exceptions.
	 */

	private static final void MultiLegSwapSample()
		throws Exception
	{
		JulianDate dtValue = JulianDate.Today();

		/*
		 * Create the Discount Curve from the rates instruments
		 */

		String[] astrCashTenor = new String[] {"3M"};
		double[] adblCashRate = new double[] {0.00276};
		String[] astrIRSTenor = new String[] {   "1Y",    "2Y",    "3Y",    "4Y",    "5Y",    "6Y",    "7Y",
		   "8Y",    "9Y",   "10Y",   "11Y",   "12Y",   "15Y",   "20Y",   "25Y",   "30Y",   "40Y",   "50Y"};
		double[] adblIRSRate = new double[]  {0.00367, 0.00533, 0.00843, 0.01238, 0.01609, 0.01926, 0.02191,
			0.02406, 0.02588, 0.02741, 0.02870, 0.02982, 0.03208, 0.03372, 0.03445, 0.03484, 0.03501, 0.03484};

		DiscountCurve dc = BuildRatesCurveFromInstruments (
			dtValue,
			astrCashTenor,
			adblCashRate,
			astrIRSTenor,
			adblIRSRate,
			0.,
			"USD"
		);

		/*
		 * Set up the valuation and the market parameters
		 */

		ValuationParams valParams = ValuationParams.CreateValParams (
			dtValue,
			0,
			"",
			Convention.DATE_ROLL_ACTUAL
		);

		double dblUSDABCFXRate = 1.;

		CurveSurfaceQuoteSet mktParams = new CurveSurfaceQuoteSet();

		mktParams.setFundingCurve (dc);

		mktParams.setFXCurve (
			FXLabel.Standard (CurrencyPair.FromCode ("USD/ABC")),
			new FlatUnivariate (dblUSDABCFXRate)
		);

		/*
		 * Create the Rates Basket from the streams
		 */

		RatesBasket rb = MakeRatesBasket (dtValue);

		/*
		 * Value the Rates Basket
		 */

		CaseInsensitiveTreeMap<Double> mapRBResults = rb.value (valParams, null, mktParams, null);

		System.out.println (mapRBResults);
	}

	public static final void main (
		final String[] astrArgs)
		throws Exception
	{
		// String strConfig = "c:\\Lakshmi\\BondAnal\\Config.xml";

		String strConfig = "";

		CreditAnalytics.Init (strConfig);

		MultiLegSwapSample();
	}
}
