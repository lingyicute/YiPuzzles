/* SPDX-License-Identifier: GPL-3.0-or-later */
// Run with Playwright available in Node's module path:
// CHROMIUM_PATH=/path/to/chromium node --test tests/web-regression.cjs
const assert = require('node:assert/strict');
const { readFile } = require('node:fs/promises');
const { createServer } = require('node:http');
const { join } = require('node:path');
const { after, before, test } = require('node:test');
const { chromium } = require('playwright');

let browser, server, origin;
const routes = new Map([['/h5-demo/', 'h5-demo'], ['/website/', 'website']]);
const board = (first = []) => [first.concat([0, 0, 0, 0]).slice(0, 4), ...Array.from({ length: 3 }, () => [0, 0, 0, 0])];
const terminal = () => Array.from({ length: 4 }, (_, y) => Array.from({ length: 4 }, (_, x) => (x + y) % 2 ? 4 : 2));
const archive = page => page.evaluate(() => JSON.parse(localStorage.getItem('yp.state.4')));

before(async () => {
  const files = new Map();
  for (const [path, directory] of routes) files.set(path, await readFile(join(__dirname, '..', directory, 'index.html')));
  server = createServer((req, res) => {
    const html = files.get(req.url);
    res.writeHead(html ? 200 : 404, { 'Content-Type': 'text/html; charset=utf-8' }).end(html);
  });
  await new Promise(resolve => server.listen(0, '127.0.0.1', resolve));
  origin = `http://127.0.0.1:${server.address().port}`;
  browser = await chromium.launch({ executablePath: process.env.CHROMIUM_PATH || undefined });
});

after(async () => {
  try { await browser?.close(); }
  finally { if (server) await new Promise(resolve => server.close(resolve)); }
});

async function open(t, path, { options, records, setup } = {}) {
  const context = await browser.newContext({ reducedMotion: 'reduce', ...options });
  await context.route('**/*', route => route.request().url().startsWith(origin + '/') ? route.continue() : route.abort());
  const page = await context.newPage();
  const errors = [];
  const requests = [];
  page.on('pageerror', error => errors.push(error.message));
  page.on('request', req => requests.push(req.url()));
  t.after(async () => {
    await context.close();
    assert.deepEqual(errors, []);
  });
  if (records) await page.addInitScript(values => {
    if (sessionStorage.getItem('seeded')) return;
    sessionStorage.setItem('seeded', '1');
    for (const [key, value] of Object.entries(values)) localStorage.setItem(key, JSON.stringify(value));
  }, records);
  if (setup) await page.addInitScript(setup);
  await page.goto(origin + path, { waitUntil: 'domcontentloaded' });
  return { context, page, requests };
}

test('a winning move survives reload until the player acknowledges it', async t => {
  const { page } = await open(t, '/h5-demo/', {
    options: { reducedMotion: 'no-preference' },
    records: { 'yp.state.4': { g: board([1024, 1024]), score: 0 } }
  });
  await page.locator('#board').press('ArrowLeft');
  assert.equal((await archive(page)).winPending, true);
  await page.reload();
  await page.locator('#overlay.show [data-act="continue"]').click();
  assert.equal((await archive(page)).winPending, false);
  await page.reload();
  assert.equal(await page.locator('#overlay').evaluate(el => el.inert), true);
  assert.equal(await page.locator('#score').getAttribute('aria-label'), '2048');
});

test('terminal wins lead to the saved game over and preserve the round record', async t => {
  const g = terminal();
  g[0][0] = 2048;
  const { page } = await open(t, '/h5-demo/', { records: {
    'yp.best.4': 2100, 'yp.state.4': { g, winPending: true, score: 2100, bestAtRoundStart: 100 }
  } });
  await page.locator('#overlay.show [data-act="continue"]').click();
  assert.match(await page.locator('#overlay p').textContent(), /新纪录/);
  await page.reload();
  await page.locator('#overlay.show [data-act="new"]').waitFor({ state: 'visible' });
  assert.match(await page.locator('#overlay p').textContent(), /新纪录/);
});

