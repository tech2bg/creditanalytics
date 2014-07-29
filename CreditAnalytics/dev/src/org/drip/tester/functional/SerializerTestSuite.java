
package org.drip.tester.functional;

/*
 * -*- mode: java; tab-width: 4; indent-tabs-mode: nil; c-basic-offset: 4 -*-
 */

/*!
 * Copyright (C) 2014 Lakshmi Krishnamurthy
 * Copyright (C) 2013 Lakshmi Krishnamurthy
 * Copyright (C) 2012 Lakshmi Krishnamurthy
 * Copyright (C) 2011 Lakshmi Krishnamurthy
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
 * SerializerTestSuite tests the serialization functionality across all products, curves, quotes, outputs,
 * 	and parameters, and their variants.
 * 
 * @author Lakshmi Krishnamurthy
 */

public class SerializerTestSuite {
	private static final boolean s_bTrace = true;

	private static final void Set2DMapValues (
		final org.drip.analytics.support.CaseInsensitiveTreeMap<java.lang.Double> map2D,
		final double dblPV,
		final double dblFairPremium)
	{
		map2D.put ("PV", dblPV);

		map2D.put ("FairPremium", dblFairPremium);
	}

	private static final void Set3DMapValues (
		final
			org.drip.analytics.support.CaseInsensitiveTreeMap<org.drip.analytics.support.CaseInsensitiveTreeMap<java.lang.Double>>
				map3D,
		final double dblPV,
		final double dblFairPremium)
	{
		org.drip.analytics.support.CaseInsensitiveTreeMap<java.lang.Double> mapIRScenario1Y = new
			org.drip.analytics.support.CaseInsensitiveTreeMap<java.lang.Double>();

		Set2DMapValues (mapIRScenario1Y, dblPV, dblFairPremium);

		map3D.put ("1Y", mapIRScenario1Y);

		org.drip.analytics.support.CaseInsensitiveTreeMap<java.lang.Double> mapIRScenario2Y = new
			org.drip.analytics.support.CaseInsensitiveTreeMap<java.lang.Double>();

		Set2DMapValues (mapIRScenario2Y, dblPV, dblFairPremium);

		map3D.put ("2Y", mapIRScenario2Y);
	}

	private static final void SetCustom3DMapValues (
		final java.lang.String strCustomSetName,
		final
			org.drip.analytics.support.CaseInsensitiveTreeMap<org.drip.analytics.support.CaseInsensitiveTreeMap<java.lang.Double>>
				map3D,
		final double dblPV,
		final double dblFairPremium)
	{
		org.drip.analytics.support.CaseInsensitiveTreeMap<java.lang.Double> mapIRScenario1Y = new
			org.drip.analytics.support.CaseInsensitiveTreeMap<java.lang.Double>();

		Set2DMapValues (mapIRScenario1Y, dblPV, dblFairPremium);

		map3D.put ("1Y", mapIRScenario1Y);

		org.drip.analytics.support.CaseInsensitiveTreeMap<java.lang.Double> mapIRScenario2Y = new
			org.drip.analytics.support.CaseInsensitiveTreeMap<java.lang.Double>();

		Set2DMapValues (mapIRScenario2Y, dblPV, dblFairPremium);

		map3D.put ("2Y", mapIRScenario2Y);
	}

	private static final void Set4DMapValues (
		final
			org.drip.analytics.support.CaseInsensitiveTreeMap<org.drip.analytics.support.CaseInsensitiveTreeMap<org.drip.analytics.support.CaseInsensitiveTreeMap<java.lang.Double>>>
				map4D,
		final double dblPV,
		final double dblFairPremium)
	{
		org.drip.analytics.support.CaseInsensitiveTreeMap<org.drip.analytics.support.CaseInsensitiveTreeMap<java.lang.Double>>
			mapARGComp1Y = new
				org.drip.analytics.support.CaseInsensitiveTreeMap<org.drip.analytics.support.CaseInsensitiveTreeMap<java.lang.Double>>();

		Set3DMapValues (mapARGComp1Y, dblPV, dblFairPremium);

		map4D.put ("ARG", mapARGComp1Y);

		org.drip.analytics.support.CaseInsensitiveTreeMap<org.drip.analytics.support.CaseInsensitiveTreeMap<java.lang.Double>>
			mapBRAComp1Y = new
				org.drip.analytics.support.CaseInsensitiveTreeMap<org.drip.analytics.support.CaseInsensitiveTreeMap<java.lang.Double>>();

		Set3DMapValues (mapBRAComp1Y, dblPV, dblFairPremium);

		map4D.put ("BRA", mapBRAComp1Y);
	}

	private static final void Verify (
		final byte[] abSer,
		final org.drip.service.stream.Serializer sDeser,
		final java.lang.String strDRIPObj)
	{
		if (null == sDeser || null == abSer || 0 == abSer.length) {
			System.out.println (strDRIPObj + " serialization FAILED!");

			System.exit (138);
		}

		byte[] abDeser = sDeser.serialize();

		if (null == abDeser || 0 == abDeser.length) {
			System.out.println (strDRIPObj + " serialization FAILED!");

			System.exit (139);
		}

		java.lang.String strSer = new java.lang.String (abSer);

		java.lang.String strDeser = new java.lang.String (abDeser);

		if (s_bTrace) System.out.println (strSer + "\n" + strDeser);

		if (!strSer.trim().equalsIgnoreCase (strDeser.trim())) {
			System.out.println (strDRIPObj + " serialization FAILED!");

			System.exit (140);
		}

		System.out.println (strDRIPObj + " serialization OK.\n----------------------\n\n");
	}

