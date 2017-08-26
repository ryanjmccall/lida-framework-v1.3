/*******************************************************************************
 * Copyright (c) 2009, 2011 The University of Memphis.  All rights reserved. 
 * This program and the accompanying materials are made available 
 * under the terms of the LIDA Software Framework Non-Commercial License v1.0 
 * which accompanies this distribution, and is available at
 * http://ccrg.cs.memphis.edu/assets/papers/2010/LIDA-framework-non-commercial-v1.0.pdf
 *******************************************************************************/
package edu.memphis.ccrg.lida.framework.shared.activation;

import java.util.logging.Level;
import java.util.logging.Logger;

import edu.memphis.ccrg.lida.framework.initialization.InitializableImpl;
import edu.memphis.ccrg.lida.framework.shared.ElementFactory;
import edu.memphis.ccrg.lida.framework.shared.Node;
import edu.memphis.ccrg.lida.framework.strategies.DecayStrategy;
import edu.memphis.ccrg.lida.framework.strategies.ExciteStrategy;
import edu.memphis.ccrg.lida.framework.tasks.Codelet;
import edu.memphis.ccrg.lida.framework.tasks.TaskManager;

/**
 * Generic {@link Activatible} Implementation. Useful for classes to extend from
 * it, e.g. {@link Node} and {@link Codelet}.
 * 
 * @author Javier Snaider
 * @author Ryan J. McCall
 */
public class ActivatibleImpl extends InitializableImpl implements Activatible {

	private static final Logger logger = Logger.getLogger(ActivatibleImpl.class
			.getCanonicalName());
	private static final ElementFactory factory = ElementFactory.getInstance();
//	private static final String DEFAULT_IS_DECAY = "noDecay";

	private ExciteStrategy exciteStrategy;
	private DecayStrategy decayStrategy;
	private DecayStrategy incentiveSalienceDecayStrategy;
	private double activation;
	private double removalThreshold;
	private double incentiveSalience;

	/**
	 * Default constructor
	 */
	public ActivatibleImpl() {
		activation = DEFAULT_ACTIVATION;
		removalThreshold = DEFAULT_ACTIVATIBLE_REMOVAL_THRESHOLD;
		incentiveSalience = DEFAULT_INCENTIVE_SALIENCE;
		decayStrategy = factory.getDefaultDecayStrategy();
		exciteStrategy = factory.getDefaultExciteStrategy();
		incentiveSalienceDecayStrategy = factory.getDefaultDecayStrategy();
	}

	/**
	 * @deprecated To be removed in the future. Only the default constructor is used by the framework. 
	 * @param activation
	 *            initial activation
	 * @param removalThreshold
	 *            initial removableThreshold
	 * @param excite
	 *            {@link ExciteStrategy}
	 * @param decay
	 *            {@link DecayStrategy}
	 */
	@Deprecated
	public ActivatibleImpl(double activation, double removalThreshold,
			ExciteStrategy excite, DecayStrategy decay) {
		this.activation = activation;
		this.removalThreshold = removalThreshold;
		this.exciteStrategy = excite;
		this.decayStrategy = decay;
	}

	@Override
	public void init(){
		//TODO think about how I want to implement this. This is one option, another is adding to FactoriesDataXmlLoader
//		String name = getParam("activatible.incentiveSalienceDecayStrategy",DEFAULT_IS_DECAY);
//		incentiveSalienceDecayStrategy = factory.getDecayStrategy(name);
	}

	@Override
	public void decay(long ticks) {
		if (decayStrategy != null) {
			if (logger.isLoggable(Level.FINEST)) {
				logger.log(Level.FINEST,
						"Before decaying {1} has current activation: {2}",
						new Object[] { TaskManager.getCurrentTick(), this,
								getActivation() });
			}
			synchronized (this) {
				activation = decayStrategy.decay(getActivation(), ticks);
				incentiveSalience=incentiveSalienceDecayStrategy.decay(getIncentiveSalience(), ticks);
			}
			if (logger.isLoggable(Level.FINEST)) {
				logger.log(Level.FINEST,
						"After decaying {1} has current activation: {2}",
						new Object[] { TaskManager.getCurrentTick(), this,
								getActivation() });
			}
		}
	}

