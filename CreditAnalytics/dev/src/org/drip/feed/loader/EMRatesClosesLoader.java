
package org.drip.feed.loader;

/*
 * -*- mode: java; tab-width: 4; indent-tabs-mode: nil; c-basic-offset: 4 -*-
 */

/*!
 * Copyright (C) 2013 Lakshmi Krishnamurthy
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
 * EMRatesClosesLoader Loads the closing marks for a given EM Rates Curve.
 * 
 * @author Lakshmi Krishnamurthy
 */

public class EMRatesClosesLoader {
	private static final boolean s_bBlog = true;
	private static java.io.BufferedWriter _writeCOB = null;
	private static java.io.BufferedWriter _writeLog = null;
	private static final java.lang.String[] s_astrFwdTenor = new java.lang.String[] {"1Y", "2Y", "3Y", "4Y",
		"5Y", "7Y", "10Y", "12Y", "15Y", "20Y", "25Y", "30Y"};

	private static final double calcMeasure (
		final org.drip.product.definition.Component comp,
		final org.drip.analytics.date.JulianDate dt,
		final org.drip.analytics.rates.DiscountCurve dc,
		final java.lang.String strMeasure,
		final java.lang.String strCurrency)
		throws java.lang.Exception
	{
		if (comp.getMaturityDate().getJulian() <= dt.getJulian()) return 0.;

		org.drip.analytics.support.CaseInsensitiveTreeMap<java.lang.Double> mapIndexFixing = new
			org.drip.analytics.support.CaseInsensitiveTreeMap<java.lang.Double>();

		mapIndexFixing.put (strCurrency + "-LIBOR-3M", 0.05);

		java.util.Map<org.drip.analytics.date.JulianDate,
			org.drip.analytics.support.CaseInsensitiveTreeMap<java.lang.Double>> mmFixings = new
				java.util.HashMap<org.drip.analytics.date.JulianDate,
					org.drip.analytics.support.CaseInsensitiveTreeMap<java.lang.Double>>();

		mmFixings.put (dt, mapIndexFixing);

		org.drip.analytics.support.CaseInsensitiveTreeMap<java.lang.Double> mapSwapCalc = comp.value (new
			org.drip.param.valuation.ValuationParams (dt, dt, strCurrency), null,
				org.drip.param.creator.ComponentMarketParamsBuilder.CreateComponentMarketParams (dc, null,
					null, null, null, null, mmFixings), null);

		return mapSwapCalc.get (strMeasure);
	}

	private static final double calcReturn (
		final org.drip.product.definition.Component comp,
		final org.drip.analytics.date.JulianDate dt1,
		final org.drip.analytics.date.JulianDate dt2,
		final org.drip.analytics.rates.DiscountCurve dc1,
		final org.drip.analytics.rates.DiscountCurve dc2,
		final java.lang.String strCurrency)
		throws java.lang.Exception
	{
		return calcMeasure (comp, dt2, dc2, "DirtyPV", strCurrency) - calcMeasure (comp, dt1, dc1, "DirtyPV",
			strCurrency);
	}

	private static final double calcCarry (
		final org.drip.product.definition.Component comp,
		final org.drip.analytics.date.JulianDate dt1,
		final org.drip.analytics.date.JulianDate dt2,
		final org.drip.analytics.rates.DiscountCurve dc,
		final java.lang.String strCurrency)
		throws java.lang.Exception
	{
		return calcMeasure (comp, dt2, dc, "FixAccrued", strCurrency) + calcMeasure (comp, dt2, dc,
			"FloatAccrued", strCurrency) - calcMeasure (comp, dt1, dc, "FixAccrued", strCurrency) -
				calcMeasure (comp, dt1, dc, "FloatAccrued", strCurrency);
	}

	private static final double calcRollDown (
		final org.drip.product.definition.Component comp,
		final org.drip.analytics.date.JulianDate dt1,
		final org.drip.analytics.date.JulianDate dt2,
		final org.drip.analytics.rates.DiscountCurve dc,
		final java.lang.String strCurrency)
		throws java.lang.Exception
	{
		return calcMeasure (comp, dt1, dc, "FairPremium", strCurrency) - calcMeasure (comp, dt2, dc,
			"FairPremium", strCurrency);
	}

