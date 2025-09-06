SUMMARY = "Orca screen reader"
DESCRIPTION = "Orca is a free, open source flexible screen reader for Unix-like systems."
HOMEPAGE = "https://wiki.gnome.org/Projects/Orca"

PV = "45.2"
LICENSE = "LGPL-2.1-only"
LIC_FILES_CHKSUM = "file://COPYING;md5=4fbd65380cdd255951079008b364516c"

MAJORVER = "${@d.getVar('PV').split('.')[0]}"
SRC_URI = "https://download.gnome.org/sources/orca/${MAJORVER}/orca-${PV}.tar.xz"
# IMPORTANT: use the archive.sha256sum key (not just sha256sum)
SRC_URI[sha256sum] = "f0f2c579a5beedfe5653bc7fe24481c4d6aac5dd1f71c1e7a208595f3c78ef76"

# S must be relative to UNPACKDIR (avoid the unpack error)
S = "${UNPACKDIR}/orca-${PV}"

# Build-time dependencies (native ones end in -native if needed)
DEPENDS = " \
    python3-pygobject \
    gtk+3 \
    at-spi2-core \
    gobject-introspection \
    gnome-common-native \
    itstool-native \
    yelp-tools-native \
"

# Runtime
RDEPENDS:${PN} += " \
    python3-core \
    python3-pygobject \
    gtk+3 \
    at-spi2-core \
    python3-setuptools\
"

# Ensure autotools path is used
inherit autotools pkgconfig gettext

# Include help, icons and lib files in the main package so packaging QA won't complain
FILES:${PN} += " \
    ${bindir} \
    ${libdir} \
    ${datadir} \
    ${datadir}/help \
    ${datadir}/icons \
"

# sanitize installed scripts to not refer to your host build tmp path
do_install:append() {
    # Make sure the main script doesn't refer to build-host absolute paths.
    if [ -f ${D}${bindir}/orca ]; then
        sed -i -E "s|/home/[^/]+/poky/build/tmp/hosttools/python3|/usr/bin/python3|g" ${D}${bindir}/orca || true
    fi
}

# If you later need to apply small configure fixes via patch, list them here (optional)
# SRC_URI += "file://my-fix.patch"

