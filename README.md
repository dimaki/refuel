refuel
======

**A simple update framework for Java**
(requires Java 8)

refuel is available from maven central:
```xml
        <dependency>
            <groupId>de.dimaki</groupId>
            <artifactId>refuel</artifactId>
            <version>[LATEST_RELEASE]</version>
        </dependency>
```

### Basic Usage
```java
Updater updater = new Updater();
ApplicationStatus status = updater.getApplicationStatus(localVersion, updateUrl);
```

### Format
Uses Appcast format for release information.
Compatible to [Sparkle update framework for Cocoa](http://sparkle-project.org).
Example:
```xml
<?xml version="1.0" encoding="utf-8"?>
<rss version="2.0" xmlns:sparkle="http://www.andymatuschak.org/xml-namespaces/sparkle"  xmlns:dc="http://purl.org/dc/elements/1.1/">
   <channel>
      <title>App Changelog</title>
      <link>https://www.someurl.xyz/appcast.xml</link>
      <description>Most recent changes with links to updates.</description>
      <language>en</language>      
         <item>
            <title>Version 2.0</title>
			<description>
				Lorem ipsum dolor sit amet, consectetur adipiscing elit. Suspendisse sed felis ac ante ultrices rhoncus.
			</description>
            <pubDate>Tue, 02 Oct 2013 15:20:11 +0100</pubDate>
            <sparkle:releaseNotesLink>https://www.someurl.xyz/release_notes.html</sparkle:releaseNotesLink>
            <enclosure url="https://www.someurl.xyz/test.zip" sparkle:version="2.0.4711" length="1505" type="application/octet-stream" sparkle:md5="ae14a99c788cff24a9548907d1c73220" />
         </item>
   </channel>
</rss>
```
