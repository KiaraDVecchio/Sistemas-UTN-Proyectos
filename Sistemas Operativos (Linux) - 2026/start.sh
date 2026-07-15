#!/bin/bash
# =============================================================================
# Script de arranque - TP Plug & Pray
# Uso: ./start.sh
# =============================================================================

BASE_DIR="$(cd "$(dirname "$0")" && pwd)"

# -----------------------------------------------------------------------------
# CONFIGURACION - Modificar para cada prueba
# -----------------------------------------------------------------------------

PROCESO_INICIAL="/home/utnso/tp/scripts/lanzador.txt"

# CPUs a levantar: agregar/quitar IDs segun necesidad
CPU_IDS=(0 1)

# Memory Sticks a levantar: cada valor es el tamanio en bytes de un stick
MEMORY_STICKS=(4096 4096)

# IOs a levantar: cada valor es el tipo (STDIN, STDOUT o SLEEP)
IO_TYPES=(SLEEP STDOUT STDIN)

# Tiempo de espera entre grupos de modulos (segundos)
WAIT=2

# -----------------------------------------------------------------------------
# Funciones
# -----------------------------------------------------------------------------

abrir_terminal() {
    local titulo="$1"
    local directorio="$2"
    local comando="$3"
    xterm -title "$titulo" -e bash -c "cd '$directorio' && $comando; echo '--- Proceso terminado. Presiona Enter para cerrar ---'; read" &
}

verificar_binario() {
    local modulo="$1"
    local binario="$BASE_DIR/$modulo/bin/$modulo"
    if [ ! -f "$binario" ]; then
        echo "[ERROR] No se encontro el binario: $binario"
        echo "        Ejecuta 'make' dentro de $modulo/ primero."
        exit 1
    fi
}

# -----------------------------------------------------------------------------
# Verificar que todos los binarios existen antes de arrancar
# -----------------------------------------------------------------------------

echo "Verificando binarios..."
verificar_binario "kernel_memory"
verificar_binario "kernel_scheduler"
verificar_binario "cpu"
verificar_binario "memory_stick"
verificar_binario "swap"
verificar_binario "io"
echo "OK - Todos los binarios encontrados."
echo ""

# -----------------------------------------------------------------------------
# Arranque en orden
# -----------------------------------------------------------------------------

# 1. Kernel Memory
echo "[1/5] Levantando Kernel Memory..."
abrir_terminal "Kernel Memory" "$BASE_DIR/kernel_memory" \
    "./bin/kernel_memory kernel_memory.config"
sleep $WAIT

# 2. SWAP y Memory Sticks
echo "[2/5] Levantando SWAP y Memory Sticks..."
abrir_terminal "SWAP" "$BASE_DIR/swap" \
    "./bin/swap swap.config"

for tamano in "${MEMORY_STICKS[@]}"; do
    abrir_terminal "Memory Stick (${tamano}b)" "$BASE_DIR/memory_stick" \
        "./bin/memory_stick memory_stick.config $tamano"
done
sleep $WAIT

# 3. Kernel Scheduler
echo "[3/5] Levantando Kernel Scheduler..."
abrir_terminal "Kernel Scheduler" "$BASE_DIR/kernel_scheduler" \
    "./bin/kernel_scheduler kernel_scheduler.config $PROCESO_INICIAL"
sleep $WAIT

# 4. CPUs
echo "[4/5] Levantando CPUs..."
for id in "${CPU_IDS[@]}"; do
    abrir_terminal "CPU $id" "$BASE_DIR/cpu" \
        "./bin/cpu cpu.config $id"
done
sleep $WAIT

# 5. IOs
echo "[5/5] Levantando modulos IO..."
for tipo in "${IO_TYPES[@]}"; do
    abrir_terminal "IO ($tipo)" "$BASE_DIR/io" \
        "./bin/io io.config $tipo"
done

echo ""
echo "Todos los modulos levantados."