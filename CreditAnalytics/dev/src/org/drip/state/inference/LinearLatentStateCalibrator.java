
package org.drip.state.inference;

/*
 * -*- mode: java; tab-width: 4; indent-tabs-mode: nil; c-basic-offset: 4 -*-
 */

/*!
 * Copyright (C) 2015 Lakshmi Krishnamurthy
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
 * LinearLatentStateCalibrator calibrates/constructs the Latent State Stretch/Span from the calibration
 * 	instrument details. The span construction may be customized using specific settings provided in
 * 	GlobalControlCurveParams.
 *
 * @author Lakshmi Krishnamurthy
 */

public class LinearLatentStateCalibrator extends org.drip.state.estimator.GlobalControlCurveParams {

	/**
	 * LinearLatentStateCalibrator constructor
	 * 
	 * @param scbc Segment Builder Control Parameters
	 * @param bs The Calibration Boundary Condition
	 * @param iCalibrationDetail The Calibration Detail
	 * @param sbfr Curve Fitness Weighted Response
	 * @param sbfrSensitivity Curve Fitness Weighted Response Sensitivity
	 * 
	 * @throws java.lang.Exception Thrown if the inputs are invalid
	 */

	public LinearLatentStateCalibrator (
		final org.drip.spline.params.SegmentCustomBuilderControl scbc,
		final org.drip.spline.stretch.BoundarySettings bs,
		final int iCalibrationDetail,
		final org.drip.spline.params.StretchBestFitResponse sbfr,
		final org.drip.spline.params.StretchBestFitResponse sbfrSensitivity)
		throws java.lang.Exception
	{
		super ("", scbc, bs, iCalibrationDetail, sbfr, sbfrSensitivity);
	}

	/**
	 * Calibrate the Span from the Instruments in the Stretches and their Details.
	 * 
	 * @param aStretchSpec The Stretch Sequence constituting the Span
	 * @param dblEpochResponse Segment Sequence Left-most Response Value
	 * @param valParams Valuation Parameter
	 * @param pricerParams Pricer Parameter
	 * @param vcp The Valuation Customization Parameters
	 * @param csqs The Market Parameters Surface and Quote
	 * 
	 * @return Instance of the Latent State Span
	 */

	public org.drip.spline.grid.OverlappingStretchSpan calibrateSpan (
		final org.drip.state.inference.LatentStateStretchSpec[] aStretchSpec,
		final double dblEpochResponse,
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.pricer.PricerParams pricerParams,
		final org.drip.param.valuation.ValuationCustomizationParams vcp,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs)
	{
		if (null == aStretchSpec || null == valParams) return null;

		int iNumStretch = aStretchSpec.length;
		org.drip.spline.grid.OverlappingStretchSpan oss = null;

		if (0 == iNumStretch) return null;

		for (org.drip.state.inference.LatentStateStretchSpec stretchSpec : aStretchSpec) {
			if (null == stretchSpec) continue;

			org.drip.state.inference.LatentStateSegmentSpec[] aSegmentSpec = stretchSpec.segmentSpec();

			int iNumCalibComp = aSegmentSpec.length;
			org.drip.state.estimator.CurveStretch cs = null;
			double[] adblPredictorOrdinate = new double[iNumCalibComp + 1];
			org.drip.spline.params.SegmentCustomBuilderControl[] aSCBC = new
				org.drip.spline.params.SegmentCustomBuilderControl[iNumCalibComp];

			for (int i = 0; i <= iNumCalibComp; ++i) {
				adblPredictorOrdinate[i] = 0 == i ? valParams.valueDate() :
					aSegmentSpec[i - 1].component().maturityDate().julian();

				if (i != iNumCalibComp) aSCBC[i] = segmentBuilderControl (stretchSpec.name());
			}

			try {
				cs = new org.drip.state.estimator.CurveStretch (stretchSpec.name(),
					org.drip.spline.stretch.MultiSegmentSequenceBuilder.CreateSegmentSet
						(adblPredictorOrdinate, aSCBC), aSCBC);

				if (!cs.setup (new org.drip.state.inference.LatentStateSequenceBuilder (dblEpochResponse,
					stretchSpec, valParams, pricerParams, csqs, vcp, oss, bestFitWeightedResponse(), new
						org.drip.analytics.support.CaseInsensitiveHashMap<org.drip.spline.params.PreceedingManifestSensitivityControl>(),
					bestFitWeightedResponseSensitivity(), calibrationBoundaryCondition()),
						calibrationDetail())) {
					System.out.println ("\tMSS Setup Failed!");

					return null;
				}
			} catch (java.lang.Exception e) {
				e.printStackTrace();

				return null;
			}

			if (null == oss) {
				try {
					oss = new org.drip.spline.grid.OverlappingStretchSpan (cs);
				} catch (java.lang.Exception e) {
					e.printStackTrace();

					return null;
				}
			} else {
				if (!oss.addStretch (cs)) return null;
			}
		}

		return oss;
	}
}
