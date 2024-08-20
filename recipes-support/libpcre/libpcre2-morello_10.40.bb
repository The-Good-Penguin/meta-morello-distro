inherit autotools binconfig-disabled purecap-sysroot

MORELLO_SRC = "poky/meta/recipes-support/libpcre/libpcre2_10.40.bb"

DESCRIPTION = "There are two major versions of the PCRE library. The \
newest version is PCRE2, which is a re-working of the original PCRE \
library to provide an entirely new API. The original, very widely \
deployed PCRE library's API and feature are stable, future releases \
 will be for bugfixes only. All new future features will be to PCRE2, \
not the original PCRE 8.x series."
SUMMARY = "Perl Compatible Regular Expressions version 2"
HOMEPAGE = "http://www.pcre.org"
SECTION = "devel"
LICENSE = "BSD-3-Clause"
LIC_FILES_CHKSUM = "file://LICENCE;md5=41bfb977e4933c506588724ce69bf5d2"

SRC_URI = "https://github.com/PhilipHazel/pcre2/releases/download/pcre2-${PV}/pcre2-${PV}.tar.bz2 \
    file://CVE-2022-41409.patch \
"

UPSTREAM_CHECK_URI = "https://github.com/PhilipHazel/pcre2/releases"

SRC_URI[sha256sum] = "14e4b83c4783933dc17e964318e6324f7cae1bc75d8f3c79bc6969f00c159d68"

CVE_PRODUCT = "pcre2"

TOOLCHAIN  = "${MORELLO_TOOLCHAIN}"

FILESEXTRAPATHS:prepend := "${THISDIR}/libpcre2:"

S = "${WORKDIR}/pcre2-${PV}"

PROVIDES += "pcre2-morello"
DEPENDS += "bzip2-morello zlib-morello"

BINCONFIG = "${bindir}/pcre2-config"

EXTRA_OECONF = "\
    --enable-newline-is-lf \
    --with-link-size=2 \
    --with-match-limit=10000000 \
    --enable-pcre2-16 \
    --enable-pcre2-32 \
"

CFLAGS += "-D_REENTRANT"
CXXFLAGS:append:powerpc = " -lstdc++"

do_install:append() {
    libtool --finish "${D}${libdir}"
}

PACKAGES =+ "libpcre2-16-morello libpcre2-32-morello pcre2grep-morello pcre2grep-doc-morello pcre2test-morello pcre2test-doc-morello"

SUMMARY:pcre2grep-morello = "grep utility that uses perl 5 compatible regexes"
SUMMARY:pcre2grep-doc-morello = "grep utility that uses perl 5 compatible regexes - docs"
SUMMARY:pcre2test-morello = "program for testing Perl-comatible regular expressions"
SUMMARY:pcre2test-doc-morello = "program for testing Perl-comatible regular expressions - docs"

# prevent a clash with the non-morello version of libpcre2 during do_package_ipk:
PKG:libpcre2-16-morello = "libpcre2-16-0-morello"
PKG:libpcre2-32-morello = "libpcre2-32-0-morello"

FILES:libpcre2-16-morello = "${libdir}/libpcre2-16.so.*"
FILES:libpcre2-32-morello = "${libdir}/libpcre2-32.so.*"
FILES:pcre2grep-morello = "${bindir}/pcre2grep"
FILES:pcre2grep-morello-doc = "${mandir}/man1/pcre2grep.1"
FILES:pcre2test-morello = "${bindir}/pcre2test"
FILES:pcre2test-morello-doc = "${mandir}/man1/pcre2test.1"
