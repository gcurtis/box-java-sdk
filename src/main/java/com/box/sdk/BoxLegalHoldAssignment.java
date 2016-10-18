package com.box.sdk;

@BoxResourceType("legal_hold_assignment")
public class BoxLegalHoldAssignment extends BoxResource {
    /**
     * Constructs a BoxLegalHoldAssignment for a resource with a given ID.
     *
     * @param api the API connection to be used by the resource.
     * @param id  the ID of the resource.
     */
    public BoxLegalHoldAssignment(BoxAPIConnection api, String id) {
        super(api, id);
    }
}
