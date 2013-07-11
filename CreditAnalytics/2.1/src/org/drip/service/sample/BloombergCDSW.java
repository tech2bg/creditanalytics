
package org.drip.service.sample;

/*
 * Generic imports
 */

import java.util.*;

/*
 * Credit Products imports
 */

import org.drip.analytics.creator.DiscountCurveBuilder;
import org.drip.analytics.date.JulianDate;
import org.drip.analytics.definition.*;
import org.drip.analytics.period.*;
import org.drip.param.creator.*;
import org.drip.param.pricer.PricerParams;
import org.drip.param.valuation.*;
import org.drip.product.definition.*;

/*
 * Credit Analytics API imports
 */

import org.drip.product.creator.*;
import org.drip.service.api.CreditAnalytics;

/*
 * DRIP Math Support
 */

import org.drip.math.common.FormatUtil;

/*
 * -*- mode: java; tab-width: 4; indent-tabs-mode: nil; c-basic-offset: 4 -*-
 */

/*!
 * Copyright (C) 2013 Lakshmi Krishnamurthy
 * Copyright (C) 2012 Lakshmi Krishnamurthy
 * 
 * This file is part of CreditAnalytics, a free-software/open-source library for fixed income analysts and
 * 		developers - http://www.credit-trader.org
 * 
 * CreditAnalytics is a free, full featured, fixed income credit analytics library, developed with a special
 * 		focus towards the needs of the bonds and credit products community.
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
 * Demo of reproduction of the calculations in Bloomberg's CDSW screen
 * 
 * @author Lakshmi Krishnamurthy
 */

public class BloombergCDSW {
	private static final java.lang.String FIELD_SEPARATOR = "   ";

	private static DiscountCurve BuildRatesCurveFromInstruments (
		final JulianDate dtStart,
		final String[] astrCashTenor,
		final double[] adblCashRate,
		final String[] astrIRSTenor,
		final double[] adblIRSRate,
		final double dblBump,
		final String strCurrency)
		throws Exception
	{
		int iNumDCInstruments = astrCashTenor.length + adblIRSRate.length;
		double adblDate[] = new double[iNumDCInstruments];
		double adblRate[] = new double[iNumDCInstruments];
		String astrCalibMeasure[] = new String[iNumDCInstruments];
		double adblCompCalibValue[] = new double[iNumDCInstruments];
		CalibratableComponent aCompCalib[] = new CalibratableComponent[iNumDCInstruments];
		String strIndex = strCurrency + "-LIBOR-3M";

		// Cash Calibration

		for (int i = 0; i < astrCashTenor.length; ++i) {
			astrCalibMeasure[i] = "Rate";
			adblRate[i] = java.lang.Double.NaN;
			adblCompCalibValue[i] = adblCashRate[i] + dblBump;

			aCompCalib[i] = CashBuilder.CreateCash (dtStart.addBusDays (2, strCurrency),
				new JulianDate (adblDate[i] = dtStart.addBusDays (2, strCurrency).addTenor (astrCashTenor[i]).getJulian()),
				strCurrency);
		}

		// IRS Calibration

		for (int i = 0; i < astrIRSTenor.length; ++i) {
			astrCalibMeasure[i + astrCashTenor.length] = "Rate";
			adblRate[i + astrCashTenor.length] = java.lang.Double.NaN;
			adblCompCalibValue[i + astrCashTenor.length] = adblIRSRate[i] + dblBump;

			aCompCalib[i + astrCashTenor.length] = RatesStreamBuilder.CreateIRS (dtStart.addBusDays (2, strCurrency),
				new JulianDate (adblDate[i + astrCashTenor.length] = dtStart.addBusDays (2, strCurrency).addTenor (astrIRSTenor[i]).getJulian()),
				0., strCurrency, strIndex, strCurrency);
		}

		/*
		 * Build the IR curve from the components, their calibration measures, and their calibration quotes.
		 */

		return RatesScenarioCurveBuilder.CreateDiscountCurve (dtStart, strCurrency,
			DiscountCurveBuilder.BOOTSTRAP_MODE_CONSTANT_FORWARD, aCompCalib, adblCompCalibValue, astrCalibMeasure, null);
	}

