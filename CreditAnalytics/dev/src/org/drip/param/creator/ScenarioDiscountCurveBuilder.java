
package org.drip.param.creator;

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
 * ScenarioDiscountCurveBuilder implements the the construction of the scenario discount curve using the input
 * 	discount curve instruments, and a wide variety of custom builds. It implements the following
 * 	functionality:
 * 	- Non-linear Custom Discount Curve
 * 	- Shape Preserving Discount Curve Builds - Standard Cubic Polynomial/Cubic KLK Hyperbolic Tension, and
 * 	 	other Custom Builds
 * 	- Smoothing Local/Control Custom Build - DC/Forward/Zero Rate LSQM's
 * 	- "Industry Standard Methodologies" - DENSE/DUALDENSE/CUSTOMDENSE and Hagan-West Forward Interpolator
 * 		Schemes
 * 
 * @author Lakshmi Krishnamurthy
 */

public class ScenarioDiscountCurveBuilder {
	static class CompQuote {
		double _dblQuote = java.lang.Double.NaN;
		org.drip.product.definition.CalibratableFixedIncomeComponent _comp = null;
 
		CompQuote (
			final org.drip.product.definition.CalibratableFixedIncomeComponent comp,
			final double dblQuote)
		{
			_comp = comp;
			_dblQuote = dblQuote;
		}
	}

	private static final boolean s_bBlog = false;

	private static final CompQuote[] CompQuote (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final java.lang.String strCurrency,
		final org.drip.analytics.date.JulianDate dtEffective,
		final org.drip.analytics.date.JulianDate dtInitialMaturity,
		final org.drip.analytics.date.JulianDate dtTerminalMaturity,
		final java.lang.String strTenor,
		final boolean bIsIRS)
	{
		java.util.List<java.lang.Double> lsCalibQuote = new java.util.ArrayList<java.lang.Double>();

		java.util.List<org.drip.product.definition.CalibratableFixedIncomeComponent> lsCompDENSE = new
			java.util.ArrayList<org.drip.product.definition.CalibratableFixedIncomeComponent>();

		org.drip.analytics.date.JulianDate dtMaturity = dtInitialMaturity;

		while (dtMaturity.julian() <= dtTerminalMaturity.julian()) {
			try {
				org.drip.product.definition.CalibratableFixedIncomeComponent comp = null;

				if (bIsIRS) {
					org.drip.product.rates.GenericStream fixStream = new org.drip.product.rates.GenericStream
						(org.drip.analytics.support.PeriodBuilder.BackwardPeriodSingleReset
							(dtEffective.julian(), dtMaturity.julian(), java.lang.Double.NaN, null, null,
								null, null, null, null, null, null, 2, "Act/360", false, "Act/360", false,
									true, strCurrency, -1., null, 0., strCurrency, strCurrency, null, null));

					org.drip.product.rates.GenericStream floatStream = new org.drip.product.rates.GenericStream
						(org.drip.analytics.support.PeriodBuilder.BackwardPeriodSingleReset
							(dtEffective.julian(), dtMaturity.julian(), java.lang.Double.NaN, null, null,
								null, null, null, null, null, null, 4, "Act/360", false, "Act/360", false,
									true, strCurrency, -1., null, 0., strCurrency, strCurrency,
										org.drip.state.identifier.ForwardLabel.Create (strCurrency, "LIBOR",
											"3M"), null));

					comp = new org.drip.product.rates.GenericFixFloatComponent (fixStream, floatStream, null);
				} else {
					comp = null;

					/* comp = new org.drip.product.rates.GenericDepositComponent (dtEffective, dtMaturity, null,
						strCurrency, "Act/360", strCurrency); */
				}

				lsCompDENSE.add (comp);

				lsCalibQuote.add (comp.measureValue (valParams, null, csqs, null, "Rate"));
			} catch (java.lang.Exception e) {
				e.printStackTrace();

				return null;
			}

			if (null == (dtMaturity = dtMaturity.addTenorAndAdjust (strTenor, strCurrency))) return null;
		}

		int iNumDENSEComp = lsCompDENSE.size();

		if (0 == iNumDENSEComp) return null;

		CompQuote[] aCQ = new CompQuote[iNumDENSEComp];

		for (int i = 0; i < iNumDENSEComp; ++i)
			aCQ[i] = new CompQuote (lsCompDENSE.get (i), lsCalibQuote.get (i));

		return aCQ;
	}

	/**
	 * Create an ScenarioDiscountCurveBuilder Instance from the currency and the array of the calibration
	 * 	instruments
	 * 
	 * @param strCurrency Currency
	 * @param strBootstrapMode Bootstrap Mode - one of the choices in DiscountCurveBuilder.BOOTSTRAP_MODE_xxx
	 * @param aCalibInst Array of the calibration instruments
	 * 
	 * @return The RatesScenarioCurve instance
	 */

