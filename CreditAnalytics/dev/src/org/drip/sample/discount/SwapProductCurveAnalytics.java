
package org.drip.sample.discount;

import java.util.*;

import org.drip.analytics.date.JulianDate;
import org.drip.analytics.definition.DiscountCurve;
import org.drip.analytics.support.CaseInsensitiveTreeMap;
import org.drip.math.common.FormatUtil;
import org.drip.math.function.QuadraticRationalShapeControl;
import org.drip.math.regime.*;
import org.drip.math.segment.*;
import org.drip.math.spline.*;
import org.drip.param.creator.*;
import org.drip.param.valuation.ValuationParams;
import org.drip.product.creator.*;
import org.drip.product.definition.CalibratableComponent;
import org.drip.service.api.CreditAnalytics;
import org.drip.state.estimator.*;

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
 * SwapProductCurveAnalytics contains a demo of a demo of the curve based analytical measures for a Swap. It
 * 	shows the following:
 * 	- 1-D Return (T) => Dirty PV (Par Swap Entered at T - 1, Date = T, Curve at T) -
 * 		Dirty PV (Par Swap Entered at T - 1, Date = T - 1, Curve at T - 1)
 * 	- 1-D Coupon Carry (T) => Coupon Accrued (Par Swap Entered at T, Date = T + 1, Curve at T) -
 * 		Coupon Accrued (Par Swap Entered at T, Date = T, Curve at T)
 * 	- 1-D Roll Down (T) => DV01 (T - 1) * {Fair Premium (Par Swap Entered at T, Date = T + 1, Curve at T) -
 * 		Fair Premium (Par Swap Entered at T, Date = T, Curve at T)}
 * 	- 1-D Curve Shift (T) => DV01 (T - 1) * {Fair Premium (Par Swap Entered at T - 1, Date = T, Curve at T) -
 * 		Fair Premium (Par Swap Entered at T - 1, Date = T, Curve at T - 1)}
 * 
 * DV01 (T) ==> DV01 (Par Swap Entered at T, Date = T, Curve at T)
 * 
 * 1-D Return (T) ~ 1-D Coupon Carry (T - 1) + 1-D Roll Down (T - 1) + 1-D Curve Shift (T)
 * 
 * @author Lakshmi Krishnamurthy
 */

public class SwapProductCurveAnalytics {
	public static final boolean s_bBlog = true;
	public static java.io.BufferedWriter _writeLog = null;

	static class PnLMetric {
		double _dbl1DReturn = Double.NaN;
		double _dbl1DCarry = Double.NaN;
		double _dbl1DRollDown = Double.NaN;
		double _dbl1DCurveShift = Double.NaN;
		double _dbl1MCarry = Double.NaN;
		double _dbl1MRollDown = Double.NaN;
		double _dbl3MCarry = Double.NaN;
		double _dbl3MRollDown = Double.NaN;
		double _dblDV01 = Double.NaN;

		PnLMetric (
			final double dbl1DReturn,
			final double dbl1DCarry,
			final double dbl1DRollDown,
			final double dbl1DCurveShift,
			final double dbl1MCarry,
			final double dbl1MRollDown,
			final double dbl3MCarry,
			final double dbl3MRollDown,
			final double dblDV01)
		{
			_dbl1DReturn = dbl1DReturn;
			_dbl1DCarry = dbl1DCarry;
			_dbl1DRollDown = dbl1DRollDown;
			_dbl1DCurveShift = dbl1DCurveShift;
			_dbl1MCarry = dbl1MCarry;
			_dbl1MRollDown = dbl1MRollDown;
			_dbl3MCarry = dbl3MCarry;
			_dbl3MRollDown = dbl3MRollDown;
			_dblDV01 = dblDV01;
		}

		double[] toArray()
		{
			List<Double> lsPnLMetric = new ArrayList<Double>();

			lsPnLMetric.add (_dbl1DReturn);

			lsPnLMetric.add (_dbl1DCarry);

			lsPnLMetric.add (_dbl1DRollDown);

			lsPnLMetric.add (_dbl1DCurveShift);

			lsPnLMetric.add (_dbl1MCarry);

			lsPnLMetric.add (_dbl1MRollDown);

			lsPnLMetric.add (_dbl3MCarry);

			lsPnLMetric.add (_dbl3MRollDown);

			lsPnLMetric.add (_dblDV01);

			int i = 0;

			double[] adblSPCA = new double[lsPnLMetric.size()];

			for (double dbl : lsPnLMetric)
				adblSPCA[i++] = dbl;

			return adblSPCA;
		}

