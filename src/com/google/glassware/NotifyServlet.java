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

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.api.client.auth.oauth2.Credential;
import com.google.api.services.glass.Glass;
import com.google.api.services.glass.model.Attachment;
import com.google.api.services.glass.model.Entity;
import com.google.api.services.glass.model.Location;
import com.google.api.services.glass.model.MenuItem;
import com.google.api.services.glass.model.MenuValue;
import com.google.api.services.glass.model.Notification;
import com.google.api.services.glass.model.NotificationConfig;
import com.google.api.services.glass.model.TimelineItem;
import com.google.common.collect.Lists;

/**
 * Handles the PubSub verification GET step and receives notifications
 *
 * @author mimming
 */
public class NotifyServlet extends HttpServlet {
  private static final Logger LOG = Logger.getLogger(MainServlet.class.getSimpleName());
  private static final ObjectMapper MAPPER = new ObjectMapper();
private Entity myCreator;

  @Override
  protected void doPost(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException {
    // Respond with OK and status 200 in a timely fashion to prevent redelivery
    response.setContentType("text/html");
    Writer writer = response.getWriter();
    writer.append("OK");
    writer.close();

    // Get the notification object from the request body (into a string so we can log it)
    BufferedReader notificationReader = new BufferedReader(new InputStreamReader(request.getInputStream()));
    String notificationString = "";
    while(notificationReader.ready()) {
      notificationString += notificationReader.readLine();
    }

    LOG.info("got raw notification " + notificationString);

    Notification notification = MAPPER.readValue(notificationString, Notification.class);

    LOG.info("Got a notification with ID: " + notification.getItemId());

    // Figure out the impacted user and get their credentials for API calls
    String userId = notification.getUserToken();
    Credential credential = AuthUtil.getCredential(userId);
    Glass glassClient = GlassClient.getGlass(credential);
    
    // Our custom card template.
    String cardTemplate = "<article> <section> <span style='position:relative;display:block;height:170px;overflow:hidden;'>" +
    		"<img style='position:absolute;top:-100px;' src='%s' width='100%%'></span>" + // image source URL
    		"<table class='text-small align-justify'> <tbody><tr>" +
    		"<td>%s</td><td>%s</td></tr></tbody></table></section></article>"; // provided name, provided company
    
    // Default image for now.
    String savedURL = "https://lh5.googleusercontent.com/--bS5I_Xf5i4/UQ0sxuqpVYI/AAAAAAAAAEQ/JCxqd1CTfGo/s754/20130202_063613_960.jpg";


    if(notification.getCollection().equals("locations")) {
      LOG.info("Notification of updated location");
      Glass glass = GlassClient.getGlass(credential);
      Location location = glass.locations().get(notification.getItemId()).execute(); // usually 'latest'

      LOG.info("New locatin is " + location.getLatitude() + ", " + location.getLongitude());
      GlassClient.insertTimelineItem(credential,
          new TimelineItem().setText("You are now at " +
              location.getLatitude() + ", " + location.getLongitude()).setNotification(
              new NotificationConfig().setLevel("audio_only"))
              .setLocation(location)
              .setMenuItems(Lists.newArrayList(new MenuItem().setAction("NAVIGATE_TO"))));

      // This is a location notification. Ping the device with a timeline item telling them where they are.
    } else if(notification.getCollection().equals("timeline")) {
      // Get the impacted timeline item
      TimelineItem timelineItem = glassClient.timeline().get(notification.getItemId()).execute();
      LOG.info("Notification impacted timeline item with ID: " + timelineItem.getId());

      // Get the first attachment on that timeline item and do stuff with it
      String attachmentId = null;
      List<Attachment> attachments = timelineItem.getAttachments();
      
      // Get the replyTo if it exists.
      String replyTo = timelineItem.getInReplyTo();
      
      
      if(replyTo != null) {
    	  // Update the replyTo Timeline item with the text (for now).
    	  TimelineItem updatedTimelineItem = glassClient.timeline().get(replyTo).execute();
    	  updatedTimelineItem.setHtml(
          		String.format(cardTemplate,savedURL,"New Name "+ System.currentTimeMillis(),"Cyberdyne Systems"));
          glassClient.timeline().update(replyTo, updatedTimelineItem);
          
          return;
      }
      
      if(attachments != null && attachments.size() > 0) {
        // Get the first attachment 
    	  //TODO Note: replyto attachements don't have IDs!
        attachmentId = attachments.get(0).getId();
        LOG.info("Found attachment with ID " + attachmentId);

        // Get the attachment content
        InputStream stream = GlassClient.getAttachmentInputStream(credential, timelineItem.getId(), attachmentId);

        // Create a new timeline item with the attachment
//        GlassClient.insertTimelineItem(credential,
//            new TimelineItem().setText("Echoing your shared item").setNotification(
//                new NotificationConfig().setLevel("audio_only")),
//            "image/jpeg", stream);


        
        //Create a new timeline Item.
        TimelineItem replyTimelineItem = new TimelineItem();

        // Triggers an audible tone when the timeline item is received
        replyTimelineItem.setNotification(new NotificationConfig().setLevel("audio_only"));
        replyTimelineItem.setHtml(
        		String.format(cardTemplate,savedURL,"John Connor","Cyberdyne Systems"));

        // add the menu item actions
        replyTimelineItem.setMenuItems(createMenuItems(getCreator(replyTimelineItem,credential)));
        GlassClient.insertTimelineItem(credential, replyTimelineItem);
        
      } else {
        LOG.warning("timeline item " + timelineItem.getId() + " has no attachments");
      }
    }
  }
  
  private Entity getCreator(TimelineItem timelineItem, Credential credential) {
      List<Entity> shareTargets;
      try {
          shareTargets = GlassClient.listSharetargets(credential).getItems();
      } catch (IOException e) {
          return null;
      }
      if (shareTargets == null) {
          return null;
      }

      for (Entity entity : shareTargets) {
          System.out.println("\n\nAn shareTarget entity is: " + entity);
          if ("facesandnames".equals(entity.getId())) {
              System.out.println("Found our creator: " + entity.getId());
              timelineItem.setCreator(entity);
              myCreator = entity;
              return entity;
          }
      }
      return null;
  }
  
  private List<MenuItem> createMenuItems(Entity entity) {
      List<MenuItem> menuItemList = new ArrayList<MenuItem>();

      // Built in actions
      menuItemList.add(new MenuItem().setAction("Reply"));
      menuItemList.add(new MenuItem().setAction("Delete"));
      
      return menuItemList;
  }

}
