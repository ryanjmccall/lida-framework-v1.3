/*******************************************************************************
 * Copyright (c) 2009, 2011 The University of Memphis.  All rights reserved. 
 * This program and the accompanying materials are made available 
 * under the terms of the LIDA Software Framework Non-Commercial License v1.0 
 * which accompanies this distribution, and is available at
 * http://ccrg.cs.memphis.edu/assets/papers/2010/LIDA-framework-non-commercial-v1.0.pdf
 *******************************************************************************/
package edu.memphis.ccrg.lida.framework.strategies;

import java.util.Map;

import edu.memphis.ccrg.lida.framework.initialization.Initializable;

/**
 * Basic {@link DecayStrategy} governed by a linear curve.
 * 
 * @author Ryan J. McCall
 * @author Javier Snaider
 */
public class LinearDecayStrategy extends StrategyImpl implements DecayStrategy {

	/*
	 * The default slope
	 */
	private static final double DEFAULT_M = 0.1;

	/*
	 * The slope of this linear curve.
	 */
	private double m;

	/**
	 * Creates a new instance of LinearCurve. Values for slope and intercept are
	 * set to the default ones.
	 */
	public LinearDecayStrategy() {
		m = DEFAULT_M;
	}
	
	private static final double DEFAULT_LOWER_BOUND=0.0;
	private double lowerBound=DEFAULT_LOWER_BOUND;

	/**
	 * If this method is overridden, this init() must be called first! i.e.
	 * super.init(); Will set parameters with the following names:<br/>
	 * <br/>
	 * 
	 * <b>m</b> slope of the excite function<br/>
	 * If any parameter is not specified its default value will be used.
	 * 
	 * @see Initializable
	 */
	@Override
	public void init() {
		m = getParam("m", DEFAULT_M);
		lowerBound=getParam("lowerBound",DEFAULT_LOWER_BOUND);
	}

	/**
	 * Decays the current activation according to some internal decay function.
	 * 
	 * @param currentActivation
	 *            activation of the entity before decay.
	 * @param ticks
	 *            The number of ticks to decay.
	 * @param params
	 *            optionally accepts 1 double parameter specifying the slope of
	 *            decay ticks and activations.
	 * @return new activation
	 */
	@Override
	public double decay(double currentActivation, long ticks, Object... params) {
		double mm = m;
		if (params != null && params.length != 0) {
			mm = (Double) params[0];
		}
		return calcActivation(currentActivation, ticks, mm);
	}

	/**
	 * Decays the current activation according to some internal decay function.
	 * 
	 * @param currentActivation
	 *            activation of the entity before decay.
	 * @param ticks
	 *            how much time has passed since last decay
	 * @param params
	 *            optionally accepts 1 parameter specifying the slope of decay
	 *            ticks and activations.
	 * @return new activation amount
	 */
	@Override
	public double decay(double currentActivation, long ticks,
			Map<String, ? extends Object> params) {
		double mm = m;
		if (params != null && params.containsKey("m")) {
			mm = (Double) params.get("m");
		}
		return calcActivation(currentActivation, ticks, mm);
	}

	/*
	 * To calculate activation value of decay operation by linear strategy
	 * 
	 * @param currentActivation current activation
	 * 
	 * @param ticks parameter of ticks
	 * 
	 * @param mm parameter of slope (default value is 0.1)
	 * 
	 * @return Calculated activation value
	 */
	private double calcActivation(double currentActivation, long ticks,
			double mm) {
		currentActivation -= (mm * ticks);
		return (currentActivation > lowerBound)? currentActivation: lowerBound;
	}
}