	private static final double calcCurveShift (
		final org.drip.product.definition.Component comp,
		final org.drip.analytics.date.JulianDate dt,
		final org.drip.analytics.rates.DiscountCurve dc1,
		final org.drip.analytics.rates.DiscountCurve dc2,
		final java.lang.String strCurrency)
		throws java.lang.Exception
	{
		return calcMeasure (comp, dt, dc1, "FairPremium", strCurrency) - calcMeasure (comp, dt, dc2,
			"FairPremium", strCurrency);
	}

	private static final double Forward (
		final org.drip.analytics.rates.DiscountCurve dc,
		final org.drip.analytics.date.JulianDate dt1,
		final org.drip.analytics.date.JulianDate dt2)
		throws java.lang.Exception
	{
		if (dt1.getJulian() >= dt2.getJulian()) return -0.;

		return dc.forward (dt1.getJulian(), dt2.getJulian());
	}

	private static final org.drip.service.api.ProductDailyPnL ComputePnLMetrics (
		final org.drip.analytics.date.JulianDate dt0D,
		final org.drip.analytics.date.JulianDate dt1D,
		final org.drip.product.definition.Component comp,
		final org.drip.analytics.rates.DiscountCurve dc0D,
		final org.drip.analytics.rates.DiscountCurve dc1D,
		final java.lang.String strCurrency)
		throws java.lang.Exception
	{
		org.drip.analytics.date.JulianDate dt1M = dt0D.addTenor ("1M");

		org.drip.analytics.date.JulianDate dt3M = dt0D.addTenor ("3M");

		double dblDV01 = calcMeasure (comp, dt0D, dc0D, "FixedDV01", strCurrency);

		double dbl1DReturn = calcReturn (comp, dt0D, dt1D, dc0D, dc1D, strCurrency);

		double dbl1DCarry = calcCarry (comp, dt0D, dt1D, dc0D, strCurrency);

		double dbl1DRollDown = calcRollDown (comp, dt0D, dt1D, dc0D, strCurrency) * dblDV01;

		double dbl1DCurveShift = calcCurveShift (comp, dt0D, dc0D, dc1D, strCurrency) * dblDV01;

		double dbl1MCarry = calcCarry (comp, dt0D, dt1M, dc0D, strCurrency);

		double dbl1MRollDown = calcRollDown (comp, dt0D, dt1M, dc0D, strCurrency) * dblDV01;

		double dbl3MCarry = calcCarry (comp, dt0D, dt3M, dc0D, strCurrency);

		double dbl3MRollDown = calcRollDown (comp, dt0D, dt3M, dc0D, strCurrency) * dblDV01;

		if (s_bBlog) {
			java.lang.StringBuffer sb = new java.lang.StringBuffer();

			sb.append ("\t1D Return       : " + org.drip.quant.common.FormatUtil.FormatDouble (dbl1DReturn,
				1, 8, 1.) + "\n");

			sb.append ("\t1D Coupon Carry : " + org.drip.quant.common.FormatUtil.FormatDouble (dbl1DCarry, 1,
				8, 1.) + "\n");

			sb.append ("\t1D Roll Down    : " + org.drip.quant.common.FormatUtil.FormatDouble (dbl1DRollDown,
				1, 8, 1.) + "\n");

			sb.append ("\t1D Curve Shift  : " + org.drip.quant.common.FormatUtil.FormatDouble
				(dbl1DCurveShift, 1, 8, 1.) + "\n");

			sb.append ("\t\t\t---------\n");

			sb.append ("\t1M Coupon Carry : " + org.drip.quant.common.FormatUtil.FormatDouble (dbl1MCarry, 1,
				8, 1.) + "\n");

			sb.append ("\t1M Roll Down    : " + org.drip.quant.common.FormatUtil.FormatDouble (dbl1MRollDown,
				1, 8, 1.) + "\n");

			sb.append ("\t\t\t---------\n");

			sb.append ("\t3M Coupon Carry : " + org.drip.quant.common.FormatUtil.FormatDouble (dbl3MCarry, 1,
				8, 1.) + "\n");

			sb.append ("\t3M Roll Down    : " + org.drip.quant.common.FormatUtil.FormatDouble (dbl3MRollDown,
				1, 8, 1.) + "\n");

			sb.append ("\t\t\t---------\n");

			sb.append ("\tDV01            : " + org.drip.quant.common.FormatUtil.FormatDouble (dblDV01, 1, 8,
				1.) + "\n");

			System.out.println (sb.toString());

			if (null != _writeLog) _writeLog.write (sb.toString());
		}

		org.drip.service.api.ProductDailyPnL pnlOP = new org.drip.service.api.ProductDailyPnL (dbl1DReturn, dbl1DCarry,
			dbl1DRollDown, dbl1DCurveShift, dbl1MCarry, dbl1MRollDown, dbl3MCarry, dbl3MRollDown, dblDV01);

		if (null != _writeCOB) {
			_writeCOB.write (pnlOP.toString());

			_writeCOB.flush();
		}

		return pnlOP;
	}

