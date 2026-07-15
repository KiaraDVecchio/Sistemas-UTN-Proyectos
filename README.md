# 🎓 Portfolio Académico - Ingeniería en Sistemas

¡Hola! En este repositorio comparto mi evolución como estudiante de Ingeniería en Sistemas en la Universidad Tecnológica Nacional (UTN FRBA). Es una bitácora de mi recorrido técnico, donde vas a encontrar los desafíos, arquitecturas y soluciones que fui desarrollando a lo largo de las distintas materias de la carrera.

## 🛠️ Tecnologías y Herramientas

**Backend & Lenguajes**
![Java](https://img.shields.io/badge/Java-ED8B00?style=flat&logo=openjdk&logoColor=white) ![Spring Boot](https://img.shields.io/badge/Spring_Boot-6DB33F?style=flat&logo=spring-boot&logoColor=white) ![Hibernate](https://img.shields.io/badge/Hibernate-59666C?style=flat&logo=Hibernate&logoColor=white) ![JavaScript](https://img.shields.io/badge/JavaScript-F7DF1E?style=flat&logo=javascript&logoColor=black) ![Node.js](https://img.shields.io/badge/Node.js-339933?style=flat&logo=nodedotjs&logoColor=white) ![Express](https://img.shields.io/badge/Express-000000?style=flat&logo=express&logoColor=white) ![Python](https://img.shields.io/badge/Python-3776AB?style=flat&logo=python&logoColor=white) ![C](https://img.shields.io/badge/C-A8B9CC?style=flat&logo=c&logoColor=white)
**Frontend**
![React](https://img.shields.io/badge/React-61DAFB?style=flat&logo=react&logoColor=black) ![Thymeleaf](https://img.shields.io/badge/Thymeleaf-005C00?style=flat&logo=Thymeleaf&logoColor=white) ![HTML5](https://img.shields.io/badge/HTML5-E34F26?style=flat&logo=html5&logoColor=white) ![CSS3](https://img.shields.io/badge/CSS3-1572B6?style=flat&logo=css3&logoColor=white)
**Bases de Datos**
![MySQL](https://img.shields.io/badge/MySQL-4479A1?style=flat&logo=mysql&logoColor=white) ![SQL Server](https://img.shields.io/badge/SQL_Server-CC292B?style=flat&logo=microsoftsqlserver&logoColor=white) ![MongoDB](https://img.shields.io/badge/MongoDB-47A248?style=flat&logo=mongodb&logoColor=white)
**Infraestructura, Metodologías & Herramientas**
![Linux](https://img.shields.io/badge/Linux-FCC624?style=flat&logo=linux&logoColor=black) ![Docker](https://img.shields.io/badge/Docker-2496ED?style=flat&logo=docker&logoColor=white) ![Postman](https://img.shields.io/badge/Postman-FF6C37?style=flat&logo=postman&logoColor=white) ![UML](https://img.shields.io/badge/UML-0073EC?style=flat&logo=unifiedmodelinglanguage&logoColor=white) ![Scrum](https://img.shields.io/badge/Scrum-004C8A?style=flat&logo=scrumalliance&logoColor=white) ![AI Prompting](https://img.shields.io/badge/AI_Prompting-7437F8?style=flat&logo=openai&logoColor=white)

## 🚀 Proyectos Destacados

### [Metamapa – Plataforma de Mapeo Colaborativo](./Diseño%20de%20Sistemas%20-%202025)
Sistema de código abierto diseñado para la recopilación y visibilización geográfica de información sensible (focos de incendio, contaminación, denuncias). El objetivo principal es potenciar la inteligencia colectiva mediante el mapeo colaborativo de "hechos" multimedia, garantizando la veracidad de los datos y el anonimato de los usuarios.

**Desafíos técnicos abordados:**
* **Arquitectura de Microservicios**: Implementación de servicios independientes para la gestión de fuentes de datos (estáticas, dinámicas y proxies), un motor de agregación y un servicio de estadísticas.
* **Seguridad y Privacidad**: Sistema de autenticación mixta (Sesiones + JWT) con un fuerte enfoque en la protección de datos personales y la recepción de denuncias.
* **Stack Tecnológico**: Java (Spring Boot), Maven, Docker, MySQL, JPA/Hibernate, Thymeleaf. 

### [Birbnb – Plataforma de Gestión de Reservas Temporales](./Desarrollo%20de%20Software%20-%202025)
Este proyecto es un sistema integral de reservas de alojamientos (estilo Airbnb) que permite a los usuarios buscar y alquilar propiedades de manera sencilla. La plataforma conecta anfitriones con huéspedes, permitiendo la gestión completa del ciclo de vida de una reserva, desde la búsqueda con filtros avanzados hasta la confirmación y el sistema de notificaciones en tiempo real.

🎥 No necesitás instalar nada para ver cómo funciona. Preparé una demo donde muestro los flujos principales (reserva, filtros, creación de alojamientos, etc.)
  
[![Ver Demo](https://img.shields.io/badge/Demo-Reproducir_Video-red?style=for-the-badge&logo=youtube)](https://www.youtube.com/watch?v=G992bgjGc2s)

**Desafíos técnicos abordados:**
* **Arquitectura API REST**: Diseño e implementación de una API robusta bajo el enfoque REST utilizando Node.js y Express, gestionando flujos complejos de reserva y disponibilidad.
* **Stack Tecnológico**: JavaScript, Node.js, Express, MongoDB, Next.js, React, Axios, Jest, Cypress.

### [Bases de Datos – Sistema de Gestión de Cursos](./Bases%20de%20Datos%20-%202025)
Este proyecto consistió en el rediseño integral y la migración de un sistema de gestión académica para una institución educativa. El desafío principal fue transformar una base de datos masiva desnormalizada ("Tabla Maestra") en un modelo relacional eficiente y, posteriormente, en un modelo de Inteligencia de Negocios para la toma de decisiones estratégicas.

**Desafíos técnicos abordados:**
* **Migración y Limpieza de Datos**: Procesamiento de datos desorganizados e inconsistentes (DNI duplicados, fechas invertidas) mediante scripts de SQL, asegurando la integridad sin modificar la fuente original.
* **Modelo Transaccional (OLTP)**: Diseño y normalización de un esquema para gestionar inscripciones, evaluaciones, finales, facturación y encuestas anónimas.
* **Inteligencia de Negocios (BI/OLAP)**: Implementación de un modelo estelar con dimensiones y tablas de hechos para la obtención de indicadores de gestión y análisis de escenarios.
* **Automatización con SQL**: Creación de Stored Procedures, Triggers y Vistas para automatizar la lógica de negocio y la carga de datos.
* **Stack Tecnológico**: SQL Server 2022, T-SQL.

### [Plug & Pray – Simulador de Sistema Operativo Distribuido](./Sistemas%20Operativos%20(Linux)%20-%202026)
Este trabajo práctico grupal estuvo enfocado en el diseño e implementación de un entorno distribuido que simula la operatoria completa de un sistema operativo real. El sistema cuenta con planificación de procesos en 7 estados, interpretación de instrucciones tipo pseudocódigo mediante una CPU con MMU, y un esquema de gestión de memoria segmentada dinámica con soporte para almacenamiento secundario (SWAP) y dispositivos de memoria física extraíbles de forma concurrente.

**Desafíos técnicos abordados:**

En este proyecto, asumí la responsabilidad principal del desarrollo de los siguientes módulos:
* **Kernel Memory & SWAP**: Gestor de memoria con asignación dinámica (Best/Worst Fit) y algoritmo de compactación. Implementación del esquema de intercambio (SWAP) en disco para la suspensión/reincorporación de procesos.
* **Memory Stick**: Simulación de conexión/desconexión de hardware en caliente, resolviendo concurrencia en lecturas/escrituras de múltiples CPUs y fallos de sistema (BSOD).
* **I/O & Concurrencia**: Módulo de E/S de un solo hilo para STDIN/STDOUT/SLEEP. Sincronización multihilo en C mediante semáforos y mutexes para evitar condiciones de carrera entre procesos.
* **Stack Tecnológico:** C, Pthreads, Sockets TCP/IP, Valgrind, so-commons-library.

### [Curso Python – Santander Open Academy (Cursor)](./Cursor-con-Python-2026)
A diferencia de los proyectos anteriores, este espacio está dedicado a mi formación complementaria fuera de la currícula de la UTN. A través de la beca de **Santander Open Academy**, profundicé en el lenguaje Python y el uso de herramientas de IA (en este caso con Cursor) para el desarrollo ágil.

**Desafíos técnicos abordados:**
* **AI-Assisted Development:** Implementación de flujo de trabajo basado en Agentes (AI Agents) para la generación de código, refactorización y debugging proactivo.
* **Desarrollo Web con Flask:** Construcción de aplicaciones con rutas dinámicas y persistencia en JSON.
* **Lógica y Algoritmia:** Resolución de desafíos técnicos optimizando la eficiencia del código mediante prompts avanzados y revisión de lógica asistida.
* **Stack Tecnológico:** Python 3, Flask, Jinja2, AI Prompting.


