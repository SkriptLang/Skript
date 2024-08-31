package ch.njol.skript.listeners;

import ch.njol.skript.Skript;
import ch.njol.skript.config.Config;
import ch.njol.skript.config.Node;
import ch.njol.skript.config.SectionNode;
import ch.njol.skript.config.SimpleNode;
import ch.njol.skript.events.bukkit.PreScriptLoadEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PreScriptLoadEventTest implements Listener {

	@EventHandler
	public void onTestScriptLoad(PreScriptLoadEvent e) {
		for (Config item: e.getScripts()) {
			for (Node node : item.getMainNode()) {
				processSectionNode((SectionNode) node);
			}
		}
	}

	public void processSectionNode(SectionNode sectionNode) {
		int c = 0;
		for (Node node : sectionNode) {
			if (node instanceof SimpleNode && isElseIf(node.getKey())) {
				String condKey = node.getKey().split("(\\) (.+))", Pattern.CASE_INSENSITIVE)[0] + ")";
				String effKey = node.getKey().split("(else )?if (\\((.+)\\) )", Pattern.CASE_INSENSITIVE)[1];
				SectionNode newSectionNode = new SectionNode(condKey,"", node.getParent(), node.getLine());
				SimpleNode simpleNode = new SimpleNode(effKey, "", node.getLine(), newSectionNode);
				newSectionNode.add(simpleNode);
				sectionNode.remove(node);
				sectionNode.insert(newSectionNode, c);
				Skript.adminBroadcast("Node: " + node.getKey()); // Debugging purposes, relatively useless right now though
			} else if (node instanceof SectionNode) {
				processSectionNode((SectionNode) node);
			}
			c++;
		}
	}

	private boolean isElseIf(String key) {
		Pattern pattern = Pattern.compile("((else )?if \\(.+\\) (.+))", Pattern.CASE_INSENSITIVE);
		Matcher matcher = pattern.matcher(key);
		if (matcher.find()) {
			return true;
		}
		return false;
	}
}
