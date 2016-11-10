package com.box.sdk;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.EnumSet;
import java.util.List;
import java.util.Scanner;
import java.util.TimeZone;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.isEmptyOrNullString;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.skyscreamer.jsonassert.JSONCompareMode.LENIENT;

import org.hamcrest.Matchers;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.containing;
import static com.github.tomakehurst.wiremock.client.WireMock.equalToJson;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlMatching;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;

import com.eclipsesource.json.JsonArray;
import com.eclipsesource.json.JsonObject;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.junit.WireMockRule;

/**
 * {@link BoxFolder} related tests.
 */
public class BoxFolderTest {

    /**
     * Wiremock
     */
    @Rule
    public final WireMockRule wireMockRule = new WireMockRule(8080);

    /**
     * Unit test of equality of two {@link BoxFolder} objects with the same ID.
     */
    @Test
    @Category(UnitTest.class)
    public void foldersWithSameIDAreEqual() {
        BoxAPIConnection api = new BoxAPIConnection("");
        BoxFolder folder1 = new BoxFolder(api, "1");
        BoxFolder folder2 = new BoxFolder(api, "1");

        assertThat(folder1, equalTo(folder2));
    }

    /**
     * Unit test for {@link BoxFolder#createFolder(String)}.
     */
    @Test
    @Category(UnitTest.class)
    public void createFolderSendsRequestWithRequiredFields() {
        BoxAPIConnection api = new BoxAPIConnection("");
        api.setBaseURL("http://localhost:8080/");
        BoxFolder rootFolder = BoxFolder.getRootFolder(api);
        String parentFolderID = rootFolder.getID();
        String createdFolderName = "[createFolderSendsRequestWithRequiredFields] Child Folder";

        stubFor(post(urlMatching("/folders"))
            .withRequestBody(equalToJson("{ \"name\": \"" + createdFolderName + "\", \"parent\": {\"id\": \""
                + parentFolderID + "\"} }", LENIENT))
            .willReturn(aResponse()
                .withHeader("Content-Type", "application/json")
                .withBody("{\"id\": \"0\"}")));

        rootFolder.createFolder(createdFolderName);
    }

    /**
     * Unit test for {@link BoxFolder.Info#Info(String)}.
     */
    @Test
    @Category(UnitTest.class)
    public void infoParsesMixedPermissionsCorrectly() {
        BoxAPIConnection api = new BoxAPIConnection("");
        String id = "id";

        EnumSet<BoxFolder.Permission> expectedPermissions = EnumSet.of(BoxFolder.Permission.CAN_UPLOAD,
            BoxFolder.Permission.CAN_DELETE, BoxFolder.Permission.CAN_INVITE_COLLABORATOR);

        JsonObject permissionsJSON = new JsonObject();
        permissionsJSON.add("can_download", false);
        permissionsJSON.add("can_upload", true);
        permissionsJSON.add("can_rename", false);
        permissionsJSON.add("can_delete", true);
        permissionsJSON.add("can_share", false);
        permissionsJSON.add("can_invite_collaborator", true);
        permissionsJSON.add("can_set_share_access", false);

        JsonObject folderJSON = new JsonObject();
        folderJSON.add("id", id);
        folderJSON.add("type", "folder");
        folderJSON.add("permissions", permissionsJSON);

        BoxFolder folder = new BoxFolder(api, id);
        BoxFolder.Info info = folder.new Info(folderJSON);

        assertThat(info.getPermissions(), is(equalTo(expectedPermissions)));
    }

    /**
     * Unit test for {@link BoxFolder#getChildrenRange(long, long, String...)}.
     */
    @Test
    @Category(UnitTest.class)
    public void getChildrenRangeRequestsCorrectOffsetLimitAndFields() {
        BoxAPIConnection api = new BoxAPIConnection("");
        api.setBaseURL("http://localhost:8080/");

        stubFor(get(urlPathEqualTo("/folders/0/items/"))
            .withQueryParam("offset", WireMock.equalTo("1"))
            .withQueryParam("limit", WireMock.equalTo("2"))
            .withQueryParam("fields", containing("name"))
            .withQueryParam("fields", containing("description"))
            .willReturn(aResponse()
                .withHeader("Content-Type", "application/json")
                .withBody("{\"total_count\": 3, \"entries\":[]}")));

        BoxFolder rootFolder = BoxFolder.getRootFolder(api);
        PartialCollection<BoxItem.Info> children = rootFolder.getChildrenRange(1, 2, "name", "description");

        assertThat(children.offset(), is(equalTo(1L)));
        assertThat(children.limit(), is(equalTo(2L)));
        assertThat(children.fullSize(), is(equalTo(3L)));
    }

