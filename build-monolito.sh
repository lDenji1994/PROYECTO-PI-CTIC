#!/bin/bash
# Empaqueta el monolito: el frontend (panel estatico HTML/CSS/JS)
# ya vive en backend/src/main/resources/static, asi que no hace
# falta ningun paso de build de frontend. Solo se compila el JAR
# del backend.

set -e

echo "== Empaquetando backend (Gradle) =="
cd backend
./gradlew clean build -x test

echo "== Listo =="
echo "JAR generado en backend/build/libs/"
echo "Ejecuta con: java -jar backend/build/libs/certificados-app.jar"
echo "En el servidor: java -jar certificados-app.jar --spring.profiles.active=prod (ver deploy/README-despliegue.md)"
echo "La app completa (frontend + API) queda en http://localhost:8080"
