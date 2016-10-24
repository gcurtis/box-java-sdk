package com.box.sdk;

import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import com.eclipsesource.json.JsonObject;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.junit.WireMockRule;

/**
 * {@link MetadataTemplate} related unit tests.
 */
public class MetadataTemplateTest {

    /**
     * Wiremock
     */
    @Rule
    public final WireMockRule wireMockRule = new WireMockRule(8080);

    /**
     * Unit test for {@link MetadataTemplate#getMetadataTemplate(BoxAPIConnection, String, String, String...)}.
     */
    @Test
    @Category(UnitTest.class)
    public void testGetMetadataTemplateSendsCorrectRequest() {
       // WireMock.stubFor(WireMock.get(WireMock.urlMatching("/metadata_templates/global/properties/schema"))
        BoxAPIConnection api = new BoxAPIConnection("");
        api.setRequestInterceptor(new RequestInterceptor() {
            @Override
            public BoxAPIResponse onRequest(BoxAPIRequest request) {
                Assert.assertEquals(
                        "https://api.box.com/2.0/metadata_templates/global/properties/schema"
                                + "?fields=displayName%2Chidden",
                        request.getUrl().toString());
                return new BoxJSONResponse() {
                    @Override
                    public String getJSON() {
                        return "{\"id\": \"0\"}";
                    }
                };
            }
        });

        MetadataTemplate.getMetadataTemplate(api, "properties", "global", "displayName", "hidden");
    }

    /**
     * Unit test for {@link MetadataTemplate#getMetadataTemplate(BoxAPIConnection)}.
     */
    @Test
    @Category(UnitTest.class)
    public void testGetMetadataTemplateParseAllFieldsCorrectly() {
        final String templateKey = "productInfo";
        final String scope = "enterprise_12345";
        final String displayName = "Product Info";
        final Boolean isHidden = false;
        final String firstFieldType = "float";
        final String firstFieldKey = "skuNumber";
        final String firstFieldDisplayName = "SKU Number";
        final Boolean firstFieldIsHidden = false;
        final String secondFieldType = "enum";
        final String secondFieldKey = "department";
        final String secondFieldDisplayName = "Department";
        final Boolean secondFieldIsHidden = false;
        final String secondFieldFirstOption = "Beauty";
        final String secondFieldSecondOption = "Accessories";

        BoxAPIConnection api = new BoxAPIConnection("");
        api.setBaseURL("http://localhost:8080/");
        WireMock.stubFor(WireMock.get(WireMock.urlMatching("/metadata_templates/global/properties/schema"))
                .willReturn(WireMock.aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\n"
                                + "    \"templateKey\": \"productInfo\",\n"
                                + "    \"scope\": \"enterprise_12345\",\n"
                                + "    \"displayName\": \"Product Info\",\n"
                                + "    \"hidden\": false,\n"
                                + "    \"fields\": [\n"
                                + "        {\n"
                                + "            \"type\": \"float\",\n"
                                + "            \"key\": \"skuNumber\",\n"
                                + "            \"displayName\": \"SKU Number\",\n"
                                + "            \"hidden\": false\n"
                                + "        },\n"
                                + "        {\n"
                                + "            \"type\": \"enum\",\n"
                                + "            \"key\": \"department\",\n"
                                + "            \"displayName\": \"Department\",\n"
                                + "            \"hidden\": false,\n"
                                + "            \"options\": [\n"
                                + "                {\n"
                                + "                    \"key\": \"Beauty\"\n"
                                + "                },\n"
                                + "                {\n"
                                + "                    \"key\": \"Accessories\"\n"
                                + "                }\n"
                                + "            ]\n"
                                + "        }\n"
                                + "    ]\n"
                                + "}")));

