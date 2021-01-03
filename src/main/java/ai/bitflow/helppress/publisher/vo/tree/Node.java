package ai.bitflow.helppress.publisher.vo.tree;

import java.util.List;

import lombok.Data;

/**
 * 도움말 트리를 구성하는 하나의 노드
 * @author metho
 */
@Data
public class Node implements Comparable<Node> {
	/*
	"folder": true,
	"key": "00038",
	"partsel": false,
	"selected": false,
	"title": "새 폴더"
	*/
	private Boolean folder;
	private String key;
	private String title;
	private List<Node> children;
	private int order;
	// private Boolean expanded;
	
	public Node(String key, String title, Boolean folder) {
		this.key = key;
		this.title = title;
		this.folder = folder;
	}
	
	@Override
	public int compareTo(Node other) {
//		if (this.order!=other.order) {
			if (this.order < other.order) {
				return -1;
			} else if (this.order > other.order) {
				return 1;
			} else {
				return this.title.compareTo(other.title);
//				return 0;
			}
//		} else {
//		}
	}
	
}
