
package org.drip.analytics.output;

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
 * ExerciseInfo is a place-holder for the set of exercise information. It contains the exercise date, the
 * 	exercise factor, and the exercise type. It also exposes methods to serialize/de-serialize off of byte
 * 	arrays.
 *
 * @author Lakshmi Krishnamurthy
 */

public class ExerciseInfo extends org.drip.service.stream.Serializer {
	private double _dblDate = java.lang.Double.NaN;
	private double _dblExerciseFactor = java.lang.Double.NaN;
	private int _iWOType = org.drip.param.valuation.WorkoutInfo.WO_TYPE_MATURITY;

	/**
	 * Constructs the ExerciseInfo from the work-out date, type, and the exercise factor
	 * 
	 * @param dblDate Work-out Date
	 * @param dblExerciseFactor Work-out Factor
	 * @param iWOType Work out Type
	 * 
	 * @throws java.lang.Exception Thrown if input is invalid
	 */

	public ExerciseInfo (
		final double dblDate,
		final double dblExerciseFactor,
		final int iWOType)
		throws java.lang.Exception
	{
		if (!org.drip.quant.common.NumberUtil.IsValid (_dblDate = dblDate) ||
			!org.drip.quant.common.NumberUtil.IsValid (_dblExerciseFactor = dblExerciseFactor))
			throw new java.lang.Exception ("ExerciseInfo ctr: Invalid Inputs!");

		_iWOType = iWOType;
	}

	/**
	 * ExerciseInfo de-serialization from input byte array
	 * 
	 * @param ab Byte Array
	 * 
	 * @throws java.lang.Exception Thrown if ExerciseInfo cannot be properly de-serialized
	 */

	public ExerciseInfo (
		final byte[] ab)
		throws java.lang.Exception
	{
		if (null == ab || 0 == ab.length)
			throw new java.lang.Exception ("ExerciseInfo de-serializer: Invalid input Byte array");

		java.lang.String strRawString = new java.lang.String (ab);

		if (null == strRawString || strRawString.isEmpty())
			throw new java.lang.Exception ("ExerciseInfo de-serializer: Empty state");

		java.lang.String strSerializedNextExerciseInfo = strRawString.substring (0, strRawString.indexOf
			(getObjectTrailer()));

		if (null == strSerializedNextExerciseInfo || strSerializedNextExerciseInfo.isEmpty())
			throw new java.lang.Exception ("ExerciseInfo de-serializer: Cannot locate state");

		java.lang.String[] astrField = org.drip.quant.common.StringUtil.Split (strSerializedNextExerciseInfo,
			getFieldDelimiter());

		if (null == astrField || 4 > astrField.length)
			throw new java.lang.Exception ("ExerciseInfo de-serializer: Invalid reqd field set");

		// double dblVersion = new java.lang.Double (astrField[0]);

		if (null == astrField[1] || astrField[1].isEmpty() ||
			org.drip.service.stream.Serializer.NULL_SER_STRING.equalsIgnoreCase (astrField[1]))
			throw new java.lang.Exception ("ExerciseInfo de-serializer: Cannot locate workout date");

		_dblDate = new java.lang.Double (astrField[1]);

		if (null == astrField[2] || astrField[2].isEmpty() ||
			org.drip.service.stream.Serializer.NULL_SER_STRING.equalsIgnoreCase (astrField[2]))
			throw new java.lang.Exception ("ExerciseInfo de-serializer: Cannot locate exercise factor");

		_dblExerciseFactor = new java.lang.Double (astrField[2]);

		if (null == astrField[3] || astrField[3].isEmpty() ||
			org.drip.service.stream.Serializer.NULL_SER_STRING.equalsIgnoreCase (astrField[3]))
			throw new java.lang.Exception ("ExerciseInfo de-serializer: Cannot locate work out type");

		_iWOType = new java.lang.Integer (astrField[3]);
	}

	/**
	 * Retrieve the Exercise Date
	 * 
	 * @return The Exercise Date
	 */

	public double date()
	{
		return _dblDate;
	}

	/**
	 * Retrieve the Exercise Factor
	 * 
	 * @return The Exercise Factor
	 */

	public double factor()
	{
		return _dblExerciseFactor;
	}

	/**
	 * Retrieve the Work-out Type
	 * 
	 * @return The Work-out Type
	 */

	public int workoutType()
	{
		return _iWOType;
	}

	@Override public byte[] serialize()
	{
		java.lang.StringBuffer sb = new java.lang.StringBuffer();

		sb.append (VERSION + getFieldDelimiter() + _dblDate + getFieldDelimiter() + _dblExerciseFactor +
			getFieldDelimiter() + _iWOType);

		return sb.append (getObjectTrailer()).toString().getBytes();
	}

	@Override public org.drip.service.stream.Serializer deserialize (
		final byte[] ab)
	{
		try {
			return new ExerciseInfo (ab);
		} catch (java.lang.Exception e) {
			e.printStackTrace();
		}

		return null;
	}
}
