<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <title>文档比对</title>
    <style>
        .drop-area {
            border: 2px dashed #ccc;
            border-radius: 10px;
            padding: 20px;
            text-align: center;
            margin: 10px 0;
            transition: border-color 0.3s;
        }
        .drop-area.dragover {
            border-color: #007bff;
            background-color: #f8f9fa;
        }
        .drop-area-content { padding: 15px; }
        .compare-container { display: flex; justify-content: space-between; }
        .compare-panel { width: 48%; }
        .file-info { margin-top: 10px; font-weight: bold; }
        .error-message { color: red; font-weight: bold; margin-top: 5px; }
        .mode-selector {
            margin-bottom: 15px;
            padding: 10px;
            background-color: #f8f9fa;
            border-radius: 8px;
        }
        .mode-selector label { margin-right: 20px; cursor: pointer; }
        .version-select-area { margin-top: 10px; }
        .version-select-area select { width: 100%; padding: 6px; border-radius: 4px; border: 1px solid #ccc; }
        .hint-text { font-size: 12px; color: #888; margin-top: 5px; }
        .source-tabs {
            display: flex;
            border-bottom: 2px solid #dee2e6;
            margin-bottom: 10px;
        }
        .source-tab {
            padding: 8px 16px;
            cursor: pointer;
            border: 1px solid transparent;
            border-bottom: none;
            margin-bottom: -2px;
            border-radius: 4px 4px 0 0;
            background: #f8f9fa;
            color: #666;
            font-size: 14px;
        }
        .source-tab.active {
            background: #fff;
            border-color: #dee2e6;
            border-bottom-color: #fff;
            color: #333;
            font-weight: bold;
        }
        .source-content { display: none; }
        .source-content.active { display: block; }
        .repo-select-wrapper { margin: 10px 0; }
        .repo-select-wrapper select { width: 100%; padding: 8px; border-radius: 4px; border: 1px solid #ccc; }
        .selected-doc-badge {
            display: inline-block;
            padding: 4px 10px;
            background: #e7f3ff;
            border: 1px solid #b3d7ff;
            border-radius: 4px;
            margin-top: 8px;
            font-size: 13px;
        }
    </style>
    <link href="/static/bootstrap.min.css" rel="stylesheet">
</head>
<body>
<div class="container-fluid">
    <div class="row">
        <main role="main" class="col-md-12 col-lg-12">
            <div class="mode-selector">
                <strong>比对模式：</strong>
                <label><input type="radio" name="compareMode" value="different" checked onchange="switchMode()"> 不同文档比对</label>
                <label><input type="radio" name="compareMode" value="version" onchange="switchMode()"> 同文档版本比对</label>
            </div>

            <div class="mode-selector">
                <strong>比对引擎：</strong>
                <label><input type="radio" name="compareEngine" value="true" checked> AI比对</label>
                <label><input type="radio" name="compareEngine" value="false"> Aspose比对</label>
                <span class="hint-text">（AI比对额外支持PDF格式，Aspose比对仅支持doc/docx）</span>
            </div>

            <button type="button" class="btn btn-primary mb-3" onclick="compareDocuments()" id="compareBtn" disabled>文档比对</button>

            <!-- 不同文档比对模式 -->
            <div id="differentMode">
                <div class="compare-container">
                    <!-- 文档A -->
                    <div class="compare-panel">
                        <h5>文档A</h5>
                        <div class="source-tabs">
                            <div class="source-tab active" onclick="switchSource('A', 'repo')">从仓库选择</div>
                            <div class="source-tab" onclick="switchSource('A', 'upload')">上传文件</div>
                        </div>
                        <div id="repoSourceA" class="source-content active">
                            <div class="repo-select-wrapper">
                                <select id="repoSelectA" class="form-control" onchange="onRepoDocSelected('A')">
                                    <option value="">-- 请选择文档 --</option>
                                </select>
                            </div>
                            <div id="repoInfoA"></div>
                        </div>
                        <div id="uploadSourceA" class="source-content">
                            <div class="drop-area" id="dropAreaA">
                                <div class="drop-area-content">
                                    <p>将文件拖拽到这里或点击选择文件</p>
                                    <input type="file" id="fileInputA" style="display: none;" accept=".doc,.docx,.pdf">
                                    <button type="button" class="btn btn-outline-primary" id="selectBtnA">选择文件</button>
                                    <div class="file-info" id="fileInfoA">未选择文件</div>
                                    <div class="error-message" id="errorA"></div>
                                </div>
                            </div>
                        </div>
                        <p class="hint-text">支持格式：doc、docx、pdf（PDF仅支持AI比对）</p>
                    </div>

                    <!-- 文档B -->
                    <div class="compare-panel">
                        <h5>文档B</h5>
                        <div class="source-tabs">
                            <div class="source-tab active" onclick="switchSource('B', 'repo')">从仓库选择</div>
                            <div class="source-tab" onclick="switchSource('B', 'upload')">上传文件</div>
                        </div>
                        <div id="repoSourceB" class="source-content active">
                            <div class="repo-select-wrapper">
                                <select id="repoSelectB" class="form-control" onchange="onRepoDocSelected('B')">
                                    <option value="">-- 请选择文档 --</option>
                                </select>
                            </div>
                            <div id="repoInfoB"></div>
                        </div>
                        <div id="uploadSourceB" class="source-content">
                            <div class="drop-area" id="dropAreaB">
                                <div class="drop-area-content">
                                    <p>将文件拖拽到这里或点击选择文件</p>
                                    <input type="file" id="fileInputB" style="display: none;" accept=".doc,.docx,.pdf">
                                    <button type="button" class="btn btn-outline-primary" id="selectBtnB">选择文件</button>
                                    <div class="file-info" id="fileInfoB">未选择文件</div>
                                    <div class="error-message" id="errorB"></div>
                                </div>
                            </div>
                        </div>
                        <p class="hint-text">支持格式：doc、docx、pdf（PDF仅支持AI比对）</p>
                    </div>
                </div>
            </div>

            <!-- 同文档版本比对模式 -->
            <div id="versionMode" style="display: none;">
                <div class="compare-container">
                    <div class="compare-panel" style="width: 100%;">
                        <h5>选择要比对的文档</h5>
                        <div style="margin-bottom: 15px;">
                            <select id="docSelect" class="form-control" onchange="onDocSelected()">
                                <option value="">-- 请选择文档 --</option>
                            </select>
                        </div>
                        <div class="compare-container">
                            <div class="compare-panel">
                                <h5>版本A</h5>
                                <div class="version-select-area">
                                    <select id="versionSelectA" class="form-control" onchange="updateVersionCompareButton()">
                                        <option value="">-- 请先选择文档 --</option>
                                    </select>
                                </div>
                            </div>
                            <div class="compare-panel">
                                <h5>版本B</h5>
                                <div class="version-select-area">
                                    <select id="versionSelectB" class="form-control" onchange="updateVersionCompareButton()">
                                        <option value="">-- 请先选择文档 --</option>
                                    </select>
                                </div>
                            </div>
                        </div>
                        <div class="error-message" id="versionError"></div>
                    </div>
                </div>
            </div>
        </main>
    </div>
</div>

<script src="/static/jquery-3.7.0.min.js"></script>
<script>
    var docIdA = null;
    var docIdB = null;
    var currentMode = 'different';
    var selectedDocId = null;
    var repoFilesCache = null;

    /** 加载仓库文档列表（带缓存） */
    function loadRepoFiles(callback) {
        if (repoFilesCache) {
            callback(repoFilesCache);
            return;
        }
        fetch('/home/local/files')
        .then(function(r) { return r.json(); })
        .then(function(files) {
            repoFilesCache = files;
            callback(files);
        })
        .catch(function(e) { console.error('加载文档列表失败:', e); });
    }

    /** 填充仓库下拉框 */
    function populateRepoSelect(selectId, files) {
        var sel = document.getElementById(selectId);
        sel.innerHTML = '<option value="">-- 请选择文档 --</option>';
        files.forEach(function(f) {
            var ext = (f.name || '').toLowerCase();
            if (ext.endsWith('.doc') || ext.endsWith('.docx') || ext.endsWith('.pdf')) {
                var opt = document.createElement('option');
                opt.value = f.id;
                opt.textContent = f.name;
                opt.setAttribute('data-name', f.name);
                sel.appendChild(opt);
            }
        });
    }

    /** 切换文档来源 tab（仓库 / 上传） */
    function switchSource(side, source) {
        var tabs = document.querySelectorAll('#differentMode .compare-panel:nth-child(' + (side === 'A' ? '1' : '2') + ') .source-tab');
        tabs.forEach(function(t) { t.classList.remove('active'); });
        if (source === 'repo') {
            tabs[0].classList.add('active');
        } else {
            tabs[1].classList.add('active');
        }

        document.getElementById('repoSource' + side).className = 'source-content' + (source === 'repo' ? ' active' : '');
        document.getElementById('uploadSource' + side).className = 'source-content' + (source === 'upload' ? ' active' : '');

        if (side === 'A') { docIdA = null; }
        else { docIdB = null; }
        updateCompareButton();

        if (source === 'repo') {
            loadRepoFiles(function(files) { populateRepoSelect('repoSelect' + side, files); });
        }
    }

    /** 从仓库下拉框选择文档 */
    function onRepoDocSelected(side) {
        var sel = document.getElementById('repoSelect' + side);
        var docId = sel.value;
        var infoEl = document.getElementById('repoInfo' + side);

        if (!docId) {
            if (side === 'A') { docIdA = null; } else { docIdB = null; }
            infoEl.innerHTML = '';
            updateCompareButton();
            return;
        }

        var selectedOpt = sel.options[sel.selectedIndex];
        var docName = selectedOpt.getAttribute('data-name') || docId;

        if (side === 'A') { docIdA = docId; } else { docIdB = docId; }
        infoEl.innerHTML = '<span class="selected-doc-badge">已选择: ' + docName + '</span>';
        updateCompareButton();
    }

    function switchMode() {
        currentMode = document.querySelector('input[name="compareMode"]:checked').value;
        document.getElementById('differentMode').style.display = currentMode === 'different' ? 'block' : 'none';
        document.getElementById('versionMode').style.display = currentMode === 'version' ? 'block' : 'none';

        docIdA = null;
        docIdB = null;
        selectedDocId = null;
        document.getElementById('compareBtn').disabled = true;

        if (currentMode === 'different') {
            loadRepoFiles(function(files) {
                populateRepoSelect('repoSelectA', files);
                populateRepoSelect('repoSelectB', files);
            });
        } else {
            loadDocList();
        }
    }

    function loadDocList() {
        loadRepoFiles(function(files) {
            var select = document.getElementById('docSelect');
            select.innerHTML = '<option value="">-- 请选择文档 --</option>';
            files.forEach(function(file) {
                var opt = document.createElement('option');
                opt.value = file.id;
                opt.textContent = file.name;
                select.appendChild(opt);
            });
        });
    }

    function onDocSelected() {
        var docId = document.getElementById('docSelect').value;
        selectedDocId = docId;
        var selectA = document.getElementById('versionSelectA');
        var selectB = document.getElementById('versionSelectB');

        if (!docId) {
            selectA.innerHTML = '<option value="">-- 请先选择文档 --</option>';
            selectB.innerHTML = '<option value="">-- 请先选择文档 --</option>';
            document.getElementById('compareBtn').disabled = true;
            return;
        }

        fetch('/v2/context/' + docId + '/versions')
        .then(function(r) { return r.json(); })
        .then(function(versions) {
            if (versions.length === 0) {
                selectA.innerHTML = '<option value="">该文档暂无历史版本</option>';
                selectB.innerHTML = '<option value="">该文档暂无历史版本</option>';
                document.getElementById('versionError').textContent = '该文档暂无历史版本，无法进行版本比对。请先编辑保存文档以产生历史版本。';
                document.getElementById('compareBtn').disabled = true;
                return;
            }
            document.getElementById('versionError').textContent = '';
            var html = '<option value="">-- 请选择版本 --</option><option value="latest">最新版本</option>';
            versions.forEach(function(v) {
                html += '<option value="' + v + '">' + new Date(parseInt(v)).toLocaleString() + '</option>';
            });
            selectA.innerHTML = html;
            selectB.innerHTML = html;
        })
        .catch(function(e) { console.error('加载版本列表失败:', e); });
    }

    function updateVersionCompareButton() {
        var vA = document.getElementById('versionSelectA').value;
        var vB = document.getElementById('versionSelectB').value;
        var errorEl = document.getElementById('versionError');

        if (vA && vB && vA === vB) {
            errorEl.textContent = '版本A和版本B不能相同';
            document.getElementById('compareBtn').disabled = true;
            return;
        }
        errorEl.textContent = '';
        document.getElementById('compareBtn').disabled = !(selectedDocId && vA && vB);
    }

    function initDropAreas() {
        setupDropArea(
            document.getElementById('dropAreaA'),
            document.getElementById('fileInputA'),
            document.getElementById('selectBtnA'), 'A');
        setupDropArea(
            document.getElementById('dropAreaB'),
            document.getElementById('fileInputB'),
            document.getElementById('selectBtnB'), 'B');
    }

    function setupDropArea(dropArea, fileInput, selectBtn, side) {
        ['dragenter', 'dragover', 'dragleave', 'drop'].forEach(function(ev) {
            dropArea.addEventListener(ev, function(e) { e.preventDefault(); e.stopPropagation(); }, false);
        });
        ['dragenter', 'dragover'].forEach(function(ev) {
            dropArea.addEventListener(ev, function() { dropArea.classList.add('dragover'); }, false);
        });
        ['dragleave', 'drop'].forEach(function(ev) {
            dropArea.addEventListener(ev, function() { dropArea.classList.remove('dragover'); }, false);
        });
        dropArea.addEventListener('drop', function(e) { handleFiles(e.dataTransfer.files, side); }, false);
        fileInput.addEventListener('change', function(e) {
            if (e.target.files.length > 0) { handleFiles(e.target.files, side); e.target.value = ''; }
        });
        selectBtn.addEventListener('click', function() { fileInput.click(); });
    }

    function isFileTypeSupported(fileName) {
        var exts = ['.doc', '.docx', '.pdf'];
        var lower = fileName.toLowerCase();
        for (var i = 0; i < exts.length; i++) { if (lower.endsWith(exts[i])) return true; }
        return false;
    }

    function clearError(side) { document.getElementById('error' + side).textContent = ''; }
    function showError(msg, side) { document.getElementById('error' + side).textContent = msg; }

    function handleFiles(files, side) {
        if (files.length === 0) return;
        var file = files[0];
        if (!isFileTypeSupported(file.name)) {
            showError('仅支持 doc、docx、pdf 格式的文档', side);
            return;
        }
        clearError(side);
        uploadFile(file, side);
    }

    function uploadFile(file, side) {
        var formData = new FormData();
        formData.append('file', file, file.name);
        formData.append('drive', 'local');

        fetch('/v2/context/file/upload', { method: 'POST', body: formData })
        .then(function(r) {
            if (!r.ok) return r.text().then(function(t) { throw new Error('上传失败: ' + (t || r.statusText)); });
            return r.json();
        })
        .then(function(data) {
            if (side === 'A') { docIdA = data.id; document.getElementById('fileInfoA').textContent = '已选择: ' + data.name; }
            else { docIdB = data.id; document.getElementById('fileInfoB').textContent = '已选择: ' + data.name; }
            repoFilesCache = null;
            updateCompareButton();
        })
        .catch(function(e) { alert('文件上传失败: ' + e.message); });
    }

    function updateCompareButton() {
        document.getElementById('compareBtn').disabled = !(docIdA && docIdB);
    }

    /** 获取当前选择的比对引擎参数 */
    function getIsAiCompare() {
        return document.querySelector('input[name="compareEngine"]:checked').value;
    }

    function compareDocuments() {
        if (currentMode === 'version') { compareByVersion(); return; }
        if (!docIdA || !docIdB) { alert('请先选择两个文档'); return; }

        var url = '/v2/context/compareDoc?docAid=' + docIdA + '&docBid=' + docIdB
                  + '&isAiCompare=' + getIsAiCompare();
        fetch(url)
        .then(function(r) {
            if (!r.ok) return r.text().then(function(t) { throw new Error('获取比对URL失败: ' + (t || r.statusText)); });
            return r.text();
        })
        .then(function(url) { window.open(url, '_blank'); })
        .catch(function(e) { alert('文档比对失败: ' + e.message); });
    }

    function compareByVersion() {
        var vA = document.getElementById('versionSelectA').value;
        var vB = document.getElementById('versionSelectB').value;
        if (!selectedDocId || !vA || !vB) { alert('请选择文档和两个不同的版本'); return; }
        if (vA === vB) { alert('版本A和版本B不能相同'); return; }

        var url = '/v2/context/compareDoc?docAid=' + selectedDocId + '&docBid=' + selectedDocId
                  + '&versionA=' + vA + '&versionB=' + vB
                  + '&isAiCompare=' + getIsAiCompare();
        fetch(url)
        .then(function(r) {
            if (!r.ok) return r.text().then(function(t) { throw new Error('获取比对URL失败: ' + (t || r.statusText)); });
            return r.text();
        })
        .then(function(u) { window.open(u, '_blank'); })
        .catch(function(e) { alert('版本比对失败: ' + e.message); });
    }

    document.addEventListener('DOMContentLoaded', function() {
        initDropAreas();
        loadRepoFiles(function(files) {
            populateRepoSelect('repoSelectA', files);
            populateRepoSelect('repoSelectB', files);
        });
    });
</script>
</body>
</html>