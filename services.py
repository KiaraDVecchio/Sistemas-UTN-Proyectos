import json
from datetime import date
from pathlib import Path

ARCHIVO_TAREAS = Path(__file__).with_name("tareas.json")
ESTADOS_VALIDOS = ("pendiente", "en_curso", "hecho")
LONGITUD_MAX_ETIQUETA = 24

# Almacenamiento temporal en memoria
tareas = []
etiquetas = []
siguiente_id = 1


def renumerar_ids():
    """Reasigna ids consecutivos comenzando en 1."""
    global siguiente_id
    for indice, tarea in enumerate(tareas, start=1):
        tarea["id"] = indice
    siguiente_id = len(tareas) + 1


def guardar_tareas():
    """Guarda tareas y etiquetas en un archivo JSON."""
    with ARCHIVO_TAREAS.open("w", encoding="utf-8") as archivo:
        json.dump(
            {"tareas": tareas, "etiquetas": etiquetas},
            archivo,
            ensure_ascii=False,
            indent=2,
        )


def limpiar_etiqueta(texto):
    """Normaliza una etiqueta escrita por el usuario."""
    etiqueta = " ".join(str(texto or "").strip().split())
    return etiqueta.lower()


def normalizar_etiquetas(data):
    """Limpia, elimina duplicados y conserva orden de etiquetas."""
    etiquetas_normalizadas = []
    vistas = set()
    for item in data or []:
        etiqueta = limpiar_etiqueta(item)
        if (
            etiqueta
            and etiqueta not in vistas
            and len(etiqueta) <= LONGITUD_MAX_ETIQUETA
        ):
            etiquetas_normalizadas.append(etiqueta)
            vistas.add(etiqueta)
    return etiquetas_normalizadas


def normalizar_tarea(tarea, fallback_id):
    """Normaliza una tarea para mantener compatibilidad de datos."""
    texto = str(tarea.get("texto", "")).strip()
    if not texto:
        texto = "Tarea sin texto"

    estado = tarea.get("estado")
    if estado not in ESTADOS_VALIDOS:
        estado = "hecho" if tarea.get("completada") else "pendiente"

    tags = tarea.get("tags", [])
    if not isinstance(tags, list):
        tags = []
    tags = normalizar_etiquetas(tags)

    deadline = tarea.get("deadline")
    if not isinstance(deadline, str):
        deadline = ""
    deadline = deadline.strip()
    if not es_fecha_iso_valida(deadline):
        deadline = ""

    return {
        "id": fallback_id,
        "texto": texto,
        "estado": estado,
        "tags": tags,
        "deadline": deadline,
    }


def es_fecha_iso_valida(valor):
    """Valida fechas en formato YYYY-MM-DD."""
    if not valor:
        return False
    try:
        date.fromisoformat(valor)
        return True
    except ValueError:
        return False


def cargar_tareas():
    """Carga tareas desde JSON y calcula el siguiente id disponible."""
    global tareas, etiquetas, siguiente_id

    if not ARCHIVO_TAREAS.exists():
        tareas = []
        etiquetas = []
        siguiente_id = 1
        return

    try:
        with ARCHIVO_TAREAS.open("r", encoding="utf-8") as archivo:
            data = json.load(archivo)
    except (json.JSONDecodeError, OSError):
        tareas = []
        etiquetas = []
        siguiente_id = 1
        return

    if isinstance(data, list):
        data = {"tareas": data, "etiquetas": []}

    if not isinstance(data, dict):
        tareas = []
        etiquetas = []
        siguiente_id = 1
        return

    data_tareas = data.get("tareas", [])
    data_etiquetas = data.get("etiquetas", [])
    if not isinstance(data_tareas, list):
        data_tareas = []
    if not isinstance(data_etiquetas, list):
        data_etiquetas = []

    tareas_normalizadas = []
    for indice, tarea in enumerate(data_tareas, start=1):
        if isinstance(tarea, dict):
            tareas_normalizadas.append(normalizar_tarea(tarea, indice))

    tareas = tareas_normalizadas
    etiquetas_desde_tareas = []
    for tarea in tareas:
        etiquetas_desde_tareas.extend(tarea.get("tags", []))
    etiquetas = normalizar_etiquetas([*data_etiquetas, *etiquetas_desde_tareas])

    for tarea in tareas:
        tarea["tags"] = [tag for tag in tarea.get("tags", []) if tag in etiquetas]

    renumerar_ids()
    guardar_tareas()


def agregar_tarea(texto):
    """Crea una tarea con id incremental y la agrega a la lista global."""
    global siguiente_id

    texto_limpio = texto.strip()
    if not texto_limpio:
        return None

    tarea = {
        "id": siguiente_id,
        "texto": texto_limpio,
        "estado": "pendiente",
        "tags": [],
        "deadline": "",
    }
    tareas.append(tarea)
    siguiente_id += 1
    guardar_tareas()
    return tarea


