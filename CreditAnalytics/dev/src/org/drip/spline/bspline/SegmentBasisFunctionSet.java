
package org.drip.spline.bspline;

/*
 * -*- mode: java; tab-width: 4; indent-tabs-mode: nil; c-basic-offset: 4 -*-
 */

/*!
 * Copyright (C) 2015 Lakshmi Krishnamurthy
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
 * SegmentBasisFunctionSet class implements per-segment function set for B Splines and tension splines.
 *  Derived implementations expose explicit targeted basis functions.
 *
 * @author Lakshmi Krishnamurthy
 */

public class SegmentBasisFunctionSet extends org.drip.spline.basis.FunctionSet {
	protected double _dblTension = java.lang.Double.NaN;

	private static final org.drip.function.deterministic.AbstractUnivariate[] responseBasis (
		final int iNumBasisToUse,
		final org.drip.function.deterministic.AbstractUnivariate[] aAUHat)
	{
		if (null == aAUHat || iNumBasisToUse > aAUHat.length) return null;

		try {
			org.drip.function.deterministic.AbstractUnivariate[] aAU = new
				org.drip.function.deterministic.AbstractUnivariate[iNumBasisToUse + 2];

			aAU[0] = new org.drip.function.deterministic1D.Polynomial (0);

			aAU[1] = new org.drip.function.deterministic1D.UnivariateReflection (new
				org.drip.function.deterministic1D.Polynomial (1));

			for (int i = 0; i < iNumBasisToUse; ++i)
				aAU[2 + i] = aAUHat[i];

			return aAU;
		} catch (java.lang.Exception e) {
			e.printStackTrace();
		}

		return null;
	}

	/**
	 * SegmentBasisFunctionSet constructor
	 * 
	 * @param iNumBasisToUse Number of Basis in the Hat Basis Set to Use
	 * @param dblTension Tension Parameter
	 * @param aAUHat The Hat Representation Function Set
	 * 
	 * @throws java.lang.Exception Thrown if the inputs are invalid
	 */

	public SegmentBasisFunctionSet (
		final int iNumBasisToUse,
		final double dblTension,
		final org.drip.function.deterministic.AbstractUnivariate[] aAUHat)
		throws java.lang.Exception
	{
		super (responseBasis (iNumBasisToUse, aAUHat));

		if (!org.drip.quant.common.NumberUtil.IsValid (_dblTension = dblTension))
			throw new java.lang.Exception ("SegmentBasisFunctionSet ctr: Invalid Inputs!");
	}

	/**
	 * Retrieve the Tension Parameter
	 * 
	 * @return The Tension Parameter
	 */

	public double tension()
	{
		return _dblTension;
	}
}
