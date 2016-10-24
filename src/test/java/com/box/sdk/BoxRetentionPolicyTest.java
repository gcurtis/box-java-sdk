package com.box.sdk;

import java.text.ParseException;
import java.util.Date;

import org.junit.Assert;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import com.eclipsesource.json.JsonObject;

/**
 * {@link BoxRetentionPolicy} related unit tests.
 */
public class BoxRetentionPolicyTest {

    /**
     * Unit test for {@link BoxRetentionPolicy#createIndefinitePolicy(BoxAPIConnection, String)}
     */
    @Test
    @Category(UnitTest.class)
    public void testCreateIndefinitePolicySendsCorrectJson() {
        final String name = "non-empty name";
        final String type = "indefinite";
        final String action = BoxRetentionPolicy.ACTION_REMOVE_RETENTION;

        BoxAPIConnection api = new BoxAPIConnection("");
        api.setRequestInterceptor(new JSONRequestInterceptor() {
            @Override
            protected BoxAPIResponse onJSONRequest(BoxJSONRequest request, JsonObject json) {
                Assert.assertEquals("https://api.box.com/2.0/retention_policies", request.getUrl().toString());
                Assert.assertEquals(name, json.get("policy_name").asString());
                Assert.assertEquals(type, json.get("policy_type").asString());
                Assert.assertEquals(action, json.get("disposition_action").asString());
                return new BoxJSONResponse() {
                    @Override
                    public String getJSON() {
                        return "{\"id\": \"0\"}";
                    }
                };
            }
        });

        BoxRetentionPolicy.createIndefinitePolicy(api, name);
    }

    /**
     * Unit test for {@link BoxRetentionPolicy#createFinitePolicy(BoxAPIConnection, String, int, String)}
     */
    @Test
    @Category(UnitTest.class)
    public void testCreateFinitePolicySendsCorrectJson() {
        final String name = "non-empty name";
        final String type = "finite";
        final String action = BoxRetentionPolicy.ACTION_PERMANENTLY_DELETE;
        final int length = 1;

        BoxAPIConnection api = new BoxAPIConnection("");
        api.setRequestInterceptor(new JSONRequestInterceptor() {
            @Override
            protected BoxAPIResponse onJSONRequest(BoxJSONRequest request, JsonObject json) {
                Assert.assertEquals("https://api.box.com/2.0/retention_policies", request.getUrl().toString());
                Assert.assertEquals(name, json.get("policy_name").asString());
                Assert.assertEquals(type, json.get("policy_type").asString());
                Assert.assertEquals(action, json.get("disposition_action").asString());
                Assert.assertEquals(length, json.get("retention_length").asInt());
                return new BoxJSONResponse() {
                    @Override
                    public String getJSON() {
                        return "{\"id\": \"0\"}";
                    }
                };
            }
        });

        BoxRetentionPolicy.createFinitePolicy(api, name, length, action);
    }

    /**
     * Unit test for {@link BoxRetentionPolicy#createFinitePolicy(BoxAPIConnection, String, int, String)}
     */
    @Test
    @Category(UnitTest.class)
    public void testCreatePolicyParseAllFieldsCorrectly() throws ParseException {
        final String id = "123456789";
        final String name = "Tax Documents";
        final String type = "finite";
        final int length = 365;
        final String action = BoxRetentionPolicy.ACTION_PERMANENTLY_DELETE;
        final String status = "active";
        final String userID = "11993747";
        final String userName = "Sean";
        final String userLogin = "sean@box.com";
        final Date createdAt = BoxDateFormat.parse("2015-05-01T11:12:54-07:00");
        final Date modifiedAt = null;

        final JsonObject fakeJSONResponse = JsonObject.readFrom("{\n"
                + "  \"type\": \"retention_policy\",\n"
                + "  \"id\": \"123456789\",\n"
                + "  \"policy_name\": \"Tax Documents\",\n"
                + "  \"policy_type\": \"finite\",\n"
                + "  \"retention_length\": 365,\n"
                + "  \"disposition_action\": \"permanently_delete\",\n"
                + "  \"status\": \"active\",\n"
                + "  \"created_by\": {\n"
                + "    \"type\": \"user\",\n"
                + "    \"id\": \"11993747\",\n"
                + "    \"name\": \"Sean\",\n"
                + "    \"login\": \"sean@box.com\"\n"
                + "  },\n"
                + "  \"created_at\": \"2015-05-01T11:12:54-07:00\",\n"
                + "  \"modified_at\": null \n"
                + "}");

        BoxAPIConnection api = new BoxAPIConnection("");
        api.setRequestInterceptor(JSONRequestInterceptor.respondWith(fakeJSONResponse));

        BoxRetentionPolicy.Info info = BoxRetentionPolicy.createFinitePolicy(api, name, length, action);
        Assert.assertEquals(id, info.getID());
        Assert.assertEquals(name, info.getPolicyName());
        Assert.assertEquals(type, info.getPolicyType());
        Assert.assertEquals(length, info.getRetentionLength());
        Assert.assertEquals(action, info.getDispositionAction());
        Assert.assertEquals(status, info.getStatus());
        Assert.assertEquals(userID, info.getCreatedBy().getID());
        Assert.assertEquals(userName, info.getCreatedBy().getName());
        Assert.assertEquals(userLogin, info.getCreatedBy().getLogin());
        Assert.assertEquals(createdAt, info.getCreatedAt());
        Assert.assertEquals(modifiedAt, info.getModifiedAt());
    }

