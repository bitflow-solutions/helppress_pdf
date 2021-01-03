var editor, selectedGroupId, selectedContentId, selectedContentTitle;
var SOURCE = [];
var _tree = null;
const URL_UPDATE_NODE = "/api/v1/ecm/node";

function initTree() {
  $('#tree').fancytree({
    extensions: ["edit"],
    checkbox: false,
    selectMode: 3,
    source: SOURCE,
    nodata: '도움말이 없습니다',
    edit: {
      triggerStart: ["f2"],
      adjustWidthOfs: 4,   // null: don't adjust input size to content
      inputCss: { minWidth: "120px" },
      beforeEdit: function(e, data){
      },
      edit: function(e, data){
      },
      save: function(e, data) {
      	console.log('save ' + e + ' ' + data);
        renameTitle(e, data);
        return true;
      },
      beforeClose: function(e, data){
      	  console.log("beforeClose");
      },
      close: function(e, data){
    	  console.log("close");
      }
    },
    activate: function(e, data){
	  var node = data.node;
	  if (!node.folder || node.folder===false) {
	  	$("#btn-delete").show();
	  	$("#btn-download").show();
	  	$("#btn-pdf-upload").show();
	  	$("#btn-modify").show();
	  	$("#btn-modify-complete").hide();
	  	// 도움말 표시
	  	loadPage(node.key);
	  } else {
	    // 폴더인 경우
	  	$("#btn-delete").show();
	  	$("#btn-download").hide();
	  	$("#btn-pdf-upload").hide();
	  	$("#btn-modify").hide();
	  	$("#btn-modify-complete").hide();
	  }
	},
	click: function(e, data){
		console.log("click");
    },
    lazyLoad: function (event, data) {},
  });
  _tree = $.ui.fancytree.getTree();
}

