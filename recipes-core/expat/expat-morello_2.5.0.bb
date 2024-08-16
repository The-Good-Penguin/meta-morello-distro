inherit cmake lib_package purecap-sysroot

MORELLO_SRC = "poky/meta/recipes-core/expat/expat_2.5.0.bb"

SUMMARY = "A stream-oriented XML parser library"
DESCRIPTION = "Expat is an XML parser library written in C. It is a stream-oriented parser in which an application registers handlers for things the parser might find in the XML document (like start tags)"
HOMEPAGE = "https://github.com/libexpat/libexpat"
SECTION = "libs"
LICENSE = "MIT"

LIC_FILES_CHKSUM = "file://COPYING;md5=7b3b078238d0901d3b339289117cb7fb"

VERSION_TAG = "${@d.getVar('PV').replace('.', '_')}"

SRC_URI = "https://github.com/libexpat/libexpat/releases/download/R_${VERSION_TAG}/expat-${PV}.tar.bz2  \
           file://0001-Apply-patch-for-CVE-2023-52426-backported-from-expat.patch \
           "

S = "${WORKDIR}/expat-${PV}"

TOOLCHAIN = "${MORELLO_TOOLCHAIN}"
CC:remove = "${CC_PURECAP_FLAGS}"

UPSTREAM_CHECK_URI = "https://github.com/libexpat/libexpat/releases/"

SRC_URI[sha256sum] = "6f0e6e01f7b30025fa05c85fdad1e5d0ec7fd35d9f61b22f34998de11969ff67"

do_configure() {
    cmake -S ${S} -B ${B} -DEXPAT_BUILD_TESTS:BOOL=OFF -DCMAKE_CXX_COMPILER_WORKS="1" -DCMAKE_INSTALL_PREFIX="${prefix}" -DCMAKE_C_COMPILER="${CC}" -DCMAKE_C_FLAGS="${CC_PURECAP_FLAGS}" -DCMAKE_C_LINK_FLAGS="${LD_PURECAP_FLAGS}"
}

CVE_PRODUCT = "expat libexpat"
