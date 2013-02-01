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
  <h1>Glassware Starter Project</h1>
  <p>java edition</p>
</header>

<% String flash = WebUtil.getClearFlash(request);
  if (flash != null) { %>
<p class="message">Message: <%= flash %></p>
<% } %>

<section id="timeline">
  <h2>Timeline</h2>
  <div class="do">
    <form action="<%= WebUtil.buildUrl(request, "/main") %>" method="post">
      <input type="hidden" name="operation" value="insertItem">
      <textarea name="message">Hello World!</textarea><br/>
      <button type="submit">The above message</button>
    </form>

    <form action="<%= WebUtil.buildUrl(request, "/main") %>" method="post">
      <input type="hidden" name="operation" value="insertItem">
      <input type="hidden" name="message" value="A solar eclipse of Saturn. Earth is also in this photo. Can you find it?">
      <input type="hidden" name="imageUrl" value="<%= appBaseUrl + "static/example-data/saturn-eclipse.jpg" %>">
      <input type="hidden" name="contentType" value="image/jpeg">

      <button type="submit">A picture <img class="button-icon" src="<%= appBaseUrl + "static/example-data/saturn-eclipse.jpg" %>"></button>
    </form>
    <form action="<%= WebUtil.buildUrl(request, "/main") %>" method="post">
      <input type="hidden" name="operation" value="insertItem">
      <input type="hidden" name="message" value="nyan nyan nyan nyan nyan nyan nyan nyan nyan nyan nyan nyan">
      <input type="hidden" name="imageUrl" value="<%= appBaseUrl + "static/example-data/nyan.gif" %>">
      <input type="hidden" name="contentType" value="image/gif">

      <button type="submit">Nyan Cat <img class="button-icon" src="<%= appBaseUrl + "static/example-data/nyan.gif" %>"></button>
    </form>
    <form action="<%= WebUtil.buildUrl(request, "/main") %>" method="post">
      <input type="hidden" name="operation" value="insertItem">
      <input type="hidden" name="message" value="It's a piano!">
      <input type="hidden" name="imageUrl" value="<%= appBaseUrl + "static/example-data/piano.mp3" %>">
      <input type="hidden" name="contentType" value="audio/mp3">

      <button type="submit">Piano sound</button>
    </form>


  </div>
  <table class="see">
    <thead>
    <tr>
      <th>ID</th>
      <th>Message</th>
      <th>Attachments</th>
    </tr>
    </thead>
    <tbody>
    <% if (timelineItems != null) {
      for (TimelineItem timelineItem : timelineItems) { %>
    <tr>
      <td class="id"><%= timelineItem.getId() %></td>
      <td><%= timelineItem.getText() != null ? timelineItem.getText() : "" %></td>
      <td>
        <% if(timelineItem.getAttachments() != null) {
          for(Attachment attachment : timelineItem.getAttachments()) {
            if(GlassClient.getAttachmentContentType(credential, timelineItem.getId(), attachment.getId()).startsWith("image")) { %>
        <img src="<%= appBaseUrl + "attachmentproxy?attachment=" +
            attachment.getId() + "&timelineItem=" + timelineItem.getId() %>">
        <%  } else { %>
        <a href="<%= appBaseUrl + "attachmentproxy?attachment=" +
            attachment.getId() + "&timelineItem=" + timelineItem.getId() %>">Download</a>
        <%  }
          }
        } %>
      </td>
    </tr>
    <% }
    } %>
    </tbody>
  </table>
  <div style="clear: both"></div>
</section>
<section id="actions">
  <h2>Actions</h2>
  <div class="do">
    <form action="<%= WebUtil.buildUrl(request, "/main") %>" method="post">
      <input type="hidden" name="operation" value="insertItemWithAction">
      <button type="submit">Insert item with actions</button>
    </form>
  </div>
  <div style="clear: both"></div>