	public static final org.drip.param.definition.ScenarioDiscountCurve FromIRCSG (
		final java.lang.String strCurrency,
		final java.lang.String strBootstrapMode,
		final org.drip.product.definition.CalibratableFixedIncomeComponent[] aCalibInst)
	{
		if (null == strCurrency || strCurrency.isEmpty() || null == aCalibInst || 0 == aCalibInst.length)
			return null;

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
	 * Create Discount Curve from the Rates Calibration Instruments
	 * 
	 * @param dt Valuation Date
	 * @param strCurrency Currency
	 * @param strBootstrapMode Bootstrap Mode - one of the choices in DiscountCurveBuilder.BOOTSTRAP_MODE_xxx
	 * @param aCalibInst Input Rates Calibration Instruments
	 * @param adblQuotes Input Calibration Quotes
	 * @param astrCalibMeasure Input Calibration Measures
	 * @param lsfc Latent State Fixings Container
	 * 
	 * @return The Calibrated Discount Curve
	 */

	public static final org.drip.analytics.rates.DiscountCurve NonlinearBuild (
		final org.drip.analytics.date.JulianDate dt,
		final java.lang.String strCurrency,
		final java.lang.String strBootstrapMode,
		final org.drip.product.definition.CalibratableFixedIncomeComponent[] aCalibInst,
		final double[] adblQuotes,
		final java.lang.String[] astrCalibMeasure,
		final org.drip.param.market.LatentStateFixingsContainer lsfc)
	{
		org.drip.param.definition.ScenarioDiscountCurve irsg = FromIRCSG (strCurrency, strBootstrapMode,
			aCalibInst);

		if (null == irsg || !irsg.cookScenarioDC (org.drip.param.valuation.ValuationParams.CreateValParams
			(dt, 0, "", org.drip.analytics.daycount.Convention.DR_ACTUAL), null, adblQuotes, 0.,
				astrCalibMeasure, lsfc, null, org.drip.param.definition.ScenarioDiscountCurve.DC_BASE))
			return null;

		return irsg.getDCBase();
	}

	/**
	 * Build the Shape Preserving Discount Curve using the Custom Parameters
	 * 
	 * @param llsc The Linear Latent State Calibrator Instance
	 * @param aStretchSpec Array of the Instrument Representation Stretches
	 * @param valParam Valuation Parameters
	 * @param pricerParam Pricer Parameters
	 * @param csqs Market Parameters
	 * @param quotingParam Quoting Parameters
	 * @param dblEpochResponse The Starting Response Value
	 * 
	 * @return Instance of the Shape Preserving Discount Curve
	 */

	public static final org.drip.analytics.rates.DiscountCurve ShapePreservingDFBuild (
		final org.drip.state.inference.LinearLatentStateCalibrator llsc,
		final org.drip.state.inference.LatentStateStretchSpec[] aStretchSpec,
		final org.drip.param.valuation.ValuationParams valParam,
		final org.drip.param.pricer.PricerParams pricerParam,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams quotingParam,
		final double dblEpochResponse)
	{
		if (null == llsc) return null;

		try {
			org.drip.state.curve.DiscountFactorDiscountCurve dcdf = new
				org.drip.state.curve.DiscountFactorDiscountCurve
					(aStretchSpec[0].segmentSpec()[0].component().payCurrency()[0], null == quotingParam ?
						null : quotingParam.coreCollateralizationParams(), (llsc.calibrateSpan (aStretchSpec,
							dblEpochResponse, valParam, pricerParam, quotingParam, csqs)));

			return dcdf.setCCIS (new org.drip.analytics.input.LatentStateShapePreservingCCIS (llsc,
				aStretchSpec, valParam, pricerParam, quotingParam, csqs)) ? dcdf : null;
		} catch (java.lang.Exception e) {
			e.printStackTrace();
		}

		return null;
	}

	/**
	 * Build a Globally Smoothed Instance of the Discount Curve using the Custom Parameters
	 * 
	 * @param dcShapePreserver Instance of the Shape Preserving Discount Curve
	 * @param llsc The Linear Latent State Calibrator Instance
	 * @param gccp Global Smoothing Curve Control Parameters
	 * @param aSRS Array of the Instrument Representation Stretches
	 * @param valParam Valuation Parameters
	 * @param pricerParam Pricer Parameters
	 * @param csqs Market Parameters
	 * @param quotingParam Quoting Parameters
	 * 
	 * @return Globally Smoothed Instance of the Discount Curve
	 */

	public static final org.drip.analytics.rates.DiscountCurve SmoothingGlobalControlBuild (
		final org.drip.analytics.rates.DiscountCurve dcShapePreserver,
		final org.drip.state.inference.LinearLatentStateCalibrator llsc,
		final org.drip.state.estimator.GlobalControlCurveParams gccp,
		final org.drip.param.valuation.ValuationParams valParam,
		final org.drip.param.pricer.PricerParams pricerParam,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams quotingParam)
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

		java.lang.String strName = dcShapePreserver.label().fullyQualifiedName();

		int i = 0;
		double[] adblQM = new double[iTruthSize];
		double[] adblDate = new double[iTruthSize];
		org.drip.spline.params.SegmentCustomBuilderControl[] aPRBP = new
			org.drip.spline.params.SegmentCustomBuilderControl[iTruthSize - 1];

		for (java.util.Map.Entry<java.lang.Double, java.lang.Double> meQMTruth : esQMTruth) {
			if (null == meQMTruth) return null;

			if (0 != i) aPRBP[i - 1] = gccp.defaultSegmentBuilderControl();

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
			org.drip.spline.stretch.MultiSegmentSequence stretch =
				org.drip.spline.stretch.MultiSegmentSequenceBuilder.CreateCalibratedStretchEstimator
					(strName + "_STRETCH", adblDate, adblQM, aPRBP, gccp.bestFitWeightedResponse(),
						gccp.calibrationBoundaryCondition(), gccp.calibrationDetail());

			org.drip.analytics.rates.DiscountCurve dcMultiPass = null;

			if (org.drip.analytics.definition.LatentStateStatic.DISCOUNT_QM_DISCOUNT_FACTOR.equalsIgnoreCase
				(strSmootheningQM))
				dcMultiPass = new org.drip.state.curve.DiscountFactorDiscountCurve (strName, null ==
					quotingParam ? null : quotingParam.coreCollateralizationParams(), new
						org.drip.spline.grid.OverlappingStretchSpan (stretch));
			else if
				(org.drip.analytics.definition.LatentStateStatic.DISCOUNT_QM_ZERO_RATE.equalsIgnoreCase
				(strSmootheningQM))
				dcMultiPass = new org.drip.state.curve.ZeroRateDiscountCurve (strName, null == quotingParam ?
					null : quotingParam.coreCollateralizationParams(), new
						org.drip.spline.grid.OverlappingStretchSpan (stretch));

			return dcMultiPass;
		} catch (java.lang.Exception e) {
			e.printStackTrace();
		}

		return null;
	}

