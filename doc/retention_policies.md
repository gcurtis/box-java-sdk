Retention Policies
======

A retention policy blocks permanent deletion of content for a specified amount of time. Admins can create retention policies and then later assign them to specific folders or their entire enterprise.

* [Create Retention Policy](#create-retention-policy)
* [Get Retention Policy](#get-retention-policy)
* [Update Retention Policy](#update-retention-policy)
* [Get Retention Policies](#get-retention-policies)
* [Get Retention Policy Assignments](#get-retention-policy-assignments)
* [Create Retention Policy Assignment](#create-retention-policy-assignment)
* [Get Retention Policy Assignment](#get-retention-policy-assignment)
* [Get File Version Retention](#get-file-version-retention)
* [Get File Version Retentions](#get-file-version-retentions)

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

Get Retention Policy
--------------

Calling [`getInfo(String...)`][get-info] will return a BoxRetentionPolicy.Info object containing information about the retention policy. If necessary to retrieve limited set of fields, it is possible to specify them using param.

```java
BoxRetentionPolicy policy = new BoxRetentionPolicy(api, id);
policy.getInfo("policy_name", "status");
```

[get-info]: http://opensource.box.com/box-java-sdk/javadoc/com/box/sdk/BoxRetentionPolicy.html#getInfo(java.lang.String...)

Update Retention Policy
--------------

Updating a retention policy's information is done by calling [`updateInfo(BoxRetentionPolicy.Info)`][update-info].

```java
BoxRetentionPolicy policy = new BoxRetentionPolicy(api, id);
BoxRetentionPolicy.Info policyInfo = policy.new Info();
policyInfo.addPendingChange("policy_name", "new policy name");
policy.updateInfo(policyInfo);
```

[update-info]: http://opensource.box.com/box-java-sdk/javadoc/com/box/sdk/BoxRetentionPolicy.html#updateInfo(com.box.sdk.BoxRetentionPolicy.Info)

Get Retention Policies
--------------

Calling the static [`getAll(BoxAPIConnection)`][get-retention-policies] will return an iterable that will page through all of the retention policies.
It is possible to specify filter for the name of retention policy, filter for the type of the policy, filter for the id of user and fields to retrieve by calling the static [`getAll(String, String, String, BoxAPIConnection, String...)`][get-retention-policies-with-fields] method.

```java
Iterable<BoxRetentionPolicy.Info> policies = BoxRetentionPolicy.getAll(api);
for (BoxRetentionPolicy.Info policyInfo : policies) {
	// Do something with the retention policy.
}
```

[get-retention-policies]: http://opensource.box.com/box-java-sdk/javadoc/com/box/sdk/BoxRetentionPolicy.html#getAll(com.box.sdk.BoxAPIConnection)
[get-retention-policies-with-fields]: http://opensource.box.com/box-java-sdk/javadoc/com/box/sdk/BoxRetentionPolicy.html#getAll(java.lang.String,%20java.lang.String,%20java.lang.String,%20com.box.sdk.BoxAPIConnection,%20java.lang.String...)

Get Retention Policy Assignments
--------------

Description [`method()`][link]

```java
example;
```

[link]: http://box.com

Create Retention Policy Assignment
--------------

Description [`method()`][link]

```java
example;
```

[link]: http://box.com

Get Retention Policy Assignment
--------------

Description [`method()`][link]

```java
example;
```

[link]: http://box.com

Get File Version Retention
--------------

Description [`method()`][link]

```java
example;
```

[link]: http://box.com

Get File Version Retentions
--------------

Description [`method()`][link]

```java
example;
```

[link]: http://box.com