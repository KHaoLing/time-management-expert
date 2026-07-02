from pathlib import Path
import re

VERSION_CODE = 2
VERSION_NAME = "1.0.2"

path = Path("android/app/build.gradle")
text = path.read_text(encoding="utf-8")

# Update the default version values in Capacitor's generated Android project.
text = re.sub(r"versionCode\s+\d+", f"versionCode {VERSION_CODE}", text)
text = re.sub(r"versionName\s+[\"'][^\"']+[\"']", f"versionName \"{VERSION_NAME}\"", text)

# Avoid fragile insertion into existing buildTypes/signingConfigs blocks.
# Gradle allows configuring the Android extension multiple times, so we append
# a clean release-signing block after the generated file content.
marker = "// --- Time Master release signing config injected by patch-android-release.py ---"
if marker in text:
    text = text.split(marker)[0].rstrip() + "\n\n"

release_block = f'''{marker}
android {{
    defaultConfig {{
        versionCode {VERSION_CODE}
        versionName "{VERSION_NAME}"
    }}

    signingConfigs {{
        release {{
            storeFile file(System.getenv("RELEASE_KEYSTORE_PATH"))
            storePassword System.getenv("RELEASE_KEYSTORE_PASSWORD")
            keyAlias System.getenv("RELEASE_KEY_ALIAS")
            keyPassword System.getenv("RELEASE_KEY_PASSWORD")
        }}
    }}

    buildTypes {{
        release {{
            signingConfig signingConfigs.release
            minifyEnabled false
        }}
    }}
}}
'''

text = text.rstrip() + "\n\n" + release_block
path.write_text(text, encoding="utf-8")
print(f"Patched Android release config: versionName={VERSION_NAME}, versionCode={VERSION_CODE}")