		public String toString()
		{
			StringBuffer sb = new StringBuffer();

			boolean bStart = true;

			for (double dbl : toArray()) {
				if (bStart)
					bStart = false;
				else
					sb.append (",");

				sb.append (dbl);
			}

			return sb.toString();
		}
	}

	static class FwdMetric {
		List<Double> _lsForward = new ArrayList<Double>();

		FwdMetric ()
		{
		}

		void addForward (
			final double dblForward)
		{
			_lsForward.add (dblForward);
		}

		double[] toArray()
		{
			if (0 == _lsForward.size()) return null;

			int i = 0;

			double[] adblForward = new double[_lsForward.size()];

			for (double dbl : adblForward)
				adblForward[i++] = dbl;

			return adblForward;
		}

		public String toString()
		{
			StringBuffer sb = new StringBuffer();

			boolean bStart = true;

			for (double dbl : toArray()) {
				if (bStart)
					bStart = false;
				else
					sb.append (",");

				sb.append (dbl);
			}

			return sb.toString();
		}
	}

	static class InstrMetric {
		FwdMetric _fwdMetric = null;
		PnLMetric _pnlMetric = null;

		InstrMetric (
			final FwdMetric fwdMetric,
			final PnLMetric pnlMetric)
		{
			_fwdMetric = fwdMetric;
			_pnlMetric = pnlMetric;
		}

		double[] toArray()
		{
			double[] adblPnLMetric = _pnlMetric.toArray();

			double[] adblFwdMetric = _fwdMetric.toArray();

			int i = 0;
			double[] adblInstrMetric = new double[adblFwdMetric.length + adblPnLMetric.length];

			for (double dbl : adblPnLMetric)
				adblInstrMetric[i++] = dbl;

			for (double dbl : adblFwdMetric)
				adblInstrMetric[i++] = dbl;

			return adblInstrMetric;
		}

		public String toString()
		{
			StringBuffer sb = new StringBuffer();

			boolean bStart = true;

			for (double dbl : toArray()) {
				if (bStart)
					bStart = false;
				else
					sb.append (",");

				sb.append (dbl);
			}

			return sb.toString();
		}
	}

	static class SPCAMetric {
		JulianDate _dt = null;
		String _strCurrency = "";
		FwdMetric _fwdMetric = null;

		List<PnLMetric> _lsPnLMetric = new ArrayList<PnLMetric>();

		SPCAMetric (
			final JulianDate dt,
			final String strCurrency)
			throws Exception
		{
			_dt = dt;
			_strCurrency = strCurrency;
		}

		void addPnLMetric (
			final PnLMetric pnLMetric)
		{
			_lsPnLMetric.add (pnLMetric);
		}

		void addFwdMetric (
			final FwdMetric fwdMetric)
		{
			_fwdMetric = fwdMetric;
		}

		InstrMetric[] instrMetric()
			throws Exception
		{
			InstrMetric[] aIM = new InstrMetric[_lsPnLMetric.size()];

			int i = 0;

			for (PnLMetric pnlMetric : _lsPnLMetric)
				aIM[i++] = new InstrMetric (_fwdMetric, pnlMetric);

			return aIM;
		}
	}

	private static final CalibratableComponent[] CashInstrumentsFromMaturityDays (
		final JulianDate dtEffective,
		final String[] astrTenor)
		throws Exception
	{
		CalibratableComponent[] aCalibComp = new CalibratableComponent[astrTenor.length];

		for (int i = 0; i < astrTenor.length; ++i)
			aCalibComp[i] = CashBuilder.CreateCash (dtEffective, dtEffective.addTenorAndAdjust (astrTenor[i], "MXN"), "MXN");

		return aCalibComp;
	}

