# Skript Documentation Templates

Skript's features are documented directly in it's Java code. But we still need

1. Clear tutorials, not just "you can check the syntax pattern"
2. Examples explained, if needed

When generating final result, each HTML file is surrounded by template.html,
which provides head element, navigation bar and so on.

## Template Patterns

Patterns have syntax of ${pattern_here}. For example, ${skript.version} is replaced with
current Skript version. Please see below for more...

You can also include other files by using ${include <filename>}. Just please make
sure that those included files don't have tags which are not allowed in position
where include is called.

## Pattern Reference
```
skript.* - Information of Skript
version - Skript's version
include <filename> - Load given file and place them here
generate <expressions/effects/events/types/functions> <loop template file> - Generated reference
content - In template.html, marks the point where other file is placed
```

## Generating the documentation

1. You will need to create a directory named `doc-templates` in `plugins/Skript/`, and copy everything from [here](https://github.com/SkriptLang/Skript/tree/master/docs) into that directory.
2. Execute the command `/sk gen-docs`.
3. The `docs/` directory will be created _(if not created already)_ in `plugins/Skript` containing the website's files.
4. Open `index.html` and browse the documentation.
5. _(Optionally)_ Add this system property `-Dskript.forceregisterhooks` in your server startup script (before the -jar property) to force generating hooks docs.