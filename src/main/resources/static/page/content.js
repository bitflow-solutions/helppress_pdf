var editor, selectedGroupId, selectedContentId, selectedContentTitle, htmlToOpen = null, isRTF = false;
var SOURCE = [];
var _tree = null;
const URL_API_NODE = "/api/v1/ecm/node";

function initTree() {
  $('#tree').fancytree({
    extensions: ["edit", "dnd5"],
    checkbox: false,
    selectMode: 3,
    source: SOURCE,
    nodata: '도움말이 없습니다',
    edit: {
      triggerStart: ["f2"],
      adjustWidthOfs: 4,   // null: don't adjust input size to content
      inputCss: { minWidth: "120px" },
      save: function(e, data) {
      	/*console.log('save ' + e + ' ' + data);*/
        renameTitle(e, data);
        return true;
      },
      beforeEdit: function(e, data){
		$("span.fancytree-focused .fancytree-title").css("background-color", "inherit");
      },
      beforeClose: function(e, data){
        $("span.fancytree-focused .fancytree-title").css("background-color", "#3875D7");
      }
    },
    dnd5: {
      // --- Drag-support:
      dragStart: function(node, data) {
          data.effectAllowed = "all";
          data.dropEffect = data.dropEffectSuggested;
          return true;
      },
      dragEnd: function(node, data) {
      	console.log("dragEnd: key " + node.key + " parent " + node.parent.key + " idx " + node.getIndex());
      	var parentKey = node.parent.key.startsWith("root")?null:node.parent.key;
      	$.ajax({
			url: URL_API_NODE,
			method: "PUT",
			data: { 
				groupId: selectedGroupId,
				parentKey: parentKey,
				key: node.key,
				index: node.getIndex()
			}
		})
		.done(function(msg) {
		})
		.always(function() {
	    });
      },
      // --- Drop-support:
      dragEnter: function(node, data) {
        node.debug( "T2: dragEnter: " + "data: " + data.dropEffect + "/" + data.effectAllowed +
          ", dataTransfer: " + data.dataTransfer.dropEffect + "/" + data.dataTransfer.effectAllowed, data );
        return true;
      },
      dragOver: function(node, data) {
          data.dropEffect = data.dropEffectSuggested;
      },
      dragDrop: function(node, data) {
        var newNode,
          transfer = data.dataTransfer,
          sourceNodes = data.otherNodeList,
          mode = data.dropEffect;
        // don't open links, files, ... even if an error occurs in this handler:
        data.originalEvent.preventDefault();
        if( data.hitMode === "after" ){
          // If node are inserted directly after tagrget node one-by-one,
          // this would reverse them. So we compensate:
          sourceNodes.reverse();
        }
        if (data.otherNode) {
          // Drop another Fancytree node from same frame
          // (maybe from another tree however)
          var sameTree = data.otherNode.tree === data.tree;
          if (mode === "move") {
            data.otherNode.moveTo(node, data.hitMode);
          } else {
            newNode = data.otherNode.copyTo(node, data.hitMode);
            if (mode === "link") {
              newNode.setTitle("Link to " + newNode.title);
            } else {
              newNode.setTitle("Copy of " + newNode.title);
            }
          }
        } else if (data.otherNodeData) {
          // Drop Fancytree node from different frame or window, so we only have
          // JSON representation available
          node.addChild(data.otherNodeData, data.hitMode);
        } else if (data.files.length) {
          // Drop files
          for(var i=0; i<data.files.length; i++) {
            var file = data.files[i];
            node.addNode( { title: "'" + file.name + "' (" + file.size + " bytes)" }, data.hitMode );
          }

        } else {
          // Drop a non-node
          node.addNode({
              title: transfer.getData("text"),
            }, data.hitMode
          );
        }
        node.setExpanded();
      },
    },
    activate: function(e, data){
	  var node = data.node;
	  selectedContentId = node.key;
	  if (!node.folder || node.folder===false) {
		// 폴더가 아닌경우
	    selectedContentTitle = node.title;
	  	$("#btn-delete").show();
	  	$("#btn-download").show();
	  	$("#btn-pdf-upload").show();
		/*	  	
		$("#btn-modify").show();
	  	$("#btn-modify-complete").hide();
		*/
	  	// 도움말 표시
	  	loadPage(node.key);
		$("#contents-detail").scrollTop();
	  } else {
	    // 폴더인 경우
	  	$("#btn-delete").show();
	  	$("#btn-download").hide();
	  	$("#btn-pdf-upload").hide();
		/*
	  	$("#btn-modify").hide();
	  	$("#btn-modify-complete").hide();
		*/
	  }
	}
  });
  _tree = $.ui.fancytree.getTree();
}

