
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
 * HullWhiteTransitionMetrics records the Transition Metrics associated with Node-to-Node Evolution of the
 * 	Instantaneous Short Rate using the Hull-White Model.
 *
 * @author Lakshmi Krishnamurthy
 */

public class HullWhiteTransitionMetrics {
	private long _lStochasticTreeIndex = -1L;
	private double _dblFinalDate = java.lang.Double.NaN;
	private double _dblXVariance = java.lang.Double.NaN;
	private double _dblFinalAlpha = java.lang.Double.NaN;
	private double _dblInitialDate = java.lang.Double.NaN;
	private double _dblProbabilityUp = java.lang.Double.NaN;
	private double _dblExpectedFinalX = java.lang.Double.NaN;
	private double _dblProbabilityDown = java.lang.Double.NaN;
	private double _dblProbabilityStay = java.lang.Double.NaN;
	private double _dblXStochasticShift = java.lang.Double.NaN;

	/**
	 * HullWhiteTransitionMetrics Constructor
	 * 
	 * @param dblInitialDate The Initial Date
	 * @param dblFinalDate The Final Date
	 * @param dblExpectedFinalX Expectation of the Final Value for X
	 * @param dblXVariance Variance of X
	 * @param dblFinalAlpha The Final Alpha
	 * 
	 * @throws java.lang.Exception Thrown if the Inputs are Invalid
	 */

	public HullWhiteTransitionMetrics (
		final double dblInitialDate,
		final double dblFinalDate,
		final double dblExpectedFinalX,
		final double dblXVariance,
		final double dblFinalAlpha)
		throws java.lang.Exception
	{
		if (!org.drip.quant.common.NumberUtil.IsValid (_dblInitialDate = dblInitialDate) ||
			!org.drip.quant.common.NumberUtil.IsValid (_dblFinalDate = dblFinalDate) ||
				!org.drip.quant.common.NumberUtil.IsValid (_dblExpectedFinalX = dblExpectedFinalX) ||
					!org.drip.quant.common.NumberUtil.IsValid (_dblXVariance = dblXVariance) ||
						!org.drip.quant.common.NumberUtil.IsValid (_dblFinalAlpha = dblFinalAlpha))
			throw new java.lang.Exception ("HullWhiteTransitionMetrics ctr: Invalid Inputs");

		_dblXStochasticShift = java.lang.Math.sqrt (_dblXVariance * 3.);

		_lStochasticTreeIndex = java.lang.Math.round (_dblExpectedFinalX / _dblXStochasticShift);

		double dblEta = _dblExpectedFinalX - _lStochasticTreeIndex * _dblXStochasticShift;
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
	 * Retrieve the Final Date
	 * 
	 * @return The Final Date
	 */

	public double finalDate()
	{
		return _dblFinalDate;
	}

	/**
	 * Retrieve the Expected Final Value for X
	 * 
	 * @return The Expected Final Value for X
	 */

	public double expectedFinalX()
	{
		return _dblExpectedFinalX;
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
	 * Retrieve the Stochastic Tree Index
	 * 
	 * @return The Stochastic Tree Index
	 */

	public long stochasticTreeIndex()
	{
		return _lStochasticTreeIndex;
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
		return (_lStochasticTreeIndex + 1) * _dblXStochasticShift;
	}

	/**
	 * Retrieve the "Down" Value for X
	 * 
	 * @return The "Down" Value for X
	 */

	public double xDown()
	{
		return (_lStochasticTreeIndex - 1) * _dblXStochasticShift;
	}

	/**
	 * Retrieve the Final Alpha
	 * 
	 * @return The Final Alpha
	 */

	public double finalAlpha()
	{
		return _dblFinalAlpha;
	}

	/**
	 * Retrieve the "Up" Node Metrics
	 * 
	 * @return The "Up" Node Metrics
	 */

	public org.drip.state.dynamics.HullWhiteNodeMetrics upNodeMetrics()
	{
		try {
			return new org.drip.state.dynamics.HullWhiteNodeMetrics ((_lStochasticTreeIndex + 1) *
				_dblXStochasticShift, _dblFinalAlpha);
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

	public org.drip.state.dynamics.HullWhiteNodeMetrics downNodeMetrics()
	{
		try {
			return new org.drip.state.dynamics.HullWhiteNodeMetrics ((_lStochasticTreeIndex - 1) *
				_dblXStochasticShift, _dblFinalAlpha);
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

	public org.drip.state.dynamics.HullWhiteNodeMetrics stayNodeMetrics()
	{
		try {
			return new org.drip.state.dynamics.HullWhiteNodeMetrics (_lStochasticTreeIndex *
				_dblXStochasticShift, _dblFinalAlpha);
		} catch (java.lang.Exception e) {
			e.printStackTrace();
		}

		return null;
	}
}
