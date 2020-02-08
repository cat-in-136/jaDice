jaDice
======

jaDice is a PDIC viewer for PC ported from android app "[adice]"

Works on Java SE 8+ / OpenJDK 8+.
![Java CI](https://github.com/cat-in-136/jaDice/workflows/Java%20CI/badge.svg)

## Dictionary

jaDice supports following PDIC formats:

* Unicode(BOCU-1) Hyper-format ver6.00 (PDIC/Unicode 5.00 or later)

Some online PDIC data needs to be converted to latest format
using [PDIC/Unicode].

jaDice includes *no dictionary data*.
PDIC files are required to use this app.

## How to Use

1. Open "Setting" dialog
2. Select "Dictionary" tab page
3. Push "Add" button to choose a PDIC file on the file dialog,
   and then you can see the new file on the list.
4. Push "OK" to close the dialog
5. Input to the textbox the words you want to search
6. Read the results

## Where setting data is stored

User setting is stored at

 * windows: `\HKEY_CURRENT_USER\Software\JavaSoft\Prefs\io\github\cat_in_136\jadice` on the registry
 * macos: `/io/github/cat_in_136/jadice` on `~/Library/Preferences/com.apple.java.util.prefs.plist`
 * linux: `~/.java/.userPrefs/io/github/_!':!}@"0!&8!a@"u!&8!:@!z!$}=/jadice/prefs.xml`

## License

The app is released under NYSL. Read LICENSE for details.

The app includes library/work licensed under the license terms as specified below.

 * PDIC/Unicode by TaN.
   http://homepage3.nifty.com/TaN/unicode/
 * aDice
   https://github.com/jiro-aqua/aDice
   Copyright (C) Aquamarine Networks.
   Licensed under NYSL.
 * ICU4J
   http://site.icu-project.org/
   Copyright (C) Unicode, Inc. All rights reserved.
   Distributed under the Terms of Use in https://www.unicode.org/copyright.html.

## How to Build

Run build task to build from the source code.

```console
% git clone https://github.com/cat-in-136/jadice
% cd jadice
% ./gradlew build
```

Run run task to launch this app.

```console
% ./gradlew run
```

[adice]: https://github.com/jiro-aqua/aDice
[PDIC/Unicode]: http://pdic.la.coocan.jp/unicode/
