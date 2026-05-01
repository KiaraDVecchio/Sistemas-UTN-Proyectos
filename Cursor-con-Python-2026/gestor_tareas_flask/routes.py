from flask import Flask, jsonify, render_template, request, redirect, url_for
import services as svc
from datetime import date

app = Flask(__name__)


@app.route("/")
def index():
    editando_id = request.args.get("editando_id", type=int)
    tag_filtro = svc.limpiar_etiqueta(request.args.get("filtro_tag", ""))
    if tag_filtro not in svc.etiquetas:
        tag_filtro = ""

    deadline_desde = request.args.get("deadline_desde", "").strip()
    deadline_hasta = request.args.get("deadline_hasta", "").strip()
    if deadline_desde and not svc.es_fecha_iso_valida(deadline_desde):
        deadline_desde = ""
    if deadline_hasta and not svc.es_fecha_iso_valida(deadline_hasta):
        deadline_hasta = ""

    tareas_filtradas = [
        tarea
        for tarea in svc.tareas
        if svc.tarea_cumple_filtros(tarea, tag_filtro, deadline_desde, deadline_hasta)
    ]

    columnas = {
        "pendiente": [t for t in tareas_filtradas if t["estado"] == "pendiente"],
        "en_curso": [t for t in tareas_filtradas if t["estado"] == "en_curso"],
        "hecho": [t for t in tareas_filtradas if t["estado"] == "hecho"],
    }
    return render_template(
        "index.html",
        columnas=columnas,
        editando_id=editando_id,
        etiquetas=svc.etiquetas,
        hoy=date.today().isoformat(),
        filtro_tag=tag_filtro,
        deadline_desde=deadline_desde,
        deadline_hasta=deadline_hasta,
    )


@app.route("/agregar", methods=["POST"])
def agregar():
    texto_tarea = request.form.get("texto_tarea", "")
    if texto_tarea:
        svc.agregar_tarea(texto_tarea)
    return redirect(url_for("index"))


@app.route("/mover/<int:id>", methods=["POST"])
def mover(id):
    nuevo_estado = request.form.get("estado", "")
    ok = svc.mover_tarea(id, nuevo_estado)

    if request.headers.get("X-Requested-With") == "XMLHttpRequest":
        status = 200 if ok else 400
        return jsonify({"ok": ok}), status

    return redirect(url_for("index"))


@app.route("/editar/<int:id>", methods=["POST"])
def editar(id):
    nuevo_texto = request.form.get("texto_tarea", "")
    svc.editar_tarea(id, nuevo_texto)
    return redirect(url_for("index"))


@app.route("/reordenar", methods=["POST"])
def reordenar():
    data = request.get_json(silent=True) or {}
    orden_columnas = data.get("columnas", {})
    ok = svc.reordenar_tablero(orden_columnas)
    status = 200 if ok else 400
    return jsonify({"ok": ok}), status


@app.route("/eliminar/<int:id>", methods=["POST"])
def eliminar(id):
    svc.eliminar_tarea(id)
    return redirect(url_for("index"))


@app.route("/tags/agregar", methods=["POST"])
def agregar_tag():
    texto_tag = request.form.get("texto_tag", "")
    svc.crear_etiqueta(texto_tag)
    return redirect(url_for("index"))


@app.route("/tags/eliminar", methods=["POST"])
def eliminar_tag():
    nombre = request.form.get("tag", "")
    svc.eliminar_etiqueta(nombre)
    return redirect(url_for("index"))


@app.route("/tarea/<int:id>/tag/agregar", methods=["POST"])
def agregar_tag_a_tarea(id):
    nombre = request.form.get("tag", "")
    svc.agregar_etiqueta_a_tarea(id, nombre)
    return redirect(url_for("index"))


@app.route("/tarea/<int:id>/tag/eliminar", methods=["POST"])
def eliminar_tag_de_tarea(id):
    nombre = request.form.get("tag", "")
    svc.quitar_etiqueta_de_tarea(id, nombre)
    return redirect(url_for("index"))


@app.route("/tarea/<int:id>/deadline", methods=["POST"])
def actualizar_deadline(id):
    nueva_fecha = request.form.get("deadline", "")
    svc.actualizar_deadline_tarea(id, nueva_fecha)
    return redirect(url_for("index"))


if __name__ == "__main__":
    app.run(debug=True)
