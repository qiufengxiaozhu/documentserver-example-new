<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <title>filez Preview Page</title>
    <style>
        iframe { top: 0; left: 0; width: 100%; height: 98vh; border: 0 }
    </style>
    <link href="/static/bootstrap.min.css" rel="stylesheet">
</head>
<body>
<div class="container-fluid">
    <div class="row">
        <main role="main" class="col-md-12 col-lg-12">
<#--            <#if action == 'edit'>-->
<#--                <button type="button" class="btn btn-light" onclick="saveAsPDF()">Save as PDF</button>-->
<#--            </#if>-->
            <div id="root">
                <#-- Edit and preview using iframe -->
                <form id="officeForm" method="get" target="integration-frame" action="${urlBase}">
                    <input type="hidden" name="repoId" value="${repoId}"/>
                    <input type="hidden" name="action" value="${action}"/>
                    <input type="hidden" name="docId" value="${docId}"/>
                    <#if userinfo??>
                        <input type="hidden" name="userinfo" value="${userinfo}"/>
                    </#if>
                    <#if meta??>
                        <input type="hidden" name="meta" value="${meta}"/>
                    </#if>
                    <#if downloadUrl??>
                        <input type="hidden" name="downloadUrl" value="${downloadUrl}"/>
                    </#if>
                    <#if uploadUrl??>
                        <input type="hidden" name="uploadUrl" value="${uploadUrl}"/>
                    </#if>
                    <input type="hidden" name="params" value="${params}"/>
                    <input type="hidden" name="ts" value="${ts}"/>
                    <input type="hidden" name="HMAC" value="${HMAC}"/>
                </form>
                <div id="doc1"></div>
            </div>
        </main>
    </div>
</div>

<script src="/static/jquery-3.7.0.min.js"></script>
<script src="/static/sdk.js"></script>
<script>

    var frameholder = document.getElementById('doc1');
    var office_frame = document.createElement('iframe');
    office_frame.name = 'integration-frame';
    office_frame.id = 'integration-frame';

    // The title should be set for accessibility
    office_frame.title = 'integration-frame';

    // This attribute allows true fullscreen mode in slideshow view
    // when using PowerPoint's 'view' action.
    office_frame.setAttribute('allowfullscreen', 'true');

    // The sandbox attribute is needed to allow automatic redirection to the O365 sign-in page in the business user flow
    office_frame.setAttribute('sandbox', 'allow-scripts allow-same-origin allow-forms allow-popups allow-top-navigation allow-popups-to-escape-sandbox');
    frameholder.appendChild(office_frame);

    const form = document.getElementById("officeForm");
    form.submit();

    // Set src attribute after form submission (add a small delay to ensure form submission is complete)
    setTimeout(function () {
        // Add src attribute to iframe, otherwise SDK will throw exceptions
        const params = new URLSearchParams(new FormData(form)).toString();
        office_frame.src = form.getAttribute('action') + "?" + params;
    }, 1000);


    /**
     * Save as PDF
     */
    async function saveAsPDF() {
        const Application = await ZOfficeSDK.connect('#root', true);
        await Application.ready();
        Application.ActiveDocument.saveAs('pdf');
    }
</script>
</body>
</html>
