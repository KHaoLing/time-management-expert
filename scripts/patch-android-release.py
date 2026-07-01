from pathlib import Path
import re

VERSION_CODE = 1
VERSION_NAME = "1.0.1"

path = Path("android/app/build.gradle")
text = path.read_text(encoding="utf-8")

text = re.sub(r"versionCode\s+\d+", f"versionCode {VERSION_CODE}", text)
text = re.sub(r"versionName\s+[\"'][^\"']+[\"']", f"versionName \"{VERSION_NAME}\"", text)

signing_block = '''    signingConfigs {
        release {
            storeFile file(System.getenv("RELEASE_KEYSTORE_PATH"))
            storePassword System.getenv("RELEASE_KEYSTORE_PASSWORD")
            keyAlias System.getenv("RELEASE_KEY_ALIAS")
            keyPassword System.getenv("RELEASE_KEY_PASSWORD")
        }
    }

'''

if "signingConfigs" not in text:
    text = text.replace("    defaultConfig {", signing_block + "    defaultConfig {", 1)

if "buildTypes" not in text:
    marker = "}\n\napply from: 'capacitor.build.gradle'"
    build_types = '''
    buildTypes {
        release {
            signingConfig signingConfigs.release
            minifyEnabled false
            proguardFiles getDefaultProguardFile('proguard-android-optimize.txt'), 'proguard-rules.pro'
        }
    }
'''
    if marker in text:
        text = text.replace(marker, build_types + "}\n\napply from: 'capacitor.build.gradle'", 1)
    else:
        raise SystemExit("Could not find place to insert buildTypes in android/app/build.gradle")
else:
    if "signingConfig signingConfigs.release" not in text:
        # Insert signingConfig into the first release block.
        text = re.sub(r"(release\s*\{\s*\n)", r"\1            signingConfig signingConfigs.release\n", text, count=1)

path.write_text(text, encoding="utf-8")
print(f"Patched Android release config: versionName={VERSION_NAME}, versionCode={VERSION_CODE}")
