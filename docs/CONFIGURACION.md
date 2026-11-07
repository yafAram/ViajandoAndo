# Configuración local

Los servicios leen su configuración de `appsettings.json` y la sobrescriben con `appsettings.Development.json` o con variables de entorno. Los valores del repositorio son **marcadores de posición**: debes reemplazarlos para que los servicios arranquen.

> `appsettings.*.json` está en `.gitignore`, así que tu archivo local no se sube al repositorio.

## Valores que debes definir

| Servicio | Clave | Descripción |
|----------|-------|-------------|
| Auth y MapsPointsUser | `ConnectionStrings:DefaultConnection` | Cadena de conexión a SQL Server |
| Auth y MapsPointsUser | `ApiSettings:JwtOptions:Secret` | Clave de firma JWT, de al menos 32 caracteres. **Debe ser la misma en ambos servicios** |
| Auth y MapsPointsUser | `ApiSettings:JwtOptions:Issuer` / `Audience` | Emisor y audiencia del token; deben coincidir entre servicios |
| MapsPointsUser | `OpenRouteService:ApiKey` | API key de [OpenRouteService](https://openrouteservice.org/) para calcular rutas |

## Opción 1: archivo local

Crea `appsettings.Development.json` junto a cada `appsettings.json` solo con las claves que quieras cambiar:

```json
{
  "ConnectionStrings": {
    "DefaultConnection": "Server=localhost;Database=ViajandoAndoAuth;User Id=sa;Password=TuPassword;Encrypt=True;TrustServerCertificate=True;MultipleActiveResultSets=True;"
  },
  "ApiSettings": {
    "JwtOptions": {
      "Secret": "una-clave-larga-y-aleatoria-de-al-menos-32-caracteres"
    }
  }
}
```

Para generar una clave aleatoria:

```bash
openssl rand -base64 48
```

## Opción 2: variables de entorno

ASP.NET Core usa `__` (doble guion bajo) como separador de niveles:

```bash
export ConnectionStrings__DefaultConnection="Server=localhost;Database=ViajandoAndoAuth;User Id=sa;Password=TuPassword;TrustServerCertificate=True;"
export ApiSettings__JwtOptions__Secret="una-clave-larga-y-aleatoria-de-al-menos-32-caracteres"
export OpenRouteService__ApiKey="tu-api-key"
```

## Bases de datos

- **Auth**: aplica las migraciones automáticamente al iniciar (`Database.Migrate()` en `Program.cs`).
- **MapsPointsUser**: aplica las migraciones manualmente con la herramienta de EF Core:

```bash
dotnet ef database update --project services/Microservice.MapsPointsUser.Api
```

## Despliegue

Define los mismos valores como variables de entorno o configuración de aplicación en tu proveedor de hosting. No los incluyas en el repositorio ni en los perfiles de publicación.
