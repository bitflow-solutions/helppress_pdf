function doLogin() {
	if ($("#username").val().length<1) {
		alert("아이디를 입력해주세요");
	} else if ($("#password").val().length<1) {
		alert("비밀번호를 입력해주세요");
	} else {
		$(".spinner").show();
		$.ajax({
			url: '/api/v1/ecm/user/login',
			method: 'POST',
			data: {username: $("#username").val(), password: $("#password").val()}
		})
		.done(function(msg) {
			console.log('msg ' + JSON.stringify(msg));
			if (msg.status===200 && msg.result) {
				location.href = msg.result;
			} else {
				$("#password").val("");
				alert('아이디 또는 비밀번호가 일치하지 않습니다');
			}
		})
		.always(function() {
			setTimeout(function() {
				$(".spinner").hide();
			}, 1000);
	    });
	}
}

function doJoin() {
	if ($("#username").val().length<1) {
		alert("아이디를 입력해주세요");
	} else if ($("#password").val().length<1) {
		alert("비밀번호를 입력해주세요");
	} else if ($("#password").val()!=$("#password2").val()) {
		alert("비밀번호와 비밀번화 확인 값이 일치하지 않습니다");
	} else {
		$(".spinner").show();
		$.ajax({
			url: '/api/v1/ecm/user/join',
			method: 'POST',
			data: {username: $("#username").val(), password: $("#password").val()}
		})
		.done(function(msg) {
			console.log('msg ' + JSON.stringify(msg));
			if (msg.status===200) {
				location.href = "/login";
			}
		})
		.always(function() {
			setTimeout(function() {
				$(".spinner").hide();
			}, 1000);
	    });
	}
}

function onLoginEnterPressed() {
  if(window.event.keyCode == 13) {
	  doLogin();
  }
}

function onJoinEnterPressed() {
  if(window.event.keyCode == 13) {
	  doJoin();
  }
}

$(function() {
	$("#btn-join").click(function () {
		doJoin();
	});
	$("#btn-login").click(function () {
		doLogin();
	});
});
