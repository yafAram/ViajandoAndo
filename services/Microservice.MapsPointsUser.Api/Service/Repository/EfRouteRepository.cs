using Microsoft.EntityFrameworkCore;
using Microservice.MapsPointsUser.Api.Data;
using Microservice.MapsPointsUser.Api.IService;
using Microservice.MapsPointsUser.Api.Model;
using Route = Microservice.MapsPointsUser.Api.Model.Route;

namespace Microservice.MapsPointsUser.Api.Service.Repository
{
    public class EfRouteRepository : IRouteRepository
    {
        private readonly AppDbContext _db;
        public EfRouteRepository(AppDbContext db) => _db = db;

        public async Task AddAsync(Route route, CancellationToken ct = default)
        {
            await _db.Routes.AddAsync(route, ct);
        }

        public async Task DeleteAsync(Route route, CancellationToken ct = default)
        {
            _db.Routes.Remove(route);
            await Task.CompletedTask;
        }

        public async Task<Route?> GetByIdAsync(Guid id, CancellationToken ct = default)
        {
            return await _db.Routes
                .Include(r => r.Waypoints)
                .FirstOrDefaultAsync(r => r.RouteId == id, ct);
        }

        public async Task<List<Route>> GetByUserIdAsync(Guid userId, CancellationToken ct = default)
        {
            return await _db.Routes
                .AsNoTracking()
                .Include(r => r.Waypoints)
                .Where(r => r.UserId == userId)
                .OrderByDescending(r => r.CreatedAt)
                .ToListAsync(ct);
        }

        public async Task UpdateAsync(Route route, CancellationToken ct = default)
        {
            _db.Routes.Update(route);
            await Task.CompletedTask;
        }

        public async Task SaveChangesAsync(CancellationToken ct = default)
        {
            await _db.SaveChangesAsync(ct);
        }
    }
}
