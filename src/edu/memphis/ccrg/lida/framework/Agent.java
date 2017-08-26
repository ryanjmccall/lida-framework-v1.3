/*******************************************************************************
 * Copyright (c) 2009, 2011 The University of Memphis.  All rights reserved. 
 * This program and the accompanying materials are made available 
 * under the terms of the LIDA Software Framework Non-Commercial License v1.0 
 * which accompanies this distribution, and is available at
 * http://ccrg.cs.memphis.edu/assets/papers/2010/LIDA-framework-non-commercial-v1.0.pdf
 *******************************************************************************/
package edu.memphis.ccrg.lida.framework;

import edu.memphis.ccrg.lida.framework.tasks.TaskManager;

/**
 * A {@link FrameworkModule} containing all of the {@link FrameworkModule}s of
 * an agent.
 * 
 * @author Javier Snaider
 * 
 */
public interface Agent extends FrameworkModule {

	/**
	 * Returns the Task Manager
	 * 
	 * @return {@link TaskManager} in charge of all tasks.
	 */
	public TaskManager getTaskManager();

}