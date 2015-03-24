
package org.drip.dynamics.hullwhite;

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
 * TrinomialTreeTransitionMetrics records the Transition Metrics associated with Node-to-Node Evolution of
 * 	the Instantaneous Short Rate using the Hull-White Model Trinomial Tree.
 *
 * @author Lakshmi Krishnamurthy
 */

public class TrinomialTreeTransitionMetrics {
	private long _lTreeTimeIndex = -1L;
	private long _lTreeStochasticBaseIndex = -1L;
	private long _lTreeStochasticDisplacementIndex = -1L;
	private double _dblXVariance = java.lang.Double.NaN;
	private double _dblInitialDate = java.lang.Double.NaN;
	private double _dblTerminalDate = java.lang.Double.NaN;
	private double _dblTerminalAlpha = java.lang.Double.NaN;
	private double _dblProbabilityUp = java.lang.Double.NaN;
	private double _dblProbabilityDown = java.lang.Double.NaN;
	private double _dblProbabilityStay = java.lang.Double.NaN;
	private double _dblXStochasticShift = java.lang.Double.NaN;
	private double _dblExpectedTerminalX = java.lang.Double.NaN;

	/**
	 * TrinomialTreeTransitionMetrics Constructor
	 * 
	 * @param dblInitialDate The Initial Date
	 * @param dblTerminalDate The Terminal/Final Date
	 * @param lTreeTimeIndex The Tree Time Index
	 * @param lTreeStochasticBaseIndex The Tree Stochastic Base Index
	 * @param dblExpectedTerminalX Expectation of the Final/Terminal Value for X
	 * @param dblXVariance Variance of X
	 * @param dblTerminalAlpha The Final/Terminal Alpha
	 * 
	 * @throws java.lang.Exception Thrown if the Inputs are Invalid
	 */

	public TrinomialTreeTransitionMetrics (
		final double dblInitialDate,
		final double dblTerminalDate,
		final long lTreeTimeIndex,
		final long lTreeStochasticBaseIndex,
		final double dblExpectedTerminalX,
		final double dblXVariance,
		final double dblTerminalAlpha)
		throws java.lang.Exception
	{
		if (!org.drip.quant.common.NumberUtil.IsValid (_dblInitialDate = dblInitialDate) ||
			!org.drip.quant.common.NumberUtil.IsValid (_dblTerminalDate = dblTerminalDate) || 0 >
				(_lTreeTimeIndex = lTreeTimeIndex) || !org.drip.quant.common.NumberUtil.IsValid
					(_dblExpectedTerminalX = dblExpectedTerminalX) ||
						!org.drip.quant.common.NumberUtil.IsValid (_dblXVariance = dblXVariance) ||
							!org.drip.quant.common.NumberUtil.IsValid (_dblTerminalAlpha = dblTerminalAlpha))
			throw new java.lang.Exception ("TrinomialTreeTransitionMetrics ctr: Invalid Inputs");

		_dblXStochasticShift = java.lang.Math.sqrt (_dblXVariance * 3.);

		_lTreeStochasticDisplacementIndex = java.lang.Math.round (_dblExpectedTerminalX /
			_dblXStochasticShift);

		_lTreeStochasticBaseIndex = lTreeStochasticBaseIndex;
		double dblEta = _dblExpectedTerminalX - _lTreeStochasticDisplacementIndex * _dblXStochasticShift;
		_dblProbabilityStay = (2. / 3.) - (dblEta * dblEta / (3. * _dblXVariance));
		_dblProbabilityDown = (1. / 6.) + (dblEta * dblEta / (6. * _dblXVariance)) - (0.5 * dblEta /
			_dblXStochasticShift);
		_dblProbabilityUp = (1. / 6.) + (dblEta * dblEta / (6. * _dblXVariance)) + (0.5 * dblEta /
			_dblXStochasticShift);
	}

	/**
	 * Retrieve the Initial Date
	 * 
	 * @return The Initial Date
	 */

	public double initialDate()
	{
		return _dblInitialDate;
	}

	/**
	 * Retrieve the Terminal Date
	 * 
	 * @return The Terminal Date
	 */

	public double terminalDate()
	{
		return _dblTerminalDate;
	}

	/**
	 * Retrieve the Tree Time Index
	 * 
	 * @return The Tree Time Index
	 */

