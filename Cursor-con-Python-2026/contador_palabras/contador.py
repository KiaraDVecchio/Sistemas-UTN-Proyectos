# Programa para contar palabras en un archivo de texto

# 1. Pedir al usuario la ruta de un archivo de texto.
# 2. Leer el contenido del archivo. 
# 3. Separar en palabras
# 4. Contar número total de palabras.
# 5. (Opcional) Mostrar las 10 palabras más frecuentes y su conteo.

archivo = input("Introduce la ruta del archivo de texto: ")
try:
    with open (archivo, "r", encoding="utf-8") as f:
        texto = f.read()
except FileNotFoundError:
    print(f"El archivo {archivo} no existe.")
    exit(1)

# Separar el contenido en palabras
import re
palabras = re.findall(r"\w+", texto.lower())
total_palabras = len(palabras)
print(f"El archivo tiene {total_palabras} palabras.")

# Contar las palabras más frecuentes
from collections import Counter
contador = Counter(palabras)
mas_comunes = contador.most_common(10)
print("\nLas 10 palabras más frecuentes:")
for palabra, conteo in mas_comunes:
    print(f"{palabra}: {conteo}")



