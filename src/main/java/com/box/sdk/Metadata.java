package com.box.sdk;

import java.util.ArrayList;
import java.util.List;

import com.eclipsesource.json.JsonArray;
import com.eclipsesource.json.JsonObject;
import com.eclipsesource.json.JsonValue;

/**
 * The Metadata class represents one type instance of Box metadata.
 *
 * Learn more about Box metadata:
 * https://developers.box.com/metadata-api/
 */
public class Metadata {

    /**
     * The default limit of entries per response.
     */
    public static final int DEFAULT_LIMIT = 100;

    /**
     * URL template for all metadata associated with item.
     */
    private static final URLTemplate GET_ALL_METADATA_URL_TEMPLATE = new URLTemplate("/metadata");

    /**
     * Values contained by the metadata object.
     */
    private final JsonObject values;

    /**
     * Operations to be applied to the metadata object.
     */
    private JsonArray operations;

    /**
     * Creates an empty metadata.
     */
    public Metadata() {
        this.values = new JsonObject();
    }

    /**
     * Creates a new metadata.
     * @param values the initial metadata values.
     */
    Metadata(JsonObject values) {
        this.values = values;
    }

    /**
     * Creates a copy of another metadata.
     * @param other the other metadata object to copy.
     */
    public Metadata(Metadata other) {
        this.values = new JsonObject(other.values);
    }

    /**
     * Used to retrieve all metadata associated with the item.
     * @param item item to get metadata for.
     * @param fields the optional fields to retrieve.
     * @return An iterable of metadata instances associated with the item.
     */
    public static Iterable<Metadata> getAllMetadata(BoxItem item, String ... fields) {
        QueryStringBuilder builder = new QueryStringBuilder();
        if (fields.length > 0) {
            builder.appendParam("fields", fields);
        }
        return new BoxResourceIterable<Metadata>(
                item.getAPI(),
                GET_ALL_METADATA_URL_TEMPLATE.buildWithQuery(item.getItemURL().toString(), builder.toString()),
                DEFAULT_LIMIT) {

            @Override
            protected Metadata factory(JsonObject jsonObject) {
                return new Metadata(jsonObject);
            }

        };
    }

    /**
     * Returns the 36 character UUID to identify the metadata object.
     * @return the metadata ID.
     */
    public String getID() {
        return this.get("/$id");
    }

    /**
     * Returns the metadata type.
     * @return the metadata type.
     */
    public String getTypeName() {
        return this.get("/$type");
    }

    /**
     * Returns the parent object ID (typically the file ID).
     * @return the parent object ID.
     */
    public String getParentID() {
        return this.get("/$parent");
    }

    /**
     * Returns the scope.
     * @return the scope.
     */
    public String getScope() {
        return this.get("/$scope");
    }

    /**
     * Returns the template name.
     * @return the template name.
     */
    public String getTemplateName() {
        return this.get("/$template");
    }

    /**
     * Adds a new metadata value.
     * @param path the path that designates the key. Must be prefixed with a "/".
     * @param value the value.
     * @return this metadata object.
     */
    public Metadata add(String path, String value) {
        this.values.add(this.pathToProperty(path), value);
        this.addOp("add", path, value);
        return this;
    }

    /**
     * Replaces an existing metadata value.
     * @param path the path that designates the key. Must be prefixed with a "/".
     * @param value the value.
     * @return this metadata object.
     */
    public Metadata replace(String path, String value) {
        this.values.set(this.pathToProperty(path), value);
        this.addOp("replace", path, value);
        return this;
    }

    /**
     * Removes an existing metadata value.
     * @param path the path that designates the key. Must be prefixed with a "/".
     * @return this metadata object.
     */
    public Metadata remove(String path) {
        this.values.remove(this.pathToProperty(path));
        this.addOp("remove", path, null);
        return this;
    }

    /**
     * Tests that a property has the expected value.
     * @param path the path that designates the key. Must be prefixed with a "/".
     * @param value the expected value.
     * @return this metadata object.
     */
    public Metadata test(String path, String value) {
        this.addOp("test", path, value);
        return this;
    }

    /**
     * Returns a value.
     * @param path the path that designates the key. Must be prefixed with a "/".
     * @return the metadata property value.
     */
    public String get(String path) {
        final JsonValue value = this.values.get(this.pathToProperty(path));
        if (value == null) {
            return null;
        }
        if (value.isNumber()) {
            return value.toString();
        }
        return value.asString();
    }

    /**
     * Returns a list of metadata property paths.
     * @return the list of metdata property paths.
     */
    public List<String> getPropertyPaths() {
        List<String> result = new ArrayList<String>();

        for (String property : this.values.names()) {
            if (!property.startsWith("$")) {
                result.add(this.propertyToPath(property));
            }
        }

        return result;
    }

    /**
     * Returns the JSON patch string with all operations.
     * @return the JSON patch string.
     */
    public String getPatch() {
        if (this.operations == null) {
            return "[]";
        }
        return this.operations.toString();
    }

    /**
     * Returns the JSON representation of this metadata.
     * @return the JSON representation of this metadata.
     */
    @Override
    public String toString() {
        return this.values.toString();
    }

    /**
     * Converts a JSON patch path to a JSON property name.
     * Currently the metadata API only supports flat maps.
     * @param path the path that designates the key.  Must be prefixed with a "/".
     * @return the JSON property name.
     */
    private String pathToProperty(String path) {
        if (path == null || !path.startsWith("/")) {
            throw new IllegalArgumentException("Path must be prefixed with a \"/\".");
        }
        return path.substring(1);
    }

    /**
     * Converts a JSON property name to a JSON patch path.
     * @param property the JSON property name.
     * @return the path that designates the key.
     */
    private String propertyToPath(String property) {
        if (property == null) {
            throw new IllegalArgumentException("Property must not be null.");
        }
        return "/" + property;
    }

    /**
     * Adds a patch operation.
     * @param op the operation type. Must be add, replace, remove, or test.
     * @param path the path that designates the key. Must be prefixed with a "/".
     * @param value the value to be set.
     */
    private void addOp(String op, String path, String value) {
        if (this.operations == null) {
            this.operations = new JsonArray();
        }

        this.operations.add(new JsonObject()
                .add("op", op)
                .add("path", path)
                .add("value", value));
    }
}
