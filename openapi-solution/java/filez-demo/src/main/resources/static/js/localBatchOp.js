const drive = "local";

// 表单提交时，发送ajax请求，发送所有选中的文件id
function batchOp(action) {
    var fileIds = [];
    // 获取类型为checkbox且被选中的input标签，不要通过类选择
    $("input[type='checkbox']:checked").each(function () {
        // 值不为空时，将值添加到数组中
        if ($(this).val() !== "") {
            fileIds.push($(this).val());
        }
    });
    sendBatchRes(action, fileIds);
}

// 删除文件名非local-开头的文件
function deleteNonLocalFile() {
    var fileIds = [];
    // 遍历tbody中的每一行,每一行的第一个td标签中input的value是文件ID，后面td标签class是filename的是文件名，获取文件名不是local-开头的文件的id
    $("tbody tr").each(function () {
        var id = $(this).children("td").children("input").val();
        var fileName = $(this).children("td.filename").text();
        if (!fileName.startsWith("local-")) {
            fileIds.push(id);
        }
    });
    sendBatchRes('delete', fileIds);
}

// 发送ajax请求，参数是文件id
function sendBatchRes(action, fileIds) {
    console.log('文件ID列表:', fileIds);
    if (fileIds.length === 0) {
        alert("请至少选择一个文件");
        return;
    }
    // json格式的数据发送ajax请求
    $.ajax({
        url: `/demo/file/batchOp/${action}?driveId=${drive}`,
        type: "post",
        contentType: "application/json;charset=utf-8",
        data: JSON.stringify(fileIds),
        dataType: "json",
        success: function (data) {
            // 每行显示data中的一个元素
            alert(data.join("\n"));
            window.location.reload();
        }
    });
}

function selectFiles() {
    // 触发选择文件的input标签
    $("#multiFiles").click();
}


// 上传多个文件
function uploadMultiFiles() {
    var formData = new FormData();
    var files = $("#multiFiles")[0].files;
    if (files.length === 0) {
        alert("请选择文件");
        return;
    }
    for (var i = 0; i < files.length; i++) {
        formData.append("files", files[i]);
    }
    $.ajax({
        url: `/v2/context/file/batchOp/upload?driveId=${drive}`,
        type: "post",
        data: formData,
        contentType: false,
        processData: false,
        success: function (data) {
            alert(data.join("\n"));
            window.location.reload();
        }
    });
}

// 文档比对
function compareDocuments() {
    const fileIds = [];
    const selectedFiles = [];
    // 获取类型为checkbox且被选中的input标签，不要通过类选择
    $("input[type='checkbox']:checked").each(function () {
        // 值不为空时，将值添加到数组中
        if ($(this).val() !== "") {
            fileIds.push($(this).val());
        }

        const fileName = $(this).closest('tr').find('.filename').text();
        selectedFiles.push({
            id: $(this).val(),
            name: fileName
        });
    });

    if (fileIds.length !== 2) {
        alert("请选择两个内容相似的文件进行文档比对");
        return;
    }

    // 通过文件名判断，仅支持doc、docx文档
    for (let i = 0; i < selectedFiles.length; i++) {
        const fileName = selectedFiles[i].name.toLowerCase();
        if (!fileName.endsWith('.doc') && !fileName.endsWith('.docx') && !fileName.endsWith('.pdf')) {
            console.log("Document comparison only supports doc and docx and pdf format files");
            return;
        }
    }

    // 调用后端 compareDoc 接口
    fetch('/v2/context/compareDoc?docAid=' + fileIds[0] + '&docBid=' + fileIds[1])
        .then(function (response) {
            if (!response.ok) {
                throw new Error('获取比对URL失败: ' + response.statusText);
            }
            return response.text();
        })
        .then(function (url) {
            // Open comparison interface in new tab
            console.log('Comparison URL:', url);
            window.open(url, '_blank');
        })
        .catch(function (error) {
            alert('文档比对失败: ' + error.message);
        });
}

$(() => {
    // 点击id为fileIds的input标签时，勾选所有的checkbox
    $("#fileIds").click(function () {
        // 类型为checkbox且的input标签，不要通过类选择
        $("input[type='checkbox']").prop("checked", this.checked);
    });
    // multiFiles的change事件，选择文件后，上传文件
    $("#multiFiles").change(function () {
        uploadMultiFiles();
    });
})
