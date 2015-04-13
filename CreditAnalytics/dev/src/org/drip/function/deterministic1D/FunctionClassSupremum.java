
package org.drip.function.deterministic1D;

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
 * FunctionClassSupremum implements the Univariate Function that corresponds to the Supremum among the
 *  specified Class of Functions.
 *
 * @author Lakshmi Krishnamurthy
 */

public class FunctionClassSupremum extends org.drip.function.deterministic.R1ToR1 {
	class IndexSupremum {
		int _iIndex = -1;
		double _dblSupremum = java.lang.Double.NaN;

		IndexSupremum (
			final int iIndex,
			final double dblSupremum)
		{
			_iIndex = iIndex;
			_dblSupremum = dblSupremum;
		}
	};

	private org.drip.function.deterministic.R1ToR1[] _aAUClass = null;

	private IndexSupremum supremum (
		final double dblVariate)
	{
		int iIndex = 0;
		int iNumFunction = _aAUClass.length;

		try {
			double dblSupremum = _aAUClass[0].evaluate (dblVariate);

			for (int i = 1; i < iNumFunction; ++i) {
				double dblValue = _aAUClass[i].evaluate (dblVariate);

				if (dblValue > dblSupremum) {
					iIndex = i;
					dblSupremum = dblValue;
				}
			}

			return new IndexSupremum (iIndex, dblSupremum);
		} catch (java.lang.Exception e) {
			e.printStackTrace();
		}

		return null;
	}

	/**
	 * FunctionClassSupremum Cnstructor
	 * 
	 * @param aAUClass Array of Functions in the Class
	 * 
	 * @throws java.lang.Exception Thrown if the Inputs are Invalid
	 */

	public FunctionClassSupremum (
		final org.drip.function.deterministic.R1ToR1[] aAUClass)
		throws java.lang.Exception
	{
		super (null);

		if (null == (_aAUClass = aAUClass) || 0 == _aAUClass.length)
			throw new java.lang.Exception ("FunctionClassSupremum ctr => Invalid Inputs");
	}

	/**
	 * Retrieve the Class of Functions
	 * 
	 * @return The Class of Functions
	 */

	public org.drip.function.deterministic.R1ToR1[] functionClass()
	{
		return _aAUClass;
	}

	/**
	 * Retrieve the Supremum Function corresponding to the specified Variate
	 * 
	 * @param dblVariate The Variate
	 * 
	 * @return The Supremum Function corresponding to the specified Variate
	 */

	public org.drip.function.deterministic.R1ToR1 supremumFunction (
		final double dblVariate)
	{
		IndexSupremum is = supremum (dblVariate);

		return null == is ? null : _aAUClass[is._iIndex];
	}

	@Override public double evaluate (
		final double dblVariate)
		throws java.lang.Exception
	{
		IndexSupremum is = supremum (dblVariate);

		if (null == is) throw new java.lang.Exception ("FunctionClassSupremum::evaluate => Invalid Inputs");

		return is._dblSupremum;
	}

	@Override public double derivative (
		final double dblVariate,
		final int iOrder)
		throws java.lang.Exception
	{
		IndexSupremum is = supremum (dblVariate);

		if (null == is)
			throw new java.lang.Exception ("FunctionClassSupremum::derivative => Invalid Inputs");

		return _aAUClass[is._iIndex].derivative (dblVariate, iOrder);
	}

	@Override public double integrate (
		final double dblBegin,
		final double dblEnd)
		throws java.lang.Exception
	{
		IndexSupremum is = supremum (0.5 * (dblBegin + dblEnd));

		if (null == is) throw new java.lang.Exception ("FunctionClassSupremum::integrate => Invalid Inputs");

		return _aAUClass[is._iIndex].integrate (dblBegin, dblEnd);
	}
}
