inherit meson gettext gtk-doc pkgconfig upstream-version-is-even bash-completion manpages gobject-introspection-data purecap-sysroot

MORELLO_SRC = "poky/meta/recipes-core/glib-2.0/*"

SUMMARY = "A general-purpose utility library"
DESCRIPTION = "GLib is a general-purpose utility library, which provides many useful data types, macros, type conversions, string utilities, file utilities, a main loop abstraction, and so on."
HOMEPAGE = "https://developer.gnome.org/glib/"

# pcre is under BSD;
# docs/reference/COPYING is with a 'public domain'-like license!
LICENSE = "LGPL-2.1-or-later & BSD-3-Clause & PD"
LIC_FILES_CHKSUM = "file://COPYING;md5=41890f71f740302b785c27661123bff5 \
                    file://glib/glib.h;beginline=4;endline=17;md5=72f7cc2847407f65d8981ef112e4e630 \
                    file://LICENSES/LGPL-2.1-or-later.txt;md5=41890f71f740302b785c27661123bff5 \
                    file://gmodule/gmodule.h;beginline=4;endline=17;md5=72f7cc2847407f65d8981ef112e4e630 \
                    file://docs/reference/COPYING;md5=f51a5100c17af6bae00735cd791e1fcc"
BUGTRACKER = "http://bugzilla.gnome.org"
SECTION = "libs"

CVE_PRODUCT = "glib"

DEPENDS = "glib-2.0-native \
           virtual/libintl \
           virtual/libiconv \
           libffi-morello \
           libpcre2-morello \
           zlib-morello \
           python3-packaging-native \
           util-linux-morello \
           "

CODEGEN_PYTHON_RDEPENDS = "python3 python3-packaging python3-xml"
CODEGEN_PYTHON_RDEPENDS:mingw32 = ""

RDEPENDS:${PN}-codegen += "${CODEGEN_PYTHON_RDEPENDS}"

RDEPENDS:${PN}:append = "\
        util-linux-morello \
        zlib-morello \
        "

PACKAGES += "${PN}-codegen ${PN}-utils"

LEAD_SONAME = "libglib-2.0.*"

DEPENDS:append:class-target = "${@' gtk-doc' if d.getVar('GTKDOC_ENABLED') == 'True' else ''}"

GTKDOC_MESON_OPTION = "gtk_doc"

S = "${WORKDIR}/git"

# Allow the configure task to pull in git submodules over the network
do_configure[network] = "1"

PACKAGECONFIG ??= "libmount \
                   ${@bb.utils.contains('PTEST_ENABLED', '1', 'tests', '', d)}"

PACKAGECONFIG[libmount] = "-Dlibmount=enabled,-Dlibmount=disabled,util-linux-morello"
PACKAGECONFIG[manpages] = "-Dman=true, -Dman=false, libxslt-native xmlto-native"
PACKAGECONFIG[libelf] = "-Dlibelf=enabled,-Dlibelf=disabled,elfutils"
PACKAGECONFIG[tests] = "-Dinstalled_tests=true,-Dinstalled_tests=false,"
PACKAGECONFIG[selinux] = "-Dselinux=enabled,-Dselinux=disabled,libselinux"

EXTRA_OEMESON += " -Ddtrace=false -Dsystemtap=false -Dtests=true -Dinstalled_tests=true"
EXTRA_OEMESON += " -Ddisable_cxx=true -Dcan_run_host_binaries=false --cross-file=${THISDIR}/glib-2.0/meson.cross.d/common-morello "

do_configure:prepend() {
	sed -i -e '1s,#!.*,#!${USRBINPATH}/env python3,' ${S}/gio/gdbus-2.0/codegen/gdbus-codegen.in
}

FILES:${PN} = "${libdir}/lib*${SOLIBS} \
                ${libdir}/gio \
                ${libexecdir}/*gio-querymodules \
                ${libexecdir}/*gio-launch-desktop \
                ${datadir}/glib-2.0/schemas \
                "