	public static final void main (
		final java.lang.String[] astrArgs)
		throws java.lang.Exception
	{
		org.drip.analytics.support.Logger.Init ("c:\\DRIP\\CreditAnalytics\\Config.xml");

		org.drip.analytics.daycount.Convention.Init ("c:\\DRIP\\CreditAnalytics\\Config.xml");

		org.drip.analytics.date.JulianDate dtToday = org.drip.analytics.date.JulianDate.Today();

		double dblStart = dtToday.julian();

		double[] adblDCDate = new double[3];
		double[] adblDCRate = new double[3];
		double[] adblHazardDate = new double[3];
		double[] adblHazardRate = new double[3];
		double[] adblRecoveryDate = new double[3];
		double[] adblRecoveryRate = new double[3];
		double[] adblFXBasisDate = new double[3];
		double[] adblFXBasis = new double[3];
		double[] adblFXFwdDate = new double[3];
		double[] adblFXFwd = new double[3];
		boolean[] abIsPIP = new boolean[3];

		for (int i = 0; i < 3; ++i) {
			adblFXFwdDate[i] = dblStart + 365. * (i + 1);
			adblFXFwd[i] = 53.51 + i;
			abIsPIP[i] = false;
		}

		for (int i = 0; i < 3; ++i) {
			adblDCDate[i] = dblStart + 365. * (i + 1);
			adblDCRate[i] = 0.01 * (i + 1);
			adblHazardDate[i] = dblStart + 365. * (i + 1);
			adblHazardRate[i] = 0.01 * (i + 1);
			adblRecoveryDate[i] = dblStart + 365. * (i + 1);
			adblRecoveryRate[i] = 0.40;
			adblFXBasisDate[i] = dblStart + 365. * (i + 1);
			adblFXBasis[i] = 0.02 * (i + 1);
		}

		org.drip.product.params.CurrencyPair cp = new org.drip.product.params.CurrencyPair ("USD", "INR",
			"INR", 1.);

		byte[] abCP = cp.serialize();

		Verify (abCP, new org.drip.product.params.CurrencyPair (abCP), "CurrencyPair");

		org.drip.analytics.rates.DiscountCurve dc = org.drip.state.creator.DiscountCurveBuilder.CreateDC
			(dtToday, "ABC", null, adblDCDate, adblDCRate,
				org.drip.state.creator.DiscountCurveBuilder.BOOTSTRAP_MODE_CONSTANT_FORWARD);

		byte[] abDC = dc.serialize();

		Verify (abDC, org.drip.state.creator.DiscountCurveBuilder.FromByteArray (abDC,
			org.drip.state.creator.DiscountCurveBuilder.BOOTSTRAP_MODE_CONSTANT_FORWARD), "DiscountCurve");

		org.drip.analytics.definition.CreditCurve cc =
			org.drip.state.creator.CreditCurveBuilder.CreateCreditCurve (dblStart, "XXS", "USD",
				adblHazardRate, adblHazardDate, adblRecoveryRate, adblRecoveryDate, java.lang.Double.NaN);

		byte[] abCC = cc.serialize();

		Verify (abCC, org.drip.state.creator.CreditCurveBuilder.FromByteArray (abCC), "CreditCurve");

		byte[] abFxBasis = org.drip.state.creator.FXBasisCurveBuilder.CreateFXBasisCurve (cp, dtToday,
			53.51, adblFXBasisDate, adblFXBasis, true).serialize();

		Verify (abFxBasis, org.drip.state.creator.FXBasisCurveBuilder.FromByteArray (abFxBasis), "FXBasis");

		byte[] abFxCurve = org.drip.state.creator.FXForwardCurveBuilder.CreateFXForwardCurve (cp, dtToday,
			53.51, adblFXFwdDate, adblFXFwd, abIsPIP).serialize();

		Verify (abFxCurve, org.drip.state.creator.FXForwardCurveBuilder.FromByteArray (abFxCurve),
			"FXCurve");

		byte[] abAAP = new org.drip.analytics.daycount.ActActDCParams (2, dblStart, dblStart +
			180.).serialize();

		Verify (abAAP, new org.drip.analytics.daycount.ActActDCParams (abAAP), "ActActDCParams");

		byte[] abDAP = new org.drip.analytics.daycount.DateAdjustParams (0, "CZK").serialize();

		Verify (abDAP, new org.drip.analytics.daycount.DateAdjustParams (abDAP), "DateAdjustParams");

		byte[] abFixH = new org.drip.analytics.holiday.Fixed (1, 3, null, "MLK Holiday").serialize();

		Verify (abFixH, new org.drip.analytics.holiday.Fixed (abFixH), "FixedHoliday");

		byte[] abFltH = new org.drip.analytics.holiday.Variable (1, 3, 4, false, null,
			"3 Jan Holiday").serialize();

		Verify (abFltH, new org.drip.analytics.holiday.Variable (abFltH), "FloatingHoliday");

		byte[] abSH = org.drip.analytics.holiday.Static.CreateFromDateDescription ("12-JUN-2020",
			"Are you kidding me?").serialize();

		Verify (abSH, new org.drip.analytics.holiday.Static (abSH), "StaticHoliday");

		byte[] abWH = org.drip.analytics.holiday.Weekend.StandardWeekend().serialize();

		Verify (abWH, new org.drip.analytics.holiday.Weekend (abWH), "WeekendHoliday");

		byte[] abPeriod = new org.drip.analytics.period.Period (dblStart, dblStart + 180, dblStart, dblStart
			+ 180, dblStart + 180, 0.5).serialize();

		Verify (abPeriod, new org.drip.analytics.period.Period (abPeriod), "Period");

		byte[] abCouponPeriod = new org.drip.analytics.period.CashflowPeriod (dblStart, dblStart + 180,
			dblStart, dblStart + 180, dblStart + 180, dblStart + 180, 2, 0.5, "30/360", true, "30/360", true,
				dblStart + 1825, "GBP", "GBP").serialize();

		Verify (abCouponPeriod, new org.drip.analytics.period.CashflowPeriod (abCouponPeriod),
			"CouponPeriod");

		byte[] abPCPCM = new org.drip.analytics.period.CashflowPeriodCurveFactors (dblStart, dblStart + 180.,
			dblStart, dblStart + 180., dblStart + 180., 0.5, 0.05, 1000000., 100000.,0.985, 0.97, 0.99, 0.98,
				java.lang.Double.NaN, java.lang.Double.NaN).serialize();

		Verify (abPCPCM, new org.drip.analytics.period.CashflowPeriodCurveFactors (abPCPCM),
			"CouponPeriodCurveFactors");

		byte[] abPLPCM = new org.drip.analytics.period.LossPeriodCurveFactors (dblStart, dblStart + 180.,
			dblStart, dblStart + 180., dblStart + 180., 0.5, 0.98, 0.94, 1000000., 0.36, 0.96).serialize();

		Verify (abPLPCM, new org.drip.analytics.period.LossPeriodCurveFactors (abPLPCM),
			"LossPeriodCurveFactors");

		byte[] abCash = org.drip.product.creator.DepositBuilder.CreateDeposit (dtToday, "1Y", "AUD").serialize();

		Verify (abCash, org.drip.product.creator.DepositBuilder.FromByteArray (abCash), "Cash");

		byte[] abEDF = org.drip.product.creator.EDFutureBuilder.CreateEDF (dtToday, "1Y", "GBP").serialize();

		Verify (abEDF, org.drip.product.creator.EDFutureBuilder.FromByteArray (abEDF), "EDFuture");

		byte[] abIRS = org.drip.product.creator.RatesStreamBuilder.CreateIRS (dtToday, "4Y", 0.03, 2,
			"Act/360", 0., 4, "Act/360", "JPY", "JPY").serialize();

		Verify (abIRS, org.drip.product.creator.RatesStreamBuilder.IRSFromByteArray (abIRS),
			"InterestRateSwap");

		byte[] abFRA = new org.drip.product.fra.FRAStandardComponent (1., "JPY", "JPY-FRA-3M", "JPY",
			org.drip.analytics.date.JulianDate.Today().julian(),
				org.drip.state.identifier.ForwardLabel.Create ("JPY-LIBOR-6M"), 0.01,
					"Act/360").serialize();

		Verify (abFRA, new org.drip.product.fra.FRAStandardComponent (abFRA), "FloatingRateAgreement");

		org.drip.product.stream.FixedStream[] aFixedStream = new org.drip.product.stream.FixedStream[3];
		org.drip.product.stream.FloatingStream[] aFloatStream = new org.drip.product.stream.FloatingStream[3];

		org.drip.analytics.daycount.DateAdjustParams dap = new org.drip.analytics.daycount.DateAdjustParams
			(org.drip.analytics.daycount.Convention.DR_FOLL, "XYZ");

		java.util.List<org.drip.analytics.period.CashflowPeriod> lsFixedPeriod3Y =
			org.drip.analytics.period.CashflowPeriod.GeneratePeriodsRegular (dtToday.julian(), "3Y", dap,
				2, "Act/360", false, false, "ABC", "DEF");

		aFixedStream[0] = new org.drip.product.stream.FixedStream ("DEF", null, 0.03, 1., null,
			lsFixedPeriod3Y);

		java.util.List<org.drip.analytics.period.CashflowPeriod> lsFixedPeriod5Y =
			org.drip.analytics.period.CashflowPeriod.GeneratePeriodsRegular (dtToday.julian(), "5Y", dap,
				2, "Act/360", false, false, "GHI", "JKL");

		aFixedStream[1] = new org.drip.product.stream.FixedStream ("JKL", null, 0.05, 1., null,
			lsFixedPeriod5Y);

		java.util.List<org.drip.analytics.period.CashflowPeriod> lsFixedPeriod7Y =
			org.drip.analytics.period.CashflowPeriod.GeneratePeriodsRegular (dtToday.julian(), "7Y", dap,
				2, "Act/360", false, false, "MNO", "PQR");

		aFixedStream[2] = new org.drip.product.stream.FixedStream ("PQR", null, 0.07, 1., null,
			lsFixedPeriod7Y);

		java.util.List<org.drip.analytics.period.CashflowPeriod> lsFloatPeriod3Y =
			org.drip.analytics.period.CashflowPeriod.GeneratePeriodsRegular (dtToday.julian(), "3Y", dap,
				4, "Act/360", false, false, "ABC", "DEF");

		aFloatStream[0] = new org.drip.product.stream.FloatingStream ("DEF", null, 0.03, -1., null,
			lsFloatPeriod3Y, org.drip.state.identifier.ForwardLabel.Create ("ABC-RI-3M"), false);

		java.util.List<org.drip.analytics.period.CashflowPeriod> lsFloatPeriod5Y =
			org.drip.analytics.period.CashflowPeriod.GeneratePeriodsRegular (dtToday.julian(), "5Y", dap,
				4, "Act/360", false, false, "ABC", "DEF");

		aFloatStream[1] = new org.drip.product.stream.FloatingStream ("DEF", null, 0.05, -1., null,
			lsFloatPeriod5Y, org.drip.state.identifier.ForwardLabel.Create ("ABC-RI-3M"), false);

		java.util.List<org.drip.analytics.period.CashflowPeriod> lsFloatPeriod7Y =
			org.drip.analytics.period.CashflowPeriod.GeneratePeriodsRegular (dtToday.julian(), "7Y", dap,
				4, "Act/360", false, false, "ABC", "DEF");

		aFloatStream[2] = new org.drip.product.stream.FloatingStream ("DEF", null, 0.07, -1., null,
			lsFloatPeriod7Y, org.drip.state.identifier.ForwardLabel.Create ("ABC-RI-3M"), false);

		byte[] abRB = new org.drip.product.rates.RatesBasket ("SAMRB", aFixedStream,
			aFloatStream).serialize();

		Verify (abRB, new org.drip.product.rates.RatesBasket (abRB), "RatesBasket");

		org.drip.param.definition.Quote q = org.drip.param.creator.QuoteBuilder.CreateQuote ("ASK", 103.,
			java.lang.Double.NaN);

		Verify (q.serialize(), org.drip.param.creator.QuoteBuilder.FromByteArray (q.serialize()), "Quote");

		org.drip.param.definition.ProductQuote cq =
			org.drip.param.creator.ProductQuoteBuilder.CreateProductQuote();

		cq.addQuote ("Price", q, false);

		cq.setMarketQuote ("SpreadToTsyBmk", org.drip.param.creator.QuoteBuilder.CreateQuote ("MID", 210.,
			java.lang.Double.NaN));

		byte[] abCQ = cq.serialize();

		Verify (abCQ, org.drip.param.creator.ProductQuoteBuilder.FromByteArray (abCQ), "ComponentQuote");

		org.drip.param.market.ProductTickQuote ctq = new org.drip.param.market.ProductTickQuote
			("TESTPRODUCT", cq, "MSIM", "IDC", true);

		byte[] abCTQ = ctq.serialize();

		Verify (abCTQ, org.drip.param.creator.ProductTickQuoteBuilder.FromByteArray (abCTQ),
			"ComponentTickQuote");

		byte[] abFxFwd = org.drip.product.creator.FXForwardBuilder.CreateFXForward (cp, dtToday,
			"18M").serialize();

		Verify (abFxFwd, org.drip.product.creator.FXForwardBuilder.FromByteArray (abFxFwd), "FXForward");

		byte[] abFxSpot = org.drip.product.creator.FXSpotBuilder.CreateFXSpot (dtToday, cp).serialize();

		Verify (abFxSpot, org.drip.product.creator.FXSpotBuilder.FromByteArray (abFxSpot), "FXSpot");

		byte[] abCDS = org.drip.product.creator.CDSBuilder.CreateSNAC (dtToday, "5Y", 0.01,
			"IBM").serialize();

		Verify (abCDS, new org.drip.product.credit.CDSComponent (abCDS), "CreditDefaultSwap");

		org.drip.analytics.output.ComponentMeasures co = new org.drip.analytics.output.ComponentMeasures();

		co._dblCalcTime = 433.7;

		Set2DMapValues (co._mBase = new
			org.drip.analytics.support.CaseInsensitiveTreeMap<java.lang.Double>(), 0.3601, 537.500);

		Set2DMapValues (co._mRRDelta = new
			org.drip.analytics.support.CaseInsensitiveTreeMap<java.lang.Double>(), 0.0015, 0.020);

		Set2DMapValues (co._mRRGamma = new
			org.drip.analytics.support.CaseInsensitiveTreeMap<java.lang.Double>(), 0.0006, 0.003);

		Set2DMapValues (co._mFlatIRDelta = new
			org.drip.analytics.support.CaseInsensitiveTreeMap<java.lang.Double>(), -0.0007, 0.006);

		Set2DMapValues (co._mFlatIRGamma = new
			org.drip.analytics.support.CaseInsensitiveTreeMap<java.lang.Double>(), -0.0001, 0.001);

		Set2DMapValues (co._mFlatCreditDelta = new
			org.drip.analytics.support.CaseInsensitiveTreeMap<java.lang.Double>(), -0.0118, 1.023);

		Set2DMapValues (co._mFlatCreditGamma = new
			org.drip.analytics.support.CaseInsensitiveTreeMap<java.lang.Double>(), -0.0004, 0.014);

		Set3DMapValues (co._mmTenorIRDelta = new
			org.drip.analytics.support.CaseInsensitiveTreeMap<org.drip.analytics.support.CaseInsensitiveTreeMap<java.lang.Double>>(),
				-0.00003751, 0.000002262);

		Set3DMapValues (co._mmTenorIRGamma = new
			org.drip.analytics.support.CaseInsensitiveTreeMap<org.drip.analytics.support.CaseInsensitiveTreeMap<java.lang.Double>>(),
				-0.00007501, 0.000004524);

		Set3DMapValues (co._mmTenorCreditDelta = new
			org.drip.analytics.support.CaseInsensitiveTreeMap<org.drip.analytics.support.CaseInsensitiveTreeMap<java.lang.Double>>(),
				-0.00000867, 0.000000238);

		Set3DMapValues (co._mmTenorCreditGamma = new
			org.drip.analytics.support.CaseInsensitiveTreeMap<org.drip.analytics.support.CaseInsensitiveTreeMap<java.lang.Double>>(),
				-0.00001734, 0.000000476);

		SetCustom3DMapValues ("CSW10PC", co._mmCustom = new
			org.drip.analytics.support.CaseInsensitiveTreeMap<org.drip.analytics.support.CaseInsensitiveTreeMap<java.lang.Double>>(),
				-0.00003468, 0.000000952);

		byte abCO[] = co.serialize();

		Verify (abCO, new org.drip.analytics.output.ComponentMeasures (abCO), "ComponentOutput");

		byte[] abBDS = ((org.drip.product.credit.CDSBasket) org.drip.product.creator.CDSBasketBuilder.MakeCDX
			(dtToday, dtToday.addYears (5), 0.01, "USD", new java.lang.String[] {"CHN", "IND", "INDO", "PAK",
				"BNG", "JPN"}, "CDX_ASIA_SOV")).serialize();

		Verify (abBDS, new org.drip.product.credit.CDSBasket (abBDS), "BasketDefaultSwap");

		org.drip.analytics.output.BasketMeasures bo = new org.drip.analytics.output.BasketMeasures();

		bo._dblCalcTime = 433.7;

		Set2DMapValues (bo._mBase = new
			org.drip.analytics.support.CaseInsensitiveTreeMap<java.lang.Double>(), 0.3601, 537.500);

		Set2DMapValues (bo._mFlatRRDelta = new
			org.drip.analytics.support.CaseInsensitiveTreeMap<java.lang.Double>(), 0.0015, 0.020);

		Set2DMapValues (bo._mFlatRRGamma = new
			org.drip.analytics.support.CaseInsensitiveTreeMap<java.lang.Double>(), 0.0006, 0.003);

		Set2DMapValues (bo._mFlatIRDelta = new
			org.drip.analytics.support.CaseInsensitiveTreeMap<java.lang.Double>(), -0.0007, 0.006);

		Set2DMapValues (bo._mFlatIRGamma = new
			org.drip.analytics.support.CaseInsensitiveTreeMap<java.lang.Double>(), -0.0001, 0.001);

		Set2DMapValues (bo._mFlatCreditDelta = new
			org.drip.analytics.support.CaseInsensitiveTreeMap<java.lang.Double>(), -0.0118, 1.023);

		Set2DMapValues (bo._mFlatCreditGamma = new
			org.drip.analytics.support.CaseInsensitiveTreeMap<java.lang.Double>(), -0.0004, 0.014);

		Set3DMapValues (bo._mmIRDelta = new
			org.drip.analytics.support.CaseInsensitiveTreeMap<org.drip.analytics.support.CaseInsensitiveTreeMap<java.lang.Double>>(),
				-0.00003751, 0.000002262);

		Set3DMapValues (bo._mmIRGamma = new
			org.drip.analytics.support.CaseInsensitiveTreeMap<org.drip.analytics.support.CaseInsensitiveTreeMap<java.lang.Double>>(),
				-0.00003751, 0.000002262);

		Set3DMapValues (bo._mmCreditDelta = new
			org.drip.analytics.support.CaseInsensitiveTreeMap<org.drip.analytics.support.CaseInsensitiveTreeMap<java.lang.Double>>(),
				-0.00003751, 0.000002262);

		Set3DMapValues (bo._mmCreditGamma = new
			org.drip.analytics.support.CaseInsensitiveTreeMap<org.drip.analytics.support.CaseInsensitiveTreeMap<java.lang.Double>>(),
				-0.00003751, 0.000002262);

		Set3DMapValues (bo._mmRRDelta = new
			org.drip.analytics.support.CaseInsensitiveTreeMap<org.drip.analytics.support.CaseInsensitiveTreeMap<java.lang.Double>>(),
				-0.00003751, 0.000002262);

		Set3DMapValues (bo._mmRRGamma = new
			org.drip.analytics.support.CaseInsensitiveTreeMap<org.drip.analytics.support.CaseInsensitiveTreeMap<java.lang.Double>>(),
				-0.00003751, 0.000002262);

		Set4DMapValues (bo._mmmIRTenorDelta = new
			org.drip.analytics.support.CaseInsensitiveTreeMap<org.drip.analytics.support.CaseInsensitiveTreeMap<org.drip.analytics.support.CaseInsensitiveTreeMap<java.lang.Double>>>(),
				-0.00003751, 0.000002262);

		Set4DMapValues (bo._mmmIRTenorGamma = new
			org.drip.analytics.support.CaseInsensitiveTreeMap<org.drip.analytics.support.CaseInsensitiveTreeMap<org.drip.analytics.support.CaseInsensitiveTreeMap<java.lang.Double>>>(),
				-0.00003751, 0.000002262);

		Set4DMapValues (bo._mmmCreditTenorDelta = new
			org.drip.analytics.support.CaseInsensitiveTreeMap<org.drip.analytics.support.CaseInsensitiveTreeMap<org.drip.analytics.support.CaseInsensitiveTreeMap<java.lang.Double>>>(),
				-0.00003751, 0.000002262);

		Set4DMapValues (bo._mmmCreditTenorGamma = new
			org.drip.analytics.support.CaseInsensitiveTreeMap<org.drip.analytics.support.CaseInsensitiveTreeMap<org.drip.analytics.support.CaseInsensitiveTreeMap<java.lang.Double>>>(),
				-0.00003751, 0.000002262);

		SetCustom3DMapValues ("CSW10PC", bo._mmCustom = new
			org.drip.analytics.support.CaseInsensitiveTreeMap<org.drip.analytics.support.CaseInsensitiveTreeMap<java.lang.Double>>(),
				-0.00003468, 0.000000952);

		byte abBO[] = bo.serialize();

		Verify (abBO, new org.drip.analytics.output.BasketMeasures (abBO), "BasketMeasures");

		double[] adblNotionalDate = new double[3];
		double[] adblPutDate = new double[3];
		double[] adblCallDate = new double[3];
		double[] adblPutFactor = new double[3];
		double[] adblCallFactor = new double[3];
		double[] adblCouponFactor = new double[3];
		double[] adblNotionalFactor = new double[3];
		adblPutFactor[0] = 0.80;
		adblPutFactor[1] = 0.90;
		adblPutFactor[2] = 1.00;
		adblCallFactor[0] = 1.20;
		adblCallFactor[1] = 1.10;
		adblCallFactor[2] = 1.00;
		adblPutDate[0] = dblStart + 30.;
		adblPutDate[1] = dblStart + 396.;
		adblPutDate[2] = dblStart + 761.;
		adblCallDate[0] = dblStart + 1126.;
		adblCallDate[1] = dblStart + 1492.;
		adblCallDate[2] = dblStart + 1857.;

		for (int i = 0; i < 3; ++i) {
			adblCouponFactor[i] = 1 - 0.1 * i;
			adblNotionalFactor[i] = 1 - 0.05 * i;
			adblNotionalDate[i] = dblStart + 365. * (i + 1);
		}

		org.drip.analytics.support.CaseInsensitiveTreeMap<java.lang.Double> mIndexFixings = new
			org.drip.analytics.support.CaseInsensitiveTreeMap<java.lang.Double>();

		mIndexFixings.put ("USD-LIBOR-6M", 0.0402);

		java.util.Map<org.drip.analytics.date.JulianDate,
			org.drip.analytics.support.CaseInsensitiveTreeMap<java.lang.Double>> mmFixings = new
				java.util.HashMap<org.drip.analytics.date.JulianDate,
					org.drip.analytics.support.CaseInsensitiveTreeMap<java.lang.Double>>();

		mmFixings.put (org.drip.analytics.date.JulianDate.Today().addDays (2), mIndexFixings);

		org.drip.product.params.PeriodGenerator bpgp = new org.drip.product.params.PeriodGenerator (dblStart
			+ 3653., dblStart, dblStart + 3653., dblStart + 182., dblStart, 2, "30/360", "30/360", null,
				null, null, null, null, null, null, null, "IGNORE", false, "USD", "USD");

		if (!bpgp.validate()) {
			System.out.println ("Cannot validate BPGP!");

			System.exit (125);
		}

		byte[] abBPGP = bpgp.serialize();

		Verify (abBPGP, new org.drip.product.params.PeriodGenerator (abBPGP), "BondPeriodParams");

		org.drip.product.credit.BondComponent bond = new org.drip.product.credit.BondComponent();

		org.drip.product.params.TsyBmkSet tbs = new org.drip.product.params.TsyBmkSet ("USD5YON", new
			java.lang.String[] {"USD3YON", "USD7YON"});

		byte[] abTBS = tbs.serialize();

		Verify (abTBS, new org.drip.product.params.TsyBmkSet (abTBS), "TsyBmkSet");

		if (!bond.setTreasuryBenchmark (tbs)) {
			System.out.println ("Cannot initialize component TSY params!");

			System.exit (126);
		}

		org.drip.product.params.CouponSetting bcp = new org.drip.product.params.CouponSetting
			(org.drip.product.params.FactorSchedule.CreateFromDateFactorArray (adblNotionalDate,
				adblCouponFactor), "FLOATER", 0.01, java.lang.Double.NaN, java.lang.Double.NaN);

		byte[] abBCP = bcp.serialize();

		Verify (abBCP, new org.drip.product.params.CouponSetting (abBCP), "BondCouponParams");

		if (!bond.setCouponSetting (new org.drip.product.params.CouponSetting
			(org.drip.product.params.FactorSchedule.CreateFromDateFactorArray (adblNotionalDate,
				adblCouponFactor), "FLOATER", 0.01, java.lang.Double.NaN, java.lang.Double.NaN))) {
			System.out.println ("Cannot initialize bond Coupon params!");

			System.exit (127);
		}

		org.drip.product.params.FactorSchedule fs =
			org.drip.product.params.FactorSchedule.CreateFromDateFactorArray (adblNotionalDate,
				adblNotionalFactor);

		byte[] abFS = fs.serialize();

		Verify (abFS, new org.drip.product.params.FactorSchedule (abFS), "FactorSchedule");

		org.drip.product.params.NotionalSetting bnp = new org.drip.product.params.NotionalSetting (fs, 1.,
			org.drip.product.params.NotionalSetting.PERIOD_AMORT_AT_START, false);

		byte[] abBNP = bnp.serialize();

		Verify (abBNP, new org.drip.product.params.NotionalSetting (abBNP), "BondNotionalParams");

		if (!bond.setNotionalSetting (bnp)) {
			System.out.println ("Cannot initialize bond Notional params!");

			System.exit (128);
		}

		org.drip.product.params.FloaterSetting bflp = new org.drip.product.params.FloaterSetting
			("USD-LIBOR-6M", "30/360", 0.01, java.lang.Double.NaN);

		byte[] abBFlP = bflp.serialize();

		Verify (abBFlP, new org.drip.product.params.FloaterSetting (abBFlP), "BondFloaterParams");

		if (!bond.setFloaterSetting (bflp)) {
			System.out.println ("Cannot initialize bond Floater params!");

			System.exit (129);
		}

		if (!bond.setFixings (mmFixings)) {
			System.out.println ("Cannot initialize bond Fixings!");

			System.exit (130);
		}

		org.drip.product.params.CurrencySet ccyParams = org.drip.product.params.CurrencySet.Create ("USD");

		byte[] abCcyParams = ccyParams.serialize();

		Verify (abCcyParams, new org.drip.product.params.CurrencySet (abCcyParams), "BondCurrencyParams");

		if (!bond.setCurrencySet (ccyParams)) {
			System.out.println ("Cannot initialize bond currency params!");

			System.exit (131);
		}

		org.drip.product.params.IdentifierSet bip = new org.drip.product.params.IdentifierSet
			("US07942381EZ", "07942381E", "IBM-US07942381EZ", "IBM");

		byte[] abBIP = bip.serialize();

		Verify (abBIP, new org.drip.product.params.IdentifierSet (abBIP), "BondIdentifierParams");

		if (!bond.setIdentifierSet (bip)) {
			System.out.println ("Cannot initialize bond Identifier params!");

			System.exit (132);
		}

		org.drip.product.params.QuoteConvention mktConv = new org.drip.product.params.QuoteConvention (new
			org.drip.param.valuation.ValuationCustomizationParams ("30/360", 2, true, null, "DKK", false,
				null, null), "REGULAR", dblStart + 2, 1., 3, "USD",
					org.drip.analytics.daycount.Convention.DR_FOLL);

		byte[] abMktConv = mktConv.serialize();

		Verify (abMktConv, new org.drip.product.params.QuoteConvention (abMktConv), "MarketConvention");

		if (!bond.setMarketConvention (mktConv)) {
			System.out.println ("Cannot initialize Market Convention!");

			System.exit (133);
		}

		org.drip.product.params.RatesSetting crvp = new org.drip.product.params.RatesSetting ("USD", "USD",
			"USD", "USD");

		byte[] abCRVP = crvp.serialize();

		Verify (abCRVP, new org.drip.product.params.RatesSetting (abCRVP),
			"ComponentRatesValuationParams");

		if (!bond.setRatesSetting (crvp)) {
			System.out.println ("Cannot initialize Component Rates Valuation params!");

			System.exit (133);
		}

		org.drip.product.params.CreditSetting crValParams = new org.drip.product.params.CreditSetting (30,
			java.lang.Double.NaN, true, "IBMSUB", false);

		byte[] abCCVP = crValParams.serialize();

		Verify (abCCVP, new org.drip.product.params.CreditSetting (abCCVP),
			"ComponentCreditValuationParams");

		if (!bond.setCreditSetting (crValParams)) {
			System.out.println ("Cannot initialize bond Credit Valuation params!");

			System.exit (134);
		}

		org.drip.product.params.TerminationSetting cfte = new org.drip.product.params.TerminationSetting
			(false, false, false);

		byte[] abCFTE = cfte.serialize();

		Verify (abCFTE, new org.drip.product.params.TerminationSetting (abCFTE),
			"ComponentTerminationModes");

		if (!bond.setTerminationSetting (cfte)) {
			System.out.println ("Cannot initialize ComponentTerminationModes!");

			System.exit (135);
		}

		if (!bond.setPeriodSet (bpgp)) {
			System.out.println ("Cannot initialize bond Period Generation params!");

			System.exit (136);
		}

		org.drip.product.params.EmbeddedOptionSchedule eosPut =
			org.drip.product.params.EmbeddedOptionSchedule.fromAmerican (dblStart, adblPutDate,
				adblPutFactor, true, 30, false, java.lang.Double.NaN, "CRAP", java.lang.Double.NaN);

		byte[] abEOS = eosPut.serialize();

		Verify (abEOS, new org.drip.product.params.EmbeddedOptionSchedule (abEOS), "EmbeddedOptionSchedule");

		bond.setEmbeddedPutSchedule (eosPut);

		bond.setEmbeddedCallSchedule (org.drip.product.params.EmbeddedOptionSchedule.fromAmerican (dblStart,
			adblCallDate, adblCallFactor, false, 30, false, java.lang.Double.NaN, "CRAP",
				java.lang.Double.NaN));

		byte[] abBond = bond.serialize();

		Verify (abBond, new org.drip.product.credit.BondComponent (abBond), "Bond");

		double[] adblRateTSY = new double[3];

		for (int i = 0; i < 3; ++i)
			adblRateTSY[i] = 0.01 * (i + 1);

		org.drip.analytics.rates.DiscountCurve dcTSY =
			org.drip.state.creator.DiscountCurveBuilder.CreateDC
				(org.drip.analytics.date.JulianDate.Today(), "ABCTSY", null, adblDCDate, adblRateTSY,
					org.drip.state.creator.DiscountCurveBuilder.BOOTSTRAP_MODE_CONSTANT_FORWARD);

		org.drip.analytics.support.CaseInsensitiveTreeMap<org.drip.param.definition.ProductQuote>
			mapTSYQuotes = new
				org.drip.analytics.support.CaseInsensitiveTreeMap<org.drip.param.definition.ProductQuote>();

		mapTSYQuotes.put ("TSY2ON", cq);

		mmFixings.put (org.drip.analytics.date.JulianDate.Today().addDays (2), mIndexFixings);

		byte[] abCSQS = org.drip.param.creator.MarketParamsBuilder.Create (dc, dcTSY, cc, "TSY2ON", cq,
			mapTSYQuotes, mmFixings).serialize();

		Verify (abCSQS, org.drip.param.creator.MarketParamsBuilder.FromByteArray (abCSQS),
			"ComponentMarketParams");

		org.drip.analytics.support.CaseInsensitiveTreeMap<org.drip.analytics.rates.DiscountCurve> mapDC =
			new org.drip.analytics.support.CaseInsensitiveTreeMap<org.drip.analytics.rates.DiscountCurve>();

		mapDC.put ("ABC", dc);

		mapDC.put ("ABCTSY", dcTSY);

		org.drip.analytics.support.CaseInsensitiveTreeMap<org.drip.analytics.definition.CreditCurve> mapCC =
			new org.drip.analytics.support.CaseInsensitiveTreeMap<org.drip.analytics.definition.CreditCurve>();

		mapCC.put ("ABCSOV", cc);

		byte[] abNTP = new org.drip.param.definition.ResponseValueTweakParams
			(org.drip.param.definition.ResponseValueTweakParams.MANIFEST_MEASURE_FLAT_TWEAK, false,
				0.1).serialize();

		Verify (abNTP, new org.drip.param.definition.ResponseValueTweakParams (abNTP), "NodeTweakParams");

		byte[] abPricer = new org.drip.param.pricer.PricerParams (7, new
			org.drip.param.definition.CalibrationParams ("KOOL", 1, new
				org.drip.param.valuation.WorkoutInfo (org.drip.analytics.date.JulianDate.Today().julian(),
					0.04, 1., org.drip.param.valuation.WorkoutInfo.WO_TYPE_MATURITY)), false, 1,
						false).serialize();

		Verify (abPricer, new org.drip.param.pricer.PricerParams (abPricer), "PricerParams");

		java.util.List<org.drip.analytics.period.CashflowPeriod> lsCouponPeriod = new
			java.util.ArrayList<org.drip.analytics.period.CashflowPeriod>();

		int i = 5;

		while (0 != i--) {
			lsCouponPeriod.add (new org.drip.analytics.period.CashflowPeriod (dblStart, dblStart + 180,
				dblStart, dblStart + 180, dblStart + 180, dblStart, 2, 0.5, "30/360", false, "30/360",
					false, java.lang.Double.NaN, "ZAR", "ZAR"));

			dblStart += 180.;
		}

		org.drip.product.params.PeriodSet bfpgp = new org.drip.product.params.PeriodSet (1., "Act/360", 2,
			lsCouponPeriod);

		bfpgp.validate();

		byte[] abBFPGP = bfpgp.serialize();

		Verify (abBFPGP, new org.drip.product.params.PeriodSet (abBFPGP),
			"BondFixedPeriodGenerationParams");

		byte[] abCSP = new org.drip.param.valuation.CashSettleParams (2, "DKK", 3).serialize();

		Verify (abCSP, new org.drip.param.valuation.CashSettleParams (abCSP), "CashSettleParams");

		byte[] abQP = new org.drip.param.valuation.ValuationCustomizationParams ("30/360", 2, true, null,
			"DKK", false, null, null).serialize();

		Verify (abQP, new org.drip.param.valuation.ValuationCustomizationParams (abQP), "QuotingParams");

		byte[] abYQ = new org.drip.param.quoting.YieldInterpreter ("30/360", 2, true, null,
			"DKK").serialize();

		Verify (abYQ, new org.drip.param.quoting.YieldInterpreter (abYQ), "YieldInterpreter");

		byte[] abQSI = new org.drip.param.quoting.QuotedSpreadInterpreter
			(org.drip.param.quoting.QuotedSpreadInterpreter.SNAC_CDS, 100.).serialize();

		Verify (abQSI, new org.drip.param.quoting.QuotedSpreadInterpreter (abQSI),
			"QuotedSpreadInterpreter");

		byte[] abVP = org.drip.param.valuation.ValuationParams.CreateValParams (dtToday, 2, "DKK",
			3).serialize();

		Verify (abVP, new org.drip.param.valuation.ValuationParams (abVP), "ValuationParams");

		byte[] abNEI = new org.drip.analytics.output.ExerciseInfo (dblStart, 1.,
			org.drip.param.valuation.WorkoutInfo.WO_TYPE_MATURITY).serialize();

		Verify (abNEI, new org.drip.analytics.output.ExerciseInfo (abNEI), "NextExerciseInfo");

		byte[] abWI = new org.drip.param.valuation.WorkoutInfo (dblStart, 0.06, 1.,
			org.drip.param.valuation.WorkoutInfo.WO_TYPE_MATURITY).serialize();

		Verify (abWI, new org.drip.param.valuation.WorkoutInfo (abWI), "WorkoutInfo");

		org.drip.product.creator.BondProductBuilder bpb = new org.drip.product.creator.BondProductBuilder();

		bpb._strISIN = "US734427FC";
		bpb._strCUSIP = "734427F";
		bpb._strTicker = "BSI";
		bpb._dblCoupon = 0.06;

		bpb._dtMaturity = org.drip.analytics.date.JulianDate.Today().addYears (20);

		bpb._iCouponFreq = 2;
		bpb._strCouponType = "FULL";
		bpb._strMaturityType = "FULL";
		bpb._strCalculationType = "REGULAR";
		bpb._strDayCountCode = "30/360";
		bpb._dblRedemptionValue = 1.;

		bpb._dtAnnounce = org.drip.analytics.date.JulianDate.Today();

		bpb._dtFirstSettle = bpb._dtAnnounce;
		bpb._dtFirstCoupon = bpb._dtAnnounce;
		bpb._dtInterestAccrualStart = bpb._dtAnnounce;
		bpb._dtIssue = bpb._dtAnnounce;
		bpb._bIsCallable = false;
		bpb._bIsPutable = false;
		bpb._bIsSinkable = false;
		bpb._strRedemptionCurrency = "USD";
		bpb._strCouponCurrency = "USD";
		bpb._strTradeCurrency = "USD";
		bpb._bHasBeenCalled = false;
		bpb._strFloatCouponConvention = "30/360";
		bpb._dblCurrentCoupon = 0.06;
		bpb._bIsFloater = false;
		bpb._dtFinalMaturity = bpb._dtMaturity;
		bpb._bIsPerpetual = false;
		bpb._bIsDefaulted = false;
		bpb._dblFloatSpread = java.lang.Double.NaN;
		bpb._strRateIndex = "USD-LIBOR-6M";
		bpb._strIssuerSPN = "BSI_SNR";

		byte[] abBPB = bpb.serialize();

		Verify (abBPB, new org.drip.product.creator.BondProductBuilder (abBPB), "BondProductBuilder");

		org.drip.product.creator.BondRefDataBuilder brdb = new org.drip.product.creator.BondRefDataBuilder();

		brdb._strISIN = "US3451683DF";
		brdb._strCUSIP = "3451683D";
		brdb._strBBGID = "1286BB45";
		brdb._strIssuerCategory = "Construction";
		brdb._strTicker = "BSI";
		brdb._strSeries = "RegS";
		brdb._strName = "Broken Systems International";
		brdb._strShortName = "Broken Systems";
		brdb._strIssuerIndustry = "Architecture & Engineering";
		brdb._strCouponType = "REGULAR";
		brdb._strMaturityType = "BULLET";
		brdb._strCalculationType = "NORMAL";
		brdb._strDayCountCode = "30/360";
		brdb._strMarketIssueType = "Primary Annual Series A";
		brdb._strIssueCountryCode = "USA";
		brdb._strIssueCountry = "United States of America";
		brdb._strCollateralType = "Equipment";
		brdb._dblIssueAmount = 1000000000.;
		brdb._dblOutstandingAmount = 800000000.;
		brdb._dblMinimumPiece = 1000.;
		brdb._dblMinimumIncrement = 1000.;
		brdb._dblParAmount = 100.;
		brdb._strLeadManager = "LEHMANN";
		brdb._strExchangeCode = "NYSE";
		brdb._dblRedemptionValue = 1.;

		brdb._dtAnnounce = org.drip.analytics.date.JulianDate.Today();

		brdb._dtFirstSettle = null;
		brdb._dtFirstCoupon = brdb._dtAnnounce;
		brdb._dtInterestAccrualStart = brdb._dtAnnounce;
		brdb._dtIssue = brdb._dtAnnounce;
		brdb._dtNextCouponDate = brdb._dtAnnounce;
		brdb._bIsCallable = false;
		brdb._bIsPutable = false;
		brdb._bIsSinkable = false;
		brdb._strBBGParent = "ADI";
		brdb._strCountryOfIncorporation = "United States of America";
		brdb._strIndustrySector = "ArchConstr";
		brdb._strIndustryGroup = "Software";
		brdb._strIndustrySubgroup = "CAD";
		brdb._strCountryOfGuarantor = "USA";
		brdb._strCountryOfDomicile = "USA";
		brdb._strDescription = "BSI Senior Series 6 pc coupon annual issue";
		brdb._strSecurityType = "BULLET";
		brdb._dtPrevCouponDate = brdb._dtAnnounce;
		brdb._strBBGUniqueID = "BSI374562IID";
		brdb._strLongCompanyName = "Broken System International Inc.";
		brdb._bIsStructuredNote = false;
		brdb._bIsUnitTraded = false;
		brdb._bIsReversibleConvertible = false;
		brdb._strRedemptionCurrency = "USD";
		brdb._strCouponCurrency = "USD";
		brdb._strTradeCurrency = "USD";
		brdb._bIsBearer = false;
		brdb._bIsRegistered = true;
		brdb._bHasBeenCalled = false;
		brdb._strIssuer = "Bentley Systems";
		brdb._dtPenultimateCouponDate = brdb._dtAnnounce;
		brdb._strFloatCouponConvention = "30/360";
		brdb._dblCurrentCoupon = 0.06;
		brdb._bIsFloater = true;
		brdb._bTradeStatus = true;
		brdb._strCDRCountryCode = "US";
		brdb._strCDRSettleCode = "US";
		brdb._bIsPrivatePlacement = false;
		brdb._bIsPerpetual = false;
		brdb._bIsDefaulted = false;
		brdb._dblFloatSpread = 0.01;
		brdb._strRateIndex = "USD-LIBOR-6M";
		brdb._strMoody = "A";
		brdb._strSnP = "A";
		brdb._strFitch = "A";
		brdb._strSnrSub = "Senior";
		brdb._strIssuerSPN = "374528";
		brdb._dblIssuePrice = 93.75;
		brdb._dblCoupon = 0.01;

		brdb._dtMaturity = brdb._dtAnnounce.addYears (10);

		brdb._dtFinalMaturity = brdb._dtMaturity;

		byte[] abBRDB = brdb.serialize();

		Verify (abBRDB, new org.drip.product.creator.BondRefDataBuilder (abBRDB), "BondRefDataBuilder");

		org.drip.product.definition.BasketProduct bb =
			org.drip.product.creator.BondBasketBuilder.CreateBondBasket ("BASKETBOND", new
				org.drip.product.credit.BondComponent[] {bond, bond}, new double[] {0.7, 1.3},
					org.drip.analytics.date.JulianDate.Today(), 1.);

		byte[] abBB = bb.serialize();

		Verify (abBB, org.drip.product.creator.BondBasketBuilder.FromByteArray (abBB), "BasketBond");

		org.drip.analytics.output.BondCouponMeasures bcm = new org.drip.analytics.output.BondCouponMeasures
			(1., 2., 3., 4.);

		byte[] abBCM = bcm.serialize();

		Verify (abBCM, new org.drip.analytics.output.BondCouponMeasures (abBCM), "BondCouponMeasures");

		org.drip.analytics.output.BondWorkoutMeasures bwm = new org.drip.analytics.output.BondWorkoutMeasures
			(null, bcm, 4., 5., 6., 7., 8., 9., 10., 11., 12., 13., 14., 15., 16.);

		byte[] abBWM = bwm.serialize();

		Verify (abBWM, new org.drip.analytics.output.BondWorkoutMeasures (abBWM), "BondWorkoutMeasures");
	}
}
