
package org.drip.param.valuation;

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
 * ValuationCustomizationParams holds the parameters needed to interpret the input quotes. It contains the
 * 	quote day count, the quote frequency, the quote EOM Adjustment, the quote Act/Act parameters, the quote
 * 	Calendar, the Core Collateralization Parameters, and the Switchable Alternate Collateralization
 * 	Parameters. It also indicates if the native quote is spread based.
 * 
 * @author Lakshmi Krishnamurthy
 */

public class ValuationCustomizationParams extends org.drip.service.stream.Serializer {
	private int _iYieldFrequency = 0;
	private boolean _bSpreadQuoted = false;
	private java.lang.String _strYieldDC = "";
	private boolean _bYieldApplyEOMAdj = false;
	private java.lang.String _strYieldCalendar = "";
	private org.drip.analytics.daycount.ActActDCParams _aapYield = null;
	private org.drip.param.valuation.CollateralizationParams _collatParamsCore = null;
	private java.util.Set<org.drip.param.valuation.CollateralizationParams> _setSwitchableCollateralBasket =
		null;

	/**
	 * ValuationCustomizationParams de-serialization from input byte array
	 * 
	 * @param ab Byte Array
	 * 
	 * @throws java.lang.Exception Thrown if ValuationCustomizationParams cannot be properly de-serialized
	 */

	public ValuationCustomizationParams (
		final byte[] ab)
		throws java.lang.Exception
	{
		if (null == ab || 0 == ab.length)
			throw new java.lang.Exception
				("ValuationCustomizationParams de-serializer: Invalid input Byte array");

		java.lang.String strRawString = new java.lang.String (ab);

		if (null == strRawString || strRawString.isEmpty())
			throw new java.lang.Exception ("ValuationCustomizationParams de-serializer: Empty state");

		java.lang.String strSerializedQuotingParams = strRawString.substring (0, strRawString.indexOf
			(objectTrailer()));

		if (null == strSerializedQuotingParams || strSerializedQuotingParams.isEmpty())
			throw new java.lang.Exception
				("ValuationCustomizationParams de-serializer: Cannot locate state");

		java.lang.String[] astrField = org.drip.quant.common.StringUtil.Split (strSerializedQuotingParams,
			fieldDelimiter());

		if (null == astrField || 7 > astrField.length)
			throw new java.lang.Exception
				("ValuationCustomizationParams de-serializer: Invalid reqd field set");

		// double dblVersion = new java.lang.Double (astrField[0]);

		if (null == astrField[1] || astrField[1].isEmpty())
			throw new java.lang.Exception
				("ValuationCustomizationParams de-serializer: Cannot locate yield DC");

		if (org.drip.service.stream.Serializer.NULL_SER_STRING.equalsIgnoreCase (astrField[1]))
			_strYieldDC = "";
		else
			_strYieldDC = astrField[1];

		if (null == astrField[2] || astrField[2].isEmpty() ||
			org.drip.service.stream.Serializer.NULL_SER_STRING.equalsIgnoreCase (astrField[2]))
			throw new java.lang.Exception
				("ValuationCustomizationParams de-serializer: Cannot locate Yield Frequency");

		_iYieldFrequency = new java.lang.Integer (astrField[2]);

		if (null == astrField[3] || astrField[3].isEmpty() ||
			org.drip.service.stream.Serializer.NULL_SER_STRING.equalsIgnoreCase (astrField[3]))
			throw new java.lang.Exception
				("ValuationCustomizationParams de-serializer: Cannot locate spread quote flag");

		_bSpreadQuoted = new java.lang.Boolean (astrField[3]);

		if (null == astrField[4] || astrField[4].isEmpty())
			throw new java.lang.Exception
				("ValuationCustomizationParams de-serializer: Cannot locate yield DC");

		if (org.drip.service.stream.Serializer.NULL_SER_STRING.equalsIgnoreCase (astrField[4]))
			_strYieldCalendar = "";
		else
			_strYieldCalendar = astrField[4];

		if (null == astrField[5] || astrField[5].isEmpty() ||
			org.drip.service.stream.Serializer.NULL_SER_STRING.equalsIgnoreCase (astrField[5]))
			throw new java.lang.Exception
				("ValuationCustomizationParams de-serializer: Cannot locate apply EOM flag");

		_bYieldApplyEOMAdj = new java.lang.Boolean (astrField[5]).booleanValue();

		if (null == astrField[6] || astrField[6].isEmpty())
			throw new java.lang.Exception
				("ValuationCustomizationParams de-serializer: Cannot locate optional yield ActAct Params");

		if (org.drip.service.stream.Serializer.NULL_SER_STRING.equalsIgnoreCase (astrField[6]))
			_aapYield = null;
		else
			_aapYield = new org.drip.analytics.daycount.ActActDCParams (astrField[6].getBytes());
	}

	/**
	 * Construct ValuationCustomizationParams from the Day Count and the Frequency parameters
	 * 
	 * @param strDC Quoting Day Count
	 * @param iFrequency Quoting Frequency
	 * @param bApplyEOMAdj TRUE => Apply the EOM Adjustment
	 * @param aap => Quoting Act/Act Parameters
	 * @param strCalendar => Quoting Calendar
	 * @param bSpreadQuoted => TRUE => Market Quotes are Spread Quoted
	 * @param collatParamsCore => The Core Collateral Parameters using which the valuation is done
	 * @param setSwitchableCollateralBasket => Switchable Collateral Basket
	 * 
	 * @throws java.lang.Exception Thrown if inputs are invalid
	 */

