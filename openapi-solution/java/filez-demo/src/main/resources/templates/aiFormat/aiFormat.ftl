<!DOCTYPE html>
<html lang="zh-CN">
<head>
    <meta charset="UTF-8">
    <title>智能排版</title>
    <link href="/static/bootstrap.min.css" rel="stylesheet">
    <style>
        body { background: #f8f9fa; margin: 0; }
        .ts-top {
            padding: 16px 24px;
            background: #fff;
            border-bottom: 1px solid #dee2e6;
        }
        .ts-top .ts-title { font-size: 18px; font-weight: 600; margin-bottom: 4px; }
        .ts-top .ts-desc { color: #666; font-size: 13px; margin-bottom: 12px; }

        .ts-select-row { display: flex; gap: 12px; align-items: center; flex-wrap: wrap; }
        .ts-select-row label { font-size: 13px; font-weight: 500; margin: 0; white-space: nowrap; }
        .ts-select-row select { max-width: 220px; font-size: 13px; }
        .ts-select-row .ts-or { color: #999; font-size: 12px; }
        .ts-upload-btn {
            white-space: nowrap; cursor: pointer; margin: 0;
            padding: 4px 12px; font-size: 12px;
            border: 1px solid #ced4da; border-radius: 4px;
            background: #fff; color: #495057;
        }
        .ts-upload-btn:hover { border-color: #86b7fe; }
        .ts-upload-btn input[type="file"] { display: none; }
        .ts-info { font-size: 12px; color: #28a745; margin-left: 4px; }
        .ts-info.error { color: #dc3545; }

        .ts-main { display: flex; height: calc(100vh - 160px); }
        .ts-doc-area { flex: 1; min-width: 0; position: relative; background: #e9ecef; }
        .ts-doc-area iframe { width: 100%; height: 100%; border: 0; }
        .ts-doc-placeholder {
            display: flex; align-items: center; justify-content: center;
            height: 100%; color: #999; font-size: 15px;
        }

        .ts-sidebar {
            width: 340px; min-width: 340px;
            background: #fff; border-left: 1px solid #dee2e6;
            overflow-y: auto; padding: 16px;
        }
        .ts-sidebar h6 { font-size: 14px; font-weight: 600; margin-bottom: 12px; }
        .ts-sidebar .form-group { margin-bottom: 10px; }
        .ts-sidebar label { font-size: 12px; font-weight: 500; margin-bottom: 2px; display: block; color: #495057; }
        .ts-sidebar input, .ts-sidebar select { font-size: 12px; }
        .ts-sidebar .btn-row { display: flex; gap: 8px; margin-top: 14px; flex-wrap: wrap; align-items: center; }
        .ts-sidebar .ts-status { font-size: 12px; color: #666; }
        .ts-sidebar .ts-result { margin-top: 12px; }
        .ts-sidebar .ts-result .alert { font-size: 12px; padding: 8px 12px; margin-bottom: 0; }
    </style>
</head>
<body>

<!-- 顶部：文件选择 + 确认排版 -->
<div class="ts-top">
    <div class="ts-title">智能排版</div>
    <p class="ts-desc">选择待排版文档和模板文件，点击确认排版后，文档将在左侧打开编辑，右侧可调整参数并下载模板。<span style="color:#999;">（仅支持 5MB 以内的 .doc / .docx 文件）</span></p>
    <div class="ts-select-row">
        <label>待排版文档</label>
        <select id="ts-source-select" class="form-control form-control-sm" style="max-width:200px;">
            <option value="">-- 选择 --</option>
            <#list files as file>
                <option value="${file.id}" data-name="${file.name}">${file.name}</option>
            </#list>
        </select>
        <label class="ts-upload-btn">上传<input type="file" id="ts-source-upload" accept=".doc,.docx"></label>
        <span class="ts-info" id="ts-source-info"></span>

        <span class="ts-or">|</span>

        <label>模板文件</label>
        <select id="ts-template-select" class="form-control form-control-sm" style="max-width:200px;">
            <option value="">-- 选择 --</option>
            <#list files as file>
                <option value="${file.id}" data-name="${file.name}">${file.name}</option>
            </#list>
        </select>
        <label class="ts-upload-btn">上传<input type="file" id="ts-template-upload" accept=".doc,.docx"></label>
        <span class="ts-info" id="ts-template-info"></span>

        <span class="ts-or">|</span>

        <button id="ts-confirm-btn" class="btn btn-primary btn-sm" disabled>确认排版</button>
        <span class="ts-status" id="ts-status" style="font-size:12px;color:#666;"></span>
    </div>
</div>

<!-- 主体：左侧文档编辑 + 右侧参数表单 -->
<div class="ts-main">
    <div class="ts-doc-area" id="ts-doc-area">
        <div class="ts-doc-placeholder" id="ts-doc-placeholder">点击「确认排版」后，文档将在此处打开</div>
    </div>

    <div class="ts-sidebar" id="ts-sidebar" style="display:none;">
        <h6>接口参数（POST /api/local/templates）</h6>
        <div class="form-group">
            <label>docId <small class="text-muted">模板文件 ID</small></label>
            <input type="text" class="form-control form-control-sm" id="param-docId">
        </div>
        <div class="form-group">
            <label>repoId <small class="text-muted">仓库 ID</small></label>
            <input type="text" class="form-control form-control-sm" id="param-repoId">
        </div>
        <div class="form-group">
            <label>clientId <small class="text-muted">客户端 ID</small></label>
            <input type="text" class="form-control form-control-sm" id="param-clientId">
        </div>
        <div class="form-group">
            <label>filename <small class="text-muted">模板文件名</small></label>
            <input type="text" class="form-control form-control-sm" id="param-filename">
        </div>
        <div class="form-group">
            <label>templateType</label>
            <select class="form-control form-control-sm" id="param-templateType">
                <option value="general" selected>general</option>
                <option value="official">official</option>
            </select>
        </div>
        <div class="form-group">
            <label>token <small class="text-muted">鉴权令牌</small></label>
            <input type="text" class="form-control form-control-sm" id="param-token">
        </div>
        <div class="btn-row">
            <button id="ts-download-btn" class="btn btn-success btn-sm">确认下载模板</button>
            <span class="ts-status" id="ts-download-status"></span>
        </div>
        <div class="ts-result" id="ts-result"></div>

        <hr style="margin: 20px 0 16px;">

        <h6>解析模板（PUT /api/local/templates/:template_id）</h6>
        <div class="form-group">
            <label>templateId <small class="text-muted">下载后的模板 docId</small></label>
            <input type="text" class="form-control form-control-sm" id="convert-templateId">
        </div>
        <div class="form-group">
            <label>templateType</label>
            <select class="form-control form-control-sm" id="convert-templateType">
                <option value="general" selected>general</option>
                <option value="official">official</option>
            </select>
        </div>
        <div class="form-group">
            <label>token <small class="text-muted">鉴权令牌</small></label>
            <input type="text" class="form-control form-control-sm" id="convert-token">
        </div>
        <div class="btn-row">
            <button id="ts-convert-btn" class="btn btn-info btn-sm">解析模板</button>
            <span class="ts-status" id="ts-convert-status"></span>
        </div>
        <div class="ts-result" id="ts-convert-result"></div>
    </div>
</div>

<script src="/static/jquery-3.7.0.min.js"></script>
<script src="/static/sdk.js"></script>
<script>
    var state = { sourceDocId: '', sourceDocName: '', docId: '', templateDocName: '' };

    var sourceSelect = document.getElementById('ts-source-select');
    var templateSelect = document.getElementById('ts-template-select');
    var confirmBtn = document.getElementById('ts-confirm-btn');
    var statusEl = document.getElementById('ts-status');

    // 默认选中
    if (sourceSelect.options.length >= 3) {
        sourceSelect.selectedIndex = 1;
        state.sourceDocId = sourceSelect.value;
        state.sourceDocName = sourceSelect.options[1].text;
    }
    if (templateSelect.options.length >= 3) {
        templateSelect.selectedIndex = Math.min(2, templateSelect.options.length - 1);
        state.docId = templateSelect.value;
        state.templateDocName = templateSelect.options[templateSelect.selectedIndex].text;
    }
    updateConfirmBtn();

    sourceSelect.addEventListener('change', function () {
        state.sourceDocId = this.value;
        state.sourceDocName = this.options[this.selectedIndex].text || '';
        document.getElementById('ts-source-info').textContent = '';
        updateConfirmBtn();
    });
    templateSelect.addEventListener('change', function () {
        state.docId = this.value;
        state.templateDocName = this.options[this.selectedIndex].text || '';
        document.getElementById('ts-template-info').textContent = '';
        updateConfirmBtn();
    });

    document.getElementById('ts-source-upload').addEventListener('change', function (e) { handleUpload(e.target.files[0], 'source'); });
    document.getElementById('ts-template-upload').addEventListener('change', function (e) { handleUpload(e.target.files[0], 'template'); });

    var MAX_FILE_SIZE = 5 * 1024 * 1024;
    var ALLOWED_EXTENSIONS = ['.doc', '.docx'];

    function validateFile(file) {
        var name = file.name.toLowerCase();
        var extOk = ALLOWED_EXTENSIONS.some(function (ext) { return name.endsWith(ext); });
        if (!extOk) return '仅支持 .doc / .docx 格式文件';
        if (file.size > MAX_FILE_SIZE) return '文件大小不能超过 5MB（当前 ' + (file.size / 1024 / 1024).toFixed(1) + 'MB）';
        return '';
    }

    function handleUpload(file, role) {
        if (!file) return;
        var infoEl = document.getElementById(role === 'source' ? 'ts-source-info' : 'ts-template-info');
        var inputEl = document.getElementById(role === 'source' ? 'ts-source-upload' : 'ts-template-upload');

        var errMsg = validateFile(file);
        if (errMsg) {
            infoEl.className = 'ts-info error';
            infoEl.textContent = errMsg;
            inputEl.value = '';
            return;
        }

        infoEl.className = 'ts-info';
        infoEl.textContent = '上传中...';
        var fd = new FormData();
        fd.append('file', file, file.name);
        fd.append('drive', 'local');
        $.ajax({
            url: '/v2/context/file/upload', type: 'POST', data: fd, processData: false, contentType: false,
            success: function (data) {
                if (!data || !data.id) return;
                if (role === 'source') { state.sourceDocId = data.id; state.sourceDocName = data.name; }
                else { state.docId = data.id; state.templateDocName = data.name; }
                infoEl.className = 'ts-info';
                infoEl.textContent = '已上传: ' + data.name;
                updateConfirmBtn();
            },
            error: function () { infoEl.className = 'ts-info error'; infoEl.textContent = '上传失败'; }
        });
    }

    function updateConfirmBtn() {
        confirmBtn.disabled = !(state.sourceDocId && state.docId);
    }

    // ── 确认排版：在页面内 iframe 打开文档 + 显示右侧参数面板 ──
    confirmBtn.addEventListener('click', async function () {
        if (!state.sourceDocId || !state.docId) return;
        if (state.sourceDocId === state.docId) {
            showResult('ts-result', 'error', '待排版文档和模板文件不能是同一个');
            return;
        }

        confirmBtn.disabled = true;
        statusEl.textContent = '正在获取编辑 URL...';

        try {
            var cbResp = await fetch('/v2/context/driver-cb?docId=' + state.sourceDocId + '&action=edit&isInFrame=true');
            if (!cbResp.ok) throw new Error('获取编辑 URL 失败: ' + cbResp.statusText);
            var editUrl = await cbResp.text();

            statusEl.textContent = '正在打开文档...';

            var luoshuInfo = extractLuoshuInfo(editUrl);
            document.getElementById('param-docId').value = state.docId;
            document.getElementById('param-repoId').value = luoshuInfo.repoId || '';
            document.getElementById('param-clientId').value = luoshuInfo.clientId || '';
            document.getElementById('param-filename').value = state.templateDocName || 'template.docx';
            document.getElementById('param-token').value = luoshuInfo.token || '';

            document.getElementById('ts-sidebar').style.display = '';

            await openDocAndFormat(editUrl, luoshuInfo);
        } catch (err) {
            console.error('confirm error:', err);
            statusEl.textContent = '';
            showResult('ts-result', 'error', err.message);
        } finally {
            confirmBtn.disabled = false;
            updateConfirmBtn();
        }
    });

    async function openDocAndFormat(editUrl, luoshuInfo) {
        var docArea = document.getElementById('ts-doc-area');
        var placeholder = document.getElementById('ts-doc-placeholder');
        if (placeholder) placeholder.style.display = 'none';

        var oldFrame = document.getElementById('ai-format-frame');
        if (oldFrame) oldFrame.remove();

        var targetUrl = editUrl;
        if (editUrl.indexOf('/home/iframe?url=') !== -1) {
            var urlObj = new URL(editUrl, window.location.origin);
            var urlParam = urlObj.searchParams.get('url');
            if (urlParam) targetUrl = decodeURIComponent(urlParam);
        }

        var frame = document.createElement('iframe');
        frame.name = 'ai-format-frame';
        frame.id = 'ai-format-frame';
        frame.title = 'ai-format-frame';
        frame.setAttribute('allowfullscreen', 'true');
        frame.setAttribute('sandbox', 'allow-scripts allow-same-origin allow-forms allow-popups allow-top-navigation allow-popups-to-escape-sandbox');
        docArea.appendChild(frame);

        await new Promise(function (resolve) {
            frame.addEventListener('load', resolve, { once: true });
            frame.src = targetUrl;
        });

        statusEl.textContent = '文档已打开，正在连接智能排版SDK...';

        var Application = await ZOfficeSDK.connect('#ts-doc-area', true);
        await Application.ready();
        statusEl.textContent = '正在执行智能排版...';
        console.log('SDK已连接，开始智能排版，repoid：' + luoshuInfo.repoId + ', docId：' + state.docId + ', templateType：general');
        await Application.ActiveDocument.AIFormat.format({
            docId: state.docId,
            templateType: 'general'
        });
        statusEl.textContent = '智能排版完成';
    }


    // ── 确认下载模板 ──
    document.getElementById('ts-download-btn').addEventListener('click', async function () {
        var btn = this;
        var downloadStatus = document.getElementById('ts-download-status');
        btn.disabled = true;
        downloadStatus.textContent = '下载中...';
        document.getElementById('ts-result').innerHTML = '';

        var params = {
            docId: document.getElementById('param-docId').value,
            repoId: document.getElementById('param-repoId').value,
            clientId: document.getElementById('param-clientId').value,
            filename: document.getElementById('param-filename').value,
            templateType: document.getElementById('param-templateType').value,
            token: document.getElementById('param-token').value
        };

        try {
            var queryParts = [];
            for (var key in params) {
                if (params[key]) queryParts.push(encodeURIComponent(key) + '=' + encodeURIComponent(params[key]));
            }

            var resp = await fetch('/home/proxyTemplateUpload', {
                method: 'POST',
                headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
                body: queryParts.join('&')
            });

            var text = await resp.text();
            var result;
            try { result = JSON.parse(text); } catch (e) { throw new Error('响应解析失败: ' + text); }
            if (result.code) throw new Error('下载失败: ' + result.code);

            var templateId = result.data && result.data.docId;
            downloadStatus.textContent = '';
            showResult('ts-result', 'success',
                '<b>模板下载完成</b><br>templateId: <code>' + (templateId || 'N/A') + '</code><br>' +
                'filename: <code>' + ((result.data && result.data.filename) || 'N/A') + '</code>'
            );

            // 自动填充解析模板表单
            if (templateId) {
                document.getElementById('convert-templateId').value = templateId;
                document.getElementById('convert-token').value = document.getElementById('param-token').value;
            }
        } catch (err) {
            console.error('download error:', err);
            downloadStatus.textContent = '';
            showResult('ts-result', 'error', err.message);
        } finally {
            btn.disabled = false;
        }
    });

    // ── 解析模板（PUT /api/local/templates/:template_id） ──
    document.getElementById('ts-convert-btn').addEventListener('click', async function () {
        var btn = this;
        var convertStatus = document.getElementById('ts-convert-status');
        btn.disabled = true;
        convertStatus.textContent = '解析中...';
        document.getElementById('ts-convert-result').innerHTML = '';

        var convertParams = {
            templateId: document.getElementById('convert-templateId').value,
            templateType: document.getElementById('convert-templateType').value,
            token: document.getElementById('convert-token').value
        };

        if (!convertParams.templateId) {
            convertStatus.textContent = '';
            showResult('ts-convert-result', 'error', 'templateId 不能为空');
            btn.disabled = false;
            return;
        }

        try {
            var queryParts = [];
            for (var key in convertParams) {
                if (convertParams[key]) queryParts.push(encodeURIComponent(key) + '=' + encodeURIComponent(convertParams[key]));
            }

            var resp = await fetch('/home/proxyTemplateConvert', {
                method: 'POST',
                headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
                body: queryParts.join('&')
            });

            var text = await resp.text();
            var result;
            try { result = JSON.parse(text); } catch (e) { throw new Error('响应解析失败: ' + text); }
            if (result.code) throw new Error('解析失败: ' + result.code);

            convertStatus.textContent = '';
            var dataVal = result.data || '';
            var isImage = typeof dataVal === 'string' && dataVal.indexOf('data:image') === 0;
            var dataHtml = isImage
                ? '<img src="' + dataVal + '" style="max-width:100%;margin-top:8px;border:1px solid #ddd;border-radius:4px;" alt="模板预览">'
                : '<code>' + (dataVal || 'N/A') + '</code>';
            showResult('ts-convert-result', 'success',
                '<b>模板解析完成</b><br>模板已转为 draft 并完成书签识别<br>' + dataHtml
            );
        } catch (err) {
            console.error('convert error:', err);
            convertStatus.textContent = '';
            showResult('ts-convert-result', 'error', err.message);
        } finally {
            btn.disabled = false;
        }
    });

    function showResult(containerId, type, html) {
        document.getElementById(containerId).innerHTML =
            '<div class="alert alert-' + (type === 'error' ? 'danger' : 'success') + '">' + html + '</div>';
    }

    function extractLuoshuInfo(editUrl) {
        try {
            var targetUrl = editUrl;
            if (editUrl.indexOf('/home/iframe?url=') !== -1) {
                var urlObj = new URL(editUrl, window.location.origin);
                var urlParam = urlObj.searchParams.get('url');
                if (urlParam) targetUrl = decodeURIComponent(urlParam);
            }
            var url = new URL(targetUrl);
            var repoId = url.searchParams.get('repoId') || '';
            var clientId = url.searchParams.get('clientId') || 'g_clientId';
            var token = url.searchParams.get('zdocs_access_token')
                     || url.searchParams.get('docs_access_token')
                     || url.searchParams.get('zoffice_access_token');
            if (!token) {
                var params = url.searchParams.get('params') || '';
                var match = params.match(/(?:zdocs_access_token|docs_access_token|zoffice_access_token)=([^;]+)/);
                if (match) token = match[1];
            }
            if (!repoId) {
                var pathMatch = url.pathname.match(/\/docs\/app\/([^/]+)\//);
                if (pathMatch) repoId = pathMatch[1];
            }
            return { repoId: repoId, clientId: clientId, token: token };
        } catch (e) {
            console.error('extractLuoshuInfo error:', e);
            return { repoId: '', clientId: '', token: '' };
        }
    }
</script>
</body>
</html>
