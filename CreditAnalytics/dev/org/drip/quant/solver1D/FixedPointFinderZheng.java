
package org.drip.quant.solver1D;

/*
 * -*- mode: java; tab-width: 4; indent-tabs-mode: nil; c-basic-offset: 4 -*-
 */

/*!
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
 * FixedPointFinderZheng implements the fixed point locator using Zheng's improvement to Brent's method.
 * 
 * FixedPointFinderZheng overrides the iterateCompoundVariate method to achieve the desired simplification in
 * 	the iterative variate selection.
 *
 * @author Lakshmi Krishnamurthy
 */

public class FixedPointFinderZheng extends org.drip.quant.solver1D.FixedPointFinderBracketing {
	@Override protected double iterateCompoundVariate (
		final double dblCurrentVariate,
		final double dblContraVariate,
		final double dblCurrentOF,
		final double dblContraPointOF,
		final org.drip.quant.solver1D.FixedPointFinderOutput rfop)
		throws java.lang.Exception
	{
		double dblVariateMid = org.drip.quant.solver1D.VariateIteratorPrimitive.Bisection (dblCurrentVariate,
			dblContraVariate);

		if (!rfop.incrOFCalcs())
			throw new java.lang.Exception
				("FixedPointFinderZheng::iterateCompoundVariate => Cannot increment rfop!");

		double dblOF = _of.evaluate (dblVariateMid);

		double dblNextVariate = java.lang.Double.NaN;

		if (dblCurrentOF != dblOF && dblContraPointOF != dblOF)
			dblNextVariate = org.drip.quant.solver1D.VariateIteratorPrimitive.InverseQuadraticInterpolation
				(dblCurrentVariate, dblVariateMid, dblContraVariate, dblCurrentOF, dblOF, dblContraPointOF);
		else
			dblNextVariate = org.drip.quant.solver1D.VariateIteratorPrimitive.FalsePosition (dblCurrentVariate,
				dblContraVariate, dblCurrentOF, dblContraPointOF);

		return dblVariateMid < dblNextVariate ? dblVariateMid : dblNextVariate;
	}

	/**
	 * FixedPointFinderZheng constructor
	 * 
	 * @param dblOFGoal OF Goal
	 * @param of Objective Function
	 * @param bWhine TRUE => Balk on Encountering Exception
	 * 
	 * @throws java.lang.Exception Propogated from below
	 */

	public FixedPointFinderZheng (
		final double dblOFGoal,
		final org.drip.quant.function1D.AbstractUnivariate of,
		final boolean bWhine)
		throws java.lang.Exception
	{
		super (dblOFGoal, of, null, org.drip.quant.solver1D.VariateIteratorPrimitive.BISECTION, bWhine);
	}
}
