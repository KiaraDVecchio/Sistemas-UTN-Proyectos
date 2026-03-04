import React, { createContext, useContext, useEffect, useState } from 'react';
import { registerUser } from './api/register';
import { useToast } from '../Toast';
import { getuserdata } from './api/getuserdata';
import { getCookie } from '../../utils/getCookie';
import { loginUser } from './api/login';
import { logoutUser } from './api/logout';

// 1. Creamos el contexto
const AuthContext = createContext();

// 2. Proveedor que envuelve la app
export function AuthProvider({ children }) {
  // user = null | { nombre, email, tipo }
  const addToast = useToast()
  const [user, setUser] = useState(null);
  const [isAuthenticated, setIsAuthenticated] = useState(typeof getCookie("com.birbnb.auth") === "string")

  useEffect(() => {
    const controller = new AbortController()
    const signal = controller.signal
    const token = getCookie('com.birbnb.auth')
    if (token != null) {
      getuserdata({ signal })
        .then(res => {
          setUser(res.data)
          setIsAuthenticated(true)
        })
        .catch(err => {
          if (err.code !== 'ERR_CANCELED') {
            setUser(null);
            setIsAuthenticated(false);
          }
          console.error(err)
        })
    } else {
      setIsAuthenticated(false)
    }
    return () => {
      controller.abort()
    }
  }, [])

  //sign up es registro de usuario
  const signUp = ({
    name,
    email,
    password,
    confirmPassword,
    isAnfitrion
  },
    callback
  ) => {
    registerUser({
      email, contrasena: password, confirmarContrasena: confirmPassword, nombre: name, tipo: isAnfitrion ? "ANFITRION" : "HUESPED"
    }).then(res => {
      addToast({
        title: "",
        content: "Usuario creado exitosamente",
        durationMs: 3000,
        type: "success"
      })
      callback(true)
      setUser({
        nombre: res.data.nombre,
        email: res.data.email,
        tipo: res.data.tipo,
        id: res.data.id,
      })
      setIsAuthenticated(true)
    }).catch((err) => {
      addToast({
        title: "Error",
        content: "Ocurrió un error al crear un usuario",
        durationMs: 3000,
        type: "danger"
      })
      setIsAuthenticated(false)
      setUser(null)
      callback(false)
    })

  };

  // Función de logout
  const signOut = () => {
    logoutUser().then((res) => {
      if (res.status === 200) {
        setUser(null);
        setIsAuthenticated(false)
        addToast({
          title: "",
          content: "Sesión cerrada exitosamente",
          durationMs: 3000,
          type: "success"
        })
        window.location.replace(window.location.origin)
      }
    })
  };

  const login = ({ email, password }, callback) => {
    loginUser({
      email, contrasena: password
    }).then(res => {
      addToast({
        title: "",
        content: "Inicio de sesión exitoso!",
        durationMs: 3000,
        type: "success"
      })
      if (callback != null) {
        callback(true)
      }
      setIsAuthenticated(true)
      setUser({
        nombre: res.data.nombre,
        email: res.data.email,
        tipo: res.data.tipo,
        id: res.data.id,
      })
    }).catch((err) => {
      addToast({
        title: "Error",
        content: "Ocurrió un error al crear un usuario",
        durationMs: 3000,
        type: "danger"
      })
      setIsAuthenticated(false)
      setUser(null)
      if (callback != null) {
        callback(false)
      }
    })
  };

  return (
    <AuthContext.Provider value={{
      signUp,
      login,
      user,
      isAuthenticated,
      signOut
    }}>
      {children}
    </AuthContext.Provider>
  );
}

// Hook para usar el contexto
export function useAuth() {
  return useContext(AuthContext);
}