/*******************************************************************************
 * Copyright (c) 2009, 2011 The University of Memphis.  All rights reserved. 
 * This program and the accompanying materials are made available 
 * under the terms of the LIDA Software Framework Non-Commercial License v1.0 
 * which accompanies this distribution, and is available at
 * http://ccrg.cs.memphis.edu/assets/papers/2010/LIDA-framework-non-commercial-v1.0.pdf
 *******************************************************************************/
package edu.memphis.ccrg.lida.workspace.workspacebuffers;

import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import edu.memphis.ccrg.lida.framework.FrameworkModuleImpl;
import edu.memphis.ccrg.lida.framework.initialization.Initializable;
import edu.memphis.ccrg.lida.framework.shared.Linkable;
import edu.memphis.ccrg.lida.framework.shared.NodeStructure;
import edu.memphis.ccrg.lida.framework.shared.NodeStructureImpl;
import edu.memphis.ccrg.lida.framework.shared.UnmodifiableNodeStructureImpl;
import edu.memphis.ccrg.lida.framework.tasks.TaskManager;
import edu.memphis.ccrg.lida.globalworkspace.Coalition;
import edu.memphis.ccrg.lida.workspace.WorkspaceContent;

/**
 * The BroadcastQueue is the data structure storing the recent contents of
 * consciousness. It is a submodule of the Workspace. There is a limit on the
 * queue's capacity and on the amount of activation {@link Linkable}s must have
 * to remain in the queue.
 * 
 * @author Ryan J. McCall
 */
public class BroadcastQueueImpl extends FrameworkModuleImpl implements
		BroadcastQueue {

	private static final Logger logger = Logger
			.getLogger(BroadcastQueueImpl.class.getCanonicalName());

	private static final int DEFAULT_QUEUE_CAPACITY = 20;
	private int broadcastQueueCapacity = DEFAULT_QUEUE_CAPACITY;
	/**
	 * Flag specifying whether this queue decays.
	 */
	protected boolean isDecay;
	/**
	 * The queue
	 */
	protected LinkedList<WorkspaceContent> broadcastQueue = new LinkedList<WorkspaceContent>();

	/**
	 * Default constructor
	 */
	public BroadcastQueueImpl() {
	}

	/**
	 * Will set parameters with the following names:<br/>
	 * <br/>
	 * 
	 * <b>workspace.broadcastQueueCapacity</b> the number of recent broadcast
	 * maintained in this BroadcastQueue<br/>
	 * 
	 * @see Initializable
	 */
	@Override
	public void init() {
		isDecay = getParam("workspace.broadcastQueueDecay",false);
		int requestedCapacity = getParam("workspace.broadcastQueueCapacity", 
										 DEFAULT_QUEUE_CAPACITY);
		if(requestedCapacity > 0){
			broadcastQueueCapacity = requestedCapacity;
		}else{
			logger.log(Level.WARNING, "Capacity must be greater than 0.",
					TaskManager.getCurrentTick());
		}
	}

	@Override
	public void receiveBroadcast(Coalition c) {
		UnmodifiableNodeStructureImpl content = (UnmodifiableNodeStructureImpl) c
				.getContent();
		// Since content is not modifiable, a copy must be made. In this class
		// the copy (of the content) will be decayed (modified).
		NodeStructure contentCopy = new NodeStructureImpl();
		contentCopy.mergeWith(content);
		addBufferContent((WorkspaceContent) contentCopy);
	}

	@Override
	public synchronized void addBufferContent(WorkspaceContent c) {
		broadcastQueue.addFirst(c);
		while (broadcastQueue.size() > broadcastQueueCapacity) {
			broadcastQueue.removeLast();// remove oldest
		}
	}

	@Override
	public WorkspaceContent getBufferContent(Map<String, Object> params) {
		if (params != null) {
			Object index = params.get("position");
			if (index instanceof Integer) {
				return getPositionContent((Integer) index);
			}
		}
		return null;
	}

	@Override
	public WorkspaceContent getPositionContent(int i) {
		if (i > -1 && i < broadcastQueue.size()) {
			if(!broadcastQueue.isEmpty()){
				return (WorkspaceContent) broadcastQueue.get(i);
			}
		}
		return null;
	}

	@Override
	public Object getModuleContent(Object... params) {
		return Collections.unmodifiableList(broadcastQueue);
	}

	@Override
	public void decayModule(long t) {
		if(isDecay){
			logger.log(Level.FINER, "Decaying Broadcast Queue", TaskManager
					.getCurrentTick());
			synchronized (this) {
				Iterator<WorkspaceContent> itr = broadcastQueue.iterator();
				while (itr.hasNext()) {
					NodeStructure ns = itr.next();
					ns.decayNodeStructure(t);
					if (ns.getNodeCount() == 0) {
						itr.remove();
					}
				}
			}
		}
	}

	@Override
	public void learn(Coalition c) {
		// Not applicable
	}
}