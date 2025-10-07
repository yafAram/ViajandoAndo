using Microsoft.EntityFrameworkCore;
using NetTopologySuite.Geometries;
using Microservice.MapsPointsUser.Api.Model;
using RouteModel = Microservice.MapsPointsUser.Api.Model.Route; // evita ambigüedad

namespace Microservice.MapsPointsUser.Api.Data
{
    public class AppDbContext : DbContext
    {
        public AppDbContext(DbContextOptions<AppDbContext> options) : base(options) { }

        public DbSet<Poi> Pois { get; set; }
        public DbSet<RouteModel> Routes { get; set; }
        public DbSet<Waypoint> Waypoints { get; set; }

        protected override void OnModelCreating(ModelBuilder modelBuilder)
        {
            base.OnModelCreating(modelBuilder);

            // -------- POI ----------
            modelBuilder.Entity<Poi>(b =>
            {
                b.ToTable("Pois");
                b.HasKey(p => p.PoiId);

                b.Property(p => p.Name)
                    .IsRequired()
                    .HasMaxLength(200);

                b.Property(p => p.Category)
                    .HasMaxLength(100);

                b.Property(p => p.Description)
                    .HasMaxLength(2000);

                // Almacenar como geography (lat,long) - NetTopologySuite + SQL Server
                b.Property(p => p.Location)
                    .HasColumnType("geography");

                b.Property(p => p.IsActive)
                    .HasDefaultValue(true);

                b.Property(p => p.CreatedAt)
                    .HasDefaultValueSql("GETUTCDATE()");

                // Índice simple (la creación de índice espacial se puede hacer en migración si prefieres)
                //b.HasIndex(p => p.Location).HasDatabaseName("IX_Pois_Location");
            });

            // -------- ROUTE ----------
            modelBuilder.Entity<RouteModel>(b =>
            {
                b.ToTable("Routes");
                b.HasKey(r => r.RouteId);

                b.Property(r => r.Name).HasMaxLength(200);
                b.Property(r => r.Polyline).HasColumnType("nvarchar(max)");
                b.Property(r => r.TotalDistanceMeters).HasDefaultValue(0);
                b.Property(r => r.TotalDurationSeconds).HasDefaultValue(0);

                b.HasMany(r => r.Waypoints)
                 .WithOne(w => w.Route)
                 .HasForeignKey(w => w.RouteId)
                 .OnDelete(DeleteBehavior.Cascade);
            });

            // -------- WAYPOINT ----------
            modelBuilder.Entity<Waypoint>(b =>
            {
                b.ToTable("Waypoints");
                b.HasKey(w => w.WaypointId);

                b.Property(w => w.Location)
                    .HasColumnType("geography");

                b.Property(w => w.Order).IsRequired();

                b.HasOne(w => w.Poi)
                 .WithMany() // no navegación inversa en POI
                 .HasForeignKey(w => w.PoiId)
                 .OnDelete(DeleteBehavior.SetNull);

                b.HasIndex(w => new { w.RouteId, w.Order }).HasDatabaseName("IX_Waypoints_Route_Order");
            });
        }
    }
}
