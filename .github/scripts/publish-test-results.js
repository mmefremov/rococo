// @ts-check
'use strict';

const fs = require('fs');

module.exports = async ({github, context, core, glob}) => {
  function parseXmlFiles(files) {
    let tests = 0, failures = 0, errors = 0, skipped = 0;
    for (const file of files) {
      try {
        const content = fs.readFileSync(file, 'utf8');
        for (const match of content.matchAll(/<testsuite\b[^>]*>/g)) {
          const attr = (name) => parseInt(
              match[0].match(new RegExp(`${name}="(\\d+)"`))?.[1] ?? '0');
          tests += attr('tests');
          failures += attr('failures');
          errors += attr('errors');
          skipped += attr('skipped');
        }
      } catch (_) {
      }
    }
    return {
      tests,
      passed: tests - failures - errors - skipped,
      failures,
      errors,
      skipped
    };
  }

  function badge(label, r) {
    const enc = (s) => encodeURIComponent(s).replace(/-/g, '--');
    if (r.tests === 0) {
      return `![${label}](https://img.shields.io/badge/${enc(
          label)}-no_results-lightgrey)`;
    }
    const failed = r.failures + r.errors;
    const msg = failed > 0
        ? `${r.passed}/${r.tests} passed, ${failed} failed`
        : `${r.passed}/${r.tests} passed`;
    const color = failed > 0 ? 'red' : 'brightgreen';
    return `![${label}](https://img.shields.io/badge/${enc(label)}-${enc(
        msg)}-${color})`;
  }

  const globber = await glob.create('**/build/test-results/**/TEST-*.xml');
  const allFiles = await globber.glob();

  const unitFiles = allFiles.filter(f => !f.includes('rococo-tests/'));
  const e2eFiles = allFiles.filter(f => f.includes('rococo-tests/'));
  const apiFiles = e2eFiles.filter(f => /\/\w*[Aa]pi\//.test(f));
  const uiFiles = e2eFiles.filter(f => /\/\w*[Uu]i\//.test(f));

  const unit = parseXmlFiles(unitFiles);
  const api = parseXmlFiles(apiFiles);
  const ui = parseXmlFiles(uiFiles);

  const badges = [
    badge('unit tests', unit),
    badge('api tests', api),
    badge('ui tests', ui),
  ].join('\n');

  const artifactUrl = process.env.ARTIFACT_URL;
  const reportBadge = artifactUrl
      ? `\n[![allure report](https://img.shields.io/badge/allure-report.zip-blueviolet)](${artifactUrl})`
      : '';

  // Job Summary
  await core.summary.addRaw(badges + reportBadge).write();

  // PR comment
  if (context.eventName === 'pull_request') {
    await github.rest.issues.createComment({
      owner: context.repo.owner,
      repo: context.repo.repo,
      issue_number: context.issue.number,
      body: badges + reportBadge,
    });
  }
};