	private static final org.drip.service.api.ForwardRates ComputeForwardMetric (
		final org.drip.product.definition.Component[] aComp,
		final org.drip.analytics.rates.DiscountCurve dc)
		throws java.lang.Exception
	{
		java.lang.StringBuffer sb = new java.lang.StringBuffer();

		if (s_bBlog) {
			sb.append ("\n\n\tFORWARD RATE GRID\n\t\t\t");

			for (int i = 0; i < aComp.length; ++ i) {
				if (0 != i) sb.append ("  |   ");

				sb.append (aComp[i].getMaturityDate());
			}

			sb.append
				("\t\t\t--------------------------------------------------------------------------------------------------------------------------------------\n");
		}

		org.drip.service.api.ForwardRates fmOP = new org.drip.service.api.ForwardRates();

		for (int i = 0; i < aComp.length; ++i) {
			if (s_bBlog) sb.append ("\t\t" + aComp[i].getMaturityDate() + " => ");

			for (int j = 0; j < aComp.length; ++j) {
				if (s_bBlog && 0 != j) sb.append (" | ");

				double dblForward = Forward (dc, aComp[j].getMaturityDate(), aComp[i].getMaturityDate());

				if (0 != dblForward) fmOP.addForward (dblForward);

				if (s_bBlog)
					sb.append (org.drip.quant.common.FormatUtil.FormatDouble (dblForward, 2, 2, 100.));
			}

			if (s_bBlog) sb.append ("\n");
		}

		if (s_bBlog) {
			System.out.println (sb.toString());

			if (null != _writeLog) _writeLog.write (sb.toString());
		}

		if (null != _writeCOB) {
			_writeCOB.write (fmOP.toString());

			_writeCOB.flush();
		}

		return fmOP;
	}

	private static final void GenerateMetrics (
		final org.drip.analytics.date.JulianDate dt0D,
		final org.drip.analytics.date.JulianDate dt1D,
		final org.drip.analytics.rates.DiscountCurve dc0D,
		final org.drip.analytics.rates.DiscountCurve dc1D,
		final java.lang.String strCurrency)
		throws java.lang.Exception
	{
		_writeLog = new java.io.BufferedWriter (new java.io.FileWriter ("C:\\IFA\\Metric.PnL"));

		org.drip.product.definition.Component[] aComp = new
			org.drip.product.definition.Component[s_astrFwdTenor.length];

		for (int i = 0; i < s_astrFwdTenor.length; ++i)
			aComp[i] = org.drip.product.creator.RatesStreamBuilder.CreateIRS (dt0D, dt0D.addTenorAndAdjust
				(s_astrFwdTenor[i], strCurrency), 0.05, strCurrency, strCurrency + "-LIBOR-6M", strCurrency);

		for (int i = 0; i < s_astrFwdTenor.length; ++i) {
			if (s_bBlog)
				System.out.println ("\n\t----\n\tComputing PnL Metrics for " + aComp[i].getComponentName() +
					"\n\t----");

			_writeCOB.write (dt0D.toString() + "," + aComp[i].tenor() + ",");

			ComputePnLMetrics (dt0D, dt1D, aComp[i], dc0D, dc1D, strCurrency);

			_writeCOB.write (",");

			ComputeForwardMetric (aComp, dc0D);

			_writeCOB.newLine();

			_writeCOB.flush();
		}

		_writeLog.flush();
	}

