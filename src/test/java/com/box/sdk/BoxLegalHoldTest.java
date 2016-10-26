package com.box.sdk;

import java.text.ParseException;
import java.util.Date;
import java.util.Iterator;

import org.junit.Assert;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import com.eclipsesource.json.JsonObject;

/**
 * {@link BoxLegalHold} related unit tests.
 */
public class BoxLegalHoldTest {

    /**
     * Unit test for {@link BoxLegalHold#getInfo(String...)}
     */
    @Test
    @Category(UnitTest.class)
    public void testGetInfoSendsCorrectRequest() {
        BoxAPIConnection api = new BoxAPIConnection("");
        api.setRequestInterceptor(new RequestInterceptor() {
            @Override
            public BoxAPIResponse onRequest(BoxAPIRequest request) {
                Assert.assertEquals("https://api.box.com/2.0/legal_hold_policies/0?fields=description%2Cstatus",
                        request.getUrl().toString());
                return new BoxJSONResponse() {
                    @Override
                    public String getJSON() {
                        return "{\"id\": \"0\"}";
                    }
                };
            }
        });

        BoxLegalHold policy = new BoxLegalHold(api, "0");
        policy.getInfo("description", "status");
    }

    /**
     * Unit test for {@link BoxLegalHold#getInfo(String...)}
     */
    @Test
    @Category(UnitTest.class)
    public void testGetInfoParseAllFieldsCorrectly() throws ParseException {
        final String id = "166757";
        final String name = "Policy 4";
        final String description = "Postman created policy";
        final String status = "active";
        final int assignmentsUser = 1;
        final int assignmentsFolder = 2;
        final int assignmentsFile = 3;
        final int assignmentsFileVersion = 4;
        final String userID = "2030388321";
        final String userName = "Steve Boxuser";
        final String userLogin = "steve@box.com";
        final Date createdAt = BoxDateFormat.parse("2016-05-18T10:28:45-07:00");
        final Date modifiedAt = BoxDateFormat.parse("2016-05-18T11:25:59-07:00");
        final Date deletedAt = null;
        final Date filterStartedAt = BoxDateFormat.parse("2016-05-17T01:00:00-07:00");
        final Date filterEndedAt = BoxDateFormat.parse("2016-05-21T01:00:00-07:00");
        final String releaseNote = "non-empty note";

        final JsonObject fakeJSONResponse = JsonObject.readFrom("{\n"
                + "  \"type\": \"legal_hold_policy\",\n"
                + "  \"id\": \"166757\",\n"
                + "  \"policy_name\": \"Policy 4\",\n"
                + "  \"description\": \"Postman created policy\",\n"
                + "  \"status\": \"active\",\n"
                + "  \"assignment_counts\": {\n"
                + "    \"user\": 1,\n"
                + "    \"folder\": 2,\n"
                + "    \"file\": 3,\n"
                + "    \"file_version\": 4\n"
                + "  },\n"
                + "  \"created_by\": {\n"
                + "    \"type\": \"user\",\n"
                + "    \"id\": \"2030388321\",\n"
                + "    \"name\": \"Steve Boxuser\",\n"
                + "    \"login\": \"steve@box.com\"\n"
                + "  },\n"
                + "  \"created_at\": \"2016-05-18T10:28:45-07:00\",\n"
                + "  \"modified_at\": \"2016-05-18T11:25:59-07:00\",\n"
                + "  \"deleted_at\": null,\n"
                + "  \"filter_started_at\": \"2016-05-17T01:00:00-07:00\",\n"
                + "  \"filter_ended_at\": \"2016-05-21T01:00:00-07:00\",\n"
                + "  \"release_notes\": \"non-empty note\"\n"
                + "}");

        BoxAPIConnection api = new BoxAPIConnection("");
        api.setRequestInterceptor(JSONRequestInterceptor.respondWith(fakeJSONResponse));

        BoxLegalHold policy = new BoxLegalHold(api, id);
        BoxLegalHold.Info info = policy.getInfo();
        Assert.assertEquals(id, info.getID());
        Assert.assertEquals(name, info.getPolicyName());
        Assert.assertEquals(description, info.getDescription());
        Assert.assertEquals(status, info.getStatus());
        Assert.assertEquals(assignmentsUser, info.getAssignmentCountUser());
        Assert.assertEquals(assignmentsFolder, info.getAssignmentCountFolder());
        Assert.assertEquals(assignmentsFile, info.getAssignmentCountFile());
        Assert.assertEquals(assignmentsFileVersion, info.getAssignmentCountFileVersion());
        Assert.assertEquals(userID, info.getCreatedBy().getID());
        Assert.assertEquals(userName, info.getCreatedBy().getName());
        Assert.assertEquals(userLogin, info.getCreatedBy().getLogin());
        Assert.assertEquals(createdAt, info.getCreatedAt());
        Assert.assertEquals(modifiedAt, info.getModifiedAt());
        Assert.assertEquals(deletedAt, info.getDeletedAt());
        Assert.assertEquals(filterStartedAt, info.getFilterStartedAt());
        Assert.assertEquals(filterEndedAt, info.getFilterEndedAt());
        Assert.assertEquals(releaseNote, info.getReleaseNotes());
    }

