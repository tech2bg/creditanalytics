
package org.drip.param.creator;

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
 * RatesScenarioCurveBuilder implements the the construction of the scenario discount curve using the input
 * 	discount curve instruments.
 * 
 * @author Lakshmi Krishnamurthy
 */

public class RatesScenarioCurveBuilder {
	private static final boolean s_bBlog = false;

	/**
	 * Creates an RatesScenarioCurve Instance from the currency and the array of the calibration
	 * 	instruments
	 * 
	 * @param strCurrency Currency
	 * @param strBootstrapMode Bootstrap Mode - one of the choices in DiscountCurveBuilder.BOOTSTRAP_MODE_xxx
	 * @param aCalibInst Array of the calibration instruments
	 * 
	 * @return The RatesScenarioCurve instance
	 */

	public static final org.drip.param.definition.RatesScenarioCurve FromIRCSG (
		final java.lang.String strCurrency,
		final java.lang.String strBootstrapMode,
		final org.drip.product.definition.CalibratableComponent[] aCalibInst)
	{
		if (null == strCurrency || strCurrency.isEmpty() || null == aCalibInst || 0 == aCalibInst.length) {
			System.out.println ("Invalid ccy/calib comp in RatesScenarioCurveBuilder.FromIRCSG");

			return null;
		}

		try {
			return new org.drip.param.market.RatesCurveScenarioContainer (new
				org.drip.state.estimator.RatesCurveScenarioGenerator (strCurrency, strBootstrapMode,
					aCalibInst));
		} catch (java.lang.Exception e) {
			e.printStackTrace();
		}

		return null;
	}

	/**
	 * Creates Discount Curve from the Rates Calibration Instruments
	 * 
	 * @param dt Valuation Date
	 * @param strCurrency Currency
	 * @param strBootstrapMode Bootstrap Mode - one of the choices in DiscountCurveBuilder.BOOTSTRAP_MODE_xxx
	 * @param aCalibInst Input Rates Calibration Instruments
	 * @param adblQuotes Input Calibration Quotes
	 * @param astrCalibMeasure Input Calibration Measures
	 * @param mmFixings (Optional) Input Fixings
	 * 
	 * @return The Calibrated Discount Curve
	 */

	public static final org.drip.analytics.definition.DiscountCurve NonlinearBuild (
		final org.drip.analytics.date.JulianDate dt,
		final java.lang.String strCurrency,
		final java.lang.String strBootstrapMode,
		final org.drip.product.definition.CalibratableComponent[] aCalibInst,
		final double[] adblQuotes,
		final java.lang.String[] astrCalibMeasure,
		final java.util.Map<org.drip.analytics.date.JulianDate,
			org.drip.analytics.support.CaseInsensitiveTreeMap<java.lang.Double>> mmFixings)
	{
		org.drip.param.definition.RatesScenarioCurve irsg = FromIRCSG (strCurrency, strBootstrapMode,
			aCalibInst);

		if (null == irsg || !irsg.cookScenarioDC (org.drip.param.valuation.ValuationParams.CreateValParams
			(dt, 0, "", org.drip.analytics.daycount.Convention.DR_ACTUAL), null, null, adblQuotes, 0.,
				astrCalibMeasure, mmFixings, null, org.drip.param.definition.RatesScenarioCurve.DC_BASE))
			return null;

		return irsg.getDCBase();
	}

	public static final org.drip.analytics.definition.DiscountCurve ShapePreservingBuild (
		final org.drip.state.estimator.LinearCurveCalibrator lcc,
		final org.drip.state.estimator.RegimeRepresentationSpec[] aRBS,
		final org.drip.param.valuation.ValuationParams valParam,
		final org.drip.param.pricer.PricerParams pricerParam,
		final org.drip.param.definition.ComponentMarketParams cmp,
		final org.drip.param.valuation.QuotingParams quotingParam)
	{
		if (null == lcc) return null;

		try {
			org.drip.state.curve.DiscountFactorDiscountCurve dcdf = new
				org.drip.state.curve.DiscountFactorDiscountCurve (aRBS[0].getCalibComp()[0].getIRCurveName(),
					(lcc.calibrateSpan (aRBS, 1., valParam, null, null, null)));

			return dcdf.setCCIS (new org.drip.analytics.definition.ShapePreservingCCIS (lcc, aRBS, valParam,
				pricerParam, quotingParam, cmp)) ? dcdf : null;
		} catch (java.lang.Exception e) {
			e.printStackTrace();
		}

		return null;
	}