	private static final org.drip.product.definition.CalibratableComponent[] CashInstrumentsFromTenor (
		final org.drip.analytics.date.JulianDate dtEffective,
		final java.lang.String[] astrTenor,
		final java.lang.String strCurrency)
	{
		if (null == astrTenor) return null;

		int iNumTenor = astrTenor.length;
		org.drip.product.definition.CalibratableComponent[] aCalibComp = new
			org.drip.product.definition.CalibratableComponent[iNumTenor];

		if (0 == iNumTenor) return null;

		for (int i = 0; i < iNumTenor; ++i)
			aCalibComp[i] = org.drip.product.creator.CashBuilder.CreateCash (dtEffective,
				dtEffective.addTenorAndAdjust (astrTenor[i], strCurrency), strCurrency);

		return aCalibComp;
	}

	private static final org.drip.product.definition.CalibratableComponent[] SwapInstrumentsFromTenor (
		final org.drip.analytics.date.JulianDate dtEffective,
		final java.lang.String[] astrTenor,
		final double[] adblQuote,
		final java.lang.String strCurrency)
	{
		if (null == astrTenor) return null;

		int iNumTenor = astrTenor.length;
		org.drip.product.definition.CalibratableComponent[] aCalibComp = new
			org.drip.product.definition.CalibratableComponent[iNumTenor];

		if (0 == iNumTenor) return null;

		for (int i = 0; i < iNumTenor; ++i)
			aCalibComp[i] = org.drip.product.creator.RatesStreamBuilder.CreateIRS (dtEffective,
				dtEffective.addTenorAndAdjust (astrTenor[i], strCurrency), adblQuote[i], strCurrency,
					strCurrency + "-LIBOR-6M", strCurrency);

		return aCalibComp;
	}