    /**
     * Unit test for {@link BoxRetentionPolicy#updateInfo(BoxRetentionPolicy.Info)} )}
     */
    @Test
    @Category(UnitTest.class)
    public void testUpdateInfoSendsCorrectJson() {
        final String name = "Non-empty name";
        final String action = BoxRetentionPolicy.ACTION_REMOVE_RETENTION;
        final String status = BoxRetentionPolicy.STATUS_RETIRED;

        BoxAPIConnection api = new BoxAPIConnection("");
        api.setRequestInterceptor(new JSONRequestInterceptor() {
            @Override
            protected BoxAPIResponse onJSONRequest(BoxJSONRequest request, JsonObject json) {
                Assert.assertEquals("https://api.box.com/2.0/retention_policies/0", request.getUrl().toString());
                Assert.assertEquals(name, json.get("policy_name").asString());
                Assert.assertEquals(action, json.get("disposition_action").asString());
                Assert.assertEquals(status, json.get("status").asString());
                return new BoxJSONResponse() {
                    @Override
                    public String getJSON() {
                        return "{}";
                    }
                };
            }
        });

        BoxRetentionPolicy policy = new BoxRetentionPolicy(api, "0");
        BoxRetentionPolicy.Info info = policy.new Info();
        info.addPendingChange("policy_name", name);
        info.addPendingChange("disposition_action", action);
        info.addPendingChange("status", status);
        policy.updateInfo(info);
    }

    /**
     * Unit test for {@link BoxRetentionPolicy#updateInfo(BoxRetentionPolicy.Info)} )}
     */
    @Test
    @Category(UnitTest.class)
    public void testUpdateInfoParseAllFieldsCorrectly() throws ParseException {
        final String id = "123456789";
        final String name = "Tax Documents";
        final String type = "finite";
        final int length = 365;
        final String action = BoxRetentionPolicy.ACTION_REMOVE_RETENTION;
        final String status = "active";
        final String userID = "11993747";
        final String userName = "Sean";
        final String userLogin = "sean@box.com";
        final Date createdAt = BoxDateFormat.parse("2015-05-01T11:12:54-07:00");
        final Date modifiedAt = BoxDateFormat.parse("2015-06-08T11:11:50-07:00");

        final JsonObject fakeJSONResponse = JsonObject.readFrom("{     \n"
                + "  \"type\": \"retention_policy\",     \n"
                + "  \"id\": \"123456789\",     \n"
                + "  \"policy_name\": \"Tax Documents\", \n"
                + "  \"policy_type\": \"finite\",     \n"
                + "  \"retention_length\": 365,     \n"
                + "  \"disposition_action\": \"remove_retention\",  \n"
                + "  \"status\": \"active\",     \n"
                + "  \"created_by\": {\n"
                + "    \"type\": \"user\",\n"
                + "    \"id\": \"11993747\",\n"
                + "    \"name\": \"Sean\",\n"
                + "    \"login\": \"sean@box.com\"\n"
                + "  },\n"
                + "  \"created_at\": \"2015-05-01T11:12:54-07:00\",\n"
                + "  \"modified_at\": \"2015-06-08T11:11:50-07:00\" \n"
                + "}");

        BoxAPIConnection api = new BoxAPIConnection("");
        api.setRequestInterceptor(JSONRequestInterceptor.respondWith(fakeJSONResponse));

        BoxRetentionPolicy policy = new BoxRetentionPolicy(api, "123456789");
        BoxRetentionPolicy.Info info = policy.new Info();
        info.addPendingChange("policy_name", name);
        policy.updateInfo(info);
        Assert.assertEquals(id, info.getID());
        Assert.assertEquals(name, info.getPolicyName());
        Assert.assertEquals(type, info.getPolicyType());
        Assert.assertEquals(length, info.getRetentionLength());
        Assert.assertEquals(action, info.getDispositionAction());
        Assert.assertEquals(status, info.getStatus());
        Assert.assertEquals(userID, info.getCreatedBy().getID());
        Assert.assertEquals(userName, info.getCreatedBy().getName());
        Assert.assertEquals(userLogin, info.getCreatedBy().getLogin());
        Assert.assertEquals(createdAt, info.getCreatedAt());
        Assert.assertEquals(modifiedAt, info.getModifiedAt());
    }
}
