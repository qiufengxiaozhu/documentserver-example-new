<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <title>filez-demo Home</title>
    <link rel="icon" href="/img/D.svg">
    <link rel="stylesheet" href="/css/home.css">
    <link href="/static/bootstrap.min.css" rel="stylesheet">
    <#if !loginUrl??>
        <link href="/css/dashboard.css" rel="stylesheet">
    </#if>
</head>

<body>

<#-- Header navigation bar -->
<nav class="navbar navbar-expand-sm sticky-top navbar-light bg-light">
    <a class="navbar-brand" href="/">filez-demo Login</a>
    <button class="navbar-toggler" type="button" data-toggle="collapse" data-target="#navbarsExample03" aria-controls="navbarsExample03"
            aria-expanded="false" aria-label="Toggle navigation">
        <span class="navbar-toggler-icon"></span>
    </button>

    <div class="collapse navbar-collapse" id="navbarsExample03">
        <ul class="navbar-nav mr-auto"></ul>
        <ul class="navbar-nav px-3">
            <#if loginUrl??>
                <li class="nav-item text-nowrap navbar-brand">
                    <a class="nav-link" href="${loginUrl}">Login</a>
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

<div class="container-fluid">
    <div class="row">

        <#-- Sidebar -->
        <nav id="sidenav" class="col-md-3 col-lg-2 d-md-block bg-light sidebar collapse">
            <div class="sidebar-sticky pt-3">
                <h2 class="sidebar-heading d-flex justify-content-between align-items-center px-3 mt-4 mb-1 text-muted">
                    <span>Repository List</span>
                </h2>
                <ul id="drive-list" class="nav flex-column mb-2"></ul>
            </div>
        </nav>

        <#--  Main content area -->
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
