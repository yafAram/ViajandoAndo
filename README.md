# ViajandoAndo

<div align="center">

### Aplicación móvil y ecosistema backend para gestión de viajes, gastos, usuarios, mapas y roles

![Android](https://img.shields.io/badge/Android-Kotlin-3DDC84?style=for-the-badge\&logo=android\&logoColor=white)
![Kotlin](https://img.shields.io/badge/Kotlin-Mobile-7F52FF?style=for-the-badge\&logo=kotlin\&logoColor=white)
![ASP.NET Core](https://img.shields.io/badge/ASP.NET_Core-Backend-512BD4?style=for-the-badge\&logo=dotnet\&logoColor=white)
![SQL Server](https://img.shields.io/badge/SQL_Server-Database-CC2927?style=for-the-badge\&logo=microsoftsqlserver\&logoColor=white)
![JWT](https://img.shields.io/badge/JWT-Authentication-000000?style=for-the-badge\&logo=jsonwebtokens\&logoColor=white)
![GitHub Actions](https://img.shields.io/badge/GitHub_Actions-CI/CD-2088FF?style=for-the-badge\&logo=githubactions\&logoColor=white)

</div>

---

## Descripción general

**ViajandoAndo** es un proyecto de software orientado a la gestión de viajes, gastos, usuarios y puntos de interés mediante una aplicación móvil Android y un conjunto de servicios backend desarrollados con tecnologías modernas.

El sistema está diseñado para manejar distintos roles de usuario, permitir la creación y seguimiento de viajes, registrar gastos, consultar puntos de interés en mapa y consumir servicios backend mediante APIs REST protegidas con autenticación JWT.

Este repositorio integra componentes móviles, servicios backend, configuración de infraestructura y workflows de integración continua.

---

## Objetivo del proyecto

El objetivo de **ViajandoAndo** es centralizar la administración de viajes y operaciones relacionadas, permitiendo que los usuarios puedan interactuar con una plataforma móvil conectada a servicios backend seguros y escalables.

El sistema busca resolver necesidades como:

* Registro e inicio de sesión de usuarios.
* Gestión de roles y permisos.
* Creación y seguimiento de viajes.
* Registro y consulta de gastos.
* Visualización de puntos de interés en mapas.
* Consumo de servicios externos para rutas y ubicaciones.
* Separación de responsabilidades entre frontend móvil, backend e infraestructura.

---

## Roles principales

El sistema contempla distintos perfiles de usuario:

### Consignatario

Usuario encargado de gestionar viajes, registrar transportistas, aprobar gastos y consultar información operativa.

### Transportista

Usuario encargado de consultar viajes asignados, iniciar viajes, registrar gastos, reportar incidencias y solicitar ajustes relacionados con el viaje.

### Administrador

Usuario con permisos para gestionar información general del sistema, usuarios, roles y datos operativos.

---

## Funcionalidades principales

### Autenticación y usuarios

* Registro de usuarios.
* Inicio de sesión.
* Autenticación mediante JWT.
* Validación de roles.
* Control de usuarios activos e inactivos.
* Protección de endpoints mediante autorización.

### Gestión de viajes

* Creación de viajes.
* Consulta de viajes por usuario.
* Seguimiento de viajes activos.
* Finalización de viajes.
* Asociación de viajes con transportistas.

### Gestión de gastos

* Registro de gastos por viaje.
* Consulta de gastos.
* Clasificación por tipo de gasto.
* Aprobación o revisión de gastos.
* Control relacionado con presupuesto disponible.

### Mapas y puntos de interés

* Visualización de puntos de interés.
* Consulta de ubicaciones.
* Integración con mapas.
* Búsqueda de puntos relevantes.
* Consumo de servicios para rutas.

### Infraestructura y calidad

* Organización por módulos.
* Separación entre aplicación móvil y servicios backend.
* Workflows de integración continua.
* Configuración preparada para despliegue y mantenimiento.

---

## Stack tecnológico

## Mobile Android

* Kotlin
* Jetpack Compose
* Material Design 3
* Retrofit
* OkHttp
* Coroutines
* ViewModel
* Hilt
* EncryptedSharedPreferences
* OSMDroid
* Firebase Cloud Messaging

## Backend

* ASP.NET Core
* C#
* Entity Framework Core
* SQL Server
* JWT Authentication
* Role-Based Access Control
* AutoMapper
* NetTopologySuite
* APIs REST

## Infraestructura y herramientas

* Git
* GitHub
* GitHub Actions
* Docker básico
* Swagger / OpenAPI
* Postman
* Visual Studio
* Android Studio

---

## Arquitectura general

El proyecto está organizado bajo una separación clara entre aplicación móvil, servicios backend e infraestructura.

```txt
ViajandoAndo/
│
├── mobile-android/
│   └── Aplicación Android desarrollada con Kotlin y Jetpack Compose
│
├── services/
│   ├── Auth/
│   │   └── Microservicio de autenticación, usuarios, JWT y roles
│   │
│   └── MapsPointsUser/
│       └── Microservicio para puntos de interés, mapas y rutas
│
├── infra/
│   └── Configuración de infraestructura y despliegue
│
└── .github/
    └── Workflows de integración continua
```

---

## Arquitectura de la aplicación móvil

La aplicación Android sigue una estructura por capas para mantener separación de responsabilidades.

```txt
mobile-android/
│
├── data/
│   ├── api/
│   ├── repository/
│   └── remote/
│
├── domain/
│   ├── model/
│   └── repository/
│
├── presentation/
│   ├── login/
│   ├── register/
│   ├── home/
│   ├── map/
│   └── components/
│
├── di/
│   └── NetworkModule
│
└── viewmodel/
```

### Capas principales

**Data:**
Contiene la comunicación con APIs, clientes HTTP, DTOs y repositorios concretos.

**Domain:**
Define modelos, contratos e interfaces que representan la lógica principal del sistema.

**Presentation:**
Incluye pantallas, componentes visuales y lógica de presentación.

**DI:**
Configura la inyección de dependencias para clientes HTTP, repositorios y servicios.

---

## Servicios backend

## Auth Service

Microservicio encargado de la autenticación y gestión de usuarios.

### Responsabilidades

* Registro de usuarios.
* Inicio de sesión.
* Generación de tokens JWT.
* Validación de credenciales.
* Gestión de roles.
* Control de usuarios activos e inactivos.
* Protección de rutas mediante autorización.

### Tecnologías

* ASP.NET Core
* C#
* Entity Framework Core
* SQL Server
* JWT
* Swagger

---

## MapsPointsUser Service

Microservicio encargado de la gestión de puntos de interés, rutas y datos geográficos.

### Responsabilidades

* Registro de puntos de interés.
* Consulta de puntos por usuario.
* Búsqueda de puntos.
* Gestión de coordenadas geográficas.
* Integración con servicios externos de rutas.
* Exposición de endpoints REST para la app móvil.

### Tecnologías

* ASP.NET Core
* C#
* Entity Framework Core
* SQL Server
* NetTopologySuite
* AutoMapper
* Swagger

---

## Endpoints principales

> Nota: los endpoints pueden variar según la configuración final de cada servicio.

### Auth

```http
POST /api/auth/register
POST /api/auth/login
GET  /api/usuario/estatus
GET  /api/usuario/presupuesto
GET  /api/usuario/viajes
```

### Gastos

```http
POST /api/gastos
GET  /api/gastos/{id}
GET  /api/usuario/GetAllGastos
GET  /api/usuario/GetGastosByTipoGasto
```

### Puntos de interés y rutas

```http
GET  /api/poi
POST /api/poi
GET  /api/poi/search
GET  /api/routes
```

---

## Seguridad

El proyecto utiliza autenticación basada en **JWT** y control de acceso por roles.

### Características

* Tokens JWT para sesiones autenticadas.
* Validación de identidad en endpoints protegidos.
* Autorización basada en roles.
* Separación de permisos por tipo de usuario.
* Almacenamiento seguro de sesión en Android mediante EncryptedSharedPreferences.

---

## Integración con mapas

La aplicación utiliza mapas para visualizar puntos de interés y apoyar la gestión de viajes.

### Características

* Visualización de marcadores.
* Consulta de puntos de interés.
* Representación de rutas.
* Uso de coordenadas geográficas.
* Integración con servicios de rutas externos.

---

## Integración continua

El repositorio incluye configuración para automatizar validaciones mediante GitHub Actions.

### Workflows incluidos

* Validación de build Android.
* Validación de servicios backend.
* Revisión básica de compilación antes de integrar cambios.

```txt
.github/
└── workflows/
    ├── android-ci.yml
    └── backend-ci.yml
```

---

## Cómo ejecutar el proyecto

## Requisitos previos

### Para Android

* Android Studio
* JDK compatible con Gradle
* Emulador Android o dispositivo físico
* Conexión a los servicios backend

### Para Backend

* .NET SDK
* SQL Server
* Visual Studio o VS Code
* Postman o Swagger para pruebas
* Docker opcional

---

## Ejecución del backend

1. Clonar el repositorio:

```bash
git clone https://github.com/yafAram/ViajandoAndo.git
```

2. Entrar al directorio del servicio:

```bash
cd ViajandoAndo/services/Auth
```

3. Restaurar dependencias:

```bash
dotnet restore
```

4. Configurar la cadena de conexión en el archivo correspondiente:

```json
{
  "ConnectionStrings": {
    "DefaultConnection": "Server=localhost;Database=ViajandoAndo;Trusted_Connection=True;TrustServerCertificate=True;"
  }
}
```

5. Ejecutar migraciones, si aplica:

```bash
dotnet ef database update
```

6. Ejecutar el servicio:

```bash
dotnet run
```

7. Abrir Swagger en el navegador:

```txt
https://localhost:puerto/swagger
```

---

## Ejecución de la app Android

1. Abrir el proyecto en Android Studio:

```txt
mobile-android/
```

2. Sincronizar Gradle.

3. Configurar las URLs base de los servicios backend en el módulo de red.

4. Ejecutar la aplicación en emulador o dispositivo físico.

---

## Variables y configuración

El proyecto puede requerir configurar:

* URL base del Auth Service.
* URL base del MapsPointsUser Service.
* URL base del servicio de rutas.
* Cadena de conexión SQL Server.
* Clave secreta JWT.
* Configuración de Firebase, si aplica.

Ejemplo recomendado para configuración backend:

```json
{
  "Jwt": {
    "Key": "TU_CLAVE_SECRETA",
    "Issuer": "ViajandoAndo",
    "Audience": "ViajandoAndoUsers"
  }
}
```

> No subas claves reales, cadenas de conexión productivas ni credenciales privadas al repositorio.

---

## Buenas prácticas aplicadas

* Separación por capas.
* Uso de APIs REST.
* Autenticación mediante JWT.
* Control de acceso por roles.
* Uso de DTOs.
* Documentación de endpoints con Swagger.
* Consumo de APIs desde Android con Retrofit.
* Manejo seguro de sesión en móvil.
* Organización modular del proyecto.
* Integración continua con GitHub Actions.

---

## Estado del proyecto

Proyecto en desarrollo / MVP académico-profesional.

Actualmente el proyecto cuenta con:

* Aplicación móvil Android.
* Servicios backend principales.
* Autenticación.
* Gestión de usuarios.
* Módulo de puntos de interés.
* Integración con mapas.
* Workflows iniciales de CI.

---

## Próximas mejoras

* Agregar pruebas unitarias en backend.
* Agregar pruebas instrumentadas en Android.
* Mejorar documentación de endpoints.
* Agregar capturas de pantalla de la app móvil.
* Agregar diagrama visual de arquitectura.
* Mejorar manejo global de errores.
* Preparar despliegue en entorno cloud.
* Agregar monitoreo y logging centralizado.

---

## Capturas de pantalla

> Agregar capturas de la aplicación cuando estén disponibles.

```txt
screenshots/
├── login.png
├── home.png
├── map.png
├── trips.png
└── expenses.png
```

---

## Aprendizajes del proyecto

Durante el desarrollo de ViajandoAndo se fortalecieron habilidades en:

* Desarrollo móvil con Kotlin y Jetpack Compose.
* Consumo de APIs REST desde Android.
* Desarrollo backend con ASP.NET Core.
* Implementación de autenticación con JWT.
* Diseño de servicios separados.
* Integración con mapas y coordenadas.
* Organización de proyectos fullstack/mobile.
* Uso de GitHub Actions para validaciones automáticas.

---

## Autor

**Yafte Aram Mercado Meza**
Junior Backend Developer

* GitHub: [github.com/yafAram](https://github.com/yafAram)
* Email: [yaftearam34@gmail.com](mailto:yaftearam34@gmail.com)

---

## Licencia

Este proyecto fue desarrollado con fines académicos y de práctica profesional.

Si deseas reutilizar alguna parte del código, revisa primero la licencia o contacta al autor.

---

<div align="center">

### ViajandoAndo

Backend, mobile y mapas trabajando juntos para gestionar viajes de forma más organizada.

</div>