    /**
     * Unit test for {@link BoxLegalHold#assignTo(BoxResource)}
     */
    @Test
    @Category(UnitTest.class)
    public void testAssignToSendsCorrectJSON() {
        final String policyID = "0";
        final String resourceType = BoxLegalHoldAssignment.TYPE_FILE_VERSION;
        final String resourceID = "1";

        BoxAPIConnection api = new BoxAPIConnection("");
        api.setRequestInterceptor(new JSONRequestInterceptor() {
            @Override
            protected BoxAPIResponse onJSONRequest(BoxJSONRequest request, JsonObject json) {
                Assert.assertEquals("https://api.box.com/2.0/legal_hold_policy_assignments",
                        request.getUrl().toString());
                Assert.assertEquals(policyID, json.get("policy_id").asString());
                Assert.assertEquals(resourceType, json.get("assign_to").asObject().get("type").asString());
                Assert.assertEquals(resourceID, json.get("assign_to").asObject().get("id").asString());
                return new BoxJSONResponse() {
                    @Override
                    public String getJSON() {
                        return "{\"id\": \"0\"}";
                    }
                };
            }
        });

        BoxLegalHold policy = new BoxLegalHold(api, "0");
        BoxFileVersion version = new BoxFileVersion(api, "{\"id\": \"1\"}", "2");
        policy.assignTo(version);
    }

    /**
     * Unit test for {@link BoxLegalHold#getAssignments(String...)}
     */
    @Test
    @Category(UnitTest.class)
    public void testGetAssignmentsSendsCorrectRequestWithFields() {
        BoxAPIConnection api = new BoxAPIConnection("");
        api.setRequestInterceptor(new RequestInterceptor() {
            @Override
            public BoxAPIResponse onRequest(BoxAPIRequest request) {
                Assert.assertEquals("https://api.box.com/2.0/legal_hold_policies/0/assignments?fields=assigned_at",
                        request.getUrl().toString());
                return new BoxJSONResponse() {
                    @Override
                    public String getJSON() {
                        return "{\"entries\": []}";
                    }
                };
            }
        });

        BoxLegalHold policy = new BoxLegalHold(api, "0");
        Iterator<BoxLegalHoldAssignment.Info> iterator = policy.getAssignments("assigned_at").iterator();
        iterator.hasNext();
    }

    /**
     * Unit test for {@link BoxLegalHold#getAssignments(String...)}
     */
    @Test
    @Category(UnitTest.class)
    public void testGetAssignmentsSendsCorrectRequestWithOptionalParams() {
        BoxAPIConnection api = new BoxAPIConnection("");
        api.setRequestInterceptor(new RequestInterceptor() {
            @Override
            public BoxAPIResponse onRequest(BoxAPIRequest request) {
                Assert.assertEquals("https://api.box.com/2.0/legal_hold_policies/0/assignments?assign_to_type=folder&assign_to_id=1&limit=99",
                        request.getUrl().toString());
                return new BoxJSONResponse() {
                    @Override
                    public String getJSON() {
                        return "{\"entries\": []}";
                    }
                };
            }
        });

        BoxLegalHold policy = new BoxLegalHold(api, "0");
        Iterator<BoxLegalHoldAssignment.Info> iterator
                = policy.getAssignments(BoxResource.getResourceType(BoxFolder.class), "1", 99).iterator();
        iterator.hasNext();
    }
}
