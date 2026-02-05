package org.isw2.dataset.jira.controller;

import org.isw2.dataset.exceptions.ProcessingException;
import org.isw2.absfactory.Controller;
import org.isw2.dataset.jira.model.Version;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.*;
import java.net.URI;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class GetVersionsFromJira implements Controller<String, List<Version>> {

    private static final String BASE_URL = "https://issues.apache.org/jira/rest/api/2/project/";
    private static final String VERSIONS = "versions";
    private static final String RELEASE_DATE = "releaseDate";
    private static final String NAME =  "name";
    private static final String DESCRIPTION = "description";
    private static final String ID  = "id";

    private final List<Version> jiraVersions = new ArrayList<>();

    @Override
    public List<Version> execute(String projectName) throws ProcessingException {
        try {
            getVersionsFromJira(projectName);
            return jiraVersions;
        } catch (IOException e) {
            throw new ProcessingException(e.getMessage());
        }
    }

    private void getVersionsFromJira(String projectName) throws IOException {
        String url = BASE_URL + projectName;
        JSONObject json = readJsonFromUrl(url);
        JSONArray jsonVersions = json.getJSONArray(VERSIONS);
        for (int i = 0; i < jsonVersions.length(); i++ ) {
            String name = "";
            String id = "";
            String description = "";
            String releaseDate = "";
            if(jsonVersions.getJSONObject(i).has(RELEASE_DATE)) {
                name = getNameFromJsonArray(jsonVersions, i);
                id = getIdFromJsonArray(jsonVersions, i);
                description = getDescriptionFromJsonArray(jsonVersions, i);
                releaseDate = getReleaseDateFromJsonArray(jsonVersions, i);
            }
            if (jsonVersions.getJSONObject(i).has("released") && jsonVersions.getJSONObject(i).get("released").toString().equals("true")) {
                Version version = new Version();
                version.setName(name);
                version.setId(id);
                version.setReleaseDate(releaseDate);
                version.setDescription(description);
                jiraVersions.add(version);
            }
        }
    }

    private String getNameFromJsonArray(JSONArray jsonVersions, int i) {
        if (jsonVersions.getJSONObject(i).has(NAME)) {
            return jsonVersions.getJSONObject(i).getString(NAME);
        }
        return null;
    }

    private String getIdFromJsonArray(JSONArray jsonVersions, int i) {
        if (jsonVersions.getJSONObject(i).has(ID)) {
            return jsonVersions.getJSONObject(i).getString(ID);
        }
        return null;
    }

    private String getDescriptionFromJsonArray(JSONArray jsonVersions, int i) {
        if (jsonVersions.getJSONObject(i).has(DESCRIPTION)) {
            return jsonVersions.getJSONObject(i).get(DESCRIPTION).toString();
        }
        return null;
    }

    private String getReleaseDateFromJsonArray(JSONArray jsonVersions, int i) {
        if (jsonVersions.getJSONObject(i).has(RELEASE_DATE)) {
            return jsonVersions.getJSONObject(i).get(RELEASE_DATE).toString();
        }
        return null;
    }

    public static JSONObject readJsonFromUrl(String url) throws IOException, JSONException {
        URL urlObj = URI.create(url).toURL();

        try (InputStream is = urlObj.openStream();
             BufferedReader rd = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
            String jsonText = readAll(rd);
            return new JSONObject(jsonText);
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
