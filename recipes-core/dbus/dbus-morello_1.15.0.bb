inherit autotools pkgconfig gettext upstream-version-is-even purecap-sysroot purecap-useradd update-rc.d systemd logging

SUMMARY = "D-Bus message bus"
DESCRIPTION = "D-Bus is a message bus system, a simple way for applications to talk to one another. In addition to interprocess communication, D-Bus helps coordinate process lifecycle; it makes it simple and reliable to code a \"single instance\" application or daemon, and to launch applications and daemons on demand when their services are needed."
HOMEPAGE = "https://dbus.freedesktop.org"
SECTION = "base"

MORELLO_SRC = "poky/meta/recipes-core/dbus/*"

LICENSE = "AFL-2.1 | GPL-2.0-or-later"

LIC_FILES_CHKSUM = "file://COPYING;md5=a11183c1ba65ef542fd09be2fcc53e52 \
                    file://dbus/dbus.h;beginline=6;endline=20;md5=866739837ccd835350af94dccd6457d8 \
                    "

FILESEXTRAPATHS:append := "${THISDIR}/dbus:"

S = "${WORKDIR}/dbus-${PV}"

TOOLCHAIN = "${MORELLO_TOOLCHAIN}"

CC:remove = "${CC_PURECAP_FLAGS}"
CFLAGS:append = "${CC_PURECAP_FLAGS}"
LDFLAGS:append = "${LD_PURECAP_FLAGS}"

SRC_URI = "https://dbus.freedesktop.org/releases/dbus/dbus-${PV}.tar.xz \
           file://tmpdir.patch \
           file://dbus-1.init \
          "

SRC_URI[sha256sum] = "5073c8cb9ad20226647bb38f4965182b762a6e1f595ccdc8e59411014bfd640a"

EXTRA_OECONF += " --disable-xml-docs \
                --disable-doxygen-docs \
                --enable-largefile \
                --with-system-socket=/run/dbus/system_bus_socket \
                --disable-tests \
                --enable-checks \
                --enable-asserts \
                --runstatedir=/run \
                --enable-shared \
                --disable-static \
                "

CONFIGUREOPTS += " CFLAGS='${CFLAGS} -XCClinker --target=${GLOBAL_ARCH_TRIPLE}' \
                  "

EXTRA_OECONF:append:class-target = " SYSTEMCTL=${base_bindir}/systemctl"

PACKAGECONFIG ??= "${@bb.utils.filter('DISTRO_FEATURES', 'systemd x11', d)} \
                   user-session \
                  "

PACKAGECONFIG[systemd] = "--enable-systemd --with-systemdsystemunitdir=${systemd_system_unitdir},--disable-systemd --without-systemdsystemunitdir,systemd"
PACKAGECONFIG[x11] = "--enable-x11-autolaunch,--without-x --disable-x11-autolaunch, virtual/libx11 libsm"
PACKAGECONFIG[user-session] = "--enable-user-session --with-systemduserunitdir=${systemd_user_unitdir},--disable-user-session"
PACKAGECONFIG[verbose-mode] = "--enable-verbose-mode,,,"
PACKAGECONFIG[audit] = "--enable-libaudit,--disable-libaudit,audit"
PACKAGECONFIG[selinux] = "--enable-selinux,--disable-selinux,libselinux"

# Disable these options for Purecap
PACKAGECONFIG:remove = "systemd"
PACKAGECONFIG:remove = "x11"

DEPENDS += "expat-morello virtual/libintl autoconf-archive-native systemd"
RDEPENDS:${PN} += " ${PN}-common ${PN}-tools ${PN}-lib expat-morello "

INITSCRIPT_NAME = "dbus-1"
INITSCRIPT_PARAMS = "start 02 5 3 2 . stop 20 0 1 6 ."

python __anonymous() {
    if not bb.utils.contains('DISTRO_FEATURES', 'sysvinit', True, False, d):
        d.setVar("INHIBIT_UPDATERCD_BBCLASS", "1")
}

PACKAGES =+ " ${PN}-lib ${PN}-common ${PN}-tools"

USERADD_PACKAGES = "${PN}-common"
USERADD_PARAM:${PN}-common = "--system --home ${localstatedir}/lib/dbus \
                             --no-create-home --shell /bin/false \
                             --user-group messagebus"

CONFFILES:${PN} = "${sysconfdir}/dbus-1/system.conf ${sysconfdir}/dbus-1/session.conf"

DEBIANNAME:${PN} = "dbus-1-morello"

OLDPKGNAME = "dbus-x11-morello"

# for compatibility
RPROVIDES:${PN} = "${OLDPKGNAME}"
RREPLACES:${PN} += "${OLDPKGNAME}"

