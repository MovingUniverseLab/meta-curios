# This is version 3.0 of 2026-07-11
DESCRIPTION = "Flight Software for CuRIOS-ED version 4.1"
HOMEPAGE = ""
LICENSE = "CLOSED"

DEPENDS = "zlib bzip2 curl openssl libusb cfitsio monit"

# This variable must be set to bash for the inspiresat .sh files.
# systemd provides journalctl for the Payload_Control journal export timer.
RDEPENDS:${PN} += "bash systemd"

# Overrides
SOLIBS = ".so"
FILES_SOLIBSDEV = ""

inherit features_check
inherit cmake pkgconfig systemd

LIC_FILES_CHKSUM = ""

SRC_URI = "\
    git://git@github.com/MovingUniverseLab/curios_fsw.git;branch=Steve_dev;protocol=ssh;destsuffix=curios_fsw;name=repoCurios \
    git://git@github.com/StarSpec-Technologies/inspiresat_config.git;branch=master;protocol=ssh;destsuffix=inspiresat_config;name=repoInspiresat \
"

SRCREV_repoCurios = "${AUTOREV}"
SRCREV_repoInspiresat = "${AUTOREV}"

SRCREV_FORMAT = "repoCurios_repoInspiresat"
PV = "1.1.0+git${SRCPV}"

S = "${WORKDIR}/curios_fsw"

SYSTEMD_SERVICE:${PN} = "xiphos-startup.service payload-control.service health-update-sh.service payload-control-journal-export.timer"
SYSTEMD_AUTO_ENABLE:${PN} = "enable"

do_install:append() {
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
    install -d ${D}${sysconfdir}/systemd/journald.conf.d

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

    # Export the last 24 hours of Payload_Control journal entries once per day.
    cat > ${D}${bindir}/export-payload-control-journal <<'EOF'
#!/bin/sh
set -e

UNIT="$1"
if [ -z "$UNIT" ]; then
    UNIT="payload-control.service"
fi

if [ -z "$LOG_DIR" ]; then
    LOG_DIR="/data/Logs"
fi

STAMP="$(date +%F)"
DEST="$LOG_DIR/Payload_Control_$STAMP.log"
TMP="$DEST.tmp"

mkdir -p "$LOG_DIR"

journalctl \
    --unit="$UNIT" \
    --since="24 hours ago" \
    --no-pager \
    --output=short-iso \
    > "$TMP"

chmod 0644 "$TMP"
mv "$TMP" "$DEST"
EOF
    chmod 0755 ${D}${bindir}/export-payload-control-journal

    cat > ${D}${sysconfdir}/systemd/system/payload-control-journal-export.service <<'EOF'
[Unit]
Description=Export last 24 hours of Payload_Control journal to /data/Logs
Documentation=man:journalctl(1)

[Service]
Type=oneshot
ExecStart=/usr/bin/export-payload-control-journal payload-control.service
EOF

    cat > ${D}${sysconfdir}/systemd/system/payload-control-journal-export.timer <<'EOF'
[Unit]
Description=Daily Payload_Control journal export

[Timer]
OnCalendar=daily
Persistent=true
RandomizedDelaySec=5m
Unit=payload-control-journal-export.service

[Install]
WantedBy=timers.target
EOF

    cat > ${D}${sysconfdir}/systemd/journald.conf.d/90-payload-control-retention.conf <<'EOF'
[Journal]
MaxRetentionSec=7day
EOF
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
    ${sysconfdir}/systemd/journald.conf.d \
    ${sysconfdir}/systemd/journald.conf.d/* \
"

REQUIRED_DISTRO_FEATURES = "systemd"

#
#  /etc/dropbear/* \
#
