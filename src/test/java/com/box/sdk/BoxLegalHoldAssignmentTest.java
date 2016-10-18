package com.box.sdk;

import java.text.ParseException;
import java.util.Date;

import org.junit.Assert;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import com.eclipsesource.json.JsonObject;

/**
 * {@link BoxLegalHoldAssignment} related unit tests.
 */
public class BoxLegalHoldAssignmentTest {

    /**
     * Unit test for {@link BoxLegalHoldAssignment#getInfo()}
     */
    @Test
    @Category(UnitTest.class)
    public void testGetInfoSendsCorrectRequest() {
        BoxAPIConnection api = new BoxAPIConnection("");
        api.setRequestInterceptor(new RequestInterceptor() {
            @Override
            public BoxAPIResponse onRequest(BoxAPIRequest request) {
                Assert.assertEquals("https://api.box.com/2.0/legal_hold_policy_assignments/0",
                        request.getUrl().toString());
                return new BoxJSONResponse() {
                    @Override
                    public String getJSON() {
                        return "{\"id\": \"0\"}";
                    }
                };
            }
        });

        BoxLegalHoldAssignment assignment = new BoxLegalHoldAssignment(api, "0");
        assignment.getInfo();
    }

    /**
     * Unit test for {@link BoxLegalHoldAssignment#getInfo()}
     */
    @Test
    @Category(UnitTest.class)
    public void testGetInfoParseAllFieldsCorrectly() throws ParseException {
        final String id = "255473";
        final String policyID = "166757";
        final String policyName = "Bug Bash 5-12 Policy 3 updated";
        final String assignedToType = "user";
        final String assignedToID = "2030388321";
        final String assignedByID = "2030388322";
        final String assignedByName = "Steve Boxuser";
        final String assignedByLogin = "sboxuser@box.com";
        final Date assignedAt = BoxDateFormat.parse("2016-05-18T10:32:19-07:00");
        final Date deletedAt = null;

        final JsonObject fakeJSONResponse = JsonObject.readFrom("{\n"
                + "  \"type\": \"legal_hold_policy_assignment\",\n"
                + "  \"id\": \"255473\",\n"
                + "  \"legal_hold_policy\": {\n"
                + "    \"type\": \"legal_hold_policy\",\n"
                + "    \"id\": \"166757\",\n"
                + "    \"policy_name\": \"Bug Bash 5-12 Policy 3 updated\"\n"
                + "  },\n"
                + "  \"assigned_to\": {\n"
                + "    \"type\": \"user\",\n"
                + "    \"id\": \"2030388321\"\n"
                + "  },\n"
                + "  \"assigned_by\": {\n"
                + "    \"type\": \"user\",\n"
                + "    \"id\": \"2030388322\",\n"
                + "    \"name\": \"Steve Boxuser\",\n"
                + "    \"login\": \"sboxuser@box.com\"\n"
                + "  },\n"
                + "  \"assigned_at\": \"2016-05-18T10:32:19-07:00\",\n"
                + "  \"deleted_at\": null\n"
                + "}");

        BoxAPIConnection api = new BoxAPIConnection("");
        api.setRequestInterceptor(JSONRequestInterceptor.respondWith(fakeJSONResponse));

        BoxLegalHoldAssignment assignment = new BoxLegalHoldAssignment(api, id);
        BoxLegalHoldAssignment.Info info = assignment.getInfo();
        Assert.assertEquals(id, info.getID());
        Assert.assertEquals(policyID, info.getLegalHold().getID());
        Assert.assertEquals(policyName, info.getLegalHold().getPolicyName());
        Assert.assertEquals(assignedToType, info.getAssignedToType());
        Assert.assertEquals(assignedToID, info.getAssignedToID());
        Assert.assertEquals(assignedByID, info.getAssignedBy().getID());
        Assert.assertEquals(assignedByName, info.getAssignedBy().getName());
        Assert.assertEquals(assignedByLogin, info.getAssignedBy().getLogin());
        Assert.assertEquals(assignedAt, info.getAssignedAt());
        Assert.assertEquals(deletedAt, info.getDeletedAt());

    }
}