	/**
	 * Build a Locally Smoothed Instance of the Discount Curve using the Custom Parameters
	 * 
	 * @param dcShapePreserver Instance of the Shape Preserving Discount Curve
	 * @param llsc The Linear Latent State Calibrator Instance
	 * @param lccp Local Smoothing Curve Control Parameters
	 * @param valParam Valuation Parameters
	 * @param pricerParam Pricer Parameters
	 * @param csqs Market Parameters
	 * @param quotingParam Quoting Parameters
	 * 
	 * @return Locally Smoothed Instance of the Discount Curve
	 */

	public static final org.drip.analytics.rates.DiscountCurve SmoothingLocalControlBuild (
		final org.drip.analytics.rates.DiscountCurve dcShapePreserver,
		final org.drip.state.inference.LinearLatentStateCalibrator llsc,
		final org.drip.state.estimator.LocalControlCurveParams lccp,
		final org.drip.param.valuation.ValuationParams valParam,
		final org.drip.param.pricer.PricerParams pricerParam,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams quotingParam)
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

		java.lang.String strName = dcShapePreserver.label().fullyQualifiedName();

		int i = 0;
		double[] adblQM = new double[iTruthSize];
		double[] adblDate = new double[iTruthSize];
		org.drip.spline.params.SegmentCustomBuilderControl[] aPRBP = new
			org.drip.spline.params.SegmentCustomBuilderControl[iTruthSize - 1];

		for (java.util.Map.Entry<java.lang.Double, java.lang.Double> meQMTruth : esQMTruth) {
			if (null == meQMTruth) return null;

			if (0 != i) aPRBP[i - 1] = lccp.defaultSegmentBuilderControl();

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
			org.drip.spline.pchip.LocalMonotoneCkGenerator lcr =
				org.drip.spline.pchip.LocalMonotoneCkGenerator.Create (adblDate, adblQM,
					lccp.C1GeneratorScheme(), lccp.eliminateSpuriousExtrema(), lccp.applyMonotoneFilter());

			if (null == lcr) return null;

			org.drip.spline.stretch.MultiSegmentSequence stretch =
				org.drip.spline.pchip.LocalControlStretchBuilder.CustomSlopeHermiteSpline (strName +
					"_STRETCH", adblDate, adblQM, lcr.C1(), aPRBP, lccp.bestFitWeightedResponse(),
						lccp.calibrationDetail());

			org.drip.analytics.rates.DiscountCurve dcMultiPass = null;

			if (org.drip.analytics.definition.LatentStateStatic.DISCOUNT_QM_DISCOUNT_FACTOR.equalsIgnoreCase
				(strSmootheningQM))
				dcMultiPass = new org.drip.state.curve.DiscountFactorDiscountCurve (strName, null ==
					quotingParam ? null : quotingParam.coreCollateralizationParams(), new
						org.drip.spline.grid.OverlappingStretchSpan (stretch));
			else if
				(org.drip.analytics.definition.LatentStateStatic.DISCOUNT_QM_ZERO_RATE.equalsIgnoreCase
				(strSmootheningQM))
				dcMultiPass = new org.drip.state.curve.ZeroRateDiscountCurve (strName, null == quotingParam ?
					null : quotingParam.coreCollateralizationParams(), new
						org.drip.spline.grid.OverlappingStretchSpan (stretch));

			return dcMultiPass;
		} catch (java.lang.Exception e) {
			e.printStackTrace();
		}

