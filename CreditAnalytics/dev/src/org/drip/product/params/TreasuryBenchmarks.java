
package org.drip.product.params;

/*
 * -*- mode: java; tab-width: 4; indent-tabs-mode: nil; c-basic-offset: 4 -*-
 */

/*!
 * Copyright (C) 2014 Lakshmi Krishnamurthy
 * Copyright (C) 2013 Lakshmi Krishnamurthy
 * Copyright (C) 2012 Lakshmi Krishnamurthy
 * Copyright (C) 2011 Lakshmi Krishnamurthy
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
 * TsyBmkSet contains the treasury benchmark set - the primary treasury benchmark, and an array of secondary
 *  treasury benchmarks. It exports serialization into and de-serialization out of byte arrays.
 *
 * @author Lakshmi Krishnamurthy
 */

public class TreasuryBenchmarks {
	private java.lang.String _strPrimary = "";
	private java.lang.String[] _astrSecondary = null;

	/**
	 * Construct the treasury benchmark set from the primary treasury benchmark, and an array of secondary
	 * 	treasury benchmarks
	 * 
	 * @param strPrimary Primary Treasury Benchmark
	 * @param astrSecondary Array of Secondary Treasury Benchmarks
	 */

	public TreasuryBenchmarks (
		final java.lang.String strPrimary,
		final java.lang.String[] astrSecondary)
	{
		_strPrimary = strPrimary;
		_astrSecondary = astrSecondary;
	}

	/**
	 * Return the Primary Treasury Benchmark
	 * 
	 * @return Primary Treasury Benchmark
	 */

	public java.lang.String primary()
	{
		return _strPrimary;
	}

	/**
	 * Return an Array of Secondary Treasury Benchmarks
	 * 
	 * @return Array of Secondary Treasury Benchmarks
	 */

	public java.lang.String[] secondary()
	{
		return _astrSecondary;
	}
}
