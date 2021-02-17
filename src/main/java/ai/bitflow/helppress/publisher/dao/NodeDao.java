package ai.bitflow.helppress.publisher.dao;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import ai.bitflow.helppress.publisher.domain.ContentsGroup;
import ai.bitflow.helppress.publisher.repository.ContentsGroupRepository;
import ai.bitflow.helppress.publisher.vo.req.DeleteNodeReq;
import ai.bitflow.helppress.publisher.vo.req.NewNodeReq;
import ai.bitflow.helppress.publisher.vo.req.UpdateNodeReq;
import ai.bitflow.helppress.publisher.vo.tree.Node;

@Component
public class NodeDao {

	private final Logger logger = LoggerFactory.getLogger(NodeDao.class);

	@Autowired
	private ContentsGroupRepository grepo;
	
	public boolean addNode(NewNodeReq params) {
		// Todo: 트리구조 저장
		Optional<ContentsGroup> row = grepo.findById(params.getGroupId());
		ContentsGroup item1 = null;
		if (!row.isPresent()) {
			return false;
		} else {
			item1 = row.get();
			List<Node> tree = new Gson().fromJson(item1.getTree(), new TypeToken<List<Node>>(){}.getType());
			if (tree==null) {
				tree = new ArrayList<Node>();
			}
			if (params.getParentKey()==null) {
				Node node = new Node(params.getKey(), params.getTitle(), params.getFolder());
				tree.add(node);
				// logger.debug("found parent to add #1");
			} else {
				findNodeAndAdd(tree, params);
			}
			String treeStr = new Gson().toJson(tree);
			// logger.debug("tree " + treeStr);
			item1.setTree(treeStr);
			grepo.save(item1);
		}
		return false;
	}
	
	private void findNodeAndAdd(List<Node> nodes, NewNodeReq params) {
		if (nodes!=null && nodes.size()>0) {
			for (Node item : nodes) {
				if (item.getKey().equals(params.getParentKey())) {
					Node node = new Node(params.getKey(), params.getTitle(), params.getFolder());
					if (item.getChildren()==null) {
						item.setChildren(new ArrayList<Node>());
					}
					item.getChildren().add(node);
					logger.debug("found parent to add #2");
					break;
				} else {
					if (item.getChildren()!=null && item.getChildren().size()>0) {
						findNodeAndAdd(item.getChildren(), params);
					}
				}
			}
		}
	}
	
	/**
	 * 노드 키 변경
	 * @param params
	 * @return
	 */
	public boolean updateNodeKey(String groupId, String before, String after) {
		Optional<ContentsGroup> row = grepo.findById(groupId);
		ContentsGroup item1 = null;
		boolean found = false;
		if (!row.isPresent()) {
			return false;
		} else {
			item1 = row.get();
			List<Node> tree = new Gson().fromJson(item1.getTree(), new TypeToken<List<Node>>(){}.getType());
			findNodeAndUpdate(tree, before, after);
			String treeStr = new Gson().toJson(tree);
			item1.setTree(treeStr);
			grepo.save(item1);
			return found;
		}
	}
	
	/**
	 * 트리 노드 삭제
	 * @param params
	 * @return
	 */
	public boolean deleteNodeByKey(DeleteNodeReq params) {
		Optional<ContentsGroup> row = grepo.findById(params.getGroupId());
		ContentsGroup item1 = null;
		boolean found = false;
		if (!row.isPresent()) {
			return false;
		} else {
			item1 = row.get();
			List<Node> tree = new Gson().fromJson(item1.getTree(), new TypeToken<List<Node>>(){}.getType());
			findNodeAndDelete(tree, params.getKey());
			String treeStr = new Gson().toJson(tree);
			item1.setTree(treeStr);
			grepo.save(item1);
			return found;
		}
	}
	
	private void findNodeAndUpdate(List<Node> nodes, String before, String after) {
		if (nodes!=null && nodes.size()>0) {
			for (Node item : nodes) {
				if (item.getKey().equals(before)) {
					item.setKey(after);
					logger.debug("found item to delete");
					break;
				} else {
					if (item.getChildren()!=null && item.getChildren().size()>0) {
						findNodeAndUpdate(item.getChildren(), before, after);
					}
				}
			}
		}
	}
	
