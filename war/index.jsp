<!--
Copyright (C) 2012 Google Inc.

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
-->
<%@ page import="com.google.api.client.auth.oauth2.Credential" %>
<%@ page import="com.google.api.services.glass.model.Entity" %>
<%@ page import="com.google.glassware.GlassClient" %>
<%@ page import="com.google.glassware.WebUtil" %>
<%@ page
    import="java.util.List" %>
<%@ page import="com.google.api.services.glass.model.TimelineItem" %>
<%@ page import="com.google.api.services.glass.model.Subscription" %>
<%@ page import="com.google.api.services.glass.model.Attachment" %>

<%@ page contentType="text/html;charset=UTF-8" language="java" %>

<!doctype html>
<%
  String userId = com.google.glassware.AuthUtil.getUserId(request);
  String appBaseUrl = WebUtil.buildUrl(request, "/");

  Credential credential = com.google.glassware.AuthUtil.getCredential(userId);
  List<Entity> shareTargets = GlassClient.listSharetargets(credential).getItems();
  List<TimelineItem> timelineItems = GlassClient.listItems(credential,5L).getItems();
  List<Subscription> subscriptions = GlassClient.listSubscriptions(credential).getItems();

%>
<html>
<head>
  <div class="sign-out"><form method="post" action="/signout"><button type="submit">Sign out</button></form></div>
  <title>Glassware Starter Project</title>
  <link rel="stylesheet" href="static/screen.css"/>
</head>
<body>
<header>
  <h1>Faces and Names</h1>
  <!-- button>Connect!</button  -->

  If you haven't already selected "Faces and Names" as a Share Target,
  please <a href="https://glass.sandbox.google.com/glass/fe/services">do that now.</a>
</header>
</body>
