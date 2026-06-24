# Recaf Discord RPC

A [Recaf](https://github.com/Col-E/Recaf) plugin that adds **Discord Rich Presence**, showing your friends exactly what you're reverse‑engineering — the JAR you opened, the class you're reading, and the type of class it is.

<p align="center">
  <img src="assets/1.png" alt="Recaf Discord Rich Presence — browsing a workspace" width="520">
</p>

The presence updates live as you switch tabs, and an elapsed timer tracks your session from the moment Recaf launched.

---

## What it shows

The presence reflects whatever you're currently looking at:


| State                         | Details                              | Sub‑text                                        |
| ------------------------------- | -------------------------------------- | -------------------------------------------------- |
| **Idle** — no workspace open | `Idle`                               | `No workspace open`                              |
| **Workspace open**            | `Browsing fabric-loader-0.16.14.jar` | `712 classes`                                    |
| **Viewing a class**           | `Viewing AppletMain`                 | `net.fabricmc.loader.impl.game.minecraft.applet` |
| **Editing in the assembler**  | `Editing AppletMain`                 | `Bytecode assembler` / `Assembler · <member>`   |

When you open a class, the large icon switches to match the **type** of class, and the Recaf logo moves to the small badge:

<p align="center">
  <img src="assets/2.png" alt="Viewing a class" width="300">
  <img src="assets/3.png" alt="Viewing an annotation" width="300">
  <img src="assets/4.png" alt="Viewing an interface" width="300">
</p>

## Class‑type icons

Each class type gets its own color‑coded icon, mirroring Recaf's own conventions:

<table>
  <tr>
    <td align="center"><img src="assets/discord/class.png" width="72"><br><b>Class</b></td>
    <td align="center"><img src="assets/discord/class_abstract.png" width="72"><br><b>Abstract class</b></td>
    <td align="center"><img src="assets/discord/interface.png" width="72"><br><b>Interface</b></td>
    <td align="center"><img src="assets/discord/enum.png" width="72"><br><b>Enum</b></td>
    <td align="center"><img src="assets/discord/annotation.png" width="72"><br><b>Annotation</b></td>
  </tr>
</table>

## Building

The plugin builds with Gradle and requires **JDK 22+** (same as Recaf).

```bash
./gradlew build
```

The plugin jar is written to `build/libs/discord-rpc-1.0.0.jar`. Drop it into your Recaf `plugins` directory:


| OS      | Location                                      |
| --------- | ----------------------------------------------- |
| Windows | `%APPDATA%\Recaf\plugins`                     |
| Linux   | `~/.config/Recaf/plugins`                     |
| macOS   | `~/Library/Application Support/Recaf/plugins` |

Then start Recaf with Discord running. That's it — it's ready to use out of the box.

> You can also launch Recaf with the plugin already loaded straight from this project:
>
> ```bash
> ./gradlew runRecaf
> ```
