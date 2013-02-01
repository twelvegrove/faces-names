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
import com.google.api.client.extensions.appengine.http.UrlFetchTransport;
import com.google.api.client.http.ByteArrayContent;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.glass.Glass;
import com.google.api.services.glass.model.*;
import com.google.common.io.ByteStreams;

import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Logger;

/**
 * A facade for easier access to basic API operations
 */
public class GlassClient {
  private static final Logger LOG = Logger.getLogger(GlassClient.class.getSimpleName());

  public static Glass getGlass(Credential credential) {
    return new Glass.Builder(new UrlFetchTransport(), new JacksonFactory(), credential)
        .setApplicationName("PG Java Starter").build();
  }

  public static void insertShareTarget(Credential credential, Entity shareTarget) throws IOException {
    Glass.ShareTargets shareTargets = getGlass(credential).shareTargets();
    shareTargets.insert(shareTarget).execute();
  }

  public static void deleteShareTarget(Credential credential, String shareTargetId) throws IOException {
    Glass.ShareTargets shareTargets = getGlass(credential).shareTargets();
    shareTargets.delete(shareTargetId).execute();
  }

  public static ShareTargetsListResponse listSharetargets(Credential credential) throws IOException {
    Glass.ShareTargets shareTargets = getGlass(credential).shareTargets();
    return shareTargets.list().execute();
  }

  public static TimelineListResponse listItems(Credential credential, long count) throws IOException {
    Glass.Timeline timelineItems = getGlass(credential).timeline();
    Glass.Timeline.List list = timelineItems.list();
    list.setMaxResults(count);
    return list.execute();
  }


  /**
   * Subscribes to notifications on the user's timeline.
   */
  public static void insertSubscription(Credential credential, String callbackUrl, String userId, String collection)
      throws IOException {
    LOG.info("Attempting to subscribe verify_token " + userId + " with callback " + callbackUrl);

    // Rewrite "appspot.com" to "Appspot.com" as a workaround for http://b/6909300.
    callbackUrl = callbackUrl.replace("appspot.com", "Appspot.com");

    Subscription subscription = new Subscription();
    // Alternatively, subscribe to "locations"
    subscription.setCollection(collection);
    subscription.setCallbackUrl(callbackUrl);
    subscription.setUserToken(userId);

    getGlass(credential).subscriptions().insert(subscription).execute();

  }

  /**
   * Subscribes to notifications on the user's timeline.
   */
  public static void deleteSubscription(Credential credential, String id)
      throws IOException {
    getGlass(credential).subscriptions().delete(id).execute();
  }

  public static SubscriptionsListResponse listSubscriptions(Credential credential) throws IOException {
    Glass.Subscriptions subscriptions = getGlass(credential).subscriptions();
    return subscriptions.list().execute();
  }

  /**
   * Inserts a simple timeline item.
   *
   * @param credential the user's credential
   * @param item       the item to insert
   */
  public static void insertTimelineItem(Credential credential, TimelineItem item) throws IOException {
    getGlass(credential).timeline().insert(item).execute();
  }

  /**
   * Inserts an item with an attachment provided as a byte array.
   *
   * @param credential            the user's credential
   * @param item                  the item to insert
   * @param attachmentContentType the MIME type of the attachment (or null if none)
   * @param attachmentData        data for the attachment (or null if none)
   */
  public static void insertTimelineItem(Credential credential, TimelineItem item,
                                        String attachmentContentType, byte[] attachmentData) throws IOException {
    Glass.Timeline timeline = getGlass(credential).timeline();
    timeline.insert(item, new ByteArrayContent(attachmentContentType, attachmentData)).execute();

  }

  /**
   * Inserts an item with an attachment provided as an input stream.
   *
   * @param credential            the user's credential
   * @param item                  the item to insert
   * @param attachmentContentType the MIME type of the attachment (or null if none)
   * @param attachmentInputStream input stream for the attachment (or null if none)
   */
  public static void insertTimelineItem(Credential credential, TimelineItem item,
                                        String attachmentContentType, InputStream attachmentInputStream) throws IOException {
    insertTimelineItem(credential, item, attachmentContentType, ByteStreams.toByteArray(attachmentInputStream));
  }

  public static InputStream getAttachmentInputStream(Credential credential, String timelineItemId, String attachmentId) throws IOException {
    Glass.Attachments attachments = getGlass(credential).attachments();
    return attachments.get(timelineItemId, attachmentId).executeAsInputStream();
  }

  public static String getAttachmentContentType(Credential credential, String timelineItemId, String attachmentId) throws IOException {
    TimelineItem timelineItem = getGlass(credential).timeline().get(timelineItemId).execute();
    for (Attachment attachment : timelineItem.getAttachments()) {
      if(attachment != null && attachment.getId() != null &&
          attachment.getId().equals(attachmentId)) {
        return attachment.getContentType();
      }
    }
    return "unknown";
  }
}