    /**
     * Unit test for {@link BoxFolder#collaborate(BoxCollaborator, BoxCollaboration.Role)}.
     */
    @Test
    @Category(UnitTest.class)
    public void collaborateShouldSendCorrectJSONWhenCollaboratingWithAGroup() {
        final String folderID = "1";
        final String groupID = "2";
        final BoxCollaboration.Role role = BoxCollaboration.Role.CO_OWNER;

        final JsonObject fakeJSONResponse = new JsonObject()
            .add("type", "collaboration")
            .add("id", "0");

        BoxAPIConnection api = new BoxAPIConnection("");
        api.setRequestInterceptor(new JSONRequestInterceptor() {
            @Override
            protected BoxAPIResponse onJSONRequest(BoxJSONRequest request, JsonObject json) {
                JsonObject itemJSON = json.get("item").asObject();
                assertEquals(folderID, itemJSON.get("id").asString());

                JsonObject accessibleByJSON = json.get("accessible_by").asObject();
                assertEquals(groupID, accessibleByJSON.get("id").asString());
                assertEquals("group", accessibleByJSON.get("type").asString());
                assertNull(accessibleByJSON.get("login"));

                assertEquals(role.toJSONString(), json.get("role").asString());

                return new BoxJSONResponse() {
                    @Override
                    public String getJSON() {
                        return fakeJSONResponse.toString();
                    }
                };
            }
        });

        BoxGroup collaborator = new BoxGroup(api, groupID);
        BoxFolder folder = new BoxFolder(api, folderID);
        folder.collaborate(collaborator, BoxCollaboration.Role.CO_OWNER);
    }

    /**
     * Unit test for {@link BoxFolder#getCollaborations()}.
     */
    @Test
    @Category(UnitTest.class)
    public void getCollaborationsShouldParseGroupsCorrectly() {
        final String groupID = "non-empty ID";
        final String groupName = "non-empty name";

        final JsonObject fakeJSONResponse = new JsonObject()
            .add("total_count", 1)
            .add("entries", new JsonArray()
                .add(new JsonObject()
                    .add("type", "collaboration")
                    .add("id", "non-empty ID")
                    .add("accessible_by", new JsonObject()
                        .add("type", "group")
                        .add("id", groupID)
                        .add("name", groupName))));

        BoxAPIConnection api = new BoxAPIConnection("");
        api.setRequestInterceptor(new RequestInterceptor() {
            @Override
            public BoxAPIResponse onRequest(BoxAPIRequest request) {
                return new BoxJSONResponse() {
                    @Override
                    public String getJSON() {
                        return fakeJSONResponse.toString();
                    }
                };
            }
        });

        BoxFolder folder = new BoxFolder(api, "non-empty ID");
        for (BoxCollaboration.Info collaboration : folder.getCollaborations()) {
            BoxCollaborator.Info collaboratorInfo = collaboration.getAccessibleBy();
            assertTrue(collaboratorInfo instanceof BoxGroup.Info);

            BoxGroup.Info groupInfo = (BoxGroup.Info) collaboratorInfo;
            assertEquals(groupID, groupInfo.getID());
            assertEquals(groupName, groupInfo.getName());
        }
    }

    /**
     * Unit test for {@link BoxFolder#createWebLink(String, URL, String)}.
     */
    @Test
    @Category(UnitTest.class)
    public void testCreateWeblinkSendsCorrectJsonWithNameAndDescription() throws MalformedURLException {
        final String url = "https://www.box.com/home";
        final String parentFolderID = "0";
        final String name = "non-empty name";
        final String description = "non-empty description";

        final JsonObject fakeJSONResponse = new JsonObject()
                .add("type", "web_link")
                .add("id", "0");

        BoxAPIConnection api = new BoxAPIConnection("");
        api.setRequestInterceptor(new RequestInterceptor() {
            @Override
            public BoxAPIResponse onRequest(BoxAPIRequest request) {
                Assert.assertEquals("https://api.box.com/2.0/web_links", request.getUrl().toString());
                Scanner body = new Scanner(request.getBody()).useDelimiter("\n");
                JsonObject json = JsonObject.readFrom(body.next());
                body.close();
                Assert.assertEquals(url, json.get("url").asString());
                Assert.assertEquals(parentFolderID, json.get("parent").asObject().get("id").asString());
                Assert.assertEquals(name, json.get("name").asString());
                Assert.assertEquals(description, json.get("description").asString());

                return new BoxJSONResponse() {
                    @Override
                    public String getJSON() {
                        return fakeJSONResponse.toString();
                    }
                };
            }
        });

        new BoxFolder(api, "0").createWebLink(name, new URL(url), description);
    }

