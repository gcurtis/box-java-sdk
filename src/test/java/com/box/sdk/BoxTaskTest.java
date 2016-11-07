package com.box.sdk;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import org.junit.Assert;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import com.eclipsesource.json.JsonObject;

public class BoxTaskTest {

    /**
     * Unit test for {@link BoxTask#}
     */
    @Test
    @Category(UnitTest.class)
    public void testGetInfoSendsCorrectRequest() {
        BoxAPIConnection api = new BoxAPIConnection("");
        api.setRequestInterceptor(new RequestInterceptor() {
            @Override
            public BoxAPIResponse onRequest(BoxAPIRequest request) {
                Assert.assertEquals("https://api.box.com/2.0/tasks/0",
                        request.getUrl().toString());
                return new BoxJSONResponse() {
                    @Override
                    public String getJSON() {
                        return "{\"id\": \"0\"}";
                    }
                };
            }
        });

        new BoxTask(api, "0").getInfo();
    }

    /**
     * Unit test for {@link BoxTask#}
     */
    @Test
    @Category(UnitTest.class)
    public void testGetInfoParseAllFieldsCorrectly() throws ParseException {
        final String id = "1839355";
        final String itemID = "7287087200";
        final String itemSequenceID = "0";
        final String itemEtag = "0";
        final String itemSha1 = "0bbd79a105c504f99573e3799756debba4c760cd";
        final String itemName = "box-logo.png";
        final Date dueAt = BoxDateFormat.parse("2014-04-03T11:09:43-07:00");
        final BoxTask.Action action = BoxTask.Action.REVIEW;
        final String message = "REVIEW PLZ K THX";
        final int assignmentCount = 0;
        final boolean isCompleted = false;
        final String createdByID = "11993747";
        final String createdByName = "☁ sean ☁";
        final String createdByLogin = "sean@box.com";
        final Date createdAt = BoxDateFormat.parse("2013-04-03T11:12:54-07:00");

        final JsonObject fakeJSONResponse = JsonObject.readFrom("{\n"
                + "    \"type\": \"task\",\n"
                + "    \"id\": \"1839355\",\n"
                + "    \"item\": {\n"
                + "        \"type\": \"file\",\n"
                + "        \"id\": \"7287087200\",\n"
                + "        \"sequence_id\": \"0\",\n"
                + "        \"etag\": \"0\",\n"
                + "        \"sha1\": \"0bbd79a105c504f99573e3799756debba4c760cd\",\n"
                + "        \"name\": \"box-logo.png\"\n"
                + "    },\n"
                + "    \"due_at\": \"2014-04-03T11:09:43-07:00\",\n"
                + "    \"action\": \"review\",\n"
                + "    \"message\": \"REVIEW PLZ K THX\",\n"
                + "    \"task_assignment_collection\": {\n"
                + "        \"total_count\": 0,\n"
                + "        \"entries\": []\n"
                + "    },\n"
                + "    \"is_completed\": false,\n"
                + "    \"created_by\": {\n"
                + "        \"type\": \"user\",\n"
                + "        \"id\": \"11993747\",\n"
                + "        \"name\": \"☁ sean ☁\",\n"
                + "        \"login\": \"sean@box.com\"\n"
                + "    },\n"
                + "    \"created_at\": \"2013-04-03T11:12:54-07:00\"\n"
                + "}");

        BoxAPIConnection api = new BoxAPIConnection("");
        api.setRequestInterceptor(JSONRequestInterceptor.respondWith(fakeJSONResponse));

        BoxTask.Info info = new BoxTask(api, id).getInfo();
        Assert.assertEquals(id, info.getID());
        Assert.assertEquals(itemID, info.getItem().getID());
        Assert.assertEquals(itemSequenceID, info.getItem().getSequenceID());
        Assert.assertEquals(itemEtag, info.getItem().getEtag());
        Assert.assertEquals(itemSha1, info.getItem().getSha1());
        Assert.assertEquals(itemName, info.getItem().getName());
        Assert.assertEquals(dueAt, info.getDueAt());
        Assert.assertEquals(action, info.getAction());
        Assert.assertEquals(message, info.getMessage());
        Assert.assertEquals(assignmentCount, info.getTaskAssignments().size());
        Assert.assertEquals(isCompleted, info.isCompleted());
        Assert.assertEquals(createdByID, info.getCreatedBy().getID());
        Assert.assertEquals(createdByName, info.getCreatedBy().getName());
        Assert.assertEquals(createdByLogin, info.getCreatedBy().getLogin());
        Assert.assertEquals(createdAt, info.getCreatedAt());
    }

    @Test
    @Category(IntegrationTest.class)
    public void updateInfoSucceeds() {
        BoxAPIConnection api = new BoxAPIConnection(TestConfig.getAccessToken());
        BoxFolder rootFolder = BoxFolder.getRootFolder(api);
        String fileName = "[updateInfoSucceeds] Test File.txt";
        byte[] fileBytes = "Non-empty string".getBytes(StandardCharsets.UTF_8);
        String originalMessage = "Original message";
        String changedMessage = "Changed message";

        InputStream uploadStream = new ByteArrayInputStream(fileBytes);
        BoxFile uploadedFile = rootFolder.uploadFile(uploadStream, fileName).getResource();

        Calendar calendar = new GregorianCalendar();
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        Date dueAt = calendar.getTime();

        BoxTask.Info taskInfo = uploadedFile.addTask(BoxTask.Action.REVIEW, originalMessage, dueAt);

        BoxTask task = taskInfo.getResource();
        taskInfo.setMessage(changedMessage);
        taskInfo.setDueAt(dueAt);
        task.updateInfo(taskInfo);

        assertThat(taskInfo.getMessage(), is(equalTo(changedMessage)));
        assertThat(taskInfo.getDueAt(), is(equalTo(dueAt)));

        uploadedFile.delete();
    }
}
