
package org.drip.state.dynamics;

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
 * QMSnapshot contains the Instantaneous Snapshot of the Evolving Discount Latent State Quantification
 * 	Metrics.
 *
 * @author Lakshmi Krishnamurthy
 */

public class QMSnapshot {
	private double _dblPrice = java.lang.Double.NaN;
	private double _dblShortRate = java.lang.Double.NaN;
	private double _dblPriceIncrement = java.lang.Double.NaN;
	private double _dblLIBORForwardRate = java.lang.Double.NaN;
	private double _dblShortRateIncrement = java.lang.Double.NaN;
	private double _dblCompoundedShortRate = java.lang.Double.NaN;
	private double _dblShiftedLIBORForwardRate = java.lang.Double.NaN;
	private double _dblInstantaneousForwardRate = java.lang.Double.NaN;
	private double _dblLIBORForwardRateIncrement = java.lang.Double.NaN;
	private double _dblCompoundedShortRateIncrement = java.lang.Double.NaN;
	private double _dblShiftedLIBORForwardRateIncrement = java.lang.Double.NaN;
	private double _dblInstantaneousForwardRateIncrement = java.lang.Double.NaN;

	/**
	 * QMSnapshot Constructor
	 * 
	 * @param dblInstantaneousForwardRate The Instantaneous Forward Rate
	 * @param dblInstantaneousForwardRateIncrement The Instantaneous Forward Rate Increment
	 * @param dblLIBORForwardRate The LIBOR Forward Rate
	 * @param dblLIBORForwardRateIncrement The LIBOR Forward Rate Increment
	 * @param dblShiftedLIBORForwardRate The Shifted LIBOR Forward Rate
	 * @param dblShiftedLIBORForwardRateIncrement The Shifted LIBOR Forward Rate Increment
	 * @param dblShortRate The Short Rate
	 * @param dblShortRateIncrement The Short Rate Increment
	 * @param dblCompoundedShortRate The Compounded Short Rate
	 * @param dblCompoundedShortRateIncrement The Compounded Short Rate Increment
	 * @param dblPrice The Price
	 * @param dblPriceIncrement The Price Increment
	 * 
	 * @throws java.lang.Exception Thrown if the Inputs are Invalid
	 */

	public QMSnapshot (
		final double dblInstantaneousForwardRate,
		final double dblInstantaneousForwardRateIncrement,
		final double dblLIBORForwardRate,
		final double dblLIBORForwardRateIncrement,
		final double dblShiftedLIBORForwardRate,
		final double dblShiftedLIBORForwardRateIncrement,
		final double dblShortRate,
		final double dblShortRateIncrement,
		final double dblCompoundedShortRate,
		final double dblCompoundedShortRateIncrement,
		final double dblPrice,
		final double dblPriceIncrement)
		throws java.lang.Exception
	{
		if (!org.drip.quant.common.NumberUtil.IsValid (_dblInstantaneousForwardRate =
			dblInstantaneousForwardRate) || !org.drip.quant.common.NumberUtil.IsValid
				(_dblInstantaneousForwardRateIncrement = dblInstantaneousForwardRateIncrement))
			throw new java.lang.Exception ("QMSnapshot ctr: Invalid Inputs");

		_dblLIBORForwardRate = dblLIBORForwardRate;
		_dblLIBORForwardRateIncrement = dblLIBORForwardRateIncrement;
		_dblShiftedLIBORForwardRate = dblShiftedLIBORForwardRate;
		_dblShiftedLIBORForwardRateIncrement = dblShiftedLIBORForwardRateIncrement;
		_dblShortRate = dblShortRate;
		_dblShortRateIncrement = dblShortRateIncrement;
		_dblCompoundedShortRate = dblCompoundedShortRate;
		_dblCompoundedShortRateIncrement = dblCompoundedShortRateIncrement;
		_dblPrice = dblPrice;
		_dblPriceIncrement = dblPriceIncrement;
	}

	/**
	 * Retrieve the Instantaneous Forward Rate
	 * 
	 * @return The Instantaneous Forward Rate
	 */

	public double instantaneousForwardRate()
	{
		return _dblInstantaneousForwardRate;
	}

	/**
	 * Retrieve the Instantaneous Forward Rate Increment
	 * 
	 * @return The Instantaneous Forward Rate Increment
	 */

	public double instantaneousForwardRateIncrement()
	{
		return _dblInstantaneousForwardRateIncrement;
	}

	/**
	 * Retrieve the LIBOR Forward Rate
	 * 
	 * @return The LIBOR Forward Rate
	 */

	public double liborForwardRate()
	{
		return _dblLIBORForwardRate;
	}

	/**
	 * Retrieve the LIBOR Forward Rate Increment
	 * 
	 * @return The LIBOR Forward Rate Increment
	 */

	public double liborForwardRateIncrement()
	{
		return _dblLIBORForwardRateIncrement;
	}

	/**
	 * Retrieve the Shifted LIBOR Forward Rate
	 * 
	 * @return The Shifted LIBOR Forward Rate
	 */

	public double shiftedLIBORForwardRate()
	{
		return _dblShiftedLIBORForwardRate;
	}

	/**
	 * Retrieve the Shifted LIBOR Forward Rate Increment
	 * 
	 * @return The Shifted LIBOR Forward Rate Increment
	 */

	public double shiftedLIBORForwardRateIncrement()
	{
		return _dblShiftedLIBORForwardRateIncrement;
	}

	/**
	 * Retrieve the Short Rate
	 * 
	 * @return The Short Rate
	 */

	public double shortRate()
	{
		return _dblShortRate;
	}

	/**
	 * Retrieve the Short Rate Increment
	 * 
	 * @return The Short Rate Increment
	 */

	public double shortRateIncrement()
	{
		return _dblShortRateIncrement;
	}

	/**
	 * Retrieve the Compounded Short Rate
	 * 
	 * @return The Compounded Short Rate
	 */

	public double compoundedShortRate()
	{
		return _dblCompoundedShortRate;
	}

	/**
	 * Retrieve the Compounded Short Rate Increment
	 * 
	 * @return The Compounded Short Rate Increment
	 */

	public double compoundedShortRateIncrement()
	{
		return _dblCompoundedShortRateIncrement;
	}

	/**
	 * Retrieve the Price
	 * 
	 * @return The Price
	 */

	public double price()
	{
		return _dblPrice;
	}

	/**
	 * Retrieve the Price Increment
	 * 
	 * @return The Price Increment
	 */

	public double priceIncrement()
	{
		return _dblPriceIncrement;
	}
}