    /**
     * Unit test for {@link BoxFolder#createWebLink(URL)}.
     */
    @Test
    @Category(UnitTest.class)
    public void testCreateWeblinkSendsCorrectJsonWithoutNameAndDescription() throws MalformedURLException {
        final String url = "https://www.box.com/home";
        final String parentFolderID = "0";

        final JsonObject fakeJSONResponse = new JsonObject()
                .add("type", "web_link")
                .add("id", "0");

        BoxAPIConnection api = new BoxAPIConnection("");
        api.setRequestInterceptor(new RequestInterceptor() {
            @Override
            public BoxAPIResponse onRequest(BoxAPIRequest request) {
                Assert.assertEquals("https://api.box.com/2.0/web_links", request.getUrl().toString());
                JsonObject json = JsonObject.readFrom(new Scanner(request.getBody()).useDelimiter("\n").next());
                assertEquals(url, json.get("url").asString());
                assertEquals(parentFolderID, json.get("parent").asObject().get("id").asString());
                return new BoxJSONResponse() {
                    @Override
                    public String getJSON() {
                        return fakeJSONResponse.toString();
                    }
                };
            }
        });

        new BoxFolder(api, "0").createWebLink(new URL(url));
    }

    /**
     * Unit test for {@link BoxFolder#createWebLink(URL)}.
     */
    @Test
    @Category(UnitTest.class)
    public void testCreateWeblinkParseAllFieldsCorrectly() throws ParseException, MalformedURLException {
        final String id = "6742981";
        final String sequenceID = "0";
        final String etag = "0";
        final String name = "Box Website";
        final String url = "https://www.box.com";
        final String creatorID = "10523870";
        final String creatorName = "Ted Blosser";
        final String creatorLogin = "ted+demo@box.com";
        final Date createdAt = BoxDateFormat.parse("2015-05-07T14:31:16-07:00");
        final Date modifiedAt = BoxDateFormat.parse("2015-05-07T14:31:16-07:00");
        final String parentID = "848123342";
        final String parentSequenceID = "1";
        final String parentEtag = "1";
        final String parentName = "Documentation";
        final String description = "Cloud Content Management";
        final String itemStatus = "active";
        final Date trashedAt = null;
        final Date purgedAt = null;
        final BoxSharedLink sharedLink = null;
        final String pathID = "848123342";
        final String pathSequenceID = "1";
        final String pathEtag = "1";
        final String pathName = "Documentation";
        final String modifiedID = "10523870";
        final String modifiedName = "Ted Blosser";
        final String modifiedLogin = "ted+demo@box.com";
        final String ownerID = "10523870";
        final String ownerName = "Ted Blosser";
        final String ownerLogin = "ted+demo@box.com";

        final JsonObject fakeJSONResponse = JsonObject.readFrom("{\n"
                + "    \"type\": \"web_link\",\n"
                + "    \"id\": \"6742981\",\n"
                + "    \"sequence_id\": \"0\",\n"
                + "    \"etag\": \"0\",\n"
                + "    \"name\": \"Box Website\",\n"
                + "    \"url\": \"https://www.box.com\",\n"
                + "    \"created_by\": {\n"
                + "        \"type\": \"user\",\n"
                + "        \"id\": \"10523870\",\n"
                + "        \"name\": \"Ted Blosser\",\n"
                + "        \"login\": \"ted+demo@box.com\"\n"
                + "    },\n"
                + "    \"created_at\": \"2015-05-07T14:31:16-07:00\",\n"
                + "    \"modified_at\": \"2015-05-07T14:31:16-07:00\",\n"
                + "    \"parent\": {\n"
                + "        \"type\": \"folder\",\n"
                + "        \"id\": \"848123342\",\n"
                + "        \"sequence_id\": \"1\",\n"
                + "        \"etag\": \"1\",\n"
                + "        \"name\": \"Documentation\"\n"
                + "    },\n"
                + "    \"description\": \"Cloud Content Management\",\n"
                + "    \"item_status\": \"active\",\n"
                + "    \"trashed_at\": null,\n"
                + "    \"purged_at\": null,\n"
                + "    \"shared_link\": null,\n"
                + "    \"path_collection\": {\n"
                + "        \"total_count\": 1,\n"
                + "        \"entries\": [\n"
                + "            {\n"
                + "                \"type\": \"folder\",\n"
                + "                \"id\": \"848123342\",\n"
                + "                \"sequence_id\": \"1\",\n"
                + "                \"etag\": \"1\",\n"
                + "                \"name\": \"Documentation\"\n"
                + "            }\n"
                + "        ]\n"
                + "    },\n"
                + "    \"modified_by\": {\n"
                + "        \"type\": \"user\",\n"
                + "        \"id\": \"10523870\",\n"
                + "        \"name\": \"Ted Blosser\",\n"
                + "        \"login\": \"ted+demo@box.com\"\n"
                + "    },\n"
                + "    \"owned_by\": {\n"
                + "        \"type\": \"user\",\n"
                + "        \"id\": \"10523870\",\n"
                + "        \"name\": \"Ted Blosser\",\n"
                + "        \"login\": \"ted+demo@box.com\"\n"
                + "    }\n"
                + "}");

        BoxAPIConnection api = new BoxAPIConnection("");
        api.setRequestInterceptor(JSONRequestInterceptor.respondWith(fakeJSONResponse));

        BoxWebLink.Info info = new BoxFolder(api, "0").createWebLink(new URL(url));
        Assert.assertEquals(id, info.getID());
        Assert.assertEquals(sequenceID, info.getSequenceID());
        Assert.assertEquals(etag, info.getEtag());
        Assert.assertEquals(name, info.getName());
        Assert.assertEquals(url, info.getLinkURL().toString());
        Assert.assertEquals(createdAt, info.getCreatedAt());
        Assert.assertEquals(modifiedAt, info.getModifiedAt());
        Assert.assertEquals(description, info.getDescription());
        Assert.assertEquals(itemStatus, info.getItemStatus());
        Assert.assertEquals(trashedAt, info.getTrashedAt());
        Assert.assertEquals(purgedAt, info.getPurgedAt());
        Assert.assertEquals(sharedLink, info.getSharedLink());
        BoxUser.Info creatorInfo = info.getCreatedBy();
        Assert.assertEquals(creatorID, creatorInfo.getID());
        Assert.assertEquals(creatorName, creatorInfo.getName());
        Assert.assertEquals(creatorLogin, creatorInfo.getLogin());
        BoxUser.Info modifiedInfo = info.getModifiedBy();
        Assert.assertEquals(modifiedID, modifiedInfo.getID());
        Assert.assertEquals(modifiedName, modifiedInfo.getName());
        Assert.assertEquals(modifiedLogin, modifiedInfo.getLogin());
        BoxUser.Info ownerInfo = info.getOwnedBy();
        Assert.assertEquals(ownerID, ownerInfo.getID());
        Assert.assertEquals(ownerName, ownerInfo.getName());
        Assert.assertEquals(ownerLogin, ownerInfo.getLogin());
        BoxFolder.Info parentInfo = info.getParent();
        Assert.assertEquals(parentID, parentInfo.getID());
        Assert.assertEquals(parentSequenceID, parentInfo.getSequenceID());
        Assert.assertEquals(parentEtag, parentInfo.getEtag());
        Assert.assertEquals(parentName, parentInfo.getName());
        BoxFolder.Info pathInfo = info.getPathCollection().get(0);
        Assert.assertEquals(pathID, pathInfo.getID());
        Assert.assertEquals(pathSequenceID, pathInfo.getSequenceID());
        Assert.assertEquals(pathEtag, pathInfo.getEtag());
        Assert.assertEquals(pathName, pathInfo.getName());
    }

