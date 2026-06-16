const drive = "local";

let allFiles = [];
let filteredFiles = [];
let currentPage = 1;
let pageSize = 15;
let currentFormat = 'all';
let searchText = '';
let sortField = 'modifiedAt';
let sortAsc = false;

const FORMAT_MAP = {
    'word': ['.doc', '.docx', '.wps'],
    'excel': ['.xls', '.xlsx', '.csv'],
    'ppt': ['.ppt', '.pptx'],
    'pdf': ['.pdf'],
    'ofd': ['.ofd'],
    'image': ['.png', '.jpg', '.jpeg', '.gif', '.bmp', '.svg', '.webp', '.ico'],
    'json': ['.json']
};

const EXT_ICON_MAP = {
    '.doc': 'file-text', '.docx': 'file-text', '.wps': 'file-text',
    '.xls': 'grid', '.xlsx': 'grid', '.csv': 'grid',
    '.ppt': 'monitor', '.pptx': 'monitor',
    '.pdf': 'book-open',
    '.ofd': 'book',
    '.png': 'image', '.jpg': 'image', '.jpeg': 'image', '.gif': 'image',
    '.bmp': 'image', '.svg': 'image', '.webp': 'image', '.ico': 'image',
};

function getFileExt(name) {
    if (!name) return '';
    const idx = name.lastIndexOf('.');
    return idx >= 0 ? name.substring(idx).toLowerCase() : '';
}

function getFileIcon(name) {
    return EXT_ICON_MAP[getFileExt(name)] || 'file';
}

function formatFileSize(bytes) {
    if (bytes == null || bytes === 0) return '-';
    if (bytes < 1024) return bytes + ' B';
    if (bytes < 1024 * 1024) return (bytes / 1024).toFixed(1) + ' KB';
    return (bytes / (1024 * 1024)).toFixed(1) + ' MB';
}

function formatDate(dateStr) {
    if (!dateStr) return '-';
    try {
        const d = new Date(dateStr);
        if (isNaN(d.getTime())) return '-';
        const pad = n => n < 10 ? '0' + n : n;
        return d.getFullYear() + '-' + pad(d.getMonth() + 1) + '-' + pad(d.getDate()) +
            ' ' + pad(d.getHours()) + ':' + pad(d.getMinutes());
    } catch (e) {
        return '-';
    }
}

function escapeHtml(text) {
    if (!text) return '';
    const div = document.createElement('div');
    div.appendChild(document.createTextNode(text));
    return div.innerHTML;
}

/** 加载文件列表 */
function loadFiles() {
    $.get('/home/local/files', function (data) {
        if (typeof data === 'string') {
            try { allFiles = JSON.parse(data); } catch (e) { allFiles = []; }
        } else {
            allFiles = data || [];
        }
        applyFilterAndRender();
    }).fail(function () {
        allFiles = [];
        applyFilterAndRender();
    });
}

/** 应用筛选、排序、分页并渲染 */
function applyFilterAndRender() {
    let list = allFiles.slice();

    if (currentFormat !== 'all') {
        const exts = FORMAT_MAP[currentFormat] || [];
        list = list.filter(f => exts.some(e => f.name && f.name.toLowerCase().endsWith(e)));
    }

    if (searchText) {
        const kw = searchText.toLowerCase();
        list = list.filter(f => f.name && f.name.toLowerCase().indexOf(kw) >= 0);
    }

    list.sort(function (a, b) {
        let va, vb;
        if (sortField === 'name') {
            va = (a.name || '').toLowerCase();
            vb = (b.name || '').toLowerCase();
            return sortAsc ? va.localeCompare(vb) : vb.localeCompare(va);
        } else if (sortField === 'size') {
            va = a.size || 0;
            vb = b.size || 0;
        } else {
            va = a.modified_at ? new Date(a.modified_at).getTime() : 0;
            vb = b.modified_at ? new Date(b.modified_at).getTime() : 0;
        }
        return sortAsc ? va - vb : vb - va;
    });

    filteredFiles = list;
    currentPage = 1;
    renderPage();
}

