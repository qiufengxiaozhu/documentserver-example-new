<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <title>Document Operation Buttons</title>
    <link rel="stylesheet" href="/css/home.css">
    <link href="/static/bootstrap.min.css" rel="stylesheet">
    <link href="/css/dashboard.css" rel="stylesheet">
</head>
<body>
<div class="container-fluid">
    <div class="row">
        <main role="main" class="col-md-9 ml-sm-auto col-lg-12 px-md-4">
            <!-- Operation buttons, including delete button, buttons need spacing -->
            <div class="btn-group" role="group" aria-label="Basic example">
                <button type="button" class="btn btn-outline-primary" onclick="batchOp('delete')">Delete Selected Files</button>
                <button type="button" class="btn btn-outline-primary" onclick="deleteNonLocalFile()">Delete Files Not Starting with 'local'</button>
                <button type="button" class="btn btn-outline-primary" onclick="selectFiles()">Upload Multiple Files</button>
                <button type="button" class="btn btn-outline-primary" onclick="compareDocuments()">Document Comparison</button>
            </div>
            <!-- Select multiple files for upload -->
            <input type="file" id="multiFiles" multiple="multiple" style="display: none"/>
            <!-- File list with checkboxes, using bootstrap styles -->
            <table class="table table-hover">
                <thead>
                <tr>
                    <th><input type="checkbox" id="fileIds" name="fileIds"/></th>
                    <th>File ID</th>
                    <th>Filename</th>
                </tr>
                </thead>
                <tbody>
                <#list files as file>
                    <tr>
                        <td>
                            <!-- input id is index, value is file id -->
                            <input type="checkbox" name="fileId" value="${file.id}"/>
                        </td>
                        <td>${file.id}</td>
                        <td class="filename">${file.name}</td>
                    </tr>
                </#list>
                </tbody>
            </table>
        </main>
    </div>
</div>
<script src="/static/jquery-3.7.0.min.js"></script>
<script src="/static/bootstrap.bundle.min.js"></script>
<script src="/static/feather-icon.min.js"></script>
<script src="/js/localBatchOp.js"></script>
</body>
</html>
