package ch.njol.skript.listeners;

import ch.njol.skript.config.*;
import ch.njol.skript.events.bukkit.PreScriptLoadEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PreScriptLoadEventTest implements Listener {

	@EventHandler
	public void onTestScriptLoad(PreScriptLoadEvent e) {
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
			if (node instanceof SimpleNode && isElseIf(node.getKey())) {
				SectionNode newSectionNode = transformConditionalNode(node);
				newNodes.add(newSectionNode);
			} else if (node instanceof SectionNode) {
				processSectionNode((SectionNode) node);
				newNodes.add(node);
			} else {
				if (!node.getKey().equals("}")) {
					newNodes.add(node);
				}
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

	private SectionNode transformConditionalNode(Node node) {
		String condKey = node.getKey().split("(\\) (.+))", Pattern.CASE_INSENSITIVE)[0] + ")";
		String effKey = node.getKey().split("(else )?if (\\((.+)\\) )", Pattern.CASE_INSENSITIVE)[1];
		SectionNode newSectionNode = new SectionNode(condKey,"", node.getParent(), node.getLine());
		SimpleNode simpleNode = new SimpleNode(effKey, "", node.getLine(), newSectionNode);
		newSectionNode.add(simpleNode);
		return newSectionNode;
	}


	private boolean isElseIf(String key) {
		Pattern pattern = Pattern.compile("((else )?if \\(.+\\) (.+))", Pattern.CASE_INSENSITIVE);
		Matcher matcher = pattern.matcher(key);
		return matcher.find();
	}
}