test('undo rejects unplayable snapshots, keeps depth, and isolates sizes', async t => {
  const undo = Array.from({ length: 25 }, (_, score) => ({ g: board([2]), score }));
  undo.push({ g: board(), score: 99 }, { g: terminal(), score: 100 });
  const { page } = await open(t, '/h5-demo/', { records: {
    'yp.state.4': { g: board([4]), score: 30, undo }
  } });
  await page.locator('#undoBtn').click();
  assert.equal((await archive(page)).score, 24);
  assert.equal((await archive(page)).undo.length, 24);
  await page.getByRole('radio', { name: '4×4' }).press('ArrowRight');
  const selected = page.getByRole('radio', { name: '5×5' });
  assert.equal(await selected.getAttribute('aria-checked'), 'true');
  assert.equal(await selected.evaluate(el => el === document.activeElement), true);
  await selected.press('ArrowLeft');
  assert.equal(await page.locator('#score').getAttribute('aria-label'), '24');
});

test('large tiles survive merging and reload without corrupting scores', async t => {
  const max = 2 ** 52;
  const g = board([max, max, 2 ** 30, 2 ** 30]);
  const { page } = await open(t, '/h5-demo/', { records: {
    'yp.state.4': { g, score: Number.MAX_SAFE_INTEGER - 10 }
  } });
  await page.locator('#board').press('ArrowLeft');
  assert.deepEqual((await archive(page)).g[0].slice(0, 3), [max, max, 2 ** 31]);
  await page.reload();
  assert.equal(await page.locator('#score').getAttribute('aria-label'), String(Number.MAX_SAFE_INTEGER));
  assert.equal(await page.locator('#tiles .inner').filter({ hasText: String(2 ** 31) }).count(), 1);
});

test('shortcuts require board focus and an offline drag can end outside the board', async t => {
  const { context, page, requests } = await open(t, '/h5-demo/', { records: {
    'yp.state.4': { g: board([2, 2]), score: 8 }
  } });
  await context.setOffline(true);
  await page.locator('#themeBtn').press('r');
  await page.keyboard.press('ArrowRight');
  assert.equal((await archive(page)).score, 8);
  const box = await page.locator('#board').boundingBox();
  await page.mouse.move(box.x + 100, box.y + 80);
  await page.mouse.down();
  await page.mouse.move(Math.max(0, box.x - 4), box.y + 80);
  await page.mouse.up();
  assert.equal((await archive(page)).score, 12);
  assert.equal(await page.locator('#board').evaluate(el => el === document.activeElement), true);
  assert.match(await page.locator('#boardState').textContent(), /第 1 行：4/);
  assert.ok(requests.every(url => url.startsWith(origin + '/')));
});

test('a game reset keeps appearance and unrelated origin data', async t => {
  const { page } = await open(t, '/h5-demo/', { records: {
    'yp.settings': { mode: 'dark', size: 4 }, 'yp.best.5': 8, 'yp.state.5': 3, 'yp.unrelated': 'keep'
  } });
  await page.locator('#menuBtn').click();
  page.once('dialog', dialog => dialog.accept());
  await page.locator('#clearBtn').click();
  assert.deepEqual(await page.evaluate(() => [localStorage.getItem('yp.settings'), localStorage.getItem('yp.best.5'), localStorage.getItem('yp.state.5'), localStorage.getItem('yp.unrelated')]),
    ['{"mode":"dark","size":4}', null, null, '"keep"']);
});

test('mobile site keeps downloads and navigation available with JavaScript disabled', async t => {
  const { page } = await open(t, '/website/', { options: {
    javaScriptEnabled: false, reducedMotion: 'no-preference', viewport: { width: 375, height: 780 }
  } });
  assert.equal(await page.locator('.nav-links').isVisible(), true);
  assert.equal(await page.locator('#loader').count(), 0);
  assert.equal(await page.locator('#download .reveal').first().evaluate(el => getComputedStyle(el).opacity), '1');
  assert.equal(await page.locator('#fab').isVisible(), false);
});

test('site tracks system colors until chosen manually and restores mobile focus', async t => {
  const { page } = await open(t, '/website/', { options: { colorScheme: 'light', viewport: { width: 375, height: 780 } } });
  await page.emulateMedia({ colorScheme: 'dark' });
  await page.waitForFunction(() => document.documentElement.dataset.theme === 'dark');
  assert.equal(await page.evaluate(() => localStorage.getItem('yi-theme')), null);
  await page.locator('#themeToggle').click();
  assert.equal(await page.evaluate(() => localStorage.getItem('yi-theme')), 'light');
  await page.reload();
  assert.equal(await page.locator('html').getAttribute('data-theme'), 'light');

  const button = page.locator('#menuBtn');
  await button.press('Enter');
  const link = page.locator('#menuDrop a[href="#download"]');
  await link.press('Escape');
  assert.equal(await button.evaluate(el => el === document.activeElement), true);
  assert.equal(await button.getAttribute('aria-expanded'), 'false');
  await button.press('Enter');
  await link.press('Enter');
  assert.equal(await page.evaluate(() => document.activeElement.id), 'download');
});

