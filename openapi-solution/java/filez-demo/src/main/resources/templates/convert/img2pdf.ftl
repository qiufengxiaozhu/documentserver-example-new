<!DOCTYPE html>
<html lang="zh-CN">
<head>
    <meta charset="UTF-8">
    <title>图片转 PDF</title>
    <link href="/static/bootstrap.min.css" rel="stylesheet">
    <style>
        .i2p-container { display: flex; height: calc(100vh - 20px); padding: 10px; gap: 15px; }
        .i2p-left { width: 480px; min-width: 480px; overflow-y: auto; }
        .i2p-right { flex: 1; display: flex; flex-direction: column; }
        .i2p-section { margin-bottom: 15px; }
        .i2p-section h6 { margin-bottom: 10px; font-weight: 600; }
        .format-tabs { display: flex; gap: 5px; margin-bottom: 10px; flex-wrap: wrap; }
        .format-tabs .btn { min-width: 60px; }
        .format-tabs .btn.active { font-weight: bold; }
        .file-list { max-height: 200px; overflow-y: auto; border: 1px solid #dee2e6; border-radius: 4px; }
        .file-item { padding: 6px 12px; cursor: pointer; font-size: 13px; display: flex; align-items: center; }
        .file-item:hover { background: #f8f9fa; }
        .file-item.selected { background: #e7f1ff; font-weight: 500; }
        .file-item .file-ext { color: #999; font-size: 11px; margin-left: auto; }
        .file-item .file-size { color: #aaa; font-size: 11px; margin-left: 8px; }
        .opt-group { display: flex; gap: 8px; flex-wrap: wrap; margin-top: 6px; }
        .opt-group label { cursor: pointer; padding: 6px 14px; border: 1px solid #ccc; border-radius: 4px; font-size: 13px; transition: all 0.2s; }
        .opt-group input:checked + label { background: #007bff; color: #fff; border-color: #007bff; }
        .opt-group input { display: none; }
        .opt-group label.disabled { opacity: 0.4; pointer-events: none; }
        .opt-hint { font-size: 11px; color: #888; margin-top: 4px; }
        .preview-area { flex: 1; border: 1px solid #dee2e6; border-radius: 4px; background: #f8f9fa; display: flex; align-items: center; justify-content: center; min-height: 300px; }
        .preview-area iframe { width: 100%; height: 100%; border: none; }
        .preview-placeholder { color: #999; font-size: 14px; }
        .status-msg { padding: 10px; margin-bottom: 10px; border-radius: 4px; display: none; }
        .status-msg.show { display: block; }
        .status-msg.info { background: #cce5ff; color: #004085; }
        .status-msg.success { background: #d4edda; color: #155724; }
        .status-msg.error { background: #f8d7da; color: #721c24; }
        .img-preview-thumb { max-width: 100%; max-height: 120px; border-radius: 4px; margin-top: 8px; border: 1px solid #dee2e6; }
        .selected-info { background: #f0f7ff; border: 1px solid #b8daff; border-radius: 4px; padding: 8px 12px; margin-top: 8px; font-size: 12px; }
    </style>
</head>
<body>
<div class="i2p-container">
    <div class="i2p-left">
        <!-- 步骤1：选择图片文件 -->
        <div class="i2p-section">
            <h6>1. 选择图片文件</h6>
            <div class="format-tabs" id="img-format-tabs">
                <button class="btn btn-sm btn-outline-primary active" onclick="filterImgFormat('all', this)">全部</button>
                <button class="btn btn-sm btn-outline-primary" onclick="filterImgFormat('jpg', this)">JPG</button>
                <button class="btn btn-sm btn-outline-primary" onclick="filterImgFormat('png', this)">PNG</button>
                <button class="btn btn-sm btn-outline-primary" onclick="filterImgFormat('bmp', this)">BMP</button>
                <button class="btn btn-sm btn-outline-primary" onclick="filterImgFormat('tiff', this)">TIFF</button>
                <button class="btn btn-sm btn-outline-primary" onclick="filterImgFormat('gif', this)">GIF</button>
            </div>
            <div class="file-list" id="img-file-list"></div>
            <div class="selected-info" id="selected-info" style="display:none;"></div>
            <img id="img-thumb" class="img-preview-thumb" style="display:none;" alt="preview"/>
        </div>

        <!-- 步骤2：页面尺寸 -->
        <div class="i2p-section">
            <h6>2. 页面尺寸</h6>
            <div class="opt-group" id="pageSize-group">
                <input type="radio" name="pageSize" id="ps-fit" value="FIT_IMAGE" checked onchange="onPageSizeChange()">
                <label for="ps-fit">适应图片</label>
                <input type="radio" name="pageSize" id="ps-a4" value="A4" onchange="onPageSizeChange()">
                <label for="ps-a4">A4</label>
                <input type="radio" name="pageSize" id="ps-a3" value="A3" onchange="onPageSizeChange()">
                <label for="ps-a3">A3</label>
            </div>
            <div class="opt-hint">适应图片模式下，PDF 页面大小等于图片尺寸，忽略方向和边距设置</div>
        </div>

        <!-- 步骤3：页面方向 -->
        <div class="i2p-section" id="orientation-section">
            <h6>3. 页面方向</h6>
            <div class="opt-group" id="orientation-group">
                <input type="radio" name="orientation" id="ori-auto" value="auto" checked>
                <label for="ori-auto" id="lbl-ori-auto">自动</label>
                <input type="radio" name="orientation" id="ori-portrait" value="portrait">
                <label for="ori-portrait" id="lbl-ori-portrait">纵向</label>
                <input type="radio" name="orientation" id="ori-landscape" value="landscape">
                <label for="ori-landscape" id="lbl-ori-landscape">横向</label>
            </div>
            <div class="opt-hint">自动模式根据图片宽高比判断方向</div>
        </div>

        <!-- 步骤4：页边距 -->
        <div class="i2p-section" id="margin-section">
            <h6>4. 页边距</h6>
            <div class="opt-group" id="margin-group">
                <input type="radio" name="margin" id="mg-none" value="none" checked>
                <label for="mg-none" id="lbl-mg-none">无边距</label>
                <input type="radio" name="margin" id="mg-narrow" value="narrow">
                <label for="mg-narrow" id="lbl-mg-narrow">窄边距</label>
                <input type="radio" name="margin" id="mg-wide" value="wide">
                <label for="mg-wide" id="lbl-mg-wide">宽边距</label>
            </div>
        </div>

        <div class="i2p-section">
            <button class="btn btn-success btn-block" onclick="submitImg2Pdf()" id="btn-submit">
                开始转换
            </button>
        </div>

        <div class="status-msg" id="status-msg"></div>
    </div>

    <div class="i2p-right">
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
    let pollingTimer = null;

    const imgFormatMap = {
        'jpg': ['.jpg', '.jpeg'],
        'png': ['.png'],
        'bmp': ['.bmp'],
        'tiff': ['.tif', '.tiff'],
        'gif': ['.gif']
    };
    const allImgExts = [].concat(...Object.values(imgFormatMap));

    function getFileExt(name) {
        const idx = name.lastIndexOf('.');
        return idx >= 0 ? name.substring(idx).toLowerCase() : '';
    }

    function isImageFile(name) {
        return allImgExts.some(function(e) { return name.toLowerCase().endsWith(e); });
    }

    // ========== 图片文件列表 ==========
    function filterImgFormat(format, btn) {
        $('#img-format-tabs .btn').removeClass('active');
        $(btn).addClass('active');
        renderImgFileList(format);
    }

    function renderImgFileList(format) {
        var container = $('#img-file-list');
        container.empty();
        var filtered = allFiles.filter(function(f) { return isImageFile(f.name); });
        if (format && format !== 'all') {
            var exts = imgFormatMap[format] || [];
            filtered = filtered.filter(function(f) {
                return exts.some(function(e) { return f.name.toLowerCase().endsWith(e); });
            });
        }
        if (filtered.length === 0) {
            container.append('<div class="p-3 text-muted text-center">无匹配图片文件，请先上传图片到本地仓库</div>');
            return;
        }
        filtered.forEach(function(file) {
            var ext = getFileExt(file.name);
            var sel = selectedFile && selectedFile.id === file.id ? ' selected' : '';
            container.append(
                '<div class="file-item' + sel + '" data-id="' + file.id + '" data-name="' + escapeAttr(file.name) +
                '" onclick="selectImgFile(this)">' +
                '<span>' + escapeHtml(file.name) + '</span>' +
                '<span class="file-ext">' + ext.toUpperCase() + '</span></div>'
            );
        });
    }

    function selectImgFile(el) {
        $('#img-file-list .file-item').removeClass('selected');
        $(el).addClass('selected');
        selectedFile = { id: $(el).data('id'), name: $(el).data('name') };

        var infoHtml = '<strong>已选择：</strong>' + escapeHtml(selectedFile.name);
        $('#selected-info').show().html(infoHtml);

        var thumbUrl = DEMO_BASE_URL + '/' + selectedFile.id + '/content';
        $('#img-thumb').attr('src', thumbUrl).show();
    }

    // ========== 页面尺寸联动 ==========
    function onPageSizeChange() {
        var ps = $('input[name="pageSize"]:checked').val();
        if (ps === 'FIT_IMAGE') {
            $('#orientation-section, #margin-section').css('opacity', '0.4').css('pointer-events', 'none');
        } else {
            $('#orientation-section, #margin-section').css('opacity', '1').css('pointer-events', 'auto');
        }
    }

    // ========== 状态 ==========
    function showStatus(msg, type) {
        $('#status-msg').removeClass('info success error show').addClass(type + ' show').text(msg);
    }

    // ========== 提交 ==========
    function submitImg2Pdf() {
        if (!selectedFile) { showStatus('请先选择图片文件', 'error'); return; }

        var pageSize = $('input[name="pageSize"]:checked').val();
        var orientation = $('input[name="orientation"]:checked').val();
        var margin = $('input[name="margin"]:checked').val();

        showStatus('正在提交图片转 PDF 任务...', 'info');
        $('#btn-submit').prop('disabled', true);

        $.ajax({
            url: '/home/imageToPdf/submit',
            type: 'POST',
            contentType: 'application/json',
            data: JSON.stringify({
                docId: selectedFile.id,
                docName: selectedFile.name,
                pageSize: pageSize,
                orientation: orientation,
                margin: margin
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

    $(function() {
        renderImgFileList('all');
        onPageSizeChange();
    });
</script>
</body>
</html>
