
package org.drip.sample.credit;

/*
 * Credit Product import
 */

import org.drip.analytics.date.JulianDate;
import org.drip.analytics.daycount.Convention;
import org.drip.analytics.definition.*;
import org.drip.analytics.period.*;
import org.drip.analytics.rates.DiscountCurve;
import org.drip.param.market.CurveSurfaceQuoteSet;
import org.drip.param.pricer.PricerParams;
import org.drip.param.valuation.*;
import org.drip.product.definition.*;
import org.drip.param.creator.*;
import org.drip.product.creator.*;
import org.drip.quant.common.FormatUtil;
import org.drip.service.api.CreditAnalytics;
import org.drip.state.creator.*;

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
 * CreditAnalyticsAPI contains a demo of the CDS Analytics API Sample. It illustrates the following:
 * - Credit Curve Creation: From flat Hazard Rate, and from an array of dates and their corresponding
 * 		survival probabilities.
 * - Create Credit Curve from CDS instruments, and recover the input measure quotes.
 * - Create an SNAC CDS, price it, and display the coupon/loss cash flow.
 * 
 * @author Lakshmi Krishnamurthy
 */

public class CreditAnalyticsAPI {
	private static final java.lang.String FIELD_SEPARATOR = "   ";

	/*
	 * Sample API demonstrating the creation/usage of the credit curve from survival and hazard rates
	 * 
	 *  	USE WITH CARE: This sample ignores errors and does not handle exceptions.
	 */

	private static final void CreditCurveAPISample()
		throws Exception
	{
		JulianDate dtStart = JulianDate.Today();

		JulianDate dt10Y = dtStart.addYears (10);

		/*
		 * Create Credit Curve from flat Hazard Rate
		 */

		CreditCurve ccFlatHazard = CreditCurveBuilder.FromFlatHazard (dtStart.julian(), "CC", "USD", 0.02, 0.4);

		System.out.println ("CCFromFlatHazard[" + dt10Y.toString() + "]; Survival=" +
			ccFlatHazard.getSurvival ("10Y") + "; Hazard=" + ccFlatHazard.calcHazard ("10Y"));

		double[] adblDate = new double[5];
		double[] adblSurvival = new double[5];

		for (int i = 0; i < 5; ++i) {
			adblDate[i] = dtStart.addYears (2 * i + 2).julian();

			adblSurvival[i] = 1. - 0.1 * (i + 1);
		}

		/*
		 * Create Credit Curve from an array of dates and their corresponding survival probabilities
		 */

		CreditCurve ccFromSurvival = CreditCurveBuilder.FromSurvival
			(dtStart.julian(), "CC", "USD", adblDate, adblSurvival, 0.4);

		System.out.println ("CCFromSurvival[" + dt10Y.toString() + "]; Survival=" +
			ccFromSurvival.getSurvival ("10Y") + "; Hazard=" + ccFromSurvival.calcHazard ("10Y"));
	}

	/*
	 * Sample API demonstrating the creation of the Credit Curve from the CDS instruments
	 * 
	 *  	USE WITH CARE: This sample ignores errors and does not handle exceptions.
	 */

	private static void CreateCreditCurveFromCDSInstruments()
		throws Exception
	{
		JulianDate dtStart = JulianDate.Today();

		/*
		 * Populate the instruments, the calibration measures, and the calibration quotes
		 */

		double[] adblQuotes = new double[5];
		String[] astrCalibMeasure = new String[5];
		CreditDefaultSwap[] aCDS = new CreditDefaultSwap[5];

		for (int i = 0; i < 5; ++i) {
			/*
			 * The Calibration CDS
			 */

			aCDS[i] = CDSBuilder.CreateSNAC (dtStart, (i + 1) + "Y", 0.01, "CORP");

			/*
			 * Calibration Quote
			 */

			adblQuotes[i] = 100.;

			/*
			 * Calibration Measure
			 */

			astrCalibMeasure[i] = "FairPremium";
		}

		/*
		 * Flat Discount Curve
		 */

		DiscountCurve dc = DiscountCurveBuilder.CreateFromFlatRate (dtStart, "USD", null, 0.05);

		/*
		 * Create the Credit Curve from the give CDS instruments
		 */

		CreditCurve cc = CreditScenarioCurveBuilder.CreateCreditCurve
			("CORP", dtStart, aCDS, dc, adblQuotes, astrCalibMeasure, 0.4, false);

		/*
		 * Valuation Parameters
		 */

		ValuationParams valParams = ValuationParams.CreateValParams (dtStart, 0, "", Convention.DR_ACTUAL);

		/*
		 * Standard Credit Pricer Parameters (check javadoc for details)
		 */

		PricerParams pricerParams = PricerParams.Standard();

		/*
		 * Re-calculate the input calibration measures for the input CDSes
		 */

		for (int i = 0; i < aCDS.length; ++i)
			System.out.println ("\t" + astrCalibMeasure[i] + "[" + i + "] = " + aCDS[i].measureValue
				(valParams, pricerParams, MarketParamsBuilder.Create
					(dc, null, null, cc, null, null, null, null), null, astrCalibMeasure[i]));
	}