	public ValuationCustomizationParams (
		final java.lang.String strDC,
		final int iFrequency,
		final boolean bApplyEOMAdj,
		final org.drip.analytics.daycount.ActActDCParams aap,
		final java.lang.String strCalendar,
		final boolean bSpreadQuoted,
		final org.drip.param.valuation.CollateralizationParams collatParamsCore,
		final java.util.Set<org.drip.param.valuation.CollateralizationParams> setSwitchableCollateralBasket)
		throws java.lang.Exception
	{
		if (null == strDC || strDC.isEmpty() || 0 == iFrequency)
			throw new java.lang.Exception ("ValuationCustomizationParams ctr: Invalid quoting params!");

		_aapYield = aap;
		_strYieldDC = strDC;
		_iYieldFrequency = iFrequency;
		_bSpreadQuoted = bSpreadQuoted;
		_strYieldCalendar = strCalendar;
		_bYieldApplyEOMAdj = bApplyEOMAdj;
		_collatParamsCore = collatParamsCore;
		_setSwitchableCollateralBasket = setSwitchableCollateralBasket;
	}

	/**
	 * Retrieve the Yield Act Act Day Count Parameters
	 * 
	 * @return The Yield Act Act Day Count Parameters
	 */

	public org.drip.analytics.daycount.ActActDCParams yieldAAP()
	{
		return _aapYield;
	}

	/**
	 * Retrieve the Yield Day Count
	 * 
	 * @return The Yield Day Count
	 */

	public java.lang.String yieldDayCount()
	{
		return _strYieldDC;
	}

	/**
	 * Retrieve the Yield Frequency
	 * 
	 * @return The Yield Frequency
	 */

	public int yieldFreq()
	{
		return _iYieldFrequency;
	}

	/**
	 * Indicate if spread Quoted
	 * 
	 * @return TRUE => Spread Quoted
	 */

	public boolean isSpreadQuoted()
	{
		return _bSpreadQuoted;
	}

	/**
	 * Retrieve the Yield Calendar
	 * 
	 * @return The Yield Calendar
	 */

	public java.lang.String yieldCalendar()
	{
		return _strYieldCalendar;
	}

	/**
	 * Indicate if EOM Adjustment is to be made for the Yield Calculation
	 * 
	 * @return TRUE => EOM Adjustment is to be made for the Yield Calculation
	 */

	public boolean applyYieldEOMAdj()
	{
		return _bYieldApplyEOMAdj;
	}

	/**
	 * Retrieve the Core Collateralization Parameters
	 * 
	 * @return The Core Collateralization Parameters
	 */

	public org.drip.param.valuation.CollateralizationParams coreCollateralizationParams()
	{
		return _collatParamsCore;
	}

	/**
	 * Retrieve the Switchable Collateralization Basket
	 * 
	 * @return The Switchable Collateralization Basket
	 */

	public java.util.Set<org.drip.param.valuation.CollateralizationParams> switchableCollateralBasket()
	{
		return _setSwitchableCollateralBasket;
	}

	@Override public java.lang.String fieldDelimiter()
	{
		return "~";
	}

	@Override public java.lang.String objectTrailer()
	{
		return "`";
	}

	@Override public byte[] serialize()
	{
		java.lang.StringBuffer sb = new java.lang.StringBuffer();

		sb.append (org.drip.service.stream.Serializer.VERSION + fieldDelimiter());

		if (null == _strYieldDC || _strYieldDC.isEmpty())
			sb.append (org.drip.service.stream.Serializer.NULL_SER_STRING + fieldDelimiter());
		else
			sb.append (_strYieldDC + fieldDelimiter());

		sb.append (_iYieldFrequency + fieldDelimiter() + _bSpreadQuoted + fieldDelimiter());

		if (null == _strYieldCalendar || _strYieldCalendar.isEmpty())
			sb.append (org.drip.service.stream.Serializer.NULL_SER_STRING + fieldDelimiter());
		else
			sb.append (_strYieldCalendar + fieldDelimiter());

		sb.append (_bYieldApplyEOMAdj + fieldDelimiter());

		if (null == _aapYield)
			sb.append (org.drip.service.stream.Serializer.NULL_SER_STRING + fieldDelimiter());
		else
			sb.append (new java.lang.String (_aapYield.serialize()) + fieldDelimiter());

		return sb.append (objectTrailer()).toString().getBytes();
	}

	@Override public org.drip.service.stream.Serializer deserialize (
		final byte[] ab)
	{
		try {
			return new ValuationCustomizationParams (ab);
		} catch (java.lang.Exception e) {
			e.printStackTrace();
		}

		return null;
	}

	public static final void main (
		final java.lang.String[] astrArgs)
		throws java.lang.Exception
	{
		ValuationCustomizationParams vcp = new ValuationCustomizationParams ("30/360", 2, true, null, "DKK",
			false, null, null);

		byte[] abVCP = vcp.serialize();

		System.out.println (new java.lang.String (abVCP));

		ValuationCustomizationParams vcpDeser = new ValuationCustomizationParams (abVCP);

		System.out.println (new java.lang.String (vcpDeser.serialize()));
	}
}
