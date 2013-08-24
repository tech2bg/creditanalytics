
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
 * SegmentEdgeParams contains the segment local boundary parameters used for the segment calibration. It
 * 	holds the edge Y value and the derivatives.
 *
 * @author Lakshmi Krishnamurthy
 */

public class SegmentEdgeParams {
	private double[] _adblDeriv = null;
	private double _dblY = java.lang.Double.NaN;

	/**
	 * Aggregate the 2 Segment Edge Parameters by applying the Cardinal Tension Weight
	 * 
	 * @param sepA Segment Edge Parameters A
	 * @param sepB Segment Edge Parameters B
	 * @param dblTension Cardinal Tension
	 * 
	 * @return The Aggregated Segment Edge Parameters
	 */

	public static final SegmentEdgeParams CardinalEdgeAggregate (
		final org.drip.math.grid.SegmentEdgeParams sepA,
		final org.drip.math.grid.SegmentEdgeParams sepB,
		final double dblTension)
	{
		if (null == sepA || null == sepB || !org.drip.math.common.NumberUtil.IsValid (dblTension))
			return null;

		int iNumDeriv = 0;

		double[] adblDerivA = sepA.getDeriv();

		double[] adblDerivB = sepB.getDeriv();

		if ((null != adblDerivA && null == adblDerivB) || (null == adblDerivA && null != adblDerivB) || (null
			!= adblDerivA && null != adblDerivB && (iNumDeriv = adblDerivA.length) != adblDerivB.length))
			return null;

		double dblYCardinalAggregated = 0.5 * (1. - dblTension) * (sepA.getY() + sepB.getY());

		if (null == adblDerivA || null == adblDerivB || 0 == iNumDeriv) {
			try {
				return new SegmentEdgeParams (dblYCardinalAggregated, null);
			} catch (java.lang.Exception e) {
				e.printStackTrace();

				return null;
			}
		}

		double[] adblDeriv = new double[iNumDeriv];

		for (int i = 0; i < iNumDeriv; ++i)
			adblDeriv[i] = 0.5 * (1. - dblTension) * (adblDerivA[i] + adblDerivB[i]);

		try {
			return new SegmentEdgeParams (dblYCardinalAggregated, adblDeriv);
		} catch (java.lang.Exception e) {
			e.printStackTrace();
		}

		return null;
	}

	/**
	 * SegmentEdgeParams constructor
	 * 
	 * @param dblY Edge Y Value
	 * @param adblDeriv Array of ordered Edge Derivatives
	 * 
	 * @throws java.lang.Exception Thrown if the inputs are invalid
	 */

	public SegmentEdgeParams (
		final double dblY,
		final double[] adblDeriv)
		throws java.lang.Exception
	{
		if (!org.drip.math.common.NumberUtil.IsValid (_dblY = dblY))
			throw new java.lang.Exception ("SegmentEdgeParams ctr: Ivalid Inputs!");

		_adblDeriv = adblDeriv;
	}

	/**
	 * Retrieve the Edge Y
	 * 
	 * @return Edge Y
	 */

	public double getY()
	{
		return _dblY;
	}

	/**
	 * Retrieve the Edge Derivative Array
	 * 
	 * @return Edge Derivative Array
	 */

	public double[] getDeriv()
	{
		return _adblDeriv;
	}
}