	public static final org.drip.analytics.definition.DiscountCurve SmoothingGlobalControlBuild (
		final org.drip.analytics.definition.DiscountCurve dcShapePreserver,
		final org.drip.state.estimator.LinearCurveCalibrator lcc,
		final org.drip.state.estimator.GlobalControlCurveParams gccp,
		final org.drip.state.estimator.RegimeRepresentationSpec[] aRBS,
		final org.drip.param.valuation.ValuationParams valParam,
		final org.drip.param.pricer.PricerParams pricerParam,
		final org.drip.param.definition.ComponentMarketParams cmp,
		final org.drip.param.valuation.QuotingParams quotingParam)
	{
		if (null == dcShapePreserver) return null;

		if (null == gccp) return dcShapePreserver;

		java.lang.String strSmootheningQM = gccp.smootheningQuantificationMetric();

		java.util.Map<java.lang.Double, java.lang.Double> mapQMTruth = dcShapePreserver.canonicalTruthness
			(strSmootheningQM);

		if (null == mapQMTruth) return null;

		int iTruthSize = mapQMTruth.size();

		if (0 == iTruthSize) return null;

		java.util.Set<java.util.Map.Entry<java.lang.Double, java.lang.Double>> esQMTruth =
			mapQMTruth.entrySet();

		if (null == esQMTruth || 0 == esQMTruth.size()) return null;

		java.lang.String strName = dcShapePreserver.name();

		int i = 0;
		double[] adblQM = new double[iTruthSize];
		double[] adblDate = new double[iTruthSize];
		org.drip.math.segment.PredictorResponseBuilderParams[] aPRBP = new
			org.drip.math.segment.PredictorResponseBuilderParams[iTruthSize - 1];

		for (java.util.Map.Entry<java.lang.Double, java.lang.Double> meQMTruth : esQMTruth) {
			if (null == meQMTruth) return null;

			if (0 != i) aPRBP[i - 1] = gccp.prbp();

			adblDate[i] = meQMTruth.getKey();

			adblQM[i++] = meQMTruth.getValue();

			if (s_bBlog) {
				try {
					System.out.println ("\t\t" + new org.drip.analytics.date.JulianDate (meQMTruth.getKey())
						+ " = " + meQMTruth.getValue());
				} catch (java.lang.Exception e) {
					e.printStackTrace();
				}
			}
		}

		try {
			org.drip.math.regime.MultiSegmentRegime regime =
				org.drip.math.regime.RegimeBuilder.CreateCalibratedRegimeEstimator (strName + "_REGIME",
					adblDate, adblQM, aPRBP, gccp.bestFitWeightedResponse(),
						gccp.calibrationBoundaryCondition(), gccp.calibrationDetail());

			org.drip.analytics.definition.DiscountCurve dcMultiPass = null;

			if (org.drip.analytics.definition.DiscountCurve.QUANTIFICATION_METRIC_DISCOUNT_FACTOR.equalsIgnoreCase
				(strSmootheningQM))
				dcMultiPass = new org.drip.state.curve.DiscountFactorDiscountCurve (strName, new
					org.drip.math.grid.OverlappingRegimeSpan (regime));
			else if (org.drip.analytics.definition.DiscountCurve.QUANTIFICATION_METRIC_ZERO_RATE.equalsIgnoreCase
				(strSmootheningQM))
				dcMultiPass = new org.drip.state.curve.ZeroRateDiscountCurve (strName, new
					org.drip.math.grid.OverlappingRegimeSpan (regime));

			return dcMultiPass.setCCIS (new org.drip.analytics.definition.SmoothingCCIS (dcShapePreserver,
				gccp, lcc, aRBS, valParam, pricerParam, quotingParam, cmp)) ? dcMultiPass : null;
		} catch (java.lang.Exception e) {
			e.printStackTrace();
		}

		return null;
	}

