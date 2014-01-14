
package org.drip.service.api;

/*
 * -*- mode: java; tab-width: 4; indent-tabs-mode: nil; c-basic-offset: 4 -*-
 */

/*!
 * Copyright (C) 2014 Lakshmi Krishnamurthy
 * Copyright (C) 2013 Lakshmi Krishnamurthy
 * 
 * This file is part of CreditAnalytics, a free-software/open-source library for fixed income analysts and
 * 		developers - http://www.credit-trader.org
 * 
 * CreditAnalytics is a free, full featured, fixed income credit analytics library, developed with a special
 * 		focus towards the needs of the bonds and credit products community.
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
 * ForwardRates contains the array of the forward rates.
 * 
 * @author Lakshmi Krishnamurthy
 */

public class ForwardRates {
	private java.util.List<java.lang.Double> _lsForward = new java.util.ArrayList<java.lang.Double>();

	/**
	 * Empty ForwardRates constructor
	 */

	public ForwardRates()
	{
	}

	/**
	 * Add a Forward Rate to the List
	 * 
	 * @param dblForward The Forward Rate to be added
	 * 
	 * @return TRUE => Successfully added
	 */

	public boolean addForward (
		final double dblForward)
	{
		if (!org.drip.quant.common.NumberUtil.IsValid (dblForward)) return false;

		_lsForward.add (dblForward);

		return true;
	}

	/**
	 * Convert the List of Forwards to an Array
	 * 
	 * @return The Array of Forwards
	 */

	public double[] toArray()
	{
		if (0 == _lsForward.size()) return null;

		int i = 0;

		double[] adblForward = new double[_lsForward.size()];

		for (double dbl : _lsForward)
			adblForward[i++] = dbl;

		return adblForward;
	}

	@Override public java.lang.String toString()
	{
		java.lang.StringBuffer sb = new java.lang.StringBuffer();

		boolean bStart = true;

		for (double dbl : toArray()) {
			if (bStart)
				bStart = false;
			else
				sb.append (",");

			sb.append (dbl);
		}

		return sb.toString();
	}
}
