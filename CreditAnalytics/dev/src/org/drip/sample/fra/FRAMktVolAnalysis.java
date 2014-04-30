
package org.drip.sample.fra;

import java.util.Map;

import org.drip.analytics.date.JulianDate;
import org.drip.analytics.rates.*;
import org.drip.param.creator.ComponentMarketParamsBuilder;
import org.drip.param.definition.ComponentMarketParams;
import org.drip.param.valuation.ValuationParams;
import org.drip.product.fra.FRAMarketComponent;
import org.drip.product.params.FloatingRateIndex;
import org.drip.quant.common.FormatUtil;
import org.drip.quant.function1D.*;
import org.drip.sample.forward.*;
import org.drip.service.api.CreditAnalytics;
import org.drip.spline.basis.ExponentialTensionSetParams;
import org.drip.spline.params.SegmentCustomBuilderControl;
import org.drip.spline.params.SegmentInelasticDesignControl;
import org.drip.spline.stretch.BoundarySettings;
import org.drip.spline.stretch.MultiSegmentSequence;
import org.drip.spline.stretch.MultiSegmentSequenceBuilder;

/*
 * -*- mode: java; tab-width: 4; indent-tabs-mode: nil; c-basic-offset: 4 -*-
 */

/*!
 * Copyright (C) 2014 Lakshmi Krishnamurthy
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
 * FRAMktVolAnalysis contains an analysis of the correlation and volatility impact on the Market FRA.
 * 
 * @author Lakshmi Krishnamurthy
 */

public class FRAMktVolAnalysis {
	static class FRAMktConvexityCorrection {
		double _dblParMktFwd = Double.NaN;
		double _dblParStdFwd = Double.NaN;
		double _dblConvexityCorrection = Double.NaN;

		FRAMktConvexityCorrection (
			final double dblParMktFwd,
			final double dblParStdFwd,
			final double dblConvexityCorrection)
		{
			_dblParMktFwd = dblParMktFwd;
			_dblParStdFwd = dblParStdFwd;
			_dblConvexityCorrection = dblConvexityCorrection;
		}
	}

	private static final AbstractUnivariate ATMLogNormalVolTermStructure (
		final JulianDate dtEpoch,
		final String[] astrTenor,
		final double[] adblATMLogNormalVolTSIn)
		throws Exception
	{
		double[] adblTime = new double[astrTenor.length + 1];
		double[] adblATMLogNormalVolTS = new double[adblATMLogNormalVolTSIn.length + 1];
		SegmentCustomBuilderControl[] aSCBC = new SegmentCustomBuilderControl[astrTenor.length];

		SegmentCustomBuilderControl scbc = new SegmentCustomBuilderControl (
			MultiSegmentSequenceBuilder.BASIS_SPLINE_HYPERBOLIC_TENSION,
			new ExponentialTensionSetParams (1.),
			SegmentInelasticDesignControl.Create (2, 2),
			null,
			null);

		for (int i = 0; i < adblTime.length; ++i) {
			if (0 != i) aSCBC[i - 1] = scbc;

			adblTime[i] = 0 == i ? dtEpoch.getJulian(): dtEpoch.addTenor (astrTenor[i - 1]).getJulian();

			adblATMLogNormalVolTS[i] = 0 == i ? adblATMLogNormalVolTSIn[0] : adblATMLogNormalVolTSIn[i - 1];
		}

		return MultiSegmentSequenceBuilder.CreateCalibratedStretchEstimator (
			"LOG_NORMAL_ATM_VOL_STRETCH",
			adblTime,
			adblATMLogNormalVolTS,
			aSCBC,
			null,
			BoundarySettings.NaturalStandard(),
			MultiSegmentSequence.CALIBRATE).toAU();
	}

