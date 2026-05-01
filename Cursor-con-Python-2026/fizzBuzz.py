# Escribir un programa que recorra los numeros del 1 al 50, pero para los multiplos de 3 imprima "Fizz" en lugar del numero y para los multiplos de 5 imprima "Buzz". Para los numeros que son multiplos de ambos, imprima "FizzBuzz".
def fizzBuzz():
    for i in range(1, 51):
        if i % 3 == 0 and i % 5 == 0:
            print("FizzBuzz")
        elif i % 3 == 0:
            print("Fizz")
        elif i % 5 == 0:
            print("Buzz")
        else:
            print(i)
fizzBuzz()
