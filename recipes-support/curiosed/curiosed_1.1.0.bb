DESCRIPTION = "Flight Software for CuRIOS-ED version 2.4"
HOMEPAGE = ""
LICENSE = "CLOSED"

DEPENDS = "zlib bzip2 curl openssl libusb cfitsio monit"

# This variable must be set to bash for the inspiresat .sh files
RDEPENDS_${PN} += "bash"

# Overrides
SOLIBS = ".so"
FILES_SOLIBSDEV = ""

inherit autotools-brokensep pkgconfig systemd

inherit features_check
inherit systemd

#PR = "r1"
LIC_FILES_CHKSUM = ""

#SRC_URI = "file:///home/curios/curios_fsw/* file:///home/curios/inspiresat_config/*"
#S = "${WORKDIR}/home/curios/curios_fsw"

SRC_URI = "\
    git://github.com/MovingUniverseLab/curios_fsw.git;branch=Steve_CuRIOS;protocol=https;destsuffix=curios_fsw \
    git://github.com/StarSpec-Technologies/inspiresat_config.git;branch=master;protocol=https;destsuffix=inspiresat_config \
"

SRCREV_default = "${AUTOREV}"

S = "${WORKDIR}/curios_fsw"

SYSTEMD_SERVICE:${PN} = "payload-control.service health-update-sh.service"
SYSTEMD_AUTO_ENABLE:${PN} = "enable"

inherit cmake

do_install:append () {
    # Make directories
    install -d ${D}${bindir}
    install -d ${D}${libdir}
    install -d ${D}/data
    install -d ${D}/home/root
    install -d ${D}${sysconfdir}/systemd
    install -d ${D}${sysconfdir}/systemd/network
    install -d ${D}${sysconfdir}/inspiresat
    install -d ${D}${sysconfdir}/flightsim
    install -d ${D}${sysconfdir}/cron.daily

    install -m 0755 ${WORKDIR}/curios_fsw/lib/libatikcameras.so ${D}${libdir}
    install -m 0755 ${WORKDIR}/curios_fsw/lib/libflightapi.a ${D}${libdir}

    # Copy the health script to /usr/bin
    install -m 0755 ${WORKDIR}/curios_fsw/src/system_scripts/Health_Update.sh ${D}${bindir}

    # Move over rootfs files
    install -m 0755 ${WORKDIR}/curios_fsw/files/q7s/home/root/.profile ${D}/home/root/
    install -m 0644 ${WORKDIR}/curios_fsw/files/q7s/etc/systemd/network/05-eth0.network ${D}${sysconfdir}/systemd/network/
    
    # Copy the journal clean script to the cron.daily directory
    install -m 0755 ${WORKDIR}/curios_fsw/files/q7s/etc/cron.daily/journal_clean.sh ${D}${sysconfdir}/cron.daily/

    # Install StarSpec config files
    cp -r ${WORKDIR}/inspiresat_config/* ${D}${sysconfdir}/inspiresat/
    cp -r ${WORKDIR}/curios_fsw/StarSpec/flightsim/* ${D}${sysconfdir}/flightsim/

    # Install Payload_Control and Health_Update service
    # Move over systemd files
    install -d ${D}${sysconfdir}/systemd/system
    install -m 0644 ${WORKDIR}/curios_fsw/files/q7s/etc/systemd/system/payload-control.service ${D}${sysconfdir}/systemd/system/
    install -m 0644 ${WORKDIR}/curios_fsw/files/q7s/etc/systemd/system/health-update-sh.service ${D}${sysconfdir}/systemd/system/    
}

FILES:${PN} += " \
  /data \
  ${bindir}/* \
  ${libdir}/* \
  /home \
  /home/root \
  /home/root/.profile \
  /home/root/* \
  ${sysconfdir}/inspiresat/* \
  ${sysconfdir} \
  ${sysconfdir}/systemd \
  ${sysconfdir}/systemd/network \
  ${sysconfdir}/systemd/network/* \
  ${sysconfdir}/systemd/system/* \
"

REQUIRED_DISTRO_FEATURES= "systemd"

#
#  /etc/dropbear/* \
#
