package com.box.sdk;

import java.net.URL;
import java.text.ParseException;
import java.util.Date;

import com.eclipsesource.json.JsonObject;
import com.eclipsesource.json.JsonValue;

/**
 * Represents a retention policy.
 *
 * <p>Unless otherwise noted, the methods in this class can throw an unchecked {@link BoxAPIException} (unchecked
 * meaning that the compiler won't force you to handle it) if an error occurs. If you wish to implement custom error
 * handling for errors related to the Box REST API, you should capture this exception explicitly.</p>
 */
@BoxResourceType("retention_policy")
public class BoxRetentionPolicy extends BoxResource {

    /**
     *  Will cause the content retained by the policy to be permanently deleted.
     */
    public static final String ACTION_PERMANENTLY_DELETE = "permanently_delete";

    /**
     * Will lift the retention policy from the content, allowing it to be deleted by users.
     */
    public static final String ACTION_REMOVE_RETENTION = "remove_retention";

    /**
     * Type for finite retention policies. Finite retention policies has the duration.
     */
    private static final String TYPE_FINITE = "finite";

    /**
     * Type for indefinite retention policies. Indefinite retention policies can have only "remove_retention"
     * assigned action.
     */
    private static final String TYPE_INDEFINITE = "indefinite";

    /**
     * The default limit of entries per response.
     */
    private static final int DEFAULT_LIMIT = 100;

    /**
     * The URL template used for operation with retention policies.
     */
    private static final URLTemplate RETENTION_POLICIES_URL_TEMPLATE = new URLTemplate("retention_policies");

    /**
     * The URL template used for operation with retention policy assignments.
     */
    private static final URLTemplate ASSIGNMENTS_URL_TEMPLATE = new URLTemplate("retention_policies/%s/assignments");

    /**
     * Constructs a retention policy for a resource with a given ID.
     *
     * @param api the API connection to be used by the resource.
     * @param id  the ID of the resource.
     */
    public BoxRetentionPolicy(BoxAPIConnection api, String id) {
        super(api, id);
    }

    /**
     * Used to create a new indefinite retention policy.
     * @param api the API connection to be used by the created user.
     * @param name the name of the retention policy.
     * @return the created retention policy's info.
     */
    public static BoxRetentionPolicy.Info createIndefinitePolicy(BoxAPIConnection api, String name) {
        return createRetentionPolicy(api, name, TYPE_INDEFINITE, 0, ACTION_REMOVE_RETENTION);
    }

    /**
     * Used to create a new finite retention policy.
     * @param api the API connection to be used by the created user.
     * @param name the name of the retention policy.
     * @param length the duration in days that the retention policy will be active for after being assigned to content.
     * @param action the disposition action can be "permanently_delete" or "remove_retention".
     * @return the created retention policy's info.
     */
    public static BoxRetentionPolicy.Info createFinitePolicy(BoxAPIConnection api, String name, int length,
                                                                      String action) {
        return createRetentionPolicy(api, name, TYPE_FINITE, length, action);
    }

    /**
     * Used to create a new retention policy.
     * @param api the API connection to be used by the created user.
     * @param name the name of the retention policy.
     * @param type the type of the retention policy. Can be "finite" or "indefinite".
     * @param length the duration in days that the retention policy will be active for after being assigned to content.
     * @param action the disposition action can be "permanently_delete" or "remove_retention".
     * @return the created retention policy's info.
     */
    private static BoxRetentionPolicy.Info createRetentionPolicy(BoxAPIConnection api, String name, String type,
                                                                int length, String action) {
        URL url = RETENTION_POLICIES_URL_TEMPLATE.build(api.getBaseURL());
        BoxJSONRequest request = new BoxJSONRequest(api, url, "POST");
        JsonObject requestJSON = new JsonObject()
                .add("policy_name", name)
                .add("policy_type", type)
                .add("disposition_action", action);
        if (!type.equals(TYPE_INDEFINITE)) {
            requestJSON.add("retention_length", length);
        }
        request.setBody(requestJSON.toString());
        BoxJSONResponse response = (BoxJSONResponse) request.send();
        JsonObject responseJSON = JsonObject.readFrom(response.getJSON());
        BoxRetentionPolicy createdPolicy = new BoxRetentionPolicy(api, responseJSON.get("id").asString());
        return createdPolicy.new Info(responseJSON);
    }