function initEvents() {
	$("#btn-modify").click(editContent);
	$("#btn-delete").click(deleteContent);
	$("#btn-download").click(downloadContent);
	$("#btn-expand-all").click(expandAll);
	$("#btn-collapse-all").click(collapseAll);
	$("#btn-modify-complete").click(function(e) {
		// 도움말 수정완료 버튼 클릭
		$("#btn-modify-complete").hide();
		$(".spinner").show();
		var url = "/api/v1/ecm/content/" + selectedContentId;
		$.ajax({
			url: url,
			method: "PUT",
			data: { 
				title: selectedContentTitle,
				content: editor.getPublishingHtml(),
			}
		})
		.done(function(msg) {
		  if (msg.result) {
			  var key = msg.result.key;
			  console.log('key ' + key);
			  if (key && key.length>0) {
				loadPage(key);
			  }
		  }
		  $("#btn-modify").show();
		  $("#btn-delete").show();
		  $("#btn-download").show();
		  alert('수정하였습니다');
		})
		.always(function() {
			setTimeout(function() {
				$(".spinner").hide();
			}, 500);
	    });
	});
	
	// (1) 파일 타입
	$.contextMenu({
	    selector: ".fancytree-ico-c > .fancytree-title",
	    callback: function(key, options) {
        },
	    items: {
	        rename: {name: "제목변경 [F2]", callback: editTitle },
	        /*modify: {name: "수정", callback: editContent },*/
	        deletecontent: {name: "삭제", callback: deleteContent },
	        downloadcontent: {name: "다운로드", callback: downloadContent }
	    },
	    events: {
			show : function(options){
			  var key = $(this).attr('key');
			  var node = _tree.getNodeByKey(key);
			  node.setActive();
			  console.log("[" + key + "] file");
	        }           
		}
	});
	// (2) 폴더 타입
	$.contextMenu({
	    selector: ".fancytree-folder > .fancytree-title",
	    callback: function(key, options) {
        },
	    items: {
	        rename: {name: "제목변경 [F2]", callback: editTitle },
	        newfolder: {name: "새 폴더", callback: appendChildFolder },
	        newcontent: {name: "새 도움말", callback: appendChildContent },
	        deleteContent: {name: "삭제", callback: deleteContent }
	    },
	    events: {
			show : function(options){
			  var key = $(this).attr('key');
			  var node = _tree.getNodeByKey(key);
			  node.setActive();
			  console.log("[" + key + "] folder");
	        }           
		}
	});
	$.contextMenu({
		selector: "#tree",
		items: {
		  newfolder:  {name: "새 폴더",  callback: appendRootFolder },
		  newcontent: {name: "새 도움말", callback: appendRootContent }
		}
	});
}

function fallbackCopyTextToClipboard(text) {
  var textArea = document.createElement("textarea");
  textArea.value = text;
  
  // Avoid scrolling to bottom
  textArea.style.top = "0";
  textArea.style.left = "0";
  textArea.style.position = "fixed";

  document.body.appendChild(textArea);
  textArea.focus();
  textArea.select();

  try {
    var successful = document.execCommand('copy');
    var msg = successful ? 'successful' : 'unsuccessful';
    console.log('Fallback: Copying text command was ' + msg);
  } catch (err) {
    console.error('Fallback: Oops, unable to copy', err);
  }

  document.body.removeChild(textArea);
}

function copyTextToClipboard(text) {
  if (!navigator.clipboard) {
    fallbackCopyTextToClipboard(text);
    return;
  }
  navigator.clipboard.writeText(text).then(function() {
  }, function(err) {
    console.error('Async: Could not copy text: ', err);
  });
}

function lazyOpenHtml() {
	editor.openHTML(htmlToOpen);
}

function initEditor() {
  synapEditorConfig['editor.type'] = 'document';
  editor = new SynapEditor('synapEditor', synapEditorConfig);
  editor.setEventListener('beforeUploadImage', function (e) {
    e.addParameter('key', selectedContentId);
	console.log("beforeUploadImage " + JSON.stringify(e));
  });
  editor.setEventListener('afterUploadImage', function (e) {
    console.log('afterUploadImage');
  });
  editor.setEventListener('beforeOpenDocument', function (e) {
	e.addParameter('key', selectedContentId);
	console.log("beforeOpenDocument " + JSON.stringify(e));
  });
  editor.setEventListener('beforePaste', function(data) {
    console.log("beforePaste");
  });
  editor.setEventListener('afterPaste', function(data) {
    console.log("afterPaste");
  });
}