	private static final CalibratableComponent[] SwapInstrumentsFromMaturityTenor (
		final JulianDate dtEffective,
		final String[] astrTenor,
		final double[] adblSwapQuote)
		throws Exception
	{
		CalibratableComponent[] aCalibComp = new CalibratableComponent[astrTenor.length];

		for (int i = 0; i < astrTenor.length; ++i)
			aCalibComp[i] = RatesStreamBuilder.CreateIRS (
				dtEffective,
				dtEffective.addTenorAndAdjust (astrTenor[i], "MXN"),
				adblSwapQuote[i],
				"MXN",
				"MXN-LIBOR-6M",
				"MXN");

		return aCalibComp;
	}

	public static final DiscountCurve BuildCurve (
		final JulianDate dt,
		final String[] astrCashTenor,
		final double[] adblCashQuote,
		final String[] astrSwapTenor,
		final double[] adblSwapQuote)
		throws Exception
	{
		CalibratableComponent[] aCashComp = CashInstrumentsFromMaturityDays (dt, astrCashTenor);

		RegimeRepresentationSpec rbsCash = RegimeRepresentationSpec.CreateRegimeBuilderSet (
			"CASH",
			DiscountCurve.LATENT_STATE_DISCOUNT,
			DiscountCurve.QUANTIFICATION_METRIC_DISCOUNT_FACTOR,
			aCashComp,
			"Rate",
			adblCashQuote);

		CalibratableComponent[] aSwapComp = SwapInstrumentsFromMaturityTenor (dt, astrSwapTenor, adblSwapQuote);

		RegimeRepresentationSpec rbsSwap = RegimeRepresentationSpec.CreateRegimeBuilderSet (
			"SWAP",
			DiscountCurve.LATENT_STATE_DISCOUNT,
			DiscountCurve.QUANTIFICATION_METRIC_DISCOUNT_FACTOR,
			aSwapComp,
			"Rate",
			adblSwapQuote);

		RegimeRepresentationSpec[] aRRS = new RegimeRepresentationSpec[] {rbsCash, rbsSwap};

		LinearCurveCalibrator lcc = new LinearCurveCalibrator (
			new PredictorResponseBuilderParams (
				RegimeBuilder.BASIS_SPLINE_EXPONENTIAL_MIXTURE,
				new ExponentialMixtureBasisSetParams (new double[] {0.01, 0.05, 0.25}),
				DesignInelasticParams.Create (2, 2),
				new ResponseScalingShapeController (true, new QuadraticRationalShapeControl (0.))),
			MultiSegmentRegime.BOUNDARY_CONDITION_NATURAL,
			MultiSegmentRegime.CALIBRATE,
			null);

		DiscountCurve dcShapePreserving = RatesScenarioCurveBuilder.ShapePreservingBuild (
			lcc,
			aRRS,
			new ValuationParams (dt, dt, "MXN"),
			null,
			null,
			null);

		return dcShapePreserving;

		/* LocalControlCurveParams lccpHyman83 = new LocalControlCurveParams (
			org.drip.math.pchip.LocalControlRegime.C1_HYMAN83,
			org.drip.analytics.definition.DiscountCurve.QUANTIFICATION_METRIC_ZERO_RATE,
			new PredictorResponseBuilderParams (
				RegimeBuilder.BASIS_SPLINE_POLYNOMIAL,
				new PolynomialBasisSetParams (4),
				DesignInelasticParams.Create (2, 2),
				new ResponseScalingShapeController (true, new QuadraticRationalShapeControl (0.))),
			MultiSegmentRegime.CALIBRATE,
			null,
			true,
			true);

		return RatesScenarioCurveBuilder.SmoothingLocalControlBuild (
			dcShapePreserving,
			lcc,
			lccpHyman83,
			aRRS,
			new ValuationParams (dt, dt, "MXN"),
			null,
			null,
			null); */
	}

