
package org.drip.function.deterministic1D;

/*
 * -*- mode: java; tab-width: 4; indent-tabs-mode: nil; c-basic-offset: 4 -*-
 */

/*!
 * Copyright (C) 2015 Lakshmi Krishnamurthy
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
 * SABRLIBORCapVolatility implements the Deterministic, Non-local Cap Volatility Scheme detailed in:
 * 
 * 	- Rebonato, R., K. McKay, and R. White (2009): The SABR/LIBOR Market Model: Pricing, Calibration, and
 * 		Hedging for Complex Interest-Rate Derivatives, John Wiley and Sons.
 * 
 * @author Lakshmi Krishnamurthy
 */

public class SABRLIBORCapVolatility extends org.drip.function.deterministic.AbstractUnivariate {
	private double _dblA = java.lang.Double.NaN;
	private double _dblB = java.lang.Double.NaN;
	private double _dblC = java.lang.Double.NaN;
	private double _dblD = java.lang.Double.NaN;
	private double _dblEpoch = java.lang.Double.NaN;

	/**
	 * SABRLIBORCapVolatility Constructor
	 * 
	 * @param dblEpochDate Epoch
	 * @param dblA A
	 * @param dblB B
	 * @param dblC C
	 * @param dblD D
	 * 
	 * @throws java.lang.Exception Thrown if Inputs are Invalid
	 */

	public SABRLIBORCapVolatility (
		final double dblEpoch,
		final double dblA,
		final double dblB,
		final double dblC,
		final double dblD)
		throws java.lang.Exception
	{
		super (null);

		if (!org.drip.quant.common.NumberUtil.IsValid (_dblEpoch = dblEpoch) ||
				!org.drip.quant.common.NumberUtil.IsValid (_dblA = dblA) ||
					!org.drip.quant.common.NumberUtil.IsValid (_dblB = dblB) ||
						!org.drip.quant.common.NumberUtil.IsValid (_dblC = dblC) ||
							!org.drip.quant.common.NumberUtil.IsValid (_dblD = dblD))
			throw new java.lang.Exception ("SABRLIBORCapVolatility ctr: Invalid Inputs");
	}

	@Override public double evaluate (
		final double dblVariate)
		throws java.lang.Exception
	{
		if (!org.drip.quant.common.NumberUtil.IsValid (dblVariate))
			throw new java.lang.Exception ("SABRLIBORCapVolatility::evaluate => Invalid Inputs");

		double dblDateGap = dblVariate - _dblEpoch;

		return (_dblB * dblDateGap + _dblA) * java.lang.Math.exp (-1. * _dblC * dblDateGap) + _dblD;
	}

	/**
	 * Return "A"
	 * 
	 * @return "A"
	 */

	public double A()
	{
		return _dblA;
	}

	/**
	 * Return "B"
	 * 
	 * @return "B"
	 */

	public double B()
	{
		return _dblB;
	}

	/**
	 * Return "C"
	 * 
	 * @return "C"
	 */

	public double C()
	{
		return _dblC;
	}

	/**
	 * Return "D"
	 * 
	 * @return "D"
	 */

	public double D()
	{
		return _dblD;
	}

	/**
	 * Return the Epoch
	 * 
	 * @return The Epoch
	 */

	public double epoch()
	{
		return _dblEpoch;
	}
}
