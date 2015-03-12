
package org.drip.quant.linearalgebra;

/*
 * -*- mode: java; tab-width: 4; indent-tabs-mode: nil; c-basic-offset: 4 -*-
 */

/*!
 * Copyright (C) 2015 Lakshmi Krishnamurthy
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
 * EigenComponent holds the Component's Eigenvector and the corresponding Eigenvalue.
 *
 * @author Lakshmi Krishnamurthy
 */

public class EigenComponent {
	private double[] _adblEigenvector = null;
	private double _dblEigenvalue = java.lang.Double.NaN;

	/**
	 * EigenComponent Constructor
	 * 
	 * @param adblEigenvector The Eigenvector
	 * @param dblEigenvalue The Eigenvalue
	 * 
	 * @throws java.lang.Exception Thrown if the Inputs are invalid
	 */

	public EigenComponent (
		final double[] adblEigenvector,
		final double dblEigenvalue)
		throws java.lang.Exception
	{
		if (null == (_adblEigenvector = adblEigenvector) || !org.drip.quant.common.NumberUtil.IsValid
			(_dblEigenvalue = dblEigenvalue))
			throw new java.lang.Exception ("EigenComponent ctr: Invalid Inputs");

		int iNumOrdinate = _adblEigenvector.length;

		if (0 == iNumOrdinate) throw new java.lang.Exception ("EigenComponent ctr: Invalid Inputs");

		for (int i = 0; i < iNumOrdinate; ++i) {
			if (!org.drip.quant.common.NumberUtil.IsValid (_adblEigenvector[i]))
				throw new java.lang.Exception ("EigenComponent ctr: Invalid Inputs");
		}
	}

	/**
	 * Retrieve the Eigenvalue
	 * 
	 * @return The Eigenvalue
	 */

	public double eigenvalue()
	{
		return _dblEigenvalue;
	}

	/**
	 * Retrieve the Eigenvector
	 * 
	 * @return The Eigenvector
	 */

	public double[] eigenvector()
	{
		return _adblEigenvector;
	}
}