	private static CreditCurve CreateCreditCurveFromCDS (
		final JulianDate dtStart,
		final double[] adblQuote,
		final String[] astrTenor,
		final DiscountCurve dc,
		final double dblRecovery,
		final String strCCName,
		final double dblStrike,
		final double dblBump)
		throws Exception
	{
		String[] astrCalibMeasure = new String[adblQuote.length];
		CreditDefaultSwap[] aCDS = new CreditDefaultSwap[adblQuote.length];

		for (int i = 0; i < astrTenor.length; ++i) {
			aCDS[i] = CDSBuilder.CreateSNAC (dtStart, astrTenor[i], dblStrike, strCCName);

			astrCalibMeasure[i] = "QuotedSpread";
			adblQuote[i] += dblBump;
		}

		/*
		 * Build the credit curve from the CDS instruments and the fair premium
		 */

		CreditCurve cc = CreditScenarioCurveBuilder.CreateCreditCurve (strCCName, dtStart, aCDS, dc,
			adblQuote, astrCalibMeasure, dblRecovery, false);

		/* for (int i = 0; i < astrTenor.length; ++i)
			System.out.println (aCDS[i].getMaturityDate() + " | " + adblQuote[i] + " | " +
				org.drip.math.common.FormatUtil.FormatDouble (1. - cc.getSurvival
					(aCDS[i].getMaturityDate()), 0, 4, 1.)); */

		return cc;
	}

	private static final void DisplayInstrumentMaturitySurvival (
		final CreditCurve cc)
		throws java.lang.Exception
	{
		CalibratableComponent[] aCDS = cc.getCalibComponents();

		double[] adblQuote = cc.getCompQuotes();

		for (int i = 0; i < aCDS.length; ++i)
			System.out.println (aCDS[i].getMaturityDate() + " | " + adblQuote[i] + " | " +
				org.drip.math.common.FormatUtil.FormatDouble (1. - cc.getSurvival
					(aCDS[i].getMaturityDate()), 0, 4, 1.));
	}

	private static CreditDefaultSwap CreateCDS (
		final JulianDate dtStart,
		final String strTenor,
		final double dblCoupon,
		final String strCCName)
	{
		return CDSBuilder.CreateSNAC (dtStart, strTenor, dblCoupon, strCCName);
	}