	private void findNodeAndDelete(List<Node> nodes, String key) {
		if (nodes!=null && nodes.size()>0) {
			for (Node item : nodes) {
				if (item.getKey().equals(key)) {
					nodes.remove(item);
					logger.debug("found item to delete");
					break;
				} else {
					if (item.getChildren()!=null && item.getChildren().size()>0) {
						findNodeAndDelete(item.getChildren(), key);
					}
				}
			}
		}
	}
	
	
	public boolean updateNodeOrder(UpdateNodeReq params) {
		// Todo: 트리구조 저장
		Optional<ContentsGroup> row = grepo.findById(params.getGroupId());
		ContentsGroup item1 = null;
		Node foundNode = null;
		if (!row.isPresent()) {
			return false;
		} else {
			item1 = row.get();
			List<Node> tree = new Gson().fromJson(item1.getTree(), new TypeToken<List<Node>>(){}.getType());
			foundNode = findFromChildren(tree, params);
			Node foundParent = findParent(tree, params);
			List<Node> children = null;
			if (foundParent==null) {
				children = tree;
			} else {
				children = foundParent.getChildren();
			}
			children.add((int)params.getIndex(), foundNode);
			//logger.debug("foundNode: " + foundNode.getKey() + " appending to: parent[" + params.getIndex()+ "] ");
			String treeStr = new Gson().toJson(tree);
			//logger.debug("tree " + treeStr);
			item1.setTree(treeStr);
			grepo.save(item1);
		}
		return foundNode!=null;
	}
	
	private Node findFromChildren(List<Node> nodes, UpdateNodeReq params) {
		Node ret = null;
		if (nodes!=null && nodes.size()>0) {
			for (Node item : nodes) {
				if (item.getKey().equals(params.getKey())) {
					ret = item;
					nodes.remove(item);
					return ret;
				} else if (item.getChildren()!=null && item.getChildren().size()>0) {
					ret = findFromChildren(item.getChildren(), params);
					if (ret!=null) {
						return ret;
					}
				}
			}
		}
		return ret;
	}
	
	private Node findParent(List<Node> nodes, UpdateNodeReq params) {
		Node ret = null;
		if (nodes!=null && nodes.size()>0) {
			for (Node item : nodes) {
				if (item.getKey().equals(params.getParentKey())) {
					ret = item;
					return ret;
				} else if (item.getChildren()!=null && item.getChildren().size()>0) {
					ret = findFromChildren(item.getChildren(), params);
					if (ret!=null) {
						return ret;
					}
				}
			}
		}
		return ret;
	}
	
	public boolean replaceTitleByKey(UpdateNodeReq params) {
		Optional<ContentsGroup> row = grepo.findById(params.getGroupId());
		ContentsGroup item1 = null;
		boolean found = false;
		if (!row.isPresent()) {
			return false;
		} else {
			item1 = row.get();
			List<Node> tree = new Gson().fromJson(item1.getTree(), new TypeToken<List<Node>>(){}.getType());
			for (Node item2 : tree) {
				found = findNodeAndReplace(item2, params.getKey(), params.getTitle());
				if (found) {
					break;
				}
			}
			if (found) {
				String treeStr = new Gson().toJson(tree);
				item1.setTree(treeStr);
				grepo.save(item1);
			}
			return found;
		}
	}
	
	private boolean findNodeAndReplace(Node node, String key, String title) {
		
		boolean found = false;
		
		if (node.getKey().equals(key)) {
			node.setTitle(title);
			return true;
		} else {
			if (node.getChildren()!=null && node.getChildren().size()>0) {
				for (Node item : node.getChildren()) {
					found = findNodeAndReplace(item, key, title);
					if (found) {
						break;
					}
				}
				return found;
			}
			return false;			
		}
	}
	
	public String getGroupTitle(UpdateNodeReq params) {
		Optional<ContentsGroup> row = grepo.findById(params.getGroupId());
		ContentsGroup item1 = null;
		if (!row.isPresent()) {
			return null;
		} else {
			item1 = row.get();
			return item1.getName();
		}
	}
	
}
