<!DOCTYPE html>
<#import "/spring.ftl" as spring/>
<html lang="zh-cn">
<head>
  <meta charset="UTF-8">
  <title>用户信息</title>
  <link rel="icon" href="/img/D.svg">
  <link href="/static/bootstrap.min.css" rel="stylesheet">
</head>
<body>
<div style='text-align: center;margin-top: 5%'>
  <form action="/home/user" method="post">
      <@spring.bind "user"/>
      <@spring.formHiddenInput "user.id" ""/>
    <div class="form-group row">
      <label for="email" class="col-sm-2 col-form-label">邮箱</label>
      <div class="col-sm-9">
          <@spring.formInput "user.email" "class='form-control' readonly='readonly'" "text"/>
      </div>
    </div>
    <div class="form-group row">
      <label for="photoUrl" class="col-sm-2 col-form-label">头像地址</label>
      <div class="col-sm-9">
          <@spring.formInput "user.photoUrl" "class='form-control'" "text"/>
      </div>
    </div>
    <div class="form-group row">
      <label for="displayName" class="col-sm-2 col-form-label">昵称</label>
      <div class="col-sm-9">
        <@spring.formInput "user.displayName" "class='form-control'" "text"/>
      </div>
    </div>
    <div class="form-group row">
      <label for="name" class="col-sm-2 col-form-label">用户名</label>
      <div class="col-sm-9">
        <@spring.formInput "user.name" "class='form-control'" "text"/>
      </div>
    </div>
    <div class="form-group row">
        <label for="orgId" class="col-sm-2 col-form-label">部门ID</label>
      <div class="col-sm-9">
          <@spring.formInput "user.orgId" "class='form-control'" "text"/>
      </div>
    </div>
    <div class="form-group row">
      <label for="orgName" class="col-sm-2 col-form-label">部门</label>
      <div class="col-sm-9">
        <@spring.formInput "user.orgName" "class='form-control'" "text"/>
      </div>
    </div>
    <div class="form-group row">
        <label for="jobTitle" class="col-sm-2 col-form-label">职位</label>
      <div class="col-sm-9">
          <@spring.formInput "user.jobTitle" "class='form-control'" "text"/>
      </div>
    </div>
    <div>
      <button type="submit" class="btn btn-primary">保存</button>
    </div>
  </form>
</div>
<script src="/static/bootstrap.bundle.min.js"></script>
</body>
</html>
