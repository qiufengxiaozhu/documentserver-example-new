<!DOCTYPE html>
<html lang="zh-CN">
<head>
    <meta charset="UTF-8">
    <title>JSON 转 Excel</title>
    <link href="/static/bootstrap.min.css" rel="stylesheet">
    <style>
        .j2e-container { display: flex; height: calc(100vh - 20px); padding: 10px; gap: 15px; }
        .j2e-left { width: 500px; min-width: 500px; overflow-y: auto; }
        .j2e-right { flex: 1; display: flex; flex-direction: column; }
        .j2e-section { margin-bottom: 15px; }
        .j2e-section h6 { margin-bottom: 10px; font-weight: 600; }
        .file-list { max-height: 180px; overflow-y: auto; border: 1px solid #dee2e6; border-radius: 4px; }
        .file-item { padding: 6px 12px; cursor: pointer; font-size: 13px; display: flex; align-items: center; }
        .file-item:hover { background: #f8f9fa; }
        .file-item.selected { background: #e7f1ff; font-weight: 500; }
        .file-item .file-ext { color: #999; font-size: 11px; margin-left: auto; }
        .opt-group { display: flex; gap: 8px; flex-wrap: wrap; margin-top: 6px; }
        .opt-group label { cursor: pointer; padding: 6px 14px; border: 1px solid #ccc; border-radius: 4px; font-size: 13px; transition: all 0.2s; }
        .opt-group input:checked + label { background: #007bff; color: #fff; border-color: #007bff; }
        .opt-group input { display: none; }
        .opt-hint { font-size: 11px; color: #888; margin-top: 4px; }
        .form-control-sm { font-size: 13px; }
        .request-preview { background: #1e1e1e; color: #d4d4d4; padding: 12px 16px; border-radius: 4px; font-size: 12px; font-family: 'Consolas', 'Monaco', monospace; white-space: pre-wrap; word-break: break-all; max-height: 260px; overflow-y: auto; margin: 0; border: 1px solid #333; line-height: 1.5; }
        .request-preview .json-key { color: #9cdcfe; }
        .request-preview .json-string { color: #ce9178; }
        .request-preview .json-number { color: #b5cea8; }
        .request-preview .json-bool { color: #569cd6; }
        .request-preview .json-null { color: #569cd6; }
        .preview-area { flex: 1; border: 1px solid #dee2e6; border-radius: 4px; background: #f8f9fa; display: flex; align-items: center; justify-content: center; min-height: 300px; }
        .preview-area iframe { width: 100%; height: 100%; border: none; }
        .preview-placeholder { color: #999; font-size: 14px; }
        .status-msg { padding: 10px; margin-bottom: 10px; border-radius: 4px; display: none; }
        .status-msg.show { display: block; }
        .status-msg.info { background: #cce5ff; color: #004085; }
        .status-msg.success { background: #d4edda; color: #155724; }
        .status-msg.error { background: #f8d7da; color: #721c24; }
        .selected-info { background: #f0f7ff; border: 1px solid #b8daff; border-radius: 4px; padding: 8px 12px; margin-top: 8px; font-size: 12px; }
        .param-row { display: flex; align-items: center; gap: 10px; margin-bottom: 8px; }
        .param-row .param-label { min-width: 90px; font-size: 13px; font-weight: 500; }
    </style>
</head>
<body>
<div class="j2e-container">
    <div class="j2e-left">
        <!-- 步骤1：选择 JSON 文件 -->
        <div class="j2e-section">
            <h6>1. 选择 JSON 文件</h6>
            <div class="file-list" id="json-file-list"></div>
            <div class="selected-info" id="selected-info" style="display:none;"></div>
        </div>

        <!-- 步骤2：工作表名称（可选） -->
        <div class="j2e-section">
            <h6>2. 工作表名称 <span class="opt-hint">（可选，不填则使用 "Sheet1"）</span></h6>
            <input type="text" class="form-control form-control-sm" id="sheetName" placeholder="留空则不传此参数" maxlength="31">
            <div class="opt-hint">最大 31 字符，不可包含 []:*?/\</div>
        </div>

        <!-- 步骤3：列顺序（可选） -->
        <div class="j2e-section">
            <h6>3. 列顺序 <span class="opt-hint">（可选，不填则按 JSON key 原始顺序）</span></h6>
            <input type="text" class="form-control form-control-sm" id="columnOrder" placeholder="逗号分隔列名，如: name,age,email">
            <div class="opt-hint">指定后仅导出这些列，按给定顺序排列</div>
        </div>

        <!-- 请求参数预览 -->
        <div class="j2e-section">
            <h6>请求参数预览 <span class="opt-hint">（只读，根据左侧选项实时变化）</span></h6>
            <pre class="request-preview" id="request-preview">请先选择 JSON 文件</pre>
        </div>

        <div class="j2e-section">
            <button class="btn btn-success btn-block" onclick="submitJson2Excel()" id="btn-submit">
                开始转换
            </button>
        </div>

        <div class="status-msg" id="status-msg"></div>
    </div>

    <div class="j2e-right">

        <!-- 结果预览 -->
        <h6>转换结果预览</h6>
        <div class="preview-area" id="preview-area">
            <span class="preview-placeholder" id="preview-placeholder">提交转换任务后，结果将在此处预览</span>
            <iframe id="preview-iframe" style="display:none;"></iframe>
        </div>
    </div>
</div>

<script src="/static/jquery-3.7.0.min.js"></script>
<script>
    const DEMO_BASE_URL = '${demoBaseUrl}';

    const allFiles = [
        <#if files??>
        <#list files as file>
        { id: '${file.id}', name: '${file.name}' }<#if file_has_next>,</#if>
        </#list>
        </#if>
    ];

    let selectedFile = null;
    let pollingTimer = null;

    function getFileExt(name) {
        const idx = name.lastIndexOf('.');
        return idx >= 0 ? name.substring(idx).toLowerCase() : '';
    }

    function isJsonFile(name) {
        return name.toLowerCase().endsWith('.json');
    }

    // ========== JSON 文件列表 ==========
    function renderJsonFileList() {
        var container = $('#json-file-list');
        container.empty();
        var filtered = allFiles.filter(function(f) { return isJsonFile(f.name); });
        if (filtered.length === 0) {
            container.append('<div class="p-3 text-muted text-center">无 JSON 文件，请先上传 .json 文件到本地仓库</div>');
            return;
        }
        filtered.forEach(function(file) {
            var sel = selectedFile && selectedFile.id === file.id ? ' selected' : '';
            container.append(
                '<div class="file-item' + sel + '" data-id="' + file.id + '" data-name="' + escapeAttr(file.name) +
                '" onclick="selectJsonFile(this)">' +
                '<span>' + escapeHtml(file.name) + '</span>' +
                '<span class="file-ext">.JSON</span></div>'
            );
        });
    }

    function selectJsonFile(el) {
        $('#json-file-list .file-item').removeClass('selected');
        $(el).addClass('selected');
        selectedFile = { id: $(el).data('id'), name: $(el).data('name') };
        $('#selected-info').show().html('<strong>已选择：</strong>' + escapeHtml(selectedFile.name));
        updateRequestPreview();
    }

    // ========== 状态 ==========
    function showStatus(msg, type) {
        $('#status-msg').removeClass('info success error show').addClass(type + ' show').text(msg);
    }

    // ========== 提交 ==========
    function submitJson2Excel() {
        if (!selectedFile) { showStatus('请先选择 JSON 文件', 'error'); return; }

        var sheetName = $('#sheetName').val().trim();
        var columnOrder = $('#columnOrder').val().trim();

        var payload = {
            docId: selectedFile.id,
            docName: selectedFile.name
        };
        // 只有非空值才传，空值代表"不指定，使用服务端默认值"
        if (sheetName) payload.sheetName = sheetName;
        if (columnOrder) payload.columnOrder = columnOrder;

        showStatus('正在提交 JSON 转 Excel 任务...', 'info');
        $('#btn-submit').prop('disabled', true);

        $.ajax({
            url: '/home/jsonToExcel/submit',
            type: 'POST',
            contentType: 'application/json',
            data: JSON.stringify(payload),
            success: function(res) {
                if (res.success) {
                    showStatus('任务已提交 (taskId: ' + res.taskId + ')，正在处理中...', 'info');
                    startPolling(res.taskId);
                } else {
                    showStatus('提交失败: ' + (res.error || '未知错误'), 'error');
                    $('#btn-submit').prop('disabled', false);
                }
            },
            error: function(xhr) {
                showStatus('请求失败: ' + xhr.statusText, 'error');
                $('#btn-submit').prop('disabled', false);
            }
        });
    }

    // ========== 轮询 & 预览 ==========
    function startPolling(taskId) {
        if (pollingTimer) clearInterval(pollingTimer);
        var pollCount = 0;
        pollingTimer = setInterval(function() {
            pollCount++;
            $.get('/home/tasks/taskStatus', { taskId: taskId }, function(res) {
                var status = res.taskStatus || res.code;
                if (status === 'SUCCESS') {
                    clearInterval(pollingTimer);
                    pollingTimer = null;
                    showStatus('转换完成！', 'success');
                    $('#btn-submit').prop('disabled', false);
                    showPreview(taskId, res.contentId);
                } else if (status === 'FAIL' || res.code === 'InvalidTaskId') {
                    clearInterval(pollingTimer);
                    pollingTimer = null;
                    showStatus('转换失败: ' + (res.msg || res.code || '未知错误'), 'error');
                    $('#btn-submit').prop('disabled', false);
                } else {
                    showStatus('转换中... (' + pollCount * 2 + 's)', 'info');
                }
            });
        }, 2000);
    }

    function showPreview(taskId, contentId) {
        if (!contentId) return;
        showStatus('正在下载结果文件并注册到仓库...', 'info');
        pollForPreviewUrl(taskId, 0);
    }

    function pollForPreviewUrl(taskId, attempt) {
        if (attempt > 15) {
            showStatus('获取预览链接超时，请在任务池中手动查看', 'error');
            return;
        }
        $.get('/home/tasks/previewUrl', { taskId: taskId }, function(res) {
            if (res.success && res.docId) {
                $.get('/v2/context/driver-cb', { docId: res.docId, action: 'view', isInFrame: true }, function(url) {
                    showStatus('转换完成！预览已加载', 'success');
                    $('#preview-iframe').attr('src', url).show();
                    $('#preview-placeholder').hide();
                });
            } else {
                setTimeout(function() { pollForPreviewUrl(taskId, attempt + 1); }, 2000);
            }
        }).fail(function() {
            setTimeout(function() { pollForPreviewUrl(taskId, attempt + 1); }, 2000);
        });
    }

    function escapeHtml(text) {
        if (!text) return '';
        var div = document.createElement('div');
        div.appendChild(document.createTextNode(text));
        return div.innerHTML;
    }

    function escapeAttr(text) {
        if (!text) return '';
        return String(text).replace(/&/g, '&amp;').replace(/"/g, '&quot;').replace(/'/g, '&#39;').replace(/</g, '&lt;').replace(/>/g, '&gt;');
    }

    // ========== 请求参数实时预览 ==========
    function buildPreviewPayload() {
        if (!selectedFile) return null;

        var payload = {
            filename: selectedFile.name,
            targetFilename: selectedFile.name.replace(/\.json$/i, '.xlsx'),
            fileUrl: DEMO_BASE_URL + '/docs/download?docId=' + selectedFile.id,
            callback: DEMO_BASE_URL + '/openapi/callback'
        };

        var opts = {};
        var sheetName = $('#sheetName').val().trim();
        var columnOrder = $('#columnOrder').val().trim();

        if (sheetName) opts.sheetName = sheetName;
        if (columnOrder) {
            opts.columnOrder = columnOrder.split(',').map(function(s) { return s.trim(); }).filter(Boolean);
        }

        if (Object.keys(opts).length > 0) {
            payload.jsonToExcelOptions = opts;
        }
        return payload;
    }

    function syntaxHighlight(json) {
        var str = JSON.stringify(json, null, 2);
        str = str.replace(/&/g, '&amp;').replace(/</g, '&lt;').replace(/>/g, '&gt;');
        return str.replace(/("(\\u[a-zA-Z0-9]{4}|\\[^u]|[^\\"])*"(\s*:)?|\b(true|false)\b|-?\d+(?:\.\d*)?(?:[eE][+\-]?\d+)?|\bnull\b)/g, function(match) {
            var cls = 'json-number';
            if (/^"/.test(match)) {
                if (/:$/.test(match)) {
                    cls = 'json-key';
                    match = match.replace(/:$/, '') + ':';
                } else {
                    cls = 'json-string';
                }
            } else if (/true|false/.test(match)) {
                cls = 'json-bool';
            } else if (/null/.test(match)) {
                cls = 'json-null';
            }
            return '<span class="' + cls + '">' + match + '</span>';
        });
    }

    function updateRequestPreview() {
        var payload = buildPreviewPayload();
        var el = $('#request-preview');
        if (!payload) {
            el.text('请先选择 JSON 文件');
        } else {
            el.html(syntaxHighlight(payload));
        }
    }

    $(function() {
        renderJsonFileList();

        // 监听所有参数变化，实时刷新预览
        $('#sheetName, #columnOrder').on('input', updateRequestPreview);
    });
</script>
</body>
</html>