function findParents(node) {
	if (typeof(node.parent)!=='undefined' && node.parent!==null) {
		node.parent.setExpanded();
		findParents(node.parent);
	}
}

function saveTree() {
  $(".spinner").show();
  $.ajax({
	  url: "/api/v1/ecm/group/" + selectedGroupId,
	  method: "GET"
  	})
	.done(function(msg) {
	  if (msg.status==401) {
	  	location.href = "/logout";
	  } else {
		  _tree.reload(msg.result.tree).done(function() {
		  	if (selectedContentId) {
		  	  try {
			    var node = _tree.getNodeByKey(selectedContentId);
			    if (node.folder && node.folder==true) {
			    	node.setExpanded();
			    }
			    findParents(node);
			    node.setActive();
			  } catch(e) {
			    console.log('err ' + e.message);
			  }
		    }
		  });
	  }
	})
	.fail(function() { })
	.always(function() {
		setTimeout(function() {
			$(".spinner").hide();
		}, 300);
	});
}

function editContent() {
  var url = "/api/v1/ecm/content/" + selectedContentId;
  $.ajax({
	url: url,
	method: "GET"
  })
  .done(function(msg) {
    editor.openHTML(msg.result.contents);
    $("#contents-detail").hide();
	$("#editor-wrapper").show();
    $("#btn-modify-complete").show();
    $("#btn-modify").hide();
    $("#btn-download").hide();
    $("#btn-delete").hide();
  });
}

function loadPage(key) {
  $("#editor-wrapper").hide();
  $("#contents-detail").attr("src", "/viewer/viewer.html?file=/" + key + ".pdf");
  $.ajax({
		url: "/" + key + ".pdf",
		method: "GET",
		statusCode: {
	        404: function(responseObject, textStatus, jqXHR) {
	            $("#btn-modify").text("글쓰기");
	            $("#btn-delete").hide();
	            $("#btn-download").hide();
				$("#contents-detail").attr("src", "/404.html");
	        },
	        200: function(responseObject, textStatus, errorThrown) {
	            $("#btn-modify").text("수정");
	            $("#btn-delete").show();
	            $("#btn-download").show();
	        }           
	    }
	});
  
  $("#contents-detail").show();
}

function doUpload() {
  if ($("#bf-content-comment").val().length<1) {
	alert("도움말 변경 사유를 입력해주세요");
  } else {
	$("#bf-modal-comment").foundation('close');
    $("#pdfFile").click();
  }
}

/**
 * 제목변경: checked OK
 */
function renameTitle(e, data) {
	try {
	    var title = data.input.val();
	    var node = data.node;
	    var data = { groupId: selectedGroupId, key: node.key, title: title };
	    if (node.folder && node.folder===true) {
	    	data.folder = true;
	    }
	    $(".spinner").show();
		$.ajax({
			url: URL_API_NODE,
			method: "PUT",
			data: data
		})
		.done(function(msg) {
		  if (msg.status==401) {
		  	location.href = "/logout";
		  } else {
	        saveTree();
		  }
		})
		.always(function() {
			setTimeout(function() {
				$(".spinner").hide();
			}, 500);
	    });
    } catch(e) { console.log(e.message); }
}

function appendChildFolder() {
  
  var parent = _tree.getActiveNode();
  if( !parent ) {
    parent = _tree.getRootNode();
  }
  $.ajax({
	  url: URL_API_NODE,
	  method: "POST",
	  data: { groupId: selectedGroupId, parentKey: parent.key, folder: true }
  	})
	.done(function(msg) {
	  if (msg.status==401) {
	  	location.href = "/logout";
	  } else {
	    // console.log('appendChildFolder - parent ' + parent.key + ' child ' + msg.result.key);
    	var existingNode = _tree.getNodeByKey(msg.result.key);
    	if (!existingNode) {
			var child = { key: msg.result.key, title: msg.result.title, folder: true };
			parent.addNode(child, 'child');
			// parent.setExpanded();
    	}
        saveTree();
	  }
	})
	.fail(function() { })
	.always(function() { });
}

