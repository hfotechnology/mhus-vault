/**
 * Copyright (C) 2020 Mike Hummel (mh@mhus.de)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.mhus.app.vault.client;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Base64;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import de.mhus.lib.core.MString;

public class VaultClientConnection {

    public static final String VALID_FROM = "_validFrom";
    public static final String VALID_TO = "_validTo";

    private String url;
    private String auth;

    private static ObjectMapper mapper = new ObjectMapper();

    public VaultClientConnection(String url, String user, String password)
            throws UnsupportedEncodingException {
        this.url = url;
        auth = Base64.getEncoder().encodeToString((user + ":" + password).getBytes("utf-8"));
    }

    /*
    {
      "validto" : 32503676400000,
      "modifydate" : 1516829960290,
      "_user" : "admin",
      "creator" : "guest",
      "secret" : "-----START SIGNATURE-----\nKeyIdent: c49ac4bb-8379-4956-a278-a426af19e829\nMethod: DSA-1\nCreated: Wed Jan 24 22:39:20 CET 2018\n\nMCwCFHR8EaJ9Zuu35TS2Tr2N5TcSnhUPAhQMBHAuj81Ot7M3lv\nwe2icUt7egLg==\n\n-----END SIGNATURE-----\n-----START CIPHER-----\nEncoding: utf-8\nKeyIdent: f62af2f1-7acc-4b2e-9d77-1ea74c656953\nMethod: RSA-1\nCreated: Wed Jan 24 22:39:20 CET 2018\n\nNyhwZJUldX3+246zNp2CNK6KWHImdqTP1nScR+omSeicLY86mX\nLb3zWVE0\/8SQU1UPyouMmuGDLW7GOrQNpt4bTsnbnQ4Aj8CZuN\neKDFp54Vlpe13TtT8pAaBxxMTAxwACzwTBUJnc6e4Zc8pIhQNY\nAW0KwoKf+G3cwzTMC974Q=\n\n-----END CIPHER-----\n",
      "secretid" : "7d693d2c-6474-4f7f-a734-7a77e906e7df",
      "target" : "test",
      "validfrom" : 1516829960129,
      "validto_" : "3000-01-01T00:00:00",
      "_timestamp" : 1517514671976,
      "creationdate_" : "2018-01-24T22:39:20",
      "secretkeyid" : "f62af2f1-7acc-4b2e-9d77-1ea74c656953",
      "creationdate" : 1516829960290,
      "vstamp" : 0,
      "_sequence" : 0,
      "group" : "test",
      "meta" : null,
      "id" : "5a68fd08520113934e98f237",
      "checksum" : "yYmoS1B\/l0Qvm3rI3HCR50+tiE2Y2gmCCPg65ejBQIU=",
      "validfrom_" : "2018-01-24T22:39:20",
      "modifydate_" : "2018-01-24T22:39:20"
    }
    	 */
    public SecretEntry getSecret(UUID secretId, String target) throws IOException {
        // open url and read content
        String u = url + "/rest/vault/" + secretId.toString() + ":" + target;
        URL url = new URL(u);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        // connection.setRequestMethod("POST");
        connection.setDoOutput(true);
        connection.setRequestProperty("Authorization", "Basic " + auth);
        connection.connect();
        int sc = connection.getResponseCode();
        if (sc != 200)
            throw new IOException("HTTP error " + sc + " " + connection.getResponseMessage());
        BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
        StringBuilder content = new StringBuilder();
        String inputLine;
        while ((inputLine = in.readLine()) != null) content.append(inputLine);
        in.close();

        // create json object
        JsonNode json = mapper.readTree(content.toString());
        if (json.has("_error"))
            throw new IOException(
                    "Rest Error: "
                            + json.get("_error").asText()
                            + " "
                            + json.get("_errorMessage").asText());

        return new SecretEntry(json);
    }

    public String createSecret(String group, Map<String, Object> parameters) throws IOException {
        return importSecret(group, null, parameters);
    }

    public String importSecret(String group, String secret, Map<String, Object> parameters)
            throws IOException {
        // open url and read content
        String u = url + "/rest/vault";
        URL url = new URL(u);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("POST");
        connection.setDoOutput(true);
        connection.setRequestProperty("Authorization", "Basic " + auth);
        List<NameValuePair> params = new ArrayList<NameValuePair>();

        if (parameters != null)
            for (Entry<String, Object> entry : parameters.entrySet())
                params.add(
                        new BasicNameValuePair(entry.getKey(), String.valueOf(entry.getValue())));
        params.add(new BasicNameValuePair("_group", group));
        if (secret != null) params.add(new BasicNameValuePair("_secret", secret));

        OutputStream os = connection.getOutputStream();
        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os, "UTF-8"));
        writer.write(getQuery(params));
        writer.flush();
        writer.close();
        os.close();

        connection.connect();
        int sc = connection.getResponseCode();
        if (sc != 200)
            throw new IOException("HTTP error " + sc + " " + connection.getResponseMessage());
        BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
        StringBuilder content = new StringBuilder();
        String inputLine;
        while ((inputLine = in.readLine()) != null) content.append(inputLine);
        in.close();

        // create json object
        JsonNode json = mapper.readTree(content.toString());
        if (json.has("_error"))
            throw new IOException(
                    "Rest Error: "
                            + json.get("_error").asText()
                            + " "
                            + json.get("_errorMessage").asText());

        return json.get("secretId").asText();
    }

    public void createUpdate(String secretId, Map<String, Object> parameters) throws IOException {
        importUpdate(secretId, null, parameters);
    }

    public void importUpdate(String secretId, String secret, Map<String, Object> parameters)
            throws IOException {
        // open url and read content
        String u = url + "/rest/vault";
        URL url = new URL(u);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("PUT");
        connection.setDoOutput(true);
        connection.setRequestProperty("Authorization", "Basic " + auth);
        List<NameValuePair> params = new ArrayList<NameValuePair>();

        if (parameters != null)
            for (Entry<String, Object> entry : parameters.entrySet())
                params.add(
                        new BasicNameValuePair(entry.getKey(), String.valueOf(entry.getValue())));
        if (secret != null) params.add(new BasicNameValuePair("_secret", secret));
        params.add(new BasicNameValuePair("_secretId", secretId));

        OutputStream os = connection.getOutputStream();
        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os, "UTF-8"));
        writer.write(getQuery(params));
        writer.flush();
        writer.close();
        os.close();

        connection.connect();
        int sc = connection.getResponseCode();
        if (sc != 200)
            throw new IOException("HTTP error " + sc + " " + connection.getResponseMessage());
        BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
        StringBuilder content = new StringBuilder();
        String inputLine;
        while ((inputLine = in.readLine()) != null) content.append(inputLine);
        in.close();

        // create json object
        JsonNode json = mapper.readTree(content.toString());
        if (json.has("_error"))
            throw new IOException(
                    "Rest Error: "
                            + json.get("_error").asText()
                            + " "
                            + json.get("_errorMessage").asText());
    }

    public void deleteSecret(String secretId) throws IOException {
        // open url and read content
        String u = url + "/rest/vault";
        URL url = new URL(u);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("DELETE");
        connection.setDoOutput(true);
        connection.setRequestProperty("Authorization", "Basic " + auth);
        List<NameValuePair> params = new ArrayList<NameValuePair>();
        params.add(new BasicNameValuePair("_secretId", secretId));

        OutputStream os = connection.getOutputStream();
        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os, "UTF-8"));
        writer.write(getQuery(params));
        writer.flush();
        writer.close();
        os.close();

        connection.connect();
        int sc = connection.getResponseCode();
        if (sc != 200)
            throw new IOException("HTTP error " + sc + " " + connection.getResponseMessage());
        BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
        StringBuilder content = new StringBuilder();
        String inputLine;
        while ((inputLine = in.readLine()) != null) content.append(inputLine);
        in.close();

        // create json object
        JsonNode json = mapper.readTree(content.toString());
        if (json.has("_error"))
            throw new IOException(
                    "Rest Error: "
                            + json.get("_error").asText()
                            + " "
                            + json.get("_errorMessage").asText());
    }

    public List<SecretEntry> search(String target, String... index) throws IOException {
        String u = url + "/rest/vault?target=" + URLEncoder.encode(target, "UTF-8");
        for (int i = 0; i < index.length; i++)
            if (MString.isSet(index[i]))
                u = u + "&index" + i + "=" + URLEncoder.encode(index[i], "UTF-8");
        URL url = new URL(u);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");
        connection.setDoOutput(true);
        connection.setRequestProperty("Authorization", "Basic " + auth);
        connection.connect();
        int sc = connection.getResponseCode();
        if (sc != 200)
            throw new IOException("HTTP error " + sc + " " + connection.getResponseMessage());
        BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
        StringBuilder content = new StringBuilder();
        String inputLine;
        while ((inputLine = in.readLine()) != null) content.append(inputLine);
        in.close();

        // create json object
        JsonNode json = mapper.readTree(content.toString());
        if (json.has("_error"))
            throw new IOException(
                    "Rest Error: "
                            + json.get("_error").asText()
                            + " "
                            + json.get("_errorMessage").asText());

        if (!json.isArray()) throw new IOException("Result is not an array");

        LinkedList<SecretEntry> out = new LinkedList<>();
        for (JsonNode jEntry : json) {
            out.add(new SecretEntry(jEntry));
        }
        return out;
    }

    private String getQuery(List<NameValuePair> params) throws UnsupportedEncodingException {
        StringBuilder result = new StringBuilder();
        boolean first = true;

        for (NameValuePair pair : params) {
            if (first) first = false;
            else result.append("&");

            result.append(URLEncoder.encode(pair.getName(), "UTF-8"));
            result.append("=");
            result.append(URLEncoder.encode(pair.getValue(), "UTF-8"));
        }

        return result.toString();
    }
}
