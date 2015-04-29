
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
 * BGMForwardTimeSnap contains the Forward Curve Rates Latent State Quantifiers traced from the Evolution
 *  Increment of the LIBOR Forward Rate as formulated in:
 * 
 * 	Brace, A., D. Gatarek, and M. Musiela (1997): The Market Model of Interest Rate Dynamics, Mathematical
 * 		Finance 7 (2), 127-155.
 *
 * @author Lakshmi Krishnamurthy
 */

public class BGMForwardTimeSnap {
	private double _dblLIBOR = java.lang.Double.NaN;
	private double _dblSpotRate = java.lang.Double.NaN;
	private double _dblDiscountFactor = java.lang.Double.NaN;
	private double _dblLIBORIncrement = java.lang.Double.NaN;
	private double _dblSpotRateIncrement = java.lang.Double.NaN;
	private double _dblContinuousForwardRate = java.lang.Double.NaN;
	private double _dblDiscountFactorIncrement = java.lang.Double.NaN;
	private double _dblLognormalLIBORVolatility = java.lang.Double.NaN;
	private double _dblContinuousForwardRateIncrement = java.lang.Double.NaN;
	private double _dblContinuouslyCompoundedForwardVolatility = java.lang.Double.NaN;

	/**
	 * BGMForwardTimeSnap Constructor
	 * 
	 * @param dblLIBOR The LIBOR Rate
	 * @param dblLIBORIncrement The LIBOR Rate Increment
	 * @param dblContinuousForwardRate The Continuously Compounded Forward Rate
	 * @param dblContinuousForwardRateIncrement The Continuously Compounded Forward Rate Increment
	 * @param dblSpotRate The Spot Rate
	 * @param dblSpotRateIncrement The Spot Rate Increment
	 * @param dblDiscountFactor The Discount Factor
	 * @param dblDiscountFactorIncrement The Discount Factor Increment
	 * @param dblLognormalLIBORVolatility The Log-normal LIBOR Rate Volatility
	 * @param dblContinuouslyCompoundedForwardVolatility The Continuously Compounded Forward Rate Volatility
	 * 
	 * @throws java.lang.Exception Thrown if the Inputs are Invalid
	 */

	public BGMForwardTimeSnap (
		final double dblLIBOR,
		final double dblLIBORIncrement,
		final double dblContinuousForwardRate,
		final double dblContinuousForwardRateIncrement,
		final double dblSpotRate,
		final double dblSpotRateIncrement,
		final double dblDiscountFactor,
		final double dblDiscountFactorIncrement,
		final double dblLognormalLIBORVolatility,
		final double dblContinuouslyCompoundedForwardVolatility)
		throws java.lang.Exception
	{
		if (!org.drip.quant.common.NumberUtil.IsValid (_dblLIBOR = dblLIBOR) ||
			!org.drip.quant.common.NumberUtil.IsValid (_dblLIBORIncrement = dblLIBORIncrement) ||
				!org.drip.quant.common.NumberUtil.IsValid (_dblContinuousForwardRate =
					dblContinuousForwardRate) || !org.drip.quant.common.NumberUtil.IsValid
						(_dblContinuousForwardRateIncrement = dblContinuousForwardRateIncrement) ||
							!org.drip.quant.common.NumberUtil.IsValid (_dblSpotRate = dblSpotRate) ||
								!org.drip.quant.common.NumberUtil.IsValid (_dblSpotRateIncrement =
									dblSpotRateIncrement) || !org.drip.quant.common.NumberUtil.IsValid
										(_dblDiscountFactor = dblDiscountFactor) ||
											!org.drip.quant.common.NumberUtil.IsValid
												(_dblDiscountFactorIncrement = dblDiscountFactorIncrement) ||
													!org.drip.quant.common.NumberUtil.IsValid
														(_dblLognormalLIBORVolatility =
															dblLognormalLIBORVolatility) ||
																!org.drip.quant.common.NumberUtil.IsValid
																	(_dblContinuouslyCompoundedForwardVolatility
			= dblContinuouslyCompoundedForwardVolatility))
			throw new java.lang.Exception ("BGMForwardTimeSnap ctr: Invalid Inputs");
	}

	/**
	 * Retrieve the LIBOR Rate
	 * 
	 * @return The LIBOR Rate
	 */

	public double libor()
	{
		return _dblLIBOR;
	}

	/**
	 * Retrieve the LIBOR Rate Increment
	 * 
	 * @return The LIBOR Rate Increment
	 */

	public double liborIncrement()
	{
		return _dblLIBORIncrement;
	}

	/**
	 * Retrieve the Continuously Compounded Forward Rate
	 * 
	 * @return The Continuously Compounded Forward Rate
	 */

	public double continuousForwardRate()
	{
		return _dblContinuousForwardRate;
	}

	/**
	 * Retrieve the Continuously Compounded Forward Rate Increment
	 * 
	 * @return The Continuously Compounded Forward Rate Increment
	 */

	public double continuousForwardRateIncrement()
	{
		return _dblContinuousForwardRateIncrement;
	}

	/**
	 * Retrieve the Spot Rate
	 * 
	 * @return The Spot Rate
	 */

	public double spotRate()
	{
		return _dblSpotRate;
	}

	/**
	 * Retrieve the Spot Rate Increment
	 * 
	 * @return The Spot Rate Increment
	 */

	public double spotRateIncrement()
	{
		return _dblSpotRateIncrement;
	}

	/**
	 * Retrieve the Discount Factor
	 * 
	 * @return The Discount Factor
	 */

	public double discountFactor()
	{
		return _dblDiscountFactor;
	}

	/**
	 * Retrieve the Discount Factor Increment
	 * 
	 * @return The Discount Factor Increment
	 */

	public double discountFactorIncrement()
	{
		return _dblDiscountFactorIncrement;
	}

	/**
	 * Retrieve the Log-normal LIBOR Volatility
	 * 
	 * @return The Log-normal LIBOR Volatility
	 */

	public double lognormalLIBORVolatility()
	{
		return _dblLognormalLIBORVolatility;
	}

	/**
	 * Retrieve the Continuously Compounded Forward Rate Volatility
	 * 
	 * @return The Continuously Compounded Forward Rate Volatility
	 */

	public double continuouslyCompoundedForwardVolatility()
	{
		return _dblContinuouslyCompoundedForwardVolatility;
	}

	@Override public java.lang.String toString()
	{
		return org.drip.quant.common.FormatUtil.FormatDouble (_dblLIBOR, 1, 2, 100.) + "% | " +
			org.drip.quant.common.FormatUtil.FormatDouble (_dblLIBORIncrement, 2, 2, 10000.) + " | " +
				org.drip.quant.common.FormatUtil.FormatDouble (_dblContinuousForwardRate, 1, 2, 100.) +
					"% | " + org.drip.quant.common.FormatUtil.FormatDouble
						(_dblContinuousForwardRateIncrement, 2, 2, 10000.) + " | " +
							org.drip.quant.common.FormatUtil.FormatDouble (_dblSpotRate, 1, 2, 100.) +
								" % | " + org.drip.quant.common.FormatUtil.FormatDouble
									(_dblSpotRateIncrement, 2, 2, 10000.) + " | " +
										org.drip.quant.common.FormatUtil.FormatDouble (_dblDiscountFactor, 1,
											4, 1.) + " | " + org.drip.quant.common.FormatUtil.FormatDouble
												(_dblDiscountFactorIncrement, 2, 2, 10000.) + " ||";
	}
}
