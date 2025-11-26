<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <link rel="stylesheet" href="/css/drive.css">
    <link href="/static/bootstrap.min.css" rel="stylesheet">
    <title></title>
</head>
<body>
<div class="container-fluid">
    <main role="main" class="col-md-12 col-lg-12 px-md-4" style="margin-top: -40px;">
        <input id="fileUpload" type="file" hidden>
        <div id="dropArea"></div>
        <table class="table table-borderless table-hover" style="margin-top: 40px;">
            <thead>
            <tr>
                <th id='file-name-th' class="file-name" scope="col">
                    Filename
                    <input id="search-input" type="text" class="form-control" placeholder="Search files"
                           style="margin-left: 5px; width: 200px;display: inline-block;">
                    <div class="btn-group" style="margin-left: 5px; display: inline-block">
                        <button type="button" class="btn btn-light dropdown-toggle" data-toggle="dropdown" aria-haspopup="true"
                                aria-expanded="false">
                            <span data-feather="plus-circle"></span>
                        </button>
                        <div class="dropdown-menu">
                            <a class="dropdown-item" href="#" disabled onclick="uploadDoc()">Upload File</a>
                        </div>
                    </div>
                    <#if drive == 'local'>
                        <button type="button" class="btn btn-light" onclick="openFileOpModal('Batch Operations', `/home/local/batch`)">
                            Batch Operations
                        </button>
                    </#if>
                </th>
                <th scope="col"></th>
                <th scope="col"></th>
            </tr>
            </thead>
            <tbody id="file-list" style="margin-top: 40px;">
            <#list files as file>
                <tr class="file-row" data-id="${file.id}">
                    <td class="file-list-item" scope="row" data-id="${file.id}">
                        <span class='mr-2' data-feather="file-text"></span>
                        ${file.name}
                    </td>
                    <td data-id="${file.id}">
                        <div class="multi-btn">
                            <div class="op-btn" onclick="openDoc('${file.id}', 'edit')">
                                <span data-feather="edit"></span>
                            </div>
                            <div class="op-btn" onclick="downloadDoc('${file.id}')">
                                <span data-feather="download"></span>
                            </div>
                            <div class="op-btn" onclick="deleteDoc('${file.id}')">
                                <span data-feather="trash-2"></span>
                            </div>
                            <div class="op-btn" onclick="onFileOpClick('${file.id}')">
                                <span data-feather="more-horizontal"></span>
                            </div>
                        </div>
                    </td>
                    <td></td>
                </tr>
            </#list>
            </tbody>
        </table>
        <!-- bootstrap modal -->
        <div class="modal fade " id="fileOpModal" tabindex="-1" role="dialog" aria-labelledby="fileOpModalLabel" aria-hidden="true">
            <div class="modal-dialog modal-xl" role="document">
                <div class="modal-content">
                    <div class="modal-header">
                        <h5 class="modal-title" id="fileOpModalLabel"></h5>
                        <button type="button" class="close" data-dismiss="modal" aria-label="Close" onclick="closeFileOpModal()">
                            <span aria-hidden="true">&times;</span>
                        </button>
                    </div>
                    <div id="fileOpBody" class="modal-body">
                        <iframe id="fileOpIframe" src="" frameborder="0" style="width: 100%; height: 700px;"></iframe>
                    </div>
                </div>
            </div>
        </div>
    </main>
</div>
<script src="/static/jquery-3.7.0.min.js"></script>
<script src="/static/bootstrap.bundle.min.js"></script>
<script src="/static/feather-icon.min.js"></script>
<script src="/js/drive.js"></script>
</body>
</html>