	@Deprecated
	@Override
	public void excite(double amount) {
		exciteActivation(amount);
	}

	@Override
	public void exciteActivation(double amount) {
		if (exciteStrategy != null) {
			if (logger.isLoggable(Level.FINEST)) {
				logger.log(Level.FINEST,
						"Before excitation {1} has current activation: {2}",
						new Object[] { TaskManager.getCurrentTick(), this,
								getActivation() });
			}
			synchronized (this) {
				activation = exciteStrategy.excite(getActivation(), amount);
			}
			if (logger.isLoggable(Level.FINEST)) {
				logger.log(Level.FINEST,
						"After excitation {1} has current activation: {2}",
						new Object[] { TaskManager.getCurrentTick(), this,
								getActivation() });
			}
		}
	}

	@Override
	public void exciteIncentiveSalience(double amount) {
		if (exciteStrategy != null) {
			if (logger.isLoggable(Level.FINEST)) {
				logger.log(Level.FINEST,
						"Before excitation {1} has current incentive salience: {2}",
						new Object[] { TaskManager.getCurrentTick(), this,
								getIncentiveSalience() });
			}
			synchronized (this) {
				incentiveSalience = exciteStrategy.excite(getIncentiveSalience(), amount);
			}
			if (logger.isLoggable(Level.FINEST)) {
				logger.log(Level.FINEST,
						"After excitation {1} has current incentive salience: {2}",
						new Object[] { TaskManager.getCurrentTick(), this,
								getIncentiveSalience()});
			}
		}
	}

	@Override
	public void setActivation(double a) {
		if(a > 1.0){
			synchronized (this) {
				activation = 1.0;
			}
		}else if (a < -1.0) {
			synchronized (this) {
				activation = -1.0;
			}
		}else{
			synchronized (this) {
				activation = a;
			}
		}
	}
	@Override
	public double getActivation() {
		return activation;
	}
	@Override
	public double getTotalActivation() {
		return getActivation();
	}
	@Override
	public double getActivatibleRemovalThreshold() {
		return removalThreshold;
	}

	@Override
	public DecayStrategy getDecayStrategy() {
		return decayStrategy;
	}

	@Override
	public ExciteStrategy getExciteStrategy() {
		return exciteStrategy;
	}
	
	@Override
	public double getIncentiveSalience() {
		return incentiveSalience;
	}
	@Override
	public synchronized void setIncentiveSalience(double s) {
		if(s > 1.0){
			synchronized (this) {
				incentiveSalience = 1.0;
			}
		}else if (s < -1.0) {
			synchronized (this) {
				incentiveSalience = -1.0;
			}
		}else{
			synchronized (this) {
				incentiveSalience = s;
			}
		}
	}
	@Override
	public double getTotalIncentiveSalience() {
		return getIncentiveSalience();
	}

	@Override
	public void setActivatibleRemovalThreshold(double t) {
		removalThreshold = t;
	}

	@Override
	public void setDecayStrategy(DecayStrategy s) {
		decayStrategy = s;
	}

	@Override
	public void setExciteStrategy(ExciteStrategy s) {
		exciteStrategy = s;
	}

	@Override
	public boolean isRemovable() {
		return getActivation() <= removalThreshold && 
				Math.abs(getIncentiveSalience()) <= removalThreshold;
	}

	@Override
	public void setIncentiveSalienceDecayStrategy(DecayStrategy s) {
		incentiveSalienceDecayStrategy=s;
	}

	@Override
	public DecayStrategy getIncentiveSalienceDecayStrategy() {
		return incentiveSalienceDecayStrategy;
	}
}
