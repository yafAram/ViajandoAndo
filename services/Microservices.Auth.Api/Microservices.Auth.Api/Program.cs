using Microservices.Auth.Api.Data;
using Microservices.Auth.Api.Models;
using Microservices.Auth.Api.Service;
using Microservices.Auth.Api.Service.IService;
using Microsoft.AspNetCore.Identity;
using Microsoft.EntityFrameworkCore;
using Microsoft.OpenApi.Models;

var builder = WebApplication.CreateBuilder(args);

// Configuraci�n de servicios:
builder.Services.AddDbContext<AppDbContext>(options =>
    options.UseSqlServer(builder.Configuration.GetConnectionString("DefaultConnection")));

builder.Services.Configure<JwtOptions>(builder.Configuration.GetSection("ApiSettings:JwtOptions"));

builder.Services.AddIdentity<ApplicationUser, IdentityRole>()
    .AddEntityFrameworkStores<AppDbContext>()
    .AddDefaultTokenProviders();

builder.Services.AddScoped<IAuthService, AuthService>();
builder.Services.AddScoped<IJwtTokenGenerator, JwtTokenGenerator>();

builder.Services.AddControllers();

// Configuraci�n de Swagger / OpenAPI
builder.Services.AddSwaggerGen(c =>
{
    c.SwaggerDoc("v1", new OpenApiInfo { Title = "Microservicio Auth", Version = "v1" });
});

var app = builder.Build();

// ** Aplicar migraciones al inicio **
using (var scope = app.Services.CreateScope())
{
    var services = scope.ServiceProvider;
    try
    {
        var db = services.GetRequiredService<AppDbContext>();
        db.Database.Migrate();  // aplica migraciones pendientes
    }
    catch (Exception ex)
    {
        // Puedes loguear el error, detener la aplicaci�n o continuar dependiendo del caso
        var logger = services.GetRequiredService<ILogger<Program>>();
        logger.LogError(ex, "Error aplicando migraciones a la base de datos");
        // En producci�n, quiz� quieras re-throw o detener la app si la migraci�n falla
        throw;
    }
}

// Configuraci�n del pipeline HTTP
if (app.Environment.IsDevelopment())
{
    app.UseSwagger();
    app.UseSwaggerUI();
}

// En producci�n: podr�as decidir no exponer Swagger, o condicionarlo
if (!app.Environment.IsProduction())
{
    app.UseSwagger();
    app.UseSwaggerUI(c =>
    {
        c.SwaggerEndpoint("/swagger/v1/swagger.json", "API v1");
        c.RoutePrefix = string.Empty;
    });
}

// Redirecci�n a HTTPS (si aplicable y certificado configurado)
app.UseHttpsRedirection();

// Autorizaci�n / autenticaci�n si aplica (no veo UseAuthentication en tu c�digo, asegurate de usarlo si usas Identity)
app.UseAuthentication();
app.UseAuthorization();

app.MapControllers();

app.Run();
