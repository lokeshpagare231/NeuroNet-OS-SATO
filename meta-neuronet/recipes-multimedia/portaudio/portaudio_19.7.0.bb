SUMMARY = "PortAudio: Portable cross-platform Audio API"
DESCRIPTION = "PortAudio is a free, cross-platform, open-source, audio I/O library."
HOMEPAGE = "http://www.portaudio.com/"
LICENSE = "MIT"
LIC_FILES_CHKSUM = "file://LICENSE.txt;md5=26107732c2ab637c5710446fcfaf02df"

# Use the local tarball instead of fetching
SRC_URI = "file://portaudio-19.7.0.tgz"

# Correct unpack directory
S = "${UNPACKDIR}/portaudio-19.7.0"

inherit cmake pkgconfig

EXTRA_OECMAKE += "-DCMAKE_POLICY_VERSION_MINIMUM=3.5"

DEPENDS = "alsa-lib"
# Ensure Yocto doesn’t fail QA on .so file and build paths
INSANE_SKIP_${PN}-dev += "dev-elf buildpaths"
FILES_${PN}-dev += "${libdir}/libportaudio.so"



INSANE_SKIP:${PN} += "buildpaths"
INSANE_SKIP:${PN}-dev += "dev-elf buildpaths"
FILES:${PN}-dev += "${libdir}/libportaudio.so"