FILES:${PN}-utils += "${bindir}/glib-genmarshal \
                      ${bindir}/glib-gettextize \
                      ${bindir}/glib-mkenums \
                      ${bindir}/glib-compile-resources \
                      "

FILES:${PN}-dev += "${libdir}/glib-2.0/include \
                    ${libdir}/gio/modules/lib*${SOLIBSDEV} \
                    ${libdir}/gio/modules/*.la \
                    ${datadir}/glib-2.0/gettext/po/Makefile.in.in \
                    ${datadir}/glib-2.0/schemas/gschema.dtd \
                    ${datadir}/glib-2.0/valgrind/glib.supp \
                    ${datadir}/gettext/its \
                    ${datadir}/glib-2.0/dtds/* \
                    "

FILES:${PN}-dbg += "${datadir}/glib-2.0/gdb \
                    ${datadir}/gdb \
                    "

FILES:${PN}-codegen = "${datadir}/glib-2.0/codegen/*.py \
                       ${bindir}/gdbus-codegen \
                       "

FILES:${PN}-utils = "${bindir}/*"

SHAREDMIMEDEP = "shared-mime-info"
# When cross compiling for Windows we don't want to include this
SHAREDMIMEDEP:mingw32 = ""

RRECOMMENDS:${PN} += "${SHAREDMIMEDEP}"

ARM_INSTRUCTION_SET:armv4 = "arm"
ARM_INSTRUCTION_SET:armv5 = "arm"
# Valgrind runtime detection works using hand-written assembly, which
# doesn't support mips16e
CPPFLAGS:append:class-target:mips16e = " -DNVALGRIND=1"

# GLib generally requires gettext to be present so for USE_NLS to yes.  For
# native builds as i18n is disabled globally we have to add a gettext-native dependency.
USE_NLS:class-target = "yes"

EXEEXT = ""
EXEEXT:mingw32 = ".exe"

do_install() {
    meson install --destdir "${D}" --no-rebuild
}

do_install:append () {
	if [ -f ${D}${bindir}/gtester-report ]; then
		sed ${D}${bindir}/gtester-report -i -e '1s|^#!.*|#!/usr/bin/env python3|'
	fi

	# Remove some unpackaged files
	rm -rf ${D}${datadir}/glib-2.0/codegen/__pycache__
	rm -f ${D}${datadir}/glib-2.0/codegen/*.pyc
	rm -f ${D}${datadir}/glib-2.0/codegen/*.pyo

	if [ -e ${D}${libdir}/charset.alias ]; then
		rm -f ${D}${libdir}/charset.alias
	fi

        # Make sure gio-querymodules is unique among multilibs
        if test "x${MLPREFIX}" != "x"; then
                mv ${D}${libexecdir}/gio-querymodules${EXEEXT} ${D}${libexecdir}/${MLPREFIX}gio-querymodules${EXEEXT}
        fi
        # Autotools does this, meson does not
        mkdir -p ${D}${libdir}/gio/modules
}

do_install:append:class-target () {
	# Tests are only installed on targets, not native builds.  Separating this out
	# keeps glib-2.0-native from depending on DISTRO_FEATURES
	if [ -f ${D}${datadir}/installed-tests/glib/gdbus-serialization.test ]; then
		if ${@bb.utils.contains("DISTRO_FEATURES", "x11", "false", "true", d)}; then
			rm ${D}${datadir}/installed-tests/glib/gdbus-serialization.test
		fi
	fi
	if [ -f ${D}${datadir}/installed-tests/glib/static-link.test ]; then
        if test "x${MLPREFIX}" != "x"; then
                mv ${D}${datadir}/installed-tests/glib/static-link.test ${D}${datadir}/installed-tests/glib/${MLPREFIX}static-link.test
        fi
	fi
        # https://gitlab.gnome.org/GNOME/glib/-/issues/2810
        rm -f ${D}${datadir}/installed-tests/glib/thread-pool-slow.test
}

# As we do not build python3 for windows, makes no sense to ship the script that's using it
do_install:append:mingw32() {
        rm -f ${D}${bindir}/gtester-report
}