	public static final CalibratableComponent[] CalibInstr (
		final JulianDate dt,
		final String[] astrCashTenor,
		final double[] adblCashQuote,
		final String[] astrSwapTenor,
		final double[] adblSwapQuote)
		throws Exception
	{
		CalibratableComponent[] aCashComp = CashInstrumentsFromMaturityDays (dt, astrCashTenor);

		CalibratableComponent[] aSwapComp = SwapInstrumentsFromMaturityTenor (dt, astrSwapTenor, adblSwapQuote);

		CalibratableComponent[] aCalibComp = new CalibratableComponent[aCashComp.length + aSwapComp.length];

		int iComp = 0;

		for (CalibratableComponent comp : aCashComp)
			aCalibComp[iComp++] = comp;

		for (CalibratableComponent comp : aSwapComp)
			aCalibComp[iComp++] = comp;

		return aCalibComp;
	}

	public static final double calcMeasure (
		final CalibratableComponent irs,
		final JulianDate dt,
		final DiscountCurve dc,
		final String strMeasure)
		throws Exception
	{
		CaseInsensitiveTreeMap<Double> mapIndexFixing = new CaseInsensitiveTreeMap<Double>();

		mapIndexFixing.put ("USD-LIBOR-3M", 0.05);

		Map<JulianDate, CaseInsensitiveTreeMap<Double>> mmFixings = new HashMap<JulianDate, CaseInsensitiveTreeMap<Double>>();

		mmFixings.put (dt, mapIndexFixing);

		CaseInsensitiveTreeMap<Double> mapSwapCalc = irs.value (
			new ValuationParams (dt, dt, "MXN"),
			null,
			ComponentMarketParamsBuilder.CreateComponentMarketParams (dc, null, null, null, null, null, mmFixings),
			null);

		return mapSwapCalc.get (strMeasure);
	}

	public static final double calcReturn (
		final CalibratableComponent irs,
		final JulianDate dt1,
		final JulianDate dt2,
		final DiscountCurve dc1,
		final DiscountCurve dc2)
		throws Exception
	{
		return calcMeasure (irs, dt2, dc2, "DirtyPV") - calcMeasure (irs, dt1, dc1, "DirtyPV");
	}

	public static final double calcCarry (
		final CalibratableComponent irs,
		final JulianDate dt1,
		final JulianDate dt2,
		final DiscountCurve dc)
		throws Exception
	{
		return calcMeasure (irs, dt2, dc, "FixAccrued") + calcMeasure (irs, dt2, dc, "FloatAccrued") - calcMeasure (irs, dt1, dc, "FixAccrued") - calcMeasure (irs, dt1, dc, "FloatAccrued");
	}

	public static final double calcRollDown (
		final CalibratableComponent irs,
		final JulianDate dt1,
		final JulianDate dt2,
		final DiscountCurve dc)
		throws Exception
	{
		return calcMeasure (irs, dt1, dc, "FairPremium") - calcMeasure (irs, dt2, dc, "FairPremium");
	}

	public static final double calcCurveShift (
		final CalibratableComponent irs,
		final JulianDate dt,
		final DiscountCurve dc1,
		final DiscountCurve dc2)
		throws Exception
	{
		return calcMeasure (irs, dt, dc1, "FairPremium") - calcMeasure (irs, dt, dc2, "FairPremium");
	}

	public static final double Forward (
		final DiscountCurve dc,
		final JulianDate dt1,
		final JulianDate dt2)
		throws Exception
	{
		if (dt1.getJulian() >= dt2.getJulian()) return -0.;

		return dc.forward (dt1.getJulian(), dt2.getJulian());
	}

