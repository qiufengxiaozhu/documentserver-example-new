const drive = "local";

// When form is submitted, send ajax request with all selected file IDs
function batchOp(action) {
    var fileIds = [];
    // Get checked checkbox input elements, don't use class selector
    $("input[type='checkbox']:checked").each(function () {
        // Add value to array when value is not empty
        if ($(this).val() !== "") {
            fileIds.push($(this).val());
        }
    });
    sendBatchRes(action, fileIds);
}

// Delete files whose names don't start with 'local-'
function deleteNonLocalFile() {
    var fileIds = [];
    // Iterate through each row in tbody, first td input value is file ID, td with class filename is filename, get IDs of files not starting with 'local-'
    $("tbody tr").each(function () {
        var id = $(this).children("td").children("input").val();
        var fileName = $(this).children("td.filename").text();
        if (!fileName.startsWith("local-")) {
            fileIds.push(id);
        }
    });
    sendBatchRes('delete', fileIds);
}

// Send ajax request with file IDs as parameter
function sendBatchRes(action, fileIds) {
    console.log('File ID list:', fileIds);
    if (fileIds.length === 0) {
        alert("Please select at least one file");
        return;
    }
    // Send ajax request with JSON format data
    $.ajax({
        url: `/demo/file/batchOp/${action}?driveId=${drive}`,
        type: "post",
        contentType: "application/json;charset=utf-8",
        data: JSON.stringify(fileIds),
        dataType: "json",
        success: function (data) {
            // Display each element in data on a separate line
            alert(data.join("\n"));
            window.location.reload();
        }
    });
}

function selectFiles() {
    // Trigger file selection input element
    $("#multiFiles").click();
}


// Upload multiple files
function uploadMultiFiles() {
    var formData = new FormData();
    var files = $("#multiFiles")[0].files;
    if (files.length === 0) {
        alert("Please select files");
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

// Document comparison
function compareDocuments() {
    const fileIds = [];
    const selectedFiles = [];
    // Get checked checkbox input elements, don't use class selector
    $("input[type='checkbox']:checked").each(function () {
        // Add value to array when value is not empty
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
        alert("Please select two files with similar content for document comparison");
        return;
    }

    // Judge by filename, only supports doc and docx documents
    for (let i = 0; i < selectedFiles.length; i++) {
        const fileName = selectedFiles[i].name.toLowerCase();
        if (!fileName.endsWith('.doc') && !fileName.endsWith('.docx')) {
            alert("Document comparison only supports doc and docx format files");
            return;
        }
    }

    // Call backend compareDoc interface
    fetch('/v2/context/compareDoc?docAid=' + fileIds[0] + '&docBid=' + fileIds[1])
        .then(function (response) {
            if (!response.ok) {
                throw new Error('Failed to get comparison URL: ' + response.statusText);
            }
            return response.text();
        })
        .then(function (url) {
            // Open comparison interface in new tab
            window.open(url, '_blank');
        })
        .catch(function (error) {
            alert('Document comparison failed: ' + error.message);
        });
}

$(() => {
    // When clicking the input tag with id fileIds, check all checkboxes
    $("#fileIds").click(function () {
        // Input tags of type checkbox, don't select by class
        $("input[type='checkbox']").prop("checked", this.checked);
    });
    // multiFiles change event, upload files after selection
    $("#multiFiles").change(function () {
        uploadMultiFiles();
    });
})
