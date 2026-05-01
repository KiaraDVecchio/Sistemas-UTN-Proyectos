
print("Hola, Cursor")

# Este bucle imprime los números del 0 al 4 en la consola
for i in range(5):
    print(i)

# Esta funcion determina si un numero es primo
def es_primo(n):
    if n <= 1:
        return False
    for i in range(2, n):
        if n % i == 0:
            return False
    return True
print(es_primo(7))
    