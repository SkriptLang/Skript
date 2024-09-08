package ch.njol.skript.listeners;

import ch.njol.skript.Skript;
import ch.njol.skript.config.*;
import ch.njol.skript.events.bukkit.PreScriptLoadEvent;
import ch.njol.util.Kleenean;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PreScriptLoadEventTest implements Listener {

	@EventHandler
	public void onTestScriptLoad(PreScriptLoadEvent e) {
//		List<Node> nodesToRemove = new ArrayList<>();
//		List<Node> nodesToAdd = new ArrayList<>();
		for (Config item: e.getScripts()) {
			for (Node node : item.getMainNode()) {

				processSectionNode((SectionNode) node);

//				Skript.adminBroadcast("Node: " + node.getKey());
//				if (node instanceof SectionNode && containsBracket(node.getKey()) != Kleenean.FALSE) {
//					parentNodes.add((SectionNode) node);
//					processSectionNode((SectionNode) node);
//					parentNodes.clear();
//				} else {
//					if (containsBracket(node.getKey()) == Kleenean.TRUE) {
//						SectionNode newSectionNode = transformBrackets(Kleenean.TRUE, node);
//						parentNodes.add(newSectionNode);
//						nodesToRemove.add(node);
//						nodesToAdd.add(newSectionNode);
//						processSectionNode(newSectionNode);
//
//					} else {
//						transformBrackets(Kleenean.UNKNOWN, node);
//						currentNodes.clear();
//						nodesToRemove.add(node);
//						if (!parentNodes.isEmpty()) {
//							parentNodes.removeLast();
//						}
//					}
//				}
			}
//			for (Node node : nodesToRemove) {
//				item.getMainNode().remove(node);
//			}
//			for (Node node : nodesToAdd) {
//				item.getMainNode().add(node);
//			}
//			nodesToRemove.clear();
//			nodesToAdd.clear();
		}

	}

//	ArrayList<Node> currentNodes = new ArrayList<>();
//	ArrayList<SectionNode> parentNodes = new ArrayList<>();
//	ArrayList<Node> newNodes = new ArrayList<>();

	public void processSectionNode(SectionNode sectionNode) {
//		int c = 0;

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


//		ArrayList<Node> newNodes = new ArrayList<>();
//		ArrayList<Node> originalNodes = new ArrayList<>();
//		for (Node node : sectionNode) {
//			if (node instanceof SimpleNode && (isElseIf(node.getKey()) | containsBracket(node.getKey()) != Kleenean.FALSE )) {
//				if (isElseIf(node.getKey())) {
//					SectionNode newSectionNode = transformConditionalNode(node);
//					currentNodes.add(newSectionNode);
//				} // else {
////					if (containsBracket(node.getKey()) == Kleenean.TRUE) {
////						SectionNode newSectionNode = transformBrackets(Kleenean.TRUE, node);
////						newNodes.add(newSectionNode);
////						parentNodes.add(newSectionNode);
////					} else if (containsBracket(node.getKey()) == Kleenean.UNKNOWN) {
////						transformBrackets(Kleenean.UNKNOWN, node);
////						currentNodes.clear();
////						parentNodes.removeLast();
////					} else {
////						currentNodes.add(node);
////					}
////				}
//			} else if (node instanceof SectionNode) {
//				processSectionNode((SectionNode) node);
//				currentNodes.add(node);
//			} else {
//				currentNodes.add(node);
//			}
//			originalNodes.add(node);
//		}
//		for (Node node : originalNodes) {
//			sectionNode.remove(node);
//		}
//		for (Node node : newNodes) {
//			sectionNode.add(node);
//		}

//		for (Node node : sectionNode) {
//			if (node instanceof SimpleNode && isElseIf(node.getKey())) {
//
//				Skript.adminBroadcast("Node: " + node.getKey()); // Debugging purposes, relatively useless right now though
//			} else if (node instanceof SectionNode) {
//				processSectionNode((SectionNode) node);
//			}
//			c++;
//		}
		}


//	private SectionNode getParent(Integer... line) {
//		if (!parentNodes.isEmpty()) {
//			return (SectionNode) parentNodes.getLast();
//		} else {
//			Skript.error("Extraneus indentation '}' on line: " + line);
//		}
//		return null;
//	}

	private Kleenean containsBracket(String key) {
		return Kleenean.FALSE;
//		Pattern patternIndentBracket = Pattern.compile("\\{$");
//		Matcher matcherIndentBracket = patternIndentBracket.matcher(key);
//		Pattern patternExitBracket = Pattern.compile("^}");
//		Matcher matcherExitBracket = patternExitBracket.matcher(key);
//		if (matcherIndentBracket.find()) {
//			return Kleenean.TRUE;
//		}
//		if (matcherExitBracket.find()) {
//			return Kleenean.UNKNOWN;
//		}
//		return Kleenean.FALSE;
	}

	private SectionNode transformConditionalNode(Node node) {
		String condKey = node.getKey().split("(\\) (.+))", Pattern.CASE_INSENSITIVE)[0] + ")";
		String effKey = node.getKey().split("(else )?if (\\((.+)\\) )", Pattern.CASE_INSENSITIVE)[1];
		SectionNode newSectionNode = new SectionNode(condKey,"", node.getParent(), node.getLine());
		SimpleNode simpleNode = new SimpleNode(effKey, "", node.getLine(), newSectionNode);
		newSectionNode.add(simpleNode);
		return newSectionNode;
	}

//	private SectionNode transformBrackets(Kleenean type, Node node) {
//		if (type == Kleenean.TRUE) {
//			String newKey = node.getKey();
//			Skript.adminBroadcast("Opening bracket: " + newKey);
//			newKey = newKey.split("\\{$")[0];
//			newKey = newKey.trim();
//			Skript.adminBroadcast("newKey: " + newKey);
//			SectionNode newSectionNode = new SectionNode(newKey,"", node.getParent(), node.getLine());
//			return newSectionNode;
//
//		} else if (type == Kleenean.UNKNOWN) {
//
//			Skript.adminBroadcast("Closing bracket (line): " + node.getLine());
//
//			for (Node oldNode : currentNodes) {
//
//				Node newNode = null;
//				if (oldNode instanceof SimpleNode) {
//					newNode = new SimpleNode(oldNode.getKey(),"", oldNode.getLine(), getParent(node.getLine()));
//				} else if (oldNode instanceof SectionNode) {
//					newNode = new SectionNode(oldNode.getKey(), "", getParent(node.getLine()), oldNode.getLine());
//				} else if (oldNode instanceof EntryNode) {
//					newNode = new EntryNode(oldNode.getKey(), ((EntryNode) oldNode).getValue(), "", getParent(node.getLine()), oldNode.getLine());
//				}
//
////				} else if (node instanceof VoidNode) {
////					newNode = new VoidNode(oldNode.getKey(), "", getParent(), oldNode.getLine());
////				}
//
//				newNodes.add(newNode);
//
//			}
//			return null;
//		}
//		return null;
//	}

	private boolean isElseIf(String key) {
		Pattern pattern = Pattern.compile("((else )?if \\(.+\\) (.+))", Pattern.CASE_INSENSITIVE);
		Matcher matcher = pattern.matcher(key);
		return matcher.find();
	}
}
