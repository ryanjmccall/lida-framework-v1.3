/*******************************************************************************
 * Copyright (c) 2009, 2011 The University of Memphis.  All rights reserved. 
 * This program and the accompanying materials are made available 
 * under the terms of the LIDA Software Framework Non-Commercial License v1.0 
 * which accompanies this distribution, and is available at
 * http://ccrg.cs.memphis.edu/assets/papers/2010/LIDA-framework-non-commercial-v1.0.pdf
 *******************************************************************************/
package edu.memphis.ccrg.lida.pam;

import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import edu.memphis.ccrg.lida.framework.Agent;
import edu.memphis.ccrg.lida.framework.initialization.FullyInitializable;
import edu.memphis.ccrg.lida.framework.initialization.GlobalInitializer;
import edu.memphis.ccrg.lida.framework.initialization.Initializer;
import edu.memphis.ccrg.lida.framework.shared.ElementFactory;
import edu.memphis.ccrg.lida.framework.shared.Link;
import edu.memphis.ccrg.lida.framework.shared.LinkCategory;
import edu.memphis.ccrg.lida.framework.shared.Node;
import edu.memphis.ccrg.lida.framework.shared.activation.Learnable;
import edu.memphis.ccrg.lida.framework.tasks.TaskManager;

/**
 * An {@link Initializer} for {@link PerceptualAssociativeMemory} that processes
 * a parameter named 'nodes' containing all node definitions and/or a parameter named 'links' containing all link
 * definitions. For each such definition an element is created and added to {@link PerceptualAssociativeMemory} based on the definitions 
 * and references to the added element are added to the {@link GlobalInitializer} indexed by the element's label. <br/><br/>
 * Each {@link PamNode} definition must by separated by a comma and has the following form: <br/><br/>
 * 
 * &nbsp;&nbsp;&nbsp; <b>label:baseLevelActivation:factoryName</b><br/><br/>
 * 
 * The parameters of a node definition are separated by ':' and only the first parameter is required.<br/>
 * 
 * E.g. <b><param name="nodes">apple, bell:0.0, dog:0.7:PamNodeImpl, hunger:0.0:FeelingPamNodeImpl</param></b>
 * <br/><br/>
 * 
 * Each {@link PamLink} definition is separate by a comma and has the following form: <br/><br/>
 * &nbsp;&nbsp;&nbsp; <b>sourceLabel:sinkLabel:baseLevelActivation:factoryName</b><br/><br/>
 * The parameters of a link definition are separated by ':' and the first 2 parameters are required.<br/>
 * 
 * E.g. <b><param name="links">apple:bell, bell:dog:0.7, bell:hunger:0.0:FeelingPamLinkImpl</param></b><br/>
 * 
 * @author Javier Snaider
 * @author Ryan J. McCall
 */
public class BasicPamInitializer implements Initializer {

	private static final Logger logger = Logger.getLogger(BasicPamInitializer.class.getCanonicalName());
	/**
	 * A {@link GlobalInitializer} reference.
	 */
	protected static final GlobalInitializer globalInitializer = GlobalInitializer.getInstance();
	private static final ElementFactory factory = ElementFactory.getInstance();

	@Override
	public void initModule(FullyInitializable m, Agent agent, Map<String, ?> params) {
		PerceptualAssociativeMemory pam = (PerceptualAssociativeMemory)m;
		initNodes(pam,params);		
		initLinkCategories(pam, params);
		initLinks(pam, params);
	}