	private static final org.drip.analytics.rates.DiscountCurve BuildCurve (
		final org.drip.analytics.date.JulianDate dt,
		final java.lang.String[] astrCashTenor,
		final double[] adblCashQuote,
		final java.lang.String[] astrSwapTenor,
		final double[] adblSwapQuote,
		final java.lang.String strCurrency)
	{
		org.drip.state.estimator.StretchRepresentationSpec rbsCash =
			org.drip.state.estimator.StretchRepresentationSpec.CreateStretchBuilderSet ("CASH",
				org.drip.analytics.rates.DiscountCurve.LATENT_STATE_DISCOUNT,
					org.drip.analytics.rates.DiscountCurve.QUANTIFICATION_METRIC_DISCOUNT_FACTOR,
						CashInstrumentsFromTenor (dt, astrCashTenor, strCurrency), "Rate", adblCashQuote,
							null);

		org.drip.state.estimator.StretchRepresentationSpec rbsSwap =
			org.drip.state.estimator.StretchRepresentationSpec.CreateStretchBuilderSet ("SWAP",
				org.drip.analytics.rates.DiscountCurve.LATENT_STATE_DISCOUNT,
					org.drip.analytics.rates.DiscountCurve.QUANTIFICATION_METRIC_DISCOUNT_FACTOR,
						SwapInstrumentsFromTenor (dt, astrSwapTenor, adblSwapQuote, strCurrency), "Rate",
							adblSwapQuote, null);

		if (null == rbsCash || null == rbsSwap) return null;

		org.drip.state.estimator.StretchRepresentationSpec[] aRRS = new
			org.drip.state.estimator.StretchRepresentationSpec[] {rbsCash, rbsSwap};

		try {
			org.drip.param.valuation.ValuationParams valParams = new org.drip.param.valuation.ValuationParams
				(dt, dt, strCurrency);

			org.drip.spline.params.ResponseScalingShapeControl rssc = new
				org.drip.spline.params.ResponseScalingShapeControl (true, new
					org.drip.quant.function1D.QuadraticRationalShapeControl (0.));

			org.drip.spline.params.SegmentDesignInelasticControl sdic =
				org.drip.spline.params.SegmentDesignInelasticControl.Create (2, 2);

			org.drip.state.estimator.LinearCurveCalibrator lcc = new
				org.drip.state.estimator.LinearCurveCalibrator (new
					org.drip.spline.params.SegmentCustomBuilderControl
						(org.drip.spline.stretch.MultiSegmentSequenceBuilder.BASIS_SPLINE_KLK_HYPERBOLIC_TENSION,
				new org.drip.spline.basis.ExponentialTensionSetParams (1.), sdic, rssc),
					org.drip.spline.stretch.BoundarySettings.NaturalStandard(),
						org.drip.spline.stretch.MultiSegmentSequence.CALIBRATE, null);

			org.drip.analytics.rates.DiscountCurve dcShapePreserving =
				org.drip.param.creator.RatesScenarioCurveBuilder.ShapePreservingDFBuild (lcc, aRRS, valParams,
					null, null, null, 1.0);

			if (null == dcShapePreserving) return null;

			org.drip.state.estimator.LocalControlCurveParams lccpHyman83 = new
				org.drip.state.estimator.LocalControlCurveParams
					(org.drip.spline.pchip.LocalMonotoneCkGenerator.C1_HYMAN83,
						org.drip.analytics.rates.DiscountCurve.QUANTIFICATION_METRIC_ZERO_RATE, new
							org.drip.spline.params.SegmentCustomBuilderControl
								(org.drip.spline.stretch.MultiSegmentSequenceBuilder.BASIS_SPLINE_POLYNOMIAL,
									new org.drip.spline.basis.PolynomialFunctionSetParams (4), sdic, rssc),
					org.drip.spline.stretch.MultiSegmentSequence.CALIBRATE, null, true, true);

			return org.drip.param.creator.RatesScenarioCurveBuilder.SmoothingLocalControlBuild
				(dcShapePreserving, lcc, lccpHyman83, aRRS, valParams, null, null, null);
		} catch (java.lang.Exception e) {
			e.printStackTrace();
		}

		return null;
	}

	private static final org.drip.service.api.DiscountCurveInputInstrument ProcessCOBInput (
		final java.lang.String[] astrTenor,
		final java.lang.String[] astrCOBRecord)
	{
		if (null == astrCOBRecord) return null;

		int iNumQuote = astrCOBRecord.length;

		java.util.List<java.lang.Double> lsCashQuote = new java.util.ArrayList<java.lang.Double>();

		java.util.List<java.lang.String> lsCashTenor = new java.util.ArrayList<java.lang.String>();

		java.util.List<java.lang.Double> lsSwapQuote = new java.util.ArrayList<java.lang.Double>();

		java.util.List<java.lang.String> lsSwapTenor = new java.util.ArrayList<java.lang.String>();

		if (iNumQuote != astrTenor.length + 1) return null;

		org.drip.analytics.date.JulianDate dt = org.drip.analytics.date.JulianDate.CreateFromMDY
			(astrCOBRecord[0], "/");

		if (null == dt) return null;

		for (int i = 1; i < iNumQuote; ++i) {
			double dblQuote = java.lang.Double.NaN;

			try {
				dblQuote = 0.01 * Double.parseDouble (astrCOBRecord[i]);
			} catch (java.lang.Exception e) {
				dblQuote = java.lang.Double.NaN;
			}

			if (org.drip.quant.common.NumberUtil.IsValid (dblQuote)) {
				if ("1M".equalsIgnoreCase (astrTenor[i - 1])) {
					lsCashTenor.add ("1M");

					lsCashQuote.add (dblQuote);
				} else {
					lsSwapTenor.add (astrTenor[i - 1]);

					lsSwapQuote.add (dblQuote);
				}
			}
		}

		try {
			return new org.drip.service.api.DiscountCurveInputInstrument (dt, lsCashTenor, lsCashQuote,
				lsSwapTenor, lsSwapQuote);
		} catch (java.lang.Exception e) {
			// e.printStackTrace();
		}

		return null;
	}