test('copy failures are reported while preserving focus and removing fallback inputs', async t => {
  const { page } = await open(t, '/website/', { setup: () => {
    Object.defineProperty(navigator, 'clipboard', { value: { writeText: async () => { throw new Error('denied'); } } });
    document.execCommand = () => false;
  } });
  const button = page.locator('[data-copy="cmd1"]');
  await button.press('Enter');
  await page.waitForFunction(() => document.querySelector('#snackbar').textContent.includes('无法访问剪贴板'));
  assert.equal(await button.evaluate(el => el === document.activeElement), true);
  assert.equal(await page.locator('textarea').count(), 0);
  await page.evaluate(() => { document.execCommand = () => true; });
  await button.press('Enter');
  await page.waitForFunction(() => document.querySelector('#snackbar').textContent.includes('已复制'));
});

test('a higher best score saved by another tab is adopted instead of overwritten', async t => {
  const { context, page } = await open(t, '/h5-demo/', {
    records: { 'yp.state.4': { g: board([2, 2, 4]), score: 0 } },
    setup: () => { Math.random = () => 0; }
  });
  await page.locator('#board').press('ArrowLeft');
  const other = await context.newPage();
  await other.goto(origin + '/h5-demo/');
  await other.evaluate(() => localStorage.setItem('yp.best.4', '132'));
  await page.waitForFunction(() => document.querySelector('#best').getAttribute('aria-label') === '132');
  await page.locator('#board').press('ArrowLeft');
  assert.equal(await page.locator('#score').getAttribute('aria-label'), '12');
  assert.equal(await page.evaluate(() => localStorage.getItem('yp.best.4')), '132');
  await page.getByRole('radio', { name: '5×5' }).click();
  await page.getByRole('radio', { name: '4×4' }).click();
  assert.equal(await page.locator('#best').getAttribute('aria-label'), '132');
});

for (const failure of ['quota', 'denied']) {
  test(`session progress survives board switches when storage is ${failure}`, async t => {
    const { page } = await open(t, '/h5-demo/', {
      records: failure === 'quota' ? {
        'yp.best.4': 8, 'yp.state.4': { g: board([2, 2]), score: 8 },
        'yp.best.5': 40, 'yp.state.5': { g: Array.from({ length: 5 }, () => [2, 0, 0, 0, 0]), score: 40 }
      } : undefined,
      setup: failure === 'quota' ? () => {
        Storage.prototype.setItem = Storage.prototype.removeItem = () => { throw new DOMException('Full', 'QuotaExceededError'); };
      } : () => {
        Math.random = () => 0;
        Object.defineProperty(window, 'localStorage', { get() { throw new DOMException('Denied', 'SecurityError'); } });
      }
    });
    await page.locator('#board').press('ArrowLeft');
    const score = await page.locator('#score').getAttribute('aria-label');
    const state = await page.locator('#boardState').textContent();
    assert.equal(score, failure === 'quota' ? '12' : '4');
    await page.getByRole('radio', { name: '5×5' }).click();
    await page.getByRole('radio', { name: '4×4' }).click();
    assert.equal(await page.locator('#score').getAttribute('aria-label'), score);
    assert.equal(await page.locator('#best').getAttribute('aria-label'), score);
    assert.equal(await page.locator('#boardState').textContent(), state);
    assert.equal(await page.locator('#storageNotice').isVisible(), true);
    await page.locator('#undoBtn').click();
    assert.equal(await page.locator('#score').getAttribute('aria-label'), failure === 'quota' ? '8' : '0');
    await page.locator('#menuBtn').click();
    page.once('dialog', dialog => dialog.accept());
    await page.locator('#clearBtn').click();
    await page.getByRole('radio', { name: '5×5' }).click();
    assert.equal(await page.locator('#score').getAttribute('aria-label'), '0');
    assert.equal(await page.locator('#best').getAttribute('aria-label'), '0');
  });
}
