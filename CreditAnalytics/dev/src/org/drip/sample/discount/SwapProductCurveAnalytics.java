
package org.drip.sample.discount;

import java.util.*;

import org.drip.analytics.date.JulianDate;
import org.drip.analytics.definition.DiscountCurve;
import org.drip.analytics.support.CaseInsensitiveTreeMap;
import org.drip.math.common.FormatUtil;
import org.drip.math.function.QuadraticRationalShapeControl;
import org.drip.math.regime.*;
import org.drip.math.segment.*;
import org.drip.math.spline.PolynomialBasisSetParams;
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
 * SwapProductCurveAnalytics contains a demo of a demo of th curve based analytical measures for a Swap. It
 * 	shows the following:
 * 	- 1-D Return
 * 	- 1-D Carry, 1-M Carry, 3-M Carry
 * 	- 1-D Roll-down, 1-M Roll-down, 3-M Roll-down
 * 	- DV01, PV01, Convexity
 * 
 * @author Lakshmi Krishnamurthy
 */

public class SwapProductCurveAnalytics {
	private static final CalibratableComponent[] CashInstrumentsFromMaturityDays (
		final JulianDate dtEffective,
		final java.lang.String[] astrTenor)
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
		final java.lang.String[] astrCashTenor,
		final double[] adblCashQuote,
		final java.lang.String[] astrSwapTenor,
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

		RegimeRepresentationSpec[] aRBS = new RegimeRepresentationSpec[] {rbsCash, rbsSwap};

		LinearCurveCalibrator lccShapePreserving = new LinearCurveCalibrator (
			new PredictorResponseBuilderParams (
				RegimeBuilder.BASIS_SPLINE_POLYNOMIAL,
				new PolynomialBasisSetParams (2),
				DesignInelasticParams.Create (0, 2),
				new ResponseScalingShapeController (true, new QuadraticRationalShapeControl (0.))),
			MultiSegmentRegime.BOUNDARY_CONDITION_NATURAL,
			MultiSegmentRegime.CALIBRATE,
			null);

