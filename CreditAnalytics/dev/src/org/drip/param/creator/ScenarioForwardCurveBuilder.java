
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
 * ScenarioForwardCurveBuilder implements the the construction of the scenario discount curve using the input
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

public class ScenarioForwardCurveBuilder {

	/**
	 * Build the Shape Preserving Forward Curve using the Custom Parameters
	 * 
	 * @param lcc The Linear Curve Calibrator Instance
	 * @param aSRS Array of the Instrument Representation Stretches
	 * @param fri The Floating Rate Index
	 * @param valParam Valuation Parameters
	 * @param pricerParam Pricer Parameters
	 * @param cmp Component Market Parameters
	 * @param quotingParam Quoting Parameters
	 * @param dblEpochResponse The Starting Response Value
	 * 
	 * @return Instance of the Shape Preserving Discount Curve
	 */

	public static final org.drip.analytics.rates.ForwardCurve ShapePreservingForwardCurve (
		final org.drip.state.estimator.LinearCurveCalibrator lcc,
		final org.drip.state.estimator.StretchRepresentationSpec[] aSRS,
		final org.drip.product.params.FloatingRateIndex fri,
		final org.drip.param.valuation.ValuationParams valParam,
		final org.drip.param.pricer.PricerParams pricerParam,
		final org.drip.param.definition.ComponentMarketParams cmp,
		final org.drip.param.valuation.QuotingParams quotingParam,
		final double dblEpochResponse)
	{
		if (null == lcc) return null;

		try {
			org.drip.analytics.rates.ForwardCurve fc = new org.drip.state.curve.BasisSplineForwardRate (fri,
				(lcc.calibrateSpan (aSRS, dblEpochResponse, valParam, pricerParam, quotingParam, cmp)));

			return fc.setCCIS (new org.drip.analytics.definition.ShapePreservingCCIS (lcc, aSRS, valParam,
				pricerParam, quotingParam, cmp)) ? fc : null;
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
	 * @param fri The Floating Rate Index
	 * @param valParams Valuation Parameters
	 * @param pricerParam Pricer Parameters
	 * @param cmp Component Market Parameters
	 * @param quotingParam Quoting Parameters
	 * @param strBasisType The Basis Type
	 * @param fsbp The Function Set Basis Parameters
	 * @param aCalibComp Array of Calibration Components
	 * @param strManifestMeasure The Calibration Manifest Measure
	 * @param adblQuote Array of Calibration Quotes
	 * @param dblEpochResponse The Stretch Start DF
	 * 
	 * @return Instance of the Shape Preserver of the desired basis type
	 */

	public static final org.drip.analytics.rates.ForwardCurve ShapePreservingForwardCurve (
		final java.lang.String strName,
		final org.drip.product.params.FloatingRateIndex fri,
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.pricer.PricerParams pricerParam,
		final org.drip.param.definition.ComponentMarketParams cmp,
		final org.drip.param.valuation.QuotingParams quotingParam,
		final java.lang.String strBasisType,
		final org.drip.spline.basis.FunctionSetBuilderParams fsbp,
		final org.drip.product.definition.CalibratableFixedIncomeComponent[] aCalibComp,
		final java.lang.String strManifestMeasure,
		final double[] adblQuote,
		final double dblEpochResponse)
	{
		if (null == strName || strName.isEmpty() || null == strBasisType || strBasisType.isEmpty() || null ==
			valParams || null == fsbp || null == strManifestMeasure || strManifestMeasure.isEmpty())
			return null;

		int iNumQuote = null == adblQuote ? 0 : adblQuote.length;
		int iNumComp = null == aCalibComp ? 0 : aCalibComp.length;

		if (0 == iNumComp || iNumComp != iNumQuote) return null;

		org.drip.state.estimator.StretchRepresentationSpec srs =
			org.drip.state.estimator.StretchRepresentationSpec.CreateStretchBuilderSet (strName + "_COMP1",
				org.drip.analytics.rates.ForwardCurve.LATENT_STATE_FORWARD,
					org.drip.analytics.rates.ForwardCurve.QUANTIFICATION_METRIC_FORWARD_RATE, aCalibComp,
						strManifestMeasure, adblQuote, null);

		org.drip.state.estimator.StretchRepresentationSpec[] aSRS = new
			org.drip.state.estimator.StretchRepresentationSpec[] {srs};

		try {
			org.drip.state.estimator.LinearCurveCalibrator lcc = new
				org.drip.state.estimator.LinearCurveCalibrator (new
					org.drip.spline.params.SegmentCustomBuilderControl (strBasisType, fsbp,
						org.drip.spline.params.SegmentInelasticDesignControl.Create (2, 2), new
							org.drip.spline.params.ResponseScalingShapeControl (true, new
								org.drip.quant.function1D.QuadraticRationalShapeControl (0.)), null),
									org.drip.spline.stretch.BoundarySettings.FinancialStandard(),
										org.drip.spline.stretch.MultiSegmentSequence.CALIBRATE, null, null);

			return ShapePreservingForwardCurve (lcc, aSRS, fri, valParams, pricerParam, cmp, quotingParam,
				dblEpochResponse);
		} catch (java.lang.Exception e) {
			e.printStackTrace();
		}

		return null;
	}
}
