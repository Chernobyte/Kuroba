/*
 * Clover - 4chan browser https://github.com/Floens/Clover/
 * Copyright (C) 2014  Floens
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.github.adamantcheese.chan.core.net;

import android.util.JsonReader;

import com.android.volley.Response;
import com.android.volley.VolleyError;

import com.adamantcheese.github.chan.BuildConfig;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import okhttp3.HttpUrl;

public class UpdateApiRequest extends JsonReaderRequest<UpdateApiRequest.UpdateApiResponse> {
    public UpdateApiRequest(Response.Listener<UpdateApiResponse> listener,
                            Response.ErrorListener errorListener) {
        super(BuildConfig.UPDATE_API_ENDPOINT, listener, errorListener);
    }

    @Override
    public UpdateApiResponse readJson(JsonReader reader) throws Exception {
        UpdateApiResponse response = new UpdateApiResponse();
        reader.beginObject();
        while (reader.hasNext()) {
            switch (reader.nextName()) {
                case "tag_name":
                    try {
                        String version = reader.nextString();
                        Pattern versionPattern = Pattern.compile("v(\\d+?)\\.(\\d+?)\\.(\\d+?)");
                        Matcher versionMatcher = versionPattern.matcher(version);
                        if (versionMatcher.matches()) {
                            response.versionCode = Integer.parseInt(versionMatcher.group(1))
                                    + (Integer.parseInt(versionMatcher.group(2)) * 100)
                                    + (Integer.parseInt(versionMatcher.group(3)) * 10000);
                        }
                    } catch (Exception e) {
                        throw new VolleyError("Tag name wasn't of the form v(major).(minor).(patch)!");
                    }
                    break;
                case "assets":
                    try {
                        reader.beginArray();
                        while (reader.hasNext()) {
                            if (response.apkURL == null) {
                                reader.beginObject();
                                while (reader.hasNext()) {
                                    if ("browser_download_url".equals(reader.nextName())) {
                                        response.apkURL = HttpUrl.parse(reader.nextString());
                                    } else {
                                        reader.skipValue();
                                    }
                                }
                                reader.endObject();
                            } else {
                                reader.skipValue();
                            }
                        }
                    } catch (Exception e) {
                        throw new VolleyError("No APK URL!");
                    }
                    reader.endArray();
                    break;
                case "body":
                    response.body = reader.nextString().replace("\r\n", "<br>");
                    break;
                default:
                    reader.skipValue();
                    break;
            }
        }
        reader.endObject();
        if (response.versionCode == 0
                || response.apkURL == null
                || response.body == null) {
            throw new VolleyError("Update API response is incomplete, issue with github release listing!");
        }
        return response;
    }

    public static class UpdateApiResponse {
        public int versionCode;
        public HttpUrl apkURL;
        public String body;
    }
}