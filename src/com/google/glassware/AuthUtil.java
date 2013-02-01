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

import com.google.api.client.auth.oauth2.AuthorizationCodeFlow;
import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.appengine.auth.oauth2.AppEngineCredentialStore;
import com.google.api.client.extensions.appengine.http.UrlFetchTransport;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.json.jackson2.JacksonFactory;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.Collections;

public class AuthUtil {
  public static final String CLIENT_ID = "YOUR_CLIENT_ID";
  public static final String CLIENT_SECRET = "YOUR_CLIENT_SECRET";

  public static final String GLASS_SCOPE =
      "https://www.googleapis.com/auth/glass.timeline " +
      "https://www.googleapis.com/auth/glass.location " +
      "https://www.googleapis.com/auth/userinfo.profile";

  /**
   * Creates and returns a new {@link AuthorizationCodeFlow} for this app.
   */
  public static AuthorizationCodeFlow newAuthorizationCodeFlow() {
    return new GoogleAuthorizationCodeFlow.Builder(
        new UrlFetchTransport(), new JacksonFactory(), CLIENT_ID, CLIENT_SECRET,
        Collections.singleton(GLASS_SCOPE))
        .setAccessType("offline")
        .setCredentialStore(new AppEngineCredentialStore())
        .build();
  }

  /**
   * Get the current user's ID from the session
   * @param request
   * @return string user id or null if no one is logged in
   */
  public static String getUserId(HttpServletRequest request) {
    HttpSession session = request.getSession();
    return (String)session.getAttribute("userId");
  }

  public static void setUserId(HttpServletRequest request, String userId) {
    HttpSession session = request.getSession();
    session.setAttribute("userId", userId);
  }

  public static void clearUserId(HttpServletRequest request) {
    request.getSession().removeAttribute("userId");
  }

  public static Credential getCredential(String userId) throws IOException {
    if(userId == null) {
      return null;
    } else {
      return AuthUtil.newAuthorizationCodeFlow().loadCredential(userId);
    }
  }

  public static Credential getCredential(HttpServletRequest req) throws IOException {
    return AuthUtil.newAuthorizationCodeFlow().loadCredential(getUserId(req));
  }
}
