#!/bin/bash

# (run as root)
# ==========================================

# Deployment Folders
# ==========================================
chmod -R g+w /app
chown -R root:g2_linux_users /app
mkdir /app/releases
ln -s /app /opt/liaison

# tomcat init.d
# ==========================================
# export URL_INITD_TOMCAT=http://gitlab-g2.liaison.tech/g2/buildbundle/raw/master/scripts/deploybundle/configureSystem/tomcat
export URL_INITD_TOMCAT=http://lsvljnkin01d.liaison.dev/job/mailbox-master/ws/tools/buildbundle/scripts/deploybundle/configureSystem/tomcat
curl -o /etc/init.d/tomcat $URL_INITD_TOMCAT
chown root:g2_linux_users /etc/init.d/tomcat
chmod ug+x /etc/init.d/tomcat
sed -i.bak 's/\/bin\/bash tomcat/'"\/bin\/bash svc_g2_jenkins"'/g' /etc/init.d/tomcat
sed -i.bak 's/svc_g2_jenkins.*$/'""'/g' /etc/sudoers
sed -i.bak 's/root\s*ALL=(ALL)\s*ALL/'"root\tALL=(ALL)\tALL\nsvc_g2_jenkins\tALL = NOPASSWD: \/sbin\/service"'/g' /etc/sudoers

# Disable iptables (firewall)
# ==========================================
service iptables save
service iptables stop
chkconfig iptables off
 