	public long treeTimeIndex()
	{
		return _lTreeTimeIndex;
	}

	/**
	 * Retrieve the Expected Final/Terminal Value for X
	 * 
	 * @return The Expected Final/Terminal Value for X
	 */

	public double expectedTerminalX()
	{
		return _dblExpectedTerminalX;
	}

	/**
	 * Retrieve the Variance in the Final Value of X
	 * 
	 * @return The Variance in the Final Value of X
	 */

	public double xVariance()
	{
		return _dblXVariance;
	}

	/**
	 * Retrieve the Stochastic Shift of X
	 * 
	 * @return The Stochastic Shift of X
	 */

	public double xStochasticShift()
	{
		return _dblXStochasticShift;
	}

	/**
	 * Retrieve the Tree Stochastic Displacement Index
	 * 
	 * @return The Tree Stochastic Displacement Index
	 */

	public long treeStochasticDisplacementIndex()
	{
		return _lTreeStochasticDisplacementIndex;
	}

	/**
	 * Retrieve the Probability of the Up Stochastic Shift
	 * 
	 * @return Probability of the Up Stochastic Shift
	 */

	public double probabilityUp()
	{
		return _dblProbabilityUp;
	}

	/**
	 * Retrieve the Probability of the Down Stochastic Shift
	 * 
	 * @return Probability of the Down Stochastic Shift
	 */

	public double probabilityDown()
	{
		return _dblProbabilityDown;
	}

	/**
	 * Retrieve the Probability of the No Shift
	 * 
	 * @return Probability of the No Shift
	 */

	public double probabilityStay()
	{
		return _dblProbabilityStay;
	}

	/**
	 * Retrieve the "Up" Value for X
	 * 
	 * @return The "Up" Value for X
	 */

	public double xUp()
	{
		return (_lTreeStochasticDisplacementIndex + 1) * _dblXStochasticShift;
	}

	/**
	 * Retrieve the "Down" Value for X
	 * 
	 * @return The "Down" Value for X
	 */

	public double xDown()
	{
		return (_lTreeStochasticDisplacementIndex - 1) * _dblXStochasticShift;
	}

	/**
	 * Retrieve the Final/Terminal Alpha
	 * 
	 * @return The Final/Terminal Alpha
	 */

	public double terminalAlpha()
	{
		return _dblTerminalAlpha;
	}

	/**
	 * Retrieve the "Up" Node Metrics
	 * 
	 * @return The "Up" Node Metrics
	 */

	public org.drip.dynamics.hullwhite.TrinomialTreeNodeMetrics upNodeMetrics()
	{
		try {
			return new org.drip.dynamics.hullwhite.TrinomialTreeNodeMetrics (_lTreeTimeIndex,
				_lTreeStochasticBaseIndex + 1, (_lTreeStochasticDisplacementIndex + 1) *
					_dblXStochasticShift, _dblTerminalAlpha);
		} catch (java.lang.Exception e) {
			e.printStackTrace();
		}

		return null;
	}

	/**
	 * Retrieve the "Down" Node Metrics
	 * 
	 * @return The "Down" Node Metrics
	 */

	public org.drip.dynamics.hullwhite.TrinomialTreeNodeMetrics downNodeMetrics()
	{
		try {
			return new org.drip.dynamics.hullwhite.TrinomialTreeNodeMetrics (_lTreeTimeIndex,
				_lTreeStochasticBaseIndex - 1, (_lTreeStochasticDisplacementIndex - 1) *
					_dblXStochasticShift, _dblTerminalAlpha);
		} catch (java.lang.Exception e) {
			e.printStackTrace();
		}

		return null;
	}

	/**
	 * Retrieve the "Stay" Node Metrics
	 * 
	 * @return The "Stay" Node Metrics
	 */

	public org.drip.dynamics.hullwhite.TrinomialTreeNodeMetrics stayNodeMetrics()
	{
		try {
			return new org.drip.dynamics.hullwhite.TrinomialTreeNodeMetrics (_lTreeTimeIndex,
				_lTreeStochasticBaseIndex, _lTreeStochasticDisplacementIndex * _dblXStochasticShift,
					_dblTerminalAlpha);
		} catch (java.lang.Exception e) {
			e.printStackTrace();
		}

		return null;
	}
}
