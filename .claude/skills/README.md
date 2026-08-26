# Project skills

Skills vendored into the repo so every Claude Code session on `tekhelet` picks
them up automatically (including ephemeral web/cloud sessions, where a global
`~/.claude/skills` install does not survive).

## hebrew-first bundle (skills-il)

Installed from the [skills-il](https://www.npmjs.com/package/skills-il) registry:

```bash
npx skills-il add-bundle hebrew-first -a claude-code
```

Source: <https://github.com/skills-il/localization> (bundle definition in
`skills-il/bundles`, slug `hebrew-first`). All five skills are MIT licensed.

| Skill | Use for |
|-------|---------|
| `hebrew-content-writer` | Hebrew copy: UI strings, marketing, articles, register and grammar |
| `hebrew-document-generator` | Hebrew/RTL PDF, DOCX, PPTX output with correct bidi |
| `hebrew-nlp-toolkit` | Hebrew text processing, nikud, morphology, NER, STT models |
| `hebrew-rtl-best-practices` | RTL layout, CSS logical properties, bidi in web UI |
| `israeli-accessibility-compliance` | IS 5568 / WCAG 2.0 AA audits for Israeli sites |

Each skill ships an English `SKILL.md` and a Hebrew `SKILL_HE.md`.

To update:

```bash
npx skills-il update-bundle hebrew-first
```

then copy the refreshed skills back into this directory.