/** 渲染当前页数据 */
function renderPage() {
    const total = filteredFiles.length;
    const totalPages = Math.max(1, Math.ceil(total / pageSize));
    if (currentPage > totalPages) currentPage = totalPages;

    const start = (currentPage - 1) * pageSize;
    const end = Math.min(start + pageSize, total);
    const pageData = filteredFiles.slice(start, end);

    const tbody = $('#file-tbody');
    tbody.empty();

    if (pageData.length === 0) {
        tbody.append('<tr><td colspan="5" class="text-center text-muted py-3">暂无匹配文件</td></tr>');
    } else {
        pageData.forEach(function (file) {
            const ext = getFileExt(file.name);
            const icon = getFileIcon(file.name);
            const sizeStr = formatFileSize(file.size);
            const dateStr = formatDate(file.modified_at);
            const safeId = escapeHtml(file.id);
            const safeName = escapeHtml(file.name);

            let extBadge = '';
            if (ext) {
                extBadge = '<span class="ext-badge">' + ext.substring(1).toUpperCase() + '</span>';
            }

            tbody.append(
                '<tr class="file-row" data-id="' + safeId + '">' +
                '<td><input type="checkbox" class="file-checkbox" value="' + safeId + '" onchange="updateBatchBtn()"></td>' +
                '<td class="file-name-cell">' +
                    '<span class="mr-2" data-feather="' + icon + '"></span>' +
                    '<a href="#" class="file-link" onclick="openDoc(\'' + safeId + '\', \'view\'); return false;" title="' + safeName + '">' + safeName + '</a>' +
                    extBadge +
                '</td>' +
                '<td class="text-muted small">' + sizeStr + '</td>' +
                '<td class="text-muted small">' + dateStr + '</td>' +
                '<td>' +
                    '<div class="action-btns">' +
                        '<button class="btn btn-sm btn-outline-primary" onclick="openDoc(\'' + safeId + '\', \'edit\')" title="编辑"><span data-feather="edit-2" style="width:14px;height:14px"></span></button>' +
                        '<button class="btn btn-sm btn-outline-secondary" onclick="downloadDoc(\'' + safeId + '\')" title="下载"><span data-feather="download" style="width:14px;height:14px"></span></button>' +
                        '<button class="btn btn-sm btn-outline-danger" onclick="deleteDoc(\'' + safeId + '\')" title="删除"><span data-feather="trash-2" style="width:14px;height:14px"></span></button>' +
                        '<button class="btn btn-sm btn-outline-info" onclick="showMoreMenu(\'' + safeId + '\', event)" title="更多"><span data-feather="more-horizontal" style="width:14px;height:14px"></span></button>' +
                    '</div>' +
                '</td>' +
                '</tr>'
            );
        });
    }

    feather.replace();
    renderPagination(total, totalPages);
    updateBatchBtn();
    $('#select-all').prop('checked', false);
}

/** 分页渲染 */
function renderPagination(total, totalPages) {
    $('#pagination-info').text('共 ' + total + ' 个文件');
    const bar = $('#pagination-btns');
    bar.empty();
    if (totalPages <= 1) return;

    bar.append('<button class="btn btn-sm btn-outline-secondary" ' +
        (currentPage <= 1 ? 'disabled' : '') + ' onclick="goPage(' + (currentPage - 1) + ')">‹</button>');

    let startP = Math.max(1, currentPage - 2);
    let endP = Math.min(totalPages, startP + 4);
    if (endP - startP < 4) startP = Math.max(1, endP - 4);

    for (let i = startP; i <= endP; i++) {
        const cls = i === currentPage ? 'btn-primary' : 'btn-outline-secondary';
        bar.append('<button class="btn btn-sm ' + cls + '" onclick="goPage(' + i + ')">' + i + '</button>');
    }

    bar.append('<button class="btn btn-sm btn-outline-secondary" ' +
        (currentPage >= totalPages ? 'disabled' : '') + ' onclick="goPage(' + (currentPage + 1) + ')">›</button>');
}

