
package org.drip.analytics.definition;

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
 * SmoothingCCIS contains the Parameters needed for the Curve Calibration/Estimation. It contains the
 *  following:
 *  - Calibration Valuation Parameters
 *  - Calibration Quoting Parameters
 *  - Array of Calibration Instruments
 *  - Map of Calibration Quotes
 *  - Map of Calibration Measures
 *  - Double Map of the Date/Index Fixings
 *
 * @author Lakshmi Krishnamurthy
 */

public class SmoothingCCIS extends org.drip.analytics.definition.ShapePreservingCCIS {
	private org.drip.state.estimator.SmoothingCurveRegimeParams _scrp = null;
	private org.drip.analytics.definition.DiscountCurve _dcShapePreserver = null;

	/**
	 * SmoothingCCIS constructor
	 * 
	 * @param dcShapePreserver Shape Preserving Discount Curve Instance
	 * @param scrp Smoothing Curve Regime Parameters
	 * @param lccShapePreserving Shape Preserving LinearCurveCalibrator instance
	 * @param aRBS Array of RegimeBuilderSet
	 * @param valParam Valuation Parameters
	 * @param pricerParam Pricer Parameters
	 * @param quotingParam Quoting Parameters
	 * @param cmp Component Market Parameters
	 * 
	 * @throws java.lang.Exception Thrown if Inputs are invalid
	 */

	public SmoothingCCIS (
		final org.drip.analytics.definition.DiscountCurve dcShapePreserver,
		final org.drip.state.estimator.SmoothingCurveRegimeParams scrp,
		final org.drip.state.estimator.LinearCurveCalibrator lccShapePreserving,
		final org.drip.state.estimator.RegimeRepresentationSpec[] aRBS,
		final org.drip.param.valuation.ValuationParams valParam,
		final org.drip.param.pricer.PricerParams pricerParam,
		final org.drip.param.valuation.QuotingParams quotingParam,
		final org.drip.param.definition.ComponentMarketParams cmp)
		throws java.lang.Exception
	{
		super (lccShapePreserving, aRBS, valParam, pricerParam, quotingParam, cmp);

		if (null == (_dcShapePreserver = dcShapePreserver) || null == (_scrp = scrp))
			throw new java.lang.Exception ("SmoothingCCIS ctr: Invalid Inputs");
	}

	/**
	 * Retrieve the Shape Preserving Discount Curve
	 * 
	 * @return The Shape Preserving Discount Curve
	 */

	public org.drip.analytics.definition.DiscountCurve getShapePreservingDC()
	{
		return _dcShapePreserver;
	}

	/**
	 * Retrieve the Smoothing Curve Regime Parameters
	 * 
	 * @return The Smoothing Curve Regime Parameters
	 */

	public org.drip.state.estimator.SmoothingCurveRegimeParams scrp()
	{
		return _scrp;
	}
}
