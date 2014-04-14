
package org.drip.state.curve;

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
 * BasisSplineForwardRate manages the Forward Latent State, using the Forward Rate as the State Response
 *  Representation. It exports the following functionality:
 *  - Calculate implied forward rate / implied forward rate Jacobian
 *  - Serialize into and de-serialize out of byte arrays
 *
 * @author Lakshmi Krishnamurthy
 */

public class BasisSplineForwardRate extends org.drip.analytics.rates.ForwardCurve {
	private org.drip.spline.grid.Span _span = null;

	/**
	 * BasisSplineForwardRate constructor
	 * 
	 * @param fri The Floating Rate Index
	 * @param span The Span over which the Forward Rate Representation is valid
	 * 
	 * @throws java.lang.Exception Thrown if the Inputs are Invalid
	 */

	public BasisSplineForwardRate (
		final org.drip.product.params.FloatingRateIndex fri,
		final org.drip.spline.grid.OverlappingStretchSpan span)
		throws java.lang.Exception
	{
		super (span.left(), fri);

		_span = span;
	}

	@Override public double forward (
		final double dblDate)
		throws java.lang.Exception
	{
		if (!org.drip.quant.common.NumberUtil.IsValid (dblDate))
			throw new java.lang.Exception ("BasisSplineForwardRate::forward => Invalid Input");

		double dblSpanLeft = _span.left();

		if (dblDate <= dblSpanLeft) return _span.calcResponseValue (dblSpanLeft);

		double dblSpanRight = _span.right();

		if (dblDate >= dblSpanRight) return _span.calcResponseValue (dblSpanRight);

		return _span.calcResponseValue (dblDate);
	}

	@Override public org.drip.param.valuation.CollateralizationParams collateralParams()
	{
		return null;
	}

	@Override public org.drip.quant.calculus.WengertJacobian jackDForwardDManifestMeasure (
		final java.lang.String strManifestMeasure,
		final double dblDate)
	{
		return _span.jackDResponseDManifestMeasure (strManifestMeasure, dblDate, 1);
	}

	@Override public byte[] serialize()
	{
		return null;
	}

	@Override public org.drip.service.stream.Serializer deserialize (
		final byte[] ab)
	{
		return null;
	}
}
