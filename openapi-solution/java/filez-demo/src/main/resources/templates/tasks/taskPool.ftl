<!DOCTYPE html>
<html lang="zh-CN">
<head>
    <meta charset="UTF-8">
    <title>任务池</title>
    <link href="/static/bootstrap.min.css" rel="stylesheet">
    <style>
        .task-container { padding: 20px; }
        .task-toolbar { display: flex; justify-content: space-between; align-items: center; margin-bottom: 15px; }
        .task-toolbar h5 { margin: 0; }
        .status-badge { padding: 3px 10px; border-radius: 12px; font-size: 12px; color: #fff; }
        .status-IN_QUEUE { background: #6c757d; }
        .status-PROCESSING { background: #007bff; }
        .status-SUCCESS { background: #28a745; }
        .status-FAIL { background: #dc3545; }
        .op-label { font-size: 12px; color: #555; background: #f0f0f0; padding: 2px 8px; border-radius: 3px; }
        .api-label { font-size: 11px; color: #888; }
        .task-id-cell { font-family: 'Consolas', monospace; font-size: 12px; color: #555; max-width: 140px; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; cursor: pointer; }
        .task-id-cell:hover { color: #007bff; }
        .pagination-bar { display: flex; justify-content: center; align-items: center; margin-top: 15px; gap: 8px; }
        .pagination-bar button { min-width: 36px; }
        .pagination-info { color: #666; font-size: 14px; margin: 0 10px; }
        .detail-modal-body { max-height: 70vh; overflow-y: auto; }
        .detail-section { margin-bottom: 12px; }
        .detail-section h6 { font-weight: 600; margin-bottom: 5px; }
        .detail-section pre { background: #f5f5f5; border: 1px solid #ddd; border-radius: 4px; padding: 8px; font-size: 12px; white-space: pre-wrap; word-break: break-all; max-height: 250px; overflow-y: auto; }
        .error-text { color: #dc3545; }
    </style>
</head>
<body>
<div class="task-container">
    <div class="task-toolbar">
        <h5>OpenAPI 任务池</h5>
        <div>
            <button class="btn btn-danger btn-sm" onclick="batchDelete()" id="btn-batch-delete" disabled>
                批量删除
            </button>
            <button class="btn btn-outline-secondary btn-sm" onclick="loadTasks()">
                刷新
            </button>
        </div>
    </div>

    <table class="table table-hover table-bordered table-sm" id="task-table">
        <thead class="thead-light">
        <tr>
            <th width="30"><input type="checkbox" id="select-all" onchange="toggleSelectAll()"></th>
            <th>任务ID</th>
            <th>接口</th>
            <th>文档名称</th>
            <th>操作类型</th>
            <th>状态</th>
            <th>创建时间</th>
            <th width="220">操作</th>
        </tr>
        </thead>
        <tbody id="task-tbody">
        <tr><td colspan="8" class="text-center text-muted">加载中...</td></tr>
        </tbody>
    </table>

    <div class="pagination-bar" id="pagination-bar"></div>
</div>

<!-- 任务详情弹窗 -->
<div class="modal fade" id="detail-modal" tabindex="-1" role="dialog">
    <div class="modal-dialog modal-lg" role="document">
        <div class="modal-content">
            <div class="modal-header">
                <h5 class="modal-title">任务详情</h5>
                <button type="button" class="close" data-dismiss="modal"><span>&times;</span></button>
            </div>
            <div class="modal-body detail-modal-body" id="detail-body"></div>
            <div class="modal-footer">
                <button type="button" class="btn btn-secondary btn-sm" data-dismiss="modal">关闭</button>
            </div>
        </div>
    </div>
</div>

<script src="/static/jquery-3.7.0.min.js"></script>
<script src="/static/bootstrap.bundle.min.js"></script>
<script>
    let currentPage = 1;
    const pageSize = 10;
    let taskCache = {};

    const opTypeMap = {
        'ApplyWatermark': '固定文字水印',
        'ApplyPicWatermark': '固定图片水印',
        'ApplyWatermarkForFixed': '平铺文字水印',
        'ApplyTiledImgWatermarkForFixed': '平铺图片水印'
    };

    function loadTasks(pageNo) {
        if (pageNo) currentPage = pageNo;
        $.get('/home/tasks/list', { pageNo: currentPage, pageSize: pageSize }, function(data) {
            renderTable(data.tasks || []);
            renderPagination(data.total || 0, data.totalPages || 1);
        });
    }

    function renderTable(tasks) {
        const tbody = $('#task-tbody');
        tbody.empty();
        taskCache = {};
        if (tasks.length === 0) {
            tbody.append('<tr><td colspan="8" class="text-center text-muted">暂无任务</td></tr>');
            return;
        }
        tasks.forEach(function(task, idx) {
            taskCache[idx] = task;
            const opName = opTypeMap[task.opType] || task.opType || '-';
            const statusClass = 'status-' + (task.status || 'IN_QUEUE');
            const createTime = task.createTime ? new Date(task.createTime).toLocaleString() : '-';
            const shortTaskId = task.taskId && task.taskId.length > 16
                ? task.taskId.substring(0, 16) + '...'
                : (task.taskId || '-');

            let actions = '';
            actions += '<button class="btn btn-outline-info btn-sm mr-1" onclick="showDetail(' + idx + ')">详情</button>';
            if (task.status === 'SUCCESS' && task.resultDocId) {
                actions += '<button class="btn btn-outline-success btn-sm mr-1" onclick="previewResult(\'' + escapeHtml(task.resultDocId) + '\')">预览</button>';
            }
            if (task.status === 'SUCCESS' && task.contentId) {
                actions += '<a class="btn btn-outline-primary btn-sm mr-1" href="/home/tasks/download?taskId=' +
                    encodeURIComponent(task.taskId) + '&contentId=' + encodeURIComponent(task.contentId) + '" target="_blank">下载</a>';
            }
            actions += '<button class="btn btn-outline-danger btn-sm" onclick="deleteTask(\'' + escapeHtml(task.taskId) + '\')">删除</button>';

            tbody.append(
                '<tr>' +
                '<td><input type="checkbox" class="task-checkbox" value="' + escapeHtml(task.taskId) + '" onchange="updateBatchBtn()"></td>' +
                '<td class="task-id-cell" title="' + escapeHtml(task.taskId) + '" onclick="copyToClipboard(\'' + escapeHtml(task.taskId) + '\')">' + escapeHtml(shortTaskId) + '</td>' +
                '<td><span class="api-label">' + escapeHtml(task.apiName || '-') + '</span></td>' +
                '<td>' + escapeHtml(task.docName) + '</td>' +
                '<td><span class="op-label">' + escapeHtml(opName) + '</span></td>' +
                '<td><span class="status-badge ' + statusClass + '">' + (task.status || 'IN_QUEUE') + '</span></td>' +
                '<td>' + createTime + '</td>' +
                '<td>' + actions + '</td>' +
                '</tr>'
            );
        });
    }

    function showDetail(idx) {
        const task = taskCache[idx];
        if (!task) return;
        let html = '';

        html += '<div class="detail-section"><h6>基本信息</h6>';
        html += '<table class="table table-sm table-bordered">';
        html += '<tr><td width="120"><strong>任务ID</strong></td><td style="font-family:monospace">' + escapeHtml(task.taskId) + '</td></tr>';
        html += '<tr><td><strong>调用接口</strong></td><td>' + escapeHtml(task.apiName || '-') + '</td></tr>';
        html += '<tr><td><strong>文档名称</strong></td><td>' + escapeHtml(task.docName) + '</td></tr>';
        html += '<tr><td><strong>操作类型</strong></td><td>' + escapeHtml(opTypeMap[task.opType] || task.opType || '-') + '</td></tr>';
        html += '<tr><td><strong>状态</strong></td><td><span class="status-badge status-' + task.status + '">' + task.status + '</span></td></tr>';
        html += '<tr><td><strong>创建时间</strong></td><td>' + (task.createTime ? new Date(task.createTime).toLocaleString() : '-') + '</td></tr>';
        html += '<tr><td><strong>更新时间</strong></td><td>' + (task.updateTime ? new Date(task.updateTime).toLocaleString() : '-') + '</td></tr>';
        if (task.contentId) {
            html += '<tr><td><strong>结果 contentId</strong></td><td style="font-family:monospace">' + escapeHtml(task.contentId) + '</td></tr>';
        }
        if (task.resultFilename) {
            html += '<tr><td><strong>结果文件名</strong></td><td>' + escapeHtml(task.resultFilename) + '</td></tr>';
        }
        html += '</table></div>';

        if (task.requestBody) {
            html += '<div class="detail-section"><h6>请求入参 (Request Body)</h6>';
            html += '<pre>' + formatJson(task.requestBody) + '</pre></div>';
        }

        if (task.errorMsg) {
            html += '<div class="detail-section"><h6 class="error-text">错误信息</h6>';
            html += '<pre class="error-text">' + escapeHtml(task.errorMsg) + '</pre></div>';
        }

        if (task.serverResponse) {
            html += '<div class="detail-section"><h6>Server 原样响应</h6>';
            html += '<pre>' + formatJson(task.serverResponse) + '</pre></div>';
        }

        $('#detail-body').html(html);
        $('#detail-modal').modal('show');
    }

    function formatJson(str) {
        if (!str) return '';
        try {
            return escapeHtml(JSON.stringify(JSON.parse(str), null, 2));
        } catch(e) {
            return escapeHtml(str);
        }
    }

    function copyToClipboard(text) {
        if (navigator.clipboard) {
            navigator.clipboard.writeText(text);
        } else {
            const textarea = document.createElement('textarea');
            textarea.value = text;
            document.body.appendChild(textarea);
            textarea.select();
            document.execCommand('copy');
            document.body.removeChild(textarea);
        }
    }

    function renderPagination(total, totalPages) {
        const bar = $('#pagination-bar');
        bar.empty();
        if (totalPages <= 1) {
            bar.append('<span class="pagination-info">共 ' + total + ' 条</span>');
            return;
        }
        bar.append('<button class="btn btn-sm btn-outline-secondary" ' + (currentPage <= 1 ? 'disabled' : '') +
            ' onclick="loadTasks(' + (currentPage - 1) + ')">上一页</button>');
        for (let i = 1; i <= totalPages; i++) {
            const active = i === currentPage ? 'btn-primary' : 'btn-outline-secondary';
            bar.append('<button class="btn btn-sm ' + active + '" onclick="loadTasks(' + i + ')">' + i + '</button>');
        }
        bar.append('<button class="btn btn-sm btn-outline-secondary" ' + (currentPage >= totalPages ? 'disabled' : '') +
            ' onclick="loadTasks(' + (currentPage + 1) + ')">下一页</button>');
        bar.append('<span class="pagination-info">共 ' + total + ' 条</span>');
    }

    function deleteTask(taskId) {
        if (!confirm('确认删除此任务？')) return;
        $.ajax({
            url: '/home/tasks/' + encodeURIComponent(taskId),
            type: 'DELETE',
            success: function() { loadTasks(); }
        });
    }

    function batchDelete() {
        const ids = [];
        $('.task-checkbox:checked').each(function() { ids.push($(this).val()); });
        if (ids.length === 0) return;
        if (!confirm('确认删除选中的 ' + ids.length + ' 条任务？')) return;
        $.ajax({
            url: '/home/tasks/batch-delete',
            type: 'POST',
            contentType: 'application/json',
            data: JSON.stringify({ taskIds: ids }),
            success: function() { loadTasks(); }
        });
    }

    function toggleSelectAll() {
        const checked = $('#select-all').is(':checked');
        $('.task-checkbox').prop('checked', checked);
        updateBatchBtn();
    }

    function updateBatchBtn() {
        const count = $('.task-checkbox:checked').length;
        $('#btn-batch-delete').prop('disabled', count === 0);
    }

    /**
     * 通过 driver-callback 在新标签页中预览结果文件
     */
    function previewResult(docId) {
        $.get('/v2/context/driver-cb', { docId: docId, action: 'view' }, function(url) {
            if (url) {
                window.open(url, '_blank');
            } else {
                alert('获取预览链接失败');
            }
        }).fail(function() {
            alert('获取预览链接失败');
        });
    }

    function escapeHtml(text) {
        if (!text) return '';
        const div = document.createElement('div');
        div.appendChild(document.createTextNode(text));
        return div.innerHTML;
    }

    $(function() { loadTasks(1); });
</script>
</body>
</html>