	public static final void GenerateEMCurveMetrics (
		final java.lang.String strCurrency)
	{
		boolean bIsHeader = true;
		java.lang.String strCOBQuote = "";
		java.lang.String[] astrTenor = null;
		java.io.BufferedReader brSwapCOB = null;
		org.drip.analytics.date.JulianDate dtPrev = null;
		org.drip.analytics.rates.DiscountCurve dcPrev = null;

		try {
			brSwapCOB = new java.io.BufferedReader (new java.io.FileReader ("C:\\IFA\\" + strCurrency +
				"_Swap_Curve_Orig.txt"));

			_writeCOB = new java.io.BufferedWriter (new java.io.FileWriter ("C:\\IFA\\SPCA." + strCurrency));

			while (null != (strCOBQuote = brSwapCOB.readLine())) {
				java.lang.String[] astrCOBRecord = strCOBQuote.split (",");

				if (null == astrCOBRecord) {
					brSwapCOB.close();

					return;
				}

				int iNumQuote = astrCOBRecord.length;

				if (0 == iNumQuote) {
					brSwapCOB.close();

					return;
				}

				if (bIsHeader) {
					bIsHeader = false;
					astrTenor = new java.lang.String[iNumQuote - 1];

					for (int i = 1; i < iNumQuote; ++i)
						astrTenor[i - 1] = astrCOBRecord[i];

					_writeCOB.write ("Date, Instrument, 1DReturn, 1DCarry, 1DRollDown, 1DCurveShift, ");

					_writeCOB.write ("1MCarry, 1MRollDown, 3MCarry, 3MRollDown, DV01, ");

					_writeCOB.write ("1Y2Y, 1Y3Y, 1Y4Y, 1Y5Y, 1Y7Y, 1Y10Y, 1Y12Y, 1Y15Y, 1Y20Y, 1Y25Y, 1Y30Y, ");

					_writeCOB.write ("2Y3Y, 2Y4Y, 2Y5Y, 2Y7Y, 2Y10Y, 2Y12Y, 2Y15Y, 2Y20Y, 2Y25Y, 2Y30Y, ");

					_writeCOB.write ("3Y4Y, 3Y5Y, 3Y7Y, 3Y10Y, 3Y12Y, 3Y15Y, 3Y20Y, 3Y25Y, 3Y30Y, ");

					_writeCOB.write ("4Y5Y, 4Y7Y, 4Y10Y, 4Y12Y, 4Y15Y, 4Y20Y, 4Y25Y, 4Y30Y, ");

					_writeCOB.write ("5Y7Y, 5Y10Y, 5Y12Y, 5Y15Y, 5Y20Y, 5Y25Y, 5Y30Y, ");

					_writeCOB.write ("7Y10Y, 7Y12Y, 7Y15Y, 7Y20Y, 7Y25Y, 7Y30Y, ");

					_writeCOB.write ("10Y12Y, 10Y15Y, 10Y20Y, 10Y25Y, 10Y30Y, ");

					_writeCOB.write ("12Y15Y, 12Y20Y, 12Y25Y, 12Y30Y, ");

					_writeCOB.write ("15Y20Y, 15Y25Y, 15Y30Y, ");

					_writeCOB.write ("20Y25Y, 20Y30Y, ");

					_writeCOB.write ("25Y30Y, ");

					_writeCOB.newLine();

					_writeCOB.flush();
				} else {
					org.drip.service.api.DiscountCurveInputInstrument dcci = ProcessCOBInput (astrTenor,
						astrCOBRecord);

					if (null != dcci) {
						org.drip.analytics.date.JulianDate dt = dcci.date();

						org.drip.analytics.rates.DiscountCurve dc = BuildCurve (dcci.date(),
							dcci.cashTenor(), dcci.cashQuote(), dcci.swapTenor(), dcci.swapQuote(), strCurrency);

						System.out.println ("Adding " + dcci.date() + " = " + dc);

						System.out.println ("\tPrev: " + dcPrev + "; Current: " + dc);

						if (null != dtPrev && null != dt && null != dcPrev && null != dc)
							GenerateMetrics (dtPrev, dt, dcPrev, dc, strCurrency);

						dcPrev = dc;

						dtPrev = dcci.date();
					}
				}
			}
		} catch (java.lang.Exception e) {
			e.printStackTrace();

			return;
		}

		try {
			brSwapCOB.close();
		} catch (java.lang.Exception e) {
			e.printStackTrace();
		}
	}

