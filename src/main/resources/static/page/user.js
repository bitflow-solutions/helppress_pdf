function getUser(obj) {
	$(".btn-list-item").removeClass("primary");
	if (obj) {
		$(obj).addClass("primary");
		// 수정
		$("#btn-create").hide();
		$("#btn-modify").show();
		$("#btn-delete").show();
		$("#username").val($(obj).attr("bf-name"));
		$("#password").val("");
		$("#username").attr("readonly", true);
	} else {
		// 등록
		$("#btn-modify").hide();
		$("#btn-delete").hide();
		$("#btn-create").show();
		$("#username").val("");
		$("#password").val("");
		$("#username").attr("readonly", false);
	}
}

$(function() {
	$("#btn-delete").click(function () {
		if (confirm("선택한 관리자가 삭제됩니다.\n정말 삭제하시겠습니까?")) {
			var url = "/api/v1/ecm/user";
			$.ajax({
				url: url,
				method: "DELETE",
				data: { username: $("#username").val() }
			})
			.done(function(msg) {
				if (msg.status===200) {
					// 성공
					alert('관리자 정보를 삭제하였습니다');
					location.reload();
				}
			});
		}
	});
	$("#btn-modify").click(function () {
		if ($("#username").val().length<1) {
			alert("관리자ID를 입력해주세요");
			return;
		} else if ($("#password").val().length<1) {
			alert("비밀번호를 입력해주세요");
			return;
		} else if ($("#password2").val().length<1) {
			alert("비밀번호 확인을 입력해주세요");
			return;
		} else if ($("#password").val()!==$("#password2").val()) {
			alert("비밀번호와 비밀번호확인이 일치하지 않습니다");
			return;	
		}
		var url = "/api/v1/ecm/user";
		$.ajax({
			url: url,
			method: "PUT",
			data: { username: $("#username").val(), password: $("#password").val() }
		})
		.done(function(msg) {
			if (msg.status===200) {
				// 성공
				alert('관리자 정보가 수정되었습니다');
				location.reload();
			}
		});
	});
	$("#btn-create").click(function () {
		if ($("#username").val().length<1) {
			alert("관리자ID를 입력해주세요");
			return;
		} else if ($("#password").val().length<1) {
			alert("비밀번호를 입력해주세요");
			return;
		} else if ($("#password2").val().length<1) {
			alert("비밀번호 확인을 입력해주세요");
			return;
		} else if ($("#password").val()!==$("#password2").val()) {
			alert("비밀번호와 비밀번호확인이 일치하지 않습니다");
			return;	
		}
		var url = "/api/v1/ecm/user/join";
		$.ajax({
			url: url,
			method: "POST",
			data: { username: $("#username").val(), password: $("#password").val() }
		})
		.done(function(msg) {
			if (msg.status===200) {
				// 성공
				alert('관리자 정보를 추가하였습니다');
				location.reload();
			}
		});
	});
	$("#category-wrapper li").click(function() {
		$("#category-wrapper li").removeClass("on");
		$(this).addClass("on");
	});
	$("#btn-create").show();
});