    @Test
    @Category(IntegrationTest.class)
    public void creatingAndDeletingFolderSucceeds() {
        BoxAPIConnection api = new BoxAPIConnection(TestConfig.getAccessToken());
        BoxFolder rootFolder = BoxFolder.getRootFolder(api);
        BoxFolder childFolder = rootFolder.createFolder("[creatingAndDeletingFolderSucceeds] Ĥȅľľő Ƒŕőďő")
            .getResource();

        assertThat(rootFolder, hasItem(Matchers.<BoxItem.Info>hasProperty("ID", equalTo(childFolder.getID()))));

        childFolder.delete(false);
        assertThat(rootFolder, not(hasItem(Matchers.<BoxItem.Info>hasProperty("ID", equalTo(childFolder.getID())))));
    }

    @Test
    @Category(IntegrationTest.class)
    public void getFolderInfoReturnsCorrectInfo() {
        BoxAPIConnection api = new BoxAPIConnection(TestConfig.getAccessToken());
        BoxUser currentUser = BoxUser.getCurrentUser(api);
        final String expectedName = "[getFolderInfoReturnsCorrectInfo] Child Folder";
        final String expectedCreatedByID = currentUser.getID();

        BoxFolder rootFolder = BoxFolder.getRootFolder(api);
        final String expectedParentFolderID = rootFolder.getID();
        final String expectedParentFolderName = rootFolder.getInfo().getName();

        BoxFolder childFolder = rootFolder.createFolder(expectedName).getResource();
        BoxFolder.Info info = childFolder.getInfo(BoxItem.ALL_FIELDS);

        String actualName = info.getName();
        String actualCreatedByID = info.getCreatedBy().getID();
        String actualParentFolderID = info.getParent().getID();
        String actualParentFolderName = info.getParent().getName();
        List<BoxFolder.Info> actualPathCollection = info.getPathCollection();

        assertThat(expectedName, equalTo(actualName));
        assertThat(expectedCreatedByID, equalTo(actualCreatedByID));
        assertThat(expectedParentFolderID, equalTo(actualParentFolderID));
        assertThat(expectedParentFolderName, equalTo(actualParentFolderName));
        assertThat(actualPathCollection, hasItem(Matchers.<BoxFolder.Info>hasProperty("ID", equalTo("0"))));
        assertThat(info.getPermissions(), is(equalTo(EnumSet.allOf(BoxFolder.Permission.class))));
        assertThat(info.getItemStatus(), is(equalTo("active")));

        childFolder.delete(false);
        assertThat(rootFolder, not(hasItem(Matchers.<BoxItem.Info>hasProperty("ID", equalTo(childFolder.getID())))));
    }

