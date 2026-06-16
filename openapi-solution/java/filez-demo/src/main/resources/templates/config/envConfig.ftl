<!DOCTYPE html>
<html lang="zh-CN">
<head>
    <meta charset="UTF-8">
    <title>环境配置</title>
    <link href="/static/bootstrap.min.css" rel="stylesheet">
    <style>
        .env-container { padding: 20px; max-width: 900px; }
        .config-group { margin-bottom: 20px; border: 1px solid #dee2e6; border-radius: 6px; overflow: hidden; }
        .config-group-header {
            padding: 10px 16px; font-weight: 600; font-size: 14px;
            display: flex; justify-content: space-between; align-items: center; cursor: pointer;
        }
        .config-group-header .badge { font-size: 10px; }
        .group-zoffice .config-group-header { background: #e3f2fd; color: #1565c0; }
        .group-demo .config-group-header { background: #e8f5e9; color: #2e7d32; }
        .group-publicapi .config-group-header { background: #fff3e0; color: #e65100; }
        .config-group-body { padding: 12px 16px; }
        .config-row { display: flex; align-items: center; margin-bottom: 10px; gap: 10px; }
        .config-label {
            width: 200px; min-width: 200px; font-size: 13px; font-weight: 500; color: #333;
            display: flex; flex-direction: column;
        }
        .config-label small { color: #999; font-weight: normal; font-size: 11px; }
        .config-input { flex: 1; }
        .config-input input, .config-input select { font-size: 13px; }
        .config-original { font-size: 11px; color: #aaa; margin-top: 2px; }
        .changed { border-color: #ffc107 !important; background: #fffde7 !important; }
        .save-bar {
            position: sticky; bottom: 0; background: #fff; border-top: 1px solid #dee2e6;
            padding: 12px 0; display: flex; align-items: center; gap: 12px;
        }
        .change-count { font-size: 13px; color: #666; }
        .save-result { font-size: 13px; margin-left: 10px; }
        .save-result.ok { color: #28a745; }
        .save-result.err { color: #dc3545; }
        .tip-banner {
            background: #fff8e1; border: 1px solid #ffe082; border-radius: 4px;
            padding: 8px 14px; margin-bottom: 15px; font-size: 12px; color: #6d4c00;
        }
    </style>
</head>
<body>
<div class="env-container">
    <h5 style="margin-bottom: 4px;">环境配置</h5>
    <div class="tip-banner">
        修改后即时生效（内存级），无需重启。但不会写入 yml 文件，重启后恢复原始值。
    </div>

    <div id="config-area"></div>

    <div class="save-bar">
        <button class="btn btn-primary btn-sm" id="btn-save" onclick="saveConfig()" disabled>保存修改</button>
        <button class="btn btn-outline-secondary btn-sm" onclick="resetAll()">还原全部</button>
        <span class="change-count" id="change-count"></span>
        <span class="save-result" id="save-result"></span>
    </div>
</div>

<script src="/static/jquery-3.7.0.min.js"></script>
<script>
    let originalConfig = {};
    let configMeta = {
        zoffice: {
            title: 'ZOffice 服务配置',
            groupClass: 'group-zoffice',
            fields: [
                { key: 'schema',    label: '协议',        desc: 'http 或 https',             type: 'select', options: ['http', 'https'] },
                { key: 'host',      label: '服务器地址',   desc: 'luoshu-server 部署的 IP/域名' },
                { key: 'port',      label: '端口号',       desc: 'luoshu-server 端口',         type: 'number' },
                { key: 'context',   label: '请求路径',     desc: 'driver-callback 路径，通常固定' },
                { key: 'cors',      label: '允许跨域',     desc: '',                           type: 'boolean' },
                { key: 'appSecret', label: '应用密钥',     desc: '需与管理控制台中的 secret 一致', type: 'password' },
                { key: 'feIntegrationEnable', label: '前端集成', desc: '开启后使用 driver-callback 方式', type: 'boolean' }
            ]
        },
        demo: {
            title: '业务系统 (Demo) 配置',
            groupClass: 'group-demo',
            fields: [
                { key: 'host',      label: '服务器地址',   desc: 'Demo 系统自身 IP/域名' },
                { key: 'context',   label: '请求路径',     desc: 'API 上下文路径' },
                { key: 'repoId',    label: '仓库 ID',     desc: '对应管理控制台中的应用 ID' },
                { key: 'tokenName', label: 'Token 参数名', desc: '认证 cookie 名称' },
                { key: 'serverPort', label: '服务端口',    desc: '当前服务运行端口（只读）', type: 'number', readonly: true }
            ]
        },
        publicapi: {
            title: 'PublicAPI 配置',
            groupClass: 'group-publicapi',
            fields: [
                { key: 'appId',   label: 'AppID',     desc: 'PublicAPI 的 appId，通常为 publicApi' },
                { key: 'secret',  label: '密钥',       desc: '需与管理控制台一致', type: 'password' },
                { key: 'context', label: '接口前缀',    desc: '如 /publicapi/v1' }
            ]
        }
    };

    function loadConfig() {
        $.get('/home/envConfig/get', function (data) {
            originalConfig = JSON.parse(JSON.stringify(data));
            renderConfig(data);
        });
    }

    function renderConfig(data) {
        const area = $('#config-area');
        area.empty();

        Object.keys(configMeta).forEach(function (group) {
            const meta = configMeta[group];
            const values = data[group] || {};

            let html = '<div class="config-group ' + meta.groupClass + '">';
            html += '<div class="config-group-header" onclick="$(this).next().toggle()">';
            html += '<span>' + meta.title + '</span>';
            html += '<span class="badge badge-light">' + meta.fields.length + ' 项</span>';
            html += '</div>';
            html += '<div class="config-group-body">';

            meta.fields.forEach(function (field) {
                const val = values[field.key] != null ? values[field.key] : '';
                const inputId = group + '_' + field.key;
                const ro = field.readonly ? 'readonly disabled' : '';

                html += '<div class="config-row">';
                html += '<div class="config-label"><span>' + field.label + '</span>';
                if (field.desc) html += '<small>' + field.desc + '</small>';
                html += '</div>';
                html += '<div class="config-input">';

                if (field.type === 'boolean') {
                    html += '<select class="form-control form-control-sm config-field" id="' + inputId + '" ' +
                        'data-group="' + group + '" data-key="' + field.key + '" ' + ro + ' onchange="markChanged(this)">';
                    html += '<option value="true"' + (val === true ? ' selected' : '') + '>是 (true)</option>';
                    html += '<option value="false"' + (val !== true ? ' selected' : '') + '>否 (false)</option>';
                    html += '</select>';
                } else if (field.type === 'select') {
                    html += '<select class="form-control form-control-sm config-field" id="' + inputId + '" ' +
                        'data-group="' + group + '" data-key="' + field.key + '" ' + ro + ' onchange="markChanged(this)">';
                    (field.options || []).forEach(function (opt) {
                        html += '<option value="' + opt + '"' + (String(val) === opt ? ' selected' : '') + '>' + opt + '</option>';
                    });
                    html += '</select>';
                } else if (field.type === 'password') {
                    html += '<div class="input-group input-group-sm">';
                    html += '<input type="password" class="form-control config-field" id="' + inputId + '" value="' + escapeAttr(val) + '" ' +
                        'data-group="' + group + '" data-key="' + field.key + '" ' + ro + ' oninput="markChanged(this)">';
                    html += '<div class="input-group-append"><button class="btn btn-outline-secondary" type="button" ' +
                        'onclick="togglePasswordVisibility(\'' + inputId + '\')">显示</button></div>';
                    html += '</div>';
                } else {
                    html += '<input type="' + (field.type === 'number' ? 'number' : 'text') + '" ' +
                        'class="form-control form-control-sm config-field" id="' + inputId + '" value="' + escapeAttr(val) + '" ' +
                        'data-group="' + group + '" data-key="' + field.key + '" ' + ro + ' oninput="markChanged(this)">';
                }

                html += '</div></div>';
            });

            html += '</div></div>';
            area.append(html);
        });
    }

    function markChanged(el) {
        const $el = $(el);
        const group = $el.data('group');
        const key = $el.data('key');
        const origVal = String(originalConfig[group] && originalConfig[group][key] != null ? originalConfig[group][key] : '');
        const curVal = $el.val();

        if (curVal !== origVal) {
            $el.addClass('changed');
        } else {
            $el.removeClass('changed');
        }
        updateChangeCount();
    }

    function updateChangeCount() {
        const count = $('.config-field.changed').length;
        $('#change-count').text(count > 0 ? count + ' 项已修改' : '');
        $('#btn-save').prop('disabled', count === 0);
        $('#save-result').text('');
    }

    function togglePasswordVisibility(inputId) {
        const input = document.getElementById(inputId);
        const btn = $(input).closest('.input-group').find('button');
        if (input.type === 'password') {
            input.type = 'text';
            btn.text('隐藏');
        } else {
            input.type = 'password';
            btn.text('显示');
        }
    }

    function collectPayload() {
        const payload = {};
        $('.config-field.changed').each(function () {
            const group = $(this).data('group');
            const key = $(this).data('key');
            let val = $(this).val();
            if (!payload[group]) payload[group] = {};
            payload[group][key] = val;
        });
        return payload;
    }

    function saveConfig() {
        const payload = collectPayload();
        if (Object.keys(payload).length === 0) return;

        $('#btn-save').prop('disabled', true);
        $.ajax({
            url: '/home/envConfig/save',
            type: 'POST',
            contentType: 'application/json',
            data: JSON.stringify(payload),
            success: function (res) {
                if (res.success) {
                    $('#save-result').text('保存成功！配置已即时生效').removeClass('err').addClass('ok');
                    loadConfig();
                } else {
                    $('#save-result').text('保存失败: ' + (res.error || '')).removeClass('ok').addClass('err');
                    $('#btn-save').prop('disabled', false);
                }
            },
            error: function (xhr) {
                $('#save-result').text('请求失败: ' + xhr.statusText).removeClass('ok').addClass('err');
                $('#btn-save').prop('disabled', false);
            }
        });
    }

    function resetAll() {
        if (!confirm('确认还原全部配置到上次加载的值？')) return;
        renderConfig(originalConfig);
        updateChangeCount();
        $('#save-result').text('');
    }

    function escapeAttr(text) {
        if (text == null) return '';
        return String(text).replace(/&/g, '&amp;').replace(/"/g, '&quot;').replace(/</g, '&lt;').replace(/>/g, '&gt;');
    }

    $(function () {
        loadConfig();
    });
</script>
</body>
</html>
