<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1, shrink-to-fit=no">
    <title>filez-demo Login</title>
    <link rel="icon" href="/img/D.svg">
    <link href="/static/bootstrap.min.css" rel="stylesheet">
    <link href="/css/login.css" rel="stylesheet">
</head>
<body class="text-center">
<form id="login-form" class="form-signin" action="/login" method="post">
    <img class="mb-4" src="/img/D.svg" alt="" width="72" height="72">
    <h1 class="h3 mb-3 font-weight-normal">filez-demo</h1>
    <label for="inputUsername" class="sr-only">Username</label>
    <input type="text" id="inputUsername" class="form-control" name="username" placeholder="Username" required autofocus
           value="${username}">
    <label for="inputPassword" class="sr-only">Password</label>
    <input type="password" id="inputPassword" class="form-control" name="password" placeholder="Password" required
           value="${pwd}">
    <button class="btn btn-lg btn-primary btn-block" type="submit">Login</button>
</form>
<script src="/static/jquery-3.7.0.min.js"></script>
</body>
</html>
