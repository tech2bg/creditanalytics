
package org.drip.state.curve;

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
 * BasisSplineForwardRate manages the Discounting Latent State, using the Forward Rate as the State
 *  Response Representation. It exports the following functionality:
 *  - Calculate discount factor / discount factor Jacobian
 *  - Calculate implied forward rate / implied forward rate Jacobian
 *  - Construct tweaked curve instances (parallel/tenor/custom tweaks)
 *  - Optionally provide the calibration instruments and quotes used to build the curve.
 *
 * @author Lakshmi Krishnamurthy
 */

public class BasisSplineForwardRate extends org.drip.analytics.rates.ForwardCurve {
	private org.drip.spline.grid.Span _span = null;

	/**
	 * BasisSplineForwardRate constructor
	 * 
	 * @param span The Span over which the Forward Rate Representation is valid
	 * @param strCurrency The Currency of the Forward Rate
	 * @param strTenor The Tenor of the Forward Rate
	 * 
	 * @throws java.lang.Exception Thrown if the Inputs are Invalid
	 */

	public BasisSplineForwardRate (
		final java.lang.String strCurrency,
		final java.lang.String strTenor,
		final org.drip.spline.grid.Span span)
		throws java.lang.Exception
	{
		super (span.left(), strCurrency, strTenor);

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
