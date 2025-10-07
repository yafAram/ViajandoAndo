using AutoMapper;
using Microservice.MapsPointsUser.Api.Data;
using Microservice.MapsPointsUser.Api.Extensions;
using Microservice.MapsPointsUser.Api.IService;
using Microservice.MapsPointsUser.Api.Mapping;
using Microservice.MapsPointsUser.Api.Service;
using Microservice.MapsPointsUser.Api.Service.Impl;
using Microservice.MapsPointsUser.Api.Service.IService;
using Microservice.MapsPointsUser.Api.Service.Repository;
using Microsoft.EntityFrameworkCore;
using NetTopologySuite;
using NetTopologySuite.Geometries;

var builder = WebApplication.CreateBuilder(args);

// Configuration
var configuration = builder.Configuration;
var connectionString = configuration.GetConnectionString("DefaultConnection")
    ?? throw new InvalidOperationException("DefaultConnection no configurada en appsettings.json");

// 1) DbContext con NetTopologySuite
builder.Services.AddDbContext<AppDbContext>(options =>
    options.UseSqlServer(connectionString, sqlOptions => sqlOptions.UseNetTopologySuite()));

// 2) GeometryFactory inyectable (SRID 4326)
builder.Services.AddSingleton(provider =>
{
    var nts = NtsGeometryServices.Instance;
    return nts.CreateGeometryFactory(srid: 4326);
});

// 3) AutoMapper (registra Profile)
builder.Services.AddAutoMapper(typeof(MapsProfile));

// 4) IHttpContextAccessor + CurrentUserService
builder.Services.AddHttpContextAccessor();
builder.Services.AddScoped<ICurrentUserService, CurrentUserService>();

// 5) CORS
builder.Services.AddCors(options =>
{
    options.AddPolicy("AllowAll", p => p
        .AllowAnyOrigin()
        .AllowAnyHeader()
        .AllowAnyMethod()
    );
});

// 6) JWT & Swagger (extensiones)
builder.Services.AddJwtAuthentication(configuration);
builder.Services.AddSwaggerConfiguration();

// 7) Controllers + JSON opt
builder.Services.AddControllers()
    .AddJsonOptions(opts =>
    {
        opts.JsonSerializerOptions.ReferenceHandler = System.Text.Json.Serialization.ReferenceHandler.IgnoreCycles;
    });

// 8) HttpClient para proveedores de routing (ORS)
builder.Services.AddHttpClient("ors")
    .SetHandlerLifetime(TimeSpan.FromMinutes(5));

// 9) Repositorios y servicios (registrar aquí tus implementaciones)
builder.Services.AddScoped<IRouteRepository, EfRouteRepository>();
builder.Services.AddScoped<IPoiRepository, EfPoiRepository>();
builder.Services.AddScoped<IRouteService, RouteService>();
builder.Services.AddScoped<IPoiService, PoiService>();
builder.Services.AddScoped<IRoutingProvider, OpenRouteServiceProvider>();


var openRouteKey = configuration["OpenRouteService:ApiKey"];
var openRouteUrl = configuration["OpenRouteService:Url"] ?? "https://api.openrouteservice.org";

builder.Services.AddHttpClient("ors", client =>
{
    client.BaseAddress = new Uri(openRouteUrl);
    // Añadir header sin validación para evitar FormatException
    if (!string.IsNullOrWhiteSpace(openRouteKey))
    {
        client.DefaultRequestHeaders.TryAddWithoutValidation("Authorization", openRouteKey);
    }
})
.SetHandlerLifetime(TimeSpan.FromMinutes(5));

var app = builder.Build();

// Pipeline
if (app.Environment.IsDevelopment())
{
    app.UseSwagger();
    app.UseSwaggerUI(c => c.SwaggerEndpoint("/swagger/v1/swagger.json", "MapsPointsUser API v1"));
}

app.UseHttpsRedirection();

app.UseCors("AllowAll");

// Archivos estáticos (por ejemplo wwwroot/images)
app.UseStaticFiles();

app.UseRouting();

app.UseAuthentication();
app.UseAuthorization();

app.MapControllers();

// Crear carpeta wwwroot/images si no existe
var imagesPath = Path.Combine(app.Environment.WebRootPath ?? "wwwroot", "images");
if (!Directory.Exists(imagesPath))
    Directory.CreateDirectory(imagesPath);

app.Run();
