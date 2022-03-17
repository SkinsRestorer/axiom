package net.skinsrestorer.axiom;

import lombok.AllArgsConstructor;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.jetbrains.annotations.Nullable;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.nodes.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@AllArgsConstructor
public class AxiomConfigurationSection {
	protected final Yaml yaml;
	protected MappingNode rootNode;

	//
	// Accessors
	//
	public Node getNode(String path) {
		String[] parts = path.split("\\.");
		AxiomConfigurationSection node = this;
		int i = 0;
		for (String part : parts) {
			Node subNode = node.getDirectSubNode(part);

			if (i == parts.length - 1) {
				return subNode;
			} else if (subNode instanceof MappingNode) {
				node = new AxiomConfigurationSection(yaml, (MappingNode) subNode);
			} else {
				return null;
			}

			i++;
		}

		return null;
	}

	public List<String> getKeys() {
		List<String> keys = new ArrayList<>();
		for (NodeTuple tuple : rootNode.getValue()) {
			if (tuple.getKeyNode() instanceof ScalarNode) {
				ScalarNode keyNode = (ScalarNode) tuple.getKeyNode();
				keys.add(keyNode.getValue());
			}
		}
		return keys;
	}

	protected Node getDirectSubNode(String name) {
		for (NodeTuple tuple : rootNode.getValue()) {
			if (tuple.getKeyNode() instanceof ScalarNode) {
				ScalarNode keyNode = (ScalarNode) tuple.getKeyNode();
				if (keyNode.getValue().equals(name)) {
					return tuple.getValueNode();
				}
			}
		}
		return null;
	}

	public AxiomConfigurationSection getSection(String path) {
		Node node = getNode(path);
		if (node instanceof MappingNode) {
			return new AxiomConfigurationSection(yaml, (MappingNode) node);
		} else {
			return null;
		}
	}

	public String getString(String path) {
		Node node = getNode(path);
		if (node instanceof ScalarNode) {
			return ((ScalarNode) node).getValue();
		} else {
			return null;
		}
	}

	public Integer getInt(String path) throws NumberFormatException {
		String value = getString(path);
		if (value != null) {
			return Integer.parseInt(value);
		} else {
			return null;
		}
	}

	public Boolean getBoolean(String path) {
		String value = getString(path);
		if (value != null) {
			return Boolean.parseBoolean(value);
		} else {
			return null;
		}
	}

	public List<String> getStringList(String path) {
		Node node = getNode(path);
		if (node instanceof SequenceNode) {
			List<String> list = new ArrayList<>();
			SequenceNode sequenceNode = (SequenceNode) node;
			for (Node valueNode : sequenceNode.getValue()) {
				if (valueNode instanceof ScalarNode) {
					ScalarNode scalarNode = (ScalarNode) valueNode;
					list.add(scalarNode.getValue());
				}
			}
			return list;
		} else {
			return null;
		}
	}

	public void set(String path, Object value) {
		String[] parts = path.split("\\.");
		String target = parts[parts.length - 1];
		System.out.println("Setting " + target + " to " + value);
		String parentPath = path.substring(0, path.length() - target.length());

		NodeTuple keyNode = null;
		Node parentNode = getNode(parentPath);
		if (parentNode instanceof MappingNode) {
			MappingNode mappingNode = (MappingNode) parentNode;
			for (NodeTuple tuple : mappingNode.getValue()) {
				if (tuple.getKeyNode() instanceof ScalarNode) {
					ScalarNode scalarKeyNode = (ScalarNode) tuple.getKeyNode();
					if (scalarKeyNode.getValue().equals(target)) {
						keyNode = tuple;
					}
				}
			}
		}

		set(path, createTuple(target, value, keyNode));
	}

