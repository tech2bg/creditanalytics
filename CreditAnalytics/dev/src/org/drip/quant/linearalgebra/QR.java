
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
 * QR hlds the Results of QR Decomposition - viz., the Q and the R Matrices.
 *
 * @author Lakshmi Krishnamurthy
 */

public class QR {
	private double[][] _aadblQ = null;
	private double[][] _aadblR = null;

	/**
	 * QR Constructor
	 * 
	 * @param aadblQ Q
	 * @param aadblR R
	 * 
	 * @throws java.lang.Exception Thrown if the Inputs are Invalid
	 */

	public QR (
		final double[][] aadblQ,
		final double[][] aadblR)
		throws java.lang.Exception
	{
		if (null == (_aadblQ = aadblQ) || null == (_aadblR = aadblR))
			throw new java.lang.Exception ("QR ctr: Invalid Inputs!");

		int iSize = _aadblQ.length;

		if (0 == iSize || null == _aadblQ[0] || iSize != _aadblQ[0].length || iSize != _aadblR.length || null
			== _aadblR[0] || iSize != _aadblR[0].length)
			throw new java.lang.Exception ("QR ctr: Invalid Inputs!");
	}

	/**
	 * Retrieve Q
	 * 
	 * @return Q
	 */

	public double[][] q()
	{
		return _aadblQ;
	}

	/**
	 * Retrieve R
	 * 
	 * @return R
	 */

	public double[][] r()
	{
		return _aadblR;
	}
}