function goPage(p) {
    currentPage = p;
    renderPage();
}

function changePageSize() {
    pageSize = parseInt($('#page-size-select').val()) || 15;
    currentPage = 1;
    renderPage();
}

/** 格式筛选 */
function setFormat(fmt, btn) {
    currentFormat = fmt;
    $('.format-filter .btn').removeClass('active');
    $(btn).addClass('active');
    applyFilterAndRender();
}

/** 搜索输入 */
function onSearchInput() {
    searchText = ($('#search-input').val() || '').trim();
    applyFilterAndRender();
}

/** 排序 */
function sortBy(field) {
    if (sortField === field) {
        sortAsc = !sortAsc;
    } else {
        sortField = field;
        sortAsc = true;
    }
    $('.sort-icon').text('');
    $('#sort-' + field).text(sortAsc ? '▲' : '▼');
    applyFilterAndRender();
}

/** 全选/反选 */
function toggleSelectAll() {
    const checked = $('#select-all').is(':checked');
    $('.file-checkbox').prop('checked', checked);
    updateBatchBtn();
}

function updateBatchBtn() {
    const count = $('.file-checkbox:checked').length;
    $('#batch-count').text(count);
    $('#btn-batch-delete').prop('disabled', count === 0);
}

/** 批量删除 */
function batchDelete() {
    const ids = [];
    $('.file-checkbox:checked').each(function () { ids.push($(this).val()); });
    if (ids.length === 0) return;
    if (!confirm('确认删除选中的 ' + ids.length + ' 个文件？此操作不可恢复。')) return;

    $.ajax({
        url: '/v2/context/file/batchOp/delete',
        type: 'POST',
        contentType: 'application/json',
        data: JSON.stringify(ids),
        success: function (results) {
            showToastInParent('批量删除完成');
            loadFiles();
        },
        error: function (xhr) {
            showToastInParent('批量删除失败: ' + xhr.statusText);
        }
    });
}

/** 打开文件（预览/编辑） */
function openDoc(docId, action, isInFrame, callback) {
    if (!docId) return;
    let url = '/v2/context/driver-cb?docId=' + docId + '&action=' + action;
    if (isInFrame) url += '&isInFrame=true';
    fetch(url).then(function (res) {
        if (!res.ok) {
            showToastInParent('打开失败: ' + res.statusText);
            return;
        }
        res.text().then(function (text) {
            if (callback) { callback(text); return; }
            window.open(text);
        });
    });
}

/** 下载文件 */
function downloadDoc(docId) {
    if (!docId) return;
    window.open('/v2/context/' + docId + '/content?download=true');
}

/** 上传文件 */
function uploadDoc() {
    $('#fileUpload').click();
}

function uploadFile(file) {
    if (!file) return;
    const formData = new FormData();
    formData.append('file', file, file.name);
    formData.append('drive', drive);
    $.ajax({
        url: '/v2/context/file/upload',
        type: 'POST',
        data: formData,
        processData: false,
        contentType: false,
        success: function (data) {
            if (data && data.id) {
                showToastInParent(data.name + ' 上传成功');
                loadFiles();
            }
        },
        error: function (xhr) {
            showToastInParent('上传失败: ' + (xhr.responseText || xhr.statusText));
        }
    });
}

/** 删除单个文件 */
function deleteDoc(docId) {
    if (!docId) return;
    if (!confirm('确认删除此文件？')) return;
    $.get('/v2/context/file/delete/' + docId + '?driveId=' + drive, function (data) {
        showToastInParent((data.name || docId) + ' 删除成功');
        loadFiles();
    }).fail(function (xhr) {
        showToastInParent('删除失败: ' + (xhr.responseText || xhr.statusText));
    });
}