def mover_tarea(id_tarea, nuevo_estado):
    """Mueve una tarea a una de las columnas válidas."""
    if nuevo_estado not in ESTADOS_VALIDOS:
        return False

    for tarea in tareas:
        if tarea["id"] == id_tarea:
            tarea["estado"] = nuevo_estado
            guardar_tareas()
            return True
    return False


def reordenar_tablero(orden_columnas):
    """Reordena tareas dentro de columnas y persiste el nuevo orden global."""
    global tareas
    if not isinstance(orden_columnas, dict):
        return False

    ids_vistos = set()
    orden_final = []
    tareas_por_id = {t["id"]: t for t in tareas}

    for estado in ESTADOS_VALIDOS:
        ids_columna = orden_columnas.get(estado, [])
        if not isinstance(ids_columna, list):
            return False

        for id_tarea in ids_columna:
            if not isinstance(id_tarea, int):
                return False
            tarea = tareas_por_id.get(id_tarea)
            if tarea is None or id_tarea in ids_vistos:
                return False

            tarea["estado"] = estado
            orden_final.append(tarea)
            ids_vistos.add(id_tarea)

    for tarea in tareas:
        if tarea["id"] not in ids_vistos:
            orden_final.append(tarea)

    tareas = orden_final
    guardar_tareas()
    return True


def editar_tarea(id_tarea, nuevo_texto):
    """Actualiza el texto de una tarea."""
    texto_limpio = nuevo_texto.strip()
    if not texto_limpio:
        return False

    for tarea in tareas:
        if tarea["id"] == id_tarea:
            tarea["texto"] = texto_limpio
            guardar_tareas()
            return True
    return False


def eliminar_tarea(id_tarea):
    """Elimina una tarea por id."""
    for i, tarea in enumerate(tareas):
        if tarea["id"] == id_tarea:
            tareas.pop(i)
            renumerar_ids()
            guardar_tareas()
            return True
    return False


def crear_etiqueta(texto):
    """Crea una etiqueta reutilizable en el tablero."""
    etiqueta = limpiar_etiqueta(texto)
    if not etiqueta or len(etiqueta) > LONGITUD_MAX_ETIQUETA:
        return False
    if etiqueta in etiquetas:
        return False
    etiquetas.append(etiqueta)
    guardar_tareas()
    return True


def eliminar_etiqueta(nombre):
    """Elimina una etiqueta del tablero y de todas las tareas."""
    etiqueta = limpiar_etiqueta(nombre)
    if etiqueta not in etiquetas:
        return False

    etiquetas.remove(etiqueta)
    for tarea in tareas:
        tarea["tags"] = [tag for tag in tarea.get("tags", []) if tag != etiqueta]
    guardar_tareas()
    return True


def agregar_etiqueta_a_tarea(id_tarea, nombre):
    """Asigna una etiqueta existente a una tarea."""
    etiqueta = limpiar_etiqueta(nombre)
    if etiqueta not in etiquetas:
        return False

    for tarea in tareas:
        if tarea["id"] == id_tarea:
            tags = tarea.setdefault("tags", [])
            if etiqueta in tags:
                return False
            tags.append(etiqueta)
            guardar_tareas()
            return True
    return False


def quitar_etiqueta_de_tarea(id_tarea, nombre):
    """Quita una etiqueta de una tarea."""
    etiqueta = limpiar_etiqueta(nombre)
    for tarea in tareas:
        if tarea["id"] == id_tarea:
            tags = tarea.setdefault("tags", [])
            if etiqueta not in tags:
                return False
            tarea["tags"] = [tag for tag in tags if tag != etiqueta]
            guardar_tareas()
            return True
    return False


def actualizar_deadline_tarea(id_tarea, nueva_fecha):
    """Configura o limpia el deadline de una tarea."""
    fecha_limpia = str(nueva_fecha or "").strip()
    if fecha_limpia and not es_fecha_iso_valida(fecha_limpia):
        return False

    for tarea in tareas:
        if tarea["id"] == id_tarea:
            tarea["deadline"] = fecha_limpia
            guardar_tareas()
            return True
    return False


def tarea_cumple_filtros(tarea, tag_filtro="", desde="", hasta=""):
    """Evalua si una tarea cumple los filtros activos del tablero."""
    if tag_filtro and tag_filtro not in tarea.get("tags", []):
        return False

    deadline = tarea.get("deadline", "")
    if desde:
        if not deadline or deadline < desde:
            return False
    if hasta:
        if not deadline or deadline > hasta:
            return False
    return True


# Cargar datos iniciales
cargar_tareas()
