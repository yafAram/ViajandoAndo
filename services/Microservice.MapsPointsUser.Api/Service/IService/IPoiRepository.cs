using Microservice.MapsPointsUser.Api.Model;
using NetTopologySuite.Geometries;

namespace Microservice.MapsPointsUser.Api.Service.IService
{
    /// <summary>
    /// Repositorio de acceso a datos para POIs.
    /// </summary>
    public interface IPoiRepository
    {
        Task<Poi?> GetByIdAsync(Guid id, CancellationToken ct = default);
        Task<List<Poi>> GetNearbyAsync(Point origin, double radiusMeters, string? category = null, string? name = null, CancellationToken ct = default);
        Task AddAsync(Poi poi, CancellationToken ct = default);
        Task UpdateAsync(Poi poi, CancellationToken ct = default);
        Task DeleteAsync(Poi poi, CancellationToken ct = default);
        Task SaveChangesAsync(CancellationToken ct = default);
    }
}