function appendChildContent() {
  
  var parent = _tree.getActiveNode();
  if( !parent ) {
    parent = _tree.getRootNode();
  }
  $.ajax({
	  url: URL_API_NODE,
	  method: "POST",
	  data: { groupId: selectedGroupId, parentKey: parent.key }
  	})
	.done(function(msg) {
	  if (msg.status==401) {
	  	location.href = "/logout";
	  } else {
	    // console.log('appendChildFolder - parent ' + parent.key + ' child ' + msg.result.key);
    	var existingNode = _tree.getNodeByKey(msg.result.key);
    	if (!existingNode) {
			var child = { key: msg.result.key, title: msg.result.title };
			parent.addNode(child, 'child');
			// parent.setExpanded();
    	}
        saveTree();
	  }
	})
	.fail(function() { })
	.always(function() { });  
}

function appendRootFolder() {
  
  var parent = _tree.getRootNode();
  $.ajax({
	  url: URL_API_NODE,
	  method: "POST",
	  data: { groupId: selectedGroupId, folder: true } // parentKey: parent.key, 
  	})
	.done(function(msg) {
	  if (msg.status==401) {
	  	location.href = "/logout";
	  } else {
    	var existingNode = _tree.getNodeByKey(msg.result.key);
    	if (!existingNode) {
			var child = { key: msg.result.key, title: msg.result.title, folder: true };
			parent.addNode(child, 'child');
    	}
    	console.log('tree ' + JSON.stringify(msg.result.tree));
        saveTree();
	  }
	})
	.fail(function() {
	})
	.always(function() {
  });
}

function appendRootContent() {
  
  var parent = _tree.getRootNode();
  $.ajax({
	  url: URL_API_NODE,
	  method: "POST",
	  data: { groupId: selectedGroupId } // , parentKey: parent.key 
  	})
	.done(function(msg) {
	  if (msg.status==401) {
	  	location.href = "/logout";
	  } else {
    	var existingNode = _tree.getNodeByKey(msg.result.key);
    	if (!existingNode) {
			var child = { key: msg.result.key, title: msg.result.title };
			parent.addNode(child, 'child');
    	}
    	console.log('tree ' + JSON.stringify(msg.result.tree));
        saveTree();
	  }
	})
	.fail(function() { })
	.always(function() {
  });
}

function deleteContent() {
	var node = _tree.getActiveNode();
	if (node) {
	  	if (node.folder==true) {
	  	  // 폴더인 경우
	  	  if (confirm("도움말폴더와 하위 도움말들이 삭제됩니다. 진행 하시겠습니까?")) {
		  	  // recursive) node.getChildren();
			  var childContentsArr = [];
			  getChildrenRecursive(node, childContentsArr);
			  if (childContentsArr) {
			  	console.log('childContentsArr ' + JSON.stringify(childContentsArr));
			  }
			  $.ajax({
				  url: URL_API_NODE,
				  method: "DELETE",
				  data: { groupId: selectedGroupId, key: node.key, child: childContentsArr, folder: true, title:  node.title }
			  	})
				.done(function(msg) {
				  if (msg.status==401) {
				  	location.href = "/logout";
				  } else {
				    node.remove();
			        saveTree();
				  }
				})
				.fail(function() {
				})
				.always(function() {
			  });
		  }
	  	} else {
	  	  if (confirm("도움말이 삭제됩니다. 진행 하시겠습니까?")) {
			  $.ajax({
				  url: URL_API_NODE,
				  method: "DELETE",
				  data: { groupId: selectedGroupId, key: node.key, title: node.title }
			  	})
				.done(function(msg) {
				  if (msg.status==401) {
				  	location.href = "/logout";
				  } else {
				  	node.remove();
				  	var firstNode = null;
			        _tree.visit(function(node) {
					  if (typeof(node.folder)=='undefined') {
				          firstNode = node;
			        	  console.log('node ' + node.key + ' ' + node.folder);
			        	  return false;
			          }
					});
					if (firstNode===null) {
		        	  saveTree();
				    } else {
				      saveTree(firstNode);
				    }
				  }
				})
				.fail(function() {
				})
				.always(function() {
			  });
		  }
		}
	}
}

function getChildrenRecursive(node, arr) {
	if (node.folder && node.folder===true) {
	    // 1) if node is folder
		if (node.hasChildren()) {
			var children = node.getChildren();
			for (var i=0; i<children.length; i++) {
			  getChildrenRecursive(children[i], arr);
			}
		}
		return;
	} else {
		// 2) if node is content
		arr.push(node.key);
		return;
	}
}

function downloadContent() {
  var node = _tree.getActiveNode();
  if( !node ) {
    alert("도움말을 선택해주세요");
    return;
  }
  var key = node.key;
  $(".spinner").show();
  $("#ifrm").attr("src", "/api/v1/ecm/release/" + key);
  setTimeout(function() {
	$(".spinner").hide();
  }, 3000);
}

