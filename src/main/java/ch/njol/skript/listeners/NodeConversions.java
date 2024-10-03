package ch.njol.skript.listeners;

import ch.njol.skript.config.*;
import ch.njol.skript.events.bukkit.PreScriptLoadEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.util.*;

public class NodeConversions implements Listener {

	@EventHandler
	public void onNodeConversion(PreScriptLoadEvent e) {
		List<Node> nodesToRemove = new ArrayList<>();
		List<Node> nodesToAdd = new ArrayList<>();
		for (Config item : e.getScripts()) {
			for (Node node : item.getMainNode()) {
				if (!node.getKey().equals("}")) {
					processSectionNode((SectionNode) node);
					nodesToAdd.add(node);
				}
				nodesToRemove.add(node);
			}
			for (Node node : nodesToRemove) {
				item.getMainNode().remove(node);
			}
			for (Node node : nodesToAdd) {
				item.getMainNode().add(node);
			}
		}
	}

	public void processSectionNode(SectionNode sectionNode) {
		List<Node> newNodes = new ArrayList<>();
		List<Node> originalNodes = new ArrayList<>();
		for (Node node : sectionNode) {
			if (!node.getKey().equals("}")) {
				newNodes.add(node);
			}
			originalNodes.add(node);
		}
		for (Node node : originalNodes) {
			sectionNode.remove(node);
		}
		for (Node node : newNodes) {
			sectionNode.add(node);
		}
		newNodes.clear();
	}
}