	/*
	 * Sample API demonstrating the display of the CDS coupon and loss cash flow
	 * 
	 *  	USE WITH CARE: This sample ignores errors and does not handle exceptions.
	 */

	private static final void CDSAPISample()
		throws Exception
	{
		JulianDate dtStart = JulianDate.Today();

		/*
		 * Flat Discount Curve
		 */

		DiscountCurve dc = DiscountCurveBuilder.CreateFromFlatRate (dtStart, "USD", null, 0.05);

		/*
		 * Flat Credit Curve
		 */

		CreditCurve cc = CreditCurveBuilder.FromFlatHazard (dtStart.julian(), "CC", "USD", 0.02, 0.4);

		/*
		 * Component Market Parameters built from the Discount and the Credit Curves
		 */

		CurveSurfaceQuoteSet mktParams = MarketParamsBuilder.Credit (dc, cc);

		/*
		 * Create an SNAC CDS
		 */

		CreditDefaultSwap cds = CDSBuilder.CreateSNAC (dtStart, "5Y", 0.1, "CC");

		/*
		 * Valuation Parameters
		 */

		ValuationParams valParams = ValuationParams.CreateValParams (dtStart, 0, "", Convention.DR_ACTUAL);

		/*
		 * Standard Credit Pricer Parameters (check javadoc for details)
		 */

		PricerParams pricerParams = PricerParams.Standard();

		System.out.println ("Acc Start       Acc End     Pay Date    Index   Spread   Cpn DCF    Pay01    Surv01");

		System.out.println ("---------      ---------    ---------   ------  ------   -------- --------- --------");

		/*
		 * CDS Coupon Cash Flow
		 */

		for (CashflowPeriodCurveFactors p : cds.getCouponFlow (valParams, pricerParams, mktParams))
			System.out.println (
				JulianDate.fromJulian (p.accrualStart()) + FIELD_SEPARATOR +
				JulianDate.fromJulian (p.accrualEnd()) + FIELD_SEPARATOR +
				JulianDate.fromJulian (p.pay()) + FIELD_SEPARATOR +
				FormatUtil.FormatDouble (p.indexRate(), 1, 4, 1.) +	FIELD_SEPARATOR +
				FormatUtil.FormatDouble (p.spread(), 1, 4, 1.) + FIELD_SEPARATOR +
				FormatUtil.FormatDouble (p.couponDCF(), 1, 4, 1.) + FIELD_SEPARATOR +
				FormatUtil.FormatDouble (dc.df (p.pay()), 1, 4, 1.) + FIELD_SEPARATOR +
				FormatUtil.FormatDouble (cc.getSurvival (p.pay()), 1, 4, 1.)
			);

		System.out.println ("Loss Start     Loss End      Pay Date      Cpn    Notl     Rec    EffDF    StartSurv  EndSurv");

		System.out.println ("----------     --------      --------      ---    ----     ---    -----    ---------  -------");

		/*
		 * CDS Loss Cash Flow
		 */

		for (LossPeriodCurveFactors dp : cds.getLossFlow (valParams, pricerParams, mktParams))
			System.out.println (
				JulianDate.fromJulian (dp.start()) + FIELD_SEPARATOR +
				JulianDate.fromJulian (dp.end()) + FIELD_SEPARATOR +
				JulianDate.fromJulian (dp.pay()) + FIELD_SEPARATOR +
				FormatUtil.FormatDouble (dp.couponDCF(), 1, 4, 1.) + FIELD_SEPARATOR +
				FormatUtil.FormatDouble (dp.effectiveNotional(), 1, 0, 1.) + FIELD_SEPARATOR +
				FormatUtil.FormatDouble (dp.effectiveRecovery(), 1, 2, 1.) + FIELD_SEPARATOR +
				FormatUtil.FormatDouble (dp.effectiveDF(), 1, 4, 1.)  + FIELD_SEPARATOR +
				FormatUtil.FormatDouble (dp.startSurvival(), 1, 4, 1.) + FIELD_SEPARATOR +
				FormatUtil.FormatDouble (dp.endSurvival(), 1, 4, 1.)
			);
	}

	public static final void main (
		final String astrArgs[])
		throws Exception
	{
		// String strConfig = "c:\\Lakshmi\\BondAnal\\Config.xml";

		String strConfig = "";

		CreditAnalytics.Init (strConfig);

		CreditCurveAPISample();

		CreateCreditCurveFromCDSInstruments();

		CDSAPISample();
	}
}