function editTitle() {
  console.log('editTitle');
  var node = _tree.getActiveNode();
  if( !node ) {
    alert("도움말을 선택해주세요");
    return;
  }
  node.editStart();
}

function onSelectChanged(select) {
  selectedGroupId = select.options[select.selectedIndex].value;
  if (!selectedGroupId || selectedGroupId.length<1) {
	  $("#tree").hide();
	  location.href = "#";
	  $(".tree-wrapper").css("background-image", "url(/img/bg-tree-gray.jpg)");
	  $(".contents-wrapper").css("background", "url(/img/bg-contents-gray.jpg)");
	  $("#contents-detail").attr("src", "about:blank");
  } else {
	  $(".tree-wrapper").css("background-image", "inherit");
	  $(".contents-wrapper").css("background", "white");
	  loadTree(selectedGroupId);
  }
}

function loadTree(groupId) {
	$.ajax({
	  url: "/api/v1/ecm/group/" + groupId,
	  method: "GET"
	})
	.done(function(msg) {
	  $("#tree").show();
	  if (msg && msg.result) {
	  	_tree.reload(msg.result.tree);
	  }
	  location.href = "#" + groupId;
	})
	.fail(function() { })
	.always(function() {
		setTimeout(function() {
			$(".spinner").hide();
		}, 300);
	});
}

function expandAll() {
  _tree.expandAll();
}

function collapseAll() {
  _tree.expandAll(false);
}

function initSocket() {
	var socket = new SockJS('ws');
	var stompClient = Stomp.over(socket);
	stompClient.debug = null;
	stompClient.connect({}, function (frame) {
        // console.log('Connected: ' + frame);
        stompClient.subscribe('/group', function (msg) {
          console.log("groupId " + JSON.parse(msg.body).groupId);
          if (selectedGroupId===JSON.parse(msg.body).groupId) {
	        _tree.reload(JSON.parse(JSON.parse(msg.body).tree));
	  	  }
        });
        stompClient.subscribe('/node', function (rawmsg) {
          saveTree();
          /*
          var msg = JSON.parse(rawmsg.body);
          if (selectedGroupId===msg.groupId) {
            if (msg.method=="ADD") {
            	var existingNode = _tree.getNodeByKey(msg.key);
            	// console.log('existingNode ' + existingNode);
            	if (existingNode) {
            	  return;
            	}
            	var parent = _tree.getNodeByKey(msg.parentKey);
            	var child = { key: msg.key, title: msg.title };
            	if (msg.folder!==null) {
            		child["folder"] = msg.folder;
            	} 
            	parent.addNode(child, 'child');
            }else if (msg.method=="DEL") {
			  var node = _tree.getNodeByKey(msg.key);
			  if (node) {
			  	node.remove();
		  	  }
		  	  // Todo: 첫번쨰 노드 선택되도록
			}else if (msg.method=="REN") {
			  var node = _tree.getNodeByKey(msg.key);
			  node.setTitle(msg.title);
            }
	  	  }
	  	  */
        });
    });
}

function handlePdf(file) {
	console.log('file ' + file);
    var node = _tree.getActiveNode();
    selectedContentId = node.key;
    selectedContentTitle = node.title;
	var form = $('#fileFrm')[0];
	var formData = new FormData(form);
	formData.append("file1",   $("#pdfFile")[0].files[0]);
	formData.append("title",   selectedContentTitle);
	formData.append("comment", $("#bf-content-comment").val());
	$("#bf-content-comment").val("");
	$.ajax({
	    url: '/api/v1/ecm/content/' + selectedContentId,
        processData: false,
        contentType: false,
        data: formData,
        type: 'PUT'
    })
    .done(function(msg) {
	  console.log('msg ' + JSON.stringify(msg));
	  loadPage(selectedContentId);
	});
}

$(function() {
    $(".spinner").show();
    // 서버쪽의 NumberFormatException: For input string: "" <- 우회를 위한 방어코드
	jQuery.ajaxSettings.traditional = true;
	initTree();
	initEvents();
	// initEditor();
	initSocket();
	var hash = window.location.hash;
	if (hash.length>1) {
		$("#sel_category").val(hash.substring(1));
		onSelectChanged($("#sel_category").get(0));
	} else {
		if (document.getElementById("sel_category").length>1) {
			$("#sel_category option:eq(1)").attr("selected", "selected");
		    onSelectChanged($("#sel_category").get(0));
		    location.href = "#" + $("#sel_category option:selected").val();
		}
	}
});