        MetadataTemplate template = MetadataTemplate.getMetadataTemplate(api);
        Assert.assertEquals(templateKey, template.getTemplateKey());
        Assert.assertEquals(scope, template.getScope());
        Assert.assertEquals(displayName, template.getDisplayName());
        Assert.assertEquals(isHidden, template.getIsHidden());
        List<MetadataTemplate.Field> templateFields = template.getFields();
        Assert.assertEquals(firstFieldType, templateFields.get(0).getType());
        Assert.assertEquals(firstFieldKey, templateFields.get(0).getKey());
        Assert.assertEquals(firstFieldDisplayName, templateFields.get(0).getDisplayName());
        Assert.assertEquals(firstFieldIsHidden, templateFields.get(0).getIsHidden());
        Assert.assertEquals(secondFieldType, templateFields.get(1).getType());
        Assert.assertEquals(secondFieldKey, templateFields.get(1).getKey());
        Assert.assertEquals(secondFieldDisplayName, templateFields.get(1).getDisplayName());
        Assert.assertEquals(secondFieldIsHidden, templateFields.get(1).getIsHidden());
        Assert.assertEquals(secondFieldFirstOption, templateFields.get(1).getOptions().get(0));
        Assert.assertEquals(secondFieldSecondOption, templateFields.get(1).getOptions().get(1));

    }

    /**
     * Unit test for {@link MetadataTemplate#getEnterpriseMetadataTemplates(BoxAPIConnection)}.
     */
    @Test(expected = NoSuchElementException.class)
    @Category(UnitTest.class)
    public void testGetEnterpriseMetadataTemplatesSendsCorrectRequest() {
        BoxAPIConnection api = new BoxAPIConnection("");
        api.setRequestInterceptor(new RequestInterceptor() {
            @Override
            public BoxAPIResponse onRequest(BoxAPIRequest request) {
                Assert.assertEquals(
                        "https://api.box.com/2.0/metadata_templates/enterprise?limit=100", request.getUrl().toString());
                return new BoxJSONResponse() {
                    @Override
                    public String getJSON() {
                        return "{\"entries\":[]}";
                    }
                };
            }
        });

        Iterator iterator = MetadataTemplate.getEnterpriseMetadataTemplates(api).iterator();
        iterator.next();
    }

    /**
     * Unit test for {@link MetadataTemplate#getEnterpriseMetadataTemplates(BoxAPIConnection)}.
     */
    @Test
    @Category(UnitTest.class)
    public void testGetEnterpriseMetadataTemplatesParseAllFieldsCorrectly() {
        final String firstEntryTemplateKey = "documentFlow";
        final String firstEntryScope = "enterprise_12345";
        final String firstEntryDisplayName = "Document Flow";
        final Boolean firstEntryIsHidden = false;
        final String firstEntryFieldType = "string";
        final String firstEntryFieldKey = "currentDocumentStage";
        final String firstEntryFieldDisplayName = "Current Document Stage";
        final Boolean firstEntryFieldIsHidden = false;
        final String firstEntryFieldDescription = "What stage in the process the document is in";
        final String secondEntryTemplateKey = "productInfo";
        final String secondEntryScope = "enterprise_12345";
        final String secondEntryDisplayName = "Product Info";
        final Boolean secondEntryIsHidden = false;
        final String secondEntryFieldType = "enum";
        final String secondEntryFieldKey = "department";
        final String secondEntryFieldDisplayName = "Department";
        final Boolean secondEntryFieldIsHidden = false;
        final String secondEntryFieldFirstOption = "Beauty";
        final String secondEntryFieldSecondOption = "Shoes";

        final JsonObject fakeJSONResponse = JsonObject.readFrom("{\n"
                + "    \"limit\": 100,\n"
                + "    \"entries\": [\n"
                + "        {\n"
                + "            \"templateKey\": \"documentFlow\",\n"
                + "            \"scope\": \"enterprise_12345\",\n"
                + "            \"displayName\": \"Document Flow\",\n"
                + "            \"hidden\": false,\n"
                + "            \"fields\": [\n"
                + "                {\n"
                + "                    \"type\": \"string\",\n"
                + "                    \"key\": \"currentDocumentStage\",\n"
                + "                    \"displayName\": \"Current Document Stage\",\n"
                + "                    \"hidden\": false,\n"
                + "                    \"description\": \"What stage in the process the document is in\"\n"
                + "                }\n"
                + "            ]\n"
                + "        },\n"
                + "        {\n"
                + "            \"templateKey\": \"productInfo\",\n"
                + "            \"scope\": \"enterprise_12345\",\n"
                + "            \"displayName\": \"Product Info\",\n"
                + "            \"hidden\": false,\n"
                + "            \"fields\": [\n"
                + "                {\n"
                + "                    \"type\": \"enum\",\n"
                + "                    \"key\": \"department\",\n"
                + "                    \"displayName\": \"Department\",\n"
                + "                    \"hidden\": false,\n"
                + "                    \"options\": [\n"
                + "                        {\n"
                + "                            \"key\": \"Beauty\"\n"
                + "                        },\n"
                + "                        {\n"
                + "                            \"key\": \"Shoes\"\n"
                + "                        }\n"
                + "                    ]\n"
                + "                }\n"
                + "            ]\n"
                + "        }\n"
                + "    ],\n"
                + "    \"next_marker\": null,\n"
                + "    \"prev_marker\": null\n"
                + "}");

        BoxAPIConnection api = new BoxAPIConnection("");
        api.setRequestInterceptor(JSONRequestInterceptor.respondWith(fakeJSONResponse));

        Iterator<MetadataTemplate> iterator = MetadataTemplate.getEnterpriseMetadataTemplates(api).iterator();
        MetadataTemplate template = iterator.next();
        Assert.assertEquals(firstEntryTemplateKey, template.getTemplateKey());
        Assert.assertEquals(firstEntryScope, template.getScope());
        Assert.assertEquals(firstEntryDisplayName, template.getDisplayName());
        Assert.assertEquals(firstEntryIsHidden, template.getIsHidden());
        Assert.assertEquals(firstEntryFieldType, template.getFields().get(0).getType());
        Assert.assertEquals(firstEntryFieldKey, template.getFields().get(0).getKey());
        Assert.assertEquals(firstEntryFieldDisplayName, template.getFields().get(0).getDisplayName());
        Assert.assertEquals(firstEntryFieldIsHidden, template.getFields().get(0).getIsHidden());
        Assert.assertEquals(firstEntryFieldDescription, template.getFields().get(0).getDescription());
        template = iterator.next();
        Assert.assertEquals(secondEntryTemplateKey, template.getTemplateKey());
        Assert.assertEquals(secondEntryScope, template.getScope());
        Assert.assertEquals(secondEntryDisplayName, template.getDisplayName());
        Assert.assertEquals(secondEntryIsHidden, template.getIsHidden());
        Assert.assertEquals(secondEntryFieldType, template.getFields().get(0).getType());
        Assert.assertEquals(secondEntryFieldKey, template.getFields().get(0).getKey());
        Assert.assertEquals(secondEntryFieldDisplayName, template.getFields().get(0).getDisplayName());
        Assert.assertEquals(secondEntryFieldIsHidden, template.getFields().get(0).getIsHidden());
        Assert.assertEquals(secondEntryFieldFirstOption, template.getFields().get(0).getOptions().get(0));
        Assert.assertEquals(secondEntryFieldSecondOption, template.getFields().get(0).getOptions().get(1));
        Assert.assertFalse(iterator.hasNext());
    }
}