	public void set(String path, NodeTuple value) {
		String[] parts = path.split("\\.");
		try {
			MappingNode node = rootNode;
			int i = 0;
			for (String part : parts) {
				if (i < parts.length - 1) {
					Optional<NodeTuple> optionalTuple = node.getValue().stream().filter(tuple -> {
						if (tuple.getKeyNode() instanceof ScalarNode) {
							ScalarNode scalarKeyNode = (ScalarNode) tuple.getKeyNode();
							return scalarKeyNode.getValue().equals(part);
						}
						return false;
					}).findFirst();

					if (optionalTuple.isPresent()) {
						if (optionalTuple.get().getValueNode() instanceof MappingNode) {
							node = (MappingNode) optionalTuple.get().getValueNode();
						} else {
							throw new Exception("Invalid node type");
						}
					} else {
						MappingNode newMapping = (MappingNode) yaml.represent(Collections.emptyMap());
						node.getValue().add(createTuple(part, newMapping));
						node = newMapping;
					}
				} else {
					if (value != null) {
						if (node.getValue().stream().anyMatch(nodeTuple -> {
							if (nodeTuple.getKeyNode() instanceof ScalarNode) {
								ScalarNode scalarKeyNode = (ScalarNode) nodeTuple.getKeyNode();
								return scalarKeyNode.getValue().equals(part);
							}
							return false;
						})) {
							node.getValue().replaceAll(nodeTuple -> {
								if (nodeTuple.getKeyNode() instanceof ScalarNode) {
									ScalarNode scalarKeyNode = (ScalarNode) nodeTuple.getKeyNode();
									if (scalarKeyNode.getValue().equals(part)) {
										return value;
									}
								}
								return nodeTuple;
							});
						} else {
							node.getValue().add(value);
						}
					} else {
						node.getValue().removeIf(tuple -> {
							if (tuple.getKeyNode() instanceof ScalarNode) {
								ScalarNode scalarKeyNode = (ScalarNode) tuple.getKeyNode();
								return scalarKeyNode.getValue().equals(part);
							}
							return false;
						});
					}
				}

				i++;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	protected void setComments(Node currentNode, Node defaultNode, boolean overWrite) {
		if (overWrite) {
			currentNode.setBlockComments(defaultNode.getBlockComments());
			currentNode.setInLineComments(defaultNode.getInLineComments());
			currentNode.setEndComments(defaultNode.getEndComments());
		} else {
			if (currentNode.getBlockComments() == null) {
				currentNode.setBlockComments(defaultNode.getBlockComments());
			}
			if (currentNode.getInLineComments() == null) {
				currentNode.setInLineComments(defaultNode.getInLineComments());
			}
			if (currentNode.getEndComments() == null) {
				currentNode.setEndComments(defaultNode.getEndComments());
			}
		}
	}

	private NodeTuple createTuple(Node key, Object value, @Nullable NodeTuple previousTuple) {
		if (value instanceof String) {
			if (NumberUtils.isParsable((String) value)) {
				value = NumberUtils.createNumber((String) value);
			} else if (BooleanUtils.toBooleanObject((String) value) != null) {
				value = BooleanUtils.toBoolean((String) value);
			}
		}

		return createTuple(key, yaml.represent(value), previousTuple);
	}

	private NodeTuple createTuple(Object key, Node value) {
		return createTuple(yaml.represent(key), value, null);
	}

	private NodeTuple createTuple(Node key, Node value, @Nullable NodeTuple previousTuple) {
		if (previousTuple != null) {
			setComments(key, previousTuple.getKeyNode(), true);
			setComments(value, previousTuple.getValueNode(), true);
		}

		if (value instanceof SequenceNode) {
			SequenceNode sequenceNode = (SequenceNode) value;
			int i = 0;
			for (Node node : new ArrayList<>(sequenceNode.getValue())) {
				if (node instanceof ScalarNode) {
					ScalarNode scalarNode = (ScalarNode) node;

					sequenceNode.getValue().set(i,
							new ScalarNode(
									scalarNode.getTag(),
									scalarNode.getValue(),
									scalarNode.getStartMark(),
									scalarNode.getEndMark(),
									DumperOptions.ScalarStyle.DOUBLE_QUOTED));
				}
				i++;
			}
		}

		return new NodeTuple(key, value);
	}

	private NodeTuple createTuple(Object key, Object value, @Nullable NodeTuple previousTuple) {
		return createTuple(yaml.represent(key), value, previousTuple);
	}
}
