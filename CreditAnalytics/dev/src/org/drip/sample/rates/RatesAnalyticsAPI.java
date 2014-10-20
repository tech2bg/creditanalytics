
package org.drip.sample.rates;

/*
 * Credit Product imports
 */

import java.util.List;

import org.drip.analytics.date.JulianDate;
import org.drip.analytics.rates.DiscountCurve;
import org.drip.analytics.support.*;
import org.drip.param.period.*;
import org.drip.param.valuation.*;
import org.drip.product.definition.*;
import org.drip.product.rates.*;
import org.drip.param.creator.*;
import org.drip.product.creator.*;
import org.drip.quant.calculus.WengertJacobian;
import org.drip.quant.common.FormatUtil;
import org.drip.service.api.CreditAnalytics;
import org.drip.state.creator.*;
import org.drip.state.identifier.ForwardLabel;

/*
 * -*- mode: java; tab-width: 4; indent-tabs-mode: nil; c-basic-offset: 4 -*-
 */

/*!
 * Copyright (C) 2014 Lakshmi Krishnamurthy
 * Copyright (C) 2013 Lakshmi Krishnamurthy
 * Copyright (C) 2012 Lakshmi Krishnamurthy
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
 * RatesAnalyticsAPI contains a demo of the Rates Analytics API Usage. It shows the following:
 * 	- Build a discount curve using: cash instruments only, EDF instruments only, IRS instruments only, or all
 * 		of them strung together.
 * 	- Re-calculate the component input measure quotes from the calibrated discount curve object.
 * 	- Compute the PVDF Wengert Jacobian across all the instruments used in the curve construction.
 * 
 * @author Lakshmi Krishnamurthy
 */

public class RatesAnalyticsAPI {

