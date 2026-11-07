# Política de seguridad

## Reporte de vulnerabilidades

Si encuentras una vulnerabilidad en este proyecto, **no abras un issue público**. Escribe a [yaftearam34@gmail.com](mailto:yaftearam34@gmail.com) con una descripción del problema y los pasos para reproducirlo. Responderé lo antes posible.

## Manejo de credenciales

Este repositorio **no debe contener secretos reales**: contraseñas de bases de datos, claves de firma JWT, API keys ni perfiles de publicación.

- Los archivos `appsettings.json` solo incluyen valores de ejemplo entre `<...>`.
- Los valores reales se definen en `appsettings.Development.json` (ignorado por Git) o con variables de entorno. Consulta [docs/CONFIGURACION.md](docs/CONFIGURACION.md).
- Los perfiles de publicación (`*.pubxml`) y los archivos `.env` están en `.gitignore`.

## Versiones soportadas

Este es un proyecto de portafolio en desarrollo activo; solo la última versión publicada en la rama `main` recibe correcciones.