	public static final PnLMetric ComputePnLMetrics (
		final JulianDate dt0D,
		final JulianDate dt1D,
		final CalibratableComponent irs,
		final DiscountCurve dc0D,
		final DiscountCurve dc1D)
		throws Exception
	{
		JulianDate dt1M = dt0D.addTenor ("1M");

		JulianDate dt3M = dt0D.addTenor ("3M");

		double dblDV01 = calcMeasure (irs, dt0D, dc0D, "FixedDV01");

		double dbl1DReturn = calcReturn (irs, dt0D, dt1D, dc0D, dc1D);

		double dbl1DCarry = calcCarry (irs, dt0D, dt1D, dc0D);

		double dbl1DRollDown = calcRollDown (irs, dt0D, dt1D, dc0D) * dblDV01;

		double dbl1DCurveShift = calcCurveShift (irs, dt0D, dc0D, dc1D) * dblDV01;

		double dbl1MCarry = calcCarry (irs, dt0D, dt1M, dc0D);

		double dbl1MRollDown = calcRollDown (irs, dt0D, dt1M, dc0D) * dblDV01;

		double dbl3MCarry = calcCarry (irs, dt0D, dt3M, dc0D);

		double dbl3MRollDown = calcRollDown (irs, dt0D, dt3M, dc0D) * dblDV01;

		if (s_bBlog) {
			StringBuffer sb = new StringBuffer();

			sb.append ("\t1D Return       : " + FormatUtil.FormatDouble (dbl1DReturn, 1, 8, 1.) + "\n\r");

			sb.append ("\t1D Coupon Carry : " + FormatUtil.FormatDouble (dbl1DCarry, 1, 8, 1.) + "\n");

			sb.append ("\t1D Roll Down    : " + FormatUtil.FormatDouble (dbl1DRollDown, 1, 8, 1.) + "\n");

			sb.append ("\t1D Curve Shift  : " + FormatUtil.FormatDouble (dbl1DCurveShift, 1, 8, 1.) + "\n");

			sb.append ("\t\t\t---------\n");

			sb.append ("\t1M Coupon Carry : " + FormatUtil.FormatDouble (dbl1MCarry, 1, 8, 1.) + "\n");

			sb.append ("\t1M Roll Down    : " + FormatUtil.FormatDouble (dbl1MRollDown, 1, 8, 1.) + "\n");

			sb.append ("\t\t\t---------\n");

			sb.append ("\t3M Coupon Carry : " + FormatUtil.FormatDouble (dbl3MCarry, 1, 8, 1.) + "\n");

			sb.append ("\t3M Roll Down    : " + FormatUtil.FormatDouble (dbl3MRollDown, 1, 8, 1.) + "\n");

			sb.append ("\t\t\t---------\n");

			sb.append ("\tDV01            : " + FormatUtil.FormatDouble (dblDV01, 1, 8, 1.) + "\n");

			System.out.println (sb.toString());

			if (null != _writeLog) _writeLog.write (sb.toString());
		}

		return new PnLMetric (dbl1DReturn, dbl1DCarry, dbl1DRollDown, dbl1DCurveShift,
			dbl1MCarry, dbl1MRollDown, dbl3MCarry, dbl3MRollDown, dblDV01);
	}

	public static final FwdMetric ComputeForwardMetric (
		final CalibratableComponent[] aCalibInstr,
		final DiscountCurve dc)
		throws Exception
	{
		String[] astrTenor = new String[] {"01M", "03M", "06M", "09M", "01Y", "02Y", "03Y", "04Y", "05Y", "07Y", "10Y", "15Y", "20Y", "30Y"};

		StringBuffer sb = new StringBuffer();

		if (s_bBlog) {
			sb.append ("\n\n\tFORWARD RATE GRID\n\t\t\t");

			for (int i = 0; i < aCalibInstr.length; ++ i) {
				if (0 != i) sb.append ("  |   ");

				sb.append (astrTenor[i]);
			}

			sb.append ("\t\t\t--------------------------------------------------------------------------------------------------------------------------------------\n");
		}

		FwdMetric fmOP = new FwdMetric();

		for (int i = 0; i < aCalibInstr.length; ++i) {
			if (s_bBlog) sb.append ("\t\t" + astrTenor[i] + " => ");

			for (int j = 0; j < aCalibInstr.length; ++j) {
				if (s_bBlog && 0 != j) sb.append (" | ");

				double dblForward = Forward (dc, aCalibInstr[j].getMaturityDate(), aCalibInstr[i].getMaturityDate());

				if (0 != dblForward) fmOP.addForward (dblForward);

				if (s_bBlog) sb.append (FormatUtil.FormatDouble (dblForward, 2, 2, 100.));
			}

			if (s_bBlog) sb.append ("\n");
		}

		if (s_bBlog) {
			System.out.println (sb.toString());

			if (null != _writeLog) _writeLog.write (sb.toString());
		}

		return fmOP;
	}

