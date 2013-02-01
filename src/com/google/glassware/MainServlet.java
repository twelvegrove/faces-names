/*
 * Copyright (C) 2012 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.google.glassware;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.googleapis.json.GoogleJsonResponseException;
import com.google.api.services.glass.model.*;
import com.google.common.collect.Lists;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

public class MainServlet extends HttpServlet {
  private static final Logger LOG = Logger.getLogger(MainServlet.class.getSimpleName());

  /**
   * Do stuff when the buttons are clicked
   * @param req
   * @param res
   * @throws IOException
   */
  @Override
  protected void doPost(HttpServletRequest req, HttpServletResponse res) throws IOException {
    String userId = AuthUtil.getUserId(req);
    Credential credential = AuthUtil.newAuthorizationCodeFlow().loadCredential(userId);
    String message = "";

    if(req.getParameter("operation").equals("insertSubscription")) {

      // subscribe (only works deployed to production)
      try {
        GlassClient.insertSubscription(credential, WebUtil.buildUrl(req, "/notify"),
            userId, req.getParameter("collection"));
        message = "Application is now subscribed to updates.";
      } catch (GoogleJsonResponseException e) {
        LOG.warning("Could not subscribe " + WebUtil.buildUrl(req, "/notify") +
            " because " + e.getDetails().toPrettyString());
        message = "Failed to subscribe. Check your log for details";
      }

    } else if(req.getParameter("operation").equals("deleteSubscription")) {

      // subscribe (only works deployed to production)
      GlassClient.deleteSubscription(credential, req.getParameter("subscriptionId"));

      message = "Application has been unsubscribed.";

    } else if(req.getParameter("operation").equals("insertItem")) {
      LOG.fine("Inserting Timeline Item");
      TimelineItem timelineItem = new TimelineItem();

      if(req.getParameter("message") != null) {
        timelineItem.setText(req.getParameter("message"));
      }

      // Triggers an audible tone when the timeline item is received
      timelineItem.setNotification(new NotificationConfig().setLevel("audio_only"));

      if(req.getParameter("imageUrl") != null) {
        // Attach an image, if we have one
        URL url = new URL(req.getParameter("imageUrl"));
        String contentType = req.getParameter("contentType");
        GlassClient.insertTimelineItem(credential, timelineItem, contentType, url.openStream());
      } else {
        GlassClient.insertTimelineItem(credential, timelineItem);
      }

      message = "A timeline item has been inserted.";

    } else if(req.getParameter("operation").equals("insertItemWithAction")) {
      LOG.fine("Inserting Timeline Item");
      TimelineItem timelineItem = new TimelineItem();
      timelineItem.setText("Tell me what you had for lunch :)");

      List<MenuItem> menuItemList = new ArrayList<MenuItem>();
      // Built in actions
      menuItemList.add(new MenuItem().setAction("Reply"));
      menuItemList.add(new MenuItem().setAction("Share"));
      menuItemList.add(new MenuItem().setAction("Read_Aloud"));

      // And custom actions
      List<MenuValue> menuValues = new ArrayList<MenuValue>();
      menuValues.add(new MenuValue()
          .setIconUrl(WebUtil.buildUrl(req, "/static/icons/drill-50.png"))
          .setDisplayName("Drill In"));
      menuItemList.add(new MenuItem().setValues(menuValues).setId("drill").setAction("CUSTOM"));

      timelineItem.setMenuItems(menuItemList);
      timelineItem.setNotification(new NotificationConfig().setLevel("audio_only"));

      GlassClient.insertTimelineItem(credential, timelineItem);

      message = "A timeline item with actions has been inserted.";

    } else if (req.getParameter("operation").equals("insertShareTarget")) {
      if(req.getParameter("iconUrl") == null || req.getParameter("name") == null) {
        message = "Must specify iconUrl and name to insert share target";
      } else {
        // Insert a share target
        LOG.fine("Inserting share target Item");
        Entity shareTarget = new Entity();
        shareTarget.setId(req.getParameter("name"));
        shareTarget.setDisplayName(req.getParameter("name"));
        final HttpServletRequest requestCopy = req;
        shareTarget.setImageUrls(Lists.newArrayList(requestCopy.getParameter("iconUrl")));
        GlassClient.insertShareTarget(credential, shareTarget);

        message = "Inserted share target: " + req.getParameter("name");
      }

    } else if (req.getParameter("operation").equals("deleteShareTarget")) {

      // Insert a share target
      LOG.fine("Deleting share target Item");
      GlassClient.deleteShareTarget(credential, req.getParameter("id"));

      message = "Share target has been deleted.";

    } else {
      // Escape to prevent XSS
      String operation = req.getParameter("operation");
      message = "I don't know how to " + operation;
    }
    WebUtil.setFlash(req, message);
    res.sendRedirect(WebUtil.buildUrl(req, "/"));
  }
}
