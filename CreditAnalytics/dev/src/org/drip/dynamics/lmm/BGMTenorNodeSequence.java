
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
 * BGMTenorNodeSequence contains the Point Nodes of the Latent State Quantifiers and their Increments present
 * 	in the specified BGMForwardTenorSnap Instance.
 * 
 * 	Brace, A., D. Gatarek, and M. Musiela (1997): The Market Model of Interest Rate Dynamics, Mathematical
 * 		Finance 7 (2), 127-155.
 *
 * @author Lakshmi Krishnamurthy
 */

public class BGMTenorNodeSequence {
	private double[] _adblDate = null;
	private double[] _adblLIBOR = null;
	private double[] _adblLIBORIncrement = null;
	private double[] _adblDiscountFactor = null;
	private double[] _adblDiscountFactorIncrement = null;

	/**
	 * BGMTenorNodeSequence Constructor
	 * 
	 * @param aBFTS Array of the BGM Forward Tenor Snap Instances
	 * 
	 * @throws java.lang.Exception Thrown if the Inputs are Invalid
	 */

	public BGMTenorNodeSequence (
		final org.drip.dynamics.lmm.BGMForwardTenorSnap[] aBFTS)
		throws java.lang.Exception
	{
		if (null == aBFTS) throw new java.lang.Exception ("BGMTenorNodeSequence ctr: Invalid Inputs!");

		int iNumSnap = aBFTS.length;
		_adblDate = new double[iNumSnap];
		_adblLIBOR = new double[iNumSnap];
		_adblLIBORIncrement = new double[iNumSnap];
		_adblDiscountFactor = new double[iNumSnap];
		_adblDiscountFactorIncrement = new double[iNumSnap];

		if (0 == iNumSnap) throw new java.lang.Exception ("BGMTenorNodeSequence ctr: Invalid Inputs!");

		for (int i = 0; i < iNumSnap; ++i) {
			_adblDate[i] = aBFTS[i].date();

			_adblLIBOR[i] = aBFTS[i].libor();

			_adblLIBORIncrement[i] = aBFTS[i].liborIncrement();

			_adblDiscountFactor[i] = aBFTS[i].discountFactor();

			_adblDiscountFactorIncrement[i] = aBFTS[i].discountFactorIncrement();
		}
	}

	/**
	 * Retrieve the Array of Tenor Dates
	 * 
	 * @return The Array of Tenor Dates
	 */

	public double[] dates()
	{
		return _adblDate;
	}

	/**
	 * Retrieve the Array of Tenor LIBOR Rates
	 * 
	 * @return The Array of Tenor LIBOR Rates
	 */

	public double[] liborRates()
	{
		return _adblLIBOR;
	}

	/**
	 * Retrieve the Array of Tenor LIBOR Rate Increments
	 * 
	 * @return The Array of Tenor LIBOR Rate Increments
	 */

	public double[] liborRateIncrements()
	{
		return _adblLIBORIncrement;
	}

	/**
	 * Retrieve the Array of Tenor Discount Factors
	 * 
	 * @return The Array of Tenor Discount Factors
	 */

	public double[] discountFactors()
	{
		return _adblDiscountFactor;
	}

	/**
	 * Retrieve the Array of Tenor Discount Factor Increments
	 * 
	 * @return The Array of Tenor Discount Factor Increments
	 */

	public double[] discountFactorIncrements()
	{
		return _adblDiscountFactorIncrement;
	}

	@Override public java.lang.String toString()
	{
		int iNumTenor = _adblDate.length;
		java.lang.String strDateDump = "\t |";
		java.lang.String strPartition = "\t |";
		java.lang.String strLIBORDump = "\t |";
		java.lang.String strLIBORIncrementDump = "\t |";
		java.lang.String strDiscountFactorDump = "\t |";
		java.lang.String strDiscountFactorIncrementDump = "\t |";

		for (int i = 0; i < iNumTenor; ++i) {
			try {
				strPartition += "-------------";

				strDateDump += " " + new org.drip.analytics.date.JulianDate (_adblDate[i]) + " |";

				strLIBORDump += "  " + org.drip.quant.common.FormatUtil.FormatDouble (_adblLIBOR[i], 1, 3,
					100.) + "%   |";

				strLIBORIncrementDump += "    " + org.drip.quant.common.FormatUtil.FormatDouble
					(_adblLIBORIncrement[i], 2, 0, 10000.) + "     |";

				strDiscountFactorDump += "  " + org.drip.quant.common.FormatUtil.FormatDouble
					(_adblDiscountFactor[i], 2, 3, 100.) + "   |";

				strDiscountFactorIncrementDump += "    " + org.drip.quant.common.FormatUtil.FormatDouble
					(_adblDiscountFactorIncrement[i], 2, 0, 10000.) + "     |";
			} catch (java.lang.Exception e) {
				e.printStackTrace();

				return null;
			}
		}

		return "\n" + strPartition + "|\n" + strDateDump + "|\n" + strPartition + "|\n" + strLIBORDump +
			"|\n" + strLIBORIncrementDump + "|\n" + strDiscountFactorDump + "|\n" +
				strDiscountFactorIncrementDump + "|\n" + strPartition + "|\n";
	}
}