	/**
	 * Inits the {@link Node} Objects of the specified {@link PerceptualAssociativeMemory} object based
	 * on params. Params must contain a String key "nodes" whose value is a list of node defs
	 * separated by ",". Node def attributes are separated by ":".
	 * @param pam the {@link PerceptualAssociativeMemory}
	 * @param params the parameters
	 */
	protected static void initNodes(PerceptualAssociativeMemory pam,Map<String, ?> params) {
		String nodes = (String)params.get("nodes");
		if (nodes != null) {
			String[] defs = nodes.split(",");
			for (String nodeDef : defs) {
				nodeDef = nodeDef.trim();
				String[] nodeParams = nodeDef.split(":");
				String label = nodeParams[0];
				if ("".equals(label)) {
					logger.log(Level.WARNING,
							"Empty string found in node specification, node labels must be non-empty");
				}else{
					logger.log(Level.INFO, "Loading PamNode: {0}", label);
					PamNode node = null;
					if(nodeParams.length >= 3){
						node=pam.addNode(nodeParams[2], label);
					}else{
						node=pam.addDefaultNode(label);
					}
					if (node == null) {
						logger.log(Level.WARNING,
								"Failed to get Node '{0}' from PAM.", label);
					}else{
						globalInitializer.setAttribute(label, node);
						if (nodeParams.length >= 2) {
							parseBaseLevelActivation(nodeParams[1],node);
						}
					}
				}
			}
		}
	}
	/**
	 * Inits the {@link LinkCategory} objects of the specified {@link PerceptualAssociativeMemory} object based
	 * on params. Params must contain a String key "linkCategories" whose value is a list of {@link LinkCategory}
	 * object names separated by ",". 
	 * @param pam the {@link PerceptualAssociativeMemory}
	 * @param params the parameters
	 */
	protected static void initLinkCategories(PerceptualAssociativeMemory pam,
			Map<String, ?> params) {
		String linkCategories = (String) params.get("linkCategories");
		if (linkCategories != null) {
			String[] defs = linkCategories.split(",");
			for (String categoryDef : defs) {
				categoryDef = categoryDef.trim();
				String[] categoryParams = categoryDef.split(":");
				String label = categoryParams[0];
				if ("".equals(label)) {
					logger.log(Level.WARNING,
							"Empty string found in link category specification, link category labels must be non-empty");
				}else{
					logger.log(Level.INFO, "Loading LinkCategory: {0}", label);
					PamNode node=(PamNode)factory.getNode("PamNodeImpl", label);
					if (node == null) {
						logger.log(Level.WARNING,
								"Failed to add LinkCategory '{0}' to PAM.", label);
					}else{
						pam.addLinkCategory(node);
						globalInitializer.setAttribute(label, node);
						if (categoryParams.length >= 2) {
							parseBaseLevelActivation(categoryParams[1],node);
						}
					}
				}
			}
		}
	}
	/**
	 * Inits the {@link Link} objects of the specified {@link PerceptualAssociativeMemory} object based
	 * on params. Params must contain a String key "links" whose value is a list of link defs
	 * separated by ",". Link def elements are separated by ":".
	 * @param pam the {@link PerceptualAssociativeMemory}
	 * @param params the parameters
	 */
	protected static void initLinks(PerceptualAssociativeMemory pam,
			Map<String, ?> params) {
		String links = (String) params.get("links");
		if (links != null) {
			String[] linkDefs = links.split(",");
			for (String linkDef: linkDefs) {
				linkDef = linkDef.trim();
				if ("".equals(linkDef)) {
					logger.log(Level.WARNING,
								"Empty string found in links specification, link defs must be non-empty");
					continue;
				}
				logger.log(Level.INFO, "Loading PamLink: {0}", linkDef);
				String[] linkParams = linkDef.split(":");
				if (linkParams.length < 2) {
					logger.log(Level.WARNING, 
								"Bad link specification "+linkDef,
								TaskManager.getCurrentTick());
					continue;
				}
				Node source = pam.getNode(linkParams[0].trim());
				Node sink = pam.getNode(linkParams[1].trim());
				if (source != null && sink != null) {
					PamLink pamLink = null;
					if(linkParams.length >= 4){//Custom Factory name desired
						pamLink = pam.addLink(linkParams[3].trim(), source, sink,
								PerceptualAssociativeMemoryImpl.PARENT_LINK_CATEGORY);
					}else{
						pamLink = pam.addDefaultLink(source, sink,
								PerceptualAssociativeMemoryImpl.PARENT_LINK_CATEGORY);
					}
					if (pamLink == null) {
						logger.log(Level.WARNING, "Bad link specification, unable to create link: {1}",
									new Object[]{TaskManager.getCurrentTick(),linkDef});
					} else if (linkParams.length >= 3) {
						parseBaseLevelActivation(linkParams[2],pamLink);
					}
				} else {
					logger.log(Level.WARNING, "Could not find source or sink: {1}",
							new Object[]{TaskManager.getCurrentTick(),linkDef});
				}
			}
		}
	}

	/**
	 * Parses a base-level activation for specified learnable.
	 * @param param a {@link String} to be parsed.
	 * @param learnable the {@link Learnable} whose base-level activation will be set.
	 */
	protected static void parseBaseLevelActivation(String param, Learnable learnable) {
		double blActivation = Learnable.DEFAULT_BASE_LEVEL_ACTIVATION;
		try{
			blActivation = Double.parseDouble(param);
		}catch(NumberFormatException e){
			logger.log(Level.WARNING,
					"NumberFormatException parsing base-level activation for Learnable: {1}.",
					new Object[]{TaskManager.getCurrentTick(),learnable});
		}
		learnable.setBaseLevelActivation(blActivation);
	}
}