FILES:${PN} = "${bindir}/dbus-daemon* \
               ${bindir}/dbus-cleanup-sockets \
               ${bindir}/dbus-launch \
               ${bindir}/dbus-run-session \
               ${libexecdir}/dbus* \
               ${sysconfdir} \
               ${localstatedir} \
               ${systemd_system_unitdir} \
               ${systemd_user_unitdir} \
               ${nonarch_libdir}/tmpfiles.d/dbus.conf \
"
FILES:${PN}-common = "${sysconfdir}/dbus-1 \
                      ${datadir}/dbus-1/services \
                      ${datadir}/dbus-1/system-services \
                      ${datadir}/dbus-1/session.d \
                      ${datadir}/dbus-1/session.conf \
                      ${datadir}/dbus-1/system.d \
                      ${datadir}/dbus-1/system.conf \
                      ${systemd_system_unitdir}/dbus.socket \
                      ${systemd_system_unitdir}/sockets.target.wants \
                      ${systemd_user_unitdir}/dbus.socket \
                      ${systemd_user_unitdir}/sockets.target.wants \
                      ${nonarch_libdir}/sysusers.d/dbus.conf \
"
FILES:${PN}-tools = "${bindir}/dbus-uuidgen \
                     ${bindir}/dbus-send \
                     ${bindir}/dbus-monitor \
                     ${bindir}/dbus-update-activation-environment \
"
FILES:${PN}-lib = "${libdir}/lib*.so.*"
RRECOMMENDS:${PN}-lib = "${PN}"

FILES:${PN}-dev += "${libdir}/dbus-1.0/include ${bindir}/dbus-test-tool ${datadir}/xml/dbus-1"

PACKAGE_WRITE_DEPS += "${@bb.utils.contains('DISTRO_FEATURES','systemd sysvinit','systemd-systemctl-native','',d)}"

pkg_postinst:dbus() {
	# If both systemd and sysvinit are enabled, mask the dbus-1 init script
        if ${@bb.utils.contains('DISTRO_FEATURES','systemd sysvinit','true','false',d)}; then
		if [ -n "$D" ]; then
			OPTS="--root=$D"
		fi
		systemctl $OPTS mask dbus-1.service
	fi

	if [ -z "$D" ] && [ -e /etc/init.d/populate-volatile.sh ] ; then
		/etc/init.d/populate-volatile.sh update
	fi
}

pkg_postinst_ontarget:${PN}() {
    chown messagebus:messagebus ${D}${localstatedir}/lib/dbus
    chown root:messagebus ${D}${libexecdir}/dbus-daemon-launch-helper
}

move_systemd_unit_files() {
    rm -rf ${D}/lib/
}

do_install() {
    autotools_do_install

	if ${@bb.utils.contains('DISTRO_FEATURES', 'sysvinit', 'true', 'false', d)}; then
		install -d ${D}${sysconfdir}/init.d
		sed 's:@bindir@:${bindir}:' < ${WORKDIR}/dbus-1.init >${WORKDIR}/dbus-1.init.sh
		install -m 0755 ${WORKDIR}/dbus-1.init.sh ${D}${sysconfdir}/init.d/dbus-1
		install -d ${D}${sysconfdir}/default/volatiles
		echo "d messagebus messagebus 0755 /run/dbus none" \
		     > ${D}${sysconfdir}/default/volatiles/99_dbus
	fi

	if ${@bb.utils.contains('DISTRO_FEATURES', 'systemd', 'true', 'false', d)}; then
		for i in dbus.target.wants sockets.target.wants multi-user.target.wants; do \
			install -d ${D}${systemd_system_unitdir}/$i; done
		install -m 0644 ${B}/bus/dbus.service ${B}/bus/dbus.socket ${D}${systemd_system_unitdir}/
		ln -fs ../dbus.socket ${D}${systemd_system_unitdir}/dbus.target.wants/dbus.socket
		ln -fs ../dbus.socket ${D}${systemd_system_unitdir}/sockets.target.wants/dbus.socket
		ln -fs ../dbus.service ${D}${systemd_system_unitdir}/multi-user.target.wants/dbus.service
	fi

	install -d ${D}${localstatedir}/lib/dbus

	chmod 4755 ${D}${libexecdir}/dbus-daemon-launch-helper

	# Remove Red Hat initscript
	rm -rf ${D}${sysconfdir}/rc.d

	# Remove empty testexec directory as we don't build tests
	rm -rf ${D}${libdir}/dbus-1.0/test

	# Remove /var/run as it is created on startup
	rm -rf ${D}${localstatedir}/run

    # move files that conflict with non-purecap dbus
    move_systemd_unit_files
}

CVE_PRODUCT += "d-bus_project:d-bus"
