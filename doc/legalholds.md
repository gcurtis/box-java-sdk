Legal Holds Policy
======

Legal Hold Policy information describes the basic characteristics of the Policy, such as name, description, and filter dates.

* [Get Legal Hold Policy](#get-legal-hold-policy)

Get Legal Hold Policy
--------------

Calling [`getInfo(String...)`][get-info] will return a BoxLegalHold.Info object containing information about the legal hold policy. If necessary to retrieve limited set of fields, it is possible to specify them using param.

```java
BoxLegalHold policy = new BoxLegalHold(api, id);
BoxLegalHold.Info policyINfo = policy.getInfo();
```

[get-info]: http://opensource.box.com/box-java-sdk/javadoc/com/box/sdk/BoxLegalHold.html#getInfo(java.lang.String...)
