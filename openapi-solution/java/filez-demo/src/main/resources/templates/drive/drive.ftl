<!DOCTYPE html>
<html lang="zh-CN">
<head>
    <meta charset="UTF-8">
    <title>本地仓库</title>
    <link href="/static/bootstrap.min.css" rel="stylesheet">
    <link rel="stylesheet" href="/css/drive.css">
</head>
<body>
<div class="drive-container">
    <!-- 顶部工具栏 -->
    <div class="drive-toolbar">
        <div class="toolbar-left">
            <div class="format-filter">
                <button class="btn btn-sm btn-outline-secondary active" data-format="all" onclick="setFormat('all', this)">全部</button>
                <button class="btn btn-sm btn-outline-secondary" data-format="word" onclick="setFormat('word', this)">Word</button>
                <button class="btn btn-sm btn-outline-secondary" data-format="excel" onclick="setFormat('excel', this)">Excel</button>
                <button class="btn btn-sm btn-outline-secondary" data-format="ppt" onclick="setFormat('ppt', this)">PPT</button>
                <button class="btn btn-sm btn-outline-secondary" data-format="pdf" onclick="setFormat('pdf', this)">PDF</button>
                <button class="btn btn-sm btn-outline-secondary" data-format="ofd" onclick="setFormat('ofd', this)">OFD</button>
                <button class="btn btn-sm btn-outline-secondary" data-format="image" onclick="setFormat('image', this)">图片</button>
            </div>
            <input type="text" class="form-control form-control-sm search-input" id="search-input"
                   placeholder="搜索文件名..." oninput="onSearchInput()">
        </div>
        <div class="toolbar-right">
            <button class="btn btn-sm btn-danger" id="btn-batch-delete" disabled onclick="batchDelete()">
                批量删除 (<span id="batch-count">0</span>)
            </button>
            <div class="btn-group">
                <button type="button" class="btn btn-sm btn-outline-primary dropdown-toggle" data-toggle="dropdown">
                    新建文件
                </button>
                <div class="dropdown-menu dropdown-menu-right">
                    <a class="dropdown-item" href="#" onclick="createNewFile('docx')">Word 文档</a>
                    <a class="dropdown-item" href="#" onclick="createNewFile('xlsx')">Excel 表格</a>
                    <a class="dropdown-item" href="#" onclick="createNewFile('pptx')">PPT 演示</a>
                </div>
            </div>
            <button class="btn btn-sm btn-primary" onclick="uploadDoc()">上传文件</button>
            <#if drive == 'local'>
                <button class="btn btn-sm btn-outline-secondary" onclick="openFileOpModal('批量操作', '/home/local/batch')">
                    批量操作
                </button>
            </#if>
        </div>
    </div>

    <!-- 拖拽上传区域 -->
    <input id="fileUpload" type="file" hidden>
    <div id="dropArea"></div>

    <!-- 文件列表 -->
    <table class="table table-hover table-sm drive-table" id="drive-table">
        <thead>
        <tr>
            <th width="30"><input type="checkbox" id="select-all" onchange="toggleSelectAll()"></th>
            <th width="80"class="sortable" onclick="sortBy('name')">文件名 <span class="sort-icon" id="sort-name"></span></th>
            <th width="100" class="sortable" onclick="sortBy('size')">大小 <span class="sort-icon" id="sort-size"></span></th>
            <th width="170" class="sortable" onclick="sortBy('modifiedAt')">修改时间 <span class="sort-icon" id="sort-modifiedAt">▼</span></th>
            <th width="180">操作</th>
        </tr>
        </thead>
        <tbody id="file-tbody">
        <tr><td colspan="5" class="text-center text-muted py-3">加载中...</td></tr>
        </tbody>
    </table>

    <!-- 分页栏 -->
    <div class="drive-pagination" id="pagination-bar">
        <div class="pagination-left">
            <span class="text-muted" id="pagination-info"></span>
        </div>
        <div class="pagination-center" id="pagination-btns"></div>
        <div class="pagination-right">
            <select class="form-control form-control-sm page-size-select" id="page-size-select" onchange="changePageSize()">
                <option value="15">15 条/页</option>
                <option value="30">30 条/页</option>
                <option value="50">50 条/页</option>
                <option value="100">100 条/页</option>
            </select>
        </div>
    </div>
</div>

<!-- 模态框（元数据编辑 / 版本预览等） -->
<div class="modal fade" id="fileOpModal" tabindex="-1" role="dialog">
    <div class="modal-dialog modal-xl" role="document">
        <div class="modal-content">
            <div class="modal-header">
                <h5 class="modal-title" id="fileOpModalLabel"></h5>
                <button type="button" class="close" data-dismiss="modal" onclick="closeFileOpModal()">
                    <span>&times;</span>
                </button>
            </div>
            <div id="fileOpBody" class="modal-body">
                <iframe id="fileOpIframe" src="" frameborder="0" style="width: 100%; height: 700px;"></iframe>
            </div>
        </div>
    </div>
</div>

<script src="/static/jquery-3.7.0.min.js"></script>
<script src="/static/bootstrap.bundle.min.js"></script>
<script src="/static/feather-icon.min.js"></script>
<script src="/js/drive.js"></script>
</body>
</html>