    @Test
    @Category(IntegrationTest.class)
    public void getInfoWithOnlyTheNameField() {
        final String expectedName = "All Files";

        BoxAPIConnection api = new BoxAPIConnection(TestConfig.getAccessToken());
        BoxFolder rootFolder = BoxFolder.getRootFolder(api);
        BoxFolder.Info rootFolderInfo = rootFolder.getInfo("name");
        final String actualName = rootFolderInfo.getName();
        final String actualDescription = rootFolderInfo.getDescription();
        final long actualSize = rootFolderInfo.getSize();

        assertThat(expectedName, equalTo(actualName));
        assertThat(actualDescription, is(nullValue()));
        assertThat(actualSize, is(0L));
    }

    @Test
    @Category(IntegrationTest.class)
    public void iterateWithOnlyTheNameField() {
        final String expectedName = "[iterateWithOnlyTheNameField] Child Folder";

        BoxAPIConnection api = new BoxAPIConnection(TestConfig.getAccessToken());
        BoxFolder rootFolder = BoxFolder.getRootFolder(api);
        BoxFolder.Info rootFolderInfo = rootFolder.getInfo("name");

        BoxFolder childFolder = rootFolder.createFolder(expectedName).getResource();

        Iterable<BoxItem.Info> children = rootFolder.getChildren("name");
        boolean found = false;
        for (BoxItem.Info childInfo : children) {
            if (childInfo.getID().equals(childFolder.getID())) {
                found = true;
                assertThat(childInfo.getName(), is(equalTo(expectedName)));
                assertThat(childInfo.getSize(), is(equalTo(0L)));
                assertThat(childInfo.getDescription(), is(nullValue()));
            }
        }
        assertThat(found, is(true));

        childFolder.delete(false);
    }

    @Test
    @Category(IntegrationTest.class)
    public void uploadFileSucceeds() {
        BoxAPIConnection api = new BoxAPIConnection(TestConfig.getAccessToken());
        BoxFolder rootFolder = BoxFolder.getRootFolder(api);

        final String fileContent = "Test file";
        InputStream stream = new ByteArrayInputStream(fileContent.getBytes(StandardCharsets.UTF_8));
        BoxFile uploadedFile = rootFolder.uploadFile(stream, "Test File.txt").getResource();

        assertThat(rootFolder, hasItem(Matchers.<BoxItem.Info>hasProperty("ID", equalTo(uploadedFile.getID()))));

        uploadedFile.delete();
        assertThat(rootFolder, not(hasItem(Matchers.<BoxItem.Info>hasProperty("ID", equalTo(uploadedFile.getID())))));
    }

