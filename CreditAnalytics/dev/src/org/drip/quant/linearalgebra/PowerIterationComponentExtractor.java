
package org.drip.quant.linearalgebra;

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
 * PowerIterationComponentExtractor extracts the Linear System Components using the Power Iteration Method.
 *
 * @author Lakshmi Krishnamurthy
 */

public class PowerIterationComponentExtractor {
	private int _iMaxIteration = -1;
	private boolean _bToleranceAbsolute = false;
	private double _dblTolerance = java.lang.Double.NaN;

	public PowerIterationComponentExtractor (
		final int iMaxIteration,
		final double dblTolerance,
		final boolean bToleranceAbsolute)
		throws java.lang.Exception
	{
		if (0 >= (_iMaxIteration = iMaxIteration) || !org.drip.quant.common.NumberUtil.IsValid (_dblTolerance
			= dblTolerance) || 0. == _dblTolerance)
			throw new java.lang.Exception ("PowerIterationComponentExtractor ctr: Invalid Inputs!");

		_bToleranceAbsolute = bToleranceAbsolute;
	}

	/**
	 * Retrieve the Maximum Number of Iterations
	 * 
	 * @return The Maximum Number of Iterations
	 */

	public int maxIterations()
	{
		return _iMaxIteration;
	}

	/**
	 * Retrieve the Tolerance Level
	 * 
	 * @return The Tolerance Level
	 */

	public double tolerance()
	{
		return _dblTolerance;
	}

	/**
	 * Indicate if the specified Tolerance is Absolute
	 * 
	 * @return TRUE => The specified Tolerance is Absolute
	 */

	public boolean isToleranceAbsolute()
	{
		return _bToleranceAbsolute;
	}

	/**
	 * Compute the Principal Component of the Specified Matrix
	 * 
	 * @param aadblA The Input Matrix
	 * 
	 * @return The Principal EigenComponent Instance
	 */

	public org.drip.quant.linearalgebra.EigenComponent principalComponent (
		final double[][] aadblA)
	{
		if (null == aadblA) return null;

		int iIter = 0;
		double dblEigenvalue = 0.;
		int iSize = aadblA.length;
		double[] adblEigenvector = new double[iSize];
		double[] adblUpdatedEigenvector = new double[iSize];

		if (0 == iSize || null == aadblA[0] || iSize != aadblA[0].length) return null;

		for (int i = 0; i < iSize; ++i) {
			adblEigenvector[i] = java.lang.Math.random();

			dblEigenvalue += adblEigenvector[i] * adblEigenvector[i];
		}

		double dblEigenvalueOld = (dblEigenvalue = java.lang.Math.sqrt (dblEigenvalue));

		for (int i = 0; i < iSize; ++i)
			adblEigenvector[i] /= dblEigenvalue;

		double dblAbsoluteTolerance = _bToleranceAbsolute ? _dblTolerance : dblEigenvalue * _dblTolerance;

		while (iIter < _iMaxIteration) {
			for (int i = 0; i < iSize; ++i) {
				adblUpdatedEigenvector[i] = 0.;

				for (int j = 0; j < iSize; ++j)
					adblUpdatedEigenvector[i] += aadblA[i][j] * adblEigenvector[j];
			}

			dblEigenvalue = 0.;

			for (int i = 0; i < iSize; ++i)
				dblEigenvalue += adblUpdatedEigenvector[i] * adblUpdatedEigenvector[i];

			dblEigenvalue = java.lang.Math.sqrt (dblEigenvalue);

			for (int i = 0; i < iSize; ++i)
				adblUpdatedEigenvector[i] /= dblEigenvalue;

			if (dblAbsoluteTolerance > java.lang.Math.abs (dblEigenvalue - dblEigenvalueOld)) break;

			adblEigenvector = adblUpdatedEigenvector;
			dblEigenvalueOld = dblEigenvalue;
			++iIter;
		}

		if (iIter >= _iMaxIteration) return null;

		try {
			return new org.drip.quant.linearalgebra.EigenComponent (adblEigenvector, java.lang.Math.pow
				(dblEigenvalue, 1. / (iIter + 1)));
		} catch (java.lang.Exception e) {
			e.printStackTrace();
		}

		return null;
	}
}