</section>
<section id="share-targets">
  <h2>Share Targets</h2>
  <p>After inserting, go to <a href="http://go/glassware">go/glassware</a> to enable them.</p>
  <div class="do">
    <form action="<%= WebUtil.buildUrl(request, "/main") %>" method="post">
      <input type="hidden" name="operation" value="insertShareTarget">
      <input type="hidden" name="iconUrl" value="<%= appBaseUrl + "static/icons/search.png" %>">
      <input type="hidden" name="name" value="Search">
      <button type="submit">Search <img class="share-target-icon" src="<%= appBaseUrl + "static/icons/search.png" %>"/></button>
    </form>

    <form action="<%= WebUtil.buildUrl(request, "/main") %>" method="post">
      <input type="hidden" name="operation" value="insertShareTarget">
      <input type="hidden" name="iconUrl" value="<%= appBaseUrl + "static/icons/file.png" %>">
      <input type="hidden" name="name" value="File">
      <button type="submit">File <img class="share-target-icon" src="<%= appBaseUrl + "static/icons/file.png" %>"/></button>
    </form>

    <form action="<%= WebUtil.buildUrl(request, "/main") %>" method="post">
      <input type="hidden" name="operation" value="insertShareTarget">
      <input type="hidden" name="iconUrl" value="<%= appBaseUrl + "static/icons/drill.png" %>">
      <input type="hidden" name="name" value="Drill">
      <button type="submit">Drill <img class="share-target-icon" src="<%= appBaseUrl + "static/icons/drill.png" %>"/></button>
    </form>

  </div>
  <table class="see">
    <thead>
    <tr>
      <th>ID</th>
      <th>Display Name</th>
      <th>Icon</th>
      <th>Delete</th>
    </tr>
    </thead>
    <tbody>
    <% if (shareTargets != null) {
      for (Entity shareTarget : shareTargets) { %>
    <tr>
      <td><%= shareTarget.getId() %>
      </td>
      <td><%= shareTarget.getDisplayName() %>
      </td>
      <td><img class="share-target-icon" src="<%= shareTarget.getImageUrls() == null ? appBaseUrl + "/static/icons/failed.png" : shareTarget.getImageUrls().get(0) %>"></td>
      <td>
        <form action="<%= WebUtil.buildUrl(request, "/main") %>" method="post">
          <input type="hidden" name="id" value="<%= shareTarget.getId() %>">
          <input type="hidden" name="operation" value="deleteShareTarget">
          <button type="submit" class="delete">X</button>
        </form>
      </td>
    </tr>
    <% }
    } %>
    </tbody>
  </table>
  <div style="clear: both"></div>

</section>
<section id="notifications">
  <h2>Notifications</h2>
  <p>Once subscribed, sharing photos will echo them back to your Glass device.</p>
  <p>Note: this will only work when deployed to AppEngine. It will not work on localhost.</p>
  <div class="do">
    <form action="<%= WebUtil.buildUrl(request, "/main") %>" method="post">
      <input type="hidden" name="operation" value="insertSubscription">
      <input type="hidden" name="collection" value="timeline">
      <button type="submit">Subscribe to timeline item updates</button>
    </form>
    <form action="<%= WebUtil.buildUrl(request, "/main") %>" method="post">
      <input type="hidden" name="operation" value="insertSubscription">
      <input type="hidden" name="collection" value="locations">
      <button type="submit">Subscribe to location updates</button>
    </form>
  </div>

  <table class="see">
    <thead>
    <tr>
      <th>ID</th>
      <th>Callback URL</th>
      <th>Delete</th>
    </tr>
    </thead>
    <tbody>
    <% if(subscriptions != null) {
      for(Subscription subscription : subscriptions) { %>
    <tr>
      <td><%= subscription.getId() %></td>
      <td><%= subscription.getCallbackUrl() %></td>
      <td>
        <form action="<%= WebUtil.buildUrl(request, "/main") %>" method="post">
          <input type="hidden" name="subscriptionId" value="<%= subscription.getId() %>">
          <input type="hidden" name="operation" value="deleteSubscription">
          <button type="submit" class="delete">X</button>
        </form>
      </td>
    </tr>
    <% }
    } %>
    </tbody>
  </table>
  <div style="clear: both"></div>

</section>

</body>
</html>