	public static final void GenerateMetrics (
		final JulianDate dt0D,
		final JulianDate dt1D,
		final double[] adblCashQuote0D,
		final double[] adblCashQuote1D,
		final double[] adblIRSQuote0D,
		final double[] adblIRSQuote1D)
		throws Exception
	{
		String[] astrCashTenor = new String[] {"1M"};
		String[] astrIRSTenor = new String[] {"3M", "6M", "9M", "1Y", "2Y", "3Y", "4Y", "5Y", "7Y", "10Y", "15Y", "20Y", "30Y"};

		CalibratableComponent[] aCalibInstr = CalibInstr (dt0D, astrCashTenor, adblCashQuote0D, astrIRSTenor, adblIRSQuote0D);

		DiscountCurve dc0D = BuildCurve (dt0D, astrCashTenor, adblCashQuote0D, astrIRSTenor, adblIRSQuote0D);

		DiscountCurve dc1D = BuildCurve (dt1D, astrCashTenor, adblCashQuote1D, astrIRSTenor, adblIRSQuote1D);

		_writeLog = new java.io.BufferedWriter (new java.io.FileWriter ("C:\\DRIP\\CreditAnalytics\\Metric.PnL"));

		SPCAMetric spcaOP = new SPCAMetric (dt0D, "MXN");

		for (CalibratableComponent comp : aCalibInstr) {
			if (!(comp instanceof org.drip.product.rates.IRSComponent)) continue;

			if (s_bBlog) System.out.println ("\n\t----\n\tComputing PnL Metrics for " + comp.getComponentName() + "\n\t----");

			spcaOP.addPnLMetric (ComputePnLMetrics (dt0D, dt1D, comp, dc0D, dc1D));
		}

		spcaOP.addFwdMetric (ComputeForwardMetric (aCalibInstr, dc0D));

		_writeLog.flush();
	}

	public static final double[] ParseSwapQuotes (
		final String[] astrSwapQuote)
	{
		double[] adblSwapQuote = new double[13];

		for (int i = 2; i < 15; ++i)
			adblSwapQuote[i - 2] = 0.01 * Double.parseDouble (astrSwapQuote[i]);

		return adblSwapQuote;
	}

	public static final void main (
		final String[] astrArgs)
		throws Exception
	{
		CreditAnalytics.Init ("");

		JulianDate dt0D = null;
		boolean bFirstEntry = true;
		double[] adblCashQuote0D = null;
		double[] adblSwapQuote0D = null;
		java.lang.String strSwapCOBLine = "";

		java.io.BufferedReader inSwapCOB = new java.io.BufferedReader (new java.io.FileReader
			("C:\\Lakshmi\\DRIP_General\\ExternalDocs\\DiscountCurveConstruction\\Data\\MXN_Swap_Curve_1.txt"));

		while (null != (strSwapCOBLine = inSwapCOB.readLine())) {
			java.lang.String[] astrSwapCOBRecord = strSwapCOBLine.split (",");

			if (null == astrSwapCOBRecord || 15 != astrSwapCOBRecord.length) return;

			System.out.println ("Date : " + JulianDate.CreateFromMDY (astrSwapCOBRecord[0], "/"));

			System.out.println ("Cash Quote : " + astrSwapCOBRecord[1]);

			for (int i = 2; i < 15; ++i)
				System.out.println ("Swap Quote : " + astrSwapCOBRecord[i]);

			if (bFirstEntry) {
				adblCashQuote0D = new double[] {0.01 * Double.parseDouble (astrSwapCOBRecord[1])};

				dt0D = JulianDate.CreateFromMDY (astrSwapCOBRecord[0], "/");

				adblSwapQuote0D = ParseSwapQuotes (astrSwapCOBRecord);

				bFirstEntry = false;
				continue;
			}

			JulianDate dt1D = JulianDate.CreateFromMDY (astrSwapCOBRecord[0], "/");

			double[] adblCashQuote1D = new double[] {Double.parseDouble (astrSwapCOBRecord[1])};

			double[] adblSwapQuote1D = ParseSwapQuotes (astrSwapCOBRecord);

			GenerateMetrics (dt0D, dt1D, adblCashQuote0D, adblCashQuote1D, adblSwapQuote0D, adblSwapQuote1D);

			dt0D = dt1D;

			adblCashQuote0D = adblCashQuote1D;

			adblSwapQuote0D = adblSwapQuote1D;
		}

	}
}