		return RatesScenarioCurveBuilder.ShapePreservingBuild (
			lccShapePreserving,
			aRBS,
			new ValuationParams (dt, dt, "MXN"),
			null,
			null,
			null);
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
		final DiscountCurve dc2,
		final double dblNotional)
		throws Exception
	{
		double dblPV1 = calcMeasure (irs, dt1, dc1, "PV");

		double dblPV2 = calcMeasure (irs, dt2, dc2, "PV");

		return (dblPV2 - dblPV1) / dblPV1 * 365.25 / (dt2.daysDiff (dt1));
	}

	public static final double calcCarry (
		final CalibratableComponent irs,
		final JulianDate dt1,
		final JulianDate dt2,
		final DiscountCurve dc1,
		final DiscountCurve dc2,
		final double dblNotional)
		throws Exception
	{
		return (calcMeasure (irs, dt2, dc2, "FixAccrued") - calcMeasure (irs, dt1, dc1, "FixAccrued")) * dblNotional;
	}

	public static final double calcRollDown (
		final CalibratableComponent irs,
		final JulianDate dt1,
		final JulianDate dt2,
		final DiscountCurve dc,
		final double dblNotional)
		throws Exception
	{
		double dblPV1 = calcMeasure (irs, dt1, dc, "PV");

		double dblPV2 = calcMeasure (irs, dt2, dc, "PV");

		return (dblPV2 - dblPV1) / dblPV1 * 365.25 / (dt2.daysDiff (dt1));
	}

	public static final void main (
		final String[] astrArgs)
		throws Exception
	{
		CreditAnalytics.Init ("");

		JulianDate dt0D = JulianDate.Today().addTenorAndAdjust ("0D", "MXN");

		JulianDate dt1D = dt0D.addBusDays (1, "MXN");

		JulianDate dt1M = dt0D.addTenor ("1M");

		JulianDate dt3M = dt0D.addTenor ("3M");

		DiscountCurve dc0D = BuildCurve (
			dt0D,
			new java.lang.String[] {"1M"},
			new double[] {0.0403},
			new java.lang.String[] {"3M", "6M", "9M", "1Y", "2Y", "3Y", "4Y", "5Y", "7Y", "10Y", "15Y", "20Y", "30Y"},
			new double[] {0.0396, 0.0387, 0.0388, 0.0389, 0.04135, 0.04455, 0.0486, 0.0526, 0.0593, 0.0649, 0.0714596, 0.0749596, 0.0776});

		DiscountCurve dc1D = BuildCurve (
			dt1D,
			new java.lang.String[] {"1M"},
			new double[] {0.0403},
			new java.lang.String[] {"3M", "6M", "9M", "1Y", "2Y", "3Y", "4Y", "5Y", "7Y", "10Y", "15Y", "20Y", "30Y"},
			new double[] {0.0396, 0.0387, 0.0388, 0.0389, 0.04135, 0.04455, 0.0486, 0.0526, 0.0593, 0.0649, 0.0714596, 0.0749596, 0.0776});

		DiscountCurve dc1M = BuildCurve (
			dt1M,
			new java.lang.String[] {"1M"},
			new double[] {0.0403},
			new java.lang.String[] {"3M", "6M", "9M", "1Y", "2Y", "3Y", "4Y", "5Y", "7Y", "10Y", "15Y", "20Y", "30Y"},
			new double[] {0.0396, 0.0387, 0.0388, 0.0389, 0.04135, 0.04455, 0.0486, 0.0526, 0.0593, 0.0649, 0.0714596, 0.0749596, 0.0776});

		DiscountCurve dc3M = BuildCurve (
			dt3M,
			new java.lang.String[] {"1M"},
			new double[] {0.0403},
			new java.lang.String[] {"3M", "6M", "9M", "1Y", "2Y", "3Y", "4Y", "5Y", "7Y", "10Y", "15Y", "20Y", "30Y"},
			new double[] {0.0396, 0.0387, 0.0388, 0.0389, 0.04135, 0.04455, 0.0486, 0.0526, 0.0593, 0.0649, 0.0714596, 0.0749596, 0.0776});

		double dblNotional = 10.e+06;

		CalibratableComponent irs = RatesStreamBuilder.CreateIRS (
			dt0D,
			dt0D.addTenorAndAdjust ("20Y", "MXN"),
			0.05,
			"MXN",
			"MXN-LIBOR-6M",
			"MXN");

		System.out.println ("\tAnnualized 1D Return : " + FormatUtil.FormatDouble (calcReturn (irs, dt0D, dt1D, dc0D, dc1D, dblNotional), 1, 2, 100.) + "%");

		System.out.println ("\t1D Carry             : " + FormatUtil.FormatDouble (calcCarry (irs, dt0D, dt1D, dc0D, dc1D, dblNotional), 1, 2, 1.));

		System.out.println ("\t1M Carry             : " + FormatUtil.FormatDouble (calcCarry (irs, dt0D, dt1M, dc0D, dc1M, dblNotional), 1, 2, 1.));

		System.out.println ("\t3M Carry             : " + FormatUtil.FormatDouble (calcCarry (irs, dt0D, dt3M, dc0D, dc3M, dblNotional), 1, 2, 1.));

		System.out.println ("\t1D Roll Down         : " + FormatUtil.FormatDouble (calcRollDown (irs, dt0D, dt1D, dc0D, dblNotional), 1, 2, 1.));

		System.out.println ("\t1M Roll Down         : " + FormatUtil.FormatDouble (calcRollDown (irs, dt0D, dt1M, dc0D, dblNotional), 1, 2, 1.));

		System.out.println ("\t3M Roll Down         : " + FormatUtil.FormatDouble (calcRollDown (irs, dt0D, dt3M, dc0D, dblNotional), 1, 2, 1.));

		System.out.println ("\tDV01                 : " + FormatUtil.FormatDouble (calcMeasure (irs, dt0D, dc0D, "FixedDV01") * dblNotional, 1, 2, 1.));

		System.out.println ("\tFair Premium         : " + FormatUtil.FormatDouble (calcMeasure (irs, dt0D, dc0D, "FairPremium"), 1, 5, 100.));
	}
}
