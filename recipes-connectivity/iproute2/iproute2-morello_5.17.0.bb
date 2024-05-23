require iproute2-morello.inc

PN_DL = "iproute2"

SRC_URI = "${KERNELORG_MIRROR}/linux/utils/net/${PN_DL}/${PN_DL}-${PV}.tar.xz \
           file://0001-libc-compat.h-add-musl-workaround.patch \
           file://0002-em_meta.c-types.patch \
           file://0003-json_print.h-add_time_header.patch \
           "

SRC_URI[sha256sum] = "6e384f1b42c75e1a9daac57866da37dcff909090ba86eb25a6e764da7893660e"

# CFLAGS are computed in Makefile and reference CCOPTS
#
EXTRA_OEMAKE:append = " CCOPTS='${CFLAGS}'"
