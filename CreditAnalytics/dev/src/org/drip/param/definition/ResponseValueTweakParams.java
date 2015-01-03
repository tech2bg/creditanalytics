
package org.drip.param.definition;

/*
 * -*- mode: java; tab-width: 4; indent-tabs-mode: nil; c-basic-offset: 4 -*-
 */

/*!
 * Copyright (C) 2015 Lakshmi Krishnamurthy
 * Copyright (C) 2014 Lakshmi Krishnamurthy
 * Copyright (C) 2013 Lakshmi Krishnamurthy
 * Copyright (C) 2012 Lakshmi Krishnamurthy
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
 * ResponseValueTweakParams contains the place holder for the scenario tweak parameters, for either a specific curve
 *  node, or the entire curve (flat). Parameter bumps can be parallel or proportional.
 *
 * @author Lakshmi Krishnamurthy
 */

public class ResponseValueTweakParams {

	/**
	 * Flat Manifest Measure Tweak Mode
	 */

	public static final int MANIFEST_MEASURE_FLAT_TWEAK = -1;

	private boolean _bIsTweakProportional = false;
	private int _iTweakNode = MANIFEST_MEASURE_FLAT_TWEAK;
	private double _dblTweakAmount = java.lang.Double.NaN;

	/**
	 * ResponseValueTweakParams constructor
	 * 
	 * @param iTweakNode Node to be tweaked - Set to NODE_FLAT_TWEAK for flat curve tweak
	 * @param bIsTweakProportional True => Tweak is proportional, False => parallel
	 * @param dblTweakAmount Amount to be tweaked - proportional tweaks are represented as percent, parallel
	 * 	tweaks are absolute numbers
	 * 
	 * @throws java.lang.Exception Thrown if Inputs are invalid
	 */

	public ResponseValueTweakParams (
		final int iTweakNode,
		final boolean bIsTweakProportional,
		final double dblTweakAmount)
		throws java.lang.Exception
	{
		if (!org.drip.quant.common.NumberUtil.IsValid (_dblTweakAmount = dblTweakAmount))
			throw new java.lang.Exception ("ResponseValueTweakParams ctr => Invalid Inputs");

		_iTweakNode = iTweakNode;
		_bIsTweakProportional = bIsTweakProportional;
	}

	/**
	 * Index of the Node to be tweaked
	 * 
	 * @return Index of the Node to be tweaked
	 */

	public int node()
	{
		return _iTweakNode;
	}

	/**
	 * Amount to be tweaked by
	 * 
	 * @return Amount to be tweaked by
	 */

	public double amount()
	{
		return _dblTweakAmount;
	}

	/**
	 * Is the Tweak Proportional
	 * 
	 * @return TRUE => The Tweak is Proportional
	 */

	public boolean isTweakProportional()
	{
		return _bIsTweakProportional;
	}
}
