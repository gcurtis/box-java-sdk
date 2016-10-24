package com.box.sdk;

import java.net.URL;
import java.text.ParseException;
import java.util.Date;

import com.eclipsesource.json.JsonObject;
import com.eclipsesource.json.JsonValue;

/**
 * Represents a legal hold policy assignment.
 * Legal hold assignments are used to assign legal hold policies to custodians, folders, files, or file versions.
 *
 * <p>Unless otherwise noted, the methods in this class can throw an unchecked {@link BoxAPIException} (unchecked
 * meaning that the compiler won't force you to handle it) if an error occurs. If you wish to implement custom error
 * handling for errors related to the Box REST API, you should capture this exception explicitly.</p>
 */
@BoxResourceType("legal_hold_assignment")
public class BoxLegalHoldAssignment extends BoxResource {

    /**
     * The URL template used for operation with legal hold policy assignment with given ID.
     */
    private static final URLTemplate LEGAL_HOLD_ASSIGNMENT_URL_TEMPLATE
            = new URLTemplate("legal_hold_policy_assignments/%s");

    /**
     * Constructs a BoxLegalHoldAssignment for a resource with a given ID.
     *
     * @param api the API connection to be used by the resource.
     * @param id  the ID of the resource.
     */
    public BoxLegalHoldAssignment(BoxAPIConnection api, String id) {
        super(api, id);
    }

    /**
     * @param fields the fields to retrieve.
     * @return information about this retention policy.
     */
    public BoxLegalHoldAssignment.Info getInfo(String ... fields) {
        QueryStringBuilder builder = new QueryStringBuilder();
        if (fields.length > 0) {
            builder.appendParam("fields", fields);
        }
        URL url = LEGAL_HOLD_ASSIGNMENT_URL_TEMPLATE.buildWithQuery(
                this.getAPI().getBaseURL(), builder.toString(), this.getID());
        BoxAPIRequest request = new BoxAPIRequest(this.getAPI(), url, "GET");
        BoxJSONResponse response = (BoxJSONResponse) request.send();
        JsonObject responseJSON = JsonObject.readFrom(response.getJSON());
        return new Info(responseJSON);
    }

    /**
     * Contains information about the legal hold policy.
     */
    public class Info extends BoxResource.Info {

        /**
         * @see #getLegalHold()
         */
        private BoxLegalHold.Info legalHold;

        /**
         * @see #getAssignedBy()
         */
        private BoxUser.Info assignedBy;

        /**
         * @see #getAssignedAt()
         */
        private Date assignedAt;

        /**
         * @see #getDeletedAt()
         */
        private Date deletedAt;

        /**
         * @see #getAssignedToType()
         */
        private String assignedToType;

        /**
         * @see #getAssignedToID()
         */
        private String assignedToID;

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
            return BoxLegalHoldAssignment.this;
        }

        /**
         * @return info about the policy that this legal hold policy assignment is part of.
         */
        public BoxLegalHold.Info getLegalHold() {
            return this.legalHold;
        }

        /**
         * @return the info about the user who created that legal hold policy assignment.
         */
        public BoxUser.Info getAssignedBy() {
            return this.assignedBy;
        }

        /**
         * @return the time that the legal hold policy assignment was created.
         */
        public Date getAssignedAt() {
            return this.assignedAt;
        }

        /**
         * @return the time that the assignment release request was sent.
         */
        public Date getDeletedAt() {
            return this.deletedAt;
        }

        /**
         * @return the entity type that this is assigned to.
         */
        public String getAssignedToType() {
            return this.assignedToType;
        }

        /**
         * @return the entity id that this is assigned to.
         */
        public String getAssignedToID() {
            return this.assignedToID;
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
                if (memberName.equals("legal_hold_policy")) {
                    JsonObject policyJSON = value.asObject();
                    if (this.legalHold == null) {
                        String policyID = policyJSON.get("id").asString();
                        BoxLegalHold policy = new BoxLegalHold(getAPI(), policyID);
                        this.legalHold = policy.new Info(policyJSON);
                    } else {
                        this.legalHold.update(policyJSON);
                    }
                } else if (memberName.equals("assigned_to")) {
                    JsonObject assignmentJSON = value.asObject();
                    this.assignedToType = assignmentJSON.get("type").asString();
                    this.assignedToID = assignmentJSON.get("id").asString();
                } else if (memberName.equals("assigned_by")) {
                    JsonObject userJSON = value.asObject();
                    if (this.assignedBy == null) {
                        String userID = userJSON.get("id").asString();
                        BoxUser user = new BoxUser(getAPI(), userID);
                        this.assignedBy = user.new Info(userJSON);
                    } else {
                        this.assignedBy.update(userJSON);
                    }
                } else if (memberName.equals("assigned_at")) {
                    this.assignedAt = BoxDateFormat.parse(value.asString());
                } else if (memberName.equals("deleted_at")) {
                    this.deletedAt = BoxDateFormat.parse(value.asString());
                }
            } catch (ParseException e) {
                assert false : "A ParseException indicates a bug in the SDK.";
            }
        }
    }
}