	public static final org.drip.analytics.definition.DiscountCurve SmoothingLocalControlBuild (
		final org.drip.analytics.definition.DiscountCurve dcShapePreserver,
		final org.drip.state.estimator.LinearCurveCalibrator lcc,
		final org.drip.state.estimator.LocalControlCurveParams lccp,
		final org.drip.state.estimator.RegimeRepresentationSpec[] aRBS,
		final org.drip.param.valuation.ValuationParams valParam,
		final org.drip.param.pricer.PricerParams pricerParam,
		final org.drip.param.definition.ComponentMarketParams cmp,
		final org.drip.param.valuation.QuotingParams quotingParam)
	{
		if (null == dcShapePreserver) return null;

		if (null == lccp) return dcShapePreserver;

		java.lang.String strSmootheningQM = lccp.smootheningQuantificationMetric();

		java.util.Map<java.lang.Double, java.lang.Double> mapQMTruth = dcShapePreserver.canonicalTruthness
			(strSmootheningQM);

		if (null == mapQMTruth) return null;

		int iTruthSize = mapQMTruth.size();

		if (0 == iTruthSize) return null;

		java.util.Set<java.util.Map.Entry<java.lang.Double, java.lang.Double>> esQMTruth =
			mapQMTruth.entrySet();

		if (null == esQMTruth || 0 == esQMTruth.size()) return null;

		java.lang.String strName = dcShapePreserver.name();

		int i = 0;
		double[] adblQM = new double[iTruthSize];
		double[] adblDate = new double[iTruthSize];
		org.drip.math.segment.PredictorResponseBuilderParams[] aPRBP = new
			org.drip.math.segment.PredictorResponseBuilderParams[iTruthSize - 1];

		for (java.util.Map.Entry<java.lang.Double, java.lang.Double> meQMTruth : esQMTruth) {
			if (null == meQMTruth) return null;

			if (0 != i) aPRBP[i - 1] = lccp.prbp();

			adblDate[i] = meQMTruth.getKey();

			adblQM[i++] = meQMTruth.getValue();

			if (s_bBlog) {
				try {
					System.out.println ("\t\t" + new org.drip.analytics.date.JulianDate (meQMTruth.getKey())
						+ " = " + meQMTruth.getValue());
				} catch (java.lang.Exception e) {
					e.printStackTrace();
				}
			}
		}

		try {
			org.drip.math.pchip.LocalControlRegime lcr = org.drip.math.pchip.LocalControlRegime.Create
				(adblDate, adblQM, lccp.C1GeneratorScheme(), lccp.eliminateSpuriousExtrema(),
					lccp.applyMonotoneFilter());

			if (null == lcr) return null;

			org.drip.math.regime.MultiSegmentRegime regime =
				org.drip.math.pchip.LocalControlRegimeBuilder.CustomSlopeHermiteSpline (strName + "_REGIME",
					adblDate, adblQM, lcr.C1(), aPRBP, lccp.bestFitWeightedResponse(),
						lccp.calibrationDetail());

			org.drip.analytics.definition.DiscountCurve dcMultiPass = null;

			if (org.drip.analytics.definition.DiscountCurve.QUANTIFICATION_METRIC_DISCOUNT_FACTOR.equalsIgnoreCase
				(strSmootheningQM))
				dcMultiPass = new org.drip.state.curve.DiscountFactorDiscountCurve (strName, new
					org.drip.math.grid.OverlappingRegimeSpan (regime));
			else if (org.drip.analytics.definition.DiscountCurve.QUANTIFICATION_METRIC_ZERO_RATE.equalsIgnoreCase
				(strSmootheningQM))
				dcMultiPass = new org.drip.state.curve.ZeroRateDiscountCurve (strName, new
					org.drip.math.grid.OverlappingRegimeSpan (regime));

			return dcMultiPass.setCCIS (new org.drip.analytics.definition.SmoothingCCIS (dcShapePreserver,
				lccp, lcc, aRBS, valParam, pricerParam, quotingParam, cmp)) ? dcMultiPass : null;
		} catch (java.lang.Exception e) {
			e.printStackTrace();
		}

		return null;
	}
}
