#!/bin/sh
set -e

: "${PORT:=10000}"

sed -i "s/port=\"8080\"/port=\"${PORT}\"/g" "$CATALINA_HOME/conf/server.xml"

if ! grep -q "port=\"${PORT}\"" "$CATALINA_HOME/conf/server.xml"; then
    echo "No se pudo configurar Tomcat para escuchar en el puerto ${PORT}" >&2
    exit 1
fi

exec "$CATALINA_HOME/bin/catalina.sh" run
