var editor, selectedGroupId, selectedContentId, selectedContentTitle, inputMenuCode, contentType, htmlToOpen = null, isRTF = false;
var SOURCE = [];
var _tree = null;
const URL_API_NODE    = "/api/v1/ecm/node";
const URL_API_CONTENT = "/api/v1/ecm/content/";

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
      	var parentKey = (node.parent.key==null||node.parent.key.indexOf("root")==0)?null:node.parent.key;
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
		.done(function(msg) { })
		.always(function() { });
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
	click: function(event, data) {
		data.node.toggleExpanded();
	},
    activate: function(e, data){
	  var node = data.node;
	  selectedContentId = node.key;
	  if (!node.folder || node.folder===false) {
		// 폴더가 아닌경우
	    selectedContentTitle = node.title;
	  	// 도움말 표시
	  	loadPage(node.key);
		$("#contents-detail").scrollTop();
	  } else {
	    // 폴더인 경우
	  	$("#btn-delete").show();
	    $("#btn-modify").hide();
	  	$("#btn-download").hide();
	  	$("#btn-pdf-upload").hide();
	  }
	}
  });
  _tree = $.ui.fancytree.getTree();
}

function commentPopUp(contentType) {
  console.log('contentType ' + contentType);
  contentType = contentType;
  if (contentType=='PDF') {
    $(".fi-page-filled").empty().html("&nbsp;&nbsp;PDF 파일 업로드")
  } else {
    $(".fi-page-filled").empty().html("&nbsp;&nbsp;HTML 작성")
  }
  if (selectedContentId && selectedContentId.length>3 && selectedContentId.charAt(0)!=='!' ) {
	$("#bf-menu-code").val(selectedContentId);
	$("#bf-menu-code").attr("readonly", true);
  } else {
	$("#bf-menu-code").val("");
	$("#bf-menu-code").attr("readonly", false);
  }
}

