
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
 * CreditManifestMeasureTweak contains the place holder for the credit curve scenario tweak parameters: in
 *  addition to the ResponseValueTweakParams fields, this exposes the calibration manifest measure, the curve
 *  node, and the nodal calibration type (entire curve/flat or a given tenor point).
 *
 * @author Lakshmi Krishnamurthy
 */

public class CreditManifestMeasureTweak extends ResponseValueTweakParams {

	/**
	 * Tweak Parameter Type of Quote
	 */

	public static final java.lang.String CREDIT_TWEAK_NODE_PARAM_QUOTE = "Quote";

	/**
	 * Tweak Parameter Type of Recovery
	 */

	public static final java.lang.String CREDIT_TWEAK_NODE_PARAM_RECOVERY = "Recovery";

	/**
	 * Tweak Measure Type of Quote
	 */

	public static final java.lang.String CREDIT_TWEAK_NODE_MEASURE_QUOTE = "Quote";

	/**
	 * Tweak Measure Type of Hazard
	 */

	public static final java.lang.String CREDIT_TWEAK_NODE_MEASURE_HAZARD = "Hazard";

	private boolean _bSingleNodeCalib = false;
	private java.lang.String _strTweakParamType = "";
	private java.lang.String _strTweakMeasureType = "";

	/**
	 * CreditManifestMeasureTweak constructor
	 * 
	 * @param strTweakParamType Node Tweak Parameter Type
	 * @param strTweakMeasureType Node Tweak Measure Type
	 * @param iTweakNode Node to be tweaked - Set to NODE_FLAT_TWEAK for flat curve tweak
	 * @param bIsTweakProportional True => Tweak is proportional, False => parallel
	 * @param dblTweakAmount Amount to be tweaked - proportional tweaks are represented as percent, parallel
	 * 			tweaks are absolute numbers
	 * @param bSingleNodeCalib Flat Calibration using a single node?
	 */

	public CreditManifestMeasureTweak (
		final java.lang.String strTweakParamType,
		final java.lang.String strTweakMeasureType,
		final int iTweakNode,
		final boolean bIsTweakProportional,
		final double dblTweakAmount,
		final boolean bSingleNodeCalib)
		throws java.lang.Exception
	{
		super (iTweakNode, bIsTweakProportional, dblTweakAmount);

		if (null == (_strTweakParamType = strTweakParamType) ||
			!CREDIT_TWEAK_NODE_PARAM_QUOTE.equalsIgnoreCase (_strTweakParamType) ||
				!CREDIT_TWEAK_NODE_PARAM_QUOTE.equalsIgnoreCase (_strTweakParamType))
			throw new java.lang.Exception
				("CreditManifestMeasureTweak ctr => Invalid Tweak Parameter Type!");

		if (null == (_strTweakMeasureType = strTweakMeasureType) ||
			!CREDIT_TWEAK_NODE_PARAM_QUOTE.equalsIgnoreCase (_strTweakMeasureType) ||
				!CREDIT_TWEAK_NODE_PARAM_QUOTE.equalsIgnoreCase (_strTweakMeasureType))
			throw new java.lang.Exception ("CreditManifestMeasureTweak ctr => Invalid Tweak Measure Type!");

		_bSingleNodeCalib = bSingleNodeCalib;
	}

	/**
	 * Single Node Calibration Flag
	 * 
	 * @return TRUE => Turn on Single Node Calibration
	 */

	public boolean singleNodeCalib()
	{
		return _bSingleNodeCalib;
	}

	/**
	 * Retrieve the Tweak Parameter Type
	 * 
	 * @return The Tweak Parameter Type
	 */

	public java.lang.String tweakParamType()
	{
		return _strTweakParamType;
	}

	/**
	 * Retrieve the Tweak Measure Type
	 * 
	 * @return The Tweak Measure Type
	 */

	public java.lang.String tweakMeasureType()
	{
		return _strTweakMeasureType;
	}
}
