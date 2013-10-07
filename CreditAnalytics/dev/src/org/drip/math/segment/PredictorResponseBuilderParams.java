	
package org.drip.math.segment;

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
 * PredictorResponseBuilderParams holds the parameters the guide the creation/behavior of the segment. It holds the
 *  segment elastic/inelastic parameters and the named basis function set.
 *
 * @author Lakshmi Krishnamurthy
 */

public class PredictorResponseBuilderParams {
	private java.lang.String _strBasisSpline = "";
	private org.drip.math.spline.BasisSetParams _bsp = null;
	private org.drip.math.segment.DesignInelasticParams _dip = null;
	private org.drip.math.segment.ResponseScalingShapeController _rssc = null;

	/**
	 * PredictorResponseBuilderParams constructor
	 * 
	 * @param strBasisSpline Named Segment Basis Spline
	 * @param bsp Segment Basis Set Construction Parameters
	 * @param dip Segment Design Inelastic Parameters
	 * @param rssc Segment Shape Controller
	 * 
	 * @throws java.lang.Exception Thrown if inputs are invalid
	 */

	public PredictorResponseBuilderParams (
		final java.lang.String strBasisSpline,
		final org.drip.math.spline.BasisSetParams bsp,
		final org.drip.math.segment.DesignInelasticParams dip,
		final org.drip.math.segment.ResponseScalingShapeController rssc)
		throws java.lang.Exception
	{
		if (null == (_strBasisSpline = strBasisSpline) || null == (_bsp = bsp) || null == (_dip = dip))
			throw new java.lang.Exception ("PredictorResponseBuilderParams ctr => Invalid Inputs");

		_rssc = rssc;
	}

	/**
	 * Retrieve the Basis Spline Name
	 * 
	 * @return The Basis Spline Name
	 */

	public java.lang.String getBasisSpline()
	{
		return _strBasisSpline;
	}

	/**
	 * Retrieve the Basis Set Parameters
	 * 
	 * @return The Basis Set Parameters
	 */

	public org.drip.math.spline.BasisSetParams getBasisSetParams()
	{
		return _bsp;
	}

	/**
	 * Retrieve the Segment Elastic Parameters
	 * 
	 * @return The Segment Elastic Parameters
	 */

	public org.drip.math.segment.DesignInelasticParams getSegmentElasticParams()
	{
		return _dip;
	}

	/**
	 * Retrieve the Segment Shape Controller
	 * 
	 * @return The Segment Shape Controller
	 */

	public org.drip.math.segment.ResponseScalingShapeController getShapeController()
	{
		return _rssc;
	}
}
