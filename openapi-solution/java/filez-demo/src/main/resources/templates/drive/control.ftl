<!DOCTYPE html>
<#import "/spring.ftl" as spring/>
<html lang="en" xmlns="http://www.w3.org/1999/html">
<head>
    <meta charset="UTF-8">
    <title>文档元数据</title>
    <link href="/static/bootstrap.min.css" rel="stylesheet">
    <link href="/css/dashboard.css" rel="stylesheet">
    <style>
        form input {
            width: 100%;
        }

        body button {
            width: 100%;
        }

        body {
            padding-bottom: 20px;
        }

    </style>
</head>
<body>
<div class="col-8 offset-2" style="margin-top: 50px">
    <h2>文档相关配置，全局生效</h2>
    <form id="metaForm" target="_self" action="/home/meta/${meta.id}" method="post">
        <div>
            <@spring.formHiddenInput "meta.id" ""/>
            <@spring.formHiddenInput "meta.createdAt" ""/>
            <@spring.formHiddenInput "meta.modifiedAt" ""/>
            <#if meta.modifiedBy??>
                <@spring.formHiddenInput "meta.modifiedBy.id" ""/>
                <@spring.formHiddenInput "meta.modifiedBy.name" ""/>
                <@spring.formHiddenInput "meta.modifiedBy.email" ""/>
            </#if>
            <@spring.formHiddenInput "meta.permissions.write" ""/>
            <@spring.formHiddenInput "meta.permissions.read" ""/>
            <@spring.formHiddenInput "meta.permissions.download" ""/>
            <@spring.formHiddenInput "meta.size" ""/>
            <@spring.formHiddenInput "meta.version" ""/>
            <table class="table table-bordered table-hover table-condensed">
                <tr>
                    <td>文档ID</td>
                    <td>${meta.id}</td>
                </tr>
                <tr>
                    <td>修改时间</td>
                    <td>${meta.modifiedAt?datetime}</td>
                </tr>
                <tr>
                    <td>name</td>
                    <td><@spring.formInput "meta.name" "" "text"/></td>
                </tr>
                <tr>
                    <td>description</td>
                    <td><@spring.formInput "meta.description" "" "text"/></td>
                </tr>
                <tr>
                    <td>createdBy.id</td>
                    <td>
                        <@spring.bind "meta.createdBy.id"/>
                        <@spring.formInput "meta.createdBy.id" "" "text"/>
                    </td>
                </tr>
                <tr>
                    <td>createdBy.name</td>
                    <td><@spring.formInput "meta.createdBy.name" "" "text"/></td>
                </tr>
                <tr>
                    <td>createdBy.email</td>
                    <td><@spring.formInput "meta.createdBy.email" "" "text"/></td>
                </tr>
                <tr>
                    <td>owner.id</td>
                    <td><@spring.formInput "meta.owner.id" "" "text"/></td>
                </tr>
                <tr>
                    <td>owner.name</td>
                    <td><@spring.formInput "meta.owner.name" "" "text"/></td>
                </tr>
                <tr>
                    <td>owner.email</td>
                    <td><@spring.formInput "meta.owner.email" "" "text"/></td>
                </tr>
            </table>
        </div>
        <div class="col-2 offset-5">
            <button class="btn-primary btn-lg" onclick="document.getElementById('metaForm').submit()">更新</button>
        </div>
    </form>
    <h2>其他配置，和用户及文档ID绑定</h2>
    <form id="controlForm" target="_self" action="${controlUrl}" method="post">
        <table class="table table-bordered table-hover table-condensed">
            <tr>
                <td>write</td>
                <td><@spring.formInput "control.docPermission.write" "" "text"/></td>
            </tr>
            <tr>
                <td>read</td>
                <td><@spring.formInput "control.docPermission.read" "" "text"/></td>
            </tr>
            <tr>
                <td>download</td>
                <td><@spring.formInput "control.docPermission.download" "" "text"/></td>
            </tr>
            <tr>
                <td>打印</td>
                <td><@spring.formInput "control.docPermission.print" "" "text"/></td>
            </tr>
            <tr>
                <td>role</td>
                <td><@spring.formInput "control.role" "" "text"/></td>
            </tr>

            <tr>
                <td>line1</td>
                <td><@spring.formInput "control.docWaterMark.line1" "" "text"/></td>
            </tr>
            <tr>
                <td>line2</td>
                <td><@spring.formInput "control.docWaterMark.line2" "" "text"/></td>
            </tr>
            <tr>
                <td>line3</td>
                <td><@spring.formInput "control.docWaterMark.line3" "" "text"/></td>
            </tr>
            <tr>
                <td>line4</td>
                <td><@spring.formInput "control.docWaterMark.line4" "" "text"/></td>
            </tr>
            <tr>
                <td>withDate</td>
                <td><@spring.formInput "control.docWaterMark.withDate" "" "text"/></td>
            </tr>
            <tr>
                <td>fontcolor</td>
                <td><@spring.formInput "control.docWaterMark.fontcolor" "" "text"/></td>
            </tr>
            <tr>
                <td>transparent</td>
                <td><@spring.formInput "control.docWaterMark.transparent" "" "text"/></td>
            </tr>
            <tr>
                <td>rotation</td>
                <td><@spring.formInput "control.docWaterMark.rotation" "" "text"/></td>
            </tr>
            <tr>
                <td>fontsize</td>
                <td><@spring.formInput "control.docWaterMark.fontsize" "" "text"/></td>
            </tr>
            <tr>
                <td>font</td>
                <td><@spring.formInput "control.docWaterMark.font" "" "text"/></td>
            </tr>
            <tr>
                <td>spacing</td>
                <td><@spring.formInput "control.docWaterMark.spacing" "" "text"/></td>
            </tr>
            <tr>
                <td>innerSpacing</td>
                <td><@spring.formInput "control.docWaterMark.innerSpacing" "" "text"/></td>
            </tr>

            <tr>
                <td>previewWithTrackChange</td>
                <td><@spring.formInput "control.extension.previewWithTrackChange" "" "text"/></td>
            </tr>
            <tr>
                <td>trackChangeForceOn</td>
                <td><@spring.formInput "control.extension.trackChangeForceOn" "" "text"/></td>
            </tr>
        </table>
        <div class="col-2 offset-5">
            <button class="btn-primary btn-lg" onclick="document.getElementById('controlForm').submit()">
                更新
            </button>
        </div>
    </form>
</div>
</body>
</html>
