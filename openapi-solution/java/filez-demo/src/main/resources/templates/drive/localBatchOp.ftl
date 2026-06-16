<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <title>文档操作按钮</title>
    <link rel="stylesheet" href="/css/home.css">
    <link href="/static/bootstrap.min.css" rel="stylesheet">
    <link href="/css/dashboard.css" rel="stylesheet">
</head>
<body>
<div class="container-fluid">
    <div class="row">
        <main role="main" class="col-md-9 ml-sm-auto col-lg-12 px-md-4">
            <!-- 操作按钮，包括删除按钮，按钮间需要包含间距 -->
            <div class="btn-group" role="group" aria-label="Basic example">
                <button type="button" class="btn btn-outline-primary" onclick="batchOp('delete')">删除选中文件</button>
                <button type="button" class="btn btn-outline-primary" onclick="deleteNonLocalFile()">删除文件名非local开头的文件</button>
                <button type="button" class="btn btn-outline-primary" onclick="selectFiles()">上传多个文件</button>
                <button type="button" class="btn btn-outline-primary" onclick="compareDocuments()">文档比对</button>
            </div>
            <!-- 选择多个文件上传 -->
            <input type="file" id="multiFiles" multiple="multiple" style="display: none"/>
            <!-- 带有勾选框的文件列表，使用bootstrap的样式 -->
            <table class="table table-hover">
                <thead>
                <tr>
                    <th><input type="checkbox" id="fileIds" name="fileIds"/></th>
                    <th>文件ID</th>
                    <th>文件名</th>
                </tr>
                </thead>
                <tbody>
                <#list files as file>
                    <tr>
                        <td>
                            <!-- input id 是index，value是文件id -->
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
