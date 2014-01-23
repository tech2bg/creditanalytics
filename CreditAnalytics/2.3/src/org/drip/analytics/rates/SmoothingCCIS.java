
package org.drip.analytics.rates;

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
 * SmoothingCCIS enhances the Shape Preserving CCIS for smoothing customizations. It exposes the shape
 * 	preserving discount curve and the smoothing curve stretch parameters.
 *
 * @author Lakshmi Krishnamurthy
 */

public class SmoothingCCIS extends org.drip.analytics.definition.ShapePreservingCCIS {
	private org.drip.analytics.rates.DiscountCurve _dcShapePreserver = null;
	private org.drip.state.estimator.SmoothingCurveStretchParams _scsp = null;

	/**
	 * SmoothingCCIS constructor
	 * 
	 * @param dcShapePreserver Shape Preserving Discount Curve Instance
	 * @param scsp Smoothing Curve Stretch Parameters
	 * @param lccShapePreserving Shape Preserving LinearCurveCalibrator instance
	 * @param aRBS Array of Stretch Representations
	 * @param valParam Valuation Parameters
	 * @param pricerParam Pricer Parameters
	 * @param quotingParam Quoting Parameters
	 * @param cmp Component Market Parameters
	 * 
	 * @throws java.lang.Exception Thrown if Inputs are invalid
	 */

	public SmoothingCCIS (
		final org.drip.analytics.rates.DiscountCurve dcShapePreserver,
		final org.drip.state.estimator.SmoothingCurveStretchParams scsp,
		final org.drip.state.estimator.LinearCurveCalibrator lccShapePreserving,
		final org.drip.state.estimator.StretchRepresentationSpec[] aRBS,
		final org.drip.param.valuation.ValuationParams valParam,
		final org.drip.param.pricer.PricerParams pricerParam,
		final org.drip.param.valuation.QuotingParams quotingParam,
		final org.drip.param.definition.ComponentMarketParams cmp)
		throws java.lang.Exception
	{
		super (lccShapePreserving, aRBS, valParam, pricerParam, quotingParam, cmp);

		if (null == (_dcShapePreserver = dcShapePreserver) || null == (_scsp = scsp))
			throw new java.lang.Exception ("SmoothingCCIS ctr: Invalid Inputs");
	}

	/**
	 * Retrieve the Shape Preserving Discount Curve
	 * 
	 * @return The Shape Preserving Discount Curve
	 */

	public org.drip.analytics.rates.DiscountCurve getShapePreservingDC()
	{
		return _dcShapePreserver;
	}

	/**
	 * Retrieve the Smoothing Curve Stretch Parameters
	 * 
	 * @return The Smoothing Curve Stretch Parameters
	 */

	public org.drip.state.estimator.SmoothingCurveStretchParams scsp()
	{
		return _scsp;
	}
}