    @Test
    @Category(IntegrationTest.class)
    public void uploadFileWithCreatedAndModifiedDatesSucceeds() {
        BoxAPIConnection api = new BoxAPIConnection(TestConfig.getAccessToken());
        BoxFolder rootFolder = BoxFolder.getRootFolder(api);
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX");
        dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));

        Date created = new Date(1415318114);
        Date modified = new Date(1315318114);
        final String fileContent = "Test file";
        InputStream stream = new ByteArrayInputStream(fileContent.getBytes(StandardCharsets.UTF_8));
        FileUploadParams params = new FileUploadParams()
            .setName("[uploadFileWithCreatedAndModifiedDatesSucceeds] Test File.txt").setContent(stream)
            .setModified(modified).setCreated(created);
        BoxFile.Info info = rootFolder.uploadFile(params);
        BoxFile uploadedFile = info.getResource();

        assertThat(dateFormat.format(info.getContentCreatedAt()), is(equalTo(dateFormat.format(created))));
        assertThat(dateFormat.format(info.getContentModifiedAt()), is(equalTo(dateFormat.format(modified))));
        assertThat(rootFolder, hasItem(Matchers.<BoxItem.Info>hasProperty("ID", equalTo(uploadedFile.getID()))));

        uploadedFile.delete();
    }

    @Test
    @Category(IntegrationTest.class)
    public void updateFolderInfoSucceeds() {
        BoxAPIConnection api = new BoxAPIConnection(TestConfig.getAccessToken());
        final String originalName = "[updateFolderInfoSucceeds] Child Folder";
        final String updatedName = "[updateFolderInfoSucceeds] Updated Child Folder";

        BoxFolder rootFolder = BoxFolder.getRootFolder(api);
        BoxFolder.Info info = rootFolder.createFolder(originalName);
        BoxFolder childFolder = info.getResource();
        info.setName(updatedName);
        childFolder.updateInfo(info);
        assertThat(info.getName(), equalTo(updatedName));

        childFolder.delete(false);
        assertThat(rootFolder, not(hasItem(Matchers.<BoxItem.Info>hasProperty("ID", equalTo(childFolder.getID())))));
    }

    @Test
    @Category(IntegrationTest.class)
    public void copyFolderToSameDestinationWithNewNameSucceeds() {
        BoxAPIConnection api = new BoxAPIConnection(TestConfig.getAccessToken());
        final String originalName = "[copyFolderToSameDestinationWithNewNameSucceeds] Child Folder";
        final String newName = "[copyFolderToSameDestinationWithNewNameSucceeds] New Child Folder";

        BoxFolder rootFolder = BoxFolder.getRootFolder(api);
        BoxFolder originalFolder = rootFolder.createFolder(originalName).getResource();
        BoxFolder.Info copiedFolderInfo = originalFolder.copy(rootFolder, newName);
        BoxFolder copiedFolder = copiedFolderInfo.getResource();

        assertThat(copiedFolderInfo.getName(), is(equalTo(newName)));
        assertThat(rootFolder, hasItem(Matchers.<BoxItem.Info>hasProperty("ID", equalTo(originalFolder.getID()))));
        assertThat(rootFolder, hasItem(Matchers.<BoxItem.Info>hasProperty("ID", equalTo(copiedFolder.getID()))));

        originalFolder.delete(false);
        copiedFolder.delete(false);
        assertThat(rootFolder, not(hasItem(Matchers.<BoxItem.Info>hasProperty("ID", equalTo(originalFolder.getID())))));
        assertThat(rootFolder, not(hasItem(Matchers.<BoxItem.Info>hasProperty("ID", equalTo(copiedFolder.getID())))));
    }

    @Test
    @Category(IntegrationTest.class)
    public void moveFolderSucceeds() {
        BoxAPIConnection api = new BoxAPIConnection(TestConfig.getAccessToken());
        final String child1Name = "[moveFolderSucceeds] Child Folder";
        final String child2Name = "[moveFolderSucceeds] Child Folder 2";

        BoxFolder rootFolder = BoxFolder.getRootFolder(api);
        BoxFolder childFolder1 = rootFolder.createFolder(child1Name).getResource();
        BoxFolder childFolder2 = rootFolder.createFolder(child2Name).getResource();

        assertThat(rootFolder, hasItem(Matchers.<BoxItem.Info>hasProperty("ID", equalTo(childFolder1.getID()))));
        assertThat(rootFolder, hasItem(Matchers.<BoxItem.Info>hasProperty("ID", equalTo(childFolder2.getID()))));

        childFolder2.move(childFolder1);

        assertThat(childFolder1, hasItem(Matchers.<BoxItem.Info>hasProperty("ID", equalTo(childFolder2.getID()))));
        assertThat(rootFolder, not(hasItem(Matchers.<BoxItem.Info>hasProperty("ID", equalTo(childFolder2.getID())))));

        childFolder1.delete(true);
        assertThat(rootFolder, not(hasItem(Matchers.<BoxItem.Info>hasProperty("ID", equalTo(childFolder1.getID())))));
    }

    @Test
    @Category(IntegrationTest.class)
    public void renameFolderSucceeds() {
        BoxAPIConnection api = new BoxAPIConnection(TestConfig.getAccessToken());
        final String originalName = "[renameFolderSucceeds] Original Name";
        final String newName = "[renameFolderSucceeds] New Name";

        BoxFolder rootFolder = BoxFolder.getRootFolder(api);
        BoxFolder childFolder = rootFolder.createFolder(originalName).getResource();
        childFolder.rename(newName);
        BoxFolder.Info childFolderInfo = childFolder.getInfo();

        assertThat(childFolderInfo.getName(), is(equalTo(newName)));

        childFolder.delete(false);
    }

    @Test
    @Category(IntegrationTest.class)
    public void addCollaboratorSucceeds() {
        BoxAPIConnection api = new BoxAPIConnection(TestConfig.getAccessToken());
        String folderName = "[addCollaborationToFolderSucceeds] Test Folder";
        String collaboratorLogin = TestConfig.getCollaborator();
        BoxCollaboration.Role collaboratorRole = BoxCollaboration.Role.CO_OWNER;

        BoxFolder rootFolder = BoxFolder.getRootFolder(api);
        BoxFolder folder = rootFolder.createFolder(folderName).getResource();

        BoxCollaboration.Info collabInfo = folder.collaborate(collaboratorLogin, collaboratorRole);
        BoxUser.Info accessibleBy = (BoxUser.Info) collabInfo.getAccessibleBy();

        assertThat(accessibleBy.getLogin(), is(equalTo(collaboratorLogin)));
        assertThat(collabInfo.getRole(), is(equalTo(collaboratorRole)));

        folder.delete(false);
    }

    @Test
    @Category(IntegrationTest.class)
    public void getCollaborationsHasCorrectCollaborations() {
        BoxAPIConnection api = new BoxAPIConnection(TestConfig.getAccessToken());
        String folderName = "[getCollaborationsSucceeds] Test Folder";
        String collaboratorLogin = TestConfig.getCollaborator();
        BoxCollaboration.Role collaboratorRole = BoxCollaboration.Role.CO_OWNER;

        BoxFolder rootFolder = BoxFolder.getRootFolder(api);
        BoxFolder folder = rootFolder.createFolder(folderName).getResource();
        BoxCollaboration.Info collabInfo = folder.collaborate(collaboratorLogin, collaboratorRole);
        String collabID = collabInfo.getID();

        Collection<BoxCollaboration.Info> collaborations = folder.getCollaborations();

        assertThat(collaborations, hasSize(1));
        assertThat(collaborations, hasItem(Matchers.<BoxCollaboration.Info>hasProperty("ID", equalTo(collabID))));

        folder.delete(false);
    }

    @Test
    @Category(IntegrationTest.class)
    public void setFolderUploadEmailSucceeds() {
        BoxAPIConnection api = new BoxAPIConnection(TestConfig.getAccessToken());
        String folderName = "[setFolderUploadEmailSucceeds] Test Folder";

        BoxUploadEmail uploadEmail = new BoxUploadEmail();
        uploadEmail.setAccess(BoxUploadEmail.Access.OPEN);

        BoxFolder rootFolder = BoxFolder.getRootFolder(api);
        BoxFolder folder = rootFolder.createFolder(folderName).getResource();
        BoxFolder.Info info = folder.new Info();
        info.setUploadEmail(uploadEmail);
        folder.updateInfo(info);

        assertThat(uploadEmail.getEmail(), not(isEmptyOrNullString()));
        assertThat(uploadEmail.getAccess(), is(equalTo(BoxUploadEmail.Access.OPEN)));

        info.setUploadEmail(null);
        uploadEmail = info.getUploadEmail();

        assertThat(uploadEmail, is(nullValue()));

        folder.delete(false);
    }

    @Test
    @Category(IntegrationTest.class)
    public void getSharedItemAndItsChildrenSucceeds() {
        BoxAPIConnection api = new BoxAPIConnection(TestConfig.getAccessToken());
        String folderName = "[getSharedItemAndItsChildrenSucceeds] Test Folder";
        String childFolderName = "[getSharedItemAndItsChildrenSucceeds] Child Folder";

        BoxFolder rootFolder = BoxFolder.getRootFolder(api);
        BoxFolder folder = rootFolder.createFolder(folderName).getResource();
        BoxFolder childFolder = folder.createFolder(childFolderName).getResource();
        BoxSharedLink sharedLink = folder.createSharedLink(BoxSharedLink.Access.OPEN, null, null);

        BoxFolder.Info sharedItem = (BoxFolder.Info) BoxItem.getSharedItem(api, sharedLink.getURL());

        assertThat(sharedItem.getID(), is(equalTo(folder.getID())));
        assertThat(sharedItem.getResource(), hasItem(Matchers.<BoxItem.Info>hasProperty("ID",
            equalTo(childFolder.getID()))));

        folder.delete(true);
    }

    @Test
    @Category(IntegrationTest.class)
    public void createWebLinkSucceeds() throws MalformedURLException {
        BoxAPIConnection api = new BoxAPIConnection(TestConfig.getAccessToken());
        BoxFolder rootFolder = BoxFolder.getRootFolder(api);

        BoxWebLink createdWebLink = rootFolder.createWebLink("[createWebLinkSucceeds] Test Web Link",
            new URL("https://api.box.com"), "[createWebLinkSucceeds] Test Web Link").getResource();

        assertThat(rootFolder, hasItem(Matchers.<BoxItem.Info>hasProperty("ID", equalTo(createdWebLink.getID()))));

        createdWebLink.delete();
        assertThat(rootFolder, not(hasItem(Matchers.<BoxItem.Info>hasProperty("ID", equalTo(createdWebLink.getID())))));
    }

    @Test
    @Category(IntegrationTest.class)
    public void createWebLinkNoNameSucceeds() throws MalformedURLException {
        BoxAPIConnection api = new BoxAPIConnection(TestConfig.getAccessToken());
        BoxFolder rootFolder = BoxFolder.getRootFolder(api);

        BoxWebLink createdWebLink = rootFolder.createWebLink(new URL("https://api.box.com"),
            "[createWebLinkSucceeds] Test Web Link").getResource();

        assertThat(rootFolder, hasItem(Matchers.<BoxItem.Info>hasProperty("ID", equalTo(createdWebLink.getID()))));

        createdWebLink.delete();
        assertThat(rootFolder, not(hasItem(Matchers.<BoxItem.Info>hasProperty("ID", equalTo(createdWebLink.getID())))));
    }

    @Test
    @Category(IntegrationTest.class)
    public void createWebLinkNoDescriptionSucceeds() throws MalformedURLException {
        BoxAPIConnection api = new BoxAPIConnection(TestConfig.getAccessToken());
        BoxFolder rootFolder = BoxFolder.getRootFolder(api);

        BoxWebLink createdWebLink = rootFolder.createWebLink("[createWebLinkSucceeds] Test Web Link",
            new URL("https://api.box.com")).getResource();

        assertThat(rootFolder, hasItem(Matchers.<BoxItem.Info>hasProperty("ID", equalTo(createdWebLink.getID()))));

        createdWebLink.delete();
        assertThat(rootFolder, not(hasItem(Matchers.<BoxItem.Info>hasProperty("ID", equalTo(createdWebLink.getID())))));
    }

    @Test
    @Category(IntegrationTest.class)
    public void createWebLinkNoNameOrDescriptionSucceeds() throws MalformedURLException {
        BoxAPIConnection api = new BoxAPIConnection(TestConfig.getAccessToken());
        BoxFolder rootFolder = BoxFolder.getRootFolder(api);

        BoxWebLink createdWebLink = rootFolder.createWebLink(new URL("https://api.box.com")).getResource();

        assertThat(rootFolder, hasItem(Matchers.<BoxItem.Info>hasProperty("ID", equalTo(createdWebLink.getID()))));

        createdWebLink.delete();
        assertThat(rootFolder, not(hasItem(Matchers.<BoxItem.Info>hasProperty("ID", equalTo(createdWebLink.getID())))));
    }
}
