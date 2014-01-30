
package org.drip.state.estimator;

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
 * LinearCurveCalibrator creates the discount curve span from the instrument cash flows. The span
 * 	construction may be customized using specific settings provided in GlobalControlCurveParams.
 *
 * @author Lakshmi Krishnamurthy
 */

public class LinearCurveCalibrator extends org.drip.state.estimator.GlobalControlCurveParams {

	/**
	 * LinearCurveCalibrator constructor
	 * 
	 * @param scbc Segment Builder Control Parameters
	 * @param bs The Calibration Boundary Condition
	 * @param iCalibrationDetail The Calibration Detail
	 * @param sbfr Curve Fitness Weighted Response
	 * @param sbfrSensitivity Curve Fitness Weighted Response Sensitivity
	 * 
	 * @throws java.lang.Exception Thrown if the inputs are invalid
	 */

	public LinearCurveCalibrator (
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
	 * Calibrate the Span from the Instruments in the Stretches, and their Cash Flows.
	 * 
	 * @param aSRS Array of the Stretch Builder Parameters
	 * @param dblEpochResponse Segment Sequence Left-most Response Value
	 * @param valParams Valuation Parameter
	 * @param pricerParams Pricer Parameter
	 * @param quotingParams Quoting Parameter
	 * @param cmp Component Market Parameter
	 * 
	 * @return Instance of the Discount Curve Span
	 */

	public org.drip.spline.grid.OverlappingStretchSpan calibrateSpan (
		final org.drip.state.estimator.StretchRepresentationSpec[] aSRS,
		final double dblEpochResponse,
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.pricer.PricerParams pricerParams,
		final org.drip.param.valuation.QuotingParams quotingParams,
		final org.drip.param.definition.ComponentMarketParams cmp)
	{
		if (null == aSRS || null == valParams) return null;

		int iNumStretch = aSRS.length;
		org.drip.state.estimator.CurveStretch csPrev = null;
		org.drip.spline.grid.OverlappingStretchSpan oss = null;

		if (0 == iNumStretch) return null;

		for (org.drip.state.estimator.StretchRepresentationSpec srs : aSRS) {
			if (null == srs) return null;

			org.drip.product.definition.CalibratableComponent[] aCalibComp = srs.getCalibComp();

			int iNumCalibComp = aCalibComp.length;
			org.drip.state.estimator.CurveStretch cs = null;
			double[] adblPredictorOrdinate = new double[iNumCalibComp + 1];
			org.drip.spline.params.SegmentCustomBuilderControl[] aSCBC = new
				org.drip.spline.params.SegmentCustomBuilderControl[iNumCalibComp];

			for (int i = 0; i <= iNumCalibComp; ++i) {
				adblPredictorOrdinate[i] = 0 == i ? valParams.valueDate() :
					aCalibComp[i - 1].getMaturityDate().getJulian();

				if (i != iNumCalibComp) aSCBC[i] = segmentBuilderControl (srs.getName());
			}

			try {
				cs = new org.drip.state.estimator.CurveStretch (srs.getName(),
					org.drip.spline.stretch.MultiSegmentSequenceBuilder.CreateSegmentSet
						(adblPredictorOrdinate, aSCBC), aSCBC);

				if (!cs.setup (new org.drip.state.estimator.RatesSegmentSequenceBuilder (dblEpochResponse,
					srs, valParams, pricerParams, cmp, quotingParams, csPrev, bestFitWeightedResponse(),
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

			csPrev = cs;
		}

		return oss;
	}
}
