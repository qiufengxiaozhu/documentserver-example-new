<!DOCTYPE html>
<#import "/spring.ftl" as spring/>
<html lang="zh-cn">
<head>
  <meta charset="UTF-8">
  <title>User Information</title>
  <link rel="icon" href="/img/D.svg">
  <link href="/static/bootstrap.min.css" rel="stylesheet">
</head>
<body>
<div style='text-align: center;margin-top: 5%'>
  <form action="/home/user" method="post">
      <@spring.bind "user"/>
      <@spring.formHiddenInput "user.id" ""/>
    <div class="form-group row">
      <label for="email" class="col-sm-2 col-form-label">Email</label>
      <div class="col-sm-9">
          <@spring.formInput "user.email" "class='form-control' readonly='readonly'" "text"/>
      </div>
    </div>
    <div class="form-group row">
      <label for="photoUrl" class="col-sm-2 col-form-label">Avatar URL</label>
      <div class="col-sm-9">
          <@spring.formInput "user.photoUrl" "class='form-control'" "text"/>
      </div>
    </div>
    <div class="form-group row">
      <label for="displayName" class="col-sm-2 col-form-label">Display Name</label>
      <div class="col-sm-9">
        <@spring.formInput "user.displayName" "class='form-control'" "text"/>
      </div>
    </div>
    <div class="form-group row">
      <label for="name" class="col-sm-2 col-form-label">Username</label>
      <div class="col-sm-9">
        <@spring.formInput "user.name" "class='form-control'" "text"/>
      </div>
    </div>
    <div class="form-group row">
        <label for="orgId" class="col-sm-2 col-form-label">Department ID</label>
      <div class="col-sm-9">
          <@spring.formInput "user.orgId" "class='form-control'" "text"/>
      </div>
    </div>
    <div class="form-group row">
      <label for="orgName" class="col-sm-2 col-form-label">Department</label>
      <div class="col-sm-9">
        <@spring.formInput "user.orgName" "class='form-control'" "text"/>
      </div>
    </div>
    <div class="form-group row">
        <label for="jobTitle" class="col-sm-2 col-form-label">Position</label>
      <div class="col-sm-9">
          <@spring.formInput "user.jobTitle" "class='form-control'" "text"/>
      </div>
    </div>
    <div>
      <button type="submit" class="btn btn-primary">Save</button>
    </div>
  </form>
</div>
<script src="/static/bootstrap.bundle.min.js"></script>
</body>
</html>
