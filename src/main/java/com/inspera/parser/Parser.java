package com.inspera.parser;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.*;

/**
 *
 */
public class Parser {

    private static final String META = "meta";
    public static final String FIELD = "field";
    public static final String TITLE = "title";

    public static final String START_TIME = "startTime";

    public static final String END_TIME = "endTime";

    public static final List<String> metaProperties = new ArrayList(Arrays.asList(TITLE, START_TIME, END_TIME));

    public static final String BEFORE = "before";
    public static final String AFTER = "after";
    public static final String CANDIDATES = "candidates";
    public static final String EDITED = "edited";
    public static final String ADDED = "added";
    public static final String REMOVED = "removed";
    public static final String ID = "id";

    public JsonObject parse(JsonObject before, JsonObject after) {
        JsonObject result = new JsonObject();
        JsonArray metaArray = buildMeta(before, after);
        result.add(META, metaArray);

        JsonObject candidates = buildCandidates(before, after);
        result.add(CANDIDATES, candidates);

        return result;
    }

    private static JsonObject buildCandidates(JsonObject before, JsonObject after) {
        JsonObject candidates = new JsonObject();
        candidates.add(EDITED, buildEdited(before, after));
        candidates.add(ADDED, buildAdded(before, after));
        candidates.add(REMOVED, buildRemoved(before, after));
        return candidates;
    }

    private static JsonArray buildEdited(JsonObject before, JsonObject after) {
        JsonArray edited = new JsonArray();
        JsonArray candidatesAfter = after.getAsJsonArray(CANDIDATES);

        candidatesAfter.forEach(candidateAfter -> {
            int candidateId = candidateAfter.getAsJsonObject().get(ID).getAsInt();
            Optional<JsonObject> candidateBeforeOptional = getCandidate(before, candidateId);
            if(candidateBeforeOptional.isPresent()) {
                JsonObject candidateBefore = candidateBeforeOptional.get();
                if(!candidateBefore.equals(candidateAfter)) {
                    JsonObject editedCandidate = new JsonObject();
                    editedCandidate.addProperty(ID, candidateId);
                    edited.add(editedCandidate);
                }
            }
        });

        return edited;
    }

    private static Optional<JsonObject> getCandidate(JsonObject data, int candidateId) {
        JsonArray candidates = data.getAsJsonArray(CANDIDATES);
        for (JsonElement jsonElement : candidates) {
            JsonObject nextCandidate = jsonElement.getAsJsonObject();
            if (nextCandidate.get(ID).getAsInt() == candidateId) {
                return Optional.of(nextCandidate);
            }
        }
        return Optional.empty();
    }

    private static JsonArray buildAdded(JsonObject before, JsonObject after) {
        JsonArray added = new JsonArray();
        JsonArray candidatesBefore = before.getAsJsonArray(CANDIDATES);
        Set<Integer> beforeIds = getIds(candidatesBefore);

        JsonArray candidatesAfter = after.getAsJsonArray(CANDIDATES);
        Set<Integer> afterIds = getIds(candidatesAfter);

        afterIds.removeAll(beforeIds);

        afterIds.forEach(id -> {
            JsonObject addedCandidateId = new JsonObject();
            addedCandidateId.addProperty(ID, id);
            added.add(addedCandidateId);
        });

        return added;
    }

    private static JsonArray buildRemoved(JsonObject before, JsonObject after) {

        JsonArray removed = new JsonArray();
        JsonArray candidatesBefore = before.getAsJsonArray(CANDIDATES);
        Set<Integer> beforeIds = getIds(candidatesBefore);

        JsonArray candidatesAfter = after.getAsJsonArray(CANDIDATES);
        Set<Integer> afterIds = getIds(candidatesAfter);

        beforeIds.removeAll(afterIds);

        beforeIds.forEach(id -> {
            JsonObject removedCandidateId = new JsonObject();
            removedCandidateId.addProperty(ID, id);
            removed.add(removedCandidateId);
        });

        return removed;
    }

    private static Set<Integer> getIds(JsonArray candidates) {
        Set<Integer> ids = new HashSet<>();
        candidates.forEach(jsonElement -> ids.add(jsonElement.getAsJsonObject().get(ID).getAsInt()));
        return ids;
    }

    private JsonArray buildMeta(JsonObject before, JsonObject after) {
        JsonArray metaArray = new JsonArray();

        for(String propertyName: metaProperties) {
            Optional<JsonObject> optionalJsonObject = buildMetaDiff(before, after, propertyName);
            optionalJsonObject.ifPresent(metaArray::add);
        }

        return metaArray;
    }

    private Optional<JsonObject> buildMetaDiff(JsonObject before, JsonObject after, String propertyName) {
        String beforeValue = before.getAsJsonObject(META).get(propertyName).getAsString();
        String afterValue = after.getAsJsonObject(META).get(propertyName).getAsString();
        if(!beforeValue.equals(afterValue)) {
            JsonObject metaDiff = new JsonObject();
            metaDiff.addProperty(FIELD, propertyName);
            metaDiff.addProperty(BEFORE, beforeValue);
            metaDiff.addProperty(AFTER, afterValue);
            return Optional.of(metaDiff);
        }

        return Optional.empty();
    }

}