    /**
     * Returns iterable with all assignments of given type of this retention policy.
     * @param type the type of the retention policy assignment to retrieve. Can either be "folder" or "enterprise".
     * @return an iterable containing all assignments of given type.
     */
    public Iterable<BoxRetentionPolicyAssignment.Info> getAssignments(String type) {
        QueryStringBuilder queryString = new QueryStringBuilder().appendParam("type", type);
        URL url = ASSIGNMENTS_URL_TEMPLATE.buildWithQuery(getAPI().getBaseURL(), queryString.toString(), getID());
        return new BoxResourceIterable<BoxRetentionPolicyAssignment.Info>(getAPI(), url, DEFAULT_LIMIT) {

            @Override
            protected BoxRetentionPolicyAssignment.Info factory(JsonObject jsonObject) {
                BoxRetentionPolicyAssignment assignment
                        = new BoxRetentionPolicyAssignment(getAPI(), jsonObject.get("id").asString());
                return assignment.new Info(jsonObject);
            }

        };
    }

    /**
     * Contains information about the retention policy.
     */
    public class Info extends BoxResource.Info {

        /**
         * @see #getPolicyName()
         */
        private String policyName;

        /**
         * @see #getPolicyType()
         */
        private String policyType;

        /**
         * @see #getRetentionLength()
         */
        private int retentionLength;

        /**
         * @see #getDispositionAction()
         */
        private String dispositionAction;

        /**
         * @see #getStatus()
         */
        private String status;

        /**
         * @see #getCreatedBy()
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
         * Constructs an empty Info object.
         */
        public Info() {
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
            return BoxRetentionPolicy.this;
        }

        /**
         * Gets the name given to the retention policy.
         * @return name given to the retention policy.
         */
        public String getPolicyName() {
            return this.policyName;
        }

        /**
         * Gets the type of the retention policy.
         * A retention policy type can either be "finite",
         * where a specific amount of time to retain the content is known upfront,
         * or "indefinite", where the amount of time to retain the content is still unknown.
         * @return the type of the retention policy.
         */
        public String getPolicyType() {
            return this.policyType;
        }

        /**
         * Gets the length of the retention policy. This length specifies the duration
         * in days that the retention policy will be active for after being assigned to content.
         * @return the length of the retention policy.
         */
        public int getRetentionLength() {
            return this.retentionLength;
        }

        /**
         * Gets the disposition action of the retention policy.
         * This action can be "permanently_delete", or "remove_retention".
         * @return the disposition action of the retention policy.
         */
        public String getDispositionAction() {
            return this.dispositionAction;
        }

        /**
         * Gets the status of the retention policy.
         * The status can be "active" or "retired".
         * @return the status of the retention policy.
         */
        public String getStatus() {
            return this.status;
        }

        /**
         * Gets info about the user created the retention policy.
         * @return info about the user created the retention policy.
         */
        public BoxUser.Info getCreatedBy() {
            return this.createdBy;
        }

        /**
         * Gets the time that the retention policy was created.
         * @return the time that the retention policy was created.
         */
        public Date getCreatedAt() {
            return this.createdAt;
        }

        /**
         * Gets the time that the retention policy was last modified.
         * @return the time that the retention policy was last modified.
         */
        public Date getModifiedAt() {
            return this.modifiedAt;
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
                } else if (memberName.equals("policy_type")) {
                    this.policyType = value.asString();
                } else if (memberName.equals("retention_length")) {
                    this.retentionLength = value.asInt();
                } else if (memberName.equals("disposition_action")) {
                    this.dispositionAction = value.asString();
                } else if (memberName.equals("status")) {
                    this.status = value.asString();
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
                }
            } catch (ParseException e) {
                assert false : "A ParseException indicates a bug in the SDK.";
            }
        }
    }
}
