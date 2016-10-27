package com.box.sdk;

import java.net.URL;
import java.text.ParseException;
import java.util.Date;

import com.eclipsesource.json.JsonObject;
import com.eclipsesource.json.JsonValue;

/**
 * Represents a legal hold policy.
 * Legal Hold Policy information describes the basic characteristics of the Policy,
 * such as name, description, and filter dates.
 *
 * @see <a href="https://docs.box.com/reference#legal-holds-object">Box legal holds</a>
 *
 * <p>Unless otherwise noted, the methods in this class can throw an unchecked {@link BoxAPIException} (unchecked
 * meaning that the compiler won't force you to handle it) if an error occurs. If you wish to implement custom error
 * handling for errors related to the Box REST API, you should capture this exception explicitly.</p>
 */
@BoxResourceType("legal_hold")
public class BoxLegalHold extends BoxResource {

    /**
     * The URL template used for operation with legal hold policy with given ID.
     * @see #getInfo(String...)
     */
    private static final URLTemplate LEGAL_HOLD_URL_TEMPLATE = new URLTemplate("legal_hold_policies/%s");

    /**
     * The URL used for operation with legal hold policy assignments of the policy with given ID.
     * @see #getAssignments(String, String, int, String...)
     */
    private static final URLTemplate LEGAL_HOLD_ASSIGNMENTS_URL_TEMPLATE
            = new URLTemplate("legal_hold_policies/%s/assignments");

    /**
     * Default limit of the legal hold assignment info entries per one response page.
     */
    private static final int DEFAULT_LIMIT = 100;

    /**
     * Constructs a BoxLegalHold for a resource with a given ID.
     *
     * @param api the API connection to be used by the resource.
     * @param id  the ID of the resource.
     */
    public BoxLegalHold(BoxAPIConnection api, String id) {
        super(api, id);
    }

    /**
     * @param fields the fields to retrieve.
     * @return information about this legal hold policy.
     */
    public Info getInfo(String ... fields) {
        QueryStringBuilder builder = new QueryStringBuilder();
        if (fields.length > 0) {
            builder.appendParam("fields", fields);
        }
        URL url = LEGAL_HOLD_URL_TEMPLATE.buildWithQuery(this.getAPI().getBaseURL(), builder.toString(), this.getID());
        BoxAPIRequest request = new BoxAPIRequest(this.getAPI(), url, "GET");
        BoxJSONResponse response = (BoxJSONResponse) request.send();
        JsonObject responseJSON = JsonObject.readFrom(response.getJSON());
        return new Info(responseJSON);
    }

    /**
     * Assigns this legal holds policy to the given box resource.
     * Currently only {@link BoxFile}, {@link BoxFileVersion}, {@link BoxFolder} and {@link BoxUser} are supported.
     * @param resource the box resource to assign legal hold policy to.
     * @return info about created legal hold policy assignment.
     */
    public BoxLegalHoldAssignment.Info assignTo(BoxResource resource) {
        return BoxLegalHoldAssignment.create(
                this.getAPI(), this.getID(), BoxResource.getResourceType(resource.getClass()), resource.getID());
    }

    /**
     * Returns iterable containing assignments for this single legal hold policy.
     * @param fields the fields to retrieve.
     * @return an iterable containing assignments for this single legal hold policy.
     */
    public Iterable<BoxLegalHoldAssignment.Info> getAssignments(String ... fields) {
        return this.getAssignments(null, null, DEFAULT_LIMIT, fields);
    }

    /**
     * Returns iterable containing assignments for this single legal hold policy.
     * Parameters can be used to filter retrieved assignments.
     * @param type filter assignments of this type only.
     *             Can be "file_version", "file", "folder", "user" or null if no type filter is necessary.
     * @param id filter assignments to this ID only. Can be null if no id filter is necessary.
     * @param limit the limit of entries per page. Default limit is 100.
     * @param fields the fields to retrieve.
     * @return an iterable containing assignments for this single legal hold policy.
     */
    public Iterable<BoxLegalHoldAssignment.Info> getAssignments(String type, String id, int limit, String ... fields) {
        QueryStringBuilder builder = new QueryStringBuilder();
        if (type != null) {
            builder.appendParam("assign_to_type", type);
        }
        if (id != null) {
            builder.appendParam("assign_to_id", id);
        }
        if (fields.length > 0) {
            builder.appendParam("fields", fields);
        }
        return new BoxResourceIterable<BoxLegalHoldAssignment.Info>(
                this.getAPI(), LEGAL_HOLD_ASSIGNMENTS_URL_TEMPLATE.buildWithQuery(
                        this.getAPI().getBaseURL(), builder.toString(), this.getID()), limit) {

            @Override
            protected BoxLegalHoldAssignment.Info factory(JsonObject jsonObject) {
                BoxLegalHoldAssignment assignment = new BoxLegalHoldAssignment(
                        BoxLegalHold.this.getAPI(), jsonObject.get("id").asString());
                return assignment.new Info(jsonObject);
            }
        };
    }