	private static final FixFloatComponent IRS (
		final JulianDate dtEffective,
		final String strCurrency,
		final String strTenor,
		final double dblCoupon)
		throws Exception
	{
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
			CompositePeriodBuilder.EDGE_DATE_SEQUENCE_SINGLE,
			null,
			ForwardLabel.Standard (strCurrency + "-LIBOR-6M"),
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
			CompositePeriodUtil.ACCRUAL_COMPOUNDING_RULE_GEOMETRIC,
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
			CompositePeriodUtil.ACCRUAL_COMPOUNDING_RULE_GEOMETRIC,
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

		List<Double> lsFixedStreamEdgeDate = CompositePeriodBuilder.RegularEdgeDates (
			dtEffective,
			"6M",
			strTenor,
			null
		);

		List<Double> lsFloatingStreamEdgeDate = CompositePeriodBuilder.RegularEdgeDates (
			dtEffective,
			"6M",
			strTenor,
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

		irs.setPrimaryCode ("IRS." + strTenor + "." + strCurrency);

		return irs;
	}

	/**
	 * Sample API demonstrating the creation of the discount curve from the rates input instruments
	 * 
	 *  	USE WITH CARE: This sample ignores errors and does not handle exceptions.
	 */

	public static void DiscountCurveFromRatesInstruments()
		throws Exception
	{
		int NUM_DC_INSTR = 30;
		double adblRate[] = new double[NUM_DC_INSTR];
		double adblMaturity[] = new double[NUM_DC_INSTR];
		String astrCalibMeasure[] = new String[NUM_DC_INSTR];
		double adblCompCalibValue[] = new double[NUM_DC_INSTR];
		CalibratableFixedIncomeComponent aCompCalib[] = new CalibratableFixedIncomeComponent[NUM_DC_INSTR];

		JulianDate dtStart = org.drip.analytics.date.JulianDate.CreateFromYMD (2011, 4, 6);

		// First 7 instruments - cash calibration

		JulianDate dtCashEffective = dtStart.addBusDays (1, "USD");

		adblMaturity[0] = dtCashEffective.addBusDays (1, "USD").julian(); // ON

		adblMaturity[1] = dtCashEffective.addBusDays (2, "USD").julian(); // 1D (TN)

		adblMaturity[2] = dtCashEffective.addBusDays (7, "USD").julian(); // 1W

		adblMaturity[3] = dtCashEffective.addBusDays (14, "USD").julian(); // 2W

		adblMaturity[4] = dtCashEffective.addBusDays (30, "USD").julian(); // 1M

		adblMaturity[5] = dtCashEffective.addBusDays (60, "USD").julian(); // 2M

		adblMaturity[6] = dtCashEffective.addBusDays (90, "USD").julian(); // 3M

		/*
		 * Cash Rate Quotes
		 */

		adblCompCalibValue[0] = .0013;
		adblCompCalibValue[1] = .0017;
		adblCompCalibValue[2] = .0017;
		adblCompCalibValue[3] = .0018;
		adblCompCalibValue[4] = .0020;
		adblCompCalibValue[5] = .0023;
		adblCompCalibValue[6] = .0026;

		UnitCouponAccrualSetting ucas = new UnitCouponAccrualSetting (
			4,
			"Act/360",
			false,
			"Act/360",
			false,
			"USD",
			false
		);

		ComposableFloatingUnitSetting cfus = new ComposableFloatingUnitSetting (
			"3M",
			CompositePeriodBuilder.EDGE_DATE_SEQUENCE_SINGLE,
			null,
			ForwardLabel.Standard ("USD-LIBOR-3M"),
			CompositePeriodBuilder.REFERENCE_PERIOD_IN_ADVANCE,
			null,
			0.
		);

		CompositePeriodSetting cps = new CompositePeriodSetting (
			4,
			"3M",
			"USD",
			null,
			CompositePeriodUtil.ACCRUAL_COMPOUNDING_RULE_GEOMETRIC,
			1.,
			null,
			null,
			null,
			null
		);

		CashSettleParams csp = new CashSettleParams (
			0,
			"USD",
			0
		);

		for (int i = 0; i < 7; ++i) {
			adblRate[i] = 0.01;
			astrCalibMeasure[i] = "Rate";

			aCompCalib[i] = SingleStreamComponentBuilder.CreateDeposit (
				dtCashEffective, // Effective
				new JulianDate (adblMaturity[i]).addBusDays (2, "USD"), // Maturity
				null, // FRI
				"USD"
			);

			aCompCalib[i] = new SingleStreamComponent (
				"DEPOSIT_" + adblMaturity[i],
				new Stream (
					CompositePeriodBuilder.FloatingCompositeUnit (
						CompositePeriodBuilder.EdgePair (
							dtStart,
							new JulianDate (adblMaturity[i]).addBusDays (2, "USD")
						),
						cps,
						ucas,
						cfus
					)
				),
				csp
			);

			aCompCalib[i].setPrimaryCode (aCompCalib[i].name());
		}

		// Next 8 instruments - EDF calibration

		adblCompCalibValue[7] = .0027;
		adblCompCalibValue[8] = .0032;
		adblCompCalibValue[9] = .0041;
		adblCompCalibValue[10] = .0054;
		adblCompCalibValue[11] = .0077;
		adblCompCalibValue[12] = .0104;
		adblCompCalibValue[13] = .0134;
		adblCompCalibValue[14] = .0160;

		CalibratableFixedIncomeComponent[] aEDF = SingleStreamComponentBuilder.GenerateFuturesPack (dtStart, 8, "USD");

		for (int i = 0; i < 8; ++i) {
			adblRate[i + 7] = 0.01;
			aCompCalib[i + 7] = aEDF[i];
			astrCalibMeasure[i + 7] = "Rate";

			adblMaturity[i + 7] = aEDF[i].maturity().julian();
		}

		// Final 15 instruments - IRS calibration

		JulianDate dtIRSEffective = dtStart.addBusDays (2, "USD");

		String[] astrIRSTenor = new String[] {
			"4Y",
			"5Y",
			"6Y",
			"7Y",
			"8Y",
			"9Y",
			"10Y",
			"11Y",
			"12Y",
			"15Y",
			"20Y",
			"25Y",
			"30Y",
			"40Y",
			"50Y",
		};

		adblMaturity[15] = dtIRSEffective.addTenor (astrIRSTenor[0]).julian();

		adblMaturity[16] = dtIRSEffective.addTenor (astrIRSTenor[1]).julian();

		adblMaturity[17] = dtIRSEffective.addTenor (astrIRSTenor[2]).julian();

		adblMaturity[18] = dtIRSEffective.addTenor (astrIRSTenor[3]).julian();

		adblMaturity[19] = dtIRSEffective.addTenor (astrIRSTenor[4]).julian();

		adblMaturity[20] = dtIRSEffective.addTenor (astrIRSTenor[5]).julian();

		adblMaturity[21] = dtIRSEffective.addTenor (astrIRSTenor[6]).julian();

		adblMaturity[22] = dtIRSEffective.addTenor (astrIRSTenor[7]).julian();

		adblMaturity[23] = dtIRSEffective.addTenor (astrIRSTenor[8]).julian();

		adblMaturity[24] = dtIRSEffective.addTenor (astrIRSTenor[9]).julian();

		adblMaturity[25] = dtIRSEffective.addTenor (astrIRSTenor[10]).julian();

		adblMaturity[26] = dtIRSEffective.addTenor (astrIRSTenor[11]).julian();

		adblMaturity[27] = dtIRSEffective.addTenor (astrIRSTenor[12]).julian();

		adblMaturity[28] = dtIRSEffective.addTenor (astrIRSTenor[13]).julian();

		adblMaturity[29] = dtIRSEffective.addTenor (astrIRSTenor[14]).julian();

		adblCompCalibValue[15] = .0166;
		adblCompCalibValue[16] = .0206;
		adblCompCalibValue[17] = .0241;
		adblCompCalibValue[18] = .0269;
		adblCompCalibValue[19] = .0292;
		adblCompCalibValue[20] = .0311;
		adblCompCalibValue[21] = .0326;
		adblCompCalibValue[22] = .0340;
		adblCompCalibValue[23] = .0351;
		adblCompCalibValue[24] = .0375;
		adblCompCalibValue[25] = .0393;
		adblCompCalibValue[26] = .0402;
		adblCompCalibValue[27] = .0407;
		adblCompCalibValue[28] = .0409;
		adblCompCalibValue[29] = .0409;

		for (int i = 0; i < 15; ++i) {
			astrCalibMeasure[i + 15] = "Rate";
			adblRate[i + 15] = 0.01;

			aCompCalib[i + 15] = IRS (
				dtIRSEffective,
				"USD",
				astrIRSTenor[i],
				0.
			);
		}

		/*
		 * Build the IR curve from the components, their calibration measures, and their calibration quotes.
		 */

		DiscountCurve dc = ScenarioDiscountCurveBuilder.NonlinearBuild (
			dtStart,
			"USD",
			DiscountCurveBuilder.BOOTSTRAP_MODE_CONSTANT_FORWARD,
			aCompCalib,
			adblCompCalibValue,
			astrCalibMeasure,
			null
		);

		/*
		 * Re-calculate the component input measure quotes from the calibrated discount curve object
		 */

		for (int i = 0; i < aCompCalib.length; ++i)
			System.out.println (astrCalibMeasure[i] + "[" + i + "] = " +
				FormatUtil.FormatDouble (aCompCalib[i].measureValue (new ValuationParams (dtStart, dtStart, "USD"), null,
					MarketParamsBuilder.Create (dc, null, null, null, null, null, null),
						null, astrCalibMeasure[i]), 1, 5, 1.) + " | " + FormatUtil.FormatDouble (adblCompCalibValue[i], 1, 5, 1.));

		for (int i = 0; i < aCompCalib.length; ++i) {
			WengertJacobian wjComp = aCompCalib[i].jackDDirtyPVDManifestMeasure (
				new ValuationParams (dtStart, dtStart, "USD"),
				null,
				MarketParamsBuilder.Create (dc, null, null, null, null, null, null),
				null
			);

			System.out.println ("PV/DF Micro Jack[" + aCompCalib[i].name() + "]=" +
				(null == wjComp ? null : wjComp.displayString()));
		}
	}

	public static final void main (
		final String astrArgs[])
		throws Exception
	{
		String strConfig = "";

		CreditAnalytics.Init (strConfig);

		DiscountCurveFromRatesInstruments();
	}
}
