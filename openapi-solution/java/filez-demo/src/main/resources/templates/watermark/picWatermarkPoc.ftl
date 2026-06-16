<!DOCTYPE html>
<html lang="zh-CN">
<head>
    <meta charset="UTF-8">
    <title>图片水印POC</title>
    <link href="/static/bootstrap.min.css" rel="stylesheet">
    <style>
        .wm-container { display: flex; height: calc(100vh - 20px); padding: 10px; gap: 15px; }
        .wm-left { width: 540px; min-width: 540px; overflow-y: auto; }
        .wm-right { flex: 1; display: flex; flex-direction: column; }
        .wm-section { margin-bottom: 15px; }
        .wm-section h6 { margin-bottom: 10px; font-weight: 600; }
        .file-list { max-height: 150px; overflow-y: auto; border: 1px solid #dee2e6; border-radius: 4px; }
        .file-item { padding: 6px 12px; cursor: pointer; font-size: 13px; display: flex; align-items: center; }
        .file-item:hover { background: #f8f9fa; }
        .file-item.selected { background: #e7f1ff; font-weight: 500; }
        .file-item .file-ext { color: #999; font-size: 11px; margin-left: auto; }
        .pic-select-section { background: #f0f7ff; border: 1px solid #b8daff; border-radius: 4px; padding: 10px; margin-top: 8px; }
        .pic-select-section h6 { font-size: 13px; color: #004085; margin-bottom: 8px; }
        .pic-file-list { max-height: 100px; overflow-y: auto; border: 1px solid #dee2e6; border-radius: 4px; background: #fff; }
        .pic-file-item { padding: 5px 12px; cursor: pointer; font-size: 12px; display: flex; align-items: center; }
        .pic-file-item:hover { background: #f8f9fa; }
        .pic-file-item.selected { background: #fff3cd; font-weight: 500; }
        .pic-file-item .file-ext { color: #999; font-size: 10px; margin-left: auto; }
        .json-editor { width: 100%; height: 280px; font-family: 'Consolas', 'Monaco', monospace; font-size: 12px; border: 1px solid #ced4da; border-radius: 4px; padding: 8px; resize: vertical; }
        .preview-area { flex: 1; border: 1px solid #dee2e6; border-radius: 4px; background: #f8f9fa; display: flex; align-items: center; justify-content: center; min-height: 300px; }
        .preview-area iframe { width: 100%; height: 100%; border: none; }
        .preview-placeholder { color: #999; font-size: 14px; }
        .status-msg { padding: 10px; margin-bottom: 10px; border-radius: 4px; display: none; }
        .status-msg.show { display: block; }
        .status-msg.info { background: #cce5ff; color: #004085; }
        .status-msg.success { background: #d4edda; color: #155724; }
        .status-msg.error { background: #f8d7da; color: #721c24; }
        .hint-text { font-size: 11px; color: #888; margin-top: 4px; }
    </style>
</head>
<body>
<div class="wm-container">
    <div class="wm-left">
        <!-- 步骤1：选择 Word 文件 -->
        <div class="wm-section">
            <h6>1. 选择 Word 文件</h6>
            <div class="file-list" id="doc-file-list"></div>
            <div class="hint-text">仅支持 Word 文档（.doc/.docx）</div>
        </div>

        <!-- 步骤2：选择主水印图片 -->
        <div class="wm-section">
            <h6>2. 选择主水印图片 <small class="text-muted">（可选，已有默认值）</small></h6>
            <div class="pic-select-section">
                <div class="pic-file-list" id="main-pic-list"></div>
            </div>
            <div class="hint-text">固定图片水印；不选则使用默认参数中的 picUrl，提交前会校验图片是否存在</div>
        </div>

        <!-- 步骤3：选择 qrCode 图片 -->
        <div class="wm-section">
            <h6>3. 选择 qrCode 图片 <small class="text-muted">（可选，已有默认值）</small></h6>
            <div class="pic-select-section">
                <div class="pic-file-list" id="qr-pic-list"></div>
            </div>
            <div class="hint-text">qrCode 使用绝对定位（posXInCm/posYInCm），非水印属性</div>
        </div>

        <!-- 步骤4：参数编辑 -->
        <div class="wm-section">
            <h6>4. 参数配置 <small class="text-muted">(可手动编辑 JSON)</small></h6>
            <textarea class="json-editor" id="params-editor"></textarea>
        </div>

        <div class="wm-section">
            <button class="btn btn-primary btn-block" onclick="submitTask()" id="btn-submit">
                提交任务
            </button>
        </div>

        <div class="status-msg" id="status-msg"></div>
    </div>

    <div class="wm-right">
        <h6>预览区</h6>
        <div class="preview-area" id="preview-area">
            <span class="preview-placeholder" id="preview-placeholder">提交任务后，结果将在此处预览</span>
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
    let selectedMainPic = null;
    let selectedQrPic = null;
    let pollingTimer = null;

    const wordExts = ['.doc', '.docx', '.wps', '.dot', '.wpt', '.dotx', '.docm', '.dotm'];
    const imageExts = ['.png', '.jpg', '.jpeg', '.gif', '.bmp', '.svg', '.webp', '.ico'];

    function buildDefaultOps() {
        return [{
            "actId": "ApplyPicWatermark",
            "options": {
                "picUrl": DEMO_BASE_URL + '/local-filez/content',
                "picName": "local-filez.png",
                "isErosion": false,
                "rotation": 0,
                "picScale": 100,
                "widthInCm": 5,
                "heightInCm": 5,
                "position": "CENTER",
                "zoom": 1,
                "qrCode": {
                    "picUrl": DEMO_BASE_URL + '/local-qrcode/content',
                    "picName": "qrcode.png",
                    "isErosion": false,
                    "rotation": 0,
                    "picScale": 10,
                    "posXInCm": 0,
                    "posYInCm": 0
                }
            }
        }];
    }

    function getFileExt(name) {
        var idx = name.lastIndexOf('.');
        return idx >= 0 ? name.substring(idx).toLowerCase() : '';
    }

    function isWordFile(name) {
        return wordExts.some(function(e) { return name.toLowerCase().endsWith(e); });
    }

    function isImageFile(name) {
        return imageExts.some(function(e) { return name.toLowerCase().endsWith(e); });
    }

    function buildPicUrl(docId) {
        return DEMO_BASE_URL + '/' + docId + '/content';
    }

    // ========== Word 文件列表 ==========
    function renderDocFileList() {
        var container = $('#doc-file-list');
        container.empty();
        var filtered = allFiles.filter(function(f) { return isWordFile(f.name); });
        if (filtered.length === 0) {
            container.append('<div class="p-3 text-muted text-center">仓库中暂无 Word 文件</div>');
            return;
        }
        filtered.forEach(function(file) {
            var ext = getFileExt(file.name);
            var selected = selectedFile && selectedFile.id === file.id ? ' selected' : '';
            container.append(
                '<div class="file-item' + selected + '" data-id="' + file.id + '" data-name="' + escapeAttr(file.name) +
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

    // ========== 主水印图片列表 ==========
    function renderMainPicList() {
        var container = $('#main-pic-list');
        container.empty();
        var imageFiles = allFiles.filter(function(f) { return isImageFile(f.name); });
        if (imageFiles.length === 0) {
            container.append('<div class="p-2 text-muted text-center" style="font-size:12px">仓库中暂无图片文件</div>');
            return;
        }
        imageFiles.forEach(function(file) {
            var ext = getFileExt(file.name);
            var selected = selectedMainPic && selectedMainPic.id === file.id ? ' selected' : '';
            container.append(
                '<div class="pic-file-item' + selected + '" data-id="' + file.id + '" data-name="' + escapeAttr(file.name) +
                '" onclick="selectMainPic(this)">' +
                '<span>' + escapeHtml(file.name) + '</span>' +
                '<span class="file-ext">' + ext + '</span></div>'
            );
        });
    }

    function selectMainPic(el) {
        $('#main-pic-list .pic-file-item').removeClass('selected');
        $(el).addClass('selected');
        selectedMainPic = { id: $(el).data('id'), name: $(el).data('name') };
        updateParamsEditor();
    }

    // ========== qrCode 图片列表 ==========
    function renderQrPicList() {
        var container = $('#qr-pic-list');
        container.empty();
        var imageFiles = allFiles.filter(function(f) { return isImageFile(f.name); });
        if (imageFiles.length === 0) {
            container.append('<div class="p-2 text-muted text-center" style="font-size:12px">仓库中暂无图片文件</div>');
            return;
        }
        imageFiles.forEach(function(file) {
            var ext = getFileExt(file.name);
            var selected = selectedQrPic && selectedQrPic.id === file.id ? ' selected' : '';
            container.append(
                '<div class="pic-file-item' + selected + '" data-id="' + file.id + '" data-name="' + escapeAttr(file.name) +
                '" onclick="selectQrPic(this)">' +
                '<span>' + escapeHtml(file.name) + '</span>' +
                '<span class="file-ext">' + ext + '</span></div>'
            );
        });
    }

    function selectQrPic(el) {
        $('#qr-pic-list .pic-file-item').removeClass('selected');
        $(el).addClass('selected');
        selectedQrPic = { id: $(el).data('id'), name: $(el).data('name') };
        updateParamsEditor();
    }

    // ========== 参数更新 ==========
    function updateParamsEditor() {
        try {
            var params = JSON.parse($('#params-editor').val());
            if (Array.isArray(params) && params.length > 0 && params[0].options) {
                if (selectedMainPic) {
                    params[0].options.picUrl = buildPicUrl(selectedMainPic.id);
                    params[0].options.picName = selectedMainPic.name;
                }
                if (selectedQrPic && params[0].options.qrCode) {
                    params[0].options.qrCode.picUrl = buildPicUrl(selectedQrPic.id);
                    params[0].options.qrCode.picName = selectedQrPic.name;
                }
                $('#params-editor').val(JSON.stringify(params, null, 2));
            }
        } catch (e) {}
    }

    // ========== 状态 ==========
    function showStatus(msg, type) {
        $('#status-msg').removeClass('info success error show').addClass(type + ' show').text(msg);
    }

    /**
     * 从 picUrl 中提取 docId（路径格式: DEMO_BASE_URL + '/{docId}/content'）
     */
    function extractDocIdFromPicUrl(picUrl) {
        if (!picUrl || !picUrl.startsWith(DEMO_BASE_URL)) return null;
        var path = picUrl.substring(DEMO_BASE_URL.length);
        var match = path.match(/^\/([^/]+)\/content$/);
        return match ? match[1] : null;
    }

    /**
     * 检查 picUrl 对应的文件是否存在于仓库中
     */
    function isPicUrlValid(picUrl) {
        var docId = extractDocIdFromPicUrl(picUrl);
        if (!docId) return false;
        return allFiles.some(function(f) { return f.id === docId; });
    }

    // ========== 提交 ==========
    function submitTask() {
        if (!selectedFile) { showStatus('请先选择一个 Word 文件', 'error'); return; }

        var opsJson;
        var ops;
        try {
            opsJson = $('#params-editor').val().trim();
            ops = JSON.parse(opsJson);
        } catch (e) {
            showStatus('参数 JSON 格式错误，请检查', 'error');
            return;
        }

        // 校验主水印图片是否存在
        if (Array.isArray(ops) && ops.length > 0 && ops[0].options) {
            var mainPicUrl = ops[0].options.picUrl;
            if (!isPicUrlValid(mainPicUrl)) {
                showStatus('主水印图片不存在于仓库中，请检查 picUrl 或手动选择图片', 'error');
                return;
            }
            // 校验 qrCode 图片是否存在
            if (ops[0].options.qrCode && ops[0].options.qrCode.picUrl) {
                var qrPicUrl = ops[0].options.qrCode.picUrl;
                if (!isPicUrlValid(qrPicUrl)) {
                    showStatus('qrCode 图片不存在于仓库中，请检查 qrCode.picUrl 或手动选择图片', 'error');
                    return;
                }
            }
        }

        showStatus('正在提交任务...', 'info');
        $('#btn-submit').prop('disabled', true);

        $.ajax({
            url: '/home/watermark/submit',
            type: 'POST',
            contentType: 'application/json',
            data: JSON.stringify({
                docId: selectedFile.id,
                docName: selectedFile.name,
                watermarkType: 'ApplyPicWatermark',
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
        var pollCount = 0;
        pollingTimer = setInterval(function() {
            pollCount++;
            $.get('/home/tasks/taskStatus', { taskId: taskId }, function(res) {
                var status = res.taskStatus || res.code;
                if (status === 'SUCCESS') {
                    clearInterval(pollingTimer);
                    pollingTimer = null;
                    showStatus('任务完成！', 'success');
                    $('#btn-submit').prop('disabled', false);
                    showPreview(taskId);
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

    function showPreview(taskId) {
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
                    showStatus('任务完成！预览已加载', 'success');
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

    // 初始化
    $(function() {
        renderDocFileList();
        renderMainPicList();
        renderQrPicList();
        $('#params-editor').val(JSON.stringify(buildDefaultOps(), null, 2));
    });
</script>
</body>
</html>
