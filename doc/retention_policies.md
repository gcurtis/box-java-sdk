Retention Policies
======

A retention policy blocks permanent deletion of content for a specified amount of time. Admins can create retention policies and then later assign them to specific folders or their entire enterprise.

* [Create Retention Policy](#create-retention-policy)

Create Retention Policy
--------------

The static [`createIndefinitePolicy(BoxAPIConnection, String)`][create-indefinite-retention-policy] method will let you create a new indefinite retention policy with a specified name.

```java
BoxRetentionPolicy.createIndefinitePolicy(api, name);
```

The static [`createFinitePolicy(BoxAPIConnection, String, int, String)`][create-finite-retention-policy] method will let you create a new indefinite retention policy with a specified name, amount of time to apply the retention policy (in days) and a disposition action. the disposition action can be "permanently_delete" or "remove_retention".

```java
BoxRetentionPolicy.createFinitePolicy(api, name, length, action);
```

[create-indefinite-retention-policy]: http://opensource.box.com/box-java-sdk/javadoc/com/box/sdk/BoxRetentionPolicy.html#createIndefinitePolicy(com.box.sdk.BoxAPIConnection,%20java.lang.String)
[create-finite-retention-policy]: http://opensource.box.com/box-java-sdk/javadoc/com/box/sdk/BoxRetentionPolicy.html#createIndefinitePolicy(com.box.sdk.BoxAPIConnection,%20java.lang.String,%20java.lang.int,%20java.lang.String)