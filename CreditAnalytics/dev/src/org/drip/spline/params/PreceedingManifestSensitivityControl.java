
package org.drip.spline.params;

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
 * PreceedingManifestSensitivityControl provides the control parameters that determine the behavior of
 * 	non-local manifest sensitivity.
 *
 * @author Lakshmi Krishnamurthy
 */

public class PreceedingManifestSensitivityControl {
	private boolean _bImpactFade = false;
	private int _iCkDBasisCoeffDPreceedingManifest = 0;
	private org.drip.spline.segment.BasisEvaluator _be = null;

	/**
	 * PreceedingManifestSensitivityControl constructor
	 * 
	 * @param bImpactFade TRUE => Fade the Manifest Sensitivity Impact; FALSE => Retain it
	 * @param iCkDBasisCoeffDPreceedingManifest Ck of DBasisCoeffDPreceedingManifest
	 * @param be Basis Evaluator Instance
	 * 
	 * @throws java.lang.Exception Thrown if Inputs are invalid
	 */

	public PreceedingManifestSensitivityControl (
		final boolean bImpactFade,
		final int iCkDBasisCoeffDPreceedingManifest,
		final org.drip.spline.segment.BasisEvaluator be)
		throws java.lang.Exception
	{
		if (0 > (_iCkDBasisCoeffDPreceedingManifest = iCkDBasisCoeffDPreceedingManifest))
			throw new java.lang.Exception ("PreceedingManifestSensitivityControl ctr: Invalid Inputs");

		_be = be;
		_bImpactFade = bImpactFade;
	}

	/**
	 * Retrieve the Ck of DBasisCoeffDPreceedingManifest
	 * 
	 * @return Ck of DBasisCoeffDPreceedingManifest
	 */

	public int Ck()
	{
		return _iCkDBasisCoeffDPreceedingManifest;
	}

	/**
	 * Retrieve the Basis Evaluator Instance
	 * 
	 * @return The Basis Evaluator Instance
	 */

	public org.drip.spline.segment.BasisEvaluator basisEvaluator()
	{
		return _be;
	}

	/**
	 * Retrieve the Preceeding Manifest Measure Impact Flag
	 * 
	 * @return The Preceeding Manifest Measure Impact Flag
	 */

	public boolean impactFade()
	{
		return _bImpactFade;
	}
}
