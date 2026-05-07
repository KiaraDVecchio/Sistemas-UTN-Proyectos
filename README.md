# 🎓 Portfolio Académico - Ingeniería en Sistemas

¡Hola! En este repositorio comparto mi evolución como estudiante de Ingeniería en Sistemas en la Universidad Tecnológica Nacional (UTN FRBA). Es una bitácora de mi recorrido técnico, donde vas a encontrar los desafíos, arquitecturas y soluciones que fui desarrollando a lo largo de las distintas materias de la carrera.

## 🚀 Proyectos Destacados

### [Metamapa – Plataforma de Mapeo Colaborativo](./Diseño%20de%20Sistemas%20-%202025)
Sistema de código abierto diseñado para la recopilación y visibilización geográfica de información sensible (focos de incendio, contaminación, denuncias). El objetivo principal es potenciar la inteligencia colectiva mediante el mapeo colaborativo de "hechos" multimedia, garantizando la veracidad de los datos y el anonimato de los usuarios.

**Desafíos técnicos abordados:**
* Arquitectura de Microservicios: Implementación de servicios independientes para la gestión de fuentes de datos (estáticas, dinámicas y proxies), un motor de agregación y un servicio de estadísticas.
* Seguridad y Privacidad: Sistema de autenticación mixta (Sesiones + JWT) con un fuerte enfoque en la protección de datos personales y la recepción de denuncias.
* Stack Tecnológico: Java (Spring Boot), Maven, Docker, MySQL, JPA/Hibernate, Thymeleaf. 

### [Birbnb – Plataforma de Gestión de Reservas Temporales](./Desarrollo%20de%20Software%20-%202025)
Este proyecto es un sistema integral de reservas de alojamientos (estilo Airbnb) que permite a los usuarios buscar y alquilar propiedades de manera sencilla. La plataforma conecta anfitriones con huéspedes, permitiendo la gestión completa del ciclo de vida de una reserva, desde la búsqueda con filtros avanzados hasta la confirmación y el sistema de notificaciones en tiempo real.
* Dentro de la carpeta del proyecto se encuentra una demo del mismo.

**Desafíos técnicos abordados:**
* Arquitectura API REST: Diseño e implementación de una API robusta bajo el enfoque REST utilizando Node.js y Express, gestionando flujos complejos de reserva y disponibilidad.
* Stack Tecnológico: JavaScript, Node.js, Express, MongoDB, Next.js, React, Axios, Jest, Cypress.

### [Bases de Datos – Sistema de Gestión de Cursos](./Bases%20de%20Datos%20-%202025)
Este proyecto consistió en el rediseño integral y la migración de un sistema de gestión académica para una institución educativa. El desafío principal fue transformar una base de datos masiva desnormalizada ("Tabla Maestra") en un modelo relacional eficiente y, posteriormente, en un modelo de Inteligencia de Negocios para la toma de decisiones estratégicas.

**Desafíos técnicos abordados:**
* Migración y Limpieza de Datos: Procesamiento de datos desorganizados e inconsistentes (DNI duplicados, fechas invertidas) mediante scripts de SQL, asegurando la integridad sin modificar la fuente original.
* Modelo Transaccional (OLTP): Diseño y normalización de un esquema para gestionar inscripciones, evaluaciones, finales, facturación y encuestas anónimas.
* Inteligencia de Negocios (BI/OLAP): Implementación de un modelo estelar con dimensiones y tablas de hechos para la obtención de indicadores de gestión y análisis de escenarios.
* Automatización con SQL: Creación de Stored Procedures, Triggers y Vistas para automatizar la lógica de negocio y la carga de datos.
* Stack Tecnológico: SQL Server 2022, T-SQL.

### [Curso Python – Santander Open Academy (Cursor)](./Cursor-con-Python-2026)
A diferencia de los proyectos anteriores, este espacio está dedicado a mi formación complementaria fuera de la currícula de la UTN. A través de la beca de **Santander Open Academy**, profundicé en el lenguaje Python y el uso de herramientas de IA (en este caso con Cursor) para el desarrollo ágil.

**Desafíos técnicos abordados:**
* **AI-Assisted Development:** Implementación de flujo de trabajo basado en Agentes (AI Agents) para la generación de código, refactorización y debugging proactivo.
* **Desarrollo Web con Flask:** Construcción de aplicaciones con rutas dinámicas y persistencia en JSON.
* **Lógica y Algoritmia:** Resolución de desafíos técnicos optimizando la eficiencia del código mediante prompts avanzados y revisión de lógica asistida.
* **Stack Tecnológico:** Python 3, Flask, Jinja2, AI Prompting.


