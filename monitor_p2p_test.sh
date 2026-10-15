#!/usr/bin/env bash
# ==============================================================================
# Velvet VIP - Monitor P2P WebRTC & Señalización en Vivo (Dual-Device)
# Samsung Galaxy A06 (Modelo) <-> Motorola Moto G20 (Gamer)
# ==============================================================================

GREEN='\033[0;32m'
CYAN='\033[0;36m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
NC='\033[0m' # No Color

echo -e "${CYAN}================================================================${NC}"
echo -e "${CYAN}      VELVET VIP v1.5 - MONITOR P2P WEBRTC EN TIEMPO REAL       ${NC}"
echo -e "${CYAN}================================================================${NC}"

# Detectar dispositivos conectados
DEVICES=($(adb devices | grep -w "device" | awk '{print $1}'))

echo -e "${YELLOW}Dispositivos ADB detectados:${NC} ${#DEVICES[@]}"
for dev in "${DEVICES[@]}"; do
    MODEL=$(adb -s "$dev" shell getprop ro.product.model 2>/dev/null | tr -d '\r')
    echo -e "  • ${GREEN}$dev${NC} ($MODEL)"
done

if [ ${#DEVICES[@]} -eq 0 ]; then
    echo -e "${RED}[!] No hay dispositivos conectados por USB o Wi-Fi ADB.${NC}"
    exit 1
fi

LOG_FILTER="WebRtcSession:V org.webrtc:V AndroidRuntime:E *:S"

case "$1" in
    samsung|modelo)
        TARGET_DEV="R8YXB10QEQF"
        # Si no está por USB, probar IP
        if ! adb devices | grep -q "$TARGET_DEV"; then
            TARGET_DEV="192.168.100.3:5555"
        fi
        echo -e "${GREEN}[*] Monitoreando exclusivamente Samsung A06 (Modelo: $TARGET_DEV)...${NC}"
        adb -s "$TARGET_DEV" logcat -v time -s WebRtcSession:V org.webrtc:D AndroidRuntime:E
        ;;
    moto|gamer)
        # Buscar el dispositivo que NO sea el Samsung
        MOTO_DEV=""
        for dev in "${DEVICES[@]}"; do
            if [[ "$dev" != "R8YXB10QEQF" && "$dev" != "192.168.100.3:5555" ]]; then
                MOTO_DEV="$dev"
                break
            fi
        done
        if [ -z "$MOTO_DEV" ]; then
            echo -e "${RED}[!] Motorola aún no detectado en ADB. Conéctalo por USB o enlázalo por Wi-Fi.${NC}"
            exit 1
        fi
        echo -e "${GREEN}[*] Monitoreando exclusivamente Motorola Moto G20 (Gamer: $MOTO_DEV)...${NC}"
        adb -s "$MOTO_DEV" logcat -v time -s WebRtcSession:V org.webrtc:D AndroidRuntime:E
        ;;
    dual|both|"")
        echo -e "${GREEN}[*] Modo Dual activado. Mostrando logs combinados de WebRTC y Runtime...${NC}"
        echo -e "${YELLOW}[Presiona Ctrl+C para detener el monitoreo]${NC}\n"
        
        # Iniciar monitoreo del Samsung
        SAMSUNG_DEV="R8YXB10QEQF"
        if ! adb devices | grep -q "$SAMSUNG_DEV"; then
            SAMSUNG_DEV="192.168.100.3:5555"
        fi
        
        # Buscar Motorola
        MOTO_DEV=""
        for dev in "${DEVICES[@]}"; do
            if [[ "$dev" != "R8YXB10QEQF" && "$dev" != "192.168.100.3:5555" ]]; then
                MOTO_DEV="$dev"
                break
            fi
        done

        if [ -n "$MOTO_DEV" ]; then
            echo -e "${CYAN}Capturando simultáneamente:${NC}"
            echo -e "  [MODELO - A06]  -> $SAMSUNG_DEV"
            echo -e "  [GAMER  - MOTO] -> $MOTO_DEV"
            
            adb -s "$SAMSUNG_DEV" logcat -v time -s WebRtcSession:V org.webrtc:I AndroidRuntime:E | sed "s/^/[MODELO-A06] /" &
            PID1=$!
            adb -s "$MOTO_DEV" logcat -v time -s WebRtcSession:V org.webrtc:I AndroidRuntime:E | sed "s/^/[GAMER-MOTO]  /" &
            PID2=$!
            
            trap "kill $PID1 $PID2 2>/dev/null; exit" SIGINT SIGTERM
            wait
        else
            echo -e "${YELLOW}[i] Motorola aún no conectado. Monitoreando Samsung A06 en espera del segundo dispositivo...${NC}"
            adb -s "$SAMSUNG_DEV" logcat -v time -s WebRtcSession:V org.webrtc:I AndroidRuntime:E | sed "s/^/[MODELO-A06] /"
        fi
        ;;
esac