	public static final java.util.Map<org.drip.analytics.date.JulianDate,
		java.util.List<org.drip.service.api.CDXCOB>> LoadCDXCloses (
			final java.lang.String strFile)
	{
		int iNumCDX = 0;
		int iLineNum = 0;
		boolean bHeader = true;
		java.lang.String strCDXCloses = "";
		java.lang.String[] astrCDXName = null;
		java.io.BufferedReader brCDXCloses = null;

		java.util.Map<org.drip.analytics.date.JulianDate, java.util.List<org.drip.service.api.CDXCOB>>
			mapDatedCDXClose = new java.util.HashMap<org.drip.analytics.date.JulianDate,
				java.util.List<org.drip.service.api.CDXCOB>>();

		try {
			brCDXCloses = new java.io.BufferedReader (new java.io.FileReader (strFile));

			while (null != (strCDXCloses = brCDXCloses.readLine())) {
				java.lang.String[] astrCDXCloses = strCDXCloses.split (",");

				if (null == astrCDXCloses || 0 == astrCDXCloses.length) continue;

				++iLineNum;

				System.out.println ("Processing line " + iLineNum);

				if (bHeader) {
					bHeader = false;
					iNumCDX = astrCDXCloses.length;
					astrCDXName = new java.lang.String[iNumCDX - 1];

					for (int i = 1; i < iNumCDX; ++i) {
						astrCDXName[i - 1] = astrCDXCloses[i];

						System.out.println ("\tCDX: " + astrCDXName[i - 1]);
					}
				} else {
					org.drip.analytics.date.JulianDate dt = org.drip.analytics.date.JulianDate.CreateFromMDY
						(astrCDXCloses[0], "/");

					if (null == dt) continue;

					java.util.List<org.drip.service.api.CDXCOB> lsCDXNamedPrice = new
						java.util.ArrayList<org.drip.service.api.CDXCOB>();

					for (int i = 1; i < iNumCDX; ++i) {
						double dblQuote = java.lang.Double.NaN;

						try {
							dblQuote = 0.01 * Double.parseDouble (astrCDXCloses[i]);
						} catch (java.lang.Exception e) {
							dblQuote = java.lang.Double.NaN;
						}

						if (org.drip.quant.common.NumberUtil.IsValid (dblQuote)) {
							org.drip.service.api.CDXCOB cdxNP = new org.drip.service.api.CDXCOB
								(astrCDXName[i - 1], dblQuote);

							lsCDXNamedPrice.add (cdxNP);

							System.out.println ("\tAdding CDX: " + cdxNP.display());
						}
					}

					if (0 < lsCDXNamedPrice.size()) mapDatedCDXClose.put (dt, lsCDXNamedPrice);
				}
			}
		} catch (java.lang.Exception e) {
			e.printStackTrace();
		}

		try {
			if (null != brCDXCloses) brCDXCloses.close();
		} catch (java.lang.Exception e) {
			e.printStackTrace();
		}

		return mapDatedCDXClose;
	}

