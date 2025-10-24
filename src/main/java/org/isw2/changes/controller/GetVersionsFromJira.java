package org.isw2.changes.controller;

import org.isw2.changes.model.Version;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.*;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

public class GetVersionsFromJira {
    List<Version> versions = new ArrayList<>();
    public List<Version> getVersionsFromJira(String projectName) throws IOException {
        String url = "https://issues.apache.org/jira/rest/api/2/project/" + projectName;
        JSONObject json = readJsonFromUrl(url);
        JSONArray jsonVersions = json.getJSONArray("versions");
        for (int i = 0; i < jsonVersions.length(); i++ ) {
            String name = "";
            String id = "";
            String description = "";
            String releaseDate = "";
            if(jsonVersions.getJSONObject(i).has("releaseDate")) {
                if (jsonVersions.getJSONObject(i).has("name"))
                    name = jsonVersions.getJSONObject(i).get("name").toString();
                if (jsonVersions.getJSONObject(i).has("id"))
                    id = jsonVersions.getJSONObject(i).get("id").toString();
                if (jsonVersions.getJSONObject(i).has("description"))
                    description = jsonVersions.getJSONObject(i).get("description").toString();
                if (jsonVersions.getJSONObject(i).has("releaseDate"))
                    releaseDate = jsonVersions.getJSONObject(i).get("releaseDate").toString();
            }
            Version version = new Version();
            version.setName(name);
            version.setId(id);
            version.setReleaseDate(releaseDate);
            version.setDescription(description);
            versions.add(version);
        }
        return versions;
    }

    public static JSONObject readJsonFromUrl(String url) throws IOException, JSONException {
        InputStream is = new URL(url).openStream();
        try {
            BufferedReader rd = new BufferedReader(new InputStreamReader(is, Charset.forName("UTF-8")));
            String jsonText = readAll(rd);
            JSONObject json = new JSONObject(jsonText);
            return json;
        } finally {
            is.close();
        }
    }

    private static String readAll(Reader rd) throws IOException {
        StringBuilder sb = new StringBuilder();
        int cp;
        while ((cp = rd.read()) != -1) {
            sb.append((char) cp);
        }
        return sb.toString();
    }
}
