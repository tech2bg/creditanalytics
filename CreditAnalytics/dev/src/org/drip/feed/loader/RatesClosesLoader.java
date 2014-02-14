
package org.drip.feed.loader;

/*
 * -*- mode: java; tab-width: 4; indent-tabs-mode: nil; c-basic-offset: 4 -*-
 */

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
 */

/**
 * RatesClosesLoader Loads the closing marks for a given Rates Curve.
 * 
 * @author Lakshmi Krishnamurthy
 */

public class RatesClosesLoader {
	private static java.io.BufferedWriter _writeCOB = null;
	private static final java.lang.String[] s_astrFwdTenor = new java.lang.String[] {"1Y", "2Y", "3Y", "4Y",
		"5Y", "6Y", "7Y", "8Y", "9Y", "10Y", "11Y", "12Y"};

	private static final double calcMeasure (
		final org.drip.product.definition.Component comp,
		final org.drip.analytics.date.JulianDate dt,
		final org.drip.analytics.rates.DiscountCurve dc,
		final java.lang.String strMeasure,
		final java.lang.String strCurrency)
		throws java.lang.Exception
	{
		if (comp.getMaturityDate().getJulian() <= dt.getJulian()) return 0.;

		return comp.value (new org.drip.param.valuation.ValuationParams (dt, dt, strCurrency), null,
			org.drip.param.creator.ComponentMarketParamsBuilder.CreateComponentMarketParams (dc, null, null,
				null, null, null, null), null).get (strMeasure);
	}

	private static final double calcCarry (
		final org.drip.product.definition.Component comp,
		final org.drip.analytics.date.JulianDate dt1,
		final org.drip.analytics.date.JulianDate dt2,
		final org.drip.analytics.rates.DiscountCurve dc,
		final java.lang.String strCurrency)
		throws java.lang.Exception
	{
		org.drip.product.rates.IRSComponent irs = (org.drip.product.rates.IRSComponent) comp;

		double dblFixedCoupon = irs.getFixedStream().getCoupon (dt1.getJulian(), null);

		double dblFloatingRate = irs.getFloatStream().getCoupon (dt1.getJulian(), new
			org.drip.param.market.ComponentMarketParamSet (dc, null, null, null, null, null, null, null));

		return dblFixedCoupon * org.drip.analytics.daycount.Convention.YearFraction (dt1.getJulian(),
			dt2.getJulian(), "30/360", false, java.lang.Double.NaN, null, strCurrency) - dblFloatingRate *
				org.drip.analytics.daycount.Convention.YearFraction (dt1.getJulian(), dt2.getJulian(),
					"Act/360", false, java.lang.Double.NaN, null, strCurrency);
	}

	private static final double calcRollDown (
		final org.drip.product.definition.Component comp,
		final org.drip.analytics.date.JulianDate dt1,
		final org.drip.analytics.date.JulianDate dt2,
		final org.drip.analytics.rates.DiscountCurve dc,
		final java.lang.String strCurrency)
		throws java.lang.Exception
	{
		return calcMeasure (comp, dt2, dc, "CalibSwapRate", strCurrency) - calcMeasure (comp, dt1, dc,
			"CalibSwapRate", strCurrency);
	}