	public static final FRAMktConvexityCorrection FRAMktMetric (
		final JulianDate dtValue,
		final DiscountCurve dcEONIA,
		final ForwardCurve fcEURIBOR6M,
		final String strForwardStartTenor,
		final AbstractUnivariate auEONIAVolTS,
		final AbstractUnivariate auEURIBOR6MVolTS,
		final double dblEONIAEURIBOR6MCorrelation)
		throws Exception
	{
		String strTenor = "6M";
		String strCurrency = "EUR";
		double dblMultiplicativeQuantoExchangeVol = 0.1;
		double dblFRIQuantoExchangeCorr = 0.2;

		FloatingRateIndex fri = FloatingRateIndex.Create (strCurrency + "-LIBOR-" + strTenor);

		JulianDate dtForward = dtValue.addTenor (strForwardStartTenor);

		FRAMarketComponent fra = new FRAMarketComponent (
			1.,
			strCurrency,
			fri.fullyQualifiedName(),
			strCurrency,
			dtForward.getJulian(),
			fri,
			0.006,
			"Act/360");

		ComponentMarketParams cmp = ComponentMarketParamsBuilder.CreateComponentMarketParams
			(dcEONIA, fcEURIBOR6M, null, null, null, null, null, null);

		ValuationParams valParams = new ValuationParams (dtValue, dtValue, strCurrency);

		cmp.setLatentStateVolSurface (
			fri.fullyQualifiedName(),
			dtForward,
			auEURIBOR6MVolTS
		);

		cmp.setLatentStateVolSurface (
			"ForwardToDomesticExchangeVolatility",
			dtForward,
			new FlatUnivariate (dblMultiplicativeQuantoExchangeVol)
		);

		cmp.setLatentStateVolSurface (
			"FRIForwardToDomesticExchangeCorrelation",
			dtForward,
			new FlatUnivariate (dblFRIQuantoExchangeCorr)
		);

		cmp.setLatentStateVolSurface (
			dcEONIA.name() + "_VOL_TS",
			dtForward,
			auEONIAVolTS
		);

		cmp.setLatentStateVolSurface (
			fri.fullyQualifiedName() + "_VOL_TS",
			dtForward,
			auEURIBOR6MVolTS
		);

		cmp.setLatentStateVolSurface (
			dcEONIA.name() + "::" + fri.fullyQualifiedName() + "_VOL_TS",
			dtForward,
			new FlatUnivariate (dblEONIAEURIBOR6MCorrelation)
		);

		Map<String, Double> mapFRAOutput = fra.value (valParams, null, cmp, null);

		return new FRAMktConvexityCorrection (
			mapFRAOutput.get ("shiftedlognormalparmarketfra"),
			mapFRAOutput.get ("parstandardfra"),
			mapFRAOutput.get ("shiftedlognormalconvexitycorrection"));
	}

	public static final void main (
		final String[] astrArgs)
		throws Exception
	{
		/*
		 * Initialize the Credit Analytics Library
		 */

		CreditAnalytics.Init ("");

		String strTenor = "6M";
		String strCurrency = "EUR";

		JulianDate dtToday = JulianDate.Today().addTenorAndAdjust ("0D", strCurrency);

		DiscountCurve dcEONIA = EONIA.MakeDC (
			dtToday,
			strCurrency,
			false);

		ForwardCurve fcEURIBOR6M = EURIBOR6MQuarticPolyVanilla.Make6MForward (
			dtToday,
			strCurrency,
			strTenor);

		String[] astrForwardStartTenor = {
			"6M", "1Y", "2Y", "3Y", "4Y", "5Y", "6Y", "7Y", "8Y", "9Y"
		};

		double dblEONIAEURIBOR6MCorrelation = 0.8;

		AbstractUnivariate auATMVolTS = ATMLogNormalVolTermStructure (
			dtToday,
			astrForwardStartTenor,
			new double[] {
				0.5946, // 6M
				0.5311,	// 1Y
				0.3307,	// 2Y
				0.2929,	// 3Y
				0.2433,	// 4Y
				0.2013,	// 5Y
				0.1855,	// 6Y
				0.1789,	// 7Y
				0.1655,	// 8Y
				0.1574	// 9Y
			});

		AbstractUnivariate auEONIAVolTS = auATMVolTS;
		AbstractUnivariate auEURIBOR6MVolTS = auATMVolTS;

		System.out.println ("\t---------------------------------");

		System.out.println ("\t---------------------------------");

		System.out.println ("\t---------------------------------");

		System.out.println ("\t---------------------------------");

		System.out.println ("\tTNR =>   MKT   |   STD   |  CONV ");

		System.out.println ("\t---------------------------------");

		for (String strForwardStartTenor : astrForwardStartTenor) {
			FRAMktConvexityCorrection fraMktMetric = FRAMktMetric (
				dtToday,
				dcEONIA,
				fcEURIBOR6M,
				strForwardStartTenor,
				auEONIAVolTS,
				auEURIBOR6MVolTS,
				dblEONIAEURIBOR6MCorrelation);

			System.out.println (
				"\t " + strForwardStartTenor + " => " +
				FormatUtil.FormatDouble (fraMktMetric._dblParMktFwd, 1, 3, 100.) + "% | " +
				FormatUtil.FormatDouble (fraMktMetric._dblParStdFwd, 1, 3, 100.) + "% | " +
				FormatUtil.FormatDouble (fraMktMetric._dblConvexityCorrection, 1, 2, 10000.));
		}
	}
}