/** 新建文件 */
function createNewFile(docType) {
    const templateMap = {
        'docx': 'new.docx',
        'xlsx': 'new.xlsx',
        'pptx': 'new.pptx'
    };
    $.ajax({
        url: '/v2/context/file/new',
        type: 'POST',
        contentType: 'application/json',
        data: JSON.stringify({
            docType: docType,
            templateName: templateMap[docType] || ('new.' + docType),
            filename: '新建文档'
        }),
        success: function (data) {
            if (data && data.id) {
                showToastInParent(data.name + ' 创建成功');
                loadFiles();
                openDoc(data.id, 'edit');
            }
        },
        error: function (xhr) {
            showToastInParent('创建失败: ' + (xhr.responseText || xhr.statusText));
        }
    });
}

/** 更多菜单 */
let contextMenu = null;
function showMoreMenu(docId, evt) {
    evt.stopPropagation();
    if (!contextMenu) {
        contextMenu = document.createElement('div');
        contextMenu.className = 'drive-context-menu';
        document.body.appendChild(contextMenu);
        document.addEventListener('click', function () {
            contextMenu.style.display = 'none';
        });
    }

    contextMenu.innerHTML =
        '<div class="ctx-item" data-action="editInTab">当前页面编辑</div>' +
        '<div class="ctx-item" data-action="viewInTab">当前页面预览</div>' +
        '<div class="ctx-item" data-action="versions">版本预览</div>' +
        '<div class="ctx-item" data-action="meta">自定义元数据</div>';

    contextMenu.style.display = 'block';
    contextMenu.style.left = evt.clientX + 'px';
    contextMenu.style.top = (evt.clientY + window.scrollY) + 'px';

    $(contextMenu).find('.ctx-item').off('click').on('click', function () {
        const action = $(this).data('action');
        contextMenu.style.display = 'none';
        if (action === 'editInTab') openInCurrentTab(docId, 'edit');
        else if (action === 'viewInTab') openInCurrentTab(docId, 'view');
        else if (action === 'versions') showVersionPreviewModal(docId);
        else if (action === 'meta') openFileOpModal('修改元数据', '/home/meta/' + docId);
    });
}

function openInCurrentTab(docId, action) {
    openDoc(docId, action, true, function (url) {
        window.parent.postMessage({ type: 'updateFrameSrc', msg: { docId: docId, url: url } }, '*');
    });
}

function showToastInParent(msg) {
    window.parent.postMessage({ type: 'showToast', msg: msg }, '*');
}

/** 模态框 */
function openFileOpModal(title, src) {
    $('#fileOpModalLabel').text(title);
    var versionContent = document.getElementById('versionPreviewContent');
    if (versionContent) versionContent.style.display = 'none';
    var iframe = document.getElementById('fileOpIframe');
    iframe.style.display = '';
    iframe.src = src;
    $('#fileOpModal').modal('show').modal('handleUpdate');
}

function closeFileOpModal() {
    var iframe = document.getElementById('fileOpIframe');
    if (iframe) iframe.src = '';
    $('#fileOpModal').modal('hide');
}

