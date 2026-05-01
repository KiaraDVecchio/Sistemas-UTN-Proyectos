# Programa de calculadora simple

# 1. Solicitar al usuario una operacion (suma, resta, multiplicacion, division) y dos numeros
# 2. Ejecutar la operacion seleccionada y mostrar el resultado. Manejar la division por cero.
# 3. Debe repetirse hasta que el usuario escriba "salir" como operacion.

def pedir_entrada(mensaje):
    """
    Pide un dato al usuario.
    Retorna None si el usuario escribe 'salir'.
    """
    valor = input(mensaje).strip()
    if valor.lower() == "salir":
        return None
    return valor

while True:
    operacion = pedir_entrada(
        "Ingrese la operacion (suma, resta, multiplicacion, division) o 'salir': "
    )

    if operacion is None:
        print("Saliendo de la calculadora...")
        break

    operacion = operacion.lower()
    if operacion not in ("suma", "resta", "multiplicacion", "division"):
        print("Operacion no valida.")
        continue

    texto_numero1 = pedir_entrada("Ingrese el primer numero (o 'salir'): ")
    if texto_numero1 is None:
        print("Saliendo de la calculadora...")
        break

    texto_numero2 = pedir_entrada("Ingrese el segundo numero (o 'salir'): ")
    if texto_numero2 is None:
        print("Saliendo de la calculadora...")
        break

    try:
        numero1 = float(texto_numero1)
        numero2 = float(texto_numero2)
    except ValueError:
        print("Por favor ingrese numeros validos.")
        continue

    if operacion == "division" and numero2 == 0:
        print("Error: No se puede dividir por cero.")
        continue

    if operacion == "suma":
        resultado = numero1 + numero2
    elif operacion == "resta":
        resultado = numero1 - numero2
    elif operacion == "multiplicacion":
        resultado = numero1 * numero2
    else:  
        resultado = numero1 / numero2

    print(f"El resultado de la operacion es: {resultado}")