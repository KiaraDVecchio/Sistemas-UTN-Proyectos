"Escribe un test para la función contar_palabras asegurando que cuenta correctamente las palabras en una cadena dada." 

def contar_palabras(texto):
    return len(texto.split())

def test_contar_palabras():
    assert contar_palabras("Hola este texto tiene seis palabras") == 6
    
def test_cadena_vacia():
    assert contar_palabras("") == 0