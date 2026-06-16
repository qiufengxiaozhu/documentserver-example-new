<!DOCTYPE html>
<html lang="zh-CN">
<head>
    <meta charset="UTF-8">
    <title>添加水印</title>
    <link href="/static/bootstrap.min.css" rel="stylesheet">
    <style>
        .wm-container { display: flex; height: calc(100vh - 20px); padding: 10px; gap: 15px; }
        .wm-left { width: 500px; min-width: 500px; overflow-y: auto; }
        .wm-right { flex: 1; display: flex; flex-direction: column; }
        .wm-section { margin-bottom: 15px; }
        .wm-section h6 { margin-bottom: 10px; font-weight: 600; }
        .format-tabs { display: flex; gap: 5px; margin-bottom: 10px; }
        .format-tabs .btn { min-width: 60px; }
        .format-tabs .btn.active { font-weight: bold; }
        .file-list { max-height: 180px; overflow-y: auto; border: 1px solid #dee2e6; border-radius: 4px; }
        .file-item { padding: 6px 12px; cursor: pointer; font-size: 13px; display: flex; align-items: center; }
        .file-item:hover { background: #f8f9fa; }
        .file-item.selected { background: #e7f1ff; font-weight: 500; }
        .file-item .file-ext { color: #999; font-size: 11px; margin-left: auto; }
        .type-radios { display: flex; flex-wrap: wrap; gap: 8px; }
        .type-radios label { cursor: pointer; padding: 6px 14px; border: 1px solid #ccc; border-radius: 4px; font-size: 13px; transition: all 0.2s; }
        .type-radios input:checked + label { background: #007bff; color: #fff; border-color: #007bff; }
        .type-radios input { display: none; }
        .json-editor { width: 100%; height: 220px; font-family: 'Consolas', 'Monaco', monospace; font-size: 12px; border: 1px solid #ced4da; border-radius: 4px; padding: 8px; resize: vertical; }
        .preview-area { flex: 1; border: 1px solid #dee2e6; border-radius: 4px; background: #f8f9fa; display: flex; align-items: center; justify-content: center; min-height: 300px; }
        .preview-area iframe { width: 100%; height: 100%; border: none; }
        .preview-placeholder { color: #999; font-size: 14px; }
        .status-msg { padding: 10px; margin-bottom: 10px; border-radius: 4px; display: none; }
        .status-msg.show { display: block; }
        .status-msg.info { background: #cce5ff; color: #004085; }
        .status-msg.success { background: #d4edda; color: #155724; }
        .status-msg.error { background: #f8d7da; color: #721c24; }
        .pic-select-section { background: #f0f7ff; border: 1px solid #b8daff; border-radius: 4px; padding: 10px; margin-top: 10px; }
        .pic-select-section h6 { font-size: 13px; color: #004085; margin-bottom: 8px; }
        .pic-file-list { max-height: 120px; overflow-y: auto; border: 1px solid #dee2e6; border-radius: 4px; background: #fff; }
        .pic-file-item { padding: 5px 12px; cursor: pointer; font-size: 12px; display: flex; align-items: center; }
        .pic-file-item:hover { background: #f8f9fa; }
        .pic-file-item.selected { background: #fff3cd; font-weight: 500; }
        .pic-file-item .file-ext { color: #999; font-size: 10px; margin-left: auto; }
        .pic-url-display { font-size: 11px; color: #666; margin-top: 6px; word-break: break-all; background: #fff; padding: 4px 8px; border-radius: 3px; border: 1px solid #eee; }
        .ppt-hint { font-size: 12px; color: #856404; background: #fff3cd; border: 1px solid #ffc107; border-radius: 4px; padding: 6px 10px; margin-top: 8px; }
    </style>
</head>
<body>
<div class="wm-container">
    <div class="wm-left">
        <div class="wm-section">
            <h6>1. 选择目标文件 <small class="text-muted">（要添加水印的文档）</small></h6>
            <div class="format-tabs" id="doc-format-tabs">
                <button class="btn btn-sm btn-outline-primary active" onclick="filterDocFormat('all', this)">全部</button>
                <button class="btn btn-sm btn-outline-primary" onclick="filterDocFormat('word', this)">Word</button>
                <button class="btn btn-sm btn-outline-primary" onclick="filterDocFormat('ppt', this)">PPT</button>
                <button class="btn btn-sm btn-outline-primary" onclick="filterDocFormat('pdf', this)">PDF</button>
                <button class="btn btn-sm btn-outline-primary" onclick="filterDocFormat('ofd', this)">OFD</button>
            </div>
            <div class="file-list" id="doc-file-list"></div>
            <div id="ppt-hint" class="ppt-hint" style="display:none;">
                &#9432; PPT 加水印实际流程：先调用转换接口将 PPT 转为 PDF，然后在 PDF 上添加水印（两步串联）
            </div>
        </div>

        <div class="wm-section">
            <h6>2. 选择水印类型</h6>
            <div class="type-radios">
                <input type="radio" name="wmType" id="type-tiled-text" value="ApplyWatermarkForFixed" checked
                       onchange="onTypeChange()">
                <label for="type-tiled-text">平铺文字</label>

                <input type="radio" name="wmType" id="type-fixed-text" value="ApplyWatermark"
                       onchange="onTypeChange()">
                <label for="type-fixed-text">固定文字</label>

                <input type="radio" name="wmType" id="type-fixed-pic" value="ApplyPicWatermark"
                       onchange="onTypeChange()">
                <label for="type-fixed-pic">固定图片</label>

                <input type="radio" name="wmType" id="type-tiled-pic" value="ApplyTiledImgWatermarkForFixed"
                       onchange="onTypeChange()">
                <label for="type-tiled-pic">平铺图片</label>
            </div>

            <!-- 图片水印选择区 -->
            <div class="pic-select-section" id="pic-select-section" style="display:none;">
                <h6>选择水印图片 <small class="text-muted">（从仓库中选择图片文件）</small></h6>
                <div class="pic-file-list" id="pic-file-list"></div>
                <div class="pic-url-display" id="pic-url-display" style="display:none;"></div>
            </div>
        </div>

        <div class="wm-section">
            <h6>3. 配置参数 <small class="text-muted">(JSON 格式，可直接编辑或粘贴)</small></h6>
            <textarea class="json-editor" id="params-editor"></textarea>
        </div>

        <div class="wm-section">
            <button class="btn btn-primary btn-block" onclick="submitWatermark()" id="btn-submit">
                确认添加水印
            </button>
        </div>

        <div class="status-msg" id="status-msg"></div>
    </div>

    <div class="wm-right">
        <h6>预览区</h6>
        <div class="preview-area" id="preview-area">
            <span class="preview-placeholder" id="preview-placeholder">提交水印任务后，结果将在此处预览</span>
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
    let pollingTimer = null;

    const docFormatMap = {
        'word': ['.doc', '.docx', '.wps'],
        'ppt': ['.ppt', '.pptx', '.pps', '.ppsx', '.pptm', '.ppsm', '.potx', '.potm'],
        'pdf': ['.pdf'],
        'ofd': ['.ofd']
    };
    const imageExts = ['.png', '.jpg', '.jpeg', '.gif', '.bmp', '.svg', '.webp', '.ico'];

    const defaultParams = {
        'ApplyWatermarkForFixed': [{
            "actId": "ApplyWatermarkForFixed",
            "options": {
                "line1": "机密文档",
                "line2": "",
                "line3": "",
                "withDate": true,
                "font": "黑体",
                "fontcolor": "#808080",
                "fontsize": "24",
                "isFontBold": false,
                "isFontItalic": false,
                "transparent": 50,
                "rotation": -45,
                "spacing": 100
            }
        }],
        'ApplyWatermark': [{
            "actId": "ApplyWatermark",
            "options": {
                "text": "机密",
                "fontcolor": "#808080",
                "fontsize": 36,
                "font": "黑体",
                "rotation": -45,
                "opacity": 50,
                "position": "CENTER"
            }
        }],
        'ApplyPicWatermark': [{
            "actId": "ApplyPicWatermark",
            "options": {
                "picUrl": "",
                "picName": "watermark.png",
                "isErosion": false,
                "rotation": 0,
                "position": "CENTER",
                'picScale': 100,
                "posXInCm":3,
                "posYInCm":3,
                'widthInCm':5,
                'heightInCm':5,
                "zoom": 1
            }
        }],
        'ApplyTiledImgWatermarkForFixed': [{
            "actId": "ApplyTiledImgWatermarkForFixed",
            "options": {
                "picUrl": "",
                "picName": "watermark.png",
                "transparent": 50,
                "rotation": 0,
                "spacing": 100
            }
        }]
    };

    function getFileExt(name) {
        const idx = name.lastIndexOf('.');
        return idx >= 0 ? name.substring(idx).toLowerCase() : '';
    }

    function isImageFile(name) {
        return imageExts.some(e => name.toLowerCase().endsWith(e));
    }

    function isPicWatermarkType(type) {
        return type === 'ApplyPicWatermark' || type === 'ApplyTiledImgWatermarkForFixed';
    }

    /**
     * 根据 docId 拼接 server 可访问的文件下载 URL
     */
    function buildPicUrl(docId) {
        return DEMO_BASE_URL + '/' + docId + '/content';
    }

    // ========== 目标文件列表 ==========
    function filterDocFormat(format, btn) {
        $('#doc-format-tabs .btn').removeClass('active');
        $(btn).addClass('active');
        renderDocFileList(format);
    }

    function renderDocFileList(format) {
        const container = $('#doc-file-list');
        container.empty();
        let filtered = allFiles.filter(f => !isImageFile(f.name));
        if (format && format !== 'all') {
            const exts = docFormatMap[format] || [];
            filtered = filtered.filter(f => exts.some(e => f.name.toLowerCase().endsWith(e)));
        }
        if (filtered.length === 0) {
            container.append('<div class="p-3 text-muted text-center">无匹配文件</div>');
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

    function isPptFile(name) {
        return docFormatMap['ppt'].some(e => name.toLowerCase().endsWith(e));
    }

    function selectDocFile(el) {
        $('#doc-file-list .file-item').removeClass('selected');
        $(el).addClass('selected');
        selectedFile = { id: $(el).data('id'), name: $(el).data('name') };
        // PPT 文件显示提示
        if (isPptFile(selectedFile.name)) {
            $('#ppt-hint').show();
        } else {
            $('#ppt-hint').hide();
        }
    }

    // ========== 水印图片选择 ==========
    function renderPicFileList() {
        const container = $('#pic-file-list');
        container.empty();
        const imageFiles = allFiles.filter(f => isImageFile(f.name));
        if (imageFiles.length === 0) {
            container.append('<div class="p-2 text-muted text-center" style="font-size:12px">仓库中暂无图片文件，请先上传</div>');
            return;
        }
        imageFiles.forEach(function(file) {
            const ext = getFileExt(file.name);
            const selected = selectedPicFile && selectedPicFile.id === file.id ? ' selected' : '';
            container.append(
                '<div class="pic-file-item' + selected + '" data-id="' + file.id + '" data-name="' + file.name +
                '" onclick="selectPicFile(this)">' +
                '<span>' + escapeHtml(file.name) + '</span>' +
                '<span class="file-ext">' + ext + '</span></div>'
            );
        });
    }

    function selectPicFile(el) {
        $('#pic-file-list .pic-file-item').removeClass('selected');
        $(el).addClass('selected');
        const id = $(el).data('id');
        const name = $(el).data('name');
        selectedPicFile = { id: id, name: name };

        const picUrl = buildPicUrl(id);
        $('#pic-url-display').show().html('<strong>picUrl:</strong> ' + escapeHtml(picUrl));

        updatePicUrlInParams(picUrl, name);
    }

    /**
     * 自动把选中图片的 URL 和文件名写入 JSON 参数
     */
    function updatePicUrlInParams(picUrl, picName) {
        try {
            const params = JSON.parse($('#params-editor').val());
            if (Array.isArray(params) && params.length > 0 && params[0].options) {
                params[0].options.picUrl = picUrl;
                params[0].options.picName = picName;
                $('#params-editor').val(JSON.stringify(params, null, 2));
            }
        } catch (e) {
            // JSON 格式不正确时不自动替换
        }
    }

    // ========== 水印类型切换 ==========
    function getSelectedType() {
        return $('input[name="wmType"]:checked').val();
    }

    function onTypeChange() {
        const type = getSelectedType();
        const isPic = isPicWatermarkType(type);

        if (isPic) {
            $('#pic-select-section').show();
            renderPicFileList();
        } else {
            $('#pic-select-section').hide();
            selectedPicFile = null;
            $('#pic-url-display').hide();
        }

        const params = JSON.parse(JSON.stringify(defaultParams[type] || []));

        if (isPic && selectedPicFile) {
            params[0].options.picUrl = buildPicUrl(selectedPicFile.id);
            params[0].options.picName = selectedPicFile.name;
        }
        $('#params-editor').val(JSON.stringify(params, null, 2));
    }

    // ========== 状态提示 ==========
    function showStatus(msg, type) {
        const el = $('#status-msg');
        el.removeClass('info success error show').addClass(type + ' show').text(msg);
    }

    function hideStatus() {
        $('#status-msg').removeClass('show');
    }

    // ========== 提交 ==========
    function submitWatermark() {
        if (!selectedFile) {
            showStatus('请先选择一个目标文件', 'error');
            return;
        }
        const watermarkType = getSelectedType();
        if (isPicWatermarkType(watermarkType) && !selectedPicFile) {
            showStatus('图片水印模式，请选择一张水印图片', 'error');
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

        $('#btn-submit').prop('disabled', true);

        // PPT 文件需先转 PDF 再加水印（两步串联）
        if (isPptFile(selectedFile.name)) {
            showStatus('PPT 加水印：步骤1/2 - 正在将 PPT 转换为 PDF...', 'info');
            $.ajax({
                url: '/home/convert/submit',
                type: 'POST',
                contentType: 'application/json',
                data: JSON.stringify({
                    docId: selectedFile.id,
                    docName: selectedFile.name,
                    targetFilename: selectedFile.name.replace(/\.[^/.]+$/, '') + '.pdf',
                    watermarkJson: null
                }),
                success: function(res) {
                    if (res.success) {
                        showStatus('PPT 加水印：步骤1 转换已提交 (taskId: ' + res.taskId + ')，等待转换完成...', 'info');
                        pollConvertThenWatermark(res.taskId, watermarkType, opsJson);
                    } else {
                        showStatus('PPT 转 PDF 失败: ' + (res.error || '未知错误'), 'error');
                        $('#btn-submit').prop('disabled', false);
                    }
                },
                error: function(xhr) {
                    showStatus('请求失败: ' + xhr.statusText, 'error');
                    $('#btn-submit').prop('disabled', false);
                }
            });
            return;
        }

        showStatus('正在提交任务...', 'info');
        $.ajax({
            url: '/home/watermark/submit',
            type: 'POST',
            contentType: 'application/json',
            data: JSON.stringify({
                docId: selectedFile.id,
                docName: selectedFile.name,
                watermarkType: watermarkType,
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
                    showStatus('任务完成！', 'success');
                    $('#btn-submit').prop('disabled', false);
                    showPreview(taskId, res.contentId);
                } else if (status === 'FAIL' || res.code === 'InvalidTaskId') {
                    clearInterval(pollingTimer);
                    pollingTimer = null;
                    showStatus('任务失败: ' + (res.msg || res.code || '未知错误'), 'error');
                    $('#btn-submit').prop('disabled', false);
                } else {
                    showStatus('处理中... (' + pollCount + 's)', 'info');
                }
            });
        }, 2000);
    }

    /**
     * PPT 加水印第二步：轮询转换状态，成功后获取 PDF docId 并提交水印任务
     */
    function pollConvertThenWatermark(convertTaskId, watermarkType, opsJson) {
        if (pollingTimer) clearInterval(pollingTimer);
        let pollCount = 0;
        pollingTimer = setInterval(function() {
            pollCount++;
            $.get('/home/tasks/taskStatus', { taskId: convertTaskId }, function(res) {
                const status = res.taskStatus || res.code;
                if (status === 'SUCCESS') {
                    clearInterval(pollingTimer);
                    pollingTimer = null;
                    showStatus('PPT 加水印：步骤1 转换完成！步骤2/2 - 正在提交水印任务...', 'info');
                    // 获取转换结果的 docId，然后提交水印任务
                    $.get('/home/tasks/previewUrl', { taskId: convertTaskId }, function(previewRes) {
                        if (previewRes.success && previewRes.docId) {
                            submitWatermarkForPdf(previewRes.docId, watermarkType, opsJson);
                        } else {
                            // 稍等后重试获取 docId
                            setTimeout(function() {
                                $.get('/home/tasks/previewUrl', { taskId: convertTaskId }, function(r2) {
                                    if (r2.success && r2.docId) {
                                        submitWatermarkForPdf(r2.docId, watermarkType, opsJson);
                                    } else {
                                        showStatus('获取转换结果失败，请在任务池中查看', 'error');
                                        $('#btn-submit').prop('disabled', false);
                                    }
                                });
                            }, 2000);
                        }
                    });
                } else if (status === 'FAIL' || res.code === 'InvalidTaskId') {
                    clearInterval(pollingTimer);
                    pollingTimer = null;
                    showStatus('PPT 转 PDF 失败: ' + (res.msg || res.code || '未知错误'), 'error');
                    $('#btn-submit').prop('disabled', false);
                } else {
                    showStatus('PPT 加水印：步骤1/2 转换中... (' + pollCount * 2 + 's)', 'info');
                }
            });
        }, 2000);
    }

    /**
     * PPT 加水印第二步：使用转换得到的 PDF 提交水印任务
     */
    function submitWatermarkForPdf(pdfDocId, watermarkType, opsJson) {
        // docName 需要以 .pdf 结尾，server 根据扩展名判断文件类型
        var pdfDocName = selectedFile ? selectedFile.name.replace(/\.[^/.]+$/, '.pdf') : 'output.pdf';
        $.ajax({
            url: '/home/watermark/submit',
            type: 'POST',
            contentType: 'application/json',
            data: JSON.stringify({
                docId: pdfDocId,
                docName: pdfDocName,
                watermarkType: watermarkType,
                opsJson: opsJson
            }),
            success: function(res) {
                if (res.success) {
                    showStatus('PPT 加水印：步骤2 水印任务已提交 (taskId: ' + res.taskId + ')，处理中...', 'info');
                    startPolling(res.taskId);
                } else {
                    showStatus('提交水印任务失败: ' + (res.error || '未知错误'), 'error');
                    $('#btn-submit').prop('disabled', false);
                }
            },
            error: function(xhr) {
                showStatus('提交水印任务请求失败: ' + xhr.statusText, 'error');
                $('#btn-submit').prop('disabled', false);
            }
        });
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
        const div = document.createElement('div');
        div.appendChild(document.createTextNode(text));
        return div.innerHTML;
    }

    // 初始化
    $(function() {
        onTypeChange();
        renderDocFileList('all');
    });
</script>
</body>
</html>