function initEvents() {
	$("#btn-modify").click(function() {
		commentPopUp('HTML');	
	});
	$("#btn-delete").click(deleteContent);
	$("#btn-download").click(downloadContent);
	$("#btn-expand-all").click(expandAll);
	$("#btn-collapse-all").click(collapseAll);
	$("#btn-comment-close").click(function() {
		$("#err-no-comment").hide();
		$("#bf-modal-comment").foundation('close');
	});
	$("#btn-pdf-upload").click(function() {
	  commentPopUp('PDF');
    });
	$("#bf-menu-code").keyup(function(event) {
		if (!(event.keyCode>=37 && event.keyCode<=40)) {
			var inputVal = $(this).val();
			$(this).val(inputVal.replace(/[^A-Z0-9]/gi, ''));
		}
	});
	$("#btn-modify-complete").click(function(e) {
		// 도움말 수정완료 버튼 클릭
		$("#btn-modify-complete").hide();
		$(".spinner").show();
		var url = URL_API_CONTENT + selectedGroupId + "/" + selectedContentId;
		$.ajax({
			url: url,
			method: "PUT",
			data: { 
				title: selectedContentTitle,
				content: editor.html.get(),
			}
		})
		.done(function(msg) {
		  if (msg.result) {
			loadPage(selectedContentId);
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

/**
 * (HTML 에디터를 사용하는 경우) 에디터 초기화
 */
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

/**
 * 메뉴 트리 로딩
 */
function loadTree() {
  $(".spinner").show();
  $.ajax({
	  url: "/api/v1/ecm/group/" + selectedGroupId,
	  method: "GET"
  	})
	.done(function(msg) {
	  if (msg.status==401) {
	  	location.href = "/logout";
	  } else {
		  location.href = "#" + selectedGroupId;
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
	.fail(function() {
		// $(".spinner").hide();
	})
	.always(function() {
		setTimeout(function() {
			$(".spinner").hide();
		}, 300);
	});
}

/**
 * (HTML 도움말인 경우) 도움말 수정
 */
function editContent() {
  // $("#contents-detail").attr("src", "/editor/html/popular/iframe.html");
  console.log('editContent');
  $("#contents-detail").hide();
  $("#btn-pdf-upload").hide();
  $("#editor-wrapper").show();
  if (!editor) {
	  /* iframe: true */
	  editor = new FroalaEditor('div#editor-wrapper', {
		events: {
	     "image.beforeUpload": function(files) {
	     var editor = this;
	      if (files.length) {
	        // Create a File Reader.
	        var reader = new FileReader();
	        // Set the reader to insert images when they are loaded.
	        reader.onload = function(e) {
	          var result = e.target.result;
	          editor.image.insert(result, null, null, editor.image.get());
	        };
	        // Read image as base64.
	        reader.readAsDataURL(files[0]);
	      }
	      editor.popups.hideAll();
	      // Stop default upload chain.
	      return false;
	     }
	   }
	  }, function () {
	    console.log('editor ' + editor.html.get());
	  });
  }
  $("#contents-detail").hide();
  $("#editor-wrapper").show();
  $("#btn-modify-complete").show();
  $("#btn-modify").hide();
  $("#btn-download").hide();
  $("#btn-delete").hide();
  /*
  var url = URL_API_CONTENT + selectedGroupId + "/" + selectedContentId;
  $.ajax({
	url: url,
	method: "GET"
  })
  .done(function(msg) {
    // editor.openHTML(msg.result.contents);
    $("#contents-detail").hide();
	$("#editor-wrapper").show();
    $("#btn-modify-complete").show();
    $("#btn-modify").hide();
    $("#btn-download").hide();
    $("#btn-delete").hide();
  });
  */ 
}

/**
 * 도움말 상세 불러오기
 */
function loadPage(key) {
  $("#editor-wrapper").hide();
  $("#btn-delete").show();
  $("#btn-download").show();
  $("#btn-modify").show();
  $("#btn-pdf-upload").show();
  $("#btn-modify-complete").hide();
  $("#contents-detail").attr("src", "/viewer/web/viewer.html?file=/" + selectedGroupId + "/" + key + ".pdf");
  $.ajax({
		url: "/" + selectedGroupId + "/" + key + ".pdf",
		method: "GET",
		statusCode: {
	        404: function(res, status, jqXHR) {
	            // $("#btn-modify").text("글쓰기");
	            $("#btn-delete").hide();
	            $("#btn-download").hide();
				$("#contents-detail").attr("src", "/404.html");
	        },
	        200: function(res, status, err) {
	            // $("#btn-modify").text("HTML작성");
				$("#btn-modify").show();
	            $("#btn-delete").show();
	            $("#btn-download").show();
	        }           
	    }
  });
  $("#contents-detail").show();
}

/**
 * 업로드 처리
 */
function doUpload() {
  $("#err-menu-code").hide();
  $("#err-dupe-id").hide();
  $("#err-no-comment").hide();
  if ($("#bf-menu-code").val().length<4) {
  	$("#err-menu-code").show();
  } else if ($("#bf-menu-code").attr("readonly")!=='readonly' && _tree.getNodeByKey($("#bf-menu-code").val())) {
	$("#err-dupe-id").show();
  } else if ($("#bf-content-comment").val().length<1) {
  	$("#err-no-comment").show();
  } else {
	inputMenuCode = $("#bf-menu-code").val();
	$("#err-dupe-id").hide();
	$("#err-menu-code").hide();
  	$("#err-no-comment").hide();
	$("#bf-modal-comment").foundation('close');
	if (contentType=='PDF') {
    	$("#pdfFile").click();
    } else {
	  editContent();
    }	
  }
}

/**
 * 제목변경: checked OK
 */
function renameTitle(e, data) {
	try {
	    var title = data.input.val();
	    var node = data.node;
	    var data = { groupId: selectedGroupId, key: node.key, title: title, rename: true };
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
	        loadTree();
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
        loadTree();
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
        loadTree();
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
        loadTree();
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
        loadTree();
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
				  data: { groupId: selectedGroupId, key: node.key, child: childContentsArr, folder: true, title: node.title }
			  	})
				.done(function(msg) {
				  if (msg.status==401) {
				  	location.href = "/logout";
				  } else {
				    node.remove();
			        loadTree();
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
		        	  loadTree();
				    } else {
				      loadTree(firstNode);
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

/**
 * PDF 도움말 다운로드
 */
function downloadContent() {
  var node = _tree.getActiveNode();
  if( !node ) {
    alert("도움말을 선택해주세요");
    return;
  }
  var key = node.key;
  $(".spinner").show();
  $("#ifrm").attr("src", "/api/v1/ecm/release/" + selectedGroupId + "/" + key);
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

/**
 * 도움말 그룹 콤보박스가 변경되었을 때 발생하는 이벤트
 */
function onSelectChanged(select) {
  selectedGroupId = select.options[select.selectedIndex].value;
  if (!selectedGroupId || selectedGroupId.length<1) {
	  $("#tree").hide();
	  location.href = "#";
	  $(".tree-wrapper").css("background-image", "url(/img/bg-tree-gray.jpg)");
	  $(".contents-wrapper").css("background", "url(/img/bg-contents-gray.jpg)");
	  $("#contents-detail").attr("src", "about:blank");
  } else {
	  $("#tree").show();
	  $(".tree-wrapper").css("background-image", "inherit");
	  $(".contents-wrapper").css("background", "white");
	  loadTree();
  }
}

/**
 * 메뉴 트리 전체 펼침
 */
function expandAll() {
  _tree.expandAll();
}

/**
 * 메뉴 트리 전체 접힘
 */
function collapseAll() {
  _tree.expandAll(false);
}
/**
 * 실시간으로 메뉴 트리 변경사항 수신
 */
function initSocket() {
	var socket = new SockJS('ws');
	var stompClient = Stomp.over(socket);
	stompClient.debug = null;
	stompClient.connect({}, function (frame) {
        // console.log('Connected: ' + frame);
        stompClient.subscribe('/group', function (msg) {
          console.log("groupId " + JSON.parse(msg.body).groupId);
          if (selectedGroupId===JSON.parse(msg.body).groupId) {
	        loadTree();
	  	  }
        });
        stompClient.subscribe('/node', function (rawmsg) {
          loadTree();
        });
    });
}

/**
 * PDF 파일 업로드
 */
function handlePdf(file) {
	console.log('file ' + file);
    var node = _tree.getActiveNode();
    selectedContentId = node.key;
    selectedContentTitle = node.title;
	var form = $('#fileFrm')[0];
	var formData = new FormData(form);
	formData.append("file1",   $("#pdfFile")[0].files[0]);
	formData.append("title",   selectedContentTitle);
	formData.append("key",     selectedContentId);
	formData.append("comment", $("#bf-content-comment").val());
	$("#bf-content-comment").val("");
	if ($("#bf-menu-code").val().length>3) {
		formData.append("menuCode", $("#bf-menu-code").val());
		$("#bf-menu-code").val("");
	}
	$.ajax({
	    url: '/api/v1/ecm/content/' + selectedGroupId,
        processData: false,
        contentType: false,
        data: formData,
        type: 'PUT'
    })
    .done(function(msg) {
	  console.log('msg ' + JSON.stringify(msg));
	  selectedContentId = msg.result.key
	  loadPage(selectedContentId);
	});
}

$(function() {
    $(".spinner").show();
    // 서버쪽의 NumberFormatException: For input string: "" <- 우회를 위한 방어코드
	jQuery.ajaxSettings.traditional = true;
	initTree();
	initEvents();
	initSocket();
	var hash = window.location.hash;
	if (hash.length>1) {
		$("#sel_category").val(hash.substring(1));
		onSelectChanged($("#sel_category").get(0));
		$(".spinner").hide();
	} else {
		if (document.getElementById("sel_category").length>1) {
			$("#sel_category option:eq(1)").attr("selected", "selected");
		    onSelectChanged($("#sel_category").get(0));
		    location.href = "#" + $("#sel_category option:selected").val();
		}
		$(".spinner").hide();
	}
});