/** 版本预览 */
function showVersionPreviewModal(docId) {
    var modalTitle = document.getElementById('fileOpModalLabel');
    var modalIframe = document.getElementById('fileOpIframe');
    modalTitle.innerText = '版本预览 - ' + docId;
    modalIframe.style.display = 'none';
    modalIframe.src = '';

    var versionContent = document.getElementById('versionPreviewContent');
    if (!versionContent) {
        versionContent = document.createElement('div');
        versionContent.id = 'versionPreviewContent';
        modalIframe.parentNode.appendChild(versionContent);
    }
    versionContent.style.display = '';
    versionContent.innerHTML = '<div style="padding:20px"><p>正在加载版本列表...</p></div>';

    $('#fileOpModal').modal('show').modal('handleUpdate');

    fetch('/v2/context/' + docId + '/versions')
        .then(function (r) { return r.json(); })
        .then(function (versions) {
            var html = '<div style="padding:20px"><h5>文档版本列表</h5>';
            html += '<div style="margin-bottom:15px; padding:10px; background:#e8f5e9; border-radius:4px; display:flex; align-items:center; justify-content:space-between;">';
            html += '<div><strong>最新版本</strong><span style="color:#888; margin-left:10px">当前线上版本</span></div>';
            html += '<button class="btn btn-sm btn-primary" onclick="previewVersion(\'' + docId + '\',\'latest\')">预览</button></div>';

            if (!versions || versions.length === 0) {
                html += '<p style="color:#888">暂无历史版本。</p>';
            } else {
                versions.forEach(function (v) {
                    var ds = new Date(parseInt(v)).toLocaleString();
                    html += '<div style="margin-bottom:8px; padding:10px; background:#f8f9fa; border-radius:4px; display:flex; align-items:center; justify-content:space-between;">';
                    html += '<div><strong>' + ds + '</strong><span style="color:#aaa; margin-left:10px; font-size:12px">版本: ' + v + '</span></div>';
                    html += '<div>';
                    html += '<button class="btn btn-sm btn-outline-primary mr-1" onclick="previewVersion(\'' + docId + '\',\'' + v + '\')">预览</button>';
                    html += '<button class="btn btn-sm btn-outline-secondary mr-1" onclick="downloadVersion(\'' + docId + '\',\'' + v + '\')">下载</button>';
                    html += '<button class="btn btn-sm btn-outline-danger" onclick="deleteVersion(\'' + docId + '\',\'' + v + '\')">删除</button>';
                    html += '</div></div>';
                });
            }
            html += '</div>';
            versionContent.innerHTML = html;
        })
        .catch(function (e) {
            versionContent.innerHTML = '<div style="padding:20px; color:red">加载失败: ' + e.message + '</div>';
        });
}

function previewVersion(docId, version) {
    var url = '/v2/context/driver-cb?docId=' + docId + '&action=view';
    if (version && version !== 'latest') url += '&version=' + version;
    fetch(url).then(function (r) {
        if (!r.ok) { alert('预览失败'); return; }
        r.text().then(function (u) { window.open(u, '_blank'); });
    });
}

function downloadVersion(docId, version) {
    var url = '/v2/context/' + docId + '/content?download=true';
    if (version && version !== 'latest') url += '&version=' + version;
    window.open(url);
}

function deleteVersion(docId, version) {
    if (!confirm('确认删除该历史版本？')) return;
    fetch('/v2/context/' + docId + '/versions/' + version, { method: 'DELETE' })
        .then(function (r) {
            if (!r.ok) return r.text().then(function (t) { throw new Error(t); });
            showVersionPreviewModal(docId);
        })
        .catch(function (e) { alert('删除失败: ' + e.message); });
}

/** 拖拽上传 */
function initDragDrop() {
    var dropArea = document.getElementById('dropArea');
    window.addEventListener('dragenter', function () { dropArea.style.display = 'block'; });
    dropArea.addEventListener('dragover', function (e) { e.preventDefault(); dropArea.classList.add('active'); });
    dropArea.addEventListener('dragleave', function () { dropArea.classList.remove('active'); dropArea.style.display = 'none'; });
    dropArea.addEventListener('drop', function (e) {
        e.preventDefault();
        dropArea.classList.remove('active');
        dropArea.style.display = 'none';
        if (e.dataTransfer.files.length > 0) uploadFile(e.dataTransfer.files[0]);
    });
}

/** 初始化 */
$(function () {
    $('#fileUpload').change(function (e) { uploadFile(e.target.files[0]); });
    initDragDrop();
    loadFiles();
});
