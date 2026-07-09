# This is a version of 2026-05-19
DESCRIPTION = "Flight Software for CuRIOS-ED version 4.1"
HOMEPAGE = ""
LICENSE = "CLOSED"

DEPENDS = "zlib bzip2 curl openssl libusb cfitsio monit"

# This variable must be set to bash for the inspiresat .sh files
RDEPENDS_${PN} += "bash"

# Overrides
SOLIBS = ".so"
FILES_SOLIBSDEV = ""

inherit features_check
inherit cmake pkgconfig systemd

LIC_FILES_CHKSUM = ""

SRC_URI = "\
    git://git@github.com/MovingUniverseLab/curios_fsw.git;branch=Steve_dev;protocol=ssh;destsuffix=curios_fsw;name=repoCurios\
    git://git@github.com/StarSpec-Technologies/inspiresat_config.git;branch=master;protocol=ssh;destsuffix=inspiresat_config;name=repoInspiresat \
"

SRCREV_repoCurios = "${AUTOREV}"
SRCREV_repoInspiresat = "${AUTOREV}"

SRCREV_FORMAT = "repoCurios_repoInspiresat"
PV = "1.1.0+git${SRCPV}" 

S = "${WORKDIR}/curios_fsw"

SYSTEMD_SERVICE:${PN} = "xiphos-startup.service payload-control.service health-update-sh.service"
SYSTEMD_AUTO_ENABLE:${PN} = "enable"

do_install:append () {
    # Make directories
    install -d ${D}${bindir}
    install -d ${D}${libdir}
    install -d ${D}/data
    install -d ${D}/data/Images
    install -d ${D}/data/Icons
    install -d ${D}/data/Logs
    install -d ${D}/data/Logs/Atik
    install -d ${D}/data/parameters
    install -d ${D}/data/sources
    install -d ${D}/data/working
    install -d ${D}/home/root
    install -d ${D}/home/root/.config
    install -d ${D}/home/root/.config/Atik
    install -d ${D}${sysconfdir}/systemd
    install -d ${D}${sysconfdir}/systemd/network
    install -d ${D}${sysconfdir}/inspiresat
    install -d ${D}${sysconfdir}/flightsim
    install -d ${D}${sysconfdir}/cron.daily
    install -d ${D}${sysconfdir}/profile.d

    install -m 0755 ${WORKDIR}/curios_fsw/lib/libatikcameras.so ${D}${libdir}
    install -m 0755 ${WORKDIR}/curios_fsw/lib/libflightapi.a ${D}${libdir}

    # Add file with version information to record the build information
    echo "Image Build Time: $(date)" > ${D}/home/root/version_info.txt

    # Add symbolic link for Atik debug files
    ln -s /data/Logs/Atik ${D}/home/root/.config/Atik/AtikCamerasDLL

    # Copy the health and startup scripts to /usr/bin. Note we moved the two critical files to the service directory
    install -m 0755 ${WORKDIR}/curios_fsw/files/q7s/etc/systemd/system/Health_Update.sh ${D}${bindir}
    install -m 0755 ${WORKDIR}/curios_fsw/files/q7s/etc/systemd/system/xiphos_startup.sh ${D}${bindir}

    # Move over rootfs files
    install -m 0755 ${WORKDIR}/curios_fsw/files/q7s/home/root/.profile ${D}/home/root/
    install -m 0644 ${WORKDIR}/curios_fsw/files/q7s/etc/systemd/network/05-eth0.network ${D}${sysconfdir}/systemd/network/
    install -m 0755 ${WORKDIR}/curios_fsw/files/q7s/etc/profile.d/aliases.sh ${D}${sysconfdir}/profile.d/
    
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
    install -m 0644 ${WORKDIR}/curios_fsw/files/q7s/etc/systemd/system/xiphos-startup.service ${D}${sysconfdir}/systemd/system/
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
