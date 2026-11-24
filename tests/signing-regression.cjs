/* SPDX-License-Identifier: GPL-3.0-or-later */
// Exercise the signing script without an Android SDK or real signing credentials.
const assert = require('node:assert/strict');
const { mkdtempSync, mkdirSync, writeFileSync, readFileSync, readdirSync, rmSync } = require('node:fs');
const { tmpdir } = require('node:os');
const { join, resolve } = require('node:path');
const { spawnSync } = require('node:child_process');
const { test } = require('node:test');
const script = resolve(__dirname, '../scripts/sign-nightly.sh');
const secretNames = ['NIGHTLY_KEYSTORE_BASE64', 'NIGHTLY_KEYSTORE_PASSWORD', 'NIGHTLY_KEY_ALIAS', 'NIGHTLY_KEY_PASSWORD'];
const persistent = {
  NIGHTLY_KEYSTORE_BASE64: Buffer.from('persistent-key').toString('base64'),
  NIGHTLY_KEYSTORE_PASSWORD: 'fixture-password',
  NIGHTLY_KEY_ALIAS: 'fixture-alias',
  NIGHTLY_KEY_PASSWORD: 'fixture-password'
};

function run(t, variables = {}) {
  const root = mkdtempSync(join(tmpdir(), 'yipuzzles-sign-test-'));
  t.after(() => rmSync(root, { recursive: true, force: true }));
  const bin = join(root, 'bin'), sdk = join(root, 'sdk'), scratch = join(root, 'scratch');
  for (const dir of [bin, scratch, join(sdk, 'build-tools/34.0.0')]) mkdirSync(dir, { recursive: true });
  writeFileSync(join(root, 'input.apk'), 'fixture-apk');
  writeFileSync(join(root, 'calls'), '');
  writeFileSync(join(bin, 'keytool'), `#!/usr/bin/env bash
set -euo pipefail
while [[ $# -gt 0 ]]; do
  if [[ $1 == -keystore ]]; then printf ephemeral-key > "$2"; break; fi
  shift
done
printf 'keytool\n' >> "$TEST_LOG"
`, { mode: 0o755 });
  writeFileSync(join(sdk, 'build-tools/34.0.0/apksigner'), `#!/usr/bin/env bash
set -euo pipefail
if [[ $1 == verify ]]; then
  printf 'verify\n' >> "$TEST_LOG"
  exit "\${FAIL_VERIFY:-0}"
fi
shift
while [[ $# -gt 0 ]]; do
  case $1 in
    --ks) key=$2; shift 2 ;;
    --out) out=$2; shift 2 ;;
    --ks-key-alias|--ks-pass|--key-pass) shift 2 ;;
    *) input=$1; shift ;;
  esac
done
[[ $(cat "$key") == "$EXPECTED_KEYSTORE" ]]
printf 'sign\n' >> "$TEST_LOG"
if [[ \${FAIL_SIGN:-0} != 0 ]]; then exit 1; fi
cp "$input" "$out"
`, { mode: 0o755 });
  const env = { ...process.env, PATH: bin + ':' + process.env.PATH, ANDROID_HOME: sdk,
    RUNNER_TEMP: scratch, TEST_LOG: join(root, 'calls'), EXPECTED_KEYSTORE: 'ephemeral-key' };
  for (const name of secretNames) delete env[name];
  Object.assign(env, variables);
  const result = spawnSync('bash', [script, join(root, 'input.apk'), join(root, 'output.apk')], { env, encoding: 'utf8' });
  assert.ifError(result.error);
  assert.deepEqual(readdirSync(scratch), [], 'temporary keystores must always be removed');
  return { ...result, calls: readFileSync(join(root, 'calls'), 'utf8') };
}

for (const [name, variables] of [
  ['unset', {}], ['empty', Object.fromEntries(secretNames.map(key => [key, '']))],
  ['whitespace-only', { NIGHTLY_KEYSTORE_BASE64: ' \r\n\t ' }]
]) test(`disposable signing accepts ${name} secrets`, t => {
  const result = run(t, variables);
  assert.equal(result.status, 0, result.stderr);
  assert.equal(result.calls, 'keytool\nsign\nverify\n');
});

test('persistent signing accepts wrapped base64 without generating a new key', t => {
  const result = run(t, { ...persistent, EXPECTED_KEYSTORE: 'persistent-key',
    NIGHTLY_KEYSTORE_BASE64: ' \n' + persistent.NIGHTLY_KEYSTORE_BASE64.match(/.{1,4}/g).join('\r\n\t') + '\n' });
  assert.equal(result.status, 0, result.stderr);
  assert.equal(result.calls, 'sign\nverify\n');
});

for (const missing of secretNames) test(`partial configuration rejects a missing ${missing}`, t => {
  const variables = { ...persistent };
  delete variables[missing];
  const result = run(t, variables);
  assert.notEqual(result.status, 0);
  assert.match(result.stderr, missing === 'NIGHTLY_KEYSTORE_BASE64' ? /Configure all four/ : /Missing nightly/);
  assert.equal(result.calls, '');
});

test('invalid base64 fails before signing and cleans its temporary directory', t => {
  const result = run(t, { ...persistent, NIGHTLY_KEYSTORE_BASE64: 'invalid!base64' });
  assert.notEqual(result.status, 0);
  assert.equal(result.calls, '');
});

for (const failure of ['FAIL_SIGN', 'FAIL_VERIFY']) test(`${failure} is not reported as a successful build`, t => {
  const result = run(t, { NIGHTLY_KEYSTORE_BASE64: '', [failure]: '1' });
  assert.notEqual(result.status, 0);
  assert.equal(result.calls, failure === 'FAIL_SIGN' ? 'keytool\nsign\n' : 'keytool\nsign\nverify\n');
});
