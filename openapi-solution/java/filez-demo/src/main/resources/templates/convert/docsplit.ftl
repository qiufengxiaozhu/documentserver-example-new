<!DOCTYPE html>
<html lang="zh-CN">
<head>
    <meta charset="UTF-8">
    <title>文档拆分</title>
    <link href="/static/bootstrap.min.css" rel="stylesheet">
    <style>
        .ps-container { display: flex; height: calc(100vh - 20px); padding: 10px; gap: 15px; }
        .ps-left { width: 500px; min-width: 500px; overflow-y: auto; }
        .ps-right { flex: 1; display: flex; flex-direction: column; }
        .ps-section { margin-bottom: 15px; }
        .ps-section h6 { margin-bottom: 10px; font-weight: 600; }
        .format-tabs { display: flex; gap: 5px; margin-bottom: 10px; flex-wrap: wrap; }
        .format-tabs .btn { min-width: 60px; }
        .format-tabs .btn.active { font-weight: bold; }
        .file-list { max-height: 200px; overflow-y: auto; border: 1px solid #dee2e6; border-radius: 4px; }
        .file-item { padding: 6px 12px; cursor: pointer; font-size: 13px; display: flex; align-items: center; }
        .file-item:hover { background: #f8f9fa; }
        .file-item.selected { background: #e7f1ff; font-weight: 500; }
        .file-item .file-ext { color: #999; font-size: 11px; margin-left: auto; }
        .opt-group { display: flex; gap: 8px; flex-wrap: wrap; margin-top: 6px; }
        .opt-group label { cursor: pointer; padding: 6px 14px; border: 1px solid #ccc; border-radius: 4px; font-size: 13px; transition: all 0.2s; }
        .opt-group input[type="radio"]:checked + label { background: #007bff; color: #fff; border-color: #007bff; }
        .opt-group input[type="radio"] { display: none; }
        .param-input { margin-top: 10px; }
        .param-input label { font-size: 13px; font-weight: 500; margin-bottom: 4px; display: block; }
        .param-input input, .param-input select { width: 100%; padding: 6px 10px; border: 1px solid #ccc; border-radius: 4px; font-size: 13px; }
        .param-input .hint { font-size: 11px; color: #888; margin-top: 3px; }
        .selected-info { background: #f0f7ff; border: 1px solid #b8daff; border-radius: 4px; padding: 8px 12px; margin-top: 8px; font-size: 12px; }
        .preview-area { flex: 1; border: 1px solid #dee2e6; border-radius: 4px; background: #f8f9fa; display: flex; align-items: center; justify-content: center; min-height: 300px; }
        .preview-area iframe { width: 100%; height: 100%; border: none; }
        .preview-placeholder { color: #999; font-size: 14px; }
        .status-msg { padding: 10px; margin-bottom: 10px; border-radius: 4px; display: none; }
        .status-msg.show { display: block; }
        .status-msg.info { background: #cce5ff; color: #004085; }
        .status-msg.success { background: #d4edda; color: #155724; }
        .status-msg.error { background: #f8d7da; color: #721c24; }
        .mode-params { display: none; margin-top: 10px; padding: 12px; background: #f8f9fa; border: 1px solid #e9ecef; border-radius: 4px; }
        .mode-params.active { display: block; }
        .output-section { margin-top: 10px; padding: 10px; background: #fff9e6; border: 1px solid #ffeaa7; border-radius: 4px; }
        .format-badge { display: inline-block; padding: 2px 8px; border-radius: 3px; font-size: 11px; font-weight: 600; margin-left: 8px; }
        .format-badge.pdf { background: #ffeaea; color: #c0392b; }
        .format-badge.word { background: #e8f4fd; color: #2980b9; }
        .split-mode-section { transition: all 0.3s; }
    </style>
</head>
<body>
<div class="ps-container">
    <div class="ps-left">
        <!-- 步骤1：选择文件 -->
        <div class="ps-section">
            <h6>1. 选择文件</h6>
            <div class="format-tabs" id="format-tabs">
                <button class="btn btn-sm btn-outline-primary active" onclick="filterFormat('all', this)">全部</button>
                <button class="btn btn-sm btn-outline-primary" onclick="filterFormat('pdf', this)">PDF</button>
                <button class="btn btn-sm btn-outline-primary" onclick="filterFormat('word', this)">Word</button>
            </div>
            <div class="file-list" id="file-list"></div>
            <div class="selected-info" id="selected-info" style="display:none;"></div>
        </div>

        <!-- 步骤2：拆分模式（根据文件类型动态展示） -->
        <div class="ps-section split-mode-section" id="split-mode-section">
            <h6>2. 拆分模式 <span class="format-badge" id="mode-badge" style="display:none;"></span></h6>

            <!-- PDF 拆分模式 -->
            <div id="pdf-split-modes" style="display:none;">
                <div class="opt-group">
                    <input type="radio" name="splitType" id="st-pagerange" value="PAGERANGE" checked onchange="onSplitTypeChange()">
                    <label for="st-pagerange">按页码范围</label>
                    <input type="radio" name="splitType" id="st-fixedpages" value="FIXEDPAGES" onchange="onSplitTypeChange()">
                    <label for="st-fixedpages">按固定页数</label>
                    <input type="radio" name="splitType" id="st-filecount" value="FILECOUNT" onchange="onSplitTypeChange()">
                    <label for="st-filecount">按文件数均分</label>
                </div>

                <div class="mode-params active" id="params-PAGERANGE">
                    <div class="param-input">
                        <label>页码范围表达式</label>
                        <input type="text" id="input-ranges" placeholder="例如：1-5,10-15,20" />
                        <div class="hint">支持单页（5）、连续范围（1-10）、多段组合（1-5,10-15,20），页码从 1 开始</div>
                    </div>
                    <div class="output-section">
                        <div class="param-input">
                            <label>输出模式</label>
                            <select id="select-output">
                                <option value="array">多文件 ZIP（默认）</option>
                                <option value="singleFile">合并为单个 PDF</option>
                            </select>
                            <div class="hint">array：每个范围段生成独立 PDF 打包为 ZIP；singleFile：所有页面合并为一个 PDF</div>
                        </div>
                    </div>
                </div>

                <div class="mode-params" id="params-FIXEDPAGES">
                    <div class="param-input">
                        <label>每份页数</label>
                        <input type="number" id="input-fixedPages" min="1" value="10" />
                        <div class="hint">将 PDF 按固定页数切分，最后一份可能不足指定页数</div>
                    </div>
                </div>

                <div class="mode-params" id="params-FILECOUNT">
                    <div class="param-input">
                        <label>拆分份数</label>
                        <input type="number" id="input-fileCount" min="2" value="2" />
                        <div class="hint">将 PDF 均分为指定份数（≥2），余数分配到前若干份</div>
                    </div>
                </div>
            </div>

            <!-- Word 拆分模式 -->
            <div id="word-split-modes" style="display:none;">
                <div class="opt-group">
                    <input type="radio" name="splitType" id="st-wordheading" value="WORDHEADING" onchange="onSplitTypeChange()">
                    <label for="st-wordheading">按标题拆分</label>
                    <input type="radio" name="splitType" id="st-sectbreak" value="SECTBREAK" onchange="onSplitTypeChange()">
                    <label for="st-sectbreak">按分节符拆分</label>
                    <input type="radio" name="splitType" id="st-text" value="TEXT" onchange="onSplitTypeChange()">
                    <label for="st-text">按关键字拆分</label>
                </div>

                <div class="mode-params" id="params-WORDHEADING">
                    <div class="param-input">
                        <div class="hint">根据文档中的标题（各级标题均识别）自动拆分为多个子文档</div>
                    </div>
                </div>

                <div class="mode-params" id="params-SECTBREAK">
                    <div class="param-input">
                        <div class="hint">根据文档中的分节符位置拆分为多个子文档</div>
                    </div>
                </div>

                <div class="mode-params" id="params-TEXT">
                    <div class="param-input">
                        <label>关键字</label>
                        <input type="text" id="input-keyword" placeholder="输入用于拆分的关键字" />
                        <div class="hint">根据标题中包含的关键字拆分文档（不区分标题等级）</div>
                    </div>
                </div>
            </div>

            <!-- 未选择文件时的提示 -->
            <div id="no-file-hint" class="text-muted" style="font-size:13px; margin-top:8px;">
                请先选择文件，拆分模式将根据文件格式自动展示
            </div>
        </div>

        <div class="ps-section">
            <button class="btn btn-success btn-block" onclick="submitDocSplit()" id="btn-submit">
                开始拆分
            </button>
        </div>

        <div class="status-msg" id="status-msg"></div>
    </div>

    <div class="ps-right">
        <h6>预览区</h6>
        <div class="preview-area" id="preview-area">
            <span class="preview-placeholder" id="preview-placeholder">提交拆分任务后，结果将在此处预览</span>
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
    let currentFileType = null; // 'pdf' | 'word' | null
    let pollingTimer = null;

    const wordExts = ['.doc', '.docx', '.wps'];
    const pdfExts = ['.pdf'];
    const supportedExts = pdfExts.concat(wordExts);

    function getFileExt(name) {
        const idx = name.lastIndexOf('.');
        return idx >= 0 ? name.substring(idx).toLowerCase() : '';
    }

    function isSupportedFile(name) {
        var ext = getFileExt(name);
        return supportedExts.indexOf(ext) >= 0;
    }

    function isPdfFile(name) {
        return getFileExt(name) === '.pdf';
    }

    function isWordFile(name) {
        return wordExts.indexOf(getFileExt(name)) >= 0;
    }

    function getFileTypeLabel(name) {
        if (isPdfFile(name)) return 'PDF';
        if (isWordFile(name)) return 'Word';
        return '';
    }

    // ========== 文件列表 ==========
    function filterFormat(format, btn) {
        $('#format-tabs .btn').removeClass('active');
        $(btn).addClass('active');
        renderFileList(format);
    }

    function renderFileList(format) {
        var container = $('#file-list');
        container.empty();
        var filtered = allFiles.filter(function(f) { return isSupportedFile(f.name); });
        if (format && format !== 'all') {
            if (format === 'pdf') {
                filtered = filtered.filter(function(f) { return isPdfFile(f.name); });
            } else if (format === 'word') {
                filtered = filtered.filter(function(f) { return isWordFile(f.name); });
            }
        }
        if (filtered.length === 0) {
            container.append('<div class="p-3 text-muted text-center">无匹配文件，请先上传 PDF 或 Word 文件到本地仓库</div>');
            return;
        }
        filtered.forEach(function(file) {
            var ext = getFileExt(file.name).toUpperCase().replace('.', '');
            var sel = selectedFile && selectedFile.id === file.id ? ' selected' : '';
            container.append(
                '<div class="file-item' + sel + '" data-id="' + file.id + '" data-name="' + escapeAttr(file.name) +
                '" onclick="selectFile(this)">' +
                '<span>' + escapeHtml(file.name) + '</span>' +
                '<span class="file-ext">' + ext + '</span></div>'
            );
        });
    }

    function selectFile(el) {
        $('#file-list .file-item').removeClass('selected');
        $(el).addClass('selected');
        selectedFile = { id: $(el).data('id'), name: $(el).data('name') };
        $('#selected-info').show().html('<strong>已选择：</strong>' + escapeHtml(selectedFile.name));

        updateSplitModes();
    }

    // ========== 根据文件类型切换拆分模式 ==========
    function updateSplitModes() {
        if (!selectedFile) return;

        var badge = $('#mode-badge');
        $('#no-file-hint').hide();

        if (isPdfFile(selectedFile.name)) {
            currentFileType = 'pdf';
            $('#pdf-split-modes').show();
            $('#word-split-modes').hide();
            badge.text('PDF').attr('class', 'format-badge pdf').show();
            // 默认选中 PAGERANGE
            $('#st-pagerange').prop('checked', true);
            onSplitTypeChange();
        } else if (isWordFile(selectedFile.name)) {
            currentFileType = 'word';
            $('#pdf-split-modes').hide();
            $('#word-split-modes').show();
            badge.text('Word').attr('class', 'format-badge word').show();
            // 默认选中 WORDHEADING
            $('#st-wordheading').prop('checked', true);
            onSplitTypeChange();
        }
    }

    // ========== 拆分模式切换 ==========
    function onSplitTypeChange() {
        var type = $('input[name="splitType"]:checked').val();
        $('.mode-params').removeClass('active');
        $('#params-' + type).addClass('active');
    }

    // ========== 状态 ==========
    function showStatus(msg, type) {
        $('#status-msg').removeClass('info success error show').addClass(type + ' show').text(msg);
    }

    // ========== 参数校验 ==========
    function validateParams(type) {
        if (type === 'PAGERANGE') {
            var ranges = $('#input-ranges').val().trim();
            if (!ranges) { showStatus('请输入页码范围表达式', 'error'); return null; }
            if (!/^(\d+(-\d+)?)(,\d+(-\d+)?)*$/.test(ranges)) {
                showStatus('页码范围格式不正确，示例：1-5,10-15,20', 'error');
                return null;
            }
            return { ranges: ranges, output: $('#select-output').val() };
        }
        if (type === 'FIXEDPAGES') {
            var fp = parseInt($('#input-fixedPages').val());
            if (isNaN(fp) || fp < 1) { showStatus('每份页数必须 ≥ 1', 'error'); return null; }
            return { fixedPages: String(fp) };
        }
        if (type === 'FILECOUNT') {
            var fc = parseInt($('#input-fileCount').val());
            if (isNaN(fc) || fc < 2) { showStatus('拆分份数必须 ≥ 2', 'error'); return null; }
            return { fileCount: String(fc) };
        }
        if (type === 'WORDHEADING') {
            return {};
        }
        if (type === 'SECTBREAK') {
            return {};
        }
        if (type === 'TEXT') {
            var keyword = $('#input-keyword').val().trim();
            if (!keyword) { showStatus('请输入拆分关键字', 'error'); return null; }
            return { keyword: keyword };
        }
        return null;
    }

    // ========== 提交 ==========
    function submitDocSplit() {
        if (!selectedFile) { showStatus('请先选择文件', 'error'); return; }

        var type = $('input[name="splitType"]:checked').val();
        if (!type) { showStatus('请选择拆分模式', 'error'); return; }

        var params = validateParams(type);
        if (params === null) return;

        var data = {
            docId: selectedFile.id,
            docName: selectedFile.name,
            type: type
        };
        if (params.ranges) data.ranges = params.ranges;
        if (params.fixedPages) data.fixedPages = params.fixedPages;
        if (params.fileCount) data.fileCount = params.fileCount;
        if (params.output) data.output = params.output;
        if (params.keyword) data.keyword = params.keyword;

        var label = currentFileType === 'pdf' ? 'PDF' : 'Word';
        showStatus('正在提交 ' + label + ' 拆分任务...', 'info');
        $('#btn-submit').prop('disabled', true);

        $.ajax({
            url: '/home/docSplit/submit',
            type: 'POST',
            contentType: 'application/json',
            data: JSON.stringify(data),
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
                    showStatus('拆分完成！', 'success');
                    $('#btn-submit').prop('disabled', false);
                    showPreview(taskId, res.contentId);
                } else if (status === 'FAIL' || res.code === 'InvalidTaskId') {
                    clearInterval(pollingTimer);
                    pollingTimer = null;
                    showStatus('拆分失败: ' + (res.msg || res.code || '未知错误'), 'error');
                    $('#btn-submit').prop('disabled', false);
                } else {
                    showStatus('拆分中... (' + pollCount * 2 + 's)', 'info');
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
                    showStatus('拆分完成！预览已加载', 'success');
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
        renderFileList('all');
    });
</script>
</body>
</html>
