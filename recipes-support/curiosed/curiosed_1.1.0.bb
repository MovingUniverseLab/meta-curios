DESCRIPTION = "Flight Software for CuRIOS-ED version 2.3"
HOMEPAGE = ""
LICENSE = "CLOSED"

DEPENDS = "zlib bzip2 curl openssl libusb cfitsio monit"

# This variable must be set to bash for the inspiresat .sh files
RDEPENDS_${PN} += "bash"

# Overrides
SOLIBS = ".so"
FILES_SOLIBSDEV = ""

inherit autotools-brokensep pkgconfig systemd

#PR = "r1"
LIC_FILES_CHKSUM = ""
#LIC_FILES_CHKSUM = "file://licenses/License.txt;md5=77856e8a5492e2119200b3401a8e7966"

#SRC_URI = "file:///home/curios/curios_fsw/* file:///home/curios/inspiresat_config/*"
SRC_URI = " \
    git://github.com/curios-lab/curios_fsw.git;branch=Steve_CuRIOS;protocol=ssh \
    git://github.com/curios-lab/inspiresat_config.git;branch=master;protocol=ssh \
"
S = "${WORKDIR}/home/curios/curios_fsw"

SYSTEM_AUTO_ENABLE = "enable"
SYSTEM_SERVICE:${PN} = "curiosed_control.service" "health-update.service"

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
#    install -d ${D}${sysconfdir}/dropbear

    install -m 0755 ${WORKDIR}/home/curios/curios_fsw/lib/libatikcameras.so ${D}${libdir}
    install -m 0755 ${WORKDIR}/home/curios/curios_fsw/lib/libflightapi.a ${D}${libdir}

    # Copy the health script to /usr/bin
    install -m 0755 ${WORKDIR}/home/curios/curios_fsw/src/system_scripts/Health_Update.sh ${D}${bindir}

    # Move over rootfs files
    install -m 0755 ${WORKDIR}/home/curios/curios_fsw/files/q7s/home/root/.profile ${D}/home/root/
#   install -m 0600 ${WORKDIR}/home/curios/curios_fsw/files/q7s/etc/dropbear/dropbear_rsa_host_key ${D}${sysconfdir}/dropbear/
    install -m 0644 ${WORKDIR}/home/curios/curios_fsw/files/q7s/etc/systemd/network/05-eth0.network ${D}${sysconfdir}/systemd/network/
    
    # Install StarSpec config files
    cp -r ${WORKDIR}/home/curios/inspiresat_config/* ${D}${sysconfdir}/inspiresat/

    # Install Payload_Control and Health_Update service
    # Move over systemd files
    install -d ${D}${sysconfdir}/systemd/system
    install -m 0644 ${WORKDIR}/home/curios/curios_fsw/files/q7s/etc/systemd/system/curiosed_control.service ${D}${sysconfdir}/systemd/system/
    install -m 0644 ${WORKDIR}/home/curios/curios_fsw/files/q7s/etc/systemd/system/health-update.service ${D}${sysconfdir}/systemd/system/
    install -m 0644 ${WORKDIR}/home/curios/curios_fsw/files/q7s/etc/systemd/system/health-update-sh.service ${D}${sysconfdir}/systemd/system/    
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
