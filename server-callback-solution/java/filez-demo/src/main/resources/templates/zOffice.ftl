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
            <div id="root">
                <iframe allowfullscreen="true" allow="fullscreen *;microphone *;camera *;midi *;encrypted-media *;clipboard-read *;clipboard-write *" src="${url}"></div>
            </div>
        </main>
    </div>
</div>

<script src="/static/jquery-3.7.0.min.js"></script>
</body>
</html>
