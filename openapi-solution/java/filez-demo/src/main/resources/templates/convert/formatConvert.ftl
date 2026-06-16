<!DOCTYPE html>
<html lang="zh-CN">
<head>
    <meta charset="UTF-8">
    <title>格式转换</title>
    <link href="/static/bootstrap.min.css" rel="stylesheet">
    <style>
        .cv-container { display: flex; height: calc(100vh - 20px); padding: 10px; gap: 15px; }
        .cv-left { width: 520px; min-width: 520px; overflow-y: auto; }
        .cv-right { flex: 1; display: flex; flex-direction: column; }
        .cv-section { margin-bottom: 15px; }
        .cv-section h6 { margin-bottom: 10px; font-weight: 600; }
        .format-tabs { display: flex; gap: 5px; margin-bottom: 10px; flex-wrap: wrap; }
        .format-tabs .btn { min-width: 60px; }
        .format-tabs .btn.active { font-weight: bold; }
        .file-list { max-height: 180px; overflow-y: auto; border: 1px solid #dee2e6; border-radius: 4px; }
        .file-item { padding: 6px 12px; cursor: pointer; font-size: 13px; display: flex; align-items: center; }
        .file-item:hover { background: #f8f9fa; }
        .file-item.selected { background: #e7f1ff; font-weight: 500; }
        .file-item .file-ext { color: #999; font-size: 11px; margin-left: auto; }
        .target-section { display: flex; gap: 8px; flex-wrap: wrap; }
        .target-section label { cursor: pointer; padding: 6px 14px; border: 1px solid #ccc; border-radius: 4px; font-size: 13px; transition: all 0.2s; }
        .target-section input:checked + label { background: #28a745; color: #fff; border-color: #28a745; }
        .target-section input { display: none; }
        .target-section label.disabled { opacity: 0.4; pointer-events: none; }
        .wm-toggle { margin-top: 10px; }
        .wm-options { margin-top: 10px; display: none; }
        .wm-type-radios { display: flex; gap: 8px; flex-wrap: wrap; margin-bottom: 10px; }
        .wm-type-radios label { cursor: pointer; padding: 5px 12px; border: 1px solid #ccc; border-radius: 4px; font-size: 12px; transition: all 0.2s; }
        .wm-type-radios input:checked + label { background: #6f42c1; color: #fff; border-color: #6f42c1; }
        .wm-type-radios input { display: none; }
        .json-editor { width: 100%; height: 200px; font-family: 'Consolas', 'Monaco', monospace; font-size: 12px; border: 1px solid #ced4da; border-radius: 4px; padding: 8px; resize: vertical; }
        .preview-area { flex: 1; border: 1px solid #dee2e6; border-radius: 4px; background: #f8f9fa; display: flex; align-items: center; justify-content: center; min-height: 300px; }
        .preview-area iframe { width: 100%; height: 100%; border: none; }
        .preview-placeholder { color: #999; font-size: 14px; }
        .status-msg { padding: 10px; margin-bottom: 10px; border-radius: 4px; display: none; }
        .status-msg.show { display: block; }
        .status-msg.info { background: #cce5ff; color: #004085; }
        .status-msg.success { background: #d4edda; color: #155724; }
        .status-msg.error { background: #f8d7da; color: #721c24; }
        .convert-hint { font-size: 11px; color: #888; margin-top: 5px; }
        .pic-select-section { background: #f0f7ff; border: 1px solid #b8daff; border-radius: 4px; padding: 10px; margin-top: 10px; }
        .pic-select-section h6 { font-size: 13px; color: #004085; margin-bottom: 8px; }
        .pic-file-list { max-height: 100px; overflow-y: auto; border: 1px solid #dee2e6; border-radius: 4px; background: #fff; }
        .pic-file-item { padding: 5px 12px; cursor: pointer; font-size: 12px; display: flex; align-items: center; }
        .pic-file-item:hover { background: #f8f9fa; }
        .pic-file-item.selected { background: #fff3cd; font-weight: 500; }
        .pic-file-item .file-ext { color: #999; font-size: 10px; margin-left: auto; }
        .pic-url-display { font-size: 11px; color: #666; margin-top: 6px; word-break: break-all; background: #fff; padding: 4px 8px; border-radius: 3px; border: 1px solid #eee; }
    </style>
</head>
<body>
<div class="cv-container">
    <div class="cv-left">
        <!-- 步骤1：选源文件 -->
        <div class="cv-section">
            <h6>1. 选择源文件</h6>
            <div class="format-tabs" id="src-format-tabs">
                <button class="btn btn-sm btn-outline-primary active" onclick="filterSrcFormat('all', this)">全部</button>
                <button class="btn btn-sm btn-outline-primary" onclick="filterSrcFormat('word', this)">Word</button>
                <button class="btn btn-sm btn-outline-primary" onclick="filterSrcFormat('excel', this)">Excel</button>
                <button class="btn btn-sm btn-outline-primary" onclick="filterSrcFormat('ppt', this)">PPT</button>
                <button class="btn btn-sm btn-outline-primary" onclick="filterSrcFormat('pdf', this)">PDF</button>
            </div>
            <div class="file-list" id="src-file-list"></div>
        </div>

        <!-- 步骤2：选目标格式 -->
        <div class="cv-section">
            <h6>2. 选择目标格式</h6>
            <div class="target-section" id="target-section">
                <input type="radio" name="targetFmt" id="target-pdf" value="pdf" onchange="onTargetChange()">
                <label for="target-pdf" id="lbl-target-pdf">PDF</label>
                <input type="radio" name="targetFmt" id="target-ofd" value="ofd" onchange="onTargetChange()">
                <label for="target-ofd" id="lbl-target-ofd">OFD</label>
            </div>
            <div class="convert-hint" id="convert-hint">请先选择源文件</div>
        </div>

        <!-- 步骤3：可选水印 -->
        <div class="cv-section">
            <h6>3. 水印设置 <small class="text-muted">（可选）</small></h6>
            <div class="wm-toggle">
                <div class="form-check">
                    <input class="form-check-input" type="checkbox" id="wm-enable" onchange="toggleWatermark()">
                    <label class="form-check-label" for="wm-enable">转换时添加水印</label>
                </div>
            </div>

            <div class="wm-options" id="wm-options">
                <div class="wm-type-radios" id="wm-type-radios">
                    <input type="radio" name="wmType" id="wm-tiled-text" value="tiledText" checked onchange="onWmTypeChange()">
                    <label for="wm-tiled-text">平铺文字</label>
                    <input type="radio" name="wmType" id="wm-tiled-pic" value="tiledPic" onchange="onWmTypeChange()">
                    <label for="wm-tiled-pic">平铺图片</label>
                    <input type="radio" name="wmType" id="wm-fixed-text" value="fixedText" onchange="onWmTypeChange()">
                    <label for="wm-fixed-text">固定文字</label>
                    <input type="radio" name="wmType" id="wm-fixed-pic" value="fixedPic" onchange="onWmTypeChange()">
                    <label for="wm-fixed-pic">固定图片</label>
                </div>

                <!-- 图片水印选择区 -->
                <div class="pic-select-section" id="pic-select-section" style="display:none;">
                    <h6>选择水印图片 <small class="text-muted">（从仓库中选择图片文件）</small></h6>
                    <div class="pic-file-list" id="pic-file-list"></div>
                    <div class="pic-url-display" id="pic-url-display" style="display:none;"></div>
                </div>

                <textarea class="json-editor" id="wm-editor"></textarea>
            </div>
        </div>

        <div class="cv-section">
            <button class="btn btn-success btn-block" onclick="submitConvert()" id="btn-submit">
                开始转换
            </button>
        </div>

        <div class="status-msg" id="status-msg"></div>
    </div>

    <div class="cv-right">
        <h6>预览区</h6>
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
    let selectedPicFile = null;
    let pollingTimer = null;

    const srcFormatMap = {
        'word': ['.doc', '.docx', '.wps', '.dot', '.wpt', '.dotx', '.docm', '.dotm'],
        'excel': ['.xls', '.xlsx', '.et', '.ett', '.xlt', '.xltx', '.xlsm', '.xltm', '.xlsb'],
        'ppt': ['.ppt', '.pptx', '.pps', '.ppsx', '.pptm', '.ppsm', '.potx', '.potm'],
        'pdf': ['.pdf']
    };
    const imageExts = ['.png', '.jpg', '.jpeg', '.gif', '.bmp', '.svg', '.webp', '.ico'];

    /**
     * 根据源文件类型决定可选的目标格式
     * Word  → PDF / OFD
     * Excel → PDF（不支持水印）
     * PPT   → PDF
     * PDF   → OFD
     */
    const convertRules = {
        'word': ['pdf', 'ofd'],
        'excel': ['pdf'],
        'ppt': ['pdf'],
        'pdf': ['ofd']
    };

    const wmDefaults = {
        'tiledText': {
            'tiledWatermark': {
                'line1': '机密文档',
                'line2': '',
                'line3': '',
                'withDate': true,
                'font': '黑体',
                'fontcolor': '#808080',
                'fontsize': '24',
                'isFontBold': false,
                'isFontItalic': false,
                'transparent': 50,
                'rotation': -45,
                'spacing': 100
            }
        },
        'tiledPic': {
            'tiledWatermark': {
                'picUrl': '',
                'picName': 'watermark.png',
                'transparent': 50,
                'rotation': 0,
                'spacing': 100
            }
        },
        'fixedText': {
            'msTextWatermark': {
                'text': '机密',
                'fontcolor': '#808080',
                'fontsize': '36',
                'font': '黑体',
                'rotation': -45,
                'transparent': 50,
                'position': 'CENTER'
            }
        },
        'fixedPic': {
            'msPicWatermark': {
                'picUrl': '',
                'picName': 'watermark.png',
                'isErosion': false,
                'rotation': 0,
                'picScale': 10,
                'widthInCm':5,
                'heightInCm':5,
                'position': 'CENTER'
            }
        }
    };

    function getFileExt(name) {
        const idx = name.lastIndexOf('.');
        return idx >= 0 ? name.substring(idx).toLowerCase() : '';
    }

    function isImageFile(name) {
        return imageExts.some(e => name.toLowerCase().endsWith(e));
    }

    function getSrcType(name) {
        const ext = getFileExt(name);
        if (srcFormatMap['word'].includes(ext)) return 'word';
        if (srcFormatMap['excel'].includes(ext)) return 'excel';
        if (srcFormatMap['ppt'].includes(ext)) return 'ppt';
        if (srcFormatMap['pdf'].includes(ext)) return 'pdf';
        return null;
    }

    function buildPicUrl(docId) {
        return DEMO_BASE_URL + '/' + docId + '/content';
    }

    // ========== 源文件列表 ==========
    function filterSrcFormat(format, btn) {
        $('#src-format-tabs .btn').removeClass('active');
        $(btn).addClass('active');
        renderSrcFileList(format);
    }

    function renderSrcFileList(format) {
        const container = $('#src-file-list');
        container.empty();
        let filtered = allFiles.filter(function (f) {
            if (isImageFile(f.name)) return false;
            const ext = getFileExt(f.name);
            return srcFormatMap['word'].includes(ext) || srcFormatMap['excel'].includes(ext) || srcFormatMap['ppt'].includes(ext) || srcFormatMap['pdf'].includes(ext);
        });
        if (format && format !== 'all') {
            const exts = srcFormatMap[format] || [];
            filtered = filtered.filter(function (f) { return exts.some(function (e) { return f.name.toLowerCase().endsWith(e); }); });
        }
        if (filtered.length === 0) {
            container.append('<div class="p-3 text-muted text-center">无匹配文件</div>');
            return;
        }
        filtered.forEach(function (file) {
            const ext = getFileExt(file.name);
            const selected = selectedFile && selectedFile.id === file.id ? ' selected' : '';
            container.append(
                '<div class="file-item' + selected + '" data-id="' + file.id + '" data-name="' + escapeAttr(file.name) +
                '" onclick="selectSrcFile(this)">' +
                '<span>' + escapeHtml(file.name) + '</span>' +
                '<span class="file-ext">' + ext + '</span></div>'
            );
        });
    }

    function selectSrcFile(el) {
        $('#src-file-list .file-item').removeClass('selected');
        $(el).addClass('selected');
        selectedFile = { id: $(el).data('id'), name: $(el).data('name') };
        updateTargetOptions();
    }

    // ========== 目标格式 ==========
    function updateTargetOptions() {
        if (!selectedFile) {
            $('#lbl-target-pdf, #lbl-target-ofd').addClass('disabled');
            $('input[name="targetFmt"]').prop('checked', false);
            $('#convert-hint').text('请先选择源文件');
            return;
        }
        const srcType = getSrcType(selectedFile.name);
        if (!srcType) {
            $('#lbl-target-pdf, #lbl-target-ofd').addClass('disabled');
            $('input[name="targetFmt"]').prop('checked', false);
            $('#convert-hint').text('不支持此文件格式的转换');
            return;
        }
        const targets = convertRules[srcType] || [];

        if (targets.includes('pdf')) {
            $('#lbl-target-pdf').removeClass('disabled');
        } else {
            $('#lbl-target-pdf').addClass('disabled');
            if ($('#target-pdf').is(':checked')) $('#target-pdf').prop('checked', false);
        }

        if (targets.includes('ofd')) {
            $('#lbl-target-ofd').removeClass('disabled');
        } else {
            $('#lbl-target-ofd').addClass('disabled');
            if ($('#target-ofd').is(':checked')) $('#target-ofd').prop('checked', false);
        }

        if (!$('input[name="targetFmt"]:checked').length && targets.length > 0) {
            $('#target-' + targets[0]).prop('checked', true);
        }

        const hintMap = {
            'word': 'Word 文件支持转换为 PDF 和 OFD',
            'excel': 'Excel 文件支持转换为 PDF（不支持水印）',
            'ppt': 'PPT 文件支持转换为 PDF（不支持水印）',
            'pdf': 'PDF 文件支持转换为 OFD'
        };
        $('#convert-hint').text(hintMap[srcType] || '');

        if (srcType === 'excel' || srcType === 'ppt') {
            $('#wm-enable').prop('checked', false);
            $('#wm-options').hide();
            $('#wm-enable').prop('disabled', true);
            var noWmLabel = srcType === 'ppt' ? '转换时添加水印（PPT 不支持）' : '转换时添加水印（Excel 不支持）';
            $('#wm-enable').closest('.form-check').find('label').text(noWmLabel);
        } else {
            $('#wm-enable').prop('disabled', false);
            $('#wm-enable').closest('.form-check').find('label').text('转换时添加水印');
        }
    }

    function onTargetChange() {}

    // ========== 水印开关 ==========
    function toggleWatermark() {
        if ($('#wm-enable').is(':checked')) {
            $('#wm-options').show();
            onWmTypeChange();
        } else {
            $('#wm-options').hide();
        }
    }

    function getWmType() {
        return $('input[name="wmType"]:checked').val();
    }

    function isPicWmType(type) {
        return type === 'tiledPic' || type === 'fixedPic';
    }

    function onWmTypeChange() {
        const type = getWmType();
        const isPic = isPicWmType(type);

        if (isPic) {
            $('#pic-select-section').show();
            renderPicFileList();
        } else {
            $('#pic-select-section').hide();
            selectedPicFile = null;
            $('#pic-url-display').hide();
        }

        const params = JSON.parse(JSON.stringify(wmDefaults[type] || {}));

        if (isPic && selectedPicFile) {
            const wmKey = type === 'tiledPic' ? 'tiledWatermark' : 'msPicWatermark';
            if (params[wmKey]) {
                params[wmKey].picUrl = buildPicUrl(selectedPicFile.id);
                params[wmKey].picName = selectedPicFile.name;
            }
        }
        $('#wm-editor').val(JSON.stringify(params, null, 2));
    }

    // ========== 水印图片选择 ==========
    function renderPicFileList() {
        const container = $('#pic-file-list');
        container.empty();
        const imageFiles = allFiles.filter(function (f) { return isImageFile(f.name); });
        if (imageFiles.length === 0) {
            container.append('<div class="p-2 text-muted text-center" style="font-size:12px">仓库中暂无图片文件，请先上传</div>');
            return;
        }
        imageFiles.forEach(function (file) {
            const ext = getFileExt(file.name);
            const selected = selectedPicFile && selectedPicFile.id === file.id ? ' selected' : '';
            container.append(
                '<div class="pic-file-item' + selected + '" data-id="' + file.id + '" data-name="' + escapeAttr(file.name) +
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

        updatePicUrlInWmEditor(picUrl, name);
    }

    function updatePicUrlInWmEditor(picUrl, picName) {
        try {
            const params = JSON.parse($('#wm-editor').val());
            const wmKey = params.tiledWatermark ? 'tiledWatermark' : (params.msPicWatermark ? 'msPicWatermark' : null);
            if (wmKey && params[wmKey]) {
                params[wmKey].picUrl = picUrl;
                params[wmKey].picName = picName;
                $('#wm-editor').val(JSON.stringify(params, null, 2));
            }
        } catch (e) {}
    }

    // ========== 状态 ==========
    function showStatus(msg, type) {
        $('#status-msg').removeClass('info success error show').addClass(type + ' show').text(msg);
    }

    // ========== 提交 ==========
    function submitConvert() {
        if (!selectedFile) { showStatus('请先选择源文件', 'error'); return; }
        var targetFmt = $('input[name="targetFmt"]:checked').val();
        if (!targetFmt) { showStatus('请选择目标格式', 'error'); return; }

        var baseName = selectedFile.name.replace(/\.[^/.]+$/, '');
        var targetFilename = baseName + '.' + targetFmt;

        var watermarkJson = null;
        if ($('#wm-enable').is(':checked')) {
            try {
                watermarkJson = $('#wm-editor').val().trim();
                JSON.parse(watermarkJson);
            } catch (e) {
                showStatus('水印参数 JSON 格式错误，请检查', 'error');
                return;
            }
            var wmType = getWmType();
            if (isPicWmType(wmType) && !selectedPicFile) {
                showStatus('图片水印模式，请先选择水印图片', 'error');
                return;
            }
        }

        showStatus('正在提交转换任务...', 'info');
        $('#btn-submit').prop('disabled', true);

        $.ajax({
            url: '/home/convert/submit',
            type: 'POST',
            contentType: 'application/json',
            data: JSON.stringify({
                docId: selectedFile.id,
                docName: selectedFile.name,
                targetFilename: targetFilename,
                watermarkJson: watermarkJson
            }),
            success: function (res) {
                if (res.success) {
                    showStatus('任务已提交 (taskId: ' + res.taskId + ')，正在处理中...', 'info');
                    startPolling(res.taskId);
                } else {
                    showStatus('提交失败: ' + (res.error || '未知错误'), 'error');
                    $('#btn-submit').prop('disabled', false);
                }
            },
            error: function (xhr) {
                showStatus('请求失败: ' + xhr.statusText, 'error');
                $('#btn-submit').prop('disabled', false);
            }
        });
    }

    // ========== 轮询 & 预览 ==========
    function startPolling(taskId) {
        if (pollingTimer) clearInterval(pollingTimer);
        var pollCount = 0;
        pollingTimer = setInterval(function () {
            pollCount++;
            $.get('/home/tasks/taskStatus', { taskId: taskId }, function (res) {
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
        $.get('/home/tasks/previewUrl', { taskId: taskId }, function (res) {
            if (res.success && res.docId) {
                $.get('/v2/context/driver-cb', { docId: res.docId, action: 'view', isInFrame: true }, function (url) {
                    showStatus('转换完成！预览已加载', 'success');
                    $('#preview-iframe').attr('src', url).show();
                    $('#preview-placeholder').hide();
                });
            } else {
                setTimeout(function () { pollForPreviewUrl(taskId, attempt + 1); }, 2000);
            }
        }).fail(function () {
            setTimeout(function () { pollForPreviewUrl(taskId, attempt + 1); }, 2000);
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

    $(function () {
        renderSrcFileList('all');
        updateTargetOptions();
    });
</script>
</body>
</html>