function initEvents() {
	$("#btn-modify").click(editContent);
	$("#btn-delete").click(deleteContent);
	$("#btn-download").click(downloadContent);
	$("#btn-expand-all").click(expandAll);
	$("#btn-collapse-all").click(collapseAll);
	$("#btn-pdf-upload").click(function(e) {
	  $("#pdfFile").click();
	});
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
		  var key = msg.result.key;
		  console.log('key ' + key);
		  if (key && key.length>0) {
			loadPage(key);
		  }
		  $("#btn-modify").show();
		  $("#btn-delete").show();
		  $("#btn-download").show();
		  alert('도움말을 수정하였습니다');
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
	        rename: {name: "제목 변경 (F2)", callback: editTitle },
	        modify: {name: "도움말 수정", callback: editContent },
	        deletecontent: {name: "도움말 삭제", callback: deleteContent },
	        downloadcontent: {name: "다운로드", callback: downloadContent }
	    },
	    events: {
			show : function(options){
			  var key = $(this).attr('key');
	    	  
			  var node = _tree.getNodeByKey(key);
			  node.setActive();
			  console.log('file ' + key);
	        }           
		}
	});
	// (2) 폴더 타입
	$.contextMenu({
	    selector: ".fancytree-folder > .fancytree-title",
	    callback: function(key, options) {
        },
	    items: {
	        rename: {name: "제목 변경 (F2)", callback: editTitle },
	        newfolder: {name: "새 폴더", callback: appendChildFolder },
	        newcontent: {name: "새 도움말", callback: appendChildContent },
	        deleteContent: {name: "폴더 삭제", callback: deleteContent }
	    },
	    events: {
			show : function(options){
			  var key = $(this).attr('key');
			  var node = _tree.getNodeByKey(key);
			  node.setActive();
			  console.log('folder ' + key);
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

function initEditor() {
  editor = new SynapEditor('synapEditor', synapEditorConfig);
  editor.setEventListener('beforeUploadImage', function (e) {
    e.addParameter('key', selectedContentId);
	console.log("beforeUploadImage " + JSON.stringify(e));
  });
  editor.setEventListener('afterUploadImage', function (e) {
    console.log('afterUploadImage ' + JSON.stringify(e));
    var fileType = e.fileType;
    var uploadPath = e.path;
    // console.log('filetype ' + fileType + ' uploadpath ' + uploadPath);
    e.editor.addUploadPath(fileType, uploadPath);
  });
  editor.setEventListener('beforeOpenDocument', function (e) {
	e.addParameter('key', selectedContentId);
	console.log("beforeOpenDocument " + JSON.stringify(e));
  });
}

function saveTree(node) {
  console.log('saveTree ' + node);
  var tree = _tree.toDict(true);
  $(".spinner").show();
  $.ajax({
	  url: "/api/v1/ecm/group/" + selectedGroupId,
	  method: "PUT",
		data: {  tree: JSON.stringify(tree.children) }
  	})
	.done(function(msg) {
	  _tree.reload(JSON.parse(msg.result.tree))
	  .done(function() {
	    if (node) {
	    	$("span[key=" + node.key + "]").click();
	    }
	  });
	})
	.fail(function() {
	})
	.always(function() {
		setTimeout(function() {
			$(".spinner").hide();
		}, 300);
	});
}

function editContent() {
  console.log("editContent");
  var node = _tree.getActiveNode();
  selectedContentId = node.key;
  selectedContentTitle = node.title;
  console.log('selectedContentTitle' + selectedContentTitle);
  var url = "/api/v1/ecm/content/" + node.key;
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
  console.log("loadPage " + key);
  selectedContentId = key;
  $("#editor-wrapper").hide();
  $.ajax({
		url: '/api/v1/ecm/content/type/' + key,
		method: "GET"
	})
	.done(function(msg) {
	  console.log('status ' + msg.status);
	  if (msg.status==401) {
	  	location.href = "/logout";
	  } else {
	  	if (msg.result=='PDF') {
	  	  $("#contents-detail").attr("src", "/viewer/viewer.html?file=/" + key + ".pdf");
	  	} else {
	  	  $("#contents-detail").attr("src", key + ".html");
	  	}
	  }
  	  $("#contents-detail").show();
	});
}

function renameTitle(e, data) {
    var title = data.input.val();
    console.log('renameTitle ' + title);
    var node = data.node;
    var data = { groupId: selectedGroupId, key: node.key, title: title };
    if (node.folder && node.folder===true) {
    	data.folder = true;
    }
    $(".spinner").show();
	$.ajax({
		url: URL_UPDATE_NODE,
		method: "PUT",
		data: data
	})
	.done(function(msg) {
	  console.log('status ' + msg.status);
	  if (msg.status==401) {
	  	location.href = "/logout";
	  } else {
	  	// node.setTitle(title);
        saveTree();
	  }
	})
	.always(function() {
		setTimeout(function() {
			$(".spinner").hide();
		}, 500);
    });
}

function appendRootFolder() {
  console.log("appendRootFolder");
  
  var parent = _tree.getRootNode();
  $.ajax({
	  url: URL_UPDATE_NODE,
	  method: "POST",
	  data: { groupId: selectedGroupId, parentKey: parent.key, folder: true }
  	})
	.done(function(msg) {
	  if (msg.status==401) {
	  	location.href = "/logout";
	  } else {
	    console.log('appendRootFolder - parent ' + parent.key + ' child ' + msg.result.key);
    	var existingNode = _tree.getNodeByKey(msg.result.key);
    	if (!existingNode) {
			var child = { key: msg.result.key, title: msg.result.title, folder: true };
			parent.addNode(child, 'child');
    	}
        saveTree();
	  }
	})
	.fail(function() {
	})
	.always(function() {
  });
}

function appendChildFolder() {
  console.log("appendChildFolder");
  
  var parent = _tree.getActiveNode();
  if( !parent ) {
    parent = _tree.getRootNode();
  }
  $.ajax({
	  url: URL_UPDATE_NODE,
	  method: "POST",
	  data: { groupId: selectedGroupId, parentKey: parent.key, folder: true }
  	})
	.done(function(msg) {
	  if (msg.status==401) {
	  	location.href = "/logout";
	  } else {
	    console.log('appendChildFolder - parent ' + parent.key + ' child ' + msg.result.key);
    	var existingNode = _tree.getNodeByKey(msg.result.key);
    	if (!existingNode) {
			var child = { key: msg.result.key, title: msg.result.title, folder: true };
			parent.addNode(child, 'child');
    	}
        saveTree();
	  }
	})
	.fail(function() {
	})
	.always(function() {
  });
}

function appendChildContent() {
  
  var parent = _tree.getActiveNode();
  if( !parent ) {
    parent = _tree.getRootNode();
  }
  $.ajax({
	  url: URL_UPDATE_NODE,
	  method: "POST",
	  data: { groupId: selectedGroupId, parentKey: parent.key }
  	})
	.done(function(msg) {
	  if (msg.status==401) {
	  	location.href = "/logout";
	  } else {
	    console.log('appendChildFolder - parent ' + parent.key + ' child ' + msg.result.key);
    	var existingNode = _tree.getNodeByKey(msg.result.key);
    	if (!existingNode) {
			var child = { key: msg.result.key, title: msg.result.title };
			parent.addNode(child, 'child');
    	}
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
	  url: URL_UPDATE_NODE,
	  method: "POST",
	  data: { groupId: selectedGroupId, parentKey: parent.key }
  	})
	.done(function(msg) {
	  if (msg.status==401) {
	  	location.href = "/logout";
	  } else {
	    console.log('appendRootFolder - parent ' + parent.key + ' child ' + msg.result.key);
    	var existingNode = _tree.getNodeByKey(msg.result.key);
    	if (!existingNode) {
			var child = { key: msg.result.key, title: msg.result.title };
			parent.addNode(child, 'child');
    	}
        saveTree();
	  }
	})
	.fail(function() {
	})
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
				  url: URL_UPDATE_NODE,
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
				  url: URL_UPDATE_NODE,
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
    console.log('[' + node.key + '] folder ' + node.folder);
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
	  $("#contents-detail").attr("src", "/empty-content.html");
	  location.href = "#";
  } else {
	loadTree(selectedGroupId);
  }
}

function loadTree(groupId) {
	$.ajax({
	  url: "/api/v1/ecm/group/" + groupId,
	  method: "GET"
	})
	.done(function(msg) {
	console.log('msg ' + JSON.stringify(msg));
	  
	  $("#tree").show();
	  if (msg && msg.result) {
	  	_tree.reload(JSON.parse(msg.result.tree));
	  }
	  location.href = "#" + groupId;
	})
	.fail(function() {
	})
	.always(function() {
	});
}

function expandAll() {
  $.ui.fancytree.getTree().expandAll();
}

function collapseAll() {
  $.ui.fancytree.getTree().expandAll(false);
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
            console.log("reloading group " + JSON.parse(msg.body).groupId);
	        // console.log("msg " + JSON.parse(msg.body).tree);
	        
	        // msg.body.groupId , msg.body.tree
	        _tree.reload(JSON.parse(JSON.parse(msg.body).tree));
	  	  }
        });
        stompClient.subscribe('/node', function (rawmsg) {
          console.log("/node: msg " + rawmsg.body);
          var msg = JSON.parse(rawmsg.body);
          if (selectedGroupId===msg.groupId) {
            console.log("reloading group " + msg.groupId);
            
            if (msg.method=="ADD") {
            	var existingNode = _tree.getNodeByKey(msg.key);
            	console.log('existingNode ' + existingNode);
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
        });
    });
}

function handlePdf(file) {
	console.log('file ' + file);
	var form = $('#fileFrm')[0];
	var formData = new FormData(form);
	formData.append("file1", $("#pdfFile")[0].files[0]);
	$.ajax({
	    url: '/api/v1/ecm/content/' + selectedContentId,
            processData: false,
            contentType: false,
            data: formData,
            type: 'PUT',
            success: function(result){
                alert("업로드 성공!!");
            }
	    });
}

$(function() {
    // 서버쪽의 NumberFormatException: For input string: "" <- 우회를 위한 방어코드
	jQuery.ajaxSettings.traditional = true;
	initTree();
	initEvents();
	initEditor();
	initSocket();
	var hash = window.location.hash;
	console.log('hash ' + hash);
	if (hash.length>1) {
		$("#sel_category").val(hash.substring(1));
		onSelectChanged($("#sel_category").get(0));
		// loadTree(hash.substring(1));
	}
});
