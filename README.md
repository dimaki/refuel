[![Build Status](https://drone.io/github.com/dimaki/refuel/status.png)](https://drone.io/github.com/dimaki/refuel/latest)

refuel
======

An application update tool for Java 8


refuel is available from maven central:
```xml
        <dependency>
            <groupId>de.dimaki</groupId>
            <artifactId>refuel</artifactId>
            <version>[LATEST_RELEASE]</version>
        </dependency>
```

Usage
```java
Updater updater = new Updater();
ApplicationStatus status = updater.getApplicationStatus(localVersion, updateUrl);
```
