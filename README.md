# Proyecto Turismo - Repo

## Prerrequisitos
- Java 17 (JDK)
- Android Studio
- .NET SDK 8
- SQL Server Express (dev) o acceso al SQL Server que provee Zoomie
- Git, GitHub

## Estructura
- /services/auth-service-dotnet
- /services/lodgings-service-node (opcional)
- /mobile-android/app
- /infra/migrations
- /infra/scripts
- /.github/workflows

## Levantar local (dev)
1. Instalar SQL Server Express y crear base de datos `turismo_db`.
2. Configurar cadena de conexión en `services/auth-service-dotnet/appsettings.Development.json`:
   `Server=localhost;Database=turismo_db;User Id=sa;Password=Your_strong!Passw0rd;`
3. `dotnet run` en el servicio ASP.NET.
4. Abrir `mobile-android` en Android Studio.

## Tests / Builds
- Backend:
  - `dotnet restore`
  - `dotnet build`
  - `dotnet test`
- Android:
  - `./gradlew assembleDebug`
  - `./gradlew testDebugUnitTest`

## Despliegue a Zoomie
- El pipeline de GitHub Actions generará artefacto `publish` y podrá desplegar por SFTP/SSH si introduces los secrets de Zoomie.
- **No añadas credenciales en código**. Usa GitHub Secrets.

## Firebase (FCM)
- Registrar app en Firebase Console y añadir `google-services.json` al proyecto Android.
- Backend debe almacenar tokens en tabla `DeviceTokens` y usar `FCM_SERVER_KEY` para enviar notificaciones.
