
package org.drip.dynamics.lmm;

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
 * ShortRateProcess implements the Short Rate Process defined in the LIBOR Market Model. The References are:
 * 
 *  1) Goldys, B., M. Musiela, and D. Sondermann (1994): Log-normality of Rates and Term Structure Models,
 *  	The University of New South Wales.
 * 
 *  2) Musiela, M. (1994): Nominal Annual Rates and Log-normal Volatility Structure, The University of New
 *   	South Wales.
 * 
 * 	3) Brace, A., D. Gatarek, and M. Musiela (1997): The Market Model of Interest Rate Dynamics, Mathematical
 * 		Finance 7 (2), 127-155.
 *
 * @author Lakshmi Krishnamurthy
 */

public class ShortRateProcess {
	private double _dblSpotDate = java.lang.Double.NaN;
	private org.drip.function.stochastic.R1R1ToR1 _funcR1R1ToR1 = null;

	/**
	 * ShortRateProcess Constructor
	 * 
	 * @param dblSpotDate The Spot Date
	 * @param funcR1R1ToR1 The Stochastic Short Rate Function
	 * 
	 * @throws java.lang.Exception Thrown if the Inputs are Invalid
	 */

	public ShortRateProcess (
		final double dblSpotDate,
		final org.drip.function.stochastic.R1R1ToR1 funcR1R1ToR1)
		throws java.lang.Exception
	{
		if (!org.drip.quant.common.NumberUtil.IsValid (_dblSpotDate = dblSpotDate) || null == (_funcR1R1ToR1
			= funcR1R1ToR1))
			throw new java.lang.Exception ("ShortRateProcess ctr: Invalid Inputs");
	}

	/**
	 * Retrieve the Spot Date
	 * 
	 * @return The Spot Date
	 */

	public double spotDate()
	{
		return _dblSpotDate;
	}

	/**
	 * Retrieve the Stochastic Short Rate Function
	 * 
	 * @return The Stochastic Short Rate Function
	 */

	public org.drip.function.stochastic.R1R1ToR1 stochasticShortRateFunction()
	{
		return _funcR1R1ToR1;
	}

	/**
	 * Retrieve the Continuously Re-invested Accruing Bank Account
	 * 
	 * @param dblMaturityDate The Maturity Date
	 * 
	 * @return The Continuously Re-invested Accruing Bank Account
	 * 
	 * @throws java.lang.Exception Thrown if the Inputs are Invalid
	 */

	public double continuouslyReinvestedAccrualFactor (
		final double dblMaturityDate)
		throws java.lang.Exception
	{
		if (!org.drip.quant.common.NumberUtil.IsValid (dblMaturityDate) || dblMaturityDate <= _dblSpotDate)
			throw new java.lang.Exception
				("ShortRateProcess::continuouslyReinvestedAccrualFactor => Invalid Maturity Date");

		return java.lang.Math.exp (_funcR1R1ToR1.integralRealization (0., dblMaturityDate - _dblSpotDate));
	}
}
