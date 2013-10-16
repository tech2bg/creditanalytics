
package org.drip.state.estimator;

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
 * LinearCurveCalibrator creates the discount curve span from the instrument cash flows.
 *
 * @author Lakshmi Krishnamurthy
 */

public class LinearCurveCalibrator {
	private int _iCalibrationDetail = -1;
	private int _iCalibrationBoundaryCondition = -1;
	private org.drip.math.segment.BestFitWeightedResponse _fwr = null;
	private org.drip.math.segment.PredictorResponseBuilderParams _prbp = null;

	/**
	 * LinearCurveCalibrator constructor
	 * 
	 * @param prbp Segment Builder Parameters
	 * @param fwr Fitness Weighted Response
	 * @param iCalibrationBoundaryCondition The Calibration Boundary Condition
	 * @param iCalibrationDetail The Calibration Detail
	 * 
	 * @throws java.lang.Exception Thrown if the inputs are invalid
	 */

	public LinearCurveCalibrator (
		final org.drip.math.segment.PredictorResponseBuilderParams prbp,
		final org.drip.math.segment.BestFitWeightedResponse fwr,
		final int iCalibrationBoundaryCondition,
		final int iCalibrationDetail)
		throws java.lang.Exception
	{
		if (null == (_prbp = prbp))
			throw new java.lang.Exception ("LinearCurveCalibrator ctr: Invalid Inputs");

		_fwr = fwr;
		_iCalibrationDetail = iCalibrationDetail;
		_iCalibrationBoundaryCondition = iCalibrationBoundaryCondition;
	}

	/**
	 * Calibrate the Span from the instruments in the regimes, and their cash flows.
	 * 
	 * @param aRBS Array of the Regime Builder Parameters
	 * @param valParams Valuation Parameter
	 * @param pricerParams Pricer Parameter
	 * @param quotingParams Quoting Parameter
	 * @param cmp Component Market Parameter
	 * 
	 * @return Instance of the Discount Curve Span
	 */

	public org.drip.math.grid.OverlappingRegimeSpan calibrateSpan (
		final org.drip.state.estimator.RegimeBuilderSet[] aRBS,
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.pricer.PricerParams pricerParams,
		final org.drip.param.valuation.QuotingParams quotingParams,
		final org.drip.param.definition.ComponentMarketParams cmp)
	{
		if (null == aRBS || null == valParams) return null;

		int iNumRegime = aRBS.length;
		org.drip.math.grid.OverlappingRegimeSpan span = null;
		org.drip.math.regime.MultiSegmentRegime regimePrev = null;

		if (0 == iNumRegime) return null;

		for (org.drip.state.estimator.RegimeBuilderSet rbs : aRBS) {
			if (null == rbs) return null;

			org.drip.product.definition.CalibratableComponent[] aCalibComp = rbs.getCalibComp();

			int iNumCalibComp = aCalibComp.length;
			org.drip.math.regime.MultiSegmentRegime regime = null;
			double[] adblPredictorOrdinate = new double[iNumCalibComp + 1];
			org.drip.math.segment.PredictorResponseBuilderParams[] aPRBP = new
				org.drip.math.segment.PredictorResponseBuilderParams[iNumCalibComp];

			for (int i = 0; i <= iNumCalibComp; ++i) {
				adblPredictorOrdinate[i] = 0 == i ? valParams._dblValue :
					aCalibComp[i - 1].getMaturityDate().getJulian();

				if (i != iNumCalibComp) aPRBP[i] = _prbp;
			}

			try {
				regime = new org.drip.state.estimator.CurveRegime (rbs.getName(),
					org.drip.math.regime.RegimeBuilder.CreateSegmentSet (adblPredictorOrdinate, aPRBP),
						aPRBP);

				if (!regime.setup (new org.drip.state.estimator.RatesSegmentSequenceBuilder (rbs, valParams,
					pricerParams, cmp, quotingParams, regimePrev, _fwr, _iCalibrationBoundaryCondition),
						_iCalibrationDetail))
					return null;
			} catch (java.lang.Exception e) {
				e.printStackTrace();

				return null;
			}

			if (null == span) {
				try {
					span = new org.drip.math.grid.OverlappingRegimeSpan (regime);
				} catch (java.lang.Exception e) {
					e.printStackTrace();

					return null;
				}
			} else {
				if (!span.addRegime (regime)) return null;
			}

			regimePrev = regime;
		}

		return span;
	}
}
