
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
 * G2PlusPlus provides the Hull-White-type, but Two-Factor Gaussian HJM Short Rate Dynamics Implementation.
 *
 * @author Lakshmi Krishnamurthy
 */

public class G2PlusPlus {
	private double _dblA = java.lang.Double.NaN;
	private double _dblB = java.lang.Double.NaN;
	private double _dblEta = java.lang.Double.NaN;
	private double _dblRho = java.lang.Double.NaN;
	private double _dblSigma = java.lang.Double.NaN;
	private org.drip.sequence.random.RandomSequenceGenerator[] _aRSG = null;
	private org.drip.function.deterministic.AbstractUnivariate _auIFRInitial = null;

	/**
	 * G2PlusPlus Constructor
	 * 
	 * @param dblSigma Sigma
	 * @param dblA A
	 * @param dblEta Eta
	 * @param dblB B
	 * @param aRSG Array of the Random Sequence Generators
	 * @param dblRho Rho
	 * @param auIFRInitial The Initial Instantaneous Forward Rate Term Structure
	 * 
	 * @throws java.lang.Exception Thrown if the Inputs are Invalid
	 */

	public G2PlusPlus (
		final double dblSigma,
		final double dblA,
		final double dblEta,
		final double dblB,
		final org.drip.sequence.random.RandomSequenceGenerator[] aRSG,
		final double dblRho,
		final org.drip.function.deterministic.AbstractUnivariate auIFRInitial)
		throws java.lang.Exception
	{
		if (!org.drip.quant.common.NumberUtil.IsValid (_dblSigma = dblSigma) ||
			!org.drip.quant.common.NumberUtil.IsValid (_dblA = dblA) ||
				!org.drip.quant.common.NumberUtil.IsValid (_dblEta = dblEta) ||
					!org.drip.quant.common.NumberUtil.IsValid (_dblB = dblB) || null == (_aRSG = aRSG) || 2
						!= _aRSG.length || !org.drip.quant.common.NumberUtil.IsValid (_dblRho = dblRho) ||
							null == (_auIFRInitial = auIFRInitial))
			throw new java.lang.Exception ("G2PlusPlus ctr: Invalid Inputs");
	}

	/**
	 * Retrieve Sigma
	 * 
	 * @return Sigma
	 */

	public double sigma()
	{
		return _dblSigma;
	}

	/**
	 * Retrieve A
	 * 
	 * @return A
	 */

	public double a()
	{
		return _dblA;
	}

	/**
	 * Retrieve Eta
	 * 
	 * @return Eta
	 */

	public double eta()
	{
		return _dblEta;
	}

	/**
	 * Retrieve B
	 * 
	 * @return B
	 */

	public double b()
	{
		return _dblB;
	}

	/**
	 * Retrieve the Initial Instantaneous Forward Rate Term Structure
	 * 
	 * @return The Initial Instantaneous Forward Rate Term Structure
	 */

	public org.drip.function.deterministic.AbstractUnivariate ifrInitialTermStructure()
	{
		return _auIFRInitial;
	}

	/**
	 * Retrieve the Random Sequence Generator Array
	 * 
	 * @return The Random Sequence Generator Array
	 */

	public org.drip.sequence.random.RandomSequenceGenerator[] rsg()
	{
		return _aRSG;
	}

	/**
	 * Retrieve Rho
	 * 
	 * @return Rho
	 */

	public double rho()
	{
		return _dblRho;
	}

	/**
	 * Compute the G2++ Phi
	 * 
	 * @param dblSpotDate The Spot Date
	 * @param dblViewDate The View Date
	 * 
	 * @return The G2++ Phi
	 * 
	 * @throws java.lang.Exception Thrown if the G2++ Phi cannot be computed
	 */

	public double phi (
		final double dblSpotDate,
		final double dblViewDate)
		throws java.lang.Exception
	{
		if (!org.drip.quant.common.NumberUtil.IsValid (dblSpotDate) ||
			!org.drip.quant.common.NumberUtil.IsValid (dblViewDate) || dblSpotDate > dblViewDate)
			throw new java.lang.Exception ("G2PlusPlus::phi => Invalid Inputs");

		double dblSpotViewDCF = (dblViewDate - dblSpotDate) / 365.25;

		double dblFactor1Phi = _dblSigma / _dblA * (1. - java.lang.Math.exp (-1. * _dblA * dblSpotViewDCF));

		double dblFactor2Phi = _dblEta / _dblB * (1. - java.lang.Math.exp (-1. * _dblB * dblSpotViewDCF));

		return _auIFRInitial.evaluate (dblViewDate) + 0.5 * dblFactor1Phi * dblFactor1Phi + 0.5 *
			dblFactor2Phi * dblFactor2Phi;
	}

	/**
	 * Compute the X Increment
	 * 
	 * @param dblSpotDate The Spot Date
	 * @param dblViewDate The View Date
	 * @param dblX The X Value
	 * @param dblViewTimeIncrement The Spot Time Increment
	 * 
	 * @return The X Increment
	 * 
	 * @throws java.lang.Exception Thrown if the Inputs are Invalid
	 */

	public double deltaX (
		final double dblSpotDate,
		final double dblViewDate,
		final double dblX,
		final double dblViewTimeIncrement)
		throws java.lang.Exception
	{
		if (!org.drip.quant.common.NumberUtil.IsValid (dblSpotDate) ||
			!org.drip.quant.common.NumberUtil.IsValid (dblViewDate) || dblSpotDate > dblViewDate ||
				!org.drip.quant.common.NumberUtil.IsValid (dblX) ||
					!org.drip.quant.common.NumberUtil.IsValid (dblViewTimeIncrement))
			throw new java.lang.Exception ("G2PlusPlus::deltaX => Invalid Inputs");

		return -1. * _dblA * dblX * dblViewTimeIncrement + _dblSigma * java.lang.Math.sqrt
			(dblViewTimeIncrement) * _aRSG[0].random();
	}

	/**
	 * Compute the Y Increment
	 * 
	 * @param dblSpotDate The Spot Date
	 * @param dblViewDate The View Date
	 * @param dblY The Y Value
	 * @param dblViewTimeIncrement The Spot Time Increment
	 * 
	 * @return The Y Increment
	 * 
	 * @throws java.lang.Exception Thrown if the Inputs are Invalid
	 */

	public double deltaY (
		final double dblSpotDate,
		final double dblViewDate,
		final double dblY,
		final double dblViewTimeIncrement)
		throws java.lang.Exception
	{
		if (!org.drip.quant.common.NumberUtil.IsValid (dblSpotDate) ||
			!org.drip.quant.common.NumberUtil.IsValid (dblViewDate) || dblSpotDate > dblViewDate ||
				!org.drip.quant.common.NumberUtil.IsValid (dblY) ||
					!org.drip.quant.common.NumberUtil.IsValid (dblViewTimeIncrement))
			throw new java.lang.Exception ("G2PlusPlus::deltaY => Invalid Inputs");

		return -1. * _dblB * dblY * dblViewTimeIncrement + _dblEta * java.lang.Math.sqrt
			(dblViewTimeIncrement) * _aRSG[1].random();
	}
}
