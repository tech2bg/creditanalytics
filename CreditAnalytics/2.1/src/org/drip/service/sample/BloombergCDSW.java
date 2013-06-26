
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
import org.drip.analytics.period.CouponPeriodCurveFactors;
import org.drip.param.definition.*;
import org.drip.param.creator.ComponentMarketParamsBuilder;
import org.drip.param.pricer.PricerParams;
import org.drip.param.valuation.*;
import org.drip.product.definition.*;

/*
 * Credit Analytics API imports
 */

import org.drip.param.creator.*;
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

	private static DiscountCurve BuildBBGRatesCurve (
		final JulianDate dtStart,
		final String[] astrCashTenor,
		final double[] adblCashRate,
		final String[] astrIRSTenor,
		final double[] adblIRSRate,
		final String strCurrency)
		throws Exception
	{
		int iNumDCInstruments = astrCashTenor.length + adblIRSRate.length;
		double adblDate[] = new double[iNumDCInstruments];
		double adblRate[] = new double[iNumDCInstruments];
		String astrCalibMeasure[] = new String[iNumDCInstruments];
		double adblCompCalibValue[] = new double[iNumDCInstruments];
		CalibratableComponent aCompCalib[] = new CalibratableComponent[iNumDCInstruments];
		String strIndex = strCurrency + "-LIBOR-6M";

		// Cash Calibration

		for (int i = 0; i < astrCashTenor.length; ++i) {
			astrCalibMeasure[i] = "Rate";
			adblRate[i] = java.lang.Double.NaN;
			adblCompCalibValue[i] = adblCashRate[i];

			aCompCalib[i] = CashBuilder.CreateCash (dtStart.addDays (2), new JulianDate (adblDate[i] =
				dtStart.addTenor (astrCashTenor[i]).getJulian()), strCurrency);
		}

		// IRS Calibration

		for (int i = 0; i < astrIRSTenor.length; ++i) {
			astrCalibMeasure[i + astrCashTenor.length] = "Rate";
			adblRate[i + astrCashTenor.length] = java.lang.Double.NaN;
			adblCompCalibValue[i + astrCashTenor.length] = adblIRSRate[i];

			aCompCalib[i + astrCashTenor.length] = RatesStreamBuilder.CreateIRS (dtStart.addDays (2), new
				JulianDate (adblDate[i + astrCashTenor.length] = dtStart.addTenor
					(astrIRSTenor[i]).getJulian()), 0., strCurrency, strIndex, strCurrency);
		}

		/*
		 * Create the sample (in this case dummy) IRS index rate fixings object
		 */

		Map<String, Double> mIndexFixings = new HashMap<String, Double>();

		mIndexFixings.put (strIndex, 0.0042);

		Map<JulianDate, Map<String, Double>> mmFixings = new HashMap<JulianDate, Map<String, Double>>();

		mmFixings.put (dtStart.addDays (2), mIndexFixings);

		/*
		 * Build the IR curve from the components, their calibration measures, and their calibration quotes.
		 */

		return RatesScenarioCurveBuilder.CreateDiscountCurve (dtStart, strCurrency,
			DiscountCurveBuilder.BOOTSTRAP_MODE_CONSTANT_FORWARD, aCompCalib, adblCompCalibValue, astrCalibMeasure, mmFixings);
	}

	private static CreditCurve CreateCreditCurveFromCDS (
		final JulianDate dtStart,
		final double[] adblQuote,
		final String[] astrTenor,
		final DiscountCurve dc,
		final double dblRecovery,
		final String strCCName)
		throws Exception
	{
		String[] astrCalibMeasure = new String[adblQuote.length];
		CreditDefaultSwap[] aCDS = new CreditDefaultSwap[adblQuote.length];

		for (int i = 0; i < astrTenor.length; ++i) {
			aCDS[i] = CDSBuilder.CreateSNAC (dtStart, astrTenor[i], 0.01, strCCName);

			astrCalibMeasure[i] = "FairPremium";
		}

		/*
		 * Build the credit curve from the CDS instruments and the fair premium
		 */

		return CreditScenarioCurveBuilder.CreateCreditCurve (strCCName, dtStart, aCDS, dc,
			adblQuote, astrCalibMeasure, dblRecovery, false);
	}

	private static CreditDefaultSwap CreateCDS (
		final JulianDate dtStart,
		final String strTenor,
		final double dblCoupon,
		final String strCCName)
	{
		return CDSBuilder.CreateSNAC (dtStart, strTenor, dblCoupon, strCCName);
	}

	private static void PriceCDS (
		final CreditDefaultSwap cds,
		final ValuationParams valParams,
		final DiscountCurve dc,
		final CreditCurve cc,
		final double dblNotional)
		throws Exception 
	{
		ComponentMarketParams cmp = ComponentMarketParamsBuilder.MakeCreditCMP (dc, cc);

		PricerParams pricerParams = PricerParams.MakeStdPricerParams();

		Map<String, Double> mapMeasures = cds.value (valParams, pricerParams, cmp, null);

		System.out.println ("\nCDS Pricing");

		System.out.println ("\tPrice: " + mapMeasures.get ("CleanPrice"));

		System.out.println ("\tRepl Spread: " + mapMeasures.get ("FairPremium"));

		System.out.println ("\tPrincipal: " + (int) (mapMeasures.get ("CleanPV") * dblNotional * 0.01));

		System.out.println ("\tAccrual Days: " + mapMeasures.get ("AccrualDays"));

		System.out.println ("\tAccrued: " + mapMeasures.get ("Accrued01") * cds.getCoupon (valParams._dblValue, null) * 100. * dblNotional);

		System.out.println ("\tPts Upf: " + mapMeasures.get ("Upfront"));

		System.out.println ("\nAcc Start     Acc End     Pay Date    Cpn DCF     Amount    Pay01    Surv01");

		System.out.println ("---------    ---------    ---------   --------  ---------  -------- --------");

		/*
		 * CDS Coupon Cash Flow
		 */

		for (CouponPeriodCurveFactors p : cds.getCouponFlow (valParams, pricerParams, cmp))
			System.out.println (
				JulianDate.fromJulian (p.getAccrualStartDate()) + FIELD_SEPARATOR +
				JulianDate.fromJulian (p.getAccrualEndDate()) + FIELD_SEPARATOR +
				JulianDate.fromJulian (p.getPayDate()) + FIELD_SEPARATOR +
				FormatUtil.FormatDouble (p.getCouponDCF(), 1, 4, 1.) + FIELD_SEPARATOR +
				FormatUtil.FormatDouble (p.getCouponDCF() * cds.getCoupon (valParams._dblValue, null) * dblNotional, 1, 2, 1.) + FIELD_SEPARATOR +
				FormatUtil.FormatDouble (dc.getDF (p.getPayDate()), 1, 4, 1.) + FIELD_SEPARATOR +
				FormatUtil.FormatDouble (cc.getSurvival (p.getPayDate()), 1, 4, 1.)
			);
	}

	public static void main (
		final String[] astrArgs)
		throws Exception
	{
		CreditAnalytics.Init ("");

		JulianDate dtCurve = JulianDate.CreateFromYMD (2013, 6, 26);

		JulianDate dtValue = JulianDate.CreateFromYMD (2013, 6, 27);

		JulianDate dtSettle = JulianDate.CreateFromYMD (2013, 7, 1);

		String[] astrCashTenor = new String[] {   "1M",     "2M",     "3M",     "6M",    "12M"};
		double[] adblCashRate = new double[] {0.001944, 0.002354, 0.002761, 0.004234, 0.007012};
		String[] astrIRSTenor = new String[] {   "2Y",     "3Y",     "4Y",     "5Y",     "6Y",     "7Y",
				"8Y",     "9Y",    "10Y",    "12Y",    "15Y",    "20Y",    "25Y",    "30Y"};
		double[] adblIRSRate = new double[] {0.005775, 0.008930, 0.012950, 0.016705, 0.019960, 0.022620,
			0.024765, 0.026575, 0.028130, 0.030530, 0.032720, 0.034280, 0.034975, 0.035360};

		DiscountCurve dc = BuildBBGRatesCurve (dtCurve, astrCashTenor, adblCashRate, astrIRSTenor,
			adblIRSRate, "USD");

		String[] astrCDSTenor = new String[] {"6M", "1Y", "2Y", "3Y", "4Y", "5Y", "7Y", "10Y"};
		double[] adblCDSParSpread = new double[] {100., 100., 100., 100., 100., 100., 100., 100.};

		CreditCurve cc = CreateCreditCurveFromCDS (dtCurve, adblCDSParSpread, astrCDSTenor, dc, 0.4, "CORP");

		CreditDefaultSwap cds = CreateCDS (dtCurve, "5Y", 0.05, "KOR");

		PriceCDS (cds, new ValuationParams (dtValue, dtSettle, "USD"), dc, cc, 10.e06);
	}
}
