
package org.drip.state.inference;

/*
 * -*- mode: java; tab-width: 4; indent-tabs-mode: nil; c-basic-offset: 4 -*-
 */

/*!
 * Copyright (C) 2014 Lakshmi Krishnamurthy
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
 * LatentStateStretchSpec carries the Latent State Segment Sequence corresponding to the calibratable
 * 	Stretch.
 * 
 * @author Lakshmi Krishnamurthy
 */

public class LatentStateStretchSpec {
	private java.lang.String _strName = "";
	private org.drip.state.inference.LatentStateSegmentSpec[] _aLSSS = null;

	/**
	 * LatentStateStretchSpec constructor
	 * 
	 * @param strName Stretch Name
	 * @param aLSSS The Latent State Segment Product/Manifest Measure Sequence contained in the Stretch
	 * 
	 * @throws java.lang.Exception Thrown if Inputs are invalid
	 */

	public LatentStateStretchSpec (
		final java.lang.String strName,
		final org.drip.state.inference.LatentStateSegmentSpec[] aLSSS)
		throws java.lang.Exception
	{
		if (null == strName || strName.isEmpty() || null == (_aLSSS = aLSSS) || 0 == _aLSSS.length)
			throw new java.lang.Exception ("LatentStateStretchSpec ctr: Invalid Inputs");
	}

	/**
	 * Retrieve the name of the LatentStateStretchSpec Instance
	 * 
	 * @return Name of the LatentStateStretchSpec Instance
	 */

	public java.lang.String name()
	{
		return _strName;
	}

	/**
	 * Retrieve the Array of the Latent State Segment Product/Manifest Measure Sequence
	 * 
	 * @return The Array of the Latent State Segment Product/Manifest Measure Sequence
	 */

	public org.drip.state.inference.LatentStateSegmentSpec[] segmentSpec()
	{
		return _aLSSS;
	}
}
