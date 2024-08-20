inherit autotools texinfo purecap-sysroot

MORELLO_SRC = "poky/meta/recipes-support/libffi/*"

SUMMARY = "A portable foreign function interface library"
HOMEPAGE = "http://sourceware.org/libffi/"
DESCRIPTION = "The `libffi' library provides a portable, high level programming interface to various calling \
conventions.  This allows a programmer to call any function specified by a call interface description at run \
time. FFI stands for Foreign Function Interface.  A foreign function interface is the popular name for the \
interface that allows code written in one language to call code written in another language.  The `libffi' \
library really only provides the lowest, machine dependent layer of a fully featured foreign function interface.  \
A layer must exist above `libffi' that handles type conversions for values passed between the two languages."

LICENSE = "MIT"
LIC_FILES_CHKSUM = "file://LICENSE;md5=679b5c9bdc79a2b93ee574e193e7a7bc"

SRC_URI = "git://github.com/CTSRD-CHERI/libffi.git;protocol=https;branch=master;rev=c666fdd8613b4d56130431c3e8f0bb05f30f07e8 \
           file://0001-Add-enable-cheri-ffi-closures-flag-to-enable-FFI-clo.patch \
           file://not-win32.patch \
           file://0001-arm-sysv-reverted-clang-VFP-mitigation.patch \
           "
SRC_URI[sha256sum] = "d66c56ad259a82cf2a9dfc408b32bf5da52371500b84745f7fb8b645712df676"

FILESEXTRAPATHS:prepend = "${THISDIR}/libffi:"

UPSTREAM_CHECK_URI = "https://github.com/libffi/libffi/releases/"
UPSTREAM_CHECK_REGEX = "libffi-(?P<pver>\d+(\.\d+)+)\.tar"

TOOLCHAIN = "${MORELLO_TOOLCHAIN}"

S = "${WORKDIR}/git"

EXTRA_OECONF += "--disable-builddir --disable-exec-static-tramp"
EXTRA_OECONF += " --enable-cheri-ffi-closures "
EXTRA_OEMAKE += "LIBTOOLFLAGS='--tag=CC'"

FILES:${PN}-dev += "${libdir}/libffi-${PV}"

# Doesn't compile in MIPS16e mode due to use of hand-written
# assembly
MIPS_INSTRUCTION_SET = "mips"
