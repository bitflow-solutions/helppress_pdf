function getCategory(obj) {
	$(".btn-list-item").removeClass("primary");
	if (obj) {
		$(obj).addClass("primary");
		console.log("" + $(obj).attr("bf-order") + " " + $(obj).attr("bf-category-id") + " " + $(obj).attr("bf-name"));
		$("#btn-create").hide();
		$("#btn-modify").show();
		$("#btn-delete").show();
		$("#orderNo").val($(obj).attr("bf-order"));
		$("#groupId").val($(obj).attr("bf-category-id"));
		$("#name").val($(obj).attr("bf-name"));
		$("#groupId").attr("readonly", true);
	} else {
		$("#btn-modify").hide();
		$("#btn-delete").hide();
		$("#btn-create").show();
		$("#orderNo").val("");
		$("#groupId").val("");
		$("#name").val("");
		$("#groupId").attr("readonly", false);
	}
}

function validateInput() {
	if ($("#groupId").val().length<1) {
	  alert("도움말그룹URI를 입력해주세요");
	  $("#groupId").focus();
	  return false; 
	} else if ($("#groupId").val()=='resources') {
	  alert("지원되지 않는 URI 키워드입니다. 'resources'");
	  $("#groupId").focus();
	  return false; 
	} else if ($("#groupId").val()=='uploads') {
	  alert("지원되지 않는 URI 키워드입니다. 'uploads'");
	  $("#groupId").focus();
	  return false;
    }
	if ($("#name").val().length<1) {
	  alert("도움말그룹명을 입력해주세요");
	  $("#name").focus();
	  return false; 
	}
	if ($("#orderNo").val().length<1) {
	  alert("정렬순서를 입력해주세요");
	  $("#orderNo").focus();
	  return false; 
	}
	var orderNo = parseInt($("#orderNo").val(), 10);
	if (orderNo>255) {
		alert("정렬순서 값은 255이하 이어야 합니다");
	    $("#orderNo").focus();
		return false; 
	}
	return true;
}

$(function() {
	$("#btn-delete").click(function () {
		if (confirm("선택한 도움말그룹과 하위 도움말들이 모두 삭제됩니다.\n정말로 삭제하시겠습니까?")) {
			var url = "/api/v1/ecm/group/" + $("#groupId").val();
			$.ajax({
				url: url,
				method: "DELETE",
				data: { }
			})
			.done(function(msg) {
				if (msg.status===200) {
					// 성공
					location.reload();
				}
			});
		}
	});
	$("#btn-modify").click(function () {
		if (!validateInput()) {
			return;
		}
		$(".spinner").show();
		var url = "/api/v1/ecm/group/" + $("#groupId").val();
		$.ajax({
			url: url,
			method: "PUT",
			data: { name: $("#name").val(), orderNo: $("#orderNo").val() }
		})
		.done(function(msg) {
			if (msg.status===200) {
				// 성공
				location.reload();
			}
		})
		.always(function() {
			setTimeout(function() {
				$(".spinner").hide();
			}, 1000);
	    });
	});
	$("#btn-create").click(function () {
		if (!validateInput()) {
			return;
		}
		$(".spinner").show();
		var url = "/api/v1/ecm/group/" + $("#groupId").val();
		$.ajax({
			url: url,
			method: "POST",
			data: { name: $("#name").val(), orderNo: $("#orderNo").val() }
		})
		.done(function(msg) {
			if (msg.status===200) {
				// 성공
				location.reload();
			}
		})
		.always(function() {
			setTimeout(function() {
				$(".spinner").hide();
			}, 1000);
	    });
	});
	$("#category-wrapper li").click(function() {
		$("#category-wrapper li").removeClass("on");
		$(this).addClass("on");
	});
	$("#groupId").keyup(function(event) {
		if (!(event.keyCode>=37 && event.keyCode<=40)) {
			var inputVal = $(this).val();
			$(this).val(inputVal.replace(/[^a-zA-Z0-9]/gi, ''));
		}
	});
	$("#name").keyup(function(event) {
		if (!(event.keyCode>=37 && event.keyCode<=40)) {
			var inputVal = $(this).val();
			$(this).val(inputVal.replace(/[^a-zA-Z0-9ㄱ-힣]/gi, ''));
		}
	});
	$("#orderNo").keyup(function(event) {
		if (!(event.keyCode>=37 && event.keyCode<=40)) {
			var inputVal = $(this).val();
			$(this).val(inputVal.replace(/[^0-9]/gi, ''));
		}
	});
});
