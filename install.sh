#!/usr/bin/env bash
# ==============================================================================
# OpenCodeMovil v1.0 - Script de Instalación y Motor Local para Termux
# Desarrollado por: ErasmoX (EX)
# Licencia: MIT
# Donaciones PayPal: erasno1@gmail.com
# ==============================================================================

set -e

# Colores para la salida en consola
CYAN='\033[0;36m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
NC='\033[0m' # No Color
BOLD='\033[1m'

echo -e "${CYAN}${BOLD}"
cat << "EOF"
   ___                   ____          _      __  __            _ _ 
  / _ \ _ __   ___ _ __ / ___|___   __| | ___|  \/  | _____   _(_) |
 | | | | '_ \ / _ \ '_ \| |   / _ \ / _` |/ _ \ |\/| |/ _ \ \ / / | |
 | |_| | |_) |  __/ | | | |__| (_) | (_| |  __/ |  | | (_) \ V /| | |
  \___/| .__/ \___|_| |_|\____\___/ \__,_|\___|_|  |_|\___/ \_/ |_|_|
       |_|                                                 v1.0 (EX)
EOF
echo -e "${NC}"
echo -e "${GREEN}Iniciando instalación del motor backend para OpenCodeMovil...${NC}\n"

# 1. Comprobación de entorno Termux / Linux
if [ -d "/data/data/com.termux" ]; then
    echo -e "${CYAN}[1/4] Entorno Termux detectado.${NC}"
    PREFIX_PATH="/data/data/com.termux/files/usr"
    APP_DIR="$HOME/.opencode"
else
    echo -e "${YELLOW}[1/4] Ejecutando en entorno Linux estándar.${NC}"
    APP_DIR="$HOME/.opencode"
fi

mkdir -p "$APP_DIR/logs"

# 2. Instalación de dependencias (Node.js o Python)
echo -e "${CYAN}[2/4] Verificando dependencias...${NC}"
if command -v node >/dev/null 2>&1; then
    RUNNER="node"
    echo -e "${GREEN}✓ Node.js detectado ($(node -v))${NC}"
elif command -v python3 >/dev/null 2>&1; then
    RUNNER="python"
    echo -e "${GREEN}✓ Python 3 detectado ($(python3 --version))${NC}"
else
    echo -e "${YELLOW}Instalando Node.js...${NC}"
    if command -v pkg >/dev/null 2>&1; then
        pkg update -y && pkg install -y nodejs
        RUNNER="node"
    elif command -v apt-get >/dev/null 2>&1; then
        sudo apt-get update && sudo apt-get install -y nodejs
        RUNNER="node"
    else
        echo -e "${RED}Error: No se encontró gestor de paquetes (pkg/apt). Instala Node.js o Python3 manualmente.${NC}"
        exit 1
    fi
fi

# 3. Creación del servidor local ligero (127.0.0.1:3001)
echo -e "${CYAN}[3/4] Configurando servicio local en puerto 3001...${NC}"

SERVER_FILE="$APP_DIR/server.js"
cat << 'EOF' > "$SERVER_FILE"
// OpenCodeMovil Local Engine Microservice v1.0
// Servidor ultraligero sin dependencias externas (usa módulo http nativo de Node.js)
const http = require('http');
const fs = require('fs');
const path = require('path');

const PORT = 3001;
const HOST = '127.0.0.1';
const LOG_FILE = path.join(__dirname, 'logs', 'engine.log');
const startTime = Date.now();

// Buffer de logs en memoria
const logBuffer = [
  `[${new Date().toLocaleTimeString()}] [INFO] Motor OpenCodeMovil v1.0 inicializado.`,
  `[${new Date().toLocaleTimeString()}] [INFO] Escuchando en http://${HOST}:${PORT}`,
  `[${new Date().toLocaleTimeString()}] [CONNECTED] Loopback listo para vincular con la App Android.`
];

function appendLog(level, message) {
  const line = `[${new Date().toLocaleTimeString()}] [${level}] ${message}`;
  logBuffer.push(line);
  if (logBuffer.length > 200) logBuffer.shift();
  try {
    fs.appendFileSync(LOG_FILE, line + '\n');
  } catch (e) {}
}

const server = http.createServer((req, res) => {
  // Configuración CORS para la app Android
  res.setHeader('Access-Control-Allow-Origin', '*');
  res.setHeader('Access-Control-Allow-Methods', 'GET, POST, OPTIONS');
  res.setHeader('Access-Control-Allow-Headers', 'Content-Type, Authorization');

  if (req.method === 'OPTIONS') {
    res.writeHead(204);
    res.end();
    return;
  }

  // Ruta Health Check
  if (req.url === '/api/health' || req.url === '/health') {
    res.writeHead(200, { 'Content-Type': 'application/json' });
    res.end(JSON.stringify({
      status: 'ok',
      engine: 'OpenCodeMovil Termux Engine',
      version: '1.0.0',
      uptime_seconds: Math.floor((Date.now() - startTime) / 1000)
    }));
    return;
  }

  // Ruta Logs
  if (req.url === '/api/logs') {
    res.writeHead(200, { 'Content-Type': 'application/json' });
    res.end(JSON.stringify({
      logs: logBuffer.slice(-60)
    }));
    return;
  }

  // Ruta Info / Status
  if (req.url === '/') {
    res.writeHead(200, { 'Content-Type': 'text/plain; charset=utf-8' });
    res.end('OpenCodeMovil Local Engine v1.0 activo y escuchando en el puerto 3001.\n');
    return;
  }

  res.writeHead(404, { 'Content-Type': 'application/json' });
  res.end(JSON.stringify({ error: 'Endpoint no encontrado' }));
});

server.listen(PORT, HOST, () => {
  console.log(`OpenCodeMovil Engine corriendo en http://${HOST}:${PORT}`);
});
EOF

# 4. Lanzar motor en segundo plano si no está corriendo
echo -e "${CYAN}[4/4] Verificando estado del servicio...${NC}"

# Matar instancia previa si existe
pkill -f "$SERVER_FILE" 2>/dev/null || true

# Ejecutar en segundo plano con nohup
nohup node "$SERVER_FILE" > "$APP_DIR/logs/console.log" 2>&1 &

sleep 1

# Comprobar si responde a /api/health
if command -v curl >/dev/null 2>&1; then
    HEALTH=$(curl -s http://127.0.0.1:3001/api/health || echo "fail")
    if [[ "$HEALTH" == *"\"status\":\"ok\""* ]]; then
        echo -e "\n${GREEN}${BOLD}======================================================${NC}"
        echo -e "${GREEN}${BOLD} ✓ ¡MOTOR LOCAL ACTIVADO EXITOSAMENTE! (Puerto 3001)   ${NC}"
        echo -e "${GREEN}${BOLD}======================================================${NC}"
        echo -e "${CYAN}Ya puedes abrir la app OpenCodeMovil en tu Android.${NC}"
        echo -e "${CYAN}La app detectará el motor automáticamente en 2 segundos.${NC}\n"
        echo -e "Desarrollador: ${BOLD}ErasmoX (EX)${NC}"
        echo -e "Donaciones: ${BOLD}erasno1@gmail.com${NC}\n"
        exit 0
    fi
fi

echo -e "\n${YELLOW}Servicio iniciado en segundo plano.${NC}"
echo -e "Abre OpenCodeMovil en tu dispositivo para verificar la conexión.\n"
