
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
 * CreditSetting contains the credit related valuation parameters - use default pay lag, use curve or the
 *  component recovery, component recovery, credit curve name, and whether there is accrual on default. It
 *  exports serialization into and de-serialization out of byte arrays.
 *
 * @author Lakshmi Krishnamurthy
 */

public class CreditSetting implements org.drip.product.params.Validatable {

	/**
	 * Default Pay Lag
	 */

	public int _iDefPayLag = -1;

	/**
	 * Use curve or component recovery
	 */

	public boolean _bUseCurveRec = true;

	/**
	 * Credit Curve Name
	 */

	public java.lang.String _strCC = "";

	/**
	 * Whether accrual gets paid on default
	 */

	public boolean _bAccrOnDefault = false;

	/**
	 * Component recovery
	 */

	public double _dblRecovery = java.lang.Double.NaN;

	/**
	 * Construct the CreditSetting from the default pay lag, use curve or the component recovery flag,
	 *  component recovery, credit curve name, and whether there is accrual on default
	 * 
	 * @param iDefPayLag Default Pay Lag
	 * @param dblRecovery Component Recovery
	 * @param bUseCurveRec Use the Curve Recovery (True) or Component Recovery (False)
	 * @param strCC Credit curve name
	 * @param bAccrOnDefault Accrual paid on default (True) 
	 */

	public CreditSetting (
		final int iDefPayLag,
		final double dblRecovery,
		final boolean bUseCurveRec,
		final java.lang.String strCC,
		final boolean bAccrOnDefault)
	{
		_strCC = strCC;
		_iDefPayLag = iDefPayLag;
		_dblRecovery = dblRecovery;
		_bUseCurveRec = bUseCurveRec;
		_bAccrOnDefault = bAccrOnDefault;
	}

	@Override public boolean validate()
	{
		if (null == _strCC || _strCC.isEmpty()) return true;

		if (!org.drip.quant.common.NumberUtil.IsValid (_dblRecovery) && !_bUseCurveRec) return false;

		return true;
	}
}
