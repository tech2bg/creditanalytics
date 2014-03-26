
package org.drip.state.curve;

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
 * BasisSplineMarketSurface implements the Market surface that holds the latent state's Dynamics parameters.
 *
 * @author Lakshmi Krishnamurthy
 */

public class BasisSplineMarketSurface extends org.drip.analytics.definition.MarketSurface {
	private org.drip.spline.multidimensional.WireSurfaceStretch _wss = null;
	private org.drip.param.valuation.CollateralizationParams _collatParams = null;

	/**
	 * BasisSplineMarketSurface Constructor
	 * 
	 * @param dblEpochDate The Starting Date
	 * @param strName Name of the Surface
	 * @param strCurrency The Currency
	 * @param wss Wire Surface Stretch Instance
	 * @param collatParams Collateral Parameters
	 * 
	 * @throws java.lang.Exception Thrown if the Inputs are Invalid
	 */

	public BasisSplineMarketSurface (
		final double dblEpochDate,
		final java.lang.String strName,
		final java.lang.String strCurrency,
		org.drip.spline.multidimensional.WireSurfaceStretch wss,
		final org.drip.param.valuation.CollateralizationParams collatParams)
		throws java.lang.Exception
	{
		super (dblEpochDate, strName, strCurrency);

		_wss = wss;
		_collatParams = collatParams;
	}

	@Override public org.drip.param.valuation.CollateralizationParams collateralParams()
	{
		return _collatParams;
	}

	@Override public byte[] serialize()
	{
		return null;
	}

	@Override public org.drip.service.stream.Serializer deserialize (
		final byte[] ab)
	{
		return null;
	}

	@Override public double node (
		final double dblStrike,
		final double dblDate)
		throws java.lang.Exception
	{
		return _wss.responseValue (dblStrike, dblDate);
	}
}
