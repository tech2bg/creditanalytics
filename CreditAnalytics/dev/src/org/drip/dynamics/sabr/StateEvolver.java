
package org.drip.dynamics.sabr;

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
 * StateEvolver provides the SABR Stochastic Volatility Evolution Dynamics.
 *
 * @author Lakshmi Krishnamurthy
 */

public class StateEvolver {
	private double _dblRho = java.lang.Double.NaN;
	private double _dblBeta = java.lang.Double.NaN;
	private double _dblAlpha = java.lang.Double.NaN;
	private org.drip.sequence.random.UnivariateSequenceGenerator _usgForwardRate = null;
	private org.drip.sequence.random.UnivariateSequenceGenerator _usgForwardRateVolatility = null;

	/**
	 * SABR StateEvolver Constructor
	 * 
	 * @param dblAlpha SABR Alpha
	 * @param dblBeta SABR Beta
	 * @param dblRho SABR Rho
	 * @param usgForwardRate The Forward Rate Univariate Sequence Generator
	 * @param usgForwardRateVolatility The Forward Rate Volatility Univariate Sequence Generator
	 * 
	 * @throws java.lang.Exception Thrown if the Inputs are Invalid
	 */

	public StateEvolver (
		final double dblAlpha,
		final double dblBeta,
		final double dblRho,
		final org.drip.sequence.random.UnivariateSequenceGenerator usgForwardRate,
		final org.drip.sequence.random.UnivariateSequenceGenerator usgForwardRateVolatility)
		throws java.lang.Exception
	{
		if (!org.drip.quant.common.NumberUtil.IsValid (_dblAlpha = dblAlpha) ||
			!org.drip.quant.common.NumberUtil.IsValid (_dblBeta = dblBeta) ||
			!org.drip.quant.common.NumberUtil.IsValid (_dblRho = dblRho) || null == (_usgForwardRate =
				usgForwardRate) || null == (_usgForwardRateVolatility = usgForwardRateVolatility))
			throw new java.lang.Exception ("StateEvolver ctr => Invalid Inputs");
	}

	/**
	 * Retrieve SABR Alpha
	 * 
	 * @return SABR Alpha
	 */

	public double alpha()
	{
		return _dblAlpha;
	}

	/**
	 * Retrieve SABR Beta
	 * 
	 * @return SABR Beta
	 */

	public double beta()
	{
		return _dblBeta;
	}

	/**
	 * Retrieve SABR Rho
	 * 
	 * @return SABR Rho
	 */

	public double rho()
	{
		return _dblRho;
	}

	/**
	 * The Forward Rate Univariate Random Variable Generator Sequence
	 * 
	 * @return The Forward Rate Univariate Random Variable Generator Sequence
	 */

	public org.drip.sequence.random.UnivariateSequenceGenerator usgForwardRate()
	{
		return _usgForwardRate;
	}

	/**
	 * The Forward Rate Volatility Univariate Random Variable Generator Sequence
	 * 
	 * @return The Forward Rate Volatility Univariate Random Variable Generator Sequence
	 */

	public org.drip.sequence.random.UnivariateSequenceGenerator usgForwardRateVolatility()
	{
		return _usgForwardRateVolatility;
	}
}
