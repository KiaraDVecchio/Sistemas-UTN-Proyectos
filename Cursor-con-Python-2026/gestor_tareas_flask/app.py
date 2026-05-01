import json
from datetime import date
from pathlib import Path

"""Entrypoint mínimo: expone la app creada en `routes.py`."""
from routes import app

if __name__ == "__main__":
    app.run(debug=True)