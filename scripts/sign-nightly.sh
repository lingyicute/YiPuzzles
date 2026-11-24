#!/usr/bin/env bash
# SPDX-License-Identifier: GPL-3.0-or-later
set -euo pipefail

if [[ $# -ne 2 ]]; then
  echo 'Usage: sign-nightly.sh input.apk output.apk' >&2
  exit 2
fi
sdk=${ANDROID_HOME:-${ANDROID_SDK_ROOT:-}}
: "${sdk:?Set ANDROID_HOME or ANDROID_SDK_ROOT}"
apksigner="$sdk/build-tools/34.0.0/apksigner"

umask 077
signing_dir=$(mktemp -d "${RUNNER_TEMP:-${TMPDIR:-/tmp}}/yipuzzles-signing.XXXXXX")
trap 'rm -rf -- "$signing_dir"' EXIT
keystore="$signing_dir/nightly.keystore"
# Locally an optional secret may be unset rather than exported as an empty value.
NIGHTLY_KEYSTORE_BASE64=${NIGHTLY_KEYSTORE_BASE64:-}
if [[ -n ${NIGHTLY_KEYSTORE_BASE64//[[:space:]]/} ]]; then
  : "${NIGHTLY_KEYSTORE_PASSWORD:?Missing nightly keystore password}"
  : "${NIGHTLY_KEY_ALIAS:?Missing nightly key alias}"
  : "${NIGHTLY_KEY_PASSWORD:?Missing nightly key password}"
  printf '%s' "$NIGHTLY_KEYSTORE_BASE64" | tr -d '[:space:]' | base64 --decode > "$keystore"
  unset NIGHTLY_KEYSTORE_BASE64
else
  if [[ -n ${NIGHTLY_KEYSTORE_PASSWORD:-}${NIGHTLY_KEY_ALIAS:-}${NIGHTLY_KEY_PASSWORD:-} ]]; then
    echo 'Configure all four NIGHTLY_* signing secrets or none.' >&2
    exit 2
  fi
  echo 'No persistent nightly keystore configured; this APK is for a clean install.' >&2
  export NIGHTLY_KEY_ALIAS=yipuzzles-nightly
  NIGHTLY_KEYSTORE_PASSWORD=$(openssl rand -hex 24)
  export NIGHTLY_KEYSTORE_PASSWORD
  export NIGHTLY_KEY_PASSWORD="$NIGHTLY_KEYSTORE_PASSWORD"
  keytool -genkeypair -keystore "$keystore" -storetype PKCS12 \
    -alias "$NIGHTLY_KEY_ALIAS" -dname 'CN=YiPuzzles Nightly' -keyalg RSA -keysize 2048 -validity 3650 \
    -storepass:env NIGHTLY_KEYSTORE_PASSWORD -keypass:env NIGHTLY_KEY_PASSWORD
fi

"$apksigner" sign --ks "$keystore" --ks-key-alias "$NIGHTLY_KEY_ALIAS" \
  --ks-pass env:NIGHTLY_KEYSTORE_PASSWORD --key-pass env:NIGHTLY_KEY_PASSWORD --out "$2" "$1"
"$apksigner" verify --print-certs "$2"
