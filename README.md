<div align="center">

<img width="" src="metadata/en-US/images/icon.png"  width=160 height=160  align="center">

# Centsation

Centsation is the easy-to-use savings tracker that helps you reach your financial goals! Whether you're saving for a vacation, a new gadget, or an emergency fund, Centsation makes it simple and straightforward.

</div>

## Features

* **Free and Open Source:** Enjoy complete transparency and community-driven development.
* **Modern Design:** Experience a beautiful and intuitive interface with Material 3 and dynamic color support.
* **Enhanced Accessibility:** Customize the UI contrast (low, medium, or high) for optimal readability.
* **Dark Mode:** Save battery and reduce eye strain with a sleek dark theme.
* **Multi-Currency Support:** Track your savings in a currency of your choice.
* **Archived Savings:** Keep your active savings organized by archiving completed or paused goals.
* **Data Management:** Easily export and import your data using JSON files.
* **Transaction History:** Review a detailed log of your savings activities.
* **Optional Deadlines:** Set and track progress towards your goals with optional deadlines.
* **Flexible Sorting:** Organize your savings by name, current amount, goal amount, or deadline.
* **Crash Reports:** If the app encounters an unexpected error, a dialog on the next launch lets you send the crash details by email or save them to a file.

## Screenshots

<div align="center">
	<div>
		<img src="metadata/en-US/images/phoneScreenshots/Screenshot 1.jpg" width="30%" />
    <img src="metadata/en-US/images/phoneScreenshots/Screenshot 2.jpg" width="30%" />
    <img src="metadata/en-US/images/phoneScreenshots/Screenshot 3.jpg" width="30%" />
    <img src="metadata/en-US/images/phoneScreenshots/Screenshot 4.jpg" width="30%" />
    <img src="metadata/en-US/images/phoneScreenshots/Screenshot 5.jpg" width="30%" />
    <img src="metadata/en-US/images/phoneScreenshots/Screenshot 6.jpg" width="30%" />
	</div>
</div>

## Downloads

Centsation is also available on IzzyOnDroid.

[<img height=80 alt="Get it on IzzyOnDroid"
src="https://gitlab.com/IzzyOnDroid/repo/-/raw/master/assets/IzzyOnDroid.png"
/>](https://apt.izzysoft.de/fdroid/index/apk/com.mowtiie.centsation)

## Verification

APK releases on GitHub are signed using my key. They can
be verified using
[apksigner](https://developer.android.com/studio/command-line/apksigner.html#options-verify):

```
apksigner verify --print-certs --verbose centsation.apk
```

The output should look like:

```
Verifies
Verified using v1 scheme (JAR signing): false
Verified using v2 scheme (APK Signature Scheme v2): true
Verified using v3 scheme (APK Signature Scheme v3): false
Verified using v3.1 scheme (APK Signature Scheme v3.1): false
Verified using v3.2 scheme (APK Signature Scheme v3.2): false
Verified using v4 scheme (APK Signature Scheme v4): false
```

> **Note:** Starting with version 1.7, releases are signed using a new keystore. Use the fingerprints below that match the version you're verifying.

### v1.7 and later

```
Owner: CN=Mowtiie
Issuer: CN=Mowtiie
Serial number: 8a256fdcdde50069
Valid from: Wed Jun 10 22:57:23 PST 2026 until: Sun Oct 26 22:57:23 PST 2053
Certificate fingerprints:
         SHA1: 56:4E:2C:DB:E4:06:C9:EC:15:E6:BC:D9:0A:88:38:72:8B:FB:13:20
         SHA256: 8B:67:51:F3:C3:31:85:63:5F:98:95:30:B6:C0:73:A1:39:7B:3D:41:2B:EF:AE:69:06:A2:EB:58:45:D2:DE:63
```

### v1.6 and earlier

```
CN=Vrixzandro Eliponga
O=OSSentials
OU=Hobbyist Developer
L=Lian
ST=Batangas
C=PH
Certificate fingerprints:
   MD5:  a8a82d68a60fe6ecf45eff4550b94d6f
   SHA1: af8be376426c6725fc3bdb287abeb268bf94b768
   SHA256: 561f3fec72e1f9878c2749d079f8b2175d02131c842955714e63365da5301baa
```

**Warning:** Please be aware that versions 1.3 and below of this application were released without digital signatures. For the best security and to ensure you are using a genuine version of the application, I strongly recommend updating to the latest version as soon as possible.

## Mapping Files

Each release on GitHub includes a `mapping-<version>.txt` file alongside the APK. This file is needed to deobfuscate stack traces from crash reports — match the file to the version shown in the crash report header and use it with `retrace` from the Android SDK.

## License

This project is licensed under the GNU General Public License v3.0. See the
[LICENSE](LICENSE) file for details.
