<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <title>filez-demo 主页</title>
    <link rel="icon" href="/img/D.svg">
    <link rel="stylesheet" href="/css/home.css">
    <link href="/static/bootstrap.min.css" rel="stylesheet">
    <#if !loginUrl??>
        <link href="/css/dashboard.css" rel="stylesheet">
    </#if>
</head>

<body>

<#-- 头部导航条 -->
<nav class="navbar navbar-expand-sm sticky-top navbar-light bg-light">
    <a class="navbar-brand" href="/">filez-demo 主页</a>
    <button class="navbar-toggler" type="button" data-toggle="collapse" data-target="#navbarsExample03" aria-controls="navbarsExample03"
            aria-expanded="false" aria-label="Toggle navigation">
        <span class="navbar-toggler-icon"></span>
    </button>

    <div class="collapse navbar-collapse" id="navbarsExample03">
        <ul class="navbar-nav mr-auto"></ul>
        <ul class="navbar-nav px-3">
            <#if loginUrl??>
                <li class="nav-item text-nowrap navbar-brand">
                    <a class="nav-link" href="${loginUrl}">登录</a>
                </li>
            <#else>
                <li class="nav-item text-nowrap">
                    <a class="nav-link" onclick="setIframeSrc('/home/user')">
                        <span data-feather="user"></span>
                    </a>
                </li>
                <li class="nav-item text-nowrap">
                    <a class="nav-link" href="/logout">
                        <span data-feather="log-out"></span>
                    </a>
                </li>
            </#if>
        </ul>
    </div>
</nav>

<script>
function toggleSidebar() {
  var container = document.querySelector('.container-fluid .row');
  var expandBtn = document.getElementById('sidebar-expand-btn');
  var isCollapsed = container.classList.toggle('sidebar-collapsed');
  expandBtn.style.display = isCollapsed ? 'block' : 'none';
  if (typeof feather !== 'undefined') feather.replace();
  try { localStorage.setItem('filez-demo-sidebar-collapsed', isCollapsed ? '1' : '0'); } catch(e) {}
}
</script>

<div class="container-fluid">
    <div class="row">

        <#-- 侧边栏 -->
        <nav id="sidenav" class="col-md-3 col-lg-2 d-md-block bg-light sidebar collapse">
            <div class="sidebar-sticky pt-3">
                <div class="px-3 mt-3 mb-2 d-flex justify-content-between align-items-center">
                    <h2 class="sidebar-heading mb-0 text-muted">仓库列表</h2>
                    <button class="btn btn-sm btn-outline-secondary" id="sidebar-toggle" onclick="toggleSidebar()" title="收起菜单" style="padding: 2px 6px; line-height: 1;">
                        <span data-feather="chevrons-left" style="width:14px;height:14px;"></span>
                    </button>
                </div>
                <ul id="drive-list" class="nav flex-column mb-2"></ul>
            </div>
        </nav>

        <#-- 收起状态下的展开按钮 -->
        <div id="sidebar-expand-btn" class="sidebar-expand-btn" style="display:none;" onclick="toggleSidebar()" title="展开菜单">
            <span data-feather="menu"></span>
        </div>

        <#--  主内容区 -->
        <main role="main" class="col-md-9 ml-sm-auto col-lg-10 px-md-3">
            <div id="zOfficeDoc"></div>
            <div id="root">
                <div class="iframeDiv">
                    <iframe id="integration-frame" src="${frameUrl!''}"></iframe>
                </div>
                <div class="loadingDivContent" id="loading-div">
                    <div class="spinner-border text-primary loading-div" role="status">
                        <span class="sr-only">Loading...</span>
                    </div>
                </div>
            </div>
        </main>
    </div>
</div>

<script src="/static/clipboard.min.js"></script>
<script src="/static/jquery-3.7.0.min.js"></script>
<script src="/static/bootstrap.bundle.min.js"></script>
<script src="/static/feather-icon.min.js"></script>
<script src="/js/home.js"></script>
</body>
</html>
