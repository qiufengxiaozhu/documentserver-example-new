<!DOCTYPE html>
<html lang="zh-CN">
<head>
    <meta charset="UTF-8">
    <title>文档套红</title>
    <link href="/static/bootstrap.min.css" rel="stylesheet">
    <style>
        .bm-container { display: flex; height: calc(100vh - 20px); padding: 10px; gap: 15px; }
        .bm-left { width: 520px; min-width: 520px; overflow-y: auto; }
        .bm-right { flex: 1; display: flex; flex-direction: column; }
        .bm-section { margin-bottom: 15px; }
        .bm-section h6 { margin-bottom: 10px; font-weight: 600; }
        .file-list { max-height: 160px; overflow-y: auto; border: 1px solid #dee2e6; border-radius: 4px; }
        .file-item { padding: 6px 12px; cursor: pointer; font-size: 13px; display: flex; align-items: center; }
        .file-item:hover { background: #f8f9fa; }
        .file-item.selected { background: #e7f1ff; font-weight: 500; }
        .file-item .file-ext { color: #999; font-size: 11px; margin-left: auto; }
        .type-radios { display: flex; flex-wrap: wrap; gap: 8px; }
        .type-radios label { cursor: pointer; padding: 6px 14px; border: 1px solid #ccc; border-radius: 4px; font-size: 13px; transition: all 0.2s; }
        .type-radios input:checked + label { background: #007bff; color: #fff; border-color: #007bff; }
        .type-radios input { display: none; }
        .json-editor { width: 100%; height: 280px; font-family: 'Consolas', 'Monaco', monospace; font-size: 12px; border: 1px solid #ced4da; border-radius: 4px; padding: 8px; resize: vertical; }
        .preview-area { flex: 1; border: 1px solid #dee2e6; border-radius: 4px; background: #f8f9fa; display: flex; align-items: center; justify-content: center; min-height: 300px; }
        .preview-area iframe { width: 100%; height: 100%; border: none; }
        .preview-placeholder { color: #999; font-size: 14px; }
        .status-msg { padding: 10px; margin-bottom: 10px; border-radius: 4px; display: none; }
        .status-msg.show { display: block; }
        .status-msg.info { background: #cce5ff; color: #004085; }
        .status-msg.success { background: #d4edda; color: #155724; }
        .status-msg.error { background: #f8d7da; color: #721c24; }
        .hint-box { font-size: 12px; color: #0c5460; background: #d1ecf1; border: 1px solid #bee5eb; border-radius: 4px; padding: 8px 10px; margin-top: 8px; line-height: 1.6; }
        .hint-box code { font-size: 11px; background: #fff; padding: 1px 4px; border-radius: 2px; border: 1px solid #bee5eb; }
        .ref-select-section { background: #f0f7ff; border: 1px solid #b8daff; border-radius: 4px; padding: 10px; margin-top: 10px; }
        .ref-select-section h6 { font-size: 13px; color: #004085; margin-bottom: 8px; }
        .ref-file-list { max-height: 100px; overflow-y: auto; border: 1px solid #dee2e6; border-radius: 4px; background: #fff; }
        .ref-file-item { padding: 5px 12px; cursor: pointer; font-size: 12px; display: flex; align-items: center; }
        .ref-file-item:hover { background: #f8f9fa; }
        .ref-file-item.selected { background: #fff3cd; font-weight: 500; }
        .ref-file-item .file-ext { color: #999; font-size: 10px; margin-left: auto; }
        .ref-url-display { font-size: 11px; color: #666; margin-top: 6px; word-break: break-all; background: #fff; padding: 4px 8px; border-radius: 3px; border: 1px solid #eee; }
    </style>
</head>
<body>
<div class="bm-container">
    <div class="bm-left">
        <div class="bm-section">
            <h6>1. 选择模板文件 <small class="text-muted">（含书签的 Word 文档）</small></h6>
            <div class="file-list" id="doc-file-list"></div>
            <div class="hint-box">
                &#9432; 模板文档需预先插入书签（如 BM_TITLE、BM_AUTHOR 等），套红操作将替换书签位置的内容。<br>
                支持格式：<code>doc/docx/wps</code>，源文件+引用文件总和不超过 300MB。
            </div>
        </div>

        <div class="bm-section">
            <h6>2. 书签引用类型</h6>
            <div class="type-radios">
                <input type="radio" name="bmRefType" id="type-text" value="TEXT" checked onchange="onRefTypeChange()">
                <label for="type-text">文本 (TEXT)</label>

                <input type="radio" name="bmRefType" id="type-pic" value="PIC" onchange="onRefTypeChange()">
                <label for="type-pic">图片 (PIC)</label>

                <input type="radio" name="bmRefType" id="type-doc" value="DOC" onchange="onRefTypeChange()">
                <label for="type-doc">文档 (DOC)</label>

                <input type="radio" name="bmRefType" id="type-mixed" value="MIXED" onchange="onRefTypeChange()">
                <label for="type-mixed">混合</label>
            </div>
            <div class="hint-box" id="type-hint" style="margin-top:8px;">
                TEXT：将书签替换为纯文本内容
            </div>

            <!-- 图片选择区（PIC/混合模式时显示） -->
            <div class="ref-select-section" id="pic-select-section" style="display:none;">
                <h6>选择图片文件 <small class="text-muted">（从仓库选择，选中后自动更新参数中的 picUrl）</small></h6>
                <div class="ref-file-list" id="pic-file-list"></div>
                <div class="ref-url-display" id="pic-url-display" style="display:none;"></div>
            </div>

            <!-- 文档选择区（DOC/混合模式时显示） -->
            <div class="ref-select-section" id="doc-select-section" style="display:none;">
                <h6>选择引用文档 <small class="text-muted">（从仓库选择，选中后自动更新参数中的 docUrl）</small></h6>
                <div class="ref-file-list" id="ref-doc-file-list"></div>
                <div class="ref-url-display" id="doc-url-display" style="display:none;"></div>
            </div>
        </div>

        <div class="bm-section">
            <h6>3. 配置参数 <small class="text-muted">(JSON 格式，可直接编辑)</small></h6>
            <textarea class="json-editor" id="params-editor"></textarea>
        </div>

        <div class="bm-section">
            <button class="btn btn-primary btn-block" onclick="submitBookmark()" id="btn-submit">
                确认套红
            </button>
        </div>

        <div class="status-msg" id="status-msg"></div>
    </div>

    <div class="bm-right">
        <h6>预览区</h6>
        <div class="preview-area" id="preview-area">
            <span class="preview-placeholder" id="preview-placeholder">提交套红任务后，结果将在此处预览</span>
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
    let selectedPicFile = null;
    let selectedRefDocFile = null;
    let pollingTimer = null;

    const wordExts = ['.doc', '.docx', '.wps'];
    const imageExts = ['.png', '.jpg', '.jpeg'];
    const docRefExts = ['.doc', '.docx', '.wps'];

    /**
     * 各引用类型的默认参数模板
     */
    const defaultParams = {
        'TEXT': [{
            "actId": "UpdateBookmarkRef",
            "options": {
                "args": [
                    { "bookname": "标题", "dataType": "TEXT", "dataRef": "关于开展年度考核工作的通知" },
                    { "bookname": "作者", "dataType": "TEXT", "dataRef": "张三" },
                    { "bookname": "日期", "dataType": "TEXT", "dataRef": "2026年6月16日" },
                    { "bookname": "正文", "dataType": "TEXT", "dataRef": "各部门、各单位：根据上级文件精神，现将年度考核工作有关事项通知如下..." }
                ]
            }
        }],
        'PIC': [{
            "actId": "UpdateBookmarkRef",
            "options": {
                "args": [
                    { "bookname": "图片", "dataType": "PIC", "dataRef": "__PIC_URL__", "refName": "seal.png" }
                ]
            }
        }],
        'DOC': [{
            "actId": "UpdateBookmarkRef",
            "options": {
                "args": [
                    { "bookname": "子文档", "dataType": "DOC", "dataRef": "__DOC_URL__", "refName": "attachment.docx" }
                ]
            }
        }],
        'MIXED': [{
            "actId": "UpdateBookmarkRef",
            "options": {
                "args": [
                    { "bookname": "标题", "dataType": "TEXT", "dataRef": "关于开展年度考核工作的通知" },
                    { "bookname": "作者", "dataType": "TEXT", "dataRef": "张三" },
                    { "bookname": "日期", "dataType": "TEXT", "dataRef": "2026年6月16日" },
                    { "bookname": "正文", "dataType": "TEXT", "dataRef": "各部门、各单位：根据上级文件精神，现将年度考核工作有关事项通知如下..." },
                    { "bookname": "图片", "dataType": "PIC", "dataRef": "__PIC_URL__", "refName": "local-filez.png" },
                    { "bookname": "子文档", "dataType": "DOC", "dataRef": "__DOC_URL__", "refName": "local-docx.docx" }
                ]
            }
        }]
    };

    const typeHints = {
        'TEXT': 'TEXT：将书签替换为纯文本内容',
        'PIC': 'PIC：将书签替换为图片（支持 png/jpg），需提供图片下载 URL',
        'DOC': 'DOC：将书签替换为子文档内容（支持 doc/docx/wps），需提供文档下载 URL',
        'MIXED': '混合模式：同时使用 TEXT、PIC、DOC 三种类型替换不同书签'
    };

    function getFileExt(name) {
        const idx = name.lastIndexOf('.');
        return idx >= 0 ? name.substring(idx).toLowerCase() : '';
    }

    function isWordFile(name) {
        return wordExts.some(e => name.toLowerCase().endsWith(e));
    }

    function isImageFile(name) {
        return imageExts.some(e => name.toLowerCase().endsWith(e));
    }

    /**
     * 构建文件下载 URL（仓库中文件可供 server 回调下载）
     */
    function buildFileUrl(docId) {
        return DEMO_BASE_URL + '/' + docId + '/content';
    }

    /**
     * 根据当前选中的引用类型，替换默认参数中的占位 URL
     */
    function buildDefaultOps(refType) {
        var ops = JSON.parse(JSON.stringify(defaultParams[refType]));
        var args = ops[0].options.args;

        // 查找仓库中的图片和文档用作默认值
        var picFile = allFiles.find(f => isImageFile(f.name));
        var docFile = allFiles.find(f => isWordFile(f.name) && f.name !== (selectedFile ? selectedFile.name : ''));

        for (var i = 0; i < args.length; i++) {
            if (args[i].dataType === 'PIC' && args[i].dataRef === '__PIC_URL__') {
                if (picFile) {
                    args[i].dataRef = buildFileUrl(picFile.id);
                    args[i].refName = picFile.name;
                } else {
                    args[i].dataRef = DEMO_BASE_URL + '/your-image-id/content';
                }
            }
            if (args[i].dataType === 'DOC' && args[i].dataRef === '__DOC_URL__') {
                if (docFile) {
                    args[i].dataRef = buildFileUrl(docFile.id);
                    args[i].refName = docFile.name;
                } else {
                    args[i].dataRef = DEMO_BASE_URL + '/your-doc-id/content';
                }
            }
        }
        return ops;
    }

    // ========== 文件列表 ==========
    function renderDocFileList() {
        const container = $('#doc-file-list');
        container.empty();
        let filtered = allFiles.filter(f => isWordFile(f.name));
        if (filtered.length === 0) {
            container.append('<div class="p-3 text-muted text-center">仓库中暂无 Word 文件</div>');
            return;
        }
        filtered.forEach(function(file) {
            const ext = getFileExt(file.name);
            const selected = selectedFile && selectedFile.id === file.id ? ' selected' : '';
            container.append(
                '<div class="file-item' + selected + '" data-id="' + file.id + '" data-name="' + file.name +
                '" onclick="selectDocFile(this)">' +
                '<span>' + escapeHtml(file.name) + '</span>' +
                '<span class="file-ext">' + ext + '</span></div>'
            );
        });
    }

    function selectDocFile(el) {
        $('#doc-file-list .file-item').removeClass('selected');
        $(el).addClass('selected');
        selectedFile = { id: $(el).data('id'), name: $(el).data('name') };
    }

    // ========== 引用类型切换 ==========
    function getSelectedRefType() {
        return $('input[name="bmRefType"]:checked').val();
    }

    function onRefTypeChange() {
        var refType = getSelectedRefType();
        $('#type-hint').text(typeHints[refType] || '');

        // 控制图片/文档选择区显隐
        var showPic = (refType === 'PIC' || refType === 'MIXED');
        var showDoc = (refType === 'DOC' || refType === 'MIXED');
        $('#pic-select-section').toggle(showPic);
        $('#doc-select-section').toggle(showDoc);

        if (showPic) renderPicFileList();
        if (showDoc) renderRefDocFileList();

        var ops = buildDefaultOps(refType);
        $('#params-editor').val(JSON.stringify(ops, null, 2));
    }

    // ========== 图片文件选择 ==========
    function renderPicFileList() {
        const container = $('#pic-file-list');
        container.empty();
        const picFiles = allFiles.filter(f => isImageFile(f.name));
        if (picFiles.length === 0) {
            container.append('<div class="p-2 text-muted text-center" style="font-size:12px">仓库中暂无图片文件（支持 png/jpg）</div>');
            return;
        }
        picFiles.forEach(function(file) {
            const ext = getFileExt(file.name);
            const selected = selectedPicFile && selectedPicFile.id === file.id ? ' selected' : '';
            container.append(
                '<div class="ref-file-item' + selected + '" data-id="' + file.id + '" data-name="' + file.name +
                '" onclick="selectPicFile(this)">' +
                '<span>' + escapeHtml(file.name) + '</span>' +
                '<span class="file-ext">' + ext + '</span></div>'
            );
        });
    }

    function selectPicFile(el) {
        $('#pic-file-list .ref-file-item').removeClass('selected');
        $(el).addClass('selected');
        var id = $(el).data('id');
        var name = $(el).data('name');
        selectedPicFile = { id: id, name: name };

        var picUrl = buildFileUrl(id);
        $('#pic-url-display').show().html('<strong>picUrl:</strong> ' + escapeHtml(picUrl));

        // 自动更新 JSON 参数中所有 PIC 类型的 dataRef
        updatePicRefInParams(picUrl, name);
    }

    function updatePicRefInParams(picUrl, picName) {
        try {
            var ops = JSON.parse($('#params-editor').val());
            if (Array.isArray(ops) && ops[0] && ops[0].options && ops[0].options.args) {
                ops[0].options.args.forEach(function(arg) {
                    if (arg.dataType === 'PIC') {
                        arg.dataRef = picUrl;
                        arg.refName = picName;
                    }
                });
                $('#params-editor').val(JSON.stringify(ops, null, 2));
            }
        } catch(e) {}
    }

    // ========== 引用文档选择 ==========
    function renderRefDocFileList() {
        const container = $('#ref-doc-file-list');
        container.empty();
        const docFiles = allFiles.filter(f => isWordFile(f.name) && (!selectedFile || f.id !== selectedFile.id));
        if (docFiles.length === 0) {
            container.append('<div class="p-2 text-muted text-center" style="font-size:12px">仓库中暂无可用的 Word 文件</div>');
            return;
        }
        docFiles.forEach(function(file) {
            const ext = getFileExt(file.name);
            const selected = selectedRefDocFile && selectedRefDocFile.id === file.id ? ' selected' : '';
            container.append(
                '<div class="ref-file-item' + selected + '" data-id="' + file.id + '" data-name="' + file.name +
                '" onclick="selectRefDocFile(this)">' +
                '<span>' + escapeHtml(file.name) + '</span>' +
                '<span class="file-ext">' + ext + '</span></div>'
            );
        });
    }

    function selectRefDocFile(el) {
        $('#ref-doc-file-list .ref-file-item').removeClass('selected');
        $(el).addClass('selected');
        var id = $(el).data('id');
        var name = $(el).data('name');
        selectedRefDocFile = { id: id, name: name };

        var docUrl = buildFileUrl(id);
        $('#doc-url-display').show().html('<strong>docUrl:</strong> ' + escapeHtml(docUrl));

        // 自动更新 JSON 参数中所有 DOC 类型的 dataRef
        updateDocRefInParams(docUrl, name);
    }

    function updateDocRefInParams(docUrl, docName) {
        try {
            var ops = JSON.parse($('#params-editor').val());
            if (Array.isArray(ops) && ops[0] && ops[0].options && ops[0].options.args) {
                ops[0].options.args.forEach(function(arg) {
                    if (arg.dataType === 'DOC') {
                        arg.dataRef = docUrl;
                        arg.refName = docName;
                    }
                });
                $('#params-editor').val(JSON.stringify(ops, null, 2));
            }
        } catch(e) {}
    }

    // ========== 状态提示 ==========
    function showStatus(msg, type) {
        const el = $('#status-msg');
        el.removeClass('info success error show').addClass(type + ' show').text(msg);
    }

    // ========== 提交 ==========
    function submitBookmark() {
        if (!selectedFile) {
            showStatus('请先选择一个模板文件', 'error');
            return;
        }

        let opsJson;
        try {
            opsJson = $('#params-editor').val().trim();
            JSON.parse(opsJson);
        } catch(e) {
            showStatus('参数 JSON 格式错误，请检查', 'error');
            return;
        }

        showStatus('正在提交套红任务...', 'info');
        $('#btn-submit').prop('disabled', true);

        $.ajax({
            url: '/home/bookmark/submit',
            type: 'POST',
            contentType: 'application/json',
            data: JSON.stringify({
                docId: selectedFile.id,
                docName: selectedFile.name,
                opsJson: opsJson
            }),
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
        let pollCount = 0;
        pollingTimer = setInterval(function() {
            pollCount++;
            $.get('/home/tasks/taskStatus', { taskId: taskId }, function(res) {
                const status = res.taskStatus || res.code;
                if (status === 'SUCCESS') {
                    clearInterval(pollingTimer);
                    pollingTimer = null;
                    showStatus('套红完成！', 'success');
                    $('#btn-submit').prop('disabled', false);
                    showPreview(taskId, res.contentId);
                } else if (status === 'FAIL' || res.code === 'InvalidTaskId') {
                    clearInterval(pollingTimer);
                    pollingTimer = null;
                    showStatus('任务失败: ' + (res.msg || res.code || '未知错误'), 'error');
                    $('#btn-submit').prop('disabled', false);
                } else {
                    showStatus('处理中... (' + pollCount * 2 + 's)', 'info');
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
                    showStatus('套红完成！预览已加载', 'success');
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
        const div = document.createElement('div');
        div.appendChild(document.createTextNode(text));
        return div.innerHTML;
    }

    // 初始化
    $(function() {
        renderDocFileList();
        onRefTypeChange();
    });
</script>
</body>
</html>