	private static final double calcCurveShift (
		final org.drip.product.definition.Component comp,
		final org.drip.analytics.date.JulianDate dt,
		final org.drip.analytics.rates.DiscountCurve dc1,
		final org.drip.analytics.rates.DiscountCurve dc2,
		final java.lang.String strCurrency)
		throws java.lang.Exception
	{
		return calcMeasure (comp, dt, dc2, "CalibSwapRate", strCurrency) - calcMeasure (comp, dt, dc1,
			"CalibSwapRate", strCurrency);
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
		double dblDV01_1 = calcMeasure (comp, dt1, dc1, "FixedDV01", strCurrency);

		double dblDV01_2 = calcMeasure (comp, dt1, dc1, "FixedDV01", strCurrency);

		return dblDV01_2 * 10000. * calcMeasure (comp, dt2, dc2, "CalibSwapRate", strCurrency) - dblDV01_1 *
			10000. * calcMeasure (comp, dt1, dc1, "CalibSwapRate", strCurrency);
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

	private static final java.lang.String ComputePnLMetrics (
		final org.drip.analytics.date.JulianDate dt0D,
		final org.drip.analytics.date.JulianDate dt1D,
		final org.drip.product.definition.Component comp,
		final org.drip.analytics.rates.DiscountCurve dcDate0Quote0,
		final org.drip.analytics.rates.DiscountCurve dcDate0Quote1,
		final org.drip.analytics.rates.DiscountCurve dcDate1Quote1,
		final double dblCalibSwapRate,
		final java.lang.String strCurrency)
		throws java.lang.Exception
	{
		org.drip.analytics.date.JulianDate dt1M = dt0D.addTenor ("1M");

		org.drip.analytics.date.JulianDate dt3M = dt0D.addTenor ("3M");

		double dblDV01 = calcMeasure (comp, dt0D, dcDate0Quote0, "FixedDV01", strCurrency);

		double dbl1DCarryPnL = calcCarry (comp, dt0D, dt1D, dcDate0Quote0, strCurrency);

		double dbl1DReturnPnL = calcReturn (comp, dt0D, dt1D, dcDate0Quote0, dcDate1Quote1, strCurrency);

		double dbl1DRollDownPnL = calcRollDown (comp, dt0D, dt1D, dcDate0Quote0, strCurrency) * 10000. *
			dblDV01;

		double dbl1DCurveShiftPnL = calcCurveShift (comp, dt1D, dcDate0Quote0, dcDate0Quote1, strCurrency) *
			10000. * dblDV01;

		double dbl1MCarryPnL = calcCarry (comp, dt0D, dt1M, dcDate0Quote0, strCurrency);

		double dbl1MRollDownPnL = calcRollDown (comp, dt0D, dt1M, dcDate0Quote0, strCurrency) * 10000. *
			dblDV01;

		double dbl3MCarryPnL = calcCarry (comp, dt0D, dt3M, dcDate0Quote0, strCurrency);

		double dbl3MRollDownPnL = calcRollDown (comp, dt0D, dt3M, dcDate0Quote0, strCurrency) * 10000. *
			dblDV01;

		try {
			return new org.drip.service.api.ProductDailyPnL (dbl1DReturnPnL, dbl1DCarryPnL, dbl1DRollDownPnL,
				dbl1DCurveShiftPnL, dbl1MCarryPnL, dbl1MRollDownPnL, dbl3MCarryPnL, dbl3MRollDownPnL,
					dblDV01, dblCalibSwapRate).toString();
		} catch (java.lang.Exception e) {
			e.printStackTrace();
		}

		return null;
	}

	private static final java.lang.String ComputeForwardMetric (
		final org.drip.product.definition.Component[] aComp,
		final org.drip.analytics.rates.DiscountCurve dc)
		throws java.lang.Exception
	{
		org.drip.service.api.ForwardRates fmOP = new org.drip.service.api.ForwardRates();

		for (int i = 0; i < aComp.length; ++i) {
			for (int j = 0; j < aComp.length; ++j) {
				double dblForward = Forward (dc, aComp[j].getMaturityDate(), aComp[i].getMaturityDate());

				if (0 != dblForward) fmOP.addForward (dblForward);
			}
		}

		return fmOP.toString();
	}

	private static final java.util.List<java.lang.String> GenerateMetrics (
		final org.drip.analytics.date.JulianDate dt0D,
		final org.drip.analytics.date.JulianDate dt1D,
		final org.drip.analytics.rates.DiscountCurve dcDate0Quote0,
		final org.drip.analytics.rates.DiscountCurve dcDate0Quote1,
		final org.drip.analytics.rates.DiscountCurve dcDate1Quote1,
		final java.lang.String strCurrency)
		throws java.lang.Exception
	{
		java.util.List<java.lang.String> lsstrDump = new java.util.ArrayList<java.lang.String>();

		double[] adblCalibSwapRate = new double[s_astrFwdTenor.length];
		org.drip.product.definition.Component[] aComp = new
			org.drip.product.definition.Component[s_astrFwdTenor.length];

		for (int i = 0; i < s_astrFwdTenor.length; ++i) {
			org.drip.product.definition.Component comp =
				org.drip.product.creator.RatesStreamBuilder.CreateIRS (dt0D, dt0D.addTenorAndAdjust
					(s_astrFwdTenor[i], strCurrency), 0.01, strCurrency, strCurrency + "-LIBOR-3M",
						strCurrency);

			adblCalibSwapRate[i] = calcMeasure (comp, dt0D, dcDate0Quote0, "CalibSwapRate", strCurrency);

			aComp[i] = org.drip.product.creator.RatesStreamBuilder.CreateIRS (dt0D, dt0D.addTenorAndAdjust
				(s_astrFwdTenor[i], strCurrency), adblCalibSwapRate[i], strCurrency, strCurrency +
					"-LIBOR-3M", strCurrency);
		}

		for (int i = 0; i < s_astrFwdTenor.length; ++i)
			lsstrDump.add (dt1D.toString() + "," + aComp[i].tenor() + "," + ComputePnLMetrics (dt0D, dt1D,
				aComp[i], dcDate0Quote0, dcDate0Quote1, dcDate1Quote1, adblCalibSwapRate[i], strCurrency) +
					"," + ComputeForwardMetric (aComp, dcDate1Quote1));

		return lsstrDump;
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
					strCurrency + "-LIBOR-3M", strCurrency);

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

		if (null == rbsCash && null == rbsSwap) return null;

		org.drip.state.estimator.StretchRepresentationSpec[] aRRS = null;

		if (null == rbsCash)
			aRRS = new org.drip.state.estimator.StretchRepresentationSpec[] {rbsSwap};
		else if (null == rbsSwap)
			aRRS = new org.drip.state.estimator.StretchRepresentationSpec[] {rbsCash};
		else
			aRRS = new org.drip.state.estimator.StretchRepresentationSpec[] {rbsCash, rbsSwap};

		org.drip.analytics.rates.DiscountCurve dcShapePreserving = null;

		try {
			org.drip.param.valuation.ValuationParams valParams = new org.drip.param.valuation.ValuationParams
				(dt, dt, strCurrency);

			org.drip.spline.params.ResponseScalingShapeControl rssc = new
				org.drip.spline.params.ResponseScalingShapeControl (true, new
					org.drip.quant.function1D.QuadraticRationalShapeControl (0.));

			org.drip.spline.params.SegmentInelasticDesignControl sdic =
				org.drip.spline.params.SegmentInelasticDesignControl.Create (2, 2);

			org.drip.state.estimator.LinearCurveCalibrator lcc = new
				org.drip.state.estimator.LinearCurveCalibrator (new
					org.drip.spline.params.SegmentCustomBuilderControl
						(org.drip.spline.stretch.MultiSegmentSequenceBuilder.BASIS_SPLINE_KLK_HYPERBOLIC_TENSION,
				new org.drip.spline.basis.ExponentialTensionSetParams (1.), sdic, rssc, null),
					org.drip.spline.stretch.BoundarySettings.NaturalStandard(),
						org.drip.spline.stretch.MultiSegmentSequence.CALIBRATE, null, null);

			dcShapePreserving = org.drip.param.creator.RatesScenarioCurveBuilder.ShapePreservingDFBuild (lcc,
				aRRS, valParams, null, null, null, 1.0);

			return dcShapePreserving;

			/* if (null == dcShapePreserving) return null;

			org.drip.state.estimator.LocalControlCurveParams lccpHyman83 = new
				org.drip.state.estimator.LocalControlCurveParams
					(org.drip.spline.pchip.LocalMonotoneCkGenerator.C1_HYMAN83,
						org.drip.analytics.rates.DiscountCurve.QUANTIFICATION_METRIC_ZERO_RATE, new
							org.drip.spline.params.SegmentCustomBuilderControl
								(org.drip.spline.stretch.MultiSegmentSequenceBuilder.BASIS_SPLINE_POLYNOMIAL,
									new org.drip.spline.basis.PolynomialFunctionSetParams (4), sdic, rssc,
										null), org.drip.spline.stretch.MultiSegmentSequence.CALIBRATE, null,
											null, true, true);

			org.drip.analytics.rates.DiscountCurve dcHyman83Smooth =
				org.drip.param.creator.RatesScenarioCurveBuilder.SmoothingLocalControlBuild
					(dcShapePreserving, lcc, lccpHyman83, aRRS, valParams, null, null, null);

			return null == dcHyman83Smooth ? dcShapePreserving : dcHyman83Smooth; */
		} catch (java.lang.Exception e) {
			e.printStackTrace();
		}

		return dcShapePreserving;
	}

