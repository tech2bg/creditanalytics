	
package org.drip.math.grid;

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
 * SpanBuilderParams holds the parameters the guide the creation/behavior of the segment. It holds the
 *  segment elastic/inelastic parameters and the named basis function set.
 *
 * @author Lakshmi Krishnamurthy
 */

public class SpanBuilderParams {
	private java.lang.String _strBasisSpline = "";
	private org.drip.math.spline.BasisSetParams _bsbp = null;
	private org.drip.math.spline.SegmentInelasticParams _segParams = null;
	private org.drip.math.function.AbstractUnivariate _auShapeControl = null;

	/**
	 * SpanBuilderParams constructor
	 * 
	 * @param strBasisSpline Named Segment Basis Spline
	 * @param bsbp Segment Basis Set Construction Parameters
	 * @param segParams Segment Inelastic Parameters
	 * @param auShapeControl Segment Shape Controller
	 * 
	 * @throws java.lang.Exception Thrown if inputs are invalid
	 */

	public SpanBuilderParams (
		final java.lang.String strBasisSpline,
		final org.drip.math.spline.BasisSetParams bsbp,
		final org.drip.math.spline.SegmentInelasticParams segParams,
		final org.drip.math.function.AbstractUnivariate auShapeControl)
		throws java.lang.Exception
	{
		if (null == (_strBasisSpline = strBasisSpline) || null == (_bsbp = bsbp) || null == (_segParams =
			segParams))
			throw new java.lang.Exception ("SpanBuilderParams ctr => Invalid Inputs");

		_auShapeControl = auShapeControl;
	}

	/**
	 * Retrieve the Named Segment Basis Spline
	 * 
	 * @return The Named Segment Basis Spline
	 */

	public java.lang.String getBasisSpline()
	{
		return _strBasisSpline;
	}

	/**
	 * Retrieve the Segment Basis Set Construction Parameters
	 * 
	 * @return The Segment Basis Set Construction Parameters
	 */

	public org.drip.math.spline.BasisSetParams getBasisSetParams()
	{
		return _bsbp;
	}

	/**
	 * Retrieve the Segment Inelastic Parameters
	 * 
	 * @return The Segment Inelastic Parameters
	 */

	public org.drip.math.spline.SegmentInelasticParams getSegmentInelasticParams()
	{
		return _segParams;
	}

	/**
	 * Retrieve the Segment Shape Controller
	 * 
	 * @return The Segment Shape Controller
	 */

	public org.drip.math.function.AbstractUnivariate getShapeController()
	{
		return _auShapeControl;
	}
}
