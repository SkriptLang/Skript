<div align="center">
  <img src="https://raw.githubusercontent.com/SkriptLang/Skript/master/.github/assets/Cover.jpg" alt="Skript">
</div>

<br>

<div align="center">

[![Latest release](https://img.shields.io/github/v/release/SkriptLang/Skript?label=Version&color=orange)](https://github.com/SkriptLang/Skript/releases)
[![Docs](https://img.shields.io/badge/docs-docs.skriptlang.org-orange?label=Docs)](https://beta-docs.skriptlang.org/)
[![Discord](https://img.shields.io/badge/chat-on%20Discord-5865F2?label=Chat&logo=discord&logoColor=white)](https://discord.gg/ZPsZAg6ygu)
[![GitHub downloads](https://img.shields.io/github/downloads/SkriptLang/Skript/total?label=GitHub%20Downloads&logo=github&logoColor=white)](https://github.com/SkriptLang/Skript/releases)
[![Modrinth downloads](https://img.shields.io/modrinth/dt/xFNYAvMk?label=Modrinth%20Downloads&logo=modrinth&logoColor=white)](https://modrinth.com/plugin/skript)
[![Hangar downloads](https://img.shields.io/hangar/dt/Skript?label=Hangar%20Downloads&logo=data:image/svg%2Bxml;base64,PHN2ZyB4bWxucz0iaHR0cDovL3d3dy53My5vcmcvMjAwMC9zdmciIHZpZXdCb3g9IjAgMCAyNDEgMjI0LjUiPjxwYXRoIGZpbGw9IiNmZmYiIGQ9Ik0xNjMuNSAxMDMuMmMtOS43LTMuNi0yNCAxMC4yLTMxIDMxLjRhODMuOSA4My45IDAgMCAwLTMuNSAzNS45bC00LjUtMS45Yy04LjYtMjYuNy0zLjgtNTUuOSAxMS4xLTc3WiIvPjxwYXRoIGZpbGw9IiNmZmYiIGQ9Im0xNjkuOCAxNDguOC0zMy4yLTEzLjdjNi4xLTE4LjUgMTcuNC0yOS42IDI0LjctMjkuNmE3LjggNy44IDAgMCAxIDIuMi40bC42LjNjMi42IDEuMiAxMC41IDggNS43IDQyLjZNMTU3LjkgNjYuN2E3My40IDczLjQgMCAwIDAtMjUuOCAyMy40Yy0xNC42IDIxLjItMTkuNiA0OS45LTEyIDc2LjdhODIuMiA4Mi4yIDAgMCAwIDMuMyA5LjVsLTQ0LjUtMTguNGMtMTYuOC00MS4yLTEuNC05MC4zIDM0LjUtMTA5LjdaIi8%2BPC9zdmc%2B)](https://hangar.papermc.io/SkriptLang/Skript)
[![Servers](https://img.shields.io/bstats/servers/722?label=Servers&logo=hangar&logoColor=white)](https://bstats.org/plugin/bukkit/Skript/722)

</div>

---

**Skript** is a scripting language for Minecraft [Paper](https://papermc.io/) servers, which allows server owners to modify their server using plain English.

```applescript
on join:
    send "Welcome, %player%" to player

command /sethome:
    trigger:
        set {home::%player's uuid%} to player's location
        send "Home saved!" to player

command /home:
    trigger:
        if {home::%player's uuid%} is set:
            teleport player to {home::%player's uuid%}
            send "Teleported you to your home!" to player
        else:
            send "You don't have a home." to player
```

**Skript** comes with hundreds of built-in syntaxes for blocks, entities, inventories, particles, GUIs, commands and much more.\
If **Skript** can't do something, an [addon](#addons) probably can.

## Requirements

**Skript** requires [Paper](https://papermc.io/downloads/paper) to run. Folia, Spigot, Bukkit are **not** supported.

**Skript** supports the last 18 months of Minecraft versions, counting from the release date of **Skript**'s latest version.\
For example, this means that 1.20.4 is supported, but 1.20.3 is not.

New Minecraft versions will be supported as soon as possible.

## Download

You can find downloads for each version with their release notes in [the releases page](https://github.com/SkriptLang/Skript/releases).

**Skript** releases a major feature update 4 times per year, with monthly patches in between. See the [release model](CLOCKWORK_RELEASE_MODEL.md) for details.

## Documentation

Information about every syntax, tutorials, and API docs are available at **[docs.skriptlang.org](https://beta-docs.skriptlang.org/)** and the unofficial [Skript Hub](https://skripthub.net) page.

## Community

Need help with a script? Start with the [docs](#documentation), then ask:

- [skUnity Forums](https://forums.skunity.com)
- [skUnity Discord](https://discord.gg/0l3WlzBPKX7WNjkf)
- [Skript Chat Discord](https://discord.gg/V4qFVnh)

These communities are run independently of the Skript team. Please don't report their issues here.

## Help Us Test

**Want to test upcoming releases?** Join the [Official Testing Discord](https://discord.gg/ZPsZAg6ygu), and whenever we start testing new features/releases you will be the first to know.

## Addons

Plugins can add new syntaxes to **Skript** to extend it. You can find addons on [skUnity](https://forums.skunity.com/forums/addon-releases) and [Skript Hub](https://skripthub.net).

Addons are maintained by their authors, not by the Skript team. When reporting a bug, try to reproduce it **without addons** first.

## Using Skript as a dependency

**Gradle**

```kotlin
repositories {
    maven("https://repo.skriptlang.org/releases")
}

dependencies {
    compileOnly("com.github.SkriptLang:Skript:2.17.1")
}
```

**Maven**

```xml
<repositories>
    <repository>
        <id>skript-releases</id>
        <url>https://repo.skriptlang.org/releases</url>
    </repository>
</repositories>

<dependency>
    <groupId>com.github.SkriptLang</groupId>
    <artifactId>Skript</artifactId>
    <version>2.17.1</version>
    <scope>provided</scope>
</dependency>
```

If dependency resolution fails on `paper-api`, add Paper's repository as well:

```kotlin
maven("https://repo.papermc.io/repository/maven-public/")
```

Skript's API is not guaranteed stable between feature releases. Pin the version you build
against and test addons against each new release you intend to support.

## Building from source

Requires **JDK 25**. The `skript-aliases` submodule is required, so clone recursively:

```bash
git clone --recurse-submodules https://github.com/SkriptLang/Skript.git
cd Skript
./gradlew clean build        # gradlew.bat on Windows
```

The plugin jar is written to `build/libs/Skript-<version>.jar`.

### Running tests

TODO TODO TODO

## Contributing

- Read the [contribution guidelines](.github/contributing.md) before opening an issue or pull request.
- If you are contributing Java code, follow our [code conventions](code-conventions.md).
- Branch layout is available in [branches.md](branches.md). Use `dev/feature` for features and breaking changes, and `dev/patch` for fixes.
- Found a security issue? Report it privately via [GitHub security advisories](https://github.com/SkriptLang/Skript/security/advisories/new) instead of the public tracker.

## License

Skript is licensed under the **GNU General Public License v3.0** see [LICENSE](LICENSE).\
Contributors may additionally release their code under the MIT License. see [LICENSING.md](LICENSING.md).

Skript began as Njol's original plugin, continued through Mirreski's fork, and is now maintained by the SkriptLang team and [contributors](https://github.com/SkriptLang/Skript/graphs/contributors).

---

[![Avatars of Skript contributors](https://contrib.rocks/image?repo=SkriptLang/Skript&max=300&columns=30)](https://github.com/SkriptLang/Skript/graphs/contributors)
