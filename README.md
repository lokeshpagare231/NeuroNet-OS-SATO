# NeuroNet-OS-SATO

NeuroNet-OS-SATO is a Yocto Project / OpenEmbedded build workspace based on the
Poky reference distribution. It is intended to build a **Sato**-style graphical
image (the classic Yocto demo UI) and to provide a place to carry project-specific
customizations as a separate layer (`meta-neuronet`).

This repository contains the full Poky integration tree (BitBake + OE-Core +
meta-poky + BSPs + docs) **plus**:

- `meta-neuronet`: a custom layer for NeuroNet-OS-SATO changes and additional recipes
- `meta-openembedded` (as a git submodule): extra community layers (OE, Python,
  Networking, GNOME, Multimedia, …)

If you are new to Yocto, start with the **Build Quick Start** section below and
then refer to the upstream manuals in `documentation/` (or online at
https://docs.yoctoproject.org/).

## Table of Contents

- [What This Repo Builds](#what-this-repo-builds)
- [Repository Layout (High Level)](#repository-layout-high-level)
- [Build Quick Start (QEMU)](#build-quick-start-qemu)
- [Build Configuration Files](#build-configuration-files)
- [NeuroNet Layer (`meta-neuronet`)](#neuronet-layer-meta-neuronet)
- [Adding New Content](#adding-new-content)
- [Common BitBake Commands](#common-bitbake-commands)
- [Troubleshooting](#troubleshooting)
- [Documentation](#documentation)
- [Security](#security)
- [License](#license)
- [Upstream References](#upstream-references)

---

## What This Repo Builds

The main “product” of this repo is a bootable Linux image produced by BitBake.
Typical outcomes include:

- a root filesystem image (e.g., `.wic`, `.ext4`, `.tar.bz2` depending on image type)
- a Linux kernel + bootloader artifacts (depending on the selected `MACHINE`)
- SDK/toolchain artifacts (optional)

### Sato image

“Sato” is a Yocto Project demo environment (applications + themes + UI defaults)
intended to showcase an embedded graphical stack. In Poky, it is built via:

```sh
bitbake core-image-sato
```

In this repository, `meta-neuronet` contains a `bbappend` for `core-image-sato`
that adjusts package contents (see `meta-neuronet/recipes-sato/images/core-image-sato.bbappend`).

---

## Repository Layout (High Level)

This is a Poky-style tree. The most important top-level paths:

- `oe-init-build-env`: environment setup script (creates/enters a build directory)
- `bitbake/`: BitBake (the build engine)
- `meta/`: OpenEmbedded-Core metadata (core recipes, classes, QA)
- `meta-poky/`: Poky reference distro configuration
- `meta-yocto-bsp/`: reference BSPs / machine configurations
- `meta-openembedded/`: additional community layers (git submodule)
- `meta-neuronet/`: project layer for NeuroNet-OS-SATO customizations
- `scripts/`: helper tools (`devtool`, `runqemu`, `bitbake-layers`, …)
- `documentation/`: Yocto Project manuals (Sphinx sources)

For upstream Poky repository details, see `README.poky.md`.

---

## Build Quick Start (QEMU)

These steps build and boot a Sato image under QEMU on a typical Linux host.

### 1) Host prerequisites

Building Yocto images requires substantial disk space and a set of host packages
(compilers, python, build tools, etc.). Follow the official “Required packages
for the build host” documentation for your distro:

- https://docs.yoctoproject.org/

Minimum practical guidance:

- Expect **tens of GB** of disk usage for a first build.
- Use a fast local disk (SSD) if possible.
- Prefer a supported Linux distribution for the selected Yocto release.

### 2) Clone and initialize submodules

This repo uses `meta-openembedded` as a submodule:

```sh
git submodule update --init --recursive
```

### 3) Initialize the build environment

Create a fresh build directory (recommended). Example:

```sh
source oe-init-build-env build-neuronet
```

This drops you into the build directory and creates `conf/local.conf` and
`conf/bblayers.conf` if they do not exist.

Note:

- A `build/` directory is present in this repository, but it may contain
  host-specific settings (e.g., absolute paths). For a clean start, create your
  own build directory (e.g., `build-neuronet/`) as shown above.

### 4) Add layers (including `meta-neuronet`)

From inside your build directory:

```sh
bitbake-layers add-layer ../meta-neuronet
bitbake-layers add-layer ../meta-openembedded/meta-oe
bitbake-layers add-layer ../meta-openembedded/meta-python
bitbake-layers add-layer ../meta-openembedded/meta-networking
bitbake-layers add-layer ../meta-openembedded/meta-gnome
bitbake-layers add-layer ../meta-openembedded/meta-multimedia
```

Verify:

```sh
bitbake-layers show-layers
```

### 5) Choose a machine (QEMU example)

Edit `conf/local.conf` and set `MACHINE`. For x86_64 QEMU:

```conf
MACHINE ??= "qemux86-64"
```

### 6) Build the image

```sh
bitbake core-image-sato
```

Build outputs appear under `tmp/deploy/images/<machine>/`.

### 7) Boot with QEMU

After the build completes:

```sh
runqemu qemux86-64 core-image-sato
```

If you want a serial-console-only boot (no graphics), try:

```sh
runqemu qemux86-64 core-image-sato nographic
```

For more QEMU details, see `README.qemu.md` and the Yocto manuals.

---

## Build Configuration Files

Once you have sourced `oe-init-build-env <build-dir>`, the build directory
contains configuration under `conf/`:

- `conf/local.conf`: local, per-build settings (machine, parallelism, image tweaks)
- `conf/bblayers.conf`: which layers are enabled for the build

### `local.conf` (most common edits)

Common variables you may set:

- `MACHINE`: your target (e.g., `qemux86-64`, `qemuarm64`, or a real board)
- `DISTRO`: distro policy (defaults to `poky` in a Poky-based setup)
- `BB_NUMBER_THREADS` / `PARALLEL_MAKE`: tune host parallelism
- `DL_DIR` / `SSTATE_DIR`: share downloads and sstate across builds
- `IMAGE_INSTALL:append`: add packages to an image

### `bblayers.conf` (enabling metadata layers)

Layers are added and removed via `bitbake-layers add-layer` and
`bitbake-layers remove-layer`. Keeping `meta-neuronet` separate from upstream
layers is intentional: it localizes NeuroNet-OS-SATO changes and makes upstream
updates easier to manage.

---

## NeuroNet Layer (`meta-neuronet`)

`meta-neuronet` is where project-specific metadata belongs. Keeping changes in a
separate layer makes it easier to:

- rebase or update the underlying Poky/OE-Core
- track and review project-specific deltas
- selectively enable/disable features via layers

### Layer compatibility

`meta-neuronet/conf/layer.conf` declares compatibility with the Yocto release
series:

- `LAYERSERIES_COMPAT_meta-neuronet = "whinlatter"`

### What `meta-neuronet` currently contains

At the time of writing, the layer contains:

- `core-image-sato` append:
  - `meta-neuronet/recipes-sato/images/core-image-sato.bbappend`
  - removes `gtk-theme-switch` from `IMAGE_INSTALL` (package no longer available)
- Accessibility recipe:
  - `meta-neuronet/recipes-accessibility/orca/orca_45.2.bb` (Orca screen reader)
- Multimedia recipe:
  - `meta-neuronet/recipes-multimedia/portaudio/portaudio_19.7.0.bb`
  - uses an in-layer source tarball at
    `meta-neuronet/recipes-multimedia/portaudio/files/portaudio-19.7.0.tgz`
- Example recipe:
  - `meta-neuronet/recipes-example/example/example_0.1.bb`

### Building layer recipes directly

From an initialized build directory:

```sh
bitbake orca
bitbake portaudio
bitbake example
```

### Including `orca` / `portaudio` in an image

These recipes are available once the layer is added, but they are not
automatically installed into `core-image-sato` unless you explicitly add them.

Option A: add packages from `conf/local.conf`:

```conf
IMAGE_INSTALL:append = " orca portaudio"
```

Option B: create a custom image recipe in a project layer (recommended for
maintainability), for example `recipes-core/images/neuronet-image.bb` that
`require`s an existing image and appends packages.

---

## Adding New Content

This repository is intended to be extended primarily via `meta-neuronet`.
Typical additions:

- new recipes (`.bb`) for project applications and dependencies
- recipe appends (`.bbappend`) to adjust upstream recipes without forking
- image recipes to define what ships in the final system
- distro / machine configuration (when you need project-specific policy or boards)

### Adding a recipe (common approaches)

- Use `devtool add` or `devtool modify` when iterating quickly.
- Use `bitbake-layers create-recipe` for basic scaffolding.
- For upstream projects using autotools/cmake/meson, prefer the matching
  `inherit` classes and follow patterns in OE-Core and meta-openembedded.

Useful discovery commands:

```sh
bitbake-layers show-recipes | less
bitbake-layers show-appends | less
```

### Creating a custom image

If you want a stable, reviewable definition of the final image, create a custom
image recipe in `meta-neuronet` instead of relying on `local.conf` edits.

Typical pattern:

- create `meta-neuronet/recipes-core/images/neuronet-image.bb`
- base it on an existing image (e.g., `core-image-sato`)
- append packages and features needed by NeuroNet-OS-SATO

Then build:

```sh
bitbake neuronet-image
```

---

## Key Concepts (Yocto / OpenEmbedded)

If you are coming from traditional distro build systems (Debian, Fedora, etc.),
Yocto works differently. The most important concepts:

- **Recipe (`.bb`)**: instructions to fetch, configure, build, and package a component
- **Append (`.bbappend`)**: modifies an existing recipe without forking it
- **Layer**: a collection of recipes/configs (e.g., `meta`, `meta-openembedded`, `meta-neuronet`)
- **Image**: a recipe that produces a complete root filesystem and deployment artifacts
- **Machine (`MACHINE`)**: target hardware selection (CPU arch, kernel config, boot method, …)
- **Distro (`DISTRO`)**: policy defaults (packaging, init system, versions, …)

Helpful layer tools:

```sh
bitbake-layers show-layers
bitbake-layers show-recipes
bitbake-layers show-appends
```

---

## Common BitBake Commands

### Rebuilding / cleaning

```sh
bitbake -c clean core-image-sato
bitbake -c cleansstate core-image-sato
```

### Inspecting environment

```sh
bitbake -e core-image-sato | less
bitbake -g core-image-sato
```

### Finding which recipe provides a file/package

```sh
oe-pkgdata-util find-path /usr/bin/<binary>
oe-pkgdata-util find-pkg <packagename>
```

### Developing a recipe

Yocto includes `devtool` (in `scripts/devtool`):

```sh
devtool modify <recipe>
devtool build <recipe>
devtool finish <recipe> <layer-path>
```

### Running unit/self tests (tools/metadata)

This repository includes `oe-selftest`:

```sh
oe-selftest --list-tests
oe-selftest --run-tests <module.class.test>
```

Note that selftests may create temporary build directories and require additional
host dependencies.

---

## Troubleshooting

### `bitbake` / `bitbake-layers` not found

You likely have not sourced the build environment. From the repo root:

```sh
source oe-init-build-env build-neuronet
```

### Submodule errors (missing `meta-openembedded`)

Initialize/update submodules:

```sh
git submodule update --init --recursive
```

### Slow builds

Consider:

- placing `DL_DIR` and `SSTATE_DIR` on a fast disk
- increasing `BB_NUMBER_THREADS` and `PARALLEL_MAKE` appropriately for your host
- reusing `downloads/` and `sstate-cache/` between builds

### QEMU boots but no UI

Sato images require a working graphical stack; check that you built a Sato image
(`core-image-sato`) and that you are not using `nographic` when you expect a GUI.

---

## Documentation

Upstream Yocto manuals are included in `documentation/`.

To build the HTML manuals locally (requires Sphinx + dependencies):

```sh
cd documentation
make html
```

The generated index is `documentation/_build/html/index.html`.

---

## Security

See `SECURITY.md`.

---

## License

This repository includes components from multiple upstream projects and thus has
multiple licenses. See:

- `LICENSE`
- `LICENSE.MIT`
- `LICENSE.GPL-2.0-only`
- per-recipe `LICENSE` fields and `LIC_FILES_CHKSUM` entries

---

## Upstream References

- Poky overview: `README.poky.md`
- OpenEmbedded-Core overview: `README.OE-Core.md`
- QEMU notes: `README.qemu.md`
- Yocto Project documentation: https://docs.yoctoproject.org/
