
package org.drip.product.definition;

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
 * BondProduct interface implements the product static data behind bonds of all kinds. Bond static data is
 * 		captured in a set of 11 container classes – BondTSYParams, BondCouponParams, BondNotionalParams,
 * 		BondFloaterParams, BondCurrencyParams, BondIdentifierParams, ComponentValuationParams,
 * 		ComponentRatesValuationParams, ComponentCreditValuationParams, ComponentTerminationEvent,
 *  	BondFixedPeriodParams, and one EmbeddedOptionSchedule object instance each for the call and the put
 *  	objects. Each of these parameter sets can be set separately.
 *
 * @author Lakshmi Krishnamurthy
 */

public interface BondProduct {

	/**
	 * Set the bond treasury benchmark Set
	 * 
	 * @param tsyBmkSet Bond treasury benchmark Set
	 * 
	 * @return TRUE if succeeded
	 */

	public abstract boolean setTreasuryBenchmark (
		final org.drip.product.params.TsyBmkSet tsyBmkSet
	);

	/**
	 * Retrieve the bond treasury benchmark Set
	 * 
	 * @return Bond treasury benchmark Set
	 */

	public abstract org.drip.product.params.TsyBmkSet treasuryBenchmark();

	/**
	 * Set the bond identifier set
	 * 
	 * @param idSet Bond identifier set
	 * 
	 * @return True if succeeded
	 */

	public abstract boolean setIdentifierSet (
		final org.drip.product.params.IdentifierSet idSet
	);

	/**
	 * Retrieve the bond identifier set
	 * 
	 * @return Bond identifier set
	 */

	public abstract org.drip.product.params.IdentifierSet identifierSet();

	/**
	 * Set the bond coupon setting
	 * 
	 * @param cpnSetting Bond coupon setting
	 * 
	 * @return True if succeeded
	 */

	public abstract boolean setCouponSetting (
		final org.drip.product.params.CouponSetting cpnSetting
	);

	/**
	 * Retrieve the bond coupon setting
	 * 
	 * @return Bond Coupon setting
	 */

	public abstract org.drip.product.params.CouponSetting couponSetting();

	/**
	 * Set the bond currency set
	 * 
	 * @param ccySet Bond currency set
	 * 
	 * @return True if succeeded
	 */

	public abstract boolean setCurrencySet (
		final org.drip.product.params.CurrencySet ccySet
	);

	/**
	 * Retrieve the bond currency set
	 * 
	 * @return Bond Currency Set
	 */

	public abstract org.drip.product.params.CurrencySet currencyParams();

	/**
	 * Set the bond floater setting
	 * 
	 * @param fltSetting Bond floater setting
	 * 
	 * @return True if succeeded
	 */

	public abstract boolean setFloaterSetting (
		final org.drip.product.params.FloaterSetting fltSetting
	);

	/**
	 * Retrieve the bond floater setting
	 * 
	 * @return Bond Floater setting
	 */

	public abstract org.drip.product.params.FloaterSetting floaterSetting();

	/**
	 * Set the bond fixings
	 * 
	 * @param lsfc Latent State Fixings Container
	 * 
	 * @return True if succeeded
	 */

	public abstract boolean setFixings (
		final org.drip.param.market.LatentStateFixingsContainer lsfc);

	/**
	 * Retrieve the bond fixings
	 * 
	 * @return Bond fixings
	 */

	public abstract org.drip.param.market.LatentStateFixingsContainer fixings();

	/**
	 * Set the Bond's Market Convention
	 * 
	 * @param mktConv Bond's Market Convention
	 * 
	 * @return True if succeeded
	 */

	public abstract boolean setMarketConvention (
		final org.drip.product.params.QuoteConvention mktConv
	);

	/**
	 * Retrieve the Bond's Market Convention
	 * 
	 * @return Bond's Market Convention
	 */

	public abstract org.drip.product.params.QuoteConvention marketConvention();

	/**
	 * Ses the Bond Rates Setting
	 * 
	 * @param ratesSetting Bond Rates Setting
	 * 
	 * @return True if succeeded
	 */

	public abstract boolean setRatesSetting (
		final org.drip.product.params.RatesSetting ratesSetting
	);

	/**
	 * Retrieve the Bond Rates Setting
	 * 
	 * @return Bond Rates Setting
	 */

	public abstract org.drip.product.params.RatesSetting ratesSetting();

	/**
	 * Set the bond Credit Setting
	 * 
	 * @param creditSetting Bond credit Setting
	 * 
	 * @return True if succeeded
	 */

	public abstract boolean setCreditSetting (
		final org.drip.product.params.CreditSetting creditSetting
	);

	/**
	 * Retrieve the bond credit Setting
	 * 
	 * @return Bond credit Setting
	 */

	public abstract org.drip.product.params.CreditSetting creditSetting();

	/**
	 * Set the bond termination setting
	 * 
	 * @param termSetting Bond termination setting
	 * 
	 * @return True if succeeded
	 */

	public abstract boolean setTerminationSetting (
		final org.drip.product.params.TerminationSetting termSetting
	);

	/**
	 * Retrieve the bond termination setting
	 * 
	 * @return Bond termination setting
	 */

	public abstract org.drip.product.params.TerminationSetting terminationSetting();

	/**
	 * Set the bond Period Set
	 * 
	 * @param periodSet Bond Period Set
	 * 
	 * @return True if succeeded
	 */

	public abstract boolean setPeriodSet (
		final org.drip.product.params.PeriodSet periodSet
	);

	/**
	 * Retrieve the bond period Set
	 * 
	 * @return Bond period Set
	 */

	public abstract org.drip.product.params.PeriodSet periodSet();

	/**
	 * Set the bond notional Setting
	 * 
	 * @param notlSetting Bond Notional Setting
	 * 
	 * @return True if succeeded
	 */

	public abstract boolean setNotionalSetting (
		final org.drip.product.params.NotionalSetting notlSetting
	);

	/**
	 * Retrieve the bond notional Setting
	 * 
	 * @return Bond notional Setting
	 */

	public abstract org.drip.product.params.NotionalSetting notionalSetting();

	/**
	 * Set the bond's embedded call schedule
	 * 
	 * @param eos Bond's embedded call schedule
	 */

	public abstract void setEmbeddedCallSchedule (
		final org.drip.product.params.EmbeddedOptionSchedule eos
	);

	/**
	 * Set the bond's embedded put schedule
	 * 
	 * @param eos Bond's embedded put schedule
	 */

	public abstract void setEmbeddedPutSchedule (
		final org.drip.product.params.EmbeddedOptionSchedule eos
	);
}
