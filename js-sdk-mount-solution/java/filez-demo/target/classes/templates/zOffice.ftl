<!DOCTYPE html>
<html lang="en">
<head>
<meta charset="UTF-8">
<title>filez Page</title>
<script type="text/javascript" src="/static/sdk.js"></script>
<style type="text/css">
    body {
        margin: 0;
        padding: 0;
        overflow: hidden;
        -ms-content-zooming: none;
    }
    #frameholder {
        width: 100%;
        height: 100vh;
        margin: 0;
        border: none;
    }
</style>
</head>
<body style="height: 100%; margin: 0;">
    <div id="frameholder"></div>
<script type="text/javascript">

ZOfficeSDK.mount(${config}, '#frameholder');

</script>
</body>
</html>