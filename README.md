![Kotlinx Gettext](kotlinx-gettext.png)

![maven](https://img.shields.io/maven-central/v/io.github.ssimco/kotlinx-gettext)
![license](https://img.shields.io/github/license/ssimco/kotlinx-gettext)

Gettext is a widely used internationalization (i18n) format supported by many tools.
This project provides pure Kotlin implementation of Gettext.
It is a fork of [kropp/kotlinx-gettext](https://github.com/kropp/kotlinx-gettext),
originally created by Victor Kropp.

The project consists of several parts:
 1. `kotlinx-gettext` library to use in your project to translate strings.
 2. `kotlinx-gettext-plugin` Kotlin compiler plugin to extract strings for translation from Kotlin code.
 3. `kotlinx-gettext-gradle-plugin` to conveniently apply Kotlin compiler plugin in your Gradle build.

## Usage

### Library

Add the `"io.github.ssimco:kotlinx-gettext:0.8.0"` to the dependencies of your project.
The library is available on [Maven Central](https://central.sonatype.com/artifact/io.github.ssimco/kotlinx-gettext).

Load translated strings and apply translations using an instance of `I18n` class.

```kotlin
val i18n = Gettext.load(Locale("de"), Thread.currentThread().contextClassLoader.getResourceAsStream("de.po")!!)
println(i18n.tr("Hello world!"))
```

`Locale` is the same class on every platform.
It accepts language tags, such as `de-DE`,
and POSIX locale names, such as `de_DE.UTF-8`.
In common code, pass the PO file as an okio `Source`:

```kotlin
val i18n = Gettext.load(Locale("de"), source)
```

`Gettext.load` also accepts compiled MO files, such as those built with `msgfmt`.
It detects the format from the file content.
MO files must use UTF-8.

On JVM, `Gettext.load` also accepts a `java.util.Locale`, such as `java.util.Locale.GERMAN`.
`toJavaLocale()` and `toGettextLocale()` convert between the two types.
The `MessageFormat` helpers, such as `i18n.tr("{0,number} files", count)`,
format numbers and dates with the catalog locale.

See `sample` module for a full example. Refer to [GNU gettext documentation](https://www.gnu.org/software/gettext/)
for more details.

### Translation

Apply Gradle plugin to extract strings and setup `gettext` task: 

```kotlin
plugins {
  id("io.github.ssimco.kotlinx-gettext") version "0.8.0"
}

gettext {
  potFile.set(File(projectDir, "src/messages.pot"))
  keywords.set(listOf("tr","trn:1,2"))
}
```

Then invoke `./gradlew gettext` to extract strings for translations into `src/messages.pot`

Translate messages with any software or service supporting Gettext format. The result will be a number of .po files,
which you should put into resources.
You can also compile them into .mo files with `msgfmt` and load those instead.

## Known issues
 * The library only supports Kotlin/JVM and Kotlin/JS as of now, Multiplatform Native port is in progress.