	public static final void ProcessCDXQuote (
		final java.util.Map<org.drip.analytics.date.JulianDate, java.util.List<org.drip.service.api.CDXCOB>>
			mapDatedCDXClose)
	{
		boolean bIsHeader = true;
		java.lang.String strCOBQuote = "";
		java.lang.String[] astrTenor = null;
		java.io.BufferedReader brSwapCOB = null;

		org.drip.param.pricer.PricerParams pricerParams = 
			org.drip.param.pricer.PricerParams.MakeStdPricerParams();

		try {
			brSwapCOB = new java.io.BufferedReader (new java.io.FileReader
				("C:\\IFA\\CDXOP\\USD_CDS_Fixing_Curve_Orig_3.txt"));

			_writeCOB = new java.io.BufferedWriter (new java.io.FileWriter ("C:\\IFA\\CDXOP\\HY5Y.LAST"));

			while (null != (strCOBQuote = brSwapCOB.readLine())) {
				java.lang.String[] astrCOBRecord = strCOBQuote.split (",");

				if (null == astrCOBRecord) {
					brSwapCOB.close();

					return;
				}

				int iNumQuote = astrCOBRecord.length;

				if (0 == iNumQuote) {
					brSwapCOB.close();

					return;
				}

				if (bIsHeader) {
					bIsHeader = false;
					astrTenor = new java.lang.String[iNumQuote - 1];

					for (int i = 1; i < iNumQuote; ++i)
						astrTenor[i - 1] = astrCOBRecord[i];
				} else {
					org.drip.service.api.DiscountCurveInputInstrument dcci = ProcessCOBInput (astrTenor,
						astrCOBRecord);

					if (null != dcci) {
						org.drip.analytics.date.JulianDate dtCOB = dcci.date();

						org.drip.param.valuation.ValuationParams valParams = new
							org.drip.param.valuation.ValuationParams (dtCOB, dtCOB, "USD");

						org.drip.analytics.rates.DiscountCurve dc = BuildCurve (dtCOB, dcci.cashTenor(),
							dcci.cashQuote(), dcci.swapTenor(), dcci.swapQuote(), "USD");

						System.out.println (dtCOB + " => " + dc);

						java.util.List<org.drip.service.api.CDXCOB> lsCDXNamedPrice =
							mapDatedCDXClose.get (dtCOB);

						if (null != lsCDXNamedPrice && 0 != lsCDXNamedPrice.size()) {
							for (org.drip.service.api.CDXCOB cdxNP : lsCDXNamedPrice) {
								org.drip.product.definition.CreditDefaultSwap cdx =
									org.drip.product.creator.CDSBuilder.CreateSNAC (dtCOB, "5Y", 0.05,
										cdxNP.name());

								org.drip.analytics.definition.CreditCurve cc =
									org.drip.param.creator.CreditScenarioCurveBuilder.CreateCreditCurve
										("CC", dtCOB, new org.drip.product.definition.CreditDefaultSwap[]
											{cdx}, dc, new double[] {100. * cdxNP.price()}, new
												java.lang.String[] {"Price"}, 0.04, false);

								org.drip.analytics.support.CaseInsensitiveTreeMap<java.lang.Double>
									mapCDXMeasures = cdx.value (valParams, pricerParams,
										org.drip.param.creator.ComponentMarketParamsBuilder.MakeCreditCMP
											(dc, cc), null);

								_writeCOB.write (dtCOB + "," + cdxNP.name() + "," + cdxNP.price() + "," +
									org.drip.quant.common.FormatUtil.FormatDouble (mapCDXMeasures.get
										("FairPremium"), 1, 2, 1.));

								_writeCOB.newLine();

								_writeCOB.flush();
							}
						}
					}
				}
			}
		} catch (java.lang.Exception e) {
			e.printStackTrace();

			return;
		}

		try {
			brSwapCOB.close();
		} catch (java.lang.Exception e) {
			e.printStackTrace();
		}
	}

	public static final void main (
		final java.lang.String[] astrArgs)
	{
		org.drip.service.api.CreditAnalytics.Init ("");

		java.util.Map<org.drip.analytics.date.JulianDate, java.util.List<org.drip.service.api.CDXCOB>>
			mapDatedCDXClose = LoadCDXCloses ("c:\\IFA\\CDXOP\\CDX_HY_PX_5Y_CONTRACTS_LAST_Orig.txt");

		ProcessCDXQuote (mapDatedCDXClose);

		// GenerateEMCurveMetrics ("ZAR");
	}
}
