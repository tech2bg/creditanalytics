
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
 * OffsetIdempotent provides the Implementation of the Offset Idempotent Operator - f(x) = x - C.
 *
 * @author Lakshmi Krishnamurthy
 */

public class OffsetIdempotent extends org.drip.function.deterministic.AbstractUnivariate {
	private double _dblOffset = java.lang.Double.NaN;

	/**
	 * OffsetIdempotent Constructor
	 * 
	 * @param dblOffset The Offset
	 * 
	 * @throws java.lang.Exception Thrown if Inputs are Invalid
	 */

	public OffsetIdempotent (
		final double dblOffset)
		throws java.lang.Exception
	{
		super (null);

		if (!org.drip.quant.common.NumberUtil.IsValid (_dblOffset = dblOffset))
			throw new java.lang.Exception ("OffsetIdempotent ctr: Invalid Inputs");
	}

	/**
	 * Retrieve the Offset
	 * 
	 * @return The Offset
	 */

	public double offset()
	{
		return _dblOffset;
	}

	@Override public double evaluate (
		final double dblVariate)
		throws java.lang.Exception
	{
		if (!org.drip.quant.common.NumberUtil.IsValid (dblVariate))
			throw new java.lang.Exception ("OffsetIdempotent::evaluate => Invalid Inputs");

		return dblVariate - _dblOffset;
	}

	@Override public double derivative (
		final double dblVariate,
		final int iOrder)
		throws java.lang.Exception
	{
		if (!org.drip.quant.common.NumberUtil.IsValid (dblVariate) || 0 > iOrder)
			throw new java.lang.Exception ("OffsetIdempotent::derivative => Invalid Inputs");

		return iOrder > 1 ? 0. : 1;
	}

	@Override public double integrate (
		final double dblBegin,
		final double dblEnd)
		throws java.lang.Exception
	{
		if (!org.drip.quant.common.NumberUtil.IsValid (dblBegin) || !org.drip.quant.common.NumberUtil.IsValid
			(dblEnd))
			throw new java.lang.Exception ("OffsetIdempotent::integrate => Invalid Inputs");

		return 0.5 * (dblEnd * dblEnd - dblBegin - dblBegin) + _dblOffset * (dblEnd - dblBegin);
	}
}