		return null;
	}

	/**
	 * Construct an instance of the Shape Preserver of the desired basis type, using the specified basis set
	 * 	builder parameters.
	 * 
	 * @param strName Curve Name
	 * @param valParams Valuation Parameters
	 * @param pricerParam Pricer Parameters
	 * @param csqs Market Parameters
	 * @param quotingParam Quoting Parameters
	 * @param strBasisType The Basis Type
	 * @param fsbp The Function Set Basis Parameters
	 * @param aCalibComp1 Array of Calibration Components #1
	 * @param adblQuote1 Array of Calibration Quotes #1
	 * @param astrManifestMeasure1 Array of Manifest Measures for component Array #1
	 * @param aCalibComp2 Array of Calibration Components #2
	 * @param adblQuote2 Array of Calibration Quotes #2
	 * @param astrManifestMeasure2 Array of Manifest Measures for component Array #2
	 * @param dblEpochResponse The Stretch Start DF
	 * @param bZeroSmooth TRUE => Turn on the Zero Rate Smoothing
	 * 
	 * @return Instance of the Shape Preserver of the desired basis type
	 */

	public static final org.drip.analytics.rates.DiscountCurve DFRateShapePreserver (
		final java.lang.String strName,
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.pricer.PricerParams pricerParam,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams quotingParam,
		final java.lang.String strBasisType,
		final org.drip.spline.basis.FunctionSetBuilderParams fsbp,
		final org.drip.product.definition.CalibratableFixedIncomeComponent[] aCalibComp1,
		final double[] adblQuote1,
		final java.lang.String[] astrManifestMeasure1,
		final org.drip.product.definition.CalibratableFixedIncomeComponent[] aCalibComp2,
		final double[] adblQuote2,
		final java.lang.String[] astrManifestMeasure2,
		final double dblEpochResponse,
		final boolean bZeroSmooth)
	{
		if (null == strName || strName.isEmpty() || null == strBasisType || strBasisType.isEmpty() || null ==
			valParams || null == fsbp)
			return null;

		int iNumQuote1 = null == adblQuote1 ? 0 : adblQuote1.length;
		int iNumQuote2 = null == adblQuote2 ? 0 : adblQuote2.length;
		int iNumComp1 = null == aCalibComp1 ? 0 : aCalibComp1.length;
		int iNumComp2 = null == aCalibComp2 ? 0 : aCalibComp2.length;
		org.drip.state.estimator.LocalControlCurveParams lccp = null;
		org.drip.state.identifier.ForwardLabel[] aForwardLabel = null;
		org.drip.analytics.rates.DiscountCurve dcShapePreserving = null;
		org.drip.state.inference.LinearLatentStateCalibrator llsc = null;
		org.drip.state.inference.LatentStateStretchSpec stretchSpec1 = null;
		org.drip.state.inference.LatentStateStretchSpec stretchSpec2 = null;
		org.drip.state.representation.LatentStateSpecification[] aLSS = null;
		org.drip.state.inference.LatentStateStretchSpec[] aStretchSpec = null;
		org.drip.state.representation.LatentStateSpecification lssFunding = null;
		int iNumManifestMeasures1 = null == astrManifestMeasure1 ? 0 : astrManifestMeasure1.length;
		int iNumManifestMeasures2 = null == astrManifestMeasure2 ? 0 : astrManifestMeasure2.length;

		if ((0 == iNumComp1 && 0 == iNumComp2) || iNumComp1 != iNumQuote1 || iNumComp2 != iNumQuote2 ||
			iNumComp1 != iNumManifestMeasures1 || iNumComp2 != iNumManifestMeasures2)
			return null;

		try {
			lssFunding = new org.drip.state.representation.LatentStateSpecification
				(org.drip.analytics.definition.LatentStateStatic.LATENT_STATE_FUNDING,
					org.drip.analytics.definition.LatentStateStatic.DISCOUNT_QM_DISCOUNT_FACTOR,
						org.drip.state.identifier.FundingLabel.Standard ((0 == iNumComp1 ? aCalibComp2 :
							aCalibComp1)[0].payCurrency()[0]));
		} catch (java.lang.Exception e) {
			e.printStackTrace();

			return null;
		}

		if (0 != iNumComp1) aForwardLabel = aCalibComp1[0].forwardLabel();

		if (null == aForwardLabel && 0 != iNumComp2) aForwardLabel = aCalibComp2[0].forwardLabel();

		if (null == aForwardLabel || 0 == aForwardLabel.length)
			aLSS = new org.drip.state.representation.LatentStateSpecification[] {lssFunding};
		else {
			try {
				aLSS = new org.drip.state.representation.LatentStateSpecification[] {lssFunding, new
					org.drip.state.representation.LatentStateSpecification
						(org.drip.analytics.definition.LatentStateStatic.LATENT_STATE_FORWARD,
							org.drip.analytics.definition.LatentStateStatic.FORWARD_QM_FORWARD_RATE,
								aForwardLabel[0])};
			} catch (java.lang.Exception e) {
				e.printStackTrace();

				return null;
			}
		}

		if (0 != iNumComp1) {
			org.drip.state.inference.LatentStateSegmentSpec[] aSegmentSpec = new
				org.drip.state.inference.LatentStateSegmentSpec[iNumComp1];

			try {
				for (int i = 0; i < iNumComp1; ++i) {
					org.drip.product.calib.ProductQuoteSet pqs = aCalibComp1[i].calibQuoteSet (aLSS);

					if (null == pqs || !pqs.set (astrManifestMeasure1[i], adblQuote1[i])) return null;

					aSegmentSpec[i] = new org.drip.state.inference.LatentStateSegmentSpec (aCalibComp1[i],
						pqs);
				}

				stretchSpec1 = new org.drip.state.inference.LatentStateStretchSpec (strName + "_COMP1",
					aSegmentSpec);
			} catch (java.lang.Exception e) {
				e.printStackTrace();

				return null;
			}
		}

		if (0 != iNumComp2) {
			org.drip.state.inference.LatentStateSegmentSpec[] aSegmentSpec = new
				org.drip.state.inference.LatentStateSegmentSpec[iNumComp2];

			try {
				for (int i = 0; i < iNumComp2; ++i) {
					org.drip.product.calib.ProductQuoteSet pqs = aCalibComp2[i].calibQuoteSet (aLSS);

					if (null == pqs || !pqs.set (astrManifestMeasure2[i], adblQuote2[i])) return null;

					aSegmentSpec[i] = new org.drip.state.inference.LatentStateSegmentSpec (aCalibComp2[i],
						pqs);
				}

				stretchSpec2 = new org.drip.state.inference.LatentStateStretchSpec (strName + "_COMP2",
					aSegmentSpec);
			} catch (java.lang.Exception e) {
				e.printStackTrace();

				return null;
			}
		}

		if (null == stretchSpec1 && null == stretchSpec2) return null;

		if (null == stretchSpec1)
			aStretchSpec = new org.drip.state.inference.LatentStateStretchSpec[] {stretchSpec2};
		else if (null == stretchSpec2)
			aStretchSpec = new org.drip.state.inference.LatentStateStretchSpec[] {stretchSpec1};
		else
			aStretchSpec = new org.drip.state.inference.LatentStateStretchSpec[] {stretchSpec1,
				stretchSpec2};

		try {
			llsc = new org.drip.state.inference.LinearLatentStateCalibrator (new
				org.drip.spline.params.SegmentCustomBuilderControl (strBasisType, fsbp,
					org.drip.spline.params.SegmentInelasticDesignControl.Create (2, 2), new
						org.drip.spline.params.ResponseScalingShapeControl (true, new
							org.drip.quant.function1D.QuadraticRationalShapeControl (0.)), null),
								org.drip.spline.stretch.BoundarySettings.NaturalStandard(),
									org.drip.spline.stretch.MultiSegmentSequence.CALIBRATE, null, null);

			dcShapePreserving = ShapePreservingDFBuild (llsc, aStretchSpec, valParams, pricerParam, csqs,
				quotingParam, dblEpochResponse);
		} catch (java.lang.Exception e) {
			e.printStackTrace();

			return null;
		}

		if (!bZeroSmooth) return dcShapePreserving;

		try {
			lccp = new org.drip.state.estimator.LocalControlCurveParams
				(org.drip.spline.pchip.LocalMonotoneCkGenerator.C1_HYMAN83,
					org.drip.analytics.definition.LatentStateStatic.DISCOUNT_QM_ZERO_RATE, new
						org.drip.spline.params.SegmentCustomBuilderControl
							(org.drip.spline.stretch.MultiSegmentSequenceBuilder.BASIS_SPLINE_POLYNOMIAL, new
								org.drip.spline.basis.PolynomialFunctionSetParams (4),
									org.drip.spline.params.SegmentInelasticDesignControl.Create (2, 2), new
										org.drip.spline.params.ResponseScalingShapeControl (true, new
											org.drip.quant.function1D.QuadraticRationalShapeControl (0.)),
												null),
													org.drip.spline.stretch.MultiSegmentSequence.CALIBRATE,
														null, null, true, true);
		} catch (java.lang.Exception e) {
			e.printStackTrace();

			return null;
		}

		return SmoothingLocalControlBuild (dcShapePreserving, llsc, lccp, valParams, null, null, null);
	}

	/**
	 * Construct an instance of the Shape Preserver of the KLK Hyperbolic Tension Type, using the specified
	 *  basis set builder parameters.
	 * 
	 * @param strName Curve Name
	 * @param valParams Valuation Parameters
	 * @param aCalibComp1 Array of Calibration Components #1
	 * @param adblQuote1 Array of Calibration Quotes #1
	 * @param astrManifestMeasure1 Array of Manifest Measures for component Array #1
	 * @param aCalibComp2 Array of Calibration Components #2
	 * @param adblQuote2 Array of Calibration Quotes #2
	 * @param astrManifestMeasure2 Array of Manifest Measures for component Array #2
	 * @param bZeroSmooth TRUE => Turn on the Zero Rate Smoothing
	 * 
	 * @return Instance of the Shape Preserver of the desired basis type
	 */

	public static final org.drip.analytics.rates.DiscountCurve CubicKLKHyperbolicDFRateShapePreserver (
		final java.lang.String strName,
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.product.definition.CalibratableFixedIncomeComponent[] aCalibComp1,
		final double[] adblQuote1,
		final java.lang.String[] astrManifestMeasure1,
		final org.drip.product.definition.CalibratableFixedIncomeComponent[] aCalibComp2,
		final double[] adblQuote2,
		final java.lang.String[] astrManifestMeasure2,
		final boolean bZeroSmooth)
	{
		try {
			return DFRateShapePreserver (strName, valParams, null, null, null,
				org.drip.spline.stretch.MultiSegmentSequenceBuilder.BASIS_SPLINE_KLK_HYPERBOLIC_TENSION, new
					org.drip.spline.basis.ExponentialTensionSetParams (1.), aCalibComp1, adblQuote1,
						astrManifestMeasure1, aCalibComp2, adblQuote2, astrManifestMeasure2, 1.,
							bZeroSmooth);
		} catch (java.lang.Exception e) {
			e.printStackTrace();
		}

		return null;
	}

	/**
	 * Construct an instance of the Shape Preserver of the Cubic Polynomial Type, using the specified
	 *  basis set builder parameters.
	 * 
	 * @param strName Curve Name
	 * @param valParams Valuation Parameters
	 * @param aCalibComp1 Array of Calibration Components #1
	 * @param adblQuote1 Array of Calibration Quotes #1
	 * @param astrManifestMeasure1 Array of Manifest Measures for component Array #1
	 * @param aCalibComp2 Array of Calibration Components #2
	 * @param adblQuote2 Array of Calibration Quotes #2
	 * @param astrManifestMeasure2 Array of Manifest Measures for component Array #2
	 * @param bZeroSmooth TRUE => Turn on the Zero Rate Smoothing
	 * 
	 * @return Instance of the Shape Preserver of the desired basis type
	 */

	public static final org.drip.analytics.rates.DiscountCurve CubicPolyDFRateShapePreserver (
		final java.lang.String strName,
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.product.definition.CalibratableFixedIncomeComponent[] aCalibComp1,
		final double[] adblQuote1,
		final java.lang.String[] astrManifestMeasure1,
		final org.drip.product.definition.CalibratableFixedIncomeComponent[] aCalibComp2,
		final double[] adblQuote2,
		final java.lang.String[] astrManifestMeasure2,
		final boolean bZeroSmooth)
	{
		try {
			return DFRateShapePreserver (strName, valParams, null, null, null,
				org.drip.spline.stretch.MultiSegmentSequenceBuilder.BASIS_SPLINE_POLYNOMIAL, new
					org.drip.spline.basis.PolynomialFunctionSetParams (4), aCalibComp1, adblQuote1,
						astrManifestMeasure1, aCalibComp2, adblQuote2, astrManifestMeasure2, 1.,
							bZeroSmooth);
		} catch (java.lang.Exception e) {
			e.printStackTrace();
		}

		return null;
	}

	/**
	 * Customizable DENSE Curve Creation Methodology - the references are:
	 * 
	 *  - Sankar, L. (1997): OFUTS – An Alternative Yield Curve Interpolator F. A. S. T. Research
	 *  	Documentation Bear Sterns.
	 *  
	 *  - Nahum, E. (2004): Changes to Yield Curve Construction – Linear Stripping of the Short End of the
	 *  	Curve F. A. S. T. Research Documentation Bear Sterns.
	 *  
	 *  - Kinlay, J., and X. Bai (2009): Yield Curve Construction Models – Tools & Techniques 
	 *  	(http://www.jonathankinlay.com/Articles/Yield Curve Construction Models.pdf)
	 *  
	 * @param strName The Curve Name
	 * @param valParams Valuation Parameters
	 * @param aCalibComp1 Array of Stretch #1 Calibration Components
	 * @param adblQuote1 Array of Stretch #1 Calibration Quotes
	 * @param strTenor1 Stretch #1 Instrument set re-construction Tenor
	 * @param astrManifestMeasure1 Array of Manifest Measures for component Array #1
	 * @param aCalibComp2 Array of Stretch #2 Calibration Components
	 * @param adblQuote2 Array of Stretch #2 Calibration Quotes
	 * @param strTenor2 Stretch #2 Instrument set re-construction Tenor
	 * @param astrManifestMeasure2 Array of Manifest Measures for component Array #2
	 * @param tldf The Turns List
	 * 
	 * @return The Customized DENSE Curve.
	 */

	public static final org.drip.analytics.rates.DiscountCurve CustomDENSE (
		final java.lang.String strName,
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.product.definition.CalibratableFixedIncomeComponent[] aCalibComp1,
		final double[] adblQuote1,
		final java.lang.String strTenor1,
		final java.lang.String[] astrManifestMeasure1,
		final org.drip.product.definition.CalibratableFixedIncomeComponent[] aCalibComp2,
		final double[] adblQuote2,
		final java.lang.String strTenor2,
		final java.lang.String[] astrManifestMeasure2,
		final org.drip.analytics.rates.TurnListDiscountFactor tldf)
	{
		org.drip.analytics.rates.DiscountCurve dcShapePreserver = CubicKLKHyperbolicDFRateShapePreserver
			(strName, valParams, aCalibComp1, adblQuote1, astrManifestMeasure1, aCalibComp2, adblQuote2,
				astrManifestMeasure2, false);

		if (null == dcShapePreserver || (null != tldf && !dcShapePreserver.setTurns (tldf))) return null;

		org.drip.param.market.CurveSurfaceQuoteSet csqs =
			org.drip.param.creator.MarketParamsBuilder.Create (dcShapePreserver, null, null, null,
				null, null, null);

		if (null == csqs) return null;

		CompQuote[] aCQ1 = null;

		java.lang.String strCurrency = aCalibComp1[0].payCurrency()[0];

		if (null == strTenor1 || strTenor1.isEmpty()) {
			if (null != aCalibComp1) {
				int iNumComp1 = aCalibComp1.length;

				if (0 != iNumComp1) {
					aCQ1 = new CompQuote[iNumComp1];

					for (int i = 0; i < iNumComp1; ++i)
						aCQ1[i] = new CompQuote (aCalibComp1[i], adblQuote1[i]);
				}
			}
		} else
			aCQ1 = CompQuote (valParams, csqs, strCurrency, aCalibComp1[0].effective(),
				aCalibComp1[0].maturity(), aCalibComp1[aCalibComp1.length - 1].maturity(), strTenor1, false);

		if (null == strTenor2 || strTenor2.isEmpty()) return dcShapePreserver;

		CompQuote[] aCQ2 = CompQuote (valParams, csqs, strCurrency, aCalibComp2[0].effective(),
			aCalibComp2[0].maturity(), aCalibComp2[aCalibComp2.length - 1].maturity(), strTenor2, true);

		int iNumDENSEComp1 = null == aCQ1 ? 0 : aCQ1.length;
		int iNumDENSEComp2 = null == aCQ2 ? 0 : aCQ2.length;
		int iTotalNumDENSEComp = iNumDENSEComp1 + iNumDENSEComp2;

		if (0 == iTotalNumDENSEComp) return null;

		double[] adblCalibQuote = new double[iTotalNumDENSEComp];
		java.lang.String[] astrCalibMeasure = new java.lang.String[iTotalNumDENSEComp];
		org.drip.product.definition.CalibratableFixedIncomeComponent[] aCalibComp = new
			org.drip.product.definition.CalibratableFixedIncomeComponent[iTotalNumDENSEComp];

		for (int i = 0; i < iNumDENSEComp1; ++i) {
			astrCalibMeasure[i] = "Rate";
			aCalibComp[i] = aCQ1[i]._comp;
			adblCalibQuote[i] = aCQ1[i]._dblQuote;
		}

		for (int i = iNumDENSEComp1; i < iTotalNumDENSEComp; ++i) {
			astrCalibMeasure[i] = "Rate";
			aCalibComp[i] = aCQ2[i - iNumDENSEComp1]._comp;
			adblCalibQuote[i] = aCQ2[i - iNumDENSEComp1]._dblQuote;
		}

		try {
			return ScenarioDiscountCurveBuilder.NonlinearBuild (new org.drip.analytics.date.JulianDate
				(valParams.valueDate()), strCurrency,
					org.drip.state.creator.DiscountCurveBuilder.BOOTSTRAP_MODE_CONSTANT_FORWARD, aCalibComp,
						adblCalibQuote, astrCalibMeasure, null);
		} catch (java.lang.Exception e) {
			e.printStackTrace();
		}

		return null;
	}

	/**
	 * The Standard DENSE Curve Creation Methodology - this uses no re-construction set for the short term,
	 * 	and uses 3M dense re-construction for the Swap Set. The references are:
	 * 
	 *  - Sankar, L. (1997): OFUTS – An Alternative Yield Curve Interpolator F. A. S. T. Research
	 *  	Documentation Bear Sterns.
	 *  
	 *  - Nahum, E. (2004): Changes to Yield Curve Construction – Linear Stripping of the Short End of the
	 *  	Curve F. A. S. T. Research Documentation Bear Sterns.
	 *  
	 *  - Kinlay, J., and X. Bai (2009): Yield Curve Construction Models – Tools & Techniques 
	 *  	(http://www.jonathankinlay.com/Articles/Yield Curve Construction Models.pdf)
	 *  
	 * @param strName The Curve Name
	 * @param valParams Valuation Parameters
	 * @param aCalibComp1 Array of Stretch #1 Calibration Components
	 * @param adblQuote1 Array of Stretch #1 Calibration Quotes
	 * @param astrManifestMeasure1 Array of Manifest Measures for component Array #1
	 * @param aCalibComp2 Array of Stretch #2 Calibration Components
	 * @param adblQuote2 Array of Stretch #2 Calibration Quotes
	 * @param astrManifestMeasure2 Array of Manifest Measures for component Array #2
	 * @param tldf The Turns List
	 * 
	 * @return The Customized DENSE Curve.
	 */

	public static final org.drip.analytics.rates.DiscountCurve DENSE (
		final java.lang.String strName,
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.product.definition.CalibratableFixedIncomeComponent[] aCalibComp1,
		final double[] adblQuote1,
		final java.lang.String[] astrManifestMeasure1,
		final org.drip.product.definition.CalibratableFixedIncomeComponent[] aCalibComp2,
		final double[] adblQuote2,
		final java.lang.String[] astrManifestMeasure2,
		final org.drip.analytics.rates.TurnListDiscountFactor tldf)
	{
		return CustomDENSE (strName, valParams, aCalibComp1, adblQuote1, null, astrManifestMeasure1,
			aCalibComp2, adblQuote2, "3M", astrManifestMeasure2, tldf);
	}

	/**
	 * The DUAL DENSE Curve Creation Methodology - this uses configurable re-construction set for the short
	 *  term, and another configurable re-construction for the Swap Set. 1D re-construction tenor for the
	 *  short end will result in CDF (Constant Daily Forward) Discount Curve. The references are:
	 * 
	 *  - Sankar, L. (1997): OFUTS – An Alternative Yield Curve Interpolator F. A. S. T. Research
	 *  	Documentation Bear Sterns.
	 *  
	 *  - Nahum, E. (2004): Changes to Yield Curve Construction – Linear Stripping of the Short End of the
	 *  	Curve F. A. S. T. Research Documentation Bear Sterns.
	 *  
	 *  - Kinlay, J., and X. Bai (2009): Yield Curve Construction Models – Tools & Techniques 
	 *  	(http://www.jonathankinlay.com/Articles/Yield Curve Construction Models.pdf)
	 *  
	 * @param strName The Curve Name
	 * @param valParams Valuation Parameters
	 * @param aCalibComp1 Array of Stretch #1 Calibration Components
	 * @param adblQuote1 Array of Stretch #1 Calibration Quotes
	 * @param strTenor1 Stretch #1 Instrument set re-construction Tenor
	 * @param astrManifestMeasure1 Array of Manifest Measures for component Array #1
	 * @param aCalibComp2 Array of Stretch #2 Calibration Components
	 * @param adblQuote2 Array of Stretch #2 Calibration Quotes
	 * @param strTenor2 Stretch #2 Instrument set re-construction Tenor
	 * @param astrManifestMeasure2 Array of Manifest Measures for component Array #2
	 * @param tldf The Turns List
	 * 
	 * @return The Customized DENSE Curve.
	 */

	public static final org.drip.analytics.rates.DiscountCurve DUALDENSE (
		final java.lang.String strName,
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.product.definition.CalibratableFixedIncomeComponent[] aCalibComp1,
		final double[] adblQuote1,
		final java.lang.String strTenor1,
		final java.lang.String[] astrManifestMeasure1,
		final org.drip.product.definition.CalibratableFixedIncomeComponent[] aCalibComp2,
		final double[] adblQuote2,
		final java.lang.String strTenor2,
		final java.lang.String[] astrManifestMeasure2,
		final org.drip.analytics.rates.TurnListDiscountFactor tldf)
	{
		return CustomDENSE (strName, valParams, aCalibComp1, adblQuote1, strTenor1, astrManifestMeasure1,
			aCalibComp2, adblQuote2, strTenor2, astrManifestMeasure2, tldf);
	}
}