	public static void main (
		final String[] astrArgs)
		throws Exception
	{
		CreditAnalytics.Init ("");

		JulianDate dtCurve = JulianDate.CreateFromYMD (2013, 7, 10);

		JulianDate dtValue = JulianDate.CreateFromYMD (2013, 7, 11);

		JulianDate dtSettle = JulianDate.CreateFromYMD (2013, 7, 15);

		double dblNotional = -10.e+06;
		String[] astrCashTenor = new String[] {   "1M",     "2M",     "3M",     "6M",    "12M"};
		double[] adblCashRate = new double[] {0.001928, 0.002360, 0.002691, 0.004084, 0.006917};
		String[] astrIRSTenor = new String[] {   "2Y",     "3Y",     "4Y",     "5Y",     "6Y",     "7Y",
				"8Y",     "9Y",    "10Y",    "12Y",    "15Y",    "20Y",    "25Y",    "30Y"};
		double[] adblIRSRate = new double[] {0.005350, 0.008615, 0.012705, 0.016995, 0.020130, 0.022965,
			0.025210, 0.027040, 0.028570, 0.031145, 0.033465, 0.035170, 0.035990, 0.036300};

		DiscountCurve dc = BuildRatesCurveFromInstruments (dtCurve, astrCashTenor, adblCashRate, astrIRSTenor,
			adblIRSRate, 0., "USD");

		String[] astrCDSTenor = new String[] {"6M", "1Y", "2Y", "3Y", "4Y", "5Y", "7Y", "10Y"};
		double[] adblCDSParSpread = new double[] {100., 100., 100., 100., 100., 100., 100., 100.};

		CreditCurve cc = CreateCreditCurveFromCDS (dtCurve, adblCDSParSpread, astrCDSTenor, dc, 0.4, "CORP", 0.01, 0.);

		DisplayInstrumentMaturitySurvival (cc);

		CreditDefaultSwap cds = CreateCDS (dtValue, "5Y", 0.01, "KOR");

		ValuationParams valParams = new ValuationParams (dtValue, dtSettle, "USD");

		PricerParams pricerParams = PricerParams.MakeStdPricerParams();

		Map<String, Double> mapBaseMeasures = cds.value (
			valParams,
			pricerParams,
			ComponentMarketParamsBuilder.MakeCreditCMP (dc, cc),
			null);

		double dblBaseDirtyPV = mapBaseMeasures.get ("DirtyPV");

		System.out.println ("\n---- CDS Measures ----");

		System.out.println ("Accrued      : " + FormatUtil.FormatDouble (mapBaseMeasures.get ("Accrued"), 1, 0, 100. * dblNotional));

		System.out.println ("Accrual Days : " + FormatUtil.FormatDouble (mapBaseMeasures.get ("AccrualDays"), 1, 0, 1.));

		System.out.println ("Coupon DV01  : " + FormatUtil.FormatDouble (mapBaseMeasures.get ("DV01"), 1, 0, 0.01 * dblNotional));

		System.out.println ("Price        : " + FormatUtil.FormatDouble (mapBaseMeasures.get ("Price"), 1, 3, 1.));

		System.out.println ("Principal    : " + FormatUtil.FormatDouble (mapBaseMeasures.get ("Upfront"), 1, 0, 1.));

		System.out.println ("Repl Spread  : " + FormatUtil.FormatDouble (mapBaseMeasures.get ("FairPremium"), 1, 4, 1.));

		DiscountCurve dc01Bump = BuildRatesCurveFromInstruments (dtCurve, astrCashTenor, adblCashRate, astrIRSTenor,
			adblIRSRate, 0.0001, "USD");

		Map<String, Double> mapRatesFlat01Measures = cds.value (
			valParams,
			pricerParams,
			ComponentMarketParamsBuilder.MakeCreditCMP (dc01Bump, cc),
			null);

		double dblRatesFlat01DirtyPV = mapRatesFlat01Measures.get ("DirtyPV");

		System.out.println ("IR01         : " + FormatUtil.FormatDouble (dblRatesFlat01DirtyPV - dblBaseDirtyPV, 1, 0, 0.01 * dblNotional));

		CreditCurve cc01Bump = CreateCreditCurveFromCDS (dtCurve, adblCDSParSpread, astrCDSTenor, dc, 0.4, "CORP", 0.01, 1.);

		Map<String, Double> mapCreditFlat01Measures = cds.value (
			valParams,
			pricerParams,
			ComponentMarketParamsBuilder.MakeCreditCMP (dc, cc01Bump),
			null);

		double dblCreditFlat01DirtyPV = mapCreditFlat01Measures.get ("DirtyPV");

		System.out.println ("CS01         : " + FormatUtil.FormatDouble (dblCreditFlat01DirtyPV - dblBaseDirtyPV, 1, 0, 0.01 * dblNotional));

		/*
		 * Generates and displays the coupon period details for the bonds
		 */

		System.out.println ("\n---- CDS Coupon Flows ----");

		for (Period p : cds.getCouponPeriod())
			System.out.println (
				JulianDate.fromJulian (p.getAccrualStartDate()) + FIELD_SEPARATOR +
				JulianDate.fromJulian (p.getAccrualEndDate()) + FIELD_SEPARATOR +
				JulianDate.fromJulian (p.getPayDate()) + FIELD_SEPARATOR +
				FormatUtil.FormatDouble (p.getCouponDCF(), 1, 2, 0.01 * dblNotional) + FIELD_SEPARATOR +
				FormatUtil.FormatDouble (dc.getDF (p.getPayDate()), 1, 4, 1.) + FIELD_SEPARATOR +
				FormatUtil.FormatDouble (cc.getSurvival (p.getPayDate()), 1, 4, 1.)
			);
	}
}