    /**
     * Contains information about the legal hold policy.
     */
    public class Info extends BoxResource.Info {

        /**
         * @see #getPolicyName()
         */
        private String policyName;

        /**
         * @see #getDescription()
         */
        private String description;

        /**
         * @see #getStatus()
         */
        private String status;

        /**
         * @see #getAssignmentCountUser()
         */
        private int assignmentCountUser;

        /**
         * @see #getAssignmentCountFolder()
         */
        private int assignmentCountFolder;

        /**
         * @see #getAssignmentCountFile()
         */
        private int assignmentCountFile;

        /**
         * @see #getAssignmentCountFileVersion()
         */
        private int assignmentCountFileVersion;

        /**
         * @see #getCreatedAt()
         */
        private BoxUser.Info createdBy;

        /**
         * @see #getCreatedAt()
         */
        private Date createdAt;

        /**
         * @see #getModifiedAt()
         */
        private Date modifiedAt;

        /**
         * @see #getDeletedAt()
         */
        private Date deletedAt;

        /**
         * @see #getFilterStartedAt()
         */
        private Date filterStartedAt;

        /**
         * @see #getFilterEndedAt()
         */
        private Date filterEndedAt;

        /**
         * @see #getReleaseNotes()
         */
        private String releaseNotes;

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
            return BoxLegalHold.this;
        }

        /**
         * @return the name of the policy.
         */
        public String getPolicyName() {
            return this.policyName;
        }

        /**
         * @return the description of the policy.
         */
        public String getDescription() {
            return this.description;
        }

        /**
         * Status can be "active", "applying", "releasing" or "released".
         * @return the status of the policy.
         */
        public String getStatus() {
            return this.status;
        }

        /**
         * @return count of users this policy assigned to.
         */
        public int getAssignmentCountUser() {
            return this.assignmentCountUser;
        }

        /**
         * @return count of folders this policy assigned to.
         */
        public int getAssignmentCountFolder() {
            return this.assignmentCountFolder;
        }

        /**
         * @return count of files this policy assigned to.
         */
        public int getAssignmentCountFile() {
            return this.assignmentCountFile;
        }

        /**
         * @return count of file versions this policy assigned to.
         */
        public int getAssignmentCountFileVersion() {
            return this.assignmentCountFileVersion;
        }

        /**
         * @return info about the user who created this policy.
         */
        public BoxUser.Info getCreatedBy() {
            return this.createdBy;
        }

        /**
         * @return time the policy was created.
         */
        public Date getCreatedAt() {
            return this.createdAt;
        }

        /**
         * @return time the policy was modified.
         */
        public Date getModifiedAt() {
            return this.modifiedAt;
        }

        /**
         * @return time that the policy release request was sent.
         */
        public Date getDeletedAt() {
            return this.deletedAt;
        }

        /**
         * @return optional date filter applies to Custodian assignments only.
         */
        public Date getFilterStartedAt() {
            return this.filterStartedAt;
        }

        /**
         * @return optional date filter applies to Custodian assignments only.
         */
        public Date getFilterEndedAt() {
            return this.filterEndedAt;
        }

        /**
         * @return notes around why the policy was released.
         */
        public String getReleaseNotes() {
            return this.releaseNotes;
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
                if (memberName.equals("policy_name")) {
                    this.policyName = value.asString();
                } else if (memberName.equals("description")) {
                    this.description = value.asString();
                } else if (memberName.equals("status")) {
                    this.status = value.asString();
                } else if (memberName.equals("release_notes")) {
                    this.releaseNotes = value.asString();
                } else if (memberName.equals("assignment_counts")) {
                    JsonObject countsJSON = value.asObject();
                    this.assignmentCountUser = countsJSON.get("user").asInt();
                    this.assignmentCountFolder = countsJSON.get("folder").asInt();
                    this.assignmentCountFile = countsJSON.get("file").asInt();
                    this.assignmentCountFileVersion = countsJSON.get("file_version").asInt();
                } else if (memberName.equals("created_by")) {
                    JsonObject userJSON = value.asObject();
                    if (this.createdBy == null) {
                        String userID = userJSON.get("id").asString();
                        BoxUser user = new BoxUser(getAPI(), userID);
                        this.createdBy = user.new Info(userJSON);
                    } else {
                        this.createdBy.update(userJSON);
                    }
                } else if (memberName.equals("created_at")) {
                    this.createdAt = BoxDateFormat.parse(value.asString());
                } else if (memberName.equals("modified_at")) {
                    this.modifiedAt = BoxDateFormat.parse(value.asString());
                } else if (memberName.equals("deleted_at")) {
                    this.deletedAt = BoxDateFormat.parse(value.asString());
                } else if (memberName.equals("filter_started_at")) {
                    this.filterStartedAt = BoxDateFormat.parse(value.asString());
                } else if (memberName.equals("filter_ended_at")) {
                    this.filterEndedAt = BoxDateFormat.parse(value.asString());
                }
            } catch (ParseException e) {
                assert false : "A ParseException indicates a bug in the SDK.";
            }
        }
    }
}
