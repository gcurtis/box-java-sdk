package com.box.sdk;

import java.net.URL;
import java.text.ParseException;
import java.util.Date;

import com.eclipsesource.json.JsonObject;
import com.eclipsesource.json.JsonValue;

/**
 * Represents a file version retention.
 * A retention policy blocks permanent deletion of content for a specified amount of time.
 * Admins can apply policies to specified folders, or an entire enterprise.
 * A file version retention is a record for a retained file version.
 *
 * @see <a href="https://docs.box.com/reference#file-version-retention-object">Box file version retention</a>
 *
 * <p>Unless otherwise noted, the methods in this class can throw an unchecked {@link BoxAPIException} (unchecked
 * meaning that the compiler won't force you to handle it) if an error occurs. If you wish to implement custom error
 * handling for errors related to the Box REST API, you should capture this exception explicitly.</p>
 */
@BoxResourceType("file_version_retention")
public class BoxFileVersionRetention extends BoxResource {

    /**
     * @see #getInfo(String...)
     */
    private static final URLTemplate RETENTION_URL_TEMPLATE = new URLTemplate("file_version_retentions/%s");

    /**
     * Constructs a BoxResource for a resource with a given ID.
     *
     * @param api the API connection to be used by the resource.
     * @param id  the ID of the resource.
     */
    public BoxFileVersionRetention(BoxAPIConnection api, String id) {
        super(api, id);
    }

    /**
     * @param fields the fields to retrieve.
     * @return information about this retention policy.
     */
    public BoxFileVersionRetention.Info getInfo(String ... fields) {
        QueryStringBuilder builder = new QueryStringBuilder();
        if (fields.length > 0) {
            builder.appendParam("fields", fields);
        }
        URL url = RETENTION_URL_TEMPLATE.buildWithQuery(this.getAPI().getBaseURL(), builder.toString(), this.getID());
        BoxAPIRequest request = new BoxAPIRequest(this.getAPI(), url, "GET");
        BoxJSONResponse response = (BoxJSONResponse) request.send();
        JsonObject responseJSON = JsonObject.readFrom(response.getJSON());
        return new Info(responseJSON);
    }

    /**
     * Contains information about the retention policy.
     */
    public class Info extends BoxResource.Info {

        /**
         * @see #getFileVersion()
         */
        private BoxFileVersion fileVersion;

        /**
         * @see #getFile()
         */
        private BoxFile.Info file;

        /**
         * @see #getAppliedAt()
         */
        private Date appliedAt;

        /**
         * @see #getDispositionAt()
         */
        private Date dispositionAt;

        /**
         * @see #getWinningPolicy()
         */
        private BoxRetentionPolicy.Info winningPolicy;

        /**
         * Constructs an empty Info object.
         */
        public Info() {
            super();
        }

        /**
         * Constructs an Info object by parsing information from a JSON string.
         * @param  json the JSON string to parse.
         */
        public Info(String json) {
            super(json);
        }

        /**
         * Constructs an Info object using an already parsed JSON object.
         * @param  jsonObject the parsed JSON object.
         */
        Info(JsonObject jsonObject) {
            super(jsonObject);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public BoxResource getResource() {
            return BoxFileVersionRetention.this;
        }

        /**
         * @return the file version this file version retention was applied to.
         */
        public BoxFileVersion getFileVersion() {
            return this.fileVersion;
        }

        /**
         * @return the file this file version retention was applied to.
         */
        public BoxFile.Info getFile() {
            return this.file;
        }

        /**
         * @return the time that this file version retention was created.
         */
        public Date getAppliedAt() {
            return this.appliedAt;
        }

        /**
         * @return the time that the retention period expires on this file version retention.
         */
        public Date getDispositionAt() {
            return this.dispositionAt;
        }

        /**
         * @return the winning retention policy applied to this file version retention.
         */
        public BoxRetentionPolicy.Info getWinningPolicy() {
            return this.winningPolicy;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        void parseJSONMember(JsonObject.Member member) {
            super.parseJSONMember(member);
            String memberName = member.getName();
            JsonValue value = member.getValue();
            try {
                if (memberName.equals("winning_retention_policy")) {
                    JsonObject policyJSON = value.asObject();
                    if (this.winningPolicy == null) {
                        String policyID = policyJSON.get("id").asString();
                        BoxRetentionPolicy policy = new BoxRetentionPolicy(getAPI(), policyID);
                        this.winningPolicy = policy.new Info(policyJSON);
                    } else {
                        this.winningPolicy.update(policyJSON);
                    }
                } else if (memberName.equals("file")) {
                    JsonObject fileJSON = value.asObject();
                    if (this.file == null) {
                        String fileID = fileJSON.get("id").asString();
                        BoxFile file = new BoxFile(getAPI(), fileID);
                        this.file = file.new Info(fileJSON);
                    } else {
                        this.file.update(fileJSON);
                    }
                } else if (memberName.equals("file_version")) {
                    JsonObject versionJSON = value.asObject();
                    String fileVersionID = versionJSON.get("id").asString();
                    this.fileVersion = new BoxFileVersion(getAPI(), versionJSON, fileVersionID);
                } else if (memberName.equals("applied_at")) {
                    this.appliedAt = BoxDateFormat.parse(value.asString());
                } else if (memberName.equals("disposition_at")) {
                    this.dispositionAt = BoxDateFormat.parse(value.asString());
                }
            } catch (ParseException e) {
                assert false : "A ParseException indicates a bug in the SDK.";
            }
        }
    }
}
