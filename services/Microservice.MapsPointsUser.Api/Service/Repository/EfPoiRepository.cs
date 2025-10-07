using Microservice.MapsPointsUser.Api.Data;
using Microservice.MapsPointsUser.Api.IService;
using Microservice.MapsPointsUser.Api.Model;
using Microservice.MapsPointsUser.Api.Service.IService;
using Microsoft.EntityFrameworkCore;
using NetTopologySuite.Geometries;

namespace Microservice.MapsPointsUser.Api.Service
{
    public class EfPoiRepository : IPoiRepository
    {
        private readonly AppDbContext _db;
        public EfPoiRepository(AppDbContext db) => _db = db;

        public async Task AddAsync(Poi poi, CancellationToken ct = default)
        {
            await _db.Pois.AddAsync(poi, ct);
        }

        public async Task DeleteAsync(Poi poi, CancellationToken ct = default)
        {
            _db.Pois.Remove(poi);
            await Task.CompletedTask;
        }

        public async Task<Poi?> GetByIdAsync(Guid id, CancellationToken ct = default)
        {
            return await _db.Pois.AsNoTracking().FirstOrDefaultAsync(p => p.PoiId == id, ct);
        }

        public async Task<List<Poi>> GetNearbyAsync(Point origin, double radiusMeters, string? category = null, string? name = null, CancellationToken ct = default)
        {
            // Usamos STDistance en geography (asumimos SRID 4326 y que Location es geography/point)
            var q = _db.Pois.AsNoTracking().Where(p => p.IsActive);

            if (!string.IsNullOrWhiteSpace(category))
                q = q.Where(p => p.Category == category);

            if (!string.IsNullOrWhiteSpace(name))
                q = q.Where(p => EF.Functions.Like(p.Name, $"%{name}%"));

            // Filtrado espacial: distancia en metros (si Location es geography)
            q = q.Where(p => p.Location.Distance(origin) <= radiusMeters)
                 .OrderBy(p => p.Location.Distance(origin));

            return await q.ToListAsync(ct);
        }

        public async Task UpdateAsync(Poi poi, CancellationToken ct = default)
        {
            _db.Pois.Update(poi);
            await Task.CompletedTask;
        }

        public async Task SaveChangesAsync(CancellationToken ct = default)
        {
            await _db.SaveChangesAsync(ct);
        }
    }
}
