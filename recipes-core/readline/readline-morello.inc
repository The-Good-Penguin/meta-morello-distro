inherit update-alternatives purecap-sysroot
inherit autotools texinfo

MORELLO_SRC = "poky/meta/recipes-core/readline/readlinge_8.1.2.bb"

SUMMARY     = "Library for editing typed command lines"
DESCRIPTION = "The GNU Readline library provides a set of functions for use by applications that allow users to edit \
command lines as they are typed in. Both Emacs and vi editing modes are available. The Readline library includes  \
additional functions to maintain a list of previously-entered command lines, to recall and perhaps reedit those   \
lines, and perform csh-like history expansion on previous commands."
SECTION  = "libs"
HOMEPAGE = "https://tiswww.case.edu/php/chet/readline/rltop.html"

# GPL-2.0-or-later (< 6.0), GPL-3.0-or-later (>= 6.0)
LICENSE          = "GPL-3.0-or-later"
LIC_FILES_CHKSUM = "file://COPYING;md5=d32239bcb673463ab874e80d47fae504"

TOOLCHAIN = "${MORELLO_TOOLCHAIN}"

DEPENDS += "ncurses-morello"

FILESEXTRAPATHS:prepend := "${THISDIR}/files:${THISDIR}/readline:"

SRC_URI  = "${GNU_MIRROR}/readline/${BPN_READLINE}-${PV_READLINE}.tar.gz;name=archive"
SRC_URI += "file://inputrc"

S = "${WORKDIR}/${BPN_READLINE}-${PV_READLINE}"
B = "${WORKDIR}/build"

BPN_READLINE = "readline"
PV_READLINE  = "8.1.2"


EXTRA_AUTORECONF += "--exclude=autoheader"
EXTRA_OECONF     += "bash_cv_termcap_lib=ncurses"

LEAD_SONAME = "libreadline.so"

do_configure:prepend () {
  if [ ! -e ${S}/acinclude.m4 ]; then
    cat ${S}/aclocal.m4 > ${S}/acinclude.m4
  fi
}

do_install:append() {

  # Make install doesn't properly install these
  oe_libinstall -so -C shlib libhistory ${D}${libdir}
  oe_libinstall -so -C shlib libreadline ${D}${libdir}

  rmdir ${D}${bindir}
  rm -rf ${D}${datadir}/${BPN_READLINE}/*.c
  rmdir ${D}${datadir}/${BPN_READLINE} || true

  install -m 0755 -d ${D}${sysconfdir}
  install -m 0644 ${WORKDIR}/inputrc ${D}${sysconfdir}/inputrc
}

CONFFILES:${PN} += "${sysconfdir}/inputrc"

ALTERNATIVE_PRIORITY             = "100"
ALTERNATIVE:${PN}-doc            = "history.3"
ALTERNATIVE_LINK_NAME[history.3] = "${mandir}/man3/history.3"

# OpenSuse injects versions into libreadline leading to conficits between our native one and theirs
# see their spec file for where this is injected. Extra versioning is harmless so we just do the same.
SRC_URI:append:class-native = " file://rl-native.map"
LDFLAGS:append:class-native = " -Wl,--version-script=${WORKDIR}/rl-native.map"