	private static final org.drip.service.api.DiscountCurveInputInstrument ProcessCOBInput (
		final java.lang.String[] astrTenor,
		final java.lang.String[] astrCOBRecord)
	{
		if (null == astrCOBRecord) return null;

		int iNumQuote = astrCOBRecord.length;

		if (iNumQuote != astrTenor.length + 1) return null;

		java.util.List<java.lang.String> lsCashTenor = new java.util.ArrayList<java.lang.String>();

		java.util.List<java.lang.String> lsSwapTenor = new java.util.ArrayList<java.lang.String>();

		java.util.List<java.lang.Double> lsCashQuote = new java.util.ArrayList<java.lang.Double>();

		java.util.List<java.lang.Double> lsSwapQuote = new java.util.ArrayList<java.lang.Double>();

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
			e.printStackTrace();
		}

		return null;
	}

	private static final org.drip.service.api.DiscountCurveInputInstrument[] ProcessCOBInput (
		final java.lang.String[] astrTenor,
		final java.lang.String[] astrCOBRecord1,
		final java.lang.String[] astrCOBRecord2)
	{
		if (null == astrCOBRecord1 || null == astrCOBRecord2) return null;

		int iNumQuote1 = astrCOBRecord1.length;
		int iNumQuote2 = astrCOBRecord2.length;

		if (iNumQuote2 != astrTenor.length + 1 || iNumQuote1 != iNumQuote2) return null;

		java.util.List<java.lang.String> lsCashTenor = new java.util.ArrayList<java.lang.String>();

		java.util.List<java.lang.String> lsSwapTenor = new java.util.ArrayList<java.lang.String>();

		java.util.List<java.lang.Double> lsCashQuote1 = new java.util.ArrayList<java.lang.Double>();

		java.util.List<java.lang.Double> lsSwapQuote1 = new java.util.ArrayList<java.lang.Double>();

		java.util.List<java.lang.Double> lsCashQuote2 = new java.util.ArrayList<java.lang.Double>();

		java.util.List<java.lang.Double> lsSwapQuote2 = new java.util.ArrayList<java.lang.Double>();

		org.drip.analytics.date.JulianDate dt1 = org.drip.analytics.date.JulianDate.CreateFromMDY
			(astrCOBRecord1[0], "/");

		org.drip.analytics.date.JulianDate dt2 = org.drip.analytics.date.JulianDate.CreateFromMDY
			(astrCOBRecord2[0], "/");

		if (null == dt1 || null == dt2) return null;

		for (int i = 1; i < iNumQuote2; ++i) {
			double dblQuote1 = java.lang.Double.NaN;
			double dblQuote2 = java.lang.Double.NaN;

			try {
				dblQuote1 = 0.01 * Double.parseDouble (astrCOBRecord1[i]);

				dblQuote2 = 0.01 * Double.parseDouble (astrCOBRecord2[i]);
			} catch (java.lang.Exception e) {
				dblQuote1 = java.lang.Double.NaN;
				dblQuote2 = java.lang.Double.NaN;
			}

			if (org.drip.quant.common.NumberUtil.IsValid (dblQuote1) &&
				org.drip.quant.common.NumberUtil.IsValid (dblQuote2)) {
				if ("1M".equalsIgnoreCase (astrTenor[i - 1])) {
					lsCashTenor.add ("1M");

					lsCashQuote1.add (dblQuote1);

					lsCashQuote2.add (dblQuote2);
				} else {
					lsSwapTenor.add (astrTenor[i - 1]);

					lsSwapQuote1.add (dblQuote1);

					lsSwapQuote2.add (dblQuote2);
				}
			}
		}

		if ((null == lsCashTenor || 0 == lsCashTenor.size()) && (null == lsSwapTenor || 0 ==
			lsSwapTenor.size())) {
			System.out.println ("\t\tBad Tenor Straddle: " + dt1 + " | " + dt2);

			return null;
		}

		try {
			return new org.drip.service.api.DiscountCurveInputInstrument[] {new
				org.drip.service.api.DiscountCurveInputInstrument (dt1, lsCashTenor, lsCashQuote1,
					lsSwapTenor, lsSwapQuote1), new org.drip.service.api.DiscountCurveInputInstrument (dt2,
						lsCashTenor, lsCashQuote2, lsSwapTenor, lsSwapQuote2)};
		} catch (java.lang.Exception e) {
			e.printStackTrace();
		}

		return null;
	}

	public static final java.util.List<java.lang.String> ProcessRecord (
		final java.lang.String[] astrTenor,
		final java.lang.String[] astrCOBRecord1,
		final java.lang.String[] astrCOBRecord2,
		final java.lang.String strCurrency,
		boolean bDumpOnDemand)
	{
		org.drip.service.api.DiscountCurveInputInstrument[] aDCII = ProcessCOBInput (astrTenor,
			astrCOBRecord1, astrCOBRecord2);

		if (null == aDCII || 2 != aDCII.length) return null;

		org.drip.analytics.date.JulianDate dt0 = aDCII[0].date();

		org.drip.analytics.rates.DiscountCurve dcDate0Quote0 = BuildCurve (dt0, aDCII[0].cashTenor(),
			aDCII[0].cashQuote(), aDCII[0].swapTenor(), aDCII[0].swapQuote(), strCurrency);

		org.drip.analytics.date.JulianDate dt1 = aDCII[1].date();

		org.drip.analytics.rates.DiscountCurve dcDate0Quote1 = BuildCurve (dt0, aDCII[1].cashTenor(),
			aDCII[1].cashQuote(), aDCII[1].swapTenor(), aDCII[1].swapQuote(), strCurrency);

		org.drip.analytics.rates.DiscountCurve dcDate1Quote1 = BuildCurve (dt1, aDCII[1].cashTenor(),
			aDCII[1].cashQuote(), aDCII[1].swapTenor(), aDCII[1].swapQuote(), strCurrency);

		System.out.println ("\tDC[" + dt0 + "]: " + dcDate0Quote0 + "; DC[" + dt1 + "]: " + dcDate0Quote1);

		java.util.List<java.lang.String> lsstrDump = null;

		if (null != dt0 && null != dt1 && null != dcDate0Quote0 && null != dcDate0Quote1) {
			try {
				lsstrDump = GenerateMetrics (dt0, dt1, dcDate0Quote0, dcDate0Quote1, dcDate1Quote1,
					strCurrency);

				for (java.lang.String strDump : lsstrDump) {
					if (bDumpOnDemand) {
						_writeCOB.write (strDump);

						_writeCOB.newLine();

						_writeCOB.flush();
					}
				}
			} catch (java.lang.Exception e) {
				e.printStackTrace();

				return null;
			}
		}

		return lsstrDump;
	}

	public static final boolean ExecUnitSequence()
	{
		java.lang.String[] astrTenor = new java.lang.String[] {"1M", "1Y", "5Y", "10Y"};
		java.lang.String[][] aastrCOBRecord = new java.lang.String[][] {
			new java.lang.String[] {"3/28/2013", "0.21", "0.52", "1.17", "1.68"},
			new java.lang.String[] {"3/29/2013", "0.21", "0.53", "1.19", "1.71"}
		};

		for (int i = 1; i < aastrCOBRecord.length; ++i) {
			java.util.List<java.lang.String> lsstrDump = ProcessRecord (astrTenor, aastrCOBRecord[i - 1],
				aastrCOBRecord[i], "USD", false);

			if (null == lsstrDump || 0 == lsstrDump.size()) return false;

			for (java.lang.String strDump : lsstrDump)
				System.out.println (strDump);
		}

		return true;
	}

	public static final void GenerateDiscountCurveMetrics (
		final java.lang.String strCurrency)
	{
		boolean bIsHeader = true;
		java.lang.String strCOBQuote = "";
		java.lang.String[] astrTenor = null;
		java.io.BufferedReader brSwapCOB = null;
		java.lang.String[] astrCOBRecordPrev = null;

		try {
			brSwapCOB = new java.io.BufferedReader (new java.io.FileReader ("C:\\IFA\\EMRates\\" +
				strCurrency + "_Clean_Input.txt"));

			_writeCOB = new java.io.BufferedWriter (new java.io.FileWriter ("C:\\IFA\\EMRates\\" +
				strCurrency + "_Metric.csv"));

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

					_writeCOB.write ("1MCarry, 1MRollDown, 3MCarry, 3MRollDown, DV01, SwapRate, ");

					_writeCOB.write ("1Y1YF, 1Y2YF, 1Y3YF, 1Y4YF, 1Y5YF, 1Y6YF, 1Y7YF, 1Y8YF, 1Y8YF, 1Y10YF, 1Y11YF, ");

					_writeCOB.write ("2Y1YF, 2Y2YF, 2Y3YF, 2Y4YF, 2Y5YF, 2Y6YF, 2Y7YF, 2Y8YF, 2Y9YF, 2Y10YF, ");

					_writeCOB.write ("3Y1YF, 3Y2YF, 3Y3YF, 3Y4YF, 3Y5YF, 3Y6YF, 3Y7YF, 3Y8YF, 3Y9YF, ");

					_writeCOB.write ("4Y1YF, 4Y2YF, 4Y3YF, 4Y4YF, 4Y5YF, 4Y6YF, 4Y7YF, 4Y8YF, ");

					_writeCOB.write ("5Y1YF, 5Y2YF, 5Y3YF, 5Y4YF, 5Y5YF, 5Y6YF, 5Y7YF, ");

					_writeCOB.write ("6Y1YF, 6Y2YF, 6Y3YF, 6Y4YF, 6Y5YF, 6Y6YF, ");

					_writeCOB.write ("7Y1YF, 7Y2YF, 7Y3YF, 7Y4YF, 7Y5YF, ");

					_writeCOB.write ("8Y1YF, 8Y2YF, 8Y3YF, 8Y4YF, ");

					_writeCOB.write ("9Y1YF, 9Y2YF, 9Y3YF, ");

					_writeCOB.write ("10Y1YF, 10Y2YF, ");

					_writeCOB.write ("11Y1YF, ");

					_writeCOB.newLine();

					_writeCOB.flush();
				} else
					ProcessRecord (astrTenor, astrCOBRecordPrev, astrCOBRecord, strCurrency, true);

				astrCOBRecordPrev = astrCOBRecord;
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

						java.util.List<org.drip.service.api.CDXCOB> lsCDXNamedPrice = mapDatedCDXClose.get
							(dtCOB);

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
		long lStartTime = System.nanoTime();

		org.drip.service.api.CreditAnalytics.Init ("");

		/* java.util.Map<org.drip.analytics.date.JulianDate, java.util.List<org.drip.service.api.CDXCOB>>
			mapDatedCDXClose = LoadCDXCloses ("c:\\IFA\\CDXOP\\CDX_HY_PX_5Y_CONTRACTS_LAST_Orig.txt");

		ProcessCDXQuote (mapDatedCDXClose); */

		GenerateDiscountCurveMetrics ("ZAR");

		// ExecUnitSequence();

		System.out.println ("Time Taken: " + ((System.nanoTime() - lStartTime) * 1.e-9) + " sec");
	}
}
