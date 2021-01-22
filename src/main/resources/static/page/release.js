$(function () {
	$("#btn-download-all").click(function() {
		downloadAll();
	});
	$("#btn-download-changed").click(function() {
		downloadChanged();
	});
	$("#check-all-file").click(function() {
		// console.log('checked ' + ($(this).prop("checked")==true));
		if ($(this).prop("checked")==true) {
			$(".changed-files:enabled").prop("checked", true);
		} else {
			$(".changed-files:enabled").prop("checked", false);
		}
	});
});

function downloadFromHistory(id) {
	if (confirm("해당 버전을 다운로드 하시겠습니까?")) {
		$(".spinner").show();
		$("#ifrm").attr("src", "/api/v1/ecm/release/all/" + id);
		setTimeout(function() {
			$(".spinner").hide();
		}, 1000);
	}
}

function downloadAll() {
	var release = false;
	if (confirm("현재 다운로드 버전을 배포 처리로 기록하시겠습니까?")) {
		release = true;
	}
	$(".spinner").show();
	$("#ifrm").attr("src", "/api/v1/ecm/release/all?release=" + release);
	setTimeout(function() {
		$(".spinner").hide();
	}, 12000);
}

function downloadChanged() {
	var release = false;
	var checkboxes = document.querySelectorAll('input[name="fileIds"]:checked');
	if (checkboxes.length<1) {
		alert("파일을 하나 이상 선택해주세요!");
		return;
	}
	var fileIds = new Array();
	for (var i=0; i<checkboxes.length; i++) {
		var checkbox = checkboxes[i];
		fileIds.push(checkbox.value);
	}
	if (confirm("배포처리 하시겠습니까?\n배포처리 하시면 변경이력에서 삭제됩니다.")) {
		release = true;
    }
    alert('다운로드에 몇 초 소요됩니다');
	$(".spinner").show();
	$("#ifrm").attr("src", "/api/v1/ecm/release/changed?release=" + release + "&fileIds=" + fileIds);
	setTimeout(function() {
		$(".spinner").hide();
	}, 3000);
}
